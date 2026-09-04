package com.multies.flagquest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.CountryRankingEngine
import com.multies.flagquest.data.model.RankingCriterion
import com.multies.flagquest.data.model.RankingQuestion
import com.multies.flagquest.ui.components.FlagGraphic
import com.multies.flagquest.ui.components.GameHeader
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.localization.stringLoc
import com.multies.flagquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun CountryRankingGameScreen(
    levelIndex: Int,
    viewModel: GameViewModel,
    navController: NavController,
    profile: UserProfileEntity?
) {
    val lang = profile?.selectedLanguage ?: "en"
    val allCountries by viewModel.countries.collectAsState()
    val context = LocalContext.current

    // Generate questions for this level
    val questions = remember(levelIndex, allCountries) {
        if (allCountries.isNotEmpty()) {
            CountryRankingEngine.generateLevelQuestions(levelIndex, allCountries, lang)
        } else {
            emptyList()
        }
    }

    var currentRoundIndex by remember { mutableIntStateOf(0) }
    var totalScore by remember { mutableIntStateOf(0) }
    var perfectRoundsCount by remember { mutableIntStateOf(0) }
    var totalMovesCount by remember { mutableIntStateOf(0) }
    var startTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var elapsedTimeSec by remember { mutableStateOf(0L) }
    var isLevelComplete by remember { mutableStateOf(false) }

    // Round specific state
    val currentQuestion = questions.getOrNull(currentRoundIndex)
    val userCountryList = remember { mutableStateListOf<Country>() }
    var isSubmitted by remember { mutableStateOf(false) }
    var roundResult by remember { mutableStateOf<CountryRankingEngine.RoundResult?>(null) }

    // Initialize round
    LaunchedEffect(currentRoundIndex, questions) {
        currentQuestion?.let { q ->
            userCountryList.clear()
            userCountryList.addAll(q.displayedCountries)
            isSubmitted = false
            roundResult = null
        }
    }

    // Timer
    LaunchedEffect(isLevelComplete) {
        while (!isLevelComplete) {
            delay(1000)
            elapsedTimeSec = (System.currentTimeMillis() - startTimeMillis) / 1000
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
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Locales.get("back", lang, context),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Locales.get("country_ranking_challenge", lang, context).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Level $levelIndex • Round ${currentRoundIndex + 1} of ${questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$totalScore Pts",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Progress bar
            val progress = if (questions.isNotEmpty()) (currentRoundIndex + 1).toFloat() / questions.size else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (currentQuestion != null && !isLevelComplete) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    // Question Instruction Banner
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val criterionText = getCriterionInstruction(currentQuestion.criterion, currentQuestion.populationReferenceYear, lang, context)
                                Text(
                                    text = criterionText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringLoc("country_ranking_desc", lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Country Cards Reorder List
                    itemsIndexed(
                        items = userCountryList,
                        key = { _, country -> country.id }
                    ) { index, country ->
                        val correctCountry = currentQuestion.correctCountryOrder.getOrNull(index)
                        val isPositionCorrect = isSubmitted && correctCountry?.id == country.id
                        val correctIndexInAnswer = currentQuestion.correctCountryOrder.indexOfFirst { it.id == country.id }

                        RankingCountryItemCard(
                            index = index,
                            totalItems = userCountryList.size,
                            country = country,
                            isSubmitted = isSubmitted,
                            isPositionCorrect = isPositionCorrect,
                            correctIndex = correctIndexInAnswer,
                            formattedValue = currentQuestion.formattedValuesMap[country.id] ?: "",
                            lang = lang,
                            onMoveUp = {
                                if (!isSubmitted && index > 0) {
                                    val temp = userCountryList[index]
                                    userCountryList[index] = userCountryList[index - 1]
                                    userCountryList[index - 1] = temp
                                    totalMovesCount++
                                }
                            },
                            onMoveDown = {
                                if (!isSubmitted && index < userCountryList.size - 1) {
                                    val temp = userCountryList[index]
                                    userCountryList[index] = userCountryList[index + 1]
                                    userCountryList[index + 1] = temp
                                    totalMovesCount++
                                }
                            }
                        )
                    }

                    // Submit & Feedback Section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        if (!isSubmitted) {
                            Button(
                                onClick = {
                                    isSubmitted = true
                                    val userIds = userCountryList.map { it.id }
                                    val correctIds = currentQuestion.correctCountryOrder.map { it.id }
                                    val result = CountryRankingEngine.evaluateRound(userIds, correctIds)
                                    roundResult = result
                                    totalScore += result.scorePoints
                                    if (result.isFullOrder) {
                                        perfectRoundsCount++
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_answer_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = stringLoc("submit_answer", lang).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            // Post submission feedback
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (roundResult?.isFullOrder == true)
                                        Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (roundResult?.isFullOrder == true)
                                                stringLoc("perfect_order_bonus", lang)
                                            else
                                                "${roundResult?.exactMatches ?: 0}/${userCountryList.size} ${stringLoc("correct_positions", lang)}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (roundResult?.isFullOrder == true) Color(0xFF2E7D32) else Color(0xFFE65100)
                                        )
                                        Text(
                                            text = "+${roundResult?.scorePoints ?: 0} Pts",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (currentRoundIndex < questions.size - 1) {
                                                currentRoundIndex++
                                            } else {
                                                isLevelComplete = true
                                            }
                                        },
                                        modifier = Modifier.testTag("next_round_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(
                                            text = if (currentRoundIndex < questions.size - 1)
                                                stringLoc("next_round", lang).uppercase()
                                            else
                                                stringLoc("see_results", lang).uppercase(),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isLevelComplete) {
                // Results Screen
                val totalRounds = questions.size
                val accuracyPct = if (totalRounds > 0) ((perfectRoundsCount.toDouble() / totalRounds) * 100).toInt() else 0
                val starsEarned = when {
                    accuracyPct >= 85 -> 3
                    accuracyPct >= 60 -> 2
                    accuracyPct >= 35 -> 1
                    else -> 0
                }
                val coinsAwarded = starsEarned * 100 + 50

                LaunchedEffect(Unit) {
                    viewModel.saveCountryRankingLevelResult(
                        levelIndex = levelIndex,
                        starsEarned = starsEarned,
                        score = totalScore,
                        isCompleted = true,
                        coinsAwarded = coinsAwarded
                    )
                }

                RankingLevelCompleteDialog(
                    levelIndex = levelIndex,
                    score = totalScore,
                    stars = starsEarned,
                    accuracyPct = accuracyPct,
                    perfectRounds = perfectRoundsCount,
                    totalRounds = totalRounds,
                    elapsedTimeSec = elapsedTimeSec,
                    coinsEarned = coinsAwarded,
                    lang = lang,
                    onReplay = {
                        currentRoundIndex = 0
                        totalScore = 0
                        perfectRoundsCount = 0
                        totalMovesCount = 0
                        startTimeMillis = System.currentTimeMillis()
                        isLevelComplete = false
                    },
                    onNextLevel = {
                        if (levelIndex < 50) {
                            navController.navigate("country_ranking_game/${levelIndex + 1}") {
                                popUpTo("country_ranking_game/$levelIndex") { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RankingCountryItemCard(
    index: Int,
    totalItems: Int,
    country: Country,
    isSubmitted: Boolean,
    isPositionCorrect: Boolean,
    correctIndex: Int,
    formattedValue: String,
    lang: String,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    val borderColor = when {
        !isSubmitted -> MaterialTheme.colorScheme.outlineVariant
        isPositionCorrect -> Color(0xFF4CAF50)
        else -> Color(0xFFFF9800)
    }

    val backgroundColor = when {
        !isSubmitted -> MaterialTheme.colorScheme.surface
        isPositionCorrect -> Color(0xFFE8F5E9)
        else -> Color(0xFFFFF3E0)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("country_card_$index")
            .semantics {
                contentDescription = "${index + 1}. ${country.getLocalizedName(lang)}. $formattedValue"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Position Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSubmitted && isPositionCorrect) Color(0xFF4CAF50)
                            else if (isSubmitted) Color(0xFFFF9800)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isSubmitted) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Flag image
                FlagGraphic(
                    countryId = country.id,
                    modifier = Modifier.size(width = 44.dp, height = 30.dp)
                )

                // Country name and post submission value
                Column {
                    Text(
                        text = country.getLocalizedName(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSubmitted) {
                        Text(
                            text = formattedValue,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!isPositionCorrect) {
                            Text(
                                text = "(Correct Rank: #${correctIndex + 1})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD84315)
                            )
                        }
                    }
                }
            }

            // Controls
            if (!isSubmitted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("move_up_$index")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = Locales.get("move_up", lang, context),
                            tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalItems - 1,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("move_down_$index")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = Locales.get("move_down", lang, context),
                            tint = if (index < totalItems - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = Locales.get("reorder_handle", lang, context),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                if (isPositionCorrect) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RankingLevelCompleteDialog(
    levelIndex: Int,
    score: Int,
    stars: Int,
    accuracyPct: Int,
    perfectRounds: Int,
    totalRounds: Int,
    elapsedTimeSec: Long,
    coinsEarned: Int,
    lang: String,
    onReplay: () -> Unit,
    onNextLevel: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringLoc("country_ranking_level_completed", lang).format(levelIndex).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stars display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val active = i <= stars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (active) Color(0xFFFFB300) else Color.LightGray,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow("Total Score", "$score Pts")
                        StatRow("Perfect Rounds", "$perfectRounds / $totalRounds")
                        StatRow("Accuracy", "$accuracyPct%")
                        StatRow("Time Taken", "${elapsedTimeSec}s")
                        StatRow("Coins Awarded", "+$coinsEarned Coins")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onReplay,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("replay_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringLoc("replay", lang).uppercase(), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("next_level_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = stringLoc("next_level", lang).uppercase(), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getCriterionInstruction(criterion: RankingCriterion, year: Int, lang: String, context: android.content.Context): String {
    val key = criterion.locKey
    val rawLoc = Locales.get(key, lang, context)
    return if (criterion == RankingCriterion.POPULATION) {
        try {
            String.format(rawLoc, year)
        } catch (e: Exception) {
            rawLoc
        }
    } else {
        rawLoc
    }
}
