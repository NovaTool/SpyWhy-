package com.spywhy.wallet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "utxos",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["account_id"]),
        Index(value = ["tx_hash", "output_index"], unique = true)
    ]
)
data class UTXOEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "account_id")
    val accountId: Long,

    @ColumnInfo(name = "tx_hash")
    val txHash: String,

    @ColumnInfo(name = "output_index")
    val outputIndex: Int,

    @ColumnInfo(name = "amount")
    val amount: Long,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "is_spent")
    val isSpent: Boolean = false,

    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean = false
)
