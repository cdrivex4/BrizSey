package sc.meteo.seymeteo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import sc.meteo.seymeteo.ui.theme.SeyBorder
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
import sc.meteo.seymeteo.ui.theme.SeySkyBlue
import sc.meteo.seymeteo.ui.theme.SeySunGold
import sc.meteo.seymeteo.ui.theme.SeySurfaceCard
import sc.meteo.seymeteo.ui.theme.SeyTextMuted
import sc.meteo.seymeteo.ui.theme.SeyTextPrimary
import sc.meteo.seymeteo.ui.theme.SeyTextSecondary

@Composable
fun SevenDayForecastCard(
    forecastItems: List<DailyForecastItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SeySurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = SeyNavyPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "7-Day Probabilistic Forecast",
                    style = MaterialTheme.typography.titleLarge,
                    color = SeyTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            forecastItems.forEachIndexed { index, item ->
                ForecastRowItem(item = item, isToday = index == 0)
                if (index < forecastItems.size - 1) {
                    HorizontalDivider(
                        color = SeyBorder,
                        thickness = 0.8.dp,
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Day & Date
        Column(modifier = Modifier.width(70.dp)) {
            Text(
                text = if (isToday) "Today" else item.dayOfWeek,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isToday) SeyNavyPrimary else SeyTextPrimary
            )
            Text(
                text = item.dateFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = SeyTextMuted
            )
        }

        // Icon & Condition
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            item.conditionIconUrl?.let { iconUrl ->
                AsyncImage(
                    model = iconUrl,
                    contentDescription = item.conditionLabel,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = item.conditionLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = SeyTextSecondary,
                maxLines = 1
            )
        }

        // Temp Range (Min / Max)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${item.tempMin.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                color = SeyTextMuted,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(SeySkyBlue, SeySunGold)
                        )
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${item.tempMax.toInt()}°",
                style = MaterialTheme.typography.bodyLarge,
                color = SeyTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
