package sc.meteo.seymeteo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sc.meteo.seymeteo.data.model.DailyForecastItem
import java.text.SimpleDateFormat
import java.util.*

data class HourlyPoint(
    val timeLabel: String,
    val temp: Int,
    val conditionLabel: String,
    val rainChancePct: Int,
    val iconEmoji: String
)

@Composable
fun HourlyForecastCurve(
    forecastItems: List<DailyForecastItem>,
    modifier: Modifier = Modifier
) {
    // Synthesize 12-hour / 24-hour progressive timeline points from daily forecast items & diurnal cycle
    val hourlyPoints = remember(forecastItems) {
        val baseTemp = forecastItems.firstOrNull()?.tempMax?.toInt() ?: 28
        val minTemp = forecastItems.firstOrNull()?.tempMin?.toInt() ?: 24
        val rainChanceStr = forecastItems.firstOrNull()?.rainChance ?: "20%"
        val baseRain = rainChanceStr.replace("%", "").trim().toIntOrNull() ?: 20

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val timeFormat = SimpleDateFormat("h a", Locale.ENGLISH)

        (0..12).map { offset ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, offset)
            }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val isNight = hour < 6 || hour >= 18

            // Diurnal temperature curve (peaking at 13:00, coolest at 05:00)
            val diurnalFactor = kotlin.math.sin((hour - 5) / 24.0 * 2.0 * kotlin.math.PI)
            val temp = (minTemp + (baseTemp - minTemp) * ((diurnalFactor + 1) / 2.0)).toInt()

            val rainPct = ((baseRain + kotlin.math.sin(offset.toDouble()) * 15).toInt()).coerceIn(5, 95)
            val emoji = when {
                rainPct >= 60 -> "🌧️"
                rainPct >= 35 -> "🌦️"
                isNight -> "🌙"
                else -> "⛅"
            }

            HourlyPoint(
                timeLabel = if (offset == 0) "Now" else timeFormat.format(cal.time),
                temp = temp,
                conditionLabel = if (rainPct >= 50) "Passing Showers" else "Partly Cloudy",
                rainChancePct = rainPct,
                iconEmoji = emoji
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0F2B48)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3338BDF8))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hourly Forecast & Rain Curve",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = "48h Forecast ›",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontally scrollable smooth Bezier curve & node column
            val scrollState = rememberScrollState()
            val pointWidth = 68.dp
            val chartHeight = 140.dp
            val totalWidth = pointWidth * hourlyPoints.size

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Column(modifier = Modifier.width(totalWidth)) {
                    // 1. Time labels & Weather icons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        hourlyPoints.forEach { point ->
                            Column(
                                modifier = Modifier.width(pointWidth),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = point.timeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = point.iconEmoji,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Continuous Cubic Bezier Temperature Curve Canvas
                    val minT = (hourlyPoints.minOfOrNull { it.temp } ?: 22) - 1
                    val maxT = (hourlyPoints.maxOfOrNull { it.temp } ?: 30) + 1

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeight)
                    ) {
                        val w = size.width
                        val h = size.height
                        val stepX = w / (hourlyPoints.size - 1)

                        val points = hourlyPoints.mapIndexed { idx, pt ->
                            val x = idx * stepX
                            val normY = (pt.temp - minT).toFloat() / (maxT - minT).toFloat()
                            val y = h * 0.65f - (normY * (h * 0.45f))
                            Offset(x, y)
                        }

                        // Build Cubic Bezier Path
                        val path = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points[0].x, points[0].y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val cx = (p0.x + p1.x) / 2f
                                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                }
                            }
                        }

                        // Gradient fill under curve
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, h)
                            lineTo(points.first().x, h)
                            close()
                        }
                        drawPath(
                            fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x5538BDF8),
                                    Color(0x1038BDF8),
                                    Color.Transparent
                                )
                            )
                        )

                        // Draw Curve Stroke
                        drawPath(
                            path,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFFFDE047),
                                    Color(0xFF38BDF8)
                                )
                            ),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw Point Knots & Temperature Labels
                        points.forEachIndexed { index, pt ->
                            val temp = hourlyPoints[index].temp
                            drawCircle(
                                color = Color(0xFF0F2B48),
                                radius = 5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color(0xFF38BDF8),
                                radius = 3.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }

                    // 3. Temperature text row aligned with points
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        hourlyPoints.forEach { point ->
                            Box(
                                modifier = Modifier.width(pointWidth),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "°",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. Rain Droplet Pills Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        hourlyPoints.forEach { point ->
                            Column(
                                modifier = Modifier.width(pointWidth),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = if (point.rainChancePct >= 40) Color(0xFF38BDF8) else Color(0xFF64748B),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (point.rainChancePct >= 40) Color(0xFFBAE6FD) else Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
