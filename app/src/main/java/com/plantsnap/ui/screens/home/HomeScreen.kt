package com.plantsnap.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.ui.state.UiState

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Column(
        modifier = Modifier
            .testTag("screen_home")
    ) {
        Text(
            text = "Home Screen"
        )
        when (val s = state) {
            is UiState.Idle -> Text("Idle")
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                val plants = s.data
                if (plants.isEmpty()) {
                    Text("No plants found")
                } else {
                    plants.forEach { plant ->
                        Text("${plant.family} (${plant.scientificName})")
                    }
                }

            }

            is UiState.Error -> Text("Error: ${s.message}")
        }

    }
}