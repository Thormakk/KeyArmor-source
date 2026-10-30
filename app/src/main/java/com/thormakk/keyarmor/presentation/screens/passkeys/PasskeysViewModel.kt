package com.thormakk.keyarmor.presentation.screens.passkeys

import com.thormakk.keyarmor.security.OrdenEnSesion
import android.content.Context
import android.os.Build
import android.view.autofill.AutofillManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.screens.passkeys.PasskeysViewModel.Companion.PREF_PROVIDER_ACTIVE
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PasskeySortOrder { NEWEST, OLDEST, A_Z, Z_A }

data class PasskeysUiState(
    val passkeys:       List<PasskeyCredential>      = emptyList(),
    val sortOrder:      PasskeySortOrder             = PasskeySortOrder.NEWEST,
    val isLoading:      Boolean                      = true,
    val snackbar:       String?                      = null,
    val hasDriveBackup: Boolean                      = false
) {
    val sorted: List<PasskeyCredential> get() {
        val base = when (sortOrder) {
            PasskeySortOrder.NEWEST -> passkeys.sortedByDescending { it.createdAt }
            PasskeySortOrder.OLDEST -> passkeys.sortedBy { it.createdAt }
            PasskeySortOrder.A_Z    -> passkeys.sortedBy { it.displayName.lowercase() }
            PasskeySortOrder.Z_A    -> passkeys.sortedByDescending { it.displayName.lowercase() }
        }
        return base.sortedByDescending { it.isPinned }
    }
}

@HiltViewModel
class PasskeysViewModel @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val passkeyRepository: PasskeyRepository,
    planPrefs:         PlanPrefs,
    private val activeVaultPrefs:  ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
) : ViewModel() {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    private val _uiState = MutableStateFlow(PasskeysUiState(sortOrder = OrdenEnSesion.leer("passkeys", PasskeySortOrder.NEWEST)))
    val uiState: StateFlow<PasskeysUiState> = _uiState


    private val autofillManager: AutofillManager? =
        context.getSystemService(AutofillManager::class.java)

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

    val hasDriveBackup: StateFlow<Boolean> = planPrefs.planFlow
        .map { it != UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() != UserPlan.FREE)

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    private val _isProviderEnabled = MutableStateFlow(true)
    val isProviderEnabled: StateFlow<Boolean> = _isProviderEnabled.asStateFlow()

    fun checkProviderEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val autofillSaysEnabled = autofillManager?.hasEnabledAutofillServices() == true
        val prefSaysEnabled =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(PREF_PROVIDER_ACTIVE, false)
        _isProviderEnabled.value = autofillSaysEnabled || prefSaysEnabled
    }

    init {
        checkProviderEnabled()
        viewModelScope.launch {
            passkeyRepository.observePasskeys()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, snackbar = e.message ?: context.getString(R.string.error_unknown)) }
                }
                .collect { list ->
                    _uiState.update { it.copy(passkeys = list, isLoading = false) }
                }
        }
    }

    fun setSortOrder(order: PasskeySortOrder) {
        OrdenEnSesion.guardar("passkeys", order)
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun archive(credentialId: String) {
        viewModelScope.launch {
            try { passkeyRepository.archive(credentialId) }
            catch (e: Exception) { _uiState.update { it.copy(snackbar = e.message ?: context.getString(R.string.error_unknown)) } }
        }
    }

    fun togglePin(credentialId: String, isPinned: Boolean) {
        viewModelScope.launch {
            if (!isPinned && passkeyRepository.countPinned() >= MAX_PINNED) {
                _uiState.update { it.copy(snackbar = context.getString(R.string.passkey_pin_limit_toast, MAX_PINNED)) }
                return@launch
            }
            passkeyRepository.togglePin(credentialId, !isPinned)
        }
    }

    fun delete(credentialId: String) {
        viewModelScope.launch {
            try {
                passkeyRepository.delete(credentialId)
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbar = e.message ?: context.getString(R.string.error_unknown)) }
            }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbar = null) }
    }


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveEntryTo(entryId: String, targetVaultId: String) {
        viewModelScope.launch { passkeyRepository.moveEntriesToVault(setOf(entryId), targetVaultId) }
    }

    fun copyEntryTo(entryId: String, targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { passkeyRepository.copyEntriesToVault(setOf(entryId), targetVaultId, inheritedFrom) }
    }

    companion object {
        const val PREFS_NAME = "keyarmor_passkey_setup"
        const val PREF_PROVIDER_ACTIVE = "provider_active"
        private const val MAX_PINNED = 3
    }
}
