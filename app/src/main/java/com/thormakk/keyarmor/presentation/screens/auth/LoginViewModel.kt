package com.thormakk.keyarmor.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.domain.model.ResultadoMaestra
import com.thormakk.keyarmor.presentation.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val password: String = "",
    val name: String = "",
    val connectedEmail: String? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val success: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val drivePrefs: DrivePrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                name           = authRepository.getName() ?: "",
                connectedEmail = drivePrefs.getConnectedEmail()
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login() {
        val state = _uiState.value
        if (state.password.isBlank()) {
            _uiState.value = state.copy(error = UiText.StringResource(R.string.error_master_password_empty))
            return
        }
        _uiState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val resultado = authRepository.unlockWithPassword(state.password)) {
                is ResultadoMaestra.Correcta -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, success = true, password = "")
                }
                is ResultadoMaestra.EnEspera -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = throttleError(resultado.faltanMs)
                    )
                }
                is ResultadoMaestra.Incorrecta -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.error_wrong_password)
                    )
                }
            }
        }
    }

    private fun throttleError(waitMs: Long): UiText {
        val minutes = ((waitMs + 59_999L) / 60_000L).toInt().coerceAtLeast(1)
        return if (minutes >= 60) {
            UiText.StringResource(R.string.error_login_throttled_hours, (minutes + 59) / 60)
        } else {
            UiText.StringResource(R.string.error_login_throttled_minutes, minutes)
        }
    }

}
