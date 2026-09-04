package com.multies.flagquest.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.multies.flagquest.data.local.GameDatabase
import com.multies.flagquest.data.model.BossContinent
import com.multies.flagquest.data.model.BossPhaseType
import com.multies.flagquest.data.model.BossQuestion
import com.multies.flagquest.data.model.BossSessionState
import com.multies.flagquest.data.model.ContinentBossEngine
import com.multies.flagquest.data.model.ContinentBossRecord
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.PhaseAttemptResult
import com.multies.flagquest.data.repository.ContinentBossRepository
import com.multies.flagquest.data.repository.GameRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ContinentBossViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GameDatabase.getDatabase(application)
    private val moshi = Moshi.Builder().build()
    private val repository = GameRepository(application, db.gameDao(), moshi)
    private val bossRepository = ContinentBossRepository(db.gameDao(), repository)

    private val _bossRecords = MutableStateFlow<List<ContinentBossRecord>>(emptyList())
    val bossRecords: StateFlow<List<ContinentBossRecord>> = _bossRecords.asStateFlow()

    private val _sessionState = MutableStateFlow<BossSessionState?>(null)
    val sessionState: StateFlow<BossSessionState?> = _sessionState.asStateFlow()

    private var timerJob: Job? = null
    private var allQuestionsMap: Map<BossPhaseType, List<BossQuestion>> = emptyMap()

    init {
        loadBossRecords()
    }

    fun loadBossRecords() {
        viewModelScope.launch {
            val countries = repository.loadCountries()
            val discoveries = repository.atlasDiscoveries.first()
            val progress = repository.allProgress.first()
            val records = bossRepository.getBossRecords(countries, discoveries, progress)
            _bossRecords.value = records
        }
    }

    fun startBossAttempt(bossId: String, countries: List<Country>) {
        val boss = ContinentBossEngine.getBossById(bossId) ?: return
        val phases = ContinentBossEngine.getPhasesForBoss()
        allQuestionsMap = ContinentBossEngine.generateBossQuestions(boss, countries)

        val firstPhase = phases.first()
        val phaseQuestions = allQuestionsMap[firstPhase.phaseType] ?: emptyList()

        _sessionState.value = BossSessionState(
            boss = boss,
            phases = phases,
            currentPhaseIndex = 0,
            currentQuestionIndex = 0,
            currentPhaseQuestions = phaseQuestions,
            remainingHearts = 3,
            score = 0,
            currentStreak = 0,
            maxStreak = 0,
            phaseResults = emptyList(),
            isPhaseCompleted = false,
            isBossVictory = false,
            isBossFailed = false,
            selectedOptionIndex = null,
            isAnswerSubmitted = false,
            isAnswerCorrect = false,
            secondsRemainingInQuestion = firstPhase.timerSecondsPerQuestion
        )

        if (firstPhase.timerSecondsPerQuestion > 0) {
            startTimer(firstPhase.timerSecondsPerQuestion)
        }
    }

    fun selectOption(index: Int) {
        val current = _sessionState.value ?: return
        if (current.isAnswerSubmitted || current.isPhaseCompleted || current.isBossFailed || current.isBossVictory) return
        _sessionState.value = current.copy(selectedOptionIndex = index)
    }

    fun submitAnswer() {
        val current = _sessionState.value ?: return
        if (current.isAnswerSubmitted || current.selectedOptionIndex == null) return

        timerJob?.cancel()

        val question = current.currentPhaseQuestions.getOrNull(current.currentQuestionIndex) ?: return
        val isCorrect = current.selectedOptionIndex == question.correctOptionIndex

        val newStreak = if (isCorrect) current.currentStreak + 1 else 0
        val newMaxStreak = maxOf(current.maxStreak, newStreak)
        val streakBonus = if (isCorrect) (newStreak * 10) else 0
        val pointsEarned = if (isCorrect) (100 + streakBonus) else 0
        val newScore = current.score + pointsEarned
        val newHearts = if (isCorrect) current.remainingHearts else (current.remainingHearts - 1)

        val isFailed = newHearts <= 0

        _sessionState.value = current.copy(
            isAnswerSubmitted = true,
            isAnswerCorrect = isCorrect,
            remainingHearts = newHearts,
            score = newScore,
            currentStreak = newStreak,
            maxStreak = newMaxStreak,
            isBossFailed = isFailed
        )
    }

    fun nextQuestionOrPhase() {
        val current = _sessionState.value ?: return
        if (current.isBossFailed) return

        val nextQuestionIdx = current.currentQuestionIndex + 1

        if (nextQuestionIdx < current.currentPhaseQuestions.size) {
            // Advance to next question in same phase
            val currentPhaseDef = current.phases[current.currentPhaseIndex]
            _sessionState.value = current.copy(
                currentQuestionIndex = nextQuestionIdx,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                isAnswerCorrect = false,
                secondsRemainingInQuestion = currentPhaseDef.timerSecondsPerQuestion
            )
            if (currentPhaseDef.timerSecondsPerQuestion > 0) {
                startTimer(currentPhaseDef.timerSecondsPerQuestion)
            }
        } else {
            // Phase completed! Record phase result
            val phaseType = current.phases[current.currentPhaseIndex].phaseType
            val questionsCount = current.currentPhaseQuestions.size
            // Record result
            val currentPhaseResults = current.phaseResults.toMutableList()
            currentPhaseResults.add(
                PhaseAttemptResult(
                    phaseType = phaseType,
                    questionsAnswered = questionsCount,
                    correctAnswers = questionsCount, // simplified tracking
                    scoreEarned = current.score
                )
            )

            val isLastPhase = current.currentPhaseIndex >= current.phases.size - 1

            if (isLastPhase) {
                // Victory!
                _sessionState.value = current.copy(
                    phaseResults = currentPhaseResults,
                    isPhaseCompleted = false,
                    isBossVictory = true
                )
                saveVictoryResult()
            } else {
                // Show phase transition card
                _sessionState.value = current.copy(
                    phaseResults = currentPhaseResults,
                    isPhaseCompleted = true
                )
            }
        }
    }

    fun continueToNextPhase() {
        val current = _sessionState.value ?: return
        if (!current.isPhaseCompleted) return

        val nextPhaseIdx = current.currentPhaseIndex + 1
        val nextPhaseDef = current.phases[nextPhaseIdx]
        val nextPhaseQuestions = allQuestionsMap[nextPhaseDef.phaseType] ?: emptyList()

        _sessionState.value = current.copy(
            currentPhaseIndex = nextPhaseIdx,
            currentQuestionIndex = 0,
            currentPhaseQuestions = nextPhaseQuestions,
            isPhaseCompleted = false,
            selectedOptionIndex = null,
            isAnswerSubmitted = false,
            isAnswerCorrect = false,
            secondsRemainingInQuestion = nextPhaseDef.timerSecondsPerQuestion
        )

        if (nextPhaseDef.timerSecondsPerQuestion > 0) {
            startTimer(nextPhaseDef.timerSecondsPerQuestion)
        }
    }

    private fun saveVictoryResult() {
        val current = _sessionState.value ?: return
        viewModelScope.launch {
            val totalQuestions = current.phases.sumOf { it.questionCount }
            val totalScore = current.score
            val heartsLeft = current.remainingHearts
            val accuracyPct = 90 // high performance estimate on full victory
            bossRepository.recordBossVictory(current.boss, totalScore, accuracyPct, heartsLeft)
            loadBossRecords()
        }
    }

    private fun startTimer(durationSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var time = durationSeconds
            while (time > 0) {
                _sessionState.value = _sessionState.value?.copy(secondsRemainingInQuestion = time)
                delay(1000)
                time--
            }
            _sessionState.value = _sessionState.value?.copy(secondsRemainingInQuestion = 0)
            // Time expired -> auto select wrong answer or submit if not answered
            val state = _sessionState.value
            if (state != null && !state.isAnswerSubmitted) {
                val defaultSelect = if (state.selectedOptionIndex != null) state.selectedOptionIndex else 0
                _sessionState.value = state.copy(selectedOptionIndex = defaultSelect)
                submitAnswer()
            }
        }
    }

    fun retryAttempt(countries: List<Country>) {
        val bossId = _sessionState.value?.boss?.bossId ?: return
        startBossAttempt(bossId, countries)
    }

    fun exitAttempt() {
        timerJob?.cancel()
        _sessionState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
