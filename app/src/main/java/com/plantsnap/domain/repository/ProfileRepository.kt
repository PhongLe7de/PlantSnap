package com.plantsnap.domain.repository

import com.plantsnap.domain.models.SupabaseProfile
import com.plantsnap.ui.screens.onboarding.ExperienceLevel
import com.plantsnap.ui.screens.onboarding.PetType
import com.plantsnap.ui.screens.onboarding.PlantInterest

interface ProfileRepository {
    suspend fun getProfile(): SupabaseProfile?
    suspend fun hasCompletedOnboarding(): Boolean
    suspend fun updateOnboardingData(
        petType: PetType?,
        plantInterests: Set<PlantInterest>,
        experienceLevel: ExperienceLevel?
    )
}
