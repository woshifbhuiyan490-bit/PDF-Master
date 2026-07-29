package com.example

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.ThemeMode
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PdfReaderScreen
import com.example.ui.theme.PdfMasterTheme
import com.example.ui.viewmodel.PdfUiState
import com.example.ui.viewmodel.PdfViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: PdfViewModel = viewModel(
                factory = PdfViewModel.Factory(applicationContext)
            )

            val uiState by viewModel.uiState.collectAsState()
            val recentPdfs by viewModel.recentPdfs.collectAsState()
            val bookmarks by viewModel.bookmarks.collectAsState()

            // Keep screen awake setting listener
            DisposableEffect(uiState.settings.keepScreenAwake) {
                if (uiState.settings.keepScreenAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            val darkTheme = when (uiState.settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            PdfMasterTheme(darkTheme = darkTheme) {
                if (uiState.currentUri != null) {
                    PdfReaderScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        bookmarks = bookmarks,
                        onBack = { viewModel.closeReader() }
                    )
                } else {
                    HomeScreen(
                        uiState = uiState,
                        recentPdfs = recentPdfs,
                        bookmarks = bookmarks,
                        onOpenPdfUri = { uri -> viewModel.openPdf(uri) },
                        onRemoveRecent = { recent -> viewModel.removeRecent(recent) },
                        onClearRecents = { viewModel.clearAllRecents() },
                        onDeleteBookmark = { bookmark -> viewModel.deleteBookmark(bookmark) },
                        onToggleFavorite = { recent -> viewModel.toggleFavorite(recent) },
                        onUpdateTag = { recent, tag -> viewModel.updateRecentTag(recent, tag) },
                        onSelectTagFilter = { filter -> viewModel.setTagFilter(filter) },
                        onOpenSettings = { viewModel.setShowSettingsSheet(true) },
                        onSaveSettings = { newSettings -> viewModel.updateSettings(newSettings) },
                        onDismissSettings = { viewModel.setShowSettingsSheet(false) },
                        onToggleTheme = {
                            val nextTheme = when (uiState.settings.themeMode) {
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.SYSTEM
                                ThemeMode.SYSTEM -> ThemeMode.LIGHT
                            }
                            viewModel.updateSettings(uiState.settings.copy(themeMode = nextTheme))
                        }
                    )
                }
            }
        }
    }
}
