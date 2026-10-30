package com.thormakk.keyarmor.presentation.screens.inheritance

import com.thormakk.keyarmor.presentation.util.establishFirebaseSession

import android.content.Context
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.FcmEventBus
import com.thormakk.keyarmor.data.repository.FcmRepository
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.data.repository.InheritanceRepository
import com.thormakk.keyarmor.security.InheritanceKeyManager
import com.thormakk.keyarmor.domain.model.NotificationType
import com.thormakk.keyarmor.notifications.AppNotificationController
import com.thormakk.keyarmor.workers.HeartbeatWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class PendingHeirKeyConfirmation(
    val heirEmail:           String,
    val fingerprint:         String,
    val previousFingerprint: String? = null
)

data class InheritanceUiState(
    val isEnabled:           Boolean      = false,
    val heirEmails:          List<String> = emptyList(),
    val acceptedHeirEmails:  Set<String>  = emptySet(),
    val heirKeyFingerprints: Map<String, String> = emptyMap(),
    val pendingKeyConfirmations: List<PendingHeirKeyConfirmation> = emptyList(),
    val attachmentCount: Int = 0,
    val inactivityDays:      Int          = InheritancePrefs.DEFAULT_INACTIVITY_DAYS,
    val isLoading:           Boolean      = false,
    val isDriveNotConnected: Boolean      = false,
    val isPremium:           Boolean      = false,
    val pendingFirstSeal:    Boolean      = false,
    val isGoogleIdentified:  Boolean      = false,
    val ownerEmailAsHeir:    String?      = null,
    val heirAcceptedAt:      Long         = 0L,
    val heirOfOwnerConfirmedAt: Long      = 0L,
    val heirInheritedAt:     Long         = 0L,
    val isIntroExpanded:     Boolean      = true,
    val wasDisarmedByDowngrade: Boolean   = false,
    val wasDisarmedByAlreadyInherited: Boolean = false,
)

sealed class InheritanceEvent {
    object HeirAdded                       : InheritanceEvent()
    data class Error(val message: String)  : InheritanceEvent()
    object HeirConsentPending              : InheritanceEvent()
    data class HeirResignedSuccess(val ownerEmail: String) : InheritanceEvent()
}

@HiltViewModel
class InheritanceViewModel @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val inheritancePrefs: InheritancePrefs,
    private val drivePrefs:       DrivePrefs,
    private val planPrefs:        PlanPrefs,
    private val fcmPrefs:         FcmPrefs,
    private val fcmEventBus:      FcmEventBus,
    private val fcmRepository:    FcmRepository,
    private val inheritanceRepo:         InheritanceRepository,
    private val inheritanceKeyManager:   InheritanceKeyManager,
    private val appNotificationController: AppNotificationController,
    private val inAppNotificationRepository: InAppNotificationRepository,
    private val heirIdentityManager: com.thormakk.keyarmor.security.HeirIdentityManager,
    private val attachmentRepository: com.thormakk.keyarmor.data.repository.AttachmentRepository,
    private val authRepository: com.thormakk.keyarmor.data.repository.AuthRepository,
    private val heirVaultEncryptionRepository: com.thormakk.keyarmor.data.repository.HeirVaultEncryptionRepository,
) : ViewModel() {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    private val _uiState = MutableStateFlow(InheritanceUiState())
    val uiState: StateFlow<InheritanceUiState> = _uiState

    private val _events = Channel<InheritanceEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        reload()
        checkGoogleIdentity()
        viewModelScope.launch {
            val n = runCatching { attachmentRepository.getAllForBackup().size }.getOrDefault(0)
            _uiState.value = _uiState.value.copy(attachmentCount = n)
        }

        viewModelScope.launch {
            fcmEventBus.heirDeclinedEvent.collect { heirEmail ->
                inheritancePrefs.removeHeirEmail(heirEmail)
                inheritancePrefs.clearHeirKeyState(heirEmail)
                reload()
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirResignedEvent.collect { heirEmail ->
                fcmPrefs.clearPendingHeirResigned()
                handleHeirResigned(heirEmail)
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirVaultEncryptedEvent.collect {
                reload()
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirKeyPendingConfirmationEvent.collect {
                reload()
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirRemovedByOwnerEvent.collect {
                fcmPrefs.pendingHeirRemovedByOwner = false
                reload()
                appNotificationController.show(context.getString(R.string.inheritance_heir_removed_by_owner))
                inAppNotificationRepository.save(
                    NotificationType.HEIR_REMOVED,
                    context.getString(R.string.inheritance_notif_heir_removed_title),
                    context.getString(R.string.inheritance_heir_removed_by_owner)
                )
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirConsentAcceptedEvent.collect {
                reload()
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirFingerprintConfirmedEvent.collect {
                reload()
            }
        }

        viewModelScope.launch {
            fcmEventBus.inheritanceDeliveredEvent.collect { heirEmails ->
                fcmPrefs.clearPendingInheritanceDelivered()
                handleInheritanceDelivered(heirEmails)
            }
        }

        processPendingFcmEvents()

        pollOwnerNotifications()
    }

    private fun pollOwnerNotifications() {
        viewModelScope.launch {
            if (!inheritancePrefs.isEnabled()) return@launch
            val now = System.currentTimeMillis()
            if (now - inheritancePrefs.getLastOwnerNotifPollAt() < OWNER_POLL_THROTTLE_MS) return@launch
            val ownerEmail = drivePrefs.getConnectedEmail() ?: return@launch
            val notifs = try {
                inheritanceRepo.pollOwnerNotifications(ownerEmail)
            } catch (e: Exception) {
                Log.w(TAG, "pollOwnerNotifications falló (se reintenta): ${e.message}")
                return@launch
            }
            inheritancePrefs.setLastOwnerNotifPollAt(now)
            val delivered = mutableListOf<String>()
            notifs.forEach { n ->
                when (n.type) {
                    "HEIR_ACCEPTED"  -> {
                        fcmPrefs.pendingHeirAcceptedEmail  = n.heirEmail
                        fcmPrefs.pendingHeirAcceptedPubKey = n.heirPublicKeyBase64
                        fcmEventBus.emitHeirAccepted(n.heirEmail, null, n.heirPublicKeyBase64)
                    }
                    "HEIR_DECLINED"  -> {
                        fcmPrefs.pendingHeirDeclinedEmail = n.heirEmail
                        fcmEventBus.emitHeirDeclined(n.heirEmail)
                    }
                    "HEIR_DELIVERED" -> delivered += n.heirEmail
                }
            }
            if (delivered.isNotEmpty()) handleInheritanceDelivered(delivered)
        }
    }

    private fun processPendingFcmEvents() {
        val resignedEmail = fcmPrefs.pendingHeirResignedEmail
        if (resignedEmail != null) {
            fcmPrefs.clearPendingHeirResigned()
            viewModelScope.launch { handleHeirResigned(resignedEmail) }
        }

        if (fcmPrefs.pendingHeirRemovedByOwner) {
            fcmPrefs.pendingHeirRemovedByOwner = false
            reload()
            appNotificationController.show(context.getString(R.string.inheritance_heir_removed_by_owner))
            viewModelScope.launch {
                inAppNotificationRepository.save(
                    NotificationType.HEIR_REMOVED,
                    context.getString(R.string.inheritance_notif_heir_removed_title),
                    context.getString(R.string.inheritance_heir_removed_by_owner)
                )
            }
        }

        val deliveredCsv = fcmPrefs.pendingInheritanceDeliveredHeirEmails
        if (deliveredCsv != null) {
            fcmPrefs.clearPendingInheritanceDelivered()
            val heirEmails = deliveredCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
            viewModelScope.launch { handleInheritanceDelivered(heirEmails) }
        }
    }

    private suspend fun handleInheritanceDelivered(heirEmails: List<String>) {
        if (heirEmails.isEmpty()) return
        heirEmails.forEach { email ->
            inheritancePrefs.removeHeirEmail(email)
            inheritancePrefs.removeHeirEncryptionEntry(email)
            inheritancePrefs.clearHeirKeyState(email)
        }
        if (inheritancePrefs.getHeirEncryptionData().isEmpty()) {
            HeartbeatWorker.cancel(context)
        }
        reload()
        val message = context.getString(R.string.inheritance_event_delivered, heirEmails.joinToString(", "))
        appNotificationController.show(message)
        inAppNotificationRepository.save(
            NotificationType.INHERITANCE_SYNC,
            context.getString(R.string.inheritance_notif_delivered_title),
            message
        )
    }

    private suspend fun handleHeirResigned(heirEmail: String) {
        inheritancePrefs.removeHeirEmail(heirEmail)
        inheritancePrefs.removeHeirEncryptionEntry(heirEmail)
        inheritancePrefs.clearHeirKeyState(heirEmail)
        if (inheritancePrefs.getHeirEncryptionData().isEmpty()) {
            HeartbeatWorker.cancel(context)
        }
        reload()
        appNotificationController.show(context.getString(R.string.inheritance_event_heir_resigned, heirEmail))
        inAppNotificationRepository.save(
            NotificationType.HEIR_RESIGNED,
            context.getString(R.string.inheritance_notif_heir_resigned_title),
            context.getString(R.string.inheritance_event_heir_resigned, heirEmail)
        )
    }

    private fun checkGoogleIdentity() {
        viewModelScope.launch {
            val identified = getFirebaseIdentityTokenOrNull(context) != null
            _uiState.value = _uiState.value.copy(isGoogleIdentified = identified)
        }
    }

    fun handleSignInResult(idToken: String) {
        viewModelScope.launch {
            try {
                viewModelScope.launch { establishFirebaseSession(idToken) }

                val fcmToken = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
                if (fcmToken != null) {
                    val ok = fcmRepository.registerToken(idToken, fcmToken)
                    if (ok) {
                        fcmPrefs.lastRegisteredFcmToken = fcmToken
                        fcmPrefs.pendingTokenRefresh    = false
                        Log.d(TAG, "FCM token registrado tras Sign-In de heredero")
                    }
                }

                _uiState.value = _uiState.value.copy(isGoogleIdentified = true)
            } catch (e: Exception) {
                Log.w(TAG, "Sign-In fallido o cancelado: ${e.message}")
            }
        }
    }

    fun reload() {
        val ownerEmail = drivePrefs.getConnectedEmail()
        _uiState.value = _uiState.value.copy(
            isEnabled           = inheritancePrefs.isEnabled(),
            heirEmails          = inheritancePrefs.getHeirEmails(),
            acceptedHeirEmails  = inheritancePrefs.getHeirEncryptionData().map { it.heirEmail }.toSet(),
            heirKeyFingerprints = inheritancePrefs.getHeirEncryptionData()
                .mapNotNull { d ->
                    com.thormakk.keyarmor.security.KeyFingerprint.of(d.heirPublicKeyB64)
                        ?.let { d.heirEmail to it }
                }.toMap(),
            pendingKeyConfirmations = buildPendingKeyConfirmations(),
            inactivityDays      = inheritancePrefs.getInactivityDays(),
            isDriveNotConnected = ownerEmail == null,
            isPremium           = planPrefs.effectivePlan() != UserPlan.FREE,
            pendingFirstSeal    = inheritancePrefs.isPendingFirstSeal(),
            ownerEmailAsHeir    = inheritancePrefs.getHeirOfOwnerEmail(),
            heirAcceptedAt      = inheritancePrefs.getHeirOfOwnerAcceptedAt(),
            heirOfOwnerConfirmedAt = inheritancePrefs.getHeirOfOwnerConfirmedAt(),
            heirInheritedAt     = inheritancePrefs.getHeirOfOwnerInheritedAt(),
            isIntroExpanded     = inheritancePrefs.isIntroExpanded(),
            wasDisarmedByDowngrade = inheritancePrefs.wasDisarmedByDowngrade(),
            wasDisarmedByAlreadyInherited = inheritancePrefs.wasDisarmedByAlreadyInherited(),
        )
    }

    fun getRecoveryCode(): String? = heirIdentityManager.getPrivateCode()

    fun getMyKeyFingerprint(): String? =
        com.thormakk.keyarmor.security.KeyFingerprint.of(heirIdentityManager.getPublicKey())

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    fun setIntroExpanded(expanded: Boolean) {
        inheritancePrefs.setIntroExpanded(expanded)
        _uiState.value = _uiState.value.copy(isIntroExpanded = expanded)
    }

    @Suppress("DEPRECATION")
    private suspend fun tryGetOwnerIdToken(): String? =
        getFirebaseIdentityTokenOrNull(context)?.idToken

    fun setEnabled(enabled: Boolean, ownerIdTokenFromCaller: String? = null) {
        inheritancePrefs.setEnabled(enabled)
        if (enabled) inheritancePrefs.setDisarmedByDowngrade(false)
        _uiState.value = _uiState.value.copy(
            isEnabled              = enabled,
            wasDisarmedByDowngrade = if (enabled) false else _uiState.value.wasDisarmedByDowngrade,
        )

        if (enabled) {
            HeartbeatWorker.cancel(context)
            val ownerEmail = drivePrefs.getConnectedEmail() ?: return
            viewModelScope.launch {
                if (ownerIdTokenFromCaller != null) {
                    viewModelScope.launch { establishFirebaseSession(ownerIdTokenFromCaller) }
                }
                val ownerIdToken = ownerIdTokenFromCaller ?: tryGetOwnerIdToken()
                val publicKeyBase64 = runCatching {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                        inheritanceKeyManager.getOrCreatePublicKeyBase64()
                    }
                }
                    .onFailure { Log.w(TAG, "setEnabled: no se pudo generar/leer la clave pública RSA — ${it.message}", it) }
                    .getOrNull()
                runCatching { inheritanceRepo.upsertOwner(email = ownerEmail, publicKeyBase64 = publicKeyBase64, ownerIdToken = ownerIdToken) }
                    .onSuccess {
                        if (inheritancePrefs.getHeirEncryptionData().isNotEmpty()) {
                            val tieneContenido = runCatching { heirVaultEncryptionRepository.hasAnyEntries() }
                                .getOrDefault(false)
                            if (tieneContenido) {
                                inheritancePrefs.setPendingFirstSeal(false)
                                _uiState.value = _uiState.value.copy(pendingFirstSeal = false)
                                runCatching { heirVaultEncryptionRepository.reEncryptAll() }
                                    .onFailure { Log.w(TAG, "sellado inmediato al reactivar falló (se reintenta en el próximo heartbeat): ${it.message}") }
                            } else {
                                inheritancePrefs.setPendingFirstSeal(true)
                                _uiState.value = _uiState.value.copy(pendingFirstSeal = true)
                            }
                        }
                    }
                    .onFailure {
                        Log.w(TAG, "upsertOwner al habilitar falló: ${it.message}")
                        inheritancePrefs.setEnabled(false)
                        _uiState.value = _uiState.value.copy(isEnabled = false)
                        _events.send(InheritanceEvent.Error(context.getString(R.string.inheritance_enable_failed)))
                    }
            }
        } else {
            HeartbeatWorker.cancel(context)
            val ownerEmail = drivePrefs.getConnectedEmail() ?: return
            viewModelScope.launch {
                runCatching { inheritanceRepo.disableOwner(ownerEmail = ownerEmail) }
                    .onFailure { Log.w(TAG, "disableOwner falló: ${it.message}") }
            }
        }
    }

    fun sealNow() {
        if (inheritancePrefs.getHeirEncryptionData().isEmpty()) return
        viewModelScope.launch {
            val tieneContenido = runCatching { heirVaultEncryptionRepository.hasAnyEntries() }.getOrDefault(false)
            if (!tieneContenido) {
                _events.send(InheritanceEvent.Error(context.getString(R.string.inheritance_empty_vault_notice_body)))
                return@launch
            }
            val resultado = runCatching { heirVaultEncryptionRepository.reEncryptAll() }
            if (resultado.isSuccess) {
                inheritancePrefs.setPendingFirstSeal(false)
                _uiState.value = _uiState.value.copy(pendingFirstSeal = false)
            } else {
                _events.send(InheritanceEvent.Error(context.getString(R.string.inheritance_enable_failed)))
            }
        }
    }

    fun setInactivityDays(days: Int) {
        inheritancePrefs.setInactivityDays(days)
        _uiState.value = _uiState.value.copy(inactivityDays = days)
        if (inheritancePrefs.isEnabled()) {
            val ownerEmail = drivePrefs.getConnectedEmail() ?: return
            viewModelScope.launch {
                val ownerIdToken = tryGetOwnerIdToken()
                runCatching { inheritanceRepo.upsertOwner(email = ownerEmail, inactivityDays = days, ownerIdToken = ownerIdToken) }
                    .onFailure { Log.w(TAG, "upsertOwner al cambiar inactividad falló: ${it.message}") }
            }
        }
    }

    fun addHeirEmail(rawEmail: String) {
        val email = rawEmail.trim().lowercase()
        val added = inheritancePrefs.addHeirEmail(email)
        if (!added) return
        reload()
        viewModelScope.launch { _events.send(InheritanceEvent.HeirAdded) }
        if (inheritancePrefs.isEnabled()) {
            val ownerEmail = drivePrefs.getConnectedEmail() ?: return
            viewModelScope.launch {
                val ownerIdToken = tryGetOwnerIdToken()
                val publicKeyBase64 = runCatching {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                        inheritanceKeyManager.getOrCreatePublicKeyBase64()
                    }
                }
                    .onFailure { Log.w(TAG, "addHeirEmail: no se pudo generar/leer la clave pública RSA — ${it.message}", it) }
                    .getOrNull()
                runCatching { inheritanceRepo.upsertOwner(email = ownerEmail, publicKeyBase64 = publicKeyBase64, ownerIdToken = ownerIdToken) }
                    .onSuccess { rechazados ->
                        if (rechazados.any { it.trim().lowercase() == email }) {
                            Log.w(TAG, "addHeirEmail: designación rechazada — ya es heredero de otro titular")
                            inheritancePrefs.removeHeirEmail(email)
                            reload()
                            _events.send(InheritanceEvent.Error(
                                context.getString(R.string.inheritance_heir_already_taken, email)
                            ))
                        }
                    }
                    .onFailure {
                        Log.w(TAG, "upsertOwner tras addHeirEmail falló: ${it.message}", it)
                        _events.send(InheritanceEvent.Error(it.message ?: context.getString(R.string.inheritance_notif_sync_error_fallback)))
                    }
            }
        }
    }

    private fun buildPendingKeyConfirmations(): List<PendingHeirKeyConfirmation> {
        val anteriores = inheritancePrefs.getHeirEncryptionData()
            .associate { it.heirEmail.lowercase().trim() to it.heirPublicKeyB64 }
        return inheritancePrefs.getPendingHeirKeys().mapNotNull { (email, key) ->
            val huella = com.thormakk.keyarmor.security.KeyFingerprint.of(key) ?: return@mapNotNull null
            val previa = (anteriores[email] ?: inheritancePrefs.getConfirmedHeirKey(email))
                ?.takeIf { it.isNotBlank() }
                ?.let { com.thormakk.keyarmor.security.KeyFingerprint.of(it) }
            PendingHeirKeyConfirmation(heirEmail = email, fingerprint = huella, previousFingerprint = previa)
        }.sortedBy { it.heirEmail }
    }

    fun confirmHeirKey(heirEmail: String) {
        val key = inheritancePrefs.getPendingHeirKey(heirEmail) ?: return
        inheritancePrefs.confirmHeirKey(heirEmail, key)
        reload()
        viewModelScope.launch {
            fcmPrefs.pendingHeirAcceptedEmail  = heirEmail
            fcmPrefs.pendingHeirAcceptedPubKey = key
            fcmEventBus.emitHeirAccepted(heirEmail, null, key)
        }
        val ownerEmail = drivePrefs.getConnectedEmail()
        if (ownerEmail != null) {
            viewModelScope.launch {
                val ownerIdToken = tryGetOwnerIdToken()
                if (ownerIdToken != null) {
                    runCatching { inheritanceRepo.confirmHeirFingerprint(ownerIdToken, ownerEmail, heirEmail) }
                        .onFailure { Log.w(TAG, "confirmHeirFingerprint falló: ${it.message}", it) }
                }
            }
        }
    }

    fun removeHeirEmail(email: String) {
        inheritancePrefs.removeHeirEmail(email)
        inheritancePrefs.removeHeirEncryptionEntry(email)
        inheritancePrefs.clearHeirKeyState(email)
        fcmEventBus.emitHeirLocallyRemoved(email)
        reload()
        appNotificationController.show(context.getString(R.string.inheritance_event_heir_removed, email))
        if (inheritancePrefs.isEnabled()) {
            val ownerEmail = drivePrefs.getConnectedEmail() ?: return
            viewModelScope.launch {
                val ownerIdToken = tryGetOwnerIdToken()
                runCatching { inheritanceRepo.upsertOwner(email = ownerEmail, ownerIdToken = ownerIdToken) }
                    .onFailure { e ->
                        Log.w(TAG, "upsertOwner tras removeHeirEmail falló: ${e.message}", e)
                        _events.send(InheritanceEvent.Error(
                            context.getString(R.string.inheritance_remove_heir_pending_toast)
                        ))
                        runCatching {
                            inAppNotificationRepository.save(
                                NotificationType.INHERITANCE_ERROR,
                                context.getString(R.string.inheritance_notif_remove_heir_pending_title),
                                context.getString(R.string.inheritance_notif_remove_heir_pending_body, email)
                            )
                        }
                    }
            }
        }
    }

    fun resignAsHeir() {
        val ownerEmail = _uiState.value.ownerEmailAsHeir ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val idToken = getFirebaseIdentityTokenOrNull(context)?.idToken
                    ?: throw Exception(context.getString(R.string.inheritance_notif_id_token_error))
                inheritanceRepo.resignAsHeir(heirIdToken = idToken, ownerEmail = ownerEmail)
                inheritancePrefs.clearHeirOfOwnerEmail()
                reload()
                _events.send(InheritanceEvent.HeirResignedSuccess(ownerEmail))
            } catch (e: Exception) {
                Log.e(TAG, "resignAsHeir falló: ${e.message}", e)
                _events.send(InheritanceEvent.Error(e.message ?: context.getString(R.string.inheritance_notif_generic_error)))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun dismissHeirReceivedCard() {
        inheritancePrefs.clearHeirOfOwnerEmail()
        reload()
    }

    fun dismissDisarmedNotice() {
        inheritancePrefs.setDisarmedByDowngrade(false)
        _uiState.value = _uiState.value.copy(wasDisarmedByDowngrade = false)
    }

    fun dismissAlreadyInheritedNotice() {
        inheritancePrefs.setDisarmedByAlreadyInherited(false)
        _uiState.value = _uiState.value.copy(wasDisarmedByAlreadyInherited = false)
    }

    companion object {
        private const val TAG = "InheritanceViewModel"
        private val OWNER_POLL_THROTTLE_MS =
            5 * 60 * 1000L
    }
}
