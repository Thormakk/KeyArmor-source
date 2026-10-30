package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.thormakk.keyarmor.data.prefs.RecentEntriesPrefs.Companion.MAX_RECENT
import com.thormakk.keyarmor.domain.model.HubEntryType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentEntriesPrefs @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val PREFS_NAME = "recent_entries_prefs"
        private const val KEY_LIST   = "recent_list"
        const val  MAX_RECENT        = 8
        private const val SEP        = ":"
        private const val DIVIDER    = "|"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _recentRefsFlow = MutableStateFlow(loadRefs())
    val recentRefsFlow: StateFlow<List<PinnedRef>> = _recentRefsFlow

    private fun loadRefs(): List<PinnedRef> =
        (prefs.getString(KEY_LIST, "") ?: "")
            .split(DIVIDER)
            .filter { it.isNotBlank() }
            .mapNotNull { entry ->
                val idx = entry.lastIndexOf(SEP)
                if (idx < 0) return@mapNotNull null
                val id      = entry.substring(0, idx)
                val typeStr = entry.substring(idx + 1)
                val type    = runCatching { HubEntryType.valueOf(typeStr) }.getOrNull()
                    ?: return@mapNotNull null
                PinnedRef(id, type)
            }

    private fun saveList(list: List<PinnedRef>) {
        val encoded = list.joinToString(DIVIDER) { "${it.id}$SEP${it.type.name}" }
        prefs.edit { putString(KEY_LIST, encoded) }
        _recentRefsFlow.value = list
    }

    fun recordAccess(id: String, type: HubEntryType) {
        val current = _recentRefsFlow.value.toMutableList()
        current.removeAll { it.id == id }
        current.add(0, PinnedRef(id, type))
        saveList(current.take(MAX_RECENT))
    }
}
