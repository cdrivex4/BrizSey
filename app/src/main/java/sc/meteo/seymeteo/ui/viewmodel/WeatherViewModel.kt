package sc.meteo.seymeteo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sc.meteo.seymeteo.data.api.SmaRepository
import sc.meteo.seymeteo.data.model.CapAlertInfo
import sc.meteo.seymeteo.data.model.DailyForecastItem
import sc.meteo.seymeteo.data.model.IslandLocation
import sc.meteo.seymeteo.data.model.MarineTideData
import sc.meteo.seymeteo.data.model.SunMoonInfo

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
    val marineTideData: MarineTideData = MarineTideData.createSampleData("Mahé"),
    val sunMoonInfo: SunMoonInfo = SunMoonInfo.createSampleData()
)

class WeatherViewModel(
    private val repository: SmaRepository = SmaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
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
            // Reload forecast for newly selected island
            val result = repository.getHomeWeatherForecast(island)
            result.onSuccess { forecasts ->
                _uiState.update {
                    it.copy(
                        forecastItems = forecasts,
                        marineTideData = repository.getMarineTideData(island)
                    )
                }
            }
        }
    }

    private suspend fun fetchDataInternal() {
        val currentIsland = _uiState.value.selectedIsland

        // Forecasts
        repository.getHomeWeatherForecast(currentIsland)
            .onSuccess { forecasts ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = false,
                        errorMessage = null,
                        forecastItems = forecasts,
                        lastUpdatedMs = System.currentTimeMillis(),
                        marineTideData = repository.getMarineTideData(currentIsland),
                        sunMoonInfo = repository.getSunMoonData()
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = true,
                        errorMessage = "Unable to reach SMA: ${e.localizedMessage ?: "Check connection"}"
                    )
                }
            }

        // Alerts (non-blocking — best effort)
        repository.getCapAlerts()
            .onSuccess { alerts ->
                _uiState.update { it.copy(activeAlerts = alerts) }
            }
    }
}
