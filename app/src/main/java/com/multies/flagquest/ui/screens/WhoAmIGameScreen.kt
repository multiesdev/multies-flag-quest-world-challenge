package com.multies.flagquest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
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
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.features.whoami.engine.WhoAmIEngine
import com.multies.flagquest.features.whoami.engine.WhoAmIQuestion
import com.multies.flagquest.features.whoami.engine.WhoAmIScoreResult
import com.multies.flagquest.ui.components.GameHeader
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.viewmodel.GameViewModel
import com.multies.flagquest.features.whoami.WhoAmIViewModel
import com.multies.flagquest.features.whoami.WhoAmIUiState
import com.multies.flagquest.features.whoami.WhoAmIQuestionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WhoAmIGameScreen(
    levelIndex: Int,
    viewModel: GameViewModel,
    navController: NavController,
    profile: UserProfileEntity?
) {
    val context = LocalContext.current
    val lang = profile?.selectedLanguage ?: "en"

    val allCountries by viewModel.countries.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val existingProgress = progressList.find { it.categoryId == "WHO_AM_I" && it.levelIndex == levelIndex }
    val existingHighScore = existingProgress?.highestScore ?: 0

    val whoAmIViewModel: WhoAmIViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by whoAmIViewModel.uiState.collectAsState()

    val questions = uiState.questions
    val currentQuestionIndex = uiState.currentQuestionIndex
    val revealedCluesCount = uiState.revealedCluesCount
    val questionState = uiState.questionState
    val currentScore = uiState.currentScore
    val currentStreak = uiState.currentStreak
    val correctAnswersCount = uiState.correctAnswersCount
    val selectedOptionIndex = uiState.selectedOptionIndex
    val isInputLocked = uiState.isInputLocked
    val elapsedTimeSec = uiState.elapsedTimeSec
    val isPaused = uiState.isPaused
    val resultSummary = uiState.resultSummary
    val showVictoryDialog = uiState.showVictoryDialog

    var showPauseDialog by remember { mutableStateOf(false) }

    // Initialize Game Board
    fun initializeGame() {
        if (allCountries.isNotEmpty()) {
            whoAmIViewModel.startSession(
                levelIndex = levelIndex,
                countries = allCountries,
                lang = lang,
                existingHighScore = existingHighScore
            )
        }
    }

    LaunchedEffect(allCountries, levelIndex, existingHighScore) {
        if (allCountries.isNotEmpty() && questions.isEmpty()) {
            initializeGame()
        }
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    // Option Click Handler
    fun onOptionSelected(index: Int) {
        whoAmIViewModel.selectOption(index, levelIndex, existingHighScore)
    }

    // Reveal Next Clue Handler
    fun onRevealNextClue() {
        whoAmIViewModel.revealNextClue()
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
                        text = "${Locales.get("who_am_i", lang)} • L$levelIndex",
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
                            whoAmIViewModel.setPaused(true)
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

            // Streak & Points Indicator Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Available Points Badge
                val currentCluePoints = when (revealedCluesCount) {
                    1 -> 500
                    2 -> 400
                    3 -> 300
                    4 -> 200
                    else -> 100
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locales.get("available_points", lang), currentCluePoints),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

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
                }

                Text(
                    text = "${Locales.get("score", lang)}: $currentScore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (currentQuestion != null) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Header Prompt Banner
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = Locales.get("who_am_i_challenge", lang),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = Locales.get("who_am_i_desc", lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // List of Revealed Clues
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (index in 0 until revealedCluesCount.coerceAtMost(currentQuestion.clues.size)) {
                                val clue = currentQuestion.clues[index]
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = String.format(Locales.get("clue_number", lang), index + 1),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = clue.getLocalizedText(lang),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Reveal Next Clue Button
                        if (revealedCluesCount < currentQuestion.clues.size && selectedOptionIndex == null) {
                            OutlinedButton(
                                onClick = { onRevealNextClue() },
                                enabled = !isInputLocked && questionState == WhoAmIQuestionState.AWAITING_ANSWER,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reveal_next_clue_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format(
                                        Locales.get("reveal_next_clue", lang),
                                        revealedCluesCount + 1,
                                        currentQuestion.clues.size
                                    ),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Educational Fact / Feedback Card (if answered)
                    AnimatedVisibility(
                        visible = selectedOptionIndex != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val isCorrect = selectedOptionIndex == currentQuestion.correctIndex
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (isCorrect) Locales.get("correct", lang) else Locales.get("incorrect", lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = currentQuestion.targetCountry.flagEmoji,
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = currentQuestion.targetCountry.getLocalizedName(lang),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                    )
                                }
                                if (!isCorrect) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${Locales.get("correct_answer", lang)} ${currentQuestion.targetCountry.getLocalizedName(lang)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFB71C1C)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = Locales.get("did_you_know", lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentQuestion.getLocalizedEducationalFact(lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4 Answer Options Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentQuestion.options.forEachIndexed { index, optionCountry ->
                            val isSelected = selectedOptionIndex == index
                            val isCorrect = index == currentQuestion.correctIndex

                            val buttonColor = when {
                                selectedOptionIndex != null && isCorrect -> Color(0xFF2E7D32) // Green
                                selectedOptionIndex != null && isSelected && !isCorrect -> Color(0xFFC62828) // Red
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val textColor = when {
                                selectedOptionIndex != null && (isCorrect || isSelected) -> Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Button(
                                onClick = { onOptionSelected(index) },
                                enabled = !isInputLocked && questionState == WhoAmIQuestionState.AWAITING_ANSWER,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor,
                                    disabledContainerColor = buttonColor,
                                    disabledContentColor = textColor
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
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
                        }
                        Spacer(modifier = Modifier.height(16.dp))
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
                whoAmIViewModel.setPaused(false)
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
                        whoAmIViewModel.setPaused(false)
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
                        text = String.format(Locales.get("who_am_i_level_completed", lang), levelIndex),
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
                    WhoAmIResultRow(label = Locales.get("score", lang), value = "${summary.finalScore}")
                    WhoAmIResultRow(label = Locales.get("accuracy", lang), value = "${summary.accuracyPercentage}%")
                    WhoAmIResultRow(label = Locales.get("avg_clues_used", lang), value = "${summary.averageCluesUsed}")
                    WhoAmIResultRow(label = Locales.get("early_answer_streak", lang), value = "${summary.bestEarlyStreak}")
                    WhoAmIResultRow(label = Locales.get("current_time", lang), value = formatTime(elapsedTimeSec))
                    if (summary.coinsEarned > 0) {
                        WhoAmIResultRow(label = Locales.get("coins", lang), value = "+${summary.coinsEarned} 🪙")
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
                                navController.navigate("who_am_i_game/${levelIndex + 1}") {
                                    popUpTo("who_am_i_game/$levelIndex") { inclusive = true }
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
private fun WhoAmIResultRow(label: String, value: String) {
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
