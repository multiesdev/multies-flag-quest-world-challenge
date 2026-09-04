package com.multies.flagquest

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.features.whoami.engine.WhoAmIEngine
import com.multies.flagquest.features.whoami.engine.WhoAmILevelConfig
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
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WhoAmIEngineTest {

    private val sampleCountries = mutableListOf<Country>()

    @Before
    fun setUp() {
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
            organizationsFr = obj.optString("organizationsFr", "ONU"),
            neighborsEn = obj.optString("neighborsEn", "None"),
            neighborsAr = obj.optString("neighborsAr", "بلا حدود برية"),
            neighborsDe = obj.optString("neighborsDe", "Keine"),
            neighborsFr = obj.optString("neighborsFr", "Aucun"),
            dataSource = obj.optString("dataSource", "World Bank Data"),
            lastVerified = obj.optString("lastVerified", "2026-07-19"),
            yearOfData = obj.optInt("yearOfData", 2026)
        )
    }

    @Test
    fun testLevelConfigsFor50Levels() {
        assertEquals(50, WhoAmILevelConfig.TOTAL_LEVELS)
        for (lvl in 1..50) {
            val config = WhoAmILevelConfig.getConfigForLevel(lvl)
            assertEquals(lvl, config.levelIndex)
            assertTrue("Question count should be between 5 and 10", config.questionCount in 5..10)
        }
    }

    @Test
    fun testQuestionGenerationAllLanguages() {
        assertTrue("Countries asset should be loaded", sampleCountries.isNotEmpty())

        val languages = listOf("en", "ar", "de", "fr")

        for (lang in languages) {
            for (level in listOf(1, 5, 10, 15, 20, 25, 30, 40, 50)) {
                val questions = WhoAmIEngine.createQuestionsForLevel(level, sampleCountries, lang)
                val expectedConfig = WhoAmILevelConfig.getConfigForLevel(level)

                assertEquals("Level $level question count mismatch for lang $lang", expectedConfig.questionCount, questions.size)

                for (q in questions) {
                    assertNotNull("Target country should exist", q.targetCountry)
                    assertEquals("Options list should contain 4 countries", 4, q.options.size)
                    assertTrue("Correct index should be within bounds 0..3", q.correctIndex in 0..3)
                    assertEquals("Target country should match option at correctIndex", q.targetCountry.id, q.options[q.correctIndex].id)

                    assertTrue("Clues should have at least 3 items", q.clues.size >= 3)

                    for (clue in q.clues) {
                        val localizedClue = clue.getLocalizedText(lang)
                        assertTrue("Clue text should not be empty for $lang", localizedClue.trim().isNotEmpty())
                        assertFalse("Clue text should not be unknown key for $lang", localizedClue.contains("clue_key_not_found"))
                    }

                    val fact = q.getLocalizedEducationalFact(lang)
                    assertTrue("Educational fact should not be empty for $lang", fact.trim().isNotEmpty())
                }
            }
        }
    }

    @Test
    fun testScoringAlgorithm() {
        val result1Clue = WhoAmIEngine.calculateScore(
            levelIndex = 1,
            totalQuestions = 5,
            correctCount = 5,
            cluesRevealedPerQuestion = listOf(1, 1, 1, 1, 1),
            elapsedTimeSec = 25L,
            bestEarlyStreak = 5,
            isNewRecord = true
        )

        assertEquals(3, result1Clue.starsEarned)
        assertEquals(100, result1Clue.accuracyPercentage)
        assertEquals(1.0, result1Clue.averageCluesUsed, 0.01)
        assertTrue("Score for 1 clue per question should be high", result1Clue.finalScore > 2000)

        val result4Clues = WhoAmIEngine.calculateScore(
            levelIndex = 1,
            totalQuestions = 5,
            correctCount = 5,
            cluesRevealedPerQuestion = listOf(4, 4, 4, 4, 4),
            elapsedTimeSec = 60L,
            bestEarlyStreak = 1,
            isNewRecord = false
        )

        assertTrue("1 clue score should be higher than 4 clues score", result1Clue.finalScore > result4Clues.finalScore)
    }
}
