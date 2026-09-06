package sc.meteo.seymeteo.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.JavascriptInterface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import sc.meteo.seymeteo.data.preferences.UserPreferences
import sc.meteo.seymeteo.ui.theme.SeyNavyPrimary

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SatelliteMapScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    val isPlayingPref by userPrefs.radarIsPlaying.collectAsState(initial = false)
    val isMutedPref by userPrefs.radarIsMuted.collectAsState(initial = true)
    val opacityPref by userPrefs.radarOpacity.collectAsState(initial = 0.55f)
    val frameIndexPref by userPrefs.radarFrameIndex.collectAsState(initial = 0)

    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val initialOpacityPct = (opacityPref * 100).toInt().coerceIn(10, 100)

    // Standalone Leaflet HTML with Multi-Frame RainViewer Radar Timeline, EUMETSAT clouds, and Persistent Controls
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
                .leaflet-control-layers { 
                    background: rgba(15, 43, 72, 0.94) !important; 
                    color: white !important; 
                    border-radius: 12px !important; 
                    border: 1px solid rgba(255,255,255,0.2) !important; 
                    padding: 8px 12px !important; 
                    box-shadow: 0 4px 12px rgba(0,0,0,0.4) !important;
                }
                .leaflet-control-layers label { color: white !important; font-family: sans-serif; font-size: 13px; margin: 4px 0; }
                .custom-popup { font-family: sans-serif; font-size: 12px; }
                .island-badge { background: #0284C7; color: white; padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: bold; }
                
                /* Opacity Slider Control Box */
                .opacity-control {
                    background: rgba(15, 43, 72, 0.94);
                    color: white;
                    padding: 6px 12px;
                    border-radius: 12px;
                    border: 1px solid rgba(255,255,255,0.2);
                    font-family: sans-serif;
                    font-size: 11px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.4);
                }
                .opacity-control input[type=range] {
                    width: 110px;
                    margin-top: 2px;
                    accent-color: #38BDF8;
                }
                
                /* Radar Timeline Player Control */
                .timeline-control {
                    background: rgba(15, 43, 72, 0.94);
                    color: white;
                    padding: 8px 14px;
                    border-radius: 16px;
                    border: 1px solid rgba(255,255,255,0.25);
                    font-family: sans-serif;
                    font-size: 12px;
                    box-shadow: 0 6px 16px rgba(0,0,0,0.5);
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    min-width: 220px;
                }
                .timeline-header {
                    display: flex;
                    justify-content: space-between;
                    width: 100%;
                    font-size: 11px;
                    margin-bottom: 4px;
                }
                .timeline-title {
                    font-weight: bold;
                    color: #38BDF8;
                }
                .timeline-time {
                    font-weight: bold;
                    color: #F8FAFC;
                    background: rgba(56, 189, 248, 0.2);
                    padding: 1px 6px;
                    border-radius: 4px;
                }
                .timeline-buttons {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    margin: 4px 0;
                }
                .t-btn {
                    background: #0284C7;
                    border: none;
                    color: white;
                    border-radius: 8px;
                    padding: 4px 10px;
                    font-size: 12px;
                    font-weight: bold;
                    cursor: pointer;
                }
                .t-btn:active { background: #0369A1; }
                .timeline-slider {
                    width: 100%;
                    accent-color: #38BDF8;
                }
                .radar-pulse {
                    display: inline-block;
                    width: 8px;
                    height: 8px;
                    border-radius: 50%;
                    background: #22c55e;
                    margin-right: 4px;
                }
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

                // 1. High-detail OpenStreetMap Base (Default)
                var osm = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; OpenStreetMap'
                }).addTo(map);

                // 2. Esri World Imagery (Satellite Topography)
                var esriSat = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                    maxZoom: 19,
                    attribution: 'Tiles &copy; Esri'
                });

                // 3. EUMETSAT Live Natural Color Cloud Layer (Restored from DataStore)
                var currentOpacity = ${opacityPref};
                var eumetsatCloud = L.tileLayer.wms('https://view.eumetsat.int/geoserver/wms', {
                    layers: 'msg_fes:rgb_naturalenhncd',
                    format: 'image/png',
                    transparent: true,
                    version: '1.3.0',
                    opacity: currentOpacity,
                    maxZoom: 18,
                    attribution: 'EUMETSAT'
                }).addTo(map);

                // 4. EUMETSAT Thermal Infrared Storm Monitor
                var eumetsatInfrared = L.tileLayer.wms('https://view.eumetsat.int/geoserver/wms', {
                    layers: 'msg_fes:ir108',
                    format: 'image/png',
                    transparent: true,
                    version: '1.3.0',
                    opacity: 0.6,
                    maxZoom: 18,
                    attribution: 'EUMETSAT IR'
                });

                // 5. Multi-Frame RainViewer Dynamic Radar Player
                var radarFrames = [];
                var radarLayers = [];
                var currentFrameIndex = ${frameIndexPref};
                var isPlaying = ${isPlayingPref};
                var playInterval = null;

                var rainViewerGroup = L.layerGroup().addTo(map);

                fetch('https://api.rainviewer.com/public/weather-maps.json')
                    .then(function(res) { return res.json(); })
                    .then(function(data) {
                        if (data && data.radar && data.radar.past && data.radar.past.length > 0) {
                            radarFrames = data.radar.past;
                            
                            radarFrames.forEach(function(frame) {
                                var tileUrl = 'https://tilecache.rainviewer.com' + frame.path + '/512/{z}/{x}/{y}/2/1_1.png';
                                var layer = L.tileLayer(tileUrl, {
                                    opacity: 0,
                                    maxZoom: 18,
                                    maxNativeZoom: 6,
                                    tileSize: 512,
                                    attribution: 'RainViewer'
                                });
                                radarLayers.push(layer);
                                rainViewerGroup.addLayer(layer);
                            });

                            if (currentFrameIndex >= radarFrames.length || currentFrameIndex <= 0) {
                                currentFrameIndex = radarFrames.length - 1;
                            }
                            showRadarFrame(currentFrameIndex);
                            updateTimelineUI();

                            if (isPlaying) {
                                startPlayLoop();
                            }
                        }
                    })
                    .catch(function(err) {
                        console.error('Failed to load RainViewer frames', err);
                    });

                function showRadarFrame(index) {
                    if (radarLayers.length === 0) return;
                    radarLayers.forEach(function(layer, idx) {
                        if (idx === index) {
                            layer.setOpacity(currentOpacity);
                        } else {
                            layer.setOpacity(0);
                        }
                    });
                    currentFrameIndex = index;
                    updateTimelineUI();

                    if (window.AndroidRadar) {
                        window.AndroidRadar.onFrameIndexChanged(index);
                    }
                }

                function formatFrameTime(timestamp) {
                    var date = new Date(timestamp * 1000);
                    var hours = String(date.getHours()).padStart(2, '0');
                    var minutes = String(date.getMinutes()).padStart(2, '0');
                    return hours + ':' + minutes;
                }

                function updateTimelineUI() {
                    var timeLabel = document.getElementById('frame-time');
                    var slider = document.getElementById('frame-slider');
                    if (radarFrames.length > 0 && currentFrameIndex < radarFrames.length) {
                        var frame = radarFrames[currentFrameIndex];
                        var isLatest = (currentFrameIndex === radarFrames.length - 1);
                        if (timeLabel) {
                            timeLabel.innerText = isLatest ? 'LIVE (' + formatFrameTime(frame.time) + ')' : formatFrameTime(frame.time);
                        }
                        if (slider) {
                            slider.max = radarFrames.length - 1;
                            slider.value = currentFrameIndex;
                        }
                    }
                }

                function startPlayLoop() {
                    var playBtn = document.getElementById('btn-play');
                    if (playBtn) playBtn.innerText = '⏸ Pause';
                    isPlaying = true;
                    clearInterval(playInterval);
                    playInterval = setInterval(function() {
                        var nextIdx = (currentFrameIndex + 1) % radarFrames.length;
                        showRadarFrame(nextIdx);
                    }, 1000);
                    if (window.AndroidRadar) {
                        window.AndroidRadar.onPlayStateChanged(true);
                    }
                }

                function stopPlayLoop() {
                    var playBtn = document.getElementById('btn-play');
                    if (playBtn) playBtn.innerText = '▶ Play';
                    isPlaying = false;
                    clearInterval(playInterval);
                    if (window.AndroidRadar) {
                        window.AndroidRadar.onPlayStateChanged(false);
                    }
                }

                function togglePlay() {
                    if (isPlaying) {
                        stopPlayLoop();
                    } else {
                        startPlayLoop();
                    }
                }

                // Timeline Controller Control
                var timelineControl = L.control({ position: 'bottomright' });
                timelineControl.onAdd = function(map) {
                    var div = L.DomUtil.create('div', 'timeline-control');
                    div.innerHTML = 
                        '<div class="timeline-header">' +
                            '<span class="timeline-title"><span class="radar-pulse"></span>Doppler Timeline</span>' +
                            '<span id="frame-time" class="timeline-time">Loading...</span>' +
                        '</div>' +
                        '<div class="timeline-buttons">' +
                            '<button id="btn-prev" class="t-btn">⏮</button>' +
                            '<button id="btn-play" class="t-btn">' + (isPlaying ? '⏸ Pause' : '▶ Play') + '</button>' +
                            '<button id="btn-next" class="t-btn">⏭</button>' +
                        '</div>' +
                        '<input id="frame-slider" class="timeline-slider" type="range" min="0" max="10" value="' + currentFrameIndex + '" />';
                    L.DomEvent.disableClickPropagation(div);
                    return div;
                };
                timelineControl.addTo(map);

                // Add interactive Cloud Opacity Slider
                var opacitySlider = L.control({ position: 'bottomleft' });
                opacitySlider.onAdd = function(map) {
                    var div = L.DomUtil.create('div', 'opacity-control');
                    div.innerHTML = '<strong>Cloud / Radar Opacity</strong><br/>' +
                                    '<input id="slider" type="range" min="0" max="100" value="${initialOpacityPct}" /> <span id="op-val">${initialOpacityPct}%</span>';
                    L.DomEvent.disableClickPropagation(div);
                    return div;
                };
                opacitySlider.addTo(map);

                // 6. Nowcasting Advection Vectors & Front Isochrones Layer Group
                var advectionGroup = L.layerGroup().addTo(map);

                // Approaching Front Wavefront Isochrones (15m, 30m, 45m)
                var isochrone15 = L.polyline([
                    [-4.5800, 55.6000],
                    [-4.6800, 55.6500],
                    [-4.7800, 55.6700]
                ], { color: '#EF4444', weight: 2.5, dashArray: '6, 6', opacity: 0.85 }).addTo(advectionGroup);
                isochrone15.bindTooltip("🌧️ Front Isochrone T+15 min", { permanent: false, className: 'custom-popup' });

                var isochrone30 = L.polyline([
                    [-4.5400, 55.6800],
                    [-4.6600, 55.7400],
                    [-4.7600, 55.7700]
                ], { color: '#F59E0B', weight: 2, dashArray: '4, 6', opacity: 0.75 }).addTo(advectionGroup);
                isochrone30.bindTooltip("🌧️ Front Isochrone T+30 min", { permanent: false, className: 'custom-popup' });

                var isochrone45 = L.polyline([
                    [-4.5000, 55.7600],
                    [-4.6400, 55.8300],
                    [-4.7400, 55.8700]
                ], { color: '#38BDF8', weight: 1.5, dashArray: '4, 4', opacity: 0.65 }).addTo(advectionGroup);
                isochrone45.bindTooltip("🌧️ Front Isochrone T+45 min", { permanent: false, className: 'custom-popup' });

                // SE Trade Wind / Advection Front Velocity Vector Arrows
                var frontArrow = L.polyline([
                    [-4.7200, 55.6500],
                    [-4.6600, 55.5100]
                ], { color: '#38BDF8', weight: 3.5, opacity: 0.9 }).addTo(advectionGroup);
                frontArrow.bindPopup("<strong>Precipitation Advection Vector</strong><br/>Velocity: 24 km/h towards NW (315°)<br/>Trade Wind Inflow: SE 135°");

                // 7. Mahé Central Granitic Mountain Spine (Orographic Shield) Layer Group
                var spineGroup = L.layerGroup().addTo(map);
                var maheRidgeLine = [
                    [-4.6150, 55.4250], // Signal Hill
                    [-4.6433, 55.4383], // Morne Seychellois (905m)
                    [-4.6700, 55.4600], // Congo Rouge / Mont Sébert
                    [-4.7100, 55.4900], // Montagne Posée
                    [-4.7600, 55.5100]  // South Mahé Ridge
                ];

                var spinePolyline = L.polyline(maheRidgeLine, {
                    color: '#F59E0B',
                    weight: 4,
                    dashArray: '8, 6',
                    opacity: 0.9
                }).addTo(spineGroup);

                spinePolyline.bindPopup(
                    '<div class="custom-popup">' +
                    '<strong>🏔️ Mahé Granitic Spine (Morne Seychellois 905m)</strong><br/>' +
                    '<span style="color:#FBBF24">● Windward East Slope:</span> +35% Orographic Uplift<br/>' +
                    '<span style="color:#34D399">● Leeward West Slope:</span> -50% Rain Shadow Shield' +
                    '</div>'
                );

                // 8. User Kinematic Position & Evasion Vector Layer Group
                var userGroup = L.layerGroup().addTo(map);
                var userMarker = L.circleMarker([-4.6191, 55.4513], {
                    radius: 9,
                    fillColor: '#10B981',
                    color: '#FFFFFF',
                    weight: 3,
                    opacity: 1,
                    fillOpacity: 0.95
                }).addTo(userGroup);

                userMarker.bindPopup(
                    '<div class="custom-popup">' +
                    '<strong>📍 User Position (Victoria / Mahé)</strong><br/>' +
                    '<span>Scenario: Nowcasting Active</span><br/>' +
                    '<strong style="color:#34D399">🛡️ Recommended Evasion:</strong> 290° WNW toward Beau Vallon Bay' +
                    '</div>'
                );

                // Evasion Heading Arrow (towards Leeward Beau Vallon)
                var evasionArrow = L.polyline([
                    [-4.6191, 55.4513],
                    [-4.6050, 55.4150]
                ], { color: '#34D399', weight: 3, dashArray: '3, 4', opacity: 0.9 }).addTo(userGroup);
                evasionArrow.bindTooltip("Optimal Evasion Vector (WNW)", { permanent: false });

                // Layer Switcher Controls
                var baseMaps = {
                    "Street Map": osm,
                    "Satellite Topo": esriSat
                };

                var overlayMaps = {
                    "Live Clouds (EUMETSAT)": eumetsatCloud,
                    "Doppler Radar Timeline": rainViewerGroup,
                    "Infrared Storm Monitor": eumetsatInfrared,
                    "Advection Isochrones (15/30/45m)": advectionGroup,
                    "Mahé Mountain Spine Shield": spineGroup,
                    "User Kinematics & Evasion": userGroup
                };

                L.control.layers(baseMaps, overlayMaps, { collapsed: false, position: 'topright' }).addTo(map);

                setTimeout(function() {
                    var btnPlay = document.getElementById('btn-play');
                    var btnPrev = document.getElementById('btn-prev');
                    var btnNext = document.getElementById('btn-next');
                    var frameSlider = document.getElementById('frame-slider');
                    var slider = document.getElementById('slider');
                    var valDisplay = document.getElementById('op-val');

                    if (btnPlay) btnPlay.addEventListener('click', togglePlay);
                    if (btnPrev) btnPrev.addEventListener('click', function() {
                        if (isPlaying) stopPlayLoop();
                        var prevIdx = (currentFrameIndex - 1 + radarFrames.length) % radarFrames.length;
                        showRadarFrame(prevIdx);
                    });
                    if (btnNext) btnNext.addEventListener('click', function() {
                        if (isPlaying) stopPlayLoop();
                        var nextIdx = (currentFrameIndex + 1) % radarFrames.length;
                        showRadarFrame(nextIdx);
                    });
                    if (frameSlider) frameSlider.addEventListener('input', function(e) {
                        if (isPlaying) stopPlayLoop();
                        showRadarFrame(parseInt(e.target.value));
                    });

                    if (slider) {
                        slider.addEventListener('input', function(e) {
                            currentOpacity = e.target.value / 100;
                            eumetsatCloud.setOpacity(currentOpacity);
                            eumetsatInfrared.setOpacity(currentOpacity);
                            if (radarLayers.length > 0 && currentFrameIndex < radarLayers.length) {
                                radarLayers[currentFrameIndex].setOpacity(currentOpacity);
                            }
                            if (valDisplay) valDisplay.innerText = e.target.value + '%';
                            if (window.AndroidRadar) {
                                window.AndroidRadar.onOpacityChanged(currentOpacity);
                            }
                        });
                    }
                }, 600);

                // Island Microclimate Markers
                var islands = [
                    { name: "Victoria & Port (Mahé)", lat: -4.6191, lon: 55.4513, desc: "Urban / Coastal Port" },
                    { name: "Pointe Larue / Airport", lat: -4.6743, lon: 55.5212, desc: "Official SMA Met Station" },
                    { name: "Morne Seychellois (905m)", lat: -4.6433, lon: 55.4383, desc: "Highlands: 3000mm/yr Orographic Rain" },
                    { name: "Beau Vallon (North)", lat: -4.6136, lon: 55.4297, desc: "NW Bay: Convective Cloud Zone" },
                    { name: "Anse Royale (South)", lat: -4.7431, lon: 55.5186, desc: "South Mahé Marine Exposure" },
                    { name: "Praslin (Vallée de Mai)", lat: -4.3251, lon: 55.7356, desc: "Praslin Central Island" },
                    { name: "La Digue", lat: -4.3601, lon: 55.8385, desc: "La Passe & Outer Reefs" }
                ];

                islands.forEach(function(isl) {
                    var marker = L.circleMarker([isl.lat, isl.lon], {
                        radius: 8,
                        fillColor: "#0284C7",
                        color: "#ffffff",
                        weight: 2,
                        opacity: 1,
                        fillOpacity: 0.95
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

    class AndroidRadarBridge {
        @JavascriptInterface
        fun onPlayStateChanged(playing: Boolean) {
            coroutineScope.launch { userPrefs.setRadarIsPlaying(playing) }
        }

        @JavascriptInterface
        fun onMuteChanged(muted: Boolean) {
            coroutineScope.launch { userPrefs.setRadarIsMuted(muted) }
        }

        @JavascriptInterface
        fun onOpacityChanged(opacity: Float) {
            coroutineScope.launch { userPrefs.setRadarOpacity(opacity) }
        }

        @JavascriptInterface
        fun onFrameIndexChanged(index: Int) {
            coroutineScope.launch { userPrefs.setRadarFrameIndex(index) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Live Satellite & Cloud Radar", style = MaterialTheme.typography.titleMedium)
                        Text("EUMETSAT Clouds + Doppler Timeline Player", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
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
                factory = { ctx ->
                    WebView(ctx).apply {
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
                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            allowFileAccess = true
                            allowContentAccess = true
                        }
                        addJavascriptInterface(AndroidRadarBridge(), "AndroidRadar")
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
