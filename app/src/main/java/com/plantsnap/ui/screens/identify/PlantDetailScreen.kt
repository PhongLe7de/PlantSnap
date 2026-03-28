package com.plantsnap.ui.screens.identify

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun PlantDetailScreen(
    plantId: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .testTag("screen_plantDetail")
    ) {
        Text(
            text = "Plant ID: $plantId"
        )
        Button(
            onClick = onBack
        ) {
            Text(text = "Back to Identification")
        }
    }
}