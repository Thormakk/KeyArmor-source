package com.thormakk.keyarmor.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.AppUpdatePrefs
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.presentation.UiText
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val name:            String  = "",
    val password:        String  = "",
    val confirmPassword: String  = "",
    val isLoading:       Boolean = false,
    val error:           UiText? = null,
    val success:         Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository:  AuthRepository,
    private val appUpdatePrefs:  AppUpdatePrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, error = null)
    }

    fun register() {
        val state = _uiState.value
        when {
            state.name.isBlank() ->
                _uiState.value = state.copy(error = UiText.StringResource(R.string.error_name_empty))
            state.password.length < MIN_LENGTH ->
                _uiState.value = state.copy(error = UiText.StringResource(R.string.error_password_too_short))
            state.password != state.confirmPassword ->
                _uiState.value = state.copy(error = UiText.StringResource(R.string.error_passwords_mismatch))
            else -> {
                _uiState.value = state.copy(isLoading = true, error = null)
                viewModelScope.launch {
                    try {
                        authRepository.register(state.name.trim(), state.password)
                        appUpdatePrefs.initForNewInstall()

                        _uiState.value = _uiState.value.copy(
                            isLoading       = false,
                            success         = true,
                            password        = "",
                            confirmPassword = ""
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error     = UiText.StringResource(R.string.error_register_failed)
                        )
                    }
                }
            }
        }
    }

    fun passwordStrength(password: String): Int =
        PasswordStrengthCalculator.calculate(password).coerceAtLeast(0)

    companion object {
        const val MIN_LENGTH = 8
    }
}
