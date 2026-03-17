package com.spywhy.wallet.domain.model

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val blockchain: Blockchain,
    val iconUrl: String,
    val decimals: Int
)
