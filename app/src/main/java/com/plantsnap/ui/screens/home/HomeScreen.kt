package com.plantsnap.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .testTag("screen_home")
    ) {
        Text(
            text = "Home Screen"
        )
    }
}