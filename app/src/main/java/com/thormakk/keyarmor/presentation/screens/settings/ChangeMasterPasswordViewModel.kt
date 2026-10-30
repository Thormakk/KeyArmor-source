package com.thormakk.keyarmor.presentation.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangeMasterPasswordUiState(
    val isLoading:          Boolean = false,
    val isSuccess:          Boolean = false,
    val errorCurrent:       String? = null,
    val errorNew:           String? = null,
    val errorConfirm:       String? = null,
    val isVerifyingCurrent: Boolean = false
)

@HiltViewModel
class ChangeMasterPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangeMasterPasswordUiState())
    val uiState: StateFlow<ChangeMasterPasswordUiState> = _uiState

    fun onSubmit(currentPassword: String, newPassword: String, confirmPassword: String) {
        var errorCurrent: String? = null
        var errorNew:     String? = null
        var errorConfirm: String? = null

        if (currentPassword.isBlank()) errorCurrent = ERROR_EMPTY
        if (newPassword.length < MIN_LENGTH) errorNew = ERROR_MIN_LENGTH
        if (newPassword == currentPassword && newPassword.isNotBlank()) errorNew = ERROR_SAME
        if (confirmPassword != newPassword) errorConfirm = ERROR_MISMATCH

        if (errorCurrent != null || errorNew != null || errorConfirm != null) {
            _uiState.value = _uiState.value.copy(
                errorCurrent = errorCurrent,
                errorNew     = errorNew,
                errorConfirm = errorConfirm
            )
            return
        }

        _uiState.value = ChangeMasterPasswordUiState(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.changeMasterPassword(currentPassword, newPassword)
            result.fold(
                onSuccess = {
                    _uiState.value = ChangeMasterPasswordUiState(isSuccess = true)
                },
                onFailure = { e ->
                    if (e is AuthRepository.WrongPasswordException) {
                        _uiState.value = ChangeMasterPasswordUiState(
                            errorCurrent = ERROR_WRONG_PASSWORD
                        )
                    } else if (e is AuthRepository.SnapshotFailedException) {
                        Log.e("ChangeMasterPassword", "Snapshot previo fallido: cambio abortado", e)
                        _uiState.value = ChangeMasterPasswordUiState(
                            errorCurrent = ERROR_SNAPSHOT_FAILED
                        )
                    } else {
                        Log.e("ChangeMasterPassword", "Fallo no reconocido al cambiar la contraseña maestra", e)
                        _uiState.value = ChangeMasterPasswordUiState(
                            errorCurrent = ERROR_GENERIC
                        )
                    }
                }
            )
        }
    }

    fun verifyCurrentPassword(password: String) {
        if (password.isBlank() || _uiState.value.isLoading || _uiState.value.isVerifyingCurrent) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingCurrent = true)
            val isValid: Boolean? = try {
                authRepository.verifyPassword(password)
            } catch (e: Exception) {
                Log.e("ChangeMasterPassword", "Fallo al verificar la contraseña actual en vivo", e)
                null
            }
            _uiState.value = _uiState.value.copy(
                isVerifyingCurrent = false,
                errorCurrent        = if (isValid == false) ERROR_WRONG_PASSWORD else null
            )
        }
    }

    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            errorCurrent = null,
            errorNew     = null,
            errorConfirm = null
        )
    }

    companion object {
        const val MIN_LENGTH           = 8
        const val ERROR_EMPTY         = "empty"
        const val ERROR_MIN_LENGTH    = "too_short"
        const val ERROR_SAME          = "same_as_current"
        const val ERROR_MISMATCH      = "mismatch"
        const val ERROR_WRONG_PASSWORD = "wrong_password"
        const val ERROR_GENERIC        = "generic_error"
        const val ERROR_SNAPSHOT_FAILED = "snapshot_failed"
    }
}
