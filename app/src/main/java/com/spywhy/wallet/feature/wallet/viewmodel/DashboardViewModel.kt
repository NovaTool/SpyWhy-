package com.spywhy.wallet.feature.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssetBalance(
    val ticker: String,
    val name: String,
    val balance: String,
    val balanceUsd: String,
    val change24h: Double,
    val price: Double
)

data class DashboardUiState(
    val totalBalanceUsd: String = "$0.00",
    val totalChange24h: Double = 0.0,
    val assets: List<AssetBalance> = emptyList(),
    val isLoading: Boolean = false,
    val walletName: String = "Main Wallet",
    val isHardwareMode: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val defaultAssets = listOf(
                AssetBalance("BTC", "Bitcoin", "0 BTC", "$0.00", 0.0, 0.0),
                AssetBalance("ETH", "Ethereum", "0 ETH", "$0.00", 0.0, 0.0),
                AssetBalance("SOL", "Solana", "0 SOL", "$0.00", 0.0, 0.0),
                AssetBalance("LTC", "Litecoin", "0 LTC", "$0.00", 0.0, 0.0),
                AssetBalance("XMR", "Monero", "0 XMR", "$0.00", 0.0, 0.0)
            )

            _uiState.value = DashboardUiState(
                totalBalanceUsd = "$0.00",
                totalChange24h = 0.0,
                assets = defaultAssets,
                isLoading = false
            )
        }
    }

    fun refreshPrices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Fetch from CoinGecko API and update balances
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun setActiveWallet(walletId: String) {
        viewModelScope.launch {
            // Switch active wallet and reload balances
            loadDashboard()
        }
    }
}
