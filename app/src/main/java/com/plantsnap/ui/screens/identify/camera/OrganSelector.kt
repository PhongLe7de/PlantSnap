package com.plantsnap.ui.screens.identify.camera

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plantsnap.ui.theme.PlantSnapTheme

// Icon is null as a placeholder for now
enum class Organ(val label: String, val icon: ImageVector? = null) {
    AUTO("Auto"),
    LEAF("Leaf"),
    FLOWER("Flower"),
    FRUIT("Fruit"),
    BARK("Bark"),
    BRANCH("Branch"),
}

@Composable
fun OrganSelector(
    selected: Organ,
    onSelected: (Organ) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Organ.entries.forEach { organ ->
            OrganButton(
                organ = organ,
                isSelected = organ == selected,
                onClick = { onSelected(organ) },
            )
        }
    }
}

@Composable
private fun OrganButton(
    organ: Organ,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val containerColor = if (isSelected) {
        scheme.primaryContainer
    } else {
        Color.White.copy(alpha = 0.15f)
    }
    val contentColor = if (isSelected) {
        scheme.onPrimaryContainer
    } else {
        Color.White
    }

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 14.dp,
            vertical = 8.dp,
        ),
    ) {
        if (organ.icon != null) {
            Icon(
                imageVector = organ.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Spacer(modifier = Modifier.size(16.dp)) // Placeholder, same size as a 16dp icon
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = organ.label,
            style = MaterialTheme.typography.labelMedium,
        )
    }

}

@Preview(showBackground = false, name = "OrganSelector — Light mode")
@Composable
private fun OrganSelectorPreviewLight() {
    PlantSnapTheme {
        OrganSelector(
            selected = Organ.AUTO,
            onSelected = {},
        )
    }
}

@Preview(showBackground = false, name = "OrganSelector - Dark Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OrganSelectorPreviewDark() {
    PlantSnapTheme(darkTheme = true) {
        OrganSelector(
            selected = Organ.AUTO,
            onSelected = {},
        )
    }
}