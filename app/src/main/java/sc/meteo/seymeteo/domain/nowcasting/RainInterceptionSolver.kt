package sc.meteo.seymeteo.domain.nowcasting

import sc.meteo.seymeteo.data.location.GpsLocation
import sc.meteo.seymeteo.data.location.haversineDistanceKm
import sc.meteo.seymeteo.data.model.*
import kotlin.math.*

/**
 * Solves Scenario A (Stationary User Ray-Casting) and Scenario B (Dynamic Kinematic Interception/Evasion),
 * incorporating the Mahé Central Granitic Mountain Spine orographic uplift and rain shadow shield.
 */
class RainInterceptionSolver(
    private val spine: TopographicSpine = TopographicSpine.MAHE_GRANITIC_SPINE
) {

    /**
     * Compute comprehensive interception solution for a user given their kinematics and approaching front.
     */
    fun solveInterception(
        user: UserKinematics,
        front: WeatherFront
    ): InterceptionSolution {
        val scenario = if (user.isMoving && user.speedKmh >= 2.0) {
            InterceptionScenario.SCENARIO_B_DYNAMIC_EVASION
        } else {
            InterceptionScenario.SCENARIO_A_STATIONARY
        }

        val distanceToCenter = haversineDistanceKm(
            user.location.latitude,
            user.location.longitude,
            front.centerLocation.latitude,
            front.centerLocation.longitude
        )

        val distanceToBoundary = max(0.0, distanceToCenter - front.boundaryRadiusKm)
        val isInsideFront = distanceToCenter <= front.boundaryRadiusKm

        // Line-of-sight unit vector from front center towards user
        val losBearing = calculateBearingDeg(
            front.centerLocation.latitude,
            front.centerLocation.longitude,
            user.location.latitude,
            user.location.longitude
        )
        val losUnitVector = VelocityVector(1.0, losBearing)

        // Relative velocity vector v_rel = v_front - v_user
        val userVector = user.velocityVector
        val frontVector = front.velocity
        val relativeVector = frontVector - userVector

        // Closing velocity along line-of-sight: positive means closing distance
        val closingSpeedKmh = relativeVector.vx * losUnitVector.vx + relativeVector.vy * losUnitVector.vy

        // Calculate Orographic Uplift & Rain Shadow Factor based on Mahé Granitic Spine
        val orographic = calculateOrographicInfluence(user.location, frontVector)

        return if (scenario == InterceptionScenario.SCENARIO_A_STATIONARY) {
            solveScenarioA(
                distanceToBoundary = distanceToBoundary,
                distanceToCenter = distanceToCenter,
                isInsideFront = isInsideFront,
                front = front,
                losUnitVector = losUnitVector,
                orographic = orographic,
                userVector = userVector,
                relativeVector = relativeVector
            )
        } else {
            solveScenarioB(
                user = user,
                distanceToBoundary = distanceToBoundary,
                distanceToCenter = distanceToCenter,
                isInsideFront = isInsideFront,
                front = front,
                closingSpeedKmh = closingSpeedKmh,
                orographic = orographic,
                userVector = userVector,
                relativeVector = relativeVector
            )
        }
    }

    /**
     * Scenario A: Stationary observer. Standard radar ray-cast arrival window.
     */
    private fun solveScenarioA(
        distanceToBoundary: Double,
        distanceToCenter: Double,
        isInsideFront: Boolean,
        front: WeatherFront,
        losUnitVector: VelocityVector,
        orographic: OrographicResult,
        userVector: VelocityVector,
        relativeVector: VelocityVector
    ): InterceptionSolution {
        val approachSpeedKmh = front.velocity.vx * losUnitVector.vx + front.velocity.vy * losUnitVector.vy

        val (timeToRainMinutes, rainDurationMinutes, isEvading) = when {
            isInsideFront -> {
                val remainingKm = front.boundaryRadiusKm - distanceToCenter + front.depthKm
                val duration = if (front.velocity.speedKmh > 1.0) {
                    ((remainingKm / front.velocity.speedKmh) * 60.0).roundToInt().coerceAtLeast(10)
                } else 45
                Triple(0, duration, false)
            }
            approachSpeedKmh > 1.0 -> {
                val etaMin = ((distanceToBoundary / approachSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)
                val durationMin = (((front.boundaryRadiusKm * 2.0 + front.depthKm) / front.velocity.speedKmh) * 60.0).roundToInt()
                Triple(etaMin, durationMin.coerceIn(15, 120), false)
            }
            else -> {
                // Front is passing laterally or moving away
                Triple(null, null, true)
            }
        }

        val isImminent = timeToRainMinutes != null && timeToRainMinutes <= 45

        val (recBearing, recLabel, safeHaven) = recommendAvoidanceRoute(front.velocity.bearingDeg, orographic)

        val headline = when {
            isInsideFront -> "Active Rain Cell Overhead"
            timeToRainMinutes != null -> "Rain Front Arrival in $timeToRainMinutes min"
            else -> "Front Passing Clear (No Direct Impact)"
        }

        val description = when {
            isInsideFront -> "Stationary location is currently inside the rain core. Expected clear window in ~$rainDurationMinutes min. ${orographic.zoneLabel}."
            timeToRainMinutes != null -> "Stationary monitoring: Front closing at ${String.format("%.1f", max(0.0, approachSpeedKmh))} km/h. Arrival window estimated in $timeToRainMinutes min (${distanceToBoundary.roundToInt()} km away). ${orographic.zoneLabel}."
            else -> "Front trajectory is tracking away from your current stationary position. Stable conditions."
        }

        return InterceptionSolution(
            scenario = InterceptionScenario.SCENARIO_A_STATIONARY,
            timeToRainMinutes = timeToRainMinutes,
            rainDurationMinutes = rainDurationMinutes,
            closingSpeedKmh = max(0.0, approachSpeedKmh),
            distanceToFrontKm = distanceToBoundary,
            isRainImminent = isImminent,
            isEvadingSuccessfully = isEvading,
            recommendedEscapeBearingDeg = recBearing,
            recommendedEscapeLabel = recLabel,
            recommendedSafeHaven = safeHaven,
            orographicUpliftFactor = orographic.upliftFactor,
            orographicZoneLabel = orographic.zoneLabel,
            summaryHeadline = headline,
            detailedDescription = description,
            frontVector = front.velocity,
            userVector = userVector,
            relativeVector = relativeVector
        )
    }

    /**
     * Scenario B: Dynamic Moving User. Relative kinematic vector solving & evasion optimization.
     */
    private fun solveScenarioB(
        user: UserKinematics,
        distanceToBoundary: Double,
        distanceToCenter: Double,
        isInsideFront: Boolean,
        front: WeatherFront,
        closingSpeedKmh: Double,
        orographic: OrographicResult,
        userVector: VelocityVector,
        relativeVector: VelocityVector
    ): InterceptionSolution {
        val (timeToRainMinutes, rainDurationMinutes, isEvading) = when {
            isInsideFront -> {
                // Moving inside front: calculate time to exit front boundary along relative vector
                val relSpeed = max(relativeVector.speedKmh, 5.0)
                val exitDistKm = front.boundaryRadiusKm - distanceToCenter + 4.0
                val exitMins = ((exitDistKm / relSpeed) * 60.0).roundToInt().coerceAtLeast(5)
                Triple(0, exitMins, false)
            }
            closingSpeedKmh > 1.5 -> {
                // Relative velocity is closing the gap
                val etaMin = ((distanceToBoundary / closingSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)
                val durationMin = (((front.boundaryRadiusKm * 1.5) / max(relativeVector.speedKmh, 10.0)) * 60.0).roundToInt()
                Triple(etaMin, durationMin.coerceIn(10, 90), false)
            }
            else -> {
                // User is successfully evading, outpacing, or moving away from front
                Triple(null, null, true)
            }
        }

        val isImminent = timeToRainMinutes != null && timeToRainMinutes <= 45
        val (recBearing, recLabel, safeHaven) = recommendAvoidanceRoute(front.velocity.bearingDeg, orographic)

        val headline = when {
            isInsideFront -> "Interception Active · Evacuating Cell"
            isEvading -> "Dry Path Maintained · Evading Front"
            timeToRainMinutes != null -> "Dynamic Interception in $timeToRainMinutes min"
            else -> "Dry Corridor Clear"
        }

        val description = when {
            isInsideFront -> "Moving at ${user.speedKmh.roundToInt()} km/h (${user.bearingDeg.roundToInt()}°). Exiting rain boundary in ~$rainDurationMinutes min. Heading toward $safeHaven advised."
            isEvading -> "User speed (${user.speedKmh.roundToInt()} km/h @ ${user.bearingDeg.roundToInt()}°) is outpacing or flanking the front. Rain avoided! ${orographic.zoneLabel}."
            timeToRainMinutes != null -> "Kinematic closing rate: ${String.format("%.1f", closingSpeedKmh)} km/h. Intersection path confirmed in $timeToRainMinutes min. Recommend adjust heading to $recLabel toward $safeHaven."
            else -> "Kinematic trajectory maintains safe clearance from precipitation envelope."
        }

        return InterceptionSolution(
            scenario = InterceptionScenario.SCENARIO_B_DYNAMIC_EVASION,
            timeToRainMinutes = timeToRainMinutes,
            rainDurationMinutes = rainDurationMinutes,
            closingSpeedKmh = max(0.0, closingSpeedKmh),
            distanceToFrontKm = distanceToBoundary,
            isRainImminent = isImminent,
            isEvadingSuccessfully = isEvading,
            recommendedEscapeBearingDeg = recBearing,
            recommendedEscapeLabel = recLabel,
            recommendedSafeHaven = safeHaven,
            orographicUpliftFactor = orographic.upliftFactor,
            orographicZoneLabel = orographic.zoneLabel,
            summaryHeadline = headline,
            detailedDescription = description,
            frontVector = front.velocity,
            userVector = userVector,
            relativeVector = relativeVector
        )
    }

    /**
     * Calculates the Granitic Mountain Spine Orographic Uplift factor and rain shadow effect.
     */
    fun calculateOrographicInfluence(
        userLoc: GpsLocation,
        frontVector: VelocityVector
    ): OrographicResult {
        // Average longitude of Mahé central spine ~ 55.45°E
        val spineLon = 55.450
        val isUserOnEastCoast = userLoc.longitude >= spineLon

        // Is the weather front approaching from East/South-East (Trade Winds 45° - 180°)?
        val isTradeWindFromEast = frontVector.bearingDeg in 225.0..360.0 || frontVector.bearingDeg in 0.0..45.0
        // (Note: Bearing towards NW (315°) means wind is FROM SE (135°))

        return if (isTradeWindFromEast) {
            if (isUserOnEastCoast) {
                OrographicResult(
                    upliftFactor = 1.35,
                    isWindward = true,
                    zoneLabel = "Windward Granitic Slopes (+35% Orographic Uplift)",
                    leewardSafeHaven = "Beau Vallon / Bel Ombre (Leeward Rain Shadow)"
                )
            } else {
                OrographicResult(
                    upliftFactor = 0.50,
                    isWindward = false,
                    zoneLabel = "Leeward Sheltered Bay (-50% Rain Shadow Shield)",
                    leewardSafeHaven = "North-West Coast / Beau Vallon Bay"
                )
            }
        } else {
            // Northwest Monsoon flow (from NW towards SE)
            if (!isUserOnEastCoast) {
                OrographicResult(
                    upliftFactor = 1.30,
                    isWindward = true,
                    zoneLabel = "NW Windward Exposure (+30% Uplift)",
                    leewardSafeHaven = "Victoria Port / Eden Island (East Coast Shadow)"
                )
            } else {
                OrographicResult(
                    upliftFactor = 0.55,
                    isWindward = false,
                    zoneLabel = "East Coast Rain Shadow (-45% Attenuation)",
                    leewardSafeHaven = "Victoria Harbour / Anse Royale"
                )
            }
        }
    }

    /**
     * Determines optimal evasion vector and designated leeward safe haven.
     */
    private fun recommendAvoidanceRoute(
        frontBearingTowardsDeg: Double,
        orographic: OrographicResult
    ): Triple<Double, String, String> {
        // Best escape heading is perpendicular to the oncoming front towards the leeward shelter
        val escapeBearing = (frontBearingTowardsDeg + 90.0) % 360.0
        val label = getCompassDirectionLabel(escapeBearing)
        return Triple(escapeBearing, "$label (${escapeBearing.roundToInt()}°)", orographic.leewardSafeHaven)
    }

    private fun calculateBearingDeg(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

        var bearing = Math.toDegrees(atan2(y, x))
        if (bearing < 0) bearing += 360.0
        return bearing
    }

    private fun getCompassDirectionLabel(bearingDeg: Double): String {
        val index = (((bearingDeg + 22.5) % 360) / 45.0).toInt()
        return when (index) {
            0 -> "North"
            1 -> "North-East"
            2 -> "East"
            3 -> "South-East"
            4 -> "South"
            5 -> "South-West"
            6 -> "West"
            7 -> "North-West"
            else -> "West-North-West"
        }
    }
}

data class OrographicResult(
    val upliftFactor: Double,
    val isWindward: Boolean,
    val zoneLabel: String,
    val leewardSafeHaven: String
)
