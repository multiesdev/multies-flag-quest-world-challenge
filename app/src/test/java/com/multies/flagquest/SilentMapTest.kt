package com.multies.flagquest

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.SilentMapEngine
import com.multies.flagquest.data.repository.CountryMapRepository
import com.multies.flagquest.ui.localization.Locales
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SilentMapTest {

    private lateinit var context: Context

    private val representativeCountries = listOf(
        "CN", // China
        "IN", // India
        "VN", // Vietnam
        "IT", // Italy
        "CL", // Chile
        "JP", // Japan
        "ID", // Indonesia
        "PH", // Philippines
        "NZ", // New Zealand
        "US"  // United States
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        CountryMapRepository.clearCache()
    }

    @Test
    fun testAllRepresentativeCountriesHaveValidMapAssets() {
        representativeCountries.forEach { isoCode ->
            assertTrue(
                "Representative country $isoCode must have a valid map asset",
                CountryMapRepository.hasMapAsset(isoCode, context)
            )
        }
    }

    @Test
    fun testMapAssetsContainUsableGeographicGeometry() {
        representativeCountries.forEach { isoCode ->
            val path = CountryMapRepository.loadCountryPath(isoCode, context)
            assertNotNull("Path for $isoCode should not be null", path)
            val bounds = path!!.getBounds()
            assertFalse("Bounds for $isoCode must not be empty", bounds.isEmpty)
            assertTrue("Width for $isoCode bounds must be > 0", bounds.width > 0f)
            assertTrue("Height for $isoCode bounds must be > 0", bounds.height > 0f)
        }
    }

    @Test
    fun testMultiPolygonAndIslandCountriesHaveValidGeometries() {
        val islandNations = listOf("ID", "JP", "PH", "NZ", "US")
        islandNations.forEach { isoCode ->
            val path = CountryMapRepository.loadCountryPath(isoCode, context)
            assertNotNull("Island nation $isoCode path must not be null", path)
            val bounds = path!!.getBounds()
            assertTrue("Island nation $isoCode must have positive area", bounds.width * bounds.height > 0f)
        }
    }

    @Test
    fun testQuestionsOnlyCreatedForCountriesWithVerifiedAssets() {
        val sampleCountries = representativeCountries.map { code ->
            Country(
                id = code,
                nameEn = "Country $code",
                nameAr = "دولة $code",
                nameDe = "Land $code",
                nameFr = "Pays $code",
                flagEmoji = "🏳️",
                capitalEn = "Capital",
                capitalAr = "عاصمة",
                capitalDe = "Hauptstadt",
                capitalFr = "Capitale",
                continentEn = "Asia",
                continentAr = "آسيا",
                continentDe = "Asien",
                continentFr = "Asie",
                population = 1000000L,
                areaSqKm = 500000.0,
                currencyEn = "USD",
                currencyAr = "دولار",
                currencyDe = "USD",
                currencyFr = "USD",
                funFactEn = "Fact",
                funFactAr = "حقيقة",
                funFactDe = "Fakt",
                funFactFr = "Fait"
            )
        }

        val questions = SilentMapEngine.createQuestionsForLevel(1, sampleCountries, "en")
        assertTrue("Questions list should not be empty", questions.isNotEmpty())

        questions.forEach { q ->
            assertTrue(
                "Question target country ${q.targetCountry.id} must have a verified map asset",
                CountryMapRepository.hasMapAsset(q.targetCountry.id, context)
            )
            val path = CountryMapRepository.loadCountryPath(q.targetCountry.id, context)
            assertNotNull("Question target country ${q.targetCountry.id} must have a loadable Path", path)
        }
    }

    @Test
    fun testMissingAssetReturnsNullAndDoesNotProducePlaceholder() {
        val invalidCode = "XYZ_NON_EXISTENT"
        assertFalse(
            "Non-existent country code must return false for hasMapAsset",
            CountryMapRepository.hasMapAsset(invalidCode, context)
        )
        val path = CountryMapRepository.loadCountryPath(invalidCode, context)
        assertNull("Non-existent asset must return null Path (no fake placeholder)", path)
    }

    @Test
    fun testPreservesAspectRatioAndBounds() {
        // Test China (wide) and Chile (narrow)
        val chinaPath = CountryMapRepository.loadCountryPath("CN", context)
        val chilePath = CountryMapRepository.loadCountryPath("CL", context)

        assertNotNull(chinaPath)
        assertNotNull(chilePath)

        val chinaBounds = chinaPath!!.getBounds()
        val chileBounds = chilePath!!.getBounds()

        // China is geographically wider than taller
        assertTrue("China width/height aspect ratio check", chinaBounds.width / chinaBounds.height > 1.0f)
        // Chile is geographically taller than wider
        assertTrue("Chile height/width aspect ratio check", chileBounds.height / chileBounds.width > 1.5f)
    }

    @Test
    fun testFourLanguageLocalizationParityForSilentMapKeys() {
        val keys = listOf(
            "silent_map",
            "silent_map_challenge",
            "map_format_silhouette",
            "map_format_continent",
            "map_format_neighbors",
            "map_format_rotated",
            "map_format_island",
            "map_loading_error"
        )

        val langs = listOf("en", "ar", "de", "fr")

        for (lang in langs) {
            for (key in keys) {
                val translation = Locales.get(key, lang)
                assertNotNull("Translation for $key in $lang should not be null", translation)
                assertFalse("Translation for $key in $lang should not be empty", translation.isBlank())
            }
        }
    }
}
