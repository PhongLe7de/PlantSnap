package com.plantsnap.ui.screens.identify.detail

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.domain.models.Candidate
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme

@Composable
fun PlantDetailScreen(
    plantId: String,
    candidateIndex: Int,
    onBack: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPlantDetail(plantId, candidateIndex)
    }

    PlantDetailScreenContent(
        state = state,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreenContent(
    state: UiState<Candidate>,
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.testTag("screen_plantDetail"),
        containerColor = scheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = scheme.surfaceContainerHigh,
                        ),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO: toggle favourite */ },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = scheme.surfaceContainerHigh,
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Add to favourites",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.background.copy(alpha = 0.85f),
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Black,
                    titleContentColor = scheme.primary,
                    actionIconContentColor = Color.Black
                ),
            )
        },
    ) { innerPadding ->
        when (state) {
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
                        text = "Error: ${state.message}",
                        color = scheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is UiState.Success -> {
                PlantDetailBody(
                    candidate = state.data,
                    contentPadding = innerPadding,
                )
            }
        }
    }
}

@Composable
private fun PlantDetailBody(
    candidate: Candidate,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
    ) {
        item { HeroSection(candidate) }
        item { Spacer(Modifier.height(24.dp)) }
        item { CareBentoSection() }
        item { Spacer(Modifier.height(24.dp)) }
        item { AiInsightsSection(candidate) }
        item { Spacer(Modifier.height(24.dp)) }
        item { NativeHabitatSection() }
        item { Spacer(Modifier.height(24.dp)) }
        item { CareRoutineSection() }
    }
}

@Composable
private fun HeroSection(candidate: Candidate) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(32.dp)),
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.primaryContainer.copy(alpha = 0.4f)),
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
            // Family badge
            Surface(
                shape = RoundedCornerShape(50),
                color = scheme.secondaryContainer,
            ) {
                Text(
                    text = "${candidate.family} Family".uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = scheme.onSecondaryContainer,
                )
            }
            Spacer(Modifier.height(8.dp))

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

            // Confidence score
            Text(
                text = "${(candidate.score * 100).toInt()}% match",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun CareBentoSection() {
    val scheme = MaterialTheme.colorScheme

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
                title = "Light",
                body = "Bright, indirect sunlight",
            )
            CareCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                iconTint = scheme.primary,
                iconBackground = scheme.primaryContainer.copy(alpha = 0.3f),
                title = "Water",
                body = "Every 1–2 weeks",
            )
        }

        ToxicityCard()
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

@Composable
private fun ToxicityCard() {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = scheme.errorContainer.copy(alpha = 0.35f),
        ),
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
                    .background(scheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = scheme.error,
                    modifier = Modifier.size(28.dp),
                )
            }

            Column {
                Text(
                    text = "Toxicity Alert",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = "Toxic to cats and dogs if ingested. Contains calcium oxalate crystals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun AiInsightsSection(candidate: Candidate) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = "AI Insights Icon",
                )

            Text(
                text = "AI Insights",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    // Placeholder text
                    text = "${candidate.scientificName} is a species of flowering plant. " +
                            "It has been introduced to many tropical areas and is a popular " +
                            "houseplant worldwide.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                    lineHeight = 26.sp,
                )

                candidate.iucnCategory?.let { iucn ->
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = scheme.secondaryContainer,
                    ) {
                        Text(
                            text = "IUCN Status: $iucn",
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
private fun NativeHabitatSection() {
    Column {
        Text(
            text = "Native Habitat",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HabitatCard(
                title = "Tropical Jungles",
                body = "Flourishes in high humidity environments with dappled shade.",
            )
            HabitatCard(
                title = "Central America",
                body = "Originates from the regions of Southern Mexico and Panama.",
            )
        }
    }
}

@Composable
private fun HabitatCard(
    title: String,
    body: String,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(scheme.primaryContainer.copy(alpha = 0.35f)),
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private data class CareItem(
    val icon: ImageVector?,
    val title: String,
    val body: String,
)

@Composable
private fun CareRoutineSection() {
    val items = listOf(
        CareItem(Icons.Outlined.Thermostat, "Temperature", "Keep between 65°F–85°F (18°C–30°C). Avoid cold drafts."),
        CareItem(Icons.Default.WaterDrop, "Humidity", "Prefers high humidity (60%+). Mist leaves or use a humidifier."),
        CareItem(Icons.Outlined.Grass, "Soil & Potting", "Well-draining, peat-based potting soil. Provide a moss pole for climbing."),
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Care Routine",
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
            .padding(vertical = 8.dp),
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
            Text(
                text = item.body,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
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

@Preview(showBackground = true, showSystemUi = true, name = "Detail – Success")
@Composable
private fun PlantDetailPreviewSuccess() {
    PlantSnapTheme {
        PlantDetailScreenContent(
            state = UiState.Success(previewCandidate),
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
            state = UiState.Success(previewCandidate),
            onBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Detail – Loading")
@Composable
private fun PlantDetailPreviewLoading() {
    PlantSnapTheme {
        PlantDetailScreenContent(
            state = UiState.Loading,
            onBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Detail – Error")
@Composable
private fun PlantDetailPreviewError() {
    PlantSnapTheme {
        PlantDetailScreenContent(
            state = UiState.Error("Plant details not found"),
            onBack = {},
        )
    }
}