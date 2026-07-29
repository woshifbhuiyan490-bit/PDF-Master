package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.preferences.PageFitMode
import com.example.data.preferences.PdfSettings
import com.example.data.preferences.ReadingMode
import com.example.data.preferences.ThemeMode

@Composable
fun SettingsDialog(
    currentSettings: PdfSettings,
    onSaveSettings: (PdfSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var readingMode by remember { mutableStateOf(currentSettings.readingMode) }
    var pageFitMode by remember { mutableStateOf(currentSettings.pageFitMode) }
    var themeMode by remember { mutableStateOf(currentSettings.themeMode) }
    var rememberPosition by remember { mutableStateOf(currentSettings.rememberPosition) }
    var showPageShadows by remember { mutableStateOf(currentSettings.showPageShadows) }
    var enableAnimations by remember { mutableStateOf(currentSettings.enablePageAnimations) }
    var keepScreenAwake by remember { mutableStateOf(currentSettings.keepScreenAwake) }
    var defaultZoom by remember { mutableFloatStateOf(currentSettings.defaultZoom) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
        title = { Text("Reading & App Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag("settings_dialog_panel")
            ) {
                // Reading Mode
                Text("Reading Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
                    ReadingMode.entries.forEach { mode ->
                        FilterChip(
                            selected = readingMode == mode,
                            onClick = { readingMode = mode },
                            label = { Text(mode.name.replace("_", " ").lowercase().capitalize()) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Page Fit Mode
                Text("Page Display Fit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
                    PageFitMode.entries.forEach { fit ->
                        FilterChip(
                            selected = pageFitMode == fit,
                            onClick = { pageFitMode = fit },
                            label = { Text(fit.name.replace("_", " ").lowercase().capitalize()) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Theme Mode
                Text("App Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
                    ThemeMode.entries.forEach { theme ->
                        FilterChip(
                            selected = themeMode == theme,
                            onClick = { themeMode = theme },
                            label = { Text(theme.name.lowercase().capitalize()) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Default Zoom
                Text("Default Zoom Level: ${(defaultZoom * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Slider(
                    value = defaultZoom,
                    onValueChange = { defaultZoom = it },
                    valueRange = 0.75f..2.0f,
                    steps = 4
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Toggles
                SettingToggle(
                    title = "Remember Reading Position",
                    subtitle = "Automatically prompt to resume last page when reopening PDFs",
                    checked = rememberPosition,
                    onCheckedChange = { rememberPosition = it }
                )

                SettingToggle(
                    title = "Show Page Shadows",
                    subtitle = "Render realistic document card elevation shadows",
                    checked = showPageShadows,
                    onCheckedChange = { showPageShadows = it }
                )

                SettingToggle(
                    title = "Keep Screen Awake",
                    subtitle = "Prevent display sleep while actively reading PDF files",
                    checked = keepScreenAwake,
                    onCheckedChange = { keepScreenAwake = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updated = PdfSettings(
                        readingMode = readingMode,
                        pageFitMode = pageFitMode,
                        themeMode = themeMode,
                        rememberPosition = rememberPosition,
                        showPageShadows = showPageShadows,
                        enablePageAnimations = enableAnimations,
                        keepScreenAwake = keepScreenAwake,
                        defaultZoom = defaultZoom
                    )
                    onSaveSettings(updated)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
