package com.spywhy.wallet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spywhy.wallet.core.database.entity.NFTEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NFTDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nft: NFTEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nfts: List<NFTEntity>)

    @Query("SELECT * FROM nfts WHERE account_id = :accountId")
    fun getByAccount(accountId: Long): Flow<List<NFTEntity>>
}
