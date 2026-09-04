package com.multies.flagquest.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.multies.flagquest.data.model.FakeFlagMutation
import com.multies.flagquest.data.model.FakeMutationType
import com.multies.flagquest.data.model.FlagAssetResolver
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Palette of flag colors
 */
object FlagColors {
    val Red = Color(0xFFE53935)
    val DarkRed = Color(0xFFB71C1C)
    val Crimson = Color(0xFFC62828)
    val Blue = Color(0xFF1E88E5)
    val DarkBlue = Color(0xFF0D47A1)
    val SkyBlue = Color(0xFF42A5F5)
    val Green = Color(0xFF43A047)
    val DarkGreen = Color(0xFF1B5E20)
    val Yellow = Color(0xFFFFEB3B)
    val Gold = Color(0xFFFFC107)
    val Orange = Color(0xFFFB8C00)
    val Saffron = Color(0xFFFF9800)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF212121)
    val Purple = Color(0xFF8E24AA)
    val Teal = Color(0xFF00897B)
    val Maroon = Color(0xFF880E4F)
    val BorderGrey = Color(0xFFCFD8DC)

    fun parseColor(code: String): Color = when (code.uppercase()) {
        "RED" -> Red
        "DARK_RED" -> DarkRed
        "CRIMSON" -> Crimson
        "BLUE" -> Blue
        "DARK_BLUE" -> DarkBlue
        "SKY_BLUE" -> SkyBlue
        "GREEN" -> Green
        "DARK_GREEN" -> DarkGreen
        "YELLOW", "GOLD" -> Gold
        "ORANGE", "SAFFRON" -> Orange
        "WHITE" -> White
        "BLACK" -> Black
        "PURPLE" -> Purple
        "TEAL" -> Teal
        "MAROON" -> Maroon
        else -> try {
            Color(android.graphics.Color.parseColor(code))
        } catch (e: Exception) {
            Blue
        }
    }
}

/**
 * Geometric Flag Definition for dynamic rendering and mutation
 */
data class FlagSpec(
    val countryId: String,
    val style: FlagStyle,
    val colors: List<Color>,
    val emblem: EmblemType = EmblemType.NONE,
    val emblemColor: Color = FlagColors.White,
    val emblemPosition: EmblemPosition = EmblemPosition.CENTER,
    val starCount: Int = 0,
    val starColor: Color = FlagColors.Yellow,
    val cantonColor: Color? = null,
    val borderThicknessDp: Float = 0f,
    val borderColor: Color = FlagColors.BorderGrey
)

enum class FlagStyle {
    HORIZONTAL_STRIPES,
    VERTICAL_STRIPES,
    NORDIC_CROSS,
    CROSS_CENTER,
    HOIST_TRIANGLE,
    CANTON_FIELD,
    SOLID_FIELD,
    SALTIRE_DIAGONAL
}

enum class EmblemType {
    NONE,
    MAPLE_LEAF,
    SUN_CIRCLE,
    ASHOKA_CHAKRA,
    CEDAR_TREE,
    CRESCENT_STAR,
    EAGLE_BADGE,
    STAR_SINGLE,
    STARS_MULTIPLE,
    GLOBE_CIRCLE,
    NORDIC_CROSS_SYMBOL,
    CROSS_SYMBOL
}

enum class EmblemPosition {
    CENTER,
    HOIST,
    TOP_LEFT,
    CANTON
}

/**
 * Repository of Flag Specs for countries
 */
object FlagSpecRepository {
    private val specs = mapOf(
        // Germany (DE): Horizontal Black, Red, Gold
        "DE" to FlagSpec("DE", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Black, FlagColors.Red, FlagColors.Gold)),
        // France (FR): Vertical Blue, White, Red
        "FR" to FlagSpec("FR", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.DarkBlue, FlagColors.White, FlagColors.Red)),
        // Italy (IT): Vertical Green, White, Red
        "IT" to FlagSpec("IT", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.Green, FlagColors.White, FlagColors.Red)),
        // Japan (JP): White field, Red Sun Circle
        "JP" to FlagSpec("JP", FlagStyle.SOLID_FIELD, listOf(FlagColors.White), emblem = EmblemType.SUN_CIRCLE, emblemColor = FlagColors.Red),
        // Canada (CA): Red-White-Red vertical, Red Maple Leaf
        "CA" to FlagSpec("CA", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.Red, FlagColors.White, FlagColors.Red), emblem = EmblemType.MAPLE_LEAF, emblemColor = FlagColors.Red),
        // USA (US): 7 horizontal stripes (Red/White), Blue Canton with 50 stars
        "US" to FlagSpec("US", FlagStyle.CANTON_FIELD, listOf(FlagColors.Red, FlagColors.White), cantonColor = FlagColors.DarkBlue, starCount = 50, starColor = FlagColors.White),
        // Spain (ES): Horizontal Red, Gold (double width), Red with emblem
        "ES" to FlagSpec("ES", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Red, FlagColors.Gold, FlagColors.Red), emblem = EmblemType.EAGLE_BADGE, emblemColor = FlagColors.Maroon, emblemPosition = EmblemPosition.HOIST),
        // India (IN): Horizontal Saffron, White, Green, Blue Ashoka Chakra
        "IN" to FlagSpec("IN", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Saffron, FlagColors.White, FlagColors.Green), emblem = EmblemType.ASHOKA_CHAKRA, emblemColor = FlagColors.DarkBlue),
        // Brazil (BR): Green field, Gold rhombus / Blue Globe Circle
        "BR" to FlagSpec("BR", FlagStyle.SOLID_FIELD, listOf(FlagColors.Green), emblem = EmblemType.GLOBE_CIRCLE, emblemColor = FlagColors.DarkBlue, starCount = 27, starColor = FlagColors.White),
        // Ireland (IE): Vertical Green, White, Orange
        "IE" to FlagSpec("IE", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.Green, FlagColors.White, FlagColors.Orange)),
        // Cote d'Ivoire (CI): Vertical Orange, White, Green
        "CI" to FlagSpec("CI", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.Orange, FlagColors.White, FlagColors.Green)),
        // Netherlands (NL): Horizontal Red, White, Blue
        "NL" to FlagSpec("NL", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Red, FlagColors.White, FlagColors.DarkBlue)),
        // Luxembourg (LU): Horizontal Red, White, Sky Blue
        "LU" to FlagSpec("LU", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Red, FlagColors.White, FlagColors.SkyBlue)),
        // Sweden (SE): Blue field, Yellow Nordic Cross
        "SE" to FlagSpec("SE", FlagStyle.NORDIC_CROSS, listOf(FlagColors.DarkBlue, FlagColors.Gold)),
        // Norway (NO): Red field, Blue Nordic Cross with White border
        "NO" to FlagSpec("NO", FlagStyle.NORDIC_CROSS, listOf(FlagColors.Red, FlagColors.DarkBlue, FlagColors.White)),
        // Denmark (DK): Red field, White Nordic Cross
        "DK" to FlagSpec("DK", FlagStyle.NORDIC_CROSS, listOf(FlagColors.Red, FlagColors.White)),
        // Finland (FI): White field, Blue Nordic Cross
        "FI" to FlagSpec("FI", FlagStyle.NORDIC_CROSS, listOf(FlagColors.White, FlagColors.DarkBlue)),
        // Romania (RO): Vertical Dark Blue, Yellow, Red
        "RO" to FlagSpec("RO", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.DarkBlue, FlagColors.Yellow, FlagColors.Red)),
        // Chad (TD): Vertical Blue, Yellow, Red
        "TD" to FlagSpec("TD", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.Blue, FlagColors.Yellow, FlagColors.Red)),
        // Indonesia (ID): Horizontal Red, White
        "ID" to FlagSpec("ID", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Red, FlagColors.White)),
        // Monaco (MC): Horizontal Red, White (proportion difference or darker red)
        "MC" to FlagSpec("MC", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.DarkRed, FlagColors.White)),
        // Poland (PL): Horizontal White, Red
        "PL" to FlagSpec("PL", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.White, FlagColors.Red)),
        // Austria (AT): Horizontal Red, White, Red
        "AT" to FlagSpec("AT", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Red, FlagColors.White, FlagColors.Red)),
        // Switzerland (CH): Red field, White Cross
        "CH" to FlagSpec("CH", FlagStyle.CROSS_CENTER, listOf(FlagColors.Red, FlagColors.White)),
        // Turkey (TR): Red field, White Crescent and Star
        "TR" to FlagSpec("TR", FlagStyle.SOLID_FIELD, listOf(FlagColors.Red), emblem = EmblemType.CRESCENT_STAR, emblemColor = FlagColors.White),
        // Saudi Arabia (SA): Green field, White Emblem
        "SA" to FlagSpec("SA", FlagStyle.SOLID_FIELD, listOf(FlagColors.DarkGreen), emblem = EmblemType.EAGLE_BADGE, emblemColor = FlagColors.White),
        // Egypt (EG): Horizontal Red, White, Gold Eagle, Black
        "EG" to FlagSpec("EG", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Red, FlagColors.White, FlagColors.Black), emblem = EmblemType.EAGLE_BADGE, emblemColor = FlagColors.Gold),
        // Vietnam (VN): Red field, Gold Star
        "VN" to FlagSpec("VN", FlagStyle.SOLID_FIELD, listOf(FlagColors.Red), emblem = EmblemType.STAR_SINGLE, emblemColor = FlagColors.Gold, starCount = 1),
        // China (CN): Red field, Gold 5 stars in canton
        "CN" to FlagSpec("CN", FlagStyle.SOLID_FIELD, listOf(FlagColors.Red), emblem = EmblemType.STARS_MULTIPLE, emblemColor = FlagColors.Gold, emblemPosition = EmblemPosition.TOP_LEFT, starCount = 5),
        // Australia (AU): Blue canton field, Union Jack + Southern Cross stars
        "AU" to FlagSpec("AU", FlagStyle.CANTON_FIELD, listOf(FlagColors.DarkBlue), cantonColor = FlagColors.DarkBlue, emblem = EmblemType.STARS_MULTIPLE, emblemColor = FlagColors.White, starCount = 6),
        // New Zealand (NZ): Blue canton field, Union Jack + 4 Red stars
        "NZ" to FlagSpec("NZ", FlagStyle.CANTON_FIELD, listOf(FlagColors.DarkBlue), cantonColor = FlagColors.DarkBlue, emblem = EmblemType.STARS_MULTIPLE, emblemColor = FlagColors.Red, starCount = 4),
        // Argentina (AR): Horizontal Light Blue, White, Light Blue, Sun
        "AR" to FlagSpec("AR", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.SkyBlue, FlagColors.White, FlagColors.SkyBlue), emblem = EmblemType.SUN_CIRCLE, emblemColor = FlagColors.Gold),
        // Lebanon (LB): Horizontal Red, White (double), Red, Green Cedar
        "LB" to FlagSpec("LB", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Red, FlagColors.White, FlagColors.Red), emblem = EmblemType.CEDAR_TREE, emblemColor = FlagColors.Green),
        // Jamaica (JM): Saltire Gold, Green & Black triangles
        "JM" to FlagSpec("JM", FlagStyle.SALTIRE_DIAGONAL, listOf(FlagColors.Gold, FlagColors.Green, FlagColors.Black)),
        // Greece (GR): Blue & White horizontal stripes with Canton Cross
        "GR" to FlagSpec("GR", FlagStyle.CANTON_FIELD, listOf(FlagColors.Blue, FlagColors.White), cantonColor = FlagColors.Blue, emblem = EmblemType.CROSS_SYMBOL, emblemColor = FlagColors.White),
        // Russia (RU): Horizontal White, Blue, Red
        "RU" to FlagSpec("RU", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.White, FlagColors.Blue, FlagColors.Red)),
        // Mexico (MX): Vertical Green, White, Red with Eagle Badge
        "MX" to FlagSpec("MX", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.Green, FlagColors.White, FlagColors.Red), emblem = EmblemType.EAGLE_BADGE, emblemColor = FlagColors.Maroon),
        // South Korea (KR): White field with Sun Circle / Taegeuk
        "KR" to FlagSpec("KR", FlagStyle.SOLID_FIELD, listOf(FlagColors.White), emblem = EmblemType.SUN_CIRCLE, emblemColor = FlagColors.Red),
        // Kenya (KE): Horizontal Black, Red, Dark Green with White borders
        "KE" to FlagSpec("KE", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Black, FlagColors.Red, FlagColors.DarkGreen), emblem = EmblemType.EAGLE_BADGE, emblemColor = FlagColors.White),
        // Nigeria (NG): Vertical Dark Green, White, Dark Green
        "NG" to FlagSpec("NG", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.DarkGreen, FlagColors.White, FlagColors.DarkGreen)),
        // Algeria (DZ): Vertical Dark Green, White with Red Crescent
        "DZ" to FlagSpec("DZ", FlagStyle.VERTICAL_STRIPES, listOf(FlagColors.DarkGreen, FlagColors.White), emblem = EmblemType.CRESCENT_STAR, emblemColor = FlagColors.Red),
        // Morocco (MA): Red field with Green Star
        "MA" to FlagSpec("MA", FlagStyle.SOLID_FIELD, listOf(FlagColors.Red), emblem = EmblemType.STAR_SINGLE, emblemColor = FlagColors.Green, starCount = 1),
        // Iceland (IS): Blue field, Red Nordic Cross
        "IS" to FlagSpec("IS", FlagStyle.NORDIC_CROSS, listOf(FlagColors.Blue, FlagColors.Red, FlagColors.White)),
        // Fiji (FJ): Sky Blue Canton Field with Stars
        "FJ" to FlagSpec("FJ", FlagStyle.CANTON_FIELD, listOf(FlagColors.SkyBlue), cantonColor = FlagColors.DarkBlue, emblem = EmblemType.STARS_MULTIPLE, emblemColor = FlagColors.White, starCount = 4),
        // Colombia (CO): Horizontal Yellow, Blue, Red
        "CO" to FlagSpec("CO", FlagStyle.HORIZONTAL_STRIPES, listOf(FlagColors.Yellow, FlagColors.Blue, FlagColors.Red)),
        // Chile (CL): Horizontal White, Red with Blue Canton
        "CL" to FlagSpec("CL", FlagStyle.CANTON_FIELD, listOf(FlagColors.White, FlagColors.Red), cantonColor = FlagColors.Blue, emblem = EmblemType.STAR_SINGLE, emblemColor = FlagColors.White, starCount = 1),
        // Philippines (PH): Horizontal Blue, Red with Hoist Triangle
        "PH" to FlagSpec("PH", FlagStyle.HOIST_TRIANGLE, listOf(FlagColors.Blue, FlagColors.Red), emblem = EmblemType.SUN_CIRCLE, emblemColor = FlagColors.Gold),
        // South Africa (ZA): Horizontal Red, Blue with Green Hoist Triangle
        "ZA" to FlagSpec("ZA", FlagStyle.HOIST_TRIANGLE, listOf(FlagColors.Red, FlagColors.Blue), emblem = EmblemType.SUN_CIRCLE, emblemColor = FlagColors.Green),
        // United Kingdom (GB): Blue field with Cross
        "GB" to FlagSpec("GB", FlagStyle.CROSS_CENTER, listOf(FlagColors.DarkBlue, FlagColors.Red, FlagColors.White))
    )

    fun getSpec(countryId: String): FlagSpec {
        return specs[countryId.uppercase()] ?: FlagSpec(
            countryId = countryId,
            style = FlagStyle.HORIZONTAL_STRIPES,
            colors = listOf(FlagColors.Blue, FlagColors.White, FlagColors.Red)
        )
    }
}

/**
 * Jetpack Compose Flag Graphic Composable
 */
@Composable
fun FlagGraphic(
    countryId: String,
    isFake: Boolean = false,
    mutation: FakeFlagMutation? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context.applicationContext)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    val resolvedAsset = remember(countryId, isFake, mutation) {
        FlagAssetResolver.resolve(context, countryId, isFake || mutation != null)
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(resolvedAsset.assetPath)
            .crossfade(false)
            .memoryCacheKey("${countryId}_${isFake}_${mutation?.type}_${resolvedAsset.assetPath}")
            .diskCacheKey("${countryId}_${isFake}_${mutation?.type}_${resolvedAsset.assetPath}")
            .listener(
                onError = { request, result ->
                    Log.e(
                        "FlagGraphic",
                        "Failed to load flag image: countryId=$countryId, isFake=$isFake, path=${resolvedAsset.assetPath}, format=${resolvedAsset.format}, exists=${resolvedAsset.exists}",
                        result.throwable
                    )
                }
            )
            .build(),
        imageLoader = imageLoader
    )

    val painterState = painter.state
    val flagSpec = remember(countryId, mutation) {
        val baseSpec = FlagSpecRepository.getSpec(countryId)
        applyMutation(baseSpec, mutation)
    }

    Box(
        modifier = modifier
            .aspectRatio(3f / 2f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFAFAFA))
            .border(
                width = 1.dp,
                color = Color(0x33000000),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (resolvedAsset.exists && resolvedAsset.format == "SVG" && painterState !is AsyncImagePainter.State.Error) {
            Image(
                painter = painter,
                contentDescription = "Flag of $countryId",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            ) {
                drawFlagSpec(flagSpec, mutation)
            }
        }
    }
}

private fun applyMutation(base: FlagSpec, mutation: FakeFlagMutation?): FlagSpec {
    if (mutation == null) return base

    var updatedStyle = base.style
    var updatedColors = base.colors.toMutableList()
    var updatedEmblem = base.emblem
    var updatedEmblemColor = base.emblemColor
    var updatedEmblemPos = base.emblemPosition
    var updatedStarCount = base.starCount
    var updatedStarColor = base.starColor
    var updatedBorderColor = base.borderColor
    var updatedBorderThickness = base.borderThicknessDp

    when (mutation.type) {
        FakeMutationType.INCORRECT_COLOR -> {
            val targetStr = mutation.targetColor
            val replaceStr = mutation.replacementColor
            if (targetStr != null && replaceStr != null) {
                val targetColor = FlagColors.parseColor(targetStr)
                val replaceColor = FlagColors.parseColor(replaceStr)
                updatedColors = updatedColors.map {
                    if (it == targetColor || updatedColors.size == 1) replaceColor else it
                }.toMutableList()
                if (updatedEmblemColor == targetColor) {
                    updatedEmblemColor = replaceColor
                }
            } else if (updatedColors.isNotEmpty()) {
                updatedColors[0] = FlagColors.Purple
            }
        }
        FakeMutationType.SWAP_STRIPE_COLORS -> {
            val pair = mutation.swappedStripeIndices ?: Pair(0, 1)
            val idx1 = pair.first.coerceIn(0, updatedColors.size - 1)
            val idx2 = pair.second.coerceIn(0, updatedColors.size - 1)
            val tmp = updatedColors[idx1]
            updatedColors[idx1] = updatedColors[idx2]
            updatedColors[idx2] = tmp
        }
        FakeMutationType.MISSING_SYMBOL -> {
            updatedEmblem = EmblemType.NONE
            updatedStarCount = 0
        }
        FakeMutationType.EXTRA_SYMBOL -> {
            if (updatedEmblem == EmblemType.NONE) {
                updatedEmblem = EmblemType.STAR_SINGLE
                updatedEmblemColor = FlagColors.Gold
            } else {
                updatedStarCount += 2
            }
        }
        FakeMutationType.INCORRECT_STAR_COUNT -> {
            val fakeCount = mutation.starCountFake ?: 3
            updatedStarCount = fakeCount
        }
        FakeMutationType.INCORRECT_STRIPE_ORIENTATION -> {
            updatedStyle = if (base.style == FlagStyle.HORIZONTAL_STRIPES) {
                FlagStyle.VERTICAL_STRIPES
            } else {
                FlagStyle.HORIZONTAL_STRIPES
            }
        }
        FakeMutationType.INCORRECT_SYMBOL_POSITION -> {
            updatedEmblemPos = if (base.emblemPosition == EmblemPosition.CENTER) {
                EmblemPosition.TOP_LEFT
            } else {
                EmblemPosition.CENTER
            }
        }
        FakeMutationType.MIRRORED_ASYMMETRIC_SYMBOL -> {
            // Handled during drawing transformation
        }
        FakeMutationType.INCORRECT_FIELD_OR_BORDER -> {
            val fakeBorder = mutation.borderFakeColor ?: "BLACK"
            updatedBorderColor = FlagColors.parseColor(fakeBorder)
            updatedBorderThickness = 3f
        }
        FakeMutationType.SUBTLE_PROPORTION_CHANGE -> {
            // Handled in drawing logic
        }
    }

    return base.copy(
        style = updatedStyle,
        colors = updatedColors,
        emblem = updatedEmblem,
        emblemColor = updatedEmblemColor,
        emblemPosition = updatedEmblemPos,
        starCount = updatedStarCount,
        starColor = updatedStarColor,
        borderColor = updatedBorderColor,
        borderThicknessDp = updatedBorderThickness
    )
}

private fun DrawScope.drawFlagSpec(spec: FlagSpec, mutation: FakeFlagMutation?) {
    val w = size.width
    val h = size.height

    // 1. Draw Base Field / Stripes
    when (spec.style) {
        FlagStyle.SOLID_FIELD -> {
            val color = spec.colors.getOrElse(0) { FlagColors.White }
            drawRect(color = color, size = size)
        }
        FlagStyle.HORIZONTAL_STRIPES -> {
            val count = spec.colors.size
            if (count > 0) {
                val isProportionMutated = mutation?.type == FakeMutationType.SUBTLE_PROPORTION_CHANGE
                if (isProportionMutated && count == 3) {
                    // Draw non-standard heights
                    drawRect(color = spec.colors[0], topLeft = Offset(0f, 0f), size = Size(w, h * 0.2f))
                    drawRect(color = spec.colors[1], topLeft = Offset(0f, h * 0.2f), size = Size(w, h * 0.5f))
                    drawRect(color = spec.colors[2], topLeft = Offset(0f, h * 0.7f), size = Size(w, h * 0.3f))
                } else {
                    val stripeHeight = h / count
                    spec.colors.forEachIndexed { i, color ->
                        drawRect(
                            color = color,
                            topLeft = Offset(0f, i * stripeHeight),
                            size = Size(w, stripeHeight)
                        )
                    }
                }
            }
        }
        FlagStyle.VERTICAL_STRIPES -> {
            val count = spec.colors.size
            if (count > 0) {
                val stripeWidth = w / count
                spec.colors.forEachIndexed { i, color ->
                    drawRect(
                        color = color,
                        topLeft = Offset(i * stripeWidth, 0f),
                        size = Size(stripeWidth, h)
                    )
                }
            }
        }
        FlagStyle.NORDIC_CROSS -> {
            val bg = spec.colors.getOrElse(0) { FlagColors.DarkBlue }
            val cross = spec.colors.getOrElse(1) { FlagColors.Gold }
            drawRect(color = bg, size = size)

            val thick = h * 0.22f
            val posX = w * 0.35f
            val posY = h * 0.5f - thick / 2f

            drawRect(color = cross, topLeft = Offset(posX - thick / 2f, 0f), size = Size(thick, h))
            drawRect(color = cross, topLeft = Offset(0f, posY), size = Size(w, thick))
        }
        FlagStyle.CROSS_CENTER -> {
            val bg = spec.colors.getOrElse(0) { FlagColors.Red }
            val cross = spec.colors.getOrElse(1) { FlagColors.White }
            drawRect(color = bg, size = size)

            val thick = h * 0.25f
            drawRect(color = cross, topLeft = Offset(w / 2f - thick / 2f, h * 0.2f), size = Size(thick, h * 0.6f))
            drawRect(color = cross, topLeft = Offset(w * 0.2f, h / 2f - thick / 2f), size = Size(w * 0.6f, thick))
        }
        FlagStyle.HOIST_TRIANGLE -> {
            val bg = spec.colors.getOrElse(0) { FlagColors.Red }
            drawRect(color = bg, size = size)
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w * 0.45f, h / 2f)
                lineTo(0f, h)
                close()
            }
            drawPath(path = path, color = spec.colors.getOrElse(1) { FlagColors.White })
        }
        FlagStyle.CANTON_FIELD -> {
            val bg = spec.colors.getOrElse(0) { FlagColors.DarkBlue }
            drawRect(color = bg, size = size)
            val cantonC = spec.cantonColor ?: FlagColors.DarkBlue
            drawRect(color = cantonC, topLeft = Offset(0f, 0f), size = Size(w * 0.45f, h * 0.5f))
        }
        FlagStyle.SALTIRE_DIAGONAL -> {
            val bg1 = spec.colors.getOrElse(1) { FlagColors.Green }
            val bg2 = spec.colors.getOrElse(2) { FlagColors.Black }
            val crossC = spec.colors.getOrElse(0) { FlagColors.Gold }

            drawRect(color = bg1, size = size)
            val pathTopBottom = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w / 2f, h / 2f)
                close()
            }
            drawPath(pathTopBottom, bg1)

            val pathLeftRight = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, h)
                lineTo(w / 2f, h / 2f)
                close()
            }
            drawPath(pathLeftRight, bg2)

            val thick = h * 0.15f
            drawLine(crossC, Offset(0f, 0f), Offset(w, h), strokeWidth = thick)
            drawLine(crossC, Offset(w, 0f), Offset(0f, h), strokeWidth = thick)
        }
    }

    // 2. Draw Emblem / Symbol if present
    if (spec.emblem != EmblemType.NONE) {
        val isMirrored = mutation?.type == FakeMutationType.MIRRORED_ASYMMETRIC_SYMBOL
        val center = when (spec.emblemPosition) {
            EmblemPosition.CENTER -> Offset(w / 2f, h / 2f)
            EmblemPosition.HOIST -> Offset(w * 0.33f, h / 2f)
            EmblemPosition.TOP_LEFT, EmblemPosition.CANTON -> Offset(w * 0.22f, h * 0.25f)
        }

        withTransform({
            if (isMirrored) {
                scale(-1f, 1f, pivot = center)
            }
        }) {
            drawEmblem(spec.emblem, center, h * 0.25f, spec.emblemColor)
        }
    }

    // 3. Draw Stars if needed
    if (spec.starCount > 0) {
        drawStarGroup(spec.starCount, spec.starColor, w, h, spec.emblemPosition)
    }
}

private fun DrawScope.drawEmblem(type: EmblemType, center: Offset, radius: Float, color: Color) {
    when (type) {
        EmblemType.SUN_CIRCLE -> {
            drawCircle(color = color, radius = radius, center = center)
        }
        EmblemType.MAPLE_LEAF -> {
            val path = Path().apply {
                moveTo(center.x, center.y - radius)
                lineTo(center.x + radius * 0.4f, center.y - radius * 0.3f)
                lineTo(center.x + radius * 0.8f, center.y - radius * 0.4f)
                lineTo(center.x + radius * 0.5f, center.y + radius * 0.2f)
                lineTo(center.x + radius * 0.7f, center.y + radius * 0.6f)
                lineTo(center.x + radius * 0.15f, center.y + radius * 0.4f)
                lineTo(center.x, center.y + radius)
                lineTo(center.x - radius * 0.15f, center.y + radius * 0.4f)
                lineTo(center.x - radius * 0.7f, center.y + radius * 0.6f)
                lineTo(center.x - radius * 0.5f, center.y + radius * 0.2f)
                lineTo(center.x - radius * 0.8f, center.y - radius * 0.4f)
                lineTo(center.x - radius * 0.4f, center.y - radius * 0.3f)
                close()
            }
            drawPath(path, color)
        }
        EmblemType.ASHOKA_CHAKRA -> {
            drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 3f))
            for (i in 0 until 24) {
                val angle = i * (2 * PI / 24)
                val endX = center.x + radius * cos(angle).toFloat()
                val endY = center.y + radius * sin(angle).toFloat()
                drawLine(color, center, Offset(endX, endY), strokeWidth = 2f)
            }
        }
        EmblemType.CRESCENT_STAR -> {
            drawCircle(color = color, radius = radius, center = center)
            drawCircle(color = FlagColors.Red, radius = radius * 0.82f, center = Offset(center.x + radius * 0.35f, center.y))
            drawFivePointStar(Offset(center.x + radius * 0.65f, center.y), radius * 0.45f, color)
        }
        EmblemType.STAR_SINGLE -> {
            drawFivePointStar(center, radius, color)
        }
        EmblemType.CEDAR_TREE -> {
            val path = Path().apply {
                moveTo(center.x, center.y - radius)
                lineTo(center.x + radius * 0.7f, center.y + radius * 0.5f)
                lineTo(center.x - radius * 0.7f, center.y + radius * 0.5f)
                close()
            }
            drawPath(path, color)
        }
        EmblemType.GLOBE_CIRCLE -> {
            drawCircle(color = color, radius = radius, center = center)
            drawLine(FlagColors.White, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), strokeWidth = 3f)
        }
        else -> {
            drawCircle(color = color, radius = radius, center = center)
        }
    }
}

private fun DrawScope.drawStarGroup(count: Int, color: Color, w: Float, h: Float, pos: EmblemPosition) {
    val starRadius = if (count > 10) 4f else 12f
    if (count == 1) {
        val center = Offset(w / 2f, h / 2f)
        drawFivePointStar(center, 20f, color)
    } else if (count in 2..6) {
        val startX = if (pos == EmblemPosition.TOP_LEFT) w * 0.15f else w * 0.65f
        val startY = h * 0.3f
        for (i in 0 until count) {
            val cx = startX + (i % 3) * 18f
            val cy = startY + (i / 3) * 18f
            drawFivePointStar(Offset(cx, cy), starRadius, color)
        }
    } else {
        // Grid of stars for USA / CN
        val cols = 5
        val rows = count / cols
        val startX = w * 0.05f
        val startY = h * 0.08f
        for (r in 0 until rows.coerceAtLeast(1)) {
            for (c in 0 until cols) {
                val cx = startX + c * (w * 0.07f)
                val cy = startY + r * (h * 0.08f)
                drawFivePointStar(Offset(cx, cy), starRadius, color)
            }
        }
    }
}

private fun DrawScope.drawFivePointStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val outerR = radius
    val innerR = radius * 0.382f
    val angleStep = PI / 5.0

    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = i * angleStep - PI / 2.0
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    drawPath(path, color)
}
