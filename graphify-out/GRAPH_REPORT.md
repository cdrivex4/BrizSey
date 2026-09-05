# Graph Report - .  (2026-09-05)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 215 nodes · 297 edges · 19 communities (17 shown, 2 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 17 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]

## God Nodes (most connected - your core abstractions)
1. `UserPreferences` - 20 edges
2. `FavouriteDao` - 11 edges
3. `IslandLocation` - 11 edges
4. `HomeScreen()` - 11 edges
5. `SmaRepository` - 10 edges
6. `WeatherViewModel` - 9 edges
7. `AlertDao` - 8 edges
8. `ForecastDao` - 8 edges
9. `SeyMeteoDatabase` - 6 edges
10. `FavouriteLocationEntity` - 6 edges

## Surprising Connections (you probably didn't know these)
- `SettingsScreen()` --calls--> `UserPreferences`  [INFERRED]
  ui/screens/SettingsScreen.kt → data/preferences/UserPreferences.kt
- `CurrentWeatherCard()` --references--> `IslandLocation`  [EXTRACTED]
  ui/components/CurrentWeatherCard.kt → data/model/IslandLocation.kt
- `IslandSelector()` --references--> `IslandLocation`  [EXTRACTED]
  ui/components/IslandSelector.kt → data/model/IslandLocation.kt
- `CurrentWeatherCard()` --references--> `DailyForecastItem`  [EXTRACTED]
  ui/components/CurrentWeatherCard.kt → data/model/WeatherForecast.kt
- `ForecastRowItem()` --references--> `DailyForecastItem`  [EXTRACTED]
  ui/components/SevenDayForecastCard.kt → data/model/WeatherForecast.kt

## Import Cycles
- None detected.

## Communities (19 total, 2 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.12
Nodes (14): create(), List, SmaApiService, List, Result, SmaRepository, IslandLocation, String (+6 more)

### Community 1 - "Community 1"
Cohesion: 0.10
Nodes (17): CapAlertProperties, Boolean, Long, AlertCard(), Modifier, CurrentWeatherCard(), ImageVector, Modifier (+9 more)

### Community 2 - "Community 2"
Cohesion: 0.17
Nodes (6): Boolean, Flow, Int, Long, String, UserPreferences

### Community 3 - "Community 3"
Cohesion: 0.12
Nodes (10): CoroutineWorker, CapAlertFeature, CapAlertGeoJson, CapAlertInfo, toCapAlertInfo(), AlertNotificationBuilder, AlertPollerWorker, Result (+2 more)

### Community 4 - "Community 4"
Cohesion: 0.16
Nodes (7): FavouriteDao, Boolean, Flow, Int, List, String, FavouriteLocationEntity

### Community 5 - "Community 5"
Cohesion: 0.20
Nodes (6): ForecastDao, Flow, List, Long, String, CachedForecastEntity

### Community 6 - "Community 6"
Cohesion: 0.20
Nodes (6): AlertDao, Flow, List, Long, String, CachedAlertEntity

### Community 7 - "Community 7"
Cohesion: 0.24
Nodes (7): IslandResolver, ResolvedIsland, GpsLocation, haversineDistanceKm(), Flow, LocationService, Double

### Community 8 - "Community 8"
Cohesion: 0.31
Nodes (9): createSampleData(), String, MarineTideData, TidePoint, Modifier, String, MarineInfoBox(), MarineTideCard() (+1 more)

### Community 9 - "Community 9"
Cohesion: 0.31
Nodes (10): Pair, Boolean, ImageVector, List, String, SettingsInfoRow(), SettingsScreen(), SettingsSectionHeader() (+2 more)

### Community 10 - "Community 10"
Cohesion: 0.28
Nodes (6): createSampleData(), SunMoonInfo, Modifier, String, SunMoonCard(), SunMoonRow()

### Community 11 - "Community 11"
Cohesion: 0.31
Nodes (4): StateFlow, WeatherUiState, WeatherViewModel, ViewModel

### Community 12 - "Community 12"
Cohesion: 0.25
Nodes (5): Bundle, ComponentActivity, MainActivity, Boolean, SeyMeteoTheme()

### Community 13 - "Community 13"
Cohesion: 0.33
Nodes (4): getInstance(), Context, SeyMeteoDatabase, RoomDatabase

### Community 14 - "Community 14"
Cohesion: 0.47
Nodes (5): ForecastRowItem(), Boolean, List, Modifier, SevenDayForecastCard()

## Knowledge Gaps
- **4 isolated node(s):** `ResolvedIsland`, `CapAlertFeature`, `DayForecastCollection`, `ForecastFeature`
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SmaRepository` connect `Community 0` to `Community 10`, `Community 3`?**
  _High betweenness centrality (0.497) - this node is a cross-community bridge._
- **Why does `IslandLocation` connect `Community 0` to `Community 1`, `Community 11`, `Community 7`?**
  _High betweenness centrality (0.279) - this node is a cross-community bridge._
- **Why does `UserPreferences` connect `Community 2` to `Community 9`, `Community 3`?**
  _High betweenness centrality (0.236) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `UserPreferences` (e.g. with `SettingsScreen()` and `.doWork()`) actually correct?**
  _`UserPreferences` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 8 inferred relationships involving `HomeScreen()` (e.g. with `.onCreate()` and `AlertCard()`) actually correct?**
  _`HomeScreen()` has 8 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `SmaRepository` (e.g. with `.doWork()` and `.doWork()`) actually correct?**
  _`SmaRepository` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ResolvedIsland`, `CapAlertFeature`, `DayForecastCollection` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._