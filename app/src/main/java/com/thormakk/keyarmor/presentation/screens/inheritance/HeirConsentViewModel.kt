package com.thormakk.keyarmor.presentation.screens.inheritance

import android.content.Context
import com.thormakk.keyarmor.presentation.util.extractGoogleSubFromJwt
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.repository.HeirConsentRepository
import com.thormakk.keyarmor.data.repository.InheritanceRepository
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.HeirIdentityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject

sealed interface HeirConsentUiState {
    object Loading  : HeirConsentUiState
    object Error    : HeirConsentUiState
    object Done     : HeirConsentUiState

    data class WaitingFingerprintConfirmation(
        val ownerEmail: String,
        val code:       String,
    ) : HeirConsentUiState

    data class ShowRecoveryCode(val code: String) : HeirConsentUiState

    data class Loaded(
        val ownerEmail:   String,
        val heirEmail:    String,
        val isSubmitting: Boolean = false,
        val errorRes:     Int? = null,
        val ownerKeyChange: OwnerKeyChange? = null
    ) : HeirConsentUiState

    data class OwnerKeyChange(
        val huellaAnterior: String,
        val huellaNueva:    String,
    )
}

@HiltViewModel
class HeirConsentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository:       HeirConsentRepository,
    private val inheritancePrefs: InheritancePrefs,
    private val fcmPrefs:         FcmPrefs,
    private val crypto:           CryptoManager,
    private val heirIdentity:     HeirIdentityManager,
    private val fcmEventBus:      com.thormakk.keyarmor.data.repository.FcmEventBus,
    private val inheritanceRepo:  InheritanceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HeirConsentUiState>(HeirConsentUiState.Loading)
    val uiState: StateFlow<HeirConsentUiState> = _uiState

    private var cachedIdToken: String? = null

    private var cachedHeirSub: String? = null

    init {
        viewModelScope.launch {
            val idToken = getFirebaseIdentityTokenOrNull(context)?.idToken
            if (idToken == null) {
                Log.w(TAG, "getFirebaseIdentityTokenOrNull falló o no devolvió idToken")
                _uiState.value = HeirConsentUiState.Error
                return@launch
            }
            cachedIdToken = idToken

            val (heirEmail, heirSub) = extractEmailAndSubFromJwt(idToken)
            if (heirEmail == null) {
                Log.w(TAG, "No se pudo extraer email del idToken JWT")
                _uiState.value = HeirConsentUiState.Error
                return@launch
            }
            cachedHeirSub = heirSub

            val ownerEmail = fcmPrefs.pendingHeirConsentOwnerEmail
            Log.d(TAG, "ownerEmail desde FcmPrefs: ${ownerEmail ?: "null"}")
            _uiState.value = when {
                ownerEmail != null -> HeirConsentUiState.Loaded(
                    ownerEmail = ownerEmail,
                    heirEmail  = heirEmail
                )
                fcmPrefs.waitingFingerprintConfirmationOwnerEmail != null ->
                    resolveWaitingState(fcmPrefs.waitingFingerprintConfirmationOwnerEmail!!)
                else -> HeirConsentUiState.Error
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirFingerprintConfirmedEvent.collect {
                val st = _uiState.value as? HeirConsentUiState.WaitingFingerprintConfirmation ?: return@collect
                fcmPrefs.clearPendingHeirFingerprintConfirmed()
                fcmPrefs.clearWaitingFingerprintConfirmation()
                inheritancePrefs.setHeirOfOwnerConfirmedAt(System.currentTimeMillis())
                _uiState.value = HeirConsentUiState.ShowRecoveryCode(st.code)
            }
        }
        viewModelScope.launch {
            fcmEventBus.heirRemovedByOwnerEvent.collect {
                if (_uiState.value !is HeirConsentUiState.WaitingFingerprintConfirmation) return@collect
                fcmPrefs.clearWaitingFingerprintConfirmation()
                _uiState.value = HeirConsentUiState.Done
            }
        }
    }

    private fun resolveWaitingState(ownerEmail: String): HeirConsentUiState {
        val code = runCatching { heirIdentity.getOrCreateIdentity().privateKeyCode }.getOrNull()
        if (code == null) {
            fcmPrefs.clearWaitingFingerprintConfirmation()
            return HeirConsentUiState.Error
        }
        return when {
            fcmPrefs.pendingHeirFingerprintConfirmed -> {
                fcmPrefs.clearPendingHeirFingerprintConfirmed()
                fcmPrefs.clearWaitingFingerprintConfirmation()
                inheritancePrefs.setHeirOfOwnerConfirmedAt(System.currentTimeMillis())
                HeirConsentUiState.ShowRecoveryCode(code)
            }
            inheritancePrefs.getHeirOfOwnerEmail() != ownerEmail -> {
                fcmPrefs.clearWaitingFingerprintConfirmation()
                HeirConsentUiState.Done
            }
            else -> HeirConsentUiState.WaitingFingerprintConfirmation(ownerEmail, code)
        }
    }

    private var cambioDeClaveConfirmado = false

    fun onOwnerKeyChangeConfirmed() {
        val st = _uiState.value as? HeirConsentUiState.Loaded ?: return
        cambioDeClaveConfirmado = true
        _uiState.value = st.copy(ownerKeyChange = null)
        submitDecision(accepted = true)
    }

    fun onOwnerKeyChangeRejected() {
        val st = _uiState.value as? HeirConsentUiState.Loaded ?: return
        cambioDeClaveConfirmado = false
        _uiState.value = st.copy(ownerKeyChange = null)
        submitDecision(accepted = false)
    }

    fun getMyKeyFingerprint(): String? =
        com.thormakk.keyarmor.security.KeyFingerprint.of(heirIdentity.getPublicKey())

    fun accept()  = submitDecision(accepted = true)
    fun decline() = submitDecision(accepted = false)

    private fun submitDecision(accepted: Boolean) {
        val state = _uiState.value as? HeirConsentUiState.Loaded ?: return
        val idToken = cachedIdToken ?: run {
            _uiState.value = HeirConsentUiState.Error
            return
        }
        _uiState.value = state.copy(isSubmitting = true)
        viewModelScope.launch {
            val identity = if (accepted) {
                runCatching { heirIdentity.getOrCreateIdentity() }.getOrElse {
                    Log.w(TAG, "No se pudo generar la identidad EC del heredero: ${it.message}")
                    _uiState.value = HeirConsentUiState.Error
                    return@launch
                }
            } else null

            val encryptedHeirSub: String? = if (accepted) {
                val heirSub = cachedHeirSub ?: run {
                    Log.w(TAG, "cachedHeirSub es null — no se puede cifrar el sub")
                    _uiState.value = HeirConsentUiState.Error
                    return@launch
                }
                val publicKey = fcmPrefs.pendingHeirConsentPublicKey ?: run {
                    Log.w(TAG, "No se encontró clave pública del titular en FcmPrefs")
                    _uiState.value = HeirConsentUiState.Error
                    return@launch
                }
                runCatching { crypto.encryptWithRsaPublicKey(heirSub, publicKey) }.getOrNull()
            } else null

            val ownerSigningKey = fcmPrefs.pendingHeirConsentOwnerSigningKey
            if (accepted && ownerSigningKey.isNullOrBlank()) {
                Log.w(TAG, "Designación sin clave de firma del titular — aceptación rechazada")
                _uiState.value = state.copy(
                    isSubmitting = false,
                    errorRes     = R.string.heir_consent_error_owner_key_missing
                )
                return@launch
            }

            val pinPrevio = inheritancePrefs.getPinnedOwnerSigningKey(state.ownerEmail)
            if (accepted && !cambioDeClaveConfirmado && pinPrevio != null && pinPrevio != ownerSigningKey) {
                val huellaVieja = com.thormakk.keyarmor.security.KeyFingerprint.of(pinPrevio)
                val huellaNueva = com.thormakk.keyarmor.security.KeyFingerprint.of(ownerSigningKey)
                if (huellaVieja != null && huellaNueva != null) {
                    Log.w(TAG, "La clave de firma del titular cambió — se pide confirmación")
                    _uiState.value = state.copy(
                        isSubmitting   = false,
                        errorRes       = null,
                        ownerKeyChange = HeirConsentUiState.OwnerKeyChange(huellaVieja, huellaNueva)
                    )
                    return@launch
                }
            }

            val confirmado = runCatching {
                repository.submitConsent(
                    heirEmail              = state.heirEmail,
                    encryptedHeirSubBase64 = encryptedHeirSub,
                    ownerEmail             = state.ownerEmail,
                    accepted               = accepted,
                    heirIdToken            = idToken,
                    heirLocale             = AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language,
                    heirPublicKeyB64       = identity?.publicKeyB64
                )
            }.getOrElse {
                Log.w(TAG, "submitConsent falló: ${it.message}", it)
                false
            }
            if (!confirmado) {
                _uiState.value = state.copy(
                    isSubmitting = false,
                    errorRes     = R.string.heir_consent_error_submit
                )
                return@launch
            }
            if (accepted) inheritancePrefs.clearDismissedHeirVault(state.ownerEmail)
            if (accepted) {
                inheritancePrefs.setHeirOfOwnerEmail(state.ownerEmail)
                inheritancePrefs.setHeirOfOwnerAcceptedAt(System.currentTimeMillis())
                inheritancePrefs.savePinnedOwnerSigningKey(state.ownerEmail, ownerSigningKey!!)
                fcmEventBus.emitHeirConsentAccepted()
            }
            fcmPrefs.clearPendingHeirConsent()
            fcmPrefs.pendingHeirConsentPublicKey = null
            fcmPrefs.pendingHeirConsentOwnerSigningKey = null
            cambioDeClaveConfirmado = false

            _uiState.value = if (accepted && identity != null) {
                fcmPrefs.waitingFingerprintConfirmationOwnerEmail = state.ownerEmail
                HeirConsentUiState.WaitingFingerprintConfirmation(state.ownerEmail, identity.privateKeyCode)
            } else {
                HeirConsentUiState.Done
            }
        }
    }

    fun cancelWhileWaiting() {
        val st = _uiState.value as? HeirConsentUiState.WaitingFingerprintConfirmation ?: return
        val idToken = cachedIdToken
        fcmPrefs.clearWaitingFingerprintConfirmation()
        _uiState.value = HeirConsentUiState.Done
        if (idToken == null) return
        viewModelScope.launch {
            runCatching { inheritanceRepo.resignAsHeir(idToken, st.ownerEmail) }
                .onFailure { Log.w(TAG, "cancelWhileWaiting: resignAsHeir falló: ${it.message}", it) }
        }
    }

    fun onRecoveryCodeSaved() {
        _uiState.value = HeirConsentUiState.Done
    }

    private fun extractEmailAndSubFromJwt(idToken: String): Pair<String?, String?> = try {
        val parts = idToken.split(".")
        if (parts.size < 2) {
            null to null
        } else {
            var payload = parts[1].replace('-', '+').replace('_', '/')
            while (payload.length % 4 != 0) payload += "="
            val decoded = String(Base64.decode(payload, Base64.DEFAULT))
            val json    = JSONObject(decoded)
            val email   = json.optString("email").ifBlank { null }
            val sub     = extractGoogleSubFromJwt(idToken)
            email to sub
        }
    } catch (e: Exception) {
        Log.w(TAG, "Error parseando JWT: ${e.message}")
        null to null
    }

    companion object {
        private const val TAG = "HeirConsentViewModel"
    }
}
