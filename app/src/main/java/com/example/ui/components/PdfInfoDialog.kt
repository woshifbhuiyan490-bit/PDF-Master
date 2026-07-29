package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pdf.PdfMetadata
import java.text.DecimalFormat

@Composable
fun PdfInfoDialog(
    metadata: PdfMetadata?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Info, contentDescription = "PDF Information")
        },
        title = {
            Text(text = "Document Properties")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (metadata != null) {
                    InfoRow(label = "File Name", value = metadata.fileName)
                    InfoRow(label = "File Size", value = formatFileSize(metadata.fileSize))
                    InfoRow(label = "Total Pages", value = "${metadata.pageCount} pages")
                    InfoRow(label = "Title", value = metadata.title.ifEmpty { "Information not available." })
                    InfoRow(label = "Author", value = metadata.author.ifEmpty { "Information not available." })
                    InfoRow(label = "Subject", value = metadata.subject.ifEmpty { "Information not available." })
                    InfoRow(label = "Creator Engine", value = metadata.creator.ifEmpty { "Information not available." })
                    InfoRow(label = "Creation Date", value = metadata.creationDate.ifEmpty { "Information not available." })
                    InfoRow(label = "Page Size", value = "${metadata.width} x ${metadata.height} pt")
                } else {
                    Text(
                        text = "Information not available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_info_dialog")
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(0.4f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(0.6f)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val df = DecimalFormat("#.##")
    return if (mb >= 1) {
        "${df.format(mb)} MB"
    } else {
        "${df.format(kb)} KB"
    }
}
