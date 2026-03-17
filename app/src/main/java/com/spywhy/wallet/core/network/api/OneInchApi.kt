package com.spywhy.wallet.core.network.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OneInchApi {

    @GET("{chainId}/quote")
    suspend fun getQuote(
        @Path("chainId") chainId: Int = 1,
        @Query("src") src: String,
        @Query("dst") dst: String,
        @Query("amount") amount: String
    ): OneInchQuoteResponse

    @GET("{chainId}/swap")
    suspend fun getSwap(
        @Path("chainId") chainId: Int = 1,
        @Query("src") src: String,
        @Query("dst") dst: String,
        @Query("amount") amount: String,
        @Query("from") from: String,
        @Query("slippage") slippage: Int = 1
    ): OneInchSwapResponse

    @GET("{chainId}/tokens")
    suspend fun getTokens(
        @Path("chainId") chainId: Int = 1
    ): OneInchTokensResponse
}

@JsonClass(generateAdapter = true)
data class OneInchQuoteResponse(
    @Json(name = "toAmount") val toAmount: String?,
    @Json(name = "fromToken") val fromToken: OneInchTokenInfo?,
    @Json(name = "toToken") val toToken: OneInchTokenInfo?,
    @Json(name = "estimatedGas") val estimatedGas: Long?
)

@JsonClass(generateAdapter = true)
data class OneInchSwapResponse(
    @Json(name = "toAmount") val toAmount: String?,
    @Json(name = "tx") val tx: OneInchTxData?
)

@JsonClass(generateAdapter = true)
data class OneInchTxData(
    @Json(name = "from") val from: String?,
    @Json(name = "to") val to: String?,
    @Json(name = "data") val data: String?,
    @Json(name = "value") val value: String?,
    @Json(name = "gas") val gas: Long?,
    @Json(name = "gasPrice") val gasPrice: String?
)

@JsonClass(generateAdapter = true)
data class OneInchTokenInfo(
    @Json(name = "symbol") val symbol: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "address") val address: String?,
    @Json(name = "decimals") val decimals: Int?,
    @Json(name = "logoURI") val logoUri: String?
)

@JsonClass(generateAdapter = true)
data class OneInchTokensResponse(
    @Json(name = "tokens") val tokens: Map<String, OneInchTokenInfo>?
)
