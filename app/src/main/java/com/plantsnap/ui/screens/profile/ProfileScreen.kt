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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

@Composable
fun ProfileScreen(
    authState: AuthUiState,
    statsState: ProfileStatsState,
    onSignOut: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        ProfileHeader(authState = authState, firstScanTimestamp = statsState.firstScanTimestamp)

        Spacer(Modifier.height(20.dp))

        RankCard(statsState = statsState)

        Spacer(Modifier.height(20.dp))

        StatItem(value = statsState.totalScans, label = stringResource(R.string.profile_stat_scanned))

        Spacer(Modifier.weight(1f))

        TextButton(onClick = onSignOut) {
            Text(
                text = stringResource(R.string.profile_sign_out),
                color = scheme.error,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ProfileHeader(authState: AuthUiState, firstScanTimestamp: Long?) {
    val scheme = MaterialTheme.colorScheme

    if (authState.profilePhotoUrl != null) {
        AsyncImage(
            model = authState.profilePhotoUrl,
            contentDescription = "Profile photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .border(2.dp, scheme.primaryContainer, CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(scheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(48.dp),
                tint = scheme.onPrimaryContainer,
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    Text(
        text = authState.displayName ?: stringResource(R.string.profile_default_name),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = scheme.primary,
    )

    if (authState.userEmail != null) {
        Spacer(Modifier.height(2.dp))
        Text(
            text = authState.userEmail,
            fontSize = 14.sp,
            color = scheme.onSurfaceVariant,
        )
    }

    if (firstScanTimestamp != null) {
        Spacer(Modifier.height(4.dp))
        val dateStr = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(firstScanTimestamp))
        Text(
            text = stringResource(R.string.profile_member_since, dateStr),
            fontSize = 12.sp,
            color = scheme.outline,
        )
    }
}

@Composable
private fun RankCard(statsState: ProfileStatsState) {
    val scheme = MaterialTheme.colorScheme
    val rank = statsState.rank

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = rank.emoji, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = rank.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                    )
                    if (rank != PlantRank.MASTER_GARDENER) {
                        Text(
                            text = stringResource(
                                R.string.profile_scans_to_next,
                                statsState.scansToNextRank,
                                PlantRank.fromScanCount(rank.maxScans).displayName
                            ),
                            fontSize = 13.sp,
                            color = scheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.profile_max_rank),
                            fontSize = 13.sp,
                            color = scheme.onSurfaceVariant,
                        )
                    }
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
                trackColor = scheme.primaryContainer.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun StatItem(modifier: Modifier = Modifier, value: Int, label: String) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = scheme.onSurfaceVariant,
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
