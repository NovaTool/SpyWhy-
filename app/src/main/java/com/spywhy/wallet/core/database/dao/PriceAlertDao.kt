package com.spywhy.wallet.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spywhy.wallet.core.database.entity.PriceAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceAlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: PriceAlertEntity): Long

    @Delete
    suspend fun delete(alert: PriceAlertEntity)

    @Query("SELECT * FROM price_alerts WHERE is_active = 1 ORDER BY created_at DESC")
    fun getActive(): Flow<List<PriceAlertEntity>>
}
