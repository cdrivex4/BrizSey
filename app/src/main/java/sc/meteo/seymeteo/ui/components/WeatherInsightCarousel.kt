package sc.meteo.seymeteo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sc.meteo.seymeteo.data.model.DailyForecastItem
import sc.meteo.seymeteo.data.model.MarineTideData
import sc.meteo.seymeteo.data.model.PredictabilityAssessment
import sc.meteo.seymeteo.data.model.SunMoonInfo

data class InsightCardItem(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val badgeColor: Color,
    val hasSolarArc: Boolean = false
)

@Composable
fun WeatherInsightCarousel(
    forecast: DailyForecastItem?,
    predictability: PredictabilityAssessment,
    marineData: MarineTideData?,
    sunMoonInfo: SunMoonInfo?,
    glassOpacity: Float = 0.35f,
    modifier: Modifier = Modifier
) {
    val rainChance = forecast?.rainChance ?: "65%"
    val rainPct = rainChance.replace("%", "").trim().toIntOrNull() ?: 65
    val sunsetTime = sunMoonInfo?.sunsetTime ?: "18:22"

    val items = remember(forecast, predictability, marineData, sunMoonInfo) {
        listOf(
            InsightCardItem(
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF38BDF8),
                title = if (rainPct >= 50) "Rain Coming" else "Clear Skies Ahead",
                subtitle = if (rainPct >= 50) "Passing convective showers likely today" else "Low precipitation probability today",
                badgeText = "$rainPct%",
                badgeColor = Color(0xFF0284C7)
            ),
            InsightCardItem(
                icon = Icons.Default.WbSunny,
                iconTint = Color(0xFFFBBF24),
                title = "Don't miss the sunset",
                subtitle = "Sunset will be at $sunsetTime in Victoria",
                badgeText = sunsetTime,
                badgeColor = Color(0xFFEA580C),
                hasSolarArc = true
            ),
            InsightCardItem(
                icon = Icons.Default.Info,
                iconTint = Color(0xFF10B981),
                title = "Atmospheric Stability",
                subtitle = predictability.headline,
                badgeText = "${predictability.score}%",
                badgeColor = Color(0xFF059669)
            ),
            InsightCardItem(
                icon = Icons.Default.Waves,
                iconTint = Color(0xFF06B6D4),
                title = "Marine & Swell Advisory",
                subtitle = "${marineData?.seaCondition ?: "Moderate"} · Waves ${marineData?.waveHeight ?: "1.5 - 2.2m"}",
                badgeText = marineData?.swellDirection ?: "SE Swell",
                badgeColor = Color(0xFF0891B2)
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { items.size })

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B48).copy(alpha = glassOpacity)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = (glassOpacity * 0.8f + 0.1f).coerceIn(0.15f, 0.45f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val item = items[page]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(item.iconTint.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }

                    if (item.hasSolarArc) {
                        // Mini glowing solar arc
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val arcPath = Path().apply {
                                    moveTo(4f, h - 6f)
                                    quadraticTo(w / 2f, 4f, w - 4f, h - 6f)
                                }
                                drawPath(
                                    arcPath,
                                    color = Color(0x55FBBF24),
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                // Golden Sun Marker
                                drawCircle(
                                    color = Color(0xFFFBBF24),
                                    radius = 4.dp.toPx(),
                                    center = Offset(w * 0.75f, h * 0.45f)
                                )
                            }
                        }
                    } else {
                        // Large Bold Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = item.badgeColor.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, item.badgeColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = item.badgeText,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pager dot indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(items.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 6.dp else 4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF38BDF8) else Color(0x4494A3B8))
                    )
                }
            }
        }
    }
}
