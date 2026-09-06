package sc.meteo.seymeteo.domain.nowcasting

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import sc.meteo.seymeteo.data.location.GpsLocation
import sc.meteo.seymeteo.data.model.*

class RainInterceptionSolverTest {

    private lateinit var solver: RainInterceptionSolver

    @Before
    fun setUp() {
        solver = RainInterceptionSolver()
    }

    @Test
    fun testScenarioA_StationaryUser_FrontApproaching() {
        // Victoria Port on Mahé: -4.6191, 55.4513
        val userLoc = GpsLocation(-4.6191, 55.4513)
        val stationaryUser = UserKinematics(
            location = userLoc,
            speedKmh = 0.0,
            bearingDeg = 0.0,
            isMoving = false
        )

        // Front located ~20 km SE of Mahé, moving NW (towards 315°) at 30 km/h
        val frontLoc = GpsLocation(-4.7500, 55.5800)
        val front = WeatherFront(
            frontId = "TEST-FRONT-1",
            name = "SE Convective Cell",
            centerLocation = frontLoc,
            velocity = VelocityVector(speedKmh = 30.0, bearingDeg = 315.0),
            depthKm = 10.0,
            boundaryRadiusKm = 8.0,
            rainIntensityMmH = 12.0
        )

        val solution = solver.solveInterception(stationaryUser, front)

        assertEquals(InterceptionScenario.SCENARIO_A_STATIONARY, solution.scenario)
        assertNotNull(solution.timeToRainMinutes)
        assertTrue("ETA should be between 10 and 50 minutes", solution.timeToRainMinutes!! in 10..50)
        assertTrue("Closing speed should be positive", solution.closingSpeedKmh > 10.0)
        assertFalse("User should not be evading when stationary facing front", solution.isEvadingSuccessfully)
    }

    @Test
    fun testScenarioB_DynamicUser_EvadingFront() {
        // User driving West/North-West away from the oncoming SE front at 45 km/h
        val userLoc = GpsLocation(-4.6191, 55.4513)
        val evadingUser = UserKinematics(
            location = userLoc,
            speedKmh = 45.0,
            bearingDeg = 315.0, // Same direction front is traveling, but faster than front
            isMoving = true
        )

        val frontLoc = GpsLocation(-4.7500, 55.5800)
        val front = WeatherFront(
            frontId = "TEST-FRONT-2",
            name = "SE Convective Cell",
            centerLocation = frontLoc,
            velocity = VelocityVector(speedKmh = 25.0, bearingDeg = 315.0),
            depthKm = 10.0,
            boundaryRadiusKm = 8.0,
            rainIntensityMmH = 10.0
        )

        val solution = solver.solveInterception(evadingUser, front)

        assertEquals(InterceptionScenario.SCENARIO_B_DYNAMIC_EVASION, solution.scenario)
        assertTrue("User outrunning front should be evading successfully", solution.isEvadingSuccessfully)
        assertNull("No intersection time when evading", solution.timeToRainMinutes)
    }

    @Test
    fun testScenarioB_DynamicUser_DrivingIntoFront() {
        // User driving South-East directly towards the oncoming SE front at 35 km/h
        val userLoc = GpsLocation(-4.6191, 55.4513)
        val interceptingUser = UserKinematics(
            location = userLoc,
            speedKmh = 35.0,
            bearingDeg = 135.0, // Directly heading into front
            isMoving = true
        )

        val frontLoc = GpsLocation(-4.7500, 55.5800)
        val front = WeatherFront(
            frontId = "TEST-FRONT-3",
            name = "SE Convective Cell",
            centerLocation = frontLoc,
            velocity = VelocityVector(speedKmh = 25.0, bearingDeg = 315.0),
            depthKm = 10.0,
            boundaryRadiusKm = 8.0,
            rainIntensityMmH = 10.0
        )

        val solution = solver.solveInterception(interceptingUser, front)

        assertEquals(InterceptionScenario.SCENARIO_B_DYNAMIC_EVASION, solution.scenario)
        assertFalse("User driving into front cannot be evading", solution.isEvadingSuccessfully)
        assertNotNull(solution.timeToRainMinutes)
        assertTrue("Closing speed should be ~60 km/h", solution.closingSpeedKmh > 50.0)
    }

    @Test
    fun testMahéGraniticSpine_OrographicEffect() {
        // East Coast (Pointe Larue Airport: 55.52°E) vs West Coast (Beau Vallon: 55.42°E)
        val eastCoastUser = GpsLocation(-4.6743, 55.5212)
        val westCoastUser = GpsLocation(-4.6136, 55.4297)

        // SE Trade front traveling NW (towards 315°)
        val seTradeVelocity = VelocityVector(speedKmh = 25.0, bearingDeg = 315.0)

        val eastResult = solver.calculateOrographicInfluence(eastCoastUser, seTradeVelocity)
        val westResult = solver.calculateOrographicInfluence(westCoastUser, seTradeVelocity)

        // East Coast should experience Windward Uplift (uplift factor > 1.0)
        assertTrue("East coast should have windward uplift", eastResult.isWindward)
        assertEquals(1.35, eastResult.upliftFactor, 0.01)

        // West Coast should experience Leeward Rain Shadow (uplift factor < 1.0)
        assertFalse("West coast should be in leeward shadow", westResult.isWindward)
        assertEquals(0.50, westResult.upliftFactor, 0.01)
    }
}
