package com.plantsnap.data.remote.supabase

import com.plantsnap.domain.models.SupabaseProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("onboarding_completed") val onboardingCompleted: Boolean = false,
    @SerialName("pet_type") val petType: String? = null,
    @SerialName("plant_interests") val plantInterests: List<String>? = null,
    @SerialName("experience_level") val experienceLevel: String? = null
) {
    fun toDomain(): SupabaseProfile = SupabaseProfile(
        userId = userId,
        createdAt = createdAt,
        onboardingCompleted = onboardingCompleted,
        petType = petType,
        plantInterests = plantInterests,
        experienceLevel = experienceLevel
    )
}

fun SupabaseProfile.toDto(): SupabaseProfileDto = SupabaseProfileDto(
    userId = userId,
    createdAt = createdAt,
    onboardingCompleted = onboardingCompleted,
    petType = petType,
    plantInterests = plantInterests,
    experienceLevel = experienceLevel
)