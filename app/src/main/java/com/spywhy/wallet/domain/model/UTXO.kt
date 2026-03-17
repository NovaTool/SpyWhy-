package com.spywhy.wallet.domain.model

data class UTXO(
    val id: Long,
    val txHash: String,
    val outputIndex: Int,
    val amount: Long,
    val address: String,
    val isSpent: Boolean,
    val isLocked: Boolean
)
