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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import sc.meteo.seymeteo.data.model.DailyForecastItem
import sc.meteo.seymeteo.data.model.IslandLocation
import sc.meteo.seymeteo.ui.theme.SeyAlertOrange
import sc.meteo.seymeteo.ui.theme.SeyNavyDark
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
import sc.meteo.seymeteo.ui.theme.SeySkyBlue
import sc.meteo.seymeteo.ui.theme.SeySunGold

@Composable
fun CurrentWeatherCard(
    island: IslandLocation,
    currentForecast: DailyForecastItem?,
    glassOpacity: Float = 0.35f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B48).copy(alpha = glassOpacity)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = (glassOpacity * 0.8f + 0.1f).coerceIn(0.15f, 0.45f)))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header: Island Name & Subtitle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = island.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = island.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SeySkyBlue
                        )
                    }
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "LIVE SMA",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Temperature & Condition Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentForecast?.let { "${it.tempMax.toInt()}°" } ?: "29°",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 68.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "H: ${currentForecast?.tempMax?.toInt() ?: 29}°",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SeySunGold,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "L: ${currentForecast?.tempMin?.toInt() ?: 24}°",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SeySkyBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        currentForecast?.conditionIconUrl?.let { iconUrl ->
                            AsyncImage(
                                model = iconUrl,
                                contentDescription = currentForecast.conditionLabel,
                                modifier = Modifier.size(68.dp)
                            )
                        }
                        Text(
                            text = currentForecast?.conditionLabel ?: "Passing Showers",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Key Metric Chips Grid (Wind, Rain, Sea State)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeatherMetricChip(
                        icon = Icons.Default.Air,
                        title = "Wind",
                        value = currentForecast?.wind ?: "SE 20-35 km/h",
                        modifier = Modifier.weight(1f)
                    )
                    WeatherMetricChip(
                        icon = Icons.Default.WaterDrop,
                        title = "Rain Chance",
                        value = currentForecast?.rainChance ?: "High",
                        modifier = Modifier.weight(1f)
                    )
                    WeatherMetricChip(
                        icon = Icons.Default.Waves,
                        title = "Sea State",
                        value = currentForecast?.seaState ?: "Mod/Rough",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherMetricChip(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0x22FFFFFF)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SeySkyBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xCCFFFFFF),
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
