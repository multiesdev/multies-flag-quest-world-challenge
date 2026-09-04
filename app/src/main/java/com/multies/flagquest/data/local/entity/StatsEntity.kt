package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
data class StatsEntity(
    @PrimaryKey val statKey: String, // e.g. "total_correct", "total_answered"
    val statValue: Long = 0L
)
