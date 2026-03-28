package com.plantsnap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val userEmail: String? = null,
    val displayName: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    val supabaseClient: SupabaseClient
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
                            userEmail = user?.email,
                            displayName = user?.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                                ?: user?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull,
                            isLoading = false
                        )
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _uiState.value = AuthUiState(isLoggedIn = false, isLoading = false)
                    }
                    is SessionStatus.Initializing -> {
                        _uiState.value = AuthUiState(isLoading = true)
                    }
                    is SessionStatus.RefreshFailure -> {
                        _uiState.value = AuthUiState(
                            isLoggedIn = false,
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
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
