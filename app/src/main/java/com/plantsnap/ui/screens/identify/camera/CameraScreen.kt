package com.plantsnap.ui.screens.identify.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.ui.state.UiState

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onPhotoCaptured: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var shutterTriggered by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .testTag("screen_camera")

    ) {
        // CameraPreview()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF173809))
        )

        when (val s = state) {
            is UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            is UiState.Error -> {
                Text(
                    text = "Error : ${(state as UiState.Error).message}",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> Unit
        }
        Button(onClick = onBack, modifier = Modifier.testTag("btn_back")) {Text("Back") }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 48.dp)
        ) {
           Box(modifier = Modifier.size(56.dp)) // GlassIconButton() TO-DO gallery picker button

            CaptureButton(
                enabled = !shutterTriggered,
                onClick = { shutterTriggered = true },
                modifier = Modifier.testTag("capture_button")
            )

            Box(modifier = Modifier.size(56.dp)) // GlassIconButton() TO-DO Help button

        }

        ShutterFlash(
            triggered = shutterTriggered,
            onAnimationEnd = {
                shutterTriggered = false
                // viewModel.capturePhoto() TO-DO
                // onPhotoCaptured()
            }
        )
    }

}
