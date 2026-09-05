package sc.meteo.seymeteo.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sc.meteo.seymeteo.data.api.SmaRepository
import sc.meteo.seymeteo.data.db.SeyMeteoDatabase
import sc.meteo.seymeteo.data.db.entity.CachedAlertEntity
import sc.meteo.seymeteo.notification.AlertNotificationBuilder

/**
 * Polls the SMA CAP GeoJSON endpoint every 15 minutes.
 * Fires a system notification for any alert not previously seen.
 */
class AlertPollerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = SeyMeteoDatabase.getInstance(applicationContext)
            val alertDao = db.alertDao()
            val repo = SmaRepository()
            val notificationBuilder = AlertNotificationBuilder(applicationContext)

            val knownIds = alertDao.getAllKnownIdentifiers().toSet()

            val result = repo.getCapAlerts()
            result.onSuccess { alerts ->
                val newAlerts = alerts.filter { it.identifier !in knownIds }

                // Fire notifications for brand-new alerts
                newAlerts.forEach { alert ->
                    notificationBuilder.notify(alert)
                }

                // Persist all current alerts
                val entities = alerts.map { alert ->
                    CachedAlertEntity(
                        identifier = alert.identifier,
                        event = alert.event,
                        severity = alert.severity,
                        urgency = alert.urgency,
                        certainty = alert.certainty,
                        headline = alert.headline,
                        description = alert.description,
                        instruction = alert.instruction,
                        areaDesc = alert.areaDesc,
                        sent = alert.sent
                    )
                }

                // Purge stale resolved alerts and save current ones
                alertDao.clearAll()
                if (entities.isNotEmpty()) {
                    alertDao.insertAll(entities)
                }
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }
}
