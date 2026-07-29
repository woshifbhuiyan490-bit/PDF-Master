package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class LinePath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float
)

val PEN_COLORS = listOf(
    Color.Black,
    Color.Blue,
    Color.Red,
    Color(0xFF2E7D32),
    Color(0xFFED6C02)
)

@Composable
fun DrawingCanvasModal(
    onSavePath: (String, String) -> Unit, // pathData, colorHex
    onDismiss: () -> Unit
) {
    var paths by remember { mutableStateOf(listOf<LinePath>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var selectedColor by remember { mutableStateOf(PEN_COLORS[0]) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gesture,
                            contentDescription = "Signature / Drawing",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Draw Signature / Freehand Markup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { paths = emptyList() },
                            modifier = Modifier.testTag("clear_drawing_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Drawing Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .pointerInput(selectedColor, strokeWidth) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPath = newPath
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                    // Trigger recomposition
                                    currentPath = currentPath
                                },
                                onDragEnd = {
                                    currentPath?.let {
                                        paths = paths + LinePath(it, selectedColor, strokeWidth)
                                    }
                                    currentPath = null
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        paths.forEach { line ->
                            drawPath(
                                path = line.path,
                                color = line.color,
                                style = Stroke(width = line.strokeWidth)
                            )
                        }
                        currentPath?.let { p ->
                            drawPath(
                                path = p,
                                color = selectedColor,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }

                    if (paths.isEmpty() && currentPath == null) {
                        Text(
                            text = "Draw your signature or document markup here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Palette & Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Color Pickers
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PEN_COLORS.forEach { color ->
                            val isSelected = selectedColor == color
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }

                    // Stroke width picker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Thickness:", style = MaterialTheme.typography.labelMedium)
                        FilterChip(
                            selected = strokeWidth == 4f,
                            onClick = { strokeWidth = 4f },
                            label = { Text("Thin") }
                        )
                        FilterChip(
                            selected = strokeWidth == 8f,
                            onClick = { strokeWidth = 8f },
                            label = { Text("Thick") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Button
                Button(
                    onClick = {
                        val colorHex = String.format("#%06X", 0xFFFFFF and selectedColor.value.toLong().toInt())
                        onSavePath("Freehand Drawing Path (${paths.size} strokes)", colorHex)
                        onDismiss()
                    },
                    enabled = paths.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_signature_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Attach Signature / Drawing")
                }
            }
        }
    }
}
