package com.plantsnap.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.plantsnap.R
import com.plantsnap.ui.screens.profile.model.PlantRank
import com.plantsnap.ui.theme.PlantSnapTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import com.plantsnap.ui.components.TopBar

@Composable
fun ProfileScreen(
    authState: AuthUiState,
    statsState: ProfileStatsState,
    onSignOut: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    isSynced: Boolean = false,
    onSyncNow: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(profilePhotoUrl = authState.profilePhotoUrl)

        if (isSynced) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Filled.CloudDone,
                    contentDescription = null,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.profile_synced),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSecondaryContainer,
                    letterSpacing = 0.5.sp,
                )
            }
            Spacer(Modifier.height(8.dp))
        } else {
            Spacer(Modifier.height(20.dp))
        }
        ProfileHero(
            authState = authState,
            fireScanTimestamp = statsState.firstScanTimestamp,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(28.dp))

        StatsBentoGrid(
            statsState = statsState,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(28.dp))

        RankProgressCard(
            statsState = statsState,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(28.dp))

        SettingsSection(
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToHistory = onNavigateToHistory,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.errorContainer)
                .clickable(onClick = onSignOut)
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = scheme.onErrorContainer,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.profile_sign_out),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onErrorContainer
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSection(
    modifier: Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.profile_account_settings),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurfaceVariant,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surfaceContainerLow),
        ) {
            Column {
                SettingsRow(icon = Icons.Filled.Settings, label = stringResource(R.string.profile_settings), onClick = onNavigateToSettings)
                SettingsRow(icon = Icons.AutoMirrored.Filled.List, label = stringResource(R.string.profile_history), isLast = true, onClick = onNavigateToHistory)
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    showDivider: Boolean = false,
    isLast: Boolean = false,
    onClick: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme

    Column {
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 20.dp)
                    .background(scheme.outlineVariant.copy(alpha = 0.3f)),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,

            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = scheme.outline,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
fun CloudSyncCard(
    isSynced: Boolean,
    onSyncNow: () -> Unit,
    modifier: Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(scheme.surfaceContainerHighest)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(scheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.CloudSync,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = if (isSynced) stringResource(R.string.profile_cloud_active)
                    else stringResource(R.string.profile_cloud_sync),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = if (isSynced) stringResource(R.string.profile_up_to_date) else stringResource(
                        R.string.profile_sync_your_scans
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = onSyncNow,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary,
            ),
            modifier = Modifier.height(38.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_sync_now),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RankProgressCard(
    statsState: ProfileStatsState,
    modifier: Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val rank = statsState.rank

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainerHigh)
            .padding(16.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = rank.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = rank.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                    Text(
                        text = if (rank == PlantRank.MASTER_GARDENER) stringResource(R.string.profile_max_rank)
                        else
                            stringResource(R.string.profile_scans_to_next, statsState.scansToNextRank, PlantRank.fromScanCount(rank.maxScans).displayName),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { statsState.rankProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = scheme.primary,
                trackColor = scheme.surfaceContainerHighest
            )
        }
    }
}

@Composable
fun StatsBentoGrid(
    statsState: ProfileStatsState,
    modifier: Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(scheme.surfaceContainerLow)
                .padding(20.dp),
        ) {
            Icon(
                Icons.Filled.LocalFlorist,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.TopStart),
            )
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = statsState.totalScans.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = scheme.primary,
                )
                Text(
                    text = stringResource(R.string.profile_plants_found),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .height(170.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(scheme.primaryContainer)
                    .padding(10.dp),
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.TopStart)
                )
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = statsState.totalScans.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer,
                        fontSize = 35.sp,

                        )
                    Text(
                        text = stringResource(R.string.profile_total_scans),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.8f),
                        letterSpacing = 1.sp,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHero(
    authState: AuthUiState,
    fireScanTimestamp: Long?,
    modifier: Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHighest)
                    .border(4.dp, scheme.surfaceContainerHighest, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (authState.profilePhotoUrl != null) {
                    AsyncImage(
                        model = authState.profilePhotoUrl,
                        contentDescription = stringResource(R.string.profile_photo_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(scheme.primary)
                    .border(4.dp, scheme.background, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.profile_edit_desc),
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = authState.displayName ?: stringResource(R.string.profile_name_placeholder),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = scheme.primary,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = authState.userEmail ?: stringResource(R.string.profile_email_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )

        if (fireScanTimestamp != null) {
            Spacer(Modifier.height(4.dp))
            val dateStr = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                .format(Date(fireScanTimestamp))
            Text(
                text = "${stringResource(R.string.profile_member_since)} $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.outline,
            )
        }
    }
}
// -- Previews --

private val previewAuthState = AuthUiState(
    isLoggedIn = true,
    userEmail = "user@example.com",
    displayName = "Jane Doe",
    profilePhotoUrl = null,
)

private val previewStatsState = ProfileStatsState(
    totalScans = 12,
    firstScanTimestamp = 1704067200000, // Jan 2024
    rank = PlantRank.SPROUT,
    rankProgress = 0.7f,
    scansToNextRank = 3,
    isLoading = false,
)

@Preview(showBackground = true, showSystemUi = true, name = "Profile – Light")
@Composable
private fun ProfileScreenPreview() {
    PlantSnapTheme {
        ProfileScreen(
            authState = previewAuthState,
            statsState = previewStatsState,
            onSignOut = {},
            isSynced = true,
        )
    }
}

@Preview(
    showBackground = true, showSystemUi = true, name = "Profile – Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProfileScreenPreviewDark() {
    PlantSnapTheme(darkTheme = true) {
        ProfileScreen(
            authState = previewAuthState,
            statsState = previewStatsState,
            onSignOut = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Profile – Empty")
@Composable
private fun ProfileScreenPreviewEmpty() {
    PlantSnapTheme {
        ProfileScreen(
            authState = previewAuthState.copy(displayName = "New User"),
            statsState = ProfileStatsState(isLoading = false),
            onSignOut = {},
        )
    }
}
