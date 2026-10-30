package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.AutoLockPrefs.Companion.NEVER
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoLockPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("auto_lock_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_SECONDS = 180
        const val NEVER           = -1

        val OPTIONS: List<Pair<Int, Int>> = listOf(
            30   to R.string.auto_lock_30s,
            60   to R.string.auto_lock_1m,
            120  to R.string.auto_lock_2m,
            180  to R.string.auto_lock_3m,
            300  to R.string.auto_lock_5m,
            600  to R.string.auto_lock_10m,
            900  to R.string.auto_lock_15m,
            1800 to R.string.auto_lock_30m,
            NEVER to R.string.auto_lock_never
        )
    }

    fun getTimeoutSeconds(): Int = prefs.getInt("timeout_seconds", DEFAULT_SECONDS)
    fun setTimeoutSeconds(seconds: Int) = prefs.edit { putInt("timeout_seconds", seconds) }

}
