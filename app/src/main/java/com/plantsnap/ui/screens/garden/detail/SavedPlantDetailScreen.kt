package com.plantsnap.ui.screens.garden.detail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.R
import com.plantsnap.domain.models.CareInfo
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.HabitatInfo
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.ui.screens.identify.detail.PlantDetailScreenContent
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme

@Composable
fun SavedPlantDetailScreen(
    savedPlantId: String,
    onBack: () -> Unit,
    viewModel: SavedPlantDetailViewModel = hiltViewModel(),
) {
    val candidateState by viewModel.candidateState.collectAsState()
    val aiInfoState by viewModel.aiInfoState.collectAsState()
    val canRetry by viewModel.canRetry.collectAsState()
    val safetyAlerts by viewModel.safetyAlerts.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val isFavourite by viewModel.isFavourite.collectAsState()
    val lastWateredAt by viewModel.lastWateredAt.collectAsState()
    val careTasks by viewModel.careTasks.collectAsState()

    LaunchedEffect(savedPlantId) {
        viewModel.loadSavedPlant(savedPlantId)
    }

    var showRenameDialog by remember { mutableStateOf(false) }

    PlantDetailScreenContent(
        candidateState = candidateState,
        aiInfoState = aiInfoState,
        canRetry = canRetry,
        safetyAlerts = safetyAlerts,
        showScanMetadata = false,
        showAddToGarden = false,
        showCareSchedule = true,
        isSaved = true,
        isFavorite = isFavourite,
        displayName = displayName.takeIf { it.isNotBlank() },
        lastWateredAt = lastWateredAt,
        careTasks = careTasks,
        onBack = onBack,
        onRetryAi = viewModel::retryAiInfo,
        onToggleFavorite = viewModel::toggleFavourite,
        onMarkCareTaskDone = viewModel::markCareTaskDone,
        onSetCareTaskCadence = viewModel::setCareTaskCadence,
        onSetCareTaskEnabled = viewModel::setCareTaskEnabled,
        onMarkWatered = viewModel::markWatered,
        onEditNickname = { showRenameDialog = true },
        onArchive = {
            viewModel.archive()
            onBack()
        },
    )

    if (showRenameDialog) {
        RenameDialog(
            initialValue = displayName,
            onConfirm = { newName ->
                viewModel.updateNickname(newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
    }
}

@Composable
private fun RenameDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(TextFieldValue(initialValue, selection = androidx.compose.ui.text.TextRange(initialValue.length))) }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_garden_detail_rename"),
        title = { Text(stringResource(R.string.garden_detail_rename_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text(stringResource(R.string.garden_detail_rename_label)) },
                modifier = Modifier.testTag("input_garden_detail_rename"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.text) },
                enabled = text.text.trim().isNotEmpty(),
                modifier = Modifier.testTag("btn_garden_detail_rename_confirm"),
            ) {
                Text(stringResource(R.string.garden_detail_rename_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.garden_detail_rename_cancel))
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
    ),
    description = "Monstera deliciosa is a flowering plant native to tropical forests of southern Mexico.",
)

@Preview(showBackground = true, showSystemUi = true, name = "Saved Plant Detail – Light")
@Composable
private fun SavedPlantDetailPreviewLight() {
    PlantSnapTheme {
        PlantDetailScreenContent(
            candidateState = UiState.Success(previewCandidate),
            aiInfoState = UiState.Success(previewAiInfo),
            showScanMetadata = false,
            showAddToGarden = false,
            isSaved = true,
            isFavorite = true,
            displayName = "Monty",
            lastWateredAt = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L,
            onBack = {},
            onMarkWatered = {},
            onEditNickname = {},
            onArchive = {},
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Saved Plant Detail – Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SavedPlantDetailPreviewDark() {
    PlantSnapTheme(darkTheme = true) {
        PlantDetailScreenContent(
            candidateState = UiState.Success(previewCandidate),
            aiInfoState = UiState.Success(previewAiInfo),
            showScanMetadata = false,
            showAddToGarden = false,
            isSaved = true,
            isFavorite = false,
            displayName = "Monty",
            lastWateredAt = null,
            onBack = {},
            onMarkWatered = {},
            onEditNickname = {},
            onArchive = {},
        )
    }
}
