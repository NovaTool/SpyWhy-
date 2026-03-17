package com.spywhy.wallet.core.network.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query

interface SimpleSwapApi {

    @GET("get_estimated")
    suspend fun getEstimated(
        @Query("api_key") apiKey: String,
        @Query("fixed") fixed: Boolean = false,
        @Query("currency_from") currencyFrom: String,
        @Query("currency_to") currencyTo: String,
        @Query("amount") amount: String
    ): String

    @GET("get_ranges")
    suspend fun getRanges(
        @Query("api_key") apiKey: String,
        @Query("fixed") fixed: Boolean = false,
        @Query("currency_from") currencyFrom: String,
        @Query("currency_to") currencyTo: String
    ): SwapRangeResponse

    @GET("get_all_currencies")
    suspend fun getAllCurrencies(
        @Query("api_key") apiKey: String
    ): List<SwapCurrencyResponse>

    @POST("create_exchange")
    suspend fun createExchange(
        @Query("api_key") apiKey: String,
        @Body request: CreateSwapRequest
    ): SwapExchangeResponse
}

@JsonClass(generateAdapter = true)
data class SwapRangeResponse(
    @Json(name = "min") val min: String?,
    @Json(name = "max") val max: String?
)

@JsonClass(generateAdapter = true)
data class SwapCurrencyResponse(
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "network") val network: String?,
    @Json(name = "has_extra_id") val hasExtraId: Boolean?
)

@JsonClass(generateAdapter = true)
data class CreateSwapRequest(
    @Json(name = "fixed") val fixed: Boolean = false,
    @Json(name = "currency_from") val currencyFrom: String,
    @Json(name = "currency_to") val currencyTo: String,
    @Json(name = "amount") val amount: String,
    @Json(name = "address_to") val addressTo: String,
    @Json(name = "extra_id_to") val extraIdTo: String? = null
)

@JsonClass(generateAdapter = true)
data class SwapExchangeResponse(
    @Json(name = "id") val id: String,
    @Json(name = "address_from") val addressFrom: String,
    @Json(name = "address_to") val addressTo: String,
    @Json(name = "amount_from") val amountFrom: String,
    @Json(name = "amount_to") val amountTo: String,
    @Json(name = "status") val status: String
)
