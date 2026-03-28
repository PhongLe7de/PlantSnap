package com.plantsnap.ui.screens.history

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .testTag("screen_history")
    ) {
        Text(
            text = "History Screen"
        )
    }
}