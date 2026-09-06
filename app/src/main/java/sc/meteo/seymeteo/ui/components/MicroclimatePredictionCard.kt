package sc.meteo.seymeteo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sc.meteo.seymeteo.data.model.CoastalZone
import sc.meteo.seymeteo.data.model.DistrictMicroclimate
import sc.meteo.seymeteo.data.model.IslandMicroclimatePrediction
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
import kotlin.math.roundToInt

@Composable
fun MicroclimatePredictionCard(
    prediction: IslandMicroclimatePrediction?,
    glassOpacity: Float,
    modifier: Modifier = Modifier
) {
    if (prediction == null || prediction.districts.isEmpty()) return

    var selectedDistrictId by remember { mutableStateOf(prediction.districts.first().id) }
    val selectedDistrict = prediction.districts.firstOrNull { it.id == selectedDistrictId } ?: prediction.districts.first()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = Color(0xFF0F2B48).copy(alpha = glassOpacity),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = (glassOpacity * 0.8f + 0.1f).coerceIn(0.15f, 0.45f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Header: Title + DEM Resolution Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terrain,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Topographic Microclimate & Beach Guide",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    color = Color(0xFFFBBF24).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "ASTER 30m DEM",
                        color = Color(0xFFFBBF24),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Thermodynamic Cloud Base & Ridge Penetration Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF071B2F).copy(alpha = (glassOpacity * 0.9f).coerceIn(0.2f, 0.7f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0284C7).copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = SeyOceanCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LCL Cloud Base: ${prediction.liftingCondensationLevelMeters.roundToInt()}m",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = " · Ridge Peak: 905m",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (prediction.isOrographicCondensationTriggered)
                                "⛰️ 905m Granitic Spine penetrates condensation level -> Forced Orographic Rain Active"
                            else "Clear boundary layer across island ridge",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Horizontal District / Beach Selector Tabs
            Text(
                text = "SELECT DISTRICT / COASTAL BAY",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                prediction.districts.forEach { district ->
                    val isSelected = district.id == selectedDistrictId
                    val isCalm = district.zone == CoastalZone.LEEWARD_SHELTERED
                    val chipBg = if (isSelected) SeyOceanCyan.copy(alpha = 0.3f) else Color(0xFF071B2F).copy(alpha = 0.45f)
                    val chipBorder = if (isSelected) SeyOceanCyan else Color.White.copy(alpha = 0.15f)

                    Surface(
                        onClick = { selectedDistrictId = district.id },
                        color = chipBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, chipBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCalm) "🟢 " else if (district.zone == CoastalZone.WINDWARD_EXPOSED) "🔴 " else "⛰️ ",
                                fontSize = 10.sp
                            )
                            Text(
                                text = district.name,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Selected District Detail Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF071B2F).copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedDistrict.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${selectedDistrict.region} · Elevation ${selectedDistrict.elevationMeters.roundToInt()}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = SeyOceanCyan,
                                fontSize = 11.sp
                            )
                        }

                        // Swimming Badge
                        Surface(
                            color = if (selectedDistrict.isSwimmingSafe) Color(0xFF10B981).copy(alpha = 0.25f)
                            else Color(0xFFEF4444).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (selectedDistrict.isSwimmingSafe) Color(0xFF34D399) else Color(0xFFF87171))
                        ) {
                            Text(
                                text = if (selectedDistrict.isSwimmingSafe) "🟢 Safe Swimming" else "🔴 Rough / Caution",
                                color = if (selectedDistrict.isSwimmingSafe) Color(0xFF34D399) else Color(0xFFF87171),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Telemetry Grid: Rain Probability, Sea Condition, Wave Height
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF0A223D).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("RAIN RISK", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                Text("${selectedDistrict.rainProbabilityPct}%", style = MaterialTheme.typography.titleSmall, color = if (selectedDistrict.rainProbabilityPct < 30) Color(0xFF34D399) else Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1.3f),
                            color = Color(0xFF0A223D).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("SEA CONDITION", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                Text(selectedDistrict.seaCondition, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF0A223D).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("WAVE SWELL", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                Text("${String.format("%.1f", selectedDistrict.waveHeightM)}m", style = MaterialTheme.typography.titleSmall, color = SeyOceanCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Natural Language Recommendation
                    Text(
                        text = selectedDistrict.recommendationSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
