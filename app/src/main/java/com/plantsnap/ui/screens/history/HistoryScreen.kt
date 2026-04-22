package com.plantsnap.ui.screens.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.plantsnap.ui.components.TopBar

enum class PlantCategory(val label: String) {
    ALL("All scans"),
    INDOOR("Indoor"),
    OUTDOOR("Outdoor"),
    SUCCULENT("Succulents"),
}

private val INDOOR_FAMILIES = setOf(
    "Araceae", "Marantaceae", "Asparagaceae", "Liliaceae", "Cactaceae",
    "Crassulaceae", "Euphorbiaceae", "Piperaceae", "Urticaceae", "Gesneriaceae",
    "Apocynaceae", "Moraceae", "Strelitziaceae", "Begoniaceae", "Commelinaceae",
    "Arecaceae", "Agavaceae", "Bromeliaceae", "Dracaenaceae", "Orchidaceae",
)

private val SUCCULENT_FAMILIES = setOf(
    "Crassulaceae", "Cactaceae", "Aizoaceae", "Portulacaceae", "Asphodelaceae",
)


fun ScanResult.inferCategory(): PlantCategory {
    val family = candidates.firstOrNull()?.family ?: return PlantCategory.OUTDOOR
    return when (family) {
        in SUCCULENT_FAMILIES -> PlantCategory.SUCCULENT
        in INDOOR_FAMILIES -> PlantCategory.INDOOR
        else -> PlantCategory.OUTDOOR
    }
}

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    profilePhotoUrl: String? = null,
    onScanSelected: (plantId: String, candidateIndex: Int) -> Unit = {_, _ -> },
) {
    val state by viewModel.uiState.collectAsState()

    HistoryScreenContent(
        state = state,
        profilePhotoUrl = profilePhotoUrl,
        onScanSelected = onScanSelected,
    )
}

@Composable
fun HistoryScreenContent(
    state: UiState<List<ScanResult>>,
    profilePhotoUrl: String?,
    onScanSelected: (plantId: String, candidateIndex: Int) -> Unit = { _, _ -> },
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(PlantCategory.ALL) }

    val scheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_history"),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            TopBar(profilePhotoUrl = profilePhotoUrl)
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = scheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.history_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))

                HistorySearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(PlantCategory.entries) { category ->
                    CategoryChip(
                        label = category.label,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        when (state) {
            is UiState.Idle,
            is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = scheme.primary
                            )
                        }
                    }
                }
            is UiState.Error -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.history_error),
                            color =  scheme.error,
                            modifier = Modifier.padding(32.dp),
                        )
                    }
                }
            }

            is UiState.Success -> {
                val filtered = state.data
                    .filter { scan ->
                        selectedCategory == PlantCategory.ALL ||
                                scan.inferCategory() == selectedCategory
                    }
                    .filter { scan ->
                        searchQuery.isBlank() ||
                                scan.bestMatch.contains(searchQuery, ignoreCase = true) ||
                                scan.candidates.firstOrNull()?.commonNames
                                    ?.any { it.contains(searchQuery, ignoreCase = true) } == true
                    }
                if (filtered.isEmpty()) {
                    item {
                        HistoryEmptyState(
                            isFiltered = searchQuery.isNotBlank() || selectedCategory != PlantCategory.ALL,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                        )
                    }
                } else {
                    items(filtered, key = { it.id }) {scan ->
                        HistoryScanCard(
                            scan = scan,
                            onClick = { onScanSelected(scan.id, 0) },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val bgColor by animateColorAsState(
        targetValue = if (selected) scheme.secondaryContainer else scheme.surfaceContainer,
        label = "chip_bg"
    )
    val  textColor by animateColorAsState(
        targetValue = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
        label = "chip_text"
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun HistorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val  scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = scheme.outline,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.history_search_bar),
                        color = scheme.outline,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = scheme.onSurface,
                    ),
                    cursorBrush = SolidColor(scheme.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun HistoryScanCard(
    scan: ScanResult,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val category = remember(scan.id) { scan.inferCategory() }
    val topCandidate = scan.candidates.firstOrNull()
    val formattedDate = remember(scan.timestamp) {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            .format(Date(scan.timestamp))
            .uppercase()
    }
    val confidencePct = topCandidate?.score?.let { (it * 100).toInt() }
        ?: scan.identificationScore?.let { (it * 100).toInt() }
    val imageModel: Any? = scan.imagePath.takeIf { it.isNotBlank() }
        ?: topCandidate?.imageUrl?.takeIf { it.isNotBlank() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surfaceContainerLowest)
            .clickable(onClick = onClick),
    ) {
        Row(modifier = Modifier.height(112.dp)) {
            Box(modifier = Modifier.size(112.dp)) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = scan.bestMatch,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(scheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Eco,
                            contentDescription = null,
                            tint = scheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                if (category != PlantCategory.ALL) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(scheme.surface.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = category.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp,
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = scan.bestMatch,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = scheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = scheme.secondary,
                    letterSpacing = 0.5.sp,
                )

                if (confidencePct != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Filled.Verified,
                            contentDescription = null,
                            tint = scheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.history_accuracy, confidencePct),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .height(112.dp)
                .background(scheme.primary.copy(alpha = 0f)),
        )
    }
}

@Composable
fun HistoryEmptyState(
    isFiltered: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(32.dp)
            .testTag("No scans yet"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Eco,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isFiltered) stringResource(R.string.history_no_Results) else stringResource(R.string.history_no_scans),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isFiltered) stringResource(R.string.history_try_different_filter) else stringResource(
                R.string.history_your_scans_appear_here
            ),
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
        timestamp = System.currentTimeMillis() - 86_400_000,
    ),
    ScanResult(
        imagePath = "",
        organ = "flower",
        bestMatch = "Rosa canina",
        candidates = listOf(
            Candidate("Rosa canina", listOf("Dog Rose"),"Rosacae", 0.82f, "LC")
        ),
        timestamp = System.currentTimeMillis() - 86_400_000,
    ),
    ScanResult(
        imagePath = "",
        organ = "bark",
        bestMatch = "Quercus robur",
        candidates = listOf(
            Candidate("Quercus robur", listOf("English Oak", "Pedunculate Oak"),"Fagaceae", 0.74f, "LC")
        ),
        timestamp = System.currentTimeMillis() - 86_400_000,
    ),
)

@Preview(showBackground = true, showSystemUi = true, name = "History - items")
@Composable
private fun HistoryScreenPreview() {
    PlantSnapTheme {
        HistoryScreenContent(
            state = UiState.Success(previewScans),
            profilePhotoUrl = "",
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "History - empty")
@Composable
private fun HistoryScreenEmptyPreview() {
    PlantSnapTheme {
        HistoryScreenContent(
            state = UiState.Success(emptyList()),
            profilePhotoUrl = "",
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "History - items")
@Composable
private fun HistoryScreenLoadingPreview() {
    PlantSnapTheme {
        HistoryScreenContent(
            state = UiState.Loading,
            profilePhotoUrl = "",
        )
    }
}