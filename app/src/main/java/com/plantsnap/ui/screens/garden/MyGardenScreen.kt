package com.plantsnap.ui.screens.garden

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import coil3.compose.AsyncImage
import com.plantsnap.R
import com.plantsnap.ui.theme.PlantSnapTheme

@Composable
fun MyGardenScreen() {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.testTag("screen_garden"),
        containerColor = scheme.surface,
        topBar = { MyGardenTopBar() },
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
            item { GardenHeader(thrivingCount = 12) }
            item { TodaysTasksSection(tasks = PREVIEW_TASKS) }
            item {
                CollectionSection(
                    hero = PREVIEW_HERO,
                    others = PREVIEW_OTHERS,
                )
            }
            item { RecentProgressSection(entries = PREVIEW_PROGRESS) }
        }
    }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────

@Composable
private fun MyGardenTopBar() {
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
                text = stringResource(R.string.garden_topbar_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = scheme.primary,
                letterSpacing = (-0.5).sp,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(
                onClick = { /* no-op */ },
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
                Text(
                    text = stringResource(R.string.garden_section_title),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scheme.primary,
                    letterSpacing = (-1).sp,
                    lineHeight = 44.sp,
                )
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
private fun TodaysTasksSection(tasks: List<TodaysTask>) {
    val scheme = MaterialTheme.colorScheme

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
private fun TaskCard(task: TodaysTask) {
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
private fun CollectionSection(hero: GardenPlant, others: List<GardenPlant>) {
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
        HeroPlantCard(plant = hero)
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            others.forEach { plant -> PlantCard(plant = plant) }
            AddSpecimenCard()
        }
    }
}

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
private fun HeroPlantCard(plant: GardenPlant) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainerLow),
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
                    StatusPill(status = plant.status)
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
private fun StatusPill(status: PlantStatus) {
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
private fun PlantCard(plant: GardenPlant) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
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
private fun AddSpecimenCard() {
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

// ─── Recent Progress ──────────────────────────────────────────────────────────

@Composable
private fun RecentProgressSection(entries: List<ProgressEntry>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.garden_recent_progress), trailing = {})
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            entries.forEach { ProgressItem(entry = it) }
            LogGrowthCard()
        }
    }
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

@Composable
private fun LogGrowthCard() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .width(240.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = scheme.outlineVariant,
                shape = RoundedCornerShape(20.dp),
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(scheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                tint = scheme.primary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.garden_log_growth),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary,
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

private data class TodaysTask(
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
    TodaysTask(
        nickname = "Monty",
        species = "Monstera Deliciosa",
        imageUrl = "https://picsum.photos/seed/monty/200/200",
        type = TaskType.WATER,
        detail = "250ml",
    ),
    TodaysTask(
        nickname = "Figgy Smalls",
        species = "Fiddle Leaf Fig",
        imageUrl = "https://picsum.photos/seed/figgy/200/200",
        type = TaskType.FERTILIZE,
        detail = "Diluted",
    ),
    TodaysTask(
        nickname = "Callie",
        species = "Calathea Ornata",
        imageUrl = "https://picsum.photos/seed/callie/200/200",
        type = TaskType.MIST,
        detail = "",
        completed = true,
    ),
)

private val PREVIEW_HERO = GardenPlant(
    nickname = "Monty",
    species = "Monstera Deliciosa",
    imageUrl = "https://picsum.photos/seed/montyhero/800/500",
    status = PlantStatus.THRIVING,
    acquiredLabel = "Oct '22",
)

private val PREVIEW_OTHERS = listOf(
    GardenPlant(
        nickname = "Rapunzel",
        species = "Golden Pothos",
        imageUrl = "https://picsum.photos/seed/rapunzel/400/400",
        status = PlantStatus.THRIVING,
        wateredAgoLabel = "3d",
        heightCm = 120,
    ),
    GardenPlant(
        nickname = "Spike",
        species = "Sansevieria",
        imageUrl = "https://picsum.photos/seed/spike/400/400",
        status = PlantStatus.OK,
        wateredAgoLabel = "14d",
        lightHint = "Low Light",
    ),
)

private val PREVIEW_PROGRESS = listOf(
    ProgressEntry(
        caption = "Monstera Leaf Unfurling",
        timeAgoLabel = "2 days ago",
        imageUrl = "https://picsum.photos/seed/progress1/400/250",
    ),
    ProgressEntry(
        caption = "Pothos Length Check",
        timeAgoLabel = "1 week ago",
        imageUrl = "https://picsum.photos/seed/progress2/400/250",
    ),
)

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "My Garden – Light")
@Composable
private fun MyGardenScreenPreviewLight() {
    PlantSnapTheme { MyGardenScreen() }
}

@Preview(
    showBackground = true, showSystemUi = true, name = "My Garden – Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MyGardenScreenPreviewDark() {
    PlantSnapTheme(darkTheme = true) { MyGardenScreen() }
}
