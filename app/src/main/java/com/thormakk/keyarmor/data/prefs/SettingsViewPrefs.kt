package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsViewPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("settings_view_prefs", Context.MODE_PRIVATE)

    private val _simple = MutableStateFlow(prefs.getBoolean(KEY_SIMPLE, false))
    val simple: StateFlow<Boolean> = _simple

    fun setSimple(value: Boolean) {
        prefs.edit { putBoolean(KEY_SIMPLE, value) }
        _simple.value = value
    }

    fun driveHasBackup(): Boolean = prefs.getBoolean(KEY_DRIVE_BACKUP, false)
    fun setDriveHasBackup(value: Boolean) = prefs.edit { putBoolean(KEY_DRIVE_BACKUP, value) }

    private companion object {
        const val KEY_SIMPLE = "simple"
        const val KEY_DRIVE_BACKUP = "drive_has_backup"
    }
}
