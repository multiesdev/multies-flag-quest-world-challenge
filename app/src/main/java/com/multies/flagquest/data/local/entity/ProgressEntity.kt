package com.multies.flagquest.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "level_progress",
    primaryKeys = ["categoryId", "levelIndex"]
)
data class ProgressEntity(
    val categoryId: String,  // e.g. "flags_continents", "capitals"
    val levelIndex: Int,      // e.g. 1, 2, 3...
    val starsEarned: Int = 0, // 0 to 3 stars
    val isCompleted: Boolean = false,
    val highestScore: Int = 0,
    val completedAt: Long = 0L // Timestamp
)
