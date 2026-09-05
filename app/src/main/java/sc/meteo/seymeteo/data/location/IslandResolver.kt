package sc.meteo.seymeteo.data.location

import sc.meteo.seymeteo.data.model.IslandLocation

/**
 * Resolves a GPS coordinate to the nearest SMA island.
 * Uses Haversine distance against known island centroids.
 */
object IslandResolver {

    /** Known SMA forecast islands with their approximate geographic centres. */
    private val knownIslands = listOf(
        ResolvedIsland(
            location = IslandLocation.DEFAULT_MAHE,
            latitude = -4.6796,
            longitude = 55.4917
        ),
        ResolvedIsland(
            location = IslandLocation.DEFAULT_PRASLIN,
            latitude = -4.3191,
            longitude = 55.7400
        ),
        ResolvedIsland(
            location = IslandLocation.DEFAULT_LA_DIGUE,
            latitude = -4.3631,
            longitude = 55.8395
        ),
    )

    /**
     * Returns the closest island to [gps], or Mahé as the default fallback.
     * All Seychelles islands are within the SMA forecast coverage zone.
     */
    fun resolve(gps: GpsLocation): IslandLocation {
        return knownIslands
            .minByOrNull { island ->
                haversineDistanceKm(gps.latitude, gps.longitude, island.latitude, island.longitude)
            }?.location ?: IslandLocation.DEFAULT_MAHE
    }

    data class ResolvedIsland(
        val location: IslandLocation,
        val latitude: Double,
        val longitude: Double
    )
}
