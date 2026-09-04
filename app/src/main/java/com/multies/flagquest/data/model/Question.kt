package com.multies.flagquest.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Question(
    val id: String,
    val category: String, // e.g. "FLAGS", "CAPITALS", "POPULATION", "CURRENCIES"
    val difficulty: String, // "EASY", "MEDIUM", "HARD"
    val questionTextEn: String,
    val questionTextAr: String,
    val questionTextDe: String,
    val questionTextFr: String,
    val optionsEn: List<String>,
    val optionsAr: List<String>,
    val optionsDe: List<String>,
    val optionsFr: List<String>,
    val correctOptionIndex: Int, // Index from 0 to 3
    val countryId: String? = null, // Related country ID for unlocking atlas / "Did You Know" facts
    val choiceIds: List<String>? = null,
    val correctId: String? = null,
    val explanationKey: String? = null,
    val requiredLevel: Int = 1,
    val timerDuration: Int = 0,
    val dataVersion: Int = 1,
    val templateKey: String? = null
) {
    fun getLocalizedQuestionText(lang: String): String = when (lang) {
        "ar" -> questionTextAr
        "de" -> questionTextDe
        "fr" -> questionTextFr
        else -> questionTextEn
    }

    fun getLocalizedOptions(lang: String): List<String> = when (lang) {
        "ar" -> optionsAr
        "de" -> optionsDe
        "fr" -> optionsFr
        else -> optionsEn
    }
}
