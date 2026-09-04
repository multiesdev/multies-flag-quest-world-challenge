package com.multies.flagquest.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Organization(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val nameDe: String,
    val nameFr: String,
    val fullMembers: List<String>,
    val candidates: List<String>,
    val observers: List<String>,
    val suspended: List<String>
) {
    fun getLocalizedName(lang: String): String = when (lang) {
        "ar" -> nameAr
        "de" -> nameDe
        "fr" -> nameFr
        else -> nameEn
    }
}

@JsonClass(generateAdapter = true)
data class OrganizationData(
    val verifiedAsOf: String,
    val organizations: List<Organization>
)
