package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

enum class ReadingMode {
    CONTINUOUS,
    SINGLE_PAGE,
    HORIZONTAL_SWIPE
}

enum class PageFitMode {
    FIT_WIDTH,
    FIT_PAGE,
    ORIGINAL_SIZE
}

enum class ReadingFilterMode {
    NORMAL,
    SEPIA,
    DARK_INVERT,
    EYE_COMFORT,
    ECO_GRAY
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class PdfSettings(
    val readingMode: ReadingMode = ReadingMode.CONTINUOUS,
    val pageFitMode: PageFitMode = PageFitMode.FIT_WIDTH,
    val filterMode: ReadingFilterMode = ReadingFilterMode.NORMAL,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val rememberPosition: Boolean = true,
    val showPageShadows: Boolean = true,
    val enablePageAnimations: Boolean = true,
    val keepScreenAwake: Boolean = false,
    val defaultZoom: Float = 1.0f,
    val ttsSpeechRate: Float = 1.0f
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pdf_master_settings", Context.MODE_PRIVATE)

    fun getSettings(): PdfSettings {
        val readingModeStr = prefs.getString("reading_mode", ReadingMode.CONTINUOUS.name) ?: ReadingMode.CONTINUOUS.name
        val pageFitModeStr = prefs.getString("page_fit_mode", PageFitMode.FIT_WIDTH.name) ?: PageFitMode.FIT_WIDTH.name
        val filterModeStr = prefs.getString("filter_mode", ReadingFilterMode.NORMAL.name) ?: ReadingFilterMode.NORMAL.name
        val themeModeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name

        return PdfSettings(
            readingMode = runCatching { ReadingMode.valueOf(readingModeStr) }.getOrDefault(ReadingMode.CONTINUOUS),
            pageFitMode = runCatching { PageFitMode.valueOf(pageFitModeStr) }.getOrDefault(PageFitMode.FIT_WIDTH),
            filterMode = runCatching { ReadingFilterMode.valueOf(filterModeStr) }.getOrDefault(ReadingFilterMode.NORMAL),
            themeMode = runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.SYSTEM),
            rememberPosition = prefs.getBoolean("remember_position", true),
            showPageShadows = prefs.getBoolean("show_page_shadows", true),
            enablePageAnimations = prefs.getBoolean("enable_animations", true),
            keepScreenAwake = prefs.getBoolean("keep_awake", false),
            defaultZoom = prefs.getFloat("default_zoom", 1.0f),
            ttsSpeechRate = prefs.getFloat("tts_speech_rate", 1.0f)
        )
    }

    fun saveSettings(settings: PdfSettings) {
        prefs.edit()
            .putString("reading_mode", settings.readingMode.name)
            .putString("page_fit_mode", settings.pageFitMode.name)
            .putString("filter_mode", settings.filterMode.name)
            .putString("theme_mode", settings.themeMode.name)
            .putBoolean("remember_position", settings.rememberPosition)
            .putBoolean("show_page_shadows", settings.showPageShadows)
            .putBoolean("enable_animations", settings.enablePageAnimations)
            .putBoolean("keep_awake", settings.keepScreenAwake)
            .putFloat("default_zoom", settings.defaultZoom)
            .putFloat("tts_speech_rate", settings.ttsSpeechRate)
            .apply()
    }
}
