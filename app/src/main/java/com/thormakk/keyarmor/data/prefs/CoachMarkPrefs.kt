package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoachMarkPrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun key(screenId: String, anchorId: String) = "$screenId/$anchorId"

    fun hasSeen(screenId: String, anchorId: String): Boolean =
        prefs.getStringSet(KEY_SEEN, emptySet()).orEmpty().contains(key(screenId, anchorId))

    fun markSeen(screenId: String, anchorId: String) {
        val k = key(screenId, anchorId)
        val current = prefs.getStringSet(KEY_SEEN, emptySet()).orEmpty()
        if (k in current) return
        prefs.edit { putStringSet(KEY_SEEN, current + k) }
    }

    fun resetAll() {
        prefs.edit { remove(KEY_SEEN) }
    }

    companion object {
        private const val PREFS_NAME = "coachmark_prefs"
        private const val KEY_SEEN   = "seen_anchor_ids"
    }
}
