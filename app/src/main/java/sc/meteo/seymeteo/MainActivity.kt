package sc.meteo.seymeteo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import sc.meteo.seymeteo.ui.screens.HomeScreen
import sc.meteo.seymeteo.ui.theme.SeyMeteoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeyMeteoTheme {
                HomeScreen()
            }
        }
    }
}
