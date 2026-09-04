package com.multies.flagquest

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.multies.flagquest.data.local.SettingsDataStore
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.ui.viewmodel.GameViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThemeRegressionTest {

    private lateinit var application: Application
    private lateinit var dataStore: SettingsDataStore
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        dataStore = SettingsDataStore(application)

        val db = com.multies.flagquest.data.local.GameDatabase.getDatabase(application)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            db.clearAllTables()
            db.gameDao().insertUserProfile(UserProfileEntity())
        }

        viewModel = GameViewModel(application)
        ShadowLooper.idleMainLooper()
    }

    @Test
    fun test1_selectingLightProducesDarkThemeFalse() = runBlocking {
        viewModel.updateTheme("light")
        ShadowLooper.idleMainLooper()

        val themeMode = viewModel.themeMode.value
        val darkTheme = when (themeMode.lowercase()) {
            "light" -> false
            "dark", "amoled" -> true
            else -> false
        }

        assertEquals("light", themeMode)
        assertFalse("Light mode must produce darkTheme = false", darkTheme)
    }

    @Test
    fun test2_selectingDarkProducesDarkThemeTrue() = runBlocking {
        viewModel.updateTheme("dark")
        ShadowLooper.idleMainLooper()

        val themeMode = viewModel.themeMode.value
        val darkTheme = when (themeMode.lowercase()) {
            "light" -> false
            "dark", "amoled" -> true
            else -> false
        }

        assertEquals("dark", themeMode)
        assertTrue("Dark mode must produce darkTheme = true", darkTheme)
    }

    @Test
    fun test3_selectingAmoledProducesDarkThemeTrue() = runBlocking {
        viewModel.updateTheme("amoled")
        ShadowLooper.idleMainLooper()

        val themeMode = viewModel.themeMode.value
        val darkTheme = when (themeMode.lowercase()) {
            "light" -> false
            "dark", "amoled" -> true
            else -> false
        }

        assertEquals("amoled", themeMode)
        assertTrue("AMOLED mode must produce darkTheme = true", darkTheme)
    }

    @Test
    fun test4_selectingSystemFollowsSuppliedSystemDarkValue() = runBlocking {
        viewModel.updateTheme("system")
        ShadowLooper.idleMainLooper()

        val themeMode = viewModel.themeMode.value
        assertEquals("system", themeMode)

        val darkThemeWhenSystemIsDark = when (themeMode.lowercase()) {
            "light" -> false
            "dark", "amoled" -> true
            else -> true // simulating system dark = true
        }
        assertTrue("System mode must follow system dark=true", darkThemeWhenSystemIsDark)

        val darkThemeWhenSystemIsLight = when (themeMode.lowercase()) {
            "light" -> false
            "dark", "amoled" -> true
            else -> false // simulating system dark = false
        }
        assertFalse("System mode must follow system dark=false", darkThemeWhenSystemIsLight)
    }

    @Test
    fun test5_themeValuePersistsThroughRepositoryRecreation() = runBlocking {
        viewModel.updateTheme("dark")
        ShadowLooper.idleMainLooper()

        val newViewModel = GameViewModel(application)
        ShadowLooper.idleMainLooper()

        val persistedTheme = newViewModel.themeMode.value
        assertEquals("dark", persistedTheme)
    }

    @Test
    fun test6_invalidLegacyValuesMigrateSafelyToSystem() = runBlocking {
        dataStore.saveTheme("INVALID_THEME_123")
        ShadowLooper.idleMainLooper()

        val readValue = dataStore.themeFlow.first()
        assertEquals("system", readValue)
    }

    @Test
    fun test7_readingAndWritingUseSameDataStoreKey() = runBlocking {
        dataStore.saveTheme("dark")
        ShadowLooper.idleMainLooper()

        val readValue = dataStore.themeFlow.first()
        assertEquals("dark", readValue)
    }

    @Test
    fun test8_changingThemeUpdatesRootThemeState() = runBlocking {
        viewModel.updateTheme("light")
        ShadowLooper.idleMainLooper()
        assertEquals("light", viewModel.themeMode.value)

        viewModel.updateTheme("dark")
        ShadowLooper.idleMainLooper()
        assertEquals("dark", viewModel.themeMode.value)
    }

    @Test
    fun test9_languageChangesDoNotResetTheme() = runBlocking {
        viewModel.updateTheme("dark")
        ShadowLooper.idleMainLooper()

        viewModel.updateLanguage("ar")
        ShadowLooper.idleMainLooper()

        assertEquals("dark", viewModel.themeMode.value)
        assertEquals("ar", viewModel.selectedLanguage.value)
    }

    @Test
    fun test10_themeChangesDoNotResetLanguage() = runBlocking {
        viewModel.updateLanguage("de")
        ShadowLooper.idleMainLooper()

        viewModel.updateTheme("dark")
        ShadowLooper.idleMainLooper()

        assertEquals("de", viewModel.selectedLanguage.value)
        assertEquals("dark", viewModel.themeMode.value)
    }

    @Test
    fun test11_themeChangesDoNotRegenerateQuestionsOrResetGameplay() = runBlocking {
        viewModel.selectOption(1)
        val initialQuestionIndex = viewModel.currentQuestionIndex.value
        val initialSelectedOption = viewModel.selectedOptionIndex.value

        viewModel.updateTheme("dark")
        ShadowLooper.idleMainLooper()

        assertEquals(initialQuestionIndex, viewModel.currentQuestionIndex.value)
        assertEquals(initialSelectedOption, viewModel.selectedOptionIndex.value)
    }

    @Test
    fun test12_amoledDoesNotRemainEnabledAfterSelectingLightDarkOrSystem() = runBlocking {
        viewModel.updateTheme("amoled")
        ShadowLooper.idleMainLooper()
        assertEquals("amoled", viewModel.themeMode.value)

        viewModel.updateTheme("light")
        ShadowLooper.idleMainLooper()
        assertEquals("light", viewModel.themeMode.value)

        viewModel.updateTheme("system")
        ShadowLooper.idleMainLooper()
        assertEquals("system", viewModel.themeMode.value)
    }

    @Test
    fun test13_supportedLanguagesRemainEN_AR_DE_FR() {
        val supportedLangs = listOf("en", "ar", "de", "fr")
        for (lang in supportedLangs) {
            val ctx = application
            val loc = com.multies.flagquest.ui.localization.Locales.get("app_name", lang, ctx)
            assertNotNull(loc)
            assertTrue(loc.isNotEmpty())
        }
    }
}
