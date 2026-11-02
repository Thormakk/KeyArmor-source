package com.thormakk.keyarmor.presentation.screens.family

import android.content.Context
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.revenuecat.purchases.Purchases
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.FamilyPrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.FamilyVaultRepository
import com.thormakk.keyarmor.data.repository.FcmEventBus
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.domain.model.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.tasks.await as awaitTask
import com.thormakk.keyarmor.appcheck.AppCheckHeader

data class FamilyUiState(
    val members:        List<FamilyPrefs.MemberInfo> = emptyList(),
    val adminEmail:     String?             = null,
    val isLoading:      Boolean             = false,
    val inviteEmail:    String              = "",
    val isAdminPlan:    Boolean             = false,
    val isMember:       Boolean             = false,
    val memberAdminEmail: String?           = null
)

sealed interface FamilyEvent {
    data class InviteSent(val email: String) : FamilyEvent
    data class Error(val message: String)    : FamilyEvent
    object MemberPremiumRevoked               : FamilyEvent
    data class MemberAccessRevoked(val adminEmail: String?) : FamilyEvent
    data class MemberLeft(val email: String) : FamilyEvent
    data class MemberDeclined(val email: String) : FamilyEvent
}

@HiltViewModel
class FamilyViewModel @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val familyPrefs:           FamilyPrefs,
    private val fcmPrefs:              FcmPrefs,
    private val fcmEventBus:           FcmEventBus,
    private val drivePrefs:            DrivePrefs,
    private val planPrefs:             PlanPrefs,
    private val familyVaultRepository: FamilyVaultRepository,
    private val inAppNotificationRepository: InAppNotificationRepository,
    private val authRepository:        AuthRepository
) : ViewModel() {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState

    private val _events = Channel<FamilyEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    companion object {
        private const val TAG                = "FamilyVM"
        private const val INVITE_URL         = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val REVOKE_URL         = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val REVOKE_PREMIUM_URL = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val LEAVE_URL          = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val POLL_URL           = "<URL DEL SERVIDOR DE GOOGLE>"
    }

    init {
        load()
        observePlanChanges()
        checkPendingMemberPremiumRevoke()
        processPendingMemberAccepted()
        observeMemberAcceptedEvents()
        processPendingMemberLeft()
        observeMemberLeftEvents()
        processPendingMemberDeclined()
        observeMemberDeclinedEvents()
        syncMemberStatusFromServer()
        observeMemberPremiumRevokedEvents()
        observeMemberRoleAcceptedEvents()
    }

    private fun observeMemberRoleAcceptedEvents() {
        viewModelScope.launch {
            fcmEventBus.memberRoleAcceptedEvent.collect {
                load()
            }
        }
    }

    private fun observePlanChanges() {
        viewModelScope.launch {
            planPrefs.planFlow.collect { plan ->
                _uiState.update { it.copy(isAdminPlan = plan == UserPlan.FAMILY) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val members    = familyPrefs.getMemberData()
            val adminEmail = drivePrefs.getConnectedEmail()
            val isAdmin    = planPrefs.effectivePlan() == UserPlan.FAMILY
            _uiState.update { it.copy(
                members          = members,
                adminEmail       = adminEmail,
                isAdminPlan      = isAdmin,
                isMember         = familyPrefs.isMember(),
                memberAdminEmail = familyPrefs.getAdminEmail()
            ) }
        }
    }

    fun onInviteEmailChange(email: String) {
        _uiState.update { it.copy(inviteEmail = email) }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.send(FamilyEvent.Error(message))
            inAppNotificationRepository.save(
                NotificationType.FAMILY_ERROR,
                context.getString(com.thormakk.keyarmor.R.string.family_error_notif_title),
                message
            )
        }
    }

    fun sendInvite() {
        val state       = _uiState.value
        val memberEmail = state.inviteEmail.trim()
        val adminEmail  = state.adminEmail ?: run {
            emitError(context.getString(com.thormakk.keyarmor.R.string.family_error_plan_not_active))
            return
        }

        if (memberEmail.equals(adminEmail, ignoreCase = true)) {
            emitError(context.getString(com.thormakk.keyarmor.R.string.family_error_self_invite))
            return
        }

        if (familyPrefs.getMemberEmails().size >= FamilyPrefs.MAX_MEMBERS) {
            emitError(context.getString(com.thormakk.keyarmor.R.string.family_admin_max_members))
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val idToken = getOrRefreshIdToken() ?: run {
                    emitError(context.getString(com.thormakk.keyarmor.R.string.family_error_google_token))
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
                val adminFcmToken = try {
                    FirebaseMessaging.getInstance().token.awaitTask()
                } catch (e: Exception) {
                    Log.w(TAG, "sendInvite: no se pudo obtener FCM token — ${e.message}")
                    null
                }
                Log.d(TAG, "sendInvite: adminFcmToken=${adminFcmToken?.takeLast(10) ?: "NULL"}")
                val deviceToken = familyPrefs.ensureDeviceToken()
                val rcAppUserId = Purchases.sharedInstance.appUserID
                val newDeviceToken = callInviteCf(
                    adminEmail    = adminEmail,
                    memberEmail   = memberEmail,
                    idToken       = idToken,
                    deviceToken   = deviceToken,
                    rcAppUserId   = rcAppUserId,
                    adminFcmToken = adminFcmToken
                )
                if (newDeviceToken != null) familyPrefs.storeDeviceToken(newDeviceToken)

                familyPrefs.addMemberEmail(memberEmail)
                familyPrefs.saveMemberEntry(
                    FamilyPrefs.MemberInfo(memberEmail = memberEmail, status = "pending")
                )
                val updated = familyPrefs.getMemberData()
                _uiState.update { it.copy(members = updated, isLoading = false, inviteEmail = "") }
                _events.send(FamilyEvent.InviteSent(memberEmail))
                inAppNotificationRepository.save(
                    NotificationType.FAMILY_INVITE_SENT,
                    context.getString(com.thormakk.keyarmor.R.string.family_invite_sent_notif_title),
                    context.getString(com.thormakk.keyarmor.R.string.family_invite_sent_notif_body, memberEmail)
                )
            } catch (e: Exception) {
                Log.e(TAG, "sendInvite error: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
                val message = when {
                    e.message?.contains("member_already_active_elsewhere") == true ->
                        context.getString(com.thormakk.keyarmor.R.string.family_error_member_already_active_elsewhere)
                    e.message?.contains("Límite de 4 members", ignoreCase = true) == true ->
                        context.getString(com.thormakk.keyarmor.R.string.family_error_member_limit)
                    e.message?.contains("revenuecat_unavailable") == true ->
                        context.getString(com.thormakk.keyarmor.R.string.family_error_revenuecat_unavailable)
                    e.message?.contains("family_plan_not_active") == true ->
                        context.getString(com.thormakk.keyarmor.R.string.family_error_plan_not_active)
                    else ->
                        e.message ?: context.getString(com.thormakk.keyarmor.R.string.family_error_sending_invite)
                }
                emitError(message)
            }
        }
    }

    private suspend fun getOrRefreshIdToken(): String? =
        getFirebaseIdentityTokenOrNull(context)?.idToken

    fun revokeMember(memberEmail: String) {
        val adminEmail  = _uiState.value.adminEmail ?: return
        val deviceToken = familyPrefs.getDeviceToken() ?: return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val adminIdToken = getOrRefreshIdToken()
                    ?: error(context.getString(com.thormakk.keyarmor.R.string.family_error_revoking_access))
                callRevokeCf(adminEmail, memberEmail, deviceToken, adminIdToken)
                familyPrefs.removeMemberEmail(memberEmail)
                familyPrefs.removeMemberEntry(memberEmail)
                val updated = familyPrefs.getMemberData()
                _uiState.update { it.copy(members = updated, isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "revokeMember error: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
                emitError(e.message ?: context.getString(com.thormakk.keyarmor.R.string.family_error_revoking_access))
            }
        }
    }

    fun leaveFamilyPlan() {
        val adminEmail = _uiState.value.memberAdminEmail ?: return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val idToken = getOrRefreshIdToken() ?: run {
                    emitError(context.getString(com.thormakk.keyarmor.R.string.family_error_google_token))
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
                callLeaveCf(idToken, adminEmail)
                familyVaultRepository.deleteMemberVault(adminEmail)
                familyPrefs.clearMemberRole()
                planPrefs.refresh()
                _uiState.update { it.copy(
                    isLoading        = false,
                    isMember         = false,
                    memberAdminEmail = null
                ) }
                Log.d(TAG, "leaveFamilyPlan: miembro dado de baja del plan de $adminEmail")
            } catch (e: Exception) {
                Log.e(TAG, "leaveFamilyPlan error: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
                emitError(e.message ?: context.getString(com.thormakk.keyarmor.R.string.family_error_leaving_plan))
            }
        }
    }

    private fun callLeaveCf(idToken: String, adminEmail: String) {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val body = """{"memberIdToken":"${idToken.esc()}","adminEmail":"${adminEmail.esc()}"}"""
        val conn = URL(LEAVE_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 20_000
        conn.readTimeout    = 40_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        if (code !in 200..299) throw Exception("onFamilyMemberLeave respondió HTTP $code")
    }

    private fun processPendingMemberLeft() {
        val memberEmail = fcmPrefs.pendingMemberLeftEmail ?: return
        applyMemberLeft(memberEmail)
        fcmPrefs.clearPendingMemberLeft()
        viewModelScope.launch {
            _events.send(FamilyEvent.MemberLeft(memberEmail))
            saveMemberLeftNotification(memberEmail)
        }
    }

    private fun observeMemberLeftEvents() {
        viewModelScope.launch {
            fcmEventBus.memberLeftEvent.collect { memberEmail ->
                applyMemberLeft(memberEmail)
                fcmPrefs.clearPendingMemberLeft()
                _events.send(FamilyEvent.MemberLeft(memberEmail))
                saveMemberLeftNotification(memberEmail)
            }
        }
    }

    private suspend fun saveMemberLeftNotification(memberEmail: String) {
        inAppNotificationRepository.save(
            NotificationType.FAMILY_MEMBER_LEFT,
            context.getString(com.thormakk.keyarmor.R.string.family_member_left_notif_title, memberEmail),
            context.getString(com.thormakk.keyarmor.R.string.family_member_left_notif_body, memberEmail)
        )
    }

    private fun applyMemberLeft(memberEmail: String) {
        familyPrefs.removeMemberEmail(memberEmail)
        familyPrefs.removeMemberEntry(memberEmail)
        val updated = familyPrefs.getMemberData()
        _uiState.update { it.copy(members = updated) }
        Log.d(TAG, "Member se fue voluntariamente — removido localmente: $memberEmail")
    }

    private fun processPendingMemberDeclined() {
        val memberEmail = fcmPrefs.pendingMemberDeclinedEmail ?: return
        viewModelScope.launch { applyMemberDeclined(memberEmail) }
        fcmPrefs.clearPendingMemberDeclined()
    }

    private fun observeMemberDeclinedEvents() {
        viewModelScope.launch {
            fcmEventBus.memberDeclinedEvent.collect { memberEmail ->
                applyMemberDeclined(memberEmail)
                fcmPrefs.clearPendingMemberDeclined()
            }
        }
    }

    private suspend fun applyMemberDeclined(memberEmail: String) {
        familyPrefs.removeMemberEmail(memberEmail)
        familyPrefs.removeMemberEntry(memberEmail)
        val updated = familyPrefs.getMemberData()
        _uiState.update { it.copy(members = updated) }
        Log.d(TAG, "Member rechazó la invitación — removido localmente: $memberEmail")
        _events.send(FamilyEvent.MemberDeclined(memberEmail))
        inAppNotificationRepository.save(
            NotificationType.FAMILY_MEMBER_DECLINED,
            context.getString(com.thormakk.keyarmor.R.string.family_member_declined_notif_title),
            context.getString(com.thormakk.keyarmor.R.string.family_member_declined_notif_body, memberEmail)
        )
    }

    private fun processPendingMemberAccepted() {
        val email = fcmPrefs.pendingMemberAcceptedEmail ?: return
        applyMemberAccepted(email)
        fcmPrefs.clearPendingMemberAccepted()
    }

    private fun observeMemberAcceptedEvents() {
        viewModelScope.launch {
            fcmEventBus.memberAcceptedEvent.collect { payload ->
                applyMemberAccepted(payload.memberEmail)
                fcmPrefs.clearPendingMemberAccepted()
            }
        }
    }

    private fun applyMemberAccepted(memberEmail: String) {
        familyPrefs.saveMemberEntry(
            FamilyPrefs.MemberInfo(memberEmail = memberEmail, status = "active")
        )
        val updated = familyPrefs.getMemberData()
        _uiState.update { it.copy(members = updated) }
        Log.d(TAG, "Member aceptó — actualizado a active: $memberEmail")
    }

    private data class ServerMemberInfo(
        val memberEmail: String,
        val status:      String
    )

    fun syncMemberStatusFromServer() {
        if (familyPrefs.isMember()) return
        if (familyPrefs.getMemberData().none { it.status == "pending" }) return
        val adminEmail  = drivePrefs.getConnectedEmail() ?: return
        val deviceToken = familyPrefs.getDeviceToken()  ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serverMembers = callPollNotificationsCf(adminEmail, deviceToken)
                    ?: return@launch
                val localMembers = familyPrefs.getMemberData()

                for (server in serverMembers) {
                    val local = localMembers.find { it.memberEmail == server.memberEmail }
                        ?: continue
                    if (local.status != "pending") continue

                    when (server.status) {
                        "revoked" -> {
                            Log.d(TAG, "syncMemberStatus: ${server.memberEmail} — revocado en servidor, aplicando decline")
                            applyMemberDeclined(server.memberEmail)
                        }
                        "active" -> {
                            Log.d(TAG, "syncMemberStatus: ${server.memberEmail} — activo en servidor, aplicando accept")
                            familyPrefs.saveMemberEntry(
                                FamilyPrefs.MemberInfo(
                                    memberEmail = server.memberEmail,
                                    status      = "active"
                                )
                            )
                            val updated = familyPrefs.getMemberData()
                            _uiState.update { it.copy(members = updated) }
                        }
                    }
                }

            } catch (e: Exception) {
                Log.w(TAG, "syncMemberStatus: fallo al sincronizar — ${e.message}")
            }
        }
    }

    private fun callPollNotificationsCf(adminEmail: String, deviceToken: String): List<ServerMemberInfo>? {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val body = """{"adminEmail":"${adminEmail.esc()}","deviceToken":"${deviceToken.esc()}"}"""
        val conn = URL(POLL_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 10_000
        conn.readTimeout    = 20_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        if (conn.responseCode !in 200..299) return null
        val response = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
        val json     = org.json.JSONObject(response)
        val arr      = json.optJSONArray("members") ?: return emptyList()
        return (0 until arr.length()).map { i ->
            val m = arr.getJSONObject(i)
            ServerMemberInfo(
                memberEmail = m.getString("memberEmail"),
                status      = m.getString("status")
            )
        }
    }

    private fun checkPendingMemberPremiumRevoke() {
        if (!familyPrefs.pendingMemberPremiumRevoke) return
        val members = familyPrefs.pendingRevokeEmails
        if (members.isEmpty()) {
            familyPrefs.pendingMemberPremiumRevoke = false
            familyPrefs.clearPendingRevokeEmails()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val idToken = getOrRefreshIdToken() ?: run {
                    Log.w(TAG, "checkPendingMemberPremiumRevoke: sin idToken, se reintentará")
                    return@launch
                }
                callRevokePremiumCf(idToken, members)
                familyPrefs.pendingMemberPremiumRevoke = false
                familyPrefs.clearPendingRevokeEmails()
                Log.d(TAG, "onRevokeMemberPremium: premium revocado para ${members.size} member(s)")
            } catch (e: Exception) {
                Log.w(TAG, "checkPendingMemberPremiumRevoke error — se reintentará: ${e.message}")
            }
        }
    }

    private fun observeMemberPremiumRevokedEvents() {
        viewModelScope.launch {
            fcmEventBus.memberPremiumRevoked.collect {
                _events.send(FamilyEvent.MemberPremiumRevoked)
            }
        }
        viewModelScope.launch {
            fcmEventBus.memberAccessRevoked.collect { adminEmail ->
                _uiState.update { it.copy(isMember = false, memberAdminEmail = null) }
                _events.send(FamilyEvent.MemberAccessRevoked(adminEmail))
            }
        }
    }

    private fun callInviteCf(
        adminEmail:    String,
        memberEmail:   String,
        idToken:       String,
        deviceToken:   String,
        rcAppUserId:   String,
        adminFcmToken: String? = null
    ): String? {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val body = buildString {
            append("""{"adminEmail":"${adminEmail.esc()}","memberEmail":"${memberEmail.esc()}","idToken":"${idToken.esc()}","deviceToken":"${deviceToken.esc()}","rcAppUserId":"${rcAppUserId.esc()}"""")
            if (adminFcmToken != null) append(""","adminFcmToken":"${adminFcmToken.esc()}"""")
            append("}")
        }
        val conn = URL(INVITE_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 20_000
        conn.readTimeout    = 40_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val errBody = try { conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: "" } catch (_: Exception) { "" }
            throw Exception("onFamilyInvite HTTP $code: $errBody")
        }
        return try {
            val response = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
            org.json.JSONObject(response).optString("deviceToken").takeIf { it.isNotEmpty() }
        } catch (_: Exception) { null }
    }

    private fun callRevokeCf(adminEmail: String, memberEmail: String, deviceToken: String, adminIdToken: String) {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val body = """{"adminEmail":"${adminEmail.esc()}","memberEmail":"${memberEmail.esc()}","deviceToken":"${deviceToken.esc()}","adminIdToken":"${adminIdToken.esc()}"}"""
        val conn = URL(REVOKE_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 20_000
        conn.readTimeout    = 40_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val errBody = try { conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: "" } catch (_: Exception) { "" }
            throw Exception("onFamilyRevokeMember HTTP $code: $errBody")
        }
    }

    private fun callRevokePremiumCf(idToken: String, memberEmails: List<String>) {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val emailsJson = memberEmails.joinToString(",") { "\"${it.esc()}\"" }
        val body = """{"idToken":"${idToken.esc()}","memberEmails":[$emailsJson]}"""
        val conn = URL(REVOKE_PREMIUM_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 20_000
        conn.readTimeout    = 40_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        if (code !in 200..299) throw Exception("onRevokeMemberPremium respondió HTTP $code")
    }
}
