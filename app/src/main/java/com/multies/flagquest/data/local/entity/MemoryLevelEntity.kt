package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_level_records")
data class MemoryLevelEntity(
    @PrimaryKey val levelIndex: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val bestCompletionTimeSec: Long = 0L,
    val bestMoves: Int = 0,
    val bestMismatches: Int = 0,
    val highestScore: Int = 0,
    val highestStars: Int = 0,
    val attemptsCount: Int = 0,
    val lastFlagIds: String = ""
)
