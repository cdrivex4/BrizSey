package sc.meteo.seymeteo.data.model

/**
 * User Persona profiles grounded in World Bank Group Policy Research (Working Paper 11407)
 * on Cost-Loss ratios (C_prot / C_loss) and optimal protective thresholding.
 */
enum class UserPersona(
    val id: String,
    val title: String,
    val subtitle: String,
    val costLossRatio: Double,
    val description: String
) {
    GENERAL_CITIZEN(
        id = "general",
        title = "General / Commuter",
        subtitle = "Optimized to eliminate false alarms",
        costLossRatio = 0.65,
        description = "High disruption cost. Only triggers notifications for high-certainty severe storms, heavy downpours, and cyclone/tsunami bulletins."
    ),
    MARITIME_FISHER(
        id = "maritime",
        title = "Maritime / Fisher",
        subtitle = "High sea risk sensitivity",
        costLossRatio = 0.20,
        description = "Low protective cost, critical vessel & life risk. Notifies on localized wind gusts >= 35 km/h, rough swell (>2m), and sudden squalls."
    ),
    FARMER_AGRICULTURE(
        id = "agriculture",
        title = "Agriculture / Farmer",
        subtitle = "Precipitation & soil moisture tracking",
        costLossRatio = 0.35,
        description = "Sensitive to rainfall duration, dry spells, and strong convective wind damage to crops and infrastructure."
    ),
    TOURISM_OUTDOOR(
        id = "tourism",
        title = "Tourism & Excursions",
        subtitle = "Balanced island-hopping safety",
        costLossRatio = 0.50,
        description = "Balanced threshold tailored for boat charters, beach excursions, trail hiking, and inter-island ferry transit."
    );

    companion object {
        fun fromId(id: String): UserPersona {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GENERAL_CITIZEN
        }
    }
}
