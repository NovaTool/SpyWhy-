package com.spywhy.wallet.domain.repository

import com.spywhy.wallet.domain.model.PriceData
import com.spywhy.wallet.domain.model.SwapQuote
import kotlinx.coroutines.flow.Flow

interface MarketRepository {
    fun getPrices(): Flow<List<PriceData>>
    fun getPriceForCoin(coinId: String): Flow<PriceData?>
    suspend fun refreshPrices()
    suspend fun getMarketChart(coinId: String, days: Int): List<Pair<Long, Double>>
    suspend fun getTopGainers(limit: Int = 20): List<PriceData>
    suspend fun getTopLosers(limit: Int = 20): List<PriceData>

    // Price Alerts
    suspend fun createPriceAlert(coinId: String, targetPrice: Double, above: Boolean)
    suspend fun deletePriceAlert(alertId: Long)
    fun getActivePriceAlerts(): Flow<List<PriceAlert>>

    // Swap
    suspend fun getSwapQuotes(fromCoin: String, toCoin: String, amount: String): List<SwapQuote>
    suspend fun executeDEXSwap(fromToken: String, toToken: String, amount: String, fromAddress: String): String
}

data class PriceAlert(
    val id: Long,
    val coinId: String,
    val targetPrice: Double,
    val isAbove: Boolean,
    val isActive: Boolean
)
