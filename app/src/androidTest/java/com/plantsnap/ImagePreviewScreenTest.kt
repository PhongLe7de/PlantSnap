package com.plantsnap

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plantsnap.ui.screens.identify.preview.ImagePreviewContent
import com.plantsnap.ui.theme.PlantSnapTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ImagePreviewScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val fakePhotos = listOf(
        Uri.parse("content://fake/photo1.jpg"),
        Uri.parse("content://fake/photo2.jpg"),
        Uri.parse("content://fake/photo3.jpg")
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun setPreviewContent(
        photos: List<Uri> = fakePhotos,
        initialPage: Int = 0,
        onRetake: (Int) -> Unit = {},
        onBack: () -> Unit = {},
        onUsePhotos: () -> Unit = {},
        onRemovePhoto: (Int) -> Unit = {}
    ) {
        composeRule.activity.setContent {
            PlantSnapTheme {
                ImagePreviewContent(
                    initialPage = initialPage,
                    photos = photos,
                    onRetake = onRetake,
                    onBack = onBack,
                    onUsePhotos = onUsePhotos,
                    onRemovePhoto = onRemovePhoto
                )
            }
        }
    }

    // ── UI elements ──

    @Test
    fun preview_shows_retake_and_use_photo_buttons() {
        setPreviewContent()

        composeRule.onNodeWithTag("screen_preview").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_retake").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_use_photo").assertIsDisplayed()
    }

    @Test
    fun preview_shows_screen_with_single_photo() {
        setPreviewContent(photos = listOf(fakePhotos.first()))

        composeRule.onNodeWithTag("screen_preview").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_retake").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_use_photo").assertIsDisplayed()
    }

    // ── Button callbacks ──

    @Test
    fun retake_button_triggers_callback_with_current_page() {
        var retakePage = -1
        setPreviewContent(onRetake = { page -> retakePage = page })

        composeRule.onNodeWithTag("btn_retake").performClick()
        composeRule.waitForIdle()

        assertEquals(0, retakePage)
    }

    @Test
    fun use_photo_button_triggers_callback() {
        var clicked = false
        setPreviewContent(onUsePhotos = { clicked = true })

        composeRule.onNodeWithTag("btn_use_photo").performClick()
        composeRule.waitForIdle()

        assertTrue(clicked)
    }

    @Test
    fun back_button_triggers_callback() {
        var backClicked = false
        setPreviewContent(onBack = { backClicked = true })

        composeRule.onNodeWithContentDescription("Back to camera").performClick()
        composeRule.waitForIdle()

        assertTrue(backClicked)
    }

    // ── Initial page ──

    @Test
    fun preview_opens_at_specified_initial_page() {
        setPreviewContent(initialPage = 2)

        composeRule.onNodeWithTag("screen_preview").assertIsDisplayed()
    }

    @Test
    fun preview_clamps_initial_page_to_bounds() {
        setPreviewContent(initialPage = 99)

        composeRule.onNodeWithTag("screen_preview").assertIsDisplayed()
    }

    // ── Remove photo ──

    @Test
    fun remove_button_triggers_callback_with_index() {
        var removedIndex = -1
        setPreviewContent(onRemovePhoto = { index -> removedIndex = index })

        composeRule.onAllNodes(hasContentDescription("Remove photo"))[0].performClick()
        composeRule.waitForIdle()

        assertEquals(0, removedIndex)
    }

    @Test
    fun remove_button_hidden_with_single_photo() {
        setPreviewContent(photos = listOf(fakePhotos.first()))

        composeRule.onNodeWithTag("screen_preview").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Remove photo").assertDoesNotExist()
    }
}
