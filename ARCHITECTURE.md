# SeyMeteo Architecture Decision Records

This document captures key architectural decisions made during development and the rationale behind them.

---

## ADR-001: Offline-First with Room over pure-network approach

**Date:** 2026-09-05  
**Status:** Accepted

**Context:** The original SMA app had no persistence — every launch fetched fresh data. On Seychelles' outer islands, mobile connectivity can be intermittent. Users need to see forecast data even offline.

**Decision:** Use Room as the primary data source. The ViewModel reads from Room via `Flow<>` (reactive, always up-to-date). A background WorkManager job refreshes Room from the SMA API every 30 minutes when network is available.

**Consequences:** Adds Room + KSP compile complexity. App launch is instant (shows cached data while refreshing in background). Stale data is clearly indicated with a "Last updated N minutes ago" badge.

---

## ADR-002: Result<T> wrappers in Repository

**Date:** 2026-09-05  
**Status:** Accepted

**Context:** Network calls fail. The original app had no error handling — it would silently show nothing on failure.

**Decision:** All `SmaRepository` methods return `Result<T>`. ViewModels pattern-match on success/failure and update `WeatherUiState.error` accordingly. The UI shows a retry button on failure.

---

## ADR-003: WorkManager for background sync (not AlarmManager)

**Date:** 2026-09-05  
**Status:** Accepted

**Context:** We need periodic background data refresh that survives device reboots and app kills.

**Decision:** Use `WorkManager` with `PeriodicWorkRequest` (15-min minimum interval). WorkManager handles Doze mode, Battery Optimization exemptions, and rescheduling after reboots automatically.

**Rejected:** `AlarmManager` — requires careful manual handling of Doze. `JobScheduler` — lower-level API that WorkManager wraps anyway.

---

## ADR-004: DataStore Preferences over SharedPreferences

**Date:** 2026-09-05  
**Status:** Accepted

**Context:** We need to store user settings (temp units, theme, notification toggles, last island slug).

**Decision:** Jetpack DataStore Preferences — type-safe, coroutine-native, no `apply()/commit()` race conditions. Returns `Flow<T>` so settings changes immediately propagate to the UI without restart.

---

## ADR-005: Haversine for island detection (no reverse geocoding API)

**Date:** 2026-09-05  
**Status:** Accepted

**Context:** We need to auto-detect which island the user is on based on GPS.

**Decision:** Simple Haversine formula against 3 known island centroids (Mahé, Praslin, La Digue). No external geocoding API needed — Seychelles is small enough that nearest-centroid is accurate for all inhabited islands.

**Consequences:** Does not distinguish between, e.g., north vs south Mahé (all map to Mahé). Adequate for the SMA's 3-city forecast resolution.

---

## ADR-006: SMA GeoJSON CAP polling (no FCM)

**Date:** 2026-09-05  
**Status:** Accepted

**Context:** The legacy app registered Firebase tokens but FCM was never actually pushed by SMA's server. We need working alerts.

**Decision:** WorkManager polls `/api/cap/alerts.geojson` every 15 minutes. New identifiers (not previously seen in Room) trigger local `NotificationManager` notifications. This works without any FCM server-side setup.

**Trade-off:** 15-min delay vs true push. Acceptable for weather alerts (typhoon warnings don't appear and disappear in 15 minutes). Can be upgraded to FCM later if SMA provides server-sent push.

---

## ADR-007: Decision-Theoretic Cost-Loss Alert Filtering & Atmospheric Predictability Index

**Date:** 2026-09-06  
**Status:** Accepted

**Context:** Naive push alerts trigger for all active warnings regardless of user context, causing severe false-alarm fatigue ("The Mistrust Penalty", World Bank WP 11407). Different users have vastly different risk tolerances (e.g. a maritime fisher facing vessel risk vs. a general citizen facing minor commute delays).

**Decision:**
1. Implement persona-based alert thresholding in `AlertPollerWorker`: calculate risk weight $P_{risk}$ and compare against persona cost-loss ratio $C_{prot} / C_{loss}$ (`General Citizen` = 0.65, `Maritime Fisher` = 0.20, `Farmer` = 0.35, `Tourism` = 0.50). Sub-threshold alerts are silently cached in Room SQLite for in-app viewing without interrupting the user.
2. Calculate a 0-100% Atmospheric Predictability & Consensus Index (`PredictabilityScore.kt`) that translates complex multi-source meteorological variance into clear psychological reassurance copy on `HomeScreen`.

---

## ADR-008: Jetpack Glance App Widget & Multi-Frame Doppler Radar Timeline

**Date:** 2026-09-06  
**Status:** Accepted

**Context:** Users need instant glanceable island weather on their Android launcher without launching the app, and need visual temporal progression of convective clouds and rain radar.

**Decision:**
1. Use `androidx.glance` to render a modern home screen widget (`SeyMeteoGlanceWidget.kt`) that reads from Room DB with 0ms network latency.
2. In `SatelliteMapScreen.kt`, query the full array of RainViewer Doppler radar frames to provide a 10-minute timestep timeline playback slider with opacity controls over Leaflet OpenStreetMap tiles.

---

## ADR-009: Living Atmospheric Window Canvas & Samsung-Grade Fluid Visualizations

**Date:** 2026-09-06  
**Status:** Accepted

**Context:** Users want a sensory, living connection to the tropical weather outside — treating the screen as a frosted plane of glass with rain droplets running down it in rainy conditions, swaying palm silhouettes modulated by live wind speed, radiant golden sunbeams, continuous cubic Bezier temperature curves, and carousel insights.

**Decision:**
1. Implement `AtmosphericWindowBackground.kt` using procedural Compose Canvas rendering:
   - Dynamic multi-stop sky atmosphere gradients.
   - Harmonic sway physics ($A \sin(\omega t + \phi)$) for palm and foliage silhouettes driven by `windSpeedKmh`.
   - Physics-based water droplets & slanted streaks sliding down the glass pane during precipitation.
   - Frosted glassmorphism container cards with subtle glass highlights and borders.
2. Implement `HourlyForecastCurve.kt` with continuous cubic Bezier splines and rain droplet percentage pills.
3. Implement `WeatherInsightCarousel.kt` and `RadialGauges.kt` (Wind compass dial, barometric pressure arc, UV, humidity).
4. Persist radar player state, timeline frames, opacity, and mute preferences in DataStore via `@JavascriptInterface` bridge.

---

## ADR-010: BrizSey Rebrand & User-Adjustable Glass Translucency System

**Date:** 2026-09-06  
**Status:** Accepted

**Context:** Users require customizable transparency levels so they can adjust the visual depth of the living atmospheric window background while retaining 100% legibility of forecast numbers, gauge readouts, and charts. Rebrand to **BrizSey** to emphasize the Seychellois breeze and trade wind heritage (*Vents Alizés*).

**Decision:**
1. Rebrand app to **BrizSey** across application manifest, strings, UI headers, and remote repository (`https://github.com/cdrivex4/BrizSey`).
2. Implement user-controlled glass translucency slider (15% to 85% opacity) in `SettingsScreen.kt` backed by reactive DataStore preference (`KEY_GLASS_OPACITY`).
3. Pass `glassOpacity` dynamically through Compose State down to all floating container cards (`CurrentWeatherCard`, `WeatherInsightCarousel`, `HourlyForecastCurve`, `RadialWeatherInstrumentsGrid`, `SevenDayForecastCard`, `MarineTideCard`, `SunMoonCard`, `SatelliteRadarCard`, `AlertCard`, `IslandSelector`).
4. Ensure all typographic layers, icons, and numerical data maintain 100% solid alpha and high-contrast color palettes regardless of the glass opacity level.


