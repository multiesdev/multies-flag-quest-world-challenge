package com.multies.flagquest.data.model

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

object CountryRankingEngine {

    val STATS = mapOf(
        "US" to CountryRankingStats("US", "US", 9833517.0, 333000000L, 6190, 19924.0, 38.8951, -77.0364, false),
        "SA" to CountryRankingStats("SA", "SA", 2149690.0, 36400000L, 3000, 2640.0, 24.7136, 46.6753, false),
        "FR" to CountryRankingStats("FR", "FR", 643801.0, 68000000L, 4810, 4853.0, 48.8566, 2.3522, false),
        "DE" to CountryRankingStats("DE", "DE", 357022.0, 84300000L, 2962, 2389.0, 52.5200, 13.4050, false),
        "JP" to CountryRankingStats("JP", "JP", 377975.0, 124500000L, 3776, 29751.0, 35.6762, 139.6503, false),
        "BR" to CountryRankingStats("BR", "BR", 8515767.0, 215300000L, 2995, 7491.0, -15.7975, -47.8919, false),
        "EG" to CountryRankingStats("EG", "EG", 1002450.0, 111000000L, 2629, 2450.0, 30.0444, 31.2357, false),
        "AU" to CountryRankingStats("AU", "AU", 7692024.0, 26500000L, 2228, 25760.0, -35.2809, 149.1300, false),
        "CA" to CountryRankingStats("CA", "CA", 9984670.0, 38900000L, 5959, 202080.0, 45.4215, -75.6972, false),
        "ZA" to CountryRankingStats("ZA", "ZA", 1221037.0, 60400000L, 3450, 2798.0, -25.7479, 28.2293, false),
        "GB" to CountryRankingStats("GB", "GB", 242495.0, 67700000L, 1345, 12429.0, 51.5074, -0.1278, false),
        "IN" to CountryRankingStats("IN", "IN", 3287263.0, 1428000000L, 8586, 7000.0, 28.6139, 77.2090, false),
        "CN" to CountryRankingStats("CN", "CN", 9596960.0, 1411000000L, 8848, 14500.0, 39.9042, 116.4074, false),
        "RU" to CountryRankingStats("RU", "RU", 17098242.0, 144400000L, 5642, 37653.0, 55.7558, 37.6173, false),
        "TR" to CountryRankingStats("TR", "TR", 783562.0, 85300000L, 5137, 7200.0, 39.9334, 32.8597, false),
        "NZ" to CountryRankingStats("NZ", "NZ", 268021.0, 5200000L, 3724, 15134.0, -41.2865, 174.7762, false),
        "MX" to CountryRankingStats("MX", "MX", 1964375.0, 128500000L, 5636, 9330.0, 19.4326, -99.1332, false),
        "AR" to CountryRankingStats("AR", "AR", 2780400.0, 45800000L, 6961, 4989.0, -34.6037, -58.3816, false),
        "IT" to CountryRankingStats("IT", "IT", 301340.0, 58900000L, 4810, 7600.0, 41.9028, 12.4964, false),
        "ES" to CountryRankingStats("ES", "ES", 505990.0, 47500000L, 3718, 4964.0, 40.4168, -3.7038, false),
        "KR" to CountryRankingStats("KR", "KR", 100210.0, 51700000L, 1947, 2413.0, 37.5665, 126.9780, false),
        "KE" to CountryRankingStats("KE", "KE", 580367.0, 55000000L, 5199, 536.0, -1.2921, 36.8219, false),
        "NG" to CountryRankingStats("NG", "NG", 923768.0, 223800000L, 2419, 853.0, 9.0765, 7.3986, false),
        "ID" to CountryRankingStats("ID", "ID", 1904569.0, 277500000L, 4884, 54716.0, -6.2088, 106.8456, false),
        "CH" to CountryRankingStats("CH", "CH", 41285.0, 8800000L, 4634, 0.0, 46.9480, 7.4474, true),
        "SE" to CountryRankingStats("SE", "SE", 450295.0, 10500000L, 2096, 3218.0, 59.3293, 18.0686, false),
        "NO" to CountryRankingStats("NO", "NO", 385207.0, 5500000L, 2469, 25148.0, 59.9139, 10.7522, false),
        "FI" to CountryRankingStats("FI", "FI", 338145.0, 5600000L, 1324, 1250.0, 60.1699, 24.9384, false),
        "DZ" to CountryRankingStats("DZ", "DZ", 2381741.0, 45600000L, 2908, 998.0, 36.7538, 3.0588, false),
        "MA" to CountryRankingStats("MA", "MA", 446550.0, 37800000L, 4167, 1835.0, 34.0209, -6.8416, false),
        "IS" to CountryRankingStats("IS", "IS", 103000.0, 388000L, 2110, 4970.0, 64.1466, -21.9426, false),
        "FJ" to CountryRankingStats("FJ", "FJ", 18274.0, 930000L, 1324, 1129.0, -18.1416, 178.4419, false),
        "CO" to CountryRankingStats("CO", "CO", 1141748.0, 52000000L, 5700, 3208.0, 4.7110, -74.0721, false),
        "VN" to CountryRankingStats("VN", "VN", 331212.0, 98800000L, 3147, 3260.0, 21.0285, 105.8542, false),
        "CL" to CountryRankingStats("CL", "CL", 756102.0, 19600000L, 6893, 6435.0, -33.4489, -70.6693, false),
        "PH" to CountryRankingStats("PH", "PH", 300000.0, 117300000L, 2954, 36289.0, 14.5995, 120.9842, false)
    )

    fun formatValue(value: Double, criterion: RankingCriterion, lang: String): String {
        val locale = when (lang) {
            "ar" -> Locale("ar")
            "de" -> Locale.GERMANY
            "fr" -> Locale.FRANCE
            else -> Locale.US
        }
        val nf = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 1
        }

        return when (criterion) {
            RankingCriterion.TOTAL_AREA -> {
                val formatted = nf.format(value.toLong())
                when (lang) {
                    "ar" -> "$formatted كم²"
                    else -> "$formatted km²"
                }
            }
            RankingCriterion.POPULATION -> {
                val formatted = nf.format(value.toLong())
                when (lang) {
                    "ar" -> "$formatted نسمة"
                    "de" -> "$formatted Einwohner"
                    "fr" -> "$formatted habitants"
                    else -> "$formatted people"
                }
            }
            RankingCriterion.HIGHEST_ELEVATION -> {
                val formatted = nf.format(value.toInt())
                when (lang) {
                    "ar" -> "$formatted م"
                    else -> "$formatted m"
                }
            }
            RankingCriterion.COASTLINE_LENGTH -> {
                val formatted = nf.format(value.toLong())
                when (lang) {
                    "ar" -> "$formatted كم"
                    else -> "$formatted km"
                }
            }
            RankingCriterion.EQUATOR_DISTANCE -> {
                val formatted = nf.format(value)
                "$formatted°"
            }
            RankingCriterion.CAPITAL_LATITUDE -> {
                val deg = abs(value)
                val formatted = nf.format(deg)
                val dir = if (value >= 0) {
                    when (lang) {
                        "ar" -> "شمالاً"
                        else -> "N"
                    }
                } else {
                    when (lang) {
                        "ar" -> "جنوباً"
                        else -> "S"
                    }
                }
                "$formatted° $dir"
            }
            RankingCriterion.CAPITAL_LONGITUDE -> {
                val deg = abs(value)
                val formatted = nf.format(deg)
                val dir = if (value >= 0) {
                    when (lang) {
                        "ar" -> "شرقاً"
                        "de" -> "O"
                        else -> "E"
                    }
                } else {
                    when (lang) {
                        "ar" -> "غرباً"
                        "fr" -> "O"
                        else -> "W"
                    }
                }
                "$formatted° $dir"
            }
        }
    }

    fun generateLevelQuestions(
        levelIndex: Int,
        allCountries: List<Country>,
        lang: String = "en"
    ): List<RankingQuestion> {
        val countryMap = allCountries.associateBy { it.id }
        val availableCountryIds = STATS.keys.filter { countryMap.containsKey(it) }

        val (countriesPerRound, roundsPerLevel, criteriaPool) = when {
            levelIndex <= 5 -> Triple(
                3, 5, listOf(
                    RankingCriterion.TOTAL_AREA,
                    RankingCriterion.POPULATION,
                    RankingCriterion.HIGHEST_ELEVATION
                )
            )
            levelIndex <= 10 -> Triple(
                4, 6, listOf(
                    RankingCriterion.TOTAL_AREA,
                    RankingCriterion.POPULATION,
                    RankingCriterion.HIGHEST_ELEVATION,
                    RankingCriterion.COASTLINE_LENGTH
                )
            )
            levelIndex <= 20 -> Triple(
                5, 8, RankingCriterion.entries.toList()
            )
            else -> Triple(
                6, 10, RankingCriterion.entries.toList()
            )
        }

        val questions = mutableListOf<RankingQuestion>()
        var roundSeed = levelIndex * 1000 + 777

        for (r in 0 until roundsPerLevel) {
            val random = Random(roundSeed++)
            var attempts = 0
            var validQuestion: RankingQuestion? = null

            while (attempts < 100 && validQuestion == null) {
                attempts++
                val criterion = criteriaPool[random.nextInt(criteriaPool.size)]

                // Filter candidates
                val eligibleIds = availableCountryIds.filter { id ->
                    val stat = STATS[id] ?: return@filter false
                    if (criterion == RankingCriterion.COASTLINE_LENGTH && stat.isLandlocked) {
                        return@filter false
                    }
                    true
                }

                if (eligibleIds.size < countriesPerRound) continue

                val selectedIds = eligibleIds.shuffled(random).take(countriesPerRound)
                val selectedCountries = selectedIds.mapNotNull { countryMap[it] }
                if (selectedCountries.size < countriesPerRound) continue

                val statsMap = selectedIds.associateWith { id ->
                    STATS[id]!!.getValueForCriterion(criterion)
                }

                // Check exact values tie
                val values = statsMap.values.toList()
                if (values.distinct().size < values.size) continue

                // Check formatted values tie
                val formattedMap = selectedIds.associateWith { id ->
                    formatValue(statsMap[id]!!, criterion, lang)
                }
                val formattedList = formattedMap.values.toList()
                if (formattedList.distinct().size < formattedList.size) continue

                // Calculate exact correct order
                val correctOrder = if (criterion.isAscending) {
                    selectedCountries.sortedBy { statsMap[it.id]!! }
                } else {
                    selectedCountries.sortedByDescending { statsMap[it.id]!! }
                }

                // Shuffle displayed cards
                val displayedCards = selectedCountries.shuffled(random)

                validQuestion = RankingQuestion(
                    roundIndex = r + 1,
                    criterion = criterion,
                    populationReferenceYear = 2026,
                    displayedCountries = displayedCards,
                    correctCountryOrder = correctOrder,
                    statsMap = statsMap,
                    formattedValuesMap = formattedMap
                )
            }

            if (validQuestion != null) {
                questions.add(validQuestion)
            }
        }

        return questions
    }

    /**
     * Evaluate round performance:
     * Compares user country ID sequence vs correct country ID sequence.
     */
    fun evaluateRound(
        userOrderIds: List<String>,
        correctOrderIds: List<String>
    ): RoundResult {
        val n = correctOrderIds.size
        var exactMatches = 0
        for (i in 0 until n) {
            if (i < userOrderIds.size && userOrderIds[i] == correctOrderIds[i]) {
                exactMatches++
            }
        }

        val totalPairs = n * (n - 1) / 2
        var correctPairs = 0
        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val correctA = correctOrderIds[i]
                val correctB = correctOrderIds[j]

                val userIdxA = userOrderIds.indexOf(correctA)
                val userIdxB = userOrderIds.indexOf(correctB)

                if (userIdxA != -1 && userIdxB != -1 && userIdxA < userIdxB) {
                    correctPairs++
                }
            }
        }

        val isFullOrder = exactMatches == n
        val pairAccuracy = if (totalPairs > 0) (correctPairs.toDouble() / totalPairs) else 1.0
        val baseScore = (correctPairs * 100) + (exactMatches * 50) + (if (isFullOrder) 200 else 0)

        return RoundResult(
            isFullOrder = isFullOrder,
            exactMatches = exactMatches,
            correctPairs = correctPairs,
            totalPairs = totalPairs,
            pairAccuracy = pairAccuracy,
            scorePoints = baseScore
        )
    }

    data class RoundResult(
        val isFullOrder: Boolean,
        val exactMatches: Int,
        val correctPairs: Int,
        val totalPairs: Int,
        val pairAccuracy: Double,
        val scorePoints: Int
    )
}
