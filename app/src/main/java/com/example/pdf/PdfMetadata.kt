package com.example.pdf

data class PdfMetadata(
    val fileName: String,
    val fileSize: Long,
    val pageCount: Int,
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val creator: String = "",
    val creationDate: String = "",
    val modDate: String = "",
    val isEncrypted: Boolean = false,
    val width: Int = 0,
    val height: Int = 0
)

data class PdfSearchResult(
    val pageNumber: Int,
    val snippet: String,
    val matchIndex: Int,
    val charOffset: Int = 0
)

data class TocItem(
    val id: String,
    val title: String,
    val pageNumber: Int,
    val level: Int = 0,
    val children: List<TocItem> = emptyList()
)
