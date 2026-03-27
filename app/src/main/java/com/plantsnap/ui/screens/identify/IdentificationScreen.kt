package com.plantsnap.ui.screens.identify

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun IdentificationScreen(
    onBack: () -> Unit,
    onPlantSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .testTag("screen_identification")
    ){
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