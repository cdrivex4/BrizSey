package sc.meteo.seymeteo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import sc.meteo.seymeteo.ui.screens.HomeScreen
import sc.meteo.seymeteo.ui.screens.SatelliteMapScreen
import sc.meteo.seymeteo.ui.screens.SettingsScreen
import sc.meteo.seymeteo.ui.theme.SeyMeteoTheme

enum class Screen {
    HOME,
    SETTINGS,
    SATELLITE_MAP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeyMeteoTheme {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val locationService = remember { SeyMeteoApplication.instance.locationService }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (fineGranted || coarseGranted) {
                        locationService.startRealtimeTracking()
                    }
                }

                LaunchedEffect(Unit) {
                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasFine || hasCoarse) {
                        locationService.startRealtimeTracking()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> locationService.startRealtimeTracking()
                            Lifecycle.Event.ON_PAUSE -> locationService.stopRealtimeTracking()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        locationService.stopRealtimeTracking()
                    }
                }

                var currentScreen by remember { mutableStateOf(Screen.HOME) }

                when (currentScreen) {
                    Screen.HOME -> {
                        HomeScreen(
                            onOpenSettings = { currentScreen = Screen.SETTINGS },
                            onOpenSatelliteRadar = { currentScreen = Screen.SATELLITE_MAP }
                        )
                    }
                    Screen.SETTINGS -> {
                        SettingsScreen(
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }
                    Screen.SATELLITE_MAP -> {
                        SatelliteMapScreen(
                            onBack = { currentScreen = Screen.HOME }
                        )
                    }
                }
            }
        }
    }
}
