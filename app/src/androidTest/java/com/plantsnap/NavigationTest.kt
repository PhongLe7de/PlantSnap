package com.plantsnap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
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

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    /**
     * Verifies that app starts on Home screen
     */
    @Test
    fun app_launches_on_home_screen() {
        composeRule
            .onNodeWithTag("screen_home")
            .assertIsDisplayed()
    }

    /**
     * All four nav items visible
     */
    @Test
    fun bottom_nav_displays_all_tabs(){
        composeRule.onNodeWithTag("nav_home").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_identify").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_garden").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_profile").assertIsDisplayed()

    }

    /**
     * On launch Home tab selected
     */
    @Test
    fun home_tab_is_selected() {
        composeRule
            .onNodeWithTag("nav_home")
            .assertIsSelected()
    }

    @Test
    fun tapping_camera_tab() {
        composeRule.onNodeWithTag("nav_identify").performClick()

        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_identify").assertIsSelected()
    }

    @Test
    fun tapping_my_garden_tab() {
        composeRule.onNodeWithTag("nav_favorite").performClick()

        composeRule.onNodeWithTag("screen_favorite").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_favorite").assertIsSelected()
    }

    @Test
    fun tapping_profile_tab() {
        composeRule.onNodeWithTag("nav_profile").performClick()

        composeRule.onNodeWithTag("screen_profile").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_profile").assertIsSelected()
    }

    @Test
    fun tapping_garden_tab() {
        composeRule.onNodeWithTag("nav_garden").performClick()

        composeRule.onNodeWithTag("screen_garden").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_garden").assertIsSelected()
    }

    /**
     * Back navigation
     */

    @Test
    fun tapping_home_tab_from_another_tab() {
        composeRule.onNodeWithTag("nav_identify").performClick()
        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_home").performClick()

        composeRule.onNodeWithTag("screen_home").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_home").assertIsSelected()
    }

    /**
     * Active tab selection
     */
    @Test
    fun only_one_tab_is_selected() {
        composeRule.onNodeWithTag("nav_identify").performClick()

        composeRule.onNodeWithTag("nav_identify").assertIsSelected()
        composeRule.onNodeWithTag("nav_home").assertIsNotSelected()
        composeRule.onNodeWithTag("nav_garden").assertIsNotSelected()
        composeRule.onNodeWithTag("nav_profile").assertIsNotSelected()
    }

    /**
     * Back stack stability
     */
    @Test
    fun re_tapping_active_tab(){
        composeRule.onNodeWithTag("nav_home").performClick()
        composeRule.onNodeWithTag("screen_home").assertIsDisplayed()
    }

    /**
     * Rapid switching of tabs
     */
    @Test
    fun rapid_tab_switching() {
        composeRule.onNodeWithTag("nav_identify").performClick()
        composeRule.onNodeWithTag("nav_favorite").performClick()
        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.onNodeWithTag("nav_identify").performClick()
        composeRule.onNodeWithTag("nav_home").performClick()
        composeRule.onNodeWithTag("nav_favorite").performClick()

        composeRule.onNodeWithTag("screen_favorite").assertIsDisplayed()
    }

    /**
     * CTA button
     */

    /**
     * Identify Plant on Home navigates to Camera
     */
    @Test
    fun identify_plant_cta_navigates_to_camera() {
        composeRule.onNodeWithTag("screen_home").assertIsDisplayed()

        composeRule.onNodeWithTag("btn_identify_plant_cta").performClick()

        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()
    }

    /**
     * AddSpecimenCard on My Garden navigates to the camera screen.
     */
    @Test
    fun garden_add_specimen_navigates_to_camera() {
        composeRule.onNodeWithTag("nav_garden").performClick()
        composeRule.onNodeWithTag("screen_garden").assertIsDisplayed()

        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("btn_garden_add_specimen"))
        composeRule.onNodeWithTag("btn_garden_add_specimen").performClick()

        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_identify").assertIsSelected()
    }

    /**
     * Regression: Garden -> AddSpecimen -> Camera -> tap Garden tab must land
     * on Garden, not bounce back to Identify (back-stack ordering bug).
     */
    @Test
    fun garden_to_camera_then_back_to_garden() {
        composeRule.onNodeWithTag("nav_garden").performClick()
        composeRule.onNodeWithTag("screen_garden").assertIsDisplayed()

        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("btn_garden_add_specimen"))
        composeRule.onNodeWithTag("btn_garden_add_specimen").performClick()
        composeRule.onNodeWithTag("screen_camera").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_garden").performClick()

        composeRule.onNodeWithTag("screen_garden").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_garden").assertIsSelected()
    }
}