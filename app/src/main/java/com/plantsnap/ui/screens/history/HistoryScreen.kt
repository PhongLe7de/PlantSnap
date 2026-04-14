package com.plantsnap.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.plantsnap.R

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onScanSelected: (plantId: String, candidateIndex: Int) -> Unit = {_, _ -> },
) {
    val state by viewModel.uiState.collectAsState()

    HistoryScreenContent(
        state = state,
        onScanSelected = onScanSelected,
    )
}

@Composable
fun HistoryScreenContent(
    state: UiState<List<ScanResult>>,
    onScanSelected: (plantId: String, candidateIndex: Int) -> Unit = {_, _ -> },
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_history"),
    ) {
        when (state) {
            is UiState.Idle,
            is UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            is UiState.Error -> {
                Text(
                    text = stringResource(R.string.history_error, state.message),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            is UiState.Success -> {
                val scans = state.data
                if (scans.isEmpty()) {
                    HistoryEmptyState(modifier = Modifier.align(Alignment.Center))
                } else {
                    HistoryList(
                        scans = scans,
                        onScanSelected = onScanSelected
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryList(
    scans: List<ScanResult>,
    onScanSelected: (plantId: String, candidateIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            )
        }

        items(scans, key = { it.id}) { scan ->
            HistoryScanCard(
                scan = scan,
                onClick = { onScanSelected(scan.id, 0)},
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        item { Spacer(Modifier.height(12.dp))}
    }
}

@Composable
fun HistoryScanCard(
    scan: ScanResult,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val topCandidate = scan.candidates.firstOrNull()
    val formattedDate = remember(scan.timestamp) {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            .format(Date(scan.timestamp))
    }

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
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                if (scan.imagePath.isNotBlank()) {
                    AsyncImage(
                        model = scan.imagePath,
                        contentDescription = scan.bestMatch,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = scheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.bestMatch,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = scheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(2.dp))

                val subtitle = topCandidate?.commonNames?.firstOrNull()
                    ?: scan.organ.replaceFirstChar { it.uppercase() }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.secondary,
                    )

                    topCandidate?.let { candidate ->
                        Text(
                            text = "•",
                            color = scheme.surfaceVariant.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            text = stringResource(R.string.history_match, (candidate.score * 100).toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
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
fun HistoryEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Eco,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.history_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("No scans yet")
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.history_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val previewScans = listOf(
    ScanResult(
        imagePath = "",
        organ = "leaf",
        bestMatch = "Monsera deliciosa",
        candidates = listOf(
            Candidate("Monstera deliciosa", listOf("Swiss Cheese Plant"),"Araceae", 0.97f, null)
        ),
        aiInfo = null,
        timestamp = System.currentTimeMillis() - 86_400_000,
    ),
    ScanResult(
        imagePath = "",
        organ = "flower",
        bestMatch = "Rosa canina",
        candidates = listOf(
            Candidate("Rosa canina", listOf("Dog Rose"),"Rosacae", 0.82f, "LC")
        ),
        aiInfo = null,
        timestamp = System.currentTimeMillis() - 86_400_000,
    ),
    ScanResult(
        imagePath = "",
        organ = "bark",
        bestMatch = "Quercus robur",
        candidates = listOf(
            Candidate("Quercus robur", listOf("English Oak", "Pedunculate Oak"),"Fagaceae", 0.74f, "LC")
        ),
        aiInfo = null,
        timestamp = System.currentTimeMillis() - 86_400_000,
    ),
)

@Preview(showBackground = true, showSystemUi = true, name = "History - items")
@Composable
private fun HistoryScreenPreview() {
    PlantSnapTheme {
        HistoryScreenContent(
            state = UiState.Success(previewScans),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "History - empty")
@Composable
private fun HistoryScreenEmptyPreview() {
    PlantSnapTheme {
        HistoryScreenContent(
            state = UiState.Success(emptyList()),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "History - items")
@Composable
private fun HistoryScreenLoadingPreview() {
    PlantSnapTheme {
        HistoryScreenContent(
            state = UiState.Loading,
        )
    }
}