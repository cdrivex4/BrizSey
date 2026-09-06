# SeyMeteo Stack & Wiring Audit: Research-Driven Enhancements Blueprint

This blueprint outlines a series of high-impact, research-backed modifications to the SeyMeteo Android Application Stack & Wiring Audit (`AUDIT_STACK_WIRING.md`). By weaving in state-of-the-art meteorological modeling and behavioral science, we elevate the application from a standard weather interface to a precision data fusion and trust-optimized decision system.

---

## Executive Summary
This document provides technical specifications to integrate recent breakthroughs in weather forecasting with user-centered behavioral design. Drawing from **Google DeepMind's WeatherNext 3** [74], the **trans4num High-Precision Meteorological Forecasting Project** [26], **World Bank Group Economic Appraisals** [1], and **psychological research on uncertainty** [57], we map out concrete architectural upgrades across SeyMeteo's repositories, background workers, map rendering engines, and UI layers.

---

## 1. Advanced Machine Learning Data Fusion Engine (`SmaRepository` Upgrade)

### Context & Research Grounding
The trans4num Challenge #4 proved that post-processing numerical forecasts using machine learning algorithms dramatically reduces local point forecast errors [36]. The project successfully mapped global inputs down to station-level targets, identifying key localized variables that drive microclimatic accuracy, such as **helicity, surface temperature, precipitable water, and local station dew point** [28]. Concurrently, Google's **WeatherNext 3** integrates a Functional Generative Network (FGN) mesh transformer architecture to output high-resolution 0.05° (~5 km) station observations and 0.1° (~10 km) gridded surface variables initialized hourly [74, 75].

### Proposed Modifications
We propose transitioning the standard REST API coordinate lookup in `SmaRepository` into a true multi-source **Data Fusion & Spatial Superresolution Engine**:

*   **Programmatic Ingestion of Next-Gen AI Feeds:**
    *   Expand `SmaRepository` and `CachedForecastEntity` to support **WeatherNext 3** variables [74].
    *   Integrate the **0.05° station-trained telemetry** for near-surface temperature and dew point to capture microclimates on mountainous terrain [75, 76].
    *   Parse the **0.1° gridded surface variables**, specifically tracking 1-hour experimental satellite-radar precipitation and turbine-height wind speeds (100m) to serve agricultural and local infrastructure planning [76].
*   **Localized Variable Parsing:**
    *   Expose and store the specific predictors validated by the trans4num framework: **helicity, surface temperature, precipitable water, and local station dew point** [28].
*   **Probabilistic Uncertainty Mapping:**
    *   Incorporate exploratory **Bayesian Neural Field** concepts to represent spatial and temporal prediction uncertainty, particularly in regions of Seychelles with sparse local station coverage [30, 31].
    *   Add a nullable `spatialUncertaintyMetric` (representing the predicted variance/ensemble deviation) to the local SQLite schema to enable risk-based decision metrics in downstream views [30].

```
┌────────────────────────────────────────────────────────┐
│                    SmaRepository                       │
└───────────────────────────┬────────────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        ▼                                       ▼
 ┌──────────────┐                        ┌──────────────┐
 │ WeatherNext3 │                        │ trans4num ML │
 │   Hourly FGN │                        │ Data Fusion  │
 └──────┬───────┘                        └──────┬───────┘
        │ (0.05° Station & 0.1° Surface)        │ (Dewpoint, Helicity, Water)
        └───────────────────┬───────────────────┘
                            ▼
               ┌─────────────────────────┐
               │    Bayesian Neural      │
               │   Uncertainty Filter    │
               └────────────┬────────────┘
                            ▼
              ┌───────────────────────────┐
              │ CachedForecastEntity (DB) │
              └───────────────────────────┘
```

---

## 2. Trust-Optimized Alerting & Dynamic Cost-Loss Engines (`AlertPollerWorker`)

### Context & Research Grounding
World Bank Group research highlights that evaluating weather forecasts based purely on raw meteorological skill often fails to capture real-world utility [1]. In operational early-warning contexts, the expected **Relative Economic Value (REV)** of a forecast is severely compromised by consecutive forecast errors [5]. 
*   **The Mistrust Penalty:** Consecutive false alarms systematically trigger trust erosion, causing a "mistrust penalty" that drastically reduces user compliance with warnings [1, 9]. 
*   **The Cost-Loss Ratio:** The optimal protective strategy depends entirely on a user's specific cost-loss ratio ($C_{prot} / C_{miss}$), representing the cost of taking protective action relative to the losses sustained if an unforecasted extreme event occurs [7, 8].

### Proposed Modifications
We propose upgrading the background `AlertPollerWorker` from a binary alert broadcaster into a **Trust-Optimized Adaptive Decision Engine**:

*   **Incorporate User-Specific Cost-Loss Profiles:**
    *   Add a `CostLossRatioProfile` enum to user settings (e.g., *Smallholder Farmer* with low protection costs but devastating potential crop loss, versus *Aviation/Maritime Operator* with high protection costs relative to losses) [2].
    *   Allow users to input custom profiles, dynamically adjusting the warning sensitivity in the app.
*   **Ensemble Threshold Optimization (Mitigating Trust Decay):**
    *   Instead of immediately pushing a system notification on any adverse event forecast, the background worker will evaluate the probability threshold $p$ against the user's specific cost-loss ratio ($C_{prot} / C_{miss}$) [6, 8].
    *   Utilize optimized thresholding to maximize the expected Relative Economic Value (REV), suppressing marginal alerts that have high false-positive rates for users with higher protection costs [1, 2, 5].
    *   By optimizing alerting logic to avoid long sequences of consecutive false alarms, the background worker aggressively mitigates the **mistrust penalty**, preserving the long-term credibility and warning compliance of the application [9, 13].

---

## 3. High-Frequency Satellite & Precipitation Overlay Engine (`SatelliteMapScreen`)

### Context & Research Grounding
Historically, dissemination has been a primary bottleneck of meteorological infrastructure; even highly accurate forecasts fail to prevent disasters when communication channels lack real-time context and visual accessibility [68, 72]. Google's WeatherNext 3 achieves high-frequency accuracy by ingesting **live global geostationary satellite mosaics** directly into its Functional Generative Network to initialize every hour and output hourly timesteps without temporal interpolation [74, 75].

### Proposed Modifications
We propose supercharging the Leaflet.js-based `SatelliteMapScreen` to render high-resolution, live AI-generated atmospheric overlays:

*   **True Hourly Playback Timelines:**
    *   Update the map's interactive timeline controller from 3- or 6-hour chunks to a highly granular, **1-hour timestep playback** that leverages WeatherNext 3's hourly initialization runs [75, 78].
*   **Advanced Cloud and Precipitation Compositing:**
    *   Render WeatherNext 3's specialized **4-layer cloud variables** (low, medium, high, and convective) as a blended semi-transparent canvas overlay [76].
    *   Integrate Google's **experimental satellite-radar precipitation reanalysis** layer (0.1° resolution) as a dynamic, hardware-accelerated heat map, giving users an intuitive, highly responsive visual model of active storm paths [76, 78].

---

## 4. Predictability-Focused UI/UX Design & Zero-Latency Cached Flow (`Glance Widgets` & `HomeScreen`)

### Context & Research Grounding
Psychological research reveals that frequently checking the weather is not merely about curiosity, but rather a subconscious **anxiety-reduction ritual** [57]. Uncertainty about the environment causes measurable emotional and physical stress [58, 59]. The transition from uncertainty to predictability is psychologically rewarding, helping individuals feel in control of their lives and schedules [57, 60]. 

### Proposed Modifications
To honor the user's psychological need for control and reassurance, we propose a redesign of the SeyMeteo UI layout, shifting from a generic dashboard to a **Predictability-Focused Experience**:

*   **Introduce a "Predictability Index" Card:**
    *   Create a prominent, soothing Composable card on the `HomeScreen` that converts model ensemble variance and spatial uncertainty metrics into a user-friendly "Atmospheric Predictability" gauge [30, 57].
    *   If the model ensemble exhibits low variance (high consensus), display reassuring copy: *"Atmosphere Highly Predictable — Plans are secure for today."* If uncertainty is high, prepare the user: *"Atmosphere Unstable — Keep check-in intervals short for localized updates."*
*   **Guaranteed Zero-Latency Offline-First UI:**
    *   Because lag or load failures on startup exacerbate anxiety, we specify that Jetpack Glance widgets and Compose UI screens must immediately render Room-cached database states (`CachedForecastEntity`) without waiting for network calls [57, 60].
    *   Display a highly visible "Last Offline Reassurance" timestamp. Perform network fetches silently in the background, smoothly updating the visual state via StateFlow only when fresh data is successfully reconciled. This guarantees a calming, dependable app launch experience under all network conditions.

---

## Mapped Source Citations
The technical modifications detailed in this blueprint are grounded in the following research papers and technical specifications:
*   **[1, 2, 5, 7, 8, 9, 13, 20]:** *World Bank Policy Research Working Paper 11407: Assessing the Real-World Economic Value of Weather Forecasts under Compounding Extremes.*
*   **[26, 28, 30, 31, 35, 36]:** *trans4num High-Precision Meteorological Forecasting Final Report on Challenge #4.*
*   **[57, 58, 59, 60]:** *Psychological Studies on Weather Forecasts, Uncertainty, and Reassurance Rituals.*
*   **[74, 75, 76, 78, 79]:** *Google DeepMind / Google Research Technical Specifications: WeatherNext 3.*
