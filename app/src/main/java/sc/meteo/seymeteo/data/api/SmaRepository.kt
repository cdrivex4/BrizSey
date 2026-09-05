package sc.meteo.seymeteo.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sc.meteo.seymeteo.data.model.CapAlertInfo
import sc.meteo.seymeteo.data.model.DailyForecastItem
import sc.meteo.seymeteo.data.model.HomeForecastResponse
import sc.meteo.seymeteo.data.model.IslandLocation
import sc.meteo.seymeteo.data.model.MarineTideData
import sc.meteo.seymeteo.data.model.SunMoonInfo
import sc.meteo.seymeteo.data.model.toCapAlertInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Single source of truth for all remote SMA data.
 * Returns [Result] wrappers so callers can distinguish success/failure without exceptions.
 */
class SmaRepository(
    private val api: SmaApiService = SmaApiService.create()
) {

    suspend fun getAvailableIslands(): List<IslandLocation> = withContext(Dispatchers.IO) {
        try {
            val remote = api.getCities()
            if (remote.isNotEmpty()) remote else IslandLocation.ALL_ISLANDS
        } catch (e: Exception) {
            IslandLocation.ALL_ISLANDS
        }
    }

    /**
     * Fetches the home weather forecast and parses it for the given island.
     * Returns [Result.success] with a list of [DailyForecastItem], or [Result.failure].
     */
    suspend fun getHomeWeatherForecast(island: IslandLocation): Result<List<DailyForecastItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getHomeWeatherForecast()
                Result.success(parseDailyForecastsForIsland(response, island))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetches raw home forecast (used for caching entire response).
     */
    suspend fun getRawHomeWeatherForecast(): Result<HomeForecastResponse> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getHomeWeatherForecast())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetches active CAP GeoJSON alerts and maps them to flat [CapAlertInfo] domain objects.
     */
    suspend fun getCapAlerts(): Result<List<CapAlertInfo>> = withContext(Dispatchers.IO) {
        try {
            val geoJson = api.getCapAlerts()
            val alerts = geoJson.features.mapNotNull { it.toCapAlertInfo() }
            Result.success(alerts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseDailyForecastsForIsland(
        response: HomeForecastResponse,
        selectedIsland: IslandLocation
    ): List<DailyForecastItem> {
        val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val fullDateFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)

        return response.data.mapNotNull { dayCollection ->
            val feature = dayCollection.features.firstOrNull { feat ->
                val slug = feat.properties.citySlug ?: ""
                val cityName = feat.properties.city ?: ""
                slug.contains(selectedIsland.slug, ignoreCase = true) ||
                        cityName.contains(selectedIsland.displayName, ignoreCase = true) ||
                        selectedIsland.slug.contains(slug, ignoreCase = true)
            } ?: dayCollection.features.firstOrNull()

            feature?.let { feat ->
                val dateStr = dayCollection.date ?: feat.properties.date ?: ""
                val parsedDate = try {
                    dateFormatInput.parse(dateStr.take(10)) ?: Date()
                } catch (e: Exception) { Date() }

                DailyForecastItem(
                    date = dateStr.take(10),
                    dayOfWeek = dayOfWeekFormat.format(parsedDate),
                    dateFormatted = fullDateFormat.format(parsedDate),
                    conditionLabel = feat.properties.conditionDisplay,
                    conditionIconUrl = feat.properties.conditionSymbolUrl,
                    tempMax = feat.properties.airTemperatureMax ?: 29.0,
                    tempMin = feat.properties.airTemperatureMin ?: 24.0,
                    wind = feat.properties.windDisplay,
                    seaState = feat.properties.seaStateDisplay,
                    rainChance = feat.properties.rainChanceDisplay
                )
            }
        }
    }

    fun getMarineTideData(island: IslandLocation): MarineTideData =
        MarineTideData.createSampleData(island.displayName)

    fun getSunMoonData(): SunMoonInfo = SunMoonInfo.createSampleData()
}
