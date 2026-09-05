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
    suspend fun setOnboardingDone() = context.dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    suspend fun setLastSelectedSlug(slug: String) = context.dataStore.edit { it[KEY_LAST_SELECTED_SLUG] = slug }
    suspend fun setLastSyncMs(ms: Long) = context.dataStore.edit { it[KEY_LAST_SYNC_MS] = ms }
}
