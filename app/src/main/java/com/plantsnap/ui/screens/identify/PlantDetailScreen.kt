package com.plantsnap.ui.screens.identify

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PlantDetailScreen(
    plantId: String,
    onBack: () -> Unit
) {
    Column {
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