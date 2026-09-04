package com.multies.flagquest.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.localization.stringLoc
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.multies.flagquest.R

/**
 * 1. PRIMARY "CONTINUE JOURNEY" CARD
 */
@Composable
fun ContinueJourneyCard(
    lang: String,
    profile: UserProfileEntity?,
    onContinueClick: () -> Unit
) {
    val context = LocalContext.current
    val totalStars = profile?.totalStars ?: 0
    val currentLevel = 1 + (totalStars / 3)
    val streak = profile?.currentStreak ?: 0

    // Strong Vibrant Blue Color Palette (Visual Anchor) with subtle continent map theme feeling
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0061A4) // Premium Strong Blue
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("continue_journey_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0061A4),
                            Color(0xFF00497D)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = context.getString(R.string.level_status_header, currentLevel),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD1E4FF)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 14.sp
                        )
                        Text(
                            text = context.getString(R.string.streak_days, streak),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD98C)
                        )
                    }
                }

                Text(
                    text = context.getString(R.string.world_tour_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = Color.White
                )

                // Modular Custom Progress Indicator
                val progress = ((totalStars % 3) / 3.0f).coerceIn(0.1f, 1.0f)
                GameProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars",
                            tint = Color(0xFFFFD98C),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = context.getString(R.string.stars_earned_format, totalStars),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD1E4FF)
                        )
                    }

                    Button(
                        onClick = onContinueClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD98C), // Gold accent button
                            contentColor = Color(0xFF281900)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = context.getString(R.string.continue_button),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. MODULAR PROGRESS INDICATOR
 */
@Composable
fun GameProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Beautifully animated progress change
    var animatedProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(progress) {
        animatedProgress = progress
    }
    val animState by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 800)
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { animState },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFFFFD98C), // Gold progress fill
            trackColor = Color(0xFFD1E4FF).copy(alpha = 0.25f)
        )
    }
}

/**
 * 3. CATEGORY CARD
 */
@Composable
fun CategoryCard(
    title: String,
    emoji: String,
    completedLevels: Int,
    isLocked: Boolean,
    accentColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val cardBackground = if (isLocked) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        accentColor
    }

    val finalContentColor = if (isLocked) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    } else {
        contentColor
    }

    val border = if (!isLocked && accentColor == MaterialTheme.colorScheme.primaryContainer) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .testTag("category_card_${title.lowercase().replace(" ", "_")}")
            .clickable(enabled = !isLocked) { onClick() }
            .semantics {
                contentDescription = "$title Category. Status: ${if (isLocked) "Locked" else "Unlocked"}"
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Emoji Icon or Locked state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = emoji,
                        fontSize = 28.sp,
                        modifier = Modifier.alpha(if (isLocked) 0.5f else 1f)
                    )

                    if (isLocked) {
                        LockedStateBadge()
                    } else if (completedLevels > 0) {
                        RewardBadge(stars = completedLevels)
                    }
                }

                // Text details
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = finalContentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = if (isLocked) "Locked" else "XP +150",
                        style = MaterialTheme.typography.labelSmall,
                        color = finalContentColor.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 4. LOCKED STATE BADGE
 */
@Composable
fun LockedStateBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "LOCKED",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

/**
 * 5. REWARD STAR BADGE
 */
@Composable
fun RewardBadge(stars: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE2F9EC))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Completed",
            tint = Color(0xFF0F8F46),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "$stars ★",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            fontWeight = FontWeight.Black,
            color = Color(0xFF004D20)
        )
    }
}

/**
 * 6. CATEGORY GRID
 */
@Composable
fun CategoryGrid(
    lang: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        CategoryItem("FLAGS", "flags", "🏳️", false, Color(0xFFD1E4FF), Color(0xFF001D36)),
        CategoryItem("CAPITALS", "capitals", "🏛️", false, Color(0xFFFFF0D4), Color(0xFF281900)),
        CategoryItem("POPULATION", "population", "👥", false, Color(0xFFE8F5E9), Color(0xFF0A4B1A)),
        CategoryItem("MIXED", "mixed", "🏆", false, Color(0xFFFFD9E2), Color(0xFF90002F)),
        CategoryItem("AREA", "area", "📐", false, Color(0xFFEAE2F8), Color(0xFF251049)),
        CategoryItem("ORGANIZATIONS", "international_orgs", "🇺🇳", false, Color(0xFFE1F5FE), Color(0xFF01579B)),
        CategoryItem("CURRENCIES", "currencies", "🪙", false, Color(0xFFFFFDE7), Color(0xFFF57F17)),
        CategoryItem("MAPS", "maps_locations", "🗺️", false, Color(0xFFFFE0B2), Color(0xFFE65100))
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Render 2 elements per row
        for (i in categories.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    val cat1 = categories[i]
                    val titleText = stringLoc(cat1.key, lang)
                    CategoryCard(
                        title = titleText,
                        emoji = cat1.emoji,
                        completedLevels = if (cat1.isLocked) 0 else 3,
                        isLocked = cat1.isLocked,
                        accentColor = cat1.accentColor,
                        contentColor = cat1.contentColor,
                        onClick = { onCategorySelected(cat1.id) }
                    )
                }

                if (i + 1 < categories.size) {
                    Box(modifier = Modifier.weight(1f)) {
                        val cat2 = categories[i + 1]
                        val titleText2 = stringLoc(cat2.key, lang)
                        CategoryCard(
                            title = titleText2,
                            emoji = cat2.emoji,
                            completedLevels = if (cat2.isLocked) 0 else 3,
                            isLocked = cat2.isLocked,
                            accentColor = cat2.accentColor,
                            contentColor = cat2.contentColor,
                            onClick = { onCategorySelected(cat2.id) }
                        )
                    }
                }
            }
        }
    }
}

private data class CategoryItem(
    val id: String,
    val key: String,
    val emoji: String,
    val isLocked: Boolean,
    val accentColor: Color,
    val contentColor: Color
)

/**
 * 7. DAILY CHALLENGE
 */
@Composable
fun DailyChallengeCard(
    lang: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEEFC3) // Bright warm yellow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_challenge_card")
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF2C94C))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2C94C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚡", fontSize = 22.sp)
                }

                Column {
                    Text(
                        text = context.getString(R.string.daily_stamp_quest),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF7B5B00)
                    )
                    Text(
                        text = context.getString(R.string.guess_flags_desc),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF452F00)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF452F00))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = context.getString(R.string.play).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

/**
 * 8. DID YOU KNOW CARD
 */
@Composable
fun DidYouKnowCard(
    lang: String,
    onViewInAtlasClick: () -> Unit
) {
    val funFactResIds = listOf(
        R.string.fact_1,
        R.string.fact_2,
        R.string.fact_3,
        R.string.fact_4,
        R.string.fact_5
    )
    val randomFactResId = remember { funFactResIds.random() }
    val randomFact = stringResource(randomFactResId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("did_you_know_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFD9E2)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💡", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.did_you_know).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF90002F)
                )
                Text(
                    text = randomFact,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = stringResource(R.string.view_in_atlas),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onViewInAtlasClick() }
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}
