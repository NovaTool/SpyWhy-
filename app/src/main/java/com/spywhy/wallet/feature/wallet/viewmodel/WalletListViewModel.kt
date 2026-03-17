package com.spywhy.wallet.feature.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletInfo(
    val id: String,
    val name: String,
    val type: WalletType,
    val totalBalanceUsd: String,
    val isActive: Boolean,
    val accountCount: Int,
    val createdAt: Long
)

enum class WalletType(val displayName: String) {
    HD("HD Wallet"),
    IMPORTED("Imported"),
    WATCH_ONLY("Watch-Only")
}

data class WalletListUiState(
    val wallets: List<WalletInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WalletListViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WalletListUiState())
    val uiState: StateFlow<WalletListUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val wallets = listOf(
                WalletInfo(
                    id = "1",
                    name = "Main Wallet",
                    type = WalletType.HD,
                    totalBalanceUsd = "$0.00",
                    isActive = true,
                    accountCount = 5,
                    createdAt = System.currentTimeMillis()
                )
            )

            _uiState.value = WalletListUiState(
                wallets = wallets,
                isLoading = false
            )
        }
    }

    fun setActiveWallet(walletId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                wallets = _uiState.value.wallets.map { wallet ->
                    wallet.copy(isActive = wallet.id == walletId)
                }
            )
        }
    }

    fun deleteWallet(walletId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                wallets = _uiState.value.wallets.filter { it.id != walletId }
            )
        }
    }

    fun renameWallet(walletId: String, newName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                wallets = _uiState.value.wallets.map { wallet ->
                    if (wallet.id == walletId) wallet.copy(name = newName) else wallet
                }
            )
        }
    }
}
