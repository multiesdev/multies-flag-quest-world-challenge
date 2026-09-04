package com.multies.flagquest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.multies.flagquest.audio.AudioManager
import com.multies.flagquest.audio.HapticHelper
import com.multies.flagquest.audio.SoundType
import com.multies.flagquest.data.local.entity.ProgressEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.SilentMapEngine
import com.multies.flagquest.data.model.SilentMapLevelConfig
import com.multies.flagquest.data.model.SilentMapQuestion
import com.multies.flagquest.data.model.SilentMapScoreResult
import com.multies.flagquest.ui.components.GameHeader
import com.multies.flagquest.ui.components.SilentMapCanvas
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SilentMapGameScreen(
    levelIndex: Int,
    viewModel: GameViewModel,
    navController: NavController,
    profile: UserProfileEntity?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lang = profile?.selectedLanguage ?: "en"

    val allCountries by viewModel.countries.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val existingProgress = progressList.find { it.categoryId == "SILENT_MAP" && it.levelIndex == levelIndex }

    val levelConfig = remember(levelIndex) { SilentMapLevelConfig.getConfig(levelIndex) }

    // State Variables
    var questions by remember { mutableStateOf<List<SilentMapQuestion>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var currentScore by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var maxStreak by remember { mutableIntStateOf(0) }
    var correctAnswersCount by remember { mutableIntStateOf(0) }

    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isInputLocked by remember { mutableStateOf(false) }

    var isTimerRunning by remember { mutableStateOf(false) }
    var elapsedTimeSec by remember { mutableLongStateOf(0L) }

    var isPaused by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showVictoryDialog by remember { mutableStateOf(false) }

    // Hints state
    var revealedContinent by remember { mutableStateOf<String?>(null) }
    var revealedFirstLetter by remember { mutableStateOf<String?>(null) }
    var removedOptionIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showFlagPeek by remember { mutableStateOf(false) }
    var hintsUsedTotal by remember { mutableIntStateOf(0) }

    var resultSummary by remember { mutableStateOf<SilentMapScoreResult?>(null) }

    // Initialize Game Board
    fun initializeGame() {
        if (allCountries.isNotEmpty()) {
            val newQuestions = SilentMapEngine.createQuestionsForLevel(levelIndex, allCountries, lang)
            questions = newQuestions
            currentQuestionIndex = 0
            currentScore = 0
            currentStreak = 0
            maxStreak = 0
            correctAnswersCount = 0
            selectedOptionIndex = null
            isInputLocked = false
            elapsedTimeSec = 0L
            isTimerRunning = true
            showVictoryDialog = false
            showPauseDialog = false

            // Reset hint states for question
            revealedContinent = null
            revealedFirstLetter = null
            removedOptionIndices = emptySet()
            showFlagPeek = false
            hintsUsedTotal = 0
        }
    }

    LaunchedEffect(allCountries, levelIndex) {
        if (allCountries.isNotEmpty() && questions.isEmpty()) {
            initializeGame()
        }
    }

    // Timer Loop
    LaunchedEffect(isTimerRunning, isPaused) {
        if (isTimerRunning && !isPaused) {
            while (isTimerRunning && !isPaused) {
                delay(1000L)
                elapsedTimeSec++
            }
        }
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    // Option Click Handler
    fun onOptionSelected(index: Int) {
        if (isInputLocked || isPaused || currentQuestion == null) return
        if (removedOptionIndices.contains(index)) return

        isInputLocked = true
        selectedOptionIndex = index

        val isCorrect = index == currentQuestion.correctIndex

        if (isCorrect) {
            AudioManager.playSoundEffect(SoundType.CORRECT)
            HapticHelper.triggerCorrect(context)

            correctAnswersCount++
            currentStreak++
            if (currentStreak > maxStreak) maxStreak = currentStreak
            currentScore += 100 + (currentStreak * 20)
        } else {
            AudioManager.playSoundEffect(SoundType.WRONG)
            HapticHelper.triggerWrong(context)
            currentStreak = 0
        }

        coroutineScope.launch {
            delay(900L) // Brief feedback display

            if (currentQuestionIndex < questions.size - 1) {
                // Next Question
                currentQuestionIndex++
                selectedOptionIndex = null
                revealedContinent = null
                revealedFirstLetter = null
                removedOptionIndices = emptySet()
                showFlagPeek = false
                isInputLocked = false
            } else {
                // Completed All Questions in Level!
                isTimerRunning = false
                AudioManager.playSoundEffect(SoundType.MILESTONE)
                HapticHelper.triggerMilestone(context)

                val isNewBest = existingProgress == null || currentScore > existingProgress.highestScore

                val scoreResult = SilentMapEngine.calculateScore(
                    levelIndex = levelIndex,
                    questionCount = questions.size,
                    correctCount = correctAnswersCount,
                    elapsedTimeSec = elapsedTimeSec,
                    maxStreak = maxStreak,
                    hintsUsedCount = hintsUsedTotal,
                    isNewRecord = isNewBest
                )

                resultSummary = scoreResult

                // Save Progress via ViewModel
                viewModel.saveSilentMapLevelResult(
                    levelIndex = levelIndex,
                    starsEarned = scoreResult.starsEarned,
                    score = scoreResult.finalScore,
                    isCompleted = true,
                    coinsAwarded = scoreResult.coinsEarned
                )

                showVictoryDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                profile = profile,
                onSettingsClick = { navController.navigate("settings") },
                onRefillLivesClick = { viewModel.refillLives() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Top HUD Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Locales.get("back", lang),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${Locales.get("level", lang)} $levelIndex",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Progress Badge & Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Question Counter Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = String.format(
                                Locales.get("question_progress_format", lang),
                                currentQuestionIndex + 1,
                                questions.size.coerceAtLeast(1)
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Timer Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTime(elapsedTimeSec),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Pause Button
                    IconButton(
                        onClick = {
                            isPaused = true
                            showPauseDialog = true
                        },
                        modifier = Modifier.testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = Locales.get("pause_title", lang),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Streak Indicator & Score Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStreak > 1) {
                    Surface(
                        color = Color(0xFFFF9800),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = String.format(Locales.get("streak_bonus", lang), currentStreak),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = "${Locales.get("score", lang)}: $currentScore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (currentQuestion != null) {
                // Map Canvas Display Component
                SilentMapCanvas(
                    question = currentQuestion,
                    lang = lang,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hints Row Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hint 1: Continent
                    OutlinedButton(
                        onClick = {
                            if (revealedContinent == null) {
                                hintsUsedTotal++
                                revealedContinent = currentQuestion.targetCountry.getLocalizedContinent(lang)
                                AudioManager.playSoundEffect(SoundType.TICK)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hint_continent_button")
                    ) {
                        Icon(imageVector = Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = revealedContinent ?: Locales.get("continent", lang),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Hint 2: 50:50
                    OutlinedButton(
                        onClick = {
                            if (removedOptionIndices.isEmpty()) {
                                hintsUsedTotal++
                                val incorrectIndices = (0..3).filter { it != currentQuestion.correctIndex }.shuffled()
                                removedOptionIndices = incorrectIndices.take(2).toSet()
                                AudioManager.playSoundEffect(SoundType.TICK)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hint_fifty_fifty_button")
                    ) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "50:50", fontSize = 11.sp)
                    }

                    // Hint 3: Flag Peek
                    OutlinedButton(
                        onClick = {
                            if (!showFlagPeek) {
                                hintsUsedTotal++
                                showFlagPeek = true
                                AudioManager.playSoundEffect(SoundType.TICK)
                                coroutineScope.launch {
                                    delay(1500L)
                                    showFlagPeek = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hint_flag_peek_button")
                    ) {
                        Icon(imageVector = Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showFlagPeek) currentQuestion.targetCountry.flagEmoji else Locales.get("hint_flag_peek", lang),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4 Answer Options Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    currentQuestion.options.forEachIndexed { index, optionCountry ->
                        val isSelected = selectedOptionIndex == index
                        val isCorrect = index == currentQuestion.correctIndex
                        val isRemoved = removedOptionIndices.contains(index)

                        val buttonColor = when {
                            selectedOptionIndex != null && isCorrect -> Color(0xFF2E7D32) // Green
                            selectedOptionIndex != null && isSelected && !isCorrect -> Color(0xFFC62828) // Red
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val textColor = when {
                            selectedOptionIndex != null && (isCorrect || isSelected) -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        if (!isRemoved) {
                            Button(
                                onClick = { onOptionSelected(index) },
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("answer_option_$index")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = optionCountry.getLocalizedName(lang),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (selectedOptionIndex != null && isCorrect) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    } else if (selectedOptionIndex != null && isSelected && !isCorrect) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        } else {
                            // Disabled / Removed Option Slot
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "---",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Pause Dialog
    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = {
                showPauseDialog = false
                isPaused = false
            },
            title = {
                Text(text = Locales.get("pause_title", lang), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = Locales.get("restart_confirmation_desc", lang))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPauseDialog = false
                        isPaused = false
                    }
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = Locales.get("resume", lang))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPauseDialog = false
                        initializeGame()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = Locales.get("restart", lang))
                }
            }
        )
    }

    // Victory Dialog
    if (showVictoryDialog && resultSummary != null) {
        val summary = resultSummary!!
        AlertDialog(
            onDismissRequest = {},
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format(Locales.get("silent_map_level_completed", lang), levelIndex),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Stars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            val active = i <= summary.starsEarned
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (active) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    if (summary.isNewRecord) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFFF9800),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = Locales.get("new_record", lang),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResultRow(label = Locales.get("score", lang), value = "${summary.finalScore}")
                    ResultRow(label = Locales.get("accuracy", lang), value = "${summary.accuracyPercentage}%")
                    ResultRow(label = Locales.get("current_time", lang), value = formatTime(elapsedTimeSec))
                    if (summary.coinsEarned > 0) {
                        ResultRow(label = Locales.get("coins", lang), value = "+${summary.coinsEarned} 🪙")
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { initializeGame() },
                        modifier = Modifier.testTag("replay_button")
                    ) {
                        Text(text = Locales.get("play_again", lang))
                    }

                    if (levelIndex < 50) {
                        Button(
                            onClick = {
                                navController.navigate("silent_map_game/${levelIndex + 1}") {
                                    popUpTo("silent_map_game/$levelIndex") { inclusive = true }
                                }
                            },
                            modifier = Modifier.testTag("next_level_button")
                        ) {
                            Text(text = Locales.get("next_level", lang))
                        }
                    } else {
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.testTag("done_button")
                        ) {
                            Text(text = Locales.get("ok", lang))
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
