package com.spywhy.wallet.domain.model

import java.math.BigDecimal

data class Transaction(
    val id: String,
    val txHash: String,
    val fromAddress: String,
    val toAddress: String,
    val amount: BigDecimal,
    val fee: BigDecimal,
    val status: TransactionStatus,
    val confirmations: Int,
    val timestamp: Long,
    val blockchain: Blockchain,
    val rawHex: String?
)

enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED
}
