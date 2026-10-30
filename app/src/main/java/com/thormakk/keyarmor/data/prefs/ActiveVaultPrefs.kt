package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.thormakk.keyarmor.domain.model.VaultMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveVaultPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME                    = "active_vault_prefs"
        private const val KEY_ACTIVE_VAULT              = "active_vault_id"
        private const val KEY_ACTIVE_VAULT_INHERITED    = "active_vault_is_inherited"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _activeVaultIdFlow = MutableStateFlow(
        prefs.getString(KEY_ACTIVE_VAULT, VaultMetadata.PRIMARY_VAULT_ID)
            ?: VaultMetadata.PRIMARY_VAULT_ID
    )

    private val _activeVaultIsInheritedFlow = MutableStateFlow(
        prefs.getBoolean(KEY_ACTIVE_VAULT_INHERITED, false)
    )

    val activeVaultIdFlow: StateFlow<String> = _activeVaultIdFlow

    val activeVaultIsInheritedFlow: StateFlow<Boolean> = _activeVaultIsInheritedFlow

    val activeVaultId: String get() = _activeVaultIdFlow.value

    val activeVaultIsInherited: Boolean get() = _activeVaultIsInheritedFlow.value

    fun setActiveVault(vaultId: String, isInherited: Boolean = false) {
        prefs.edit {
            putString(KEY_ACTIVE_VAULT, vaultId)
            putBoolean(KEY_ACTIVE_VAULT_INHERITED, isInherited)
        }
        _activeVaultIdFlow.value          = vaultId
        _activeVaultIsInheritedFlow.value = isInherited
    }

    fun resetToPrimary() = setActiveVault(VaultMetadata.PRIMARY_VAULT_ID, isInherited = false)
}
