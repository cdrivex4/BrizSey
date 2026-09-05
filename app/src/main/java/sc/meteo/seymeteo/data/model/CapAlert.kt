package sc.meteo.seymeteo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CapAlertGeoJson(
    @Json(name = "type") val type: String = "FeatureCollection",
    @Json(name = "features") val features: List<CapAlertFeature> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CapAlertFeature(
    @Json(name = "id") val id: String? = null,
    @Json(name = "properties") val properties: CapAlertProperties? = null
)

@JsonClass(generateAdapter = true)
data class CapAlertProperties(
    @Json(name = "identifier") val identifier: String? = null,
    @Json(name = "sender") val sender: String? = null,
    @Json(name = "sent") val sent: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "msgType") val msgType: String? = null,
    @Json(name = "event") val event: String? = null,
    @Json(name = "urgency") val urgency: String? = null,
    @Json(name = "severity") val severity: String? = null,
    @Json(name = "certainty") val certainty: String? = null,
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "instruction") val instruction: String? = null,
    @Json(name = "areaDesc") val areaDesc: String? = null
) {
    val isSevere: Boolean
        get() = severity?.equals("Extreme", ignoreCase = true) == true ||
                severity?.equals("Severe", ignoreCase = true) == true

    val severityBadgeColorHex: Long
        get() = when (severity?.lowercase()) {
            "extreme" -> 0xFFDC2626 // Red
            "severe" -> 0xFFEA580C // Orange
            "moderate" -> 0xFFF59E0B // Amber
            else -> 0xFF0284C7 // Blue info
        }
}

/**
 * Flat domain model for use in workers and notification builders,
 * not tied to the GeoJSON structure.
 */
data class CapAlertInfo(
    val identifier: String,
    val event: String?,
    val severity: String?,
    val urgency: String?,
    val certainty: String?,
    val headline: String?,
    val description: String?,
    val instruction: String?,
    val areaDesc: String?,
    val sent: String?
)

/** Convenience extension to map from GeoJSON feature to flat domain model. */
fun CapAlertFeature.toCapAlertInfo(): CapAlertInfo? {
    val props = properties ?: return null
    val id = props.identifier ?: id ?: return null
    return CapAlertInfo(
        identifier = id,
        event = props.event,
        severity = props.severity,
        urgency = props.urgency,
        certainty = props.certainty,
        headline = props.headline,
        description = props.description,
        instruction = props.instruction,
        areaDesc = props.areaDesc,
        sent = props.sent
    )
}

