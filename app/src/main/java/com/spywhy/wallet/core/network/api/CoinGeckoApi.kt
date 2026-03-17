package com.spywhy.wallet.core.network.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {

    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = true,
        @Query("price_change_percentage") priceChangePercentage: String = "24h,7d"
    ): List<CoinMarketResponse>

    @GET("coins/{id}")
    suspend fun getCoinDetail(
        @Path("id") coinId: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("sparkline") sparkline: Boolean = true
    ): CoinDetailResponse

    @GET("coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") coinId: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: Int = 30
    ): MarketChartResponse

    @GET("simple/price")
    suspend fun getSimplePrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "usd",
        @Query("include_24hr_change") include24hChange: Boolean = true,
        @Query("include_market_cap") includeMarketCap: Boolean = true,
        @Query("include_24hr_vol") include24hVol: Boolean = true
    ): Map<String, Map<String, Double>>

    @GET("coins/markets")
    suspend fun getTopGainers(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "price_change_percentage_24h_desc",
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<CoinMarketResponse>

    @GET("coins/markets")
    suspend fun getTopLosers(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "price_change_percentage_24h_asc",
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<CoinMarketResponse>
}

@JsonClass(generateAdapter = true)
data class CoinMarketResponse(
    @Json(name = "id") val id: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: String?,
    @Json(name = "current_price") val currentPrice: Double?,
    @Json(name = "market_cap") val marketCap: Double?,
    @Json(name = "market_cap_rank") val marketCapRank: Int?,
    @Json(name = "total_volume") val totalVolume: Double?,
    @Json(name = "price_change_percentage_24h") val priceChangePercentage24h: Double?,
    @Json(name = "price_change_percentage_7d_in_currency") val priceChangePercentage7d: Double?,
    @Json(name = "sparkline_in_7d") val sparklineIn7d: SparklineData?
)

@JsonClass(generateAdapter = true)
data class SparklineData(
    @Json(name = "price") val price: List<Double>?
)

@JsonClass(generateAdapter = true)
data class CoinDetailResponse(
    @Json(name = "id") val id: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name") val name: String,
    @Json(name = "market_data") val marketData: MarketDataResponse?
)

@JsonClass(generateAdapter = true)
data class MarketDataResponse(
    @Json(name = "current_price") val currentPrice: Map<String, Double>?,
    @Json(name = "market_cap") val marketCap: Map<String, Double>?,
    @Json(name = "total_volume") val totalVolume: Map<String, Double>?,
    @Json(name = "price_change_percentage_24h") val priceChangePercentage24h: Double?,
    @Json(name = "price_change_percentage_7d") val priceChangePercentage7d: Double?
)

@JsonClass(generateAdapter = true)
data class MarketChartResponse(
    @Json(name = "prices") val prices: List<List<Double>>,
    @Json(name = "market_caps") val marketCaps: List<List<Double>>,
    @Json(name = "total_volumes") val totalVolumes: List<List<Double>>
)
