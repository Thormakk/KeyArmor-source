package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteHistoryPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("note_history_prefs", Context.MODE_PRIVATE)

    fun isGlobalEnabled(): Boolean   = prefs.getBoolean("global_enabled", false)
    fun setGlobalEnabled(v: Boolean) = prefs.edit { putBoolean("global_enabled", v) }
}
