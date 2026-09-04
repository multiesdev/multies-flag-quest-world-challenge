package com.multies.flagquest

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.features.whoami.WhoAmIViewModel
import com.multies.flagquest.features.whoami.WhoAmIQuestionState
import com.multies.flagquest.features.whoami.engine.WhoAmILevelConfig
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WhoAmIViewModelTest {

    private lateinit var application: Application
    private lateinit var viewModel: WhoAmIViewModel
    private val sampleCountries = mutableListOf<Country>()

    @Before
    fun setUp() = kotlinx.coroutines.runBlocking {
        application = ApplicationProvider.getApplicationContext()

        // Setup Database
        val db = com.multies.flagquest.data.local.GameDatabase.getDatabase(application)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            db.clearAllTables()
            db.gameDao().insertUserProfile(com.multies.flagquest.data.local.entity.UserProfileEntity())
        }

        viewModel = WhoAmIViewModel(application)

        // Load Countries
        sampleCountries.clear()
        val jsonFile = listOf(
            File("src/main/assets/countries.json"),
            File("app/src/main/assets/countries.json"),
            File("../app/src/main/assets/countries.json")
        ).firstOrNull { it.exists() }

        val jsonText = jsonFile?.readText() ?: try {
            val context: Context = ApplicationProvider.getApplicationContext()
            context.assets.open("countries.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }

        if (jsonText.isNullOrEmpty()) {
            error("Could not find or load countries.json asset file")
        }

        val jsonArray = JSONArray(jsonText)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            sampleCountries.add(parseCountry(obj))
        }

        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun parseCountry(obj: JSONObject): Country {
        fun getStringOrThrow(key: String, id: String): String {
            if (!obj.has(key) || obj.isNull(key)) {
                throw IllegalArgumentException("Country '$id' is missing required field '$key'")
            }
            return obj.getString(key)
        }

        val id = if (obj.has("id")) obj.getString("id") else throw IllegalArgumentException("Country object missing 'id'")

        return Country(
            id = id,
            nameEn = getStringOrThrow("nameEn", id),
            nameAr = getStringOrThrow("nameAr", id),
            nameDe = getStringOrThrow("nameDe", id),
            nameFr = getStringOrThrow("nameFr", id),
            flagEmoji = getStringOrThrow("flagEmoji", id),
            capitalEn = getStringOrThrow("capitalEn", id),
            capitalAr = getStringOrThrow("capitalAr", id),
            capitalDe = getStringOrThrow("capitalDe", id),
            capitalFr = getStringOrThrow("capitalFr", id),
            continentEn = getStringOrThrow("continentEn", id),
            continentAr = getStringOrThrow("continentAr", id),
            continentDe = getStringOrThrow("continentDe", id),
            continentFr = getStringOrThrow("continentFr", id),
            subregionEn = obj.optString("subregionEn", "Global"),
            subregionAr = obj.optString("subregionAr", "عالمي"),
            subregionDe = obj.optString("subregionDe", "Global"),
            subregionFr = obj.optString("subregionFr", "Global"),
            population = obj.optLong("population", 0L),
            areaSqKm = obj.optDouble("areaSqKm", 0.0),
            currencyEn = getStringOrThrow("currencyEn", id),
            currencyAr = getStringOrThrow("currencyAr", id),
            currencyDe = getStringOrThrow("currencyDe", id),
            currencyFr = getStringOrThrow("currencyFr", id),
            funFactEn = getStringOrThrow("funFactEn", id),
            funFactAr = getStringOrThrow("funFactAr", id),
            funFactDe = getStringOrThrow("funFactDe", id),
            funFactFr = getStringOrThrow("funFactFr", id),
            languagesEn = obj.optString("languagesEn", "English"),
            languagesAr = obj.optString("languagesAr", "الإنجليزية"),
            languagesDe = obj.optString("languagesDe", "Englisch"),
            languagesFr = obj.optString("languagesFr", "Anglais"),
            organizationsEn = obj.optString("organizationsEn", "UN"),
            organizationsAr = obj.optString("organizationsAr", "الأمم المتحدة"),
            organizationsDe = obj.optString("organizationsDe", "UN"),
            organizationsFr = obj.optString("organizationsFr", "ONU")
        )
    }

    private fun advanceTime(ms: Long) {
        shadowOf(Looper.getMainLooper()).idleFor(ms, TimeUnit.MILLISECONDS)
    }

    private fun waitForState(predicate: () -> Boolean) {
        var attempts = 0
        while (!predicate() && attempts < 150) {
            shadowOf(Looper.getMainLooper()).idleFor(10, TimeUnit.MILLISECONDS)
            Thread.sleep(10)
            attempts++
        }
    }

    @Test
    fun testInitialStateAndStartSession() {
        // Initially empty state
        var state = viewModel.uiState.value
        assertTrue(state.questions.isEmpty())
        assertEquals(0, state.currentQuestionIndex)

        // Start session for Level 1
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        state = viewModel.uiState.value

        assertFalse(state.questions.isEmpty())
        assertEquals(0, state.currentQuestionIndex)
        assertEquals(WhoAmIQuestionState.AWAITING_ANSWER, state.questionState)
        assertEquals(0, state.currentScore)
    }

    @Test
    fun testAllLevelsSupported() {
        assertEquals(50, WhoAmILevelConfig.TOTAL_LEVELS)
        // Check start of all 50 levels doesn't crash
        for (i in 1..50) {
            viewModel.startSession(levelIndex = i, countries = sampleCountries, lang = "en", existingHighScore = 0)
            shadowOf(Looper.getMainLooper()).idle()
            val state = viewModel.uiState.value
            assertFalse(state.questions.isEmpty())
        }
    }

    @Test
    fun testSubmitCorrectAnswer() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        var state = viewModel.uiState.value
        val correctIndex = state.questions[0].correctIndex

        viewModel.selectOption(correctIndex, levelIndex = 1, existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        state = viewModel.uiState.value

        assertTrue(state.isInputLocked)
        assertEquals(correctIndex, state.selectedOptionIndex)
        assertEquals(WhoAmIQuestionState.SHOWING_FEEDBACK, state.questionState)
        assertEquals(1, state.correctAnswersCount)
        assertEquals(1, state.currentStreak)
        assertTrue(state.currentScore > 0)
    }

    @Test
    fun testSubmitIncorrectAnswer() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        var state = viewModel.uiState.value
        val incorrectIndex = (state.questions[0].correctIndex + 1) % 4

        viewModel.selectOption(incorrectIndex, levelIndex = 1, existingHighScore = 1000)
        shadowOf(Looper.getMainLooper()).idle()
        state = viewModel.uiState.value

        assertTrue(state.isInputLocked)
        assertEquals(incorrectIndex, state.selectedOptionIndex)
        assertEquals(WhoAmIQuestionState.SHOWING_FEEDBACK, state.questionState)
        assertEquals(0, state.correctAnswersCount)
        assertEquals(0, state.currentStreak)
        assertEquals(0, state.currentScore)
    }

    @Test
    fun testNoDoubleScoringWhenLocked() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        var state = viewModel.uiState.value
        val correctIndex = state.questions[0].correctIndex

        viewModel.selectOption(correctIndex, levelIndex = 1, existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        state = viewModel.uiState.value
        val scoreAfterFirstSubmit = state.currentScore
        assertEquals(1, state.correctAnswersCount)

        // Submit again while locked
        viewModel.selectOption(correctIndex, levelIndex = 1, existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        state = viewModel.uiState.value

        // Score and correct count should remain exactly the same
        assertEquals(scoreAfterFirstSubmit, state.currentScore)
        assertEquals(1, state.correctAnswersCount)
    }

    @Test
    fun testRevealClues() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        var state = viewModel.uiState.value
        assertEquals(1, state.revealedCluesCount)

        viewModel.revealNextClue()
        shadowOf(Looper.getMainLooper()).idle()
        state = viewModel.uiState.value
        assertEquals(2, state.revealedCluesCount)

        viewModel.revealNextClue()
        shadowOf(Looper.getMainLooper()).idle()
        state = viewModel.uiState.value
        assertEquals(3, state.revealedCluesCount)
    }

    @Test
    fun testPauseGame() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        assertFalse(viewModel.uiState.value.isPaused)

        viewModel.setPaused(true)
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(viewModel.uiState.value.isPaused)

        viewModel.setPaused(false)
        shadowOf(Looper.getMainLooper()).idle()
        assertFalse(viewModel.uiState.value.isPaused)
    }

    @Test
    fun testAdvancesToNextQuestionExactlyOnce() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        var state = viewModel.uiState.value
        val correctIndex = state.questions[0].correctIndex

        viewModel.selectOption(correctIndex, levelIndex = 1, existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(WhoAmIQuestionState.SHOWING_FEEDBACK, viewModel.uiState.value.questionState)

        advanceTime(1800)

        state = viewModel.uiState.value
        assertEquals(1, state.currentQuestionIndex)
        assertEquals(WhoAmIQuestionState.AWAITING_ANSWER, state.questionState)
        assertFalse(state.isInputLocked)
    }

    @Test
    fun testFinalQuestionCompletesSession() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        val totalQuestions = viewModel.uiState.value.questions.size

        for (i in 0 until totalQuestions) {
            val q = viewModel.uiState.value.questions[i]
            viewModel.selectOption(q.correctIndex, levelIndex = 1, existingHighScore = 0)
            shadowOf(Looper.getMainLooper()).idle()

            advanceTime(1800)
        }

        waitForState { viewModel.uiState.value.questionState == WhoAmIQuestionState.COMPLETED }

        val state = viewModel.uiState.value
        assertEquals(WhoAmIQuestionState.COMPLETED, state.questionState)
        assertTrue(state.showVictoryDialog)
        assertNotNull(state.resultSummary)
        assertEquals(3, state.resultSummary!!.starsEarned)
    }

    @Test
    fun testRepeatedCompletionDoesNotSaveTwice() = kotlinx.coroutines.runBlocking {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        val totalQuestions = viewModel.uiState.value.questions.size

        for (i in 0 until totalQuestions) {
            val q = viewModel.uiState.value.questions[i]
            viewModel.selectOption(q.correctIndex, levelIndex = 1, existingHighScore = 0)
            shadowOf(Looper.getMainLooper()).idle()

            advanceTime(1800)
        }

        waitForState { viewModel.uiState.value.questionState == WhoAmIQuestionState.COMPLETED }

        val scoreAtCompletion = viewModel.uiState.value.currentScore
        val db = com.multies.flagquest.data.local.GameDatabase.getDatabase(application)
        
        val progressListBefore = db.gameDao().getProgressByCategoryFlow("WHO_AM_I").first()
        val initialCoins = db.gameDao().getUserProfile()?.coins ?: 0

        // Attempting to select options again (re-completion attempt)
        viewModel.selectOption(0, levelIndex = 1, existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        advanceTime(3000)

        val progressListAfter = db.gameDao().getProgressByCategoryFlow("WHO_AM_I").first()
        val finalCoins = db.gameDao().getUserProfile()?.coins ?: 0
        assertEquals(progressListBefore.size, progressListAfter.size)
        assertEquals(scoreAtCompletion, viewModel.uiState.value.currentScore)
        assertEquals(initialCoins, finalCoins)
    }

    @Test
    fun testCompletionPersistsProgressExactlyOnce() = kotlinx.coroutines.runBlocking {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        val totalQuestions = viewModel.uiState.value.questions.size

        for (i in 0 until totalQuestions) {
            val q = viewModel.uiState.value.questions[i]
            viewModel.selectOption(q.correctIndex, levelIndex = 1, existingHighScore = 0)
            shadowOf(Looper.getMainLooper()).idle()

            advanceTime(1800)
        }

        waitForState { viewModel.uiState.value.questionState == WhoAmIQuestionState.COMPLETED }

        val db = com.multies.flagquest.data.local.GameDatabase.getDatabase(application)
        val progressList = db.gameDao().getProgressByCategoryFlow("WHO_AM_I").first()

        assertFalse(progressList.isEmpty())
        val progress = progressList.first { it.levelIndex == 1 }
        assertEquals(3, progress.starsEarned)
        assertTrue(progress.highestScore > 0)
    }

    @Test
    fun testCompletionPersistsCoinsExactlyOnce() = kotlinx.coroutines.runBlocking {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        val totalQuestions = viewModel.uiState.value.questions.size

        for (i in 0 until totalQuestions) {
            val q = viewModel.uiState.value.questions[i]
            viewModel.selectOption(q.correctIndex, levelIndex = 1, existingHighScore = 0)
            shadowOf(Looper.getMainLooper()).idle()

            advanceTime(1800)
        }

        waitForState { viewModel.uiState.value.questionState == WhoAmIQuestionState.COMPLETED }

        val state = viewModel.uiState.value
        val db = com.multies.flagquest.data.local.GameDatabase.getDatabase(application)
        val profile = db.gameDao().getUserProfile()

        assertNotNull(profile)
        assertTrue(state.resultSummary!!.coinsEarned > 0)
        // Profile starts with 100 coins, plus 10 level completion bonus for Level 1, plus coinsEarned
        val expectedCoins = 100 + 10 + state.resultSummary!!.coinsEarned
        assertEquals(expectedCoins, profile?.coins ?: 0)
    }

    @Test
    fun testTimerAdvancesDuringActiveGameplay() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(0L, viewModel.uiState.value.elapsedTimeSec)

        advanceTime(2500)
        assertTrue(viewModel.uiState.value.elapsedTimeSec >= 2L)
    }

    @Test
    fun testTimerStopsWhilePaused() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(0L, viewModel.uiState.value.elapsedTimeSec)

        viewModel.setPaused(true)
        shadowOf(Looper.getMainLooper()).idle()

        advanceTime(2500)
        assertEquals(0L, viewModel.uiState.value.elapsedTimeSec)
    }

    @Test
    fun testTimerStopsDuringFeedback() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(0L, viewModel.uiState.value.elapsedTimeSec)

        val q = viewModel.uiState.value.questions[0]
        viewModel.selectOption(q.correctIndex, levelIndex = 1, existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(WhoAmIQuestionState.SHOWING_FEEDBACK, viewModel.uiState.value.questionState)

        val initialTime = viewModel.uiState.value.elapsedTimeSec
        advanceTime(1000)
        assertEquals(initialTime, viewModel.uiState.value.elapsedTimeSec)
    }

    @Test
    fun testTimerStopsAfterCompletion() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        val totalQuestions = viewModel.uiState.value.questions.size

        for (i in 0 until totalQuestions) {
            val q = viewModel.uiState.value.questions[i]
            viewModel.selectOption(q.correctIndex, levelIndex = 1, existingHighScore = 0)
            shadowOf(Looper.getMainLooper()).idle()

            advanceTime(1800)
        }

        waitForState { viewModel.uiState.value.questionState == WhoAmIQuestionState.COMPLETED }
        val finalTime = viewModel.uiState.value.elapsedTimeSec

        advanceTime(2500)
        assertEquals(finalTime, viewModel.uiState.value.elapsedTimeSec)
    }

    @Test
    fun testTimerAndFeedbackCleanupOnCleared() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()

        val onClearedMethod = WhoAmIViewModel::class.java.getDeclaredMethod("onCleared")
        onClearedMethod.isAccessible = true
        onClearedMethod.invoke(viewModel)

        val initialTime = viewModel.uiState.value.elapsedTimeSec
        advanceTime(2500)
        assertEquals(initialTime, viewModel.uiState.value.elapsedTimeSec)
    }

    @Test
    fun testResetOnStartSession() {
        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()

        val q = viewModel.uiState.value.questions[0]
        viewModel.selectOption(q.correctIndex, levelIndex = 1, existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        
        advanceTime(500)
        assertTrue(viewModel.uiState.value.currentScore > 0)

        viewModel.startSession(levelIndex = 1, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()

        val state = viewModel.uiState.value
        assertEquals(0, state.currentScore)
        assertEquals(0L, state.elapsedTimeSec)
        assertEquals(WhoAmIQuestionState.AWAITING_ANSWER, state.questionState)
        assertEquals(0, state.currentQuestionIndex)
    }

    @Test
    fun testLevel50CompletesWithoutAdvancingToLevel51() {
        viewModel.startSession(levelIndex = 50, countries = sampleCountries, lang = "en", existingHighScore = 0)
        shadowOf(Looper.getMainLooper()).idle()
        val totalQuestions = viewModel.uiState.value.questions.size

        for (i in 0 until totalQuestions) {
            val q = viewModel.uiState.value.questions[i]
            viewModel.selectOption(q.correctIndex, levelIndex = 50, existingHighScore = 0)
            shadowOf(Looper.getMainLooper()).idle()

            advanceTime(1800)
        }

        waitForState { viewModel.uiState.value.questionState == WhoAmIQuestionState.COMPLETED }

        val state = viewModel.uiState.value
        assertEquals(WhoAmIQuestionState.COMPLETED, state.questionState)
        assertEquals(totalQuestions - 1, state.currentQuestionIndex)
    }
}
