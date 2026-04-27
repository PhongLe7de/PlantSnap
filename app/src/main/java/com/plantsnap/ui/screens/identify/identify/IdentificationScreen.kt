package com.plantsnap.ui.screens.identify.identify

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.plantsnap.R
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme

@Composable
fun IdentificationScreen(
    viewModel: IdentifyViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPlantSelected: (String, Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val photos by viewModel.photos.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result not needed - PlantService checks permission internally */ }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        if (viewModel.uiState.value is UiState.Idle) {
            viewModel.startIdentification()
        }
    }

    IdentificationScreenContent(
        state = state,
        photos = photos,
        onBack = onBack,
        onPlantSelected = onPlantSelected,
    )
}

@Composable
private fun IdentificationScreenContent(
    state: UiState<ScanResult>,
    photos: List<Uri> = emptyList(),
    onBack: () -> Unit = {},
    onPlantSelected: (String, Int) -> Unit = { _, _ -> },
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_identify"),
    ) {
        when (state) {
            is UiState.Idle,
            is UiState.Loading -> IdentificationLoadingContent()

            is UiState.Error -> IdentificationErrorContent(
                message = state.message,
                onBack = onBack,
            )

            is UiState.Success -> IdentificationSuccessContent(
                scanResult = state.data,
                photos = photos,
                onBack = onBack,
                onPlantSelected = onPlantSelected,
            )
        }
    }
}

@Composable
private fun IdentificationLoadingContent(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = scheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.id_analyzing),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IdentificationErrorContent(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = scheme.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.id_error, message),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = stringResource(R.string.id_retake_photo),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun IdentificationSuccessContent(
    scanResult: ScanResult,
    photos: List<Uri>,
    onBack: () -> Unit,
    onPlantSelected: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.id_back),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
        item { SafetyDisclaimerBanner(modifier = Modifier.padding(horizontal = 20.dp)) }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            AnalysisCompleteChip(modifier = Modifier.padding(horizontal = 20.dp))
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            UploadedImagePreview(
                photoUri = photos.firstOrNull(),
                imagePath = scanResult.imagePath,
                topScore = scanResult.candidates.firstOrNull()?.score ?: 0f,
                candidateCount = scanResult.candidates.size,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        item { Spacer(Modifier.height(32.dp)) }

        itemsIndexed(scanResult.candidates) { index, candidate ->
            CandidateCard(
                candidate = candidate,
                onClick = { onPlantSelected(scanResult.id, index) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        item { Spacer(Modifier.height(12.dp)) }
        item { RetakeCTASection(onBack = onBack, modifier = Modifier.padding(horizontal = 20.dp)) }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SafetyDisclaimerBanner(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.errorContainer),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = scheme.error.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = scheme.onErrorContainer,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = stringResource(R.string.id_safety_title).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = scheme.onErrorContainer,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.id_safety_body),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onErrorContainer,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun AnalysisCompleteChip(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .background(scheme.secondaryContainer, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = scheme.onSecondaryContainer,
        )
        Text(
            text = stringResource(R.string.id_analysis_complete).uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = scheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun UploadedImagePreview(
    photoUri: Uri?,
    imagePath: String,
    topScore: Float,
    candidateCount: Int,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val imageModel = photoUri ?: imagePath

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.id_results_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = scheme.primary,
            letterSpacing = (-0.5).sp,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.id_results_subtitle, candidateCount),
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f),
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = stringResource(R.string.id_uploaded_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Confidence badge overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-12).dp, y = 24.dp)
                .background(scheme.primary, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Verified,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.id_confidence_label).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = scheme.inversePrimary,
                    fontSize = 9.sp,
                )
                Text(
                    text = "%.1f%%".format(topScore * 100),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = scheme.onPrimary,
                )
            }
        }
    }

    // Extra spacer to account for the overflowing badge
    Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CandidateCard(
    candidate: Candidate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                if (candidate.imageUrl != null) {
                    AsyncImage(
                        model = candidate.imageUrl,
                        contentDescription = candidate.scientificName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = scheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = candidate.scientificName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${(candidate.score * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSecondaryContainer,
                    )
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = candidate.commonNames.firstOrNull() ?: candidate.family,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = scheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagChip(text = candidate.family)
                    candidate.iucnCategory?.let { TagChip(text = it) }
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = scheme.primary.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun TagChip(text: String) {
    val scheme = MaterialTheme.colorScheme

    Text(
        text = text.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = scheme.onSurface,
        letterSpacing = (-0.3).sp,
        fontSize = 10.sp,
        modifier = Modifier
            .background(scheme.surfaceContainerHighest, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun RetakeCTASection(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.primary)
            .padding(32.dp),
    ) {
        // Decorative circle
        Box(
            modifier = Modifier
                .size(128.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .background(scheme.primaryContainer.copy(alpha = 0.2f), CircleShape),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.id_not_seeing_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.id_not_seeing_body),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.secondaryContainer,
                    contentColor = scheme.onSecondaryContainer,
                ),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = stringResource(R.string.id_retake_photo).uppercase(Locale.getDefault()),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

private val previewScanResult = ScanResult(
    imagePath = "",
    organ = "leaf",
    bestMatch = "Monstera deliciosa",
    candidates = listOf(
        Candidate(
            scientificName = "Monstera deliciosa",
            commonNames = listOf("Swiss Cheese Plant"),
            family = "Araceae",
            score = 0.98f,
            iucnCategory = null,
        ),
        Candidate(
            scientificName = "Monstera adansonii",
            commonNames = listOf("Monkey Mask Plant"),
            family = "Araceae",
            score = 0.82f,
            iucnCategory = null,
        ),
        Candidate(
            scientificName = "Epipremnum aureum",
            commonNames = listOf("Golden Pothos"),
            family = "Araceae",
            score = 0.45f,
            iucnCategory = "LC",
        ),
    ),
)

@Preview(showBackground = true, showSystemUi = true, name = "Success - Light")
@Composable
private fun IdentificationScreenPreviewSuccess() {
    PlantSnapTheme {
        IdentificationScreenContent(
            state = UiState.Success(previewScanResult),
        )
    }
}

@Preview(
    showBackground = true, showSystemUi = true, name = "Success - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun IdentificationScreenPreviewSuccessDark() {
    PlantSnapTheme(darkTheme = true) {
        IdentificationScreenContent(
            state = UiState.Success(previewScanResult),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Loading")
@Composable
private fun IdentificationScreenPreviewLoading() {
    PlantSnapTheme {
        IdentificationScreenContent(
            state = UiState.Loading,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Error")
@Composable
private fun IdentificationScreenPreviewError() {
    PlantSnapTheme {
        IdentificationScreenContent(
            state = UiState.Error("Failed to identify plant"),
        )
    }
}

