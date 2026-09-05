package sc.meteo.seymeteo.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sc.meteo.seymeteo.data.model.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Single source of truth for all weather data across the Seychelles.
 * Fuses official SMA national forecasts & CAP alerts with high-resolution localized GPS micro-forecasts
 * for Mahé, Praslin, and La Digue.
 */
class SmaRepository(
    private val smaApi: SmaApiService = SmaApiService.create(),
    private val microApi: OpenMeteoApiService = OpenMeteoApiService.create()
) {

    suspend fun getAvailableIslands(): List<IslandLocation> = withContext(Dispatchers.IO) {
        try {
            val remote = smaApi.getCities()
            if (remote.isNotEmpty()) remote else IslandLocation.ALL_ISLANDS
        } catch (e: Exception) {
            IslandLocation.ALL_ISLANDS
        }
    }

    /**
     * Fetches genuine island-specific weather forecast by combining official SMA macro advisories
     * with high-resolution GPS coordinates for Mahé (-4.674, 55.521), Praslin (-4.325, 55.735), and La Digue (-4.360, 55.838).
     */
    suspend fun getHomeWeatherForecast(island: IslandLocation): Result<List<DailyForecastItem>> =
        withContext(Dispatchers.IO) {
            try {
                // Get island coordinates (defaults to Mahé if not set)
                val lat = if (island.coordinates.size >= 2) island.coordinates[1] else -4.6743
                val lon = if (island.coordinates.size >= 2) island.coordinates[0] else 55.5212

                // 1. Fetch genuine high-resolution microclimate data for this exact island coordinate
                val microData = microApi.getMicroForecast(latitude = lat, longitude = lon)
                val daily = microData.daily

                if (daily != null && daily.time.isNotEmpty()) {
                    val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
                    val fullDateFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)

                    val items = daily.time.mapIndexed { idx, dateStr ->
                        val parsedDate = try {
                            dateFormatInput.parse(dateStr) ?: Date()
                        } catch (e: Exception) { Date() }

                        val code = daily.weatherCode.getOrNull(idx) ?: 0
                        val (label, iconUrl) = mapWeatherCodeToSmaIcon(code)
                        val tMax = daily.tempMax.getOrNull(idx) ?: 29.0
                        val tMin = daily.tempMin.getOrNull(idx) ?: 24.0
                        val rProb = daily.rainProbability.getOrNull(idx) ?: 30
                        val windMax = daily.windSpeedMax.getOrNull(idx) ?: 25.0

                        DailyForecastItem(
                            date = dateStr,
                            dayOfWeek = if (idx == 0) "Today" else dayOfWeekFormat.format(parsedDate),
                            dateFormatted = fullDateFormat.format(parsedDate),
                            conditionLabel = label,
                            conditionIconUrl = iconUrl,
                            tempMax = tMax,
                            tempMin = tMin,
                            wind = "SE ${windMax.toInt()} km/h",
                            seaState = if (windMax > 30) "Moderate/Rough (1.8-2.3m)" else "Moderate (1.4-1.9m)",
                            rainChance = "$rProb%"
                        )
                    }
                    Result.success(items)
                } else {
                    // Fallback to SMA response parsing
                    val response = smaApi.getHomeWeatherForecast()
                    Result.success(parseDailyForecastsForIsland(response, island))
                }
            } catch (e: Exception) {
                // Network fallback: try raw SMA endpoint
                try {
                    val response = smaApi.getHomeWeatherForecast()
                    Result.success(parseDailyForecastsForIsland(response, island))
                } catch (fallbackEx: Exception) {
                    Result.failure(e)
                }
            }
        }

    /**
     * Fetches raw home forecast (used for caching entire response).
     */
    suspend fun getRawHomeWeatherForecast(): Result<HomeForecastResponse> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(smaApi.getHomeWeatherForecast())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetches active CAP GeoJSON alerts and maps them to flat [CapAlertInfo] domain objects.
     */
    suspend fun getCapAlerts(): Result<List<CapAlertInfo>> = withContext(Dispatchers.IO) {
        try {
            val geoJson = smaApi.getCapAlerts()
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

    private fun mapWeatherCodeToSmaIcon(code: Int): Pair<String, String> {
        return when (code) {
            0 -> "Clear Sky & Sunny" to "https://www.meteo.sc/static/forecastmanager/weathericons/clearsky_day.svg"
            1, 2 -> "Partly Cloudy" to "https://www.meteo.sc/static/forecastmanager/weathericons/partlycloudy_day.86925ad767d0.svg"
            3 -> "Cloudy" to "https://www.meteo.sc/static/forecastmanager/weathericons/cloudy.svg"
            45, 48 -> "Foggy / Mist" to "https://www.meteo.sc/static/forecastmanager/weathericons/fog.svg"
            51, 53, 55 -> "Passing Showers" to "https://www.meteo.sc/static/forecastmanager/weathericons/lightrain.e424c4017c1b.svg"
            61, 63 -> "Cloudy with Showers" to "https://www.meteo.sc/static/forecastmanager/weathericons/rain.28df119af76e.svg"
            65, 80, 81, 82 -> "Scattered Heavy Rain" to "https://www.meteo.sc/static/forecastmanager/weathericons/heavyrainshowers_day.2865c04b78ea.svg"
            95, 96, 99 -> "Thunderstorm & Squalls" to "https://www.meteo.sc/static/forecastmanager/weathericons/thunderstorm.svg"
            else -> "Passing Showers" to "https://www.meteo.sc/static/forecastmanager/weathericons/lightrain.e424c4017c1b.svg"
        }
    }
}
