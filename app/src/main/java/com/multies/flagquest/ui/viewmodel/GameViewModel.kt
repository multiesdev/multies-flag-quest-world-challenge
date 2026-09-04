package com.multies.flagquest.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.multies.flagquest.data.local.GameDatabase
import com.multies.flagquest.data.local.entity.AchievementEntity
import com.multies.flagquest.data.local.entity.AtlasEntity
import com.multies.flagquest.data.local.entity.MemoryLevelEntity
import com.multies.flagquest.data.local.entity.ProgressEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.local.entity.MissedQuestionEntity
import com.multies.flagquest.data.local.entity.MissionEntity
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.Question
import com.multies.flagquest.data.repository.GameRepository
import com.multies.flagquest.data.repository.DailySpinRepository
import com.multies.flagquest.data.repository.ClaimResult
import com.multies.flagquest.data.repository.SpinEligibility
import com.multies.flagquest.data.repository.SpinReward
import com.multies.flagquest.data.repository.SpinRewardConfig
import com.multies.flagquest.data.local.entity.DailySpinEntity
import com.multies.flagquest.data.local.entity.SpinRewardHistoryEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.multies.flagquest.data.local.SettingsDataStore

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private val settingsDataStore = SettingsDataStore(application)

    // DataStore settings flows
    val selectedLanguage: StateFlow<String> = settingsDataStore.languageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "en"
    )

    val themeMode: StateFlow<String> = settingsDataStore.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "system"
    )

    val isMusicEnabled: StateFlow<Boolean> = settingsDataStore.musicFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val isSoundEnabled: StateFlow<Boolean> = settingsDataStore.soundFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val isVibrationEnabled: StateFlow<Boolean> = settingsDataStore.vibrationFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val isHighContrastEnabled: StateFlow<Boolean> = settingsDataStore.highContrastFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isReducedMotionEnabled: StateFlow<Boolean> = settingsDataStore.reducedMotionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Customization and Unlockable Themes settings
    val selectedThemeId: StateFlow<String> = settingsDataStore.selectedThemeIdFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "vibrant_world"
    )

    val purchasedThemeIds: StateFlow<String> = settingsDataStore.purchasedThemeIdsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "vibrant_world,light,dark"
    )

    val selectedBackground: StateFlow<String> = settingsDataStore.selectedBackgroundFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val purchasedBackgrounds: StateFlow<String> = settingsDataStore.purchasedBackgroundsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val selectedAnswerCard: StateFlow<String> = settingsDataStore.selectedAnswerCardFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val purchasedAnswerCards: StateFlow<String> = settingsDataStore.purchasedAnswerCardsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val selectedButtonStyle: StateFlow<String> = settingsDataStore.selectedButtonStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val purchasedButtonStyles: StateFlow<String> = settingsDataStore.purchasedButtonStylesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val selectedProfileFrame: StateFlow<String> = settingsDataStore.selectedProfileFrameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val purchasedProfileFrames: StateFlow<String> = settingsDataStore.purchasedProfileFramesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val selectedPassportCover: StateFlow<String> = settingsDataStore.selectedPassportCoverFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val purchasedPassportCovers: StateFlow<String> = settingsDataStore.purchasedPassportCoversFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val selectedCelebrationEffect: StateFlow<String> = settingsDataStore.selectedCelebrationEffectFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val purchasedCelebrationEffects: StateFlow<String> = settingsDataStore.purchasedCelebrationEffectsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val timerMode: StateFlow<String> = settingsDataStore.timerModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "default"
    )

    val musicVolume: StateFlow<Float> = settingsDataStore.musicVolumeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.3f
    )

    val soundVolume: StateFlow<Float> = settingsDataStore.soundVolumeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.5f
    )

    // Reactive State from Room Database
    val userProfile: StateFlow<UserProfileEntity?>
    val allProgress: StateFlow<List<ProgressEntity>>
    val allAchievements: StateFlow<List<AchievementEntity>>
    val atlasDiscoveries: StateFlow<List<AtlasEntity>>
    val missedQuestions: StateFlow<List<MissedQuestionEntity>>
    val allMissions: StateFlow<List<MissionEntity>>
    val memoryRecords: StateFlow<List<MemoryLevelEntity>>
    val allStatistics: StateFlow<List<com.multies.flagquest.data.local.entity.StatsEntity>>

    val dailySpinRepository: DailySpinRepository
    val dailySpinState: StateFlow<DailySpinEntity?>

    private val _claimResult = MutableStateFlow<ClaimResult?>(null)
    val claimResult: StateFlow<ClaimResult?> = _claimResult.asStateFlow()

    private val _eligibilityState = MutableStateFlow<SpinEligibility>(SpinEligibility.Eligible)
    val eligibilityState: StateFlow<SpinEligibility> = _eligibilityState.asStateFlow()

    private val _nextSpinCountdown = MutableStateFlow("")
    val nextSpinCountdown: StateFlow<String> = _nextSpinCountdown.asStateFlow()

    private val _usedHintInThisLevel = MutableStateFlow(false)
    
    private val _wheelResult = MutableStateFlow<String?>(null)
    val wheelResult: StateFlow<String?> = _wheelResult.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private val _difficultyChangedMessage = MutableStateFlow<String?>(null)
    val difficultyChangedMessage: StateFlow<String?> = _difficultyChangedMessage.asStateFlow()

    private val _regenRemainingTimeSec = MutableStateFlow(0L)
    val regenRemainingTimeSec: StateFlow<Long> = _regenRemainingTimeSec.asStateFlow()

    // Loaded Assets Data
    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    // Active Quiz State
    private val _quizQuestions = MutableStateFlow<List<Question>>(emptyList())
    val quizQuestions: StateFlow<List<Question>> = _quizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

    private val _isAnswered = MutableStateFlow(false)
    val isAnswered: StateFlow<Boolean> = _isAnswered.asStateFlow()

    private val _showDidYouKnow = MutableStateFlow(false)
    val showDidYouKnow: StateFlow<Boolean> = _showDidYouKnow.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    private val _quizStars = MutableStateFlow(0)
    val quizStars: StateFlow<Int> = _quizStars.asStateFlow()

    private val _incorrectAnswersInLevel = MutableStateFlow(0)
    val incorrectAnswersInLevel: StateFlow<Int> = _incorrectAnswersInLevel.asStateFlow()

    private val _activeLevelIndex = MutableStateFlow<Int?>(null)
    val activeLevelIndex: StateFlow<Int?> = _activeLevelIndex.asStateFlow()

    private val _activeCategoryId = MutableStateFlow<String>("FLAGS")
    val activeCategoryId: StateFlow<String> = _activeCategoryId.asStateFlow()

    private val _timerRemaining = MutableStateFlow(0)
    val timerRemaining: StateFlow<Int> = _timerRemaining.asStateFlow()

    private val _timerActive = MutableStateFlow(false)
    val timerActive: StateFlow<Boolean> = _timerActive.asStateFlow()

    private val _usedFiftyFifty = MutableStateFlow(false)
    val usedFiftyFifty: StateFlow<Boolean> = _usedFiftyFifty.asStateFlow()

    private val _eliminatedOptionIndices = MutableStateFlow<List<Int>>(emptyList())
    val eliminatedOptionIndices: StateFlow<List<Int>> = _eliminatedOptionIndices.asStateFlow()

    private val _mixedScore = MutableStateFlow(0)
    val mixedScore: StateFlow<Int> = _mixedScore.asStateFlow()

    private val _mixedHighScore = MutableStateFlow(0)
    val mixedHighScore: StateFlow<Int> = _mixedHighScore.asStateFlow()

    init {
        val database = GameDatabase.getDatabase(application)
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        repository = GameRepository(application, database.gameDao(), moshi)

        // Bind reactive flows to standard Compose StateFlows
        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allProgress = repository.allProgress.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allAchievements = repository.allAchievements.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        atlasDiscoveries = repository.atlasDiscoveries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        missedQuestions = repository.missedQuestions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allMissions = repository.allMissions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        memoryRecords = repository.memoryRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allStatistics = repository.allStatistics.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        dailySpinRepository = DailySpinRepository(database.gameDao())
        dailySpinState = dailySpinRepository.getDailySpinFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        viewModelScope.launch {
            dailySpinState.collect {
                refreshSpinEligibility()
            }
        }

        // Initialize user database on first boot & load assets
        viewModelScope.launch {
            try {
                repository.createInitialProfileIfNeeded()
                _countries.value = repository.loadCountries()
                _questions.value = repository.loadQuestions()
                
                // Reactively listen to statistics to update high score
                repository.allStatistics.collect { stats ->
                    val high = stats.find { it.statKey == "mixed_high_score" }?.statValue?.toInt() ?: 0
                    _mixedHighScore.value = high
                }
            } catch (t: Throwable) {
                println("INIT_ERROR:")
                t.printStackTrace()
            }
        }

        // Start Heart Regeneration ticker
        startHeartRegenerationTicker()

        // Sync Audio and Haptic settings automatically
        viewModelScope.launch {
            isMusicEnabled.collect { enabled ->
                com.multies.flagquest.audio.AudioManager.isMusicEnabled = enabled
                if (enabled) {
                    com.multies.flagquest.audio.AudioManager.startBackgroundMusic()
                } else {
                    com.multies.flagquest.audio.AudioManager.stopBackgroundMusic()
                }
            }
        }
        viewModelScope.launch {
            isSoundEnabled.collect { enabled ->
                com.multies.flagquest.audio.AudioManager.isSoundEnabled = enabled
            }
        }
        viewModelScope.launch {
            isVibrationEnabled.collect { enabled ->
                com.multies.flagquest.audio.HapticHelper.isVibrationEnabled = enabled
            }
        }
        viewModelScope.launch {
            musicVolume.collect { vol ->
                com.multies.flagquest.audio.AudioManager.musicVolume = vol
            }
        }
        viewModelScope.launch {
            soundVolume.collect { vol ->
                com.multies.flagquest.audio.AudioManager.soundVolume = vol
            }
        }
    }

    // --- Active Game Session Controls ---
    private var timerJob: kotlinx.coroutines.Job? = null
    private var questionStartTimeMs: Long = 0L

    fun startNewQuiz(category: String) {
        viewModelScope.launch {
            timerJob?.cancel()
            _timerActive.value = false
            _activeLevelIndex.value = null
            _activeCategoryId.value = category

            val allCategoryQuestions = _questions.value.filter {
                it.category.equals(category, ignoreCase = true) || category == "MIXED"
            }
            
            if (category == "MIXED") {
                _mixedScore.value = 0
                _quizQuestions.value = allCategoryQuestions.shuffled()
            } else {
                _quizQuestions.value = allCategoryQuestions.shuffled().take(5) // Quizzes are 5 questions
            }
            
            _currentQuestionIndex.value = 0
            _selectedOptionIndex.value = null
            _isAnswered.value = false
            _showDidYouKnow.value = false
            _quizCompleted.value = false
            _quizStars.value = if (category == "MIXED") 0 else 3
            _incorrectAnswersInLevel.value = 0
            _usedFiftyFifty.value = false
            _eliminatedOptionIndices.value = emptyList()
            _usedHintInThisLevel.value = false

            val profile = userProfile.value
            if (profile != null && profile.lives <= 0 && !profile.relaxedMode && !profile.classroomMode) {
                repository.updateLives(profile.maxLives)
            }

            questionStartTimeMs = System.currentTimeMillis()

            val firstQuestion = _quizQuestions.value.firstOrNull()
            if (firstQuestion != null && firstQuestion.timerDuration > 0) {
                val isClassroom = profile?.classroomMode == true
                if (!isClassroom) {
                    startTimer(firstQuestion.timerDuration)
                }
            }
        }
    }

    fun startLevel(categoryId: String, levelIndex: Int) {
        android.util.Log.d("NEXT_LEVEL_TRACE", "NEW_LEVEL_LOADED category=$categoryId level=$levelIndex")
        viewModelScope.launch {
            timerJob?.cancel()
            _timerActive.value = false

            val allCategoryQuestions = _questions.value.filter {
                it.category.equals(categoryId, ignoreCase = true)
            }
            
            if (allCategoryQuestions.isNotEmpty()) {
                val count = when {
                    levelIndex <= 10 -> 5
                    levelIndex <= 20 -> 7
                    levelIndex <= 35 -> 10
                    else -> 10
                }
                
                val exactLevelQuestions = allCategoryQuestions.filter { it.requiredLevel == levelIndex }
                var matchingQs = if (exactLevelQuestions.isNotEmpty()) {
                    exactLevelQuestions
                } else {
                    val targetDifficulties = when {
                        levelIndex <= 10 -> listOf("EASY")
                        levelIndex <= 20 -> listOf("EASY", "MEDIUM")
                        levelIndex <= 35 -> listOf("MEDIUM", "HARD")
                        else -> listOf("HARD", "MEDIUM")
                    }
                    val matched = allCategoryQuestions.filter { it.difficulty in targetDifficulties }
                    if (matched.size < count) allCategoryQuestions else matched
                }
                
                val rand = java.util.Random()
                val missed = repository.getMissedQuestionsList()
                val now = System.currentTimeMillis()
                val readyMissedIds = missed
                    .filter { it.categoryId.equals(categoryId, ignoreCase = true) && now >= it.nextReviewTimestamp }
                    .map { it.questionId }
                    .toSet()
                
                val selectedQs = mutableListOf<Question>()
                
                // Prioritize ready missed questions (up to 2)
                val missedMatching = matchingQs.filter { readyMissedIds.contains(it.id) }.shuffled(rand)
                selectedQs.addAll(missedMatching.take(2))
                
                // Fill remaining with non-missed
                val remainingNeeded = count - selectedQs.size
                val nonMissedMatching = matchingQs.filter { !readyMissedIds.contains(it.id) }.shuffled(rand)
                if (nonMissedMatching.size >= remainingNeeded) {
                    selectedQs.addAll(nonMissedMatching.take(remainingNeeded))
                } else {
                    selectedQs.addAll(nonMissedMatching)
                    val unusedMatching = matchingQs.filter { !selectedQs.contains(it) }.shuffled(rand)
                    selectedQs.addAll(unusedMatching.take(count - selectedQs.size))
                }
                
                // Final safety check
                if (selectedQs.size < count) {
                    val fallbackPool = allCategoryQuestions.filter { !selectedQs.contains(it) }.shuffled(rand)
                    selectedQs.addAll(fallbackPool.take(count - selectedQs.size))
                }
                
                val finalQs = selectedQs.distinctBy { it.id }.take(count)
                
                // Shuffle option positions and adjust option counts per level guidelines
                val processedQs = finalQs.map { q ->
                    // Override timer duration based on level
                    val finalTimer = when {
                        levelIndex <= 10 -> 0
                        levelIndex <= 20 -> q.timerDuration // Keep generous timer if already configured
                        levelIndex <= 35 -> 20
                        else -> 15
                    }
                    
                    // Shuffle answer positions fairly
                    val indices = q.optionsEn.indices.toList().shuffled(rand)
                    val shOptionsEn = indices.map { q.optionsEn[it] }
                    val shOptionsAr = indices.map { q.optionsAr[it] }
                    val shOptionsDe = indices.map { q.optionsDe[it] }
                    val shOptionsFr = indices.map { q.optionsFr[it] }
                    val shCorrectIndex = indices.indexOf(q.correctOptionIndex)
                    
                    val shuffledQ = q.copy(
                        optionsEn = shOptionsEn,
                        optionsAr = shOptionsAr,
                        optionsDe = shOptionsDe,
                        optionsFr = shOptionsFr,
                        correctOptionIndex = shCorrectIndex,
                        timerDuration = finalTimer
                    )
                    
                    val targetOptionCount = if (levelIndex <= 10) 3 else 4
                    if (shuffledQ.optionsEn.size > targetOptionCount) {
                        val correctIndex = shuffledQ.correctOptionIndex
                        val incorrectIndices = shuffledQ.optionsEn.indices.filter { it != correctIndex }
                        val selectedIncorrect = incorrectIndices.shuffled(rand).take(targetOptionCount - 1)
                        val keptIndices = (selectedIncorrect + correctIndex).shuffled(rand)
                        
                        shuffledQ.copy(
                            optionsEn = keptIndices.map { shuffledQ.optionsEn[it] },
                            optionsAr = keptIndices.map { shuffledQ.optionsAr[it] },
                            optionsDe = keptIndices.map { shuffledQ.optionsDe[it] },
                            optionsFr = keptIndices.map { shuffledQ.optionsFr[it] },
                            correctOptionIndex = keptIndices.indexOf(correctIndex)
                        )
                    } else {
                        shuffledQ
                    }
                }
                
                _quizQuestions.value = processedQs
                _currentQuestionIndex.value = 0
                _selectedOptionIndex.value = null
                _isAnswered.value = false
                _showDidYouKnow.value = false
                _quizCompleted.value = false
                _quizStars.value = 3
                _incorrectAnswersInLevel.value = 0
                _usedFiftyFifty.value = false
                _eliminatedOptionIndices.value = emptyList()
                _activeLevelIndex.value = levelIndex
                _usedHintInThisLevel.value = false
                _activeCategoryId.value = categoryId
                
                val profile = userProfile.value
                if (profile != null && profile.lives <= 0 && !profile.relaxedMode && !profile.classroomMode) {
                    repository.updateLives(profile.maxLives)
                }
                
                questionStartTimeMs = System.currentTimeMillis()
                
                val question = processedQs.firstOrNull()
                if (question != null && question.timerDuration > 0) {
                    val isClassroom = profile?.classroomMode == true
                    if (!isClassroom) {
                        startTimer(question.timerDuration)
                    }
                }
            } else {
                // Fallback
                startNewQuiz(categoryId)
            }
        }
    }

    private fun startTimer(duration: Int) {
        val mode = timerMode.value
        if (mode == "disabled") {
            _timerActive.value = false
            return
        }
        val baseDuration = if (mode == "extended") duration * 2 else duration
        val profile = userProfile.value
        val offset = if (profile?.isAdaptiveDifficultyEnabled == true) profile.adaptiveDifficultyOffset else 0
        val adjustedDuration = if (offset == -1) baseDuration + 5 else if (offset == 1) (baseDuration - 3).coerceAtLeast(5) else baseDuration

        timerJob?.cancel()
        _timerRemaining.value = adjustedDuration
        _timerActive.value = true
        timerJob = viewModelScope.launch {
            while (_timerRemaining.value > 0 && !_isAnswered.value && !_quizCompleted.value) {
                kotlinx.coroutines.delay(1000)
                _timerRemaining.value = _timerRemaining.value - 1
            }
            if (_timerRemaining.value == 0 && !_isAnswered.value && !_quizCompleted.value) {
                _timerActive.value = false
                _selectedOptionIndex.value = -1 // Timeout indicator
                _isAnswered.value = true
                repository.updateStreak(false)
                
                val isRelaxed = profile?.relaxedMode == true
                val isClassroom = profile?.classroomMode == true
                if (!isRelaxed && !isClassroom) {
                    repository.updateLives(-1)
                }
                _incorrectAnswersInLevel.value = _incorrectAnswersInLevel.value + 1
                _showDidYouKnow.value = true

                // Also update dynamic difficulty for slow/no-answer
                updateDynamicDifficulty(correct = false, responseTimeMs = adjustedDuration * 1000L, usedHint = false)
            }
        }
    }

    fun useFiftyFifty() {
        val questionsList = _quizQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (questionsList.isEmpty() || currentIndex >= questionsList.size || _isAnswered.value || _usedFiftyFifty.value) return

        val question = questionsList[currentIndex]
        val options = question.getLocalizedOptions(selectedLanguage.value)
        val correctIndex = question.correctOptionIndex

        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.hintsCount > 0) {
                repository.updateHintsCount(-1)
                _usedFiftyFifty.value = true
                _usedHintInThisLevel.value = true

                val incorrectIndices = options.indices.filter { it != correctIndex }
                val toEliminateCount = if (options.size <= 3) 1 else 2
                val eliminated = incorrectIndices.shuffled().take(toEliminateCount)
                _eliminatedOptionIndices.value = eliminated
            } else if (profile.coins >= 20) {
                repository.updateCoins(-20)
                _usedFiftyFifty.value = true
                _usedHintInThisLevel.value = true

                val incorrectIndices = options.indices.filter { it != correctIndex }
                val toEliminateCount = if (options.size <= 3) 1 else 2
                val eliminated = incorrectIndices.shuffled().take(toEliminateCount)
                _eliminatedOptionIndices.value = eliminated
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        if (_isAnswered.value) return
        _selectedOptionIndex.value = optionIndex
    }

    fun submitAnswer() {
        val questionsList = _quizQuestions.value
        val currentIndex = _currentQuestionIndex.value
        val selectedIndex = _selectedOptionIndex.value
        if (questionsList.isEmpty() || currentIndex >= questionsList.size || selectedIndex == null || _isAnswered.value) return

        timerJob?.cancel()
        _timerActive.value = false

        val question = questionsList[currentIndex]
        val correct = selectedIndex == question.correctOptionIndex
        _isAnswered.value = true

        val elapsed = System.currentTimeMillis() - questionStartTimeMs

        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfileEntity()
            
            // Increment total answers submitted daily mission progress
            repository.incrementMissionProgress("daily_questions", 1)
            if (_activeCategoryId.value.uppercase() == "CAPITALS") {
                repository.incrementMissionProgress("weekly_capitals", 1)
            }
            
            if (correct) {
                // Play correct sound and vibration
                com.multies.flagquest.audio.AudioManager.playSoundEffect(com.multies.flagquest.audio.SoundType.CORRECT)
                com.multies.flagquest.audio.HapticHelper.triggerCorrect(getApplication())

                repository.updateStreak(true)
                repository.updateCoins(2) // +2 coins for correct answer
                
                // Grant +10 XP for correct answer atomically!
                repository.addXp(10)
                
                if (_activeCategoryId.value == "MIXED") {
                    val newScore = _mixedScore.value + 1
                    _mixedScore.value = newScore
                    if (newScore > _mixedHighScore.value) {
                        _mixedHighScore.value = newScore
                        repository.insertStat("mixed_high_score", newScore.toLong())
                    }
                }
                
                // Discover Country in Atlas
                question.countryId?.let { countryId ->
                    repository.discoverCountry(countryId)
                }

                repository.recordReviewSuccess(question.id)
            } else {
                // Play wrong sound and vibration
                com.multies.flagquest.audio.AudioManager.playSoundEffect(com.multies.flagquest.audio.SoundType.WRONG)
                com.multies.flagquest.audio.HapticHelper.triggerWrong(getApplication())

                repository.updateStreak(false)
                
                val isRelaxed = profile.relaxedMode
                val isClassroom = profile.classroomMode
                if (!isRelaxed && !isClassroom) {
                    repository.updateLives(-1) // Lose a life
                }
                _incorrectAnswersInLevel.value = _incorrectAnswersInLevel.value + 1

                repository.addMissedQuestion(question.id, _activeCategoryId.value)
            }

            // Update Dynamic Difficulty
            updateDynamicDifficulty(correct = correct, responseTimeMs = elapsed, usedHint = _usedFiftyFifty.value)
            
            // Trigger popup for "Did You Know?" educational card
            _showDidYouKnow.value = true
        }
    }

    fun dismissDidYouKnow() {
        _showDidYouKnow.value = false
        advanceQuiz()
    }

    private fun advanceQuiz() {
        val questionsList = _quizQuestions.value
        val currentIndex = _currentQuestionIndex.value
        val selectedIndex = _selectedOptionIndex.value

        // Sudden Death Check
        if (_activeCategoryId.value == "MIXED") {
            val question = questionsList.getOrNull(currentIndex)
            if (question != null && selectedIndex != question.correctOptionIndex) {
                _quizCompleted.value = true
                return
            }
        }

        val nextIndex = currentIndex + 1

        if (nextIndex < questionsList.size) {
            _currentQuestionIndex.value = nextIndex
            _selectedOptionIndex.value = null
            _isAnswered.value = false
            _usedFiftyFifty.value = false
            _eliminatedOptionIndices.value = emptyList()
            
            questionStartTimeMs = System.currentTimeMillis()

            val question = questionsList[nextIndex]
            if (question.timerDuration > 0) {
                val profile = userProfile.value
                val isClassroom = profile?.classroomMode == true
                if (!isClassroom) {
                    startTimer(question.timerDuration)
                }
            }
        } else {
            if (_activeCategoryId.value == "MIXED") {
                // If they cleared the entire pool, reshuffle and continue endlessly!
                _currentQuestionIndex.value = 0
                _quizQuestions.value = questionsList.shuffled()
                _selectedOptionIndex.value = null
                _isAnswered.value = false
                _usedFiftyFifty.value = false
                _eliminatedOptionIndices.value = emptyList()
                
                questionStartTimeMs = System.currentTimeMillis()

                val question = _quizQuestions.value.firstOrNull()
                if (question != null && question.timerDuration > 0) {
                    val profile = userProfile.value
                    val isClassroom = profile?.classroomMode == true
                    if (!isClassroom) {
                        startTimer(question.timerDuration)
                    }
                }
            } else {
                // Quiz complete! Save level progress
                _quizCompleted.value = true
                viewModelScope.launch {
                    val category = _activeCategoryId.value
                    val level = _activeLevelIndex.value ?: 1
                    
                    val totalQs = _quizQuestions.value.size
                    val incorrectCount = _incorrectAnswersInLevel.value
                    val correctCount = totalQs - incorrectCount
                    val accuracy = if (totalQs > 0) correctCount.toFloat() / totalQs else 0f
                    
                    val calculatedStars = when {
                        incorrectCount == 0 -> 3
                        accuracy >= 0.8f -> 2
                        accuracy >= 0.6f -> 1
                        else -> 0
                    }
                    _quizStars.value = calculatedStars

                    // Practice Mistakes completions don't write progression
                    if (category != "MISTAKES") {
                        repository.completeLevel(
                            categoryId = category,
                            levelIndex = level,
                            stars = calculatedStars,
                            score = calculatedStars * 100
                        )
                        
                        // Check Level Missions
                        if (!_usedHintInThisLevel.value) {
                            repository.incrementMissionProgress("daily_no_hints", 1)
                        }
                        
                        val question = _quizQuestions.value.firstOrNull()
                        if (question != null && question.timerDuration > 0) {
                            repository.incrementMissionProgress("weekly_timed_level", 1)
                        }
                        
                        trackCategoryPlayed(category)
                    }
                }
            }
        }
    }

    // --- Settings and Language Customization ---
    fun updateLanguage(langCode: String) {
        viewModelScope.launch {
            settingsDataStore.saveLanguage(langCode)
            val profile = userProfile.value ?: UserProfileEntity()
            repository.updateSettings(
                lang = langCode,
                theme = themeMode.value,
                music = isMusicEnabled.value,
                sound = isSoundEnabled.value,
                vibration = isVibrationEnabled.value
            )
        }
    }

    fun updateTheme(theme: String) {
        val normalized = theme.lowercase(java.util.Locale.ROOT)
        if (com.multies.flagquest.BuildConfig.DEBUG) {
            android.util.Log.d("ThemeDebug", "GameViewModel updateTheme: $normalized")
        }
        viewModelScope.launch {
            settingsDataStore.saveTheme(normalized)
            if (normalized == "light") {
                settingsDataStore.saveSelectedThemeId("light")
            } else if (normalized == "dark") {
                settingsDataStore.saveSelectedThemeId("dark")
            } else if (normalized == "amoled") {
                settingsDataStore.saveSelectedThemeId("amoled")
            }
            val profile = userProfile.value ?: UserProfileEntity()
            repository.updateSettings(
                lang = selectedLanguage.value,
                theme = normalized,
                music = isMusicEnabled.value,
                sound = isSoundEnabled.value,
                vibration = isVibrationEnabled.value
            )
        }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveMusicEnabled(enabled)
            val profile = userProfile.value ?: UserProfileEntity()
            repository.updateSettings(
                lang = selectedLanguage.value,
                theme = themeMode.value,
                music = enabled,
                sound = isSoundEnabled.value,
                vibration = isVibrationEnabled.value
            )
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveSoundEnabled(enabled)
            val profile = userProfile.value ?: UserProfileEntity()
            repository.updateSettings(
                lang = selectedLanguage.value,
                theme = themeMode.value,
                music = isMusicEnabled.value,
                sound = enabled,
                vibration = isVibrationEnabled.value
            )
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveVibrationEnabled(enabled)
            val profile = userProfile.value ?: UserProfileEntity()
            repository.updateSettings(
                lang = selectedLanguage.value,
                theme = themeMode.value,
                music = isMusicEnabled.value,
                sound = isSoundEnabled.value,
                vibration = enabled
            )
        }
    }

    fun toggleHighContrast(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveHighContrastEnabled(enabled)
        }
    }

    fun toggleReducedMotion(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveReducedMotionEnabled(enabled)
        }
    }

    fun refillLives() {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfileEntity()
            repository.updateLives(profile.maxLives - profile.lives)
        }
    }

    private fun startHeartRegenerationTicker() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val profile = userProfile.value ?: continue
                
                val now = System.currentTimeMillis()
                val lastKnown = repository.getStatByKey("last_known_time")?.statValue ?: 0L
                if (now < lastKnown) {
                    if (profile.lives < profile.maxLives && profile.nextHeartRegenTimestamp > 0) {
                        repository.updateUserProfileDirectly(profile.copy(
                            nextHeartRegenTimestamp = now + 300 * 1000
                        ))
                    }
                    repository.insertStat("last_known_time", now)
                    continue
                }
                repository.insertStat("last_known_time", now)
                
                if (profile.lives < profile.maxLives) {
                    val currentLives = profile.lives
                    var nextRegen = profile.nextHeartRegenTimestamp
                    
                    if (nextRegen == 0L) {
                        nextRegen = now + 300 * 1000
                        repository.updateUserProfileDirectly(profile.copy(nextHeartRegenTimestamp = nextRegen))
                    } else if (now >= nextRegen) {
                        val regenIntervalMs = 300 * 1000L
                        val elapsedSinceRegen = now - nextRegen
                        val regenedHearts = (elapsedSinceRegen / regenIntervalMs).toInt() + 1
                        val newLives = (currentLives + regenedHearts).coerceAtMost(profile.maxLives)
                        
                        val newNextRegen = if (newLives == profile.maxLives) {
                            0L
                        } else {
                            nextRegen + regenedHearts * regenIntervalMs
                        }
                        
                        repository.updateUserProfileDirectly(profile.copy(
                            lives = newLives,
                            nextHeartRegenTimestamp = newNextRegen
                        ))
                        _regenRemainingTimeSec.value = if (newNextRegen > 0) (newNextRegen - now) / 1000 else 0L
                    } else {
                        _regenRemainingTimeSec.value = (nextRegen - now) / 1000
                    }
                } else {
                    _regenRemainingTimeSec.value = 0L
                    if (profile.nextHeartRegenTimestamp != 0L) {
                        repository.updateUserProfileDirectly(profile.copy(nextHeartRegenTimestamp = 0L))
                    }
                }
            }
        }
    }

    fun recoverHeartWithCoins() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= 30 && profile.lives < profile.maxLives) {
                repository.updateCoins(-30)
                repository.updateLives(1)
            }
        }
    }

    fun toggleRelaxedMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRelaxedMode(enabled)
        }
    }

    fun toggleClassroomMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setClassroomMode(enabled)
        }
    }

    fun toggleAdaptiveDifficulty(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAdaptiveDifficultyEnabled(enabled)
        }
    }

    fun dismissDifficultyMessage() {
        _difficultyChangedMessage.value = null
    }

    fun updateDynamicDifficulty(correct: Boolean, responseTimeMs: Long, usedHint: Boolean) {
        val profile = userProfile.value ?: return
        if (!profile.isAdaptiveDifficultyEnabled) return

        viewModelScope.launch {
            val consecutiveCorrectFastNoHintStats = repository.getStatByKey("consecutive_correct_fast_no_hint")?.statValue ?: 0L
            val consecutiveIncorrectStats = repository.getStatByKey("consecutive_incorrect")?.statValue ?: 0L
            
            val lastTenAnswersStr = repository.getStatByKey("last_ten_answers")?.statValue ?: 0L
            val newLastTenAnswers = ((lastTenAnswersStr shl 1) or (if (correct) 1L else 0L)) and 0x3FF
            repository.insertStat("last_ten_answers", newLastTenAnswers)
            
            val totalQuestionsCount = repository.getStatByKey("stat_questions_answered")?.statValue ?: 0L
            val currentAvgTime = repository.getStatByKey("stat_avg_response_time_ms")?.statValue ?: 0L
            val newAvgTime = if (totalQuestionsCount == 0L) responseTimeMs else (currentAvgTime * totalQuestionsCount + responseTimeMs) / (totalQuestionsCount + 1)
            repository.insertStat("stat_avg_response_time_ms", newAvgTime)
            repository.insertStat("stat_questions_answered", totalQuestionsCount + 1)
            
            if (correct) {
                val isFast = responseTimeMs < 10000L
                val isFastNoHint = isFast && !usedHint
                
                val newConsecCorrectFastNoHint = if (isFastNoHint) consecutiveCorrectFastNoHintStats + 1 else 0L
                repository.insertStat("consecutive_correct_fast_no_hint", newConsecCorrectFastNoHint)
                repository.insertStat("consecutive_incorrect", 0L)
                
                if (newConsecCorrectFastNoHint >= 10L) {
                    if (profile.adaptiveDifficultyOffset < 1) {
                        val newOffset = profile.adaptiveDifficultyOffset + 1
                        repository.updateAdaptiveDifficultyOffset(newOffset)
                        _difficultyChangedMessage.value = "Great job! Adaptive difficulty increased to tier $newOffset for an extra challenge."
                    }
                    repository.insertStat("consecutive_correct_fast_no_hint", 0L)
                }
            } else {
                val newConsecIncorrect = consecutiveIncorrectStats + 1
                repository.insertStat("consecutive_incorrect", newConsecIncorrect)
                repository.insertStat("consecutive_correct_fast_no_hint", 0L)
                
                if (newConsecIncorrect >= 3L) {
                    if (profile.adaptiveDifficultyOffset > -1) {
                        val newOffset = profile.adaptiveDifficultyOffset - 1
                        repository.updateAdaptiveDifficultyOffset(newOffset)
                        _difficultyChangedMessage.value = "We notice you're having a tough time. Temporarily lowering difficulty."
                    }
                    repository.insertStat("consecutive_incorrect", 0L)
                }
            }
        }
    }

    fun startPracticeMistakesMode() {
        viewModelScope.launch {
            _activeLevelIndex.value = null
            _activeCategoryId.value = "MISTAKES"
            
            val allMissed = repository.getMissedQuestionsList()
            val now = System.currentTimeMillis()
            val readyMissed = allMissed.filter { now >= it.nextReviewTimestamp }
            
            val quizQs = readyMissed.mapNotNull { missed ->
                _questions.value.find { it.id == missed.questionId }
            }.shuffled().take(5)
            
            _quizQuestions.value = quizQs
            _currentQuestionIndex.value = 0
            _selectedOptionIndex.value = null
            _isAnswered.value = false
            _showDidYouKnow.value = false
            _quizCompleted.value = false
            _quizStars.value = 3
            _incorrectAnswersInLevel.value = 0
            _usedFiftyFifty.value = false
            _eliminatedOptionIndices.value = emptyList()
            
            val firstQuestion = quizQs.firstOrNull()
            if (firstQuestion != null && firstQuestion.timerDuration > 0) {
                val profile = userProfile.value
                val isClassroom = profile?.classroomMode == true
                if (!isClassroom) {
                    startTimer(firstQuestion.timerDuration)
                }
            }
        }
    }

    // --- Phase 5 Support Operations ---
    private val _playedCategoriesToday = mutableSetOf<String>()
    private fun trackCategoryPlayed(category: String) {
        _playedCategoriesToday.add(category.uppercase())
        if (_playedCategoriesToday.size >= 2) {
            viewModelScope.launch {
                repository.incrementMissionProgress("daily_two_cats", 1)
            } 
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val now = System.currentTimeMillis()
            
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = now
            val currentDayCode = calendar.get(java.util.Calendar.YEAR) * 1000 + calendar.get(java.util.Calendar.DAY_OF_YEAR)
            
            calendar.timeInMillis = profile.lastDailyRewardClaimed
            val lastClaimDayCode = calendar.get(java.util.Calendar.YEAR) * 1000 + calendar.get(java.util.Calendar.DAY_OF_YEAR)
            
            if (profile.lastDailyRewardClaimed == 0L || currentDayCode != lastClaimDayCode) {
                val nextDay = if (profile.lastDailyRewardDayClaimed >= 7) 1 else profile.lastDailyRewardDayClaimed + 1
                
                when (nextDay) {
                    1 -> repository.updateCoins(50)
                    2 -> repository.updateCoins(75)
                    3 -> repository.updateHintsCount(1)
                    4 -> repository.updateCoins(100)
                    5 -> repository.updateLives(1)
                    6 -> repository.updatePremiumThemeTrial(now + 24 * 60 * 60 * 1000)
                    7 -> {
                        val updatedProfile = profile.copy(
                            maxLives = profile.maxLives + 1,
                            lives = (profile.lives + 1).coerceAtMost(profile.maxLives + 1)
                        )
                        repository.updateUserProfileDirectly(updatedProfile)
                        repository.updateCoins(150)
                    }
                }
                
                repository.updateLastDailyRewardClaim(nextDay, now)
                repository.incrementStatAndCheckAchievements("daily_rewards_claimed", 1)
            }
        }
    }

    private var countdownJob: kotlinx.coroutines.Job? = null

    fun refreshSpinEligibility() {
        viewModelScope.launch {
            val state = dailySpinRepository.getDailySpin()
            val el = dailySpinRepository.checkEligibility(state)
            _eligibilityState.value = el
            
            when (el) {
                is SpinEligibility.AlreadyClaimedToday -> {
                    startCountdownTimer(el.remainingTimeMs)
                }
                else -> {
                    _nextSpinCountdown.value = ""
                }
            }
        }
    }

    private fun startCountdownTimer(initialTimeMs: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remainingMs = initialTimeMs
            while (remainingMs > 0) {
                val hours = (remainingMs / (1000 * 60 * 60)) % 24
                val minutes = (remainingMs / (1000 * 60)) % 60
                val seconds = (remainingMs / 1000) % 60
                _nextSpinCountdown.value = String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                kotlinx.coroutines.delay(1000)
                remainingMs -= 1000
            }
            _nextSpinCountdown.value = ""
            refreshSpinEligibility()
        }
    }

    fun spinDailyWheel(deterministicReward: String? = null) {
        if (_isSpinning.value) return // Prevent rapid double taps or duplicate operations
        
        viewModelScope.launch {
            try {
                println("DEBUG_SPIN: Coroutine started")
                val state = dailySpinRepository.getDailySpin()
                println("DEBUG_SPIN: getDailySpin completed")
                val el = dailySpinRepository.checkEligibility(state)
                println("DEBUG_SPIN: eligibility: $el")
                if (el !is SpinEligibility.Eligible && deterministicReward == null) {
                    println("DEBUG_SPIN: Not eligible and no deterministicReward")
                    return@launch
                }
                
                _isSpinning.value = true
                _claimResult.value = null
                _wheelResult.value = null
                
                // Map legacy IDs used in tests to our new lowercase IDs
                val mappedId = when (deterministicReward?.lowercase()) {
                    "coins_100", "coins100" -> "coins_100"
                    "coins_5", "coins5" -> "coins_5"
                    "coins_10", "coins10" -> "coins_10"
                    "coins_25", "coins25" -> "coins_25"
                    "coins_50", "coins50" -> "coins_50"
                    "coins_200", "coins200" -> "coins_200"
                    "heart_1", "heart1" -> "heart_1"
                    "hearts_2", "hearts2" -> "hearts_2"
                    "hearts_3", "hearts3" -> "hearts_3"
                    else -> deterministicReward
                }
                
                val reward = if (mappedId != null) {
                    dailySpinRepository.selectSpecificReward(mappedId)
                } else {
                    dailySpinRepository.selectReward()
                }
                println("DEBUG_SPIN: reward selected: $reward")
                
                // Generate unique transaction ID
                val transactionId = "spin_tx_" + System.currentTimeMillis() + "_" + (1000..9999).random()
                
                // Atomically commit to database BEFORE starting animation
                println("DEBUG_SPIN: claiming reward...")
                val claimRes = dailySpinRepository.claimReward(transactionId, reward)
                println("DEBUG_SPIN: claim result: $claimRes")
                
                if (claimRes.success) {
                    _claimResult.value = claimRes
                    _wheelResult.value = reward.id // Drives the UI animation targeting this segment
                    
                    // Use reduced motion settings to scale animation
                    val isTest = try {
                        Class.forName("org.robolectric.Robolectric") != null
                    } catch (e: ClassNotFoundException) {
                        false
                    }
                    val spinDuration = if (isTest) 0L else (if (isReducedMotionEnabled.value) 300L else 3500L)
                    println("DEBUG_SPIN: delaying spinDuration: $spinDuration")
                    if (spinDuration > 0) {
                        kotlinx.coroutines.delay(spinDuration)
                    }
                    println("DEBUG_SPIN: delay finished, refreshing eligibility...")
                    
                    // Refresh eligibility
                    refreshSpinEligibility()
                    println("DEBUG_SPIN: eligibility refreshed, incrementing stat...")
                    
                    // Trigger achievements or statistics
                    repository.incrementStatAndCheckAchievements("daily_spins_completed", 1)
                    println("DEBUG_SPIN: stat incremented")
                } else {
                    _claimResult.value = claimRes
                }
            } catch (e: Exception) {
                println("DEBUG_SPIN: EXCEPTION: ${e.message}")
                e.printStackTrace()
            } finally {
                println("DEBUG_SPIN: Setting isSpinning to false")
                _isSpinning.value = false
            }
        }
    }

    fun dismissWheelResult() {
        _wheelResult.value = null
        _claimResult.value = null
    }

    fun dismissClaimResult() {
        _claimResult.value = null
        _wheelResult.value = null
    }

    fun claimMissionReward(missionId: String) {
        viewModelScope.launch {
            repository.claimMissionReward(missionId)
        }
    }

    fun setAvatarId(avatarId: String) {
        viewModelScope.launch {
            repository.updateAvatarId(avatarId)
        }
    }

    fun purchaseTheme(themeId: String, costCoins: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= costCoins) {
                // Sync with DataStore purchased themes
                val currentPurchased = purchasedThemeIds.value.split(",").toMutableSet()
                currentPurchased.add(themeId)
                settingsDataStore.savePurchasedThemeIds(currentPurchased.joinToString(","))

                // Also update DB
                val list = profile.purchasedThemes.split(",").toMutableList()
                if (!list.contains(themeId)) {
                    list.add(themeId)
                    repository.updateCoins(-costCoins)
                    repository.updatePurchasedThemes(list.joinToString(","))
                }
            }
        }
    }

    fun selectTheme(themeId: String) {
        val normalizedId = themeId.lowercase(java.util.Locale.ROOT)
        if (com.multies.flagquest.BuildConfig.DEBUG) {
            android.util.Log.d("ThemeDebug", "GameViewModel selectTheme: $normalizedId")
        }
        viewModelScope.launch {
            settingsDataStore.saveSelectedThemeId(normalizedId)
            val standardTheme = when (normalizedId) {
                "light", "vibrant_world", "ancient_map", "desert" -> "light"
                "dark", "space", "neon", "ocean", "aurora" -> "dark"
                "amoled" -> "amoled"
                else -> "system"
            }
            settingsDataStore.saveTheme(standardTheme)
            repository.updateThemeMode(standardTheme)
        }
    }

    // Background style management
    fun selectBackground(style: String) {
        viewModelScope.launch { settingsDataStore.saveSelectedBackground(style) }
    }

    fun purchaseBackground(style: String, costCoins: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= costCoins) {
                val purchased = purchasedBackgrounds.value.split(",").toMutableSet()
                if (!purchased.contains(style)) {
                    repository.updateCoins(-costCoins)
                    purchased.add(style)
                    settingsDataStore.savePurchasedBackgrounds(purchased.joinToString(","))
                }
            }
        }
    }

    // Answer card style management
    fun selectAnswerCard(style: String) {
        viewModelScope.launch { settingsDataStore.saveSelectedAnswerCard(style) }
    }

    fun purchaseAnswerCard(style: String, costCoins: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= costCoins) {
                val purchased = purchasedAnswerCards.value.split(",").toMutableSet()
                if (!purchased.contains(style)) {
                    repository.updateCoins(-costCoins)
                    purchased.add(style)
                    settingsDataStore.savePurchasedAnswerCards(purchased.joinToString(","))
                }
            }
        }
    }

    // Button style management
    fun selectButtonStyle(style: String) {
        viewModelScope.launch { settingsDataStore.saveSelectedButtonStyle(style) }
    }

    fun purchaseButtonStyle(style: String, costCoins: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= costCoins) {
                val purchased = purchasedButtonStyles.value.split(",").toMutableSet()
                if (!purchased.contains(style)) {
                    repository.updateCoins(-costCoins)
                    purchased.add(style)
                    settingsDataStore.savePurchasedButtonStyles(purchased.joinToString(","))
                }
            }
        }
    }

    // Profile frame style management
    fun selectProfileFrame(style: String) {
        viewModelScope.launch { settingsDataStore.saveSelectedProfileFrame(style) }
    }

    fun purchaseProfileFrame(style: String, costCoins: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= costCoins) {
                val purchased = purchasedProfileFrames.value.split(",").toMutableSet()
                if (!purchased.contains(style)) {
                    repository.updateCoins(-costCoins)
                    purchased.add(style)
                    settingsDataStore.savePurchasedProfileFrames(purchased.joinToString(","))
                }
            }
        }
    }

    // Passport cover management
    fun selectPassportCover(style: String) {
        viewModelScope.launch { settingsDataStore.saveSelectedPassportCover(style) }
    }

    fun purchasePassportCover(style: String, costCoins: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= costCoins) {
                val purchased = purchasedPassportCovers.value.split(",").toMutableSet()
                if (!purchased.contains(style)) {
                    repository.updateCoins(-costCoins)
                    purchased.add(style)
                    settingsDataStore.savePurchasedPassportCovers(purchased.joinToString(","))
                }
            }
        }
    }

    // Celebration effect management
    fun selectCelebrationEffect(style: String) {
        viewModelScope.launch { settingsDataStore.saveSelectedCelebrationEffect(style) }
    }

    fun purchaseCelebrationEffect(style: String, costCoins: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= costCoins) {
                val purchased = purchasedCelebrationEffects.value.split(",").toMutableSet()
                if (!purchased.contains(style)) {
                    repository.updateCoins(-costCoins)
                    purchased.add(style)
                    settingsDataStore.savePurchasedCelebrationEffects(purchased.joinToString(","))
                }
            }
        }
    }

    fun updateTimerMode(mode: String) {
        viewModelScope.launch { settingsDataStore.saveTimerMode(mode) }
    }

    fun updateMusicVolume(volume: Float) {
        viewModelScope.launch { settingsDataStore.saveMusicVolume(volume) }
    }

    fun updateSoundVolume(volume: Float) {
        viewModelScope.launch { settingsDataStore.saveSoundVolume(volume) }
    }

    fun toggleFavorite(countryId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(countryId)
        }
    }

    fun saveMemoryLevelResult(
        levelIndex: Int,
        elapsedTimeSec: Long,
        movesCount: Int,
        mismatchesCount: Int,
        score: Int,
        starsEarned: Int,
        isNewRecord: Boolean,
        coinsAwarded: Int,
        usedFlagIds: String
    ) {
        viewModelScope.launch {
            val existing = repository.getMemoryRecord(levelIndex)
            val isFirstTime = existing == null || !existing.isCompleted

            val bestTime = if (isFirstTime || elapsedTimeSec < (existing?.bestCompletionTimeSec ?: Long.MAX_VALUE) || (existing?.bestCompletionTimeSec ?: 0L) == 0L) {
                elapsedTimeSec
            } else existing!!.bestCompletionTimeSec

            val bestMoves = if (isFirstTime || movesCount < (existing?.bestMoves ?: Int.MAX_VALUE) || (existing?.bestMoves ?: 0) == 0) {
                movesCount
            } else existing!!.bestMoves

            val bestMismatches = if (isFirstTime || mismatchesCount < (existing?.bestMismatches ?: Int.MAX_VALUE)) {
                mismatchesCount
            } else existing!!.bestMismatches

            val highestScore = maxOf(existing?.highestScore ?: 0, score)
            val highestStars = maxOf(existing?.highestStars ?: 0, starsEarned)
            val attemptsCount = (existing?.attemptsCount ?: 0) + 1

            val updatedRecord = MemoryLevelEntity(
                levelIndex = levelIndex,
                isUnlocked = true,
                isCompleted = true,
                bestCompletionTimeSec = bestTime,
                bestMoves = bestMoves,
                bestMismatches = bestMismatches,
                highestScore = highestScore,
                highestStars = highestStars,
                attemptsCount = attemptsCount,
                lastFlagIds = usedFlagIds
            )

            repository.saveMemoryRecord(updatedRecord)

            // Auto-unlock next level record entry if levelIndex < 30
            val nextLevelIndex = levelIndex + 1
            if (nextLevelIndex <= 30) {
                val nextRecord = repository.getMemoryRecord(nextLevelIndex)
                if (nextRecord == null) {
                    repository.saveMemoryRecord(MemoryLevelEntity(levelIndex = nextLevelIndex, isUnlocked = true))
                } else if (!nextRecord.isUnlocked) {
                    repository.saveMemoryRecord(nextRecord.copy(isUnlocked = true))
                }
            }

            // Award coins if any
            if (coinsAwarded > 0) {
                repository.updateCoins(coinsAwarded)
            }
        }
    }

    fun saveSilentMapLevelResult(
        levelIndex: Int,
        starsEarned: Int,
        score: Int,
        isCompleted: Boolean,
        coinsAwarded: Int
    ) {
        viewModelScope.launch {
            if (isCompleted) {
                repository.completeLevel(
                    categoryId = "SILENT_MAP",
                    levelIndex = levelIndex,
                    stars = starsEarned,
                    score = score
                )
            }
            if (coinsAwarded > 0) {
                repository.updateCoins(coinsAwarded)
            }
        }
    }

    fun saveWhoAmILevelResult(
        levelIndex: Int,
        starsEarned: Int,
        score: Int,
        isCompleted: Boolean,
        coinsAwarded: Int
    ) {
        viewModelScope.launch {
            if (isCompleted) {
                repository.completeLevel(
                    categoryId = "WHO_AM_I",
                    levelIndex = levelIndex,
                    stars = starsEarned,
                    score = score
                )
            }
            if (coinsAwarded > 0) {
                repository.updateCoins(coinsAwarded)
            }
        }
    }

    fun saveSpotTheFakeLevelResult(
        levelIndex: Int,
        starsEarned: Int,
        score: Int,
        isCompleted: Boolean,
        coinsAwarded: Int
    ) {
        viewModelScope.launch {
            if (isCompleted) {
                repository.completeLevel(
                    categoryId = "SPOT_THE_FAKE",
                    levelIndex = levelIndex,
                    stars = starsEarned,
                    score = score
                )
            }
            if (coinsAwarded > 0) {
                repository.updateCoins(coinsAwarded)
            }
        }
    }

    fun saveCountryRankingLevelResult(
        levelIndex: Int,
        starsEarned: Int,
        score: Int,
        isCompleted: Boolean,
        coinsAwarded: Int
    ) {
        viewModelScope.launch {
            if (isCompleted) {
                repository.completeLevel(
                    categoryId = "RANKING",
                    levelIndex = levelIndex,
                    stars = starsEarned,
                    score = score
                )
            }
            if (coinsAwarded > 0) {
                repository.updateCoins(coinsAwarded)
            }
        }
    }

    fun saveQuickGeographyHighScore(highScore: Int) {
        viewModelScope.launch {
            repository.insertStat("quick_geography_high_score", highScore.toLong())
        }
    }

    fun awardQuickGeographyCoins(coins: Int) {
        if (coins > 0) {
            viewModelScope.launch {
                repository.updateCoins(coins)
            }
        }
    }

    fun completeQuickGeographyLevel(levelIndex: Int, score: Int) {
        viewModelScope.launch {
            val stars = when {
                score >= 2000 -> 3
                score >= 1000 -> 2
                score >= 500 -> 1
                else -> 1
            }
            repository.completeLevel(
                categoryId = "QUICK_GEOGRAPHY",
                levelIndex = levelIndex,
                stars = stars,
                score = score
            )
        }
    }
}
