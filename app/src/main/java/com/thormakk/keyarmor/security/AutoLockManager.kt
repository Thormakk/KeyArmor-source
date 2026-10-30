package com.thormakk.keyarmor.security

import android.os.SystemClock
import com.thormakk.keyarmor.data.prefs.AutoLockPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoLockManager @Inject constructor(
    private val prefs:          AutoLockPrefs,
    private val sessionManager: SessionManager
) {
    private val scope           = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var inactivityJob:  Job  = Job().also { it.cancel() }
    private var backgroundJob:  Job  = Job().also { it.cancel() }
    private var backgroundedAt: Long = 0L

    private val skipRemaining = AtomicInteger(0)

    private var skipArmedAt: Long = 0L

    private val _foregroundResumedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val foregroundResumedFlow: SharedFlow<Unit> = _foregroundResumedFlow.asSharedFlow()

    fun skipLockOnNextForeground() {
        skipRemaining.set(2)
        skipArmedAt = SystemClock.elapsedRealtime()
        inactivityJob.cancel()
        backgroundJob.cancel()
        backgroundedAt = 0L
    }

    fun onAppBackground() {
        skipRemaining.getAndUpdate { if (it > 0) it - 1 else 0 }
        if (!sessionManager.isUnlocked()) {
            inactivityJob.cancel()
            return
        }
        backgroundedAt = SystemClock.elapsedRealtime()
        inactivityJob.cancel()

        val timeout = prefs.getTimeoutSeconds()
        if (timeout == AutoLockPrefs.NEVER) return
        backgroundJob.cancel()
        backgroundJob = scope.launch {
            delay(timeout * 1000L)
            if (sessionManager.isUnlocked()) sessionManager.lock()
        }
    }

    fun enforceIfExpired(): Boolean {
        if (!sessionManager.isUnlocked()) return false
        val timeout = prefs.getTimeoutSeconds()
        if (timeout == AutoLockPrefs.NEVER) return true
        val desde = backgroundedAt
        if (desde <= 0L) return true
        val segundos = (SystemClock.elapsedRealtime() - desde) / 1000L
        if (segundos < timeout) return true
        sessionManager.lock()
        return false
    }

    fun onExternalUnlock() {
        if (!sessionManager.isUnlocked()) return
        val timeout = prefs.getTimeoutSeconds()
        if (timeout == AutoLockPrefs.NEVER) return
        backgroundedAt = SystemClock.elapsedRealtime()
        inactivityJob.cancel()
        backgroundJob.cancel()
        backgroundJob = scope.launch {
            delay(timeout * 1000L)
            if (sessionManager.isUnlocked()) sessionManager.lock()
        }
    }

    fun onAppForeground() {
        _foregroundResumedFlow.tryEmit(Unit)
        backgroundJob.cancel()
        if (!sessionManager.isUnlocked()) return

        val huboSkip   = skipRemaining.getAndSet(0) > 0
        val skipVigente = huboSkip && skipArmedAt > 0L &&
            (SystemClock.elapsedRealtime() - skipArmedAt) <= SKIP_TTL_MS
        skipArmedAt = 0L
        if (skipVigente) {
            backgroundedAt = 0L
            startInactivityTimer()
            return
        }

        val timeout = prefs.getTimeoutSeconds()
        if (timeout == AutoLockPrefs.NEVER) return

        if (backgroundedAt > 0L) {
            val elapsedSeconds = (SystemClock.elapsedRealtime() - backgroundedAt) / 1000L
            backgroundedAt = 0L
            if (elapsedSeconds >= timeout) {
                sessionManager.lock()
                return
            }
        }
        startInactivityTimer()
    }

    fun recordInteraction() {
        if (!sessionManager.isUnlocked()) return
        startInactivityTimer()
    }

    fun startInactivityTimer() {
        val timeout = prefs.getTimeoutSeconds()
        if (timeout == AutoLockPrefs.NEVER) return
        inactivityJob.cancel()
        inactivityJob = scope.launch {
            delay(timeout * 1000L)
            if (sessionManager.isUnlocked()) {
                sessionManager.lock()
            }
        }
    }


    private companion object {
        const val SKIP_TTL_MS = 60_000L
    }
}
