package com.spywhy.wallet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spywhy.wallet.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Query("SELECT * FROM accounts WHERE wallet_id = :walletId")
    fun getByWallet(walletId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE blockchain = :blockchain")
    fun getByBlockchain(blockchain: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE address = :address LIMIT 1")
    suspend fun getByAddress(address: String): AccountEntity?
}
