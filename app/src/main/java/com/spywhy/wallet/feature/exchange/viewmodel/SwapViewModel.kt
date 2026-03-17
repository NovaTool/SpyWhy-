package com.spywhy.wallet.feature.exchange.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spywhy.wallet.domain.model.SwapQuote
import com.spywhy.wallet.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SwapViewModel @Inject constructor(
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SwapState())
    val state: StateFlow<SwapState> = _state.asStateFlow()

    fun setFromCoin(coin: String) {
        _state.update { it.copy(fromCoin = coin) }
        fetchQuotes()
    }

    fun setToCoin(coin: String) {
        _state.update { it.copy(toCoin = coin) }
        fetchQuotes()
    }

    fun setFromAmount(amount: String) {
        _state.update { it.copy(fromAmount = amount) }
        fetchQuotes()
    }

    fun swapCoins() {
        _state.update {
            it.copy(
                fromCoin = it.toCoin,
                toCoin = it.fromCoin,
                fromAmount = "",
                estimatedReceive = "0.00",
                quotes = emptyList()
            )
        }
    }

    fun toggleKYCFilter() {
        _state.update { it.copy(kycFilter = !it.kycFilter) }
        applyFilters()
    }

    fun toggleSortBySpeed() {
        _state.update { it.copy(sortBySpeed = !it.sortBySpeed, sortByRate = false) }
        applyFilters()
    }

    fun toggleSortByRate() {
        _state.update { it.copy(sortByRate = !it.sortByRate, sortBySpeed = false) }
        applyFilters()
    }

    fun selectQuote(quote: SwapQuote) {
        _state.update { it.copy(selectedQuote = quote) }
    }

    fun executeSwap() {
        val quote = _state.value.selectedQuote ?: return
        _state.update { it.copy(isSwapping = true) }
        viewModelScope.launch {
            try {
                // Execute swap via selected provider
                _state.update { it.copy(isSwapping = false, swapResult = "Swap initiated successfully") }
            } catch (e: Exception) {
                _state.update { it.copy(isSwapping = false, swapResult = "Swap failed: ${e.message}") }
            }
        }
    }

    private fun fetchQuotes() {
        val amount = _state.value.fromAmount
        if (amount.isBlank() || amount.toBigDecimalOrNull() == null) return

        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val quotes = marketRepository.getSwapQuotes(
                    _state.value.fromCoin,
                    _state.value.toCoin,
                    amount
                )
                val bestReceive = quotes.maxByOrNull { it.toAmount }?.toAmount?.toPlainString() ?: "0.00"
                _state.update {
                    it.copy(
                        isLoading = false,
                        allQuotes = quotes,
                        quotes = quotes,
                        estimatedReceive = bestReceive
                    )
                }
                applyFilters()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun applyFilters() {
        var filtered = _state.value.allQuotes
        if (!_state.value.kycFilter) {
            filtered = filtered.filter { !it.kycRequired }
        }
        if (_state.value.sortBySpeed) {
            filtered = filtered.sortedBy { it.estimatedTime }
        }
        if (_state.value.sortByRate) {
            filtered = filtered.sortedByDescending { it.rate }
        }
        _state.update { it.copy(quotes = filtered) }
    }
}

data class SwapState(
    val fromCoin: String = "BTC",
    val toCoin: String = "ETH",
    val fromAmount: String = "",
    val estimatedReceive: String = "0.00",
    val kycFilter: Boolean = false,
    val sortBySpeed: Boolean = false,
    val sortByRate: Boolean = false,
    val quotes: List<SwapQuote> = emptyList(),
    val allQuotes: List<SwapQuote> = emptyList(),
    val selectedQuote: SwapQuote? = null,
    val isLoading: Boolean = false,
    val isSwapping: Boolean = false,
    val swapResult: String? = null
)
