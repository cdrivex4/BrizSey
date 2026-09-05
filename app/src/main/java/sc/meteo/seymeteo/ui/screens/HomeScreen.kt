package sc.meteo.seymeteo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import sc.meteo.seymeteo.ui.components.*
import sc.meteo.seymeteo.ui.theme.SeyNavyDark
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
import sc.meteo.seymeteo.ui.theme.SeySurfaceLight
import sc.meteo.seymeteo.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel = viewModel(),
    onOpenSettings: () -> Unit = {},
    onOpenSatelliteRadar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.ENGLISH) }
    val lastUpdatedText = remember(uiState.lastUpdatedMs) {
        if (uiState.lastUpdatedMs > 0) {
            "Live Sync: ${timeFormatter.format(Date(uiState.lastUpdatedMs))} (Auto-sync 30m)"
        } else {
            "Connecting to live feed..."
        }
    }

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
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
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
                        text = "Loading live Seychelles weather...",
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
                // Live Sync Status Banner
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = SeyNavyDark,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = SeyOceanCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = lastUpdatedText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                            }
                            if (uiState.isRefreshing) {
                                Text(
                                    text = "Updating...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SeyOceanCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

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

                // Satellite Radar Feed Card (Clickable)
                item {
                    SatelliteRadarCard(
                        onOpenRadar = onOpenSatelliteRadar
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
