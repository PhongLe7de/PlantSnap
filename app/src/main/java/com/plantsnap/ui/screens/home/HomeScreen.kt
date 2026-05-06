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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.plantsnap.R
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.PlantOfTheDay
import com.plantsnap.domain.models.ScanResult
import com.plantsnap.ui.components.TopBar
import com.plantsnap.ui.screens.garden.CareTaskUi
import com.plantsnap.ui.screens.garden.DueLabel
import com.plantsnap.ui.screens.garden.dueLabelFor
import com.plantsnap.ui.screens.profile.AuthUiState
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme
import com.plantsnap.ui.util.FALLBACK_IMAGE_URL
import com.plantsnap.ui.util.validImageUrlOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Navigation callbacks consumed by the Home screen. Grouped to keep param counts low. */
data class HomeCallbacks(
    val onIdentifyPlantSelected: () -> Unit = {},
    val onLearnMorePlantOfTheDay: () -> Unit = {},
    val onViewAllScans: () -> Unit = {},
    val onScanSelected: (plantId: String, candidateIndex: Int) -> Unit = { _, _ -> },
    val onProfileSelected: () -> Unit = {},
)

@Composable
fun HomeScreen(
    callbacks: HomeCallbacks,
    authState: AuthUiState,
    profilePhotoUrl: String? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val scansState by viewModel.scansState.collectAsState()
    val plantOfTheDayState by viewModel.plantOfTheDayState.collectAsState()
    val careTasks by viewModel.upcomingCareTasks.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    HomeScreenContent(
        callbacks = callbacks,
        profilePhotoUrl = profilePhotoUrl,
        scansState = scansState,
        plantOfTheDayState = plantOfTheDayState,
        authState = authState,
        careTasks = careTasks,
        onCareTaskDone = viewModel::markCareTaskDone,
    )
}

@Composable
fun HomeScreenContent(
    callbacks: HomeCallbacks = HomeCallbacks(),
    profilePhotoUrl: String? = null,
    scansState: UiState<List<ScanResult>>,
    plantOfTheDayState: UiState<PlantOfTheDay> = UiState.Idle,
    authState: AuthUiState,
    careTasks: List<CareTaskUi> = emptyList(),
    onCareTaskDone: (String) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.testTag("screen_home"),
        containerColor = scheme.surface,
        topBar = {
            TopBar(
                profilePhotoUrl = profilePhotoUrl,
                onProfileSelected = callbacks.onProfileSelected,
            )
         },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item { WelcomeSection(authState = authState) }
            item { Spacer(Modifier.height(20.dp)) }
            item { IdentifySection(onIdentifyPlantSelected = callbacks.onIdentifyPlantSelected) }
            item { Spacer(Modifier.height(20.dp)) }

            item {
                RecentScansHeader(onViewAllScans = callbacks.onViewAllScans)
                Spacer(Modifier.height(12.dp))
            }

            when (scansState) {
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
                        text = stringResource(R.string.home_error, scansState.message),
                        color = scheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }

                is UiState.Success -> {
                    if (scansState.data.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.home_no_plants),
                                color = scheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                    } else {
                        items(scansState.data) { plant ->
                            val formattedDate = remember(plant.timestamp) {
                                SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                                    .format(Date(plant.timestamp))
                            }
                            val topCandidate = plant.candidates.firstOrNull()
                            ScanCard(
                                modifier = Modifier.fillMaxWidth(),
                                plantName = plant.bestMatch,
                                commonName = topCandidate?.commonNames?.firstOrNull() ?: "",
                                timeLabel = formattedDate,
                                imageModel = plant.imagePath.takeIf { it.isNotBlank() }
                                    ?: topCandidate?.imageUrl?.takeIf { it.isNotBlank() },
                                onClick = { callbacks.onScanSelected(plant.id, 0) },
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { PlantOfTheDaySection(plantOfTheDayState, callbacks.onLearnMorePlantOfTheDay) }
            item { Spacer(Modifier.height(20.dp)) }
            item { DailyCareSection(tasks = careTasks, onMarkDone = onCareTaskDone) }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

fun getGreeting(): Int {
    return try {
        val hour = java.time.LocalTime.now().hour
        return when (hour) {
            in 5..11 -> R.string.home_morning
            in 12..16 -> R.string.home_afternoon
            in 17..21 -> R.string.home_evening
            in 22..23, in 0..4 -> R.string.home_night
            else -> R.string.home_greeting
        }
    } catch (e: Exception) {
        R.string.home_greeting
    }
}

@Composable
private fun WelcomeSection(authState: AuthUiState) {
    val scheme = MaterialTheme.colorScheme

    val displayName = authState.displayName ?: "Gardener"
    val firstName = displayName.split(" ").firstOrNull() ?: "Gardener"
    val greetingText = stringResource(getGreeting(), firstName)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        Text(
            text = greetingText,
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
private fun RecentScansHeader(onViewAllScans: () -> Unit) {
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
        TextButton(onClick = onViewAllScans) {
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
    imageModel: Any? = null,
    onClick: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(scheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.TopStart,
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = plantName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

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
private fun PlantOfTheDaySection(
    state: UiState<PlantOfTheDay>,
    onLearnMore: () -> Unit,
) {
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
            when (state) {
                is UiState.Idle, is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = scheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    ) {
                        Text(state.message, color = scheme.error)
                    }
                }
                is UiState.Success -> {
                    val data = state.data
                    Column(modifier = Modifier.padding(20.dp)) {
                        AsyncImage(
                            model = data.imageUrl.validImageUrlOrNull() ?: FALLBACK_IMAGE_URL,
                            contentDescription = data.commonName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(scheme.secondary.copy(alpha = 0.28f)),
                        )
                    }
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(
                            text = data.commonName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = scheme.onSecondaryContainer,
                            lineHeight = 28.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = data.description ?: stringResource(R.string.detail_info_unavailable),
                            fontSize = 13.sp,
                            color = scheme.onSecondaryContainer.copy(alpha = 0.75f),
                            lineHeight = 20.sp,
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onLearnMore,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.home_learn),
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimary,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyCareSection(
    tasks: List<CareTaskUi>,
    onMarkDone: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_daily_care),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )
        Spacer(Modifier.height(12.dp))

        if (tasks.isEmpty()) {
            Text(
                text = stringResource(R.string.home_care_empty),
                fontSize = 13.sp,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            tasks.forEachIndexed { index, task ->
                CareTaskItem(
                    title = "${task.taskTypeShort()}: ${task.plantNickname}",
                    subtitle = task.dueSubtitle(),
                    accentColor = scheme.primary,
                    onDoneClick = { onMarkDone(task.id) },
                )
                if (index != tasks.lastIndex) Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CareTaskUi.taskTypeShort(): String = stringResource(
    when (taskType) {
        com.plantsnap.domain.models.CareTaskType.WATER -> R.string.care_task_water_short
        com.plantsnap.domain.models.CareTaskType.FERTILIZE -> R.string.care_task_fertilize_short
        com.plantsnap.domain.models.CareTaskType.MIST -> R.string.care_task_mist_short
        com.plantsnap.domain.models.CareTaskType.ROTATE -> R.string.care_task_rotate_short
        com.plantsnap.domain.models.CareTaskType.REPOT -> R.string.care_task_repot_short
    }
)

@Composable
private fun CareTaskUi.dueSubtitle(): String {
    val now = System.currentTimeMillis()
    return when (val label = dueLabelFor(nextDueAt, now)) {
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
}

@Composable
private fun CareTaskItem(
    title: String,
    subtitle: String,
    accentColor: Color,
    onDoneClick: () -> Unit,
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
                    onClick = onDoneClick,
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

private const val PREVIEW_SNAKE_PLANT_SCIENTIFIC = "Dracaena trifasciata"

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
        bestMatch = PREVIEW_SNAKE_PLANT_SCIENTIFIC,
        candidates = listOf(
            Candidate(
                scientificName = PREVIEW_SNAKE_PLANT_SCIENTIFIC,
                commonNames = listOf("Snake Plant", "Mother-in-law's Tongue"),
                family = "Asparagaceae",
                score = 0.91f,
                iucnCategory = "LC",
            )
        ),
    ),
)

private val previewAuthState = AuthUiState(
    isLoggedIn = true,
    userEmail = "user@example.com",
    displayName = "Jane Doe",
    profilePhotoUrl = null,
)

private val plantOfTheDay = PlantOfTheDay(
    scientificName = PREVIEW_SNAKE_PLANT_SCIENTIFIC,
    commonName = "Snake Plant",
    description = "A hardy, low-maintenance plant with striking upright leaves.",
)

@Preview(showBackground = true, showSystemUi = true, name = "Success – Light")
@Composable
private fun HomeScreenPreviewSuccess() {
    PlantSnapTheme {
        HomeScreenContent(
            scansState = UiState.Success(previewPlants),
            plantOfTheDayState = UiState.Success(plantOfTheDay),
            authState = previewAuthState,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Success – Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreviewSuccessDark() {
    PlantSnapTheme(darkTheme = true) {
        HomeScreenContent(
            scansState = UiState.Success(previewPlants),
            plantOfTheDayState = UiState.Success(plantOfTheDay),
            authState = previewAuthState,
            )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Loading")
@Composable
private fun HomeScreenPreviewLoading() {
    PlantSnapTheme {
        HomeScreenContent(
            scansState = UiState.Loading,
            plantOfTheDayState = UiState.Loading,
            authState = previewAuthState,
            )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Empty")
@Composable
private fun HomeScreenPreviewEmpty() {
    PlantSnapTheme {
        HomeScreenContent(
            scansState = UiState.Success(emptyList()),
            authState = previewAuthState,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Error")
@Composable
private fun HomeScreenPreviewError() {
    PlantSnapTheme {
        HomeScreenContent(
            scansState = UiState.Error("Couldn't fetch results"),
            authState = previewAuthState,
        )
    }
}