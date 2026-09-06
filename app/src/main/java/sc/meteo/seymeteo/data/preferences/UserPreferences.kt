package sc.meteo.seymeteo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sc.meteo.seymeteo.data.model.UserPersona

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/** All user settings, backed by Jetpack DataStore Preferences. */
class UserPreferences(private val context: Context) {

    companion object {
        val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")             // "C" | "F"
        val KEY_WIND_UNIT = stringPreferencesKey("wind_unit")             // "kmh" | "knots" | "ms"
        val KEY_TIME_FORMAT = stringPreferencesKey("time_format")         // "24h" | "12h"
        val KEY_THEME = stringPreferencesKey("theme")                     // "system" | "light" | "dark"
        val KEY_LANGUAGE = stringPreferencesKey("language")               // "en" | "fr" | "crs"
        val KEY_REFRESH_INTERVAL_MIN = intPreferencesKey("refresh_min")   // 15 | 30 | 60
        val KEY_NOTIFY_EXTREME = booleanPreferencesKey("notify_extreme")
        val KEY_NOTIFY_SEVERE = booleanPreferencesKey("notify_severe")
        val KEY_NOTIFY_MODERATE = booleanPreferencesKey("notify_moderate")
        val KEY_USER_PERSONA = stringPreferencesKey("user_persona")       // "general" | "maritime" | "agriculture" | "tourism"
        val KEY_RADAR_PLAYING = booleanPreferencesKey("radar_playing")
        val KEY_RADAR_MUTED = booleanPreferencesKey("radar_muted")
        val KEY_RADAR_OPACITY = androidx.datastore.preferences.core.floatPreferencesKey("radar_opacity")
        val KEY_RADAR_FRAME_INDEX = intPreferencesKey("radar_frame_index")
        val KEY_GLASS_OPACITY = androidx.datastore.preferences.core.floatPreferencesKey("glass_opacity")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_LAST_SELECTED_SLUG = stringPreferencesKey("last_island_slug")
        val KEY_LAST_SYNC_MS = longPreferencesKey("last_sync_ms")
    }

    // ---- Reads ----

    val tempUnit: Flow<String> = context.dataStore.data.map {
        it[KEY_TEMP_UNIT] ?: "C"
    }

    val windUnit: Flow<String> = context.dataStore.data.map {
        it[KEY_WIND_UNIT] ?: "kmh"
    }

    val timeFormat: Flow<String> = context.dataStore.data.map {
        it[KEY_TIME_FORMAT] ?: "24h"
    }

    val theme: Flow<String> = context.dataStore.data.map {
        it[KEY_THEME] ?: "system"
    }

    val language: Flow<String> = context.dataStore.data.map {
        it[KEY_LANGUAGE] ?: "en"
    }

    val refreshIntervalMinutes: Flow<Int> = context.dataStore.data.map {
        it[KEY_REFRESH_INTERVAL_MIN] ?: 30
    }

    val notifyExtreme: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_NOTIFY_EXTREME] ?: true
    }

    val notifySevere: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_NOTIFY_SEVERE] ?: true
    }

    val notifyModerate: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_NOTIFY_MODERATE] ?: false
    }

    val userPersona: Flow<UserPersona> = context.dataStore.data.map {
        UserPersona.fromId(it[KEY_USER_PERSONA] ?: UserPersona.GENERAL_CITIZEN.id)
    }

    val radarIsPlaying: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_RADAR_PLAYING] ?: false
    }

    val radarIsMuted: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_RADAR_MUTED] ?: true
    }

    val radarOpacity: Flow<Float> = context.dataStore.data.map {
        it[KEY_RADAR_OPACITY] ?: 0.55f
    }

    val radarFrameIndex: Flow<Int> = context.dataStore.data.map {
        it[KEY_RADAR_FRAME_INDEX] ?: 0
    }

    val glassOpacity: Flow<Float> = context.dataStore.data.map {
        it[KEY_GLASS_OPACITY] ?: 0.35f
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_ONBOARDING_DONE] ?: false
    }

    val lastSelectedSlug: Flow<String> = context.dataStore.data.map {
        it[KEY_LAST_SELECTED_SLUG] ?: "international-airport-pointe-larue"
    }

    val lastSyncMs: Flow<Long> = context.dataStore.data.map {
        it[KEY_LAST_SYNC_MS] ?: 0L
    }

    // ---- Writes ----

    suspend fun setTempUnit(value: String) = context.dataStore.edit { it[KEY_TEMP_UNIT] = value }
    suspend fun setWindUnit(value: String) = context.dataStore.edit { it[KEY_WIND_UNIT] = value }
    suspend fun setTimeFormat(value: String) = context.dataStore.edit { it[KEY_TIME_FORMAT] = value }
    suspend fun setTheme(value: String) = context.dataStore.edit { it[KEY_THEME] = value }
    suspend fun setLanguage(value: String) = context.dataStore.edit { it[KEY_LANGUAGE] = value }
    suspend fun setRefreshIntervalMinutes(value: Int) = context.dataStore.edit { it[KEY_REFRESH_INTERVAL_MIN] = value }
    suspend fun setNotifyExtreme(value: Boolean) = context.dataStore.edit { it[KEY_NOTIFY_EXTREME] = value }
    suspend fun setNotifySevere(value: Boolean) = context.dataStore.edit { it[KEY_NOTIFY_SEVERE] = value }
    suspend fun setNotifyModerate(value: Boolean) = context.dataStore.edit { it[KEY_NOTIFY_MODERATE] = value }
    suspend fun setUserPersona(persona: UserPersona) = context.dataStore.edit { it[KEY_USER_PERSONA] = persona.id }
    suspend fun setRadarIsPlaying(value: Boolean) = context.dataStore.edit { it[KEY_RADAR_PLAYING] = value }
    suspend fun setRadarIsMuted(value: Boolean) = context.dataStore.edit { it[KEY_RADAR_MUTED] = value }
    suspend fun setRadarOpacity(value: Float) = context.dataStore.edit { it[KEY_RADAR_OPACITY] = value }
    suspend fun setRadarFrameIndex(value: Int) = context.dataStore.edit { it[KEY_RADAR_FRAME_INDEX] = value }
    suspend fun setGlassOpacity(value: Float) = context.dataStore.edit { it[KEY_GLASS_OPACITY] = value }
    suspend fun setOnboardingDone() = context.dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    suspend fun setLastSelectedSlug(slug: String) = context.dataStore.edit { it[KEY_LAST_SELECTED_SLUG] = slug }
    suspend fun setLastSyncMs(ms: Long) = context.dataStore.edit { it[KEY_LAST_SYNC_MS] = ms }
}
