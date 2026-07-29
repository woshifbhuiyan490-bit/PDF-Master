package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.BookmarkEntity
import com.example.data.preferences.ReadingFilterMode
import com.example.data.preferences.ReadingMode
import com.example.pdf.PdfEngine
import com.example.ui.components.AnnotationSheet
import com.example.ui.components.BookmarkSheet
import com.example.ui.components.DrawingCanvasModal
import com.example.ui.components.PasswordDialog
import com.example.ui.components.PdfInfoDialog
import com.example.ui.components.SearchDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SpeechPlayerBar
import com.example.ui.components.ThumbnailSheet
import com.example.ui.components.TocDrawer
import com.example.ui.viewmodel.PdfUiState
import com.example.ui.viewmodel.PdfViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    viewModel: PdfViewModel,
    uiState: PdfUiState,
    bookmarks: List<BookmarkEntity>,
    onBack: () -> Unit
) {
    val pdfEngine = viewModel.currentPdfEngine
    val totalPages = uiState.totalPages
    val currentPage = uiState.currentPage
    val zoomScale = uiState.zoomScale

    var showJumpPageDialog by remember { mutableStateOf(false) }
    var showZoomMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Resume reading location prompt dialog
    if (uiState.showResumePrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResumePrompt() },
            title = { Text("Resume Reading") },
            text = { Text("Continue reading from page ${uiState.previousReadPage}?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmResumePage(uiState.previousReadPage) },
                    modifier = Modifier.testTag("confirm_resume_page_button")
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResumePrompt() }) {
                    Text("Start from page 1")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Password Dialog
    if (uiState.showPasswordModal) {
        PasswordDialog(
            onConfirm = { password ->
                uiState.currentUri?.let { uri ->
                    viewModel.openPdf(uri, password)
                }
            },
            onDismiss = onBack
        )
    }

    // Document Info Dialog
    if (uiState.showInfoModal) {
        PdfInfoDialog(
            metadata = uiState.metadata,
            onDismiss = { viewModel.setShowInfoModal(false) }
        )
    }

    // TOC Sheet
    if (uiState.showTocSheet) {
        TocDrawer(
            tocItems = uiState.tocItems,
            currentPage = currentPage,
            onSelectPage = { page -> viewModel.setPage(page) },
            onDismiss = { viewModel.setShowTocSheet(false) }
        )
    }

    // Thumbnail Browser Sheet
    if (uiState.showThumbnailSheet) {
        ThumbnailSheet(
            pdfEngine = pdfEngine,
            totalPages = totalPages,
            currentPage = currentPage,
            onSelectPage = { page -> viewModel.setPage(page) },
            onDismiss = { viewModel.setShowThumbnailSheet(false) }
        )
    }

    // Bookmarks Sheet
    if (uiState.showBookmarkModal) {
        BookmarkSheet(
            bookmarks = bookmarks.filter { it.pdfUri == uiState.currentUri.toString() },
            currentPage = currentPage,
            onSelectBookmark = { page -> viewModel.setPage(page) },
            onAddBookmark = { note -> viewModel.addBookmark(note) },
            onDeleteBookmark = { bookmark -> viewModel.deleteBookmark(bookmark) },
            onDismiss = { viewModel.setShowBookmarkModal(false) }
        )
    }

    // Annotations Sheet
    if (uiState.showAnnotationSheet) {
        AnnotationSheet(
            annotations = uiState.annotations.filter { it.pageNumber == currentPage },
            currentPage = currentPage,
            onAddHighlight = { text, color -> viewModel.addAnnotation(currentPage, "HIGHLIGHT", text, colorHex = color) },
            onAddNote = { note -> viewModel.addAnnotation(currentPage, "NOTE", note) },
            onOpenSignatureModal = { viewModel.setShowDrawingCanvas(true) },
            onDeleteAnnotation = { annotation -> viewModel.deleteAnnotation(annotation) },
            onDismiss = { viewModel.setShowAnnotationSheet(false) }
        )
    }

    // Signature / Freehand Drawing Canvas Modal
    if (uiState.showDrawingCanvas) {
        DrawingCanvasModal(
            onSavePath = { pathData, colorHex ->
                viewModel.addAnnotation(
                    pageNumber = currentPage,
                    type = "DRAWING",
                    textNote = "Freehand Drawing",
                    contentData = pathData,
                    colorHex = colorHex
                )
            },
            onDismiss = { viewModel.setShowDrawingCanvas(false) }
        )
    }

    // Settings Dialog
    if (uiState.showSettingsSheet) {
        SettingsDialog(
            currentSettings = uiState.settings,
            onSaveSettings = { newSettings -> viewModel.updateSettings(newSettings) },
            onDismiss = { viewModel.setShowSettingsSheet(false) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.currentTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Text to Speech Voice Reader Button
                    IconButton(
                        onClick = { viewModel.startTtsSpeech() },
                        modifier = Modifier.testTag("tts_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read Aloud (TTS)",
                            tint = if (uiState.isTtsSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Annotation / Notes Button
                    IconButton(
                        onClick = { viewModel.setShowAnnotationSheet(true) },
                        modifier = Modifier.testTag("annotations_sheet_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Annotations & Notes")
                    }

                    // Reading Filter Color Tint Button
                    Box {
                        IconButton(
                            onClick = { showFilterMenu = true },
                            modifier = Modifier.testTag("filter_mode_button")
                        ) {
                            Icon(Icons.Default.ColorLens, contentDescription = "Reading Filter Mode")
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            ReadingFilterMode.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.name.lowercase().replace('_', ' ')) },
                                    onClick = {
                                        viewModel.setFilterMode(filter)
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Rotate Page Button
                    IconButton(
                        onClick = { viewModel.rotatePageClockwise() },
                        modifier = Modifier.testTag("rotate_page_button")
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate Clockwise")
                    }

                    IconButton(
                        onClick = { viewModel.setSearchOpen(!uiState.isSearchOpen) },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search PDF")
                    }

                    val isBookmarked = bookmarks.any { it.pdfUri == uiState.currentUri.toString() && it.pageNumber == currentPage }
                    IconButton(
                        onClick = { viewModel.setShowBookmarkModal(true) },
                        modifier = Modifier.testTag("bookmark_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmarks",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setShowTocSheet(true) },
                        modifier = Modifier.testTag("toc_button")
                    ) {
                        Icon(Icons.Default.ListAlt, contentDescription = "Table of Contents")
                    }

                    IconButton(
                        onClick = { viewModel.setShowThumbnailSheet(true) },
                        modifier = Modifier.testTag("thumbnail_grid_button")
                    ) {
                        Icon(Icons.Default.GridView, contentDescription = "Thumbnails")
                    }

                    IconButton(
                        onClick = { viewModel.setShowInfoModal(true) },
                        modifier = Modifier.testTag("pdf_info_button")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "PDF Info")
                    }

                    IconButton(
                        onClick = { viewModel.setShowSettingsSheet(true) },
                        modifier = Modifier.testTag("pdf_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column {
                // Text To Speech Floating Controls Bar
                if (uiState.showTtsPlayer) {
                    SpeechPlayerBar(
                        isSpeaking = uiState.isTtsSpeaking,
                        speechRate = uiState.settings.ttsSpeechRate,
                        onPlayPause = { viewModel.toggleTtsPlayPause() },
                        onStop = { viewModel.stopTtsSpeech() },
                        onChangeRate = { newRate -> viewModel.setTtsRate(newRate) }
                    )
                }

                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Page Nav Group
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.previousPage() },
                                enabled = currentPage > 1,
                                modifier = Modifier.testTag("prev_page_button")
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page")
                            }

                            TextButton(
                                onClick = { showJumpPageDialog = true },
                                modifier = Modifier.testTag("page_counter_button")
                            ) {
                                Text(
                                    text = "Page $currentPage / $totalPages",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.nextPage() },
                                enabled = currentPage < totalPages,
                                modifier = Modifier.testTag("next_page_button")
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                            }
                        }

                        // Zoom Controls Group
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.zoomOut() },
                                enabled = zoomScale > 0.5f,
                                modifier = Modifier.testTag("zoom_out_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                            }

                            Box {
                                TextButton(
                                    onClick = { showZoomMenu = true },
                                    modifier = Modifier.testTag("zoom_percentage_button")
                                ) {
                                    Text(
                                        text = "${(zoomScale * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                DropdownMenu(
                                    expanded = showZoomMenu,
                                    onDismissRequest = { showZoomMenu = false }
                                ) {
                                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { scale ->
                                        DropdownMenuItem(
                                            text = { Text("${(scale * 100).toInt()}%") },
                                            onClick = {
                                                viewModel.setZoom(scale)
                                                showZoomMenu = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Fit Width") },
                                        onClick = {
                                            viewModel.setZoom(1.0f)
                                            showZoomMenu = false
                                        }
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.zoomIn() },
                                enabled = zoomScale < 4.0f,
                                modifier = Modifier.testTag("zoom_in_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In")
                            }
                        }
                    }
                }
            }
        }
    }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.currentTitle.ifEmpty { "Opening PDF Document..." },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uiState.loadingProgress / 100f },
                        modifier = Modifier.width(200.dp)
                    )
                }
            } else if (uiState.errorMessage != null && !uiState.showPasswordModal) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        uiState.currentUri?.let { uri -> viewModel.openPdf(uri) }
                    }) {
                        Text("Retry")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onBack) {
                        Text("Close")
                    }
                }
            } else {
                // PDF Document View Container
                if (uiState.readingMode == ReadingMode.SINGLE_PAGE) {
                    SinglePageView(
                        pdfEngine = pdfEngine,
                        currentPage = currentPage,
                        zoomScale = zoomScale,
                        rotationDegrees = uiState.pageRotationDegrees,
                        filterMode = uiState.filterMode,
                        showShadows = uiState.settings.showPageShadows,
                        onDoubleTapZoom = {
                            if (zoomScale > 1.0f) viewModel.setZoom(1.0f) else viewModel.setZoom(1.8f)
                        }
                    )
                } else if (uiState.readingMode == ReadingMode.HORIZONTAL_SWIPE) {
                    HorizontalSwipeView(
                        pdfEngine = pdfEngine,
                        totalPages = totalPages,
                        currentPage = currentPage,
                        zoomScale = zoomScale,
                        rotationDegrees = uiState.pageRotationDegrees,
                        filterMode = uiState.filterMode,
                        onPageChanged = { page -> viewModel.setPage(page) }
                    )
                } else {
                    // Default Continuous Vertical Scroll View
                    ContinuousScrollView(
                        pdfEngine = pdfEngine,
                        totalPages = totalPages,
                        currentPage = currentPage,
                        zoomScale = zoomScale,
                        rotationDegrees = uiState.pageRotationDegrees,
                        filterMode = uiState.filterMode,
                        showShadows = uiState.settings.showPageShadows,
                        onPageVisible = { page -> viewModel.setPage(page) },
                        onDoubleTapZoom = {
                            if (zoomScale > 1.0f) viewModel.setZoom(1.0f) else viewModel.setZoom(1.8f)
                        }
                    )
                }
            }

            // Search Overlay Panel
            AnimatedVisibility(
                visible = uiState.isSearchOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SearchDialog(
                    initialQuery = uiState.searchQuery,
                    searchResults = uiState.searchResults,
                    currentIndex = uiState.currentSearchIndex,
                    isSearching = uiState.isSearchingText,
                    onSearch = { query, cs, ww -> viewModel.performSearch(query, cs, ww) },
                    onNextResult = { viewModel.nextSearchResult() },
                    onPreviousResult = { viewModel.previousSearchResult() },
                    onSelectResult = { idx ->
                        val result = uiState.searchResults.getOrNull(idx)
                        if (result != null) viewModel.setPage(result.pageNumber)
                    },
                    onClose = { viewModel.setSearchOpen(false) }
                )
            }
        }
    }

    // Jump to Page Dialog
    if (showJumpPageDialog) {
        JumpPageDialog(
            currentPage = currentPage,
            totalPages = totalPages,
            onConfirm = { targetPage ->
                viewModel.setPage(targetPage)
                showJumpPageDialog = false
            },
            onDismiss = { showJumpPageDialog = false }
        )
    }
}

@Composable
private fun ContinuousScrollView(
    pdfEngine: PdfEngine?,
    totalPages: Int,
    currentPage: Int,
    zoomScale: Float,
    rotationDegrees: Int,
    filterMode: ReadingFilterMode,
    showShadows: Boolean,
    onPageVisible: (Int) -> Unit,
    onDoubleTapZoom: () -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentPage - 1).coerceAtLeast(0))
    val configuration = LocalConfiguration.current
    val screenWidthPx = (configuration.screenWidthDp * configuration.densityDpi / 160)

    LaunchedEffect(currentPage) {
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != currentPage - 1) {
            listState.scrollToItem((currentPage - 1).coerceAtLeast(0))
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            onPageVisible(index + 1)
        }
    }

    var scale by remember { mutableFloatStateOf(zoomScale) }
    LaunchedEffect(zoomScale) { scale = zoomScale }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTapZoom() }
                )
            }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            items(totalPages) { index ->
                val pageNum = index + 1
                PdfPageCard(
                    pdfEngine = pdfEngine,
                    pageIndex = index,
                    pageNum = pageNum,
                    rotationDegrees = rotationDegrees,
                    filterMode = filterMode,
                    targetWidthPx = screenWidthPx,
                    showShadow = showShadows
                )
            }
        }
    }
}

@Composable
private fun SinglePageView(
    pdfEngine: PdfEngine?,
    currentPage: Int,
    zoomScale: Float,
    rotationDegrees: Int,
    filterMode: ReadingFilterMode,
    showShadows: Boolean,
    onDoubleTapZoom: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthPx = (configuration.screenWidthDp * configuration.densityDpi / 160)
    var scale by remember { mutableFloatStateOf(zoomScale) }

    LaunchedEffect(zoomScale) { scale = zoomScale }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTapZoom() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            PdfPageCard(
                pdfEngine = pdfEngine,
                pageIndex = currentPage - 1,
                pageNum = currentPage,
                rotationDegrees = rotationDegrees,
                filterMode = filterMode,
                targetWidthPx = screenWidthPx,
                showShadow = showShadows
            )
        }
    }
}

@Composable
private fun HorizontalSwipeView(
    pdfEngine: PdfEngine?,
    totalPages: Int,
    currentPage: Int,
    zoomScale: Float,
    rotationDegrees: Int,
    filterMode: ReadingFilterMode,
    onPageChanged: (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = (currentPage - 1).coerceAtLeast(0),
        pageCount = { totalPages }
    )
    val configuration = LocalConfiguration.current
    val screenWidthPx = (configuration.screenWidthDp * configuration.densityDpi / 160)

    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage - 1) {
            pagerState.scrollToPage((currentPage - 1).coerceAtLeast(0))
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage + 1)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { index ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            PdfPageCard(
                pdfEngine = pdfEngine,
                pageIndex = index,
                pageNum = index + 1,
                rotationDegrees = rotationDegrees,
                filterMode = filterMode,
                targetWidthPx = screenWidthPx,
                showShadow = true
            )
        }
    }
}

@Composable
private fun PdfPageCard(
    pdfEngine: PdfEngine?,
    pageIndex: Int,
    pageNum: Int,
    rotationDegrees: Int,
    filterMode: ReadingFilterMode,
    targetWidthPx: Int,
    showShadow: Boolean
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val pageSize = remember(pdfEngine, pageIndex) {
        pdfEngine?.getPageSize(pageIndex) ?: Pair(595, 842)
    }
    val rawAspectRatio = pageSize.second.toFloat() / pageSize.first.toFloat().coerceAtLeast(1f)
    val aspectRatio = if (rotationDegrees == 90 || rotationDegrees == 270) 1f / rawAspectRatio else rawAspectRatio

    LaunchedEffect(pdfEngine, pageIndex, targetWidthPx, rotationDegrees) {
        isLoading = true
        bitmap = pdfEngine?.renderPage(pageIndex, targetWidth = targetWidthPx, rotationDegrees = rotationDegrees, renderScale = 1.0f)
        isLoading = false
    }

    val colorFilter = remember(filterMode) {
        when (filterMode) {
            ReadingFilterMode.NORMAL -> null
            ReadingFilterMode.SEPIA -> ColorFilter.tint(Color(0xFF704214), androidx.compose.ui.graphics.BlendMode.ColorBurn)
            ReadingFilterMode.DARK_INVERT -> ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        -1f,  0f,  0f, 0f, 255f,
                         0f, -1f,  0f, 0f, 255f,
                         0f,  0f, -1f, 0f, 255f,
                         0f,  0f,  0f, 1f,   0f
                    )
                )
            )
            ReadingFilterMode.EYE_COMFORT -> ColorFilter.tint(Color(0xFFFFFAEB), androidx.compose.ui.graphics.BlendMode.Multiply)
            ReadingFilterMode.ECO_GRAY -> ColorFilter.colorMatrix(
                ColorMatrix().apply { setToSaturation(0f) }
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f / aspectRatio)
            .padding(horizontal = 8.dp)
            .shadow(
                elevation = if (showShadow) 6.dp else 0.dp,
                shape = RoundedCornerShape(4.dp)
            ),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val bmp = bitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "PDF Page $pageNum",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    colorFilter = colorFilter
                )
            } else if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rendering Page $pageNum...",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun JumpPageDialog(
    currentPage: Int,
    totalPages: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf(currentPage.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Go to Page") },
        text = {
            Column {
                Text("Enter page number (1 to $totalPages):")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    singleLine = true,
                    label = { Text("Page Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("jump_page_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pageNum = textInput.toIntOrNull()
                    if (pageNum != null && pageNum in 1..totalPages) {
                        onConfirm(pageNum)
                    }
                },
                modifier = Modifier.testTag("jump_page_confirm_button")
            ) {
                Text("Go")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
