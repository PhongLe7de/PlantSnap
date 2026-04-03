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
import androidx.test.rule.GrantPermissionRule
import org.junit.After


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CameraScreenTest {

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
    fun setUp(){
        hiltRule.inject()
        composeRule.onNodeWithTag("nav_home").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_identify").performClick()
        composeRule.waitForIdle()
    }

    @After
    fun tearDown() {
        composeRule.mainClock.autoAdvance = true
        composeRule.onNodeWithTag("nav_home").performClick()
        composeRule.waitForIdle()
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
            .onNodeWithTag("btn_identify")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    //TODO gallery_button_is_displayed

    //TODO help_button_is_displayed

    @Test
    fun shutter_flash_not_visible_on_load(){
        composeRule
            .onNodeWithTag("shutter_flash")
            .assertDoesNotExist()
    }

    @Test
    fun capture_button_disabled_during_flash(){
        composeRule.mainClock.autoAdvance = false

        composeRule.onNodeWithTag("btn_identify").performClick()
        composeRule.mainClock.advanceTimeBy(50L)

        composeRule
            .onNodeWithTag("btn_identify")
            .assertIsNotEnabled()

        composeRule.mainClock.autoAdvance = true
    }

}