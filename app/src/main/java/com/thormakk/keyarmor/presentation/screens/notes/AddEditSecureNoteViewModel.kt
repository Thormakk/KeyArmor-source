package com.thormakk.keyarmor.presentation.screens.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.NoteHistoryPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.SecureNoteHistoryRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class AddEditSecureNoteUiState(
    val id:         String        = UUID.randomUUID().toString(),
    val title:      String        = "",
    val titleError: Boolean       = false,
    val content:    String        = "",
    val originalContent: String   = "",
    val category:   EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val tags:       List<String>  = emptyList(),
    val isFavorite:   Boolean       = false,
    val isTravelSafe: Boolean       = false,
    val isPinned:     Boolean       = false,
    val isHidden:     Boolean       = false,
    val isProtected:  Boolean       = false,
    val historyEnabled: Boolean     = false,
    val isNew:             Boolean       = true,
    val isLoading:         Boolean       = false,
    val isSaved:           Boolean       = false,
    val isDeleted:         Boolean       = false,
    val createdAt:         Long          = System.currentTimeMillis(),
    val updatedAt:         Long          = System.currentTimeMillis(),
    val isClone:            Boolean = false,
    val originalTitle:      String  = "",
    val cloneNameError:     Boolean = false,
    val duplicateNameError: Boolean = false,
    val inheritedFrom:      String? = null
)

@HiltViewModel
class AddEditSecureNoteViewModel @Inject constructor(
    private val repository:        SecureNoteRepository,
    private val historyRepository: SecureNoteHistoryRepository,
    private val noteHistoryPrefs:  NoteHistoryPrefs,
    private val tagRepository:     TagRepository,
    private val planPrefs: PlanPrefs,
    private val activeVaultPrefs:  ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val pinnedHubPrefs:    PinnedHubPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    savedStateHandle:              SavedStateHandle
) : ViewModel() {

    private val noteId:  String = savedStateHandle["id"] ?: "new"

    val isNewNote: Boolean get() = noteId == "new"
    private val cloneOf: String = savedStateHandle["cloneOf"] ?: ""

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    private val _uiState = MutableStateFlow(AddEditSecureNoteUiState())
    val uiState: StateFlow<AddEditSecureNoteUiState> = _uiState

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
                val src = repository.getNote(cloneOf)
                if (src != null) {
                    _uiState.value = AddEditSecureNoteUiState(
                        id             = UUID.randomUUID().toString(),
                        title          = src.title,
                        content        = src.content,
                        category       = src.category,
                        tags           = src.tags,
                        isFavorite     = false,
                        isTravelSafe   = src.isTravelSafe,
                        historyEnabled = noteHistoryPrefs.isGlobalEnabled(),
                        isNew          = true,
                        isClone        = true,
                        originalTitle  = src.title
                    )
                }
            }
            noteId == "new" -> {
                _uiState.value = _uiState.value.copy(
                    historyEnabled = noteHistoryPrefs.isGlobalEnabled()
                )
            }
            else -> viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.observeNoteById(noteId)
                    .collect { note ->
                        val current = _uiState.value
                        if (current.isSaved || current.isDeleted) return@collect
                        if (note != null) {
                            _uiState.value = AddEditSecureNoteUiState(
                                id              = note.id,
                                title           = note.title,
                                content         = note.content,
                                originalContent = note.content,
                                category        = note.category,
                                tags            = note.tags,
                                isFavorite      = note.isFavorite,
                                isTravelSafe    = note.isTravelSafe,
                                isPinned        = note.isPinned,
                                isHidden        = note.isHidden,
                                isProtected     = note.isProtected,
                                historyEnabled  = note.historyEnabled,
                                isNew           = false,
                                isLoading       = false,
                                createdAt       = note.createdAt,
                                updatedAt       = note.updatedAt,
                                inheritedFrom   = note.inheritedFrom
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
            }
        }
    }

    fun onTitleChange(v: String)   { _uiState.value = _uiState.value.copy(title   = v, titleError = false, cloneNameError = false) }
    fun onContentChange(v: String) { if (v.length <= 5000) _uiState.value = _uiState.value.copy(content = v) }
    fun toggleFavorite()           { _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite) }

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

    private val _pinLimitReached = MutableStateFlow(false)
    val pinLimitReached: StateFlow<Boolean> = _pinLimitReached
    fun onPinLimitDismissed() { _pinLimitReached.value = false }

    val isHubPinned: StateFlow<Boolean> =
        pinnedHubPrefs.pinnedRefsFlow
            .map { refs -> refs.any { it.id == noteId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), pinnedHubPrefs.isPinned(noteId))

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin() {
        if (noteId == "new") return
        if (pinnedHubPrefs.isPinned(noteId)) {
            pinnedHubPrefs.unpin(noteId)
        } else {
            val ok = pinnedHubPrefs.pin(noteId, com.thormakk.keyarmor.domain.model.HubEntryType.SECURE_NOTE, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }



    private val _globalHistoryEnabled = MutableStateFlow(noteHistoryPrefs.isGlobalEnabled())
    val globalHistoryEnabled: StateFlow<Boolean> = _globalHistoryEnabled.asStateFlow()

    fun toggleNoteHistory() {
        val s = _uiState.value
        val newValue = !s.historyEnabled
        _uiState.value = s.copy(historyEnabled = newValue)
        if (!s.isNew) {
            viewModelScope.launch {
                repository.updateHistoryEnabled(s.id, newValue)
            }
        }
    }

    fun deleteNoteHistory() {
        val id = _uiState.value.id
        viewModelScope.launch { historyRepository.deleteForNote(id) }
    }

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
        val s = _uiState.value
        if (!s.isNew) {
            viewModelScope.launch {
                repository.saveNote(
                    SecureNote(
                        id         = s.id,
                        title      = s.title.trim(),
                        content    = s.content,
                        category   = category,
                        tags       = tags,
                        isFavorite   = s.isFavorite,
                        isTravelSafe = s.isTravelSafe,
                        isPinned     = s.isPinned,
                        isHidden     = s.isHidden,
                        isProtected  = s.isProtected,
                        createdAt  = s.createdAt,
                        updatedAt  = System.currentTimeMillis(),
                        isDeleted  = false,
                        deletedAt  = null
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
        if (s.isClone && s.title.trim() == s.originalTitle.trim()) {
            _uiState.value = _uiState.value.copy(cloneNameError = true, titleError = true)
            return
        }
        if (s.title.isBlank()) {
            _uiState.value = _uiState.value.copy(titleError = true)
            return
        }
        viewModelScope.launch {
            val titleTrimmed = s.title.trim()
            val duplicate = repository.observeNotes().first()
                .any { it.id != s.id && it.title.trim().lowercase(Locale.ROOT) == titleTrimmed.lowercase(Locale.ROOT) }
            if (duplicate) {
                _uiState.value = _uiState.value.copy(duplicateNameError = true)
                return@launch
            }

            val contentChanged = s.content != s.originalContent
            if (!s.isNew && !s.isProtected && s.historyEnabled && noteHistoryPrefs.isGlobalEnabled() && contentChanged) {
                historyRepository.recordVersion(s.id, s.originalContent)
            }

            repository.saveNote(
                SecureNote(
                    id           = s.id,
                    title        = titleTrimmed,
                    content      = s.content,
                    category     = s.category,
                    tags         = s.tags,
                    isFavorite   = s.isFavorite,
                    isTravelSafe = s.isTravelSafe,
                    isPinned     = s.isPinned,
                    isHidden     = s.isHidden,
                    isProtected  = s.isProtected,
                    historyEnabled = s.historyEnabled,
                    createdAt    = s.createdAt,
                    updatedAt    = System.currentTimeMillis(),
                    isDeleted    = false,
                    deletedAt    = null,
                    vaultId      = activeVaultPrefs.activeVaultId,
                )
            )
            _uiState.value = _uiState.value.copy(
                isSaved         = true,
                title           = titleTrimmed,
                content         = s.content,
                originalContent = s.content,
                updatedAt       = System.currentTimeMillis()
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


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveEntryTo(targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(noteId), targetVaultId) }
    }

    fun removeInheritedFrom() {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.removeInheritedFrom(id) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(noteId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
}
