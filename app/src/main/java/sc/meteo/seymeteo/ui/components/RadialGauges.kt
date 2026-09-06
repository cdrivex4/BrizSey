package sc.meteo.seymeteo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sc.meteo.seymeteo.data.model.DailyForecastItem
import sc.meteo.seymeteo.data.model.MarineTideData
import kotlin.math.*

@Composable
fun RadialWeatherInstrumentsGrid(
    forecast: DailyForecastItem?,
    marineData: MarineTideData?,
    glassOpacity: Float = 0.35f,
    modifier: Modifier = Modifier
) {
    val windStr = forecast?.wind ?: "SE 21 km/h"
    val windSpeed = remember(windStr) {
        val digits = Regex("\\d+").find(windStr)?.value?.toFloatOrNull() ?: 21f
        digits
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Row 1: Wind Compass Dial & Barometric Pressure Half-Arc
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wind Compass Gauge Card
            WindCompassCard(
                windSpeedKmh = windSpeed,
                directionLabel = if (windStr.contains("NW", ignoreCase = true)) "NW" else "SE",
                glassOpacity = glassOpacity,
                modifier = Modifier.weight(1f)
            )

            // Pressure Semi-Circle Arc Card
            PressureGaugeCard(
                pressureHpa = 1013.8f,
                trend = "Currently stable",
                glassOpacity = glassOpacity,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: UV Index & Humidity Meters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UvIndexCard(
                uvIndex = 9,
                glassOpacity = glassOpacity,
                modifier = Modifier.weight(1f)
            )

            HumidityCard(
                humidityPct = 82,
                dewPoint = "24°C",
                glassOpacity = glassOpacity,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WindCompassCard(
    windSpeedKmh: Float,
    directionLabel: String,
    glassOpacity: Float = 0.35f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WindNeedle")
    val needleWobble by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )

    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B48).copy(alpha = glassOpacity)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = (glassOpacity + 0.15f).coerceAtMost(0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Wind & Gusts",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = directionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                )
            }

            // Radial Compass Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(68.dp)) {
                    val r = size.width / 2f
                    val center = Offset(r, r)

                    // Outer dial tick marks
                    for (i in 0 until 12) {
                        val angle = (i * 30.0) * (PI / 180.0)
                        val start = Offset(
                            center.x + cos(angle).toFloat() * (r - 4f),
                            center.y + sin(angle).toFloat() * (r - 4f)
                        )
                        val end = Offset(
                            center.x + cos(angle).toFloat() * r,
                            center.y + sin(angle).toFloat() * r
                        )
                        drawLine(
                            color = if (i % 3 == 0) Color(0xFF38BDF8) else Color(0x4494A3B8),
                            start = start,
                            end = end,
                            strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // Rotating Arrow
                    val baseAngle = if (directionLabel == "SE") 135.0 else 315.0
                    val currentAngle = (baseAngle + needleWobble) * (PI / 180.0)
                    val tip = Offset(
                        center.x + cos(currentAngle).toFloat() * (r - 12f),
                        center.y + sin(currentAngle).toFloat() * (r - 12f)
                    )
                    drawLine(
                        color = Color(0xFF38BDF8),
                        start = center,
                        end = tip,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = Color(0xFF38BDF8), radius = 4.dp.toPx(), center = center)
                }

                Text(
                    text = "${windSpeedKmh.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            }

            Text(
                text = "km/h · Moderate breeze",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun PressureGaugeCard(
    pressureHpa: Float,
    trend: String,
    glassOpacity: Float = 0.35f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B48).copy(alpha = glassOpacity)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = (glassOpacity + 0.15f).coerceAtMost(0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Compress,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pressure",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Semi-Circle Arc Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val arcRect = Size(w * 0.75f, h * 1.5f)
                    val topLeft = Offset(w * 0.125f, 4f)

                    // Track
                    drawArc(
                        color = Color(0x3394A3B8),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcRect,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Value Arc (Normalized 1000 - 1025 hPa)
                    val norm = ((pressureHpa - 1000f) / 25f).coerceIn(0f, 1f)
                    drawArc(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
                        ),
                        startAngle = 180f,
                        sweepAngle = 180f * norm,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcRect,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text(
                        text = "$pressureHpa",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "hPa",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = trend,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun UvIndexCard(
    uvIndex: Int,
    glassOpacity: Float = 0.35f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B48).copy(alpha = glassOpacity)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = (glassOpacity + 0.15f).coerceAtMost(0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "UV Index",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column {
                Text(
                    text = "$uvIndex · Very High",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (uvIndex / 12f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFFEA580C),
                    trackColor = Color(0x3394A3B8)
                )
            }

            Text(
                text = "Sun protection required",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun HumidityCard(
    humidityPct: Int,
    dewPoint: String,
    glassOpacity: Float = 0.35f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B48).copy(alpha = glassOpacity)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = (glassOpacity + 0.15f).coerceAtMost(0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Humidity",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column {
                Text(
                    text = "$humidityPct%",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (humidityPct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFF38BDF8),
                    trackColor = Color(0x3394A3B8)
                )
            }

            Text(
                text = "Dew point is $dewPoint",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}
