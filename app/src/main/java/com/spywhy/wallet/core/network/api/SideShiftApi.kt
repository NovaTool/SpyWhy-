package com.spywhy.wallet.core.network.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

interface SideShiftApi {

    @GET("coins")
    suspend fun getCoins(): List<SideShiftCoinResponse>

    @GET("pair/{from}/{to}")
    suspend fun getPair(
        @Path("from") from: String,
        @Path("to") to: String
    ): SideShiftPairResponse

    @POST("shifts/fixed")
    suspend fun createFixedShift(
        @Body request: SideShiftCreateRequest
    ): SideShiftShiftResponse

    @POST("shifts/variable")
    suspend fun createVariableShift(
        @Body request: SideShiftCreateRequest
    ): SideShiftShiftResponse

    @GET("shifts/{id}")
    suspend fun getShiftStatus(
        @Path("id") id: String
    ): SideShiftShiftResponse
}

@JsonClass(generateAdapter = true)
data class SideShiftCoinResponse(
    @Json(name = "coin") val coin: String,
    @Json(name = "networks") val networks: List<String>?,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class SideShiftPairResponse(
    @Json(name = "rate") val rate: String?,
    @Json(name = "min") val min: String?,
    @Json(name = "max") val max: String?,
    @Json(name = "depositCoin") val depositCoin: String?,
    @Json(name = "settleCoin") val settleCoin: String?
)

@JsonClass(generateAdapter = true)
data class SideShiftCreateRequest(
    @Json(name = "depositCoin") val depositCoin: String,
    @Json(name = "settleCoin") val settleCoin: String,
    @Json(name = "settleAddress") val settleAddress: String,
    @Json(name = "depositAmount") val depositAmount: String? = null
)

@JsonClass(generateAdapter = true)
data class SideShiftShiftResponse(
    @Json(name = "id") val id: String,
    @Json(name = "depositAddress") val depositAddress: String?,
    @Json(name = "settleAddress") val settleAddress: String?,
    @Json(name = "depositCoin") val depositCoin: String?,
    @Json(name = "settleCoin") val settleCoin: String?,
    @Json(name = "depositAmount") val depositAmount: String?,
    @Json(name = "settleAmount") val settleAmount: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "rate") val rate: String?
)
