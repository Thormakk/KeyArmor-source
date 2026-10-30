package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreachCheckPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("breach_check_prefs", Context.MODE_PRIVATE)

    companion object {
        val FREQUENCY_OPTIONS = listOf(1, 3, 7)
        const val DEFAULT_FREQUENCY = 7
    }

    fun isEnabled(): Boolean    = prefs.getBoolean("enabled", false)
    fun setEnabled(v: Boolean)  = prefs.edit { putBoolean("enabled", v) }

    fun getFrequencyDays(): Int      = prefs.getInt("frequency_days", DEFAULT_FREQUENCY)
    fun setFrequencyDays(days: Int)  = prefs.edit { putInt("frequency_days", days) }
}
