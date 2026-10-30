package com.thormakk.keyarmor.security

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginThrottle @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun remainingWaitMs(): Long {
        val count = prefs.getInt(KEY_FAIL_COUNT, 0)
        val required = requiredDelayMs(count)
        if (required == 0L) return 0L

        val lastElapsed = prefs.getLong(KEY_LAST_FAIL_ELAPSED, 0L)
        val lastWall    = prefs.getLong(KEY_LAST_FAIL_WALL, 0L)
        val nowElapsed  = SystemClock.elapsedRealtime()
        val nowWall     = System.currentTimeMillis()

        val elapsedByRealtime = nowElapsed - lastElapsed
        val elapsed = if (elapsedByRealtime >= 0L) {
            elapsedByRealtime
        } else {
            (nowWall - lastWall).coerceAtLeast(0L)
        }
        return (required - elapsed).coerceAtLeast(0L)
    }

    fun recordFailure() {
        val count = prefs.getInt(KEY_FAIL_COUNT, 0) + 1
        prefs.edit {
            putInt(KEY_FAIL_COUNT, count)
            putLong(KEY_LAST_FAIL_ELAPSED, SystemClock.elapsedRealtime())
            putLong(KEY_LAST_FAIL_WALL, System.currentTimeMillis())
        }
    }

    fun recordSuccess() {
        prefs.edit {
            remove(KEY_FAIL_COUNT)
            remove(KEY_LAST_FAIL_ELAPSED)
            remove(KEY_LAST_FAIL_WALL)
        }
    }

    companion object {
        private const val PREFS_NAME = "login_throttle_prefs"
        private const val KEY_FAIL_COUNT         = "fail_count"
        private const val KEY_LAST_FAIL_ELAPSED  = "last_fail_elapsed"
        private const val KEY_LAST_FAIL_WALL     = "last_fail_wall"

        private const val MIN = 60_000L
        private const val HOUR = 60L * MIN

        fun requiredDelayMs(failCount: Int): Long = when {
            failCount <= 3 -> 0L
            failCount == 4 -> 1L * MIN
            failCount == 5 -> 5L * MIN
            failCount == 6 -> 10L * MIN
            failCount == 7 -> 30L * MIN
            failCount == 8 -> 1L * HOUR
            else           -> 6L * HOUR
        }
    }
}
