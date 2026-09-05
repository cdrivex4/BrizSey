package sc.meteo.seymeteo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sc.meteo.seymeteo.data.preferences.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    val tempUnit by prefs.tempUnit.collectAsState(initial = "C")
    val windUnit by prefs.windUnit.collectAsState(initial = "kmh")
    val timeFormat by prefs.timeFormat.collectAsState(initial = "24h")
    val theme by prefs.theme.collectAsState(initial = "system")
    val refreshMin by prefs.refreshIntervalMinutes.collectAsState(initial = 30)
    val notifyExtreme by prefs.notifyExtreme.collectAsState(initial = true)
    val notifySevere by prefs.notifySevere.collectAsState(initial = true)
    val notifyModerate by prefs.notifyModerate.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ---- Display ----
            item { SettingsSectionHeader(text = "Display", icon = Icons.Default.Palette) }

            item {
                SettingsSegmentedRow(
                    label = "Temperature",
                    options = listOf("°C" to "C", "°F" to "F"),
                    selected = tempUnit,
                    onSelect = { scope.launch { prefs.setTempUnit(it) } }
                )
            }

            item {
                SettingsSegmentedRow(
                    label = "Wind Speed",
                    options = listOf("km/h" to "kmh", "Knots" to "knots", "m/s" to "ms"),
                    selected = windUnit,
                    onSelect = { scope.launch { prefs.setWindUnit(it) } }
                )
            }

            item {
                SettingsSegmentedRow(
                    label = "Time Format",
                    options = listOf("24h" to "24h", "12h" to "12h"),
                    selected = timeFormat,
                    onSelect = { scope.launch { prefs.setTimeFormat(it) } }
                )
            }

            item {
                SettingsSegmentedRow(
                    label = "Theme",
                    options = listOf("System" to "system", "Light" to "light", "Dark" to "dark"),
                    selected = theme,
                    onSelect = { scope.launch { prefs.setTheme(it) } }
                )
            }

            // ---- Data & Sync ----
            item { SettingsSectionHeader(text = "Data & Sync", icon = Icons.Default.Sync) }

            item {
                SettingsSegmentedRow(
                    label = "Auto-refresh",
                    options = listOf("15 min" to "15", "30 min" to "30", "1 hour" to "60"),
                    selected = refreshMin.toString(),
                    onSelect = { scope.launch { prefs.setRefreshIntervalMinutes(it.toInt()) } }
                )
            }

            // ---- Notifications ----
            item { SettingsSectionHeader(text = "Notifications", icon = Icons.Default.Notifications) }

            item {
                SettingsSwitchRow(
                    label = "🔴 Extreme Alerts",
                    subtitle = "Cyclone, tsunami warnings",
                    checked = notifyExtreme,
                    onToggle = { scope.launch { prefs.setNotifyExtreme(it) } }
                )
            }

            item {
                SettingsSwitchRow(
                    label = "🟠 Severe Warnings",
                    subtitle = "Heavy rain, storm surge",
                    checked = notifySevere,
                    onToggle = { scope.launch { prefs.setNotifySevere(it) } }
                )
            }

            item {
                SettingsSwitchRow(
                    label = "🟡 Advisories",
                    subtitle = "Small craft, wind watches",
                    checked = notifyModerate,
                    onToggle = { scope.launch { prefs.setNotifyModerate(it) } }
                )
            }

            // ---- About ----
            item { SettingsSectionHeader(text = "About", icon = Icons.Default.Info) }

            item {
                SettingsInfoRow(label = "Data Source", value = "Seychelles Met Authority")
            }
            item {
                SettingsInfoRow(label = "Version", value = "1.0.0 (debug)")
            }
            item {
                SettingsInfoRow(label = "SMA Website", value = "www.meteo.sc")
            }
        }
    }
}

// ---- Helper Composables ----

@Composable
private fun SettingsSectionHeader(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun SettingsSegmentedRow(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { idx, (display, value) ->
                SegmentedButton(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = options.size),
                    label = { Text(display, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
