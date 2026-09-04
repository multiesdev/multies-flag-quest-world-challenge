package com.multies.flagquest.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Country(
    val id: String, // Stable ISO-3166-1 alpha-2 code, e.g. "US", "SA"
    val nameEn: String,
    val nameAr: String,
    val nameDe: String,
    val nameFr: String,
    val flagEmoji: String,
    val capitalEn: String,
    val capitalAr: String,
    val capitalDe: String,
    val capitalFr: String,
    val continentEn: String,
    val continentAr: String,
    val continentDe: String,
    val continentFr: String,
    val subregionEn: String = "Global",
    val subregionAr: String = "عالمي",
    val subregionDe: String = "Global",
    val subregionFr: String = "Global",
    val population: Long,
    val areaSqKm: Double,
    val currencyEn: String,
    val currencyAr: String,
    val currencyDe: String,
    val currencyFr: String,
    val funFactEn: String,
    val funFactAr: String,
    val funFactDe: String,
    val funFactFr: String,
    val languagesEn: String = "English",
    val languagesAr: String = "الإنجليزية",
    val languagesDe: String = "Englisch",
    val languagesFr: String = "Anglais",
    val organizationsEn: String = "UN",
    val organizationsAr: String = "الأمم المتحدة",
    val organizationsDe: String = "UN",
    val organizationsFr: String = "ONU",
    val neighborsEn: String = "None",
    val neighborsAr: String = "بلا حدود برية",
    val neighborsDe: String = "Keine",
    val neighborsFr: String = "Aucun",
    val dataSource: String = "World Bank Data",
    val lastVerified: String = "2026-07-19",
    val yearOfData: Int = 2026
) {
    // Helper to get localized fields reactively
    fun getLocalizedName(lang: String): String = when (lang) {
        "ar" -> nameAr
        "de" -> nameDe
        "fr" -> nameFr
        else -> nameEn
    }

    fun getLocalizedCapital(lang: String): String = when (lang) {
        "ar" -> capitalAr
        "de" -> capitalDe
        "fr" -> capitalFr
        else -> capitalEn
    }

    fun getLocalizedContinent(lang: String): String = when (lang) {
        "ar" -> continentAr
        "de" -> continentDe
        "fr" -> continentFr
        else -> continentEn
    }

    fun getLocalizedSubregion(lang: String): String = when (lang) {
        "ar" -> subregionAr
        "de" -> subregionDe
        "fr" -> subregionFr
        else -> subregionEn
    }

    fun getLocalizedCurrency(lang: String): String = when (lang) {
        "ar" -> currencyAr
        "de" -> currencyDe
        "fr" -> currencyFr
        else -> currencyEn
    }

    fun getLocalizedFunFact(lang: String): String = when (lang) {
        "ar" -> funFactAr
        "de" -> funFactDe
        "fr" -> funFactFr
        else -> funFactEn
    }

    fun getLocalizedLanguages(lang: String): String = when (lang) {
        "ar" -> languagesAr
        "de" -> languagesDe
        "fr" -> languagesFr
        else -> languagesEn
    }

    fun getLocalizedOrganizations(lang: String): String = when (lang) {
        "ar" -> organizationsAr
        "de" -> organizationsDe
        "fr" -> organizationsFr
        else -> organizationsEn
    }

    fun getLocalizedNeighbors(lang: String): String = when (lang) {
        "ar" -> neighborsAr
        "de" -> neighborsDe
        "fr" -> neighborsFr
        else -> neighborsEn
    }
}
