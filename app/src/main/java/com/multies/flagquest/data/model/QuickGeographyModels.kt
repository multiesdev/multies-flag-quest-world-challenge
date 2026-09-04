package com.multies.flagquest.data.model

enum class QuickGeographyQuestionType {
    FLAG_TO_COUNTRY,
    COUNTRY_TO_CAPITAL,
    CAPITAL_TO_COUNTRY,
    COUNTRY_TO_CONTINENT,
    MAP_SILHOUETTE,
    AREA_COMPARISON,
    FARTHER_NORTH,
    TRUE_FALSE,
    LAND_BORDER
}

data class QuickGeographyQuestion(
    val id: String,
    val type: QuickGeographyQuestionType,
    val promptEn: String,
    val promptAr: String,
    val promptDe: String,
    val promptFr: String,
    val flagEmoji: String? = null,
    val mapSvgPath: String? = null,
    val optionsEn: List<String>,
    val optionsAr: List<String>,
    val optionsDe: List<String>,
    val optionsFr: List<String>,
    val correctOptionIndex: Int,
    val educationalFactEn: String? = null,
    val educationalFactAr: String? = null,
    val educationalFactDe: String? = null,
    val educationalFactFr: String? = null,
    val countryId: String? = null
) {
    fun getLocalizedPrompt(lang: String): String = when (lang) {
        "ar" -> promptAr
        "de" -> promptDe
        "fr" -> promptFr
        else -> promptEn
    }

    fun getLocalizedOptions(lang: String): List<String> = when (lang) {
        "ar" -> optionsAr
        "de" -> optionsDe
        "fr" -> optionsFr
        else -> optionsEn
    }

    fun getLocalizedEducationalFact(lang: String): String? = when (lang) {
        "ar" -> educationalFactAr
        "de" -> educationalFactDe
        "fr" -> educationalFactFr
        else -> educationalFactEn
    }
}

data class QuickGeographySessionResult(
    val finalScore: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val questionsAnswered: Int,
    val accuracyPercentage: Int,
    val longestStreak: Int,
    val averageResponseTimeSeconds: Float,
    val isNewHighScore: Boolean,
    val rewardCoinsEarned: Int
)
