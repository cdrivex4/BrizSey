package sc.meteo.seymeteo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IslandLocation(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "slug") val slug: String,
    @Json(name = "coordinates") val coordinates: List<Double> = emptyList()
) {
    val displayName: String
        get() = when {
            slug.contains("pointe-larue", ignoreCase = true) || name.contains("Airport", ignoreCase = true) -> "Mahé"
            slug.contains("praslin", ignoreCase = true) -> "Praslin"
            slug.contains("la-digue", ignoreCase = true) -> "La Digue"
            else -> name
        }

    val subtitle: String
        get() = when {
            slug.contains("pointe-larue", ignoreCase = true) -> "Victoria & Airport"
            slug.contains("praslin", ignoreCase = true) -> "Baie Ste Anne & Grand Anse"
            slug.contains("la-digue", ignoreCase = true) -> "La Passe & Anse Source d'Argent"
            else -> "Seychelles"
        }

    companion object {
        val DEFAULT_MAHE = IslandLocation(
            id = "2a38c33c-eac7-4448-8b58-8b9f305186af",
            name = "International Airport Pointe Larue",
            slug = "international-airport-pointe-larue",
            coordinates = listOf(55.5212, -4.6743)
        )
        val DEFAULT_PRASLIN = IslandLocation(
            id = "c6e42806-16cd-4415-87f6-6585c06a15f3",
            name = "Praslin Seychelles",
            slug = "praslin-seychelles",
            coordinates = listOf(55.7356, -4.3251)
        )
        val DEFAULT_LA_DIGUE = IslandLocation(
            id = "afe6c930-e634-4f8a-ba15-f2f7bb7c6917",
            name = "La Digue",
            slug = "la-digue",
            coordinates = listOf(55.8385, -4.3601)
        )

        val ALL_ISLANDS = listOf(DEFAULT_MAHE, DEFAULT_PRASLIN, DEFAULT_LA_DIGUE)
    }
}
