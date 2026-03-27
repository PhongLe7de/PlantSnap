package com.plantsnap.ui.screens.identify

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun CameraScreen(onPhotoCaptured: () -> Unit) {
    Column(
        modifier = Modifier
            .testTag("screen_camera")
    ){
        Text(
            text = "Camera Screen"
        )
        Button(
            onClick = onPhotoCaptured,
            modifier = Modifier.testTag("btn_identify")
        ) {
            Text(text = "Capture Photo")
        }
    }
}