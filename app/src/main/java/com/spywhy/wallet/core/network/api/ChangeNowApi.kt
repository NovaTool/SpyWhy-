package com.spywhy.wallet.core.network.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

interface ChangeNowApi {

    @GET("exchange/estimated-amount")
    suspend fun getEstimatedAmount(
        @Query("fromCurrency") fromCurrency: String,
        @Query("toCurrency") toCurrency: String,
        @Query("fromAmount") fromAmount: String,
        @Query("fromNetwork") fromNetwork: String? = null,
        @Query("toNetwork") toNetwork: String? = null
    ): ChangeNowEstimateResponse

    @GET("exchange/min-amount")
    suspend fun getMinAmount(
        @Query("fromCurrency") fromCurrency: String,
        @Query("toCurrency") toCurrency: String
    ): ChangeNowMinAmountResponse

    @GET("exchange/currencies")
    suspend fun getCurrencies(
        @Query("active") active: Boolean = true
    ): List<ChangeNowCurrencyResponse>

    @POST("exchange")
    suspend fun createExchange(
        @Body request: ChangeNowCreateRequest
    ): ChangeNowExchangeResponse

    @GET("exchange/by-id/{id}")
    suspend fun getExchangeStatus(
        @Path("id") id: String
    ): ChangeNowExchangeResponse
}

@JsonClass(generateAdapter = true)
data class ChangeNowEstimateResponse(
    @Json(name = "estimatedAmount") val estimatedAmount: Double?,
    @Json(name = "transactionSpeedForecast") val transactionSpeedForecast: String?,
    @Json(name = "warningMessage") val warningMessage: String?
)

@JsonClass(generateAdapter = true)
data class ChangeNowMinAmountResponse(
    @Json(name = "minAmount") val minAmount: Double?
)

@JsonClass(generateAdapter = true)
data class ChangeNowCurrencyResponse(
    @Json(name = "ticker") val ticker: String,
    @Json(name = "name") val name: String,
    @Json(name = "network") val network: String?,
    @Json(name = "hasExternalId") val hasExternalId: Boolean?,
    @Json(name = "isStable") val isStable: Boolean?
)

@JsonClass(generateAdapter = true)
data class ChangeNowCreateRequest(
    @Json(name = "fromCurrency") val fromCurrency: String,
    @Json(name = "toCurrency") val toCurrency: String,
    @Json(name = "fromAmount") val fromAmount: String,
    @Json(name = "address") val address: String,
    @Json(name = "extraId") val extraId: String? = null,
    @Json(name = "flow") val flow: String = "standard"
)

@JsonClass(generateAdapter = true)
data class ChangeNowExchangeResponse(
    @Json(name = "id") val id: String,
    @Json(name = "payinAddress") val payinAddress: String?,
    @Json(name = "payoutAddress") val payoutAddress: String?,
    @Json(name = "fromCurrency") val fromCurrency: String?,
    @Json(name = "toCurrency") val toCurrency: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "expectedSendAmount") val expectedSendAmount: Double?,
    @Json(name = "expectedReceiveAmount") val expectedReceiveAmount: Double?
)
