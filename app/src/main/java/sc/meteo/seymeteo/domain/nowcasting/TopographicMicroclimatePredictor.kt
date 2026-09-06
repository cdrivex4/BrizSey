package sc.meteo.seymeteo.domain.nowcasting

import sc.meteo.seymeteo.data.location.GpsLocation
import sc.meteo.seymeteo.data.model.*
import kotlin.math.*

/**
 * Thermodynamic & Topographic Microclimate Prediction Engine.
 * Ingests environmental temperature, humidity, wind direction/speed, and DEM elevation gradients
 * to calculate Lifting Condensation Level (LCL), local rain probability per coast, and beach sea calmness.
 */
class TopographicMicroclimatePredictor {

    /**
     * Compute comprehensive island microclimate and beach prediction.
     */
    fun predictIslandMicroclimate(
        currentForecast: DailyForecastItem?,
        ambientTempC: Double = 28.5,
        relativeHumidityPct: Int = 80
    ): IslandMicroclimatePrediction {
        // 1. Parse wind vector
        val windStr = currentForecast?.wind ?: "SE 22 km/h"
        val windSpeedKmh = Regex("\\d+").find(windStr)?.value?.toDoubleOrNull() ?: 22.0
        val windBearingFrom = parseWindBearingFrom(windStr)

        // 2. Compute Dew Point and Lifting Condensation Level (LCL Cloud Base)
        val dewPointC = calculateDewPoint(ambientTempC, relativeHumidityPct)
        val lclMeters = max(150.0, 125.0 * (ambientTempC - dewPointC))

        val ridgeCrestM = 905.0 // Morne Seychellois Peak
        val isCondensationTriggered = ridgeCrestM >= lclMeters

        val baseRainChance = currentForecast?.rainChance?.filter { it.isDigit() }?.toIntOrNull() ?: 45

        // 3. Evaluate each key district on Mahé
        val districtProfiles = getMaheDistrictProfiles()
        val districtPredictions = districtProfiles.map { profile ->
            evaluateDistrict(
                profile = profile,
                windBearingFrom = windBearingFrom,
                windSpeedKmh = windSpeedKmh,
                ambientTempC = ambientTempC,
                lclMeters = lclMeters,
                baseRainChance = baseRainChance
            )
        }

        val bestBeaches = districtPredictions
            .filter { it.isSwimmingSafe && it.seaCondition.contains("Calm", ignoreCase = true) }
            .map { "${it.name} (${it.region})" }

        val highRiskDistricts = districtPredictions
            .filter { it.rainProbabilityPct >= 65 }
            .map { "${it.name} (${it.rainProbabilityPct}%)" }

        val isTradeWindFromEast = windBearingFrom in 45.0..180.0
        val windwardCoast = if (isTradeWindFromEast) "East & South Coast (Pointe Larue to Anse Royale)" else "North-West & West Coast (Beau Vallon to Port Glaud)"
        val leewardCoast = if (isTradeWindFromEast) "North-West & West Coast (Beau Vallon, Bel Ombre, Port Glaud)" else "East & South-East Coast (Victoria, Eden Island, Anse Royale)"

        return IslandMicroclimatePrediction(
            ambientTempC = ambientTempC,
            dewPointTempC = dewPointC,
            relativeHumidityPct = relativeHumidityPct,
            liftingCondensationLevelMeters = lclMeters,
            ridgeCrestElevationMeters = ridgeCrestM,
            isOrographicCondensationTriggered = isCondensationTriggered,
            prevailingWindSpeedKmh = windSpeedKmh,
            prevailingWindDirectionLabel = getWindDirectionLabel(windBearingFrom),
            windwardCoastName = windwardCoast,
            leewardCoastName = leewardCoast,
            districts = districtPredictions,
            bestBeachesForSwimming = bestBeaches,
            highRainRiskDistricts = highRiskDistricts
        )
    }

    private fun evaluateDistrict(
        profile: DistrictProfile,
        windBearingFrom: Double,
        windSpeedKmh: Double,
        ambientTempC: Double,
        lclMeters: Double,
        baseRainChance: Int
    ): DistrictMicroclimate {
        // Angle between wind arrival and slope aspect
        val deltaAngleRad = Math.toRadians(windBearingFrom - profile.aspectDeg)
        val exposure = cos(deltaAngleRad) // +1 = Direct Windward, -1 = Direct Leeward Shadow

        val isHighland = profile.elevationMeters >= 400.0
        val isWindward = exposure > 0.2
        val isLeeward = exposure < -0.2

        val zone = when {
            isHighland -> CoastalZone.HIGH_MOUNTAIN_SPINE
            isWindward -> CoastalZone.WINDWARD_EXPOSED
            isLeeward -> CoastalZone.LEEWARD_SHELTERED
            else -> CoastalZone.FLANK_TRANSITION
        }

        // Thermodynamic lapse rate temperature adjustment (0.65°C per 100m)
        val localTempC = if (isLeeward && !isHighland) {
            ambientTempC + 1.2 // Föhn adiabatic compression warming
        } else {
            ambientTempC - (profile.elevationMeters * 0.0065)
        }

        // Rain Probability calculation
        val calculatedRainPct = when (zone) {
            CoastalZone.HIGH_MOUNTAIN_SPINE -> {
                val orographicBoost = if (profile.elevationMeters >= lclMeters) 35 else 20
                (baseRainChance + orographicBoost).coerceIn(40, 98)
            }
            CoastalZone.WINDWARD_EXPOSED -> {
                val boost = (exposure * 30.0).roundToInt()
                (baseRainChance + boost).coerceIn(30, 95)
            }
            CoastalZone.LEEWARD_SHELTERED -> {
                val reduction = (abs(exposure) * 40.0).roundToInt()
                (baseRainChance - reduction).coerceIn(8, 55)
            }
            CoastalZone.FLANK_TRANSITION -> {
                baseRainChance.coerceIn(20, 80)
            }
        }

        val rainIntensityMmH = (calculatedRainPct / 100.0) * (if (isWindward || isHighland) 14.0 else 4.0)

        // Sea State & Wave Height
        val (seaCondition, waveHeightM, isSwimmingSafe) = when (zone) {
            CoastalZone.LEEWARD_SHELTERED -> {
                Triple("Glassy / Calm", 0.3, true)
            }
            CoastalZone.WINDWARD_EXPOSED -> {
                val waveH = (windSpeedKmh / 35.0) * 2.4
                Triple("Rough Onshore Swell", max(1.5, waveH), false)
            }
            CoastalZone.FLANK_TRANSITION -> {
                Triple("Moderate Light Chop", 0.8, true)
            }
            CoastalZone.HIGH_MOUNTAIN_SPINE -> {
                Triple("Inland Highland", 0.0, false)
            }
        }

        val recommendation = when (zone) {
            CoastalZone.LEEWARD_SHELTERED -> "☀️ Leeward Rain Shadow active. Sea is flat and calm (< 0.4m), perfect for swimming and beach activities."
            CoastalZone.WINDWARD_EXPOSED -> "🌧️ Windward exposure. Orographic cloud uplift active with onshore wave chop (${String.format("%.1f", waveHeightM)}m). Swimming caution advised."
            CoastalZone.HIGH_MOUNTAIN_SPINE -> "⛰️ Mountain crest above LCL (${lclMeters.roundToInt()}m). Frequent cloud immersion, cool mist, and high rain rate."
            CoastalZone.FLANK_TRANSITION -> "⛅ Moderate coastal exposure. Mixed conditions with light ocean breeze."
        }

        return DistrictMicroclimate(
            id = profile.id,
            name = profile.name,
            region = profile.region,
            location = profile.location,
            elevationMeters = profile.elevationMeters,
            zone = zone,
            rainProbabilityPct = calculatedRainPct,
            expectedRainIntensityMmH = rainIntensityMmH,
            temperatureC = localTempC,
            isRainShadowActive = isLeeward,
            seaCondition = seaCondition,
            waveHeightM = waveHeightM,
            isSwimmingSafe = isSwimmingSafe,
            recommendationSummary = recommendation
        )
    }

    private fun calculateDewPoint(tempC: Double, rhPct: Int): Double {
        val a = 17.27
        val b = 237.7
        val alpha = ((a * tempC) / (b + tempC)) + ln(rhPct.coerceIn(1, 100) / 100.0)
        return (b * alpha) / (a - alpha)
    }

    private fun parseWindBearingFrom(windStr: String): Double {
        return when {
            windStr.contains("NW", ignoreCase = true) -> 315.0
            windStr.contains("SE", ignoreCase = true) -> 135.0
            windStr.contains("NE", ignoreCase = true) -> 45.0
            windStr.contains("SW", ignoreCase = true) -> 225.0
            windStr.contains("N", ignoreCase = true) -> 0.0
            windStr.contains("E", ignoreCase = true) -> 90.0
            windStr.contains("S", ignoreCase = true) -> 180.0
            windStr.contains("W", ignoreCase = true) -> 270.0
            else -> 135.0 // Southeast Trade Wind default
        }
    }

    private fun getWindDirectionLabel(bearingDeg: Double): String {
        val index = (((bearingDeg + 22.5) % 360) / 45.0).toInt()
        return when (index) {
            0 -> "North"
            1 -> "North-East"
            2 -> "East"
            3 -> "South-East (Alizés)"
            4 -> "South"
            5 -> "South-West"
            6 -> "West"
            7 -> "North-West (Monsoon)"
            else -> "South-East"
        }
    }

    private fun getMaheDistrictProfiles(): List<DistrictProfile> {
        return listOf(
            DistrictProfile(
                id = "beau_vallon",
                name = "Beau Vallon Bay",
                region = "North-West Coast",
                location = GpsLocation(-4.6136, 55.4297),
                elevationMeters = 8.0,
                aspectDeg = 315.0 // Facing NW
            ),
            DistrictProfile(
                id = "bel_ombre",
                name = "Bel Ombre & Anse Major",
                region = "North-West Coast",
                location = GpsLocation(-4.6190, 55.4120),
                elevationMeters = 15.0,
                aspectDeg = 300.0 // Facing WNW
            ),
            DistrictProfile(
                id = "victoria_port",
                name = "Victoria Port & Eden Island",
                region = "East Coast",
                location = GpsLocation(-4.6191, 55.4513),
                elevationMeters = 6.0,
                aspectDeg = 080.0 // Facing East
            ),
            DistrictProfile(
                id = "morne_summit",
                name = "Morne Seychellois Summit (905m)",
                region = "Central Mountain Spine",
                location = GpsLocation(-4.6433, 55.4383),
                elevationMeters = 905.0,
                aspectDeg = 135.0 // Central Ridge Crest
            ),
            DistrictProfile(
                id = "pointe_larue",
                name = "Pointe Larue & Airport",
                region = "East Coast",
                location = GpsLocation(-4.6743, 55.5212),
                elevationMeters = 5.0,
                aspectDeg = 110.0 // Facing ESE
            ),
            DistrictProfile(
                id = "port_glaud",
                name = "Port Glaud & Port Launay",
                region = "West Coast",
                location = GpsLocation(-4.6600, 55.4100),
                elevationMeters = 10.0,
                aspectDeg = 250.0 // Facing WSW
            ),
            DistrictProfile(
                id = "anse_royale",
                name = "Anse Royale & Anse Forbans",
                region = "South-East Coast",
                location = GpsLocation(-4.7431, 55.5186),
                elevationMeters = 12.0,
                aspectDeg = 135.0 // Facing SE
            )
        )
    }
}

data class DistrictProfile(
    val id: String,
    val name: String,
    val region: String,
    val location: GpsLocation,
    val elevationMeters: Double,
    val aspectDeg: Double // Slope facing direction in degrees [0..360]
)
