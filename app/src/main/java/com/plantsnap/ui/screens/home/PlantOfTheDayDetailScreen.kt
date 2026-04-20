package com.plantsnap.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.ui.screens.identify.detail.PlantDetailScreenContent
import com.plantsnap.ui.state.UiState

@Composable
fun PlantOfTheDayDetailScreen(
    onBack: () -> Unit,
    viewModel: PlantOfTheDayDetailViewModel = hiltViewModel(),
) {
    val plant by viewModel.plant.collectAsState()
    val current = plant

    if (current == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val candidate = Candidate(
        scientificName = current.scientificName,
        commonNames = listOf(current.commonName),
        family = "",
        score = 1f,
        iucnCategory = null,
        imageUrl = current.imageUrl,
    )
    val aiInfo = PlantAiInfo(
        care = current.care,
        toxicity = current.toxicity,
        habitat = current.habitat,
        description = current.description,
    )

    PlantDetailScreenContent(
        candidateState = UiState.Success(candidate),
        aiInfoState = UiState.Success(aiInfo),
        canRetry = false,
        onBack = onBack,
        onRetryAi = {},
    )
}
