package com.multies.flagquest

import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.CountryRankingEngine
import com.multies.flagquest.data.model.RankingCriterion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class CountryRankingEngineTest {

    private lateinit var sampleCountries: List<Country>

    @Before
    fun setUp() {
        sampleCountries = CountryRankingEngine.STATS.keys.map { id ->
            Country(
                id = id,
                nameEn = id,
                nameAr = "اسم $id",
                nameDe = "Name $id",
                nameFr = "Nom $id",
                flagEmoji = "🏴",
                capitalEn = "Capital $id",
                capitalAr = "عاصمة $id",
                capitalDe = "Hauptstadt $id",
                capitalFr = "Capitale $id",
                continentEn = "Continent $id",
                continentAr = "قارة $id",
                continentDe = "Kontinent $id",
                continentFr = "Continent $id",
                population = CountryRankingEngine.STATS[id]!!.population,
                areaSqKm = CountryRankingEngine.STATS[id]!!.areaSqKm,
                currencyEn = "Currency $id",
                currencyAr = "عملة $id",
                currencyDe = "Währung $id",
                currencyFr = "Devise $id",
                funFactEn = "Fact $id",
                funFactAr = "حقيقة $id",
                funFactDe = "Fakt $id",
                funFactFr = "Fait $id"
            )
        }
    }

    @Test
    fun testCountryCatalogCompletenessAndSanity() {
        assertEquals(36, CountryRankingEngine.STATS.size)

        val us = CountryRankingEngine.STATS["US"]!!
        val ca = CountryRankingEngine.STATS["CA"]!!
        val ru = CountryRankingEngine.STATS["RU"]!!
        val ke = CountryRankingEngine.STATS["KE"]!!
        val eg = CountryRankingEngine.STATS["EG"]!!
        val no = CountryRankingEngine.STATS["NO"]!!
        val de = CountryRankingEngine.STATS["DE"]!!
        val ch = CountryRankingEngine.STATS["CH"]!!

        // Total area sanity
        assertTrue("Russia area > Canada area", ru.areaSqKm > ca.areaSqKm)
        assertTrue("Canada area > US area", ca.areaSqKm > us.areaSqKm)

        // Landlocked sanity
        assertTrue("Switzerland is landlocked", ch.isLandlocked)
        assertFalse("United States is not landlocked", us.isLandlocked)

        // Equator distance sanity
        assertTrue("Nairobi closer to equator than Cairo", abs(ke.capitalLat) < abs(eg.capitalLat))
        assertTrue("Cairo closer to equator than Oslo", abs(eg.capitalLat) < abs(no.capitalLat))

        // Capital latitude
        assertTrue("Oslo north of Berlin", no.capitalLat > de.capitalLat)
    }

    @Test
    fun testLevelQuestionGenerationAcrossAll30Levels() {
        for (levelIndex in 1..30) {
            val questions = CountryRankingEngine.generateLevelQuestions(levelIndex, sampleCountries, "en")
            val expectedRounds = when {
                levelIndex <= 5 -> 5
                levelIndex <= 10 -> 6
                levelIndex <= 20 -> 8
                else -> 10
            }
            val expectedCountriesPerRound = when {
                levelIndex <= 5 -> 3
                levelIndex <= 10 -> 4
                levelIndex <= 20 -> 5
                else -> 6
            }

            assertEquals("Level $levelIndex round count", expectedRounds, questions.size)

            for (q in questions) {
                assertEquals("Countries count per round", expectedCountriesPerRound, q.displayedCountries.size)

                // Uniqueness check
                val ids = q.displayedCountries.map { it.id }
                assertEquals("No duplicate countries in round", ids.distinct().size, ids.size)

                // Coastline landlocked check
                if (q.criterion == RankingCriterion.COASTLINE_LENGTH) {
                    for (c in q.displayedCountries) {
                        assertFalse("No landlocked country in coastline ranking", CountryRankingEngine.STATS[c.id]!!.isLandlocked)
                    }
                }

                // Check no value ties
                val rawValues = q.statsMap.values.toList()
                assertEquals("No exact value ties", rawValues.distinct().size, rawValues.size)

                val formattedValues = q.formattedValuesMap.values.toList()
                assertEquals("No display formatting ties", formattedValues.distinct().size, formattedValues.size)

                // Self evaluation test
                val correctIds = q.correctCountryOrder.map { it.id }
                val eval = CountryRankingEngine.evaluateRound(correctIds, correctIds)
                assertTrue("Correct order gives full order bonus", eval.isFullOrder)
                assertEquals("100% pair accuracy", 1.0, eval.pairAccuracy, 0.001)
            }
        }
    }

    @Test
    fun testFormattingLocalizationParity() {
        val usArea = CountryRankingEngine.STATS["US"]!!.areaSqKm
        val fmtEn = CountryRankingEngine.formatValue(usArea, RankingCriterion.TOTAL_AREA, "en")
        val fmtAr = CountryRankingEngine.formatValue(usArea, RankingCriterion.TOTAL_AREA, "ar")
        val fmtDe = CountryRankingEngine.formatValue(usArea, RankingCriterion.TOTAL_AREA, "de")
        val fmtFr = CountryRankingEngine.formatValue(usArea, RankingCriterion.TOTAL_AREA, "fr")

        assertTrue("Contains km² in EN", fmtEn.contains("km²"))
        assertTrue("Contains كم² in AR", fmtAr.contains("كم²"))
        assertTrue("Contains km² in DE", fmtDe.contains("km²"))
        assertTrue("Contains km² in FR", fmtFr.contains("km²"))
    }
}
