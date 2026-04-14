package com.plantsnap.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plantsnap.R

enum class ExperienceLevel(
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
    @param:StringRes val descriptionRes: Int,
) {
    BEGINNER(Icons.Filled.Eco, R.string.onboarding_experience_option_beginner_label, R.string.onboarding_experience_option_beginner_desc),
    INTERMEDIATE(Icons.Filled.Psychology, R.string.onboarding_experience_option_intermediate_label, R.string.onboarding_experience_option_intermediate_desc),
    EXPERT(Icons.Filled.Park, R.string.onboarding_experience_option_expert_label, R.string.onboarding_experience_option_expert_desc),
}

@Composable
fun ExperienceLevelPage(
    selectedExperience: ExperienceLevel?,
    onSelectExperience: (ExperienceLevel) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Hero image with gradient + overlapping title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .offset(y = (-16).dp),
        ) {
            Image(
                painter = painterResource(R.drawable.potted_plant),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .align(Alignment.TopCenter),
            )

            // Gradient fade from transparent → surface at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, scheme.surface),
                        )
                    ),
            )

            // Title overlapping on top of the gradient
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_experience_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = stringResource(R.string.onboarding_experience_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
                lineHeight = 24.sp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExperienceLevel.entries.forEach { level ->
                    ExperienceCard(
                        level = level,
                        selected = selectedExperience == level,
                        onClick = { onSelectExperience(level) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExperienceCard(
    level: ExperienceLevel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val cardBg = if (selected) scheme.surfaceContainerHighest else scheme.surfaceContainerLow

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
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
                    .background(if (selected) scheme.primary else cardBg),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) scheme.primary.copy(alpha = 0.12f)
                            else scheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = level.icon,
                        contentDescription = null,
                        tint = if (selected) scheme.primary else scheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(level.labelRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) scheme.primary else scheme.onSurface,
                    )
                    Text(
                        text = stringResource(level.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}
