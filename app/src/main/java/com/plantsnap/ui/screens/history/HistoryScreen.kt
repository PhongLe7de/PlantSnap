package com.plantsnap.ui.screens.history

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.ui.state.UiState

@Composable
fun HistoryScreen(
    viewModelView: HistoryViewModel = hiltViewModel()
) {
    val state by viewModelView.uiState.collectAsState()

    Column(
        modifier = Modifier
            .testTag("screen_history")
    ) {
        Text(
            text = "History Screen"
        )

        when (val s = state) {
            is UiState.Idle -> Text("Idle")
            is UiState.Loading -> Text("Loading...")
            is UiState.Success -> {
                val history = s.data
                if (history == Unit) {
                    Text("No history found")
                } else {
                    Text("History loaded successfully")
                }
            }
            is UiState.Error -> Text("Error: ${s.message}")
        }
    }
}