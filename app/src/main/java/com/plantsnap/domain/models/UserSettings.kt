package com.plantsnap.domain.models

data class UserSettings (
    val theme: AppTheme = AppTheme.SYSTEM,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val plantCareReminders: Boolean = true,
)

enum class AppTheme {
    LIGHT, DARK, SYSTEM;

    val displayName: String get() = when (this) {
        LIGHT -> "Light"
        DARK -> "Dark"
        SYSTEM -> "System default"
    }
}

enum class TemperatureUnit {
    CELSIUS, FAHRENHEIT;

    val displayName: String get() = when (this) {
        CELSIUS -> "Celsius (°C)"
        FAHRENHEIT -> "Fahrenheit (°F)"
    }

    val symbol: String get() =  when (this) {
        CELSIUS -> "°C"
        FAHRENHEIT -> "°F"
    }
}