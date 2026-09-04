package com.multies.flagquest

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.multies.flagquest.ui.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.launch

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameViewModelTest {

    private lateinit var application: Application
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() = kotlinx.coroutines.runBlocking {
        application = ApplicationProvider.getApplicationContext()
        
        val db = com.multies.flagquest.data.local.GameDatabase.getDatabase(application)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            db.clearAllTables()
            db.gameDao().insertUserProfile(com.multies.flagquest.data.local.entity.UserProfileEntity())
        }
        
        viewModel = GameViewModel(application)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
    }

    @Test
    fun testViewModelInitialization() {
        assertNotNull(viewModel)
        assertEquals(0, viewModel.currentQuestionIndex.value)
        assertEquals(false, viewModel.isAnswered.value)
        assertEquals(false, viewModel.quizCompleted.value)
        assertEquals(false, viewModel.timerActive.value)
        assertEquals(0, viewModel.timerRemaining.value)
        assertEquals(false, viewModel.usedFiftyFifty.value)
        assertEquals(0, viewModel.eliminatedOptionIndices.value.size)
    }

    @Test
    fun testSelectOptionAndStateChange() {
        viewModel.selectOption(1)
        assertEquals(1, viewModel.selectedOptionIndex.value)
    }

    @Test
    fun testDismissDidYouKnowResetsStates() {
        viewModel.dismissDidYouKnow()
        assertEquals(false, viewModel.showDidYouKnow.value)
    }

    @Test
    fun testToggleRelaxedMode() {
        // Verify toggle runs without crashing
        viewModel.toggleRelaxedMode(true)
    }

    @Test
    fun testToggleClassroomMode() {
        // Verify toggle runs without crashing
        viewModel.toggleClassroomMode(true)
    }

    @Test
    fun testToggleAdaptiveDifficulty() {
        // Verify toggle runs without crashing
        viewModel.toggleAdaptiveDifficulty(true)
    }

    @Test
    fun testSpinDailyWheelState() = kotlinx.coroutines.runBlocking {
        // Collect userProfile to activate WhileSubscribed StateFlow
        val collectJob = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
            viewModel.userProfile.collect {}
        }
        
        // Wait for background IO thread to complete DB write and emit profile
        var attempts = 0
        while (viewModel.userProfile.value == null && attempts < 40) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(50)
            attempts++
        }

        println("DEBUG: profile value = ${viewModel.userProfile.value}")

        // Initially no wheel result and not spinning
        assertEquals(null, viewModel.wheelResult.value)
        assertEquals(false, viewModel.isSpinning.value)

        // Spin deterministic COINS_100
        viewModel.spinDailyWheel("COINS_100")
        
        println("DEBUG: spinning value after spin call = ${viewModel.isSpinning.value}")

        // 1. Wait for spinning to start
        var attemptsStart = 0
        while (!viewModel.isSpinning.value && attemptsStart < 40) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(50)
            attemptsStart++
        }

        // 2. Wait for background IO thread to complete database writes and set isSpinning to false
        var attemptsSpin = 0
        while (viewModel.isSpinning.value && attemptsSpin < 120) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(50)
            attemptsSpin++
        }

        println("DEBUG: wheelResult after idle = ${viewModel.wheelResult.value}")
        println("DEBUG: spinning after idle = ${viewModel.isSpinning.value}")

        // Assert state updates
        assertEquals("coins_100", viewModel.wheelResult.value)
        assertEquals(false, viewModel.isSpinning.value)

        // Dismiss resets the result
        viewModel.dismissWheelResult()
        assertEquals(null, viewModel.wheelResult.value)

        collectJob.cancel()
    }

    @Test
    fun testMissionsAndAchievementsState() {
        // Assert reactive missions flow is initialized
        assertNotNull(viewModel.allMissions.value)
        
        // Assert achievements flow is initialized
        assertNotNull(viewModel.allAchievements.value)
    }

    @Test
    fun testLevelQuestionCountAndSelection() = kotlinx.coroutines.runBlocking {
        try {
            // Wait for background IO thread to load questions
            var attempts = 0
            while (viewModel.questions.value.isEmpty() && attempts < 200) {
                org.robolectric.shadows.ShadowLooper.idleMainLooper()
                Thread.sleep(10)
                attempts++
            }
            println("DEBUG_TEST: general questions size = ${viewModel.questions.value.size}, attempts = $attempts")

            // Collect quizQuestions to activate stateFlow
            val collectJob = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
                viewModel.quizQuestions.collect {}
            }

            // Start level 1 (should fetch 5 questions)
            viewModel.startLevel("FLAGS", 1)
            var wait1 = 0
            while (viewModel.quizQuestions.value.size != 5 && wait1 < 200) {
                org.robolectric.shadows.ShadowLooper.idleMainLooper()
                kotlinx.coroutines.delay(10)
                wait1++
            }

            val questions = viewModel.quizQuestions.value
            println("DEBUG_TEST: questions size = ${questions.size}")
            assertNotNull(questions)
            // Verify question count is 5 for level <= 10
            assertEquals(5, questions.size)

            // Start level 15 (should fetch 7 questions)
            viewModel.startLevel("FLAGS", 15)
            var wait15 = 0
            while (viewModel.quizQuestions.value.size != 7 && wait15 < 200) {
                org.robolectric.shadows.ShadowLooper.idleMainLooper()
                kotlinx.coroutines.delay(10)
                wait15++
            }
            assertEquals(7, viewModel.quizQuestions.value.size)

            // Start level 25 (should fetch 10 questions)
            viewModel.startLevel("FLAGS", 25)
            var wait25 = 0
            while (viewModel.quizQuestions.value.size != 10 && wait25 < 200) {
                org.robolectric.shadows.ShadowLooper.idleMainLooper()
                kotlinx.coroutines.delay(10)
                wait25++
            }
            assertEquals(10, viewModel.quizQuestions.value.size)

            collectJob.cancel()
        } catch (e: Throwable) {
            println("CATCH_ERROR:")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testQuestionProgressionAndStarCalculations() = kotlinx.coroutines.runBlocking {
        // Collect questions and wait for loading
        val collectQuestions = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
            viewModel.questions.collect {}
        }
        var attempts = 0
        while (viewModel.questions.value.isEmpty() && attempts < 100) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(50)
            attempts++
        }
        collectQuestions.cancel()

        // Collect flows
        val collectJob = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
            viewModel.quizQuestions.collect {}
        }
        val collectIndex = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
            viewModel.currentQuestionIndex.collect {}
        }
        val collectCompleted = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
            viewModel.quizCompleted.collect {}
        }
        val collectStars = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
            viewModel.quizStars.collect {}
        }

        viewModel.startLevel("FLAGS", 1)
        var waitStart = 0
        while (viewModel.quizQuestions.value.size != 5 && waitStart < 200) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(10)
            waitStart++
        }

        val totalQs = viewModel.quizQuestions.value.size
        assertEquals(5, totalQs)

        // Progress through all 5 questions
        for (i in 0 until totalQs) {
            assertEquals(i, viewModel.currentQuestionIndex.value)
            
            // Get correct answer index
            val currentQ = viewModel.quizQuestions.value[i]
            val correctIdx = currentQ.correctOptionIndex
            
            // Select correct answer and submit
            viewModel.selectOption(correctIdx)
            viewModel.submitAnswer()
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            
            // Dismiss DYK card and advance
            viewModel.dismissDidYouKnow()
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
        }

        // Verify quiz is completed
        assertEquals(true, viewModel.quizCompleted.value)
        
        // 0 incorrect answers should result in 3 stars
        assertEquals(3, viewModel.quizStars.value)

        collectJob.cancel()
        collectIndex.cancel()
        collectCompleted.cancel()
        collectStars.cancel()
    }

    @Test
    fun testLevelCompletionPersistence() = kotlinx.coroutines.runBlocking {
        // Collect questions and wait for loading
        val collectQuestions = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
            viewModel.questions.collect {}
        }
        var attempts = 0
        while (viewModel.questions.value.isEmpty() && attempts < 100) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(50)
            attempts++
        }
        collectQuestions.cancel()

        val collectProgress = launch(kotlinx.coroutines.Dispatchers.Unconfined) {
            viewModel.allProgress.collect {}
        }

        // Complete level and verify that the progression database is populated
        viewModel.startLevel("FLAGS", 1)
        var waitStart = 0
        while (viewModel.quizQuestions.value.size != 5 && waitStart < 200) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(10)
            waitStart++
        }

        // Fast-track completing the level by answering all correctly
        val totalQs = viewModel.quizQuestions.value.size
        for (i in 0 until totalQs) {
            val correctIdx = viewModel.quizQuestions.value[i].correctOptionIndex
            viewModel.selectOption(correctIdx)
            viewModel.submitAnswer()
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            viewModel.dismissDidYouKnow()
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
        }

        // Wait for database write
        var waitProgress = 0
        while (viewModel.allProgress.value.none { it.categoryId == "FLAGS" && it.levelIndex == 1 } && waitProgress < 200) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(10)
            waitProgress++
        }

        val progressList = viewModel.allProgress.value
        assertNotNull(progressList)
        
        // Check that Level 1 of FLAGS is completed
        val completedFlagsLevel1 = progressList.find { it.categoryId == "FLAGS" && it.levelIndex == 1 }
        assertNotNull(completedFlagsLevel1)
        assertEquals(3, completedFlagsLevel1?.starsEarned)

        collectProgress.cancel()
    }
}
