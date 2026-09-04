package com.multies.flagquest.ui.screens

import android.os.SystemClock
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.multies.flagquest.data.model.*
import com.multies.flagquest.data.repository.CountryMapRepository
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class QuickGeoState {
    INSTRUCTIONS,
    COUNTDOWN,
    ACTIVE_SESSION,
    PAUSED,
    RESULTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickGeographyGameScreen(
    levelIndex: Int = 1,
    viewModel: GameViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val lang by viewModel.selectedLanguage.collectAsState()
    val countryCatalog by viewModel.countries.collectAsState()
    val allStats by viewModel.allStatistics.collectAsState()

    val savedHighScore = remember(allStats) {
        allStats.firstOrNull { it.statKey == "quick_geography_high_score" }?.statValue?.toInt() ?: 0
    }

    var gameState by remember { mutableStateOf(QuickGeoState.INSTRUCTIONS) }
    var countdownValue by remember { mutableStateOf(3) }

    // Session Data
    var questionQueue by remember { mutableStateOf<List<QuickGeographyQuestion>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    // Performance & Scoring
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var maxStreak by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var totalAnswered by remember { mutableStateOf(0) }
    var totalResponseTimeMs by remember { mutableStateOf(0L) }
    var questionStartTimeMs by remember { mutableStateOf(0L) }

    // Timer (60.0s session)
    val sessionDurationMs = 60_000L
    var remainingMs by remember { mutableStateOf(sessionDurationMs) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var lastTickTimeMs by remember { mutableStateOf(0L) }

    var isNewRecord by remember { mutableStateOf(false) }
    var coinsEarned by remember { mutableStateOf(0) }

    val currentQuestion = questionQueue.getOrNull(currentQuestionIndex)

    // Monotonic Timer Coroutine
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            lastTickTimeMs = SystemClock.elapsedRealtime()
            while (isTimerRunning && remainingMs > 0) {
                delay(50)
                val now = SystemClock.elapsedRealtime()
                val delta = now - lastTickTimeMs
                lastTickTimeMs = now
                remainingMs = (remainingMs - delta).coerceAtLeast(0L)

                if (remainingMs <= 0L) {
                    isTimerRunning = false
                    // Finish session
                    val isRecord = score > savedHighScore
                    isNewRecord = isRecord
                    coinsEarned = (score / 5).coerceAtLeast(10)

                    if (isRecord) {
                        viewModel.saveQuickGeographyHighScore(score)
                    }
                    viewModel.awardQuickGeographyCoins(coinsEarned)
                    viewModel.completeQuickGeographyLevel(levelIndex, score)

                    gameState = QuickGeoState.RESULTS
                }
            }
        }
    }

    // Function to start a new session
    fun startNewSession() {
        questionQueue = QuickGeographyEngine.generateSessionQuestions(
            levelIndex = levelIndex,
            countryCatalog = countryCatalog
        )
        currentQuestionIndex = 0
        selectedOptionIndex = null
        isAnswered = false
        score = 0
        streak = 0
        maxStreak = 0
        correctCount = 0
        totalAnswered = 0
        totalResponseTimeMs = 0L
        remainingMs = sessionDurationMs
        isNewRecord = false
        coinsEarned = 0

        gameState = QuickGeoState.COUNTDOWN
        countdownValue = 3
    }

    // Countdown logic
    LaunchedEffect(gameState, countdownValue) {
        if (gameState == QuickGeoState.COUNTDOWN) {
            if (countdownValue > 0) {
                delay(1000)
                countdownValue--
            } else {
                gameState = QuickGeoState.ACTIVE_SESSION
                questionStartTimeMs = SystemClock.elapsedRealtime()
                isTimerRunning = true
            }
        }
    }

    // Answer selection logic
    fun submitAnswer(optionIndex: Int) {
        if (isAnswered || currentQuestion == null || gameState != QuickGeoState.ACTIVE_SESSION) return
        isAnswered = true
        selectedOptionIndex = optionIndex

        val now = SystemClock.elapsedRealtime()
        val responseMs = now - questionStartTimeMs
        totalResponseTimeMs += responseMs
        totalAnswered++

        val isCorrect = optionIndex == currentQuestion.correctOptionIndex
        if (isCorrect) {
            correctCount++
            streak++
            if (streak > maxStreak) maxStreak = streak

            val qScore = QuickGeographyEngine.calculateQuestionScore(
                baseScore = 100,
                elapsedMs = responseMs,
                streak = streak
            )
            score += qScore
        } else {
            streak = 0
        }

        // Advance question after 300ms feedback
        CoroutineScope(Dispatchers.Main).launch {
            delay(300)
            if (gameState == QuickGeoState.ACTIVE_SESSION && remainingMs > 0) {
                if (currentQuestionIndex < questionQueue.size - 1) {
                    currentQuestionIndex++
                    selectedOptionIndex = null
                    isAnswered = false
                    questionStartTimeMs = SystemClock.elapsedRealtime()
                } else {
                    // Queue exhausted, generate extra batch
                    val newBatch = QuickGeographyEngine.generateSessionQuestions(
                        levelIndex = levelIndex,
                        countryCatalog = countryCatalog,
                        seed = System.currentTimeMillis()
                    )
                    questionQueue = questionQueue + newBatch
                    currentQuestionIndex++
                    selectedOptionIndex = null
                    isAnswered = false
                    questionStartTimeMs = SystemClock.elapsedRealtime()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Locales.get("quick_geography", lang, context),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (gameState == QuickGeoState.ACTIVE_SESSION) {
                                isTimerRunning = false
                                gameState = QuickGeoState.PAUSED
                            } else {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.testTag("quick_geo_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$savedHighScore",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (gameState) {
                QuickGeoState.INSTRUCTIONS -> {
                    QuickGeoInstructionsView(
                        lang = lang,
                        context = context,
                        highScore = savedHighScore,
                        onStartClicked = { startNewSession() }
                    )
                }

                QuickGeoState.COUNTDOWN -> {
                    QuickGeoCountdownView(countdownValue = countdownValue, lang = lang, context = context)
                }

                QuickGeoState.ACTIVE_SESSION -> {
                    if (currentQuestion != null) {
                        QuickGeoActiveGameplayView(
                            lang = lang,
                            context = context,
                            question = currentQuestion,
                            score = score,
                            streak = streak,
                            remainingMs = remainingMs,
                            sessionDurationMs = sessionDurationMs,
                            selectedOptionIndex = selectedOptionIndex,
                            isAnswered = isAnswered,
                            onOptionSelected = { submitAnswer(it) }
                        )
                    }
                }

                QuickGeoState.PAUSED -> {
                    AlertDialog(
                        onDismissRequest = {
                            gameState = QuickGeoState.ACTIVE_SESSION
                            isTimerRunning = true
                        },
                        title = { Text(Locales.get("quick_geography_challenge", lang, context)) },
                        text = { Text("Game is paused. What would you like to do?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    gameState = QuickGeoState.ACTIVE_SESSION
                                    isTimerRunning = true
                                },
                                modifier = Modifier.testTag("resume_quiz_button")
                            ) {
                                Text("Resume")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.testTag("leave_quiz_button")
                            ) {
                                Text("Leave")
                            }
                        }
                    )
                }

                QuickGeoState.RESULTS -> {
                    val avgSpeedSec = if (totalAnswered > 0) (totalResponseTimeMs / 1000f) / totalAnswered else 0f
                    val accuracy = if (totalAnswered > 0) ((correctCount.toFloat() / totalAnswered) * 100).roundToInt() else 0

                    QuickGeoResultsView(
                        lang = lang,
                        context = context,
                        levelIndex = levelIndex,
                        score = score,
                        correctCount = correctCount,
                        totalAnswered = totalAnswered,
                        accuracyPercent = accuracy,
                        maxStreak = maxStreak,
                        avgSpeedSec = avgSpeedSec,
                        isNewRecord = isNewRecord,
                        coinsEarned = coinsEarned,
                        onPlayAgain = { startNewSession() },
                        onNextLevel = {
                            if (levelIndex < 50) {
                                navController.navigate("quick_geography_game/${levelIndex + 1}") {
                                    popUpTo("quick_geography_game/$levelIndex") { inclusive = true }
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onReturnToChallenges = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickGeoInstructionsView(
    lang: String,
    context: android.content.Context,
    highScore: Int,
    onStartClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = Locales.get("quick_geography", lang, context),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = Locales.get("quick_geography_instructions", lang, context),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "High Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$highScore pts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onStartClicked,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .testTag("start_quick_geo_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Locales.get("start_quiz", lang, context),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickGeoCountdownView(
    countdownValue: Int,
    lang: String,
    context: android.content.Context
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Locales.get("ready_set_go", lang, context),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (countdownValue > 0) "$countdownValue" else "GO!",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickGeoActiveGameplayView(
    lang: String,
    context: android.content.Context,
    question: QuickGeographyQuestion,
    score: Int,
    streak: Int,
    remainingMs: Long,
    sessionDurationMs: Long,
    selectedOptionIndex: Int?,
    isAnswered: Boolean,
    onOptionSelected: (Int) -> Unit
) {
    val remainingSeconds = (remainingMs / 1000f).coerceAtLeast(0f)
    val progressFraction = (remainingMs.toFloat() / sessionDurationMs.toFloat()).coerceIn(0f, 1f)
    val multiplier = QuickGeographyEngine.getStreakMultiplier(streak)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Timer & Stats Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score Badge
            Column {
                Text(
                    text = "SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Streak & Multiplier Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (streak >= 3) Color(0xFFFF9800) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (streak >= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Streak: $streak (x${String.format("%.1f", multiplier)})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (streak >= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Seconds Remaining Badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${remainingSeconds.roundToInt()}s",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (remainingSeconds <= 10f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time Progress Bar
        LinearProgressIndicator(
            progress = progressFraction,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = if (remainingSeconds <= 10f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Question Card Container
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Visual Indicator (Flag or Map Silhouette) if applicable
                if (!question.flagEmoji.isNullOrBlank()) {
                    Text(
                        text = question.flagEmoji,
                        fontSize = 64.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else if (!question.mapSvgPath.isNullOrBlank() && question.countryId != null) {
                    val path = remember(question.countryId) {
                        CountryMapRepository.loadCountryPath(question.countryId, context)
                    }
                    if (path != null) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val bounds = remember(path) { path.getBounds() }
                            val fillColor = MaterialTheme.colorScheme.primary
                            val strokeColor = MaterialTheme.colorScheme.onSurface

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (bounds.width > 0 && bounds.height > 0) {
                                    val scaleFactor = (size.minDimension * 0.85f) / maxOf(bounds.width, bounds.height)
                                    val dx = (size.width - bounds.width * scaleFactor) / 2f - bounds.left * scaleFactor
                                    val dy = (size.height - bounds.height * scaleFactor) / 2f - bounds.top * scaleFactor

                                    translate(dx, dy) {
                                        scale(scaleFactor, scaleFactor, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                                            drawPath(path = path, color = fillColor)
                                            drawPath(path = path, color = strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f / scaleFactor))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Prompt Text
                Text(
                    text = question.getLocalizedPrompt(lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Options List
                val options = question.getLocalizedOptions(lang)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    options.forEachIndexed { index, optionText ->
                        val isSelected = selectedOptionIndex == index
                        val isCorrect = index == question.correctOptionIndex

                        val containerColor = when {
                            isAnswered && isCorrect -> Color(0xFF4CAF50)
                            isAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val textColor = when {
                            isAnswered && (isCorrect || (isSelected && !isCorrect)) -> Color.White
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = containerColor,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("quick_geo_option_$index")
                                .clickable(enabled = !isAnswered) { onOptionSelected(index) }
                                .border(
                                    width = 1.5.dp,
                                    color = if (isAnswered && isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isAnswered) {
                                    if (isCorrect) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                    } else if (isSelected) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickGeoResultsView(
    lang: String,
    context: android.content.Context,
    levelIndex: Int,
    score: Int,
    correctCount: Int,
    totalAnswered: Int,
    accuracyPercent: Int,
    maxStreak: Int,
    avgSpeedSec: Float,
    isNewRecord: Boolean,
    coinsEarned: Int,
    onPlayAgain: () -> Unit,
    onNextLevel: () -> Unit,
    onReturnToChallenges: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isNewRecord) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFB300),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = Locales.get("new_high_score", lang, context),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        Text(
            text = Locales.get("final_results", lang, context),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big Score Display
        Text(
            text = "$score",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Total Score",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Grid Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(Locales.get("questions_answered", lang, context), style = MaterialTheme.typography.bodyMedium)
                    Text("$correctCount / $totalAnswered", fontWeight = FontWeight.Bold)
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(Locales.get("accuracy", lang, context), style = MaterialTheme.typography.bodyMedium)
                    Text("$accuracyPercent%", fontWeight = FontWeight.Bold)
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(Locales.get("longest_streak", lang, context), style = MaterialTheme.typography.bodyMedium)
                    Text("$maxStreak 🔥", fontWeight = FontWeight.Bold)
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(Locales.get("avg_response_time", lang, context), style = MaterialTheme.typography.bodyMedium)
                    Text("${String.format("%.1f", avgSpeedSec)}s", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        if (levelIndex < 50) {
            Button(
                onClick = onNextLevel,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_level_button")
            ) {
                Text(
                    text = Locales.get("next_level", lang, context),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = onPlayAgain,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("play_again_quick_geo_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Locales.get("play_again", lang, context),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onReturnToChallenges,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("return_challenges_quick_geo_button")
        ) {
            Text(
                text = Locales.get("return_to_challenges", lang, context),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
