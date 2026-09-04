package com.multies.flagquest.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.multies.flagquest.data.local.entity.ProgressEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.ui.components.GameHeader
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.localization.stringLoc
import com.multies.flagquest.R
import androidx.compose.ui.platform.LocalContext

@Composable
fun LevelSelectionScreen(
    profile: UserProfileEntity?,
    allProgress: List<ProgressEntity>,
    categoryId: String,
    navController: NavController,
    onLevelSelected: (String, Int) -> Unit,
    onRefillLives: () -> Unit
) {
    val lang = profile?.selectedLanguage ?: "en"

    // Group progress by level index for easy lookup
    val completedLevelsMap = allProgress
        .filter { it.categoryId.equals(categoryId, ignoreCase = true) && it.isCompleted }
        .associateBy { it.levelIndex }

    Scaffold(
        topBar = {
            GameHeader(
                profile = profile,
                onSettingsClick = { navController.navigate("settings") },
                onRefillLivesClick = onRefillLives
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
            // Header bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Spacer(modifier = Modifier.width(8.dp))
                val locKey = when (categoryId.uppercase()) {
                    "FLAGS" -> "flags"
                    "SILENT_MAP" -> "silent_map"
                    "WHO_AM_I" -> "who_am_i"
                    "SPOT_THE_FAKE" -> "spot_the_fake"
                    "RANKING", "COUNTRY_RANKING" -> "rank_the_countries"
                    "QUICK_GEOGRAPHY" -> "quick_geography"
                    "MEMORY" -> "flag_memory"
                    "ORGANIZATIONS" -> "international_orgs"
                    "AREA" -> "area"
                    "POPULATION" -> "population"
                    "CAPITALS" -> "capitals"
                    "CURRENCIES" -> "currencies"
                    "MAPS" -> "maps_locations"
                    "MIXED" -> "mixed"
                    else -> categoryId.lowercase()
                }
                val categoryTitle = stringLoc(locKey, lang).uppercase()
                Column {
                    Text(
                        text = categoryTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringLoc("complete_levels_in_sequence", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val totalLevels = when (categoryId.uppercase()) {
                "FLAGS", "SPOT_THE_FAKE" -> 100
                "SILENT_MAP", "MEMORY", "ORGANIZATIONS", "AREA", "POPULATION", "CAPITALS", "CURRENCIES", "MAPS", "RANKING", "QUICK_GEOGRAPHY", "WHO_AM_I" -> 50
                else -> 50
            }

            // Level Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(totalLevels) { index ->
                    val levelNumber = index + 1
                    val progress = completedLevelsMap[levelNumber]
                    val isCompleted = progress != null
                    val starsEarned = progress?.starsEarned ?: 0

                    // Unlocking logic: level 1 is always unlocked; level N is unlocked if level N-1 is completed
                    val isUnlocked = levelNumber == 1 || completedLevelsMap.containsKey(levelNumber - 1)

                    LevelGridItem(
                        levelNumber = levelNumber,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        starsEarned = starsEarned,
                        onClick = {
                            if (isUnlocked) {
                                onLevelSelected(categoryId, levelNumber)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelGridItem(
    levelNumber: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    starsEarned: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val containerColor = when {
        !isUnlocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        isCompleted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    }

    val borderColor = when {
        !isUnlocked -> Color.Transparent
        isCompleted -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .testTag("level_card_$levelNumber")
            .clickable(enabled = isUnlocked) { onClick() }
            .semantics {
                val levelDesc = context.getString(R.string.level_content_description, levelNumber)
                val statusDesc = when {
                    !isUnlocked -> context.getString(R.string.locked_status)
                    isCompleted -> context.getString(R.string.completed_with_stars_format, starsEarned)
                    else -> context.getString(R.string.unlocked_ready_status)
                }
                contentDescription = "$levelDesc. $statusDesc"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$levelNumber",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val activeStar = i <= starsEarned
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (activeStar) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
