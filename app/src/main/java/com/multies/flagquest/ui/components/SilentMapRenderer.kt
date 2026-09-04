package com.multies.flagquest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.SilentMapFormat
import com.multies.flagquest.data.model.SilentMapQuestion
import com.multies.flagquest.data.repository.CountryMapRepository
import com.multies.flagquest.ui.localization.Locales

@Composable
fun SilentMapCanvas(
    question: SilentMapQuestion,
    lang: String,
    modifier: Modifier = Modifier
) {
    val target = question.targetCountry
    val context = LocalContext.current

    val countryPath = remember(target.id) {
        CountryMapRepository.loadCountryPath(target.id, context)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.25f)
            .testTag("silent_map_canvas_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("silent_map_canvas")
            ) {
                val w = size.width
                val h = size.height

                // Draw Water / Grid Background
                drawRect(
                    color = Color(0xFF0F172A).copy(alpha = 0.08f),
                    size = size
                )

                // Grid Lines
                val gridPaint = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                val gridColor = onSurfaceVariant.copy(alpha = 0.12f)
                val numGrid = 6
                for (i in 1 until numGrid) {
                    val x = w * (i / numGrid.toFloat())
                    val y = h * (i / numGrid.toFloat())
                    drawLine(gridColor, Offset(x, 0f), Offset(x, h), pathEffect = gridPaint)
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), pathEffect = gridPaint)
                }

                if (countryPath != null) {
                    when (question.format) {
                        SilentMapFormat.SILHOUETTE -> {
                            drawRealCountryMap(
                                path = countryPath,
                                primaryColor = primaryColor,
                                strokeColor = primaryColor,
                                canvasWidth = w,
                                canvasHeight = h
                            )
                        }
                        SilentMapFormat.CONTINENT_CONTEXT -> {
                            drawRealCountryMap(
                                path = countryPath,
                                primaryColor = tertiaryColor,
                                strokeColor = tertiaryColor,
                                canvasWidth = w,
                                canvasHeight = h
                            )
                        }
                        SilentMapFormat.NEIGHBORS_CONTEXT -> {
                            drawRealCountryMap(
                                path = countryPath,
                                primaryColor = primaryColor,
                                strokeColor = secondaryColor,
                                canvasWidth = w,
                                canvasHeight = h
                            )
                        }
                        SilentMapFormat.ROTATED_SILHOUETTE -> {
                            rotate(question.rotationDegrees, pivot = Offset(w / 2f, h / 2f)) {
                                drawRealCountryMap(
                                    path = countryPath,
                                    primaryColor = secondaryColor,
                                    strokeColor = primaryColor,
                                    canvasWidth = w,
                                    canvasHeight = h
                                )
                            }
                        }
                        SilentMapFormat.ISLAND_ZOOM -> {
                            val center = Offset(w / 2f, h / 2f)
                            val maxRadius = minOf(w, h) * 0.45f
                            for (r in listOf(0.3f, 0.6f, 0.9f)) {
                                drawCircle(
                                    color = tertiaryColor.copy(alpha = 0.15f),
                                    radius = maxRadius * r,
                                    center = center,
                                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
                                )
                            }
                            drawRealCountryMap(
                                path = countryPath,
                                primaryColor = primaryColor,
                                strokeColor = tertiaryColor,
                                canvasWidth = w,
                                canvasHeight = h
                            )
                        }
                        SilentMapFormat.REGIONAL_CONTEXT -> {
                            drawRealCountryMap(
                                path = countryPath,
                                primaryColor = primaryColor,
                                strokeColor = primaryColor,
                                canvasWidth = w,
                                canvasHeight = h
                            )
                        }
                    }
                }
            }

            // If map loading fails, display localized error card instead of fake shape
            if (countryPath == null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = Locales.get("map_loading_error", lang),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Overlay Badge for Question Format
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (icon, formatKey) = when (question.format) {
                        SilentMapFormat.SILHOUETTE -> Icons.Default.Map to "map_format_silhouette"
                        SilentMapFormat.CONTINENT_CONTEXT -> Icons.Default.Public to "map_format_continent"
                        SilentMapFormat.NEIGHBORS_CONTEXT -> Icons.Default.Explore to "map_format_neighbors"
                        SilentMapFormat.ROTATED_SILHOUETTE -> Icons.Default.CompassCalibration to "map_format_rotated"
                        SilentMapFormat.ISLAND_ZOOM -> Icons.Default.ZoomIn to "map_format_island"
                        SilentMapFormat.REGIONAL_CONTEXT -> Icons.Default.Public to "map_format_continent"
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Locales.get(formatKey, lang),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Compass / Orientation indicator in corner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                Text(
                    text = if (question.format == SilentMapFormat.ROTATED_SILHOUETTE) "🧭 ↻ ${question.rotationDegrees.toInt()}°" else "🧭 N",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawRealCountryMap(
    path: Path,
    primaryColor: Color,
    strokeColor: Color,
    canvasWidth: Float,
    canvasHeight: Float,
    paddingDp: Float = 20f
) {
    val bounds = path.getBounds()
    if (bounds.isEmpty || bounds.width <= 0f || bounds.height <= 0f) return

    val padding = paddingDp * density
    val availW = maxOf(10f, canvasWidth - 2 * padding)
    val availH = maxOf(10f, canvasHeight - 2 * padding)

    val scale = minOf(availW / bounds.width, availH / bounds.height)
    val targetLeft = padding + (availW - bounds.width * scale) / 2f
    val targetTop = padding + (availH - bounds.height * scale) / 2f

    val translateX = targetLeft - bounds.left * scale
    val translateY = targetTop - bounds.top * scale

    val strokeWidth = maxOf(2f, minOf(canvasWidth, canvasHeight) * 0.008f)

    withTransform({
        translate(left = translateX, top = translateY)
        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
    }) {
        // Fill landmasses (mainland + island archipelagos)
        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.88f)
        )

        // Outer outline stroke
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = strokeWidth / scale, cap = StrokeCap.Round)
        )

        // Inner contrast highlight
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.35f),
            style = Stroke(width = (strokeWidth * 0.4f) / scale, cap = StrokeCap.Round)
        )
    }
}
