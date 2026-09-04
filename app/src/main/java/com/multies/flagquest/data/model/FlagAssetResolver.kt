package com.multies.flagquest.data.model

import android.content.Context
import android.util.Log

data class ResolvedFlagAsset(
    val countryId: String,
    val isFake: Boolean,
    val assetPath: String,
    val format: String, // "SVG" or "PNG"
    val exists: Boolean
)

object FlagAssetResolver {
    private const val TAG = "FlagAssetResolver"
    @Volatile
    private var authenticAssets: Set<String>? = null
    @Volatile
    private var fakeAssets: Set<String>? = null

    @Synchronized
    fun initialize(context: Context) {
        if (authenticAssets == null) {
            authenticAssets = try {
                context.applicationContext.assets.list("flags/authentic")?.toSet() ?: emptySet()
            } catch (e: Exception) {
                Log.e(TAG, "Error listing flags/authentic assets", e)
                emptySet()
            }
        }
        if (fakeAssets == null) {
            fakeAssets = try {
                context.applicationContext.assets.list("flags/fake")?.toSet() ?: emptySet()
            } catch (e: Exception) {
                Log.e(TAG, "Error listing flags/fake assets", e)
                emptySet()
            }
        }
    }

    fun resolve(context: Context, countryId: String, isFake: Boolean): ResolvedFlagAsset {
        initialize(context)
        val cCode = countryId.lowercase().trim()
        val folder = if (isFake) "flags/fake" else "flags/authentic"
        val prefix = if (isFake) "flag_${cCode}_fake" else "flag_${cCode}"
        val assetSet = if (isFake) fakeAssets else authenticAssets

        val svgFilename = "$prefix.svg"
        val pngFilename = "$prefix.png"

        // Prefer SVG first because SVG files in flags/ are valid XML/SVG assets
        val (filename, format, exists) = when {
            assetSet?.contains(svgFilename) == true -> Triple(svgFilename, "SVG", true)
            assetSet?.contains(pngFilename) == true -> Triple(pngFilename, "PNG", true)
            else -> {
                Log.w(TAG, "Flag asset not found in assets for countryId: $countryId (cCode: $cCode), isFake: $isFake")
                Triple(svgFilename, "SVG", false)
            }
        }

        val fullPath = "file:///android_asset/$folder/$filename"
        return ResolvedFlagAsset(
            countryId = countryId,
            isFake = isFake,
            assetPath = fullPath,
            format = format,
            exists = exists
        )
    }

    fun getFlagAssetPath(context: Context, countryId: String, isFake: Boolean): String {
        return resolve(context, countryId, isFake).assetPath
    }
}
