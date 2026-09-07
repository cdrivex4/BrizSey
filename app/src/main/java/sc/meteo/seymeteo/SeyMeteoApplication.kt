package sc.meteo.seymeteo

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sc.meteo.seymeteo.data.location.LocationService
import sc.meteo.seymeteo.data.preferences.UserPreferences
import sc.meteo.seymeteo.notification.SeyMeteoNotificationChannels
import sc.meteo.seymeteo.worker.AlertPollerWorker
import sc.meteo.seymeteo.worker.ForecastSyncWorker
import java.util.concurrent.TimeUnit

class SeyMeteoApplication : Application() {

    val locationService by lazy { LocationService(this) }
    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Create notification channels (required on API 26+)
        SeyMeteoNotificationChannels.createAll(this)

        // Schedule background workers respecting user preferences
        applicationScope.launch {
            val prefs = UserPreferences(this@SeyMeteoApplication)
            val interval = prefs.refreshIntervalMinutes.first()
            scheduleForecastSync(interval)
            scheduleAlertPoller()
        }
    }

    fun scheduleForecastSync(intervalMinutes: Int) {
        val wm = WorkManager.getInstance(this)
        val networkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val forecastWork = PeriodicWorkRequestBuilder<ForecastSyncWorker>(
            intervalMinutes.coerceAtLeast(15).toLong(),
            TimeUnit.MINUTES
        )
            .setConstraints(networkConstraint)
            .build()

        wm.enqueueUniquePeriodicWork(
            "forecast_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            forecastWork
        )
    }

    fun triggerImmediateSync() {
        val wm = WorkManager.getInstance(this)
        val networkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWork = OneTimeWorkRequestBuilder<ForecastSyncWorker>()
            .setConstraints(networkConstraint)
            .build()

        wm.enqueueUniqueWork(
            "manual_forecast_sync",
            ExistingWorkPolicy.REPLACE,
            oneTimeWork
        )
    }

    private fun scheduleAlertPoller() {
        val wm = WorkManager.getInstance(this)
        val networkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val alertWork = PeriodicWorkRequestBuilder<AlertPollerWorker>(15, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .build()

        wm.enqueueUniquePeriodicWork(
            "alert_poller",
            ExistingPeriodicWorkPolicy.KEEP,
            alertWork
        )
    }

    companion object {
        lateinit var instance: SeyMeteoApplication
            private set
    }
}
