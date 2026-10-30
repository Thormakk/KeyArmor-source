package com.thormakk.keyarmor.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HiddenEntriesUiState(
    val isEnabled:         Boolean = false,
    val isSuccess:         Boolean = false,
    val showDisableDialog: Boolean = false,
    val errorNew:          String? = null,
    val errorConfirm:      String? = null
)

@HiltViewModel
class HiddenEntriesViewModel @Inject constructor(
    private val hiddenEntriesPrefs:    HiddenEntriesPrefs,
    private val authRepository:        AuthRepository,
    private val exportImportRepository: com.thormakk.keyarmor.data.repository.ExportImportRepository,
    planPrefs: PlanPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(HiddenEntriesUiState(isEnabled = hiddenEntriesPrefs.isEnabled()))
    val uiState: StateFlow<HiddenEntriesUiState> = _uiState

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    fun onSubmit(newPhrase: String, confirmPhrase: String) {
        var errorNew:     String? = null
        var errorConfirm: String? = null

        if (newPhrase.isBlank())                errorNew     = ERROR_EMPTY
        else if (newPhrase.length < MIN_LENGTH) errorNew     = ERROR_MIN_LENGTH
        if (confirmPhrase != newPhrase)         errorConfirm = ERROR_MISMATCH

        if (errorNew != null || errorConfirm != null) {
            _uiState.value = _uiState.value.copy(
                errorNew     = errorNew,
                errorConfirm = errorConfirm
            )
            return
        }

        hiddenEntriesPrefs.setPhrase(newPhrase)
        _uiState.value = HiddenEntriesUiState(isEnabled = true, isSuccess = true)
    }

    fun clearErrors() {
        _uiState.value = _uiState.value.copy(errorNew = null, errorConfirm = null)
    }

    fun onSuccessConsumed() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    fun onRequestDisable()  { _uiState.value = _uiState.value.copy(showDisableDialog = true) }
    fun onDismissDisable()  { _uiState.value = _uiState.value.copy(showDisableDialog = false) }

    fun onConfirmDisable() {
        viewModelScope.launch {
            exportImportRepository.unhideAllEntries()
            hiddenEntriesPrefs.clearPhrase()
            _uiState.value = HiddenEntriesUiState(isEnabled = false)
        }
    }

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    companion object {
        const val MIN_LENGTH           = 4
        const val ERROR_EMPTY          = "empty"
        const val ERROR_MIN_LENGTH     = "too_short"
        const val ERROR_MISMATCH       = "mismatch"
    }
}
