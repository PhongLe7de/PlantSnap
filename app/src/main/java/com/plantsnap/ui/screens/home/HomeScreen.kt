package com.plantsnap.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plantsnap.R


private data class AppColors(
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val surface: Color,
    val surfaceContainerLow: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
)

@Composable
private fun rememberAppColors() = AppColors(
    primary              = colorResource(R.color.primary),
    primaryContainer     = colorResource(R.color.primary_container),
    secondary            = colorResource(R.color.secondary),
    secondaryContainer   = colorResource(R.color.secondary_container),
    onSecondaryContainer = colorResource(R.color.on_secondary_container),
    tertiary             = colorResource(R.color.tertiary),
    surface              = colorResource(R.color.surface),
    surfaceContainerLow  = colorResource(R.color.surface_container_low),
    onSurface            = colorResource(R.color.on_surface),
    onSurfaceVariant     = colorResource(R.color.on_surface_variant),
    outline              = colorResource(R.color.outline),
)

@Composable
fun HomeScreen() {
    val colors = rememberAppColors()

    Scaffold(
        containerColor = colors.surface,
        topBar = { TopBar(colors) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item { WelcomeSection(colors) }
            item { Spacer(Modifier.height(20.dp)) }
            item { HeroCard(colors) }
            item { Spacer(Modifier.height(20.dp)) }
            item { RecentScansSection(colors) }
            item { Spacer(Modifier.height(20.dp)) }
            item { PlantOfTheDaySection(colors) }
            item { Spacer(Modifier.height(20.dp)) }
            item { DailyCareSection(colors) }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun TopBar(colors: AppColors) {
    Surface(
        color = colors.surface.copy(alpha = 0.92f),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.primary,
                letterSpacing = (-0.5).sp,
            )
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerLow)
            )
        }
    }
}

@Composable
private fun WelcomeSection(colors: AppColors) {

}

@Composable
private fun HeroCard(colors: AppColors) {

}

@Composable
private fun RecentScansSection(colors: AppColors) {

}

@Composable
private fun PlantOfTheDaySection(colors: AppColors) {

}

@Composable
private fun DailyCareSection(colors: AppColors) {

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}