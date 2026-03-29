package com.plantsnap.ui.screens.identify.identify

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
                Text("Identified Plant: ${result.candidates.forEach { 
                    Text("${it.family} (${it.scientificName})")
                }}")
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