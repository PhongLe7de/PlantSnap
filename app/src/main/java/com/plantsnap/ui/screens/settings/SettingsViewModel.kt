package com.plantsnap.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.domain.models.AppTheme
import com.plantsnap.domain.models.TemperatureUnit
import com.plantsnap.domain.models.UserSettings
import com.plantsnap.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository
        .observeSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserSettings()
        )

    fun setTheme(theme: AppTheme) = update { copy(theme = theme) }

    fun setTemperatureUnit(unit: TemperatureUnit) = update { copy(temperatureUnit = unit) }

    fun setLanguage(language: String) = update { copy(language = language) }

    fun setNotificationsEnabled(enabled: Boolean) = update { copy(notificationsEnabled = enabled) }

    fun setPlantCareReminders(enabled: Boolean) = update { copy(plantCareReminders = enabled) }

    private fun update(transform: UserSettings.() -> UserSettings) {
        viewModelScope.launch {
            val updated = settings.value.transform()
            settingsRepository.updateSettings(updated)
        }
    }
}