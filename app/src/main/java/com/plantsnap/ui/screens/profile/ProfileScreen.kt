package com.plantsnap.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .testTag("screen_profile")
    ) {
        Text(
            text = "Profile Screen"
        )
    }
}