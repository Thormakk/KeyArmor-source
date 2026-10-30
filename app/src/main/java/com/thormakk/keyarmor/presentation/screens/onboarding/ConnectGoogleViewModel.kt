package com.thormakk.keyarmor.presentation.screens.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.billing.RevenueCatManager
import com.thormakk.keyarmor.data.drive.GoogleIdentityVerifier
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.OnboardingPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.FcmRepository
import com.thormakk.keyarmor.presentation.util.establishFirebaseSession
import com.thormakk.keyarmor.presentation.util.registerFcmTokenFromLogin
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import javax.inject.Inject

data class ConnectGoogleUiState(
    val isConnecting:   Boolean = false,
    val connectedEmail: String? = null,
    val errorMessage:   String? = null
)

@HiltViewModel
class ConnectGoogleViewModel @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val drivePrefs:       DrivePrefs,
    private val identityVerifier: GoogleIdentityVerifier,
    private val rcManager:        RevenueCatManager,
    private val fcmRepository:    FcmRepository,
    private val fcmPrefs:         FcmPrefs,
    private val planPrefs:        PlanPrefs,
    private val onboardingPrefs:  OnboardingPrefs
) : ViewModel() {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    private val _uiState = MutableStateFlow(ConnectGoogleUiState())
    val uiState: StateFlow<ConnectGoogleUiState> = _uiState.asStateFlow()

    fun connectAccount(email: String, idToken: String) {
        _uiState.update { it.copy(isConnecting = true, errorMessage = null) }
        viewModelScope.launch {
            val verified = identityVerifier.verify(idToken)
            if (verified == null) {
                _uiState.update {
                    it.copy(
                        isConnecting = false,
                        errorMessage = context.getString(R.string.error_google_verification_failed)
                    )
                }
                return@launch
            }
            val verifiedEmail = verified.email ?: email
            drivePrefs.saveConnectedEmail(verifiedEmail)
            drivePrefs.saveOwnerGoogleSub(verified.sub)

            withTimeoutOrNull(5_000) {
                suspendCancellableCoroutine<Boolean> { cont ->
                    rcManager.linkIdentity(verified.sub) { ok -> if (cont.isActive) cont.resume(ok) }
                }
            }

            viewModelScope.launch { establishFirebaseSession(idToken) }

            viewModelScope.launch { registerFcmTokenFromLogin(idToken, fcmRepository, fcmPrefs) }

            _uiState.update { it.copy(isConnecting = false, connectedEmail = verifiedEmail) }
        }
    }

    fun shouldSkipTutorial(): Boolean {
        val conoceLaApp = drivePrefs.getConnectedEmail() != null && planPrefs.getUserPlan() != UserPlan.FREE
        if (conoceLaApp) {
            onboardingPrefs.hasSeenWelcome = true
            onboardingPrefs.tutorialSkipped = true
        }
        return conoceLaApp
    }

    fun setError(message: String?) {
        _uiState.update { it.copy(isConnecting = false, errorMessage = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
