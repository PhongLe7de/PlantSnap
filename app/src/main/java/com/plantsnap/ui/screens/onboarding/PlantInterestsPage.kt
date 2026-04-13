package com.plantsnap.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plantsnap.R

enum class PlantInterest(val icon: ImageVector, @param:StringRes val labelRes: Int) {
    INDOOR(Icons.Filled.LocalFlorist,R.string.onboarding_interests_option_indoor),
    OUTDOOR(Icons.Outlined.WbSunny, R.string.onboarding_interests_option_outdoor),
    EDIBLE(Icons.Outlined.Spa,R.string.onboarding_interests_option_edible),
    AIR_PURIFIERS(Icons.Outlined.Air,R.string.onboarding_interests_option_air_purifiers),
    SUCCULENTS(Icons.Filled.Grass,R.string.onboarding_interests_option_succulents),
    FLOWERS(Icons.Filled.LocalFlorist,R.string.onboarding_interests_option_flowers),
    TREES(Icons.Outlined.Forest,R.string.onboarding_interests_option_trees),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlantInterestsPage(
    selectedInterests: Set<PlantInterest>,
    onToggleInterest: (PlantInterest) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        // Hero image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(scheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFlorist,
                contentDescription = null,
                tint = scheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.height(72.dp),
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_interests_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_interests_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurfaceVariant,
            lineHeight = 24.sp,
        )

        Spacer(Modifier.height(28.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlantInterest.entries.forEach { interest ->
                val selected = interest in selectedInterests
                FilterChip(
                    selected = selected,
                    onClick = { onToggleInterest(interest) },
                    label = { Text(stringResource(interest.labelRes)) },
                    leadingIcon = {
                        Icon(
                            imageVector = interest.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }
    }
}
