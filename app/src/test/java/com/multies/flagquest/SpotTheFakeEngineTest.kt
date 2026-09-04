package com.multies.flagquest

import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.FakeMutationType
import com.multies.flagquest.data.model.GameFormat
import com.multies.flagquest.data.model.SpotTheFakeEngine
import com.multies.flagquest.data.model.SpotTheFakeLevelRepository
import com.multies.flagquest.ui.localization.Locales
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SpotTheFakeEngineTest {

    private val sampleCountries = listOf(
        Country(id = "DE", nameEn = "Germany", nameAr = "ألمانيا", nameDe = "Deutschland", nameFr = "Allemagne", flagEmoji = "🇩🇪", capitalEn = "Berlin", capitalAr = "برلين", capitalDe = "Berlin", capitalFr = "Berlin", continentEn = "Europe", continentAr = "أوروبا", continentDe = "Europa", continentFr = "Europe", population = 83000000L, areaSqKm = 357022.0, currencyEn = "EUR", currencyAr = "يورو", currencyDe = "EUR", currencyFr = "EUR", funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait"),
        Country(id = "FR", nameEn = "France", nameAr = "فرنسا", nameDe = "Frankreich", nameFr = "France", flagEmoji = "🇫🇷", capitalEn = "Paris", capitalAr = "باريس", capitalDe = "Paris", capitalFr = "Paris", continentEn = "Europe", continentAr = "أوروبا", continentDe = "Europa", continentFr = "Europe", population = 67000000L, areaSqKm = 551695.0, currencyEn = "EUR", currencyAr = "يورو", currencyDe = "EUR", currencyFr = "EUR", funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait"),
        Country(id = "JP", nameEn = "Japan", nameAr = "اليابان", nameDe = "Japan", nameFr = "Japon", flagEmoji = "🇯🇵", capitalEn = "Tokyo", capitalAr = "طوكيو", capitalDe = "Tokio", capitalFr = "Tokyo", continentEn = "Asia", continentAr = "آسيا", continentDe = "Asien", continentFr = "Asie", population = 125000000L, areaSqKm = 377975.0, currencyEn = "JPY", currencyAr = "ين", currencyDe = "JPY", currencyFr = "JPY", funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait"),
        Country(id = "CA", nameEn = "Canada", nameAr = "كندا", nameDe = "Kanada", nameFr = "Canada", flagEmoji = "🇨🇦", capitalEn = "Ottawa", capitalAr = "أوتاوا", capitalDe = "Ottawa", capitalFr = "Ottawa", continentEn = "Americas", continentAr = "أمريكا", continentDe = "Amerika", continentFr = "Amériques", population = 38000000L, areaSqKm = 9984670.0, currencyEn = "CAD", currencyAr = "دولار", currencyDe = "CAD", currencyFr = "CAD", funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait"),
        Country(id = "US", nameEn = "United States", nameAr = "الولايات المتحدة", nameDe = "Vereinigte Staaten", nameFr = "États-Unis", flagEmoji = "🇺🇸", capitalEn = "Washington D.C.", capitalAr = "واشنطن", capitalDe = "Washington D.C.", capitalFr = "Washington D.C.", continentEn = "Americas", continentAr = "أمريكا", continentDe = "Amerika", continentFr = "Amériques", population = 331000000L, areaSqKm = 9833517.0, currencyEn = "USD", currencyAr = "دولار", currencyDe = "USD", currencyFr = "USD", funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait")
    )

    @Test
    fun testLevelConfig30Levels() {
        val levels = SpotTheFakeLevelRepository.levels
        assertEquals(100, levels.size)

        for (i in 1..100) {
            val lvl = SpotTheFakeLevelRepository.getLevel(i)
            assertEquals(i, lvl.levelIndex)
            assertTrue(lvl.roundsCount > 0)
            assertTrue(lvl.timerSecPerRound > 0)
            assertTrue(lvl.allowedFormats.isNotEmpty())
            assertTrue(lvl.allowedMutations.isNotEmpty())
            assertTrue(lvl.descriptionEn.isNotEmpty())
            assertTrue(lvl.descriptionAr.isNotEmpty())
            assertTrue(lvl.descriptionDe.isNotEmpty())
            assertTrue(lvl.descriptionFr.isNotEmpty())
        }
    }

    @Test
    fun testRoundGenerationFormatAHasExactlyOneFake() {
        val config = SpotTheFakeLevelRepository.getLevel(1)
        val questions = SpotTheFakeEngine.generateLevelQuestions(config, sampleCountries, Random(12345))

        assertEquals(config.roundsCount, questions.size)

        for (q in questions) {
            val fakeCount = q.options.count { it.isFake }
            assertEquals("Every round must contain exactly 1 fake flag", 1, fakeCount)
        }
    }

    @Test
    fun testRoundGenerationFormatBHasExactlyOneFake() {
        val config = SpotTheFakeLevelRepository.getLevel(6) // Format B included
        val questions = SpotTheFakeEngine.generateLevelQuestions(config, sampleCountries, Random(54321))

        assertEquals(config.roundsCount, questions.size)

        for (q in questions) {
            val fakeCount = q.options.count { it.isFake }
            assertEquals("Format B rounds must contain exactly 1 fake flag", 1, fakeCount)
        }
    }

    @Test
    fun testLocalizedExplanationsAllLanguages() {
        val targetCountry = sampleCountries[0]

        for (mutationType in FakeMutationType.values()) {
            val fakeMutation = com.multies.flagquest.data.model.FakeFlagMutation(type = mutationType)
            val explanations = SpotTheFakeEngine.buildLocalizedExplanations(targetCountry, fakeMutation)

            val enExp = explanations["en"]
            val arExp = explanations["ar"]
            val deExp = explanations["de"]
            val frExp = explanations["fr"]

            assertNotNull(enExp)
            assertNotNull(arExp)
            assertNotNull(deExp)
            assertNotNull(frExp)

            assertTrue("EN explanation for $mutationType must not be blank", enExp!!.isNotBlank())
            assertTrue("AR explanation for $mutationType must not be blank", arExp!!.isNotBlank())
            assertTrue("DE explanation for $mutationType must not be blank", deExp!!.isNotBlank())
            assertTrue("FR explanation for $mutationType must not be blank", frExp!!.isNotBlank())
        }
    }

    @Test
    fun testScoreCalculationStarsAndCoins() {
        val result3Stars = SpotTheFakeEngine.calculateResult(
            levelIndex = 1,
            correctAnswers = 5,
            totalRounds = 5,
            maxStreak = 5,
            timeTakenSec = 25
        )

        assertEquals(3, result3Stars.stars)
        assertEquals(100, result3Stars.accuracyPercent)
        assertTrue(result3Stars.coinsEarned >= 150)

        val result1Star = SpotTheFakeEngine.calculateResult(
            levelIndex = 1,
            correctAnswers = 3,
            totalRounds = 5,
            maxStreak = 2,
            timeTakenSec = 50
        )

        assertEquals(1, result1Star.stars)
        assertEquals(60, result1Star.accuracyPercent)
    }

    @Test
    fun testLocalesContainSpotTheFakeKeysInAllLanguages() {
        val keys = listOf(
            "spot_the_fake",
            "spot_the_fake_desc",
            "spot_the_fake_challenge",
            "prompt_format_a",
            "prompt_format_b",
            "prompt_format_c",
            "authentic",
            "fake",
            "vexillological_explanation",
            "next_round",
            "see_results",
            "spot_fake_levels"
        )

        val languages = listOf("en", "ar", "de", "fr")

        for (lang in languages) {
            for (key in keys) {
                val value = Locales.get(key, lang, null)
                assertTrue("Locales key '$key' for lang '$lang' must not return key fallback", value != key)
                assertTrue("Locales key '$key' for lang '$lang' must not be empty", value.isNotEmpty())
            }
        }
    }
}
