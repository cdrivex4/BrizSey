package sc.meteo.seymeteo.data.model

import sc.meteo.seymeteo.data.location.GpsLocation

/**
 * Coastal & Terrain Zone Classification.
 */
enum class CoastalZone {
    WINDWARD_EXPOSED,
    LEEWARD_SHELTERED,
    HIGH_MOUNTAIN_SPINE,
    FLANK_TRANSITION
}

/**
 * Predicted local microclimate and marine conditions for a specific district or beach.
 */
data class DistrictMicroclimate(
    val id: String,
    val name: String,
    val region: String, // e.g. "North-West Coast", "East Coast", "Central Highlands"
    val location: GpsLocation,
    val elevationMeters: Double,
    val zone: CoastalZone,
    val rainProbabilityPct: Int,
    val expectedRainIntensityMmH: Double,
    val temperatureC: Double,
    val isRainShadowActive: Boolean,
    val seaCondition: String,       // e.g. "Glassy / Calm", "Moderate Chop", "Rough Onshore Swell"
    val waveHeightM: Double,
    val isSwimmingSafe: Boolean,
    val recommendationSummary: String
)

/**
 * Island-Wide Thermodynamic & Topographic Microclimate Prediction Output.
 */
data class IslandMicroclimatePrediction(
    val timestampMs: Long = System.currentTimeMillis(),
    val ambientTempC: Double,
    val dewPointTempC: Double,
    val relativeHumidityPct: Int,
    val liftingCondensationLevelMeters: Double, // LCL cloud base height
    val ridgeCrestElevationMeters: Double = 905.0,
    val isOrographicCondensationTriggered: Boolean, // True if Ridge > LCL & Windward Lift > 0
    val prevailingWindSpeedKmh: Double,
    val prevailingWindDirectionLabel: String,
    val windwardCoastName: String,
    val leewardCoastName: String,
    val districts: List<DistrictMicroclimate>,
    val bestBeachesForSwimming: List<String>,
    val highRainRiskDistricts: List<String>
)
