package com.thormakk.keyarmor.presentation.screens.privacy

import androidx.lifecycle.ViewModel
import com.thormakk.keyarmor.data.prefs.ScreenCapturePrefs
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.security.ScreenCaptureManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ScreenCaptureViewModel @Inject constructor(
    private val manager:        ScreenCaptureManager,
    private val prefs:          ScreenCapturePrefs,
    private val authRepository: AuthRepository
) : ViewModel() {

    val permitActive: StateFlow<Boolean> = manager.permitActive

    private val _durationSeconds = MutableStateFlow(prefs.getDurationSeconds())
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    fun setDuration(seconds: Int) {
        prefs.setDurationSeconds(seconds)
        _durationSeconds.value = seconds
    }

    fun grant() = manager.grant(prefs.getDurationSeconds())

    fun revoke() = manager.revoke()

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)
}
