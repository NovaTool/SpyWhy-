package com.spywhy.wallet.domain.model

import java.math.BigDecimal

data class SwapQuote(
    val provider: String,
    val fromCoin: String,
    val toCoin: String,
    val fromAmount: BigDecimal,
    val toAmount: BigDecimal,
    val rate: Double,
    val fee: Double,
    val estimatedTime: String,
    val kycRequired: Boolean
)
