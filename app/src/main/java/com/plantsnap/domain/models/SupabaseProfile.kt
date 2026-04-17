package com.plantsnap.domain.models

data class SupabaseProfile(
    val id: String,
    val userId: String,
    val createdAt: String,
    val onboardingCompleted: Boolean = false,
    val petType: String? = null,
    val plantInterests: List<String>? = null,
    val experienceLevel: String? = null
)
