package com.plantsnap.data.repository

import android.util.Log
import com.plantsnap.data.remote.supabase.SupabaseProfileDto
import com.plantsnap.data.remote.supabase.toDto
import com.plantsnap.domain.models.SupabaseProfile
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.ui.screens.onboarding.ExperienceLevel
import com.plantsnap.ui.screens.onboarding.PetType
import com.plantsnap.ui.screens.onboarding.PlantInterest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ProfileRepository {

    companion object {
        private const val TAG = "ProfileRepository"
        private const val TABLE = "profiles"
    }

    override suspend fun getProfile(): SupabaseProfile? {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return null
        return try {
            supabase.postgrest.from(TABLE)
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<SupabaseProfileDto>()
                ?.toDomain()
        } catch (e: Exception) {
            Log.w(TAG, "getProfile failed", e)
            null
        }
    }

    override suspend fun hasCompletedOnboarding(): Boolean {
        return getProfile()?.onboardingCompleted ?: false
    }

    override suspend fun updateOnboardingData(
        petType: PetType?,
        plantInterests: Set<PlantInterest>,
        experienceLevel: ExperienceLevel?
    ) {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w(TAG, "updateOnboardingData: not authenticated")
            return
        }
        try {
            supabase.postgrest.from(TABLE)
                .update(
                    OnboardingUpdate(
                        onboardingCompleted = true,
                        petType = petType?.name,
                        plantInterests = plantInterests.map { it.name },
                        experienceLevel = experienceLevel?.name
                    )
                ) { filter { eq("user_id", userId) } }
            Log.d(TAG, "onboarding data updated for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "updateOnboardingData failed", e)
            throw e
        }
    }
}

// Using this instead of mapOf updated values mainly for type safety and reusability
@Serializable
internal data class OnboardingUpdate(
    @SerialName("onboarding_completed") val onboardingCompleted: Boolean,
    @SerialName("pet_type") val petType: String?,
    @SerialName("plant_interests") val plantInterests: List<String>,
    @SerialName("experience_level") val experienceLevel: String?
)
