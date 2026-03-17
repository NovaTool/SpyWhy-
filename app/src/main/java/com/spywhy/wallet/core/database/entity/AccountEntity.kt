package com.spywhy.wallet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["wallet_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["wallet_id"]),
        Index(value = ["address"], unique = true)
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "wallet_id")
    val walletId: Long,

    @ColumnInfo(name = "blockchain")
    val blockchain: String,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "derivation_index")
    val derivationIndex: Int
)
