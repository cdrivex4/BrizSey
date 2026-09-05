package sc.meteo.seymeteo.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SatelliteMapScreen(
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Standalone Leaflet HTML with OpenStreetMap + live EUMETSAT Indian Ocean Satellite & Wind WMS overlays
    val leafletHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #0b1f3a; }
                .leaflet-control-layers { background: rgba(15, 43, 72, 0.9) !important; color: white !important; border-radius: 12px !important; border: 1px solid rgba(255,255,255,0.2) !important; padding: 8px 12px !important; }
                .leaflet-control-layers label { color: white !important; font-family: sans-serif; font-size: 13px; margin: 4px 0; }
                .custom-popup { font-family: sans-serif; }
                .island-badge { background: #0284C7; color: white; padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: bold; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                // Centered on Seychelles Archipelago (Mahé / Praslin / La Digue)
                var map = L.map('map', {
                    center: [-4.6743, 55.5212],
                    zoom: 10,
                    minZoom: 4,
                    maxZoom: 18,
                    zoomControl: true
                });

                // 1. High-detail OpenStreetMap Base
                var osm = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; OpenStreetMap contributors'
                }).addTo(map);

                // 2. Esri World Imagery (Satellite Background)
                var esriSat = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                    attribution: 'Tiles &copy; Esri'
                });

                // 3. EUMETSAT Live Natural Color Cloud Layer (Direct Geoserver WMS used by SMA)
                var eumetsatCloud = L.tileLayer.wms('https://view.eumetsat.int/geoserver/wms', {
                    layers: 'msg_fes:rgb_naturalenhncd',
                    format: 'image/png',
                    transparent: true,
                    version: '1.3.0',
                    attribution: 'EUMETSAT Live'
                }).addTo(map);

                // 4. Open-Meteo Precipitation / Radar WMS Layer
                var rainRadar = L.tileLayer('https://tile.openweathermap.org/map/precipitation_new/{z}/{x}/{y}.png?appid=9de243494c0b295cca9337e1e96b00e2', {
                    opacity: 0.6,
                    attribution: 'Rain Radar'
                });

                // Layer Switcher Controls
                var baseMaps = {
                    "Street Map": osm,
                    "Satellite Topo": esriSat
                };

                var overlayMaps = {
                    "Live EUMETSAT Clouds": eumetsatCloud,
                    "Precipitation Radar": rainRadar
                };

                L.control.layers(baseMaps, overlayMaps, { collapsed: false, position: 'topright' }).addTo(map);

                // Island Microclimate Markers
                var islands = [
                    { name: "Mahé (Victoria & Airport)", lat: -4.6743, lon: 55.5212, desc: "East Coast Trade Winds & Port" },
                    { name: "Morne Seychellois (Highlands 905m)", lat: -4.6433, lon: 55.4383, desc: "High Rainfall Orographic Ridge (3000mm/yr)" },
                    { name: "Beau Vallon (North Mahé)", lat: -4.6136, lon: 55.4297, desc: "Sheltered Bay / Convective Showers" },
                    { name: "Praslin (Vallée de Mai)", lat: -4.3251, lon: 55.7356, desc: "Central Granitic Island" },
                    { name: "La Digue", lat: -4.3601, lon: 55.8385, desc: "Anse Source d'Argent & Outer Reefs" }
                ];

                islands.forEach(function(isl) {
                    var marker = L.circleMarker([isl.lat, isl.lon], {
                        radius: 7,
                        fillColor: "#0284C7",
                        color: "#ffffff",
                        weight: 2,
                        opacity: 1,
                        fillOpacity: 0.9
                    }).addTo(map);

                    marker.bindPopup(
                        '<div class="custom-popup">' +
                        '<strong>' + isl.name + '</strong><br/>' +
                        '<span class="island-badge">' + isl.desc + '</span>' +
                        '</div>'
                    );
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Live Satellite & Cloud Radar", style = MaterialTheme.typography.titleMedium)
                        Text("EUMETSAT Geoserver & OpenStreetMap", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { webViewRef?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SeyNavyPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }
                        webChromeClient = WebChromeClient()
                        loadDataWithBaseURL("https://www.meteo.sc", leafletHtml, "text/html", "UTF-8", null)
                        webViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
