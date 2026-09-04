package com.multies.flagquest.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object LanguageSelection : Screen("language_selection")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Journey : Screen("journey")
    object Play : Screen("play_tab")
    object Atlas : Screen("atlas")
    object Profile : Screen("profile")
    object Game : Screen("game/{categoryId}") {
        fun createRoute(categoryId: String) = "game/$categoryId"
    }
    object LevelSelection : Screen("level_selection/{categoryId}") {
        fun createRoute(categoryId: String) = "level_selection/$categoryId"
    }
    object Achievements : Screen("achievements")
    object Rewards : Screen("rewards")
    object Settings : Screen("settings")
    object AccessibilitySettings : Screen("accessibility_settings")
    object About : Screen("about")
    object FlagMemoryGame : Screen("flag_memory_game/{levelIndex}") {
        fun createRoute(levelIndex: Int) = "flag_memory_game/$levelIndex"
    }
    object SilentMapGame : Screen("silent_map_game/{levelIndex}") {
        fun createRoute(levelIndex: Int) = "silent_map_game/$levelIndex"
    }
    object WhoAmIGame : Screen("who_am_i_game/{levelIndex}") {
        fun createRoute(levelIndex: Int) = "who_am_i_game/$levelIndex"
    }
    object SpotTheFakeGame : Screen("spot_the_fake_game/{levelIndex}") {
        fun createRoute(levelIndex: Int) = "spot_the_fake_game/$levelIndex"
    }
    object CountryRankingGame : Screen("country_ranking_game/{levelIndex}") {
        fun createRoute(levelIndex: Int) = "country_ranking_game/$levelIndex"
    }
    object QuickGeographyGame : Screen("quick_geography_game/{levelIndex}") {
        fun createRoute(levelIndex: Int = 1) = "quick_geography_game/$levelIndex"
    }
    object ContinentBossIntro : Screen("continent_boss_intro")
    object ContinentBossGameplay : Screen("continent_boss_game/{bossId}") {
        fun createRoute(bossId: String) = "continent_boss_game/$bossId"
    }
}

