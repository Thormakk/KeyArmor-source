package com.thormakk.keyarmor.presentation.screens.vaultmanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.usecase.DeleteVaultUseCase
import com.thormakk.keyarmor.domain.usecase.GetVaultEntryCountUseCase
import com.thormakk.keyarmor.domain.usecase.IsVaultEmptyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class VaultManagementUiState(
    val vaults:             List<VaultMetadata> = emptyList(),
    val isPremium:          Boolean             = false,
    val canCreateVault:     Boolean             = false,
    val pendingDeleteVault: VaultMetadata?      = null,
    val pendingDeleteVaultIsEmpty: Boolean        = false,
    val pendingRenameVault: VaultMetadata?      = null,
    val isLoading:          Boolean             = false,
    val protectedKeptCount: Int                 = 0,
    val entryCounts:        Map<String, Int>    = emptyMap(),
    val activeVaultId:      String              = VaultMetadata.PRIMARY_VAULT_ID,
) {
    companion object {
        const val MAX_OWN_VAULTS_FREE    = 1
        const val MAX_OWN_VAULTS_PREMIUM = 5
    }
}

@HiltViewModel
class VaultManagementViewModel @Inject constructor(
    private val vaultMetadataRepository: VaultMetadataRepository,
    private val deleteVaultUseCase:      DeleteVaultUseCase,
    private val isVaultEmptyUseCase:    IsVaultEmptyUseCase,
    private val getVaultEntryCountUseCase: GetVaultEntryCountUseCase,
    private val activeVaultPrefs:        ActiveVaultPrefs,
    private val planPrefs:               PlanPrefs,
    private val inheritancePrefs:        InheritancePrefs,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultManagementUiState())
    val uiState: StateFlow<VaultManagementUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                vaultMetadataRepository.observeAll(),
                planPrefs.planFlow
            ) { vaults, plan -> vaults to plan }
                .collect { (vaults, plan) ->
                    val isPremium = plan != UserPlan.FREE
                    val ownVaultCount = vaults.count { !it.isInherited && !it.isPrivate }
                    val maxOwn = if (isPremium) VaultManagementUiState.MAX_OWN_VAULTS_PREMIUM
                                 else VaultManagementUiState.MAX_OWN_VAULTS_FREE
                    _uiState.value = _uiState.value.copy(
                        vaults         = vaults,
                        isPremium      = isPremium,
                        canCreateVault = isPremium && ownVaultCount < maxOwn,
                    )
                    val counts = vaults.associate { it.id to getVaultEntryCountUseCase(it.id) }
                    _uiState.value = _uiState.value.copy(entryCounts = counts)
                }
        }

        viewModelScope.launch {
            activeVaultPrefs.activeVaultIdFlow.collect { id ->
                _uiState.value = _uiState.value.copy(activeVaultId = id)
            }
        }
    }

    fun setActiveVault(vaultId: String) {
        val isInherited = _uiState.value.vaults.find { it.id == vaultId }?.isInherited == true
        activeVaultPrefs.setActiveVault(vaultId, isInherited)
    }

    fun createVault(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (!_uiState.value.canCreateVault) return
        viewModelScope.launch {
            val sortOrder = _uiState.value.vaults.count { !it.isInherited && !it.isPrivate }
            vaultMetadataRepository.save(
                VaultMetadata(
                    id        = UUID.randomUUID().toString(),
                    name      = trimmed,
                    sortOrder = sortOrder,
                )
            )
        }
    }

    private fun isSystemVault(vault: VaultMetadata): Boolean =
        vault.id == VaultMetadata.PRIMARY_VAULT_ID || vault.isPrivate

    fun startRename(vault: VaultMetadata) {
        if (isSystemVault(vault)) return
        _uiState.value = _uiState.value.copy(pendingRenameVault = vault)
    }

    fun confirmRename(newName: String) {
        val vault = _uiState.value.pendingRenameVault ?: return
        val trimmed = newName.trim()
        if (trimmed.isBlank()) { cancelRename(); return }
        viewModelScope.launch {
            vaultMetadataRepository.save(vault.copy(name = trimmed))
        }
        _uiState.value = _uiState.value.copy(pendingRenameVault = null)
    }

    fun cancelRename() {
        _uiState.value = _uiState.value.copy(pendingRenameVault = null)
    }

    fun startDelete(vault: VaultMetadata) {
        if (isSystemVault(vault)) return
        viewModelScope.launch {
            val isEmpty = isVaultEmptyUseCase(vault.id)
            _uiState.value = _uiState.value.copy(
                pendingDeleteVault        = vault,
                pendingDeleteVaultIsEmpty = isEmpty
            )
        }
    }

    fun confirmDelete() {
        val vault = _uiState.value.pendingDeleteVault ?: return
        if (isSystemVault(vault)) { cancelDelete(); return }
        _uiState.value = _uiState.value.copy(pendingDeleteVault = null, isLoading = true)
        viewModelScope.launch {
            val inheritedFrom = if (vault.isInherited) vault.ownerEmail.ifBlank { null } else null
            deleteVaultUseCase(vault.id, inheritedFrom, sendToTrash = false)
            if (activeVaultPrefs.activeVaultId == vault.id) {
                activeVaultPrefs.setActiveVault(VaultMetadata.PRIMARY_VAULT_ID)
            }
            if (vault.isInherited && vault.ownerEmail.isNotBlank()) {
                inheritancePrefs.markHeirVaultDismissed(vault.ownerEmail)
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun confirmDeleteTrash() {
        val vault = _uiState.value.pendingDeleteVault ?: return
        if (isSystemVault(vault)) { cancelDelete(); return }
        _uiState.value = _uiState.value.copy(pendingDeleteVault = null, isLoading = true)
        viewModelScope.launch {
            val inheritedFrom = if (vault.isInherited) vault.ownerEmail.ifBlank { null } else null
            val protegidas = deleteVaultUseCase(vault.id, inheritedFrom, sendToTrash = true)
            if (activeVaultPrefs.activeVaultId == vault.id) {
                activeVaultPrefs.setActiveVault(VaultMetadata.PRIMARY_VAULT_ID)
            }
            if (vault.isInherited && vault.ownerEmail.isNotBlank()) {
                inheritancePrefs.markHeirVaultDismissed(vault.ownerEmail)
            }
            _uiState.value = _uiState.value.copy(isLoading = false, protectedKeptCount = protegidas)
        }
    }

    fun dismissProtectedKept() {
        _uiState.value = _uiState.value.copy(protectedKeptCount = 0)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(pendingDeleteVault = null, pendingDeleteVaultIsEmpty = false)
    }
}
