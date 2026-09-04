package com.multies.flagquest.features.whoami

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.multies.flagquest.audio.AudioManager
import com.multies.flagquest.audio.HapticHelper
import com.multies.flagquest.audio.SoundType
import com.multies.flagquest.data.local.GameDatabase
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.repository.GameRepository
import com.multies.flagquest.features.whoami.engine.WhoAmIEngine
import com.multies.flagquest.features.whoami.engine.WhoAmIQuestion
import com.multies.flagquest.features.whoami.engine.WhoAmIScoreResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WhoAmIQuestionState {
    AWAITING_ANSWER,
    SHOWING_FEEDBACK,
    ADVANCING,
    COMPLETED
}

data class WhoAmIUiState(
    val questions: List<WhoAmIQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val revealedCluesCount: Int = 1,
    val cluesRevealedPerQuestion: List<Int> = emptyList(),
    val questionState: WhoAmIQuestionState = WhoAmIQuestionState.AWAITING_ANSWER,
    val currentScore: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val correctAnswersCount: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isInputLocked: Boolean = false,
    val elapsedTimeSec: Long = 0L,
    val isPaused: Boolean = false,
    val resultSummary: WhoAmIScoreResult? = null,
    val showVictoryDialog: Boolean = false
)

class WhoAmIViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GameDatabase.getDatabase(application)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val repository = GameRepository(application, db.gameDao(), moshi)

    private val _uiState = MutableStateFlow(WhoAmIUiState())
    val uiState: StateFlow<WhoAmIUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var feedbackJob: Job? = null

    private val CORRECT_FEEDBACK_DURATION_MS = 1800L
    private val INCORRECT_FEEDBACK_DURATION_MS = 2500L

    fun startSession(
        levelIndex: Int,
        countries: List<Country>,
        lang: String,
        existingHighScore: Int
    ) {
        if (countries.isEmpty()) return

        feedbackJob?.cancel()

        val newQuestions = WhoAmIEngine.createQuestionsForLevel(levelIndex, countries, lang)

        _uiState.value = WhoAmIUiState(
            questions = newQuestions,
            currentQuestionIndex = 0,
            revealedCluesCount = 1,
            cluesRevealedPerQuestion = emptyList(),
            questionState = WhoAmIQuestionState.AWAITING_ANSWER,
            currentScore = 0,
            currentStreak = 0,
            maxStreak = 0,
            correctAnswersCount = 0,
            selectedOptionIndex = null,
            isInputLocked = false,
            elapsedTimeSec = 0L,
            isPaused = false,
            resultSummary = null,
            showVictoryDialog = false
        )

        startTimerLoop()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _uiState.value
                if (state.questions.isNotEmpty() &&
                    !state.isPaused &&
                    state.questionState == WhoAmIQuestionState.AWAITING_ANSWER &&
                    !state.showVictoryDialog
                ) {
                    _uiState.update { it.copy(elapsedTimeSec = it.elapsedTimeSec + 1) }
                }
            }
        }
    }

    fun selectOption(index: Int, levelIndex: Int, existingHighScore: Int) {
        val state = _uiState.value
        val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex)
        if (state.questionState != WhoAmIQuestionState.AWAITING_ANSWER ||
            state.isInputLocked ||
            state.isPaused ||
            currentQuestion == null
        ) {
            return
        }

        val activeQuestionId = currentQuestion.id
        val newCluesRevealed = state.cluesRevealedPerQuestion + state.revealedCluesCount
        val isCorrect = index == currentQuestion.correctIndex

        var newCorrectCount = state.correctAnswersCount
        var newStreak = state.currentStreak
        var newMaxStreak = state.maxStreak
        var newScore = state.currentScore

        if (isCorrect) {
            AudioManager.playSoundEffect(SoundType.CORRECT)
            HapticHelper.triggerCorrect(getApplication())

            newCorrectCount++
            newStreak++
            if (newStreak > newMaxStreak) {
                newMaxStreak = newStreak
            }

            val pointsForClueLevel = when (state.revealedCluesCount) {
                1 -> 500
                2 -> 400
                3 -> 300
                4 -> 200
                else -> 100
            }
            newScore += pointsForClueLevel + (newStreak * 20)
        } else {
            AudioManager.playSoundEffect(SoundType.WRONG)
            HapticHelper.triggerWrong(getApplication())

            newStreak = 0
        }

        _uiState.update {
            it.copy(
                isInputLocked = true,
                selectedOptionIndex = index,
                questionState = WhoAmIQuestionState.SHOWING_FEEDBACK,
                cluesRevealedPerQuestion = newCluesRevealed,
                correctAnswersCount = newCorrectCount,
                currentStreak = newStreak,
                maxStreak = newMaxStreak,
                currentScore = newScore
            )
        }

        val feedbackDelayMs = if (isCorrect) CORRECT_FEEDBACK_DURATION_MS else INCORRECT_FEEDBACK_DURATION_MS

        feedbackJob?.cancel()
        feedbackJob = viewModelScope.launch {
            delay(feedbackDelayMs)

            val currentState = _uiState.value
            val activeQuestion = currentState.questions.getOrNull(currentState.currentQuestionIndex)
            if (activeQuestion?.id == activeQuestionId) {
                if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
                    // Next Question
                    _uiState.update {
                        it.copy(
                            questionState = WhoAmIQuestionState.AWAITING_ANSWER,
                            currentQuestionIndex = currentState.currentQuestionIndex + 1,
                            revealedCluesCount = 1,
                            selectedOptionIndex = null,
                            isInputLocked = false
                        )
                    }
                } else {
                    // Completed Level!
                    timerJob?.cancel()
                    AudioManager.playSoundEffect(SoundType.MILESTONE)
                    HapticHelper.triggerMilestone(getApplication())

                    val isNewBest = existingHighScore == 0 || currentState.currentScore > existingHighScore

                    val scoreResult = WhoAmIEngine.calculateScore(
                        levelIndex = levelIndex,
                        totalQuestions = currentState.questions.size,
                        correctCount = currentState.correctAnswersCount,
                        cluesRevealedPerQuestion = currentState.cluesRevealedPerQuestion,
                        elapsedTimeSec = currentState.elapsedTimeSec,
                        bestEarlyStreak = currentState.maxStreak,
                        isNewRecord = isNewBest
                    )

                    // Save Progress directly to repository
                    repository.completeLevel(
                        categoryId = "WHO_AM_I",
                        levelIndex = levelIndex,
                        stars = scoreResult.starsEarned,
                        score = scoreResult.finalScore
                    )
                    if (scoreResult.coinsEarned > 0) {
                        repository.updateCoins(scoreResult.coinsEarned)
                    }

                    _uiState.update {
                        it.copy(
                            questionState = WhoAmIQuestionState.COMPLETED,
                            resultSummary = scoreResult,
                            showVictoryDialog = true
                        )
                    }
                }
            }
        }
    }

    fun revealNextClue() {
        val state = _uiState.value
        val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex)
        if (state.isInputLocked || state.isPaused || currentQuestion == null) return

        if (state.revealedCluesCount < currentQuestion.clues.size) {
            AudioManager.playSoundEffect(SoundType.TICK)
            HapticHelper.triggerTick(getApplication())
            _uiState.update {
                it.copy(revealedCluesCount = it.revealedCluesCount + 1)
            }
        }
    }

    fun setPaused(paused: Boolean) {
        _uiState.update { it.copy(isPaused = paused) }
    }

    fun dismissVictoryDialog() {
        _uiState.update { it.copy(showVictoryDialog = false) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        feedbackJob?.cancel()
    }
}
