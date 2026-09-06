package sc.meteo.seymeteo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
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
