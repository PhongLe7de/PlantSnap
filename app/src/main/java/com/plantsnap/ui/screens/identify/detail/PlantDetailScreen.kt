package com.plantsnap.ui.screens.identify.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
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
fun PlantDetailScreen(
    plantId: String,
    candidateIndex: Int,
    onBack: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPlantDetail(plantId, candidateIndex)
    }

    Column(
        modifier = Modifier
            .testTag("screen_plantDetail")
    ) {
        when (val s = state) {
            is UiState.Idle -> Text("Idle")
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                val candidate = s.data
                Text(candidate.scientificName)
                Text("Common names: ${candidate.commonNames.joinToString()}")
                Text("Family: ${candidate.family}")
                Text("Confidence: ${(candidate.score * 100).toInt()}%")
                candidate.iucnCategory?.let { Text("IUCN: $it") }
            }
            is UiState.Error -> Text("Error: ${s.message}")
        }

        Button(onClick = onBack) {
            Text(text = "Back to Identification")
        }
    }
}
