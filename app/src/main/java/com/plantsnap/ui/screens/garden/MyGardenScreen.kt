package com.plantsnap.ui.screens.garden

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.plantsnap.ui.state.UiState
import com.plantsnap.ui.theme.PlantSnapTheme

@Composable
fun MyGardenScreen(
    onAddSpecimen: () -> Unit,
    onPlantClick: (savedPlantId: String) -> Unit = {},
) {
    val viewModel: MyGardenViewModel = hiltViewModel()
    val plantsState by viewModel.plants.collectAsState()
    MyGardenScreenContent(
        plantsState = plantsState,
        onAddSpecimen = {
            viewModel.resetIdentifyFlow()
            onAddSpecimen()
        },
        onPlantClick = onPlantClick,
    )
}

@Composable
private fun MyGardenScreenContent(
    plantsState: UiState<List<SavedPlantUi>>,
    onAddSpecimen: () -> Unit,
    onPlantClick: (savedPlantId: String) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.testTag("screen_garden"),
        containerColor = scheme.surface,
        topBar = { MyGardenTopBar(onAddClick = onAddSpecimen) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            item {
                val thrivingCount = (plantsState as? UiState.Success)?.data?.size ?: 0
                GardenHeader(thrivingCount = thrivingCount)
            }
            item { TodayTasksSection() }
            item {
                when (plantsState) {
                    is UiState.Idle, is UiState.Loading -> CollectionLoadingSection()
                    is UiState.Error -> CollectionEmptySection(onAddSpecimen = onAddSpecimen)
                    is UiState.Success -> {
                        if (plantsState.data.isEmpty()) {
                            CollectionEmptySection(onAddSpecimen = onAddSpecimen)
                        } else {
                            CollectionSectionFromSaved(
                                saved = plantsState.data,
                                onAddSpecimen = onAddSpecimen,
                                onPlantClick = onPlantClick,
                            )
                        }
                    }
                }
            }
            item {
                val saved = (plantsState as? UiState.Success)?.data.orEmpty()
                if (saved.isNotEmpty()) RecentAdditionsSection(saved = saved)
            }
        }
    }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────

@Composable
private fun MyGardenTopBar(onAddClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheme.surface.copy(alpha = 0.85f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                imageVector = Icons.Filled.LocalFlorist,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = stringResource(R.string.garden_section_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = scheme.primary,
                letterSpacing = (-0.5).sp,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHigh),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = scheme.primary,
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun GardenHeader(thrivingCount: Int) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.garden_section_subtitle, thrivingCount),
                    fontSize = 15.sp,
                    color = scheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = scheme.secondaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = null,
                        tint = scheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.garden_all_watered),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

// ─── Today's Tasks ────────────────────────────────────────────────────────────

@Composable
private fun TodayTasksSection() {
    val scheme = MaterialTheme.colorScheme
    val tasks = PREVIEW_TASKS

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.garden_tasks_title),
            trailing = {
                TextButton(onClick = { /* no-op */ }) {
                    Text(
                        text = stringResource(R.string.garden_view_all),
                        color = scheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            },
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            tasks.forEach { task -> TaskCard(task = task) }
        }
    }
}

@Composable
private fun TaskCard(task: TodayTask) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainerLowest),
    ) {
        // Decorative corner blob (top-right quarter circle)
        val blobTint = when (task.type) {
            TaskType.FERTILIZE -> scheme.tertiaryContainer.copy(alpha = 0.1f)
            else -> scheme.primary.copy(alpha = 0.05f)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(96.dp)
                .offset(x = 16.dp, y = (-16).dp)
                .clip(CircleShape)
                .background(blobTint),
        )

        // Main content (dimmed when completed)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (task.completed) Modifier.alpha(0.55f) else Modifier)
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    AsyncImage(
                        model = task.imageUrl,
                        contentDescription = task.nickname,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(scheme.surfaceContainerHigh)
                            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.15f), CircleShape),
                    )
                    Column {
                        Text(
                            text = task.nickname,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                        )
                        Text(
                            text = task.species,
                            fontSize = 13.sp,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                TaskCheckButton(checked = task.completed)
            }
            Spacer(Modifier.height(20.dp))
            TaskTypeLabel(type = task.type, detail = task.detail)
        }

        // Centered "Completed" pill overlay
        if (task.completed) {
            Surface(
                shape = RoundedCornerShape(50),
                color = scheme.surfaceContainerHighest.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .border(
                        1.dp,
                        scheme.outlineVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(50),
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = scheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.garden_completed),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCheckButton(checked: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (checked) scheme.primary else Color.Transparent)
            .border(
                width = 2.dp,
                color = scheme.primary,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = scheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun TaskTypeLabel(type: TaskType, detail: String) {
    val scheme = MaterialTheme.colorScheme
    val (icon: ImageVector, label: String, tint: Color) = when (type) {
        TaskType.WATER -> Triple(
            Icons.Filled.WaterDrop,
            stringResource(R.string.garden_task_water, detail),
            scheme.primary,
        )
        TaskType.FERTILIZE -> Triple(
            Icons.Outlined.Eco,
            stringResource(R.string.garden_task_fertilize, detail),
            scheme.tertiary,
        )
        TaskType.MIST -> Triple(
            Icons.Filled.Cloud,
            stringResource(R.string.garden_task_mist),
            scheme.primary,
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = tint)
    }
}

// ─── Collection ───────────────────────────────────────────────────────────────

@Composable
private fun CollectionSectionFromSaved(
    saved: List<SavedPlantUi>,
    onAddSpecimen: () -> Unit,
    onPlantClick: (savedPlantId: String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.garden_collection_title),
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallCircleButton(icon = Icons.Filled.FilterList)
                    SmallCircleButton(icon = Icons.Outlined.GridView)
                }
            },
        )
        Spacer(Modifier.height(16.dp))
        val heroSaved = saved.first()
        HeroPlantCard(
            plant = heroSaved.toGardenPlant(),
            savedPlantId = heroSaved.plant.id,
            onClick = { onPlantClick(heroSaved.plant.id) },
        )
        val others = saved.drop(1).take(2)
        if (others.isNotEmpty() || saved.size > 1) {
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                others.forEach { sp ->
                    PlantCard(
                        plant = sp.toGardenPlant(),
                        savedPlantId = sp.plant.id,
                        onClick = { onPlantClick(sp.plant.id) },
                    )
                }
                AddSpecimenCard(onClick = onAddSpecimen)
            }
        } else {
            Spacer(Modifier.height(16.dp))
            AddSpecimenCard(onClick = onAddSpecimen)
        }
    }
}

@Composable
private fun CollectionEmptySection(onAddSpecimen: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.garden_collection_title),
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallCircleButton(icon = Icons.Filled.FilterList)
                    SmallCircleButton(icon = Icons.Outlined.GridView)
                }
            },
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(scheme.surfaceContainerLow)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.garden_empty_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.garden_empty_desc),
                    fontSize = 13.sp,
                    color = scheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        AddSpecimenCard(onClick = onAddSpecimen)
    }
}

@Composable
private fun CollectionLoadingSection() {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.garden_collection_title), trailing = {})
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator(color = scheme.primary) }
    }
}

private fun SavedPlantUi.toGardenPlant(): GardenPlant = GardenPlant(
    nickname = plant.plant.commonNames.firstOrNull() ?: plant.plant.scientificName,
    species = plant.plant.scientificName,
    imageUrl = displayImageUrl ?: "https://picsum.photos/seed/${plant.plant.scientificName.hashCode()}/600/400",
    status = PlantStatus.THRIVING,
    acquiredLabel = null,
    wateredAgoLabel = null,
    heightCm = null,
    lightHint = null,
)

@Composable
private fun SmallCircleButton(icon: ImageVector) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(scheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun HeroPlantCard(
    plant: GardenPlant,
    savedPlantId: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .testTag("garden_plant_card_$savedPlantId"),
    ) {
        AsyncImage(
            model = plant.imageUrl,
            contentDescription = plant.nickname,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // bottom gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.55f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.45f),
                        ),
                    ),
                ),
        )
        // Glass panel
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = scheme.surface.copy(alpha = 0.72f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    StatusPill()
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = plant.nickname,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.primary,
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        text = buildString {
                            append(plant.species)
                            plant.acquiredLabel?.let { append(" • ").append(stringResource(R.string.garden_acquired_label, it)) }
                        },
                        fontSize = 12.sp,
                        color = scheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(scheme.surfaceContainerLowest),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = scheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill() {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(50),
        color = scheme.surface.copy(alpha = 0.85f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(scheme.inversePrimary),
            )
            Text(
                text = stringResource(R.string.garden_status_thriving),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
private fun PlantCard(
    plant: GardenPlant,
    savedPlantId: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("garden_plant_card_$savedPlantId"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(scheme.surfaceContainerLow),
        ) {
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = plant.nickname,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // small status icon top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerLowest.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center,
            ) {
                val (icon: ImageVector, tint: Color) = when (plant.status) {
                    PlantStatus.THRIVING -> Icons.Outlined.Eco to scheme.primary
                    PlantStatus.OK -> Icons.Filled.Shield to scheme.secondary
                    PlantStatus.NEEDS_ATTENTION -> Icons.Outlined.WbSunny to scheme.tertiary
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = plant.nickname,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(scheme.inversePrimary),
            )
        }
        Text(
            text = plant.species,
            fontSize = 12.sp,
            color = scheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            plant.wateredAgoLabel?.let { ago ->
                PlantStat(icon = Icons.Filled.WaterDrop, label = ago)
            }
            plant.heightCm?.let { h ->
                PlantStat(icon = Icons.Outlined.Straighten, label = stringResource(R.string.garden_plant_height, h))
            }
            plant.lightHint?.let { light ->
                PlantStat(icon = Icons.Outlined.WbSunny, label = light)
            }
        }
    }
}

@Composable
private fun PlantStat(icon: ImageVector, label: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AddSpecimenCard(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = scheme.outlineVariant,
                shape = RoundedCornerShape(20.dp),
            )
            .background(scheme.surfaceContainerLow.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .testTag("btn_garden_add_specimen")
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(scheme.surfaceContainerLowest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.AddCircle,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.garden_add_specimen),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )
        Text(
            text = stringResource(R.string.garden_add_specimen_desc),
            fontSize = 11.sp,
            color = scheme.onSurfaceVariant,
        )
    }
}

// ─── Recent Additions ─────────────────────────────────────────────────────────

@Composable
private fun RecentAdditionsSection(saved: List<SavedPlantUi>) {
    val now = System.currentTimeMillis()
    val entries = saved
        .sortedByDescending { it.plant.createdAt }
        .take(5)
        .map { it.toProgressEntry(now) }

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.garden_recent_additions), trailing = {})
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            entries.forEach { ProgressItem(entry = it) }
        }
    }
}

private fun SavedPlantUi.toProgressEntry(now: Long): ProgressEntry {
    val candidate = plant.plant
    val nickname = candidate.commonNames.firstOrNull() ?: candidate.scientificName
    val timeAgo = DateUtils.getRelativeTimeSpanString(
        plant.createdAt,
        now,
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
    return ProgressEntry(
        caption = nickname,
        timeAgoLabel = timeAgo,
        imageUrl = displayImageUrl
            ?: "https://picsum.photos/seed/${candidate.scientificName.hashCode()}/400/250",
    )
}

@Composable
private fun ProgressItem(entry: ProgressEntry) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .width(240.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainerLowest)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.caption,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = scheme.surfaceContainer,
            ) {
                Text(
                    text = entry.timeAgoLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 10.sp,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
        AsyncImage(
            model = entry.imageUrl,
            contentDescription = entry.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(scheme.surfaceContainerLow),
        )
    }
}

// ─── Shared bits ──────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, trailing: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )
        trailing()
    }
}

// ─── Data classes + preview data ──────────────────────────────────────────────

private enum class TaskType { WATER, FERTILIZE, MIST }

private enum class PlantStatus { THRIVING, OK, NEEDS_ATTENTION }

private data class TodayTask(
    val nickname: String,
    val species: String,
    val imageUrl: String,
    val type: TaskType,
    val detail: String,
    val completed: Boolean = false,
)

private data class GardenPlant(
    val nickname: String,
    val species: String,
    val imageUrl: String,
    val status: PlantStatus,
    val acquiredLabel: String? = null,
    val wateredAgoLabel: String? = null,
    val heightCm: Int? = null,
    val lightHint: String? = null,
)

private data class ProgressEntry(
    val caption: String,
    val timeAgoLabel: String,
    val imageUrl: String,
)

private val PREVIEW_TASKS = listOf(
    TodayTask(
        nickname = "Monty",
        species = "Monstera Deliciosa",
        imageUrl = "https://picsum.photos/seed/monty/200/200",
        type = TaskType.WATER,
        detail = "250ml",
    ),
    TodayTask(
        nickname = "Figgy Smalls",
        species = "Fiddle Leaf Fig",
        imageUrl = "https://picsum.photos/seed/figgy/200/200",
        type = TaskType.FERTILIZE,
        detail = "Diluted",
    ),
    TodayTask(
        nickname = "Callie",
        species = "Calathea Ornata",
        imageUrl = "https://picsum.photos/seed/callie/200/200",
        type = TaskType.MIST,
        detail = "",
        completed = true,
    ),
)

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "My Garden – Light")
@Composable
private fun MyGardenScreenPreviewLight() {
    PlantSnapTheme {
        MyGardenScreenContent(plantsState = UiState.Success(emptyList()), onAddSpecimen = {})
    }
}

@Preview(
    showBackground = true, showSystemUi = true, name = "My Garden – Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MyGardenScreenPreviewDark() {
    PlantSnapTheme(darkTheme = true) {
        MyGardenScreenContent(plantsState = UiState.Success(emptyList()), onAddSpecimen = {})
    }
}
