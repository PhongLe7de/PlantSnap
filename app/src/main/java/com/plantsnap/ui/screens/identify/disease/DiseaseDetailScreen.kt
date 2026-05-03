package com.plantsnap.ui.screens.identify.disease

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.plantsnap.R
import com.plantsnap.domain.models.DiseaseAiInfo
import com.plantsnap.domain.models.DiseaseCandidate
import com.plantsnap.ui.state.UiState
import java.util.Locale

@Composable
fun DiseaseDetailScreen(
    candidateIndex: Int,
    onBack: () -> Unit,
    viewModel: DiseaseDetailViewModel = hiltViewModel(),
) {
    val candidateState by viewModel.candidateState.collectAsState()
    val aiInfoState by viewModel.aiInfoState.collectAsState()
    val canRetry by viewModel.canRetry.collectAsState()

    LaunchedEffect(candidateIndex) {
        viewModel.loadDetail(candidateIndex)
    }

    DiseaseDetailContent(
        candidateState = candidateState,
        aiInfoState = aiInfoState,
        canRetry = canRetry,
        onBack = onBack,
        onRetryAi = viewModel::retryAiInfo,
    )
}

@Composable
private fun DiseaseDetailContent(
    candidateState: UiState<DiseaseCandidate>,
    aiInfoState: UiState<DiseaseAiInfo>,
    canRetry: Boolean,
    onBack: () -> Unit,
    onRetryAi: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.background)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = scheme.surfaceContainerHigh,
                    ),
                    modifier = Modifier.clip(CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.detail_back),
                    )
                }
                Text(
                    text = stringResource(R.string.disease_detail_topbar_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(40.dp))
            }
        },
    ) { innerPadding ->
        when (candidateState) {
            is UiState.Idle, is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = scheme.primary)
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = candidateState.message,
                        color = scheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is UiState.Success -> {
                DiseaseDetailBody(
                    candidate = candidateState.data,
                    aiInfoState = aiInfoState,
                    canRetry = canRetry,
                    onRetryAi = onRetryAi,
                    contentPadding = innerPadding,
                )
            }
        }
    }
}

@Composable
private fun DiseaseDetailBody(
    candidate: DiseaseCandidate,
    aiInfoState: UiState<DiseaseAiInfo>,
    canRetry: Boolean,
    onRetryAi: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
    ) {
        item { DiseaseHeroSection(candidate) }
        item { Spacer(Modifier.height(16.dp)) }
        item { DiseaseDisclaimerBanner(modifier = Modifier.padding(horizontal = 16.dp)) }
        item { Spacer(Modifier.height(24.dp)) }
        item { DiseaseOverviewSection(aiInfoState, canRetry, onRetryAi) }
        item { Spacer(Modifier.height(24.dp)) }
        item { DiseaseSymptomsSection(aiInfoState) }
        item { Spacer(Modifier.height(24.dp)) }
        item { DiseaseCausesSection(aiInfoState) }
        item { Spacer(Modifier.height(24.dp)) }
        item { DiseaseManagementBento(aiInfoState) }
    }
}

@Composable
private fun DiseaseHeroSection(candidate: DiseaseCandidate) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(32.dp)),
    ) {
        if (candidate.imageUrl != null) {
            AsyncImage(
                model = candidate.imageUrl,
                contentDescription = candidate.commonName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.BugReport,
                    contentDescription = null,
                    tint = scheme.outline.copy(alpha = 0.4f),
                    modifier = Modifier.size(72.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.35f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.70f),
                        )
                    )
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
        ) {
            Text(
                text = candidate.commonName,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 36.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = candidate.eppoCode.uppercase(Locale.getDefault()),
                    modifier = Modifier
                        .background(
                            scheme.secondaryContainer.copy(alpha = 0.85f),
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSecondaryContainer,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = stringResource(R.string.detail_match, (candidate.score * 100).toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
private fun DiseaseOverviewSection(
    aiInfoState: UiState<DiseaseAiInfo>,
    canRetry: Boolean,
    onRetryAi: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val aiInfo = (aiInfoState as? UiState.Success)?.data
    val isLoading = aiInfoState is UiState.Idle || aiInfoState is UiState.Loading
    val isError = aiInfoState is UiState.Error

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = scheme.primary,
                        )
                        Text(
                            text = stringResource(R.string.disease_detail_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                } else if (isError) {
                    Text(
                        text = (aiInfoState as UiState.Error).message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.error,
                    )
                    if (canRetry) {
                        Spacer(Modifier.height(12.dp))
                        RetryButton(onClick = onRetryAi)
                    }
                } else {
                    Text(
                        text = aiInfo?.description ?: stringResource(R.string.detail_info_unavailable),
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant,
                        lineHeight = 26.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun DiseaseSymptomsSection(aiInfoState: UiState<DiseaseAiInfo>) {
    DiseaseInfoCard(
        title = stringResource(R.string.disease_detail_symptoms),
        body = (aiInfoState as? UiState.Success)?.data?.symptoms,
        icon = Icons.Filled.Eco,
        isLoading = aiInfoState is UiState.Idle || aiInfoState is UiState.Loading,
    )
}

@Composable
private fun DiseaseCausesSection(aiInfoState: UiState<DiseaseAiInfo>) {
    DiseaseInfoCard(
        title = stringResource(R.string.disease_detail_causes),
        body = (aiInfoState as? UiState.Success)?.data?.causes,
        icon = Icons.Filled.Science,
        isLoading = aiInfoState is UiState.Idle || aiInfoState is UiState.Loading,
    )
}

@Composable
private fun DiseaseInfoCard(
    title: String,
    body: String?,
    icon: ImageVector,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(scheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        SmallLoadingIndicator()
                    } else {
                        Text(
                            text = body ?: stringResource(R.string.detail_info_unavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            lineHeight = 22.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiseaseManagementBento(aiInfoState: UiState<DiseaseAiInfo>) {
    val scheme = MaterialTheme.colorScheme
    val aiInfo = (aiInfoState as? UiState.Success)?.data
    val isLoading = aiInfoState is UiState.Idle || aiInfoState is UiState.Loading

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.disease_detail_management),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ManagementCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Filled.Spa,
                title = stringResource(R.string.disease_detail_treatment),
                body = aiInfo?.treatment ?: stringResource(R.string.detail_info_unavailable),
                isLoading = isLoading,
                iconBackground = scheme.errorContainer.copy(alpha = 0.4f),
                iconTint = scheme.error,
            )
            ManagementCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Filled.Shield,
                title = stringResource(R.string.disease_detail_prevention),
                body = aiInfo?.prevention ?: stringResource(R.string.detail_info_unavailable),
                isLoading = isLoading,
                iconBackground = scheme.primaryContainer.copy(alpha = 0.4f),
                iconTint = scheme.primary,
            )
        }
    }
}

@Composable
private fun ManagementCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    body: String,
    isLoading: Boolean,
    iconBackground: Color,
    iconTint: Color,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                if (isLoading) {
                    SmallLoadingIndicator()
                } else {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier
            .padding(top = 8.dp)
            .size(16.dp),
        strokeWidth = 2.dp,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun RetryButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.detail_retry))
    }
}
