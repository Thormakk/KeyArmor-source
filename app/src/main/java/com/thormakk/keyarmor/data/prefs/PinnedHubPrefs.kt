package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.VaultMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class PinnedRef(val id: String, val type: HubEntryType, val vaultId: String = "")

@Singleton
class PinnedHubPrefs @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val PREFS_NAME  = "pinned_hub_prefs"
        private const val KEY_RAW     = "pinned_entries"
        private const val KEY_ORDER   = "pinned_entries_order"
        const val  MAX_PINNED         = 4
        private const val SEP         = ":"

        fun toJson(refs: List<PinnedRef>): JSONArray = JSONArray().also { a ->
            refs.forEach { r ->
                a.put(JSONObject().apply {
                    put("id",      r.id)
                    put("type",    r.type.name)
                    put("vaultId", r.vaultId)
                })
            }
        }

        fun fromJson(arr: JSONArray): List<PinnedRef> = buildList {
            for (i in 0 until arr.length()) {
                val o    = arr.optJSONObject(i) ?: continue
                val id   = o.optString("id").ifBlank { null } ?: continue
                val type = runCatching { HubEntryType.valueOf(o.optString("type")) }.getOrNull() ?: continue
                add(PinnedRef(id, type, o.optString("vaultId").ifBlank { VaultMetadata.PRIMARY_VAULT_ID }))
            }
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadRefs(): List<PinnedRef> {
        val raw     = prefs.getStringSet(KEY_RAW, emptySet()) ?: emptySet()
        val ordered = (prefs.getString(KEY_ORDER, "") ?: "")
            .split("|")
            .filter { it.isNotBlank() }
        val refMap: Map<String, PinnedRef> = raw.mapNotNull { entry ->
            val parts = entry.split(SEP)
            if (parts.size < 2) return@mapNotNull null
            val vaultId = if (parts.size >= 3) parts[parts.size - 1] else VaultMetadata.PRIMARY_VAULT_ID
            val typeStr = if (parts.size >= 3) parts[parts.size - 2] else parts[parts.size - 1]
            val id      = parts.dropLast(if (parts.size >= 3) 2 else 1).joinToString(SEP)
            val type    = runCatching { HubEntryType.valueOf(typeStr) }.getOrNull()
                          ?: return@mapNotNull null
            if (id.isBlank()) return@mapNotNull null
            id to PinnedRef(id, type, vaultId)
        }.toMap()
        return if (ordered.isEmpty()) refMap.values.toList()
               else ordered.mapNotNull { refMap[it] }
    }

    private fun saveList(list: List<PinnedRef>) {
        val raw   = list.map { "${it.id}$SEP${it.type.name}$SEP${it.vaultId}" }.toSet()
        val order = list.joinToString("|") { it.id }
        prefs.edit {
            putStringSet(KEY_RAW, raw)
            putString(KEY_ORDER, order)
        }
        _pinnedRefsFlow.value = list
    }

    private val _pinnedRefsFlow = MutableStateFlow(loadRefs())

    val pinnedRefsFlow: StateFlow<List<PinnedRef>> = _pinnedRefsFlow

    fun pin(id: String, type: HubEntryType, vaultId: String): Boolean {
        val current = _pinnedRefsFlow.value
        if (current.any { it.id == id }) return true
        val countInVault = current.count { it.vaultId == vaultId }
        if (countInVault >= MAX_PINNED) return false
        saveList(current + PinnedRef(id, type, vaultId))
        return true
    }

    fun unpin(id: String) {
        val current = _pinnedRefsFlow.value
        if (current.none { it.id == id }) return
        saveList(current.filter { it.id != id })
    }

    fun unpinAllByVault(vaultId: String) {
        val current = _pinnedRefsFlow.value
        if (current.none { it.vaultId == vaultId }) return
        saveList(current.filter { it.vaultId != vaultId })
    }

    fun unpinAll() {
        if (_pinnedRefsFlow.value.isEmpty()) return
        saveList(emptyList())
    }

    fun currentRefs(): List<PinnedRef> = _pinnedRefsFlow.value

    fun restoreFromBackup(
        refsDelBackup: List<PinnedRef>,
        idMap: Map<String, String> = emptyMap(),
        completar: Boolean = false
    ) {
        val actuales = _pinnedRefsFlow.value
        val base = if (completar) actuales.toMutableList() else mutableListOf()
        val nuevos = mutableListOf<PinnedRef>()
        val bovedasDelBackup = HashSet<String>()
        for (r in refsDelBackup) {
            bovedasDelBackup += r.vaultId
            val nuevoId = idMap[r.id] ?: r.id
            if (base.any { it.id == nuevoId } || nuevos.any { it.id == nuevoId }) continue
            val enBoveda = base.count { it.vaultId == r.vaultId } + nuevos.count { it.vaultId == r.vaultId }
            if (enBoveda >= MAX_PINNED) continue
            nuevos += r.copy(id = nuevoId)
        }
        val conservados =
            if (completar) actuales
            else actuales.filter { it.vaultId !in bovedasDelBackup }
        saveList(conservados + nuevos)
    }

    fun isPinned(id: String): Boolean = _pinnedRefsFlow.value.any { it.id == id }

    fun reorder(vaultId: String, fromIndex: Int, toIndex: Int) {
        val current = _pinnedRefsFlow.value
        val vaultRefs = current.filter { it.vaultId == vaultId }.toMutableList()
        if (fromIndex !in vaultRefs.indices || toIndex !in vaultRefs.indices) return
        val item = vaultRefs.removeAt(fromIndex)
        vaultRefs.add(toIndex, item)
        var vi = 0
        val merged = current.map { ref -> if (ref.vaultId == vaultId) vaultRefs[vi++] else ref }
        saveList(merged)
    }
}
