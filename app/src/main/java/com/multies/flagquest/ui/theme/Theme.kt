package com.multies.flagquest.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
    onError = LightOnError
)

// Minimalist Light Theme Colors
private val MinimalistLightColorScheme = lightColorScheme(
    primary = Color(0xFF1E1E1E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFECECEC),
    onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFFF1C40F),
    onSecondary = Color(0xFF2C3E50),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFF555555)
)

// Space Theme Colors (Cosmic Deep Blue/Purple)
private val SpaceColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color(0xFF1F003C),
    primaryContainer = Color(0xFF4A148C),
    onPrimaryContainer = Color(0xFFF3E5F5),
    secondary = Color(0xFFFFD54F),
    onSecondary = Color(0xFF3E2723),
    background = Color(0xFF0A0B1E),
    onBackground = Color(0xFFE0E0FF),
    surface = Color(0xFF14153B),
    onSurface = Color(0xFFE0E0FF),
    surfaceVariant = Color(0xFF282B5A),
    onSurfaceVariant = Color(0xFFB0B3D6)
)

// Ancient Map Theme Colors (Parchment/Bronze)
private val AncientMapColorScheme = lightColorScheme(
    primary = Color(0xFF8B5E3C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEDBB2),
    onPrimaryContainer = Color(0xFF3D2714),
    secondary = Color(0xFFB38D46),
    onSecondary = Color(0xFF261900),
    background = Color(0xFFF5E6CC),
    onBackground = Color(0xFF4E3629),
    surface = Color(0xFFFAF2E4),
    onSurface = Color(0xFF4E3629),
    surfaceVariant = Color(0xFFEBDCBD),
    onSurfaceVariant = Color(0xFF6F523B)
)

// Neon Theme Colors (Pitch Black/Electric Glowing Pink/Green)
private val NeonColorScheme = darkColorScheme(
    primary = Color(0xFFFF007F),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF00FF66),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFF00FFFF),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF111111),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF222222),
    onSurfaceVariant = Color(0xFFCCCCCC)
)

// Ocean Theme Colors (Teal/Deep Sea Marine)
private val OceanColorScheme = darkColorScheme(
    primary = Color(0xFF00F0FF),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF005B60),
    onPrimaryContainer = Color(0xFFE0FFFF),
    secondary = Color(0xFFFF9E00),
    onSecondary = Color(0xFF331F00),
    background = Color(0xFF011627),
    onBackground = Color(0xFFE2F1F8),
    surface = Color(0xFF0B293F),
    onSurface = Color(0xFFE2F1F8),
    surfaceVariant = Color(0xFF1B435E),
    onSurfaceVariant = Color(0xFFA5C2D5)
)

// Desert Theme Colors (Sands/Warm Rust Terracotta)
private val DesertColorScheme = lightColorScheme(
    primary = Color(0xFFD35400),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD2A6),
    onPrimaryContainer = Color(0xFF4A1C00),
    secondary = Color(0xFFE67E22),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFFF5E6),
    onBackground = Color(0xFF5C2D13),
    surface = Color(0xFFFAECD5),
    onSurface = Color(0xFF5C2D13),
    surfaceVariant = Color(0xFFECCEB1),
    onSurfaceVariant = Color(0xFF8C5333)
)

// Aurora Theme Colors (Midnight boreal dark green/black, vivid mint primary)
private val AuroraColorScheme = darkColorScheme(
    primary = Color(0xFF00FFCC),
    onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF005F4B),
    onPrimaryContainer = Color(0xFFE0FFEE),
    secondary = Color(0xFFCCFF00),
    onSecondary = Color(0xFF2B3800),
    background = Color(0xFF05110E),
    onBackground = Color(0xFFE5F5F0),
    surface = Color(0xFF0D211A),
    onSurface = Color(0xFFE5F5F0),
    surfaceVariant = Color(0xFF1A3D33),
    onSurfaceVariant = Color(0xFF99C2B8)
)

// Amoled Theme Colors (Pure Pitch Black Background)
private val AmoledColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = Color.Black,
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF101010),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    error = DarkError,
    onError = DarkOnError
)

@Composable
fun FlagQuestTheme(
    selectedThemeId: String = "vibrant_world",
    themeMode: String = "system",
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    dynamicColor: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val normalizedMode = themeMode.lowercase(java.util.Locale.ROOT)
    val isDark = when (normalizedMode) {
        "light" -> false
        "dark", "amoled" -> true
        else -> darkTheme
    }

    val isAmoledActive = isAmoled || normalizedMode == "amoled" || selectedThemeId.lowercase(java.util.Locale.ROOT) == "amoled"

    // Determine target color scheme based on user theme mode and selection
    val baseColorScheme: ColorScheme = if (isDark) {
        if (isAmoledActive) {
            AmoledColorScheme
        } else {
            when (selectedThemeId.lowercase(java.util.Locale.ROOT)) {
                "space" -> SpaceColorScheme
                "neon" -> NeonColorScheme
                "ocean" -> OceanColorScheme
                "aurora" -> AuroraColorScheme
                else -> DarkColorScheme
            }
        }
    } else {
        when (selectedThemeId.lowercase(java.util.Locale.ROOT)) {
            "light" -> MinimalistLightColorScheme
            "ancient_map" -> AncientMapColorScheme
            "desert" -> DesertColorScheme
            else -> LightColorScheme
        }
    }

    val colorScheme = if (highContrast) {
        if (isDark) {
            baseColorScheme.copy(
                background = Color.Black,
                surface = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White,
                primary = Color.White,
                onPrimary = Color.Black,
                surfaceVariant = Color.DarkGray,
                onSurfaceVariant = Color.White
            )
        } else {
            baseColorScheme.copy(
                background = Color.White,
                surface = Color.White,
                onBackground = Color.Black,
                onSurface = Color.Black,
                primary = Color.Black,
                onPrimary = Color.White,
                surfaceVariant = Color.LightGray,
                onSurfaceVariant = Color.Black
            )
        }
    } else {
        baseColorScheme
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    if (com.multies.flagquest.BuildConfig.DEBUG) {
        android.util.Log.d("ThemeDebug", "FlagQuestTheme applied -> themeMode: $themeMode, selectedThemeId: $selectedThemeId, isDark: $isDark, isAmoled: $isAmoledActive")
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
