package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spin_reward_history")
data class SpinRewardHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: String,
    val rewardId: String,
    val timestamp: Long,
    val dateStr: String,
    val coinsWon: Int,
    val heartsWon: Int,
    val overflowCoins: Int
)
