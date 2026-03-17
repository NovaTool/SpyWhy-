package com.spywhy.wallet.domain.model

data class PriceData(
    val coinId: String,
    val symbol: String = "",
    val name: String = "",
    val priceUsd: Double,
    val marketCap: Double,
    val volume24h: Double,
    val change24h: Double,
    val change7d: Double,
    val sparkline: List<Double>,
    val updatedAt: Long
)
