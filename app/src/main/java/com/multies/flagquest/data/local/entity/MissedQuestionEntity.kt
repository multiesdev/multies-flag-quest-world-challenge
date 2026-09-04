package com.multies.flagquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missed_questions")
data class MissedQuestionEntity(
    @PrimaryKey val questionId: String,
    val categoryId: String,
    val incorrectCount: Int = 1,
    val correctCountInARow: Int = 0,
    val nextReviewTimestamp: Long = 0L
)
