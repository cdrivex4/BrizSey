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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import sc.meteo.seymeteo.data.preferences.UserPreferences
import sc.meteo.seymeteo.ui.components.*
import sc.meteo.seymeteo.ui.theme.SeyNavyDark
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
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
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val glassOpacity by prefs.glassOpacity.collectAsState(initial = 0.35f)

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.ENGLISH) }
    val lastUpdatedText = remember(uiState.lastUpdatedMs) {
        if (uiState.lastUpdatedMs > 0) {
            "Live Sync: ${timeFormatter.format(Date(uiState.lastUpdatedMs))} (Auto-sync 30m)"
        } else {
            "Connecting to live feed..."
        }
    }

    val currentForecast = uiState.forecastItems.firstOrNull()
    val condition = remember(currentForecast) {
        resolveCondition(
            conditionLabel = currentForecast?.conditionLabel,
            rainChance = currentForecast?.rainChance,
            wind = currentForecast?.wind
        )
    }

    val windSpeedKmh = remember(currentForecast) {
        val windStr = currentForecast?.wind ?: "21"
        Regex("\\d+").find(windStr)?.value?.toFloatOrNull() ?: 21f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BrizSey",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Seychelles Archipelago Weather & Marine · SMA",
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
                    containerColor = Color(0xDD0A192F)
                )
            )
        },
        containerColor = Color(0xFF071324)
    ) { innerPadding ->
        AtmosphericWindowBackground(
            condition = condition,
            windSpeedKmh = windSpeedKmh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.forecastItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Looking through the island window...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Live Sync Status Banner (Frosted Glass)
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            color = Color(0xFF0F2B48).copy(alpha = glassOpacity),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = (glassOpacity * 0.8f + 0.1f).coerceIn(0.15f, 0.45f)))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
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

                    // 2. Active CAP Emergency Alerts
                    if (uiState.activeAlerts.isNotEmpty()) {
                        items(uiState.activeAlerts.size) { index ->
                            AlertCard(
                                alert = uiState.activeAlerts[index],
                                glassOpacity = glassOpacity
                            )
                        }
                    }

                    // 3. Weather Insight Carousel (Swipeable Rain Alert, Golden Hour Sunset, Stability)
                    item {
                        WeatherInsightCarousel(
                            forecast = currentForecast,
                            predictability = uiState.predictability,
                            marineData = uiState.marineTideData,
                            sunMoonInfo = uiState.sunMoonInfo,
                            glassOpacity = glassOpacity
                        )
                    }

                    // 4. Nowcasting Precipitation Advection & Interception Engine
                    item {
                        RainInterceptionCard(
                            solution = uiState.interceptionSolution,
                            isSimulationMode = uiState.isSimulationMode,
                            simulatedSpeedKmh = uiState.simulatedSpeedKmh,
                            simulatedBearingDeg = uiState.simulatedBearingDeg,
                            onUpdateSimulation = { speed, bearing, isSim ->
                                viewModel.updateSimulation(speed, bearing, isSim)
                            },
                            glassOpacity = glassOpacity
                        )
                    }

                    // 5. Topographic Microclimate & Beach Calmness Prediction (DEM 30m)
                    item {
                        MicroclimatePredictionCard(
                            prediction = uiState.microclimatePrediction,
                            glassOpacity = glassOpacity
                        )
                    }

                    // 6. Island Selector (Mahé, Praslin, La Digue)
                    item {
                        IslandSelector(
                            islands = uiState.availableIslands,
                            selectedIsland = uiState.selectedIsland,
                            onIslandSelected = { island -> viewModel.selectIsland(island) },
                            glassOpacity = glassOpacity
                        )
                    }

                    // 5. Main Current Weather Hero Card
                    item {
                        CurrentWeatherCard(
                            island = uiState.selectedIsland,
                            currentForecast = currentForecast,
                            glassOpacity = glassOpacity
                        )
                    }

                    // 6. Smooth Bezier Hourly Forecast & Precipitation Curve
                    if (uiState.forecastItems.isNotEmpty()) {
                        item {
                            HourlyForecastCurve(
                                forecastItems = uiState.forecastItems,
                                glassOpacity = glassOpacity
                            )
                        }
                    }

                    // 7. 2x2 Radial Weather Instruments Cluster (Wind Compass, Pressure Arc, UV, Humidity)
                    item {
                        RadialWeatherInstrumentsGrid(
                            forecast = currentForecast,
                            marineData = uiState.marineTideData,
                            glassOpacity = glassOpacity
                        )
                    }

                    // 8. 7-Day Extended Forecast with Gradient Range Capsules
                    if (uiState.forecastItems.isNotEmpty()) {
                        item {
                            SevenDayForecastCard(
                                forecastItems = uiState.forecastItems,
                                glassOpacity = glassOpacity
                            )
                        }
                    }

                    // 9. Marine & Tides Hub
                    item {
                        MarineTideCard(
                            marineData = uiState.marineTideData,
                            glassOpacity = glassOpacity
                        )
                    }

                    // 10. Sun & Moon Celestial Tracker
                    item {
                        SunMoonCard(
                            sunMoonInfo = uiState.sunMoonInfo,
                            glassOpacity = glassOpacity
                        )
                    }

                    // 11. Interactive Satellite & Doppler Radar Card (Clickable)
                    item {
                        SatelliteRadarCard(
                            glassOpacity = glassOpacity,
                            onOpenRadar = onOpenSatelliteRadar
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}
