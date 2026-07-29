package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AnnotationEntity
import com.example.data.db.AppDatabase
import com.example.data.db.BookmarkEntity
import com.example.data.db.RecentPdfEntity
import com.example.data.preferences.PdfSettings
import com.example.data.preferences.ReadingFilterMode
import com.example.data.preferences.SettingsManager
import com.example.data.repository.PdfRepository
import com.example.pdf.PdfEngine
import com.example.pdf.PdfSpeechPlayer
import com.example.pdf.SamplePdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfViewModel(
    private val context: Context,
    private val repository: PdfRepository
) : ViewModel() {

    private val settingsManager = SettingsManager(context)

    private val _uiState = MutableStateFlow(PdfUiState(settings = settingsManager.getSettings(), filterMode = settingsManager.getSettings().filterMode))
    val uiState: StateFlow<PdfUiState> = _uiState.asStateFlow()

    private val ttsPlayer: PdfSpeechPlayer by lazy {
        PdfSpeechPlayer(context)
    }

    private var annotationJob: Job? = null

    val recentPdfs: StateFlow<List<RecentPdfEntity>> = repository.allRecents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var currentPdfEngine: PdfEngine? = null
        private set

    init {
        // Observe TTS state
        viewModelScope.launch {
            ttsPlayer.state.collect { tts ->
                _uiState.update { it.copy(ttsState = tts, isTtsSpeaking = tts.isPlaying) }
            }
        }

        // Load settings and ensure sample PDF is initialized
        viewModelScope.launch(Dispatchers.IO) {
            val sampleFile = SamplePdfGenerator.getOrCreateSamplePdf(context)
            val sampleUri = Uri.fromFile(sampleFile)
            val existing = repository.getRecentByUri(sampleUri.toString())
            if (existing == null) {
                repository.addOrUpdateRecent(
                    RecentPdfEntity(
                        uri = sampleUri.toString(),
                        title = "PDF Master Welcome Guide & Sample Document.pdf",
                        filePath = sampleFile.absolutePath,
                        totalPages = 6,
                        lastPage = 1,
                        fileSize = sampleFile.length(),
                        isSample = true,
                        tag = "General"
                    )
                )
            }
        }
    }

    fun openPdf(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingProgress = 10, errorMessage = null) }
            
            withContext(Dispatchers.IO) {
                try {
                    currentPdfEngine?.close()
                    
                    _uiState.update { it.copy(loadingProgress = 40) }

                    val engine = PdfEngine(context, uri, password)
                    currentPdfEngine = engine

                    _uiState.update { it.copy(loadingProgress = 80) }

                    val title = engine.metadata.fileName
                    val pageCount = engine.pageCount

                    // Check if previous position exists
                    val existingRecent = repository.getRecentByUri(uri.toString())
                    val lastSavedPage = existingRecent?.lastPage ?: 1
                    val hasPreviousReadingPosition = lastSavedPage > 1

                    // Save or update to recent files DB
                    val recentEntity = RecentPdfEntity(
                        uri = uri.toString(),
                        title = title,
                        filePath = uri.path ?: "",
                        totalPages = pageCount,
                        lastPage = lastSavedPage,
                        lastOpenedTimestamp = System.currentTimeMillis(),
                        fileSize = engine.metadata.fileSize,
                        isSample = uri.toString().contains("PDF_Master_Welcome_Guide.pdf"),
                        isFavorite = existingRecent?.isFavorite ?: false,
                        tag = existingRecent?.tag ?: "General",
                        totalReadingTimeMs = existingRecent?.totalReadingTimeMs ?: 0L
                    )
                    repository.addOrUpdateRecent(recentEntity)

                    // Observe annotations for this document
                    annotationJob?.cancel()
                    annotationJob = viewModelScope.launch {
                        repository.getAnnotationsForPdf(uri.toString()).collect { list ->
                            _uiState.update { it.copy(annotations = list) }
                        }
                    }

                    _uiState.update {
                        it.copy(
                            currentUri = uri,
                            currentTitle = title,
                            totalPages = pageCount,
                            currentPage = if (hasPreviousReadingPosition && it.settings.rememberPosition) 1 else 1,
                            previousReadPage = lastSavedPage,
                            showResumePrompt = hasPreviousReadingPosition && it.settings.rememberPosition,
                            metadata = engine.metadata,
                            tocItems = engine.tocItems,
                            isLoading = false,
                            loadingProgress = 100,
                            errorMessage = null,
                            showPasswordModal = false
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val msg = if (e.message?.contains("password", ignoreCase = true) == true) {
                        "This PDF is protected. Enter the password to continue."
                    } else {
                        "We could not open this PDF. Please choose another supported PDF document."
                    }
                    val isProtected = e.message?.contains("password", ignoreCase = true) == true

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = msg,
                            isPasswordProtected = isProtected,
                            showPasswordModal = isProtected
                        )
                    }
                }
            }
        }
    }

    fun confirmResumePage(page: Int) {
        setPage(page)
        _uiState.update { it.copy(showResumePrompt = false) }
    }

    fun dismissResumePrompt() {
        setPage(1)
        _uiState.update { it.copy(showResumePrompt = false) }
    }

    fun setPage(page: Int) {
        val target = page.coerceIn(1, _uiState.value.totalPages.coerceAtLeast(1))
        _uiState.update { it.copy(currentPage = target) }

        // Persist last page position to DB
        val currentUri = _uiState.value.currentUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getRecentByUri(currentUri.toString())
            if (existing != null) {
                repository.addOrUpdateRecent(
                    existing.copy(
                        lastPage = target,
                        lastOpenedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun nextPage() {
        setPage(_uiState.value.currentPage + 1)
    }

    fun previousPage() {
        setPage(_uiState.value.currentPage - 1)
    }

    fun setZoom(scale: Float) {
        val clamped = scale.coerceIn(0.5f, 4.0f)
        _uiState.update { it.copy(zoomScale = clamped) }
    }

    fun zoomIn() {
        setZoom(_uiState.value.zoomScale + 0.25f)
    }

    fun zoomOut() {
        setZoom(_uiState.value.zoomScale - 0.25f)
    }

    // --- Audio Reader (TTS) ---
    fun toggleTtsPlayer() {
        val nextShow = !_uiState.value.showTtsPlayer
        _uiState.update { it.copy(showTtsPlayer = nextShow) }
        if (!nextShow) {
            ttsPlayer.stop()
        }
    }

    fun startTtsSpeech() = toggleTtsPlayer()
    fun toggleTtsPlayPause() = playPauseTts()
    fun stopTtsSpeech() = stopTts()
    fun setTtsRate(rate: Float) = setTtsSpeechRate(rate)

    fun playPauseTts() {
        if (ttsPlayer.state.value.isPlaying) {
            ttsPlayer.pause()
        } else {
            speakCurrentPage()
        }
    }

    fun speakCurrentPage() {
        val engine = currentPdfEngine ?: return
        val pageIdx = _uiState.value.currentPage - 1
        val pageText = engine.getPageText(pageIdx)

        ttsPlayer.speak(pageText, pageIdx + 1) {
            // Auto advance to next page if available
            if (_uiState.value.currentPage < _uiState.value.totalPages) {
                nextPage()
                speakCurrentPage()
            }
        }
    }

    fun stopTts() {
        ttsPlayer.stop()
        _uiState.update { it.copy(showTtsPlayer = false) }
    }

    fun setTtsSpeechRate(rate: Float) {
        ttsPlayer.setSpeechRate(rate)
        val currentSettings = _uiState.value.settings
        updateSettings(currentSettings.copy(ttsSpeechRate = rate))
    }

    // --- Filter Modes ---
    fun setReadingFilterMode(filterMode: ReadingFilterMode) {
        _uiState.update { it.copy(filterMode = filterMode) }
        val newSettings = _uiState.value.settings.copy(filterMode = filterMode)
        settingsManager.saveSettings(newSettings)
    }

    fun setFilterMode(filterMode: ReadingFilterMode) = setReadingFilterMode(filterMode)

    // --- Page Rotations ---
    fun rotatePage(pageIndex: Int, degrees: Int = 90) {
        val nextDegrees = (_uiState.value.pageRotationDegrees + degrees) % 360
        _uiState.update { it.copy(pageRotationDegrees = nextDegrees) }
        currentPdfEngine?.setPageRotation(pageIndex, nextDegrees)
    }

    fun rotatePageClockwise() = rotatePage(_uiState.value.currentPage - 1, 90)

    // --- Annotations & Notes ---
    fun addAnnotation(
        pageNumber: Int,
        type: String = "HIGHLIGHT",
        textNote: String = "",
        colorHex: String = "#FFE082",
        contentData: String = ""
    ) {
        val uri = _uiState.value.currentUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAnnotation(
                AnnotationEntity(
                    pdfUri = uri.toString(),
                    pageNumber = pageNumber,
                    colorHex = colorHex,
                    type = type,
                    noteText = if (textNote.isBlank() && contentData.isNotBlank()) contentData else textNote,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteAnnotation(annotation: AnnotationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAnnotation(annotation)
        }
    }

    fun setShowAnnotationSheet(show: Boolean) {
        _uiState.update { it.copy(showAnnotationSheet = show) }
    }

    fun setShowDrawingCanvas(show: Boolean) {
        _uiState.update { it.copy(showDrawingCanvas = show) }
    }

    // --- Favorite & Tagging ---
    fun toggleFavorite(recent: RecentPdfEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(recent.uri, !recent.isFavorite)
        }
    }

    fun updatePdfTag(recent: RecentPdfEntity, newTag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTag(recent.uri, newTag)
        }
    }

    fun updateRecentTag(recent: RecentPdfEntity, tag: String) = updatePdfTag(recent, tag)

    fun setSelectedTagFilter(tag: String) {
        _uiState.update { it.copy(selectedTagFilter = tag) }
    }

    fun setTagFilter(tag: String) = setSelectedTagFilter(tag)

    fun performSearch(query: String, caseSensitive: Boolean = false, wholeWord: Boolean = false) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                searchCaseSensitive = caseSensitive,
                searchWholeWord = wholeWord,
                isSearchingText = true
            )
        }

        viewModelScope.launch {
            val engine = currentPdfEngine ?: return@launch
            val results = engine.searchText(query, caseSensitive, wholeWord)

            _uiState.update {
                it.copy(
                    searchResults = results,
                    currentSearchIndex = 0,
                    isSearchingText = false
                )
            }

            if (results.isNotEmpty()) {
                setPage(results[0].pageNumber)
            }
        }
    }

    fun nextSearchResult() {
        val state = _uiState.value
        if (state.searchResults.isEmpty()) return
        val nextIdx = (state.currentSearchIndex + 1) % state.searchResults.size
        _uiState.update { it.copy(currentSearchIndex = nextIdx) }
        setPage(state.searchResults[nextIdx].pageNumber)
    }

    fun previousSearchResult() {
        val state = _uiState.value
        if (state.searchResults.isEmpty()) return
        val prevIdx = if (state.currentSearchIndex - 1 < 0) state.searchResults.size - 1 else state.currentSearchIndex - 1
        _uiState.update { it.copy(currentSearchIndex = prevIdx) }
        setPage(state.searchResults[prevIdx].pageNumber)
    }

    fun addBookmark(note: String) {
        val uri = _uiState.value.currentUri ?: return
        val title = _uiState.value.currentTitle
        val page = _uiState.value.currentPage

        viewModelScope.launch(Dispatchers.IO) {
            repository.addBookmark(
                BookmarkEntity(
                    pdfUri = uri.toString(),
                    pdfTitle = title,
                    pageNumber = page,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBookmark(bookmark)
        }
    }

    fun removeRecent(recent: RecentPdfEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecent(recent)
        }
    }

    fun clearAllRecents() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllRecents()
        }
    }

    fun updateSettings(newSettings: PdfSettings) {
        settingsManager.saveSettings(newSettings)
        _uiState.update { it.copy(settings = newSettings, readingMode = newSettings.readingMode, pageFitMode = newSettings.pageFitMode, filterMode = newSettings.filterMode) }
    }

    fun setSearchOpen(open: Boolean) {
        _uiState.update { it.copy(isSearchOpen = open) }
    }

    fun setShowInfoModal(show: Boolean) {
        _uiState.update { it.copy(showInfoModal = show) }
    }

    fun setShowTocSheet(show: Boolean) {
        _uiState.update { it.copy(showTocSheet = show) }
    }

    fun setShowThumbnailSheet(show: Boolean) {
        _uiState.update { it.copy(showThumbnailSheet = show) }
    }

    fun setShowSettingsSheet(show: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = show) }
    }

    fun setShowBookmarkModal(show: Boolean) {
        _uiState.update { it.copy(showBookmarkModal = show) }
    }

    fun closeReader() {
        ttsPlayer.stop()
        currentPdfEngine?.close()
        currentPdfEngine = null
        annotationJob?.cancel()
        _uiState.update {
            it.copy(
                currentUri = null,
                currentTitle = "",
                totalPages = 0,
                currentPage = 1,
                metadata = null,
                tocItems = emptyList(),
                isSearchOpen = false,
                searchResults = emptyList(),
                showTtsPlayer = false,
                showAnnotationSheet = false,
                showDrawingCanvas = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsPlayer.destroy()
        currentPdfEngine?.close()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            val repo = PdfRepository(db.pdfDao())
            return PdfViewModel(context, repo) as T
        }
    }
}
