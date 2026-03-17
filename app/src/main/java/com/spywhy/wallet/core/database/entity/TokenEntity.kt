package com.spywhy.wallet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tokens",
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
        Index(value = ["contract_address", "account_id"], unique = true)
    ]
)
data class TokenEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "account_id")
    val accountId: Long,

    @ColumnInfo(name = "contract_address")
    val contractAddress: String,

    @ColumnInfo(name = "symbol")
    val symbol: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "decimals")
    val decimals: Int,

    @ColumnInfo(name = "balance")
    val balance: String, // BigDecimal stored as String

    @ColumnInfo(name = "blockchain")
    val blockchain: String,

    @ColumnInfo(name = "icon_url")
    val iconUrl: String? = null
)
