package com.thormakk.keyarmor.presentation.screens.custom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.CustomEntry
import com.thormakk.keyarmor.domain.model.CustomField
import com.thormakk.keyarmor.domain.model.CustomFieldType
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
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
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class AddEditCustomEntryUiState(
    val id:         String            = "",
    val title:      String            = "",
    val fields:     List<CustomField> = emptyList(),
    val category:   EntryCategory     = EntryCategory.SIN_CATEGORIZAR,
    val tags:       List<String>      = emptyList(),
    val isFavorite:   Boolean           = false,
    val isProtected:  Boolean           = false,
    val isTravelSafe: Boolean           = false,
    val isPinned:     Boolean       = false,
    val isHidden:     Boolean       = false,
    val isNew:             Boolean           = true,
    val isSaved:           Boolean           = false,
    val isDeleted:         Boolean           = false,
    val createdAt:         Long              = System.currentTimeMillis(),
    val updatedAt:         Long              = System.currentTimeMillis(),
    val isClone:            Boolean    = false,
    val originalTitle:      String     = "",
    val cloneNameError:     Boolean    = false,
    val duplicateNameError: Boolean    = false,
    val titleError:         Boolean    = false,
    val inheritedFrom:      String?    = null
)

@HiltViewModel
class AddEditCustomEntryViewModel @Inject constructor(
    private val repository:      CustomEntryRepository,
    private val tagRepository:   TagRepository,
    private val planPrefs: PlanPrefs,
    private val authRepository:  AuthRepository,
    val biometricPrefs:          BiometricPrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val pinnedHubPrefs: PinnedHubPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    savedStateHandle:            SavedStateHandle
) : ViewModel() {

    private val entryId: String = savedStateHandle["id"] ?: "new"
    private val cloneOf: String = savedStateHandle["cloneOf"] ?: ""

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    private val _uiState = MutableStateFlow(AddEditCustomEntryUiState())
    val uiState: StateFlow<AddEditCustomEntryUiState> = _uiState

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)
    val isReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, vaultId, isInherited ->
        isInherited || (plan == UserPlan.FREE && vaultId != VaultMetadata.PRIMARY_VAULT_ID)
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited ||
        (planPrefs.effectivePlan() == UserPlan.FREE &&
        activeVaultPrefs.activeVaultId != VaultMetadata.PRIMARY_VAULT_ID)
    )

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        when {
            cloneOf.isNotEmpty() -> viewModelScope.launch {
                repository.getEntry(cloneOf)?.let { src ->
                    _uiState.value = AddEditCustomEntryUiState(
                        id           = UUID.randomUUID().toString(),
                        title        = src.title,
                        fields       = src.fields.map { it.copy(id = UUID.randomUUID().toString()) },
                        category     = src.category,
                        tags         = src.tags,
                        isFavorite   = false,
                        isTravelSafe = src.isTravelSafe,
                        isNew        = true,
                        isClone      = true,
                        originalTitle = src.title
                    )
                }
            }
            entryId != "new" -> viewModelScope.launch {
                repository.observeEntryById(entryId)
                    .collect { e ->
                        val current = _uiState.value
                        if (current.isSaved || current.isDeleted) return@collect
                        if (e != null) {
                            _uiState.value = AddEditCustomEntryUiState(
                                id           = e.id,
                                title        = e.title,
                                fields       = e.fields,
                                category     = e.category,
                                tags         = e.tags,
                                isFavorite   = e.isFavorite,
                                isProtected  = e.isProtected,
                                isTravelSafe = e.isTravelSafe,
                                isPinned      = e.isPinned,
                                isHidden      = e.isHidden,
                                isNew         = false,
                                createdAt     = e.createdAt,
                                updatedAt     = e.updatedAt,
                                inheritedFrom = e.inheritedFrom
                            )
                        }
                    }
            }
        }
    }

    fun onTitleChange(v: String) {
        _uiState.value = _uiState.value.copy(title = v, cloneNameError = false, titleError = false)
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
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

    fun saveFavoriteOnly(v: Boolean) {
        val s = _uiState.value.copy(isFavorite = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setFavorite(s.id, v) }
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
            .map { refs -> refs.any { it.id == entryId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), pinnedHubPrefs.isPinned(entryId))

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin() {
        if (entryId == "new") return
        if (pinnedHubPrefs.isPinned(entryId)) {
            pinnedHubPrefs.unpin(entryId)
        } else {
            val ok = pinnedHubPrefs.pin(entryId, com.thormakk.keyarmor.domain.model.HubEntryType.CUSTOM_ENTRY, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }




    fun onTravelSafeChange(v: Boolean) {
        _uiState.value = _uiState.value.copy(isTravelSafe = v)
    }

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

    fun addField(type: CustomFieldType, defaultLabel: String) {
        val new = CustomField(
            id    = UUID.randomUUID().toString(),
            label = defaultLabel,
            type  = type,
            value = ""
        )
        _uiState.value = _uiState.value.copy(fields = _uiState.value.fields + new)
    }

    fun updateFieldValue(fieldId: String, value: String) {
        _uiState.value = _uiState.value.copy(
            fields = _uiState.value.fields.map { if (it.id == fieldId) it.copy(value = value) else it }
        )
    }

    fun removeField(fieldId: String) {
        _uiState.value = _uiState.value.copy(
            fields = _uiState.value.fields.filter { it.id != fieldId }
        )
    }

    fun moveFieldUp(fieldId: String) {
        val list = _uiState.value.fields.toMutableList()
        val idx = list.indexOfFirst { it.id == fieldId }
        if (idx > 0) { val tmp = list[idx - 1]; list[idx - 1] = list[idx]; list[idx] = tmp }
        _uiState.value = _uiState.value.copy(fields = list)
    }

    fun moveFieldDown(fieldId: String) {
        val list = _uiState.value.fields.toMutableList()
        val idx = list.indexOfFirst { it.id == fieldId }
        if (idx < list.size - 1) { val tmp = list[idx + 1]; list[idx + 1] = list[idx]; list[idx] = tmp }
        _uiState.value = _uiState.value.copy(fields = list)
    }


    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String)                   { viewModelScope.launch { tagRepository.addTag(name) } }
    fun renameTag(id: String, newName: String) {
        viewModelScope.launch {
            val oldName = tagRepository.observeTags().first().find { it.id == id }?.name
            tagRepository.renameTag(id, newName)
            if (oldName != null) {
                val current = _uiState.value.tags
                if (current.contains(oldName)) {
                    _uiState.value = _uiState.value.copy(
                        tags = current.map { if (it == oldName) newName.trim() else it }
                    )
                }
            }
        }
    }
    fun deleteTag(id: String)                  { viewModelScope.launch { tagRepository.deleteTag(id) } }

    fun saveMetaOnly(category: EntryCategory, tags: List<String>) {
        _uiState.value = _uiState.value.copy(category = category, tags = tags)
        val state = _uiState.value
        if (!state.isNew) {
            viewModelScope.launch {
                repository.saveEntry(
                    CustomEntry(
                        id           = state.id,
                        title        = state.title,
                        fields       = state.fields,
                        category     = category,
                        tags         = tags,
                        isFavorite   = state.isFavorite,
                        isProtected  = state.isProtected,
                        isTravelSafe = state.isTravelSafe,
                        isHidden     = state.isHidden,
                        createdAt    = state.createdAt,
                        updatedAt    = System.currentTimeMillis(),
                        vaultId      = activeVaultPrefs.activeVaultId,
                    ), soloMetadatos = true
                )
            }
        }
    }


    fun dismissDuplicateNameError() {
        _uiState.value = _uiState.value.copy(duplicateNameError = false)
    }

    fun save() {
        val s = _uiState.value
        if (s.title.isBlank()) {
            _uiState.value = _uiState.value.copy(titleError = true)
            return
        }
        if (s.isClone && s.title.trim() == s.originalTitle.trim()) {
            _uiState.value = _uiState.value.copy(cloneNameError = true)
            return
        }
        viewModelScope.launch {
            val titleTrimmed = s.title.trim()
            val duplicate = repository.observeEntries().first()
                .any { it.id != s.id && it.title.trim().lowercase(Locale.ROOT) == titleTrimmed.lowercase(Locale.ROOT) }
            if (duplicate) {
                _uiState.value = _uiState.value.copy(duplicateNameError = true)
                return@launch
            }
            repository.saveEntry(
                CustomEntry(
                    id           = s.id.ifEmpty { UUID.randomUUID().toString() },
                    title        = titleTrimmed,
                    fields       = s.fields,
                    category     = s.category,
                    tags         = s.tags,
                    isFavorite   = s.isFavorite,
                    isProtected  = s.isProtected,
                    isTravelSafe = s.isTravelSafe,
                    isPinned     = s.isPinned,
                    isHidden     = s.isHidden,
                    createdAt    = s.createdAt,
                    updatedAt    = System.currentTimeMillis(),
                    vaultId      = activeVaultPrefs.activeVaultId,
                )
            )
            _uiState.value = _uiState.value.copy(
                isSaved   = true,
                title     = titleTrimmed,
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


    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveEntryTo(targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(entryId), targetVaultId) }
    }

    fun removeInheritedFrom() {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.removeInheritedFrom(id) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(entryId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
}
