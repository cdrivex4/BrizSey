package sc.meteo.seymeteo.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import sc.meteo.seymeteo.data.api.SmaRepository
import sc.meteo.seymeteo.data.db.SeyMeteoDatabase
import sc.meteo.seymeteo.data.db.entity.CachedAlertEntity
import sc.meteo.seymeteo.data.model.CapAlertInfo
import sc.meteo.seymeteo.data.model.UserPersona
import sc.meteo.seymeteo.data.preferences.UserPreferences
import sc.meteo.seymeteo.notification.AlertNotificationBuilder

/**
 * Polls the SMA CAP GeoJSON endpoint every 15 minutes.
 * Applies Decision-Theoretic Cost-Loss thresholding (World Bank Working Paper 11407)
 * based on the user's risk persona to prevent alert fatigue ("The Mistrust Penalty").
 */
class AlertPollerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = SeyMeteoDatabase.getInstance(applicationContext)
            val alertDao = db.alertDao()
            val prefs = UserPreferences(applicationContext)
            val repo = SmaRepository()
            val notificationBuilder = AlertNotificationBuilder(applicationContext)

            val currentPersona = prefs.userPersona.first()
            val knownIds = alertDao.getAllKnownIdentifiers().toSet()

            val result = repo.getCapAlerts()
            result.onSuccess { alerts ->
                val newAlerts = alerts.filter { it.identifier !in knownIds }

                // Fire notifications only for alerts that pass the user's Cost-Loss threshold
                newAlerts.forEach { alert ->
                    if (shouldNotifyForPersona(alert, currentPersona)) {
                        notificationBuilder.notify(alert)
                    }
                }

                // Persist all current alerts to Room DB for in-app viewing
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

    /**
     * Evaluates alert risk weight against the persona's Cost-Loss Ratio (C_prot / C_loss).
     * Mathematical condition for optimal protective action: P_risk >= C_prot / C_loss.
     */
    private fun shouldNotifyForPersona(alert: CapAlertInfo, persona: UserPersona): Boolean {
        val riskWeight = when {
            alert.isExtreme -> 0.95
            alert.isSevere -> 0.70
            alert.severity?.contains("Moderate", ignoreCase = true) == true -> 0.30
            else -> 0.15
        }

        return riskWeight >= persona.costLossRatio
    }
}
