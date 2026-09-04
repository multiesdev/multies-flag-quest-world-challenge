package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_spin_state")
data class DailySpinEntity(
    @PrimaryKey val id: Int = 1, // Singleton row
    val lastSpinDate: String = "", // Local calendar day string e.g., "2026-07-20"
    val lastSpinTimestamp: Long = 0L,
    val nextEligibleDate: String = "", // e.g. "2026-07-21"
    val lastRewardId: String = "",
    val lastTransactionId: String = "",
    val claimStatus: String = "NOT_STARTED", // "NOT_STARTED", "SPINNING", "CLAIMED"
    val timezoneId: String = ""
)
