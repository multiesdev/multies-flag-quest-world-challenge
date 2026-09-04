package com.multies.flagquest.ui.screens

import com.multies.flagquest.ui.localization.Locales

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.multies.flagquest.R
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.Question
import com.multies.flagquest.ui.components.GameHeader
import com.multies.flagquest.ui.localization.stringLoc

@Composable
fun GamePlayScreen(
    profile: UserProfileEntity?,
    quizQuestions: List<Question>,
    countries: List<Country>,
    currentIndex: Int,
    selectedOption: Int?,
    isAnswered: Boolean,
    showDidYouKnow: Boolean,
    quizCompleted: Boolean,
    starsEarned: Int,
    timerRemaining: Int,
    timerActive: Boolean,
    usedFiftyFifty: Boolean,
    eliminatedOptionIndices: List<Int>,
    activeCategoryId: String,
    mixedScore: Int,
    mixedHighScore: Int,
    onOptionSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    onDismissDidYouKnow: () -> Unit,
    onBackToHome: () -> Unit,
    onPlayAgain: () -> Unit,
    onRefillLives: () -> Unit,
    onUseFiftyFifty: () -> Unit,
    onBuyHeart: () -> Unit,
    onNextLevel: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lang = profile?.selectedLanguage ?: "en"
    val lives = profile?.lives ?: 3

    // Handle Game Over UI (Lives = 0)
    if (lives <= 0) {
        GameOverView(
            lang = lang,
            profile = profile,
            onRefillLives = onRefillLives,
            onBackToHome = onBackToHome,
            onBuyHeart = onBuyHeart
        )
        return
    }

    // Handle Quiz Completed View
    if (quizCompleted) {
        if (activeCategoryId == "MIXED") {
            MixedChallengeSuccessView(
                lang = lang,
                score = mixedScore,
                highScore = mixedHighScore,
                onPlayAgain = onPlayAgain,
                onBackToHome = onBackToHome
            )
        } else {
            QuizSuccessView(
                lang = lang,
                starsEarned = starsEarned,
                onPlayAgain = onPlayAgain,
                onBackToHome = onBackToHome,
                onNextLevel = onNextLevel
            )
        }
        return
    }

    if (quizQuestions.isEmpty()) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             Text(context.getString(R.string.loading_questions))
         }
         return
     }

    val currentQuestion = quizQuestions[currentIndex]
    val relatedCountry = countries.find { it.id == currentQuestion.countryId }

    Scaffold(
        topBar = {
            GameHeader(profile = profile, onSettingsClick = null)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val progressText = if (activeCategoryId == "MIXED") {
                    Locales.get("streak_score_format", lang, context).format(mixedScore)
                } else {
                    Locales.get("question_progress_format", lang, context).format(currentIndex + 1, quizQuestions.size)
                }
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val diffKey = when (currentQuestion.difficulty.uppercase()) {
                    "EASY" -> "easy"
                    "MEDIUM" -> "medium"
                    "HARD" -> "hard"
                    else -> "medium"
                }
                val localizedDiff = Locales.get(diffKey, lang, context)
                val detailText = if (activeCategoryId == "MIXED") {
                    Locales.get("best_score_format", lang, context).format(mixedHighScore)
                } else {
                    Locales.get("difficulty_format", lang, context).format(localizedDiff)
                }
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeCategoryId != "MIXED") {
                LinearProgressIndicator(
                    progress = { (currentIndex.toFloat() / quizQuestions.size) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (timerActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (timerRemaining <= 5) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = if (timerRemaining <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Locales.get("seconds_left_format", lang, context).format(timerRemaining),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (timerRemaining <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    LinearProgressIndicator(
                        progress = { timerRemaining.toFloat() / 20f },
                        modifier = Modifier
                            .width(100.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (timerRemaining <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Flag Card if it's a flag question
            if (currentQuestion.category == "FLAGS" && relatedCountry != null) {
                Card(
                    modifier = Modifier
                        .size(140.dp)
                        .padding(4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = relatedCountry.flagEmoji,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Question Text Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = currentQuestion.getLocalizedQuestionText(lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Helper row: Streak & 50/50 Hint Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "🔥", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Locales.get("streak_format", lang, context).format(profile?.currentStreak ?: 0),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                val hasCoins = (profile?.coins ?: 0) >= 20
                val canUseHint = !usedFiftyFifty && !isAnswered && hasCoins
                OutlinedButton(
                    onClick = onUseFiftyFifty,
                    enabled = canUseHint,
                    border = BorderStroke(1.5.dp, if (canUseHint) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (canUseHint) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("hint_50_50_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "💡", fontSize = 14.sp)
                        Text(
                            text = if (usedFiftyFifty) Locales.get("fifty_fifty_used", lang, context) else "50/50 (-20 🪙)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Option Buttons
            val options = currentQuestion.getLocalizedOptions(lang)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                options.forEachIndexed { index, optionText ->
                    val isSelected = selectedOption == index
                    val isCorrectAnswer = index == currentQuestion.correctOptionIndex

                    if (!eliminatedOptionIndices.contains(index)) {
                        val (containerColor, contentColor, border) = when {
                            isAnswered && isCorrectAnswer -> Triple(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.tertiary,
                                BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
                            )
                            isAnswered && isSelected && !isCorrectAnswer -> Triple(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.error,
                                BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                            )
                            isSelected -> Triple(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primary,
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            )
                            else -> Triple(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.onSurface,
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            )
                        }

                        OutlinedButton(
                            onClick = { onOptionSelected(index) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("option_$index"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
                            border = border
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor
                                )
                                if (isAnswered) {
                                    if (isCorrectAnswer) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Correct",
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Incorrect",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit / Action Button
            Button(
                onClick = onSubmit,
                enabled = selectedOption != null && !isAnswered,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = stringLoc("submit", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Did You Know Dialog
    if (showDidYouKnow && relatedCountry != null) {
        val wasCorrect = selectedOption == currentQuestion.correctOptionIndex
        AlertDialog(
            onDismissRequest = onDismissDidYouKnow,
            icon = {
                Icon(
                    imageVector = if (wasCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (wasCorrect) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = if (wasCorrect) stringLoc("correct", lang) else stringLoc("wrong", lang),
                    fontWeight = FontWeight.Bold,
                    color = if (wasCorrect) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stringLoc("did_you_know", lang)} 💡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = relatedCountry.getLocalizedFunFact(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissDidYouKnow,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringLoc("next", lang))
                }
            }
        )
    }
}

@Composable
fun QuizSuccessView(
    lang: String,
    starsEarned: Int,
    onPlayAgain: () -> Unit,
    onBackToHome: () -> Unit,
    onNextLevel: (() -> Unit)? = null
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringLoc("quiz_completed", lang),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Star rating indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val starIcon = if (i <= starsEarned) Icons.Default.Star else Icons.Default.StarBorder
                    val starColor = if (i <= starsEarned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                    Icon(
                        imageVector = starIcon,
                        contentDescription = null,
                        tint = starColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "+${starsEarned * 10} ${stringLoc("coins", lang)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (onNextLevel != null) {
                Button(
                    onClick = {
                        android.util.Log.d("NEXT_LEVEL_TRACE", "BUTTON_TAPPED in QuizSuccessView")
                        onNextLevel()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("next_level_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringLoc("next_level", lang), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (onNextLevel != null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringLoc("play_again", lang),
                    fontWeight = FontWeight.Bold,
                    color = if (onNextLevel != null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringLoc("back_to_home", lang), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GameOverView(
    lang: String,
    profile: UserProfileEntity?,
    onRefillLives: () -> Unit,
    onBackToHome: () -> Unit,
    onBuyHeart: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SentimentVeryDissatisfied,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Out of Lives!",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Refill lives for free to continue learning about the world!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Coins purchase option
            if (profile != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "My Coins: ${profile.coins} 🪙",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onBuyHeart,
                            enabled = profile.coins >= 30,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F8F46)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(stringLoc("buy_heart_coins", lang), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Button(
                onClick = onRefillLives,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringLoc("refill_lives", lang), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringLoc("back_to_home", lang), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MixedChallengeSuccessView(
    lang: String,
    score: Int,
    highScore: Int,
    onPlayAgain: () -> Unit,
    onBackToHome: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🏆",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Challenge Completed!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You survived the Sudden Death with a magnificent streak!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Final Streak:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$score countries",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Personal Best:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$highScore countries",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Coins Reward:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+${score * 10} 🪙",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringLoc("play_again", lang), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringLoc("back_to_home", lang), fontWeight = FontWeight.Bold)
            }
        }
    }
}
