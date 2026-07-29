package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pdfUri: String,
    val pageNumber: Int,
    val colorHex: String = "#FFFF00", // Yellow, Green, Blue, Pink, Orange
    val type: String = "HIGHLIGHT", // "HIGHLIGHT", "NOTE", "DRAWING"
    val noteText: String = "",
    val pathData: String = "",
    val rectX: Float = 0f,
    val rectY: Float = 0f,
    val rectW: Float = 0f,
    val rectH: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
