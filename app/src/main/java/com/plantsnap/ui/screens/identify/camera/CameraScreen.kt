package com.plantsnap.ui.screens.identify.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
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
import androidx.compose.material.icons.filled.Camera
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreenContent(
    flashEnabled: Boolean,
    isLoading: Boolean,
    onFlashToggle: () -> Unit,
    onCapture: () -> Unit,
    onGrantPermission: () -> Unit,
    hasCameraPermission: Boolean,
    errorMessage: String? = null,
    cameraPreview: @Composable BoxScope.() -> Unit = {}
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    if (hasCameraPermission) {
        BottomSheetScaffold(scaffoldState = scaffoldState, sheetPeekHeight = 40.dp, sheetContent = {
            Text(
                text = "Gallery",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            HorizontalDivider(
                modifier = Modifier
                    .width(30.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 6.dp)
            )
            Text("Nothing")
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
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
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
                IconButton(
                    onClick = onCapture,
                    enabled = !isLoading,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 58.dp)
                        .size(85.dp)
                        .testTag("btn_identify"),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.4f),
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Take picture",
                        modifier = Modifier.fillMaxSize(0.8f)
                    )
                }
            }
        }
    } else {
        // TODO: Edge cases
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
    onPhotoCaptured: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
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

    var flashEnabled by remember { mutableStateOf(false) }

    CameraScreenContent(
        flashEnabled = flashEnabled,
        isLoading = state is UiState.Loading,
        onFlashToggle = {
            flashEnabled = !flashEnabled
            cameraController.imageCaptureFlashMode =
                if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        },
        onCapture = { /*viewModel.capturePhoto(cameraController)*/ },
        onGrantPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        hasCameraPermission = hasCameraPermission,
        errorMessage = (state as? UiState.Error)?.message,
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
            isLoading = false,
            onFlashToggle = {},
            onCapture = {},
            onGrantPermission = {},
            hasCameraPermission = false
        )
    }
}
