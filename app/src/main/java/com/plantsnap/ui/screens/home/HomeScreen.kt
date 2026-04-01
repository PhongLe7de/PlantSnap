package com.plantsnap.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.R
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.ui.components.TopBar
import com.plantsnap.ui.state.UiState

private data class AppColors(
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val surface: Color,
    val surfaceContainerLow: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
)

@Composable
private fun rememberAppColors() = AppColors(
    primary = colorResource(R.color.primary),
    primaryContainer = colorResource(R.color.primary_container),
    secondary = colorResource(R.color.secondary),
    secondaryContainer = colorResource(R.color.secondary_container),
    onSecondaryContainer = colorResource(R.color.on_secondary_container),
    tertiary = colorResource(R.color.tertiary),
    surface = colorResource(R.color.surface),
    surfaceContainerLow = colorResource(R.color.surface_container_low),
    onSurface = colorResource(R.color.on_surface),
    onSurfaceVariant = colorResource(R.color.on_surface_variant),
    outline = colorResource(R.color.outline),
)

@Composable
fun HomeScreen(
    onIdentifyPlantSelected: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    HomeScreenContent(
        onIdentifyPlantSelected = onIdentifyPlantSelected,
        state = state,
    )
}

@Composable
fun HomeScreenContent(
    onIdentifyPlantSelected: () -> Unit,
    state: UiState<List<ScanResult>>,
) {
    val colors = rememberAppColors()

    Scaffold(
        modifier = Modifier.testTag("screen_home"),
        containerColor = colors.surface,
        topBar = { TopBar() },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item { WelcomeSection(colors) }
            item { Spacer(Modifier.height(20.dp)) }
            item {
                IdentifySection(
                    colors,
                    onIdentifyPlantSelected = onIdentifyPlantSelected,
                )
            }
            item { Spacer(Modifier.height(20.dp)) }

            item {
                RecentScansHeader(colors)
                Spacer(Modifier.height(12.dp))
            }

            when (state) {
                is UiState.Idle,
                is UiState.Loading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                is UiState.Error -> item {
                    Text(
                        text = stringResource(R.string.error_loading_plants, state.message),
                        color = colors.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }

                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_plants_found),
                                color = colors.onSurfaceVariant,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                    } else {
                        items(state.data) { plant ->
                            ScanCard(
                                modifier = Modifier.fillMaxWidth(),
                                plantName = plant.bestMatch,
                                commonName = plant.candidates.firstOrNull()?.commonNames?.firstOrNull()
                                    ?: "",
                                timeLabel = stringResource(R.string.scan_time), // TODO: replace with real scan timestamp
                                colors = colors,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { PlantOfTheDaySection(colors) }
            item { Spacer(Modifier.height(20.dp)) }
            item { DailyCareSection(colors) }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun WelcomeSection(colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.home_greeting),
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.primary,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.home_subtitle),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun IdentifySection(
    colors: AppColors,
    onIdentifyPlantSelected: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.primaryContainer.copy(alpha = 0.75f),
                        colors.primary
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column {
            Text(
                text = stringResource(R.string.identify_title),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.identify_description),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.80f),
                modifier = Modifier.fillMaxWidth(0.72f),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onIdentifyPlantSelected()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_identify_plant_cta"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.identify_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun RecentScansHeader(colors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.recent_scans),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
        )
        TextButton(onClick = { /* TODO: Navigate to scans */ }) {
            Text(
                text = stringResource(R.string.view_all),
                color = colors.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun ScanCard(
    modifier: Modifier = Modifier,
    plantName: String,
    commonName: String,
    timeLabel: String,
    colors: AppColors,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(colors.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.TopStart,
        ) {}

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = plantName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
            Text(
                text = commonName,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
            )
            if (timeLabel.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = timeLabel,
                    fontSize = 12.sp,
                    color = colors.outline,
                )
            }
        }
    }
}

@Composable
private fun PlantOfTheDaySection(colors: AppColors) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.plant_of_the_day),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
        )
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(colors.secondary.copy(alpha = 0.28f)),
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Cool Plant", // Placeholder text
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primary,
                    letterSpacing = (-0.3).sp,
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Cool plant description blah blah blah blah blah blah blah blah blah",
                    fontSize = 14.sp,
                    color = colors.onSurface.copy(alpha = 0.75f),
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {}, // TODO: navigate to plant detail
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.learn_more),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyCareSection(colors: AppColors) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.daily_care_tasks),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
        )
        Spacer(Modifier.height(12.dp))

        CareTaskItem(
            title = stringResource(R.string.care_watering_title),
            subtitle = stringResource(R.string.care_watering_desc),
            accentColor = colors.primary,
            colors = colors,
        )
        Spacer(Modifier.height(8.dp))
        CareTaskItem(
            title = stringResource(R.string.care_rotate_title),
            subtitle = stringResource(R.string.care_rotate_desc),
            accentColor = colors.primary,
            colors = colors,
        )
    }
}

@Composable
private fun CareTaskItem(
    title: String,
    subtitle: String,
    accentColor: Color,
    colors: AppColors,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(accentColor),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Icon placeholder
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(accentColor.copy(alpha = 0.55f), CircleShape)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                    )
                }

                OutlinedButton(
                    onClick = {}, // TODO: mark task done
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Success")
@Composable
private fun HomeScreenPreviewSuccess() {
    HomeScreenContent(
        state = UiState.Success(
            listOf(
                ScanResult(
                    imagePath = "",
                    organ = "leaf",
                    bestMatch = "Monstera deliciosa",
                    candidates = listOf(
                        Candidate(
                            scientificName = "Monstera deliciosa",
                            commonNames = listOf("Swiss Cheese Plant"),
                            family = "Araceae",
                            score = 0.97f,
                            iucnCategory = null,
                        )
                    ),
                    aiInfo = null,
                ),
                ScanResult(
                    imagePath = "",
                    organ = "leaf",
                    bestMatch = "Dracaena trifasciata",
                    candidates = listOf(
                        Candidate(
                            scientificName = "Dracaena trifasciata",
                            commonNames = listOf("Snake Plant", "Mother-in-law's Tongue"),
                            family = "Asparagaceae",
                            score = 0.91f,
                            iucnCategory = "LC",
                        )
                    ),
                    aiInfo = null,
                ),
            )
        ),
        onIdentifyPlantSelected = {}
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Loading")
@Composable
private fun HomeScreenPreviewLoading() {
    HomeScreenContent(state = UiState.Loading, onIdentifyPlantSelected = {})
}

@Preview(showBackground = true, showSystemUi = true, name = "Empty")
@Composable
private fun HomeScreenPreviewEmpty() {
    HomeScreenContent(state = UiState.Success(emptyList()), onIdentifyPlantSelected = {})
}

@Preview(showBackground = true, showSystemUi = true, name = "Error")
@Composable
private fun HomeScreenPreviewError() {
    HomeScreenContent(
        state = UiState.Error("Couldn't fetch results"),
        onIdentifyPlantSelected = {})
}