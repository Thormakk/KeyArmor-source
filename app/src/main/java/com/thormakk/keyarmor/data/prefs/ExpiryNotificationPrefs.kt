package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpiryNotificationPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("expiry_notif_prefs", Context.MODE_PRIVATE)

    companion object {
        val WARNING_DAY_OPTIONS = listOf(1, 3, 7, 14, 30, 60, 90)
        private val DEFAULT_WARNING_DAY_SET = setOf(7)
    }

    fun isEnabled(): Boolean = prefs.getBoolean("enabled", true)
    fun setEnabled(enabled: Boolean) = prefs.edit { putBoolean("enabled", enabled) }

    fun getWarningDaySet(): Set<Int> {
        val stored = prefs.getStringSet("warning_day_set", null)
        if (stored != null) return stored.mapNotNull { it.toIntOrNull() }.toSet()
        val legacy = prefs.getInt("warning_days", -1)
        return if (legacy > 0) setOf(legacy) else DEFAULT_WARNING_DAY_SET
    }

    fun setWarningDaySet(days: Set<Int>) {
        prefs.edit { putStringSet("warning_day_set", days.map { it.toString() }.toSet()) }
    }

    fun toggleWarningDay(day: Int) {
        val current = getWarningDaySet().toMutableSet()
        if (current.contains(day)) {
            if (current.size > 1) current.remove(day)
        } else {
            current.add(day)
        }
        setWarningDaySet(current)
    }

    fun getWarningDays(): Int = getWarningDaySet().maxOrNull() ?: 7

    fun getNotifiedKeys(): Set<String> =
        prefs.getStringSet("notified_keys", emptySet()) ?: emptySet()

    fun setNotifiedKeys(keys: Set<String>) {
        prefs.edit { putStringSet("notified_keys", keys) }
    }
}
