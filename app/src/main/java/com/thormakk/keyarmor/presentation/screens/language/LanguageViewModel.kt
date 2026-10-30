package com.thormakk.keyarmor.presentation.screens.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.security.AutoLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val authRepository:  AuthRepository,
    private val tagRepository:   TagRepository,
    private val autoLockManager: AutoLockManager
) : ViewModel() {

    val currentLanguage: StateFlow<String> = flow {
        emit(authRepository.getLanguage() ?: detectSystemLanguage())
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = detectSystemLanguage()
    )

    companion object {
        fun detectSystemLanguage(): String {
            val systemLang = when (val raw = Locale.getDefault().language) {
                "in" -> "id"
                "iw" -> "he"
                "ji" -> "yi"
                else -> raw
            }
            return if (systemLang in TagRepository.SUPPORTED_LANGUAGE_CODES) systemLang else "en"
        }
    }

    fun saveLanguage(code: String) {
        viewModelScope.launch {
            authRepository.setLanguage(code)
            autoLockManager.skipLockOnNextForeground()
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(code)
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { tagRepository.reconcileAfterLanguageChange(code) }
        }
    }
}
