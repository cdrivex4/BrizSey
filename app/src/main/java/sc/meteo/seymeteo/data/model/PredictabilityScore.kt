package sc.meteo.seymeteo.data.model

import androidx.compose.ui.graphics.Color

enum class PredictabilityLevel(
    val label: String,
    val color: Color
) {
    HIGH("Atmosphere Highly Stable", Color(0xFF10B981)),      // Emerald Green
    MODERATE("Moderate Variability", Color(0xFFF59E0B)),     // Amber Gold
    UNSTABLE("Atmospheric Instability", Color(0xFFEF4444))   // Coral Red
}

data class PredictabilityAssessment(
    val score: Int,                                // 0 to 100
    val level: PredictabilityLevel,
    val headline: String,
    val reassuranceText: String,
    val volatilityFactor: String,
    val calculatedAtMs: Long = System.currentTimeMillis()
) {
    companion object {
        fun evaluate(
            forecasts: List<DailyForecastItem>,
            alerts: List<CapAlertInfo>
        ): PredictabilityAssessment {
            if (forecasts.isEmpty()) {
                return PredictabilityAssessment(
                    score = 75,
                    level = PredictabilityLevel.MODERATE,
                    headline = "Synchronizing Telemetry",
                    reassuranceText = "Awaiting full model consensus. Standard maritime trade wind patterns expected.",
                    volatilityFactor = "Model initialization in progress"
                )
            }

            // 1. Alert Penalty
            var alertPenalty = 0
            if (alerts.isNotEmpty()) {
                alertPenalty = when {
                    alerts.any { it.isExtreme } -> 60
                    alerts.any { it.isSevere } -> 40
                    else -> 20
                }
            }

            // 2. Rain Probability Volatility
            val todayForecast = forecasts.firstOrNull()
            val rainProbInt = todayForecast?.rainChance?.replace("%", "")?.trim()?.toIntOrNull() ?: 30
            val rainPenalty = when {
                rainProbInt >= 80 -> 25
                rainProbInt >= 50 -> 15
                rainProbInt >= 30 -> 5
                else -> 0
            }

            // 3. Wind Gust & Convective Factor
            val windStr = todayForecast?.wind ?: ""
            val hasHighGusts = windStr.contains("Max", ignoreCase = true) || windStr.contains("40") || windStr.contains("50") || windStr.contains("55")
            val windPenalty = if (hasHighGusts) 15 else 0

            // 4. Condition Volatility
            val conditionStr = todayForecast?.conditionLabel?.lowercase() ?: ""
            val conditionPenalty = when {
                conditionStr.contains("thunder") || conditionStr.contains("squall") -> 25
                conditionStr.contains("heavy") -> 15
                conditionStr.contains("shower") || conditionStr.contains("rain") -> 8
                else -> 0
            }

            // Final Composite Score (0 - 100)
            val totalPenalty = alertPenalty + rainPenalty + windPenalty + conditionPenalty
            val score = (100 - totalPenalty).coerceIn(15, 98)

            val level = when {
                score >= 80 -> PredictabilityLevel.HIGH
                score >= 50 -> PredictabilityLevel.MODERATE
                else -> PredictabilityLevel.UNSTABLE
            }

            val headline = when (level) {
                PredictabilityLevel.HIGH -> "Plans Are Secure Today"
                PredictabilityLevel.MODERATE -> "Favorable With Passing Showers"
                PredictabilityLevel.UNSTABLE -> "Convective Weather Alert"
            }

            val reassurance = when (level) {
                PredictabilityLevel.HIGH -> "High atmospheric stability across the granitic islands. Marine conditions and outdoor activities have very high predictability."
                PredictabilityLevel.MODERATE -> "Trade wind convection may bring localized passing showers over coastal and high-elevation ridges, but conditions remain broadly predictable."
                PredictabilityLevel.UNSTABLE -> "Active atmospheric convergence detected. Rapid shower development and sudden wind shifts possible over Morne Seychellois and sea channels."
            }

            val volatility = when {
                alerts.isNotEmpty() -> "Active official bulletin: ${alerts.first().headline ?: alerts.first().event ?: "Advisory"}"
                hasHighGusts -> "Wind gust variability up to ${windStr.takeLast(15)}"
                rainProbInt >= 50 -> "High precipitation probability (${rainProbInt}%)"
                else -> "Low thermal and precipitation variance"
            }

            return PredictabilityAssessment(
                score = score,
                level = level,
                headline = headline,
                reassuranceText = reassurance,
                volatilityFactor = volatility
            )
        }
    }
}
