package com.multies.flagquest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.multies.flagquest.data.model.BossPhaseType
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.SilentMapFormat
import com.multies.flagquest.data.model.SilentMapQuestion
import com.multies.flagquest.ui.components.SilentMapCanvas
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.viewmodel.ContinentBossViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinentBossGameplayScreen(
    bossId: String,
    bossViewModel: ContinentBossViewModel,
    navController: NavController,
    countries: List<Country>,
    lang: String
) {
    val context = LocalContext.current
    val session by bossViewModel.sessionState.collectAsState()

    LaunchedEffect(bossId, countries) {
        if (session == null || session?.boss?.bossId != bossId) {
            bossViewModel.startBossAttempt(bossId, countries)
        }
    }

    val activeState = session

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    activeState?.let { st ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = st.boss.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = st.boss.continentEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            bossViewModel.exitAttempt()
                            navController.popBackStack()
                        },
                        modifier = Modifier.testTag("boss_exit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit")
                    }
                },
                actions = {
                    activeState?.let { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            repeat(3) { index ->
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Heart",
                                    tint = if (index < st.remainingHearts) Color(0xFFE53935) else Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${st.score} pts",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (activeState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Preparing Boss Challenge...", style = MaterialTheme.typography.titleMedium)
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Phase Progress Header
                val currentPhaseDef = activeState.phases[activeState.currentPhaseIndex]
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(activeState.boss.themeColorHex).copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Locales.get(currentPhaseDef.titleKey, lang, context),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(activeState.boss.themeColorHex)
                            )
                            Text(
                                text = "Question ${activeState.currentQuestionIndex + 1}/${currentPhaseDef.questionCount}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (currentPhaseDef.timerSecondsPerQuestion > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activeState.secondsRemainingInQuestion}s",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }

                val currentQuestion = activeState.currentPhaseQuestions.getOrNull(activeState.currentQuestionIndex)
                if (currentQuestion != null) {
                    // Question Visual Display
                    when (currentQuestion.visualType) {
                        "MAP" -> {
                            val targetCountry = countries.find { it.id == currentQuestion.countryId }
                            if (targetCountry != null) {
                                val dummyQuestion = SilentMapQuestion(
                                    id = currentQuestion.id,
                                    targetCountry = targetCountry,
                                    options = emptyList(),
                                    correctIndex = 0,
                                    format = SilentMapFormat.SILHOUETTE
                                )
                                SilentMapCanvas(
                                    question = dummyQuestion,
                                    lang = lang,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                        }
                        "FLAG" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = currentQuestion.visualData, fontSize = 64.sp)
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(activeState.boss.themeColorHex).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentQuestion.visualData,
                                    fontSize = 40.sp
                                )
                            }
                        }
                    }

                    // Prompt Text
                    val promptText = when (lang) {
                        "ar" -> currentQuestion.promptAr
                        "de" -> currentQuestion.promptDe
                        "fr" -> currentQuestion.promptFr
                        else -> currentQuestion.promptEn
                    }
                    Text(
                        text = promptText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Options List
                    val options = when (lang) {
                        "ar" -> currentQuestion.optionsAr
                        "de" -> currentQuestion.optionsDe
                        "fr" -> currentQuestion.optionsFr
                        else -> currentQuestion.optionsEn
                    }

                    options.forEachIndexed { index, optionLabel ->
                        val isSelected = activeState.selectedOptionIndex == index
                        val isCorrect = index == currentQuestion.correctOptionIndex
                        val isSubmitted = activeState.isAnswerSubmitted

                        val cardBg = when {
                            isSubmitted && isCorrect -> Color(0xFF2E7D32)
                            isSubmitted && isSelected && !isCorrect -> Color(0xFFC62828)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }

                        val textColor = when {
                            isSubmitted && (isCorrect || isSelected) -> Color.White
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isSubmitted) {
                                    bossViewModel.selectOption(index)
                                    bossViewModel.submitAnswer()
                                }
                                .testTag("boss_option_$index")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A' + index)}.  $optionLabel",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Educational Fact Explanation Banner when answer submitted
                    if (activeState.isAnswerSubmitted) {
                        val explanation = when (lang) {
                            "ar" -> currentQuestion.explanationAr
                            "de" -> currentQuestion.explanationDe
                            "fr" -> currentQuestion.explanationFr
                            else -> currentQuestion.explanationEn
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (activeState.isAnswerCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (activeState.isAnswerCorrect) "✓ Correct!" else "✗ Wrong Answer!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (activeState.isAnswerCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Button(
                            onClick = { bossViewModel.nextQuestionOrPhase() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(activeState.boss.themeColorHex)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(top = 8.dp)
                                .testTag("boss_next_button")
                        ) {
                            Text(
                                text = Locales.get("next", lang, context).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Phase Completed Transition Card
            if (activeState.isPhaseCompleted) {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .padding(24.dp)
                                .testTag("phase_completed_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = Locales.get("phase_completed_title", lang, context),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${activeState.remainingHearts} Hearts Remaining",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val nextPhase = activeState.phases.getOrNull(activeState.currentPhaseIndex + 1)
                                if (nextPhase != null) {
                                    Text(
                                        text = "Next Phase:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Locales.get(nextPhase.titleKey, lang, context),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(activeState.boss.themeColorHex)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { bossViewModel.continueToNextPhase() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(activeState.boss.themeColorHex)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("continue_phase_button")
                                ) {
                                    Text(
                                        text = Locales.get("continue_boss_challenge", lang, context),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Boss Victory Overlay
            if (activeState.isBossVictory) {
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(20.dp)
                                .testTag("boss_victory_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "👑 ${activeState.boss.emoji} 👑", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = Locales.get("boss_victory_title", lang, context),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = Color(0xFF4CAF50),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row {
                                    repeat(3) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Final Score: ${activeState.score} pts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )

                                Text(
                                    text = "Reward Granted: +250 🪙",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        bossViewModel.exitAttempt()
                                        navController.popBackStack()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("boss_victory_continue_button")
                                ) {
                                    Text(
                                        text = Locales.get("return_to_challenges", lang, context),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Boss Failure Overlay
            if (activeState.isBossFailed) {
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .padding(20.dp)
                                .testTag("boss_failure_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "💔", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = Locales.get("boss_failure_title", lang, context),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = Color(0xFFD32F2F),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Reached Phase ${activeState.currentPhaseIndex + 1}/${activeState.phases.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { bossViewModel.retryAttempt(countries) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("boss_retry_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Locales.get("retry_boss", lang, context),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
