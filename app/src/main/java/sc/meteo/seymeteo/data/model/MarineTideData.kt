package sc.meteo.seymeteo.data.model

data class TidePoint(
    val time: String,
    val heightMeters: Double,
    val isHighTide: Boolean
)

data class MarineTideData(
    val islandName: String,
    val dateFormatted: String,
    val seaCondition: String,
    val waveHeight: String,
    val swellDirection: String,
    val waterTemperature: String,
    val monsoonSeason: String,
    val tides: List<TidePoint>
) {
    companion object {
        fun createSampleData(islandName: String): MarineTideData {
            return MarineTideData(
                islandName = islandName,
                dateFormatted = "Today",
                seaCondition = "Moderate to Rough",
                waveHeight = "1.5 - 2.2 m",
                swellDirection = "South-Easterly Swell",
                waterTemperature = "27.5 °C",
                monsoonSeason = "South-East Monsoon (Vents Alizés)",
                tides = listOf(
                    TidePoint(time = "04:18", heightMeters = 0.5, isHighTide = false),
                    TidePoint(time = "10:32", heightMeters = 1.8, isHighTide = true),
                    TidePoint(time = "16:45", heightMeters = 0.6, isHighTide = false),
                    TidePoint(time = "22:58", heightMeters = 1.9, isHighTide = true)
                )
            )
        }
    }
}
