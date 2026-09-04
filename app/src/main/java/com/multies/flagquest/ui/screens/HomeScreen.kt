package com.multies.flagquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.res.stringResource
import com.multies.flagquest.R
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.ui.components.ContinueJourneyCard
import com.multies.flagquest.ui.components.CategoryGrid
import com.multies.flagquest.ui.components.DailyChallengeCard
import com.multies.flagquest.ui.components.DidYouKnowCard
import com.multies.flagquest.ui.components.FlagQuestBottomNavigation
import com.multies.flagquest.ui.components.GameHeader
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.navigation.Screen

@Composable
fun HomeScreen(
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
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Primary "Continue Journey" Card
            ContinueJourneyCard(
                lang = lang,
                profile = profile,
                onContinueClick = { onCategorySelected("MIXED") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1b. Daily Rewards, Wheel & Missions Entry Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("rewards") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁",
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.daily_rewards).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.rewards_card_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Text(
                        text = "➔",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Daily Challenge Card
            Text(
                text = stringResource(R.string.daily_special_header),
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.1.sp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            DailyChallengeCard(
                lang = lang,
                onClick = { onCategorySelected("FLAGS") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Category Grid Header & List
            Text(
                text = stringResource(R.string.categories).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.1.sp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            CategoryGrid(
                lang = lang,
                onCategorySelected = onCategorySelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Did You Know Card
            Text(
                text = stringResource(R.string.educational_facts_header),
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.1.sp),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            DidYouKnowCard(
                lang = lang,
                onViewInAtlasClick = { navController.navigate(Screen.Atlas.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
