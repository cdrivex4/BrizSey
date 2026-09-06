package sc.meteo.seymeteo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sc.meteo.seymeteo.data.model.InterceptionScenario
import sc.meteo.seymeteo.data.model.InterceptionSolution
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
import kotlin.math.roundToInt

@Composable
fun RainInterceptionCard(
    solution: InterceptionSolution?,
    glassOpacity: Float,
    modifier: Modifier = Modifier
) {
    if (solution == null) return

    val isUserMoving = solution.scenario == InterceptionScenario.SCENARIO_B_DYNAMIC_EVASION || solution.userVector.speedKmh >= 2.0

    val borderColor = if (solution.isRainImminent && !solution.isEvadingSuccessfully) {
        Color(0xFFF59E0B).copy(alpha = (glassOpacity * 0.9f + 0.15f).coerceIn(0.2f, 0.6f))
    } else {
        Color(0xFF38BDF8).copy(alpha = (glassOpacity * 0.8f + 0.1f).coerceIn(0.15f, 0.45f))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = Color(0xFF0F2B48).copy(alpha = glassOpacity),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Header row: Title + Live Motion Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = SeyOceanCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nowcasting Advection Engine",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Dynamic Status Badge automatically resolved from kinematics
                val (badgeText, badgeBg, badgeColor) = when {
                    solution.isEvadingSuccessfully && isUserMoving -> Triple(
                        "🛡️ Evading Front",
                        Color(0xFF10B981).copy(alpha = 0.25f),
                        Color(0xFF34D399)
                    )
                    isUserMoving && solution.userVector.speedKmh < 10.0 -> Triple(
                        "🚶 Walking · ${solution.userVector.speedKmh.roundToInt()} km/h",
                        Color(0xFF0284C7).copy(alpha = 0.25f),
                        Color(0xFF38BDF8)
                    )
                    isUserMoving -> Triple(
                        "🚗 In Motion · ${solution.userVector.speedKmh.roundToInt()} km/h",
                        Color(0xFF0284C7).copy(alpha = 0.25f),
                        Color(0xFF38BDF8)
                    )
                    solution.isRainImminent -> Triple(
                        "🌧️ Rain Imminent",
                        Color(0xFFF59E0B).copy(alpha = 0.25f),
                        Color(0xFFFBBF24)
                    )
                    else -> Triple(
                        "📍 Stationary",
                        Color(0xFF64748B).copy(alpha = 0.25f),
                        Color(0xFF94A3B8)
                    )
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Hero Status Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF071B2F).copy(alpha = (glassOpacity * 0.9f).coerceIn(0.2f, 0.7f)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Icon with Compass / Evasion Ring
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (solution.isEvadingSuccessfully) Color(0xFF10B981).copy(alpha = 0.2f)
                                else Color(0xFF0284C7).copy(alpha = 0.25f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when {
                            solution.isEvadingSuccessfully -> Icons.Default.Shield
                            solution.timeToRainMinutes != null -> Icons.Default.WaterDrop
                            else -> Icons.Default.Air
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (solution.isEvadingSuccessfully) Color(0xFF34D399) else Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = solution.summaryHeadline,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = solution.detailedDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Telemetry Matrix (Closing Velocity, Distance, Safe Haven, Mountain Orographic Uplift)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Closing Speed
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF071B2F).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "CLOSING RATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${solution.closingSpeedKmh.roundToInt()} km/h",
                            style = MaterialTheme.typography.titleSmall,
                            color = SeyOceanCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Metric 2: Distance to Boundary
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF071B2F).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "FRONT DISTANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${String.format("%.1f", solution.distanceToFrontKm)} km",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Metric 3: Orographic Factor
                Surface(
                    modifier = Modifier.weight(1.2f),
                    color = Color(0xFF071B2F).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "RIDGE SHIELD",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${String.format("%.2f", solution.orographicUpliftFactor)}x ${if (solution.orographicUpliftFactor > 1.0) "Uplift" else "Shadow"}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (solution.orographicUpliftFactor > 1.0) Color(0xFFFBBF24) else Color(0xFF34D399),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 4. Safe-Haven & Evasion Vector Guidance Banner
            if (solution.recommendedSafeHaven != null || solution.recommendedEscapeLabel != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0A223D).copy(alpha = 0.65f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dynamic Evasion Bearing Arrow
                        solution.recommendedEscapeBearingDeg?.let { bearing ->
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(bearing.toFloat())
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Column {
                            Text(
                                text = "Optimal Evasion Heading: ${solution.recommendedEscapeLabel ?: "Maintain Bearing"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            if (solution.recommendedSafeHaven != null) {
                                Text(
                                    text = "Safe Haven: ${solution.recommendedSafeHaven}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Live GPS Kinematics Telemetry Strip (Direct Hardware GPS Feed)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF071B2F).copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = if (isUserMoving) Color(0xFF34D399) else Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUserMoving) {
                                "GPS: ${solution.userVector.speedKmh.roundToInt()} km/h · Heading ${solution.userVector.bearingDeg.roundToInt()}° (${getBearingLabel(solution.userVector.bearingDeg.toFloat())})"
                            } else {
                                "GPS: 0 km/h (Stationary Observer)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "🛰️ Live GPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

private fun getBearingLabel(deg: Float): String {
    val index = (((deg + 22.5f) % 360f) / 45f).toInt()
    return when (index) {
        0 -> "North"
        1 -> "North-East"
        2 -> "East"
        3 -> "South-East"
        4 -> "South"
        5 -> "South-West"
        6 -> "West"
        7 -> "North-West"
        else -> "North"
    }
}
