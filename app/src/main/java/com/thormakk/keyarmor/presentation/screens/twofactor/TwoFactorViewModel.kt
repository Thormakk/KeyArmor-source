package com.thormakk.keyarmor.presentation.screens.twofactor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.SnapshotRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.components.selection.BulkMode
import com.thormakk.keyarmor.presentation.screens.search.fuzzyMatchesPrimary
import com.thormakk.keyarmor.presentation.screens.search.shouldRunFuzzy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import com.thormakk.keyarmor.presentation.util.sinOcultos
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TWO_FACTOR_FREE_TOTP_LIMIT = 1

data class TwoFactorUiState(
    val accounts: List<TwoFactorAccount>      = emptyList(),
    val fuzzyAccounts: List<TwoFactorAccount> = emptyList(),
    val query: String                         = "",
    val isLoading: Boolean                    = true,
    val isAtFreeTotpLimit: Boolean            = false
)

@HiltViewModel
class TwoFactorViewModel @Inject constructor(
    private val repository:             TwoFactorRepository,
    private val snapshotRepository:     SnapshotRepository,
    planPrefs:               PlanPrefs,
    private val activeVaultPrefs:        ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val isActiveVaultReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, vaultId, isInherited ->
        isInherited || (plan == UserPlan.FREE && vaultId != VaultMetadata.PRIMARY_VAULT_ID)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited ||
        (planPrefs.effectivePlan() == UserPlan.FREE && activeVaultPrefs.activeVaultId != VaultMetadata.PRIMARY_VAULT_ID))

    private val _hiddenIds = MutableStateFlow<Set<String>>(emptySet())
    private val _bulkDeleting = MutableStateFlow(false)
    val bulkDeleting: StateFlow<Boolean> = _bulkDeleting.asStateFlow()

    val uiState: StateFlow<TwoFactorUiState> = combine(
        repository.observeAccounts().sinOcultos(_hiddenIds) { it.id },
        _query,
        planPrefs.planFlow
    ) { accounts, query, plan ->
        val filtered = if (query.isBlank()) accounts
        else accounts.filter { account ->
            account.name.contains(query, ignoreCase = true) ||
                account.issuer.contains(query, ignoreCase = true) ||
                account.accountLabel.contains(query, ignoreCase = true)
        }
        val fuzzyAccounts = if (shouldRunFuzzy(query)) {
            val exactIds = filtered.mapTo(HashSet()) { it.id }
            accounts.filter { it.id !in exactIds && it.fuzzyMatchesPrimary(query) }
        } else emptyList()
        val totpCount = accounts.count { it.hasTotp }
        TwoFactorUiState(
            accounts          = filtered,
            fuzzyAccounts     = fuzzyAccounts,
            query             = query,
            isLoading         = false,
            isAtFreeTotpLimit = plan == UserPlan.FREE && totpCount >= TWO_FACTOR_FREE_TOTP_LIMIT
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = TwoFactorUiState(isLoading = true)
    )

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    fun onQueryChange(value: String) { _query.value = value }


    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    val isSelectionMode: Boolean get() = _selectedIds.value.isNotEmpty()

    fun toggleSelection(id: String) {
        _selectedIds.update { if (id in it) it - id else it + id }
    }

    fun selectAll() {
        _selectedIds.value = uiState.value.accounts.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun bulkArchive() {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            repository.archiveBatch(ids)
            _selectedIds.value = emptySet()
        }
    }

    fun bulkSetProtected(value: Boolean) {
        if (value && isFreeUser.value) return
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            ids.forEach { repository.setProtected(it, value) }
            _selectedIds.value = emptySet()
        }
    }

    private val _protectedSkipped = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val protectedSkipped: SharedFlow<Int> = _protectedSkipped.asSharedFlow()

    fun bulkDelete() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty() || _bulkDeleting.value) return
        _bulkDeleting.value = true
        viewModelScope.launch {
            try {
                val protegidas = repository.protectedAmong(ids)
                _hiddenIds.value = ids.filter { it !in protegidas }.toSet()
                _selectedIds.value = emptySet()
                withContext(NonCancellable) {
                    if (ids.size >= 3) {
                        snapshotRepository.createSnapshotBestEffort(SnapshotTrigger.PRE_BULK_DELETE)
                    }
                    repository.softDeleteBatch(ids)
                    if (protegidas.isNotEmpty()) _protectedSkipped.tryEmit(protegidas.size)
                    delay(400)
                }
            } finally {
                _hiddenIds.value = emptySet()
                _bulkDeleting.value = false
            }
        }
    }

    fun bulkUpdateCategory(category: EntryCategory, mode: BulkMode) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            uiState.value.accounts
                .filter { it.id in ids }
                .forEach { entry ->
                    val newCategory = when (mode) {
                        BulkMode.ADDITIVE -> if (entry.category == EntryCategory.SIN_CATEGORIZAR) category else entry.category
                        BulkMode.REMOVE   -> EntryCategory.SIN_CATEGORIZAR
                        BulkMode.REPLACE  -> category
                    }
                    if (newCategory != entry.category) {
                        repository.saveAccount(entry.copy(category = newCategory), soloMetadatos = true)
                    }
                }
            _selectedIds.value = emptySet()
        }
    }

    fun bulkUpdateTags(tags: Set<String>, mode: BulkMode) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            uiState.value.accounts
                .filter { it.id in ids }
                .forEach { entry ->
                    val newTags = when (mode) {
                        BulkMode.ADDITIVE -> (entry.tags + tags).distinct()
                        BulkMode.REMOVE   -> entry.tags.filter { it !in tags }
                        BulkMode.REPLACE  -> tags.toList()
                    }
                    if (newTags != entry.tags) {
                        repository.saveAccount(entry.copy(tags = newTags), soloMetadatos = true)
                    }
                }
            _selectedIds.value = emptySet()
        }
    }

    fun toggleFavorite(account: TwoFactorAccount) {
        viewModelScope.launch {
            repository.setFavorite(account.id, !account.isFavorite)
        }
    }

    fun togglePin(account: TwoFactorAccount) {
        viewModelScope.launch {
            if (!account.isPinned && repository.countPinned() >= MAX_PINNED) {
                _pinLimitReached.value = true
                return@launch
            }
            repository.setPin(account.id, !account.isPinned)
        }
    }

    private val _pinLimitReached = MutableStateFlow(false)
    val pinLimitReached: StateFlow<Boolean> = _pinLimitReached
    fun onPinLimitDismissed() { _pinLimitReached.value = false }


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun bulkMoveTo(targetVaultId: String) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            repository.moveEntriesToVault(ids, targetVaultId)
            _selectedIds.value = emptySet()
        }
    }

    fun bulkCopyTo(targetVaultId: String) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited) {
                activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
            } else null
            repository.copyEntriesToVault(ids, targetVaultId, inheritedFrom)
            _selectedIds.value = emptySet()
        }
    }

    fun moveEntryTo(entryId: String, targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(entryId), targetVaultId) }
    }

    fun copyEntryTo(entryId: String, targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited) {
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        } else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(entryId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
}