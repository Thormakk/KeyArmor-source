package com.thormakk.keyarmor.presentation.screens.bank

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditBankAccountUiState(
    val id:              String        = "",
    val bankName:        String        = "",
    val swiftBic:        String        = "",
    val accountType:     String        = "",
    val accountNumber:   String        = "",
    val cbuIban:         String        = "",
    val alias:           String        = "",
    val notes:           String        = "",
    val linkedPasswordId: String       = "",
    val category:        EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val tags:            List<String>  = emptyList(),
    val isFavorite:      Boolean       = false,
    val isTravelSafe:    Boolean       = false,
    val isPinned:        Boolean       = false,
    val isHidden:        Boolean       = false,
    val isProtected:     Boolean       = false,
    val isNew:              Boolean       = true,
    val isSaved:            Boolean       = false,
    val isDeleted:          Boolean       = false,
    val createdAt:          Long          = System.currentTimeMillis(),
    val updatedAt:          Long          = System.currentTimeMillis(),
    val isClone:            Boolean       = false,
    val inheritedFrom:      String?       = null
)

@HiltViewModel
class AddEditBankAccountViewModel @Inject constructor(
    private val repository:       BankAccountRepository,
    private val tagRepository:    TagRepository,
    private val vaultRepository:  VaultRepository,
    private val planPrefs: PlanPrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val pinnedHubPrefs: PinnedHubPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    savedStateHandle:             SavedStateHandle
) : ViewModel() {

    private val accountId: String = savedStateHandle["id"] ?: "new"
    private val cloneOf:   String = savedStateHandle["cloneOf"] ?: ""

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    private val _uiState = MutableStateFlow(AddEditBankAccountUiState())
    val uiState: StateFlow<AddEditBankAccountUiState> = _uiState

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)
    val isReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, _, isInherited ->
        isInherited || plan == UserPlan.FREE
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited || planPrefs.effectivePlan() == UserPlan.FREE
    )

    private val _vaultEntries = MutableStateFlow<List<VaultEntry>>(emptyList())
    val vaultEntries: StateFlow<List<VaultEntry>> = _vaultEntries

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            vaultRepository.observeEntries().collect { list ->
                _vaultEntries.value = list
            }
        }
        when {
            cloneOf.isNotEmpty() -> viewModelScope.launch {
                repository.getAccount(cloneOf)?.let { src ->
                    _uiState.value = AddEditBankAccountUiState(
                        id               = java.util.UUID.randomUUID().toString(),
                        bankName         = src.bankName,
                        swiftBic         = src.swiftBic,
                        accountType      = src.accountType,
                        accountNumber    = src.accountNumber,
                        cbuIban          = src.cbuIban,
                        alias            = src.alias,
                        notes            = src.notes,
                        linkedPasswordId = src.linkedPasswordId,
                        category         = src.category,
                        tags             = src.tags,
                        isFavorite       = false,
                        isTravelSafe     = src.isTravelSafe,
                        isNew            = true,
                        isClone          = true
                    )
                }
            }
            accountId != "new" -> viewModelScope.launch {
                repository.observeAccountById(accountId)
                    .collect { acc ->
                        val current = _uiState.value
                        if (current.isSaved || current.isDeleted) return@collect
                        if (acc != null) {
                            _uiState.value = AddEditBankAccountUiState(
                                id               = acc.id,
                                bankName         = acc.bankName,
                                swiftBic         = acc.swiftBic,
                                accountType      = acc.accountType,
                                accountNumber    = acc.accountNumber,
                                cbuIban          = acc.cbuIban,
                                alias            = acc.alias,
                                notes            = acc.notes,
                                linkedPasswordId = acc.linkedPasswordId,
                                category         = acc.category,
                                tags             = acc.tags,
                                isFavorite       = acc.isFavorite,
                                isTravelSafe     = acc.isTravelSafe,
                                isPinned         = acc.isPinned,
                                isHidden         = acc.isHidden,
                                isProtected      = acc.isProtected,
                                isNew            = false,
                                createdAt        = acc.createdAt,
                                updatedAt        = acc.updatedAt,
                                inheritedFrom    = acc.inheritedFrom
                            )
                        }
                    }
            }
        }
    }

    fun onBankNameChange(v: String)         { _uiState.value = _uiState.value.copy(bankName = v) }
    fun onSwiftBicChange(v: String)         { _uiState.value = _uiState.value.copy(swiftBic = v) }
    fun onAccountTypeChange(v: String)      { _uiState.value = _uiState.value.copy(accountType = v) }
    fun onCbuIbanChange(v: String)          { if (v.length <= 29) _uiState.value = _uiState.value.copy(cbuIban = v) }
    fun onAliasChange(v: String)            { _uiState.value = _uiState.value.copy(alias = v) }
    fun onNotesChange(v: String)            { if (v.length <= 300) _uiState.value = _uiState.value.copy(notes = v) }
    fun onLinkedPasswordIdChange(v: String) { _uiState.value = _uiState.value.copy(linkedPasswordId = v) }
    fun toggleFavorite()                    { _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite) }

    fun saveFavoriteOnly(v: Boolean) {
        val s = _uiState.value.copy(isFavorite = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setFavorite(s.id, v) }
    }

    val canProtectNew: Boolean get() = planPrefs.effectivePlan() != UserPlan.FREE

    fun setProtected(value: Boolean) {
        val s = _uiState.value
        if (s.isNew) return
        if (value && !canProtectNew) return
        viewModelScope.launch {
            repository.setProtected(s.id, value)
            _uiState.value = _uiState.value.copy(isProtected = value)
        }
    }

    fun togglePin() {
        val s = _uiState.value
        if (s.isNew) return
        viewModelScope.launch {
            if (!s.isPinned && repository.countPinned() >= MAX_PINNED) {
                _pinLimitReached.value = true
                return@launch
            }
            repository.setPin(s.id, !s.isPinned)
            _uiState.value = _uiState.value.copy(isPinned = !s.isPinned)
        }
    }

    private val _pinLimitReached = MutableStateFlow(false)
    val pinLimitReached: StateFlow<Boolean> = _pinLimitReached
    fun onPinLimitDismissed() { _pinLimitReached.value = false }

    val isHubPinned: StateFlow<Boolean> =
        pinnedHubPrefs.pinnedRefsFlow
            .map { refs -> refs.any { it.id == accountId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), pinnedHubPrefs.isPinned(accountId))

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin() {
        if (accountId == "new") return
        if (pinnedHubPrefs.isPinned(accountId)) {
            pinnedHubPrefs.unpin(accountId)
        } else {
            val ok = pinnedHubPrefs.pin(accountId, com.thormakk.keyarmor.domain.model.HubEntryType.BANK_ACCOUNT, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }


    fun onTravelSafeChange(v: Boolean) { _uiState.value = _uiState.value.copy(isTravelSafe = v) }

    fun saveTravelSafeOnly(v: Boolean) {
        val s = _uiState.value.copy(isTravelSafe = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setTravelSafe(s.id, v) }
    }

    fun saveHiddenOnly(v: Boolean) {
        val s = _uiState.value.copy(isHidden = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setHidden(s.id, v) }
    }


    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String)                   { viewModelScope.launch { tagRepository.addTag(name) } }
    fun renameTag(id: String, newName: String) {
        viewModelScope.launch {
            val oldName = tagRepository.observeTags().first().find { it.id == id }?.name
            tagRepository.renameTag(id, newName)
            oldName?.let { name ->
                val current = _uiState.value.tags
                if (current.contains(name)) {
                    _uiState.value = _uiState.value.copy(
                        tags = current.map { if (it == name) newName.trim() else it }
                    )
                }
            }
        }
    }
    fun deleteTag(id: String) { viewModelScope.launch { tagRepository.deleteTag(id) } }

    fun saveMetaOnly(category: EntryCategory, tags: List<String>) {
        _uiState.value = _uiState.value.copy(category = category, tags = tags)
        val s = _uiState.value
        if (!s.isNew) {
            viewModelScope.launch {
                repository.saveAccount(buildAccount(s).copy(category = category, tags = tags), soloMetadatos = true)
            }
        }
    }

    fun save() {
        if (isReadOnly.value) return
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveAccount(buildAccount(s))
            _uiState.value = _uiState.value.copy(
                isSaved   = true,
                notes     = s.notes,
                updatedAt = System.currentTimeMillis()
            )
        }
    }


    fun delete() {
        viewModelScope.launch {
            val id = _uiState.value.id
            if (id.isNotEmpty()) {
                if (!repository.softDelete(id)) return@launch
                pinnedHubPrefs.unpin(id)
            }
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    fun archive() {
        viewModelScope.launch {
            val id = _uiState.value.id
            if (!_uiState.value.isNew) { repository.archive(id); pinnedHubPrefs.unpin(id) }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    private fun buildAccount(s: AddEditBankAccountUiState): BankAccount = BankAccount(
        id               = if (s.id.isEmpty()) java.util.UUID.randomUUID().toString() else s.id,
        bankName         = s.bankName,
        swiftBic         = s.swiftBic,
        accountType      = s.accountType,
        accountNumber    = s.accountNumber,
        cbuIban          = s.cbuIban,
        alias            = s.alias,
        notes            = s.notes,
        linkedPasswordId = s.linkedPasswordId,
        category         = s.category,
        tags             = s.tags,
        isFavorite       = s.isFavorite,
        isTravelSafe     = s.isTravelSafe,
        isPinned         = s.isPinned,
        isHidden         = s.isHidden,
        isProtected      = s.isProtected,
        createdAt        = s.createdAt,
        updatedAt        = System.currentTimeMillis(),
        vaultId          = activeVaultPrefs.activeVaultId,
    )


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveEntryTo(targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(accountId), targetVaultId) }
    }

    fun removeInheritedFrom() {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.removeInheritedFrom(id) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(accountId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
}
