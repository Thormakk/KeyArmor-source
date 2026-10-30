@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.thormakk.keyarmor.presentation.screens.vault

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.SecurityPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.remote.HibpPasswordChecker
import com.thormakk.keyarmor.data.remote.HibpResult
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

sealed class HibpCheckState {
    object Idle         : HibpCheckState()
    object Checking     : HibpCheckState()
    data class Pwned(val count: Int) : HibpCheckState()
    object NotPwned     : HibpCheckState()
    object NetworkError : HibpCheckState()
}

sealed class BreachSaveState {
    object Idle                      : BreachSaveState()
    object Checking                  : BreachSaveState()
    data class PwnedPendingConfirm(val count: Int) : BreachSaveState()
    object Clean                     : BreachSaveState()
}

data class AddEditEntryUiState(
    val id: String               = UUID.randomUUID().toString(),
    val title: String            = "",
    val username: String         = "",
    val email: String            = "",
    val emailError: Boolean      = false,
    val password: String         = "",
    val urls: List<String>       = emptyList(),
    val urlBeingAdded: String    = "",
    val urlBeingAddedError: Boolean = false,
    val notes: String            = "",
    val category: EntryCategory  = EntryCategory.SIN_CATEGORIZAR,
    val isFavorite:   Boolean      = false,
    val isTravelSafe: Boolean      = false,
    val isPinned:     Boolean      = false,
    val isHidden:     Boolean      = false,
    val tags: List<String>         = emptyList(),
    val createdAt: Long          = System.currentTimeMillis(),
    val updatedAt: Long          = System.currentTimeMillis(),
    val isEditing: Boolean       = false,
    val isLoading: Boolean       = false,
    val titleError: Boolean      = false,
    val success: Boolean         = false,
    val isSaved: Boolean         = false,
    val isClone: Boolean         = false,
    val originalTitle: String    = "",
    val cloneNameError: Boolean  = false,
    val duplicateNameError: Boolean = false,
    val expiryDate: Long? = null,
    val userHasEdited: Boolean = false,
    val passwordChangedAt: Long? = null,
    val fieldLabel: String = "",
    val isProtected: Boolean = false,
    val originalPassword: String = "",
    val breachSaveState: BreachSaveState = BreachSaveState.Idle,
    val inheritedFrom: String? = null,
) {
    val url: String get() = urls.firstOrNull() ?: ""
}

@HiltViewModel
class AddEditEntryViewModel @Inject constructor(
    private val repository:       VaultRepository,
    private val tagRepository:    TagRepository,
    private val securityPrefs:    SecurityPrefs,
    private val planPrefs: PlanPrefs,
    private val authRepository:   AuthRepository,
    val biometricPrefs:           BiometricPrefs,
    private val twoFactorRepository: TwoFactorRepository,
    private val passkeyRepository:   PasskeyRepository,
    private val hibpChecker:          HibpPasswordChecker,
    private val activeVaultPrefs: ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val pinnedHubPrefs: PinnedHubPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId:    String  = savedStateHandle["id"]        ?: "new"
    private val cloneOf:    String  = savedStateHandle["cloneOf"]   ?: ""
    private val prefillUrl: String  = savedStateHandle["prefillUrl"] ?: ""

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    private val _uiState = MutableStateFlow(AddEditEntryUiState())
    val uiState: StateFlow<AddEditEntryUiState> = _uiState

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

    private val _hibpState = MutableStateFlow<HibpCheckState>(HibpCheckState.Idle)
    val hibpState: StateFlow<HibpCheckState> = _hibpState.asStateFlow()

    val isPasswordDuplicated: StateFlow<Boolean> = combine(
        _uiState,
        repository.observeEntries()
    ) { state, entries ->
        val pwd = state.password
        if (pwd.isBlank()) false
        else entries.count { it.password == pwd && it.id != state.id } > 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val linkedTwoFactor: StateFlow<TwoFactorAccount?> =
        _uiState
            .flatMapLatest { s ->
                if (s.isEditing) twoFactorRepository.observeLinkedToPassword(s.id)
                else flowOf(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val linkedPasskey: StateFlow<PasskeyCredential?> =
        _uiState
            .flatMapLatest { s ->
                if (s.isEditing) passkeyRepository.observeLinkedToPassword(s.id)
                else flowOf(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val availablePasskeys: StateFlow<List<PasskeyCredential>> =
        passkeyRepository.observePasskeys()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val availableTwoFactorAccounts: StateFlow<List<TwoFactorAccount>> =
        twoFactorRepository.observeAccounts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        savedStateHandle.get<String>("generated_password")?.takeIf { it.isNotEmpty() }?.let {
            _uiState.value = _uiState.value.copy(password = it)
        }
        viewModelScope.launch {
            savedStateHandle.getStateFlow("generated_password", "").collect { pwd ->
                if (pwd.isNotEmpty()) _uiState.value = _uiState.value.copy(password = pwd)
            }
        }

        when {
            cloneOf.isNotEmpty() -> viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val source = repository.getEntry(cloneOf)
                if (source != null) {
                    _uiState.value = _uiState.value.copy(
                        id           = UUID.randomUUID().toString(),
                        title        = source.title,
                        username     = source.username,
                        email        = source.email,
                        password     = source.password,
                        urls         = source.urls,
                        notes        = source.notes,
                        category     = source.category,
                        isFavorite   = false,
                        isTravelSafe = source.isTravelSafe,
                        tags         = source.tags,
                        isEditing    = false,
                        isLoading    = false,
                        isClone      = true,
                        originalTitle = source.title
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
            entryId != "new" -> viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.observeById(entryId)
                    .collect { entry ->
                        val current = _uiState.value
                        if (current.isSaved || current.success || current.userHasEdited) return@collect
                        if (entry != null) {
                            val loadedEmail = entry.email.trim()
                            val preExistingEmailError = loadedEmail.isNotEmpty() &&
                                !Patterns.EMAIL_ADDRESS.matcher(loadedEmail).matches()
                            _uiState.value = current.copy(
                                id           = entry.id,
                                title        = entry.title,
                                username     = entry.username,
                                email        = entry.email,
                                emailError   = preExistingEmailError,
                                password     = entry.password,
                                urls         = entry.urls,
                                notes        = entry.notes,
                                category     = entry.category,
                                isFavorite   = entry.isFavorite,
                                isTravelSafe = entry.isTravelSafe,
                                isPinned     = entry.isPinned,
                                isHidden     = entry.isHidden,
                                tags         = entry.tags,
                                createdAt    = entry.createdAt,
                                updatedAt         = entry.updatedAt,
                                passwordChangedAt = entry.passwordChangedAt,
                                fieldLabel        = entry.fieldLabel,
                                isProtected       = entry.isProtected,
                                originalPassword  = entry.password,
                                isEditing         = true,
                                isLoading    = false,
                                expiryDate   = entry.expiryDate,
                                inheritedFrom = entry.inheritedFrom
                            )
                        } else {
                            _uiState.value = current.copy(isLoading = false)
                        }
                    }
            }
            prefillUrl.isNotEmpty() -> {
                _uiState.value = _uiState.value.copy(urls = listOf(normalizeUrl(prefillUrl)))
            }
        }
    }

    fun onTitleChange(v: String)    { _uiState.value = _uiState.value.copy(title    = v, titleError = false, cloneNameError = false, userHasEdited = true) }
    fun onUsernameChange(v: String) { _uiState.value = _uiState.value.copy(username = v, userHasEdited = true) }
    fun onEmailChange(v: String)    { _uiState.value = _uiState.value.copy(email = v, emailError = false, userHasEdited = true) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v, userHasEdited = true) }
    fun onUrlBeingAddedChange(v: String) {
        _uiState.value = _uiState.value.copy(urlBeingAdded = v, urlBeingAddedError = false, userHasEdited = true)
    }
    fun confirmUrlBeingAdded() {
        val raw = _uiState.value.urlBeingAdded.trim()
        if (raw.isEmpty()) { _uiState.value = _uiState.value.copy(urlBeingAdded = ""); return }
        val normalized = normalizeUrl(raw)
        _uiState.value = _uiState.value.copy(
            urls          = _uiState.value.urls + normalized,
            urlBeingAdded = "",
            userHasEdited = true
        )
    }

    fun removeUrl(index: Int) {
        val updated = _uiState.value.urls.toMutableList().also { it.removeAt(index) }
        _uiState.value = _uiState.value.copy(urls = updated, userHasEdited = true)
    }
    private fun normalizeUrl(raw: String): String =
        raw.removePrefix("https://").removePrefix("http://").trimEnd('/')
    fun onNotesChange(v: String)    {
        if (v.length <= 300) _uiState.value = _uiState.value.copy(notes = v, userHasEdited = true)
    }

    fun onTravelSafeChange(v: Boolean) { _uiState.value = _uiState.value.copy(isTravelSafe = v, userHasEdited = true) }
    fun onExpiryDateChange(dateMs: Long?) {
        _uiState.value = _uiState.value.copy(expiryDate = dateMs, userHasEdited = true)
    }

    fun saveTravelSafeOnly(v: Boolean) {
        _uiState.value = _uiState.value.copy(isTravelSafe = v)
        val s = _uiState.value
        if (s.isEditing) viewModelScope.launch { repository.setTravelSafe(s.id, v) }
    }

    fun saveHiddenOnly(v: Boolean) {
        _uiState.value = _uiState.value.copy(isHidden = v)
        val s = _uiState.value
        if (s.isEditing) viewModelScope.launch { repository.setHidden(s.id, v) }
    }

    fun returnToViewMode() {
        _uiState.value = _uiState.value.copy(isSaved = false, userHasEdited = false)
    }

    fun toggleFavorite() { _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite) }

    val canProtectNew: Boolean get() = planPrefs.effectivePlan() != UserPlan.FREE

    fun setProtected(value: Boolean) {
        val s = _uiState.value
        if (!s.isEditing) return
        if (value && !canProtectNew) return
        viewModelScope.launch {
            repository.setProtected(s.id, value)
            _uiState.value = _uiState.value.copy(isProtected = value)
        }
    }

    fun togglePin() {
        val s = _uiState.value
        if (!s.isEditing) return
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
            val ok = pinnedHubPrefs.pin(entryId, HubEntryType.PASSWORD, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }




    fun delete() {
        viewModelScope.launch {
            val id = _uiState.value.id
            if (_uiState.value.isEditing) {
                if (!repository.softDelete(id)) return@launch
                pinnedHubPrefs.unpin(id)
            }
            _uiState.value = _uiState.value.copy(success = true)
        }
    }

    fun archive() {
        viewModelScope.launch {
            val id = _uiState.value.id
            if (_uiState.value.isEditing) { repository.archive(id); pinnedHubPrefs.unpin(id) }
            _uiState.value = _uiState.value.copy(success = true)
        }
    }



    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String) {
        viewModelScope.launch { tagRepository.addTag(name) }
    }

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

    fun deleteTag(id: String) {
        viewModelScope.launch { tagRepository.deleteTag(id) }
    }

    fun saveMetaOnly(category: EntryCategory, tags: List<String>) {
        _uiState.value = _uiState.value.copy(category = category, tags = tags)
        val state = _uiState.value
        if (state.isEditing) {
            viewModelScope.launch {
                repository.saveEntry(
                    VaultEntry(
                        id           = state.id,
                        title        = state.title.trim(),
                        username     = state.username.trim(),
                        email        = state.email.trim(),
                        password     = state.password,
                        urls         = state.urls,
                        notes        = state.notes.trim(),
                        category     = category,
                        isFavorite   = state.isFavorite,
                        isTravelSafe = state.isTravelSafe,
                        isPinned     = state.isPinned,
                        isHidden     = state.isHidden,
                        tags         = tags,
                        createdAt    = state.createdAt,
                        updatedAt    = System.currentTimeMillis(),
                        isDeleted    = false,
                        deletedAt    = null,
                        expiryDate        = state.expiryDate,
                        vaultId           = activeVaultPrefs.activeVaultId,
                        passwordChangedAt = state.passwordChangedAt,
                        fieldLabel        = state.fieldLabel,
                        isProtected       = state.isProtected,
                    ), soloMetadatos = true
                )
            }
        }
    }

    fun dismissDuplicateNameError() {
        _uiState.value = _uiState.value.copy(duplicateNameError = false)
    }

    fun save() {
        val state = _uiState.value
        var hasError = false

        if (state.isClone && state.title.trim() == state.originalTitle.trim()) {
            _uiState.value = _uiState.value.copy(cloneNameError = true, titleError = true)
            return
        }

        if (state.title.isBlank()) {
            _uiState.value = _uiState.value.copy(titleError = true)
            hasError = true
        }

        val emailTrimmed = state.email.trim()
        if (emailTrimmed.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
            _uiState.value = _uiState.value.copy(emailError = true)
            hasError = true
        }

        if (hasError) return

        _uiState.value = _uiState.value.copy(isLoading = true, breachSaveState = BreachSaveState.Checking)
        viewModelScope.launch {
            val titleTrimmed = state.title.trim()
            val duplicate = repository.observeEntries().first()
                .any { it.id != state.id && it.title.trim().lowercase(Locale.ROOT) == titleTrimmed.lowercase(Locale.ROOT) }
            if (duplicate) {
                _uiState.value = _uiState.value.copy(isLoading = false, duplicateNameError = true, breachSaveState = BreachSaveState.Idle)
                return@launch
            }

            val passwordChanged = !state.isEditing || state.password != state.originalPassword
            if (state.password.isNotBlank() && passwordChanged) {
                when (val result = hibpChecker.check(state.password)) {
                    is HibpResult.Pwned -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading      = false,
                            breachSaveState = BreachSaveState.PwnedPendingConfirm(result.count)
                        )
                        return@launch
                    }
                    HibpResult.Clean -> {
                        persistEntry(state, breachClean = true)
                        return@launch
                    }
                    HibpResult.RateLimited,
                    HibpResult.NetworkError -> {
                    }
                }
            }

            persistEntry(state)
        }
    }

    fun saveAnyway() {
        _uiState.value = _uiState.value.copy(breachSaveState = BreachSaveState.Idle, isLoading = true)
        viewModelScope.launch { persistEntry(_uiState.value) }
    }

    fun dismissBreachDialog() {
        _uiState.value = _uiState.value.copy(breachSaveState = BreachSaveState.Idle, isLoading = false)
    }

    private suspend fun persistEntry(state: AddEditEntryUiState, breachClean: Boolean = false) {
        val finalUrls = if (state.urlBeingAdded.isNotBlank())
            state.urls + normalizeUrl(state.urlBeingAdded.trim())
        else
            state.urls

        val passwordActuallyChanged = !state.isEditing || state.password != state.originalPassword
        val newPasswordChangedAt = if (passwordActuallyChanged) System.currentTimeMillis()
                                   else state.passwordChangedAt

        repository.saveEntry(
            VaultEntry(
                id                = state.id,
                title             = state.title.trim(),
                username          = state.username.trim(),
                email             = state.email.trim(),
                password          = state.password,
                urls              = finalUrls,
                notes             = state.notes.trim(),
                category          = state.category,
                isFavorite        = state.isFavorite,
                isTravelSafe      = state.isTravelSafe,
                isPinned          = state.isPinned,
                isHidden          = state.isHidden,
                tags              = state.tags,
                createdAt         = state.createdAt,
                updatedAt         = System.currentTimeMillis(),
                isDeleted         = false,
                deletedAt         = null,
                expiryDate        = state.expiryDate,
                vaultId           = activeVaultPrefs.activeVaultId,
                passwordChangedAt = newPasswordChangedAt,
                fieldLabel        = state.fieldLabel,
                isProtected       = state.isProtected,
            )
        )
        securityPrefs.updateLastReview()
        _uiState.value = _uiState.value.copy(
            isLoading       = false,
            isSaved         = true,
            breachSaveState = if (breachClean) BreachSaveState.Clean else BreachSaveState.Idle,
            urls            = finalUrls,
            urlBeingAdded   = "",
            notes           = state.notes.trim(),
            updatedAt       = System.currentTimeMillis()
        )
    }

    fun checkHibp(password: String) {
        if (password.isBlank()) return
        if (_hibpState.value is HibpCheckState.Checking) return
        _hibpState.value = HibpCheckState.Checking
        viewModelScope.launch {
            _hibpState.value = when (val result = hibpChecker.check(password)) {
                is HibpResult.Pwned      -> HibpCheckState.Pwned(result.count)
                HibpResult.Clean         -> HibpCheckState.NotPwned
                HibpResult.RateLimited   -> HibpCheckState.NetworkError
                HibpResult.NetworkError  -> HibpCheckState.NetworkError
            }
        }
    }

    fun linkTwoFactor(twoFactorId: String) {
        val id = _uiState.value.id
        if (id.isBlank()) return
        viewModelScope.launch {
            twoFactorRepository.linkToPassword(twoFactorId, id)
        }
    }

    fun unlinkTwoFactor() {
        val id = _uiState.value.id
        if (id.isBlank()) return
        viewModelScope.launch {
            twoFactorRepository.unlinkFromPassword(id)
        }
    }

    fun linkPasskey(credentialId: String) {
        val id = _uiState.value.id
        if (id.isBlank()) return
        viewModelScope.launch {
            passkeyRepository.linkToPassword(credentialId, id)
        }
    }

    fun unlinkPasskey() {
        val id = _uiState.value.id
        if (id.isBlank()) return
        viewModelScope.launch {
            passkeyRepository.unlinkFromPassword(id)
        }
    }

    fun passwordStrength(password: String): Int =
        PasswordStrengthCalculator.calculate(password).coerceAtLeast(0)

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveEntryTo(targetVaultId: String) {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.moveEntriesToVault(setOf(id), targetVaultId) }
    }

    fun removeInheritedFrom() {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.removeInheritedFrom(id) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(id), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
}
