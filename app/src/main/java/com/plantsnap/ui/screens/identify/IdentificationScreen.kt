package com.plantsnap.ui.screens.identify

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun IdentificationScreen(
    onBack: () -> Unit,
    onPlantSelected: (String) -> Unit
) {
    Column{
        Text(
            text = "Identification Screen"
        )
        Button(
            onClick = { onPlantSelected("Example Plant") }
        ) {
            Text(text = "Select Plant")
        }
        Button(
            onClick = onBack
        ) {
            Text(text = "Back to Camera")
        }
    }
}