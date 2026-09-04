package com.multies.flagquest.data.model

enum class FakeMutationType {
    INCORRECT_COLOR,
    SWAP_STRIPE_COLORS,
    MISSING_SYMBOL,
    EXTRA_SYMBOL,
    INCORRECT_STAR_COUNT,
    INCORRECT_STRIPE_ORIENTATION,
    INCORRECT_SYMBOL_POSITION,
    MIRRORED_ASYMMETRIC_SYMBOL,
    INCORRECT_FIELD_OR_BORDER,
    SUBTLE_PROPORTION_CHANGE
}

data class FakeFlagMutation(
    val type: FakeMutationType,
    val difficulty: Int = 1,
    val targetColor: String? = null,
    val replacementColor: String? = null,
    val swappedStripeIndices: Pair<Int, Int>? = null,
    val symbolName: String? = null,
    val starCountActual: Int? = null,
    val starCountFake: Int? = null,
    val originalOrientation: String? = null,
    val fakeOrientation: String? = null,
    val originalPosition: String? = null,
    val fakePosition: String? = null,
    val borderOriginalColor: String? = null,
    val borderFakeColor: String? = null,
    val stripeName: String? = null
)

enum class GameFormat {
    FORMAT_A, // 4 versions of the same country's flag (1 fake, 3 authentic)
    FORMAT_B, // 4 different country flags (1 contains an alteration)
    FORMAT_C  // 1 large flag (Player decides Authentic or Fake)
}

data class SpotTheFakeOption(
    val id: String,
    val country: Country,
    val isFake: Boolean,
    val mutation: FakeFlagMutation? = null
)

data class SpotTheFakeQuestion(
    val roundIndex: Int,
    val format: GameFormat,
    val targetCountry: Country,
    val options: List<SpotTheFakeOption>,
    val fakeOptionIndex: Int, // Index of the fake option in options list
    val explanationEn: String,
    val explanationAr: String,
    val explanationDe: String,
    val explanationFr: String
)

data class SpotTheFakeLevelConfig(
    val levelIndex: Int,
    val titleKey: String,
    val roundsCount: Int,
    val timerSecPerRound: Int,
    val allowedFormats: List<GameFormat>,
    val targetCountries: List<String>,
    val allowedMutations: List<FakeMutationType>,
    val descriptionEn: String,
    val descriptionAr: String,
    val descriptionDe: String,
    val descriptionFr: String
)

data class SpotTheFakeScoreResult(
    val levelIndex: Int,
    val score: Int,
    val correctAnswers: Int,
    val totalRounds: Int,
    val stars: Int,
    val coinsEarned: Int,
    val maxStreak: Int,
    val accuracyPercent: Int,
    val timeTakenSec: Long
)
