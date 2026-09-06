package sc.meteo.seymeteo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.*
import kotlin.random.Random

enum class AtmosphericCondition {
    SUNNY,
    PARTLY_CLOUDY,
    OVERCAST,
    PASSING_SHOWERS,
    HEAVY_RAIN,
    THUNDERSTORM,
    WINDY,
    NIGHT_CLEAR
}

fun resolveCondition(conditionLabel: String?, rainChance: String?, wind: String?): AtmosphericCondition {
    val text = (conditionLabel ?: "").lowercase()
    val rainInt = rainChance?.replace("%", "")?.trim()?.toIntOrNull() ?: 0
    val windVal = wind?.lowercase() ?: ""

    return when {
        text.contains("thunder") || text.contains("storm") || text.contains("lightning") -> AtmosphericCondition.THUNDERSTORM
        text.contains("heavy rain") || text.contains("torrential") || rainInt >= 80 -> AtmosphericCondition.HEAVY_RAIN
        text.contains("shower") || text.contains("rain") || rainInt >= 40 -> AtmosphericCondition.PASSING_SHOWERS
        windVal.contains("40") || windVal.contains("50") || windVal.contains("gust") || windVal.contains("fresh") -> AtmosphericCondition.WINDY
        text.contains("cloud") || text.contains("overcast") -> AtmosphericCondition.OVERCAST
        text.contains("partly") || text.contains("fair") -> AtmosphericCondition.PARTLY_CLOUDY
        else -> AtmosphericCondition.SUNNY
    }
}

/**
 * Atmospheric Window: A frosted-glass window through which the living
 * Seychelles tropical climate is visible — animated swaying palm silhouettes,
 * granitic mountain ridges, rain streaks sliding down the glass pane,
 * and warm pulsing sunbeams.
 */
@Composable
fun AtmosphericWindowBackground(
    condition: AtmosphericCondition,
    windSpeedKmh: Float = 22f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AtmosphericWindow")

    // Continuous time ticker (0 to 1 over 10 seconds)
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Fast loop for rain & breeze particles
    val rainTicker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainTicker"
    )

    // Gentle sun pulse
    val sunGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunGlow"
    )

    // Deterministic random drops on the glass pane
    val rainDrops = remember {
        List(45) {
            RainDropOnGlass(
                xPct = Random.nextFloat(),
                initialY = Random.nextFloat(),
                speed = 0.4f + Random.nextFloat() * 0.8f,
                length = 25f + Random.nextFloat() * 45f,
                width = 1.5f + Random.nextFloat() * 2.5f,
                alpha = 0.35f + Random.nextFloat() * 0.45f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Procedural Living Window Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // A. Dynamic Atmospheric Sky Gradient
            drawSkyBackdrop(condition, w, h)

            // B. Distant Granitic Peaks (Morne Seychellois / Silhouette)
            drawGraniticMountainRidges(condition, w, h)

            // C. Blurred Swaying Tropical Palm & Foliage Silhouettes (Modulated by wind)
            drawSwayingTropicalSilhouette(time, windSpeedKmh, condition, w, h)

            // D. Sunny Radiance & Golden Lens Glow
            if (condition == AtmosphericCondition.SUNNY || condition == AtmosphericCondition.PARTLY_CLOUDY) {
                drawSunRadiance(sunGlow, w, h)
            }

            // E. Glass Pane Frosted Vignette
            drawGlassFrostedVignette(condition, w, h)

            // F. Rain Streaks & Water Droplets on Glass Pane
            if (condition == AtmosphericCondition.PASSING_SHOWERS ||
                condition == AtmosphericCondition.HEAVY_RAIN ||
                condition == AtmosphericCondition.THUNDERSTORM
            ) {
                drawRainStreaksOnGlass(rainTicker, rainDrops, windSpeedKmh, w, h, condition)
            }

            // G. Wind Wisps (Breeze streaks across the glass)
            if (condition == AtmosphericCondition.WINDY || windSpeedKmh >= 28f) {
                drawWindWisps(time, windSpeedKmh, w, h)
            }
        }

        // 2. Translucent Glass Tint Overlay for high UI contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x150A192F),
                            Color(0x25071324),
                            Color(0x3D050C17)
                        )
                    )
                )
        )

        // 3. App UI Content
        content()
    }
}

private class RainDropOnGlass(
    val xPct: Float,
    val initialY: Float,
    val speed: Float,
    val length: Float,
    val width: Float,
    val alpha: Float
)

private fun DrawScope.drawSkyBackdrop(condition: AtmosphericCondition, w: Float, h: Float) {
    val gradientColors = when (condition) {
        AtmosphericCondition.SUNNY -> listOf(
            Color(0xFF0284C7), // Tropical Azure
            Color(0xFF0369A1),
            Color(0xFF0F3A65),
            Color(0xFF0B1E36)
        )
        AtmosphericCondition.PARTLY_CLOUDY -> listOf(
            Color(0xFF0284C7),
            Color(0xFF38BDF8),
            Color(0xFF1E3A5F),
            Color(0xFF0C1929)
        )
        AtmosphericCondition.OVERCAST -> listOf(
            Color(0xFF334155),
            Color(0xFF1E293B),
            Color(0xFF0F172A),
            Color(0xFF080D1A)
        )
        AtmosphericCondition.PASSING_SHOWERS -> listOf(
            Color(0xFF1E3A5F),
            Color(0xFF0F2B48),
            Color(0xFF0A1F35),
            Color(0xFF061322)
        )
        AtmosphericCondition.HEAVY_RAIN -> listOf(
            Color(0xFF0F172A),
            Color(0xFF0A192F),
            Color(0xFF07111E),
            Color(0xFF03070D)
        )
        AtmosphericCondition.THUNDERSTORM -> listOf(
            Color(0xFF180D2B),
            Color(0xFF0F172A),
            Color(0xFF090D17),
            Color(0xFF04060A)
        )
        AtmosphericCondition.WINDY -> listOf(
            Color(0xFF0369A1),
            Color(0xFF0284C7),
            Color(0xFF0F3254),
            Color(0xFF091B2E)
        )
        AtmosphericCondition.NIGHT_CLEAR -> listOf(
            Color(0xFF050B14),
            Color(0xFF0A1526),
            Color(0xFF0D1E36),
            Color(0xFF060D18)
        )
    }

    drawRect(
        brush = Brush.verticalGradient(
            colors = gradientColors,
            startY = 0f,
            endY = h
        )
    )
}

private fun DrawScope.drawGraniticMountainRidges(condition: AtmosphericCondition, w: Float, h: Float) {
    val ridgePath = Path().apply {
        moveTo(0f, h * 0.72f)
        cubicTo(w * 0.2f, h * 0.65f, w * 0.35f, h * 0.69f, w * 0.55f, h * 0.63f)
        cubicTo(w * 0.75f, h * 0.58f, w * 0.88f, h * 0.66f, w, h * 0.62f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }

    val ridgeColor = when (condition) {
        AtmosphericCondition.SUNNY -> Color(0x28064E3B) // Lush granitic emerald tint
        AtmosphericCondition.THUNDERSTORM -> Color(0x350F172A)
        else -> Color(0x280F2B48)
    }

    drawPath(ridgePath, color = ridgeColor)
}

private fun DrawScope.drawSwayingTropicalSilhouette(
    time: Float,
    windSpeedKmh: Float,
    condition: AtmosphericCondition,
    w: Float,
    h: Float
) {
    // Wind factor determines sway amplitude & speed
    val windFactor = (windSpeedKmh / 20f).coerceIn(0.5f, 2.5f)
    val swayOffset = sin(time * 0.8f * windFactor) * (18f * windFactor)
    val swayOffset2 = cos(time * 0.6f * windFactor) * (14f * windFactor)

    // Left Palm Tree Silhouette (Blurred foliage)
    val palmTrunk = Path().apply {
        moveTo(w * 0.12f, h)
        cubicTo(
            w * 0.14f + swayOffset * 0.3f, h * 0.75f,
            w * 0.18f + swayOffset * 0.7f, h * 0.50f,
            w * 0.22f + swayOffset, h * 0.32f
        )
    }
    drawPath(
        palmTrunk,
        color = Color(0x400A1F33),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16f, cap = StrokeCap.Round)
    )

    // Palm Crown Fronds
    val crownX = w * 0.22f + swayOffset
    val crownY = h * 0.32f

    val frondColor = Color(0x380D2B45)
    for (i in 0 until 6) {
        val angle = (i * 60.0 + swayOffset2 * 0.8).toDouble() * (PI / 180.0)
        val endX = crownX + cos(angle).toFloat() * 110f
        val endY = crownY + sin(angle).toFloat() * 70f + 25f

        val frond = Path().apply {
            moveTo(crownX, crownY)
            quadraticTo(
                (crownX + endX) / 2f + swayOffset * 0.5f,
                (crownY + endY) / 2f - 20f,
                endX,
                endY
            )
        }
        drawPath(
            frond,
            color = frondColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10f, cap = StrokeCap.Round)
        )
    }

    // Right Foliage Silhouette
    val rightBush = Path().apply {
        moveTo(w * 0.75f + swayOffset2, h)
        cubicTo(w * 0.8f + swayOffset2, h * 0.78f, w * 0.9f + swayOffset, h * 0.70f, w, h * 0.68f)
        lineTo(w, h)
        close()
    }
    drawPath(rightBush, color = Color(0x35081928))
}

private fun DrawScope.drawSunRadiance(sunGlow: Float, w: Float, h: Float) {
    val sunCenter = Offset(w * 0.82f, h * 0.14f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x55F59E0B), // Warm amber glow
                Color(0x25FDE047),
                Color(0x00FDE047)
            ),
            center = sunCenter,
            radius = 240f * sunGlow
        ),
        radius = 240f * sunGlow,
        center = sunCenter
    )
}

private fun DrawScope.drawGlassFrostedVignette(condition: AtmosphericCondition, w: Float, h: Float) {
    // Subtle frosted rim glow simulating looking through a glass pane
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x10FFFFFF),
                Color(0x220A1F33)
            ),
            center = Offset(w / 2f, h / 2f),
            radius = max(w, h) * 0.75f
        )
    )
}

private fun DrawScope.drawRainStreaksOnGlass(
    ticker: Float,
    drops: List<RainDropOnGlass>,
    windSpeedKmh: Float,
    w: Float,
    h: Float,
    condition: AtmosphericCondition
) {
    val slant = (windSpeedKmh * 0.45f).coerceIn(4f, 22f)
    val dropCount = if (condition == AtmosphericCondition.HEAVY_RAIN || condition == AtmosphericCondition.THUNDERSTORM) 45 else 24

    for (i in 0 until dropCount) {
        val drop = drops[i]
        // Vertical progress with wrapping
        val progress = (drop.initialY + ticker * drop.speed) % 1.0f
        val x = drop.xPct * w + (progress * slant)
        val y = progress * (h + drop.length) - drop.length

        // Streak tail
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFBAE6FD).copy(alpha = drop.alpha * 0.4f),
                    Color(0xFFE0F2FE).copy(alpha = drop.alpha)
                ),
                startY = y,
                endY = y + drop.length
            ),
            start = Offset(x, y),
            end = Offset(x + (slant * 0.15f), y + drop.length),
            strokeWidth = drop.width,
            cap = StrokeCap.Round
        )

        // Droplet Head on Glass
        drawCircle(
            color = Color(0xFFFFFFFF).copy(alpha = drop.alpha * 0.9f),
            radius = drop.width * 0.9f,
            center = Offset(x + (slant * 0.15f), y + drop.length)
        )
    }
}

private fun DrawScope.drawWindWisps(time: Float, windSpeedKmh: Float, w: Float, h: Float) {
    val speedFactor = (windSpeedKmh / 25f).coerceIn(0.8f, 2.0f)
    for (i in 0 until 5) {
        val yBase = h * (0.25f + i * 0.15f)
        val offset = ((time * 180f * speedFactor + i * 140f) % (w + 200f)) - 100f
        val path = Path().apply {
            moveTo(offset, yBase)
            cubicTo(
                offset + 60f, yBase - 12f,
                offset + 120f, yBase + 12f,
                offset + 180f, yBase - 4f
            )
        }
        drawPath(
            path,
            color = Color(0x18FFFFFF),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}
