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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import com.plantsnap.ui.theme.PlantSnapTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onIdentifyPlantSelected: () -> Unit,
    profilePhotoUrl: String? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    HomeScreenContent(
        onIdentifyPlantSelected = onIdentifyPlantSelected,
        profilePhotoUrl = profilePhotoUrl,
        state = state,
    )
}

@Composable
fun HomeScreenContent(
    onIdentifyPlantSelected: () -> Unit,
    profilePhotoUrl: String? = null,
    state: UiState<List<ScanResult>>,
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.testTag("screen_home"),
        containerColor = scheme.surface,
        topBar = { TopBar(profilePhotoUrl = profilePhotoUrl) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item { WelcomeSection() }
            item { Spacer(Modifier.height(20.dp)) }
            item { IdentifySection(onIdentifyPlantSelected = onIdentifyPlantSelected) }
            item { Spacer(Modifier.height(20.dp)) }

            item {
                RecentScansHeader()
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
                        CircularProgressIndicator(color = scheme.primary)
                    }
                }

                is UiState.Error -> item {
                    Text(
                        text = stringResource(R.string.home_error, state.message),
                        color = scheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }

                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.home_no_plants),
                                color = scheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                    } else {
                        items(state.data) { plant ->
                            val formattedDate = remember(plant.timestamp) {
                                SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                                    .format(Date(plant.timestamp))
                            }
                            ScanCard(
                                modifier = Modifier.fillMaxWidth(),
                                plantName = plant.bestMatch,
                                commonName = plant.candidates.firstOrNull()?.commonNames?.firstOrNull() ?: "",
                                timeLabel = formattedDate,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { PlantOfTheDaySection() }
            item { Spacer(Modifier.height(20.dp)) }
            item { DailyCareSection() }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun WelcomeSection() {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.home_greeting),
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 40.sp,
            color = scheme.primary,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.home_subtitle, 12), // Int placeholder
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IdentifySection(onIdentifyPlantSelected: () -> Unit) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        scheme.primary.copy(alpha = 0.4f),
                        scheme.primary
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_identify_title),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_identify_desc),
                fontSize = 14.sp,
                color = scheme.onPrimary.copy(alpha = 0.80f),
                modifier = Modifier.fillMaxWidth(0.72f),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onIdentifyPlantSelected() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_identify_plant_cta"),
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_identify_button),
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun RecentScansHeader() {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_recent_scans),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )
        TextButton(onClick = { /* TODO: Navigate to scans */ }) {
            Text(
                text = stringResource(R.string.home_view_all),
                color = scheme.primary,
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
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(scheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.TopStart,
        ) {}

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = plantName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            Text(
                text = commonName,
                fontSize = 12.sp,
                color = scheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
            )
            if (timeLabel.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = timeLabel,
                    fontSize = 12.sp,
                    color = scheme.outline,
                )
            }
        }
    }
}

@Composable
private fun PlantOfTheDaySection() {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_potd),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(scheme.secondary.copy(alpha = 0.28f)),
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Cool Plant", // Placeholder text
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scheme.primary,
                    letterSpacing = (-0.3).sp,
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Cool plant description blah blah blah blah blah blah blah blah blah",
                    fontSize = 14.sp,
                    color = scheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {}, // TODO: navigate to plant detail
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_learn),
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimary,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyCareSection() {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_daily_care),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )
        Spacer(Modifier.height(12.dp))

        CareTaskItem(
            title = stringResource(R.string.home_watering_title, "Plant Name"),
            subtitle = stringResource(R.string.home_watering_desc),
            accentColor = scheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        CareTaskItem(
            title = stringResource(R.string.home_rotate_title),
            subtitle = stringResource(R.string.home_rotate_desc),
            accentColor = scheme.primary,
        )
    }
}

@Composable
private fun CareTaskItem(
    title: String,
    subtitle: String,
    accentColor: Color,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
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
                        color = scheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = scheme.onSurfaceVariant,
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
                        text = stringResource(R.string.home_done),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private val previewPlants = listOf(
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
    ),
)

@Preview(showBackground = true, showSystemUi = true, name = "Success – Light")
@Composable
private fun HomeScreenPreviewSuccess() {
    PlantSnapTheme {
        HomeScreenContent(state = UiState.Success(previewPlants), onIdentifyPlantSelected = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Success – Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreviewSuccessDark() {
    PlantSnapTheme(darkTheme = true) {
        HomeScreenContent(state = UiState.Success(previewPlants), onIdentifyPlantSelected = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Loading")
@Composable
private fun HomeScreenPreviewLoading() {
    PlantSnapTheme {
        HomeScreenContent(state = UiState.Loading, onIdentifyPlantSelected = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Empty")
@Composable
private fun HomeScreenPreviewEmpty() {
    PlantSnapTheme {
        HomeScreenContent(state = UiState.Success(emptyList()), onIdentifyPlantSelected = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Error")
@Composable
private fun HomeScreenPreviewError() {
    PlantSnapTheme {
        HomeScreenContent(state = UiState.Error("Couldn't fetch results"), onIdentifyPlantSelected = {})
    }
}