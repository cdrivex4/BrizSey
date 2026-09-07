package sc.meteo.seymeteo.ui.screens

import sc.meteo.seymeteo.BuildConfig
import sc.meteo.seymeteo.SeyMeteoApplication
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import sc.meteo.seymeteo.data.model.UserPersona
import sc.meteo.seymeteo.data.preferences.UserPreferences
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary
import sc.meteo.seymeteo.ui.theme.SeyOceanCyan
import sc.meteo.seymeteo.ui.theme.SeyTextPrimary
import sc.meteo.seymeteo.ui.theme.SeyTextSecondary

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
    val glassOpacity by prefs.glassOpacity.collectAsState(initial = 0.35f)
    val refreshMin by prefs.refreshIntervalMinutes.collectAsState(initial = 30)
    val lastSyncMs by prefs.lastSyncMs.collectAsState(initial = 0L)
    val userPersona by prefs.userPersona.collectAsState(initial = UserPersona.GENERAL_CITIZEN)
    var isManualSyncing by remember { mutableStateOf(false) }
    var syncFeedbackMessage by remember { mutableStateOf<String?>(null) }
    val syncTimeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.ENGLISH) }
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
            // ---- Appearance & Glass Translucency ----
            item { SettingsSectionHeader(text = "Appearance & Glass Translucency", icon = Icons.Default.Tune) }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Card Glass Translucency",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${(glassOpacity * 100).toInt()}% Opacity",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "Adjust the tint and transparency of floating weather cards over the live atmospheric window.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SeyTextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                    )
                    Slider(
                        value = glassOpacity,
                        onValueChange = { scope.launch { prefs.setGlassOpacity(it) } },
                        valueRange = 0.15f..0.85f,
                        steps = 14
                    )
                }
            }

            // ---- Display ----
            item { SettingsSectionHeader(text = "Display & Units", icon = Icons.Default.Palette) }

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

            // ---- Cost-Loss Profile (Decision Theory Grounding) ----
            item { SettingsSectionHeader(text = "Alert Sensitivity Profile", icon = Icons.Default.Tune) }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text(
                        text = "Cost-Loss Protection Model (World Bank 11407)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Customizes alert thresholds to match your personal activity and mitigate false-alarm alert fatigue.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SeyTextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    UserPersona.entries.forEach { persona ->
                        PersonaCard(
                            persona = persona,
                            isSelected = userPersona == persona,
                            onSelect = { scope.launch { prefs.setUserPersona(persona) } }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // ---- Data & Sync ----
            item { SettingsSectionHeader(text = "Data & Sync", icon = Icons.Default.Sync) }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Auto-refresh Interval",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Background synchronization frequency for live forecasts, CAP alerts, and radar grids.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SeyTextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    // Row 1: High frequency (15m, 30m, 60m)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val row1Options = listOf("15 min" to 15, "30 min" to 30, "60 min" to 60)
                        row1Options.forEachIndexed { idx, (display, minutes) ->
                            SegmentedButton(
                                selected = refreshMin == minutes,
                                onClick = {
                                    scope.launch {
                                        prefs.setRefreshIntervalMinutes(minutes)
                                        SeyMeteoApplication.instance.scheduleForecastSync(minutes)
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = row1Options.size),
                                label = { Text(display, style = MaterialTheme.typography.labelSmall, fontWeight = if (refreshMin == minutes) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Standard/Extended frequency (3h, 6h, 12h)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val row2Options = listOf("3 hours" to 180, "6 hours" to 360, "12 hours" to 720)
                        row2Options.forEachIndexed { idx, (display, minutes) ->
                            SegmentedButton(
                                selected = refreshMin == minutes,
                                onClick = {
                                    scope.launch {
                                        prefs.setRefreshIntervalMinutes(minutes)
                                        SeyMeteoApplication.instance.scheduleForecastSync(minutes)
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = row2Options.size),
                                label = { Text(display, style = MaterialTheme.typography.labelSmall, fontWeight = if (refreshMin == minutes) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Manual Synchronize Card & Button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Manual Synchronization",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (lastSyncMs > 0) "Last synced: ${syncTimeFormatter.format(Date(lastSyncMs))}" else "Last synced: Auto on start",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SeyOceanCyan,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (!isManualSyncing) {
                                            scope.launch {
                                                isManualSyncing = true
                                                syncFeedbackMessage = null
                                                SeyMeteoApplication.instance.triggerImmediateSync()
                                                delay(1000)
                                                prefs.setLastSyncMs(System.currentTimeMillis())
                                                isManualSyncing = false
                                                syncFeedbackMessage = "Weather data updated successfully!"
                                            }
                                        }
                                    },
                                    enabled = !isManualSyncing,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    if (isManualSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Syncing...", fontSize = 12.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            syncFeedbackMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✓ $msg",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // ---- Notifications ----
            item { SettingsSectionHeader(text = "Notification Channels", icon = Icons.Default.Notifications) }

            item {
                SettingsSwitchRow(
                    label = "🔴 Extreme Alerts",
                    subtitle = "Cyclone, tsunami bulletins",
                    checked = notifyExtreme,
                    onToggle = { scope.launch { prefs.setNotifyExtreme(it) } }
                )
            }

            item {
                SettingsSwitchRow(
                    label = "🟠 Severe Warnings",
                    subtitle = "Torrential rain, storm surges",
                    checked = notifySevere,
                    onToggle = { scope.launch { prefs.setNotifySevere(it) } }
                )
            }

            item {
                SettingsSwitchRow(
                    label = "🟡 Advisories",
                    subtitle = "Small craft watches, wind gusts",
                    checked = notifyModerate,
                    onToggle = { scope.launch { prefs.setNotifyModerate(it) } }
                )
            }

            // ---- About ----
            item { SettingsSectionHeader(text = "About & Research", icon = Icons.Default.Info) }

            item {
                SettingsInfoRow(label = "Application", value = "BrizSey")
            }
            item {
                SettingsInfoRow(label = "Version", value = "v${BuildConfig.VERSION_NAME}")
            }
            item {
                SettingsInfoRow(label = "Build Number", value = "Build #${BuildConfig.BUILD_NUMBER}")
            }
            item {
                SettingsInfoRow(label = "Data Provider", value = "Seychelles Met Authority (meteo.sc)")
            }
            item {
                SettingsInfoRow(label = "Microclimates", value = "30m DEM + Open-Meteo High-Res Grid")
            }
            item {
                SettingsInfoRow(label = "Radar & Nowcasting", value = "EUMETSAT & RainViewer Live")
            }

            // ---- Bottom Build & Copyright Footer ----
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BrizSey v${BuildConfig.VERSION_NAME} · Build #${BuildConfig.BUILD_NUMBER}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SeyTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${BuildConfig.COPYRIGHT_NOTICE}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SeyOceanCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://cdrivex4.github.io/")
                            )
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Granitic Archipelago Meteorological Intelligence",
                        style = MaterialTheme.typography.labelSmall,
                        color = SeyTextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ---- Helper Composables ----

@Composable
private fun PersonaCard(
    persona: UserPersona,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SeyNavyPrimary.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) SeyNavyPrimary else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = SeyNavyPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = persona.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) SeyNavyPrimary else SeyTextPrimary
                    )
                    Surface(
                        color = if (isSelected) SeyNavyPrimary else Color(0xFF94A3B8),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "C/L ${(persona.costLossRatio * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = persona.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = SeyOceanCyan,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = persona.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SeyTextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

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
