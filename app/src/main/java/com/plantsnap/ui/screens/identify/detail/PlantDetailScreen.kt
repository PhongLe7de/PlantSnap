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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
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
import com.plantsnap.domain.models.CareTask
import com.plantsnap.domain.models.CareTaskType
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.HabitatInfo
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.safety.SafetyAlert
import com.plantsnap.ui.screens.garden.DueLabel
import com.plantsnap.ui.screens.garden.dueLabelFor
import com.plantsnap.ui.screens.garden.visuals
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme
import com.plantsnap.ui.util.FALLBACK_IMAGE_URL
import com.plantsnap.ui.util.validImageUrlOrNull
import android.text.format.DateUtils
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected

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
    val isSaved by viewModel.isSaved.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val scanLocation by viewModel.scanLocation.collectAsState()
    val careTasks by viewModel.careTasks.collectAsState()

    LaunchedEffect(plantId, candidateIndex) {
        viewModel.loadPlantDetail(plantId, candidateIndex)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    val candidate = (candidateState as? UiState.Success)?.data
    val defaultNickname = candidate?.let { it.commonNames.firstOrNull() ?: it.scientificName }.orEmpty()

    PlantDetailScreenContent(
        candidateState = candidateState,
        aiInfoState = aiInfoState,
        canRetry = canRetry,
        safetyAlerts = safetyAlerts,
        isSaved = isSaved,
        onBack = onBack,
        onRetryAi = viewModel::retryAiInfo,
        isFavorite = isFavorite,
        onToggleFavorite = viewModel::toggleFavorite,
        onToggleSaved = {
            if (isSaved) viewModel.toggleSaved() else showAddDialog = true
        },
        scanLocation = scanLocation,
        careTasks = careTasks,
        onMarkCareTaskDone = viewModel::markCareTaskDone,
        onSetCareTaskCadence = viewModel::setCareTaskCadence,
        onSetCareTaskEnabled = viewModel::setCareTaskEnabled,
    )

    if (showAddDialog) {
        AddToGardenDialog(
            initialValue = defaultNickname,
            onConfirm = { nickname ->
                viewModel.saveWithNickname(nickname)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun AddToGardenDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember {
        mutableStateOf(
            androidx.compose.ui.text.input.TextFieldValue(
                initialValue,
                selection = androidx.compose.ui.text.TextRange(initialValue.length),
            )
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_add_to_garden"),
        title = { Text(stringResource(R.string.add_garden_dialog_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.add_garden_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.add_garden_dialog_label)) },
                    modifier = Modifier.testTag("input_add_to_garden_nickname"),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.text) },
                enabled = text.text.trim().isNotEmpty(),
                modifier = Modifier.testTag("btn_add_to_garden_confirm"),
            ) {
                Text(stringResource(R.string.add_garden_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.garden_detail_rename_cancel))
            }
        },
    )
}

@Composable
fun PlantDetailScreenContent(
    candidateState: UiState<Candidate>,
    aiInfoState: UiState<PlantAiInfo> = UiState.Idle,
    canRetry: Boolean = true,
    safetyAlerts: List<SafetyAlert> = emptyList(),
    showScanMetadata: Boolean = true,
    isSaved: Boolean = false,
    showAddToGarden: Boolean = true,
    isFavorite: Boolean = false,
    scanLocation: Pair<Double, Double>? = null,
    careTasks: List<CareTask> = emptyList(),
    displayName: String? = null,
    lastWateredAt: Long? = null,
    onBack: () -> Unit,
    onRetryAi: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onToggleSaved: () -> Unit = {},
    onMarkCareTaskDone: (String) -> Unit = {},
    onSetCareTaskCadence: (String, Int) -> Unit = { _, _ -> },
    onSetCareTaskEnabled: (String, Boolean) -> Unit = { _, _ -> },
    onMarkWatered: (() -> Unit)? = null,
    onEditNickname: (() -> Unit)? = null,
    onArchive: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.testTag("screen_plantDetail"),
        containerColor = scheme.background,
        topBar = {
            PlantDetailTopBar(
                isFavorite = isFavorite,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite,
                onArchive = onArchive,
            )
        },
    ) { innerPadding ->
        PlantDetailContent(
            candidateState = candidateState,
            aiInfoState = aiInfoState,
            canRetry = canRetry,
            safetyAlerts = safetyAlerts,
            showScanMetadata = showScanMetadata,
            isSaved = isSaved,
            showAddToGarden = showAddToGarden,
            scanLocation = scanLocation,
            displayName = displayName,
            lastWateredAt = lastWateredAt,
            careTasks = careTasks,
            onRetryAi = onRetryAi,
            onToggleSaved = onToggleSaved,
            onMarkWatered = onMarkWatered,
            onEditNickname = onEditNickname,
            onMarkCareTaskDone = onMarkCareTaskDone,
            onSetCareTaskCadence = onSetCareTaskCadence,
            onSetCareTaskEnabled = onSetCareTaskEnabled,
            contentPadding = innerPadding,
        )
    }
}

@Composable
private fun PlantDetailTopBar(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: (() -> Unit)?,
) {
    val scheme = MaterialTheme.colorScheme
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
            modifier = Modifier.clip(CircleShape)
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
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
            textAlign = TextAlign.Center,
        )
        FavoriteToggleButton(isFavorite = isFavorite, onClick = onToggleFavorite)
        if (onArchive != null) {
            Spacer(Modifier.width(4.dp))
            ArchiveOverflowMenu(onArchive = onArchive)
        }
    }
}

@Composable
private fun FavoriteToggleButton(isFavorite: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val tint = if (isFavorite) Color.Red else scheme.onSurfaceVariant
    val icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = scheme.surfaceContainerHigh,
            contentColor = tint,
        ),
        modifier = Modifier
            .clip(CircleShape)
            .semantics { selected = isFavorite },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.detail_favourite),
            tint = tint,
        )
    }
}

@Composable
private fun ArchiveOverflowMenu(onArchive: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { menuExpanded = true },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = scheme.surfaceContainerHigh,
            ),
            modifier = Modifier
                .clip(CircleShape)
                .testTag("btn_garden_detail_overflow"),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.garden_detail_more_options),
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.garden_detail_remove_confirm)) },
                onClick = {
                    menuExpanded = false
                    onArchive()
                },
                modifier = Modifier.testTag("menu_garden_detail_remove"),
            )
        }
    }
}

@Composable
private fun PlantDetailContent(
    candidateState: UiState<Candidate>,
    aiInfoState: UiState<PlantAiInfo>,
    canRetry: Boolean,
    safetyAlerts: List<SafetyAlert>,
    showScanMetadata: Boolean,
    isSaved: Boolean,
    showAddToGarden: Boolean,
    scanLocation: Pair<Double, Double>?,
    displayName: String?,
    lastWateredAt: Long?,
    careTasks: List<CareTask>,
    onRetryAi: () -> Unit,
    onToggleSaved: () -> Unit,
    onMarkWatered: (() -> Unit)?,
    onEditNickname: (() -> Unit)?,
    onMarkCareTaskDone: (String) -> Unit,
    onSetCareTaskCadence: (String, Int) -> Unit,
    onSetCareTaskEnabled: (String, Boolean) -> Unit,
    contentPadding: PaddingValues,
) {
    when (candidateState) {
        is UiState.Idle, is UiState.Loading -> CenteredFill(contentPadding) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        is UiState.Error -> CenteredFill(contentPadding) {
            Text(
                text = "Error: ${candidateState.message}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        is UiState.Success -> PlantDetailBody(
            candidate = candidateState.data,
            aiInfoState = aiInfoState,
            canRetry = canRetry,
            safetyAlerts = safetyAlerts,
            showScanMetadata = showScanMetadata,
            isSaved = isSaved,
            showAddToGarden = showAddToGarden,
            scanLocation = scanLocation,
            displayName = displayName,
            lastWateredAt = lastWateredAt,
            careTasks = careTasks,
            onRetryAi = onRetryAi,
            onToggleSaved = onToggleSaved,
            onMarkWatered = onMarkWatered,
            onEditNickname = onEditNickname,
            onMarkCareTaskDone = onMarkCareTaskDone,
            onSetCareTaskCadence = onSetCareTaskCadence,
            onSetCareTaskEnabled = onSetCareTaskEnabled,
            contentPadding = contentPadding,
        )
    }
}

@Composable
private fun CenteredFill(contentPadding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun PlantDetailBody(
    candidate: Candidate,
    aiInfoState: UiState<PlantAiInfo>,
    canRetry: Boolean,
    safetyAlerts: List<SafetyAlert>,
    showScanMetadata: Boolean,
    isSaved: Boolean,
    showAddToGarden: Boolean,
    scanLocation: Pair<Double, Double>?,
    displayName: String?,
    lastWateredAt: Long?,
    careTasks: List<CareTask>,
    onRetryAi: () -> Unit,
    onToggleSaved: () -> Unit,
    onMarkWatered: (() -> Unit)?,
    onEditNickname: (() -> Unit)?,
    onMarkCareTaskDone: (String) -> Unit,
    onSetCareTaskCadence: (String, Int) -> Unit,
    onSetCareTaskEnabled: (String, Boolean) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
    ) {
        item {
            val savedPlantExtras = if (onMarkWatered != null && onEditNickname != null) {
                SavedPlantHeroExtras(
                    displayName = displayName,
                    lastWateredAt = lastWateredAt,
                    onMarkWatered = onMarkWatered,
                    onEditNickname = onEditNickname,
                )
            } else null
            HeroSection(
                candidate = candidate,
                showScanMetadata = showScanMetadata,
                isSaved = isSaved,
                showAddToGarden = showAddToGarden,
                onToggleSaved = onToggleSaved,
                savedPlantExtras = savedPlantExtras,
            )
        }
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
        if (isSaved && careTasks.isNotEmpty()) {
            item { Spacer(Modifier.height(24.dp)) }
            item {
                CareScheduleSection(
                    tasks = careTasks,
                    onMarkDone = onMarkCareTaskDone,
                    onSetCadence = onSetCareTaskCadence,
                    onSetEnabled = onSetCareTaskEnabled,
                )
            }
        }
        if (scanLocation != null) {
            item { Spacer(Modifier.height(24.dp)) }
            item { ScanLocationSection(scanLocation) }
        }
    }
}

internal data class SavedPlantHeroExtras(
    val displayName: String?,
    val lastWateredAt: Long?,
    val onMarkWatered: () -> Unit,
    val onEditNickname: () -> Unit,
)

@Composable
private fun HeroSection(
    candidate: Candidate,
    showScanMetadata: Boolean = true,
    isSaved: Boolean = false,
    showAddToGarden: Boolean = true,
    onToggleSaved: () -> Unit = {},
    savedPlantExtras: SavedPlantHeroExtras? = null,
) {
    val displayName = savedPlantExtras?.displayName
    val lastWateredAt = savedPlantExtras?.lastWateredAt
    val onMarkWatered = savedPlantExtras?.onMarkWatered
    val onEditNickname = savedPlantExtras?.onEditNickname
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
            contentDescription = stringResource(R.string.detail_image_description, candidate.scientificName),
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

            val titleText = displayName ?: candidate.scientificName
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 36.sp,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .semantics { heading() },
                )
                if (onEditNickname != null) {
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = onEditNickname,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .testTag("btn_garden_detail_edit_nickname"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.garden_detail_edit_nickname),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            val subtitle = if (displayName != null) {
                candidate.scientificName
            } else {
                candidate.commonNames.firstOrNull()
            }
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
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

            if (showAddToGarden) {
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onToggleSaved,
                    shape = RoundedCornerShape(50),
                    colors = if (isSaved) {
                        ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = scheme.primary,
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = scheme.primary,
                            contentColor = scheme.onPrimary,
                        )
                    },
                    modifier = Modifier.height(48.dp),
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (isSaved) R.string.detail_saved_to_garden
                            else R.string.detail_add_to_garden
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else if (onMarkWatered != null) {
                Spacer(Modifier.height(14.dp))
                val label = if (lastWateredAt != null) {
                    val rel = android.text.format.DateUtils.getRelativeTimeSpanString(
                        lastWateredAt,
                        System.currentTimeMillis(),
                        android.text.format.DateUtils.MINUTE_IN_MILLIS,
                    ).toString()
                    stringResource(R.string.garden_detail_watered_ago, rel)
                } else {
                    stringResource(R.string.garden_detail_mark_watered)
                }
                Button(
                    onClick = onMarkWatered,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary,
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("btn_garden_detail_mark_watered"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
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
        modifier = modifier
            .aspectRatio(1f)
            .semantics(mergeDescendants = true) { },
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
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { },
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
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
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
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { heading() },
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
        modifier = Modifier
            .width(260.dp)
            .semantics(mergeDescendants = true) { },
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
            modifier = Modifier
                .padding(bottom = 12.dp)
                .semantics { heading() },
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
            .padding(horizontal = 4.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) { },
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
            modifier = Modifier
                .padding(bottom = 12.dp)
                .semantics { heading() },
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

@Composable
private fun CareScheduleSection(
    tasks: List<CareTask>,
    onMarkDone: (String) -> Unit,
    onSetCadence: (String, Int) -> Unit,
    onSetEnabled: (String, Boolean) -> Unit,
) {
    var editingTask by remember { mutableStateOf<CareTask?>(null) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.care_schedule_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        // Render in canonical task-type order (matches Generator output).
        val byType = tasks.associateBy { it.taskType }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CareTaskType.entries.forEach { type ->
                val task = byType[type] ?: return@forEach
                CareScheduleRow(
                    task = task,
                    onMarkDone = { onMarkDone(task.id) },
                    onCadenceClick = { editingTask = task },
                    onToggleEnabled = { onSetEnabled(task.id, it) },
                )
            }
        }
    }

    editingTask?.let { task ->
        CadenceEditDialog(
            initialDays = task.cadenceDays.takeIf { it > 0 } ?: 7,
            onDismiss = { editingTask = null },
            onSave = { days ->
                onSetCadence(task.id, days)
                editingTask = null
            },
        )
    }
}

@Composable
private fun CareScheduleRow(
    task: CareTask,
    onMarkDone: () -> Unit,
    onCadenceClick: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val visuals = task.taskType.visuals()
    val now = System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(scheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = visuals.icon,
                        contentDescription = null,
                        tint = scheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = stringResource(visuals.shortLabelRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = task.enabled, onCheckedChange = onToggleEnabled)
            }
            if (task.enabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 52.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        TextButton(
                            onClick = onCadenceClick,
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        ) {
                            Text(
                                text = androidx.compose.ui.res.pluralStringResource(
                                    R.plurals.care_every_n_days,
                                    task.cadenceDays,
                                    task.cadenceDays,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.primary,
                            )
                        }
                        Text(
                            text = task.cadenceFootnote(now),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    OutlinedButton(
                        onClick = onMarkDone,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_done),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CareTask.cadenceFootnote(now: Long): String {
    val due = when (val label = dueLabelFor(nextDueAt, now)) {
        DueLabel.DueToday -> stringResource(R.string.care_due_today)
        is DueLabel.Overdue -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.care_overdue_days,
            label.daysLate,
            label.daysLate,
        )
        is DueLabel.Upcoming -> androidx.compose.ui.res.pluralStringResource(
            R.plurals.care_upcoming_days,
            label.daysUntil,
            label.daysUntil,
        )
    }
    val lastDone = lastCompletedAt?.let {
        val rel = DateUtils.getRelativeTimeSpanString(it, now, DateUtils.DAY_IN_MILLIS).toString()
        stringResource(R.string.care_last_done, rel)
    } ?: stringResource(R.string.care_never_done)
    return "$due  ·  $lastDone"
}

@Composable
private fun CadenceEditDialog(
    initialDays: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
) {
    var text by remember { mutableStateOf(initialDays.toString()) }
    val parsed = text.toIntOrNull()
    val valid = parsed != null && parsed in 1..3650

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.care_cadence_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text(stringResource(R.string.care_cadence_dialog_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !valid && text.isNotEmpty(),
                    singleLine = true,
                )
                if (!valid && text.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.care_cadence_dialog_invalid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let(onSave) },
                enabled = valid,
            ) { Text(stringResource(R.string.care_cadence_dialog_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.care_cadence_dialog_cancel))
            }
        },
    )
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
