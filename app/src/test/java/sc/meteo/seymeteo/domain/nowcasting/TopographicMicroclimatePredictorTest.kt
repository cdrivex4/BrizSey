package sc.meteo.seymeteo.domain.nowcasting

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import sc.meteo.seymeteo.data.model.CoastalZone
import sc.meteo.seymeteo.data.model.DailyForecastItem

class TopographicMicroclimatePredictorTest {

    private lateinit var predictor: TopographicMicroclimatePredictor

    @Before
    fun setUp() {
        predictor = TopographicMicroclimatePredictor()
    }

    @Test
    fun testLiftingCondensationLevel_AndRidgePiercing() {
        val forecast = DailyForecastItem(
            date = "2026-09-07",
            dayOfWeek = "Today",
            dateFormatted = "07 Sep",
            conditionLabel = "Partly Cloudy",
            conditionIconUrl = null,
            tempMax = 30.0,
            tempMin = 26.0,
            wind = "SE 25 km/h",
            seaState = "Moderate",
            rainChance = "40%"
        )

        // Typical tropical day: T = 28.5°C, RH = 80%
        val prediction = predictor.predictIslandMicroclimate(
            currentForecast = forecast,
            ambientTempC = 28.5,
            relativeHumidityPct = 80
        )

        // T - Td ≈ 3.7°C -> LCL ≈ 125 * 3.7 ≈ 462.5m
        assertTrue("LCL should be between 400m and 550m", prediction.liftingCondensationLevelMeters in 400.0..550.0)
        assertTrue("Orographic condensation is triggered by Morne Seychellois (905m)", prediction.isOrographicCondensationTriggered)
        assertTrue("Prevailing wind label contains South-East", prediction.prevailingWindDirectionLabel.contains("South-East"))
    }

    @Test
    fun testSETradeWinds_MicroclimateSplit() {
        // SE Trade Wind (SE 28 km/h)
        val forecast = DailyForecastItem(
            date = "2026-09-07",
            dayOfWeek = "Today",
            dateFormatted = "07 Sep",
            conditionLabel = "Scattered Showers",
            conditionIconUrl = null,
            tempMax = 29.0,
            tempMin = 25.0,
            wind = "SE 28 km/h",
            seaState = "Rough",
            rainChance = "50%"
        )

        val prediction = predictor.predictIslandMicroclimate(
            currentForecast = forecast,
            ambientTempC = 28.0,
            relativeHumidityPct = 82
        )

        val beauVallon = prediction.districts.first { it.id == "beau_vallon" }
        val anseRoyale = prediction.districts.first { it.id == "anse_royale" }
        val airport = prediction.districts.first { it.id == "pointe_larue" }
        val portGlaud = prediction.districts.first { it.id == "port_glaud" }

        // East & South should have boosted rain and rough seas
        assertEquals(CoastalZone.WINDWARD_EXPOSED, anseRoyale.zone)
        assertTrue("Airport should have high rain probability", airport.rainProbabilityPct > 60)
        assertTrue("Anse Royale sea should be rough", anseRoyale.seaCondition.contains("Rough"))
        assertFalse("Anse Royale should not be recommended for swimming in strong SE Trades", anseRoyale.isSwimmingSafe)

        // West & North should be in rain shadow with calm seas
        assertEquals(CoastalZone.LEEWARD_SHELTERED, beauVallon.zone)
        assertTrue("Beau Vallon rain prob should be lower than synoptic 50%", beauVallon.rainProbabilityPct < 50)
        assertTrue("Beau Vallon sea condition should be calm", beauVallon.seaCondition.contains("Calm"))
        assertTrue("Beau Vallon should be safe for swimming in SE Trades", beauVallon.isSwimmingSafe)
        assertTrue("Port Glaud should be safe for swimming", portGlaud.isSwimmingSafe)
        assertTrue("Best beaches list contains Beau Vallon", prediction.bestBeachesForSwimming.any { it.contains("Beau Vallon") })
    }

    @Test
    fun testNWMonsoon_MicroclimateInversion() {
        // NW Monsoon (NW 24 km/h)
        val forecast = DailyForecastItem(
            date = "2026-09-07",
            dayOfWeek = "Today",
            dateFormatted = "07 Sep",
            conditionLabel = "Overcast Showers",
            conditionIconUrl = null,
            tempMax = 31.0,
            tempMin = 27.0,
            wind = "NW 24 km/h",
            seaState = "Moderate",
            rainChance = "50%"
        )

        val prediction = predictor.predictIslandMicroclimate(
            currentForecast = forecast,
            ambientTempC = 30.0,
            relativeHumidityPct = 85
        )

        val beauVallon = prediction.districts.first { it.id == "beau_vallon" }
        val anseRoyale = prediction.districts.first { it.id == "anse_royale" }

        // Beau Vallon should be windward during NW Monsoon
        assertEquals(CoastalZone.WINDWARD_EXPOSED, beauVallon.zone)
        assertTrue("Beau Vallon has higher rain chance in NW Monsoon", beauVallon.rainProbabilityPct >= 50)
        assertFalse("Beau Vallon gets rough waves in NW Monsoon", beauVallon.isSwimmingSafe)

        // Anse Royale should be leeward and calmer in NW Monsoon
        assertEquals(CoastalZone.LEEWARD_SHELTERED, anseRoyale.zone)
        assertTrue("Anse Royale rain chance is reduced in NW Monsoon", anseRoyale.rainProbabilityPct <= 45)
        assertTrue("Anse Royale is sheltered and safe in NW Monsoon", anseRoyale.isSwimmingSafe)
    }
}
