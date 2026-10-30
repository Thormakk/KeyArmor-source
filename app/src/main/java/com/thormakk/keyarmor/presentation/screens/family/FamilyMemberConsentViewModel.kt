package com.thormakk.keyarmor.presentation.screens.family

import android.content.Context
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.thormakk.keyarmor.data.prefs.FamilyPrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.repository.FcmEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import com.thormakk.keyarmor.appcheck.AppCheckHeader

sealed interface FamilyMemberConsentUiState {
    object Loading   : FamilyMemberConsentUiState
    object Error     : FamilyMemberConsentUiState
    object Done      : FamilyMemberConsentUiState
    object Cancelled : FamilyMemberConsentUiState

    data class Loaded(
        val adminEmail:   String,
        val memberEmail:  String,
        val isSubmitting: Boolean = false
    ) : FamilyMemberConsentUiState
}

private class InviteCancelledException : Exception()

@HiltViewModel
class FamilyMemberConsentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val familyPrefs:      FamilyPrefs,
    private val fcmPrefs:         FcmPrefs,
    private val planPrefs:        PlanPrefs,
    private val fcmEventBus:      FcmEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<FamilyMemberConsentUiState>(FamilyMemberConsentUiState.Loading)
    val uiState: StateFlow<FamilyMemberConsentUiState> = _uiState

    private var cachedIdToken:   String? = null
    private var cachedAdminEmail: String? = null
    private var cachedToken:     String? = null

    companion object {
        private const val TAG = "FamilyMemberConsentVM"
        private const val CONSENT_URL = "<URL DEL SERVIDOR DE GOOGLE>"
    }

    fun init(adminEmail: String, inviteToken: String) {
        cachedAdminEmail = adminEmail
        cachedToken      = inviteToken

        viewModelScope.launch {
            val account = getFirebaseIdentityTokenOrNull(context)

            val idToken    = account?.idToken
            val memberEmail = account?.email
            if (idToken == null || memberEmail == null) {
                Log.w(TAG, "getFirebaseIdentityTokenOrNull falló o no devolvió idToken/email")
                fcmPrefs.pendingFamilyInviteAdminEmail = null
                fcmPrefs.pendingFamilyInviteToken      = null
                _uiState.value = FamilyMemberConsentUiState.Error
                return@launch
            }
            cachedIdToken = idToken

            _uiState.value = FamilyMemberConsentUiState.Loaded(
                adminEmail  = adminEmail,
                memberEmail = memberEmail
            )
        }
    }

    fun accept()  = submitDecision(accepted = true)
    fun decline() = submitDecision(accepted = false)

    private fun submitDecision(accepted: Boolean) {
        val state      = _uiState.value as? FamilyMemberConsentUiState.Loaded ?: return
        val idToken    = cachedIdToken ?: run { _uiState.value = FamilyMemberConsentUiState.Error; return }
        val adminEmail = cachedAdminEmail ?: run { _uiState.value = FamilyMemberConsentUiState.Error; return }
        val inviteToken = cachedToken ?: run { _uiState.value = FamilyMemberConsentUiState.Error; return }

        _uiState.value = state.copy(isSubmitting = true)

        viewModelScope.launch(Dispatchers.IO) {
            fcmPrefs.pendingFamilyInviteAdminEmail = null
            fcmPrefs.pendingFamilyInviteToken      = null

            try {
                callConsentCf(
                    memberEmail   = state.memberEmail,
                    adminEmail    = adminEmail,
                    inviteToken   = inviteToken,
                    accepted      = accepted,
                    memberIdToken = idToken
                )

                if (accepted) {
                    familyPrefs.setAdminEmail(adminEmail)
                    familyPrefs.setInviteToken(inviteToken)
                    familyPrefs.isPremiumMember = true
                    planPrefs.refresh()
                    fcmPrefs.pendingMemberPremiumRevoked = false
                    fcmPrefs.pendingMemberAccessRevoked  = false
                    fcmPrefs.pendingMemberAccessRevokedAdminEmail = null
                    Log.d(TAG, "submitDecision: aceptado — isMember=${familyPrefs.isMember()} isPremiumMember=${familyPrefs.isPremiumMember} plan=${planPrefs.effectivePlan()}")
                    fcmEventBus.emitMemberRoleAccepted()
                }

                _uiState.value = FamilyMemberConsentUiState.Done
            } catch (e: InviteCancelledException) {
                if (accepted) {
                    Log.w(TAG, "submitDecision: invite revocado por el admin (member intentó aceptar)")
                    _uiState.value = FamilyMemberConsentUiState.Cancelled
                } else {
                    Log.d(TAG, "submitDecision: invite revocado por el admin (member también canceló — Done)")
                    _uiState.value = FamilyMemberConsentUiState.Done
                }
            } catch (e: Exception) {
                Log.e(TAG, "submitDecision: error — ${e.message}")
                _uiState.value = FamilyMemberConsentUiState.Error
            }
        }
    }

    private fun callConsentCf(
        memberEmail:   String,
        adminEmail:    String,
        inviteToken:   String,
        accepted:      Boolean,
        memberIdToken: String
    ) {
        val body = org.json.JSONObject().apply {
            put("memberEmail", memberEmail)
            put("adminEmail", adminEmail)
            put("inviteToken", inviteToken)
            put("accepted", accepted)
            put("memberIdToken", memberIdToken)
        }.toString()
        val conn   = URL(CONSENT_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 20_000
        conn.readTimeout    = 40_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        if (code == 403 || code == 409) throw InviteCancelledException()
        if (code !in 200..299) throw Exception("onFamilyMemberConsent respondió HTTP $code")
    }
}
