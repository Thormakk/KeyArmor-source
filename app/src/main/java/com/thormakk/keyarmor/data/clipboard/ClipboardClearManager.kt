package com.thormakk.keyarmor.data.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import com.thormakk.keyarmor.data.prefs.ClipboardPrefs
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardClearManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs:          ClipboardPrefs,
    private val sessionManager: SessionManager
) {
    private val androidClipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pendingJob: Job? = null

    @Volatile private var isAppForeground = false

    @Volatile private var lastOwnClipTimestamp: Long? = null

    @Volatile private var pendingDebt = false

    @Volatile private var suppressNextClipChange = false

    private val primaryClipListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (suppressNextClipChange) {
            suppressNextClipChange = false
            return@OnPrimaryClipChangedListener
        }
        if (!isAppForeground) return@OnPrimaryClipChangedListener
        lastOwnClipTimestamp = androidClipboard.primaryClipDescription?.timestamp
        scheduleClear()
    }

    init {
        androidClipboard.addPrimaryClipChangedListener(primaryClipListener)

        scope.launch {
            var wasUnlocked = false
            sessionManager.isUnlockedFlow.collect { unlocked ->
                if (unlocked) {
                    wasUnlocked = true
                } else if (wasUnlocked) {
                    wasUnlocked = false
                    clearNow()
                }
            }
        }
    }

    fun onActivityForeground() {
        isAppForeground = true
        payPendingClearIfAny()
    }

    fun onActivityBackground() {
        isAppForeground = false
    }

    fun scheduleClear() {
        if (!prefs.isEnabled()) return
        pendingJob?.cancel()
        pendingJob = scope.launch {
            delay(prefs.getTimeoutSeconds() * 1000L)
            doClear()
        }
    }

    fun clearNow() {
        pendingJob?.cancel()
        pendingJob = null
        doClear()
    }

    fun payPendingClearIfAny() {
        if (pendingDebt) {
            pendingDebt = false
            performClear()
        }
    }

    private fun doClear() {
        if (!isAppForeground) {
            pendingDebt = true
            return
        }
        performClear()
    }

    private fun performClear() {
        if (!ownsCurrentClip()) return
        suppressNextClipChange = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            androidClipboard.clearPrimaryClip()
        } else {
            @Suppress("DEPRECATION")
            androidClipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }

    private fun ownsCurrentClip(): Boolean {
        val current = androidClipboard.primaryClipDescription?.timestamp ?: return false
        return current == lastOwnClipTimestamp
    }
}
