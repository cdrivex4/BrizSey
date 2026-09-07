# BrizSey: Master AI & Developer Handoff Document (`HANDOFF.md`) 🤝🏝️

> **Welcome, AI / Developer!** This document provides the complete context, architectural blueprints, current implementation state, and immediate next action items for **BrizSey** (`sc.meteo.seymeteo`). Read this file first before making changes.

---

## 🧭 1. Project Overview & Core Mission

- **Application Name**: **BrizSey** (formerly SeyMeteo)
- **GitHub Repository**: [https://github.com/cdrivex4/BrizSey](https://github.com/cdrivex4/BrizSey)
- **Package ID**: `sc.meteo.seymeteo` (debug: `sc.meteo.seymeteo.debug`)
- **Target Platform**: Android 8.0 (API 26) through Android 15 (API 35)
- **Primary Objective**: Bridge the gap between low-resolution global NWP models (ECMWF, GFS at 9–13 km) and hyper-local tropical reality on the steep granitic islands of the Seychelles (Mahé, Praslin, La Digue).
- **Core Principles**:
  1. **Topographically-Resolved Microclimate Engine**: Real-time evaluation of orographic precipitation uplift ($\omega = ec{v}_{\text{wind}} \cdot \nabla z$) and leeward Föhn rain shadows using an embedded 30m ASTER Digital Elevation Model (DEM).
  2. **Boundary Layer Thermodynamics**: Magnus-Tetens dew point ($T_d$) and Lifting Condensation Level ($z_{\text{LCL}} \approx 125(T - T_d)\,\text{m}$) comparing cloud base against Mahé's 905m Morne Seychellois ridge.
  3. **100% Live GPS Kinematics & Nowcasting**: Continuous evaluation of relative vector geometry ($\vec{v}_{\text{rel}} = \vec{v}_{\text{front}} - \vec{v}_{\text{user}}$) for stationary observers (Scenario A) and moving vehicles/boats (Scenario B).
  4. **Coastal Marine Hydrodynamics**: Sverdrup-Munk-Bretschneider (SMB) shallow-water wave growth and topographic fetch sheltering calculating hyper-local Beach Calmness Scores ($S_{\text{calm}}$).
  5. **Offline-First Resilience**: Room local SQLite cache, zero cloud API dependencies for core physics calculations (0.05 ms latency, 22 KB memory footprint).

---

## 🏗️ 2. Architectural Structure & Key Files

```
SeyMeteo/
├── app/src/main/java/sc/meteo/seymeteo/
│   ├── data/
│   │   ├── api/
│   │   │   ├── SmaApiService.kt            # Retrofit interface for meteo.sc API
│   │   │   └── SmaRepository.kt            # Single source of truth (Room + API + Open-Meteo)
│   │   ├── db/
│   │   │   ├── AppDatabase.kt              # Room DB instance (seymeteo.db)
│   │   │   ├── ForecastDao.kt              # Room DAO for forecasts & weather items
│   │   │   └── AlertDao.kt                 # Room DAO for CAP severe weather alerts
│   │   ├── location/
│   │   │   ├── LocationService.kt          # Android FusedLocationProviderClient (foreground adaptive)
│   │   │   └── IslandLocationResolver.kt   # Haversine nearest island resolver (Mahé, Praslin, La Digue)
│   │   ├── model/
│   │   │   ├── WeatherModels.kt            # WeatherForecast, DailyForecast, WeatherAlert, CAP schemas
│   │   │   ├── IslandMicroclimate.kt       # MicroclimatePrediction, BeachLocation, ElevationPoint
│   │   │   ├── MaheTopographyGrid.kt       # 64x64 DEM elevation grid, bilinear interpolation, Sobel ∇z
│   │   │   ├── IslandMicroclimatePredictor.kt # LCL calculation, orographic uplift factor, beach calmness
│   │   │   ├── RadarAdvectionEngine.kt     # Multi-frame radar advection tracking & isochrone generator
│   │   │   └── RainInterceptionSolver.kt   # Relative vector solver (v_rel = v_front - v_user, ETA, evasion)
│   │   └── preferences/
│   │       └── UserPreferencesRepository.kt # DataStore preferences (units, persona, theme, radar layers)
│   ├── notification/
│   │   └── CapAlertNotifier.kt             # Android NotificationManager for severe CAP weather alerts
│   ├── ui/
│   │   ├── components/
│   │   │   ├── CurrentWeatherCard.kt       # Glassmorphic temperature, dew point, humidity, wind
│   │   │   ├── RainInterceptionCard.kt     # Live nowcasting card with real-time GPS kinematics strip
│   │   │   ├── MicroclimateCard.kt         # ASTER 30m DEM elevation, LCL cloud base, beach calmness list
│   │   │   ├── RadarPlayerCard.kt          # 12-frame Doppler radar playback controller
│   │   │   ├── MarineTideCard.kt           # Tide heights, moon phase, sea swell
│   │   │   └── PersonaAlertCard.kt         # World Bank 11407 Cost-Loss persona recommendation
│   │   ├── screens/
│   │   │   ├── HomeScreen.kt               # Main dashboard with pull-to-refresh & vertical card stack
│   │   │   ├── SatelliteMapScreen.kt       # Interactive Leaflet.js radar/satellite WebView with overlays
│   │   │   ├── FavouritesScreen.kt         # Saved beaches and custom coordinate waypoints
│   │   │   └── SettingsScreen.kt           # Metric/Imperial, persona selector, auto-sync intervals
│   │   ├── theme/                          # Material 3 glassmorphic design system & dynamic typography
│   │   └── viewmodel/
│   │       └── WeatherViewModel.kt         # StateFlow<WeatherUiState>, reactive location & radar flows
│   ├── widget/
│   │   └── SeyMeteoGlanceWidget.kt         # Android Glance Home Screen widget
│   └── worker/
│       └── WeatherSyncWorker.kt            # WorkManager periodic background sync
├── doc/
│   ├── BRIZSEY_SCIENTIFIC_DISSERTATION.md  # 43-citation publication-grade meteorological dissertation
│   └── Mahemaps/                           # Raw ASTER/SRTM DEM height rasters
├── TODO.md                                 # Comprehensive strategic AI & system roadmap
├── README.md                               # Project presentation & developer quickstart
├── HANDOFF.md                              # This master AI handoff guide
└── graphify-out/                           # AST knowledge graph, analysis, and GRAPH_REPORT.md
```

---

## ⚙️ 3. Physical & Mathematical Models Implemented

### A. Boundary Layer Thermodynamics
- **Dew Point Formula**: Magnus-Tetens with Alduchov & Eskridge (1996) parameters:
  $$\alpha(T, RH) = \frac{17.27 \cdot T}{237.7 + T} + \ln\left(\frac{RH}{100}\right), \quad T_d = \frac{237.7 \cdot \alpha(T, RH)}{17.27 - \alpha(T, RH)}$$
- **Lifting Condensation Level ($z_{\text{LCL}}$)**:
  $$z_{\text{LCL}} \approx 125.0 \cdot (T - T_d) \quad [\text{meters}]$$
- **Ridge Piercing Check**: When $z_{\text{LCL}} \le 905.0\,\text{m}$, the cloud base penetrates Morne Seychellois, triggering forced orographic condensation.

### B. Orographic Lift & Föhn Rain Shadow
- **Vertical Surface Velocity**:
  $$\omega = \vec{v}_{\text{wind}} \cdot \nabla z = u \frac{\partial z}{\partial x} + v \frac{\partial z}{\partial y}$$
- **Orographic Multiplier**:
  $$\mu_{\text{oro}} = \begin{cases} 1.0 + 0.95 \cdot (\omega / \omega_{\text{max}}) & \text{if } \omega \ge 0 \quad (\text{Windward Uplift } +35\% \text{ to } +85\%) \\ \max(0.20,\, 1.0 - 0.80 \cdot |\omega / \omega_{\text{max}}|) & \text{if } \omega < 0 \quad (\text{Leeward Rain Shadow } -50\% \text{ to } -80\%) \end{cases}$$
- **Föhn Warming**: Leeward adiabatic warming $\Delta T \approx +3.89^\circ\text{C}$.

### C. Kinematic Nowcasting Vector Geometry
- **Relative Velocity**:
  $$\vec{v}_{\text{rel}} = \vec{v}_{\text{front}} - \vec{v}_{\text{user}} = \begin{pmatrix} v_{fx} - s_A \sin\theta_A \\ v_{fy} - s_A \cos\theta_A \end{pmatrix}$$
- **Closing Rate**: $v_{\text{closing}} = \vec{v}_{\text{rel}} \cdot \hat{u}_{\text{LOS}}$.
  - If $v_{\text{closing}} > 0$: $\text{ETA} = \left(\frac{d_{\text{boundary}}}{v_{\text{closing}}}\right) \cdot 60\,\text{min}$.
  - If $v_{\text{closing}} \le 0$: `isEvadingSuccessfully = true` ($t^* \to \infty$).

### D. Coastal Marine Hydrodynamics
- **Beach Calmness Score ($S_{\text{calm}}$)**:
  $$S_{\text{calm}}(i, t) = 1.0 - \left( \frac{\|\vec{v}_{\text{wind}}(t)\|}{v_{\text{ref}}} \cdot \max\left(0, \cos(\theta_{\text{wind}}(t) - \theta_{\text{coast\_normal}}(i))\right) \right)$$
  - $\ge 0.75$: 🟢 Calm & Protected (Glassy sea, $<0.5\,\text{m}$ waves).
  - $0.45 - 0.75$: 🟡 Moderate (Light chop, $0.6 - 1.2\,\text{m}$ waves).
  - $< 0.45$: 🔴 Rough & Exposed (Strong onshore swell $1.5 - 2.8\,\text{m}$, rip currents).

---

## 🚀 4. Current State & Recent Sprint Achievements

1. **Simulation Mode Completely Removed**:
   - User kinematics are derived **100% from hardware GPS sensors** via Android `LocationService.kt`.
   - Replaced all manual test sliders with the low-profile **Live GPS Kinematics Telemetry Strip** (`🛰️ Live GPS: 0 km/h (Stationary)` or `🛰️ Live GPS: 35 km/h @ 315° NW (Moving)`).
2. **Scientific Dissertation Expanded**:
   - [`doc/BRIZSEY_SCIENTIFIC_DISSERTATION.md`](doc/BRIZSEY_SCIENTIFIC_DISSERTATION.md) updated with 43 academic citations and Section 8 on AI Foundation Models, DGMR Nowcasting, Commute Departure Optimization, and Generative Video.
3. **Strategic AI Roadmap Established**:
   - [`TODO.md`](TODO.md) created detailing Phases 1–6 (Google DeepMind GraphCast, DGMR, Commute Planner, Generative Video Reports, Climate Change/Coral DHW).
4. **Incremental Build Numbering & Copyright System**:
   - Centralized `version.properties` tracking `BUILD_NUMBER`, `VERSION_NAME`, and `COPYRIGHT_NOTICE` (`Copyright of https://cdrivex4.github.io/ 2026.`).
   - Exposed via Android `BuildConfig` and displayed both in the "About & Research" list and in the interactive footer of `SettingsScreen.kt`.
5. **Configurable Multi-Interval Auto-Refresh & Manual Sync Engine**:
   - Expanded background synchronization frequency choices: **`15 min`**, **`30 min`**, **`60 min` (1 hour)**, **`3 hours`**, **`6 hours`**, and **`12 hours`**.
   - Dynamic WorkManager rescheduling via `SeyMeteoApplication.instance.scheduleForecastSync(minutes)` using `ExistingPeriodicWorkPolicy.UPDATE`.
   - Live Manual Sync action button (**`🔄 Synchronize Live Weather Now`**) with live feedback and timestamp tracking in `SettingsScreen.kt`.
6. **Build & Test Verified**:
   - Unit tests passing ($100\%$, Build #10).
   - Debug APK compiled (`app-debug.apk`).

---

## 📋 5. Developer Runbook & Essential Commands

### Build & Test
```powershell
# Run unit tests
.\gradlew.bat testDebugUnitTest

# Assemble debug APK
.\gradlew.bat assembleDebug

# Sideload to connected Android device via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Knowledge Graph Update (Graphify)
```powershell
python C:\Users\curtis\.gemini\antigravity\brain\507d019e-2679-42cd-a596-eede2b0cffd4\scratch\run_update.py
```

### Git Sync & Push Protocol (`/gitsyncpush`)
```powershell
git status
git add .
git commit -m "<type>: <concise description>"
git push origin master
```

---

## 🎯 6. Next Steps & Future Work (Where to Continue)

Refer to [`TODO.md`](TODO.md) for full phase breakdowns. The recommended immediate coding tasks are:

1. **Phase 3.1 & 3.2: "When to Leave Work" Commute Departure Time Optimizer**:
   - Implement `CommuteDepartureOptimizer.kt` evaluating $\tau^* = \arg\min_\tau \mathcal{J}(\tau)$ over road routes between Victoria, Beau Vallon, Anse Royale, and Eden Island.
   - Add an interactive Departure Time Slider / Timeline Card to `HomeScreen.kt`.
2. **Phase 3.3: Dynamic Divergence / Convergence Push Alerts**:
   - Implement real-time background notification when the user is driving toward an oncoming storm cell.
3. **Phase 4.4: Broadcast Video Story Reel UI Component**:
   - Create a `DailyVideoForecastCard.kt` in Jetpack Compose with video preview and expandable player.
4. **Phase 5.1: Coral Reef Bleaching & Degree Heating Weeks (DHW) Monitor**:
   - Ingest NOAA Coral Reef Watch SST data and display thermal alert levels for Seychelles Marine Parks in `MarineTideCard.kt`.

---

*Handoff document generated: 2026-09-07 — Synchronized with BrizSey v2.0.*
