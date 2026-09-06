package sc.meteo.seymeteo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import sc.meteo.seymeteo.data.model.DailyForecastItem

@Composable
fun SevenDayForecastCard(
    forecastItems: List<DailyForecastItem>,
    modifier: Modifier = Modifier
) {
    val overallMin = forecastItems.minOfOrNull { it.tempMin.toInt() } ?: 23
    val overallMax = forecastItems.maxOfOrNull { it.tempMax.toInt() } ?: 29

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0F2B48)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3338BDF8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "7-Day Probabilistic Forecast",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "15-day forecast ›",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            forecastItems.forEachIndexed { index, item ->
                ForecastRowItem(
                    item = item,
                    isToday = index == 0,
                    overallMin = overallMin,
                    overallMax = overallMax
                )
                if (index < forecastItems.size - 1) {
                    HorizontalDivider(
                        color = Color(0x18FFFFFF),
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastRowItem(
    item: DailyForecastItem,
    isToday: Boolean,
    overallMin: Int,
    overallMax: Int,
    modifier: Modifier = Modifier
) {
    val rainChanceStr = item.rainChance ?: "20%"
    val rainPct = rainChanceStr.replace("%", "").trim().toIntOrNull() ?: 20

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Day Label
        Text(
            text = if (isToday) "Today" else item.dayOfWeek,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (isToday) Color(0xFF38BDF8) else Color(0xFFF1F5F9),
            modifier = Modifier.width(60.dp)
        )

        // Rain Probability Droplet Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(55.dp)
        ) {
            if (rainPct >= 25) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFBAE6FD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Weather Icon
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            item.conditionIconUrl?.let { iconUrl ->
                AsyncImage(
                    model = iconUrl,
                    contentDescription = item.conditionLabel,
                    modifier = Modifier.size(24.dp)
                )
            } ?: Text(
                text = if (rainPct >= 60) "🌧️" else "⛅",
                fontSize = 16.sp
            )
        }

        // Min Temp
        Text(
            text = "°",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(26.dp)
        )

        // Gradient Capsule Bar
        val range = (overallMax - overallMin).coerceAtLeast(1)
        val leftOffsetNorm = ((item.tempMin.toInt() - overallMin).toFloat() / range).coerceIn(0f, 0.4f)
        val barWidthNorm = ((item.tempMax.toInt() - item.tempMin.toInt()).toFloat() / range).coerceIn(0.2f, 1f)

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(6.dp)
                .clip(CircleShape)
                .background(Color(0x2238BDF8))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(barWidthNorm)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFFFBBF24))
                        )
                    )
            )
        }

        // Max Temp
        Text(
            text = "°",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(26.dp)
        )
    }
}
