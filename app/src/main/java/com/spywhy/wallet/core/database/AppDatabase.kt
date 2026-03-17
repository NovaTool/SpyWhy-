package com.spywhy.wallet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spywhy.wallet.core.database.dao.AccountDao
import com.spywhy.wallet.core.database.dao.ContactDao
import com.spywhy.wallet.core.database.dao.NFTDao
import com.spywhy.wallet.core.database.dao.PriceAlertDao
import com.spywhy.wallet.core.database.dao.PriceCacheDao
import com.spywhy.wallet.core.database.dao.TokenDao
import com.spywhy.wallet.core.database.dao.TransactionDao
import com.spywhy.wallet.core.database.dao.UTXODao
import com.spywhy.wallet.core.database.dao.WalletDao
import com.spywhy.wallet.core.database.entity.AccountEntity
import com.spywhy.wallet.core.database.entity.ContactEntity
import com.spywhy.wallet.core.database.entity.NFTEntity
import com.spywhy.wallet.core.database.entity.PriceAlertEntity
import com.spywhy.wallet.core.database.entity.PriceCacheEntity
import com.spywhy.wallet.core.database.entity.TokenEntity
import com.spywhy.wallet.core.database.entity.TransactionEntity
import com.spywhy.wallet.core.database.entity.UTXOEntity
import com.spywhy.wallet.core.database.entity.WalletEntity

@Database(
    entities = [
        WalletEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        UTXOEntity::class,
        TokenEntity::class,
        NFTEntity::class,
        PriceAlertEntity::class,
        PriceCacheEntity::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun utxoDao(): UTXODao
    abstract fun tokenDao(): TokenDao
    abstract fun nftDao(): NFTDao
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun priceCacheDao(): PriceCacheDao
    abstract fun contactDao(): ContactDao

    companion object {
        const val DATABASE_NAME = "spywhy_wallet.db"
    }
}
