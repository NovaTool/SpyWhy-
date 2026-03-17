package com.spywhy.wallet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_cache")
data class PriceCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "coin_id")
    val coinId: String,

    @ColumnInfo(name = "price_usd")
    val priceUsd: Double,

    @ColumnInfo(name = "market_cap")
    val marketCap: Double,

    @ColumnInfo(name = "volume_24h")
    val volume24h: Double,

    @ColumnInfo(name = "change_24h")
    val change24h: Double,

    @ColumnInfo(name = "change_7d")
    val change7d: Double,

    @ColumnInfo(name = "sparkline")
    val sparkline: String, // JSON-encoded List<Double>

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
