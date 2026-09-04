package com.multies.flagquest.features.whoami.engine

import com.multies.flagquest.data.model.Country

data class WhoAmIClue(
    val id: String,
    val textEn: String,
    val textAr: String,
    val textDe: String,
    val textFr: String
) {
    fun getLocalizedText(lang: String): String = when (lang) {
        "ar" -> textAr
        "de" -> textDe
        "fr" -> textFr
        else -> textEn
    }
}

data class WhoAmIQuestion(
    val id: String,
    val targetCountry: Country,
    val clues: List<WhoAmIClue>,
    val options: List<Country>,
    val correctIndex: Int,
    val difficulty: Int,
    val timeTargetSec: Int,
    val educationalFactEn: String,
    val educationalFactAr: String,
    val educationalFactDe: String,
    val educationalFactFr: String
) {
    fun getLocalizedEducationalFact(lang: String): String = when (lang) {
        "ar" -> educationalFactAr
        "de" -> educationalFactDe
        "fr" -> educationalFactFr
        else -> educationalFactEn
    }
}

data class WhoAmIScoreResult(
    val finalScore: Int,
    val starsEarned: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val accuracyPercentage: Int,
    val averageCluesUsed: Double,
    val bestEarlyStreak: Int,
    val elapsedTimeSec: Long,
    val coinsEarned: Int,
    val isNewRecord: Boolean
)
