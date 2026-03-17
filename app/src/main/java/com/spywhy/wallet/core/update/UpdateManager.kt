package com.spywhy.wallet.core.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.spywhy.wallet.core.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String
)

@Singleton
class UpdateManager @Inject constructor(
    private val context: Context,
    private val okHttpClient: OkHttpClient
) {

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _updateState = MutableStateFlow(UpdateState.READY)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var progressJob: Job? = null

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(Constants.UPDATE_VERSION_URL)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Timber.w("Update check failed: HTTP ${response.code}")
                return@withContext null
            }

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)

            val remoteVersionCode = json.getInt("versionCode")
            val currentVersionCode = getCurrentVersionCode()

            Timber.d("Update check: current=$currentVersionCode, remote=$remoteVersionCode")

            if (remoteVersionCode > currentVersionCode) {
                UpdateInfo(
                    versionCode = remoteVersionCode,
                    versionName = json.getString("versionName"),
                    apkUrl = json.getString("apkUrl"),
                    changelog = json.optString("changelog", "")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Update check error")
            null
        }
    }

    fun downloadAndInstall(updateInfo: UpdateInfo) {
        _updateState.value = UpdateState.DOWNLOADING
        _downloadProgress.value = 0f
        _errorMessage.value = null

        val updatesDir = context.getExternalFilesDir("updates")
        // Clean up old APKs
        updatesDir?.listFiles()?.forEach { it.delete() }

        val apkFile = File(updatesDir, "spywhy-${updateInfo.versionName}.apk")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadRequest = DownloadManager.Request(Uri.parse(updateInfo.apkUrl))
            .setTitle("SpyWhy Wallet ${updateInfo.versionName}")
            .setDescription("Téléchargement de la mise à jour...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(Uri.fromFile(apkFile))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)

        try {
            val downloadId = downloadManager.enqueue(downloadRequest)

            // Start progress tracking
            startProgressTracking(downloadManager, downloadId)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        try {
                            ctx.unregisterReceiver(this)
                        } catch (_: Exception) {}
                        progressJob?.cancel()

                        // Check download status
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = downloadManager.query(query)
                        if (cursor != null && cursor.moveToFirst()) {
                            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            cursor.close()
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                _downloadProgress.value = 1f
                                _updateState.value = UpdateState.INSTALLING
                                installApk(apkFile)
                            } else {
                                _updateState.value = UpdateState.ERROR
                                _errorMessage.value = "Le téléchargement a échoué (status: $status)"
                            }
                        } else {
                            cursor?.close()
                            _updateState.value = UpdateState.ERROR
                            _errorMessage.value = "Le téléchargement a échoué"
                        }
                    }
                }
            }

            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )

            Timber.d("Download started for ${updateInfo.versionName}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start download")
            _updateState.value = UpdateState.ERROR
            _errorMessage.value = "Impossible de démarrer le téléchargement: ${e.message}"
        }
    }

    private fun startProgressTracking(downloadManager: DownloadManager, downloadId: Long) {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor: Cursor? = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING) {
                            val bytesDownloaded = cursor.getLong(
                                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            )
                            val bytesTotal = cursor.getLong(
                                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            )
                            if (bytesTotal > 0) {
                                _downloadProgress.value = bytesDownloaded.toFloat() / bytesTotal.toFloat()
                            }
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            cursor.close()
                            _updateState.value = UpdateState.ERROR
                            _errorMessage.value = "Le téléchargement a échoué"
                            break
                        }
                        cursor.close()
                    } else {
                        cursor?.close()
                    }
                } catch (_: Exception) {}
                delay(500)
            }
        }
    }

    private fun installApk(apkFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
            Timber.d("Install intent launched for ${apkFile.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to launch install intent")
            _updateState.value = UpdateState.ERROR
            _errorMessage.value = "Impossible de lancer l'installation: ${e.message}"
        }
    }

    fun resetState() {
        progressJob?.cancel()
        _updateState.value = UpdateState.READY
        _downloadProgress.value = 0f
        _errorMessage.value = null
    }

    private fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get version code")
            0
        }
    }
}
