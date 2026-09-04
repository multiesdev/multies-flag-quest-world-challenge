package com.multies.flagquest.features.whoami.engine

data class WhoAmILevelConfig(
    val levelIndex: Int,
    val questionCount: Int,
    val poolType: String, // "FAMOUS", "CONTINENT_SAME", "REGIONAL_NEIGHBORS", "HARD"
    val maxCluesPerQuestion: Int,
    val timeTargetSec: Int
) {
    companion object {
        const val TOTAL_LEVELS = 50

        fun getConfig(levelIndex: Int): WhoAmILevelConfig {
            val clampedLevel = levelIndex.coerceIn(1, 50)
            return when (clampedLevel) {
                in 1..5 -> WhoAmILevelConfig(
                    levelIndex = clampedLevel,
                    questionCount = 5,
                    poolType = "FAMOUS",
                    maxCluesPerQuestion = 5,
                    timeTargetSec = 60
                )
                in 6..10 -> WhoAmILevelConfig(
                    levelIndex = clampedLevel,
                    questionCount = 6,
                    poolType = "CONTINENT_SAME",
                    maxCluesPerQuestion = 5,
                    timeTargetSec = 50
                )
                in 11..20 -> WhoAmILevelConfig(
                    levelIndex = clampedLevel,
                    questionCount = 8,
                    poolType = "REGIONAL_NEIGHBORS",
                    maxCluesPerQuestion = 4,
                    timeTargetSec = 45
                )
                else -> WhoAmILevelConfig(
                    levelIndex = clampedLevel,
                    questionCount = 10,
                    poolType = "HARD",
                    maxCluesPerQuestion = 4,
                    timeTargetSec = 40
                )
            }
        }

        fun getConfigForLevel(levelIndex: Int): WhoAmILevelConfig = getConfig(levelIndex)
    }
}
