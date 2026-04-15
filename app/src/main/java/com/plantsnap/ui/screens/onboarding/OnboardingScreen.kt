package com.plantsnap.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsnap.ui.theme.PlantSnapTheme
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 4

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    OnboardingScreenContent(
        state = state,
        onSelectPets = viewModel::selectPets,
        onToggleInterest = viewModel::toggleInterest,
        onSelectExperience = viewModel::selectExperience,
        onFinished = {
            viewModel.completeOnboarding()
            onFinished()
        },
    )
}

@Composable
fun OnboardingScreenContent(
    state: OnboardingViewModel.State,
    onSelectPets: (PetType) -> Unit,
    onToggleInterest: (PlantInterest) -> Unit,
    onSelectExperience: (ExperienceLevel) -> Unit,
    onFinished: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    val isLastPage = pagerState.currentPage == PAGE_COUNT - 1

    fun navigateToPage(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Top bar: step counter + Skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${pagerState.currentPage + 1} of $PAGE_COUNT",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            if (!isLastPage) {
                TextButton(onClick = { navigateToPage(PAGE_COUNT - 1) }) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Spacer(Modifier.width(64.dp))
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) { page ->
            when (page) {
                0 -> PetSafetyPage(
                    selectedPets = state.selectedPets,
                    onSelectPets = onSelectPets,
                )

                1 -> PlantInterestsPage(
                    selectedInterests = state.selectedInterests,
                    onToggleInterest = onToggleInterest,
                )

                2 -> ExperienceLevelPage(
                    selectedExperience = state.selectedExperience,
                    onSelectExperience = onSelectExperience,
                )

                3 -> OnboardingCompletePage(
                    selectedPets = state.selectedPets,
                    selectedInterests = state.selectedInterests,
                    selectedExperience = state.selectedExperience,
                    onStartExploring = onFinished,
                )
            }
        }

        // Bottom bar: Back + dots + Continue
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Back button
            if (pagerState.currentPage > 0) {
                IconButton(onClick = { navigateToPage(pagerState.currentPage - 1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = scheme.onSurfaceVariant,
                    )
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }

            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(PAGE_COUNT) { index ->
                    val active = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (active) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) scheme.primary
                                else scheme.surfaceContainerHighest
                            ),
                    )
                }
            }

            // Continue button (hidden on last page — CTA is inside page 4)
            if (!isLastPage) {
                Button(
                    onClick = { navigateToPage(pagerState.currentPage + 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Continue",
                        tint = scheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Onboarding – Light")
@Composable
private fun OnboardingScreenPreview() {
    PlantSnapTheme {
        OnboardingScreenContent(
            state = OnboardingViewModel.State(),
            onSelectPets = {},
            onToggleInterest = {},
            onSelectExperience = {},
            onFinished = {},
        )
    }
}
