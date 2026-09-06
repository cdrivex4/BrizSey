package sc.meteo.seymeteo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sc.meteo.seymeteo.data.model.PredictabilityAssessment
import sc.meteo.seymeteo.data.model.PredictabilityLevel
import sc.meteo.seymeteo.ui.theme.SeyNavyDark
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
import sc.meteo.seymeteo.ui.theme.SeySkyBlue
import sc.meteo.seymeteo.ui.theme.SeySurfaceCard
import sc.meteo.seymeteo.ui.theme.SeyTextPrimary
import sc.meteo.seymeteo.ui.theme.SeyTextSecondary

@Composable
fun PredictabilityCard(
    assessment: PredictabilityAssessment,
    glassOpacity: Float = 0.35f,
    modifier: Modifier = Modifier
) {
    val progressAnimation by animateFloatAsState(
        targetValue = assessment.score / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "predictability_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B48).copy(alpha = glassOpacity)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = (glassOpacity * 0.8f + 0.1f).coerceIn(0.15f, 0.45f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with Score Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when (assessment.level) {
                                    PredictabilityLevel.HIGH -> Color(0x3310B981)
                                    PredictabilityLevel.MODERATE -> Color(0x33FBBF24)
                                    PredictabilityLevel.UNSTABLE -> Color(0x33EF4444)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (assessment.level) {
                                PredictabilityLevel.HIGH -> Icons.Default.Shield
                                PredictabilityLevel.MODERATE -> Icons.Default.Info
                                PredictabilityLevel.UNSTABLE -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = assessment.level.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Atmospheric Stability",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = assessment.level.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = assessment.level.color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Numerical Badge
                Surface(
                    color = assessment.level.color.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, assessment.level.color.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${assessment.score}% Consensus",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smooth Consensus Progress Bar
            LinearProgressIndicator(
                progress = { progressAnimation },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = assessment.level.color,
                trackColor = Color(0x3394A3B8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reassurance Copy Grounded in Behavioral Research
            Text(
                text = assessment.headline,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = assessment.reassuranceText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Volatility Factors Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x2238BDF8))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Key Driver: ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SeySkyBlue
                )
                Text(
                    text = assessment.volatilityFactor,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE2E8F0)
                )
            }
        }
    }
}
