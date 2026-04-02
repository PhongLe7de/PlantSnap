package com.plantsnap.ui.screens.identify.camera

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
fun CameraScreen(
    onBack: () -> Unit,
    onPhotoCaptured: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .testTag("screen_camera")
    ) {
        Text(text = "Camera Screen")

        when (val s = state) {
            is UiState.Idle -> {
                Column() {
                    Button(
                        onClick = onPhotoCaptured,
                        modifier = Modifier.testTag("btn_identify")
                    ) {
                        Text(text = "Capture Photo")
                    }

                    Button(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Text(text = "Back to Home")
                    }
                }

            }

            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> Text("Photo captured: ${s.data}")
            is UiState.Error -> Text("Error: ${s.message}")
        }
    }
}
