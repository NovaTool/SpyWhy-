package com.spywhy.wallet.feature.portfolio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spywhy.wallet.domain.repository.MarketRepository
import com.spywhy.wallet.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioState())
    val state: StateFlow<PortfolioState> = _state.asStateFlow()

    init {
        loadPortfolio()
    }

    fun setPeriod(period: String) {
        _state.update { it.copy(selectedPeriod = period) }
        loadPortfolio()
    }

    private fun loadPortfolio() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Load wallet balances and market prices
                // Calculate PNL based on selected period
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class PortfolioState(
    val totalValue: String = "0.00",
    val totalPnl: String = "0.00",
    val totalPnlPercent: Double = 0.0,
    val selectedPeriod: String = "24H",
    val holdings: List<Holding> = emptyList(),
    val isLoading: Boolean = false
)

data class Holding(
    val symbol: String,
    val name: String,
    val amount: String,
    val valueUsd: String,
    val pnlPercent: Double,
    val costBasis: String = "0.00"
)
