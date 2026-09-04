package com.multies.flagquest.data.model

import com.multies.flagquest.data.local.entity.MemoryLevelEntity
import kotlin.math.max

enum class MemoryCardState {
    FACE_DOWN,
    REVEALED,
    MATCHED
}

data class MemoryCard(
    val id: String,          // Unique card identifier, e.g., "US_card_1"
    val countryId: String,   // Country code, e.g., "US"
    val flagEmoji: String,   // Flag emoji representation
    val countryName: String, // Localized name
    val state: MemoryCardState = MemoryCardState.FACE_DOWN
)

data class MemoryScoreResult(
    val finalScore: Int,
    val starsEarned: Int,
    val isNewRecord: Boolean,
    val timeBonus: Int,
    val difficultyBonus: Int,
    val newRecordBonus: Int,
    val movePenalty: Int,
    val mismatchPenalty: Int
)

object MemoryGameEngine {

    // Known visually tricky pairs for higher levels
    private val VISUALLY_SIMILAR_PAIRS = listOf(
        Pair("RO", "TD"), // Romania & Chad
        Pair("ID", "MC"), // Indonesia & Monaco
        Pair("IE", "CI"), // Ireland & Côte d'Ivoire
        Pair("NL", "LU"), // Netherlands & Luxembourg
        Pair("AU", "NZ"), // Australia & New Zealand
        Pair("CO", "EC"), // Colombia & Ecuador
        Pair("CO", "VE"), // Colombia & Venezuela
        Pair("SE", "NO"), // Sweden & Norway
        Pair("FI", "DK"), // Finland & Denmark
        Pair("AE", "PS")  // UAE & Palestine
    )

    fun createBoard(
        levelConfig: MemoryLevelConfig,
        allCountries: List<Country>,
        lang: String,
        lastFlagIds: String = ""
    ): List<MemoryCard> {
        val pairCount = levelConfig.pairCount
        val lastUsedSet = lastFlagIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

        // Filter valid countries that have valid flags and names
        val validCountries = allCountries.filter { it.id.isNotEmpty() && it.flagEmoji.isNotEmpty() }
        val countryMap = validCountries.associateBy { it.id }

        val selectedCountries = mutableListOf<Country>()

        // 1. If visually similar is enabled, attempt to include 1-3 tricky pairs
        if (levelConfig.isVisuallySimilar) {
            val suitablePairs = VISUALLY_SIMILAR_PAIRS.filter { (id1, id2) ->
                countryMap.containsKey(id1) && countryMap.containsKey(id2)
            }.shuffled()

            for (pair in suitablePairs) {
                if (selectedCountries.size + 2 <= pairCount) {
                    val c1 = countryMap[pair.first]
                    val c2 = countryMap[pair.second]
                    if (c1 != null && c2 != null && !selectedCountries.contains(c1) && !selectedCountries.contains(c2)) {
                        selectedCountries.add(c1)
                        selectedCountries.add(c2)
                    }
                }
            }
        }

        // 2. Select remaining countries, preferring ones not in lastUsedSet for variety
        val remainingNeeded = pairCount - selectedCountries.size
        if (remainingNeeded > 0) {
            val unusedCandidates = validCountries
                .filter { c -> !selectedCountries.contains(c) && !lastUsedSet.contains(c.id) }
                .shuffled()

            val addedFromUnused = unusedCandidates.take(remainingNeeded)
            selectedCountries.addAll(addedFromUnused)

            // If still need more (e.g. not enough unused candidates)
            val stillNeeded = pairCount - selectedCountries.size
            if (stillNeeded > 0) {
                val fallbackCandidates = validCountries
                    .filter { c -> !selectedCountries.contains(c) }
                    .shuffled()
                selectedCountries.addAll(fallbackCandidates.take(stillNeeded))
            }
        }

        // 3. Generate exactly two cards for each selected country flag
        val cards = mutableListOf<MemoryCard>()
        selectedCountries.forEach { country ->
            val name = country.getLocalizedName(lang)
            cards.add(MemoryCard(id = "${country.id}_1", countryId = country.id, flagEmoji = country.flagEmoji, countryName = name))
            cards.add(MemoryCard(id = "${country.id}_2", countryId = country.id, flagEmoji = country.flagEmoji, countryName = name))
        }

        // 4. Shuffle cards thoroughly
        return cards.shuffled()
    }

    fun isBetterRecord(
        newTimeSec: Long,
        newMoves: Int,
        newMismatches: Int,
        existingRecord: MemoryLevelEntity?
    ): Boolean {
        if (existingRecord == null || !existingRecord.isCompleted || existingRecord.bestCompletionTimeSec == 0L) {
            return true
        }
        if (newTimeSec < existingRecord.bestCompletionTimeSec) return true
        if (newTimeSec > existingRecord.bestCompletionTimeSec) return false

        // Time tie-breaker: moves
        if (newMoves < existingRecord.bestMoves) return true
        if (newMoves > existingRecord.bestMoves) return false

        // Moves tie-breaker: mismatches
        return newMismatches < existingRecord.bestMismatches
    }

    fun calculateStars(
        elapsedTimeSec: Long,
        movesCount: Int,
        pairCount: Int,
        targetTimeSec: Int
    ): Int {
        val perfectMoves = pairCount
        val extraMoves = max(0, movesCount - perfectMoves)

        val meetsThreeStarTime = elapsedTimeSec <= targetTimeSec
        val meetsThreeStarMoves = extraMoves <= (pairCount / 2).coerceAtLeast(1)

        if (meetsThreeStarTime && meetsThreeStarMoves) {
            return 3
        }

        val meetsTwoStarTime = elapsedTimeSec <= (targetTimeSec * 1.5).toLong()
        val meetsTwoStarMoves = extraMoves <= pairCount

        if (meetsTwoStarTime && meetsTwoStarMoves) {
            return 2
        }

        return 1
    }

    fun calculateScore(
        levelIndex: Int,
        pairCount: Int,
        targetTimeSec: Int,
        elapsedTimeSec: Long,
        movesCount: Int,
        mismatchesCount: Int,
        isNewRecord: Boolean
    ): MemoryScoreResult {
        val baseScore = pairCount * 100
        val timeBonus = (max(0L, targetTimeSec - elapsedTimeSec) * 15).toInt()
        val perfectMoves = pairCount
        val extraMoves = max(0, movesCount - perfectMoves)
        val movePenalty = extraMoves * 10
        val mismatchPenalty = mismatchesCount * 15
        val difficultyBonus = levelIndex * 50
        val newRecordBonus = if (isNewRecord) 100 else 0

        val rawScore = (baseScore + timeBonus + difficultyBonus + newRecordBonus) - movePenalty - mismatchPenalty
        val finalScore = max(50, rawScore)
        val starsEarned = calculateStars(elapsedTimeSec, movesCount, pairCount, targetTimeSec)

        return MemoryScoreResult(
            finalScore = finalScore,
            starsEarned = starsEarned,
            isNewRecord = isNewRecord,
            timeBonus = timeBonus,
            difficultyBonus = difficultyBonus,
            newRecordBonus = newRecordBonus,
            movePenalty = movePenalty,
            mismatchPenalty = mismatchPenalty
        )
    }

    fun getRecommendedGridColumns(cardCount: Int): Int {
        return when (cardCount) {
            6 -> 3
            10 -> 2
            14 -> 3
            16 -> 4
            20 -> 4
            24 -> 4
            28 -> 4
            30 -> 5
            else -> 4
        }
    }
}
