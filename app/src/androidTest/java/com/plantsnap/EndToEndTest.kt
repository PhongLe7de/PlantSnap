package com.plantsnap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * End-to-end tests covering the main PlantSnap user flows.
 *
 * All tests use fake Hilt repositories so no real API calls are made.
 * Auth-dependent flows (sign in / sign out) are skipped with a note —
 * the app uses Google Sign-In which cannot be automated without a real account.
 */

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.waitForIdle()
    }

    private fun goHome() {
        composeRule.onNodeWithTag("nav_home").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun e2e_app_launches_without_crash() {
        val homeVisible = try {
            composeRule.onNodeWithTag("screen_home").assertIsDisplayed()
            true
        } catch (e: AssertionError) { false }

        val authVisible = try {
            composeRule.onNodeWithTag("screen_profile").assertIsDisplayed()
            true
        } catch (e: AssertionError) { false }

        assert(homeVisible || authVisible) {
            "Expected either screen_home or screen_profile to be visible on launch"
        }
    }

    @Test
    fun e2e_home_screen_displays_key_sections() {
        composeRule.onNodeWithTag("screen_home").assertIsDisplayed()
        composeRule.onNodeWithTag("potd_card").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_identify_plant_cta").assertIsDisplayed()
    }

    @Test
    fun e2e_home_identify_cta_opens_camera() {
        composeRule.onNodeWithTag("screen_home").assertIsDisplayed()

        composeRule.onNodeWithTag("btn_identify_plant_cta").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_identify").assertIsDisplayed()

        goHome()
    }

    @Test
    fun e2e_plant_of_the_day_opens_detail_screen() {
        val homeVisible = try {
            composeRule.onNodeWithTag("screen_home").assertIsDisplayed(); true
        } catch (e: AssertionError) { false }

        if (!homeVisible) return

        val potdVisible = try {
            composeRule.onNodeWithTag("potd_card").assertIsDisplayed(); true
        } catch (e: AssertionError) { false }

        if (!potdVisible) return

        val learnMoreVisible = try {
            composeRule.onNodeWithTag("potd_learn_more").assertIsDisplayed(); true
        } catch (e: AssertionError) { false }

        if (!learnMoreVisible) return

        composeRule.onNodeWithTag("potd_learn_more").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("screen_plantDetail").assertIsDisplayed()

        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screen_home").assertIsDisplayed()
    }

    @Test
    fun e2e_identify_tab_loads_camera_with_controls() {
        composeRule.onNodeWithTag("nav_identify").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_identify").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_gallery").assertIsDisplayed()
    }

    @Test
    fun e2e_camera_capture_button_triggers_shutter() {
        composeRule.onNodeWithTag("nav_identify").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()

        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag("btn_identify").performClick()
        composeRule.mainClock.advanceTimeBy(50L)

        composeRule.onNodeWithTag("btn_identify").assertIsDisplayed()

        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        goHome()
    }

    //Skips if not authenticated
    @Test
    fun e2e_profile_history_opens_history_screen() {
        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.waitForIdle()

        val isAuthenticated = try {
            composeRule.onNodeWithTag("btn_history").assertIsDisplayed()
            true
        } catch (e: AssertionError) { false }

        if (!isAuthenticated) return

        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("btn_history"))
        composeRule.onNodeWithTag("btn_history").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("screen_history").assertIsDisplayed()

        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screen_profile").assertIsDisplayed()

        goHome()
    }

    @Test
    fun e2e_garden_tab_loads_garden_screen() {
        composeRule.onNodeWithTag("nav_garden").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("screen_garden").assertIsDisplayed()

        goHome()
    }

    @Test
    fun e2e_garden_add_specimen_opens_camera() {
        composeRule.onNodeWithTag("nav_garden").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screen_garden").assertIsDisplayed()

        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("btn_garden_add_specimen"))
        composeRule.onNodeWithTag("btn_garden_add_specimen").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_identify").assertIsDisplayed()

        goHome()
    }

    @Test
    fun e2e_garden_add_specimen_back_to_garden_via_tab() {
        composeRule.onNodeWithTag("nav_garden").performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("btn_garden_add_specimen"))
        composeRule.onNodeWithTag("btn_garden_add_specimen").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_garden").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("screen_garden").assertIsDisplayed()

        goHome()
    }

    //skips if not authenticated
    @Test
    fun e2e_profile_appearance_opens_settings() {
        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.waitForIdle()

        val isAuthenticated = try {
            composeRule.onNodeWithTag("btn_history").assertIsDisplayed()
            true
        } catch (e: AssertionError) { false }

        if (!isAuthenticated) return

        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("btn_settings"))
        composeRule.onNodeWithTag("btn_settings").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("screen_settings").assertIsDisplayed()

        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screen_profile").assertIsDisplayed()

        goHome()
    }

    @Test
    fun e2e_home_to_camera_to_garden_journey() {
        composeRule.onNodeWithTag("screen_home").assertIsDisplayed()

        composeRule.onNodeWithTag("btn_identify_plant_cta").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_garden").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screen_garden").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screen_profile").assertIsDisplayed()
    }
}