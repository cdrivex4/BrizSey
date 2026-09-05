package sc.meteo.seymeteo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

object SeyMeteoNotificationChannels {

    const val CHANNEL_EXTREME = "ch_extreme_alerts"
    const val CHANNEL_SEVERE = "ch_severe_alerts"
    const val CHANNEL_MODERATE = "ch_moderate_alerts"
    const val CHANNEL_GENERAL = "ch_general"

    fun createAll(context: Context) {
        val nm = context.getSystemService<NotificationManager>() ?: return

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_EXTREME,
                "Extreme Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Cyclone, tsunami, and extreme weather warnings"
                enableVibration(true)
                enableLights(true)
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SEVERE,
                "Severe Weather Warnings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Heavy rain, storm surge, and severe wind warnings"
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_MODERATE,
                "Advisory & Watches",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Small craft advisories and weather watches"
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GENERAL,
                "General Updates",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Forecast refresh and sync notifications"
            }
        )
    }
}
