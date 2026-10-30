package com.thormakk.keyarmor.presentation.screens.auth

import android.content.Context
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.decoy.DecoyVaultManager
import com.thormakk.keyarmor.data.decoy.HideVaultManager
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.PanicMode
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.prefs.SecurityPrefs
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.WipeRepository
import com.thormakk.keyarmor.domain.model.ResultadoMaestra
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.UiText
import com.thormakk.keyarmor.security.BiometricCryptoManager
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

data class LockUiState(
    val password: String            = "",
    val name: String                = "",
    val connectedEmail: String?     = null,
    val isLoading: Boolean          = false,
    val error: UiText?              = null,
    val success: Boolean            = false,
    val biometricAvailable: Boolean = false,
    val biometricEnabled: Boolean   = false,
    val biometricSetupPending: Boolean = false,
    val retryBiometricUnlock: Boolean = false
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val biometricCrypto: BiometricCryptoManager,
    private val securityPrefs: SecurityPrefs,
    private val panicPinPrefs: PanicPinPrefs,
    private val wipeRepository: WipeRepository,
    private val drivePrefs: DrivePrefs,
    private val session: SessionManager,
    private val activeVaultPrefs: ActiveVaultPrefs,
    private val decoyVaultManager: DecoyVaultManager,
    private val hideVaultManager: HideVaultManager,
    private val inheritanceDisarmer: com.thormakk.keyarmor.data.repository.InheritanceDisarmer,
    private val inheritancePrefs:    com.thormakk.keyarmor.data.prefs.InheritancePrefs,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState

    val coercionActive: StateFlow<Boolean> = panicPinPrefs.coercionActiveFlow

    private var cachedBiometricKey: String? = null

    private var biometricAutoRetried = false

    init {
        purgarMaterialDeCoaccionViejo()
        viewModelScope.launch {
            val name               = authRepository.getName() ?: ""
            val biometricAvailable = isBiometricAvailable()
            val biometricKeyStored = biometricAvailable && authRepository.isBiometricKeyStored()

            if (biometricKeyStored) {
                cachedBiometricKey = authRepository.getBiometricKey()
            }

            _uiState.value = _uiState.value.copy(
                name               = name,
                connectedEmail     = drivePrefs.getConnectedEmail(),
                biometricAvailable = biometricAvailable,
                biometricEnabled   = biometricKeyStored
            )
        }
    }

    private fun purgarMaterialDeCoaccionViejo() {
        if (!panicPinPrefs.isCoercionMaterialStale()) return
        viewModelScope.launch {
            runCatching {
                decoyVaultManager.purgeDecoyContent()
                hideVaultManager.purgeHideContent()
            }.onSuccess {
                panicPinPrefs.consumeCoercionMaterialStale()
            }
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun unlock() {
        val state = _uiState.value
        if (state.password.isBlank()) {
            _uiState.value = state.copy(error = UiText.StringResource(R.string.error_master_password_empty))
            return
        }
        _uiState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val panicMatched = panicPinPrefs.verifyPin(state.password)

            val resultado = if (panicMatched) {
                ResultadoMaestra.Incorrecta
            } else {
                authRepository.unlockWithPassword(state.password)
            }
            if (resultado is ResultadoMaestra.EnEspera) {
                _uiState.value = state.copy(isLoading = false, error = throttleError(resultado.faltanMs))
                return@launch
            }
            val passwordOk = resultado is ResultadoMaestra.Correcta

            if (panicMatched) {
                when (panicPinPrefs.getPanicMode()) {
                    PanicMode.WIPE -> {
                        wipeRepository.wipeForPanic()
                        runCatching {
                            authRepository.clearBiometricKey()
                            biometricCrypto.deleteKey()
                        }
                        panicPinPrefs.activateWipeCoercion()
                    }
                    PanicMode.HIDE -> {
                        val hideKey = hideVaultManager.deriveOrCreateHideKey(state.password)
                        session.setSecretKey(hideKey)
                        activeVaultPrefs.setActiveVault(VaultMetadata.HIDE_MAIN_VAULT_ID)
                        panicPinPrefs.activateHideMode()
                    }
                    PanicMode.DECOY -> {
                        val decoyKey = decoyVaultManager.deriveDecoyKey(state.password)
                        if (decoyKey != null) {
                            session.setSecretKey(decoyKey)
                            activeVaultPrefs.setActiveVault(VaultMetadata.DECOY_MAIN_VAULT_ID)
                            panicPinPrefs.activateDecoyMode()
                        }
                    }
                }
                if (inheritancePrefs.isEnabled() || inheritancePrefs.isPendingDisarm()) {
                    inheritanceDisarmer.requestDisarm(
                        com.thormakk.keyarmor.data.repository.DisarmReason.PANIC_PIN
                    )
                }
                drivePrefs.markDisconnectedByPanicPin()
                drivePrefs.clear()
                securityPrefs.updateLastReview()
                _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                return@launch
            }

            if (passwordOk) {
                panicPinPrefs.clearAllCoercion()
                val shouldSetup = isBiometricAvailable()
                        && !authRepository.isBiometricKeyStored()
                        && !authRepository.isBiometricSetupSeen()
                if (shouldSetup) {
                    _uiState.value = _uiState.value.copy(
                        isLoading             = false,
                        biometricSetupPending = true
                    )
                } else {
                    securityPrefs.updateLastReview()
                    _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = UiText.StringResource(R.string.error_wrong_password)
                )
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

    fun getUnlockCipher(): Cipher? {
        val key = cachedBiometricKey ?: run {
            Log.w("LockViewModel", "getUnlockCipher: cachedBiometricKey es null (¿todavía no cargó desde storage?)")
            return null
        }
        return try {
            biometricCrypto.getDecryptionCipher(key)
        } catch (e: KeyPermanentlyInvalidatedException) {
            Log.e("LockViewModel", "getUnlockCipher: KeyPermanentlyInvalidatedException real o espuria (Android no distingue) — invalidando clave", e)
            onBiometricKeyInvalidated()
            null
        } catch (e: Exception) {
            Log.e("LockViewModel", "getUnlockCipher: excepción NO relacionada con invalidación de huellas (${e::class.java.name}: ${e.message}) — no se toca la clave", e)
            null
        }
    }

    fun getSetupCipher(): Cipher? =
        try { biometricCrypto.getEncryptionCipher() } catch (e: Exception) { null }

    fun onBiometricUnlockSuccess(cipher: Cipher) {
        if (panicPinPrefs.coercionActiveFlow.value && !panicPinPrefs.isHideModeExpired()) {
            Log.w("LockViewModel", "onBiometricUnlockSuccess: rechazado, hay coacción activa")
            _uiState.value = _uiState.value.copy(
                error = UiText.StringResource(R.string.error_wrong_password)
            )
            return
        }
        val key = cachedBiometricKey ?: run {
            _uiState.value = _uiState.value.copy(error = UiText.StringResource(R.string.error_biometric_internal))
            return
        }
        try {
            val masterKeyBytes = biometricCrypto.decryptMasterKey(cipher, key)
            authRepository.setSessionKeyFromBytes(masterKeyBytes)
            masterKeyBytes.fill(0)
            if (panicPinPrefs.isHideModeExpired()) panicPinPrefs.deactivateHideMode()
            securityPrefs.updateLastReview()
            biometricAutoRetried = false
            _uiState.value = _uiState.value.copy(success = true)
        } catch (e: KeyPermanentlyInvalidatedException) {
            Log.e("LockViewModel", "onBiometricUnlockSuccess: KeyPermanentlyInvalidatedException — invalidando clave", e)
            onBiometricKeyInvalidated()
        } catch (e: Exception) {
            Log.e("LockViewModel", "onBiometricUnlockSuccess: fallo NO relacionado con invalidación de huellas (${e::class.java.name}: ${e.message}) — reintento=${!biometricAutoRetried}", e)
            if (!biometricAutoRetried) {
                biometricAutoRetried = true
                _uiState.value = _uiState.value.copy(retryBiometricUnlock = true)
            } else {
                biometricAutoRetried = false
                _uiState.value = _uiState.value.copy(error = UiText.StringResource(R.string.error_biometric_internal))
            }
        }
    }

    fun consumeBiometricRetry() {
        _uiState.value = _uiState.value.copy(retryBiometricUnlock = false)
    }

    fun onBiometricSetupSuccess(cipher: Cipher) {
        val masterKeyBytes = authRepository.getMasterKeyBytes() ?: run {
            skipBiometricSetup(); return
        }
        viewModelScope.launch {
            try {
                val encrypted = biometricCrypto.encryptMasterKey(cipher, masterKeyBytes)
                masterKeyBytes.fill(0)
                authRepository.setBiometricKey(encrypted)
                cachedBiometricKey = encrypted
                securityPrefs.updateLastReview()
                _uiState.value = _uiState.value.copy(
                    biometricSetupPending = false,
                    biometricEnabled      = true,
                    success               = true
                )
            } catch (e: Exception) {
                masterKeyBytes.fill(0)
                skipBiometricSetup()
            }
        }
    }

    fun skipBiometricSetup() {
        securityPrefs.updateLastReview()
        _uiState.value = _uiState.value.copy(biometricSetupPending = false, success = true)
        viewModelScope.launch { authRepository.markBiometricSetupSeen() }
    }

    fun onBiometricKeyInvalidated() {
        if (cachedBiometricKey == null) return
        cachedBiometricKey = null
        viewModelScope.launch {
            authRepository.clearBiometricKey()
            biometricCrypto.deleteKey()
            _uiState.value = _uiState.value.copy(
                biometricEnabled = false,
                error = UiText.StringResource(R.string.error_biometric_invalidated)
            )
        }
    }

    fun onBiometricUnavailableTransient() {
        _uiState.value = _uiState.value.copy(error = UiText.StringResource(R.string.error_biometric_internal))
    }

    private fun isBiometricAvailable(): Boolean =
        BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
}
