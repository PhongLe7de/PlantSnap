package com.plantsnap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        composeRule.onNodeWithTag("nav_history").assertIsDisplayed()
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
    fun tapping_history_tab() {
        composeRule.onNodeWithTag("nav_history").performClick()

        composeRule.onNodeWithTag("screen_history").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_history").assertIsSelected()
    }

    @Test
    fun tapping_profile_tab() {
        composeRule.onNodeWithTag("nav_profile").performClick()

        composeRule.onNodeWithTag("screen_profile").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_profile").assertIsSelected()
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
        composeRule.onNodeWithTag("nav_history").assertIsNotSelected()
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
        composeRule.onNodeWithTag("nav_history").performClick()
        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.onNodeWithTag("nav_identify").performClick()
        composeRule.onNodeWithTag("nav_home").performClick()
        composeRule.onNodeWithTag("nav_history").performClick()

        composeRule.onNodeWithTag("screen_history").assertIsDisplayed()
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
     * Placeholder screens
     */

    @Test
    fun history_placeholder_shows_label(){
        composeRule.onNodeWithTag("nav_history").performClick()
        composeRule.onNodeWithText("History Screen").assertIsDisplayed()
    }

    @Test
    fun profile_placeholder_shows_label(){
        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.onNodeWithText("Welcome to PlantSnap").assertIsDisplayed()
    }


}