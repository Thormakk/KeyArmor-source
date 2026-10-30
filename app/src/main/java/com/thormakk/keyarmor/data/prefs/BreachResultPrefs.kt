package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreachResultPrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("breach_result_prefs", Context.MODE_PRIVATE)

    data class Resultado(
        val checkedAt: Long,
        val conteos: Map<String, Int>
    )

    fun load(): Resultado {
        val conteos = prefs.getStringSet("items", emptySet()).orEmpty().mapNotNull { s ->
            val i = s.lastIndexOf('|')
            if (i <= 0) null else s.substring(0, i) to (s.substring(i + 1).toIntOrNull() ?: 0)
        }.toMap()
        return Resultado(prefs.getLong("checked_at", 0L), conteos)
    }

    fun save(conteos: Map<String, Int>, checkedAt: Long = System.currentTimeMillis()) {
        prefs.edit {
            putLong("checked_at", checkedAt)
            putStringSet("items", conteos.map { (id, n) -> "$id|$n" }.toSet())
        }
    }

    fun clear() = prefs.edit { clear() }
}
