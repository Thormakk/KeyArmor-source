package com.thormakk.keyarmor.presentation.screens.voicedictation

import android.content.Context
import android.os.Build
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.VoiceDictationPrefs
import com.thormakk.keyarmor.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class VoiceDictationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: VoiceDictationPrefs,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _enabled = MutableStateFlow(prefs.isEnabled())
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _languageTag = MutableStateFlow(Locale.getDefault().toLanguageTag())
    val languageTag: StateFlow<String> = _languageTag.asStateFlow()

    val available: Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        SpeechRecognizer.isRecognitionAvailable(context)

    init {
        viewModelScope.launch {
            _languageTag.value = toSpeechTag(authRepository.getLanguage())
        }
    }

    fun setEnabled(value: Boolean) {
        prefs.setEnabled(value)
        _enabled.value = value
    }

    private fun toSpeechTag(code: String?): String = when (code) {
        null, "" -> Locale.getDefault().toLanguageTag()
        "zh"     -> "zh-CN"
        "in"     -> "id-ID"
        else     -> code
    }
}
