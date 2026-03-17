package com.spywhy.wallet.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.spywhy.wallet.core.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PanicMode @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @Volatile
    var isActivated: Boolean = false
        private set

    suspend fun activatePanic(database: AppDatabase) {
        isActivated = true

        // Clear all database tables
        database.clearAllTables()

        // Clear encrypted shared preferences
        clearEncryptedPrefs("spywhy_pin_prefs")
        clearEncryptedPrefs("spywhy_secure_prefs")

        // Overwrite shared prefs files with random data, then delete
        overwritePrefsFiles()

        // Delete the database files
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
        context.deleteDatabase("${AppDatabase.DATABASE_NAME}-shm")
        context.deleteDatabase("${AppDatabase.DATABASE_NAME}-wal")
        context.deleteDatabase("${AppDatabase.DATABASE_NAME}-journal")

        // Clear all app shared preferences
        val prefsDir = context.filesDir.parentFile?.resolve("shared_prefs")
        prefsDir?.listFiles()?.forEach { file ->
            overwriteFileWithRandomData(file)
            file.delete()
        }

        // Clear internal files directory
        context.filesDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                overwriteFileWithRandomData(file)
                file.delete()
            }
        }

        // Clear DataStore files
        val dataStoreDir = context.filesDir.resolve("datastore")
        dataStoreDir.listFiles()?.forEach { file ->
            overwriteFileWithRandomData(file)
            file.delete()
        }
    }

    private fun clearEncryptedPrefs(name: String) {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs = EncryptedSharedPreferences.create(
                name,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.edit().clear().commit()
        } catch (_: Exception) {
            // Prefs may not exist; that's fine
        }
    }

    private fun overwritePrefsFiles() {
        val prefsDir = context.filesDir.parentFile?.resolve("shared_prefs") ?: return
        prefsDir.listFiles()?.forEach { file ->
            overwriteFileWithRandomData(file)
        }
    }

    private fun overwriteFileWithRandomData(file: java.io.File) {
        try {
            if (!file.isFile) return
            val length = file.length().toInt()
            if (length <= 0) return
            val random = SecureRandom()
            val randomData = ByteArray(length)

            // Overwrite multiple passes
            repeat(3) {
                random.nextBytes(randomData)
                file.writeBytes(randomData)
            }

            // Final pass with zeros
            randomData.fill(0)
            file.writeBytes(randomData)
        } catch (_: Exception) {
            // Best effort
        }
    }
}
