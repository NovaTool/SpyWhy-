package com.spywhy.wallet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
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
        Index(value = ["tx_hash"]),
        Index(value = ["status"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "account_id")
    val accountId: Long,

    @ColumnInfo(name = "tx_hash")
    val txHash: String,

    @ColumnInfo(name = "from_address")
    val fromAddress: String,

    @ColumnInfo(name = "to_address")
    val toAddress: String,

    @ColumnInfo(name = "amount")
    val amount: String, // BigDecimal stored as String

    @ColumnInfo(name = "fee")
    val fee: String, // BigDecimal stored as String

    @ColumnInfo(name = "status")
    val status: String, // PENDING, CONFIRMED, FAILED

    @ColumnInfo(name = "confirmations")
    val confirmations: Int = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "blockchain")
    val blockchain: String,

    @ColumnInfo(name = "raw_hex")
    val rawHex: String? = null
)
