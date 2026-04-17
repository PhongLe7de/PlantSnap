package com.plantsnap.domain.repository

import com.plantsnap.domain.models.SupabaseProfile

interface ProfileRepository {
    suspend fun getProfile(): SupabaseProfile?
    suspend fun updateOnboardingData(profile: SupabaseProfile)
}
