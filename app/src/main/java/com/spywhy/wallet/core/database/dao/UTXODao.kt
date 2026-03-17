package com.spywhy.wallet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spywhy.wallet.core.database.entity.UTXOEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UTXODao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(utxo: UTXOEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(utxos: List<UTXOEntity>)

    @Query("SELECT * FROM utxos WHERE account_id = :accountId AND is_spent = 0 AND is_locked = 0")
    fun getUnspent(accountId: Long): Flow<List<UTXOEntity>>

    @Query("SELECT * FROM utxos WHERE account_id = :accountId")
    fun getByAccount(accountId: Long): Flow<List<UTXOEntity>>

    @Query("UPDATE utxos SET is_spent = 1 WHERE id = :utxoId")
    suspend fun markSpent(utxoId: Long)

    @Query("UPDATE utxos SET is_locked = :locked WHERE id = :utxoId")
    suspend fun lockUTXO(utxoId: Long, locked: Boolean)
}
