package com.thormakk.keyarmor.presentation.screens.passkeys

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.VaultEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasskeyEditUiState(
    val isLoading:           Boolean = true,
    val credentialId:        String  = "",
    val rpId:                String  = "",
    val displayName:         String  = "",
    val isAccountLabelEditable: Boolean = false,
    val customAccountLabel:  String  = "",
    val notes:               String  = "",
    val linkedPasswordId:    String  = "",
    val isSaved:             Boolean = false,
    val snackbar:            String? = null
)

@HiltViewModel
class PasskeyEditViewModel @Inject constructor(
    savedStateHandle:       SavedStateHandle,
    private val repository: PasskeyRepository,
    vaultRepository:        VaultRepository,
    @ApplicationContext private val appContextInyectado: Context
) : ViewModel() {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)

    private val credentialId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(PasskeyEditUiState())
    val uiState: StateFlow<PasskeyEditUiState> = _uiState

    val vaultEntries: StateFlow<List<VaultEntry>> = vaultRepository.observeEntries()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            val pk = repository.getPasskey(credentialId)
            if (pk != null) {
                _uiState.value = PasskeyEditUiState(
                    isLoading              = false,
                    credentialId           = pk.credentialId,
                    rpId                   = pk.rpId,
                    displayName            = pk.displayName,
                    isAccountLabelEditable = pk.isAccountLabelEditable,
                    customAccountLabel     = pk.customAccountLabel,
                    notes                  = pk.notes,
                    linkedPasswordId       = pk.linkedPasswordId
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onCustomAccountLabelChange(v: String) {
        _uiState.update { it.copy(customAccountLabel = v) }
    }

    fun onNotesChange(v: String) {
        if (v.length <= 500) _uiState.update { it.copy(notes = v) }
    }

    fun onLinkedPasswordIdChange(v: String) {
        _uiState.update { it.copy(linkedPasswordId = v) }
    }

    fun save() {
        val s  = _uiState.value
        viewModelScope.launch {
            try {
                val pk = repository.getPasskey(credentialId) ?: return@launch
                val guardado = repository.saveMetadata(
                    pk.copy(
                        customAccountLabel = s.customAccountLabel,
                        notes              = s.notes,
                        linkedPasswordId   = s.linkedPasswordId
                    )
                )
                if (guardado) {
                    _uiState.update { it.copy(isSaved = true) }
                } else {
                    _uiState.update { it.copy(snackbar = appContext.getString(R.string.protect_locked_edit)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbar = appContext.getString(R.string.share_error_saving)) }
            }
        }
    }

    fun dismissSnackbar() { _uiState.update { it.copy(snackbar = null) } }
}
