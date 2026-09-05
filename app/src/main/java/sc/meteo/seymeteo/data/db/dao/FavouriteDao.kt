package sc.meteo.seymeteo.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import sc.meteo.seymeteo.data.db.entity.FavouriteLocationEntity

@Dao
interface FavouriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: FavouriteLocationEntity)

    @Delete
    suspend fun delete(location: FavouriteLocationEntity)

    @Update
    suspend fun update(location: FavouriteLocationEntity)

    @Query("SELECT * FROM favourite_locations ORDER BY sortOrder ASC, addedAtMs ASC")
    fun observeFavourites(): Flow<List<FavouriteLocationEntity>>

    @Query("SELECT * FROM favourite_locations ORDER BY sortOrder ASC, addedAtMs ASC")
    suspend fun getFavourites(): List<FavouriteLocationEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_locations WHERE slug = :slug)")
    fun observeIsFavourite(slug: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_locations WHERE slug = :slug)")
    suspend fun isFavourite(slug: String): Boolean

    @Query("SELECT COUNT(*) FROM favourite_locations")
    suspend fun count(): Int

    /** Remove the GPS-current entry before inserting a fresh one. */
    @Query("DELETE FROM favourite_locations WHERE isGpsCurrent = 1")
    suspend fun clearGpsCurrent()
}
