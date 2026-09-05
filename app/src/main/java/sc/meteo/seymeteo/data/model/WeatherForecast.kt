package sc.meteo.seymeteo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeForecastResponse(
    @Json(name = "data") val data: List<DayForecastCollection> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DayForecastCollection(
    @Json(name = "date") val date: String? = null,
    @Json(name = "datetime") val datetime: String? = null,
    @Json(name = "features") val features: List<ForecastFeature> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ForecastFeature(
    @Json(name = "properties") val properties: ForecastProperties
)

@JsonClass(generateAdapter = true)
data class ForecastProperties(
    @Json(name = "date") val date: String? = null,
    @Json(name = "effective_period_label") val effectivePeriodLabel: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "city_slug") val citySlug: String? = null,
    @Json(name = "condition") val condition: String? = null,
    @Json(name = "condition_label") val conditionLabel: String? = null,
    @Json(name = "condition_symbol_url") val conditionSymbolUrl: String? = null,
    @Json(name = "air_temperature_max") val airTemperatureMax: Double? = null,
    @Json(name = "air_temperature_min") val airTemperatureMin: Double? = null,
    @Json(name = "wind_speed") val windSpeed: String? = null,
    @Json(name = "wind_from_direction") val windFromDirection: String? = null,
    @Json(name = "Probability") val rainProbability: String? = null,
    @Json(name = "State Of the Sea") val stateOfTheSea: String? = null
) {
    val tempMaxFormatted: String
        get() = airTemperatureMax?.let { "${it.toInt()}°C" } ?: "--°"

    val tempMinFormatted: String
        get() = airTemperatureMin?.let { "${it.toInt()}°C" } ?: "--°"

    val conditionDisplay: String
        get() = conditionLabel?.takeIf { it.isNotBlank() } ?: "Fair & Sunny"

    val windDisplay: String
        get() = when {
            windSpeed != null && windFromDirection != null -> "$windFromDirection $windSpeed km/h"
            windSpeed != null -> "$windSpeed km/h"
            else -> "Light & Variable"
        }

    val seaStateDisplay: String
        get() = stateOfTheSea?.takeIf { it.isNotBlank() } ?: "Moderate (1.5 - 2.0m)"

    val rainChanceDisplay: String
        get() = rainProbability?.takeIf { it.isNotBlank() } ?: "Moderate"
}

data class DailyForecastItem(
    val date: String,               // "yyyy-MM-dd" for Room cache key
    val dayOfWeek: String,
    val dateFormatted: String,
    val conditionLabel: String,
    val conditionIconUrl: String?,
    val tempMax: Double,
    val tempMin: Double,
    val wind: String,
    val seaState: String,
    val rainChance: String
)
