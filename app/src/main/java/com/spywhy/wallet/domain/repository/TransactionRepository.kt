package com.spywhy.wallet.domain.repository

import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.Transaction
import com.spywhy.wallet.domain.model.UTXO
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface TransactionRepository {
    fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>>
    fun getPendingTransactions(): Flow<List<Transaction>>
    fun getTransactionByHash(txHash: String): Flow<Transaction?>
    suspend fun refreshTransactions(accountId: Long, blockchain: Blockchain, address: String)
    suspend fun createTransaction(
        accountId: Long,
        blockchain: Blockchain,
        toAddress: String,
        amount: BigDecimal,
        fee: BigDecimal,
        selectedUtxos: List<UTXO>? = null
    ): String
    suspend fun signTransaction(rawTxHex: String, walletId: Long, blockchain: Blockchain): String
    suspend fun broadcastTransaction(blockchain: Blockchain, signedTxHex: String): String
    suspend fun estimateFee(blockchain: Blockchain, from: String, to: String, amount: BigDecimal): FeeEstimate
    fun getUnspentUTXOs(accountId: Long): Flow<List<UTXO>>
    suspend fun lockUTXO(utxoId: Long, locked: Boolean)
}

data class FeeEstimate(
    val low: BigDecimal,
    val medium: BigDecimal,
    val high: BigDecimal,
    val estimatedTimeLow: String,
    val estimatedTimeMedium: String,
    val estimatedTimeHigh: String
)
