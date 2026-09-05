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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsBoat
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sc.meteo.seymeteo.data.model.MarineTideData
import sc.meteo.seymeteo.data.model.TidePoint
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
import sc.meteo.seymeteo.ui.theme.SeySkyBlue
import sc.meteo.seymeteo.ui.theme.SeySurfaceCard
import sc.meteo.seymeteo.ui.theme.SeyTealLight
import sc.meteo.seymeteo.ui.theme.SeyTextMuted
import sc.meteo.seymeteo.ui.theme.SeyTextPrimary
import sc.meteo.seymeteo.ui.theme.SeyTextSecondary

@Composable
fun MarineTideCard(
    marineData: MarineTideData,
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
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = SeyOceanCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Marine & Tides",
                        style = MaterialTheme.typography.titleLarge,
                        color = SeyTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = SeyTealLight,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = marineData.monsoonSeason,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = SeyNavyPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sea condition & wave summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MarineInfoBox(
                    label = "Sea Condition",
                    value = marineData.seaCondition,
                    modifier = Modifier.weight(1f)
                )
                MarineInfoBox(
                    label = "Wave Height",
                    value = marineData.waveHeight,
                    modifier = Modifier.weight(1f)
                )
                MarineInfoBox(
                    label = "Water Temp",
                    value = marineData.waterTemperature,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Today's Tide Schedule (Victoria Port & Inner Islands)",
                style = MaterialTheme.typography.titleMedium,
                color = SeyTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Daily Tides Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                marineData.tides.forEach { tide ->
                    TideItemPill(tide = tide, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MarineInfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SeyTextMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = SeyTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TideItemPill(
    tide: TidePoint,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (tide.isHighTide) Color(0xFFE0F2FE) else Color(0xFFF1F5F9)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (tide.isHighTide) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (tide.isHighTide) SeyOceanCyan else SeyTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tide.time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SeyTextPrimary
            )
            Text(
                text = "${tide.heightMeters} m",
                style = MaterialTheme.typography.labelSmall,
                color = if (tide.isHighTide) SeyOceanCyan else SeyTextMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
            Text(
                text = if (tide.isHighTide) "High" else "Low",
                style = MaterialTheme.typography.labelSmall,
                color = SeyTextSecondary,
                fontSize = 9.sp
            )
        }
    }
}
