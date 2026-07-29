package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_pdfs")
data class RecentPdfEntity(
    @PrimaryKey val uri: String,
    val title: String,
    val filePath: String = "",
    val totalPages: Int = 1,
    val lastPage: Int = 1,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val fileSize: Long = 0L,
    val isSample: Boolean = false,
    val isFavorite: Boolean = false,
    val tag: String = "General",
    val totalReadingTimeMs: Long = 0L
)
