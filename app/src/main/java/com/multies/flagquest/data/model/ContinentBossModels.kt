package com.multies.flagquest.data.model

enum class BossPhaseType {
    FLAG_MASTERY,
    MAP_MASTERY,
    CAPITAL_MASTERY,
    KNOWLEDGE_MASTERY,
    BORDERS_MASTERY,
    SPEED_FINALE
}

data class BossContinent(
    val bossId: String,
    val continentEn: String,
    val emoji: String,
    val themeColorHex: Long,
    val requiredDiscoveries: Int,
    val requiredStars: Int
)

data class BossPhaseDefinition(
    val phaseType: BossPhaseType,
    val titleKey: String,
    val descKey: String,
    val questionCount: Int,
    val timerSecondsPerQuestion: Int
)

data class BossQuestion(
    val id: String,
    val phaseType: BossPhaseType,
    val bossId: String,
    val countryId: String,
    val promptEn: String,
    val promptAr: String,
    val promptDe: String,
    val promptFr: String,
    val optionsEn: List<String>,
    val optionsAr: List<String>,
    val optionsDe: List<String>,
    val optionsFr: List<String>,
    val correctOptionIndex: Int,
    val visualType: String, // "FLAG", "MAP", "CAPITAL_BADGE", "FACT_CARD"
    val visualData: String,
    val explanationEn: String,
    val explanationAr: String,
    val explanationDe: String,
    val explanationFr: String
)

data class PhaseAttemptResult(
    val phaseType: BossPhaseType,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val scoreEarned: Int
)

data class BossSessionState(
    val boss: BossContinent,
    val phases: List<BossPhaseDefinition>,
    val currentPhaseIndex: Int = 0,
    val currentQuestionIndex: Int = 0,
    val currentPhaseQuestions: List<BossQuestion> = emptyList(),
    val remainingHearts: Int = 3,
    val score: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val phaseResults: List<PhaseAttemptResult> = emptyList(),
    val isPhaseCompleted: Boolean = false,
    val isBossVictory: Boolean = false,
    val isBossFailed: Boolean = false,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val isAnswerCorrect: Boolean = false,
    val secondsRemainingInQuestion: Int = 0
)

data class ContinentBossRecord(
    val bossId: String,
    val isUnlocked: Boolean,
    val unlockRequirementText: String,
    val isCompleted: Boolean,
    val starsEarned: Int,
    val highestScore: Int,
    val bestAccuracyPct: Int,
    val bestHeartsLeft: Int
)
