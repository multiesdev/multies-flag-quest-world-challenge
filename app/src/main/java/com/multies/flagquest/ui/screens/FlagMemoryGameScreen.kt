package com.multies.flagquest.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.multies.flagquest.data.local.entity.MemoryLevelEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.MemoryCard
import com.multies.flagquest.data.model.MemoryCardState
import com.multies.flagquest.data.model.MemoryGameEngine
import com.multies.flagquest.data.model.MemoryLevelConfig
import com.multies.flagquest.data.model.MemoryScoreResult
import com.multies.flagquest.ui.components.GameHeader
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.localization.stringLoc
import com.multies.flagquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FlagMemoryGameScreen(
    levelIndex: Int,
    viewModel: GameViewModel,
    navController: NavController,
    profile: UserProfileEntity?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lang = profile?.selectedLanguage ?: "en"

    val allCountries by viewModel.countries.collectAsState()
    val memoryRecords: List<MemoryLevelEntity> by viewModel.memoryRecords.collectAsState()
    val existingRecord = memoryRecords.find { it.levelIndex == levelIndex }

    val levelConfig = remember(levelIndex) { MemoryLevelConfig.getConfig(levelIndex) }

    // Game state
    var cards by remember { mutableStateOf<List<MemoryCard>>(emptyList()) }
    var firstSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var secondSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var isInputLocked by remember { mutableStateOf(false) }

    var isTimerRunning by remember { mutableStateOf(false) }
    var hasTimerStarted by remember { mutableStateOf(false) }
    var elapsedTimeSec by remember { mutableLongStateOf(0L) }

    var movesCount by remember { mutableIntStateOf(0) }
    var mismatchesCount by remember { mutableIntStateOf(0) }
    var matchedPairsCount by remember { mutableIntStateOf(0) }

    var isPreviewing by remember { mutableStateOf(levelConfig.previewDurationMs > 0) }
    var previewCountdownSec by remember { mutableIntStateOf((levelConfig.previewDurationMs / 1000).toInt()) }

    var isPaused by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showVictoryDialog by remember { mutableStateOf(false) }

    var resultSummary by remember { mutableStateOf<MemoryScoreResult?>(null) }
    var coinsAwardedInSession by remember { mutableIntStateOf(0) }

    // Helper function to initialize board
    fun initializeGame() {
        if (allCountries.isNotEmpty()) {
            val lastFlags = existingRecord?.lastFlagIds ?: ""
            val newCards = MemoryGameEngine.createBoard(levelConfig, allCountries, lang, lastFlags)
            
            val initialCards = if (levelConfig.previewDurationMs > 0) {
                newCards.map { it.copy(state = MemoryCardState.REVEALED) }
            } else {
                newCards
            }

            cards = initialCards
            firstSelectedIndex = null
            secondSelectedIndex = null
            isInputLocked = levelConfig.previewDurationMs > 0
            isTimerRunning = false
            hasTimerStarted = false
            elapsedTimeSec = 0L
            movesCount = 0
            mismatchesCount = 0
            matchedPairsCount = 0
            showVictoryDialog = false
            showPauseDialog = false
            showRestartDialog = false
            isPreviewing = levelConfig.previewDurationMs > 0
            previewCountdownSec = (levelConfig.previewDurationMs / 1000).toInt()
        }
    }

    LaunchedEffect(allCountries, levelIndex, lang) {
        if (allCountries.isNotEmpty() && cards.isEmpty()) {
            initializeGame()
        }
    }

    // Handle preview countdown
    LaunchedEffect(isPreviewing) {
        if (isPreviewing && levelConfig.previewDurationMs > 0) {
            isInputLocked = true
            while (previewCountdownSec > 0) {
                delay(1000L)
                previewCountdownSec--
            }
            // End preview
            cards = cards.map { it.copy(state = MemoryCardState.FACE_DOWN) }
            isPreviewing = false
            isInputLocked = false
        }
    }

    // Timer loop
    LaunchedEffect(isTimerRunning, isPaused) {
        if (isTimerRunning && !isPaused) {
            while (isTimerRunning && !isPaused) {
                delay(1000L)
                elapsedTimeSec++
            }
        }
    }

    // Card Tap Handler
    fun onCardTapped(index: Int) {
        if (isInputLocked || isPaused || isPreviewing) return
        if (index !in cards.indices) return

        val tappedCard = cards[index]
        if (tappedCard.state != MemoryCardState.FACE_DOWN) return

        // Start timer on first tap
        if (!hasTimerStarted) {
            hasTimerStarted = true
            isTimerRunning = true
        }

        AudioManager.playSoundEffect(SoundType.TICK)
        HapticHelper.triggerTick(context)

        if (firstSelectedIndex == null) {
            // First card revealed
            firstSelectedIndex = index
            cards = cards.toMutableList().apply {
                this[index] = tappedCard.copy(state = MemoryCardState.REVEALED)
            }
        } else if (secondSelectedIndex == null && index != firstSelectedIndex) {
            // Second card revealed
            val firstIndex = firstSelectedIndex!!
            secondSelectedIndex = index
            movesCount++

            val updatedList = cards.toMutableList()
            updatedList[index] = tappedCard.copy(state = MemoryCardState.REVEALED)
            cards = updatedList

            val firstCard = cards[firstIndex]
            val secondCard = cards[index]

            if (firstCard.countryId == secondCard.countryId) {
                // MATCH!
                AudioManager.playSoundEffect(SoundType.CORRECT)
                HapticHelper.triggerCorrect(context)

                matchedPairsCount++
                cards = cards.toMutableList().apply {
                    this[firstIndex] = this[firstIndex].copy(state = MemoryCardState.MATCHED)
                    this[index] = this[index].copy(state = MemoryCardState.MATCHED)
                }

                firstSelectedIndex = null
                secondSelectedIndex = null

                // Check victory condition
                if (matchedPairsCount >= levelConfig.pairCount) {
                    isTimerRunning = false
                    AudioManager.playSoundEffect(SoundType.MILESTONE)
                    HapticHelper.triggerMilestone(context)

                    val isBetterRecord = MemoryGameEngine.isBetterRecord(
                        newTimeSec = elapsedTimeSec,
                        newMoves = movesCount,
                        newMismatches = mismatchesCount,
                        existingRecord = existingRecord
                    )

                    val isFirstCompletion = existingRecord == null || !existingRecord.isCompleted

                    val scoreResult = MemoryGameEngine.calculateScore(
                        levelIndex = levelIndex,
                        pairCount = levelConfig.pairCount,
                        targetTimeSec = levelConfig.targetTimeSec,
                        elapsedTimeSec = elapsedTimeSec,
                        movesCount = movesCount,
                        mismatchesCount = mismatchesCount,
                        isNewRecord = isBetterRecord
                    )

                    resultSummary = scoreResult

                    // Award coins: 100 for first completion, 30 for new record
                    val coinsToAward = when {
                        isFirstCompletion -> 100
                        isBetterRecord -> 30
                        else -> 5
                    }
                    coinsAwardedInSession = coinsToAward

                    val usedFlagIds = cards.map { it.countryId }.distinct().joinToString(",")

                    viewModel.saveMemoryLevelResult(
                        levelIndex = levelIndex,
                        elapsedTimeSec = elapsedTimeSec,
                        movesCount = movesCount,
                        mismatchesCount = mismatchesCount,
                        score = scoreResult.finalScore,
                        starsEarned = scoreResult.starsEarned,
                        isNewRecord = isBetterRecord,
                        coinsAwarded = coinsToAward,
                        usedFlagIds = usedFlagIds
                    )

                    showVictoryDialog = true
                }
            } else {
                // MISMATCH!
                mismatchesCount++
                isInputLocked = true
                AudioManager.playSoundEffect(SoundType.WRONG)
                HapticHelper.triggerWrong(context)

                coroutineScope.launch {
                    delay(800L)
                    cards = cards.toMutableList().apply {
                        this[firstIndex] = this[firstIndex].copy(state = MemoryCardState.FACE_DOWN)
                        this[index] = this[index].copy(state = MemoryCardState.FACE_DOWN)
                    }
                    firstSelectedIndex = null
                    secondSelectedIndex = null
                    isInputLocked = false
                }
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
        ) {
            // Top Navigation & HUD Bar
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

                // Timer & Moves HUD
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Timer Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(2.dp)
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
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Moves Counter Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${Locales.get("moves", lang)}: $movesCount",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
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

            // Preview Banner or Best Record Subheader
            if (isPreviewing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locales.get("preview_countdown", lang), previewCountdownSec),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            } else if (existingRecord != null && existingRecord.isCompleted && existingRecord.bestCompletionTimeSec > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Locales.get("best_time", lang)}: ${formatTime(existingRecord.bestCompletionTimeSec)} (${existingRecord.bestMoves} ${Locales.get("moves", lang)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Memory Card Grid
            val columns = MemoryGameEngine.getRecommendedGridColumns(cards.size)

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cards.size) { index ->
                    val card = cards[index]
                    MemoryCardView(
                        card = card,
                        index = index,
                        lang = lang,
                        onClick = { onCardTapped(index) }
                    )
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
                Text(
                    text = Locales.get("pause_title", lang),
                    fontWeight = FontWeight.Bold
                )
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

    // Victory Results Dialog
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
                        text = String.format(Locales.get("memory_level_completed", lang), levelIndex),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Stars Display
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
                    ResultRow(label = Locales.get("current_time", lang), value = formatTime(elapsedTimeSec))
                    ResultRow(label = Locales.get("moves", lang), value = "$movesCount")
                    ResultRow(label = Locales.get("mismatches", lang), value = "$mismatchesCount")
                    ResultRow(label = Locales.get("score", lang), value = "${summary.finalScore}")
                    if (coinsAwardedInSession > 0) {
                        ResultRow(label = Locales.get("coins", lang), value = "+$coinsAwardedInSession 🪙")
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
                            initializeGame()
                        },
                        modifier = Modifier.testTag("replay_button")
                    ) {
                        Text(text = Locales.get("play_again", lang))
                    }

                    if (levelIndex < 50) {
                        Button(
                            onClick = {
                                navController.navigate("flag_memory_game/${levelIndex + 1}") {
                                    popUpTo("flag_memory_game/$levelIndex") { inclusive = true }
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
fun MemoryCardView(
    card: MemoryCard,
    index: Int,
    lang: String,
    onClick: () -> Unit
) {
    val isFaceUp = card.state != MemoryCardState.FACE_DOWN
    val isMatched = card.state == MemoryCardState.MATCHED

    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    val containerColor = when {
        isMatched -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
        isFaceUp -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val borderColor = when {
        isMatched -> MaterialTheme.colorScheme.secondary
        isFaceUp -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMatched) 1.dp else 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .testTag("memory_card_$index")
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !isFaceUp) { onClick() }
            .semantics {
                contentDescription = when {
                    isMatched -> String.format(Locales.get("card_matched_semantics", lang), card.countryName)
                    isFaceUp -> String.format(Locales.get("card_revealed_semantics", lang), card.countryName)
                    else -> String.format(Locales.get("card_hidden_semantics", lang), index + 1)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation > 90f) {
                // Card Front (Revealed or Matched)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f } // Un-mirror front text
                ) {
                    Text(
                        text = card.flagEmoji,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = card.countryName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    if (isMatched) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                    }
                }
            } else {
                // Card Back (Face Down)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌐",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
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
