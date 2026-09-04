package com.multies.flagquest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.multies.flagquest.data.local.entity.MissionEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.ui.localization.Locales
import com.multies.flagquest.ui.viewmodel.GameViewModel
import com.multies.flagquest.data.repository.ClaimResult
import com.multies.flagquest.data.repository.SpinEligibility
import com.multies.flagquest.data.repository.SpinRewardConfig
import com.multies.flagquest.audio.SoundType
import com.multies.flagquest.audio.AudioManager
import com.multies.flagquest.audio.HapticHelper
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.snap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    profile: UserProfileEntity?,
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val lang = profile?.selectedLanguage ?: "en"
    var selectedTab by remember { mutableIntStateOf(0) }

    val allMissions by viewModel.allMissions.collectAsState()
    val isSpinning by viewModel.isSpinning.collectAsState()
    val wheelResult by viewModel.wheelResult.collectAsState()
    val isReducedMotion by viewModel.isReducedMotionEnabled.collectAsState()

    val eligibilityState by viewModel.eligibilityState.collectAsState()
    val nextSpinCountdown by viewModel.nextSpinCountdown.collectAsState()
    val claimResult by viewModel.claimResult.collectAsState()

    // Determine current day eligibility for daily rewards
    val lastClaimedTime = profile?.lastDailyRewardClaimed ?: 0L
    val lastClaimedDayIndex = profile?.lastDailyRewardDayClaimed ?: 0

    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = now
    val currentDayCode = calendar.get(java.util.Calendar.YEAR) * 1000 + calendar.get(java.util.Calendar.DAY_OF_YEAR)

    calendar.timeInMillis = lastClaimedTime
    val lastClaimDayCode = calendar.get(java.util.Calendar.YEAR) * 1000 + calendar.get(java.util.Calendar.DAY_OF_YEAR)

    val canClaimDailyReward = lastClaimedTime == 0L || currentDayCode != lastClaimDayCode
    val nextEligibleDayIndex = if (canClaimDailyReward) {
        if (lastClaimedDayIndex >= 7) 1 else lastClaimedDayIndex + 1
    } else {
        lastClaimedDayIndex
    }

    var showClaimSuccessAnimation by remember { mutableStateOf(false) }
    var lastClaimedRewardText by remember { mutableStateOf("") }

    // Callback on claim success
    val onClaimClick = {
        if (canClaimDailyReward) {
            val rewardText = when (nextEligibleDayIndex) {
                1 -> "50 ${Locales.get("coins", lang)}"
                2 -> "75 ${Locales.get("coins", lang)}"
                3 -> "1 Free Hint"
                4 -> "100 ${Locales.get("coins", lang)}"
                5 -> "1 Heart"
                6 -> "Premium Theme Trial"
                7 -> "Extra Maximum Heart + 150 ${Locales.get("coins", lang)}"
                else -> "Daily Reward"
            }
            lastClaimedRewardText = rewardText
            viewModel.claimDailyReward()
            showClaimSuccessAnimation = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = Locales.get("rewards_and_missions", lang, LocalContext.current),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        if (profile != null) {
                            Text(
                                text = "${Locales.get("level", lang)} ${profile.level} • ${profile.xp} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (profile != null) {
                        // Quick balance display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(text = "🪙 ${profile.coins}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "💡 ${profile.hintsCount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Material 3 Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(Locales.get("daily_rewards", lang), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(Locales.get("wheel", lang), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(Locales.get("missions", lang), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(Locales.get("shop", lang), fontWeight = FontWeight.Bold) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> DailyRewardsTab(
                        lang = lang,
                        claimedDayIndex = lastClaimedDayIndex,
                        canClaim = canClaimDailyReward,
                        nextDayIndex = nextEligibleDayIndex,
                        onClaim = onClaimClick
                    )
                    1 -> LuckyWheelTab(
                        lang = lang,
                        profile = profile,
                        isSpinning = isSpinning,
                        wheelResult = wheelResult,
                        isReducedMotion = isReducedMotion,
                        eligibilityState = eligibilityState,
                        nextSpinCountdown = nextSpinCountdown,
                        claimResult = claimResult,
                        onSpin = { viewModel.spinDailyWheel() },
                        onDismiss = { viewModel.dismissWheelResult() },
                        onDismissClaim = { viewModel.dismissClaimResult() }
                    )
                    2 -> MissionsTab(
                        lang = lang,
                        missions = allMissions,
                        onClaimMission = { viewModel.claimMissionReward(it) }
                    )
                    3 -> ShopTab(
                        lang = lang,
                        profile = profile,
                        viewModel = viewModel
                    )
                }

                // Claim success overlay animation
                if (showClaimSuccessAnimation) {
                    Dialog(onDismissRequest = { showClaimSuccessAnimation = false }) {
                        Card(
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🎉", fontSize = 64.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = Locales.get("reward_claimed", lang, LocalContext.current),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = String.format(Locales.get("you_received_format", lang, LocalContext.current), lastClaimedRewardText),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { showClaimSuccessAnimation = false },
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(Locales.get("awesome", lang, LocalContext.current))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyRewardsTab(
    lang: String,
    claimedDayIndex: Int,
    canClaim: Boolean,
    nextDayIndex: Int,
    onClaim: () -> Unit
) {
    val days = listOf(
        DayReward(1, "50 🪙", "50 Coins", "🪙"),
        DayReward(2, "75 🪙", "75 Coins", "🪙"),
        DayReward(3, "1 💡", "1 Free Hint", "💡"),
        DayReward(4, "100 🪙", "100 Coins", "🪙"),
        DayReward(5, "1 ❤️", "1 Heart", "❤️"),
        DayReward(6, "Trial 🎨", "Premium Theme", "🎨"),
        DayReward(7, "Bonus 👑", "Max Heart + 150 Coins", "🎁")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = Locales.get("seven_day_gift_calendar", lang, LocalContext.current),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = Locales.get("calendar_desc", lang, LocalContext.current),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid for Day 1-6
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(6) { idx ->
                val day = days[idx]
                val isClaimed = day.dayIndex <= claimedDayIndex
                val isCurrent = canClaim && day.dayIndex == nextDayIndex

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isClaimed) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else if (isCurrent) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${Locales.get("day", lang)} ${day.dayIndex}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = day.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isClaimed) Color.Gray else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Big Card for Day 7
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                val day7 = days[6]
                val isClaimed = day7.dayIndex <= claimedDayIndex
                val isCurrent = canClaim && day7.dayIndex == nextDayIndex

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isClaimed) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else if (isCurrent) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = Locales.get("day_7_grand_reward", lang, LocalContext.current),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isCurrent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Locales.get("day_7_reward_desc", lang, LocalContext.current),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(text = "🎁", fontSize = 48.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Claim Button
        Button(
            onClick = onClaim,
            enabled = canClaim,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("claim_daily_button")
        ) {
            if (canClaim) {
                Text(
                    text = Locales.get("claim_day_gift_format", lang, LocalContext.current).format(nextDayIndex),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.1.sp
                )
            } else {
                Text(
                    text = Locales.get("daily_reward_claim_msg", lang),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private data class DayReward(
    val dayIndex: Int,
    val title: String,
    val description: String,
    val emoji: String
)

@Composable
fun LuckyWheelTab(
    lang: String,
    profile: UserProfileEntity?,
    isSpinning: Boolean,
    wheelResult: String?,
    isReducedMotion: Boolean,
    eligibilityState: SpinEligibility,
    nextSpinCountdown: String,
    claimResult: ClaimResult?,
    onSpin: () -> Unit,
    onDismiss: () -> Unit,
    onDismissClaim: () -> Unit
) {
    val context = LocalContext.current
    var showOddsDialog by remember { mutableStateOf(false) }

    // CENTRALIZED SECTOR CONFIGURATION
    val segments = listOf(
        WheelSegment("5 🪙", Color(0xFFFFCDD2), "coins_5"),
        WheelSegment("10 🪙", Color(0xFFF0F4C3), "coins_10"),
        WheelSegment("25 🪙", Color(0xFFE1BEE7), "coins_25"),
        WheelSegment("50 🪙", Color(0xFFB2EBF2), "coins_50"),
        WheelSegment("100 🪙", Color(0xFFFFE082), "coins_100"),
        WheelSegment("200 🪙", Color(0xFFC8E6C9), "coins_200"),
        WheelSegment("1 ❤️", Color(0xFFD1C4E9), "heart_1"),
        WheelSegment("2 ❤️", Color(0xFFFFCCBC), "hearts_2"),
        WheelSegment("3 ❤️", Color(0xFFB3E5FC), "hearts_3")
    )

    val sectorAngle = 360f / 9f

    // Rotation calculation
    var targetRotationAngle by remember { mutableStateOf(0f) }
    LaunchedEffect(isSpinning, wheelResult) {
        if (isSpinning && wheelResult != null) {
            val targetIndex = segments.indexOfFirst { it.rewardCode == wheelResult }.coerceAtLeast(0)
            val rounds = if (isReducedMotion) 2 else 6
            // Rotate so target lands perfectly under pointer at -90 degrees
            targetRotationAngle = 360f * rounds - (targetIndex * sectorAngle)
        }
    }

    val animatedAngle by animateFloatAsState(
        targetValue = if (isSpinning) targetRotationAngle else targetRotationAngle % 360f,
        animationSpec = if (isReducedMotion) {
            snap()
        } else {
            tween(
                durationMillis = 3500,
                easing = CubicBezierEasing(0.12f, 1f, 0.22f, 1f)
            )
        },
        label = "WheelRotation"
    )

    // Dynamic segment highlight calculation for ticks during spin
    val currentSegment = remember(animatedAngle) {
        val relativeAngle = (360f - (animatedAngle % 360f) + (sectorAngle / 2f)) % 360f
        (relativeAngle / sectorAngle).toInt() % 9
    }

    LaunchedEffect(currentSegment) {
        if (isSpinning && !isReducedMotion) {
            AudioManager.playSoundEffect(SoundType.TICK)
            HapticHelper.triggerTick(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Heading
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = Locales.get("daily_spin", lang),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Locales.get("already_spun", lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Custom drawn Canvas Wheel Container
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(8.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Sector Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedAngle)
            ) {
                val numSegments = segments.size
                val arcAngle = 360f / numSegments

                for (i in 0 until numSegments) {
                    val startAngle = i * arcAngle - 90f - (arcAngle / 2f)
                    drawArc(
                        color = segments[i].color,
                        startAngle = startAngle,
                        sweepAngle = arcAngle,
                        useCenter = true
                    )
                }

                // Divider Pegs/Lines
                for (i in 0 until numSegments) {
                    val angleDeg = i * arcAngle - 90f - (arcAngle / 2f)
                    val radians = angleDeg * PI / 180f
                    val lineLength = size.width / 2f
                    val x = center.x + lineLength * cos(radians).toFloat()
                    val y = center.y + lineLength * sin(radians).toFloat()
                    drawLine(
                        color = Color.Black.copy(alpha = 0.12f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Labels rotating in sync
            val numSegments = segments.size
            val arcAngle = 360f / numSegments
            for (i in 0 until numSegments) {
                val textAngle = animatedAngle + (i * arcAngle)
                Box(
                    modifier = Modifier
                        .rotate(textAngle)
                        .padding(bottom = 145.dp)
                ) {
                    Text(
                        text = segments[i].label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }

            // Central Hub with Pin Indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(3.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📌", fontSize = 22.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }

        // Action controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            when (eligibilityState) {
                is SpinEligibility.Eligible -> {
                    Button(
                        onClick = onSpin,
                        enabled = !isSpinning,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("spin_wheel_button")
                    ) {
                        Text(
                            text = if (isSpinning) Locales.get("spinning", lang) else Locales.get("spin_now", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.1.sp
                        )
                    }
                }
                is SpinEligibility.AlreadyClaimedToday -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("spin_wheel_disabled")
                    ) {
                        Text(
                            text = "${Locales.get("next_spin", lang)} $nextSpinCountdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is SpinEligibility.SuspiciousClock -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = Locales.get("time_uncertain", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transparent odds dialog trigger
            TextButton(
                onClick = { showOddsDialog = true },
                modifier = Modifier.testTag("odds_info_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = Locales.get("prob_info", lang),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // ODDS DIALOG
    if (showOddsDialog) {
        AlertDialog(
            onDismissRequest = { showOddsDialog = false },
            title = {
                Text(
                    text = Locales.get("prob_info", lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column {
                    SpinRewardConfig.rewards.forEach { reward ->
                        val rewardLabel = when (reward.id) {
                            "coins_5" -> "5 Coins 🪙"
                            "coins_10" -> "10 Coins 🪙"
                            "coins_25" -> "25 Coins 🪙"
                            "coins_50" -> "50 Coins 🪙"
                            "coins_100" -> "100 Coins 🪙"
                            "coins_200" -> "200 Coins 🪙"
                            "heart_1" -> "1 Heart ❤️"
                            "hearts_2" -> "2 Hearts ❤️"
                            "hearts_3" -> "3 Hearts ❤️"
                            else -> reward.id
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = rewardLabel, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(
                                text = reward.probabilityText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOddsDialog = false }) {
                    Text(Locales.get("ok", lang, LocalContext.current), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // CELEBRATION CLAIM RESULT DIALOG
    if (claimResult != null && !isSpinning) {
        val reward = SpinRewardConfig.rewards.find { it.id == claimResult.rewardId }
        val wonLabel = when (claimResult.rewardId) {
            "coins_5" -> "5 Coins 🪙"
            "coins_10" -> "10 Coins 🪙"
            "coins_25" -> "25 Coins 🪙"
            "coins_50" -> "50 Coins 🪙"
            "coins_100" -> "100 Coins 🪙"
            "coins_200" -> "200 Coins 🪙"
            "heart_1" -> "1 Heart ❤️"
            "hearts_2" -> "2 Hearts ❤️"
            "hearts_3" -> "3 Hearts ❤️"
            else -> claimResult.rewardId
        }

        // Play celebration audio & haptic on claim dialog opening
        LaunchedEffect(claimResult) {
            AudioManager.playSoundEffect(SoundType.MILESTONE)
            HapticHelper.triggerMilestone(context)
        }

        Dialog(onDismissRequest = onDismissClaim) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("claim_result_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎉", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = Locales.get("congrats", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = wonLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    if (claimResult.overflowCoins > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = Locales.get("hearts_full", lang) + "\n+${claimResult.overflowCoins} Coins 🪙",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismissClaim,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().testTag("claim_collect_button")
                    ) {
                        Text(Locales.get("collect", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class WheelSegment(
    val label: String,
    val color: Color,
    val rewardCode: String
)

@Composable
fun MissionsTab(
    lang: String,
    missions: List<MissionEntity>,
    onClaimMission: (String) -> Unit
) {
    val context = LocalContext.current
    if (missions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = Locales.get("no_missions_active", lang, context), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val dailyMissions = missions.filter { it.type == "DAILY" }
    val weeklyMissions = missions.filter { it.type == "WEEKLY" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (dailyMissions.isNotEmpty()) {
            item {
                Text(
                    text = Locales.get("daily_missions", lang, context),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(dailyMissions) { mission ->
                MissionCard(lang = lang, mission = mission, onClaim = { onClaimMission(mission.id) })
            }
        }

        if (weeklyMissions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = Locales.get("weekly_grand_missions", lang, context),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(weeklyMissions) { mission ->
                MissionCard(lang = lang, mission = mission, onClaim = { onClaimMission(mission.id) })
            }
        }
    }
}

@Composable
fun MissionCard(
    lang: String,
    mission: MissionEntity,
    onClaim: () -> Unit
) {
    val context = LocalContext.current
    val titleKey = if (mission.id.isNotEmpty()) mission.id else mission.titleKey
    val title = Locales.getMissionTitle(titleKey, lang, context)
    val desc = Locales.getMissionDesc(titleKey, mission.targetProgress, lang, context)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.isClaimed) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (mission.type == "WEEKLY") "⚡" else "📋",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (mission.isClaimed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (mission.isCompleted && !mission.isClaimed) {
                    Button(
                        onClick = onClaim,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("claim_mission_${mission.id}")
                    ) {
                        Text(Locales.get("claim", lang, context), fontWeight = FontWeight.Bold)
                    }
                } else if (mission.isClaimed) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${Locales.get("claimed", lang, context)} ✓",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    Text(
                        text = "${mission.currentProgress} / ${mission.targetProgress}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            val progressFraction = if (mission.targetProgress > 0) {
                mission.currentProgress.toFloat() / mission.targetProgress.toFloat()
            } else {
                0f
            }.coerceIn(0f, 1f)

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (mission.type == "WEEKLY") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Reward details
                Text(
                    text = "🪙 +${mission.rewardCoins}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// Customization Shop Item Model
data class ShopItem(
    val id: String,
    val name: String,
    val cost: Int,
    val category: String,
    val primaryColor: Color = Color.Gray,
    val secondaryColor: Color = Color.LightGray,
    val description: String = ""
) {
    fun getLocalizedName(lang: String, context: android.content.Context? = null): String {
        val key = "item_${category}_${id}_name"
        val localized = Locales.get(key, lang, context)
        return if (localized != key) localized else name
    }

    fun getLocalizedDesc(lang: String, context: android.content.Context? = null): String {
        val key = "item_${category}_${id}_desc"
        val localized = Locales.get(key, lang, context)
        return if (localized != key) localized else description
    }
}

@Composable
fun ShopTab(
    lang: String,
    profile: UserProfileEntity?,
    viewModel: GameViewModel
) {
    var selectedCategory by remember { mutableStateOf("themes") }
    
    // Collect settings states from view model
    val selectedThemeId by viewModel.selectedThemeId.collectAsState()
    val purchasedThemeIds by viewModel.purchasedThemeIds.collectAsState()
    
    val selectedBackground by viewModel.selectedBackground.collectAsState()
    val purchasedBackgrounds by viewModel.purchasedBackgrounds.collectAsState()

    val selectedAnswerCard by viewModel.selectedAnswerCard.collectAsState()
    val purchasedAnswerCards by viewModel.purchasedAnswerCards.collectAsState()

    val selectedButtonStyle by viewModel.selectedButtonStyle.collectAsState()
    val purchasedButtonStyles by viewModel.purchasedButtonStyles.collectAsState()

    val selectedProfileFrame by viewModel.selectedProfileFrame.collectAsState()
    val purchasedProfileFrames by viewModel.purchasedProfileFrames.collectAsState()

    val selectedPassportCover by viewModel.selectedPassportCover.collectAsState()
    val purchasedPassportCovers by viewModel.purchasedPassportCovers.collectAsState()

    val selectedCelebrationEffect by viewModel.selectedCelebrationEffect.collectAsState()
    val purchasedCelebrationEffects by viewModel.purchasedCelebrationEffects.collectAsState()

    // Preview state
    var previewItem by remember { mutableStateOf<ShopItem?>(null) }
    
    // Dialog states
    var itemToPurchase by remember { mutableStateOf<ShopItem?>(null) }
    var showNotEnoughCoinsAlert by remember { mutableStateOf(false) }

    // Categories definition
    val context = LocalContext.current
    val categories = listOf(
        "themes" to Locales.get("themes", lang, context),
        "backgrounds" to Locales.get("backgrounds", lang, context),
        "answer_cards" to Locales.get("answer_cards", lang, context),
        "buttons" to Locales.get("buttons", lang, context),
        "frames" to Locales.get("frames", lang, context),
        "passport_covers" to Locales.get("passport_covers", lang, context),
        "celebration" to Locales.get("celebration", lang, context)
    )

    // Items population
    val themesItems = listOf(
        ShopItem("vibrant_world", "Vibrant World", 0, "themes", Color(0xFF0061A4), Color(0xFFF7F9FF), "Default vibrant light theme with beautiful details."),
        ShopItem("light", "Minimalist Light", 0, "themes", Color(0xFF1E1E1E), Color(0xFFFAFAFA), "Clean, minimalist light design for focused gameplay."),
        ShopItem("dark", "Classic Dark", 0, "themes", Color(0xFF9ECAFF), Color(0xFF1A1C1E), "Sleek, dark, elegant night theme that reduces eye strain."),
        ShopItem("space", "Space Odyssey", 150, "themes", Color(0xFFBB86FC), Color(0xFF0A0B1E), "A futuristic deep-space theme with cosmic purple neon glows."),
        ShopItem("ancient_map", "Ancient Map", 200, "themes", Color(0xFF8B5E3C), Color(0xFFF5E6CC), "Parchment sand aesthetic mimicking vintage explorer maps."),
        ShopItem("neon", "Cyber Neon", 250, "themes", Color(0xFFFF007F), Color(0xFF000000), "Vaporwave glowing cyber pink and electric green styles."),
        ShopItem("ocean", "Ocean Deep", 250, "themes", Color(0xFF00F0FF), Color(0xFF011627), "Deep marine teal and high-contrast aqua tides."),
        ShopItem("desert", "Sahara Sands", 300, "themes", Color(0xFFD35400), Color(0xFFFFF5E6), "Warm golden dunes and vibrant clay terracotta accents."),
        ShopItem("aurora", "Aurora Borealis", 300, "themes", Color(0xFF00FFCC), Color(0xFF05110E), "Dark boreal woods backdropped by green curtains of light.")
    )

    val backgroundItems = listOf(
        ShopItem("default", "Classic Canvas", 0, "backgrounds", Color.Gray, Color.White, "Default clean background canvas."),
        ShopItem("grid", "Blueprint Grid", 50, "backgrounds", Color(0xFF2196F3), Color.White, "Sophisticated architect layout grid lines."),
        ShopItem("waves", "Dynamic Waves", 80, "backgrounds", Color(0xFF00BCD4), Color.White, "Peaceful vector wave lines rolling across edges."),
        ShopItem("stars", "Midnight Starry", 120, "backgrounds", Color(0xFFFFEB3B), Color.Black, "A beautiful starry constellation backdrop."),
        ShopItem("striped", "Retro Stripes", 150, "backgrounds", Color(0xFFFF5722), Color.White, "Vintage dynamic retro racing stripe margins.")
    )

    val answerCardItems = listOf(
        ShopItem("default", "Standard Rounded", 0, "answer_cards", Color.Gray, Color.White, "Standard Material card borders."),
        ShopItem("rounded_elegant", "Sculpted Corners", 50, "answer_cards", Color(0xFF9C27B0), Color.White, "Extra deep-rounded luxury curves (24dp)."),
        ShopItem("neon_border", "Laser Glow", 100, "answer_cards", Color(0xFFE91E63), Color.White, "Double-bordered glowing cyber-neon outline card."),
        ShopItem("glassmorphic", "Frosted Glass", 150, "answer_cards", Color(0xFF00E676), Color.White, "Beautiful semi-translucent glass overlay look.")
    )

    val buttonItems = listOf(
        ShopItem("default", "Classic Button", 0, "buttons", Color.Gray, Color.White, "Material 3 filled button style."),
        ShopItem("glossy_rounded", "Liquid Gel Pill", 50, "buttons", Color(0xFF00BCD4), Color.White, "Glossy glassy button pill with soft shadows."),
        ShopItem("retro_blocky", "8-Bit Arcade", 100, "buttons", Color(0xFF4CAF50), Color.White, "Retro pixelated block borders with thick borders."),
        ShopItem("gradient_pulse", "Cosmic Gradient", 150, "buttons", Color(0xFFE91E63), Color.White, "Modern two-tone flowing linear color gradients.")
    )

    val profileFrameItems = listOf(
        ShopItem("default", "Standard Circle", 0, "frames", Color.Gray, Color.White, "No frames overlay."),
        ShopItem("bronze_laurels", "Bronze Laurels", 50, "frames", Color(0xFFCD7F32), Color.White, "Wreath of bronze olive leaves around avatar."),
        ShopItem("silver_shield", "Silver Shield", 100, "frames", Color(0xFFC0C0C0), Color.White, "Sleek metallic silver protective crest trim."),
        ShopItem("golden_crowns", "Imperial Gold", 150, "frames", Color(0xFFFFD700), Color.White, "Shining 18k royal golden crown topping avatar.")
    )

    val passportCoverItems = listOf(
        ShopItem("default", "Global Standard", 0, "passport_covers", Color.Gray, Color.White, "Classic blue standard explorer passport book."),
        ShopItem("vintage_leather", "Vintage Leather", 80, "passport_covers", Color(0xFF8B5E3C), Color.White, "Warm distressed grain genuine brown leather binder."),
        ShopItem("royal_velvet", "Royal Velvet", 120, "passport_covers", Color(0xFF3F51B5), Color.White, "Elegant royal purple velvet fabric book stitching."),
        ShopItem("golden_crest", "Golden Crest", 180, "passport_covers", Color(0xFFFFD700), Color.White, "High-contrast golden eagle crest heat-stamped.")
    )

    val celebrationItems = listOf(
        ShopItem("default", "Material Ripple", 0, "celebration", Color.Gray, Color.White, "Standard successful level animation."),
        ShopItem("sparklers", "Electric Sparklers", 50, "celebration", Color(0xFFFFEB3B), Color.White, "Bright colorful sparks blasting from answer center."),
        ShopItem("confetti_rain", "Confetti Rain", 100, "celebration", Color(0xFFFF4081), Color.White, "Delightful paper confetti showers tumbling down."),
        ShopItem("firework_blast", "Firework Show", 150, "celebration", Color(0xFF00E676), Color.White, "Magnificent dynamic multi-colored firework rockets.")
    )

    val activeItems = when (selectedCategory) {
        "themes" -> themesItems
        "backgrounds" -> backgroundItems
        "answer_cards" -> answerCardItems
        "buttons" -> buttonItems
        "frames" -> profileFrameItems
        "passport_covers" -> passportCoverItems
        "celebration" -> celebrationItems
        else -> themesItems
    }

    // Determine current ownership set
    val ownedSet = when (selectedCategory) {
        "themes" -> purchasedThemeIds.split(",").toSet()
        "backgrounds" -> purchasedBackgrounds.split(",").toSet()
        "answer_cards" -> purchasedAnswerCards.split(",").toSet()
        "buttons" -> purchasedButtonStyles.split(",").toSet()
        "frames" -> purchasedProfileFrames.split(",").toSet()
        "passport_covers" -> purchasedPassportCovers.split(",").toSet()
        "celebration" -> purchasedCelebrationEffects.split(",").toSet()
        else -> emptySet()
    }

    val selectedId = when (selectedCategory) {
        "themes" -> selectedThemeId
        "backgrounds" -> selectedBackground
        "answer_cards" -> selectedAnswerCard
        "buttons" -> selectedButtonStyle
        "frames" -> selectedProfileFrame
        "passport_covers" -> selectedPassportCover
        "celebration" -> selectedCelebrationEffect
        else -> ""
    }

    // Auto-select first item as preview default if not selected
    LaunchedEffect(selectedCategory) {
        previewItem = activeItems.find { it.id == selectedId } ?: activeItems.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { (catId, label) ->
                    val isSelected = selectedCategory == catId
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .clickable { selectedCategory = catId }
                            .height(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Live Preview Box Header
        item {
            previewItem?.let { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${Locales.get("preview", lang, context)}: ${item.getLocalizedName(lang, context)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (ownedSet.contains(item.id)) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Owned",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Mock Representation Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(item.secondaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            when (item.category) {
                                "themes" -> {
                                    // Render a miniature styled quiz preview screen
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(20.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(item.primaryColor)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp, 28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White)
                                                    .border(1.dp, item.primaryColor, RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("A", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = item.primaryColor)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp, 28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(item.primaryColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                                "backgrounds" -> {
                                    // Custom Canvas background pattern previews
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        when (item.id) {
                                            "grid" -> {
                                                val size = 20.dp.toPx()
                                                var x = 0f
                                                while (x < this.size.width) {
                                                    drawLine(Color.LightGray.copy(alpha = 0.5f), Offset(x, 0f), Offset(x, this.size.height), strokeWidth = 1f)
                                                    x += size
                                                }
                                                var y = 0f
                                                while (y < this.size.height) {
                                                    drawLine(Color.LightGray.copy(alpha = 0.5f), Offset(0f, y), Offset(this.size.width, y), strokeWidth = 1f)
                                                    y += size
                                                }
                                            }
                                            "waves" -> {
                                                var x = 0f
                                                val path = androidx.compose.ui.graphics.Path()
                                                path.moveTo(0f, this.size.height / 2)
                                                while (x < this.size.width) {
                                                    path.quadraticBezierTo(
                                                        x + 50f, this.size.height / 2 - 40f,
                                                        x + 100f, this.size.height / 2
                                                    )
                                                    path.quadraticBezierTo(
                                                        x + 150f, this.size.height / 2 + 40f,
                                                        x + 200f, this.size.height / 2
                                                    )
                                                    x += 200f
                                                }
                                                drawPath(path, Color.Cyan, style = Stroke(width = 3f))
                                            }
                                            "stars" -> {
                                                drawCircle(Color.Yellow, 4f, Offset(40f, 30f))
                                                drawCircle(Color.Yellow, 3f, Offset(200f, 50f))
                                                drawCircle(Color.Yellow, 5f, Offset(350f, 20f))
                                                drawCircle(Color.Yellow, 4f, Offset(120f, 90f))
                                                drawCircle(Color.Yellow, 3f, Offset(280f, 100f))
                                            }
                                            "striped" -> {
                                                drawLine(Color.Red.copy(alpha = 0.4f), Offset(0f, 30f), Offset(this.size.width, 30f), strokeWidth = 8f)
                                                drawLine(Color(0xFFFFA500).copy(alpha = 0.4f), Offset(0f, 42f), Offset(this.size.width, 42f), strokeWidth = 8f)
                                                drawLine(Color.Yellow.copy(alpha = 0.4f), Offset(0f, 54f), Offset(this.size.width, 54f), strokeWidth = 8f)
                                            }
                                            else -> {
                                                // Default canvas
                                            }
                                        }
                                    }
                                    Text(item.getLocalizedName(lang, context), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                }
                                "answer_cards" -> {
                                    // Show answer card style
                                    val shape = when (item.id) {
                                        "rounded_elegant" -> RoundedCornerShape(24.dp)
                                        else -> RoundedCornerShape(12.dp)
                                    }
                                    val borderModifier = when (item.id) {
                                        "neon_border" -> Modifier.border(2.dp, Color(0xFFFF007F), shape)
                                        else -> Modifier.border(1.dp, Color.LightGray, shape)
                                    }
                                    Card(
                                        shape = shape,
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        modifier = Modifier
                                            .width(200.dp)
                                            .height(60.dp)
                                            .then(borderModifier)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Text(
                                                Locales.get("option_preview", lang, context), 
                                                style = MaterialTheme.typography.bodyMedium, 
                                                fontWeight = FontWeight.Bold, 
                                                color = if (item.id == "neon_border") Color(0xFFFF007F) else Color.DarkGray
                                            )
                                        }
                                    }
                                }
                                "buttons" -> {
                                    // Button visual representation
                                    val shape = when (item.id) {
                                        "glossy_rounded" -> RoundedCornerShape(24.dp)
                                        "retro_blocky" -> RoundedCornerShape(0.dp)
                                        else -> RoundedCornerShape(12.dp)
                                    }
                                    val borderModifier = when (item.id) {
                                        "retro_blocky" -> Modifier.border(3.dp, Color.Black, shape)
                                        else -> Modifier
                                    }
                                    val background = when (item.id) {
                                        "gradient_pulse" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF00F0FF)))
                                        else -> null
                                    }
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .width(180.dp)
                                            .height(48.dp)
                                            .clip(shape)
                                            .background(Color(0xFF0061A4))
                                            .then(borderModifier)
                                    ) {
                                        if (background != null) {
                                            Box(modifier = Modifier.fillMaxSize().background(background))
                                        }
                                        Text(
                                            Locales.get("submit_answer", lang, context),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                                "frames" -> {
                                    // Render a profile frame preview circling a generic avatar
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                                        // Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(Color.LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("👦", fontSize = 32.sp)
                                        }
                                        // Frame overlay
                                        when (item.id) {
                                            "bronze_laurels" -> {
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    drawCircle(Color(0xFFCD7F32), radius = 35.dp.toPx(), style = Stroke(width = 4.dp.toPx()))
                                                }
                                                Text("🌿", fontSize = 14.sp, modifier = Modifier.align(Alignment.BottomCenter))
                                            }
                                            "silver_shield" -> {
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    drawCircle(Color(0xFFC0C0C0), radius = 35.dp.toPx(), style = Stroke(width = 5.dp.toPx()))
                                                }
                                            }
                                            "golden_crowns" -> {
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    drawCircle(Color(0xFFFFD700), radius = 35.dp.toPx(), style = Stroke(width = 5.dp.toPx()))
                                                }
                                                Text("👑", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp))
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                                "passport_covers" -> {
                                    // Mini Passport Design Representation
                                    val coverColor = when (item.id) {
                                        "vintage_leather" -> Color(0xFF6E473B)
                                        "royal_velvet" -> Color(0xFF3F51B5)
                                        "golden_crest" -> Color(0xFF34495E)
                                        else -> Color(0xFF1B4F72)
                                    }
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = coverColor),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(110.dp)
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(Locales.get("passport_preview_label", lang, context), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = if (item.id == "golden_crest") "⚜️" else "🌍", 
                                                fontSize = 20.sp, 
                                                color = if (item.id == "golden_crest") Color(0xFFFFD700) else Color.White
                                            )
                                            Text(Locales.get("passport_app_name", lang, context), fontSize = 6.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                        }
                                    }
                                }
                                "celebration" -> {
                                    // Celebration representation
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🏆", fontSize = 48.sp)
                                        when (item.id) {
                                            "sparklers" -> Text(Locales.get("sparkles_label", lang, context), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
                                            "confetti_rain" -> Text(Locales.get("confetti_label", lang, context), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Magenta)
                                            "firework_blast" -> Text(Locales.get("fireworks_label", lang, context), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = item.getLocalizedDesc(lang, context),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions for Preview Item
                        val isOwned = ownedSet.contains(item.id)
                        val isEquipped = item.id == selectedId

                        if (isEquipped) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text(Locales.get("equipped", lang), fontWeight = FontWeight.Black)
                            }
                        } else if (isOwned) {
                            Button(
                                onClick = {
                                    when (selectedCategory) {
                                        "themes" -> viewModel.selectTheme(item.id)
                                        "backgrounds" -> viewModel.selectBackground(item.id)
                                        "answer_cards" -> viewModel.selectAnswerCard(item.id)
                                        "buttons" -> viewModel.selectButtonStyle(item.id)
                                        "frames" -> viewModel.selectProfileFrame(item.id)
                                        "passport_covers" -> viewModel.selectPassportCover(item.id)
                                        "celebration" -> viewModel.selectCelebrationEffect(item.id)
                                    }
                                    // Synthesize equp sound
                                    com.multies.flagquest.audio.AudioManager.playSoundEffect(com.multies.flagquest.audio.SoundType.MILESTONE)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(Locales.get("equip", lang), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (profile != null && profile.coins >= item.cost) {
                                        itemToPurchase = item
                                    } else {
                                        showNotEnoughCoinsAlert = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${Locales.get("unlock", lang)} (🪙 ${item.cost})", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Grid list of items
        gridItems(
            items = activeItems,
            nColumns = 2
        ) { item ->
            val isOwned = ownedSet.contains(item.id)
            val isEquipped = item.id == selectedId
            val isSelectedForPreview = previewItem?.id == item.id
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { previewItem = item }
                    .border(
                        width = if (isSelectedForPreview) 2.dp else 1.dp,
                        color = if (isSelectedForPreview) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelectedForPreview) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Small thumbnail preview
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(item.primaryColor)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isOwned) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (isEquipped) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Equipped",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.getLocalizedName(lang, context),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isOwned) {
                        Text(
                            text = if (isEquipped) Locales.get("equipped", lang) else Locales.get("equip", lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEquipped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🪙 ${item.cost}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Purchase confirmation dialog
    itemToPurchase?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToPurchase = null },
            title = { Text(Locales.get("confirm_purchase", lang), fontWeight = FontWeight.Black) },
            text = { Text(String.format(Locales.get("purchase_msg", lang), item.cost)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Deduct coins and record purchase
                        when (selectedCategory) {
                            "themes" -> viewModel.purchaseTheme(item.id, item.cost)
                            "backgrounds" -> viewModel.purchaseBackground(item.id, item.cost)
                            "answer_cards" -> viewModel.purchaseAnswerCard(item.id, item.cost)
                            "buttons" -> viewModel.purchaseButtonStyle(item.id, item.cost)
                            "frames" -> viewModel.purchaseProfileFrame(item.id, item.cost)
                            "passport_covers" -> viewModel.purchasePassportCover(item.id, item.cost)
                            "celebration" -> viewModel.purchaseCelebrationEffect(item.id, item.cost)
                        }
                        // Instantly equip newly purchased item!
                        when (selectedCategory) {
                            "themes" -> viewModel.selectTheme(item.id)
                            "backgrounds" -> viewModel.selectBackground(item.id)
                            "answer_cards" -> viewModel.selectAnswerCard(item.id)
                            "buttons" -> viewModel.selectButtonStyle(item.id)
                            "frames" -> viewModel.selectProfileFrame(item.id)
                            "passport_covers" -> viewModel.selectPassportCover(item.id)
                            "celebration" -> viewModel.selectCelebrationEffect(item.id)
                        }
                        
                        // Play rewarding purchase sound
                        com.multies.flagquest.audio.AudioManager.playSoundEffect(com.multies.flagquest.audio.SoundType.MILESTONE)
                        
                        itemToPurchase = null
                    }
                ) {
                    Text(Locales.get("claim", lang), fontWeight = FontWeight.Bold) // Reuse claim string as confirm/unlock
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToPurchase = null }) {
                    Text(if (lang == "ar") "إلغاء" else if (lang == "de") "Abbrechen" else if (lang == "fr") "Annuler" else "Cancel")
                }
            }
        )
    }

    // Not enough coins alert
    if (showNotEnoughCoinsAlert) {
        AlertDialog(
            onDismissRequest = { showNotEnoughCoinsAlert = false },
            title = { Text(Locales.get("not_enough_coins", lang, LocalContext.current), fontWeight = FontWeight.Black) },
            text = { Text(Locales.get("not_enough_coins_desc", lang, LocalContext.current)) },
            confirmButton = {
                TextButton(onClick = { showNotEnoughCoinsAlert = false }) {
                    Text(Locales.get("ok", lang, LocalContext.current))
                }
            }
        )
    }
}

// Utility extension function to render grid items easily inside LazyColumn
fun <T> LazyListScope.gridItems(
    items: List<T>,
    nColumns: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    itemContent: @Composable RowScope.(T) -> Unit
) {
    val rows = items.chunked(nColumns)
    this.items(rows) { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = horizontalArrangement
        ) {
            rowItems.forEach { item ->
                Box(modifier = Modifier.weight(1f)) {
                    this@Row.itemContent(item)
                }
            }
            // Add spacer placeholders if row is incomplete
            val emptyCells = nColumns - rowItems.size
            if (emptyCells > 0) {
                repeat(emptyCells) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
