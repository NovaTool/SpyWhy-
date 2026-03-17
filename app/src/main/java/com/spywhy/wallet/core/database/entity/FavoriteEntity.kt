package com.spywhy.wallet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    indices = [
        Index(value = ["coin_id"], unique = true)
    ]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "coin_id")
    val coinId: String,

    @ColumnInfo(name = "coin_name")
    val coinName: String,

    @ColumnInfo(name = "ticker")
    val ticker: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
