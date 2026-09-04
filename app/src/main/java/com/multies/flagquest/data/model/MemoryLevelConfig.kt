package com.multies.flagquest.data.model

data class MemoryLevelConfig(
    val levelIndex: Int,
    val cardCount: Int,
    val targetTimeSec: Int,
    val previewDurationMs: Long = 0L,
    val isVisuallySimilar: Boolean = false,
    val themeKey: String = "world_mix"
) {
    val pairCount: Int get() = cardCount / 2

    companion object {
        val DEFAULT_LEVELS = listOf(
            MemoryLevelConfig(levelIndex = 1, cardCount = 6, targetTimeSec = 15, previewDurationMs = 1500L, isVisuallySimilar = false),
            MemoryLevelConfig(levelIndex = 2, cardCount = 10, targetTimeSec = 25, previewDurationMs = 1200L, isVisuallySimilar = false),
            MemoryLevelConfig(levelIndex = 3, cardCount = 14, targetTimeSec = 35, previewDurationMs = 0L, isVisuallySimilar = false),
            MemoryLevelConfig(levelIndex = 4, cardCount = 16, targetTimeSec = 40, previewDurationMs = 0L, isVisuallySimilar = false),
            MemoryLevelConfig(levelIndex = 5, cardCount = 20, targetTimeSec = 50, previewDurationMs = 0L, isVisuallySimilar = false),
            MemoryLevelConfig(levelIndex = 6, cardCount = 20, targetTimeSec = 50, previewDurationMs = 0L, isVisuallySimilar = true),
            MemoryLevelConfig(levelIndex = 7, cardCount = 24, targetTimeSec = 60, previewDurationMs = 0L, isVisuallySimilar = false),
            MemoryLevelConfig(levelIndex = 8, cardCount = 24, targetTimeSec = 60, previewDurationMs = 0L, isVisuallySimilar = true),
            MemoryLevelConfig(levelIndex = 9, cardCount = 28, targetTimeSec = 70, previewDurationMs = 0L, isVisuallySimilar = true),
            MemoryLevelConfig(levelIndex = 10, cardCount = 30, targetTimeSec = 80, previewDurationMs = 0L, isVisuallySimilar = true)
        )

        fun getConfig(levelIndex: Int): MemoryLevelConfig {
            return DEFAULT_LEVELS.find { it.levelIndex == levelIndex } ?: run {
                val clampedLevel = levelIndex.coerceIn(1, 50)
                val calculatedCards = when {
                    clampedLevel <= 5 -> 6
                    clampedLevel <= 10 -> 10
                    clampedLevel <= 15 -> 12
                    clampedLevel <= 20 -> 16
                    clampedLevel <= 25 -> 20
                    clampedLevel <= 30 -> 24
                    clampedLevel <= 40 -> 28
                    else -> 30
                }
                MemoryLevelConfig(
                    levelIndex = clampedLevel,
                    cardCount = calculatedCards,
                    targetTimeSec = 40 + clampedLevel * 3,
                    previewDurationMs = if (clampedLevel <= 15) 1000L else 0L,
                    isVisuallySimilar = clampedLevel >= 6
                )
            }
        }
    }
}
