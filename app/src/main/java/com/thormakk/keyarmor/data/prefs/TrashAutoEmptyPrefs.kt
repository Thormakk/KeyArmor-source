package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashAutoEmptyPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("trash_auto_empty_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DISABLED = 0
        val OPTIONS = listOf(DISABLED, 7, 15, 30)
    }

    fun getPeriodDays(): Int = prefs.getInt("period_days", DISABLED)
    fun setPeriodDays(days: Int) = prefs.edit { putInt("period_days", days) }
}
