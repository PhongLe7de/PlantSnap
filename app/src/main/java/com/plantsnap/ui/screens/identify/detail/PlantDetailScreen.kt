package com.plantsnap.ui.screens.identify.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.plantsnap.R
import com.plantsnap.domain.models.CareInfo
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.HabitatInfo
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.safety.SafetyAlert
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme
import com.plantsnap.ui.util.FALLBACK_IMAGE_URL
import com.plantsnap.ui.util.validImageUrlOrNull

@Composable
fun PlantDetailScreen(
    plantId: String,
    candidateIndex: Int,
    onBack: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel(),
) {
    val candidateState by viewModel.candidateState.collectAsState()
    val aiInfoState by viewModel.aiInfoState.collectAsState()
    val canRetry by viewModel.canRetry.collectAsState()
    val safetyAlerts by viewModel.safetyAlerts.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val scanLocation by viewModel.scanLocation.collectAsState()

    LaunchedEffect(plantId, candidateIndex) {
        viewModel.loadPlantDetail(plantId, candidateIndex)
    }

    PlantDetailScreenContent(
        candidateState = candidateState,
        aiInfoState = aiInfoState,
        canRetry = canRetry,
        safetyAlerts = safetyAlerts,
        onBack = onBack,
        onRetryAi = viewModel::retryAiInfo,
        isFavorite = isFavorite,
        onToggleFavorite = viewModel::toggleFavorite,
        scanLocation = scanLocation,
    )
}

@Composable
fun PlantDetailScreenContent(
    candidateState: UiState<Candidate>,
    aiInfoState: UiState<PlantAiInfo> = UiState.Idle,
    canRetry: Boolean = true,
    safetyAlerts: List<SafetyAlert> = emptyList(),
    showScanMetadata: Boolean = true,
    isFavorite: Boolean = false,
    scanLocation: Pair<Double, Double>? = null,
    onBack: () -> Unit,
    onRetryAi: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.testTag("screen_plantDetail"),
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
                    text = stringResource(R.string.detail_topbar_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                IconButton(
                    onClick = onToggleFavorite,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = scheme.surfaceContainerHigh,
                        contentColor = if (isFavorite) Color.Red else scheme.onSurfaceVariant
                    ),
                    modifier = Modifier.clip(CircleShape),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.detail_favourite),
                        tint = if (isFavorite) Color.Red else scheme.onSurfaceVariant
                    )
                }
            }
        },
    ) { innerPadding ->
        when (candidateState) {
            is UiState.Idle,
            is UiState.Loading -> {
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
                        text = "Error: ${candidateState.message}",
                        color = scheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is UiState.Success -> {
                PlantDetailBody(
                    candidate = candidateState.data,
                    aiInfoState = aiInfoState,
                    canRetry = canRetry,
                    safetyAlerts = safetyAlerts,
                    showScanMetadata = showScanMetadata,
                    scanLocation = scanLocation,
                    onRetryAi = onRetryAi,
                    contentPadding = innerPadding,
                )
            }
        }
    }
}

@Composable
private fun PlantDetailBody(
    candidate: Candidate,
    aiInfoState: UiState<PlantAiInfo>,
    canRetry: Boolean,
    safetyAlerts: List<SafetyAlert>,
    showScanMetadata: Boolean,
    scanLocation: Pair<Double, Double>?,
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
        item { HeroSection(candidate, showScanMetadata) }
        item { Spacer(Modifier.height(24.dp)) }
        item { AiInsightsSection(candidate, aiInfoState, canRetry, onRetryAi) }
        item { Spacer(Modifier.height(24.dp)) }
        item { SafetyAlertsSection(safetyAlerts, aiInfoState) }
        item { Spacer(Modifier.height(24.dp)) }
        item { CareBentoSection(aiInfoState, canRetry, onRetryAi) }
        item { Spacer(Modifier.height(24.dp)) }
        item { NativeHabitatSection(aiInfoState) }
        item { Spacer(Modifier.height(24.dp)) }
        item { CareRoutineSection(aiInfoState) }
        if (scanLocation != null) {
            item { Spacer(Modifier.height(24.dp)) }
            item { ScanLocationSection(scanLocation) }
        }
    }
}

@Composable
private fun HeroSection(candidate: Candidate, showScanMetadata: Boolean = true) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(32.dp)),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(candidate.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = candidate.scientificName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.4f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.65f),
                        )
                    )
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
        ) {
            if (showScanMetadata) {
                // Family badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = scheme.secondaryContainer,
                ) {
                    Text(
                        text = stringResource(R.string.detail_family, candidate.family).uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = scheme.onSecondaryContainer,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = candidate.scientificName,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 36.sp,
            )

            candidate.commonNames.firstOrNull()?.let { commonName ->
                Text(
                    text = commonName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.80f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (showScanMetadata) {
                // Confidence score
                Text(
                    text = stringResource(R.string.detail_match, (candidate.score * 100).toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun CareBentoSection(
    aiInfoState: UiState<PlantAiInfo>,
    canRetry: Boolean,
    onRetryAi: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val aiInfo = (aiInfoState as? UiState.Success)?.data
    val isLoading = aiInfoState is UiState.Idle || aiInfoState is UiState.Loading
    val isError = aiInfoState is UiState.Error

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CareCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WbSunny,
                iconTint = scheme.primary,
                iconBackground = scheme.primaryContainer.copy(alpha = 0.3f),
                title = stringResource(R.string.detail_light),
                body = aiInfo?.care?.light ?: stringResource(R.string.detail_info_unavailable),
                isLoading = isLoading,
            )
            CareCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                iconTint = scheme.primary,
                iconBackground = scheme.primaryContainer.copy(alpha = 0.3f),
                title = stringResource(R.string.detail_water),
                body = aiInfo?.care?.water ?: stringResource(R.string.detail_info_unavailable),
                isLoading = isLoading,
            )
        }

        if (isError && canRetry) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                RetryButton(onClick = onRetryAi)
            }
        }
    }
}

@Composable
private fun CareCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    body: String,
    isLoading: Boolean,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Icon area
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp),
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
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyAlertsSection(
    alerts: List<SafetyAlert>,
    aiInfoState: UiState<PlantAiInfo>,
) {
    val scheme = MaterialTheme.colorScheme
    val aiInfo = (aiInfoState as? UiState.Success)?.data
    val isLoading = aiInfoState is UiState.Idle || aiInfoState is UiState.Loading
    val isError = aiInfoState is UiState.Error

    if (alerts.isEmpty() && isError) return

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (alerts.isEmpty()) {
            SafetyAlertCard(
                title = stringResource(R.string.detail_toxicity_alert),
                body = aiInfo?.toxicity ?: stringResource(R.string.detail_info_unavailable),
                containerColor = scheme.errorContainer,
                accentColor = scheme.onSurface,
                iconBackground = scheme.errorContainer,
                iconTint = scheme.error,
                isLoading = isLoading,
            )
            return@Column
        }

        alerts.forEach { alert ->
            when (alert) {
                is SafetyAlert.PetToxicity -> {
                    val (titleRes, defaultBodyRes) = when (alert.pet) {
                        SafetyAlert.Pet.DOG -> R.string.safety_alert_dog_toxic_title to R.string.safety_alert_pet_toxic_body_dog
                        SafetyAlert.Pet.CAT -> R.string.safety_alert_cat_toxic_title to R.string.safety_alert_pet_toxic_body_cat
                    }
                    SafetyAlertCard(
                        title = stringResource(titleRes),
                        body = alert.symptoms ?: stringResource(defaultBodyRes),
                        containerColor = scheme.errorContainer,
                        accentColor = scheme.error,
                        iconBackground = scheme.errorContainer,
                        iconTint = scheme.error,
                    )
                }

                is SafetyAlert.ForagingCaution -> {
                    val titleRes = when (alert.reason) {
                        SafetyAlert.Reason.TOXIC_TO_HUMANS -> R.string.safety_alert_foraging_toxic_title
                        SafetyAlert.Reason.INEDIBLE -> R.string.safety_alert_foraging_inedible_title
                        SafetyAlert.Reason.GENERAL_CAUTION -> R.string.safety_alert_foraging_caution_title
                    }
                    SafetyAlertCard(
                        title = stringResource(titleRes),
                        body = alert.guidance ?: stringResource(R.string.safety_alert_foraging_body_default),
                        containerColor = scheme.tertiaryContainer,
                        accentColor = scheme.onTertiaryContainer,
                        iconBackground = scheme.tertiaryContainer,
                        iconTint = scheme.onTertiaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyAlertCard(
    title: String,
    body: String,
    containerColor: Color,
    accentColor: Color,
    iconBackground: Color,
    iconTint: Color,
    isLoading: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp),
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
                if (isLoading) {
                    SmallLoadingIndicator()
                } else {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AiInsightsSection(
    candidate: Candidate,
    aiInfoState: UiState<PlantAiInfo>,
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
                            text = stringResource(R.string.detail_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                } else if (isError) {
                    Text(
                        text = (aiInfoState).message,
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

                candidate.iucnCategory?.let { iucn ->
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = scheme.secondaryContainer,
                    ) {
                        Text(
                            text = stringResource(R.string.detail_iucn, iucn),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NativeHabitatSection(aiInfoState: UiState<PlantAiInfo>) {
    val aiInfo = (aiInfoState as? UiState.Success)?.data
    val isLoading = aiInfoState is UiState.Idle || aiInfoState is UiState.Loading

    Column {
        Text(
            text = stringResource(R.string.detail_habitat),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isLoading) {
                repeat(2) {
                    HabitatCard(title = "", body = "", isLoading = true)
                }
            } else {
                val habitats = aiInfo?.habitat ?: emptyList()
                if (habitats.isEmpty()) {
                    HabitatCard(
                        title = stringResource(R.string.detail_habitat),
                        body = stringResource(R.string.detail_info_unavailable),
                        isLoading = false,
                    )
                } else {
                    habitats.forEach { habitat ->
                        HabitatCard(
                            title = habitat.title.orEmpty(),
                            body = habitat.body ?: stringResource(R.string.detail_info_unavailable),
                            isLoading = false,
                            latitude = habitat.latitude,
                            longitude = habitat.longitude,
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun HabitatCard(
    title: String,
    body: String,
    isLoading: Boolean,
    latitude: Double? = null,
    longitude: Double? = null,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(scheme.primaryContainer.copy(alpha = 0.35f)),
            )
        } else if (latitude != null && longitude != null) {
            val position = LatLng(latitude, longitude)
            val cameraPositionState = rememberCameraPositionState {
                this.position = CameraPosition.fromLatLngZoom(position, 4f)
            }
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                ),
            ) {
                Marker(
                    state = MarkerState(position = position),
                    title = title.ifEmpty { null },
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(scheme.primaryContainer.copy(alpha = 0.35f)),
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when {
                    isLoading -> stringResource(R.string.detail_loading)
                    title.isEmpty() -> stringResource(R.string.detail_info_unavailable)
                    else -> title
                },
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
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

private data class CareItem(
    val icon: ImageVector?,
    val title: String,
    val body: String,
    val isLoading: Boolean = false,
)

@Composable
private fun CareRoutineSection(aiInfoState: UiState<PlantAiInfo>) {
    val aiInfo = (aiInfoState as? UiState.Success)?.data
    val isLoading = aiInfoState is UiState.Idle || aiInfoState is UiState.Loading

    val items = listOf(
        CareItem(
            Icons.Outlined.Thermostat,
            stringResource(R.string.detail_temperature),
            aiInfo?.care?.temperature ?: stringResource(R.string.detail_info_unavailable),
            isLoading,
        ),
        CareItem(
            Icons.Default.WaterDrop,
            stringResource(R.string.detail_humidity),
            aiInfo?.care?.humidity ?: stringResource(R.string.detail_info_unavailable),
            isLoading,
        ),
        CareItem(
            Icons.Outlined.Grass,
            stringResource(R.string.detail_soil),
            aiInfo?.care?.soil ?: stringResource(R.string.detail_info_unavailable),
            isLoading,
        ),
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.detail_care),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        items.forEach { item ->
            CareRoutineItem(item)
        }
    }
}

@Composable
private fun CareRoutineItem(item: CareItem) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (item.icon != null) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(scheme.onSecondaryContainer.copy(alpha = 0.5f), CircleShape),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            if (item.isLoading) {
                SmallLoadingIndicator(size = 14.dp, topPadding = 6.dp)
            } else {
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun SmallLoadingIndicator(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 16.dp,
    topPadding: androidx.compose.ui.unit.Dp = 8.dp,
) {
    CircularProgressIndicator(
        modifier = modifier
            .padding(top = topPadding)
            .size(size),
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
        Spacer(Modifier.size(8.dp))
        Text(stringResource(R.string.detail_retry))
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun ScanLocationSection(scanLocation: Pair<Double, Double>) {
    val scheme = MaterialTheme.colorScheme
    val (lat, lng) = scanLocation
    val position = LatLng(lat, lng)
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 14f)
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.detail_scan_location),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp)),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                ),
            ) {
                Marker(
                    state = MarkerState(position = position),
                    title = stringResource(R.string.detail_scanned_here),
                )
            }
        }
    }
}

private val previewCandidate = Candidate(
    scientificName = "Monstera Deliciosa",
    commonNames = listOf("Swiss Cheese Plant"),
    family = "Araceae",
    score = 0.97f,
    iucnCategory = "LC",
)

private val previewAiInfo = PlantAiInfo(
    care = CareInfo(
        light = "Bright, indirect light",
        water = "Every 1-2 weeks, let top inch dry",
        temperature = "65-85°F (18-30°C), avoid cold drafts",
        humidity = "60%+ preferred, mist regularly",
        soil = "Well-draining peat-based mix",
    ),
    toxicity = "Toxic to cats and dogs, contains calcium oxalate crystals.",
    habitat = listOf(
        HabitatInfo("Tropical Jungles", "Flourishes in high humidity environments with dappled shade."),
        HabitatInfo("Central America", "Originates from the regions of Southern Mexico and Panama."),
    ),
    description = "Monstera deliciosa is a species of flowering plant native to tropical forests of southern Mexico, south to Panama. It has been introduced to many tropical areas, and has become a mildly invasive species in Hawaii, Seychelles, Ascension Island and the Society Islands.",
)

@Preview(showBackground = true, showSystemUi = true, name = "Detail – Success")
@Composable
private fun PlantDetailPreviewSuccess() {
    PlantSnapTheme {
        PlantDetailScreenContent(
            candidateState = UiState.Success(previewCandidate),
            aiInfoState = UiState.Success(previewAiInfo),
            onBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Detail – Success Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlantDetailPreviewSuccessDark() {
    PlantSnapTheme(darkTheme = true) {
        PlantDetailScreenContent(
            candidateState = UiState.Success(previewCandidate),
            aiInfoState = UiState.Success(previewAiInfo),
            onBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Detail – Loading")
@Composable
private fun PlantDetailPreviewLoading() {
    PlantSnapTheme {
        PlantDetailScreenContent(
            candidateState = UiState.Loading,
            onBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Detail – Error")
@Composable
private fun PlantDetailPreviewError() {
    PlantSnapTheme {
        PlantDetailScreenContent(
            candidateState = UiState.Error("Plant details not found"),
            onBack = {},
        )
    }
}
