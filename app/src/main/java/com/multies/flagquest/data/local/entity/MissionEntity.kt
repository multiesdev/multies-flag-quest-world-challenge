package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: String, // e.g., "daily_questions", "daily_countries", "weekly_capitals"
    val titleKey: String,       // Resource key or direct string
    val type: String,           // "DAILY" or "WEEKLY"
    val currentProgress: Int = 0,
    val targetProgress: Int = 10,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardCoins: Int = 20,
    val rewardHints: Int = 0,
    val lastUpdatedTimestamp: Long = 0L
)
