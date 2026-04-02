package com.plantsnap.ui.screens.identify.camera

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plantsnap.ui.theme.PlantSnapTheme
import kotlinx.coroutines.delay

@Composable
fun ShutterFlash(
    triggered: Boolean,
    onAnimationEnd: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(triggered) {
        if (!triggered) return@LaunchedEffect
        visible = true
        delay(80L)
        visible = false
        delay(200L)
        onAnimationEnd()
    }

    val  alpha by animateFloatAsState(
        targetValue = if (visible) 0.85f else 0f,
        animationSpec = tween(
            durationMillis = if (visible) 0 else 200
        ),
        label = "shutter_flash"
    )

    if (alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("shutter_flash")
                .alpha(alpha)
                .background(Color.White)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2C4F1D)
@Composable
fun ShutterFlashIdlePreview(){
    PlantSnapTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Camera content behind flash", color = Color.White)
            ShutterFlash(triggered = false, onAnimationEnd = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2C4F1D)
@Composable
fun ShutterFlashInteractivePreview(){
    PlantSnapTheme {
        var triggered by remember { mutableStateOf(false) }
        var flashCount by remember { mutableStateOf(0) }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Flash count: $flashCount",
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {triggered = true}) {
                    Text("Fire Shutter")
                }
            }

            ShutterFlash(
                triggered = triggered,
                onAnimationEnd = {
                    triggered = false
                    flashCount++
                }
            )
        }
    }
}