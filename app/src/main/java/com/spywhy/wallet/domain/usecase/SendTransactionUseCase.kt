package com.spywhy.wallet.domain.usecase

import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.UTXO
import com.spywhy.wallet.domain.repository.FeeEstimate
import com.spywhy.wallet.domain.repository.TransactionRepository
import java.math.BigDecimal
import javax.inject.Inject

class SendTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend fun estimateFees(
        blockchain: Blockchain,
        fromAddress: String,
        toAddress: String,
        amount: BigDecimal
    ): FeeEstimate {
        return transactionRepository.estimateFee(blockchain, fromAddress, toAddress, amount)
    }

    suspend fun createAndSign(
        accountId: Long,
        walletId: Long,
        blockchain: Blockchain,
        toAddress: String,
        amount: BigDecimal,
        fee: BigDecimal,
        selectedUtxos: List<UTXO>? = null
    ): String {
        val rawTx = transactionRepository.createTransaction(
            accountId, blockchain, toAddress, amount, fee, selectedUtxos
        )
        return transactionRepository.signTransaction(rawTx, walletId, blockchain)
    }

    suspend fun broadcast(blockchain: Blockchain, signedTxHex: String): String {
        return transactionRepository.broadcastTransaction(blockchain, signedTxHex)
    }

    fun isDustAmount(blockchain: Blockchain, amount: BigDecimal): Boolean {
        return when (blockchain) {
            Blockchain.BITCOIN, Blockchain.LITECOIN -> amount < BigDecimal("0.00000546")
            Blockchain.ETHEREUM -> amount < BigDecimal("0.000001")
            Blockchain.SOLANA -> amount < BigDecimal("0.000001")
            Blockchain.MONERO -> amount < BigDecimal("0.000000000001")
        }
    }

    fun isAddressSimilar(address1: String, address2: String): Boolean {
        if (address1.length != address2.length) return false
        val diffCount = address1.zip(address2).count { (a, b) -> a != b }
        return diffCount in 1..3
    }
}
