package com.example.pdf

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TtsState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val speechRate: Float = 1.0f,
    val currentPageIndex: Int = 0,
    val currentSentence: String = "",
    val isReady: Boolean = false,
    val error: String? = null
)

class PdfSpeechPlayer(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _state = MutableStateFlow(TtsState())
    val state: StateFlow<TtsState> = _state.asStateFlow()

    private var onPageComplete: (() -> Unit)? = null

    init {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _state.value = _state.value.copy(isPlaying = true, isPaused = false)
            }

            override fun onDone(utteranceId: String?) {
                _state.value = _state.value.copy(isPlaying = false, isPaused = false)
                onPageComplete?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _state.value = _state.value.copy(isPlaying = false, error = "Speech error occurred")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _state.value = _state.value.copy(isPlaying = false, error = "Speech error code: $errorCode")
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            val isAvailable = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            _state.value = _state.value.copy(isReady = isAvailable)
        } else {
            _state.value = _state.value.copy(isReady = false, error = "TTS Engine initialization failed")
        }
    }

    fun speak(text: String, pageIndex: Int, onComplete: () -> Unit) {
        if (text.isBlank()) {
            onComplete()
            return
        }
        this.onPageComplete = onComplete
        tts?.setSpeechRate(_state.value.speechRate)
        _state.value = _state.value.copy(currentPageIndex = pageIndex, currentSentence = text.take(120) + "...")
        
        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "pdf_tts_page_$pageIndex"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pdf_tts_page_$pageIndex")
    }

    fun pause() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            _state.value = _state.value.copy(isPlaying = false, isPaused = true)
        }
    }

    fun stop() {
        tts?.stop()
        _state.value = _state.value.copy(isPlaying = false, isPaused = false)
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.5f)
        _state.value = _state.value.copy(speechRate = clamped)
        tts?.setSpeechRate(clamped)
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
