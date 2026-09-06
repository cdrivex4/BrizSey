# Graph Report - SeyMeteo  (2026-09-07)

## Corpus Check
- 80 files · ~455,636 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 347 nodes · 660 edges · 18 communities
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 26 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Module 0
- Module 1
- Module 2
- Module 3
- Module 4
- Module 5
- Module 6
- Module 7
- Module 8
- Module 9
- Module 10
- Module 11
- Module 12
- Module 13

## God Nodes (most connected - your core abstractions)
1. `UserPreferences` - 31 edges
2. `GpsLocation` - 25 edges
3. `DailyForecastItem` - 22 edges
4. `HomeScreen()` - 20 edges
5. `VelocityVector` - 16 edges
6. `AtmosphericCondition` - 16 edges
7. `IslandLocation` - 15 edges
8. `SmaRepository` - 14 edges
9. `UserKinematics` - 13 edges
10. `UserPersona` - 13 edges

## Surprising Connections (you probably didn't know these)
- `HomeScreen()` --calls--> `AlertCard()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/AlertCard.kt
- `HomeScreen()` --calls--> `resolveCondition()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/AtmosphericWindowBackground.kt
- `HomeScreen()` --calls--> `AtmosphericWindowBackground()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/AtmosphericWindowBackground.kt
- `HomeScreen()` --calls--> `CurrentWeatherCard()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/CurrentWeatherCard.kt
- `HomeScreen()` --calls--> `HourlyForecastCurve()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/HourlyForecastCurve.kt

## Import Cycles
- None detected.

## Communities (18 total, 0 thin omitted)

### Community 0 - "Module 0"
Cohesion: 0.11
Nodes (16): GpsLocation, haversineDistanceKm(), Flow, LocationService, InterceptionScenario, SCENARIO_A_STATIONARY, SCENARIO_B_DYNAMIC_EVASION, InterceptionSolution (+8 more)

### Community 1 - "Module 1"
Cohesion: 0.07
Nodes (12): AlertDao, Flow, FavouriteDao, Flow, ForecastDao, Flow, CachedAlertEntity, CachedForecastEntity (+4 more)

### Community 2 - "Module 2"
Cohesion: 0.10
Nodes (21): PredictabilityAssessment, PredictabilityLevel, HIGH, MODERATE, UNSTABLE, SunMoonInfo, Modifier, PredictabilityCard() (+13 more)

### Community 3 - "Module 3"
Cohesion: 0.10
Nodes (13): SmaApiService, Result, SmaRepository, IslandResolver, ResolvedIsland, CapAlertGeoJson, IslandLocation, HomeForecastResponse (+5 more)

### Community 4 - "Module 4"
Cohesion: 0.10
Nodes (6): Flow, UserPreferences, SatelliteMapScreen(), WebViewClient, Bitmap, WebView

### Community 5 - "Module 5"
Cohesion: 0.11
Nodes (15): CapAlertFeature, CapAlertInfo, CapAlertProperties, toCapAlertInfo(), UserPersona, FARMER_AGRICULTURE, GENERAL_CITIZEN, MARITIME_FISHER (+7 more)

### Community 6 - "Module 6"
Cohesion: 0.14
Nodes (15): DailyForecastItem, DayForecastCollection, ForecastFeature, ForecastProperties, CurrentWeatherCard(), ImageVector, Modifier, WeatherMetricChip() (+7 more)

### Community 7 - "Module 7"
Cohesion: 0.18
Nodes (20): AtmosphericCondition, HEAVY_RAIN, NIGHT_CLEAR, OVERCAST, PARTLY_CLOUDY, PASSING_SHOWERS, SUNNY, THUNDERSTORM (+12 more)

### Community 8 - "Module 8"
Cohesion: 0.16
Nodes (11): CoastalZone, FLANK_TRANSITION, HIGH_MOUNTAIN_SPINE, LEEWARD_SHELTERED, WINDWARD_EXPOSED, DistrictMicroclimate, IslandMicroclimatePrediction, DistrictProfile (+3 more)

### Community 9 - "Module 9"
Cohesion: 0.16
Nodes (8): Context, SeyMeteoNotificationChannels, SeyMeteoApplication, WeatherUiState, WeatherViewModel, Application, StateFlow, ViewModel

### Community 10 - "Module 10"
Cohesion: 0.16
Nodes (15): MainActivity, Screen, HOME, SATELLITE_MAP, SETTINGS, ImageVector, PersonaCard(), SettingsInfoRow() (+7 more)

### Community 11 - "Module 11"
Cohesion: 0.26
Nodes (12): MarineTideData, TidePoint, Modifier, MarineInfoBox(), MarineTideCard(), TideItemPill(), HumidityCard(), Modifier (+4 more)

### Community 12 - "Module 12"
Cohesion: 0.29
Nodes (7): GlanceAppWidget, SeyMeteoGlanceReceiver, Context, GlanceAppWidget, SeyMeteoGlanceWidget, GlanceAppWidgetReceiver, GlanceId

### Community 13 - "Module 13"
Cohesion: 0.33
Nodes (4): OpenMeteoApiService, OpenMeteoCurrent, OpenMeteoDaily, OpenMeteoResponse

## Knowledge Gaps
- **32 isolated node(s):** `HOME`, `SETTINGS`, `SATELLITE_MAP`, `OpenMeteoCurrent`, `OpenMeteoDaily` (+27 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 80 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `HomeScreen()` connect `Module 2` to `Module 3`, `Module 4`, `Module 5`, `Module 6`, `Module 7`, `Module 8`, `Module 9`, `Module 10`, `Module 11`?**
  _High betweenness centrality (0.265) - this node is a cross-community bridge._
- **Why does `UserPreferences` connect `Module 4` to `Module 3`, `Module 2`, `Module 10`, `Module 5`?**
  _High betweenness centrality (0.216) - this node is a cross-community bridge._
- **Why does `DailyForecastItem` connect `Module 6` to `Module 0`, `Module 2`, `Module 3`, `Module 8`, `Module 11`?**
  _High betweenness centrality (0.134) - this node is a cross-community bridge._
- **Are the 14 inferred relationships involving `HomeScreen()` (e.g. with `AlertCard()` and `AtmosphericWindowBackground()`) actually correct?**
  _`HomeScreen()` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `VelocityVector` (e.g. with `.testMahéGraniticSpine_OrographicEffect()` and `.testScenarioA_StationaryUser_FrontApproaching()`) actually correct?**
  _`VelocityVector` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `HOME`, `SETTINGS`, `SATELLITE_MAP` to the rest of the system?**
  _32 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Module 0` be split into smaller, more focused modules?**
  _Cohesion score 0.10808080808080808 - nodes in this community are weakly interconnected._