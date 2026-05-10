package com.plantsnap

import com.plantsnap.data.local.CareTaskDao
import com.plantsnap.data.local.SavedPlantDao
import com.plantsnap.data.local.ScanDao
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.ui.screens.profile.AuthViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var sessionStatusFlow: MutableStateFlow<SessionStatus>
    private lateinit var profileRepository: ProfileRepository
    private lateinit var auth: Auth
    private lateinit var supabaseClient: SupabaseClient
    private lateinit var viewModel: AuthViewModel


    private fun authenticatedStatus(
        email: String? = "user@example.com",
        fullName: String? = "Jane Doe",
        avatarUrl: String? = "https://example.com/photo.jpg",
    ): SessionStatus.Authenticated {
        val metadata = buildJsonObject {
            fullName?.let { put("full_name", JsonPrimitive(it)) }
            avatarUrl?.let { put("avatar_url", JsonPrimitive(it)) }
        }
        val user: UserInfo = mockk {
            every { this@mockk.email } returns email
            every { userMetadata } returns metadata
        }
        val session: UserSession = mockk {
            every { this@mockk.user } returns user
        }
        return mockk<SessionStatus.Authenticated> {
            every { this@mockk.session } returns session
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionStatusFlow = MutableStateFlow(SessionStatus.Initializing)
        profileRepository = mockk()

        mockkStatic("io.github.jan.supabase.auth.AuthKt")
        auth = mockk(relaxed = true)
        supabaseClient = mockk(relaxed = true)
        every { auth.sessionStatus } returns sessionStatusFlow
        every { supabaseClient.auth } returns auth

        coEvery { profileRepository.hasCompletedOnboarding() } returns true
        viewModel = AuthViewModel(
            supabaseClient,
            profileRepository,
            scanDao = mockk(relaxed = true),
            savedPlantDao = mockk(relaxed = true),
            careTaskDao = mockk(relaxed = true),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic("io.github.jan.supabase.auth.AuthKt")
    }

    @Test
    fun `initial uiState has isLoading true before any session emission`() {
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `Initializing sets isLoading true and isLoggedIn false`() = runTest {
        sessionStatusFlow.emit(SessionStatus.Initializing)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `Authenticated sets isLoggedIn true and isLoading false`() = runTest {
        sessionStatusFlow.emit(authenticatedStatus())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Authenticated populates userEmail`() = runTest {
        sessionStatusFlow.emit(authenticatedStatus(email = "jane@example.com"))
        advanceUntilIdle()

        assertEquals("jane@example.com", viewModel.uiState.value.userEmail)
    }

    @Test
    fun `Authenticated populates displayName from full_name metadata`() = runTest {
        sessionStatusFlow.emit(authenticatedStatus(fullName = "Jane Doe"))
        advanceUntilIdle()

        assertEquals("Jane Doe", viewModel.uiState.value.displayName)
    }

    @Test
    fun `Authenticated populates profilePhotoUrl from avatar_url metadata`() = runTest {
        sessionStatusFlow.emit(authenticatedStatus(avatarUrl = "https://example.com/photo.jpg"))
        advanceUntilIdle()

        assertEquals("https://example.com/photo.jpg", viewModel.uiState.value.profilePhotoUrl)
    }

    @Test
    fun `Authenticated reflects hasCompletedOnboarding true from repository`() = runTest {
        coEvery { profileRepository.hasCompletedOnboarding() } returns true
        sessionStatusFlow.emit(authenticatedStatus())
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.hasCompletedOnboarding)
    }

    @Test
    fun `Authenticated reflects hasCompletedOnboarding false from repository`() = runTest {
        coEvery { profileRepository.hasCompletedOnboarding() } returns false
        sessionStatusFlow.emit(authenticatedStatus())
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.hasCompletedOnboarding)
    }

    @Test
    fun `Authenticated calls profileRepository exactly once`() = runTest {
        sessionStatusFlow.emit(authenticatedStatus())
        advanceUntilIdle()

        coVerify(exactly = 1) { profileRepository.hasCompletedOnboarding() }
    }

    @Test
    fun `Authenticated clears a previous errorMessage`() = runTest {
        sessionStatusFlow.emit(SessionStatus.RefreshFailure(mockk<RefreshFailureCause>(relaxed = true)))
        advanceUntilIdle()
        assertEquals("Session expired. Please sign in again.", viewModel.uiState.value.errorMessage)

        sessionStatusFlow.emit(authenticatedStatus())
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `NotAuthenticated sets isLoggedIn false and isLoading false`() = runTest {
        sessionStatusFlow.emit(SessionStatus.NotAuthenticated(isSignOut = false))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `NotAuthenticated sets hasCompletedOnboarding true so guests skip onboarding`() = runTest {
        sessionStatusFlow.emit(SessionStatus.NotAuthenticated(isSignOut = false))
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.hasCompletedOnboarding)
    }

    @Test
    fun `NotAuthenticated has null errorMessage`() = runTest {
        sessionStatusFlow.emit(SessionStatus.NotAuthenticated(isSignOut = false))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `NotAuthenticated does not call profileRepository`() = runTest {
        sessionStatusFlow.emit(SessionStatus.NotAuthenticated(isSignOut = false))
        advanceUntilIdle()

        coVerify(exactly = 0) { profileRepository.hasCompletedOnboarding() }
    }

    @Test
    fun `RefreshFailure sets isLoggedIn false and isLoading false`() = runTest {
        sessionStatusFlow.emit(SessionStatus.RefreshFailure(mockk<RefreshFailureCause>(relaxed = true)))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `RefreshFailure sets the session expired error message`() = runTest {
        sessionStatusFlow.emit(SessionStatus.RefreshFailure(mockk<RefreshFailureCause>(relaxed = true)))
        advanceUntilIdle()

        assertEquals("Session expired. Please sign in again.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `RefreshFailure sets hasCompletedOnboarding true treating user as guest`() = runTest {
        sessionStatusFlow.emit(SessionStatus.RefreshFailure(mockk<RefreshFailureCause>(relaxed = true)))
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.hasCompletedOnboarding)
    }

    @Test
    fun `signOut calls supabase auth signOut`() = runTest {
        viewModel.signOut()
        advanceUntilIdle()

        coVerify(exactly = 1) { auth.signOut() }
    }

    @Test
    fun `signOut followed by NotAuthenticated sets isLoggedIn false`() = runTest {
        sessionStatusFlow.emit(authenticatedStatus())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoggedIn)

        viewModel.signOut()
        sessionStatusFlow.emit(SessionStatus.NotAuthenticated(isSignOut = false))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `setError updates errorMessage`() = runTest {
        viewModel.setError("Google sign-in cancelled")

        assertEquals("Google sign-in cancelled", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `setError preserves the rest of uiState`() = runTest {
        sessionStatusFlow.emit(authenticatedStatus(email = "jane@example.com"))
        advanceUntilIdle()

        viewModel.setError("Something went wrong")

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertEquals("jane@example.com", viewModel.uiState.value.userEmail)
        assertEquals("Something went wrong", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `setError called twice keeps the last message`() {
        viewModel.setError("First error")
        viewModel.setError("Second error")

        assertEquals("Second error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearError sets errorMessage to null`() {
        viewModel.setError("Some error")

        viewModel.clearError()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearError preserves the rest of uiState`() = runTest {
        sessionStatusFlow.emit(authenticatedStatus(email = "jane@example.com"))
        advanceUntilIdle()
        viewModel.setError("Oops")

        viewModel.clearError()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertEquals("jane@example.com", viewModel.uiState.value.userEmail)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearError is a no-op when errorMessage is already null`() = runTest {
        sessionStatusFlow.emit(SessionStatus.NotAuthenticated(isSignOut = false))
        advanceUntilIdle()

        viewModel.clearError()

        assertNull(viewModel.uiState.value.errorMessage)
    }

}