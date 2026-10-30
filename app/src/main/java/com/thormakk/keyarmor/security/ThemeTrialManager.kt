package com.thormakk.keyarmor.security

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeTrialManager @Inject constructor() {

    companion object {
        const val TRIAL_MINUTES = 15
        private const val TRIAL_MILLIS = TRIAL_MINUTES * 60 * 1000L
    }

    private val _trialThemeId = MutableStateFlow<String?>(null)
    val trialThemeId: StateFlow<String?> = _trialThemeId.asStateFlow()

    private var expiresAtElapsed = 0L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var expiryJob: Job? = null

    fun startTrial(themeId: String) {
        expiryJob?.cancel()
        expiresAtElapsed = SystemClock.elapsedRealtime() + TRIAL_MILLIS
        _trialThemeId.value = themeId
        expiryJob = scope.launch {
            delay(TRIAL_MILLIS)
            endTrial()
        }
    }

    fun endTrial() {
        expiryJob?.cancel()
        expiryJob = null
        expiresAtElapsed = 0L
        if (_trialThemeId.value != null) _trialThemeId.value = null
    }

    fun checkExpiry() {
        if (_trialThemeId.value == null) return
        if (expiresAtElapsed != 0L && SystemClock.elapsedRealtime() >= expiresAtElapsed) {
            endTrial()
        }
    }

    fun onVaultLocked() = endTrial()
}
