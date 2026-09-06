package sc.meteo.seymeteo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sc.meteo.seymeteo.SeyMeteoApplication
import sc.meteo.seymeteo.data.api.SmaRepository
import sc.meteo.seymeteo.data.location.GpsLocation
import sc.meteo.seymeteo.data.location.LocationService
import sc.meteo.seymeteo.data.model.*
import sc.meteo.seymeteo.domain.nowcasting.RadarAdvectionEngine
import sc.meteo.seymeteo.domain.nowcasting.RainInterceptionSolver
import sc.meteo.seymeteo.domain.nowcasting.TopographicMicroclimatePredictor

data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdatedMs: Long = 0L,
    val availableIslands: List<IslandLocation> = IslandLocation.ALL_ISLANDS,
    val selectedIsland: IslandLocation = IslandLocation.DEFAULT_MAHE,
    val forecastItems: List<DailyForecastItem> = emptyList(),
    val activeAlerts: List<CapAlertInfo> = emptyList(),
    val predictability: PredictabilityAssessment = PredictabilityAssessment.evaluate(emptyList(), emptyList()),
    val marineTideData: MarineTideData = MarineTideData.createSampleData("Mahé"),
    val sunMoonInfo: SunMoonInfo = SunMoonInfo.createSampleData(),
    val activeFront: WeatherFront? = null,
    val interceptionSolution: InterceptionSolution? = null,
    val microclimatePrediction: IslandMicroclimatePrediction? = null,
    val userKinematics: UserKinematics = UserKinematics(location = GpsLocation(-4.6191, 55.4513))
)

class WeatherViewModel(
    private val repository: SmaRepository = SmaRepository(),
    private val advectionEngine: RadarAdvectionEngine = RadarAdvectionEngine(),
    private val interceptionSolver: RainInterceptionSolver = RainInterceptionSolver(),
    private val microclimatePredictor: TopographicMicroclimatePredictor = TopographicMicroclimatePredictor(),
    private val locationService: LocationService = SeyMeteoApplication.instance.locationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeLiveGpsKinematics()
    }

    private fun observeLiveGpsKinematics() {
        viewModelScope.launch {
            locationService.userKinematics.collect { kinematics ->
                _uiState.update { current ->
                    val front = current.activeFront ?: advectionEngine.estimateActiveFront(
                        kinematics.location,
                        current.forecastItems.firstOrNull()
                    )
                    val solution = interceptionSolver.solveInterception(kinematics, front)
                    current.copy(
                        userKinematics = kinematics,
                        activeFront = front,
                        interceptionSolution = solution
                    )
                }
            }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            fetchDataInternal()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            fetchDataInternal()
        }
    }

    fun selectIsland(island: IslandLocation) {
        _uiState.update { it.copy(selectedIsland = island) }
        viewModelScope.launch {
            val result = repository.getHomeWeatherForecast(island)
            result.onSuccess { forecasts ->
                _uiState.update { current ->
                    val islandGps = GpsLocation(island.latitude, island.longitude)
                    val front = advectionEngine.estimateActiveFront(islandGps, forecasts.firstOrNull())
                    val currentKinematics = locationService.currentUserKinematics
                    val solution = interceptionSolver.solveInterception(currentKinematics, front)
                    val microclimate = microclimatePredictor.predictIslandMicroclimate(forecasts.firstOrNull())

                    current.copy(
                        forecastItems = forecasts,
                        predictability = PredictabilityAssessment.evaluate(forecasts, current.activeAlerts),
                        marineTideData = repository.getMarineTideData(island),
                        activeFront = front,
                        interceptionSolution = solution,
                        microclimatePrediction = microclimate
                    )
                }
            }
        }
    }

    private suspend fun fetchDataInternal() {
        val currentIsland = _uiState.value.selectedIsland
        val islandGps = GpsLocation(currentIsland.latitude, currentIsland.longitude)

        // 1. Fetch Alerts (non-blocking)
        val alertsResult = repository.getCapAlerts()
        val alerts = alertsResult.getOrDefault(emptyList())

        // 2. Fetch Forecasts
        repository.getHomeWeatherForecast(currentIsland)
            .onSuccess { forecasts ->
                val front = advectionEngine.estimateActiveFront(islandGps, forecasts.firstOrNull())
                val currentKinematics = locationService.currentUserKinematics
                val solution = interceptionSolver.solveInterception(currentKinematics, front)
                val microclimate = microclimatePredictor.predictIslandMicroclimate(forecasts.firstOrNull())

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = false,
                        errorMessage = null,
                        forecastItems = forecasts,
                        activeAlerts = alerts,
                        predictability = PredictabilityAssessment.evaluate(forecasts, alerts),
                        lastUpdatedMs = System.currentTimeMillis(),
                        marineTideData = repository.getMarineTideData(currentIsland),
                        sunMoonInfo = repository.getSunMoonData(),
                        userKinematics = currentKinematics,
                        activeFront = front,
                        interceptionSolution = solution,
                        microclimatePrediction = microclimate
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = true,
                        activeAlerts = alerts,
                        predictability = PredictabilityAssessment.evaluate(it.forecastItems, alerts),
                        errorMessage = "Unable to reach SMA: ${e.localizedMessage ?: "Check connection"}"
                    )
                }
            }
    }
}
