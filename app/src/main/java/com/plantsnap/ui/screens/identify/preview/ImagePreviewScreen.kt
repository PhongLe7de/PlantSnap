package com.plantsnap.ui.screens.identify.preview

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.plantsnap.ui.screens.identify.camera.CapturedPhotosHolder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePreviewScreen(
    initialPage: Int = 0,
    onRetake: () -> Unit,
    onUsePhotos: () -> Unit,
    photosHolder: CapturedPhotosHolder
) {
    val currentPhotos by photosHolder.photos.collectAsState()

    // Keep a stable snapshot so the screen doesn't flash blank during exit animation
    val displayPhotos = remember { mutableStateOf(currentPhotos) }
    if (currentPhotos.isNotEmpty()) {
        displayPhotos.value = currentPhotos
    }

    if (displayPhotos.value.isEmpty()) return

    ImagePreviewContent(
        initialPage = initialPage,
        photos = displayPhotos.value,
        onRetake = { currentPage ->
            onRetake()
            photosHolder.removePhoto(currentPage)
        },
        onBack = onRetake,
        onUsePhotos = onUsePhotos,
        onRemovePhoto = { index -> photosHolder.removePhoto(index) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePreviewContent(
    initialPage: Int = 0,
    photos: List<Uri>,
    onRetake: (currentPage: Int) -> Unit,
    onBack: () -> Unit,
    onUsePhotos: () -> Unit,
    onRemovePhoto: (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, (photos.size - 1).coerceAtLeast(0)),
        pageCount = { photos.size }
    )

    // Keep pager in bounds when photos are removed
    LaunchedEffect(photos.size) {
        if (pagerState.currentPage >= photos.size && photos.isNotEmpty()) {
            pagerState.scrollToPage(photos.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen_preview")
    ) {
        PreviewTopBar(onBack = onBack)

        PhotoPager(
            pagerState = pagerState,
            photos = photos,
            onRemovePhoto = onRemovePhoto,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (photos.size > 1) {
            PageIndicator(
                pageCount = photos.size,
                currentPage = pagerState.currentPage
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        PreviewActionButtons(
            onRetake = { onRetake(pagerState.currentPage) },
            onUsePhotos = onUsePhotos
        )
    }
}

@Composable
private fun PreviewTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to camera",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "Review Photos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoPager(
    pagerState: PagerState,
    photos: List<Uri>,
    onRemovePhoto: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 12.dp,
        modifier = modifier
    ) { page ->
        if (page < photos.size) {
            PhotoPage(
                photoUri = photos[page],
                pageNumber = page,
                showRemoveButton = photos.size > 1,
                onRemove = { onRemovePhoto(page) }
            )
        }
    }
}

@Composable
private fun PhotoPage(
    photoUri: Uri,
    pageNumber: Int,
    showRemoveButton: Boolean,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = "Captured photo ${pageNumber + 1}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        )

        if (showRemoveButton) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove photo",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (index == currentPage) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun PreviewActionButtons(
    onRetake: () -> Unit,
    onUsePhotos: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onRetake,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("btn_retake"),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Retake",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onUsePhotos,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("btn_use_photo"),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Use Photo",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
