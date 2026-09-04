package com.multies.flagquest.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.local.entity.AchievementEntity
import com.multies.flagquest.ui.localization.Locales

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    profile: UserProfileEntity?,
    achievements: List<AchievementEntity>,
    onBack: () -> Unit
) {
    val lang = profile?.selectedLanguage ?: "en"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Locales.get("achievements", lang),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(achievements) { ach ->
                AchievementCard(lang = lang, ach = ach)
            }
        }
    }
}

@Composable
fun AchievementCard(lang: String, ach: AchievementEntity) {
    val title = Locales.get(ach.titleKey, lang)
    val desc = Locales.get(ach.descKey, lang)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ach.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon mapping
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (ach.isUnlocked) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when (ach.iconName) {
                    "emoji_star" -> "⭐"
                    "emoji_medal" -> "🏅"
                    "emoji_trophy" -> "🏆"
                    "emoji_fire" -> "🔥"
                    "emoji_lightning" -> "⚡"
                    "emoji_crown" -> "👑"
                    "emoji_map_europe" -> "🇪🇺"
                    "emoji_map_africa" -> "🌍"
                    "emoji_map_asia" -> "🌏"
                    "emoji_flag_master" -> "🎏"
                    "emoji_org_master" -> "🏢"
                    "emoji_area_master" -> "🗺️"
                    "emoji_pop_master" -> "👥"
                    "emoji_globe_25" -> "🌐"
                    "emoji_globe_50" -> "🌎"
                    "emoji_globe_100" -> "🪐"
                    "emoji_perfect" -> "💯"
                    else -> "🏆"
                }
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(if (ach.isUnlocked) 0.dp else 4.dp)
                        .alpha(if (ach.isUnlocked) 1f else 0.4f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (ach.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val progressFraction = if (ach.maxProgress > 0) {
                    ach.progress.toFloat() / ach.maxProgress.toFloat()
                } else {
                    0f
                }.coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!ach.isUnlocked) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${ach.progress}/${ach.maxProgress}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val rewardString = when (ach.id) {
                            "correct_1" -> "50 ${Locales.get("coins", lang)}"
                            "correct_10" -> "100 ${Locales.get("coins", lang)}"
                            "correct_100" -> "250 ${Locales.get("coins", lang)} + 1 Hint"
                            "streak_5" -> "50 ${Locales.get("coins", lang)}"
                            "streak_10" -> "100 ${Locales.get("coins", lang)}"
                            "streak_20" -> "200 ${Locales.get("coins", lang)} + 1 Hint"
                            "perfect_level" -> "50 ${Locales.get("coins", lang)}"
                            "explorer_europe", "explorer_africa", "explorer_asia" -> "50 ${Locales.get("coins", lang)}"
                            "master_flag", "master_org", "master_area", "master_pop" -> "100 ${Locales.get("coins", lang)}"
                            "atlas_25" -> "100 ${Locales.get("coins", lang)}"
                            "atlas_50" -> "200 ${Locales.get("coins", lang)}"
                            "atlas_100" -> "500 ${Locales.get("coins", lang)} + 3 Hints"
                            else -> "50 ${Locales.get("coins", lang)}"
                        }
                        Text(
                            text = "Unlocked! (+$rewardString)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
