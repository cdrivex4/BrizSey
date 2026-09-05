package sc.meteo.seymeteo.data.model

data class SunMoonInfo(
    val sunriseTime: String,
    val sunsetTime: String,
    val dawnTime: String,
    val duskTime: String,
    val moonriseTime: String,
    val moonsetTime: String,
    val moonPhaseName: String,
    val moonIlluminationPct: Int,
    val uvIndexMax: Int
) {
    companion object {
        fun createSampleData(): SunMoonInfo {
            return SunMoonInfo(
                sunriseTime = "06:14",
                sunsetTime = "18:22",
                dawnTime = "05:52",
                duskTime = "18:44",
                moonriseTime = "02:10",
                moonsetTime = "14:35",
                moonPhaseName = "Waning Crescent",
                moonIlluminationPct = 28,
                uvIndexMax = 11 // Very High (Typical for Seychelles equator)
            )
        }
    }
}
