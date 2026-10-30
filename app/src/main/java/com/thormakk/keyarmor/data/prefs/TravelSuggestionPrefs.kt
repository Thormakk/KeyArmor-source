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
class TravelSuggestionPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("travel_suggestion_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENABLED           = "calendar_suggestion_enabled"
        private const val KEY_NOTIFIED_KEYS     = "notified_event_keys"
        private const val KEY_DISMISSED_KEYS    = "dismissed_event_keys"
        private const val KEY_PENDING_EVENT_KEY = "pending_event_key"
        private const val KEY_PENDING_TRIP_DATE = "pending_trip_date_millis"
    }

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ENABLED, enabled) }
        if (!enabled) clearPending()
    }

    fun getNotifiedKeys(): Set<String> = prefs.getStringSet(KEY_NOTIFIED_KEYS, emptySet()) ?: emptySet()

    fun setNotifiedKeys(keys: Set<String>) {
        prefs.edit { putStringSet(KEY_NOTIFIED_KEYS, keys) }
    }

    fun isDismissed(eventKey: String): Boolean =
        (prefs.getStringSet(KEY_DISMISSED_KEYS, emptySet()) ?: emptySet()).contains(eventKey)

    fun addDismissed(eventKey: String) {
        val current = (prefs.getStringSet(KEY_DISMISSED_KEYS, emptySet()) ?: emptySet()).toMutableSet()
        current.add(eventKey)
        prefs.edit { putStringSet(KEY_DISMISSED_KEYS, current) }
    }

    data class PendingSuggestion(val eventKey: String, val tripDateMillis: Long)

    private val _pendingFlow = MutableStateFlow(readPending())
    val pendingFlow: StateFlow<PendingSuggestion?> = _pendingFlow.asStateFlow()

    fun setPending(eventKey: String, tripDateMillis: Long) {
        prefs.edit {
            putString(KEY_PENDING_EVENT_KEY, eventKey)
            putLong(KEY_PENDING_TRIP_DATE, tripDateMillis)
        }
        _pendingFlow.value = PendingSuggestion(eventKey, tripDateMillis)
    }

    fun clearPending() {
        prefs.edit {
            remove(KEY_PENDING_EVENT_KEY)
            remove(KEY_PENDING_TRIP_DATE)
        }
        _pendingFlow.value = null
    }

    private fun readPending(): PendingSuggestion? {
        val key  = prefs.getString(KEY_PENDING_EVENT_KEY, null) ?: return null
        val date = prefs.getLong(KEY_PENDING_TRIP_DATE, -1L)
        if (date <= 0) return null
        return PendingSuggestion(key, date)
    }
}
