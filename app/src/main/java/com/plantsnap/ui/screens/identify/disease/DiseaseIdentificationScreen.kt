package com.plantsnap.ui.screens.identify.disease

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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.plantsnap.domain.models.DiseaseCandidate
import com.plantsnap.domain.models.DiseaseScanResult
import com.plantsnap.ui.state.UiState
import java.util.Locale

@Composable
fun DiseaseIdentificationScreen(
    viewModel: DiseaseViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCandidateSelected: (Int) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val photos by viewModel.photos.collectAsState()

    LaunchedEffect(Unit) {
        if (viewModel.uiState.value is UiState.Idle) {
            viewModel.startDiseaseIdentification()
        }
    }

    DiseaseIdentificationContent(
        state = state,
        photos = photos,
        onBack = onBack,
        onCandidateSelected = onCandidateSelected,
    )
}

@Composable
private fun DiseaseIdentificationContent(
    state: UiState<DiseaseScanResult>,
    photos: List<Uri> = emptyList(),
    onBack: () -> Unit = {},
    onCandidateSelected: (Int) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is UiState.Idle,
            is UiState.Loading -> DiseaseLoadingContent()

            is UiState.Error -> DiseaseErrorContent(
                message = state.message,
                onBack = onBack,
            )

            is UiState.Success -> DiseaseSuccessContent(
                result = state.data,
                photos = photos,
                onBack = onBack,
                onCandidateSelected = onCandidateSelected,
            )
        }
    }
}

@Composable
private fun DiseaseLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Analyzing for diseases…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DiseaseErrorContent(
    message: String,
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.fillMaxSize(),
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
                text = "Identification error: $message",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                shape = RoundedCornerShape(50),
            ) {
                Text(text = "Retake Photo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DiseaseSuccessContent(
    result: DiseaseScanResult,
    photos: List<Uri>,
    onBack: () -> Unit,
    onCandidateSelected: (Int) -> Unit = {},
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        item { DiseaseDisclaimerBanner(modifier = Modifier.padding(horizontal = 20.dp)) }
        item { Spacer(Modifier.height(24.dp)) }

        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "ANALYSIS COMPLETE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            DiseaseImagePreview(
                photoUri = photos.firstOrNull(),
                imagePath = result.imagePath,
                topScore = result.candidates.firstOrNull()?.score ?: 0f,
                candidateCount = result.candidates.size,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        item { Spacer(Modifier.height(32.dp)) }

        itemsIndexed(result.candidates) { index, candidate ->
            DiseaseCandidateCard(
                candidate = candidate,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clickable { onCandidateSelected(index) },
            )
            Spacer(Modifier.height(12.dp))
        }

        item { Spacer(Modifier.height(12.dp)) }

        item {
            DiseaseRetakeCTA(
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
internal fun DiseaseDisclaimerBanner(modifier: Modifier = Modifier) {
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
                    text = "IMPORTANT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = scheme.onErrorContainer,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Results are AI-generated and for informational purposes only. " +
                            "Consult a plant pathologist or agronomist before treating your plant.",
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
private fun DiseaseImagePreview(
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
            text = "Disease Results",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = scheme.primary,
            letterSpacing = (-0.5).sp,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "$candidateCount potential disease${if (candidateCount != 1) "s" else ""} found",
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
                    contentDescription = "Uploaded photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

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
                        Icons.Filled.Science,
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column {
                    Text(
                        text = "CONFIDENCE",
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

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DiseaseCandidateCard(
    candidate: DiseaseCandidate,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
                        contentDescription = candidate.commonName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Filled.BugReport,
                        contentDescription = null,
                        tint = scheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = candidate.commonName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                        maxLines = 2,
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

                Spacer(Modifier.height(10.dp))

                Text(
                    text = candidate.eppoCode.uppercase(Locale.getDefault()),
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
        }
    }
}

@Composable
private fun DiseaseRetakeCTA(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.primary)
            .padding(32.dp),
    ) {
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
                text = "Not what you expected?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Try a closer photo focusing on the affected area — leaves, stems, or spots.",
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
                    text = "RETAKE PHOTO",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
