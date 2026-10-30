package com.thormakk.keyarmor.presentation.screens.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.security.BiometricCryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

data class BiometricSetupUiState(
    val showDialog: Boolean    = false,
    val setupPending: Boolean  = false
)

@HiltViewModel
class BiometricSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val biometricCrypto: BiometricCryptoManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BiometricSetupUiState())
    val uiState: StateFlow<BiometricSetupUiState> = _uiState

    init {
        viewModelScope.launch {
            val shouldShow = isBiometricAvailable()
                    && !authRepository.isBiometricKeyStored()
                    && !authRepository.isBiometricSetupSeen()
            _uiState.value = _uiState.value.copy(showDialog = shouldShow)
        }
    }

    fun getSetupCipher(): Cipher? =
        try { biometricCrypto.getEncryptionCipher() } catch (e: Exception) { null }

    fun onSetupSuccess(cipher: Cipher) {
        val masterKeyBytes = authRepository.getMasterKeyBytes() ?: run { skip(); return }
        viewModelScope.launch {
            try {
                val encrypted = biometricCrypto.encryptMasterKey(cipher, masterKeyBytes)
                masterKeyBytes.fill(0)
                authRepository.setBiometricKey(encrypted)
                authRepository.markBiometricSetupSeen()
                _uiState.value = _uiState.value.copy(showDialog = false, setupPending = false)
            } catch (e: Exception) {
                masterKeyBytes.fill(0)
                skip()
            }
        }
    }

    fun skip() {
        viewModelScope.launch {
            authRepository.markBiometricSetupSeen()
            _uiState.value = _uiState.value.copy(showDialog = false, setupPending = false)
        }
    }

    fun requestActivation() {
        _uiState.value = _uiState.value.copy(setupPending = true)
    }

    private fun isBiometricAvailable(): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
}
