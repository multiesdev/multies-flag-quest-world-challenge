package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String, // e.g. "first_victory", "streak_10"
    val titleKey: String,       // String resource key for localization
    val descKey: String,        // String resource key for localization
    val iconName: String,       // Drawable name or icon symbol
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 1,   // For multi-step achievements
    val unlockedAt: Long = 0L   // Timestamp
)
