package com.plantsnap.ui.screens.identify

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CameraScreen(onPhotoCaptured: () -> Unit) {
    Column{
        Text(
            text = "Camera Screen"
        )
        Button(
            onClick = onPhotoCaptured
        ) {
            Text(text = "Capture Photo")
        }
    }
}