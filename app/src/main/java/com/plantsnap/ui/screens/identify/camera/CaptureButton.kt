package com.plantsnap.ui.screens.identify.camera

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.plantsnap.ui.theme.PlantSnapTheme


@Composable
fun CaptureButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = 600f,
        ),
        label = "shutter_scale"
    )

    val glowBlur by animateFloatAsState(
        targetValue = if (isPressed) 6f else 16f,
        animationSpec = spring(stiffness = 400f),
        label = "shutter_glow_blur"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .semantics {if (!enabled) disabled()}
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .background(
                    color = colorScheme.secondaryContainer.copy(alpha = 0.30f),
                    shape = CircleShape,
                )
                .blur(glowBlur.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(scale)
                .size(96.dp)
                .alpha(if (enabled) 1f else 0.4f)
                .border(
                    width = 4.dp,
                    color = Color.White,
                    shape = CircleShape,
                )
                .padding(6.dp)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        },
                    )
                },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2C4F1D),
                                Color(0xFF436833),
                            ),
                        ),
                        shape = CircleShape,
                    ),
            ){
                Icon(
                    imageVector = Icons.Filled.FilterCenterFocus,
                    contentDescription = "Capture photo",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF173809)
@Composable
fun CaptureButtonPreview(){
    Surface(color = Color(0xFF173809)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            CaptureButton(
                onClick = {},
                enabled = true,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF173809)
@Composable
fun CaptureButtonDisabledPreview() {
    PlantSnapTheme {
        Surface(color = Color(0xFF173809)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ){
                CaptureButton(
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }
}
@Preview(showBackground = true, backgroundColor = 0xFF2C4F1D)
@Composable
fun CaptureButtonInteractivePreview() {
    PlantSnapTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            CaptureButton(
                onClick = {},
                enabled = true,
            )
        }
    }
}