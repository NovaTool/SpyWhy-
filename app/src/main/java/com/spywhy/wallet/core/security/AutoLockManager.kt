package com.spywhy.wallet.core.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.autoLockDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auto_lock_prefs"
)

@Singleton
class AutoLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_PAUSE_TIME = longPreferencesKey("pause_time")
        private val KEY_LOCK_TIMEOUT = longPreferencesKey("lock_timeout")
        private const val DEFAULT_TIMEOUT_MILLIS = 60_000L // 1 minute
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore = context.autoLockDataStore

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun recordPauseTime() {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_PAUSE_TIME] = System.currentTimeMillis()
            }
        }
    }

    fun shouldLock(): Boolean {
        return runBlocking {
            val prefs = dataStore.data.first()
            val pauseTime = prefs[KEY_PAUSE_TIME] ?: return@runBlocking true
            val timeout = prefs[KEY_LOCK_TIMEOUT] ?: DEFAULT_TIMEOUT_MILLIS
            val elapsed = System.currentTimeMillis() - pauseTime
            elapsed > timeout
        }
    }

    fun setLockTimeout(millis: Long) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_LOCK_TIMEOUT] = millis
            }
        }
    }

    fun lock() {
        _isLocked.value = true
    }

    fun unlock() {
        _isLocked.value = false
    }

    suspend fun getLockTimeout(): Long {
        return dataStore.data.map { prefs ->
            prefs[KEY_LOCK_TIMEOUT] ?: DEFAULT_TIMEOUT_MILLIS
        }.first()
    }
}
