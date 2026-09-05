package sc.meteo.seymeteo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark surface colours defined as named constants to avoid missing-import compile errors
private val DarkSurface = Color(0xFF0F2B48)
private val DarkOnSurface = Color(0xFFF1F5F9)

private val LightColorScheme = lightColorScheme(
    primary = SeyNavyPrimary,
    onPrimary = SeySurfaceCard,
    primaryContainer = SeyTealLight,
    onPrimaryContainer = SeyNavyDark,
    secondary = SeyOceanCyan,
    onSecondary = SeySurfaceCard,
    secondaryContainer = SeyTealLight,
    onSecondaryContainer = SeyNavyDark,
    tertiary = SeySunGold,
    background = SeySurfaceLight,
    surface = SeySurfaceCard,
    onBackground = SeyTextPrimary,
    onSurface = SeyTextPrimary,
    outline = SeyBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = SeySkyBlue,
    onPrimary = SeyNavyDark,
    primaryContainer = SeyNavyLight,
    onPrimaryContainer = SeyTealLight,
    secondary = SeyOceanCyan,
    background = SeyNavyDark,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
)

@Composable
fun SeyMeteoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
