package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

val PDF_TAG_CATEGORIES = listOf(
    "All" to null,
    "Favorites" to Icons.Default.Star,
    "Work" to Icons.Default.Bookmark,
    "Study" to Icons.Default.Bookmark,
    "Personal" to Icons.Default.Bookmark,
    "Financial" to Icons.Default.Bookmark,
    "Books" to Icons.Default.Bookmark
)

@Composable
fun TagFilterBar(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(PDF_TAG_CATEGORIES) { (name, icon) ->
            val isSelected = selectedFilter == name
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(name) },
                label = { Text(name) },
                leadingIcon = if (icon != null) {
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = name,
                            modifier = Modifier.testTag("tag_chip_$name")
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("filter_chip_$name")
            )
        }
    }
}
