package com.spywhy.wallet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "active_addresses",
    indices = [
        Index(value = ["address"], unique = true),
        Index(value = ["blockchain"])
    ]
)
data class ActiveAddressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "wallet_id")
    val walletId: Long,

    @ColumnInfo(name = "blockchain")
    val blockchain: String,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "derivation_index")
    val derivationIndex: Int,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "label")
    val label: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
