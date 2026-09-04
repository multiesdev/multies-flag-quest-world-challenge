package com.multies.flagquest.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.multies.flagquest.data.local.entity.ProgressEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.ui.components.FlagQuestBottomNavigation
import com.multies.flagquest.ui.components.GameHeader
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.localization.stringLoc
import com.multies.flagquest.R
import com.multies.flagquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun JourneyScreen(
    profile: UserProfileEntity?,
    allProgress: List<ProgressEntity>,
    navController: NavController,
    onLevelSelected: (String, Int) -> Unit,
    onRefillLives: () -> Unit,
    viewModel: GameViewModel
) {
    val context = LocalContext.current
    val lang = profile?.selectedLanguage ?: "en"
    
    // Categories list and metadata
    val categories = listOf(
        JourneyCategory("FLAGS", "flags", "🚩", "Flags & Continents", 100),
        JourneyCategory("SILENT_MAP", "silent_map", "🧭", "Silent Map", 50),
        JourneyCategory("SPOT_THE_FAKE", "spot_the_fake", "🔍", "Spot the Fake", 100),
        JourneyCategory("MEMORY", "flag_memory", "🧠", "Flag Memory", 50),
        JourneyCategory("ORGANIZATIONS", "international_orgs", "🌍", "Organizations", 50),
        JourneyCategory("AREA", "area", "📏", "Country Area", 50),
        JourneyCategory("POPULATION", "population", "👥", "Population", 50),
        JourneyCategory("CAPITALS", "capitals", "🏛️", "Capitals", 50),
        JourneyCategory("CURRENCIES", "currencies", "🪙", "Currencies", 50),
        JourneyCategory("MAPS", "maps_locations", "🗺️", "Maps & Locations", 50),
        JourneyCategory("MIXED", "mixed", "🎲", "Mixed Challenge", -1) // Endless
    )

    var selectedCategoryId by remember { mutableStateOf("FLAGS") }
    val selectedCategory = categories.first { it.id == selectedCategoryId }

    // Collect variables from ViewModel
    val missedQuestions by viewModel.missedQuestions.collectAsState()
    val regenRemainingTime by viewModel.regenRemainingTimeSec.collectAsState()
    val difficultyMessage by viewModel.difficultyChangedMessage.collectAsState()

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            GameHeader(
                profile = profile,
                onSettingsClick = { navController.navigate("settings") },
                onRefillLivesClick = onRefillLives
            )
        },
        bottomBar = {
            FlagQuestBottomNavigation(
                navController = navController,
                lang = lang
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
            // Difficulty Alert Banner
            difficultyMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.dismissDifficultyMessage() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                // Header Title
                Text(
                    text = Locales.get("journey", lang, LocalContext.current).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textAlign = TextAlign.Center
                )

                // Category Selection Horizontal Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        val progressList = allProgress.filter { it.categoryId == cat.id }
                        val completedCount = progressList.count { it.isCompleted }
                        val percentage = if (cat.totalLevels > 0) {
                            (completedCount * 100) / cat.totalLevels
                        } else 0

                        Card(
                            onClick = { selectedCategoryId = cat.id },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                                 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("cat_tab_${cat.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = cat.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringLoc(cat.localizationKey, lang),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (cat.totalLevels > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$percentage%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) 
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Heart Recovery Countdown Banner
                if (profile != null && profile.lives < profile.maxLives) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Hearts",
                                        tint = Color.Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${Locales.get("lives", lang, LocalContext.current)}: ${profile.lives}/${profile.maxLives}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val min = regenRemainingTime / 60
                                val sec = regenRemainingTime % 60
                                Text(
                                    text = context.getString(R.string.regenerating_in, String.format("%02d:%02d", min, sec)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Button(
                                onClick = { viewModel.recoverHeartWithCoins() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                enabled = profile.coins >= 30,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Locales.get("buy_1_heart_coins", lang, context), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Practice Mistakes Banner (Smart Review)
                if (missedQuestions.isNotEmpty()) {
                    val readyToReview = missedQuestions.filter { System.currentTimeMillis() >= it.nextReviewTimestamp }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                        border = BorderStroke(1.5.dp, Color(0xFFFBC02D))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = context.getString(R.string.smart_review_title),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF5D4037)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = context.getString(R.string.smart_review_desc, readyToReview.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF795548)
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.startPracticeMistakesMode()
                                    navController.navigate("game/MISTAKES")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(context.getString(R.string.practice_button), color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Mode Controls Panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = context.getString(R.string.game_learning_modes),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = context.getString(R.string.relaxed_mode_desc),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Switch(
                                checked = profile?.relaxedMode ?: false,
                                onCheckedChange = { viewModel.toggleRelaxedMode(it) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = context.getString(R.string.classroom_mode_desc),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Switch(
                                checked = profile?.classroomMode ?: false,
                                onCheckedChange = { viewModel.toggleClassroomMode(it) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = context.getString(R.string.adaptive_difficulty_desc),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Switch(
                                checked = profile?.isAdaptiveDifficultyEnabled ?: true,
                                onCheckedChange = { viewModel.toggleAdaptiveDifficulty(it) }
                            )
                        }
                    }
                }

                // Category Progress & Mastery Card
                val completedInCat = allProgress.filter { it.categoryId == selectedCategoryId }.count { it.isCompleted }
                val totalInCat = selectedCategory.totalLevels

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringLoc(selectedCategory.localizationKey, lang).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (totalInCat > 0) {
                                        context.getString(R.string.levels_complete_format, completedInCat, totalInCat)
                                    } else {
                                        context.getString(R.string.endless_challenge_desc)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = selectedCategory.emoji, fontSize = 24.sp)
                            }
                        }

                        if (totalInCat > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val frac = completedInCat.toFloat() / totalInCat.toFloat()
                            LinearProgressIndicator(
                                progress = frac,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Continue Current Level Button
                            val firstUncompleted = (1..totalInCat).firstOrNull { lvl ->
                                !allProgress.any { it.categoryId == selectedCategoryId && it.levelIndex == lvl && it.isCompleted }
                            } ?: totalInCat

                            Button(
                                onClick = { onLevelSelected(selectedCategoryId, firstUncompleted) },
                                modifier = Modifier.fillMaxWidth().testTag("continue_btn_${selectedCategoryId}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Locales.get("continue_level_format", lang, context).format(firstUncompleted), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // MIXED Endless Category Button
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    viewModel.startNewQuiz("MIXED")
                                    navController.navigate("game/MIXED")
                                },
                                modifier = Modifier.fillMaxWidth().testTag("continue_btn_mixed"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Locales.get("start_mixed_challenge", lang, context), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Visual Roadmap Path of Levels (For non-endless categories)
                if (totalInCat > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (lvlIndex in 1..totalInCat) {
                            val progress = allProgress.find { it.categoryId == selectedCategoryId && it.levelIndex == lvlIndex }
                            val isCompleted = progress?.isCompleted == true
                            val stars = progress?.starsEarned ?: 0
                            val bestScore = progress?.highestScore ?: 0
                            
                            // Unlocked rule: lvlIndex == 1 is unlocked. > 1 is unlocked if previous is completed.
                            val isUnlocked = lvlIndex == 1 || allProgress.any { it.categoryId == selectedCategoryId && it.levelIndex == lvlIndex - 1 && it.isCompleted }

                            RoadmapNodeRow(
                                levelIndex = lvlIndex,
                                isUnlocked = isUnlocked,
                                isCompleted = isCompleted,
                                stars = stars,
                                bestScore = bestScore,
                                isLast = lvlIndex == totalInCat,
                                onPlay = {
                                    if (isUnlocked) {
                                        onLevelSelected(selectedCategoryId, lvlIndex)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoadmapNodeRow(
    levelIndex: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    stars: Int,
    bestScore: Int,
    isLast: Boolean,
    onPlay: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        // Connector Dot and vertical line column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Color(0xFFE2F9EC)
                            isUnlocked -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable(enabled = isUnlocked) { onPlay() }
                    .semantics {
                        val levelDesc = context.getString(R.string.level_content_description, levelIndex)
                        val statusDesc = when {
                            isCompleted -> context.getString(R.string.completed_with_stars_format, stars)
                            isUnlocked -> context.getString(R.string.unlocked_ready_status)
                            else -> context.getString(R.string.locked_status)
                        }
                        contentDescription = "$levelDesc. $statusDesc"
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCompleted -> Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF0F8F46),
                        modifier = Modifier.size(28.dp)
                    )
                    isUnlocked -> Text(
                        text = "$levelIndex",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    else -> Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!isLast) {
                val lineColor = if (isCompleted) Color(0xFF0F8F46) else if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                Canvas(
                    modifier = Modifier
                        .height(64.dp)
                        .width(4.dp)
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right side info card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isCompleted -> MaterialTheme.colorScheme.surface
                    isUnlocked -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                }
            ),
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = isUnlocked) { onPlay() },
            border = if (isUnlocked && !isCompleted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${context.getString(R.string.level).uppercase()} $levelIndex",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    if (isCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (i in 1..3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= stars) Color(0xFFFFB300) else Color.LightGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else if (isUnlocked) {
                         Box(
                             modifier = Modifier
                                 .clip(RoundedCornerShape(6.dp))
                                 .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                 .padding(horizontal = 6.dp, vertical = 2.dp)
                         ) {
                             Text(
                                 text = context.getString(R.string.play).uppercase(),
                                 fontSize = 9.sp,
                                 fontWeight = FontWeight.Black,
                                 color = MaterialTheme.colorScheme.primary
                             )
                         }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isCompleted) {
                        context.getString(R.string.best_score_format, bestScore)
                    } else if (isUnlocked) {
                        context.getString(R.string.ready_to_challenge)
                    } else {
                        context.getString(R.string.locked_lowercase)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

data class JourneyCategory(
    val id: String,
    val localizationKey: String,
    val emoji: String,
    val title: String,
    val totalLevels: Int
)
