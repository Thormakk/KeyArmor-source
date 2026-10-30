package com.thormakk.keyarmor.presentation.screens.settings

import android.content.Context
import android.view.autofill.AutofillManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.AutofillPrefs
import com.thormakk.keyarmor.data.prefs.BlockKind
import com.thormakk.keyarmor.data.prefs.BlockLevel
import com.thormakk.keyarmor.data.prefs.BlockTarget
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.VaultEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import javax.inject.Inject

data class AutofillRowItem(
    val target: BlockTarget,
    val label: String,
    val level: BlockLevel? = null,
    val customName: String? = null,
) {
    val isSite: Boolean get() = target.kind == BlockKind.SITE
    val showsRawBelow: Boolean get() = customName != null
}

@HiltViewModel
class AutofillSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AutofillPrefs,
    private val vaultRepository: VaultRepository,
) : ViewModel() {

    private val _blocked = MutableStateFlow<List<AutofillRowItem>>(emptyList())
    val blocked: StateFlow<List<AutofillRowItem>> = _blocked.asStateFlow()

    private val _suggestions = MutableStateFlow<List<AutofillRowItem>>(emptyList())
    val suggestions: StateFlow<List<AutofillRowItem>> = _suggestions.asStateFlow()

    private val _offerUnlockWhenLocked = MutableStateFlow(prefs.offerUnlockWhenLocked())
    val offerUnlockWhenLocked: StateFlow<Boolean> = _offerUnlockWhenLocked.asStateFlow()

    val isServiceEnabled: Boolean
        get() = context.getSystemService(AutofillManager::class.java)
            ?.hasEnabledAutofillServices() == true

    private val collator: Collator = Collator.getInstance().apply { strength = Collator.PRIMARY }

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val blockedMap = prefs.blocked()
            val recent = prefs.recentTargets()
            val (blockedList, suggestionList) = withContext(Dispatchers.IO) {
                val rows = blockedMap
                    .map { (target, level) ->
                        val custom = prefs.name(target)
                        AutofillRowItem(target, custom ?: labelOf(target), level, custom)
                    }
                    .sortedWith(compareBy<AutofillRowItem> { !it.isSite }
                        .thenBy(collator) { it.label })
                val suggested = recent
                    .filter { it !in blockedMap }
                    .map { AutofillRowItem(it, labelOf(it)) }
                rows to suggested
            }
            _blocked.value = blockedList
            _suggestions.value = suggestionList
        }
    }

    private fun labelOf(target: BlockTarget): String =
        if (target.kind == BlockKind.SITE) target.value
        else runCatching {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(target.value, 0)).toString()
        }.getOrDefault(target.value)

    fun apply(target: BlockTarget, level: BlockLevel?, name: String?) {
        if (level != null) prefs.setName(target, name)
        prefs.setLevel(target, level)
        refresh()
    }

    fun addManually(input: String, kind: BlockKind): Boolean {
        val clean = input.filterNot { it.isWhitespace() }
            .removePrefix("https://").removePrefix("http://")
            .substringBefore("/")
            .removePrefix("www.")
        val pattern = if (kind == BlockKind.APP) PACKAGE_PATTERN else DOMAIN_PATTERN
        if (!pattern.matches(clean)) return false
        val target = if (kind == BlockKind.APP) BlockTarget.app(clean) else BlockTarget.site(clean)
        prefs.setLevel(target, BlockLevel.ALL)
        refresh()
        return true
    }

    fun clearRecent() {
        prefs.clearRecent()
        refresh()
    }

    fun removeSeen(target: BlockTarget) {
        prefs.removeSeen(target)
        refresh()
    }

    suspend fun entriesForAssociation(): List<VaultEntry> =
        vaultRepository.observeEntriesForAutofill().first()

    fun associateWithExistingEntry(entryId: String, target: BlockTarget) {
        viewModelScope.launch {
            val entry = vaultRepository.getEntry(entryId) ?: return@launch
            if (target.value in entry.urls) return@launch
            vaultRepository.saveEntry(
                entry.copy(urls = entry.urls + target.value, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun setOfferUnlockWhenLocked(enabled: Boolean) {
        prefs.setOfferUnlockWhenLocked(enabled)
        _offerUnlockWhenLocked.value = enabled
    }

    companion object {
        private val PACKAGE_PATTERN =
            Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$")

        private val DOMAIN_PATTERN =
            Regex("^([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$")
    }
}
