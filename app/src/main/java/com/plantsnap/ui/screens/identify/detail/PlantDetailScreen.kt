package com.plantsnap.ui.screens.identify.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.ui.state.UiState

@Composable
fun PlantDetailScreen(
    plantId: String,
    onBack: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .testTag("screen_plantDetail")
    ) {
        Text(text = "Plant ID: $plantId")

        when (val s = state) {
            is UiState.Idle -> Text("Idle")
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                val result = s.data
                Text("Best Match: ${result.bestMatch}")
                result.candidates.forEach { candidate ->
                    Text("${candidate.family} (${candidate.scientificName})")
                }
            }
            is UiState.Error -> Text("Error: ${s.message}")
        }

        Button(onClick = onBack) {
            Text(text = "Back to Identification")
        }
    }
}
