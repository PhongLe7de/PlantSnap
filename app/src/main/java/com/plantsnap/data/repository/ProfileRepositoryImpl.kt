package com.plantsnap.data.repository

import android.util.Log
import com.plantsnap.data.remote.supabase.SupabaseProfileDto
import com.plantsnap.data.remote.supabase.toDto
import com.plantsnap.domain.models.SupabaseProfile
import com.plantsnap.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
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

    override suspend fun updateOnboardingData(profile: SupabaseProfile) {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.w(TAG, "updateOnboardingData: not authenticated")
            return
        }
        try {
            supabase.postgrest.from(TABLE)
                .update(profile.toDto()) { filter { eq("user_id", userId) } }
            Log.d(TAG, "onboarding data updated for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "updateOnboardingData failed", e)
            throw e
        }
    }
}
