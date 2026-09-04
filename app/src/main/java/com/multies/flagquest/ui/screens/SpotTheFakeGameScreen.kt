package com.multies.flagquest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.multies.flagquest.audio.AudioManager
import com.multies.flagquest.audio.HapticHelper
import com.multies.flagquest.audio.SoundType
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.model.GameFormat
import com.multies.flagquest.data.model.SpotTheFakeEngine
import com.multies.flagquest.data.model.SpotTheFakeLevelRepository
import com.multies.flagquest.data.model.SpotTheFakeOption
import com.multies.flagquest.data.model.SpotTheFakeQuestion
import com.multies.flagquest.data.model.SpotTheFakeScoreResult
import com.multies.flagquest.ui.components.FlagGraphic
import com.multies.flagquest.ui.components.GameHeader
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SpotTheFakeGameScreen(
    levelIndex: Int,
    viewModel: GameViewModel,
    navController: NavController,
    profile: UserProfileEntity?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lang = profile?.selectedLanguage ?: "en"

    val allCountries by viewModel.countries.collectAsState()
    val levelConfig = remember(levelIndex) { SpotTheFakeLevelRepository.getLevel(levelIndex) }

    // Questions generated for this level
    var questions by remember { mutableStateOf<List<SpotTheFakeQuestion>>(emptyList()) }
    var currentRoundIndex by remember { mutableIntStateOf(0) }

    // Round state
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isInputLocked by remember { mutableStateOf(false) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var isCorrectAnswer by remember { mutableStateOf(false) }

    // Scoring & Stats
    var correctCount by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var maxStreak by remember { mutableIntStateOf(0) }
    var totalTimeSec by remember { mutableLongStateOf(0L) }

    // Timer state
    var timerRemainingSec by remember { mutableIntStateOf(levelConfig.timerSecPerRound) }
    var isTimerActive by remember { mutableStateOf(false) }

    // Dialogs
    var isPaused by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    var resultSummary by remember { mutableStateOf<SpotTheFakeScoreResult?>(null) }

    // Initialize level questions
    fun initializeGame() {
        if (allCountries.isNotEmpty()) {
            questions = SpotTheFakeEngine.generateLevelQuestions(levelConfig, allCountries)
            currentRoundIndex = 0
            selectedOptionIndex = null
            isInputLocked = false
            isAnswerSubmitted = false
            isCorrectAnswer = false
            correctCount = 0
            currentStreak = 0
            maxStreak = 0
            totalTimeSec = 0L
            timerRemainingSec = levelConfig.timerSecPerRound
            isTimerActive = true
            showVictoryDialog = false
            resultSummary = null
        }
    }

    LaunchedEffect(allCountries, levelIndex) {
        initializeGame()
    }

    // Timer Coroutine
    LaunchedEffect(isTimerActive, timerRemainingSec, isPaused) {
        if (isTimerActive && !isPaused && timerRemainingSec > 0 && !isAnswerSubmitted) {
            delay(1000L)
            timerRemainingSec -= 1
            totalTimeSec += 1
            if (timerRemainingSec == 0) {
                // Time expired! Mark as incorrect answer
                isInputLocked = true
                isAnswerSubmitted = true
                isCorrectAnswer = false
                currentStreak = 0
                AudioManager.playSoundEffect(SoundType.WRONG)
                HapticHelper.triggerWrong(context)
            }
        }
    }

    val currentQuestion = questions.getOrNull(currentRoundIndex)

    fun submitAnswer(chosenIndex: Int) {
        if (isInputLocked || currentQuestion == null) return

        isInputLocked = true
        selectedOptionIndex = chosenIndex
        isAnswerSubmitted = true
        isTimerActive = false

        val chosenOption = currentQuestion.options.getOrNull(chosenIndex)

        val isCorrect = when (currentQuestion.format) {
            GameFormat.FORMAT_A, GameFormat.FORMAT_B -> chosenOption?.isFake == true
            GameFormat.FORMAT_C -> chosenIndex == currentQuestion.fakeOptionIndex
        }

        isCorrectAnswer = isCorrect

        if (isCorrect) {
            correctCount++
            currentStreak++
            if (currentStreak > maxStreak) maxStreak = currentStreak
            AudioManager.playSoundEffect(SoundType.CORRECT)
            HapticHelper.triggerCorrect(context)
        } else {
            currentStreak = 0
            AudioManager.playSoundEffect(SoundType.WRONG)
            HapticHelper.triggerWrong(context)
        }
    }

    fun nextRound() {
        if (currentRoundIndex + 1 < questions.size) {
            currentRoundIndex++
            selectedOptionIndex = null
            isInputLocked = false
            isAnswerSubmitted = false
            isCorrectAnswer = false
            timerRemainingSec = levelConfig.timerSecPerRound
            isTimerActive = true
        } else {
            // All rounds complete -> Show victory dialog!
            val summary = SpotTheFakeEngine.calculateResult(
                levelIndex = levelIndex,
                correctAnswers = correctCount,
                totalRounds = levelConfig.roundsCount,
                maxStreak = maxStreak,
                timeTakenSec = totalTimeSec
            )
            resultSummary = summary
            showVictoryDialog = true

            // Persist progress to database
            viewModel.saveSpotTheFakeLevelResult(
                levelIndex = levelIndex,
                starsEarned = summary.stars,
                score = summary.score,
                isCompleted = true,
                coinsAwarded = summary.coinsEarned
            )
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
        ) {
            // Top action bar with back, title, pause
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Locales.get("back", lang, context)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Locales.get("spot_the_fake", lang, context),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = Locales.get("question_progress_format", lang, context).format(currentRoundIndex + 1, levelConfig.roundsCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        isPaused = true
                        showPauseDialog = true
                    },
                    modifier = Modifier.testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = Locales.get("pause_title", lang, context)
                    )
                }
            }

            // Timer Progress Bar
            val timerFraction = timerRemainingSec.toFloat() / levelConfig.timerSecPerRound.toFloat()
            LinearProgressIndicator(
                progress = { timerFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(horizontal = 16.dp),
                color = if (timerRemainingSec <= 5) Color(0xFFE53935) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (currentQuestion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // Prompt Card
                    val promptText = when (currentQuestion.format) {
                        GameFormat.FORMAT_A -> Locales.get("prompt_format_a", lang, context).format(currentQuestion.targetCountry.getLocalizedName(lang))
                        GameFormat.FORMAT_B -> Locales.get("prompt_format_b", lang, context)
                        GameFormat.FORMAT_C -> Locales.get("prompt_format_c", lang, context)
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = promptText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }

                    // Options Grid or Format C Display
                    if (currentQuestion.format == GameFormat.FORMAT_A || currentQuestion.format == GameFormat.FORMAT_B) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (rowIndex in 0..1) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    for (colIndex in 0..1) {
                                        val index = rowIndex * 2 + colIndex
                                        if (index in currentQuestion.options.indices) {
                                            val option = currentQuestion.options[index]
                                            val isSelected = selectedOptionIndex == index
                                            val isFake = option.isFake

                                            val borderColor = when {
                                                !isAnswerSubmitted -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                isFake -> Color(0xFF43A047) // Green for fake flag target
                                                isSelected -> Color(0xFFE53935) // Red if picked authentic flag
                                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                            }

                                            Card(
                                                shape = RoundedCornerShape(16.dp),
                                                border = BorderStroke(3.dp, borderColor),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1.2f)
                                                    .testTag("flag_option_$index")
                                                    .clickable(enabled = !isInputLocked) {
                                                        submitAnswer(index)
                                                    }
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    FlagGraphic(
                                                        countryId = option.country.id,
                                                        isFake = option.isFake,
                                                        mutation = option.mutation,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(8.dp)
                                                    )

                                                    if (isAnswerSubmitted) {
                                                        if (isFake) {
                                                            Surface(
                                                                color = Color(0xFF43A047),
                                                                shape = CircleShape,
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .align(Alignment.TopEnd)
                                                                    .padding(4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = null,
                                                                    tint = Color.White,
                                                                    modifier = Modifier.padding(2.dp)
                                                                )
                                                            }
                                                        } else if (isSelected) {
                                                            Surface(
                                                                color = Color(0xFFE53935),
                                                                shape = CircleShape,
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .align(Alignment.TopEnd)
                                                                    .padding(4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = null,
                                                                    tint = Color.White,
                                                                    modifier = Modifier.padding(2.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Format C Single Flag Choice
                        val fakeOption = currentQuestion.options.firstOrNull { it.isFake }
                        val displayOption = if (currentQuestion.fakeOptionIndex == 0) fakeOption else currentQuestion.options.firstOrNull { !it.isFake }

                        if (displayOption != null) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.5f)
                                    .padding(vertical = 12.dp)
                            ) {
                                FlagGraphic(
                                    countryId = displayOption.country.id,
                                    isFake = displayOption.isFake,
                                    mutation = displayOption.mutation,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = { submitAnswer(1) }, // 1 = Authentic
                                    enabled = !isInputLocked,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("btn_authentic")
                                ) {
                                    Text(
                                        text = Locales.get("authentic", lang, context),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Button(
                                    onClick = { submitAnswer(0) }, // 0 = Fake
                                    enabled = !isInputLocked,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("btn_fake")
                                ) {
                                    Text(
                                        text = Locales.get("fake", lang, context),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Answer Explanation Feedback Panel
                    AnimatedVisibility(
                        visible = isAnswerSubmitted,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCorrectAnswer) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                            border = BorderStroke(1.5.dp, if (isCorrectAnswer) Color(0xFF43A047) else Color(0xFFE53935)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 24.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCorrectAnswer) Icons.Default.Check else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isCorrectAnswer) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Locales.get("vexillological_explanation", lang, context),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrectAnswer) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val explanationText = when (lang) {
                                    "ar" -> currentQuestion.explanationAr
                                    "de" -> currentQuestion.explanationDe
                                    "fr" -> currentQuestion.explanationFr
                                    else -> currentQuestion.explanationEn
                                }

                                Text(
                                    text = explanationText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { nextRound() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("next_round_btn")
                                ) {
                                    Text(
                                        text = if (currentRoundIndex + 1 < questions.size) Locales.get("next_round", lang, context) else Locales.get("see_results", lang, context),
                                        fontWeight = FontWeight.Bold
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
            title = { Text(Locales.get("pause_title", lang, context)) },
            text = { Text(Locales.get("restart_confirmation_desc", lang, context)) },
            confirmButton = {
                Button(
                    onClick = {
                        showPauseDialog = false
                        isPaused = false
                    }
                ) {
                    Text(Locales.get("resume", lang, context))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPauseDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text(Locales.get("home", lang, context))
                }
            }
        )
    }

    // Victory/Results Dialog
    if (showVictoryDialog && resultSummary != null) {
        val summary = resultSummary!!
        AlertDialog(
            onDismissRequest = { },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Locales.get("spot_the_fake_level_completed", lang, context).format(levelIndex),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        repeat(3) { starIdx ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (starIdx < summary.stars) Color(0xFFFFC107) else Color.LightGray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Locales.get("accuracy", lang, context))
                        Text("${summary.accuracyPercent}%", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Locales.get("streak_bonus", lang, context).format(summary.maxStreak))
                        Text("+${summary.coinsEarned} Coins", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            showVictoryDialog = false
                            initializeGame()
                        },
                        modifier = Modifier.testTag("replay_button")
                    ) {
                        Text(Locales.get("play_again", lang, context))
                    }

                    if (levelIndex < 100) {
                        Button(
                            onClick = {
                                showVictoryDialog = false
                                navController.navigate("spot_the_fake_game/${levelIndex + 1}") {
                                    popUpTo("spot_the_fake_game/$levelIndex") { inclusive = true }
                                }
                            },
                            modifier = Modifier.testTag("next_level_button")
                        ) {
                            Text(Locales.get("next_level", lang, context))
                        }
                    } else {
                        Button(
                            onClick = {
                                showVictoryDialog = false
                                navController.popBackStack()
                            },
                            modifier = Modifier.testTag("done_button")
                        ) {
                            Text(Locales.get("home", lang, context))
                        }
                    }
                }
            }
        )
    }
}
