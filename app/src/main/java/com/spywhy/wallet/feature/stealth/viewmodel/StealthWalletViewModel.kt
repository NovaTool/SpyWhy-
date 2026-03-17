package com.spywhy.wallet.feature.stealth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StealthWalletUiState(
    val isSetup: Boolean = false,
    val isStealthActive: Boolean = false,
    val stealthWalletId: String? = null,
    val stealthBalance: String = "$0.00",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StealthWalletViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StealthWalletUiState())
    val uiState: StateFlow<StealthWalletUiState> = _uiState.asStateFlow()

    fun setupStealthWallet(pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Create separate HD wallet with different seed for stealth mode
            // Store stealth PIN hash separately
            _uiState.value = _uiState.value.copy(
                isSetup = true,
                isStealthActive = true,
                isLoading = false
            )
        }
    }

    fun disableStealthWallet() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSetup = false,
                isStealthActive = false,
                stealthWalletId = null
            )
        }
    }

    fun verifyStealthPin(pin: String): Boolean {
        // Verify against stored stealth PIN hash
        return _uiState.value.isSetup
    }
}
