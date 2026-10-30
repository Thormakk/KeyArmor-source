package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.thormakk.keyarmor.data.prefs.BiometricPrefs.Companion.MODE_PER_ACCESS
import com.thormakk.keyarmor.data.prefs.BiometricPrefs.Companion.MODE_SESSION
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
    private val planPrefs: PlanPrefs
) {
    private val prefs = context.getSharedPreferences("biometric_gate_prefs", Context.MODE_PRIVATE)

    companion object {
        const val SECTION_PASSWORDS  = "passwords"
        const val SECTION_CARDS      = "cards"
        const val SECTION_CUSTOM     = "custom"
        const val SECTION_TWO_FACTOR = "two_factor"

        const val DEFAULT_TIMEOUT   = 60
        val TIMEOUT_OPTIONS         = listOf(30, 60, 120)

        const val MODE_PER_ACCESS   = "PER_ACCESS"
        const val MODE_SESSION      = "SESSION"
    }

    fun isEnabled(): Boolean = prefs.getBoolean("gate_enabled", false)
    fun setEnabled(enabled: Boolean) { prefs.edit { putBoolean("gate_enabled", enabled) } }

    fun getEnabledSections(): Set<String> =
        prefs.getStringSet("gate_sections", emptySet()) ?: emptySet()

    fun setEnabledSections(sections: Set<String>) {
        prefs.edit { putStringSet("gate_sections", sections) }
    }

    fun toggleSection(section: String) {
        val current = getEnabledSections().toMutableSet()
        if (section in current) current.remove(section) else current.add(section)
        setEnabledSections(current)
    }

    fun isSectionEnabled(section: String): Boolean =
        planPrefs.effectivePlan() != UserPlan.FREE &&
        isEnabled() &&
        section in getEnabledSections()

    fun getMode(): String = prefs.getString("gate_mode", MODE_PER_ACCESS) ?: MODE_PER_ACCESS
    fun setMode(mode: String) { prefs.edit { putString("gate_mode", mode) } }

    fun getSessionTimeoutSeconds(): Int = prefs.getInt("gate_timeout", DEFAULT_TIMEOUT)
    fun setSessionTimeoutSeconds(seconds: Int) { prefs.edit { putInt("gate_timeout", seconds) } }
}
