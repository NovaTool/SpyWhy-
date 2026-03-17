package com.spywhy.wallet.core.di

import com.spywhy.wallet.core.database.dao.PriceAlertDao
import com.spywhy.wallet.core.database.dao.PriceCacheDao
import com.spywhy.wallet.core.network.api.CoinGeckoApi
import com.spywhy.wallet.data.repository.MarketRepositoryImpl
import com.spywhy.wallet.domain.model.Blockchain
import com.spywhy.wallet.domain.model.PriceData
import com.spywhy.wallet.domain.model.SwapQuote
import com.spywhy.wallet.domain.model.Transaction
import com.spywhy.wallet.domain.model.UTXO
import com.spywhy.wallet.domain.model.Wallet
import com.spywhy.wallet.domain.repository.FeeEstimate
import com.spywhy.wallet.domain.repository.MarketRepository
import com.spywhy.wallet.domain.repository.PriceAlert
import com.spywhy.wallet.domain.repository.TransactionRepository
import com.spywhy.wallet.domain.repository.WalletRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWalletRepository(): WalletRepository = object : WalletRepository {
        override fun getAllWallets(): Flow<List<Wallet>> = flowOf(emptyList())
        override fun getWalletById(id: Long): Flow<Wallet?> = flowOf(null)
        override fun getHiddenWallets(): Flow<List<Wallet>> = flowOf(emptyList())
        override suspend fun createWallet(name: String, seedPhrase: List<String>, blockchains: List<Blockchain>): Long = 1L
        override suspend fun importWallet(name: String, seedPhrase: List<String>, blockchains: List<Blockchain>): Long = 1L
        override suspend fun createWatchOnlyWallet(name: String, addresses: Map<Blockchain, String>): Long = 1L
        override suspend fun deleteWallet(walletId: Long) {}
        override suspend fun renameWallet(walletId: Long, newName: String) {}
        override suspend fun toggleHidden(walletId: Long, isHidden: Boolean) {}
        override suspend fun getDecryptedSeedPhrase(walletId: Long): List<String> = emptyList()
        override suspend fun exportEncryptedBackup(walletId: Long): ByteArray = byteArrayOf()
        override suspend fun importEncryptedBackup(data: ByteArray, password: String): Long = 1L
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(): TransactionRepository = object : TransactionRepository {
        override fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getPendingTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
        override fun getTransactionByHash(txHash: String): Flow<Transaction?> = flowOf(null)
        override suspend fun refreshTransactions(accountId: Long, blockchain: Blockchain, address: String) {}
        override suspend fun createTransaction(
            accountId: Long, blockchain: Blockchain, toAddress: String,
            amount: BigDecimal, fee: BigDecimal, selectedUtxos: List<UTXO>?
        ): String = ""
        override suspend fun signTransaction(rawTxHex: String, walletId: Long, blockchain: Blockchain): String = ""
        override suspend fun broadcastTransaction(blockchain: Blockchain, signedTxHex: String): String = ""
        override suspend fun estimateFee(blockchain: Blockchain, from: String, to: String, amount: BigDecimal): FeeEstimate =
            FeeEstimate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "~60min", "~30min", "~10min")
        override fun getUnspentUTXOs(accountId: Long): Flow<List<UTXO>> = flowOf(emptyList())
        override suspend fun lockUTXO(utxoId: Long, locked: Boolean) {}
    }

    @Provides
    @Singleton
    fun provideMarketRepository(
        coinGeckoApi: CoinGeckoApi,
        priceCacheDao: PriceCacheDao,
        priceAlertDao: PriceAlertDao
    ): MarketRepository = MarketRepositoryImpl(coinGeckoApi, priceCacheDao, priceAlertDao)
}
