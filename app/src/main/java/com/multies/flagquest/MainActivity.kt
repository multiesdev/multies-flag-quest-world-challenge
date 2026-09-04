package com.multies.flagquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.multies.flagquest.ui.navigation.Screen
import com.multies.flagquest.ui.screens.AchievementsScreen
import com.multies.flagquest.ui.screens.RewardsScreen
import com.multies.flagquest.ui.screens.AtlasScreen
import com.multies.flagquest.ui.screens.GamePlayScreen
import com.multies.flagquest.ui.screens.HomeScreen
import com.multies.flagquest.ui.screens.JourneyScreen
import com.multies.flagquest.ui.screens.PlayScreen
import com.multies.flagquest.ui.screens.ProfileScreen
import com.multies.flagquest.ui.screens.SettingsScreen
import com.multies.flagquest.ui.screens.SplashScreen
import com.multies.flagquest.ui.screens.LanguageSelectionScreen
import com.multies.flagquest.ui.screens.OnboardingScreen
import com.multies.flagquest.ui.screens.AccessibilitySettingsScreen
import com.multies.flagquest.ui.screens.AboutScreen
import com.multies.flagquest.ui.screens.FlagMemoryGameScreen
import com.multies.flagquest.ui.screens.SilentMapGameScreen
import com.multies.flagquest.ui.screens.WhoAmIGameScreen
import com.multies.flagquest.ui.screens.ContinentBossIntroScreen
import com.multies.flagquest.ui.screens.ContinentBossGameplayScreen
import com.multies.flagquest.ui.theme.FlagQuestTheme
import com.multies.flagquest.ui.viewmodel.GameViewModel
import com.multies.flagquest.ui.viewmodel.ContinentBossViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private val bossViewModel: ContinentBossViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Observe profile settings and datastore states
            val profile by viewModel.userProfile.collectAsState()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val selectedThemeId by viewModel.selectedThemeId.collectAsState()
            val isHighContrast by viewModel.isHighContrastEnabled.collectAsState()
            val isReducedMotion by viewModel.isReducedMotionEnabled.collectAsState()
            val isMusicEnabled by viewModel.isMusicEnabled.collectAsState()
            val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
            val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()

            // Set dynamic dark theme depending on user setting
            val normalizedMode = themeMode.lowercase(java.util.Locale.ROOT)
            val darkTheme = when (normalizedMode) {
                "light" -> false
                "dark", "amoled" -> true
                else -> isSystemInDarkTheme()
            }

            if (BuildConfig.DEBUG) {
                android.util.Log.d("ThemeDebug", "MainActivity root theme -> themeMode: $themeMode, selectedThemeId: $selectedThemeId, darkTheme: $darkTheme")
            }

            // Support full dynamic RTL for Arabic, even if OS is in English
            val layoutDirection = if (selectedLanguage == "ar") {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            val context = androidx.compose.ui.platform.LocalContext.current
            val localizedContext = androidx.compose.runtime.remember(selectedLanguage) {
                val locale = java.util.Locale(selectedLanguage)
                java.util.Locale.setDefault(locale)
                val config = android.content.res.Configuration(context.resources.configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                context.createConfigurationContext(config)
            }

            FlagQuestTheme(
                selectedThemeId = selectedThemeId,
                themeMode = themeMode,
                darkTheme = darkTheme,
                isAmoled = (normalizedMode == "amoled"),
                highContrast = isHighContrast
            ) {
                CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalContext provides localizedContext,
                    LocalLayoutDirection provides layoutDirection
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 0a. Splash Screen
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                lang = selectedLanguage,
                                onNavigateNext = {
                                    navController.navigate(Screen.LanguageSelection.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 0b. Language Selection Screen
                        composable(Screen.LanguageSelection.route) {
                            LanguageSelectionScreen(
                                currentLanguage = selectedLanguage,
                                onLanguageSelected = { viewModel.updateLanguage(it) },
                                onContinue = {
                                    navController.navigate(Screen.Onboarding.route) {
                                        popUpTo(Screen.LanguageSelection.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 0c. Onboarding Screen
                        composable(Screen.Onboarding.route) {
                            OnboardingScreen(
                                lang = selectedLanguage,
                                onGetStarted = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 1. Home / Dashboard Screen
                        composable(Screen.Home.route) {
                            HomeScreen(
                                profile = profile,
                                navController = navController,
                                onCategorySelected = { categoryId ->
                                    if (categoryId == "FLAGS") {
                                        navController.navigate(Screen.LevelSelection.createRoute(categoryId))
                                    } else {
                                        viewModel.startNewQuiz(categoryId)
                                        navController.navigate(Screen.Game.createRoute(categoryId))
                                    }
                                },
                                onRefillLives = { viewModel.refillLives() }
                            )
                        }

                        // 1b. Journey Roadmap Screen
                        composable(Screen.Journey.route) {
                            val allProgress by viewModel.allProgress.collectAsState()
                            JourneyScreen(
                                profile = profile,
                                allProgress = allProgress,
                                navController = navController,
                                onLevelSelected = { cat, lvl ->
                                    viewModel.startLevel(cat, lvl)
                                    navController.navigate(Screen.Game.createRoute(cat))
                                },
                                onRefillLives = { viewModel.refillLives() },
                                viewModel = viewModel
                            )
                        }

                        // 1c. Special Play Challenge Modes Screen
                        composable(Screen.Play.route) {
                            PlayScreen(
                                profile = profile,
                                navController = navController,
                                onCategorySelected = { categoryId ->
                                    if (categoryId == "FLAGS") {
                                        navController.navigate(Screen.LevelSelection.createRoute(categoryId))
                                    } else {
                                        viewModel.startNewQuiz(categoryId)
                                        navController.navigate(Screen.Game.createRoute(categoryId))
                                    }
                                },
                                onRefillLives = { viewModel.refillLives() }
                            )
                        }

                        // 1e. Level Selection Screen
                        composable(Screen.LevelSelection.route) { backStackEntry ->
                            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: "FLAGS"
                            val allProgress by viewModel.allProgress.collectAsState()
                            
                            com.multies.flagquest.ui.screens.LevelSelectionScreen(
                                profile = profile,
                                allProgress = allProgress,
                                categoryId = categoryId,
                                navController = navController,
                                onLevelSelected = { cat, lvl ->
                                    if (cat.equals("MEMORY", ignoreCase = true)) {
                                        navController.navigate(Screen.FlagMemoryGame.createRoute(lvl))
                                    } else if (cat.equals("SILENT_MAP", ignoreCase = true)) {
                                        navController.navigate(Screen.SilentMapGame.createRoute(lvl))
                                    } else if (cat.equals("WHO_AM_I", ignoreCase = true)) {
                                        navController.navigate(Screen.WhoAmIGame.createRoute(lvl))
                                    } else if (cat.equals("SPOT_THE_FAKE", ignoreCase = true)) {
                                        navController.navigate(Screen.SpotTheFakeGame.createRoute(lvl))
                                    } else if (cat.equals("RANKING", ignoreCase = true) || cat.equals("COUNTRY_RANKING", ignoreCase = true)) {
                                        navController.navigate(Screen.CountryRankingGame.createRoute(lvl))
                                    } else if (cat.equals("QUICK_GEOGRAPHY", ignoreCase = true)) {
                                        navController.navigate(Screen.QuickGeographyGame.createRoute(lvl))
                                    } else {
                                        viewModel.startLevel(cat, lvl)
                                        navController.navigate(Screen.Game.createRoute(cat))
                                    }
                                },
                                onRefillLives = { viewModel.refillLives() }
                            )
                        }

                        // 1f. Flag Memory Gameplay Screen
                        composable(
                            route = Screen.FlagMemoryGame.route,
                            arguments = listOf(navArgument("levelIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val levelIndex = backStackEntry.arguments?.getInt("levelIndex") ?: 1
                            FlagMemoryGameScreen(
                                levelIndex = levelIndex,
                                viewModel = viewModel,
                                navController = navController,
                                profile = profile
                            )
                        }

                        // 1g. Silent Map Challenge Screen
                        composable(
                            route = Screen.SilentMapGame.route,
                            arguments = listOf(navArgument("levelIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val levelIndex = backStackEntry.arguments?.getInt("levelIndex") ?: 1
                            SilentMapGameScreen(
                                levelIndex = levelIndex,
                                viewModel = viewModel,
                                navController = navController,
                                profile = profile
                            )
                        }

                        // 1h. Who Am I? Challenge Screen
                        composable(
                            route = Screen.WhoAmIGame.route,
                            arguments = listOf(navArgument("levelIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val levelIndex = backStackEntry.arguments?.getInt("levelIndex") ?: 1
                            WhoAmIGameScreen(
                                levelIndex = levelIndex,
                                viewModel = viewModel,
                                navController = navController,
                                profile = profile
                            )
                        }

                        // 1i. Spot the Fake Flag Screen
                        composable(
                            route = Screen.SpotTheFakeGame.route,
                            arguments = listOf(navArgument("levelIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val levelIndex = backStackEntry.arguments?.getInt("levelIndex") ?: 1
                            com.multies.flagquest.ui.screens.SpotTheFakeGameScreen(
                                levelIndex = levelIndex,
                                viewModel = viewModel,
                                navController = navController,
                                profile = profile
                            )
                        }

                        // 1j. Country Ranking Challenge Screen
                        composable(
                            route = Screen.CountryRankingGame.route,
                            arguments = listOf(navArgument("levelIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val levelIndex = backStackEntry.arguments?.getInt("levelIndex") ?: 1
                            com.multies.flagquest.ui.screens.CountryRankingGameScreen(
                                levelIndex = levelIndex,
                                viewModel = viewModel,
                                navController = navController,
                                profile = profile
                            )
                        }

                        // 1k. Quick Geography Challenge Screen
                        composable(
                            route = Screen.QuickGeographyGame.route,
                            arguments = listOf(navArgument("levelIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val levelIndex = backStackEntry.arguments?.getInt("levelIndex") ?: 1
                            com.multies.flagquest.ui.screens.QuickGeographyGameScreen(
                                levelIndex = levelIndex,
                                viewModel = viewModel,
                                navController = navController
                            )
                        }

                        // 1d. Profile and Achievements Dashboard Screen
                        composable(Screen.Profile.route) {
                            val achievements by viewModel.allAchievements.collectAsState()
                            ProfileScreen(
                                profile = profile,
                                achievements = achievements,
                                navController = navController,
                                onRefillLives = { viewModel.refillLives() }
                            )
                        }

                        // 2. Interactive Gameplay Screen
                        composable(Screen.Game.route) { backStackEntry ->
                            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: "FLAGS"
                            
                            val quizQuestions by viewModel.quizQuestions.collectAsState()
                            val countries by viewModel.countries.collectAsState()
                            val currentIndex by viewModel.currentQuestionIndex.collectAsState()
                            val selectedOption by viewModel.selectedOptionIndex.collectAsState()
                            val isAnswered by viewModel.isAnswered.collectAsState()
                            val showDidYouKnow by viewModel.showDidYouKnow.collectAsState()
                            val quizCompleted by viewModel.quizCompleted.collectAsState()
                            val starsEarned by viewModel.quizStars.collectAsState()
                            val timerRemaining by viewModel.timerRemaining.collectAsState()
                            val timerActive by viewModel.timerActive.collectAsState()
                            val usedFiftyFifty by viewModel.usedFiftyFifty.collectAsState()
                            val eliminatedOptionIndices by viewModel.eliminatedOptionIndices.collectAsState()
                            val mixedScore by viewModel.mixedScore.collectAsState()
                            val mixedHighScore by viewModel.mixedHighScore.collectAsState()

                            GamePlayScreen(
                                profile = profile,
                                quizQuestions = quizQuestions,
                                countries = countries,
                                currentIndex = currentIndex,
                                selectedOption = selectedOption,
                                isAnswered = isAnswered,
                                showDidYouKnow = showDidYouKnow,
                                quizCompleted = quizCompleted,
                                starsEarned = starsEarned,
                                timerRemaining = timerRemaining,
                                timerActive = timerActive,
                                usedFiftyFifty = usedFiftyFifty,
                                eliminatedOptionIndices = eliminatedOptionIndices,
                                activeCategoryId = categoryId,
                                mixedScore = mixedScore,
                                mixedHighScore = mixedHighScore,
                                onOptionSelected = { viewModel.selectOption(it) },
                                onSubmit = { viewModel.submitAnswer() },
                                onDismissDidYouKnow = { viewModel.dismissDidYouKnow() },
                                onBackToHome = {
                                     navController.navigate(Screen.Home.route) {
                                         popUpTo(Screen.Home.route) { inclusive = false }
                                     }
                                 },
                                onPlayAgain = {
                                    val activeLvl = viewModel.activeLevelIndex.value
                                    if (activeLvl != null) {
                                        viewModel.startLevel(categoryId, activeLvl)
                                    } else {
                                        viewModel.startNewQuiz(categoryId)
                                    }
                                },
                                onRefillLives = { viewModel.refillLives() },
                                onUseFiftyFifty = { viewModel.useFiftyFifty() },
                                onBuyHeart = { viewModel.recoverHeartWithCoins() },
                                 onNextLevel = {
                                     android.util.Log.d("NEXT_LEVEL_TRACE", "CALLBACK_INVOKED in MainActivity Game screen")
                                     val activeLvl = viewModel.activeLevelIndex.value
                                     val catId = backStackEntry.arguments?.getString("categoryId") ?: "FLAGS"
                                     android.util.Log.d("NEXT_LEVEL_TRACE", "GAME_MODE: $catId, CURRENT_LEVEL: $activeLvl")
                                     val maxLevels = if (catId.equals("FLAGS", ignoreCase = true)) 100 else 50
                                     val targetLevel = if (activeLvl != null) activeLvl + 1 else 2
                                     android.util.Log.d("NEXT_LEVEL_TRACE", "NEXT_LEVEL: $targetLevel")
                                     if (activeLvl == null || activeLvl < maxLevels) {
                                         android.util.Log.d("NEXT_LEVEL_TRACE", "START_NEXT_LEVEL_CALLED for level $targetLevel")
                                         viewModel.startLevel(catId, targetLevel)
                                     }
                                 }
                            )
                        }

                        // 3. Interactive Atlas Screen
                        composable("atlas?countryId={countryId}") { backStackEntry ->
                            val countryId = backStackEntry.arguments?.getString("countryId")
                            val countries by viewModel.countries.collectAsState()
                            val discoveries by viewModel.atlasDiscoveries.collectAsState()
                            val achievements by viewModel.allAchievements.collectAsState()

                            AtlasScreen(
                                lang = selectedLanguage,
                                countries = countries,
                                discoveries = discoveries,
                                onBack = { navController.popBackStack() },
                                navController = navController,
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onRelatedQuizSelected = { category ->
                                    viewModel.startNewQuiz(category)
                                    navController.navigate(Screen.Game.createRoute(category))
                                },
                                allAchievements = achievements,
                                initialCountryId = countryId
                            )
                        }

                        // 4. Achievements Screen
                        composable(Screen.Achievements.route) {
                            val achievements by viewModel.allAchievements.collectAsState()

                            AchievementsScreen(
                                profile = profile,
                                achievements = achievements,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 4b. Rewards, Daily Wheel and Missions Screen
                        composable(Screen.Rewards.route) {
                            RewardsScreen(
                                profile = profile,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 5. Settings Screen
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                lang = selectedLanguage,
                                activeTheme = themeMode,
                                isMusic = isMusicEnabled,
                                isSound = isSoundEnabled,
                                isVibration = isVibrationEnabled,
                                onLanguageChange = { viewModel.updateLanguage(it) },
                                onThemeChange = { viewModel.updateTheme(it) },
                                onMusicToggle = { viewModel.toggleMusic(it) },
                                onSoundToggle = { viewModel.toggleSound(it) },
                                onVibrationToggle = { viewModel.toggleVibration(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 6. Accessibility Settings Screen
                        composable(Screen.AccessibilitySettings.route) {
                            AccessibilitySettingsScreen(
                                lang = selectedLanguage,
                                isHighContrast = isHighContrast,
                                isReducedMotion = isReducedMotion,
                                onHighContrastToggle = { viewModel.toggleHighContrast(it) },
                                onReducedMotionToggle = { viewModel.toggleReducedMotion(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 7. About Screen
                        composable(Screen.About.route) {
                            AboutScreen(
                                lang = selectedLanguage,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 8. Continent Boss Screens
                        composable(Screen.ContinentBossIntro.route) {
                            ContinentBossIntroScreen(
                                bossViewModel = bossViewModel,
                                navController = navController,
                                lang = selectedLanguage
                            )
                        }

                        composable("continent_boss_game/{bossId}") { backStackEntry ->
                            val bossId = backStackEntry.arguments?.getString("bossId") ?: "boss_europe"
                            val countries by viewModel.countries.collectAsState()
                            ContinentBossGameplayScreen(
                                bossId = bossId,
                                bossViewModel = bossViewModel,
                                navController = navController,
                                countries = countries,
                                lang = selectedLanguage
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        com.multies.flagquest.audio.AudioManager.startBackgroundMusic()
    }

    override fun onStop() {
        super.onStop()
        com.multies.flagquest.audio.AudioManager.stopBackgroundMusic()
    }
}
