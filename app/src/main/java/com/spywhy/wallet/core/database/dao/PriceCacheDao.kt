package com.spywhy.wallet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spywhy.wallet.core.database.entity.PriceCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(priceCache: PriceCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(prices: List<PriceCacheEntity>)

    @Query("SELECT * FROM price_cache WHERE coin_id = :coinId LIMIT 1")
    suspend fun getByCoinId(coinId: String): PriceCacheEntity?

    @Query("SELECT * FROM price_cache ORDER BY updated_at DESC")
    fun getAll(): Flow<List<PriceCacheEntity>>
}
