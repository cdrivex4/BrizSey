package sc.meteo.seymeteo.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached forecast row for a specific island slug and date.
 * TTL is enforced by checking [fetchedAtMs] against current time.
 */
@Entity(tableName = "cached_forecasts")
data class CachedForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val islandSlug: String,
    val date: String,                   // "2026-09-05"
    val conditionLabel: String,
    val conditionIconUrl: String?,
    val tempMax: Double,
    val tempMin: Double,
    val windDisplay: String,
    val seaState: String,
    val rainChance: String,
    val fetchedAtMs: Long = System.currentTimeMillis()
)
