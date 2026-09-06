# BrizSey 🌤️🏝️

> A living, fluid glassmorphic weather and marine app for the Seychelles archipelago — inspired by *Vents Alizés* trade winds and built on the **Seychelles Meteorological Authority (SMA)** live API at [meteo.sc](https://www.meteo.sc).

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![minSdk](https://img.shields.io/badge/minSdk-26%20(Oreo)-orange.svg)](https://developer.android.com/reference/android/os/Build.VERSION_CODES#O)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ✨ Features

| Feature | Status |
|---------|--------|
| 7-day forecast (Mahé, Praslin, La Digue) | ✅ Live |
| Offline-first Room DB cache | ✅ Live |
| Topographic Microclimate & Beach Calmness Engine (DEM 30m) | ✅ Live |
| Lifting Condensation Level (LCL) Thermodynamic Piercing | ✅ Live |
| Leeward Rain Shadow & Windward Orographic Lift Predictor | ✅ Live |
| Coastal Safe Swimming & Wave Swell Guide (Beau Vallon, Anse Royale, etc.) | ✅ Live |
| Nowcasting Precipitation Advection & Dynamic Interception | ✅ Live |
| Atmospheric Predictability & Reassurance Gauge | ✅ Live |
| Cost-Loss Persona Alert Engine (World Bank 11407) | ✅ Live |
| 12-Frame Interactive Doppler Radar Timeline Player | ✅ Live |
| Home screen Glance widget | ✅ Live |
| Background auto-refresh (WorkManager) | ✅ Live |
| GPS auto-detect closest island & kinematic speed/heading | ✅ Live |
| Favourite locations system | ✅ Live |
| CAP severe weather alerts & local notifications | ✅ Live |
| Marine sea-state & tide overview | ✅ Live |
| Sun/moon rise-set info | ✅ Live |
| Satellite/radar preview with opacity controls | ✅ Live |
| Scientific Dissertation & Meteorological Model Paper | 📄 [Read Paper](doc/BRIZSEY_SCIENTIFIC_DISSERTATION.md) |
| Settings (°C/°F, wind units, theme, 12/24h, persona) | ✅ Live |
| Android 8.0 (API 26) through Android 15 support | ✅ Live |

---

## 📸 Screenshots

> _Coming soon — install the debug APK on your device and send us screenshots!_

---

## 🏗️ Architecture

SeyMeteo follows a clean **MVVM + Repository + Offline-First** architecture:

```
┌─────────────────────────────────────────────┐
│              UI Layer (Compose)              │
│  HomeScreen · SettingsScreen · Favourites   │
└──────────────────┬──────────────────────────┘
                   │ collectAsState / StateFlow
┌──────────────────▼──────────────────────────┐
│            ViewModel Layer                  │
│         WeatherViewModel (StateFlow)        │
└──────────────────┬──────────────────────────┘
                   │ suspend / Flow
┌──────────────────▼──────────────────────────┐
│            Repository Layer                 │
│   SmaRepository — single source of truth   │
│   FavouriteDao · ForecastDao · AlertDao     │
│   UserPreferences (DataStore)               │
└──────┬──────────────────────────────┬───────┘
       │ Retrofit                     │ Room
┌──────▼──────┐              ┌────────▼───────┐
│  SMA API    │              │  Local DB      │
│ meteo.sc    │              │  seymeteo.db   │
└─────────────┘              └────────────────┘
```

### Key Libraries

| Library | Purpose |
|---------|---------|
| Jetpack Compose + Material 3 | Modern declarative UI |
| Retrofit + Moshi | REST API calls + JSON parsing |
| Room | Local SQLite persistence (offline cache) |
| DataStore Preferences | User settings |
| WorkManager | Periodic background sync |
| Coil | Async SVG/image loading from SMA CDN |
| Fused Location Provider | GPS island auto-detection |
| Glance | Android home screen widget |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog+ (or any IDE with Kotlin support)
- JDK 17+
- Android SDK API 26+ (Oreo)
- ADB (for sideloading to device)

### Build & Run

```bash
# Clone the repo
git clone https://github.com/cdrivex4/BrizSey.git
cd BrizSey

# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Build Variants

| Variant | Application ID | Description |
|---------|---------------|-------------|
| `debug` | `sc.meteo.seymeteo.debug` | Development, logging enabled |
| `release` | `sc.meteo.seymeteo` | Production, minified |

---

## 🌐 API

SeyMeteo consumes the **public SMA API** at `https://www.meteo.sc`. No API key is required — all endpoints are publicly accessible.

| Endpoint | Description |
|----------|-------------|
| `GET /api/cities` | Available island locations |
| `GET /weather/home-weather-forecast/` | 7-day forecast (GeoJSON FeatureCollection) |
| `GET /api/cap/alerts.geojson` | Active CAP severe weather alerts |
| `GET /api/home-map/settings` | Radar/satellite map layer config |

> **Note:** The SMA API does not require authentication for read access. Forecast data is returned for Mahé, Praslin, and La Digue.

---

## 📦 Project Structure

```
SeyMeteo/
├── app/
│   └── src/main/java/sc/meteo/seymeteo/
│       ├── data/
│       │   ├── api/           # Retrofit service + SmaRepository
│       │   ├── db/            # Room database, DAOs, entities
│       │   ├── location/      # GPS + island resolver
│       │   ├── model/         # Domain data classes
│       │   └── preferences/   # DataStore user settings
│       ├── notification/      # CAP alert notifications
│       ├── ui/
│       │   ├── components/    # Reusable Compose cards
│       │   ├── screens/       # HomeScreen, SettingsScreen, etc.
│       │   ├── theme/         # Color, Type, Theme
│       │   └── viewmodel/     # WeatherViewModel
│       ├── widget/            # Glance home screen widget
│       └── worker/            # WorkManager background jobs
├── gradle/
│   ├── libs.versions.toml     # Version catalog
│   └── wrapper/
├── api_discovery/             # Sample SMA API responses (dev reference)
├── decompiled_meteo_sez/      # JADX output of legacy app (reference only)
└── pulled_apk/                # Pulled APKs from device (gitignored)
```

---

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on branching, commit style, and pull requests.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE). Weather data is provided by the Seychelles Meteorological Authority and remains their intellectual property.

---

## 🙏 Credits

- **Data Provider:** [Seychelles Meteorological Authority (SMA)](https://www.meteo.sc)
- **Original app inspiration:** _Meteo Sez_ by SMA (package `sc.gov.meteo`)
- **Icons:** SMA weather icon CDN + Material Symbols

---

> *"Beautiful islands deserve a beautiful weather app."* 🏝️
