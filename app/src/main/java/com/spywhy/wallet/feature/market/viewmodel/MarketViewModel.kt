package com.spywhy.wallet.feature.market.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spywhy.wallet.domain.model.PriceData
import com.spywhy.wallet.domain.repository.MarketRepository
import com.spywhy.wallet.domain.repository.PriceAlert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MarketTab(val label: String) {
    ALL("All"),
    FAVORITES("Favorites"),
    TOP_GAINERS("Gainers"),
    TOP_LOSERS("Losers")
}

enum class ChartTimeRange(val label: String, val days: Int) {
    DAY_1("24H", 1),
    DAY_7("7D", 7),
    DAY_30("30D", 30),
    YEAR_1("1Y", 365),
    ALL("ALL", 1825)
}

data class MarketUiState(
    val coinList: List<PriceData> = emptyList(),
    val filteredCoinList: List<PriceData> = emptyList(),
    val topGainers: List<PriceData> = emptyList(),
    val topLosers: List<PriceData> = emptyList(),
    val favorites: List<PriceData> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedTab: MarketTab = MarketTab.ALL,
    val selectedCoinPrice: PriceData? = null,
    val chartData: List<Pair<Long, Double>> = emptyList(),
    val selectedTimeRange: ChartTimeRange = ChartTimeRange.DAY_1,
    val priceAlerts: List<PriceAlert> = emptyList()
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        loadPrices()
        loadTopMovers()
        loadAlerts()
        refresh()
    }

    private fun loadPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            marketRepository.getPrices().collect { prices ->
                _uiState.update { state ->
                    state.copy(
                        coinList = prices,
                        filteredCoinList = filterCoins(prices, state.searchQuery),
                        favorites = prices.filter { state.favoriteIds.contains(it.coinId) },
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadTopMovers() {
        viewModelScope.launch {
            try {
                val gainers = marketRepository.getTopGainers()
                val losers = marketRepository.getTopLosers()
                _uiState.update {
                    it.copy(topGainers = gainers, topLosers = losers)
                }
            } catch (_: Exception) {
                // Silently handle top movers load failure
            }
        }
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            marketRepository.getActivePriceAlerts().collect { alerts ->
                _uiState.update { it.copy(priceAlerts = alerts) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCoinList = filterCoins(state.coinList, query)
            )
        }
    }

    fun selectTab(tab: MarketTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFavorite(coinId: String) {
        _uiState.update { state ->
            val newFavorites = state.favoriteIds.toMutableSet()
            if (newFavorites.contains(coinId)) {
                newFavorites.remove(coinId)
            } else {
                newFavorites.add(coinId)
            }
            state.copy(
                favoriteIds = newFavorites,
                favorites = state.coinList.filter { newFavorites.contains(it.coinId) }
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                marketRepository.refreshPrices()
                loadTopMovers()
            } catch (_: Exception) {
                // Handle refresh failure
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loadCoinDetail(coinId: String) {
        viewModelScope.launch {
            marketRepository.getPriceForCoin(coinId).collect { priceData ->
                _uiState.update { it.copy(selectedCoinPrice = priceData) }
            }
        }
        loadChart(coinId, _uiState.value.selectedTimeRange)
    }

    fun selectTimeRange(range: ChartTimeRange, coinId: String) {
        _uiState.update { it.copy(selectedTimeRange = range) }
        loadChart(coinId, range)
    }

    private fun loadChart(coinId: String, range: ChartTimeRange) {
        viewModelScope.launch {
            try {
                val data = marketRepository.getMarketChart(coinId, range.days)
                _uiState.update { it.copy(chartData = data) }
            } catch (_: Exception) {
                _uiState.update { it.copy(chartData = emptyList()) }
            }
        }
    }

    fun createAlert(coinId: String, targetPrice: Double, isAbove: Boolean) {
        viewModelScope.launch {
            try {
                marketRepository.createPriceAlert(coinId, targetPrice, isAbove)
            } catch (_: Exception) {
                // Handle alert creation failure
            }
        }
    }

    fun deleteAlert(alertId: Long) {
        viewModelScope.launch {
            try {
                marketRepository.deletePriceAlert(alertId)
            } catch (_: Exception) {
                // Handle alert deletion failure
            }
        }
    }

    fun toggleAlert(alertId: Long, isActive: Boolean) {
        // In production, update the alert's active state in the repository
        _uiState.update { state ->
            state.copy(
                priceAlerts = state.priceAlerts.map {
                    if (it.id == alertId) it.copy(isActive = isActive) else it
                }
            )
        }
    }

    private fun filterCoins(coins: List<PriceData>, query: String): List<PriceData> {
        if (query.isBlank()) return coins
        val lowerQuery = query.lowercase()
        return coins.filter { coin ->
            coin.coinId.lowercase().contains(lowerQuery)
        }
    }
}
