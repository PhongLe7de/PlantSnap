package com.plantsnap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.data.local.CareTaskDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.ScanDao
import com.plantsnap.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val hasCompletedOnboarding: Boolean? = null,
    val userEmail: String? = null,
    val displayName: String? = null,
    val profilePhotoUrl: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    val supabaseClient: SupabaseClient,
    val profileRepository: ProfileRepository,
    private val scanDao: ScanDao,
    private val savedPlantDao: SavedPlantDao,
    private val careTaskDao: CareTaskDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            supabaseClient.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        _uiState.value = AuthUiState(
                            isLoggedIn = true,
                            hasCompletedOnboarding = profileRepository.hasCompletedOnboarding(),
                            userEmail = user?.email,
                            displayName = user?.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                                ?: user?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull,
                            profilePhotoUrl = user?.userMetadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
                                ?: user?.userMetadata?.get("picture")?.jsonPrimitive?.contentOrNull,
                            isLoading = false
                        )
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _uiState.value = AuthUiState(
                            isLoggedIn = false,
                            hasCompletedOnboarding = true,  // guests skip onboarding
                            isLoading = false
                        )
                    }
                    is SessionStatus.Initializing -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is SessionStatus.RefreshFailure -> {
                        _uiState.value = AuthUiState(
                            isLoggedIn = false,
                            hasCompletedOnboarding = true,  // treat as guest
                            isLoading = false,
                            errorMessage = "Session expired. Please sign in again."
                        )
                    }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            supabaseClient.auth.signOut()
            // Wipe user-scoped local data so the next session (guest or different account)
            // doesn't see leftover scans/plants. plant_details is shared cache, kept.
            careTaskDao.deleteAll()
            savedPlantDao.deleteAll()
            scanDao.deleteAll()
        }
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
