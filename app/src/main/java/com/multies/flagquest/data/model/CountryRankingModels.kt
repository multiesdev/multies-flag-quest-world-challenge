package com.multies.flagquest.data.model

import kotlin.math.abs

enum class RankingCriterion(
    val id: String,
    val locKey: String,
    val isAscending: Boolean
) {
    TOTAL_AREA("TOTAL_AREA", "criterion_total_area", false),
    POPULATION("POPULATION", "criterion_population", false),
    HIGHEST_ELEVATION("HIGHEST_ELEVATION", "criterion_highest_elevation", false),
    COASTLINE_LENGTH("COASTLINE_LENGTH", "criterion_coastline_length", false),
    EQUATOR_DISTANCE("EQUATOR_DISTANCE", "criterion_equator_distance", true),
    CAPITAL_LATITUDE("CAPITAL_LATITUDE", "criterion_capital_latitude", false),
    CAPITAL_LONGITUDE("CAPITAL_LONGITUDE", "criterion_capital_longitude", true);

    companion object {
        fun fromId(id: String): RankingCriterion = entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: TOTAL_AREA
    }
}

data class CountryRankingStats(
    val countryId: String,
    val isoAlpha2: String,
    val areaSqKm: Double,
    val population: Long,
    val highestElevationMeters: Int,
    val coastlineKm: Double,
    val capitalLat: Double,
    val capitalLon: Double,
    val isLandlocked: Boolean = false
) {
    fun getValueForCriterion(criterion: RankingCriterion): Double {
        return when (criterion) {
            RankingCriterion.TOTAL_AREA -> areaSqKm
            RankingCriterion.POPULATION -> population.toDouble()
            RankingCriterion.HIGHEST_ELEVATION -> highestElevationMeters.toDouble()
            RankingCriterion.COASTLINE_LENGTH -> coastlineKm
            RankingCriterion.EQUATOR_DISTANCE -> abs(capitalLat)
            RankingCriterion.CAPITAL_LATITUDE -> capitalLat
            RankingCriterion.CAPITAL_LONGITUDE -> capitalLon
        }
    }
}

data class RankingQuestion(
    val roundIndex: Int,
    val criterion: RankingCriterion,
    val populationReferenceYear: Int = 2026,
    val displayedCountries: List<Country>,
    val correctCountryOrder: List<Country>,
    val statsMap: Map<String, Double>,
    val formattedValuesMap: Map<String, String>
)

data class RankingScoreResult(
    val finalScore: Int,
    val starsEarned: Int,
    val totalRounds: Int,
    val perfectRounds: Int,
    val accuracyPercentage: Int,
    val totalMovesTaken: Int,
    val elapsedTimeSec: Long,
    val coinsEarned: Int,
    val isNewRecord: Boolean
)
