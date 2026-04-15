package com.plantsnap.ui.screens.identify.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.ui.state.UiState

@Composable
fun PlantDetailScreen(
    plantId: String,
    candidateIndex: Int,
    onBack: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val candidateState by viewModel.candidateState.collectAsState()
    val aiInfoState by viewModel.aiInfoState.collectAsState()

    LaunchedEffect(plantId, candidateIndex) {
        viewModel.loadPlantDetail(plantId, candidateIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("screen_plantDetail")
    ) {
        when (val s = candidateState) {
            is UiState.Idle -> Unit
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                val candidate = s.data
                Text(
                    text = candidate.scientificName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text("Common names: ${candidate.commonNames.joinToString()}")
                Text("Family: ${candidate.family}")
                Text("Confidence: ${(candidate.score * 100).toInt()}%")
                candidate.iucnCategory?.let { Text("IUCN: $it") }

                Spacer(Modifier.height(16.dp))
                AiInfoCard(
                    state = aiInfoState,
                    onRetry = viewModel::retryAiInfo,
                )
            }
            is UiState.Error -> Text("Error: ${s.message}")
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text(text = "Back to Identification")
        }
    }
}

@Composable
private fun AiInfoCard(
    state: UiState<PlantAiInfo>,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI CARE GUIDE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))

            when (state) {
                is UiState.Idle -> Unit
                is UiState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = "Loading care info…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                is UiState.Success -> {
                    AiInfoSection(label = "Care", body = state.data.care)
                    Spacer(Modifier.height(8.dp))
                    AiInfoSection(label = "Toxicity", body = state.data.toxicity)
                    Spacer(Modifier.height(8.dp))
                    AiInfoSection(label = "Habitat", body = state.data.habitat)
                }
                is UiState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun AiInfoSection(label: String, body: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
