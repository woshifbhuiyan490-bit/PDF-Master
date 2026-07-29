package com.example.ui.viewmodel

import android.net.Uri
import com.example.data.db.AnnotationEntity
import com.example.data.db.BookmarkEntity
import com.example.data.db.RecentPdfEntity
import com.example.data.preferences.PageFitMode
import com.example.data.preferences.PdfSettings
import com.example.data.preferences.ReadingFilterMode
import com.example.data.preferences.ReadingMode
import com.example.data.preferences.ThemeMode
import com.example.pdf.PdfMetadata
import com.example.pdf.PdfSearchResult
import com.example.pdf.TocItem
import com.example.pdf.TtsState

data class PdfUiState(
    val currentUri: Uri? = null,
    val currentTitle: String = "",
    val totalPages: Int = 0,
    val currentPage: Int = 1,
    val zoomScale: Float = 1.0f,
    val readingMode: ReadingMode = ReadingMode.CONTINUOUS,
    val pageFitMode: PageFitMode = PageFitMode.FIT_WIDTH,
    val filterMode: ReadingFilterMode = ReadingFilterMode.NORMAL,
    val metadata: PdfMetadata? = null,
    val tocItems: List<TocItem> = emptyList(),
    
    // Annotations & Highlights
    val annotations: List<AnnotationEntity> = emptyList(),
    val showAnnotationSheet: Boolean = false,
    val showDrawingCanvas: Boolean = false,

    // Audio Reader (Text to Speech)
    val ttsState: TtsState = TtsState(),
    val showTtsPlayer: Boolean = false,
    val isTtsSpeaking: Boolean = false,
    val pageRotationDegrees: Int = 0,

    // Search
    val searchQuery: String = "",
    val searchResults: List<PdfSearchResult> = emptyList(),
    val currentSearchIndex: Int = 0,
    val isSearchOpen: Boolean = false,
    val isSearchingText: Boolean = false,
    val searchCaseSensitive: Boolean = false,
    val searchWholeWord: Boolean = false,

    // Home Screen Filtering & Tags
    val selectedTagFilter: String = "All",

    // Loading & Dialogs
    val isLoading: Boolean = false,
    val loadingProgress: Int = 0,
    val errorMessage: String? = null,
    val isPasswordProtected: Boolean = false,
    val showPasswordModal: Boolean = false,
    val showInfoModal: Boolean = false,
    val showTocSheet: Boolean = false,
    val showThumbnailSheet: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val showBookmarkModal: Boolean = false,
    val showResumePrompt: Boolean = false,
    val previousReadPage: Int = 1,

    // App Preferences
    val settings: PdfSettings = PdfSettings()
)
