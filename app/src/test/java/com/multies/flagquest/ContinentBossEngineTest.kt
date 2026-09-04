package com.multies.flagquest

import com.multies.flagquest.data.model.BossPhaseType
import com.multies.flagquest.data.model.ContinentBossEngine
import com.multies.flagquest.data.model.Country
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinentBossEngineTest {

    private val sampleCountries = listOf(
        Country(
            id = "FR",
            nameEn = "France",
            nameAr = "فرنسا",
            nameDe = "Frankreich",
            nameFr = "France",
            flagEmoji = "🇫🇷",
            capitalEn = "Paris",
            capitalAr = "باريس",
            capitalDe = "Paris",
            capitalFr = "Paris",
            continentEn = "Europe",
            continentAr = "أوروبا",
            continentDe = "Europa",
            continentFr = "l'Europe",
            population = 67000000L,
            areaSqKm = 551695.0,
            currencyEn = "Euro",
            currencyAr = "يورو",
            currencyDe = "Euro",
            currencyFr = "Euro",
            funFactEn = "Most visited country in the world.",
            funFactAr = "أكثر بلد زيارة في العالم.",
            funFactDe = "Meistbesuchtes Land der Welt.",
            funFactFr = "Pays le plus visité au monde."
        ),
        Country(
            id = "DE",
            nameEn = "Germany",
            nameAr = "ألمانيا",
            nameDe = "Deutschland",
            nameFr = "Allemagne",
            flagEmoji = "🇩🇪",
            capitalEn = "Berlin",
            capitalAr = "برلين",
            capitalDe = "Berlin",
            capitalFr = "Berlin",
            continentEn = "Europe",
            continentAr = "أوروبا",
            continentDe = "Europa",
            continentFr = "l'Europe",
            population = 83000000L,
            areaSqKm = 357022.0,
            currencyEn = "Euro",
            currencyAr = "يورو",
            currencyDe = "Euro",
            currencyFr = "Euro",
            funFactEn = "Known for Autobahn and castles.",
            funFactAr = "تشتهر بالطرق السريعة والقلاع.",
            funFactDe = "Bekannt für Autobahnen und Schlösser.",
            funFactFr = "Connu pour ses autoroutes et ses châteaux."
        ),
        Country(
            id = "IT",
            nameEn = "Italy",
            nameAr = "إيطاليا",
            nameDe = "Italien",
            nameFr = "Italie",
            flagEmoji = "🇮🇹",
            capitalEn = "Rome",
            capitalAr = "روما",
            capitalDe = "Rom",
            capitalFr = "Rome",
            continentEn = "Europe",
            continentAr = "أوروبا",
            continentDe = "Europa",
            continentFr = "l'Europe",
            population = 59000000L,
            areaSqKm = 301340.0,
            currencyEn = "Euro",
            currencyAr = "يورو",
            currencyDe = "Euro",
            currencyFr = "Euro",
            funFactEn = "Home to the Colosseum.",
            funFactAr = "موطن الكولوسيوم.",
            funFactDe = "Heimat des Kolosseums.",
            funFactFr = "Patrie du Colisée."
        ),
        Country(
            id = "ES",
            nameEn = "Spain",
            nameAr = "إسبانيا",
            nameDe = "Spanien",
            nameFr = "Espagne",
            flagEmoji = "🇪🇸",
            capitalEn = "Madrid",
            capitalAr = "مدريد",
            capitalDe = "Madrid",
            capitalFr = "Madrid",
            continentEn = "Europe",
            continentAr = "أوروبا",
            continentDe = "Europa",
            continentFr = "l'Europe",
            population = 47000000L,
            areaSqKm = 505990.0,
            currencyEn = "Euro",
            currencyAr = "يورو",
            currencyDe = "Euro",
            currencyFr = "Euro",
            funFactEn = "Famous for Sagrada Familia.",
            funFactAr = "مشهورة بلكنيسة ساغرادا فاميليا.",
            funFactDe = "Berühmt für Sagrada Família.",
            funFactFr = "Célèbre pour la Sagrada Família."
        )
    )

    @Test
    fun testSupportedBossesCount() {
        val bosses = ContinentBossEngine.supportedBosses
        assertEquals(6, bosses.size)
        val europeBoss = ContinentBossEngine.getBossById("boss_europe")
        assertNotNull(europeBoss)
        assertEquals("Europe", europeBoss?.continentEn)
    }

    @Test
    fun testPhaseDefinitions() {
        val phases = ContinentBossEngine.getPhasesForBoss()
        assertEquals(6, phases.size)

        val firstPhase = phases.first()
        assertEquals(BossPhaseType.FLAG_MASTERY, firstPhase.phaseType)
        assertEquals(0, firstPhase.timerSecondsPerQuestion)

        val lastPhase = phases.last()
        assertEquals(BossPhaseType.SPEED_FINALE, lastPhase.phaseType)
        assertEquals(15, lastPhase.timerSecondsPerQuestion)
    }

    @Test
    fun testGenerateQuestionsForEuropeBoss() {
        val boss = ContinentBossEngine.getBossById("boss_europe")!!
        val questionsMap = ContinentBossEngine.generateBossQuestions(boss, sampleCountries, seed = 12345L)

        assertEquals(6, questionsMap.size)

        // Phase 1: Flag Mastery
        val flagQuestions = questionsMap[BossPhaseType.FLAG_MASTERY]
        assertNotNull(flagQuestions)
        assertEquals(4, flagQuestions!!.size)

        flagQuestions.forEach { q ->
            assertEquals(4, q.optionsEn.size)
            assertTrue(q.correctOptionIndex in 0..3)
            assertTrue(q.promptEn.contains(boss.continentEn))
        }

        // Phase 6: Speed Finale
        val speedQuestions = questionsMap[BossPhaseType.SPEED_FINALE]
        assertNotNull(speedQuestions)
        assertEquals(5, speedQuestions!!.size)
    }
}
