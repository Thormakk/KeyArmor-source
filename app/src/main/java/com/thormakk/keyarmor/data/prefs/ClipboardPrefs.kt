package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("clipboard_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_SECONDS = 30
        val OPTIONS_SECONDS = listOf(10, 20, 30, 45, 60, 90, 120)
    }

    fun isEnabled(): Boolean = prefs.getBoolean("enabled", true)
    fun setEnabled(enabled: Boolean) = prefs.edit { putBoolean("enabled", enabled) }

    fun getTimeoutSeconds(): Int = prefs.getInt("timeout_seconds", DEFAULT_SECONDS)
    fun setTimeoutSeconds(seconds: Int) = prefs.edit { putInt("timeout_seconds", seconds) }
}
