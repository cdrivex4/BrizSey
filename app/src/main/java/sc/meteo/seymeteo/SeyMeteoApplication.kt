package sc.meteo.seymeteo

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import sc.meteo.seymeteo.data.location.LocationService
import sc.meteo.seymeteo.notification.SeyMeteoNotificationChannels
import sc.meteo.seymeteo.worker.AlertPollerWorker
import sc.meteo.seymeteo.worker.ForecastSyncWorker
import java.util.concurrent.TimeUnit

class SeyMeteoApplication : Application() {

    val locationService by lazy { LocationService(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Create notification channels (required on API 26+)
        SeyMeteoNotificationChannels.createAll(this)

        // Schedule periodic background workers
        scheduleBackgroundSync()
    }

    private fun scheduleBackgroundSync() {
        val wm = WorkManager.getInstance(this)
        val networkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Forecast sync — every 30 minutes on network
        val forecastWork = PeriodicWorkRequestBuilder<ForecastSyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .build()
        wm.enqueueUniquePeriodicWork(
            "forecast_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            forecastWork
        )

        // Alert poller — every 15 minutes on network (more urgent)
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
