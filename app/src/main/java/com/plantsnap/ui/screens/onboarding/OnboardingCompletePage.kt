package com.plantsnap.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plantsnap.R
import com.plantsnap.ui.components.OnBoardingHeroSection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingCompletePage(
    selectedPets: PetType?,
    selectedInterests: Set<PlantInterest>,
    selectedExperience: ExperienceLevel?,
    onStartExploring: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val hasSelections =
        selectedPets != null || selectedInterests.isNotEmpty() || selectedExperience != null

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnBoardingHeroSection(
            imageRes = R.drawable.onboarding_done,
            titleRes = R.string.onboarding_complete_title,
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = stringResource(R.string.onboarding_complete_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
            )

            if (hasSelections) {
                Spacer(Modifier.height(24.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selectedPets?.let { pet ->
                        val painter = pet.imageVector?.let { rememberVectorPainter(it) }
                            ?: painterResource(pet.drawableRes!!)
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text(stringResource(pet.labelRes)) },
                            leadingIcon = {
                                Icon(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                    }
                    selectedInterests.forEach { interest ->
                        FilterChip(
                            selected = true,
                            onClick = {},
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
                    selectedExperience?.let { exp ->
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text(stringResource(exp.labelRes)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = exp.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onStartExploring,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_complete_cta),
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimary,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                Spacer(Modifier.padding(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
