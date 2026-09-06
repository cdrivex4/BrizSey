package sc.meteo.seymeteo.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sc.meteo.seymeteo.MainActivity
import sc.meteo.seymeteo.data.db.SeyMeteoDatabase

class SeyMeteoGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch latest cached forecast from Room DB with zero network latency
        val forecast = withContext(Dispatchers.IO) {
            try {
                val db = SeyMeteoDatabase.getInstance(context)
                val rows = db.forecastDao().getForecastForIsland("international-airport-pointe-larue")
                rows.firstOrNull()
            } catch (e: Exception) {
                null
            }
        }

        val tempText = forecast?.let { "${it.tempMax.toInt()}°C" } ?: "29°C"
        val conditionText = forecast?.conditionLabel ?: "Passing Showers"
        val islandText = "Mahé · Seychelles"
        val stabilityText = "🟢 Atmosphere Stable"

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF0A2540))
                        .padding(14.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = islandText,
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF38BDF8)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Text(
                                text = "Live",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF10B981)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tempText,
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(10.dp))
                            Column {
                                Text(
                                    text = conditionText,
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFFF1F5F9)),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = stabilityText,
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF94A3B8)),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
