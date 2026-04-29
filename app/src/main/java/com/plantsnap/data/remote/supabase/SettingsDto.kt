package com.plantsnap.data.remote.supabase

import com.plantsnap.domain.models.AppTheme
import com.plantsnap.domain.models.TemperatureUnit
import com.plantsnap.domain.models.UserSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SettingsDto(
    @SerialName("user_id") val userId: String,
    @SerialName("theme") val theme: String = "SYSTEM",
    @SerialName("temperature_unit") val temperatureUnit: String = "CELSIUS",
    @SerialName("language") val language: String = "en",
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerialName("plant_care_reminders") val plantCareReminders: Boolean = true,
){
    fun toDomain(): UserSettings = UserSettings(
        theme = AppTheme.entries.firstOrNull { it.name == theme } ?: AppTheme.SYSTEM,
        temperatureUnit = TemperatureUnit.entries.firstOrNull { it.name == temperatureUnit } ?: TemperatureUnit.CELSIUS,
        language = language,
        notificationsEnabled = notificationsEnabled,
        plantCareReminders = plantCareReminders,
    )
}

@Serializable
data class SettingsUpdate(
    @SerialName("theme") val theme: String,
    @SerialName("temperature_unit") val temperatureUnit: String,
    @SerialName("language") val language: String,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean,
    @SerialName("plant_care_reminders") val plantCareReminders: Boolean,
)

fun UserSettings.toUpdate() = SettingsUpdate(
    theme = theme.name,
    temperatureUnit = temperatureUnit.name,
    language = language,
    notificationsEnabled = notificationsEnabled,
    plantCareReminders = plantCareReminders
)