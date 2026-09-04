package com.multies.flagquest

import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.QuickGeographyEngine
import com.multies.flagquest.data.model.QuickGeographyQuestionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuickGeographyEngineTest {

    private lateinit var sampleCountries: List<Country>

    @Before
    fun setUp() {
        sampleCountries = listOf(
            Country(
                id = "US",
                nameEn = "United States",
                nameAr = "الولايات المتحدة",
                nameDe = "Vereinigte Staaten",
                nameFr = "États-Unis",
                flagEmoji = "🇺🇸",
                capitalEn = "Washington, D.C.",
                capitalAr = "واشنطن العاصمة",
                capitalDe = "Washington, D.C.",
                capitalFr = "Washington, D.C.",
                continentEn = "North America",
                continentAr = "أمريكا الشمالية",
                continentDe = "Nordamerika",
                continentFr = "Amérique du Nord",
                population = 333000000L,
                areaSqKm = 9833517.0,
                currencyEn = "USD", currencyAr = "دولار أمريكي", currencyDe = "USD", currencyFr = "USD",
                funFactEn = "Third largest country by land area.",
                funFactAr = "ثالث أكبر دولة مساحة.",
                funFactDe = "Drittgrößtes Land der Erde.",
                funFactFr = "Troisième plus grand pays."
            ),
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
                continentFr = "Europe",
                population = 68000000L,
                areaSqKm = 643801.0,
                currencyEn = "EUR", currencyAr = "يورو", currencyDe = "EUR", currencyFr = "EUR",
                funFactEn = "Most visited country in the world.",
                funFactAr = "الدولة الأكثر زيارة في العالم.",
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
                continentFr = "Europe",
                population = 84300000L,
                areaSqKm = 357022.0,
                currencyEn = "EUR", currencyAr = "يورو", currencyDe = "EUR", currencyFr = "EUR",
                funFactEn = "Famous for Autobahn and castles.",
                funFactAr = "تشتهر بالطرق السريعة والقلاع.",
                funFactDe = "Bekannt für Autobahnen und Schlösser.",
                funFactFr = "Célèbre pour ses châteaux."
            ),
            Country(
                id = "JP",
                nameEn = "Japan",
                nameAr = "اليابان",
                nameDe = "Japan",
                nameFr = "Japon",
                flagEmoji = "🇯🇵",
                capitalEn = "Tokyo",
                capitalAr = "طوكيو",
                capitalDe = "Tokio",
                capitalFr = "Tokyo",
                continentEn = "Asia",
                continentAr = "آسيا",
                continentDe = "Asien",
                continentFr = "Asie",
                population = 124500000L,
                areaSqKm = 377975.0,
                currencyEn = "JPY", currencyAr = "ين ياباني", currencyDe = "JPY", currencyFr = "JPY",
                funFactEn = "An island nation in East Asia.",
                funFactAr = "دولة جزرية في شرق آسيا.",
                funFactDe = "Inselstaat in Ostasien.",
                funFactFr = "Nation insulaire d'Asie de l'Est."
            ),
            Country(
                id = "BR",
                nameEn = "Brazil",
                nameAr = "البرازيل",
                nameDe = "Brasilien",
                nameFr = "Brésil",
                flagEmoji = "🇧🇷",
                capitalEn = "Brasília",
                capitalAr = "برازيليا",
                capitalDe = "Brasília",
                capitalFr = "Brasília",
                continentEn = "South America",
                continentAr = "أمريكا الجنوبية",
                continentDe = "Südamerika",
                continentFr = "Amérique du Sud",
                population = 215300000L,
                areaSqKm = 8515767.0,
                currencyEn = "BRL", currencyAr = "ريال برازيلي", currencyDe = "BRL", currencyFr = "BRL",
                funFactEn = "Home to the Amazon Rainforest.",
                funFactAr = "موطن غابات الأمازون المطيرة.",
                funFactDe = "Heimat des Amazonastrophenwaldes.",
                funFactFr = "Foyer de la forêt amazonienne."
            ),
            Country(
                id = "EG",
                nameEn = "Egypt",
                nameAr = "مصر",
                nameDe = "Ägypten",
                nameFr = "Égypte",
                flagEmoji = "🇪🇬",
                capitalEn = "Cairo",
                capitalAr = "القاهرة",
                capitalDe = "Kairo",
                capitalFr = "Le Caire",
                continentEn = "Africa",
                continentAr = "أفريقيا",
                continentDe = "Afrika",
                continentFr = "Afrique",
                population = 111000000L,
                areaSqKm = 1002450.0,
                currencyEn = "EGP", currencyAr = "جنيه مصري", currencyDe = "EGP", currencyFr = "EGP",
                funFactEn = "Home to ancient pyramids.",
                funFactAr = "موطن الأهرامات القديمة.",
                funFactDe = "Heimat der alten Pyramiden.",
                funFactFr = "Foyer des pyramides antiques."
            )
        )
    }

    @Test
    fun testGenerateSessionQuestions_returnsValidQuestions() {
        val questions = QuickGeographyEngine.generateSessionQuestions(
            levelIndex = 1,
            countryCatalog = sampleCountries,
            seed = 42L
        )

        assertTrue("Generated questions list should not be empty", questions.isNotEmpty())

        questions.forEach { q ->
            assertNotNull(q.id)
            assertTrue("Options size should be >= 2", q.optionsEn.size >= 2)
            assertEquals("English options should be distinct", q.optionsEn.distinct().size, q.optionsEn.size)
            assertEquals("Arabic options should be distinct", q.optionsAr.distinct().size, q.optionsAr.size)
            assertEquals("German options should be distinct", q.optionsDe.distinct().size, q.optionsDe.size)
            assertEquals("French options should be distinct", q.optionsFr.distinct().size, q.optionsFr.size)
            assertTrue("Correct index in bounds", q.correctOptionIndex in q.optionsEn.indices)
            assertTrue("Prompt EN non blank", q.promptEn.isNotBlank())
            assertTrue("Prompt AR non blank", q.promptAr.isNotBlank())
            assertTrue("Prompt DE non blank", q.promptDe.isNotBlank())
            assertTrue("Prompt FR non blank", q.promptFr.isNotBlank())
        }
    }

    @Test
    fun testScoringFormula_and_StreakMultiplier() {
        // Streak < 3: x1.0
        val score1 = QuickGeographyEngine.calculateQuestionScore(100, elapsedMs = 1500L, streak = 0)
        assertEquals(150, score1) // (100 + 50) * 1.0 = 150

        // Streak 3..4: x1.2
        val score2 = QuickGeographyEngine.calculateQuestionScore(100, elapsedMs = 2500L, streak = 3)
        assertEquals(156, score2) // (100 + 30) * 1.2 = 156

        // Streak 5..9: x1.5
        val score3 = QuickGeographyEngine.calculateQuestionScore(100, elapsedMs = 4500L, streak = 5)
        assertEquals(165, score3) // (100 + 10) * 1.5 = 165

        // Streak 10+: x2.0
        val score4 = QuickGeographyEngine.calculateQuestionScore(100, elapsedMs = 6000L, streak = 10)
        assertEquals(200, score4) // (100 + 0) * 2.0 = 200
    }

    @Test
    fun testStreakMultipliers() {
        assertEquals(1.0f, QuickGeographyEngine.getStreakMultiplier(0), 0.01f)
        assertEquals(1.0f, QuickGeographyEngine.getStreakMultiplier(2), 0.01f)
        assertEquals(1.2f, QuickGeographyEngine.getStreakMultiplier(3), 0.01f)
        assertEquals(1.2f, QuickGeographyEngine.getStreakMultiplier(4), 0.01f)
        assertEquals(1.5f, QuickGeographyEngine.getStreakMultiplier(5), 0.01f)
        assertEquals(1.5f, QuickGeographyEngine.getStreakMultiplier(9), 0.01f)
        assertEquals(2.0f, QuickGeographyEngine.getStreakMultiplier(10), 0.01f)
        assertEquals(2.0f, QuickGeographyEngine.getStreakMultiplier(25), 0.01f)
    }
}
