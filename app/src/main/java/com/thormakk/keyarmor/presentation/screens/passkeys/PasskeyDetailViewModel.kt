package com.thormakk.keyarmor.presentation.screens.passkeys

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasskeyDetailUiState(
    val passkey:      PasskeyCredential? = null,
    val isLoading:    Boolean            = true,
    val isTravelSafe: Boolean            = false,
    val isPinned:     Boolean            = false,
    val isProtected:  Boolean            = false,
    val category:     EntryCategory      = EntryCategory.SIN_CATEGORIZAR,
    val tags:         List<String>       = emptyList(),
    val snackbar:     String?            = null
)

@HiltViewModel
class PasskeyDetailViewModel @Inject constructor(
    savedStateHandle:          SavedStateHandle,
    private val repository:    PasskeyRepository,
    private val tagRepository: TagRepository,
    vaultRepository:           VaultRepository,
    private val planPrefs:         PlanPrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    @ApplicationContext private val appContextInyectado: Context
) : ViewModel() {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)

    private val credentialId: String = checkNotNull(savedStateHandle["id"])

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    val isActiveVaultReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, vaultId, isInherited ->
        isInherited || (plan == UserPlan.FREE && vaultId != VaultMetadata.PRIMARY_VAULT_ID)
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited ||
        (planPrefs.effectivePlan() == UserPlan.FREE && activeVaultPrefs.activeVaultId != VaultMetadata.PRIMARY_VAULT_ID)
    )

    private val _uiState = MutableStateFlow(PasskeyDetailUiState())
    val uiState: StateFlow<PasskeyDetailUiState> = _uiState

    val vaultEntries: StateFlow<List<VaultEntry>> = vaultRepository.observeEntries()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    init {
        viewModelScope.launch {
            repository.observeById(credentialId).collect { pk ->
                if (pk != null) {
                    _uiState.update {
                        it.copy(
                            passkey      = pk,
                            isLoading    = false,
                            isTravelSafe = pk.isTravelSafe,
                            isPinned     = pk.isPinned,
                            isProtected  = pk.isProtected,
                            category     = EntryCategory.entries
                                .firstOrNull { c -> c.name == pk.category }
                                ?: EntryCategory.SIN_CATEGORIZAR,
                            tags         = pk.tagList
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    val canProtectNew: Boolean get() = planPrefs.effectivePlan() != UserPlan.FREE

    fun setProtected(value: Boolean) {
        if (value && !canProtectNew) return
        _uiState.update { it.copy(isProtected = value) }
        viewModelScope.launch {
            try { repository.setProtected(credentialId, value) }
            catch (e: Exception) {
                _uiState.update { it.copy(isProtected = !value, snackbar = appContext.getString(R.string.share_error_saving)) }
            }
        }
    }

    fun saveTravelSafeOnly(value: Boolean) {
        _uiState.update { it.copy(isTravelSafe = value) }
        viewModelScope.launch {
            try { repository.saveTravelSafeOnly(credentialId, value) }
            catch (e: Exception) { _uiState.update { it.copy(snackbar = appContext.getString(R.string.share_error_saving)) } }
        }
    }

    fun saveHiddenOnly(value: Boolean) {
        val pk = _uiState.value.passkey ?: return
        _uiState.update { it.copy(passkey = pk.copy(isHidden = value)) }
        viewModelScope.launch {
            try { repository.saveHiddenOnly(credentialId, value) }
            catch (e: Exception) { _uiState.update { it.copy(snackbar = appContext.getString(R.string.share_error_saving)) } }
        }
    }

    private val _pinLimitReached = MutableStateFlow(false)
    val pinLimitReached: StateFlow<Boolean> = _pinLimitReached
    fun onPinLimitDismissed() { _pinLimitReached.value = false }

    fun togglePin() {
        val currentlyPinned = _uiState.value.isPinned
        viewModelScope.launch {
            if (!currentlyPinned && repository.countPinned() >= MAX_PINNED) {
                _pinLimitReached.value = true
                return@launch
            }
            val newValue = !currentlyPinned
            _uiState.update { it.copy(isPinned = newValue) }
            try { repository.togglePin(credentialId, newValue) }
            catch (e: Exception) { _uiState.update { it.copy(isPinned = currentlyPinned) } }
        }
    }

    fun onCategoryAndTagsSave(category: EntryCategory, tags: List<String>) {
        _uiState.update { it.copy(category = category, tags = tags) }
        viewModelScope.launch {
            try {
                val pk = _uiState.value.passkey ?: return@launch
                repository.saveMetadata(
                    pk.copy(
                        category = category.name,
                        tags     = tags.joinToString(",")
                    ),
                    soloMetadatos = true
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbar = appContext.getString(R.string.share_error_saving)) }
            }
        }
    }

    fun archive() {
        viewModelScope.launch {
            try { repository.archive(credentialId) }
            catch (e: Exception) { _uiState.update { it.copy(snackbar = appContext.getString(R.string.passkey_error_archiving)) } }
        }
    }

    fun delete() {
        viewModelScope.launch {
            try {
                if (!repository.delete(credentialId)) return@launch
            } catch (e: Exception) { _uiState.update { it.copy(snackbar = appContext.getString(R.string.passkey_error_deleting)) } }
        }
    }

    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String)               { viewModelScope.launch { tagRepository.addTag(name) } }
    fun renameTag(id: String, name: String){ viewModelScope.launch { tagRepository.renameTag(id, name) } }
    fun deleteTag(id: String)              { viewModelScope.launch { tagRepository.deleteTag(id) } }

    fun dismissSnackbar() { _uiState.update { it.copy(snackbar = null) } }


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveEntryTo(targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(credentialId), targetVaultId) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(credentialId), targetVaultId, inheritedFrom) }
    }

    fun removeInheritedFrom() {
        viewModelScope.launch { repository.removeInheritedFrom(credentialId) }
    }

    companion object { private const val MAX_PINNED = 3 }
}
