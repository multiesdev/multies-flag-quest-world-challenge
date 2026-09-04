package com.multies.flagquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.multies.flagquest.data.local.entity.UserProfileEntity

@Composable
fun GameHeader(
    profile: UserProfileEntity?,
    onSettingsClick: (() -> Unit)? = null,
    onRefillLivesClick: (() -> Unit)? = null
) {
    val coins = profile?.coins ?: 100
    val lives = profile?.lives ?: 3
    val streak = profile?.currentStreak ?: 0
    val stars = profile?.totalStars ?: 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stars, Coins, Lives, Streak Stats
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Stars Stat
                StatBadge(
                    icon = Icons.Default.Star,
                    text = stars.toString(),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Coins Stat
                StatBadge(
                    icon = Icons.Default.MonetizationOn,
                    text = coins.toString(),
                    backgroundColor = Color(0xFFFFD98C), // Gold
                    contentColor = Color(0xFF281900)     // Dark Gold/Brown
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Lives Stat
                StatBadge(
                    icon = Icons.Default.Favorite,
                    text = lives.toString(),
                    backgroundColor = Color(0xFFFFDAD6), // Heart pink-red
                    contentColor = Color(0xFF410002),     // Dark Red
                    onClick = onRefillLivesClick
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Streak Stat
                if (streak > 0) {
                    StatBadge(
                        icon = Icons.Default.LocalActivity,
                        text = "$streak🔥",
                        backgroundColor = Color(0xFFFFD9E2), // Soft Fact/Streak Pink
                        contentColor = Color(0xFF90002F)     // Dark Streak Crimson
                    )
                }
            }

            // Settings Button
            if (onSettingsClick != null) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp)) // Vibrant high-rounded corners (rounded-full style)
            .background(backgroundColor)
            .then(clickModifier)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.width(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}
