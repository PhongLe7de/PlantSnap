package com.plantsnap.ui.screens.identify.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreenContent(
    flashEnabled: Boolean,
    capturedPhotos: List<Uri>,
    photoCount: Int,
    isLoading: Boolean,
    onFlashToggle: () -> Unit,
    onCapture: () -> Unit,
    onGrantPermission: () -> Unit,
    hasCameraPermission: Boolean,
    errorMessage: String? = null,
    cameraPreview: @Composable BoxScope.() -> Unit = {}
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    var shutterTriggered by remember { mutableStateOf(false) }

    if (hasCameraPermission) {
        BottomSheetScaffold(scaffoldState = scaffoldState, sheetPeekHeight = 40.dp, sheetContent = {
            BottomSheetContent(modifier = Modifier, capturedPhotos = capturedPhotos)
        }) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("screen_camera")
            ) {

                cameraPreview()

                // Flash button
                IconButton(
                    onClick = onFlashToggle,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 58.dp, end = 16.dp)
                        .size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (flashEnabled) "Flash on" else "Flash off"
                    )
                }

                // Shutter button
                CaptureButton(
                    onClick = { shutterTriggered = true },
                    enabled = !isLoading && !shutterTriggered && photoCount < 5,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 58.dp)
                        .testTag("btn_identify")
                )

                ShutterFlash(
                    triggered = shutterTriggered,
                    onAnimationEnd = {
                        shutterTriggered = false
                        if (!isLoading) onCapture()
                    }
                )
                // Image counter
                Text(
                    text = "$photoCount/5",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 68.dp, start = 16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    } else {
        // TODO: Edge cases?
        // Denied state
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission required")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onGrantPermission) { Text("Grant permission") }
            }
            // Error
            errorMessage?.let {
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onSubmitPhotos: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraScreenState by viewModel.screenState.collectAsState()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    // Request permission on first composition if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Camera controller, bound to this composable's lifecycle
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    CameraScreenContent(
        flashEnabled = cameraScreenState.flashEnabled,
        capturedPhotos = cameraScreenState.capturedPhotos,
        photoCount = cameraScreenState.picturesTaken,
        isLoading = uiState is UiState.Loading,
        onFlashToggle = { viewModel.toggleFlash(cameraController) },
        onCapture = { viewModel.capturePhoto(cameraController) },
        onGrantPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        hasCameraPermission = hasCameraPermission,
        errorMessage = (uiState as? UiState.Error)?.message,
        cameraPreview = {
            CameraPreview(controller = cameraController, modifier = Modifier.fillMaxSize())
        }
    )
}

@Preview(showBackground = true, name = "Camera — permission granted")
@Composable
private fun CameraScreenGrantedPreview() {
    PlantSnapTheme {
        CameraScreenContent(
            flashEnabled = false,
            capturedPhotos = emptyList(),
            photoCount = 0,
            isLoading = false,
            onFlashToggle = {},
            onCapture = {},
            onGrantPermission = {},
            hasCameraPermission = true,
            cameraPreview = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray)
                )
            }
        )
    }
}

@Preview(showBackground = true, name = "Camera — permission denied")
@Composable
private fun CameraScreenDeniedPreview() {
    PlantSnapTheme {
        CameraScreenContent(
            flashEnabled = false,
            capturedPhotos = emptyList(),
            photoCount = 0,
            isLoading = false,
            onFlashToggle = {},
            onCapture = {},
            onGrantPermission = {},
            hasCameraPermission = false
        )
    }
}
