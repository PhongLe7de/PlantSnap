package com.plantsnap.ui.screens.identify.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.ui.state.UiState
import com.plantsnap.utils.MAX_PHOTOS
import com.plantsnap.ui.theme.PlantSnapTheme

@Composable
private fun BoxScope.GalleryButton(
    photoCount: Int,
    onClick: () -> Unit,
    showBadge: Boolean = false
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(bottom = 58.dp, start = 32.dp)
    ) {
        IconButton(
            onClick = onClick,
            enabled = photoCount < MAX_PHOTOS,
            modifier = Modifier
                .size(56.dp)
                .testTag("btn_gallery"),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.4f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Pick from gallery",
                modifier = Modifier.size(28.dp)
            )
        }
        if (showBadge && photoCount > 0) {
            Text(
                text = "$photoCount",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreenContent(
    flashEnabled: Boolean,
    capturedPhotos: List<Uri>,
    photoCount: Int,
    isLoading: Boolean,
    onFlashToggle: () -> Unit,
    onCapture: () -> Unit,
    onGalleryClick: () -> Unit,
    onBack: () -> Unit,
    onReviewPhotos: () -> Unit,
    onNavigateToPreview: (page: Int) -> Unit,
    onGrantPermission: () -> Unit,
    onDismissError: () -> Unit = {},
    hasCameraPermission: Boolean,
    errorMessage: String? = null,
    cameraPreview: @Composable BoxScope.() -> Unit = {}
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    var shutterTriggered by remember { mutableStateOf(false) }

    if (hasCameraPermission) {
        BottomSheetScaffold(scaffoldState = scaffoldState, sheetPeekHeight = 40.dp, sheetContent = {
            BottomSheetContent(
                modifier = Modifier,
                capturedPhotos = capturedPhotos,
                onPhotoSelected = { index -> onNavigateToPreview(index) }
            )
        }) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("screen_camera")
            ) {

                cameraPreview()

                // Back button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 16.dp, start = 8.dp)
                        .size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to home"
                    )
                }

                // Flash button
                IconButton(
                    onClick = onFlashToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = if (photoCount > 0) 96.dp else 16.dp)
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
                    enabled = !isLoading && !shutterTriggered && photoCount < MAX_PHOTOS,
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
                GalleryButton(
                    photoCount = photoCount,
                    onClick = onGalleryClick,
                    showBadge = true
                )
                // Review button
                if (photoCount > 0) {
                    Button(
                        onClick = onReviewPhotos,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                            .testTag("btn_review"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Review",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                // Errors
                errorMessage?.let {
                    AlertDialog(
                        onDismissRequest = onDismissError,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        title = { Text("Camera Error") },
                        text = { Text(it) },
                        confirmButton = {
                            TextButton(onClick = onDismissError) { Text("Dismiss") }
                        }
                    )
                }
            }
        }
    } else {
        // Denied state — gallery is still accessible
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission required")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onGrantPermission) { Text("Grant permission") }
            }
            GalleryButton(
                photoCount = photoCount,
                onClick = onGalleryClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onReviewPhotos: () -> Unit,
    onNavigateToPreview: (page: Int) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraScreenState by viewModel.screenState.collectAsState()
    val capturedPhotos by viewModel.photosHolder.photos.collectAsState()
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

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addPhotosFromGallery(uris)
        }
    }

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
        capturedPhotos = capturedPhotos,
        photoCount = capturedPhotos.size,
        isLoading = uiState is UiState.Loading,
        onFlashToggle = { viewModel.toggleFlash(cameraController) },
        onCapture = { viewModel.capturePhoto(cameraController) },
        onGalleryClick = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onBack = onBack,
        onReviewPhotos = onReviewPhotos,
        onNavigateToPreview = onNavigateToPreview,
        onGrantPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        onDismissError = { viewModel.clearError() },
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
            onGalleryClick = {},
            onBack = {},
            onReviewPhotos = {},
            onNavigateToPreview = {},
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

@Preview(showBackground = true, name = "Camera — error dialog")
@Composable
private fun CameraScreenErrorPreview() {
    PlantSnapTheme {
        CameraScreenContent(
            flashEnabled = false,
            capturedPhotos = emptyList(),
            photoCount = 0,
            isLoading = false,
            onFlashToggle = {},
            onCapture = {},
            onGalleryClick = {},
            onGrantPermission = {},
            hasCameraPermission = true,
            errorMessage = "Camera closed unexpectedly",
            cameraPreview = {
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
            },
            onBack = {},
            onReviewPhotos = {},
            onNavigateToPreview = {},
            onDismissError = {}
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
            onGalleryClick = {},
            onBack = {},
            onReviewPhotos = {},
            onNavigateToPreview = {},
            onGrantPermission = {},
            hasCameraPermission = false
        )
    }
}
