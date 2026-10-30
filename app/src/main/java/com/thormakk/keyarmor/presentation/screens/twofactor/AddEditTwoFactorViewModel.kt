package com.thormakk.keyarmor.presentation.screens.twofactor

import androidx.core.net.toUri
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
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.RecoveryCode
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.TotpAlgorithm
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.util.TotpGenerator
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
import javax.inject.Inject

data class AddEditTwoFactorUiState(
    val id:               String           = "",
    val name:             String           = "",
    val issuer:           String           = "",
    val accountLabel:     String           = "",
    val url:              String           = "",
    val secret:           String           = "",
    val digits:           Int              = 6,
    val period:           Int              = 30,
    val algorithm:        TotpAlgorithm    = TotpAlgorithm.SHA1,
    val recoveryCodesText: String          = "",
    val secretError:       Boolean          = false,
    val totpLimitReached:  Boolean          = false,
    val isNew:             Boolean          = true,
    val isTravelSafe:      Boolean          = false,
    val isPinned:          Boolean          = false,
    val isHidden:          Boolean          = false,
    val isProtected:       Boolean          = false,
    val category:          EntryCategory    = EntryCategory.SIN_CATEGORIZAR,
    val tags:              List<String>     = emptyList(),
    val createdAt:         Long             = 0L,
    val updatedAt:         Long             = 0L,
    val inheritedFrom:     String?          = null
)

@HiltViewModel
class AddEditTwoFactorViewModel @Inject constructor(
    private val repository:      TwoFactorRepository,
    private val tagRepository:   TagRepository,
    private val planPrefs:       PlanPrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val pinnedHubPrefs:  PinnedHubPrefs,
    private val authRepository:  AuthRepository,
    val biometricPrefs:          BiometricPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    savedStateHandle:            SavedStateHandle
) : ViewModel() {

    private val argId: String = savedStateHandle["id"] ?: "new"

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    val isHubPinned: StateFlow<Boolean> =
        pinnedHubPrefs.pinnedRefsFlow
            .map { refs -> refs.any { it.id == argId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), pinnedHubPrefs.isPinned(argId))

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin() {
        if (argId == "new") return
        if (pinnedHubPrefs.isPinned(argId)) {
            pinnedHubPrefs.unpin(argId)
        } else {
            val ok = pinnedHubPrefs.pin(argId, HubEntryType.TWO_FACTOR, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    private val _uiState = MutableStateFlow(
        AddEditTwoFactorUiState(isNew = argId == "new")
    )
    val uiState: StateFlow<AddEditTwoFactorUiState> = _uiState.asStateFlow()

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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (argId != "new") {
            viewModelScope.launch {
                repository.observeAccountById(argId)
                    .collect { acc ->
                        if (acc != null) {
                            _uiState.value = AddEditTwoFactorUiState(
                                id              = acc.id,
                                name            = acc.name,
                                issuer          = acc.issuer,
                                accountLabel    = acc.accountLabel,
                                secret          = acc.secret,
                                digits          = acc.digits,
                                period          = acc.period,
                                algorithm       = acc.algorithm,
                                recoveryCodesText = acc.recoveryCodes.joinToString("\n") { rc ->
                                    if (rc.used) "${rc.code} ✓" else rc.code
                                },
                                secretError     = false,
                                isNew           = false,
                                url             = acc.url,
                                isTravelSafe    = acc.isTravelSafe,
                                isPinned        = acc.isPinned,
                                isHidden        = acc.isHidden,
                                isProtected     = acc.isProtected,
                                category        = acc.category,
                                tags            = acc.tags,
                                createdAt       = acc.createdAt,
                                updatedAt       = acc.updatedAt,
                                inheritedFrom   = acc.inheritedFrom
                            )
                        }
                    }
            }
        }
    }

    fun onQrScanned(uri: String) {
        try {
            val u = uri.toUri()
            if (u.scheme?.lowercase() != "otpauth") return

            val label = u.path?.removePrefix("/")?.let {
                android.net.Uri.decode(it)
            } ?: ""
            val (issuerFromLabel, account) = if (label.contains(":")) {
                label.substringBefore(":").trim() to label.substringAfter(":").trim()
            } else {
                "" to label.trim()
            }

            val secret    = u.getQueryParameter("secret") ?: return
            val issuer    = u.getQueryParameter("issuer")?.trim() ?: issuerFromLabel
            val digits    = u.getQueryParameter("digits")?.toIntOrNull() ?: 6
            val period    = u.getQueryParameter("period")?.toIntOrNull() ?: 30
            val algorithm = when (u.getQueryParameter("algorithm")?.uppercase()) {
                "SHA256" -> TotpAlgorithm.SHA256
                "SHA512" -> TotpAlgorithm.SHA512
                else     -> TotpAlgorithm.SHA1
            }
            val cleanSecret = secret.trim().replace(" ", "").uppercase()

            _uiState.value = _uiState.value.copy(
                name         = issuer.ifBlank { account },
                issuer       = issuer,
                accountLabel = account,
                secret       = cleanSecret,
                digits       = digits.coerceIn(6, 8),
                period       = period.coerceIn(15, 120),
                algorithm    = algorithm,
                secretError  = TotpGenerator.generate(cleanSecret) == null
            )
        } catch (_: Exception) {   }
    }

    fun onNameChange(v: String)         { _uiState.value = _uiState.value.copy(name = v) }
    fun onIssuerChange(v: String)       { _uiState.value = _uiState.value.copy(issuer = v) }
    fun onAccountLabelChange(v: String) { _uiState.value = _uiState.value.copy(accountLabel = v) }
    fun onUrlChange(v: String)          { _uiState.value = _uiState.value.copy(url = v) }
    fun onSecretChange(v: String) {
        _uiState.value = _uiState.value.copy(
            secret      = v,
            secretError = v.isNotBlank() && TotpGenerator.generate(v) == null
        )
    }
    fun onDigitsChange(v: Int)             { _uiState.value = _uiState.value.copy(digits = v.coerceIn(6, 8)) }
    fun onPeriodChange(v: Int)             { _uiState.value = _uiState.value.copy(period = v.coerceIn(15, 120)) }
    fun onAlgorithmChange(v: TotpAlgorithm){ _uiState.value = _uiState.value.copy(algorithm = v) }
    fun onTravelSafeChange(v: Boolean) { _uiState.value = _uiState.value.copy(isTravelSafe = v) }
    fun saveTravelSafeOnly(v: Boolean) {
        val s = _uiState.value.copy(isTravelSafe = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setTravelSafe(argId, v) }
    }

    fun saveHiddenOnly(v: Boolean) {
        val s = _uiState.value.copy(isHidden = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setHidden(argId, v) }
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

    fun onTotpLimitDismissed() { _uiState.value = _uiState.value.copy(totpLimitReached = false) }


    fun saveMetaOnly(category: EntryCategory, tags: List<String>) {
        _uiState.value = _uiState.value.copy(category = category, tags = tags)
        val s = _uiState.value
        if (!s.isNew) {
            viewModelScope.launch {
                val existing = repository.getAccount(argId) ?: return@launch
                repository.saveAccount(existing.copy(category = category, tags = tags, updatedAt = System.currentTimeMillis()), soloMetadatos = true)
            }
        }
    }


    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String)                   { viewModelScope.launch { tagRepository.addTag(name) } }
    fun renameTag(id: String, newName: String) { viewModelScope.launch { tagRepository.renameTag(id, newName) } }
    fun deleteTag(id: String)                  { viewModelScope.launch { tagRepository.deleteTag(id) } }

    fun save(onSaved: () -> Unit) {
        val s = _uiState.value
        if (s.isNew && s.secret.isBlank()) return
        if (s.secret.isNotBlank() && TotpGenerator.generate(s.secret) == null) {
            _uiState.value = s.copy(secretError = true)
            return
        }
        if (s.name.isBlank() && s.issuer.isBlank() && s.secret.isBlank() && s.recoveryCodesText.isBlank()) {
            return
        }
        viewModelScope.launch {
            val recoveryCodes = parseRecoveryCodes(s.recoveryCodesText)
            val existing = if (s.isNew) null else repository.getAccount(argId)

            val willHaveTotp = s.secret.isNotBlank()
            val hadTotp = existing?.secret?.isNotBlank() == true
            if (planPrefs.effectivePlan() == UserPlan.FREE && willHaveTotp && !hadTotp) {
                val currentTotpCount = repository.observeAccounts().first().count { it.hasTotp }
                if (currentTotpCount >= TWO_FACTOR_FREE_TOTP_LIMIT) {
                    _uiState.value = s.copy(totpLimitReached = true)
                    return@launch
                }
            }

            val account = TwoFactorAccount(
                id              = existing?.id ?: java.util.UUID.randomUUID().toString(),
                name            = s.name.trim(),
                issuer          = s.issuer.trim(),
                accountLabel    = s.accountLabel.trim(),
                secret          = s.secret.trim().replace(" ", "").uppercase(),
                digits          = s.digits,
                period          = s.period,
                algorithm       = s.algorithm,
                recoveryCodes   = recoveryCodes,
                category        = s.category,
                tags            = s.tags,
                isFavorite       = existing?.isFavorite ?: false,
                isPinned         = existing?.isPinned ?: s.isPinned,
                isTravelSafe     = s.isTravelSafe,
                isHidden         = s.isHidden,
                isProtected      = s.isProtected,
                createdAt       = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt       = System.currentTimeMillis(),
                isDeleted       = false,
                deletedAt       = null,
                url              = s.url.trim(),
                linkedPasswordId = existing?.linkedPasswordId ?: "",
                notes            = existing?.notes ?: "",
                vaultId          = activeVaultPrefs.activeVaultId,
            )
            repository.saveAccount(account)
            onSaved()
        }
    }


    fun delete(onDeleted: () -> Unit) {
        val s = _uiState.value
        if (s.isNew) { onDeleted(); return }
        viewModelScope.launch {
            if (!repository.softDelete(argId)) return@launch
            pinnedHubPrefs.unpin(argId)
            onDeleted()
        }
    }

    fun archive(onArchived: () -> Unit = {}) {
        val s = _uiState.value
        if (s.isNew) { onArchived(); return }
        viewModelScope.launch {
            repository.archive(argId)
            pinnedHubPrefs.unpin(argId)
            onArchived()
        }
    }

    private fun parseRecoveryCodes(text: String): List<RecoveryCode> =
        text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { line ->
                if (line.endsWith(" ✓")) RecoveryCode(code = line.removeSuffix(" ✓").trim(), used = true)
                else RecoveryCode(code = line, used = false)
            }


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeInheritedFrom() {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.removeInheritedFrom(id) }
    }

    fun moveEntryTo(targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(argId), targetVaultId) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(argId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
}
