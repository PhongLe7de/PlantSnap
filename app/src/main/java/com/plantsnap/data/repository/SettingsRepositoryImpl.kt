package com.plantsnap.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.plantsnap.data.remote.supabase.SettingsDto
import com.plantsnap.domain.models.AppTheme
import com.plantsnap.domain.models.TemperatureUnit
import com.plantsnap.domain.models.UserSettings
import com.plantsnap.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabase: SupabaseClient,
) :  SettingsRepository {


    companion object {
        private const val TAG = "SettingsRepository"
        private const val TABLE = "settings"

        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_UNIT = stringPreferencesKey("temperature_unit")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        private val KEY_REMINDERS = booleanPreferencesKey("plant_care_reminders")
    }

    override fun observeSettings(): Flow<UserSettings> =
        context.settingsDataStore.data.map { it.toSettings() }

    override suspend fun getSettings(): UserSettings {
        val cached = context.settingsDataStore.data.first().toSettings()
        val remote = fetchFromSupabase()
        if (remote != null) {
            writeToCache(remote)
            return remote
        }
        return cached
    }

    override suspend fun updateSettings(settings: UserSettings) {
        writeToCache(settings)
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w(TAG, "updateSettings: not authenticated, saved locally only")
            return
        }
        try {
            supabase.postgrest.from(TABLE)
                .upsert(
                    mapOf(
                        "user_id" to userId,
                        "theme" to settings.theme.name,
                        "temperature_unit" to settings.temperatureUnit,
                        "language" to settings.language,
                        "notifications_enabled" to settings.notificationsEnabled,
                        "plant_care_reminders" to settings.plantCareReminders
                    )
                )
            Log.d(TAG, "settings synced to Supabase for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "updateSettings: Supabase sync failed (cached locally)", e)
        }
    }

    private suspend fun fetchFromSupabase(): UserSettings? {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return null
        return try {
            supabase.postgrest.from(TABLE)
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<SettingsDto>()
                ?.toDomain()
        } catch (e: Exception) {
            Log.w(TAG, "fetchFromSupabase failed", e)
            null
        }
    }

    private suspend fun writeToCache(settings: UserSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_THEME] = settings.theme.name
            prefs[KEY_UNIT] = settings.temperatureUnit.name
            prefs[KEY_LANGUAGE] = settings.language
            prefs[KEY_NOTIFICATIONS] = settings.notificationsEnabled
            prefs[KEY_REMINDERS] = settings.plantCareReminders
        }
    }

    private fun Preferences.toSettings() = UserSettings(
        theme = AppTheme.entries.firstOrNull { it.name == this[KEY_THEME] } ?: AppTheme.SYSTEM,
        temperatureUnit = TemperatureUnit.entries.firstOrNull { it.name == this[KEY_UNIT] } ?: TemperatureUnit.CELSIUS,
        language = this[KEY_LANGUAGE] ?: "en",
        notificationsEnabled = this[KEY_NOTIFICATIONS] ?: true,
        plantCareReminders =  this[KEY_REMINDERS] ?: true,
    )
}