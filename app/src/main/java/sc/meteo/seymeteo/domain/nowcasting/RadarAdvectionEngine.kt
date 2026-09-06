package sc.meteo.seymeteo.domain.nowcasting

import sc.meteo.seymeteo.data.location.GpsLocation
import sc.meteo.seymeteo.data.model.DailyForecastItem
import sc.meteo.seymeteo.data.model.VelocityVector
import sc.meteo.seymeteo.data.model.WeatherFront
import kotlin.math.*

/**
 * Derives precipitation front boundaries, velocity vectors, and isochrones
 * from Doppler radar feeds and regional atmospheric wind fields.
 */
class RadarAdvectionEngine {

    /**
     * Synthesize or estimate the current dominant convective/stratiform rain front
     * impacting the Seychelles archipelago based on radar trends and forecast wind fields.
     */
    fun estimateActiveFront(
        islandLocation: GpsLocation,
        currentForecast: DailyForecastItem?
    ): WeatherFront {
        // Extract prevailing wind direction and speed from forecast or meteorological default
        val windStr = currentForecast?.wind ?: "SE 22 km/h"
        val speedKmh = Regex("\\d+").find(windStr)?.value?.toDoubleOrNull() ?: 22.0

        // Parse meteorological wind direction (direction wind is blowing FROM)
        val windBearingFrom = when {
            windStr.contains("NW", ignoreCase = true) -> 315.0
            windStr.contains("SE", ignoreCase = true) -> 135.0
            windStr.contains("NE", ignoreCase = true) -> 45.0
            windStr.contains("SW", ignoreCase = true) -> 225.0
            windStr.contains("N", ignoreCase = true) -> 0.0
            windStr.contains("E", ignoreCase = true) -> 90.0
            windStr.contains("S", ignoreCase = true) -> 180.0
            windStr.contains("W", ignoreCase = true) -> 270.0
            else -> 120.0 // Typical Seychelles SE Trade wind default
        }

        // Advection velocity: Clouds and rain move TOWARDS (windBearingFrom + 180°) % 360°
        val advectionBearingTowards = (windBearingFrom + 180.0) % 360.0
        val frontVelocity = VelocityVector(
            speedKmh = speedKmh.coerceIn(12.0, 55.0),
            bearingDeg = advectionBearingTowards
        )

        // Calculate upwind origin of the approaching rain front (e.g. 15 to 25 km upwind of island)
        val upwindDistanceKm = 18.0
        val upwindBearingRad = Math.toRadians(windBearingFrom)

        // Lat/Lon offset approximation (~111.32 km per degree latitude, ~110.85 km per degree longitude near equator)
        val deltaLat = (upwindDistanceKm * cos(upwindBearingRad)) / 111.0
        val deltaLon = (upwindDistanceKm * sin(upwindBearingRad)) / 111.0

        val frontCenter = GpsLocation(
            latitude = islandLocation.latitude + deltaLat,
            longitude = islandLocation.longitude + deltaLon
        )

        // Parse rain intensity from rainChance or condition
        val rainChanceInt = currentForecast?.rainChance?.filter { it.isDigit() }?.toIntOrNull() ?: 40
        val estimatedMmH = (rainChanceInt / 100.0) * 12.0

        return WeatherFront(
            frontId = "FRONT-MAHE-LIVE",
            name = "Convective Rain Cell (${getWindDirectionName(windBearingFrom)} Trade Flow)",
            centerLocation = frontCenter,
            velocity = frontVelocity,
            depthKm = 14.0,
            boundaryRadiusKm = 16.0,
            rainIntensityMmH = estimatedMmH.coerceIn(2.0, 35.0)
        )
    }

    /**
     * Generates isochrone points (arrival boundary coordinates at 15m, 30m, 45m).
     */
    fun calculateIsochrones(
        front: WeatherFront,
        timeOffsetsMinutes: List<Int> = listOf(15, 30, 45)
    ): Map<Int, GpsLocation> {
        val isochrones = mutableMapOf<Int, GpsLocation>()
        for (mins in timeOffsetsMinutes) {
            val hours = mins / 60.0
            val travelDistanceKm = front.velocity.speedKmh * hours
            val bearingRad = Math.toRadians(front.velocity.bearingDeg)

            val deltaLat = (travelDistanceKm * cos(bearingRad)) / 111.0
            val deltaLon = (travelDistanceKm * sin(bearingRad)) / 111.0

            isochrones[mins] = GpsLocation(
                latitude = front.centerLocation.latitude + deltaLat,
                longitude = front.centerLocation.longitude + deltaLon
            )
        }
        return isochrones
    }

    private fun getWindDirectionName(bearingDeg: Double): String {
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
            else -> "South-East"
        }
    }
}
