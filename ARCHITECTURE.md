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
