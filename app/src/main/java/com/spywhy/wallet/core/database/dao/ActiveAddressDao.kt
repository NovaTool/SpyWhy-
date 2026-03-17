package com.spywhy.wallet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spywhy.wallet.core.database.entity.ActiveAddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveAddressDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(address: ActiveAddressEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(addresses: List<ActiveAddressEntity>)

    @Update
    suspend fun update(address: ActiveAddressEntity)

    @Query("SELECT * FROM active_addresses WHERE wallet_id = :walletId AND blockchain = :blockchain AND is_active = 1 ORDER BY derivation_index ASC")
    fun getActiveByBlockchain(walletId: Long, blockchain: String): Flow<List<ActiveAddressEntity>>

    @Query("SELECT * FROM active_addresses WHERE wallet_id = :walletId AND is_active = 1 ORDER BY blockchain, derivation_index ASC")
    fun getAllActive(walletId: Long): Flow<List<ActiveAddressEntity>>

    @Query("SELECT * FROM active_addresses WHERE address = :address LIMIT 1")
    suspend fun getByAddress(address: String): ActiveAddressEntity?

    @Query("SELECT * FROM active_addresses WHERE blockchain = :blockchain ORDER BY derivation_index ASC")
    fun getByBlockchain(blockchain: String): Flow<List<ActiveAddressEntity>>

    @Query("SELECT MAX(derivation_index) FROM active_addresses WHERE wallet_id = :walletId AND blockchain = :blockchain")
    suspend fun getMaxDerivationIndex(walletId: Long, blockchain: String): Int?
}
