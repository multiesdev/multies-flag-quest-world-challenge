package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1, // Singleton row
    val coins: Int = 100,
    val lives: Int = 5,
    val maxLives: Int = 5,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalStars: Int = 0,
    val selectedLanguage: String = "en", // "en", "ar", "de", "fr"
    val themeMode: String = "system",    // "system", "light", "dark"
    val isMusicEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isAdsRemoved: Boolean = false,
    val lastDailyRewardClaimed: Long = 0L, // Timestamp
    val nextHeartRegenTimestamp: Long = 0L,
    val relaxedMode: Boolean = false,
    val classroomMode: Boolean = false,
    val isAdaptiveDifficultyEnabled: Boolean = true,
    val adaptiveDifficultyOffset: Int = 0, // -1, 0, 1
    
    // Phase 5 Level, XP, Hints and Daily state fields
    val xp: Int = 0,
    val level: Int = 1,
    val hintsCount: Int = 3,
    val premiumThemeTrialExpiry: Long = 0L,
    val lastDailyRewardDayClaimed: Int = 0, // 1 to 7 corresponding to the day index claimed
    val lastSpinTimestamp: Long = 0L,
    val avatarId: String = "default",
    val purchasedThemes: String = "system,light,dark"
)
