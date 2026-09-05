package sc.meteo.seymeteo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import sc.meteo.seymeteo.data.db.entity.CachedAlertEntity

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<CachedAlertEntity>)

    @Query("SELECT * FROM cached_alerts ORDER BY fetchedAtMs DESC")
    fun observeActiveAlerts(): Flow<List<CachedAlertEntity>>

    @Query("SELECT * FROM cached_alerts ORDER BY fetchedAtMs DESC")
    suspend fun getActiveAlerts(): List<CachedAlertEntity>

    /** Returns identifiers of newly seen alerts (not already in DB). Used for push notifications. */
    @Query("SELECT identifier FROM cached_alerts")
    suspend fun getAllKnownIdentifiers(): List<String>

    @Query("DELETE FROM cached_alerts WHERE fetchedAtMs < :olderThanMs")
    suspend fun deleteOlderThan(olderThanMs: Long)

    @Query("DELETE FROM cached_alerts")
    suspend fun clearAll()
}
