package com.spywhy.wallet.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spywhy.wallet.core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE coin_id = :coinId")
    suspend fun deleteByCoinId(coinId: String)

    @Query("SELECT * FROM favorites ORDER BY created_at DESC")
    fun getAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE coin_id = :coinId LIMIT 1")
    suspend fun getByCoinId(coinId: String): FavoriteEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE coin_id = :coinId)")
    fun isFavorite(coinId: String): Flow<Boolean>
}
