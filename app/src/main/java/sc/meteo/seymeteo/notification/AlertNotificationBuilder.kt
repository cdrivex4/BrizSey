package sc.meteo.seymeteo.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import sc.meteo.seymeteo.MainActivity
import sc.meteo.seymeteo.R
import sc.meteo.seymeteo.data.model.CapAlertInfo

class AlertNotificationBuilder(private val context: Context) {

    private val nm = context.getSystemService<NotificationManager>()

    fun notify(alert: CapAlertInfo) {
        val channel = when (alert.severity?.uppercase()) {
            "EXTREME" -> SeyMeteoNotificationChannels.CHANNEL_EXTREME
            "SEVERE" -> SeyMeteoNotificationChannels.CHANNEL_SEVERE
            "MODERATE" -> SeyMeteoNotificationChannels.CHANNEL_MODERATE
            else -> SeyMeteoNotificationChannels.CHANNEL_GENERAL
        }

        val tapIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("alert_id", alert.identifier)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val severityEmoji = when (alert.severity?.uppercase()) {
            "EXTREME" -> "🔴"
            "SEVERE" -> "🟠"
            "MODERATE" -> "🟡"
            else -> "🔵"
        }

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$severityEmoji ${alert.event ?: "Weather Alert"}")
            .setContentText(alert.headline ?: alert.description ?: "Tap for details")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(alert.description ?: "")
                    .setSummaryText(alert.areaDesc ?: "Seychelles")
            )
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .setPriority(
                when (alert.severity?.uppercase()) {
                    "EXTREME" -> NotificationCompat.PRIORITY_MAX
                    "SEVERE" -> NotificationCompat.PRIORITY_HIGH
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .build()

        nm?.notify(alert.identifier.hashCode(), notification)
    }
}
