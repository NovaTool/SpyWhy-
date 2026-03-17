package com.spywhy.wallet.domain.model

import java.math.BigDecimal

data class Token(
    val contractAddress: String,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val balance: BigDecimal,
    val blockchain: Blockchain,
    val iconUrl: String?
)
