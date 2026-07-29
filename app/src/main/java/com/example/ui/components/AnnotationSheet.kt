package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AnnotationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val HIGHLIGHT_COLORS = listOf(
    "#FFE082" to "Yellow",
    "#A5D6A7" to "Green",
    "#80D8FF" to "Blue",
    "#F48FB1" to "Pink",
    "#FFAB91" to "Orange"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationSheet(
    annotations: List<AnnotationEntity>,
    currentPage: Int,
    onAddHighlight: (String, String) -> Unit, // text, color
    onAddNote: (String) -> Unit,
    onOpenSignatureModal: () -> Unit,
    onDeleteAnnotation: (AnnotationEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        var selectedColor by remember { mutableStateOf(HIGHLIGHT_COLORS[0].first) }
        var noteInputText by remember { mutableStateOf("") }
        var selectedTab by remember { mutableIntStateOf(0) } // 0 = Add New, 1 = View All

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
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
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Annotations",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Page $currentPage Annotations & Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_annotation_sheet")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Add Annotation") },
                    icon = { Icon(Icons.Default.AddComment, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("All Notes (${annotations.size})") },
                    icon = { Icon(Icons.Default.Notes, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                // ADD ANNOTATION PANEL
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Highlight Color",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Color Picker Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(HIGHLIGHT_COLORS) { (hex, name) ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = selectedColor == hex

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Sticky Note Text Field
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        label = { Text("Sticky Note / Annotation Text") },
                        placeholder = { Text("Type summary, memory note, or bookmark detail...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("annotation_text_input"),
                        minLines = 3,
                        maxLines = 5
                    )

                    // Action Buttons Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                onOpenSignatureModal()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).testTag("open_drawing_canvas_btn")
                        ) {
                            Icon(Icons.Default.Gesture, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Draw / Signature")
                        }

                        Button(
                            onClick = {
                                val text = noteInputText.ifBlank { "Page $currentPage Highlight" }
                                onAddHighlight(text, selectedColor)
                                noteInputText = ""
                                selectedTab = 1
                            },
                            modifier = Modifier.weight(1f).testTag("save_annotation_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save Note")
                        }
                    }
                }
            } else {
                // VIEW ALL ANNOTATIONS LIST
                if (annotations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No annotations saved yet.\nSwitch to 'Add Annotation' to add notes or drawings!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .padding(top = 8.dp)
                    ) {
                        items(annotations, key = { it.id }) { item ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(item.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primaryContainer
                            }

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Page ${item.pageNumber}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "• ${item.type}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (item.noteText.isNotEmpty()) {
                                            Text(
                                                text = item.noteText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(item.timestamp)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteAnnotation(item) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete annotation",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
