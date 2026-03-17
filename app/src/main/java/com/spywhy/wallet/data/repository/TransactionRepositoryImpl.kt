package com.spywhy.wallet.data.repository

import com.spywhy.wallet.core.database.dao.TransactionDao
import com.spywhy.wallet.core.database.dao.UTXODao
import com.spywhy.wallet.core.database.entity.TransactionEntity
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.Transaction
import com.spywhy.wallet.domain.model.TransactionStatus
import com.spywhy.wallet.domain.model.UTXO
import com.spywhy.wallet.domain.repository.FeeEstimate
import com.spywhy.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val utxoDao: UTXODao
) : TransactionRepository {

    override fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> {
        return transactionDao.getByAccount(accountId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPendingTransactions(): Flow<List<Transaction>> {
        return transactionDao.getPending().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionByHash(txHash: String): Flow<Transaction?> {
        return transactionDao.getAll().map { entities ->
            entities.find { it.txHash == txHash }?.toDomain()
        }
    }

    override suspend fun refreshTransactions(
        accountId: Long,
        blockchain: Blockchain,
        address: String
    ) {
        // In production: fetch from blockchain explorer API and insert into DB
    }

    override suspend fun createTransaction(
        accountId: Long,
        blockchain: Blockchain,
        toAddress: String,
        amount: BigDecimal,
        fee: BigDecimal,
        selectedUtxos: List<UTXO>?
    ): String {
        val txId = UUID.randomUUID().toString()
        val entity = TransactionEntity(
            id = txId,
            accountId = accountId,
            txHash = "",
            fromAddress = "",
            toAddress = toAddress,
            amount = amount.toPlainString(),
            fee = fee.toPlainString(),
            status = TransactionStatus.PENDING.name,
            timestamp = System.currentTimeMillis(),
            blockchain = blockchain.ticker
        )
        transactionDao.insert(entity)
        return txId
    }

    override suspend fun signTransaction(
        rawTxHex: String,
        walletId: Long,
        blockchain: Blockchain
    ): String {
        // In production: sign with private key from wallet
        return rawTxHex
    }

    override suspend fun broadcastTransaction(
        blockchain: Blockchain,
        signedTxHex: String
    ): String {
        // In production: broadcast to network
        // Update the tx in DB with the hash
        val txHash = "0x" + signedTxHex.hashCode().toUInt().toString(16).padStart(64, '0')
        val existing = transactionDao.getByTxHash("")
        if (existing != null) {
            transactionDao.insert(existing.copy(txHash = txHash))
        }
        return txHash
    }

    override suspend fun estimateFee(
        blockchain: Blockchain,
        from: String,
        to: String,
        amount: BigDecimal
    ): FeeEstimate {
        return when (blockchain) {
            Blockchain.BITCOIN -> FeeEstimate(
                low = BigDecimal("0.00001"),
                medium = BigDecimal("0.00005"),
                high = BigDecimal("0.0002"),
                estimatedTimeLow = "~60 min",
                estimatedTimeMedium = "~30 min",
                estimatedTimeHigh = "~10 min"
            )
            Blockchain.ETHEREUM -> FeeEstimate(
                low = BigDecimal("0.001"),
                medium = BigDecimal("0.003"),
                high = BigDecimal("0.008"),
                estimatedTimeLow = "~5 min",
                estimatedTimeMedium = "~2 min",
                estimatedTimeHigh = "~30 sec"
            )
            Blockchain.SOLANA -> FeeEstimate(
                low = BigDecimal("0.000005"),
                medium = BigDecimal("0.000005"),
                high = BigDecimal("0.00001"),
                estimatedTimeLow = "~1 min",
                estimatedTimeMedium = "~30 sec",
                estimatedTimeHigh = "~10 sec"
            )
            Blockchain.LITECOIN -> FeeEstimate(
                low = BigDecimal("0.0001"),
                medium = BigDecimal("0.0005"),
                high = BigDecimal("0.001"),
                estimatedTimeLow = "~30 min",
                estimatedTimeMedium = "~10 min",
                estimatedTimeHigh = "~5 min"
            )
            Blockchain.MONERO -> FeeEstimate(
                low = BigDecimal("0.00001"),
                medium = BigDecimal("0.00005"),
                high = BigDecimal("0.0001"),
                estimatedTimeLow = "~20 min",
                estimatedTimeMedium = "~10 min",
                estimatedTimeHigh = "~5 min"
            )
        }
    }

    override fun getUnspentUTXOs(accountId: Long): Flow<List<UTXO>> {
        return utxoDao.getUnspent(accountId).map { entities ->
            entities.map { entity ->
                UTXO(
                    id = entity.id,
                    txHash = entity.txHash,
                    outputIndex = entity.outputIndex,
                    amount = entity.amount,
                    address = entity.address,
                    isSpent = entity.isSpent,
                    isLocked = entity.isLocked
                )
            }
        }
    }

    override suspend fun lockUTXO(utxoId: Long, locked: Boolean) {
        utxoDao.lockUTXO(utxoId, locked)
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            txHash = txHash,
            fromAddress = fromAddress,
            toAddress = toAddress,
            amount = BigDecimal(amount),
            fee = BigDecimal(fee),
            status = try { TransactionStatus.valueOf(status) } catch (_: Exception) { TransactionStatus.PENDING },
            confirmations = confirmations,
            timestamp = timestamp,
            blockchain = Blockchain.fromTicker(blockchain) ?: Blockchain.BITCOIN,
            rawHex = rawHex
        )
    }
}
