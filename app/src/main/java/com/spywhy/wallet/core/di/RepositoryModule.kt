package com.spywhy.wallet.core.di

import com.spywhy.wallet.core.crypto.AddressGenerator
import com.spywhy.wallet.core.database.dao.AccountDao
import com.spywhy.wallet.core.database.dao.ActiveAddressDao
import com.spywhy.wallet.core.database.dao.PriceAlertDao
import com.spywhy.wallet.core.database.dao.PriceCacheDao
import com.spywhy.wallet.core.database.dao.TransactionDao
import com.spywhy.wallet.core.database.dao.UTXODao
import com.spywhy.wallet.core.database.dao.WalletDao
import com.spywhy.wallet.core.network.api.CoinGeckoApi
import com.spywhy.wallet.data.repository.MarketRepositoryImpl
import com.spywhy.wallet.data.repository.TransactionRepositoryImpl
import com.spywhy.wallet.data.repository.WalletRepositoryImpl
import com.spywhy.wallet.domain.repository.MarketRepository
import com.spywhy.wallet.domain.repository.TransactionRepository
import com.spywhy.wallet.domain.repository.WalletRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWalletRepository(
        walletDao: WalletDao,
        accountDao: AccountDao,
        activeAddressDao: ActiveAddressDao,
        addressGenerator: AddressGenerator,
        encryptedPrefs: android.content.SharedPreferences
    ): WalletRepository = WalletRepositoryImpl(
        walletDao, accountDao, activeAddressDao, addressGenerator, encryptedPrefs
    )

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        utxoDao: UTXODao
    ): TransactionRepository = TransactionRepositoryImpl(transactionDao, utxoDao)

    @Provides
    @Singleton
    fun provideMarketRepository(
        coinGeckoApi: CoinGeckoApi,
        priceCacheDao: PriceCacheDao,
        priceAlertDao: PriceAlertDao
    ): MarketRepository = MarketRepositoryImpl(coinGeckoApi, priceCacheDao, priceAlertDao)
}
