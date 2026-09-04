package com.multies.flagquest.ui.screens

import androidx.compose.ui.platform.LocalContext
import com.multies.flagquest.ui.localization.Locales

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.ui.components.FlagQuestBottomNavigation
import com.multies.flagquest.ui.components.GameHeader

@Composable
fun PlayScreen(
    profile: UserProfileEntity?,
    navController: NavController,
    onCategorySelected: (String) -> Unit,
    onRefillLives: () -> Unit
) {
    val lang = profile?.selectedLanguage ?: "en"

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            val context = LocalContext.current
            Text(
                text = Locales.get("special_challenges", lang, context).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.2.sp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = Locales.get("special_challenges_desc", lang, context),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Play Mode 1: Blitz Mode
            PlayModeCard(
                title = Locales.get("time_blitz_challenge", lang, context),
                description = Locales.get("time_blitz_desc", lang, context),
                icon = Icons.Default.Timer,
                iconColor = Color(0xFFF2994A),
                reward = Locales.get("reward_coins_format", lang, context).format(150),
                lang = lang,
                onClick = { onCategorySelected("FLAGS") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Play Mode 2: Survival Mode
            PlayModeCard(
                title = Locales.get("sudden_death_survival", lang, context),
                description = Locales.get("sudden_death_desc", lang, context),
                icon = Icons.Default.HeartBroken,
                iconColor = Color(0xFFEB5757),
                reward = Locales.get("reward_coins_format", lang, context).format(300),
                lang = lang,
                onClick = { onCategorySelected("MIXED") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Play Mode 3: Capital Master
            PlayModeCard(
                title = Locales.get("capital_city_marathon", lang, context),
                description = Locales.get("capital_city_desc", lang, context),
                icon = Icons.Default.FlashOn,
                iconColor = Color(0xFFF2C94C),
                reward = Locales.get("reward_coins_format", lang, context).format(200),
                lang = lang,
                onClick = { onCategorySelected("CAPITALS") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Play Mode 4: Flag Memory Challenge
            PlayModeCard(
                title = Locales.get("flag_memory_challenge", lang, context),
                description = Locales.get("flag_memory_desc", lang, context),
                icon = Icons.Default.Timer,
                iconColor = Color(0xFF9C27B0),
                reward = Locales.get("reward_coins_format", lang, context).format(100),
                lang = lang,
                onClick = { navController.navigate(com.multies.flagquest.ui.navigation.Screen.LevelSelection.createRoute("MEMORY")) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Play Mode 5: Silent Map Challenge
            PlayModeCard(
                title = Locales.get("silent_map_challenge", lang, context),
                description = Locales.get("silent_map_desc", lang, context),
                icon = Icons.Default.Public,
                iconColor = Color(0xFF009688),
                reward = Locales.get("reward_coins_format", lang, context).format(250),
                lang = lang,
                onClick = { navController.navigate(com.multies.flagquest.ui.navigation.Screen.LevelSelection.createRoute("SILENT_MAP")) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Play Mode 6: Who Am I? Challenge
            PlayModeCard(
                title = Locales.get("who_am_i_challenge", lang, context),
                description = Locales.get("who_am_i_desc", lang, context),
                icon = Icons.Default.FlashOn,
                iconColor = Color(0xFFE91E63),
                reward = Locales.get("reward_coins_format", lang, context).format(300),
                lang = lang,
                onClick = { navController.navigate(com.multies.flagquest.ui.navigation.Screen.LevelSelection.createRoute("WHO_AM_I")) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Play Mode 7: Spot the Fake Flag
            PlayModeCard(
                title = Locales.get("spot_the_fake_challenge", lang, context),
                description = Locales.get("spot_the_fake_desc", lang, context),
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFFF9800),
                reward = Locales.get("reward_coins_format", lang, context).format(350),
                lang = lang,
                onClick = { navController.navigate(com.multies.flagquest.ui.navigation.Screen.LevelSelection.createRoute("SPOT_THE_FAKE")) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Play Mode 8: Country Ranking Challenge
            PlayModeCard(
                title = Locales.get("country_ranking_challenge", lang, context),
                description = Locales.get("country_ranking_desc", lang, context),
                icon = Icons.Default.Public,
                iconColor = Color(0xFF3F51B5),
                reward = Locales.get("reward_coins_format", lang, context).format(400),
                lang = lang,
                onClick = { navController.navigate(com.multies.flagquest.ui.navigation.Screen.LevelSelection.createRoute("RANKING")) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Play Mode 9: Quick Geography
            PlayModeCard(
                title = Locales.get("quick_geography_challenge", lang, context),
                description = Locales.get("quick_geography_desc", lang, context),
                icon = Icons.Default.Timer,
                iconColor = Color(0xFFE91E63),
                reward = Locales.get("reward_coins_format", lang, context).format(500),
                lang = lang,
                onClick = { navController.navigate(com.multies.flagquest.ui.navigation.Screen.LevelSelection.createRoute("QUICK_GEOGRAPHY")) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Play Mode 10: Continent Master Boss Challenge
            PlayModeCard(
                title = Locales.get("continent_master", lang, context),
                description = Locales.get("continent_master_desc", lang, context),
                icon = Icons.Default.Public,
                iconColor = Color(0xFF673AB7),
                reward = Locales.get("reward_coins_format", lang, context).format(250),
                lang = lang,
                onClick = { navController.navigate(com.multies.flagquest.ui.navigation.Screen.ContinentBossIntro.route) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlayModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    reward: String,
    lang: String = "en",
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("play_mode_${title.lowercase().replace(" ", "_")}")
            .clickable { onClick() }
            .semantics {
                contentDescription = "Challenge Mode: $title. $description. Reward: $reward"
            }
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = Locales.get("active", lang, context).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Locales.get("reward_label_format", lang, context).format(reward),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFB45309)
                )

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = Locales.get("start", lang, context).uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
