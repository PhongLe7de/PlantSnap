package com.plantsnap.ui.screens.identify.identify

import android.util.Log
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
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.ui.state.UiState

@Composable
fun IdentificationScreen(
    viewModel: IdentifyViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPlantSelected: (String) -> Unit
) {

    val state by viewModel.uiState.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val organByPhoto by viewModel.organByPhoto.collectAsState()

    photos.forEachIndexed { index, uri ->
        Log.d("IdentificationScreen", "Photo[$index]: $uri → organ: ${organByPhoto[uri] ?: "none"}")
    }

    Column(
        modifier = Modifier
            .testTag("screen_identify")
    ){
        Text(
            text = "Identification Screen"
        )

        when (val s = state) {
            is UiState.Idle -> Text("Idle")
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                val result = s.data
                Column {
                    Text("Identified Plant:")
                    result.candidates.forEach { candidate ->
                        Text("${candidate.family} (${candidate.scientificName})")
                    }
                }
            }

            is UiState.Error -> Text("Error: ${s.message}")
        }

        Button(
            onClick = { onPlantSelected("Example Plant") }
        ) {
            Text(text = "Select Plant")
        }

        Button(
            onClick = onBack
        ) {
            Text(text = "Back to Camera")
        }
    }
}