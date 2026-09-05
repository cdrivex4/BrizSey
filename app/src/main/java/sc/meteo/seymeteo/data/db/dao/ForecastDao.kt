package sc.meteo.seymeteo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import sc.meteo.seymeteo.data.db.entity.CachedForecastEntity

@Dao
interface ForecastDao {

    /** Insert or replace all rows for a given island (full refresh). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(forecasts: List<CachedForecastEntity>)

    /** Get the 7 most recent rows for an island, sorted by date ascending. */
    @Query("SELECT * FROM cached_forecasts WHERE islandSlug = :slug ORDER BY date ASC LIMIT 7")
    fun observeForecastForIsland(slug: String): Flow<List<CachedForecastEntity>>

    /** One-shot read for background workers. */
    @Query("SELECT * FROM cached_forecasts WHERE islandSlug = :slug ORDER BY date ASC LIMIT 7")
    suspend fun getForecastForIsland(slug: String): List<CachedForecastEntity>

    /** Age of the newest cached row for this island (ms epoch). Returns null if no data. */
    @Query("SELECT MAX(fetchedAtMs) FROM cached_forecasts WHERE islandSlug = :slug")
    suspend fun getNewestFetchTimeMs(slug: String): Long?

    /** Delete stale data older than the given epoch ms. */
    @Query("DELETE FROM cached_forecasts WHERE fetchedAtMs < :olderThanMs")
    suspend fun deleteOlderThan(olderThanMs: Long)

    /** Wipe all cached forecasts (used on manual clear or logout). */
    @Query("DELETE FROM cached_forecasts")
    suspend fun clearAll()
}
