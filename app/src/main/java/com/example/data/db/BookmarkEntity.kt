package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pdfUri: String,
    val pdfTitle: String,
    val pageNumber: Int,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
