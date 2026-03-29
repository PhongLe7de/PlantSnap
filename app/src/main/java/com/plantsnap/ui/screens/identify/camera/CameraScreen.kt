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
                Button(
                    onClick = onPhotoCaptured,
                    modifier = Modifier.testTag("btn_identify")
                ) {
                    Text(text = "Capture Photo")
                }
            }
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> Text("Photo captured: ${s.data}")
            is UiState.Error -> Text("Error: ${s.message}")
        }
    }
}
