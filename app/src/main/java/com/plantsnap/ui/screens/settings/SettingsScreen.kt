package com.plantsnap.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plantsnap.domain.models.AppTheme
import com.plantsnap.domain.models.TemperatureUnit
import com.plantsnap.domain.models.UserSettings
import com.plantsnap.ui.theme.PlantSnapTheme
import com.plantsnap.ui.components.TopBar



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onBack: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onPlantCareRemindersChange: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var showThemePicker by remember { mutableStateOf(false) }
    var showUnitPicker by remember { mutableStateOf(false)}
    var showLanguagePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(profilePhotoUrl = null)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.primary
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = (8.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSection(title = "Appearance") {
                SettingsRow(
                    icon = Icons.Filled.Palette,
                    label = "Theme",
                    value = settings.theme.displayName,
                    onClick = { showThemePicker = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.Thermostat,
                    label = "Temperature",
                    value = settings.temperatureUnit.displayName,
                    onClick = { showUnitPicker = true},
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Filled.Language,
                    label = "Language",
                    value = languageDisplayName(settings.language),
                    onClick = { showLanguagePicker = true},
                )
            }

            Spacer(Modifier.height(8.dp))

            SettingsSection(title = "Plant Care Reminders") {
                SettingsToggleRow(
                    icon = Icons.Filled.Notifications,
                    label = "Push Notifications",
                    subtitle = "Receive alerts about your plants",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = onNotificationsChange,
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Filled.WaterDrop,
                    label = "Care Reminders",
                    subtitle = "Watering and care schedule alerts",
                    checked = settings.plantCareReminders,
                    enabled = settings.notificationsEnabled,
                    onCheckedChange = onPlantCareRemindersChange,
                )
            }
        }
    }

    if (showThemePicker) {
        PickerBottomSheet(
            title = "Theme",
            options = AppTheme.entries.map { it to it.displayName },
            selected = settings.theme,
            onSelect = { onThemeChange(it); showThemePicker = false },
            onDismiss = { showThemePicker = false}
        )
    }

    if (showUnitPicker) {
        PickerBottomSheet(
            title = "Temperature Unit",
            options = TemperatureUnit.entries.map { it to it.displayName },
            selected = settings.temperatureUnit,
            onSelect = { onTemperatureUnitChange(it); showUnitPicker = false },
            onDismiss = { showUnitPicker = false}
        )
    }

    if (showLanguagePicker) {
        PickerBottomSheet(
            title = "Language",
            options = SUPPORTED_LANGUAGES.entries.map { it.key to it.value },
            selected = settings.language,
            onSelect = { onLanguageChange(it); showLanguagePicker = false },
            onDismiss = { showLanguagePicker = false}
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurfaceVariant,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)

        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(scheme.surfaceContainerLow)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
){
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
){
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) scheme.onSurfaceVariant else scheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) scheme.onSurface else scheme.onSurface.copy(alpha = 0.4f),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) scheme.onSurfaceVariant else scheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = scheme.onPrimary,
                checkedTrackColor = scheme.primary,
            ),
        )
    }
}

@Composable
private fun SettingsDivider() {
    val scheme = MaterialTheme.colorScheme
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = scheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> PickerBottomSheet(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentColor = scheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            )
            options.forEach { (value, displayName) ->
                val isSelected = value == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable{ onSelect(value) }
                        .background(
                            if (isSelected) scheme.primaryContainer
                            else androidx.compose.ui.graphics.Color.Transparent
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight =  if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color =  if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

}

private val SUPPORTED_LANGUAGES = linkedMapOf(
    "en" to "English",
    "fi" to "Suomi",
    "sv" to "Svenska",
    "de" to "Deutsch",
    "fr" to "Français",
    "es" to "Español",
    "pt" to "Português",
)

private fun languageDisplayName(code: String) : String =
    SUPPORTED_LANGUAGES[code] ?: code

@Preview(showBackground = true, showSystemUi = true, name = "Settings - Light")
@Composable
private fun SettingsScreenPreview() {
    PlantSnapTheme {
        SettingsScreen(
            settings = UserSettings(),
            onBack = {},
            onThemeChange = {},
            onTemperatureUnitChange = {},
            onLanguageChange = {},
            onNotificationsChange = {},
            onPlantCareRemindersChange = {}
        )
    }
}