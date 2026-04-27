package com.plantsnap.domain.repository

import com.plantsnap.domain.models.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<UserSettings>

    suspend fun getSettings(): UserSettings

    suspend fun updateSettings(settings: UserSettings)
}