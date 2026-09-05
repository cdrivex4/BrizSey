package sc.meteo.seymeteo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import sc.meteo.seymeteo.ui.components.AlertCard
import sc.meteo.seymeteo.ui.components.CurrentWeatherCard
import sc.meteo.seymeteo.ui.components.IslandSelector
import sc.meteo.seymeteo.ui.components.MarineTideCard
import sc.meteo.seymeteo.ui.components.SatelliteRadarCard
import sc.meteo.seymeteo.ui.components.SevenDayForecastCard
import sc.meteo.seymeteo.ui.components.SunMoonCard
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeySurfaceLight
import sc.meteo.seymeteo.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SeyMeteo",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Seychelles Meteorological Authority",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xCCFFFFFF),
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SeyNavyPrimary
                )
            )
        },
        containerColor = SeySurfaceLight
    ) { innerPadding ->
        if (uiState.isLoading && uiState.forecastItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SeyNavyPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading Seychelles weather data...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SeyNavyPrimary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Active CAP Emergency Alerts
                if (uiState.activeAlerts.isNotEmpty()) {
                    items(uiState.activeAlerts.size) { index ->
                        AlertCard(alert = uiState.activeAlerts[index])
                    }
                }

                // Island Selector (Mahé, Praslin, La Digue)
                item {
                    IslandSelector(
                        islands = uiState.availableIslands,
                        selectedIsland = uiState.selectedIsland,
                        onIslandSelected = { island -> viewModel.selectIsland(island) }
                    )
                }

                // Main Current Weather Card
                item {
                    CurrentWeatherCard(
                        island = uiState.selectedIsland,
                        currentForecast = uiState.forecastItems.firstOrNull()
                    )
                }

                // 7-Day Probabilistic Forecast
                if (uiState.forecastItems.isNotEmpty()) {
                    item {
                        SevenDayForecastCard(forecastItems = uiState.forecastItems)
                    }
                }

                // Marine & Tides Hub
                item {
                    MarineTideCard(marineData = uiState.marineTideData)
                }

                // Sun & Moon Tracker
                item {
                    SunMoonCard(sunMoonInfo = uiState.sunMoonInfo)
                }

                // Satellite Radar Feed Preview
                item {
                    SatelliteRadarCard()
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
