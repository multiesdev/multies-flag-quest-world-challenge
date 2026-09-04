package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "atlas_discoveries")
data class AtlasEntity(
    @PrimaryKey val countryId: String, // Stable ISO-3166 code e.g. "SA"
    val isDiscovered: Boolean = false,
    val answeredCorrectlyCount: Int = 0,
    val discoveredAt: Long = 0L, // Timestamp
    val isFavorite: Boolean = false
)
