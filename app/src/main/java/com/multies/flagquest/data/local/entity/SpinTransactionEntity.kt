package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spin_transactions")
data class SpinTransactionEntity(
    @PrimaryKey val transactionId: String,
    val timestamp: Long,
    val rewardId: String,
    val status: String, // "PENDING", "COMMITTED"
    val dateStr: String, // e.g. "2026-07-20"
    val coinsGranted: Int,
    val heartsGranted: Int,
    val overflowCoinsGranted: Int
)
