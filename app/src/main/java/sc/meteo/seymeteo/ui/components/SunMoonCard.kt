package sc.meteo.seymeteo.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sc.meteo.seymeteo.data.model.SunMoonInfo
import sc.meteo.seymeteo.ui.theme.SeyAlertOrange
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeySunGold
import sc.meteo.seymeteo.ui.theme.SeySurfaceCard
import sc.meteo.seymeteo.ui.theme.SeyTextMuted
import sc.meteo.seymeteo.ui.theme.SeyTextPrimary
import sc.meteo.seymeteo.ui.theme.SeyTextSecondary

@Composable
fun SunMoonCard(
    sunMoonInfo: SunMoonInfo,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Brightness5,
                        contentDescription = null,
                        tint = SeySunGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sun & Moon Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        color = SeyTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "UV Index: ${sunMoonInfo.uvIndexMax} (Extreme)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = SeyAlertOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sun Section
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFFBEB)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = SeySunGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sun",
                                style = MaterialTheme.typography.titleMedium,
                                color = SeyNavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SunMoonRow(label = "Sunrise", value = sunMoonInfo.sunriseTime)
                        SunMoonRow(label = "Sunset", value = sunMoonInfo.sunsetTime)
                        SunMoonRow(label = "Dawn / Dusk", value = "${sunMoonInfo.dawnTime} / ${sunMoonInfo.duskTime}")
                    }
                }

                // Moon Section
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = SeyNavyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Moon",
                                style = MaterialTheme.typography.titleMedium,
                                color = SeyNavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SunMoonRow(label = "Phase", value = sunMoonInfo.moonPhaseName)
                        SunMoonRow(label = "Moonrise", value = sunMoonInfo.moonriseTime)
                        SunMoonRow(label = "Moonset", value = sunMoonInfo.moonsetTime)
                    }
                }
            }
        }
    }
}

@Composable
fun SunMoonRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SeyTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = SeyTextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
