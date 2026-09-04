package com.multies.flagquest.data.repository

import android.content.Context
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path

/**
 * Repository and Provider for Real Geographic Country SVG Map Outlines.
 * Map data source: Natural Earth vector dataset (Public Domain / CC0 License).
 * All map assets are stored offline in app assets under maps/countries/.
 */
object CountryMapRepository {

    private const val TAG = "CountryMapRepository"
    private const val MAPS_DIR = "maps/countries"

    // Set of verified country ISO codes that have valid SVG geographic map assets
    val verifiedCountryCodes: Set<String> = setOf(
        "US", "SA", "FR", "DE", "JP", "BR", "EG", "AU", "CA", "ZA",
        "GB", "IN", "CN", "RU", "TR", "NZ", "MX", "AR", "IT", "ES",
        "KR", "KE", "NG", "ID", "CH", "SE", "NO", "FI", "DZ", "MA",
        "IS", "FJ", "CO", "VN", "CL", "PH"
    )

    // Cache of parsed raw Path and bounds by country ID
    private val pathCache = mutableMapOf<String, Path>()
    private val boundsCache = mutableMapOf<String, Rect>()

    fun hasMapAsset(countryId: String, context: Context? = null): Boolean {
        val upperId = countryId.uppercase()
        if (verifiedCountryCodes.contains(upperId)) return true
        if (context == null) return false
        return try {
            val list = context.assets.list(MAPS_DIR) ?: emptyArray()
            val fileName = "${upperId.lowercase()}.svg"
            list.any { it.equals(fileName, ignoreCase = true) }
        } catch (e: Exception) {
            false
        }
    }

    fun loadCountryPath(countryId: String, context: Context? = null): Path? {
        val key = countryId.uppercase()
        if (pathCache.containsKey(key)) {
            return pathCache[key]
        }

        val pathData = getRawSvgPathData(key, context)
        if (pathData.isNullOrBlank()) {
            Log.e(TAG, "Missing map asset for country ID: $key")
            return null
        }

        val parsedPath = parseSvgPath(pathData)
        if (parsedPath == null || parsedPath.getBounds().isEmpty) {
            Log.e(TAG, "Invalid path geometry in map asset for country ID: $key")
            return null
        }

        pathCache[key] = parsedPath
        boundsCache[key] = parsedPath.getBounds()
        return parsedPath
    }

    fun getCountryBounds(countryId: String, context: Context? = null): Rect? {
        val key = countryId.uppercase()
        if (boundsCache.containsKey(key)) {
            return boundsCache[key]
        }
        loadCountryPath(key, context)
        return boundsCache[key]
    }

    private fun getRawSvgPathData(countryId: String, context: Context?): String? {
        val upperId = countryId.uppercase()
        val fileName = "${upperId.lowercase()}.svg"
        
        if (context != null) {
            try {
                context.assets.open("$MAPS_DIR/$fileName").use { inputStream ->
                    val content = inputStream.bufferedReader().use { it.readText() }
                    return extractPathDataFromSvg(content)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not open asset $MAPS_DIR/$fileName: ${e.message}")
            }
        }
        
        // Fallback for JVM unit tests where Context assets might not be loaded:
        return try {
            val resourcePath = "/assets/$MAPS_DIR/$fileName"
            val stream = javaClass.getResourceAsStream(resourcePath)
            if (stream != null) {
                val content = stream.bufferedReader().use { it.readText() }
                extractPathDataFromSvg(content)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun extractPathDataFromSvg(svgContent: String): String? {
        val regex = "<path[^>]*d=[\"']([^\"']+)[\"']".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(svgContent)
        return match?.groupValues?.get(1)
    }

    fun parseSvgPath(d: String): Path? {
        val path = Path()
        val tokens = d.trim().split("\\s+".toRegex())
        var idx = 0
        var hasPoints = false

        try {
            while (idx < tokens.size) {
                val cmd = tokens[idx]
                when (cmd.uppercase()) {
                    "M" -> {
                        if (idx + 2 < tokens.size) {
                            val x = tokens[idx + 1].toFloat()
                            val y = tokens[idx + 2].toFloat()
                            path.moveTo(x, y)
                            hasPoints = true
                            idx += 3
                        } else break
                    }
                    "L" -> {
                        if (idx + 2 < tokens.size) {
                            val x = tokens[idx + 1].toFloat()
                            val y = tokens[idx + 2].toFloat()
                            path.lineTo(x, y)
                            hasPoints = true
                            idx += 3
                        } else break
                    }
                    "Z" -> {
                        path.close()
                        idx += 1
                    }
                    else -> idx++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SVG path string: ${e.message}")
            return null
        }

        return if (hasPoints) path else null
    }

    fun clearCache() {
        pathCache.clear()
        boundsCache.clear()
    }
}
