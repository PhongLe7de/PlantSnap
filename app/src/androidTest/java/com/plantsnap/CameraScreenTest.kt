package com.plantsnap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class CameraScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp(){
        hiltRule.inject()

        composeRule.onNodeWithTag("nav_identify").performClick()
    }

    @Test
    fun camera_screen_is_displayed() {
        composeRule
            .onNodeWithTag("screen_camera")
            .assertIsDisplayed()
    }

    @Test
    fun capture_button_is_displayed_and_enabled() {
        composeRule
            .onNodeWithTag("capture_button")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    // gallery_button_is_displayed TO-DO

    // help_button_is_displayed TO-DO

    @Test
    fun shutter_flash_not_visible_on_load(){
        composeRule
            .onNodeWithTag("shutter_flash")
            .assertDoesNotExist()
    }

    @Test
    fun capture_button_disabled_during_flash(){
        composeRule.onNodeWithTag("capture_button").performClick()

        composeRule
            .onNodeWithTag("capture_button")
            .assertIsNotEnabled()
    }

    @Test
    fun capture_button_re_enables_after_flash() {
        composeRule.onNodeWithTag("capture_button").performClick()

        composeRule.mainClock.advanceTimeBy(400L)

        composeRule
            .onNodeWithTag("capture_button")
            .assertIsEnabled()
    }
}