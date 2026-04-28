package com.plantsnap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plantsnap.data.local.PlantSnapDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import javax.inject.Inject


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: PlantSnapDatabase

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
        db.clearAllTables()
        composeRule.onNodeWithTag("nav_history").performClick()
        composeRule.waitForIdle()
    }

    @After
    fun tearDown() {
        composeRule.onNodeWithTag("nav_home").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun history_screen_is_displayed() {
        composeRule
            .onNodeWithTag("screen_history")
            .assertIsDisplayed()
    }

    @Test
    fun empty_state_shows_no_scans_message() {
        composeRule.waitForIdle()
        composeRule
            .onNodeWithTag("No scans yet")
            .assertIsDisplayed()
    }

    @Test
    fun empty_state_shows_helper_test() {
        composeRule.waitForIdle()
        composeRule
            .onNodeWithText("Your identified plants will appear here")
            .assertIsDisplayed()
    }

}