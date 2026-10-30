package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.security.ScreenCaptureManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenCapturePrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("screen_capture_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_SECONDS = 300

        val OPTIONS: List<Pair<Int, Int>> = listOf(
            60   to R.string.screen_capture_dur_1m,
            300  to R.string.screen_capture_dur_5m,
            900  to R.string.screen_capture_dur_15m,
            ScreenCaptureManager.UNTIL_APP_CLOSE to R.string.screen_capture_dur_until_close
        )
    }

    fun getDurationSeconds(): Int = prefs.getInt("duration_seconds", DEFAULT_SECONDS)
    fun setDurationSeconds(seconds: Int) = prefs.edit { putInt("duration_seconds", seconds) }
}
