package com.spywhy.wallet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spywhy.wallet.core.database.entity.TokenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: TokenEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tokens: List<TokenEntity>)

    @Query("SELECT * FROM tokens WHERE account_id = :accountId")
    fun getByAccount(accountId: Long): Flow<List<TokenEntity>>

    @Query("SELECT * FROM tokens WHERE contract_address = :contractAddress AND account_id = :accountId LIMIT 1")
    suspend fun getByContract(contractAddress: String, accountId: Long): TokenEntity?
}
