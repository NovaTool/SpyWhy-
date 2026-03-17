package com.spywhy.wallet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "coin_id")
    val coinId: String,

    @ColumnInfo(name = "target_price")
    val targetPrice: Double,

    @ColumnInfo(name = "is_above")
    val isAbove: Boolean, // true = alert when price goes above target, false = below

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
