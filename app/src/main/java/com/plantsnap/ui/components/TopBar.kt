package com.plantsnap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
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

@Composable
fun TopBar (
    modifier: Modifier = Modifier,
) {
    val primary = colorResource(R.color.primary)
    val surface = colorResource(R.color.surface)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = surface.copy(alpha = 0.92f),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {}, // TODO: Open sidebar
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.LightGray),
                ) {
                    // TODO: Add hamburger button icon
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primary,
                    letterSpacing = (-0.5).sp,
                )
            }

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}