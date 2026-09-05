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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SatelliteAlt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sc.meteo.seymeteo.ui.theme.SeyNavyDark
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
import sc.meteo.seymeteo.ui.theme.SeySkyBlue
import sc.meteo.seymeteo.ui.theme.SeySurfaceCard
import sc.meteo.seymeteo.ui.theme.SeyTextMuted
import sc.meteo.seymeteo.ui.theme.SeyTextPrimary
import sc.meteo.seymeteo.ui.theme.SeyTextSecondary

@Composable
fun SatelliteRadarCard(
    modifier: Modifier = Modifier,
    onOpenRadar: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SeySurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onOpenRadar
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
                        imageVector = Icons.Default.SatelliteAlt,
                        contentDescription = null,
                        tint = SeyOceanCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Indian Ocean Satellite Radar",
                        style = MaterialTheme.typography.titleLarge,
                        color = SeyTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "EUMETSAT LIVE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Radar Visual Banner Mock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(SeyNavyDark, Color(0xFF0F3E6D), SeyOceanCyan)
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "High-Resolution Cloud & Tropical Storm Monitor",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Multi-spectral RGB Natural Color & Infrared feeds covering Seychelles Archipelago",
                        style = MaterialTheme.typography.labelSmall,
                        color = SeySkyBlue,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updates every 15 minutes from SMA geoserver",
                    style = MaterialTheme.typography.labelSmall,
                    color = SeyTextMuted
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View Map",
                        style = MaterialTheme.typography.labelSmall,
                        color = SeyOceanCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = SeyOceanCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
