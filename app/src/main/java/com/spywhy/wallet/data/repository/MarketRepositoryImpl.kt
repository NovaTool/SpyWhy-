package com.spywhy.wallet.data.repository

import com.spywhy.wallet.core.database.dao.PriceAlertDao
import com.spywhy.wallet.core.database.dao.PriceCacheDao
import com.spywhy.wallet.core.database.entity.PriceAlertEntity
import com.spywhy.wallet.core.database.entity.PriceCacheEntity
import com.spywhy.wallet.core.network.api.CoinGeckoApi
import com.spywhy.wallet.core.network.api.CoinMarketResponse
import com.spywhy.wallet.domain.model.PriceData
import com.spywhy.wallet.domain.model.SwapQuote
import com.spywhy.wallet.domain.repository.MarketRepository
import com.spywhy.wallet.domain.repository.PriceAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepositoryImpl @Inject constructor(
    private val coinGeckoApi: CoinGeckoApi,
    private val priceCacheDao: PriceCacheDao,
    private val priceAlertDao: PriceAlertDao
) : MarketRepository {

    override fun getPrices(): Flow<List<PriceData>> {
        return priceCacheDao.getAll().map { entities ->
            entities.map { it.toPriceData() }
        }
    }

    override fun getPriceForCoin(coinId: String): Flow<PriceData?> {
        return priceCacheDao.getAll().map { entities ->
            entities.find { it.coinId == coinId }?.toPriceData()
        }
    }

    override suspend fun refreshPrices() {
        try {
            val markets = coinGeckoApi.getMarkets()
            val entities = markets.map { it.toCacheEntity() }
            priceCacheDao.insertOrUpdateAll(entities)
            Timber.d("Refreshed ${entities.size} coin prices from CoinGecko")
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh prices from CoinGecko")
            throw e
        }
    }

    override suspend fun getMarketChart(coinId: String, days: Int): List<Pair<Long, Double>> {
        return try {
            val response = coinGeckoApi.getMarketChart(coinId = coinId, days = days)
            response.prices.map { point ->
                Pair(point[0].toLong(), point[1])
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch chart data for $coinId")
            emptyList()
        }
    }

    override suspend fun getTopGainers(limit: Int): List<PriceData> {
        return try {
            coinGeckoApi.getTopGainers(perPage = limit).map { it.toPriceData() }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch top gainers")
            emptyList()
        }
    }

    override suspend fun getTopLosers(limit: Int): List<PriceData> {
        return try {
            coinGeckoApi.getTopLosers(perPage = limit).map { it.toPriceData() }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch top losers")
            emptyList()
        }
    }

    override suspend fun createPriceAlert(coinId: String, targetPrice: Double, above: Boolean) {
        priceAlertDao.insert(
            PriceAlertEntity(
                coinId = coinId,
                targetPrice = targetPrice,
                isAbove = above
            )
        )
    }

    override suspend fun deletePriceAlert(alertId: Long) {
        priceAlertDao.delete(
            PriceAlertEntity(
                id = alertId,
                coinId = "",
                targetPrice = 0.0,
                isAbove = true
            )
        )
    }

    override fun getActivePriceAlerts(): Flow<List<PriceAlert>> {
        return priceAlertDao.getActive().map { entities ->
            entities.map {
                PriceAlert(
                    id = it.id,
                    coinId = it.coinId,
                    targetPrice = it.targetPrice,
                    isAbove = it.isAbove,
                    isActive = it.isActive
                )
            }
        }
    }

    override suspend fun getSwapQuotes(
        fromCoin: String,
        toCoin: String,
        amount: String
    ): List<SwapQuote> = emptyList()

    override suspend fun executeDEXSwap(
        fromToken: String,
        toToken: String,
        amount: String,
        fromAddress: String
    ): String = ""

    // ── Mappers ─────────────────────────────────────────────────────────

    private fun CoinMarketResponse.toPriceData(): PriceData = PriceData(
        coinId = id,
        priceUsd = currentPrice ?: 0.0,
        marketCap = marketCap ?: 0.0,
        volume24h = totalVolume ?: 0.0,
        change24h = priceChangePercentage24h ?: 0.0,
        change7d = priceChangePercentage7d ?: 0.0,
        sparkline = sparklineIn7d?.price ?: emptyList(),
        updatedAt = System.currentTimeMillis()
    )

    private fun CoinMarketResponse.toCacheEntity(): PriceCacheEntity = PriceCacheEntity(
        coinId = id,
        priceUsd = currentPrice ?: 0.0,
        marketCap = marketCap ?: 0.0,
        volume24h = totalVolume ?: 0.0,
        change24h = priceChangePercentage24h ?: 0.0,
        change7d = priceChangePercentage7d ?: 0.0,
        sparkline = sparklineIn7d?.price?.joinToString(",") ?: ""
    )

    private fun PriceCacheEntity.toPriceData(): PriceData = PriceData(
        coinId = coinId,
        priceUsd = priceUsd,
        marketCap = marketCap,
        volume24h = volume24h,
        change24h = change24h,
        change7d = change7d,
        sparkline = if (sparkline.isBlank()) emptyList()
        else sparkline.split(",").mapNotNull { it.toDoubleOrNull() },
        updatedAt = updatedAt
    )
}
