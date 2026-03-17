package com.spywhy.wallet.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.spywhy.wallet.core.database.AppDatabase
import com.spywhy.wallet.core.update.UpdateManager
import com.spywhy.wallet.core.util.Constants
import com.spywhy.wallet.SecurityManager
import dagger.Binds
import okhttp3.OkHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spywhy_settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `favorites` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `coin_id` TEXT NOT NULL, `coin_name` TEXT NOT NULL, `ticker` TEXT NOT NULL, `created_at` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_favorites_coin_id` ON `favorites` (`coin_id`)"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `active_addresses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `wallet_id` INTEGER NOT NULL, `blockchain` TEXT NOT NULL, `address` TEXT NOT NULL, `derivation_index` INTEGER NOT NULL, `is_active` INTEGER NOT NULL, `label` TEXT, `created_at` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_active_addresses_address` ON `active_addresses` (`address`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_active_addresses_blockchain` ON `active_addresses` (`blockchain`)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): android.content.SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            Constants.ENCRYPTED_PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    fun provideWalletDao(database: AppDatabase) = database.walletDao()

    @Provides
    fun provideAccountDao(database: AppDatabase) = database.accountDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase) = database.transactionDao()

    @Provides
    fun provideUTXODao(database: AppDatabase) = database.utxoDao()

    @Provides
    fun provideTokenDao(database: AppDatabase) = database.tokenDao()

    @Provides
    fun provideNFTDao(database: AppDatabase) = database.nftDao()

    @Provides
    fun providePriceAlertDao(database: AppDatabase) = database.priceAlertDao()

    @Provides
    fun providePriceCacheDao(database: AppDatabase) = database.priceCacheDao()

    @Provides
    fun provideContactDao(database: AppDatabase) = database.contactDao()

    @Provides
    fun provideFavoriteDao(database: AppDatabase) = database.favoriteDao()

    @Provides
    fun provideActiveAddressDao(database: AppDatabase) = database.activeAddressDao()

    @Provides
    @Singleton
    fun provideUpdateManager(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): UpdateManager = UpdateManager(context, okHttpClient)

    @Provides
    @Singleton
    fun provideSecurityManager(): SecurityManager = DefaultSecurityManager()
}

class DefaultSecurityManager @Inject constructor() : SecurityManager {
    private var pauseTimestamp: Long = 0L

    override fun recordPauseTimestamp() {
        pauseTimestamp = System.currentTimeMillis()
    }

    override fun shouldLock(): Boolean {
        if (pauseTimestamp == 0L) return false
        return (System.currentTimeMillis() - pauseTimestamp) > Constants.AUTO_LOCK_TIMEOUT_MS
    }
}
