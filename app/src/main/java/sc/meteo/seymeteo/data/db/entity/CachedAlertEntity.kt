package sc.meteo.seymeteo.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Locally cached CAP (Common Alerting Protocol) alert.
 */
@Entity(tableName = "cached_alerts")
data class CachedAlertEntity(
    @PrimaryKey val identifier: String,
    val event: String?,
    val severity: String?,
    val urgency: String?,
    val certainty: String?,
    val headline: String?,
    val description: String?,
    val instruction: String?,
    val areaDesc: String?,
    val sent: String?,
    val fetchedAtMs: Long = System.currentTimeMillis()
)
