# Graph Report - SeyMeteo  (2026-09-06)

## Corpus Check
- Corpus is ~43,068 words - fits in a single context window. You may not need a graph.

## Summary
- 235 nodes · 393 edges · 20 communities
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 10 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Community 0
- Community 1
- Community 2
- Community 3
- Community 4
- Community 5
- Community 6
- Community 7
- Community 8
- Community 9
- Community 10
- Community 11
- Community 12
- Community 13
- Community 14
- Community 15

## God Nodes (most connected - your core abstractions)
1. `UserPreferences` - 22 edges
2. `IslandLocation` - 15 edges
3. `SmaRepository` - 14 edges
4. `UserPersona` - 13 edges
5. `HomeScreen()` - 13 edges
6. `FavouriteDao` - 12 edges
7. `CapAlertInfo` - 10 edges
8. `WeatherViewModel` - 10 edges
9. `SeyMeteoDatabase` - 9 edges
10. `AlertDao` - 9 edges

## Surprising Connections (you probably didn't know these)
- `HomeScreen()` --calls--> `AlertCard()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/AlertCard.kt
- `HomeScreen()` --calls--> `CurrentWeatherCard()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/CurrentWeatherCard.kt
- `HomeScreen()` --calls--> `IslandSelector()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/IslandSelector.kt
- `HomeScreen()` --calls--> `MarineTideCard()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/MarineTideCard.kt
- `HomeScreen()` --calls--> `PredictabilityCard()`  [INFERRED]
  app/src/main/java/sc/meteo/seymeteo/ui/screens/HomeScreen.kt → app/src/main/java/sc/meteo/seymeteo/ui/components/PredictabilityCard.kt

## Import Cycles
- None detected.

## Communities (20 total, 0 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.09
Nodes (14): UserPersona, FARMER_AGRICULTURE, GENERAL_CITIZEN, MARITIME_FISHER, TOURISM_OUTDOOR, Flow, UserPreferences, ImageVector (+6 more)

### Community 1 - "Community 1"
Cohesion: 0.12
Nodes (11): SmaApiService, Result, SmaRepository, CapAlertGeoJson, IslandLocation, HomeForecastResponse, IslandSelector(), Modifier (+3 more)

### Community 2 - "Community 2"
Cohesion: 0.16
Nodes (6): FavouriteDao, Flow, FavouriteLocationEntity, Context, SeyMeteoDatabase, RoomDatabase

### Community 3 - "Community 3"
Cohesion: 0.18
Nodes (12): MainActivity, Screen, HOME, SATELLITE_MAP, SETTINGS, SatelliteMapScreen(), WebViewClient, SeyMeteoTheme() (+4 more)

### Community 4 - "Community 4"
Cohesion: 0.18
Nodes (10): CapAlertFeature, CapAlertInfo, CapAlertProperties, toCapAlertInfo(), AlertNotificationBuilder, AlertCard(), Modifier, AlertPollerWorker (+2 more)

### Community 5 - "Community 5"
Cohesion: 0.20
Nodes (9): PredictabilityAssessment, PredictabilityLevel, HIGH, MODERATE, UNSTABLE, Modifier, PredictabilityCard(), Modifier (+1 more)

### Community 6 - "Community 6"
Cohesion: 0.25
Nodes (11): DailyForecastItem, DayForecastCollection, ForecastFeature, ForecastProperties, CurrentWeatherCard(), ImageVector, Modifier, WeatherMetricChip() (+3 more)

### Community 7 - "Community 7"
Cohesion: 0.23
Nodes (3): AlertDao, Flow, CachedAlertEntity

### Community 8 - "Community 8"
Cohesion: 0.23
Nodes (3): ForecastDao, Flow, CachedForecastEntity

### Community 9 - "Community 9"
Cohesion: 0.33
Nodes (6): HomeScreen(), Modifier, WeatherUiState, WeatherViewModel, StateFlow, ViewModel

### Community 10 - "Community 10"
Cohesion: 0.40
Nodes (6): MarineTideData, TidePoint, Modifier, MarineInfoBox(), MarineTideCard(), TideItemPill()

### Community 11 - "Community 11"
Cohesion: 0.29
Nodes (6): IslandResolver, ResolvedIsland, GpsLocation, haversineDistanceKm(), Flow, LocationService

### Community 12 - "Community 12"
Cohesion: 0.29
Nodes (7): GlanceAppWidget, SeyMeteoGlanceReceiver, Context, GlanceAppWidget, SeyMeteoGlanceWidget, GlanceAppWidgetReceiver, GlanceId

### Community 13 - "Community 13"
Cohesion: 0.31
Nodes (4): Context, SeyMeteoNotificationChannels, SeyMeteoApplication, Application

### Community 14 - "Community 14"
Cohesion: 0.36
Nodes (4): SunMoonInfo, Modifier, SunMoonCard(), SunMoonRow()

### Community 15 - "Community 15"
Cohesion: 0.33
Nodes (4): OpenMeteoApiService, OpenMeteoCurrent, OpenMeteoDaily, OpenMeteoResponse

## Knowledge Gaps
- **18 isolated node(s):** `HOME`, `SETTINGS`, `SATELLITE_MAP`, `OpenMeteoCurrent`, `OpenMeteoDaily` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 57 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `IslandLocation` connect `Community 1` to `Community 10`, `Community 11`, `Community 6`?**
  _High betweenness centrality (0.183) - this node is a cross-community bridge._
- **Why does `SeyMeteoDatabase` connect `Community 2` to `Community 1`, `Community 4`, `Community 7`, `Community 8`, `Community 12`?**
  _High betweenness centrality (0.172) - this node is a cross-community bridge._
- **Why does `UserPreferences` connect `Community 0` to `Community 1`, `Community 4`?**
  _High betweenness centrality (0.172) - this node is a cross-community bridge._
- **Are the 8 inferred relationships involving `HomeScreen()` (e.g. with `AlertCard()` and `CurrentWeatherCard()`) actually correct?**
  _`HomeScreen()` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `HOME`, `SETTINGS`, `SATELLITE_MAP` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.09032258064516129 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.1164021164021164 - nodes in this community are weakly interconnected._