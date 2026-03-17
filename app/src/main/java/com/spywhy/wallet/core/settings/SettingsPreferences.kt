package com.spywhy.wallet.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // Security
    val biometricEnabled: Flow<Boolean> = dataStore.data.map { it[BIOMETRIC_ENABLED] ?: true }
    val screenshotProtection: Flow<Boolean> = dataStore.data.map { it[SCREENSHOT_PROTECTION] ?: true }
    val rootDetection: Flow<Boolean> = dataStore.data.map { it[ROOT_DETECTION] ?: true }
    val autoLockTimeout: Flow<String> = dataStore.data.map { it[AUTO_LOCK_TIMEOUT] ?: "60" }
    val maxPinAttempts: Flow<String> = dataStore.data.map { it[MAX_PIN_ATTEMPTS] ?: "10" }
    val clipboardClearDelay: Flow<String> = dataStore.data.map { it[CLIPBOARD_CLEAR_DELAY] ?: "30" }

    // Nodes
    val btcNode: Flow<String> = dataStore.data.map { it[BTC_NODE] ?: "" }
    val ethNode: Flow<String> = dataStore.data.map { it[ETH_NODE] ?: "" }
    val solNode: Flow<String> = dataStore.data.map { it[SOL_NODE] ?: "" }
    val ltcNode: Flow<String> = dataStore.data.map { it[LTC_NODE] ?: "" }
    val xmrNode: Flow<String> = dataStore.data.map { it[XMR_NODE] ?: "node.moneroworld.com:18089" }

    // Currency
    val defaultCurrency: Flow<String> = dataStore.data.map { it[DEFAULT_CURRENCY] ?: "USD" }

    // Notifications
    val priceAlerts: Flow<Boolean> = dataStore.data.map { it[PRICE_ALERTS] ?: true }
    val txNotifications: Flow<Boolean> = dataStore.data.map { it[TX_NOTIFICATIONS] ?: true }
    val dailySummary: Flow<Boolean> = dataStore.data.map { it[DAILY_SUMMARY] ?: false }
    val largeTransactions: Flow<Boolean> = dataStore.data.map { it[LARGE_TRANSACTIONS] ?: true }
    val largeThreshold: Flow<String> = dataStore.data.map { it[LARGE_THRESHOLD] ?: "1000" }

    // Widgets
    val portfolioWidget: Flow<Boolean> = dataStore.data.map { it[PORTFOLIO_WIDGET] ?: false }
    val priceTickerWidget: Flow<Boolean> = dataStore.data.map { it[PRICE_TICKER_WIDGET] ?: false }
    val quickSendWidget: Flow<Boolean> = dataStore.data.map { it[QUICK_SEND_WIDGET] ?: false }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    companion object {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SCREENSHOT_PROTECTION = booleanPreferencesKey("screenshot_protection")
        val ROOT_DETECTION = booleanPreferencesKey("root_detection")
        val AUTO_LOCK_TIMEOUT = stringPreferencesKey("auto_lock_timeout")
        val MAX_PIN_ATTEMPTS = stringPreferencesKey("max_pin_attempts")
        val CLIPBOARD_CLEAR_DELAY = stringPreferencesKey("clipboard_clear_delay")

        val BTC_NODE = stringPreferencesKey("btc_node")
        val ETH_NODE = stringPreferencesKey("eth_node")
        val SOL_NODE = stringPreferencesKey("sol_node")
        val LTC_NODE = stringPreferencesKey("ltc_node")
        val XMR_NODE = stringPreferencesKey("xmr_node")

        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")

        val PRICE_ALERTS = booleanPreferencesKey("price_alerts")
        val TX_NOTIFICATIONS = booleanPreferencesKey("tx_notifications")
        val DAILY_SUMMARY = booleanPreferencesKey("daily_summary")
        val LARGE_TRANSACTIONS = booleanPreferencesKey("large_transactions")
        val LARGE_THRESHOLD = stringPreferencesKey("large_threshold")

        val PORTFOLIO_WIDGET = booleanPreferencesKey("portfolio_widget")
        val PRICE_TICKER_WIDGET = booleanPreferencesKey("price_ticker_widget")
        val QUICK_SEND_WIDGET = booleanPreferencesKey("quick_send_widget")
    }
}
