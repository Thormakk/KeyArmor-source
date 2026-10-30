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
class ScreenCaptureManager @Inject constructor() {

    companion object {
        const val UNTIL_APP_CLOSE = -1
    }

    private val _permitActive = MutableStateFlow(false)
    val permitActive: StateFlow<Boolean> = _permitActive.asStateFlow()

    private var expiresAtElapsed = 0L
    private var untilAppClose = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var expiryJob: Job? = null

    fun grant(durationSeconds: Int) {
        expiryJob?.cancel()
        if (durationSeconds == UNTIL_APP_CLOSE) {
            untilAppClose = true
            expiresAtElapsed = 0L
        } else {
            untilAppClose = false
            expiresAtElapsed = SystemClock.elapsedRealtime() + durationSeconds * 1000L
            expiryJob = scope.launch {
                delay(durationSeconds * 1000L)
                revoke()
            }
        }
        _permitActive.value = true
    }

    fun revoke() {
        expiryJob?.cancel()
        expiryJob = null
        untilAppClose = false
        expiresAtElapsed = 0L
        if (_permitActive.value) _permitActive.value = false
    }

    fun remainingMillis(): Long? {
        if (!_permitActive.value || untilAppClose || expiresAtElapsed == 0L) return null
        val rem = expiresAtElapsed - SystemClock.elapsedRealtime()
        return if (rem > 0) rem else 0L
    }

    fun checkExpiry() {
        if (!_permitActive.value || untilAppClose) return
        if (expiresAtElapsed != 0L && SystemClock.elapsedRealtime() >= expiresAtElapsed) {
            revoke()
        }
    }

    fun onVaultLocked() = revoke()
}
