package com.multies.flagquest.data.model

import com.multies.flagquest.data.repository.CountryMapRepository
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

enum class SilentMapFormat {
    SILHOUETTE,
    CONTINENT_CONTEXT,
    NEIGHBORS_CONTEXT,
    ROTATED_SILHOUETTE,
    ISLAND_ZOOM,
    REGIONAL_CONTEXT
}

data class SilentMapQuestion(
    val id: String,
    val targetCountry: Country,
    val options: List<Country>,
    val correctIndex: Int,
    val format: SilentMapFormat,
    val rotationDegrees: Float = 0f
)

data class SilentMapLevelConfig(
    val levelIndex: Int,
    val questionCount: Int,
    val timePerQuestionSec: Int,
    val allowedFormats: List<SilentMapFormat>,
    val targetCountriesPoolCategory: String // "FAMOUS", "CONTINENT", "REGIONAL", "MIXED"
) {
    companion object {
        fun getConfig(levelIndex: Int): SilentMapLevelConfig {
            val validLevel = levelIndex.coerceIn(1, 30)
            return when {
                validLevel <= 5 -> SilentMapLevelConfig(
                    levelIndex = validLevel,
                    questionCount = 5,
                    timePerQuestionSec = 30,
                    allowedFormats = listOf(SilentMapFormat.SILHOUETTE, SilentMapFormat.CONTINENT_CONTEXT),
                    targetCountriesPoolCategory = "FAMOUS"
                )
                validLevel <= 10 -> SilentMapLevelConfig(
                    levelIndex = validLevel,
                    questionCount = 7,
                    timePerQuestionSec = 25,
                    allowedFormats = listOf(
                        SilentMapFormat.SILHOUETTE,
                        SilentMapFormat.CONTINENT_CONTEXT,
                        SilentMapFormat.NEIGHBORS_CONTEXT
                    ),
                    targetCountriesPoolCategory = "CONTINENT"
                )
                validLevel <= 20 -> SilentMapLevelConfig(
                    levelIndex = validLevel,
                    questionCount = 8,
                    timePerQuestionSec = 20,
                    allowedFormats = listOf(
                        SilentMapFormat.SILHOUETTE,
                        SilentMapFormat.CONTINENT_CONTEXT,
                        SilentMapFormat.NEIGHBORS_CONTEXT,
                        SilentMapFormat.ROTATED_SILHOUETTE,
                        SilentMapFormat.ISLAND_ZOOM
                    ),
                    targetCountriesPoolCategory = "REGIONAL"
                )
                else -> SilentMapLevelConfig(
                    levelIndex = validLevel,
                    questionCount = 10,
                    timePerQuestionSec = 15,
                    allowedFormats = SilentMapFormat.values().toList(),
                    targetCountriesPoolCategory = "MIXED"
                )
            }
        }
    }
}

data class SilentMapScoreResult(
    val finalScore: Int,
    val accuracyPercentage: Int,
    val starsEarned: Int,
    val isNewRecord: Boolean,
    val streakMax: Int,
    val timeBonus: Int,
    val coinsEarned: Int
)

object SilentMapEngine {

    // List of famous ISO country codes for early level pools
    private val famousCountryCodes = setOf(
        "US", "CA", "MX", "BR", "AR", "FR", "DE", "IT", "ES", "GB",
        "RU", "CN", "IN", "JP", "KR", "AU", "EG", "ZA", "SA", "TR"
    )

    fun createQuestionsForLevel(
        levelIndex: Int,
        allCountries: List<Country>,
        lang: String,
        seedModifier: Long = System.currentTimeMillis()
    ): List<SilentMapQuestion> {
        // Exclude any country without a verified real geographic map asset
        val mapCountries = allCountries.filter { CountryMapRepository.hasMapAsset(it.id) }
        if (mapCountries.size < 4) return emptyList()

        val config = SilentMapLevelConfig.getConfig(levelIndex)
        val rng = Random(levelIndex * 1000L + seedModifier % 10000L)

        // Select pool of target countries
        val targetPool = when (config.targetCountriesPoolCategory) {
            "FAMOUS" -> {
                val famousList = mapCountries.filter { it.id in famousCountryCodes }
                if (famousList.size >= config.questionCount) famousList else mapCountries
            }
            "CONTINENT" -> {
                val continents = listOf("Europe", "Asia", "Africa", "North America", "South America", "Oceania")
                val targetContinent = continents[(levelIndex - 1) % continents.size]
                val continentCountries = mapCountries.filter { it.continentEn.equals(targetContinent, ignoreCase = true) }
                if (continentCountries.size >= config.questionCount) continentCountries else mapCountries
            }
            "REGIONAL" -> {
                mapCountries.sortedByDescending { it.areaSqKm }.take(120)
            }
            else -> mapCountries
        }

        val shuffledTargets = targetPool.shuffled(rng).take(config.questionCount)
        val questions = mutableListOf<SilentMapQuestion>()

        shuffledTargets.forEachIndexed { qIdx, targetCountry ->
            // Choose 3 distractors from same continent/subregion or similar area
            val sameContinentOthers = mapCountries.filter {
                it.id != targetCountry.id && it.continentEn.equals(targetCountry.continentEn, ignoreCase = true)
            }

            val otherPool = if (sameContinentOthers.size >= 3) sameContinentOthers else mapCountries.filter { it.id != targetCountry.id }
            val distractors = otherPool.shuffled(rng).take(3)

            val optionsList = (distractors + targetCountry).shuffled(rng)
            val correctIndex = optionsList.indexOfFirst { it.id == targetCountry.id }

            // Select Format
            val selectedFormat = config.allowedFormats[qIdx % config.allowedFormats.size]
            val rotationDegrees = if (selectedFormat == SilentMapFormat.ROTATED_SILHOUETTE) {
                listOf(45f, 90f, 180f, 270f, 315f).shuffled(rng).first()
            } else 0f

            questions.add(
                SilentMapQuestion(
                    id = "q_${levelIndex}_${qIdx}_${targetCountry.id}",
                    targetCountry = targetCountry,
                    options = optionsList,
                    correctIndex = correctIndex,
                    format = selectedFormat,
                    rotationDegrees = rotationDegrees
                )
            )
        }

        return questions
    }

    fun calculateScore(
        levelIndex: Int,
        questionCount: Int,
        correctCount: Int,
        elapsedTimeSec: Long,
        maxStreak: Int,
        hintsUsedCount: Int,
        isNewRecord: Boolean
    ): SilentMapScoreResult {
        val baseScore = correctCount * 100
        val streakBonus = maxStreak * 25
        val targetTotalTimeSec = questionCount * 20L
        val speedBonus = max(0, ((targetTotalTimeSec - elapsedTimeSec) * 5).toInt())
        val hintPenalty = hintsUsedCount * 15

        val totalScore = max(0, baseScore + streakBonus + speedBonus - hintPenalty)
        val accuracy = if (questionCount > 0) ((correctCount.toDouble() / questionCount) * 100).roundToInt() else 0

        val stars = when {
            accuracy >= 85 -> 3
            accuracy >= 65 -> 2
            correctCount > 0 -> 1
            else -> 0
        }

        val coins = when {
            stars == 3 && isNewRecord -> 50
            stars >= 1 -> 20
            else -> 5
        }

        return SilentMapScoreResult(
            finalScore = totalScore,
            accuracyPercentage = accuracy,
            starsEarned = stars,
            isNewRecord = isNewRecord,
            streakMax = maxStreak,
            timeBonus = speedBonus,
            coinsEarned = coins
        )
    }
}
