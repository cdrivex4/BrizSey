package sc.meteo.seymeteo.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-saved favourite location.
 * [sortOrder] controls display order; lower = first.
 */
@Entity(tableName = "favourite_locations")
data class FavouriteLocationEntity(
    @PrimaryKey val slug: String,
    val displayName: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
    val isGpsCurrent: Boolean = false,  // true for the "My Location" virtual entry
    val sortOrder: Int = 0,
    val addedAtMs: Long = System.currentTimeMillis()
)
