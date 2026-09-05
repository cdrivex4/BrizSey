package sc.meteo.seymeteo.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sc.meteo.seymeteo.data.api.SmaRepository
import sc.meteo.seymeteo.data.db.SeyMeteoDatabase
import sc.meteo.seymeteo.data.db.entity.CachedForecastEntity
import sc.meteo.seymeteo.data.model.IslandLocation
import sc.meteo.seymeteo.data.preferences.UserPreferences

/**
 * Periodic WorkManager worker that refreshes forecast data for all favourite locations.
 * Runs on network availability, once per refresh interval (default 30 min).
 */
class ForecastSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = SeyMeteoDatabase.getInstance(applicationContext)
            val prefs = UserPreferences(applicationContext)
            val repo = SmaRepository()

            // Refresh forecasts for all 3 main islands (covers what users might switch to)
            val islands = listOf(IslandLocation.MAHE, IslandLocation.PRASLIN, IslandLocation.LA_DIGUE)

            islands.forEach { island ->
                val result = repo.getHomeWeatherForecast(island)
                result.onSuccess { forecasts ->
                    // Convert to Room entities
                    val entities = forecasts.mapIndexed { _, forecast ->
                        CachedForecastEntity(
                            islandSlug = island.slug,
                            date = forecast.date,
                            conditionLabel = forecast.conditionLabel,
                            conditionIconUrl = forecast.conditionIconUrl,
                            tempMax = forecast.tempMax,
                            tempMin = forecast.tempMin,
                            windDisplay = forecast.windDisplay,
                            seaState = forecast.seaState,
                            rainChance = forecast.rainChance
                        )
                    }
                    // Delete old rows and insert fresh ones
                    db.forecastDao().deleteOlderThan(
                        System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                    )
                    db.forecastDao().insertAll(entities)
                }
            }

            // Record sync timestamp
            prefs.setLastSyncMs(System.currentTimeMillis())

            Result.success()
        } catch (e: Exception) {
            // Retry on transient failures (network hiccup)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
