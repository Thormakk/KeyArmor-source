package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TravelModePrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("travel_mode_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENABLED = "travel_mode_enabled"
    }

    private val _travelModeFlow = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    val travelModeFlow: StateFlow<Boolean> = _travelModeFlow.asStateFlow()

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ENABLED, enabled) }
        _travelModeFlow.value = enabled
    }
}
