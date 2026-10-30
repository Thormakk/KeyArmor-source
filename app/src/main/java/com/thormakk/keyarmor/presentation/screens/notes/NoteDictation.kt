package com.thormakk.keyarmor.presentation.screens.notes

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.ModelDownloadListener
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

class NoteDictationController internal constructor(
    private val appContext: Context,
    private val languageTag: () -> String,
    private val onText: (text: String, isFinal: Boolean) -> Unit,
    private val reportError: (Int) -> Unit,
) {
    var isListening by mutableStateOf(false)
        private set

    private var recognizer: SpeechRecognizer? = null
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var muted = false
    private val beepHandler = Handler(Looper.getMainLooper())
    private val unmuteRunnable = Runnable { doUnmute() }
    private val beepStreams = intArrayOf(
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_SYSTEM,
        AudioManager.STREAM_NOTIFICATION
    )

    private var triedDefaultLang = false

    val available: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                SpeechRecognizer.isRecognitionAvailable(appContext)

    @RequiresApi(Build.VERSION_CODES.S)
    private fun ensureRecognizer(): SpeechRecognizer {
        recognizer?.let { return it }
        return SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext).also {
            it.setRecognitionListener(listener)
            recognizer = it
        }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            if (isLanguageError(error) && !triedDefaultLang) {
                triedDefaultLang = true
                startInternal(withLanguage = false)
                return
            }
            scheduleUnmute()
            isListening = false
            reportError(error)
        }
        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) onText(text, true)
            scheduleUnmute()
            isListening = false
        }
        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) onText(text, false)
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) { reportError(GENERIC_ERROR); return }
        if (isListening) return
        triedDefaultLang = false
        startInternal(withLanguage = true)
    }

    private fun startInternal(withLanguage: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) { reportError(GENERIC_ERROR); return }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            if (withLanguage) putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag())
        }
        try {
            muteBeep()
            ensureRecognizer().startListening(intent)
            isListening = true
        } catch (_: Exception) {
            unmuteBeepNow()
            isListening = false
            reportError(GENERIC_ERROR)
        }
    }

    fun stop() {
        try { recognizer?.stopListening() } catch (_: Exception) {}
        isListening = false
    }

    private fun muteBeep() {
        beepHandler.removeCallbacks(unmuteRunnable)
        if (muted) return
        beepStreams.forEach {
            try { audioManager.adjustStreamVolume(it, AudioManager.ADJUST_MUTE, 0) } catch (_: Exception) {}
        }
        muted = true
    }

    private fun scheduleUnmute() {
        beepHandler.removeCallbacks(unmuteRunnable)
        beepHandler.postDelayed(unmuteRunnable, 900L)
    }

    private fun unmuteBeepNow() {
        beepHandler.removeCallbacks(unmuteRunnable)
        doUnmute()
    }

    private fun doUnmute() {
        if (!muted) return
        beepStreams.forEach {
            try { audioManager.adjustStreamVolume(it, AudioManager.ADJUST_UNMUTE, 0) } catch (_: Exception) {}
        }
        muted = false
    }

    val canRequestDownload: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    var isDownloading by mutableStateOf(false)
        private set

    var downloadFailed by mutableStateOf(false)
        private set

    var downloadProgress by mutableStateOf<Int?>(null)
        private set

    fun clearDownload() {
        isDownloading = false
        downloadFailed = false
        downloadProgress = null
    }

    fun requestLanguageDownload() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag())
        }
        isDownloading = true
        downloadFailed = false
        downloadProgress = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                triggerDownloadWithProgress(intent)
            } else {
                ensureRecognizer().triggerModelDownload(intent)
            }
        } catch (_: Exception) {
            isDownloading = false
            downloadFailed = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun triggerDownloadWithProgress(intent: Intent) {
        ensureRecognizer().triggerModelDownload(
            intent,
            ContextCompat.getMainExecutor(appContext),
            object : ModelDownloadListener {
                override fun onProgress(completedPercent: Int) {
                    isDownloading = true
                    downloadFailed = false
                    downloadProgress = completedPercent
                }
                override fun onScheduled() {
                    isDownloading = true
                    downloadFailed = false
                    downloadProgress = null
                }
                override fun onSuccess() {
                    isDownloading = false
                    downloadFailed = false
                    downloadProgress = null
                }
                override fun onError(error: Int) {
                    isDownloading = false
                    downloadFailed = true
                    downloadProgress = null
                }
            }
        )
    }

    fun destroy() {
        unmuteBeepNow()
        try { recognizer?.destroy() } catch (_: Exception) {}
        recognizer = null
        isListening = false
    }

    companion object {
        const val GENERIC_ERROR = -1

        private const val ERR_LANGUAGE_NOT_SUPPORTED = 12
        private const val ERR_LANGUAGE_UNAVAILABLE = 13

        fun isLanguageError(error: Int): Boolean =
            error == ERR_LANGUAGE_NOT_SUPPORTED || error == ERR_LANGUAGE_UNAVAILABLE
    }
}

@Composable
fun rememberNoteDictationController(
    languageTag: () -> String,
    onText: (text: String, isFinal: Boolean) -> Unit,
    onError: (Int) -> Unit,
): NoteDictationController {
    val context = LocalContext.current.applicationContext
    val controller = remember {
        NoteDictationController(context, languageTag, onText, onError)
    }
    DisposableEffect(Unit) {
        onDispose { controller.destroy() }
    }
    return controller
}
