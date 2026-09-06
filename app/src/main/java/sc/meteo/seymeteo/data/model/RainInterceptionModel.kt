package sc.meteo.seymeteo.data.model

import sc.meteo.seymeteo.data.location.GpsLocation
import kotlin.math.*

/**
 * 2D Meteorological Velocity Vector (Speed in km/h, Bearing in degrees from true North [0..360]).
 */
data class VelocityVector(
    val speedKmh: Double,
    val bearingDeg: Double
) {
    // Meteorological coordinate convention: 0° is North (+Y), 90° is East (+X), 180° is South (-Y), 270° is West (-X)
    val vx: Double get() = speedKmh * sin(Math.toRadians(bearingDeg))
    val vy: Double get() = speedKmh * cos(Math.toRadians(bearingDeg))

    operator fun plus(other: VelocityVector): VelocityVector {
        val newVx = this.vx + other.vx
        val newVy = this.vy + other.vy
        return fromComponents(newVx, newVy)
    }

    operator fun minus(other: VelocityVector): VelocityVector {
        val newVx = this.vx - other.vx
        val newVy = this.vy - other.vy
        return fromComponents(newVx, newVy)
    }

    fun dot(other: VelocityVector): Double {
        return this.vx * other.vx + this.vy * other.vy
    }

    companion object {
        val ZERO = VelocityVector(0.0, 0.0)

        fun fromComponents(vx: Double, vy: Double): VelocityVector {
            val speed = hypot(vx, vy)
            var bearing = Math.toDegrees(atan2(vx, vy))
            if (bearing < 0) bearing += 360.0
            return VelocityVector(speed, bearing)
        }
    }
}

/**
 * Real-time Kinematic State of the User.
 */
data class UserKinematics(
    val location: GpsLocation,
    val speedKmh: Double = 0.0,
    val bearingDeg: Double = 0.0,
    val isMoving: Boolean = false,
    val accuracyMeters: Float = 0f,
    val timestampMs: Long = System.currentTimeMillis()
) {
    val velocityVector: VelocityVector
        get() = if (isMoving && speedKmh > 1.5) VelocityVector(speedKmh, bearingDeg) else VelocityVector.ZERO
}

/**
 * Observed Precipitation Weather Front Boundary and Motion Vector.
 */
data class WeatherFront(
    val frontId: String,
    val name: String,
    val centerLocation: GpsLocation,
    val velocity: VelocityVector,
    val depthKm: Double = 12.0,
    val boundaryRadiusKm: Double = 18.0,
    val rainIntensityMmH: Double = 8.5,
    val timestampMs: Long = System.currentTimeMillis()
)

/**
 * Topographic Barrier representing Mahé's central granitic ridge.
 */
data class TopographicSpine(
    val name: String,
    val ridgePoints: List<GpsLocation>,
    val crestElevationMeters: Double = 905.0,
    val ridgeAxisDeg: Double = 330.0 // NNW to SSE orientation
) {
    companion object {
        val MAHE_GRANITIC_SPINE = TopographicSpine(
            name = "Mahé Central Granitic Spine (Morne Seychellois Ridge)",
            ridgePoints = listOf(
                GpsLocation(-4.6150, 55.4250), // Northern Ridge (Signal Hill)
                GpsLocation(-4.6433, 55.4383), // Morne Seychellois (905m Peak)
                GpsLocation(-4.6700, 55.4600), // Congo Rouge / Mont Sébert
                GpsLocation(-4.7100, 55.4900), // Montagne Posée Ridge
                GpsLocation(-4.7600, 55.5100)  // Southern Hills (Police Bay Ridge)
            ),
            crestElevationMeters = 905.0,
            ridgeAxisDeg = 330.0
        )
    }
}

/**
 * Classification of Nowcasting Scenario (Scenario A: Stationary, Scenario B: Dynamic Evasion).
 */
enum class InterceptionScenario {
    SCENARIO_A_STATIONARY,
    SCENARIO_B_DYNAMIC_EVASION
}

/**
 * Comprehensive Mathematical Interception & Avoidance Solution.
 */
data class InterceptionSolution(
    val scenario: InterceptionScenario,
    val timeToRainMinutes: Int?,          // null if evading / no arrival
    val rainDurationMinutes: Int?,        // duration while inside front envelope
    val closingSpeedKmh: Double,          // speed at which front and user are approaching
    val distanceToFrontKm: Double,        // spatial separation
    val isRainImminent: Boolean,          // true if arrival < 45 min
    val isEvadingSuccessfully: Boolean,   // true if user motion prevents front intersection
    val recommendedEscapeBearingDeg: Double?,
    val recommendedEscapeLabel: String?,  // e.g. "WNW (290°)"
    val recommendedSafeHaven: String?,    // e.g. "Beau Vallon Leeward Bay"
    val orographicUpliftFactor: Double,   // 1.35x for windward uplift, 0.50x for leeward shelter
    val orographicZoneLabel: String,      // "Highland Uplift Zone" vs "Leeward Rain Shadow"
    val summaryHeadline: String,
    val detailedDescription: String,
    val frontVector: VelocityVector,
    val userVector: VelocityVector,
    val relativeVector: VelocityVector
)
