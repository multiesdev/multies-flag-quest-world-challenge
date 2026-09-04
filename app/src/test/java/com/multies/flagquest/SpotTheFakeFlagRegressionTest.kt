package com.multies.flagquest

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.FakeFlagMutation
import com.multies.flagquest.data.model.FakeMutationType
import com.multies.flagquest.data.model.FlagAssetResolver
import com.multies.flagquest.data.model.GameFormat
import com.multies.flagquest.data.model.SpotTheFakeEngine
import com.multies.flagquest.data.model.SpotTheFakeLevelRepository
import com.multies.flagquest.data.model.SpotTheFakeOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SpotTheFakeFlagRegressionTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testGermanyFlagAssetResolution() {
        val resolvedAuth = FlagAssetResolver.resolve(context, "DE", isFake = false)
        assertTrue("Germany authentic flag must exist", resolvedAuth.exists)
        assertTrue("Germany authentic flag path should be SVG or PNG", resolvedAuth.assetPath.startsWith("file:///android_asset/flags/authentic/flag_de."))
        assertFalse("Path must not have duplicate android_asset prefix", resolvedAuth.assetPath.contains("android_asset/android_asset"))

        val resolvedFake = FlagAssetResolver.resolve(context, "DE", isFake = true)
        assertTrue("Germany fake flag must exist", resolvedFake.exists)
        assertTrue("Germany fake flag path should be SVG or PNG", resolvedFake.assetPath.startsWith("file:///android_asset/flags/fake/flag_de_fake."))
    }

    @Test
    fun testFranceFlagAssetResolution() {
        val resolvedAuth = FlagAssetResolver.resolve(context, "FR", isFake = false)
        assertTrue("France authentic flag must exist", resolvedAuth.exists)
        assertTrue("France authentic flag path should be SVG or PNG", resolvedAuth.assetPath.startsWith("file:///android_asset/flags/authentic/flag_fr."))

        val resolvedFake = FlagAssetResolver.resolve(context, "FR", isFake = true)
        assertTrue("France fake flag must exist", resolvedFake.exists)
        assertTrue("France fake flag path should be SVG or PNG", resolvedFake.assetPath.startsWith("file:///android_asset/flags/fake/flag_fr_fake."))
    }

    @Test
    fun testJapanFlagAssetResolution() {
        val resolvedAuth = FlagAssetResolver.resolve(context, "JP", isFake = false)
        assertTrue("Japan authentic flag must exist", resolvedAuth.exists)
        assertTrue("Japan authentic flag path should be SVG or PNG", resolvedAuth.assetPath.startsWith("file:///android_asset/flags/authentic/flag_jp."))

        val resolvedFake = FlagAssetResolver.resolve(context, "JP", isFake = true)
        assertTrue("Japan fake flag must exist", resolvedFake.exists)
        assertTrue("Japan fake flag path should be SVG or PNG", resolvedFake.assetPath.startsWith("file:///android_asset/flags/fake/flag_jp_fake."))
    }

    @Test
    fun testEgyptFlagAssetResolution() {
        val resolvedAuth = FlagAssetResolver.resolve(context, "EG", isFake = false)
        assertTrue("Egypt authentic flag must exist", resolvedAuth.exists)
        assertTrue("Egypt authentic flag path should be SVG or PNG", resolvedAuth.assetPath.startsWith("file:///android_asset/flags/authentic/flag_eg."))

        val resolvedFake = FlagAssetResolver.resolve(context, "EG", isFake = true)
        assertTrue("Egypt fake flag must exist", resolvedFake.exists)
        assertTrue("Egypt fake flag path should be SVG or PNG", resolvedFake.assetPath.startsWith("file:///android_asset/flags/fake/flag_eg_fake."))
    }

    @Test
    fun testAll36CountriesAssetsExistInBundle() {
        val allCountries = listOf(
            "US", "SA", "FR", "DE", "JP", "BR", "EG", "AU", "CA", "ZA",
            "GB", "IN", "CN", "RU", "TR", "NZ", "MX", "AR", "IT", "ES",
            "KR", "KE", "NG", "ID", "CH", "SE", "NO", "FI", "DZ", "MA",
            "IS", "FJ", "CO", "VN", "CL", "PH"
        )

        for (code in allCountries) {
            val auth = FlagAssetResolver.resolve(context, code, isFake = false)
            assertTrue("Country $code authentic flag asset must exist in bundle", auth.exists)
            assertTrue("Path for $code authentic must start with file:///android_asset/flags/authentic/", auth.assetPath.startsWith("file:///android_asset/flags/authentic/"))

            val fake = FlagAssetResolver.resolve(context, code, isFake = true)
            assertTrue("Country $code fake flag asset must exist in bundle", fake.exists)
            assertTrue("Path for $code fake must start with file:///android_asset/flags/fake/", fake.assetPath.startsWith("file:///android_asset/flags/fake/"))
        }
    }

    @Test
    fun testOptionShufflingAndCopyPreservesFlagAsset() {
        val country = Country(
            id = "DE", nameEn = "Germany", nameAr = "ألمانيا", nameDe = "Deutschland", nameFr = "Allemagne",
            flagEmoji = "🇩🇪", capitalEn = "Berlin", capitalAr = "برلين", capitalDe = "Berlin", capitalFr = "Berlin",
            continentEn = "Europe", continentAr = "أوروبا", continentDe = "Europa", continentFr = "Europe",
            population = 83000000L, areaSqKm = 357022.0, currencyEn = "EUR", currencyAr = "يورو", currencyDe = "EUR", currencyFr = "EUR",
            funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait"
        )

        val option = SpotTheFakeOption(
            id = "DE_fake",
            country = country,
            isFake = true,
            mutation = FakeFlagMutation(type = FakeMutationType.INCORRECT_COLOR)
        )

        val copiedOption = option.copy(id = "DE_fake_copied")
        assertEquals(country, copiedOption.country)
        assertTrue(copiedOption.isFake)
        assertNotNull(copiedOption.mutation)

        val resolved = FlagAssetResolver.resolve(context, copiedOption.country.id, copiedOption.isFake)
        assertTrue(resolved.assetPath.startsWith("file:///android_asset/flags/fake/flag_de_fake."))
    }

    @Test
    fun testExactlyOneFakeOptionPerRoundInFormatAandB() {
        val sampleCountries = listOf(
            Country(id = "DE", nameEn = "Germany", nameAr = "ألمانيا", nameDe = "Deutschland", nameFr = "Allemagne", flagEmoji = "🇩🇪", capitalEn = "Berlin", capitalAr = "برلين", capitalDe = "Berlin", capitalFr = "Berlin", continentEn = "Europe", continentAr = "أوروبا", continentDe = "Europa", continentFr = "Europe", population = 83000000L, areaSqKm = 357022.0, currencyEn = "EUR", currencyAr = "يورو", currencyDe = "EUR", currencyFr = "EUR", funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait"),
            Country(id = "FR", nameEn = "France", nameAr = "فرنسا", nameDe = "Frankreich", nameFr = "France", flagEmoji = "🇫🇷", capitalEn = "Paris", capitalAr = "باريس", capitalDe = "Paris", capitalFr = "Paris", continentEn = "Europe", continentAr = "أوروبا", continentDe = "Europa", continentFr = "Europe", population = 67000000L, areaSqKm = 551695.0, currencyEn = "EUR", currencyAr = "يورو", currencyDe = "EUR", currencyFr = "EUR", funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait"),
            Country(id = "JP", nameEn = "Japan", nameAr = "اليابان", nameDe = "Japan", nameFr = "Japon", flagEmoji = "🇯🇵", capitalEn = "Tokyo", capitalAr = "طوكيو", capitalDe = "Tokio", capitalFr = "Tokyo", continentEn = "Asia", continentAr = "آسيا", continentDe = "Asien", continentFr = "Asie", population = 125000000L, areaSqKm = 377975.0, currencyEn = "JPY", currencyAr = "ين", currencyDe = "JPY", currencyFr = "JPY", funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait"),
            Country(id = "EG", nameEn = "Egypt", nameAr = "مصر", nameDe = "Ägypten", nameFr = "Égypte", flagEmoji = "🇪🇬", capitalEn = "Cairo", capitalAr = "القاهرة", capitalDe = "Kairo", capitalFr = "Le Caire", continentEn = "Africa", continentAr = "أفريقيا", continentDe = "Afrika", continentFr = "Afrique", population = 100000000L, areaSqKm = 1002450.0, currencyEn = "EGP", currencyAr = "جنيه", currencyDe = "EGP", currencyFr = "EGP", funFactEn = "Fact", funFactAr = "حقيقة", funFactDe = "Fakt", funFactFr = "Fait")
        )

        val level1 = SpotTheFakeLevelRepository.getLevel(1)
        val questions = SpotTheFakeEngine.generateLevelQuestions(level1, sampleCountries, Random(42))

        for (q in questions) {
            val fakeCount = q.options.count { it.isFake }
            assertEquals("Format A / B questions must have exactly 1 fake option", 1, fakeCount)
            val fakeOpt = q.options[q.fakeOptionIndex]
            assertTrue("fakeOptionIndex must point to the fake option", fakeOpt.isFake)
        }
    }
}
