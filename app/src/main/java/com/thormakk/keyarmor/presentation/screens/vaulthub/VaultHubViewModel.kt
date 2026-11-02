@file:OptIn(kotlinx.coroutines.FlowPreview::class)
package com.thormakk.keyarmor.presentation.screens.vaulthub

import com.thormakk.keyarmor.presentation.util.ServiceIconProvider
import android.content.Context
import com.thormakk.keyarmor.presentation.util.extractGoogleSubFromJwt
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.firebase.messaging.FirebaseMessaging
import com.thormakk.keyarmor.MainActivity
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.AppUpdatePrefs
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.FamilyPrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.FamilyStatusRepository
import com.thormakk.keyarmor.data.repository.FcmEventBus
import com.thormakk.keyarmor.data.repository.FcmRepository
import com.thormakk.keyarmor.data.repository.CancelGraceResult
import com.thormakk.keyarmor.data.repository.HeirInheritanceRepository
import com.thormakk.keyarmor.data.repository.HeirPrivateKeyInvalidException
import com.thormakk.keyarmor.data.repository.InheritanceOriginUnverifiedException
import com.thormakk.keyarmor.data.repository.HeirPrivateKeyRequiredException
import com.thormakk.keyarmor.data.repository.HeirVaultEncryptionRepository
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.data.repository.InheritanceRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.ReEncryptAllResult
import com.thormakk.keyarmor.data.repository.ReEncryptFailureReason
import com.thormakk.keyarmor.data.repository.ReminderRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.AttachmentRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.NotificationType
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.usecase.ConsolidateVaultUseCase
import com.thormakk.keyarmor.domain.usecase.DeleteVaultUseCase
import com.thormakk.keyarmor.domain.usecase.HealOrphanEntriesUseCase
import com.thormakk.keyarmor.domain.usecase.IsVaultEmptyUseCase
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import com.thormakk.keyarmor.security.SessionManager
import com.thormakk.keyarmor.workers.CancelGraceWorker
import com.thormakk.keyarmor.workers.HeartbeatWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

data class PinnedHubEntry(
    val id:            String,
    val type:          HubEntryType,
    val displayTitle:  String,
    val url:           String = "",
    val strengthScore: Int    = -1,
    val avatarLabel:   String = ""
)

data class VaultHubUiState(
    val passwordCount:     Int          = 0,
    val bankAccountCount:  Int          = 0,
    val customEntryCount:  Int          = 0,
    val secureNoteCount:   Int          = 0,
    val reminderCount:     Int          = 0,
    val creditCardCount:   Int          = 0,
    val twoFactorCount:    Int          = 0,
    val cryptoWalletCount: Int          = 0,
    val passkeyCount:          Int          = 0,
    val identityDocumentCount: Int          = 0,
    val vehicleCount:          Int          = 0,
    val weakPasswordCount:   Int          = 0,
    val mediumPasswordCount: Int          = 0,
    val strongPasswordCount: Int          = 0,
    val showWhatsNew:      Boolean       = false,
    val isFreeUser:        Boolean       = true,
    val driveAuthForInheritanceIntent: android.content.Intent? = null,
    val driveAuthForEncryptIntent: android.content.Intent? = null,
    val showInheritanceRecoveryPrompt: Boolean = false,
    val inheritanceRecoveryKeyError: Boolean = false,
    val inheritanceImportErrorRes: Int? = null,
    val availableVaults: List<VaultMetadata> = emptyList(),
    val activeVaultId: String = VaultMetadata.PRIMARY_VAULT_ID,
    val isActiveVaultReadOnly: Boolean = false,
    val pinnedEntries: List<PinnedHubEntry> = emptyList(),
    val memberRevokedSnackbar: Boolean = false,
    val memberRevokedAdminEmail: String? = null,
    val memberPremiumRevokedSnackbar: Boolean = false,
    val showDriveReconnectNotice: Boolean = false,
)

private val HEIR_POLL_THROTTLE_MS =
    7L * 24 * 60 * 60 * 1000
private val MEMBER_STATUS_POLL_THROTTLE_MS =
    24L * 60 * 60 * 1000
private val OWNER_RESUME_THROTTLE_MS =
    7L * 24 * 60 * 60 * 1000

@HiltViewModel
class VaultHubViewModel @Inject constructor(
    private val vaultRepository:        VaultRepository,
    private val bankAccountRepository:  BankAccountRepository,
    private val customEntryRepository:  CustomEntryRepository,
    private val secureNoteRepository:   SecureNoteRepository,
    private val creditCardRepository:   CreditCardRepository,
    private val twoFactorRepository:    TwoFactorRepository,
    private val cryptoWalletRepository: CryptoWalletRepository,
    private val passkeyRepository:      PasskeyRepository,
    private val identityDocumentRepository:  IdentityDocumentRepository,
    private val vehicleRepository:           VehicleRepository,
    private val reminderRepository:          ReminderRepository,
    private val planPrefs:                   PlanPrefs,
    private val consolidateVaultUseCase:     ConsolidateVaultUseCase,
    private val deleteVaultUseCase:          DeleteVaultUseCase,
    private val healOrphanEntriesUseCase:    HealOrphanEntriesUseCase,
    private val isVaultEmptyUseCase:         IsVaultEmptyUseCase,
    appUpdatePrefs:                          AppUpdatePrefs,
    private val sessionManager:              SessionManager,
    private val heirVaultEncryptionRepository: HeirVaultEncryptionRepository,
    private val heirInheritanceRepository:     HeirInheritanceRepository,
    private val drivePrefs:                    DrivePrefs,
    private val panicPinPrefs:                 PanicPinPrefs,
    private val travelModePrefs:               com.thormakk.keyarmor.data.prefs.TravelModePrefs,
    private val vaultMetadataRepository:       VaultMetadataRepository,
    private val attachmentRepository:          AttachmentRepository,
    private val activeVaultPrefs:              ActiveVaultPrefs,
    private val inheritancePrefs:              InheritancePrefs,
    private val inheritanceRepository:         InheritanceRepository,
    private val heirIdentity:                  com.thormakk.keyarmor.security.HeirIdentityManager,
    private val pinnedHubPrefs:                PinnedHubPrefs,
    private val fcmRepository:                 FcmRepository,
    private val fcmEventBus:                   FcmEventBus,
    private val fcmPrefs:                      FcmPrefs,
    private val familyPrefs:                   FamilyPrefs,
    private val familyStatusRepository:        FamilyStatusRepository,
    @ApplicationContext private val contextInyectado: Context,
    private val appNotificationController: com.thormakk.keyarmor.notifications.AppNotificationController,
    private val appConsentController:       com.thormakk.keyarmor.notifications.AppConsentController,
    private val inAppNotificationRepository: InAppNotificationRepository,
) : ViewModel() {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    private val _uiState = MutableStateFlow(run {
        val isFree   = planPrefs.effectivePlan() == UserPlan.FREE
        val activeId = activeVaultPrefs.activeVaultId
        VaultHubUiState(
            isFreeUser            = isFree,
            activeVaultId         = activeId,
            isActiveVaultReadOnly = activeVaultPrefs.activeVaultIsInherited ||
                                    (isFree && activeId != VaultMetadata.PRIMARY_VAULT_ID)
        )
    })
    val uiState: StateFlow<VaultHubUiState> = _uiState

    private val processedHeirAcceptances = mutableSetOf<String>()
    private val importingInheritance = mutableSetOf<String>()

    init {
        observarBovedaActiva()

        if (!panicPinPrefs.coercionActiveFlow.value && drivePrefs.consumeDisconnectedByPanicPin()) {
            _uiState.value = _uiState.value.copy(showDriveReconnectNotice = true)
        }


        checkPendingHeirFromPrefs()

        checkPendingFamilyInviteFromPrefs()

        viewModelScope.launch {
            fcmEventBus.heirConsentEvent.collect { _ ->
                appConsentController.requestConsent()
            }
        }

        viewModelScope.launch {
            fcmEventBus.memberPremiumRevoked.collect {
                _uiState.value = _uiState.value.copy(memberPremiumRevokedSnackbar = true)
                inAppNotificationRepository.save(
                    NotificationType.FAMILY_PREMIUM_REVOKED,
                    context.getString(com.thormakk.keyarmor.R.string.family_premium_revoked_notif_title),
                    context.getString(com.thormakk.keyarmor.R.string.family_premium_revoked_notif_body)
                )
            }
        }

        viewModelScope.launch {
            fcmEventBus.memberAccessRevoked.collect { adminEmail ->
                _uiState.value = _uiState.value.copy(
                    memberRevokedSnackbar = true,
                    memberRevokedAdminEmail = adminEmail
                )
                val body = if (!adminEmail.isNullOrBlank())
                    context.getString(com.thormakk.keyarmor.R.string.family_access_revoked_notif_body_named, adminEmail)
                else
                    context.getString(com.thormakk.keyarmor.R.string.family_access_revoked_notif_body)
                inAppNotificationRepository.save(
                    NotificationType.FAMILY_ACCESS_REVOKED,
                    context.getString(com.thormakk.keyarmor.R.string.family_access_revoked_notif_title),
                    body
                )
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirAcceptedEvent.collect { payload ->
                fcmPrefs.clearPendingHeirAccepted()
                handleHeirAccepted(payload.heirEmail, payload.heirPublicKeyBase64)
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirLocallyRemovedEvent.collect { heirEmail ->
                processedHeirAcceptances.remove(heirEmail)
            }
        }

        viewModelScope.launch {
            fcmEventBus.heirDeclinedEvent.collect { heirEmail ->
                fcmPrefs.clearPendingHeirDeclined()
                inheritancePrefs.removeHeirEmail(heirEmail)
                inheritancePrefs.removeHeirEncryptionEntry(heirEmail)
                if (inheritancePrefs.getHeirEncryptionData().isEmpty()) {
                    HeartbeatWorker.cancel(context)
                }
                val ownerEmail = drivePrefs.getConnectedEmail()
                if (ownerEmail != null && inheritancePrefs.getHeirEncryptionData().isNotEmpty()) {
                    runCatching { inheritanceRepository.upsertOwner(email = ownerEmail) }
                }
                appNotificationController.show(context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_heir_declined_snackbar, heirEmail))
                inAppNotificationRepository.save(
                    NotificationType.HEIR_DECLINED,
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_heir_declined_title),
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_heir_declined_body, heirEmail)
                )
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (!inheritancePrefs.isApproachingGracePeriod()) return@launch
            val ownerEmail = drivePrefs.getConnectedEmail() ?: return@launch
            val ownerIdToken = tryGetOwnerIdToken()
            runCatching { inheritanceRepository.cancelGrace(ownerEmail, ownerIdToken) }
                .onSuccess { resultado ->
                    if (resultado == CancelGraceResult.ALREADY_INHERITED) {
                        Log.e("VaultHubVM", "cancelGrace: la herencia YA se entregó — no se revierte")
                        appNotificationController.show(
                            context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_already_inherited_snackbar)
                        )
                        inAppNotificationRepository.save(
                            NotificationType.INHERITANCE_ERROR,
                            context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_already_inherited_title),
                            context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_already_inherited_body)
                        )
                        return@onSuccess
                    }
                    inheritancePrefs.setLastHeartbeatSentAt(System.currentTimeMillis())
                    CancelGraceWorker.cancelRetries(context)
                    HeartbeatWorker.cancelBackup(context)
                    if (resultado == CancelGraceResult.CANCELLED) {
                        appNotificationController.show(context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_grace_cancelled_snackbar))
                        inAppNotificationRepository.save(
                            NotificationType.INHERITANCE_SYNC,
                            context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_grace_cancelled_title),
                            context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_grace_cancelled_body)
                        )
                    }
                }
                .onFailure {
                    CancelGraceWorker.scheduleRetries(context)
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (!inheritancePrefs.isEnabled()) return@launch
            if (inheritancePrefs.getHeirEncryptionData().isEmpty()) return@launch
            val ownerEmail = drivePrefs.getConnectedEmail() ?: return@launch
            val lastSentAt = inheritancePrefs.getLastHeartbeatSentAt()
            val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
            if (lastSentAt > 0L && System.currentTimeMillis() - lastSentAt < thirtyDaysMs) return@launch
            runCatching { inheritanceRepository.sendHeartbeat(ownerEmail) }
                .onSuccess {
                    val result = runCatching { heirVaultEncryptionRepository.reEncryptAll() }.getOrNull()
                    if (result != null && result.attempted) {
                        notifyReEncryptResult(result)
                    }
                }
                .onFailure {
                    HeartbeatWorker.scheduleBackup(context)
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (inheritancePrefs.wasHeirPinWarned()) return@launch
            if (!inheritancePrefs.hasHeirIdentity()) return@launch
            val titular = inheritancePrefs.getHeirOfOwnerEmail() ?: return@launch
            if (inheritancePrefs.getPinnedOwnerSigningKey(titular) != null) return@launch
            inheritancePrefs.setHeirPinWarned(true)
            inAppNotificationRepository.save(
                NotificationType.INHERITANCE_ERROR,
                context.getString(com.thormakk.keyarmor.R.string.inheritance_heir_pin_missing_title),
                context.getString(com.thormakk.keyarmor.R.string.inheritance_heir_pin_missing_body, titular)
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (inheritancePrefs.isEnabled()) return@launch
            val now = System.currentTimeMillis()
            if (now - inheritancePrefs.getLastOwnerResumeCheckAt() < OWNER_RESUME_THROTTLE_MS) return@launch

            val idToken = getFirebaseIdentityTokenOrNull(context)?.idToken ?: return@launch

            val result = try {
                inheritanceRepository.resumeOwnerInheritance(idToken)
            } catch (e: Exception) {
                Log.w("VaultHubVM", "resumeOwnerInheritance falló (se reintenta): ${e.message}")
                return@launch
            } ?: return@launch
            inheritancePrefs.setLastOwnerResumeCheckAt(now)

            if (!result.hasInheritance) return@launch

            if (result.status == "inherited") {
                inAppNotificationRepository.save(
                    NotificationType.INHERITANCE_ERROR,
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_inherited_reinstall_title),
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_inherited_reinstall_body)
                )
                return@launch
            }

            result.inactivityDays?.let { inheritancePrefs.setInactivityDays(it) }
            result.heirs.forEach { h ->
                inheritancePrefs.addHeirEmail(h.email)
                if (!h.publicKey.isNullOrEmpty()) {
                    inheritancePrefs.setPendingHeirKey(h.email, h.publicKey)
                    fcmEventBus.emitHeirKeyPendingConfirmation(h.email)
                }
            }
            appNotificationController.show(
                context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_disarmed_reinstall_title)
            )
            inAppNotificationRepository.save(
                NotificationType.INHERITANCE_ERROR,
                context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_disarmed_reinstall_title),
                context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_disarmed_reinstall_body)
            )
        }

        viewModelScope.launch {
            combine(
                combine(
                    vaultRepository.observeEntries(),
                    bankAccountRepository.observeAccounts()
                ) { passwords, banks -> passwords to banks.size },
                combine(
                    customEntryRepository.observeEntries(),
                    secureNoteRepository.observeNotes()
                ) { c, n -> c.size to n.size },
                creditCardRepository.observeCards(),
                combine(
                    twoFactorRepository.observeAccounts(),
                    cryptoWalletRepository.observeWallets()
                ) { tf, cw -> tf.size to cw.size },
                combine(
                    passkeyRepository.observePasskeys(),
                    combine(
                        identityDocumentRepository.observeDocuments(),
                        vehicleRepository.observeVehicles()
                    ) { idList, vList -> idList.size to vList.size }
                ) { pkList, idVeh -> pkList.size to idVeh }
            ) { (passwords, bCount), (cCount, nCount), creditCards, (tfCount, cwCount), (pkCount, idVeh) ->
                val (idCount, vCount) = idVeh
                val withPassword = passwords.filter { it.password.isNotEmpty() }
                val weakEntries = withPassword.filter { PasswordStrengthCalculator.calculate(it.password) <= 1 }.toSet()
                val mediumCount = withPassword.count { entry ->
                    entry !in weakEntries && PasswordStrengthCalculator.calculate(entry.password) == 2
                }
                val strongCount = withPassword.size - weakEntries.size - mediumCount
                _uiState.value = _uiState.value.copy(
                    passwordCount         = passwords.size,
                    weakPasswordCount     = weakEntries.size,
                    mediumPasswordCount   = mediumCount,
                    strongPasswordCount   = strongCount,
                    bankAccountCount      = bCount,
                    customEntryCount      = cCount,
                    secureNoteCount       = nCount,
                    creditCardCount       = creditCards.size,
                    twoFactorCount        = tfCount,
                    cryptoWalletCount     = cwCount,
                    passkeyCount          = pkCount,
                    identityDocumentCount = idCount,
                    vehicleCount          = vCount,
                )
            }.collect {}
        }

        viewModelScope.launch {
            reminderRepository.observeActive().collect { list ->
                _uiState.value = _uiState.value.copy(reminderCount = list.size)
            }
        }

        viewModelScope.launch {
            planPrefs.planFlow.collect { _ ->
                val isFree = planPrefs.effectivePlan() == UserPlan.FREE
                Log.d("VaultHubVM", "planFlow.collect: effectivePlan=${planPrefs.effectivePlan()}")
                _uiState.value = _uiState.value.copy(
                    isFreeUser            = isFree,
                    isActiveVaultReadOnly = activeVaultPrefs.activeVaultIsInherited ||
                                           (isFree && activeVaultPrefs.activeVaultId != VaultMetadata.PRIMARY_VAULT_ID)
                )
            }
        }

        viewModelScope.launch {
            vaultMetadataRepository.ensurePrimaryVaultExists()
            if (planPrefs.effectivePlan() != UserPlan.FREE) {
                vaultMetadataRepository.ensurePrivateVaultExists()
            } else if (vaultMetadataRepository.getById(VaultMetadata.PRIVATE_VAULT_ID) != null &&
                       isVaultEmptyUseCase(VaultMetadata.PRIVATE_VAULT_ID)) {
                deleteVaultUseCase(
                    vaultId       = VaultMetadata.PRIVATE_VAULT_ID,
                    inheritedFrom = null,
                    sendToTrash   = false
                )
                if (activeVaultPrefs.activeVaultId == VaultMetadata.PRIVATE_VAULT_ID) {
                    activeVaultPrefs.setActiveVault(VaultMetadata.PRIMARY_VAULT_ID)
                }
            }
            vaultMetadataRepository.healSystemVaultFlags()
            healOrphanEntriesUseCase()
            vaultMetadataRepository.stripLegacyPanelPrefix()
            runCatching { attachmentRepository.purgarArchivosHuerfanos() }
                .onFailure { Log.w("VaultHubVM", "No se pudieron purgar los .enc huérfanos", it) }
            vaultMetadataRepository.observeAll().collect { vaults ->
                val isFree = planPrefs.effectivePlan() == UserPlan.FREE
                Log.d("VaultHubVM", "observeAll.collect: effectivePlan=${planPrefs.effectivePlan()} activeMembers=${familyPrefs.getMemberData().count { it.status == "active" }}")
                val rawActiveId = activeVaultPrefs.activeVaultId
                val activeId = if (sessionManager.isUnlocked() && vaults.none { it.id == rawActiveId }) {
                    activeVaultPrefs.resetToPrimary()
                    VaultMetadata.PRIMARY_VAULT_ID
                } else {
                    rawActiveId
                }
                _uiState.value = _uiState.value.copy(
                    availableVaults       = vaults,
                    activeVaultId         = activeId,
                    isActiveVaultReadOnly = activeVaultPrefs.activeVaultIsInherited ||
                                           (isFree && activeId != VaultMetadata.PRIMARY_VAULT_ID)
                )
            }
        }

        viewModelScope.launch {
            val acceptedEmail = fcmPrefs.pendingHeirAcceptedEmail
            if (acceptedEmail != null) {
                val heirPub = fcmPrefs.pendingHeirAcceptedPubKey
                fcmPrefs.clearPendingHeirAccepted()
                handleHeirAccepted(acceptedEmail, heirPub)
            }
            val declinedEmail = fcmPrefs.pendingHeirDeclinedEmail
            if (declinedEmail != null) {
                fcmPrefs.clearPendingHeirDeclined()
                inheritancePrefs.removeHeirEmail(declinedEmail)
                inheritancePrefs.removeHeirEncryptionEntry(declinedEmail)
                inheritancePrefs.clearHeirKeyState(declinedEmail)
                if (inheritancePrefs.getHeirEncryptionData().isEmpty()) {
                    HeartbeatWorker.cancel(context)
                }
                val ownerEmail = drivePrefs.getConnectedEmail()
                if (ownerEmail != null && inheritancePrefs.getHeirEncryptionData().isNotEmpty()) {
                    runCatching { inheritanceRepository.upsertOwner(email = ownerEmail) }
                }
                appNotificationController.show(context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_heir_declined_snackbar, declinedEmail))
                inAppNotificationRepository.save(
                    NotificationType.HEIR_DECLINED,
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_heir_declined_title),
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_heir_declined_body, declinedEmail)
                )
            }
        }

        viewModelScope.launch {
            val ownerEmail  = fcmPrefs.pendingInheritanceOwnerEmail
            val driveFileId = fcmPrefs.pendingInheritanceDriveFileId
            if (ownerEmail != null) {
                fcmPrefs.clearPendingInheritance()
                importInheritedVaultFromPush(ownerEmail, driveFileId)
            }
        }

        viewModelScope.launch {
            fcmEventBus.inheritanceActivatedEvent.collect { payload ->
                importInheritedVaultFromPush(payload.ownerEmail, payload.driveFileId)
            }
        }

        if (fcmPrefs.pendingTokenRefresh || fcmPrefs.lastRegisteredFcmToken == null) {
            viewModelScope.launch { registerFcmTokenIfNeeded() }
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (now - inheritancePrefs.getLastHeirPollAt() < HEIR_POLL_THROTTLE_MS) return@launch

            val account = getFirebaseIdentityTokenOrNull(context) ?: return@launch

            val ownerAsHeir = inheritancePrefs.getHeirOfOwnerEmail()
            if (ownerAsHeir != null) {
                importInheritedVaultFromPush(ownerAsHeir, "", marcarPollAlConcluir = true)
            } else {
                val idToken   = account.idToken.orEmpty()
                if (idToken.isEmpty()) return@launch
                val heirEmail = account.email.orEmpty()
                if (heirEmail.isEmpty()) return@launch
                val activeInfo = try {
                    inheritanceRepository.checkHeirActive(heirEmail, idToken)
                } catch (e: Exception) {
                    Log.w("VaultHubVM", "checkHeirActive falló (se reintenta): ${e.message}")
                    return@launch
                }
                if (activeInfo != null) {
                    inheritancePrefs.setLastHeirPollAt(now)
                    if (!heirIdentity.hasIdentity()) {
                        val codes = runCatching { heirIdentity.getOrCreateIdentity() }.getOrNull()
                        if (codes != null) {
                            val ok = try {
                                inheritanceRepository.republishHeirKey(heirEmail, idToken, codes.publicKeyB64)
                            } catch (e: Exception) {
                                Log.w("VaultHubVM", "republishHeirKey falló (se reintenta en 7 días): ${e.message}")
                                false
                            }
                            if (ok) {
                                inheritancePrefs.setHeirOfOwnerEmail(activeInfo.ownerEmail)
                                inheritancePrefs.setHeirOfOwnerAcceptedAt(System.currentTimeMillis())
                                fcmEventBus.emitHeirConsentAccepted()
                                Log.w("VaultHubVM", "identidad republicada para ${ofuscar(heirEmail)} (titular ${ofuscar(activeInfo.ownerEmail)})")
                            }
                        }
                    } else {
                        inheritancePrefs.setHeirOfOwnerEmail(activeInfo.ownerEmail)
                    }
                    return@launch
                }
                val pending = try {
                    inheritanceRepository.checkHeirPending(heirEmail, idToken)
                } catch (e: Exception) {
                    Log.w("VaultHubVM", "checkHeirPending falló (se reintenta): ${e.message}")
                    return@launch
                }
                inheritancePrefs.setLastHeirPollAt(now)
                if (pending != null) {
                    fcmPrefs.pendingHeirConsentOwnerEmail = pending.ownerEmail
                    fcmPrefs.pendingHeirConsentPublicKey  = pending.publicKeyBase64
                    fcmPrefs.pendingHeirConsentOwnerSigningKey =
                        pending.ownerSigningPublicKeyBase64?.takeIf { it.isNotEmpty() }
                    checkPendingHeirFromPrefs()
                }
            }
        }

        viewModelScope.launch {
            if (planPrefs.getUserPlan() != UserPlan.FREE) return@launch
            if (!familyPrefs.isMember() || !familyPrefs.isPremiumMember) return@launch
            val adminEmail = familyPrefs.getAdminEmail() ?: return@launch
            val now = System.currentTimeMillis()
            if (now - familyPrefs.getLastMemberStatusPollAt() < MEMBER_STATUS_POLL_THROTTLE_MS) return@launch

            val idToken = getFirebaseIdentityTokenOrNull(context)?.idToken ?: return@launch

            val status = try {
                familyStatusRepository.checkMemberStatus(idToken, adminEmail)
            } catch (e: Exception) {
                Log.w("VaultHubVM", "checkMemberStatus falló (se reintenta): ${e.message}")
                return@launch
            }
            familyPrefs.setLastMemberStatusPollAt(now)
            when (status) {
                FamilyStatusRepository.MemberStatus.REVOKED -> {
                    familyPrefs.isPremiumMember = false
                    planPrefs.refresh()
                    fcmEventBus.emitMemberPremiumRevoked()
                }
                FamilyStatusRepository.MemberStatus.REMOVED -> {
                    familyPrefs.clearMemberRole()
                    planPrefs.refresh()
                    fcmEventBus.emitMemberAccessRevoked(adminEmail)
                }
                else -> {   }
            }
        }

        viewModelScope.launch {
            data class RepoBundle(
                val cryptoWallets:  List<com.thormakk.keyarmor.domain.model.CryptoWallet>,
                val secureNotes:    List<com.thormakk.keyarmor.domain.model.SecureNote>,
                val customEntries:  List<com.thormakk.keyarmor.domain.model.CustomEntry>,
                val identityDocs:   List<com.thormakk.keyarmor.domain.model.IdentityDocument>,
                val vehicles:       List<com.thormakk.keyarmor.domain.model.Vehicle>,
                val twoFactorAccounts: List<com.thormakk.keyarmor.domain.model.TwoFactorAccount>
            )
            val pinnedFlow = combine(
                combine(pinnedHubPrefs.pinnedRefsFlow, activeVaultPrefs.activeVaultIdFlow) { allRefs, activeVaultId ->
                    allRefs.filter { it.vaultId == activeVaultId }
                },
                vaultRepository.observeEntries(),
                bankAccountRepository.observeAccounts(),
                creditCardRepository.observeCards(),
                combine(
                    cryptoWalletRepository.observeWallets(),
                    secureNoteRepository.observeNotes(),
                    customEntryRepository.observeEntries(),
                    identityDocumentRepository.observeDocuments(),
                    combine(
                        vehicleRepository.observeVehicles(),
                        twoFactorRepository.observeAccounts()
                    ) { v, tf -> v to tf }
                ) { cw, sn, ce, id, vTf -> RepoBundle(cw, sn, ce, id, vTf.first, vTf.second) }
            ) { refs, passwords, banks, cards, bundle ->
                val resolved = refs.mapNotNull { ref ->
                    when (ref.type) {
                        HubEntryType.PASSWORD ->
                            passwords.find { it.id == ref.id }
                                ?.let { e -> PinnedHubEntry(
                                    id            = ref.id,
                                    type          = ref.type,
                                    displayTitle  = e.title.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_password) },
                                    url           = ServiceIconProvider.bestUrl(e.urls),
                                    strengthScore = PasswordStrengthCalculator.calculate(e.password)
                                ) }
                        HubEntryType.BANK_ACCOUNT ->
                            banks.find { it.id == ref.id }
                                ?.let { e -> PinnedHubEntry(
                                    id           = ref.id,
                                    type         = ref.type,
                                    displayTitle = e.bankName.ifEmpty { e.accountType }.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_bank_account) },
                                    avatarLabel  = e.bankName
                                ) }
                        HubEntryType.CREDIT_CARD ->
                            cards.find { it.id == ref.id }
                                ?.let { e -> PinnedHubEntry(
                                    id           = ref.id,
                                    type         = ref.type,
                                    displayTitle = e.brand.ifEmpty { e.issuer }.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_credit_card) },
                                    avatarLabel  = e.brand.ifEmpty { e.issuer }
                                ) }
                        HubEntryType.CRYPTO_WALLET ->
                            bundle.cryptoWallets.find { it.id == ref.id }
                                ?.let { e -> PinnedHubEntry(
                                    id           = ref.id,
                                    type         = ref.type,
                                    displayTitle = e.asset.ifEmpty { e.network }.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_crypto_wallet) },
                                    avatarLabel  = e.asset.ifEmpty { e.network }
                                ) }
                        HubEntryType.SECURE_NOTE ->
                            bundle.secureNotes.find { it.id == ref.id }
                                ?.let { e -> PinnedHubEntry(ref.id, ref.type,
                                    e.title.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_secure_note) },
                                    avatarLabel = e.title.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_secure_note) }) }
                        HubEntryType.CUSTOM_ENTRY ->
                            bundle.customEntries.find { it.id == ref.id }
                                ?.let { e -> PinnedHubEntry(ref.id, ref.type,
                                    e.title.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_custom_entry) },
                                    avatarLabel = e.title.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_custom_entry) }) }
                        HubEntryType.IDENTITY_DOCUMENT ->
                            bundle.identityDocs.find { it.id == ref.id }
                                ?.let { e -> PinnedHubEntry(ref.id, ref.type,
                                    e.holderName.ifEmpty { e.docType }.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_identity_document) },
                                    avatarLabel = e.holderName.ifEmpty { e.docType }.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_identity_document) }) }
                        HubEntryType.VEHICLE ->
                            bundle.vehicles.find { it.id == ref.id }
                                ?.let { e ->
                                    val name = listOf(e.make, e.model).filter { s -> s.isNotBlank() }.joinToString(" ").ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_vehicle) }
                                    PinnedHubEntry(ref.id, ref.type, name, avatarLabel = name)
                                }
                        HubEntryType.TWO_FACTOR ->
                            bundle.twoFactorAccounts.find { it.id == ref.id }
                                ?.let { e ->
                                    val name = e.name.ifEmpty { e.issuer }.ifEmpty { context.getString(com.thormakk.keyarmor.R.string.hub_pinned_fallback_two_factor) }
                                    PinnedHubEntry(ref.id, ref.type, name, avatarLabel = name)
                                }
                    }
                }
                refs to resolved
            }.shareIn(this, SharingStarted.WhileSubscribed(5_000), replay = 1)

            launch {
                pinnedFlow.collect { (_, resolved) ->
                    _uiState.value = _uiState.value.copy(pinnedEntries = resolved)
                }
            }

            launch {
                pinnedFlow.debounce(600).collect { (refs, resolved) ->
                    if (refs.isEmpty()) return@collect
                    if (resolved.size == refs.size) return@collect
                    if (!sessionManager.isUnlocked()) return@collect
                    if (travelModePrefs.travelModeFlow.value || panicPinPrefs.coercionActiveFlow.value) return@collect
                    val resolvedIds = resolved.mapTo(HashSet()) { it.id }
                    refs.forEach { ref -> if (ref.id !in resolvedIds) pinnedHubPrefs.unpin(ref.id) }
                }
            }
        }

    }

    private fun handleHeirAccepted(heirEmail: String, heirPublicKeyBase64: String?) {
        if (heirPublicKeyBase64 == null) {
            Log.w("VaultHubVM", "handleHeirAccepted: heirPublicKeyBase64 es null para ${ofuscar(heirEmail)} — ignorando (v4 requiere la pública del heredero)")
            return
        }
        val confirmedKey = inheritancePrefs.getConfirmedHeirKey(heirEmail)
        if (confirmedKey != heirPublicKeyBase64) {
            val pinnedKey = inheritancePrefs.getHeirEncryptionData()
                .firstOrNull { it.heirEmail == heirEmail }?.heirPublicKeyB64
            val esCambio = !confirmedKey.isNullOrBlank() || !pinnedKey.isNullOrBlank()
            val yaPendiente = inheritancePrefs.getPendingHeirKey(heirEmail) == heirPublicKeyBase64
            inheritancePrefs.setPendingHeirKey(heirEmail, heirPublicKeyBase64)
            fcmEventBus.emitHeirKeyPendingConfirmation(heirEmail)
            Log.w("VaultHubVM", "handleHeirAccepted: clave de ${ofuscar(heirEmail)} sin confirmar — sellado en espera (cambio=$esCambio)")
            if (!yaPendiente) {
                viewModelScope.launch {
                    val huellaNueva = com.thormakk.keyarmor.security.KeyFingerprint.of(heirPublicKeyBase64)
                    val huellaVieja = pinnedKey?.takeIf { it.isNotBlank() }
                        ?.let { com.thormakk.keyarmor.security.KeyFingerprint.of(it) }
                        ?: confirmedKey?.takeIf { it.isNotBlank() }
                            ?.let { com.thormakk.keyarmor.security.KeyFingerprint.of(it) }
                    val titulo = if (esCambio)
                        context.getString(com.thormakk.keyarmor.R.string.inheritance_heir_key_changed_title)
                    else
                        context.getString(com.thormakk.keyarmor.R.string.inheritance_heir_key_confirm_title)
                    val cuerpo = buildString {
                        if (esCambio) {
                            append(context.getString(com.thormakk.keyarmor.R.string.inheritance_heir_key_changed_body, heirEmail))
                            if (huellaNueva != null && huellaVieja != null) {
                                append("\n\n")
                                append(context.getString(
                                    com.thormakk.keyarmor.R.string.inheritance_heir_key_changed_fingerprints,
                                    huellaVieja, huellaNueva
                                ))
                            }
                        } else {
                            append(context.getString(
                                com.thormakk.keyarmor.R.string.inheritance_heir_key_confirm_body,
                                heirEmail
                            ))
                            append("\n\n")
                            append(huellaNueva ?: "—")
                        }
                    }
                    appNotificationController.show(titulo)
                    inAppNotificationRepository.save(NotificationType.INHERITANCE_ERROR, titulo, cuerpo)
                }
            }
            return
        }
        if (!processedHeirAcceptances.add(heirEmail)) {
            Log.d("VaultHubVM", "handleHeirAccepted: $heirEmail ya fue procesado en esta sesión — skip")
            return
        }
        viewModelScope.launch {
            try {
                heirVaultEncryptionRepository.encryptAndUpload(heirEmail, heirPublicKeyBase64)
                fcmEventBus.emitHeirVaultEncrypted(heirEmail)
                appNotificationController.show(context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_heir_accepted_snackbar, heirEmail))
                inAppNotificationRepository.save(
                    NotificationType.HEIR_ACCEPTED,
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_heir_accepted_title),
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_heir_accepted_body, heirEmail)
                )
                Log.d("VaultHubVM", "handleHeirAccepted: vault cifrado y subido para $heirEmail")
            } catch (e: UserRecoverableAuthIOException) {
                processedHeirAcceptances.remove(heirEmail)
                _uiState.value = _uiState.value.copy(driveAuthForEncryptIntent = e.intent)
                fcmPrefs.pendingHeirAcceptedEmail  = heirEmail
                fcmPrefs.pendingHeirAcceptedPubKey = heirPublicKeyBase64
                Log.w("VaultHubVM", "handleHeirAccepted: Drive re-auth requerida para ${ofuscar(heirEmail)}")
            } catch (e: Exception) {
                processedHeirAcceptances.remove(heirEmail)
                Log.e("VaultHubVM", "handleHeirAccepted: error al cifrar vault para ${ofuscar(heirEmail)}", e)
                appNotificationController.show(context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_prepare_failed_snackbar, heirEmail))
                inAppNotificationRepository.save(
                    NotificationType.INHERITANCE_ERROR,
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_prepare_failed_title),
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_prepare_failed_body, heirEmail)
                )
                fcmPrefs.pendingHeirAcceptedEmail  = heirEmail
                fcmPrefs.pendingHeirAcceptedPubKey = heirPublicKeyBase64
            }
        }
    }

    private suspend fun notifyReEncryptResult(result: ReEncryptAllResult) {
        if (result.failed.isEmpty()) {
            if (result.succeeded.isEmpty()) return
            val body = if (result.succeeded.size == 1)
                context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_reencrypt_success_single, result.succeeded.first())
            else
                context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_reencrypt_success_plural, result.succeeded.joinToString(", "))
            inAppNotificationRepository.save(
                NotificationType.INHERITANCE_SYNC,
                context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_reencrypt_success_title),
                body
            )
            return
        }

        val details = result.failed.joinToString("\n\n") { failure ->
            when (failure.reason) {
                ReEncryptFailureReason.LOCAL_KEY_LOST ->
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_reencrypt_fail_key_lost, failure.heirEmail, failure.heirEmail)
                ReEncryptFailureReason.UPLOAD_ERROR ->
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_reencrypt_fail_upload, failure.heirEmail)
                ReEncryptFailureReason.SIGNING_KEY_LOST ->
                    context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_reencrypt_fail_signing_key_lost, failure.heirEmail)
            }
        }
        val title = if (result.failed.size == 1)
            context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_reencrypt_fail_title_single)
        else
            context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_reencrypt_fail_title_plural, result.failed.size)
        inAppNotificationRepository.save(NotificationType.INHERITANCE_ERROR, title, details)
    }

    private suspend fun tryGetOwnerIdToken(): String? =
        getFirebaseIdentityTokenOrNull(context)?.idToken

    private fun importInheritedVaultFromPush(
        ownerEmail: String,
        driveFileId: String?,
        marcarPollAlConcluir: Boolean = false,
    ) {
        if (inheritancePrefs.isHeirVaultDismissed(ownerEmail)) return
        if (!importingInheritance.add(ownerEmail)) return
        viewModelScope.launch {
            try {
                val account   = getFirebaseIdentityTokenOrNull(context) ?: return@launch
                val idToken   = account.idToken
                val heirEmail = account.email ?: return@launch

                val heirSub = extractGoogleSubFromJwt(idToken) ?: return@launch

                val info = HeirInheritanceRepository.InheritedVaultInfo(
                    ownerEmail  = ownerEmail,
                    driveFileId = driveFileId ?: "",
                )
                try {
                    heirInheritanceRepository.downloadAndImport(heirEmail, heirSub, idToken, info)
                    if (marcarPollAlConcluir) inheritancePrefs.setLastHeirPollAt(System.currentTimeMillis())
                    onInheritedVaultImported(ownerEmail)
                } catch (e: HeirPrivateKeyRequiredException) {
                    if (marcarPollAlConcluir) inheritancePrefs.setLastHeirPollAt(System.currentTimeMillis())
                    pendingInheritanceImport = PendingInheritanceImport(ownerEmail, heirEmail, idToken, info)
                    _uiState.value = _uiState.value.copy(
                        showInheritanceRecoveryPrompt = true,
                        inheritanceRecoveryKeyError   = false,
                        inheritanceImportErrorRes     = null
                    )
                    Log.d("VaultHubVM", "importInheritedVaultFromPush: clave privada no local para $ownerEmail — pidiendo código")
                } catch (e: UserRecoverableAuthIOException) {
                    _uiState.value = _uiState.value.copy(driveAuthForInheritanceIntent = e.intent)
                } catch (e: InheritanceOriginUnverifiedException) {
                    if (marcarPollAlConcluir) inheritancePrefs.setLastHeirPollAt(System.currentTimeMillis())
                    Log.e("VaultHubVM", "downloadAndImport: origen no verificado para ${ofuscar(ownerEmail)}", e)
                    _uiState.value = _uiState.value.copy(
                        inheritanceImportErrorRes = com.thormakk.keyarmor.R.string.inheritance_error_origin_unverified
                    )
                    inAppNotificationRepository.save(
                        NotificationType.INHERITANCE_ERROR,
                        context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_import_failed_title),
                        context.getString(com.thormakk.keyarmor.R.string.inheritance_error_origin_unverified)
                    )
                } catch (e: HeirPrivateKeyInvalidException) {
                    if (marcarPollAlConcluir) inheritancePrefs.setLastHeirPollAt(System.currentTimeMillis())
                    Log.w("VaultHubVM", "downloadAndImport: clave privada local inválida para ${ofuscar(ownerEmail)}", e)
                    pendingInheritanceImport = PendingInheritanceImport(ownerEmail, heirEmail, idToken, info)
                    _uiState.value = _uiState.value.copy(
                        showInheritanceRecoveryPrompt = true,
                        inheritanceRecoveryKeyError   = false,
                        inheritanceImportErrorRes     = null
                    )
                } catch (e: Exception) {
                    Log.e("VaultHubVM", "downloadAndImport failed for ${ofuscar(ownerEmail)}", e)
                    inAppNotificationRepository.save(
                        NotificationType.INHERITANCE_ERROR,
                        context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_import_failed_title),
                        context.getString(com.thormakk.keyarmor.R.string.inheritance_error_import_failed)
                    )
                }
            } catch (e: Exception) {
                Log.d("VaultHubVM", "importInheritedVaultFromPush: silentSignIn skipped: ${e.message}")
            } finally {
                importingInheritance.remove(ownerEmail)
            }
        }
    }

    private data class PendingInheritanceImport(
        val ownerEmail: String,
        val heirEmail:  String,
        val idToken:    String,
        val info:       HeirInheritanceRepository.InheritedVaultInfo,
    )
    private var pendingInheritanceImport: PendingInheritanceImport? = null

    fun submitInheritanceRecoveryKey(code: String) {
        val pending = pendingInheritanceImport ?: return
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = _uiState.value.copy(inheritanceRecoveryKeyError = true)
            return
        }
        viewModelScope.launch {
            try {
                heirInheritanceRepository.downloadAndImport(
                    pending.heirEmail, "", pending.idToken, pending.info,
                    privateKeyCodeOverride = trimmed
                )
                pendingInheritanceImport = null
                _uiState.value = _uiState.value.copy(
                    showInheritanceRecoveryPrompt = false,
                    inheritanceRecoveryKeyError   = false,
                    inheritanceImportErrorRes     = null
                )
                onInheritedVaultImported(pending.ownerEmail)
            } catch (e: HeirPrivateKeyInvalidException) {
                _uiState.value = _uiState.value.copy(
                    inheritanceRecoveryKeyError = true,
                    inheritanceImportErrorRes   = null
                )
            } catch (e: InheritanceOriginUnverifiedException) {
                Log.e("VaultHubVM", "Herencia con origen no verificado", e)
                _uiState.value = _uiState.value.copy(
                    inheritanceRecoveryKeyError = false,
                    inheritanceImportErrorRes   = com.thormakk.keyarmor.R.string.inheritance_error_origin_unverified
                )
            } catch (e: UserRecoverableAuthIOException) {
                _uiState.value = _uiState.value.copy(
                    showInheritanceRecoveryPrompt = false,
                    driveAuthForInheritanceIntent = e.intent
                )
            } catch (e: Exception) {
                Log.e("VaultHubVM", "submitInheritanceRecoveryKey failed", e)
                _uiState.value = _uiState.value.copy(
                    inheritanceRecoveryKeyError = false,
                    inheritanceImportErrorRes   = com.thormakk.keyarmor.R.string.inheritance_error_import_failed
                )
            }
        }
    }

    fun dismissInheritanceRecoveryPrompt() {
        pendingInheritanceImport = null
        _uiState.value = _uiState.value.copy(
            showInheritanceRecoveryPrompt = false,
            inheritanceRecoveryKeyError   = false,
            inheritanceImportErrorRes     = null
        )
    }

    private suspend fun onInheritedVaultImported(ownerEmail: String) {
        inheritancePrefs.markHeirVaultDismissed(ownerEmail)
        if (inheritancePrefs.getHeirOfOwnerEmail() == ownerEmail) {
            inheritancePrefs.setHeirOfOwnerInheritedAt(System.currentTimeMillis())
        }
        appNotificationController.show(context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_vault_imported_snackbar, ownerEmail))
        inAppNotificationRepository.save(
            NotificationType.VAULT_INHERITED,
            context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_vault_imported_title),
            context.getString(com.thormakk.keyarmor.R.string.inheritance_notif_vault_imported_body, ownerEmail)
        )
    }

    private suspend fun registerFcmTokenIfNeeded() {
        try {
            val idToken = getFirebaseIdentityTokenOrNull(context)?.idToken ?: return

            val fcmToken = runCatching {
                FirebaseMessaging.getInstance().token.await()
            }.getOrNull() ?: return

            if (fcmToken == fcmPrefs.lastRegisteredFcmToken && !fcmPrefs.pendingTokenRefresh) return

            val ok = fcmRepository.registerToken(idToken, fcmToken)
            if (ok) {
                fcmPrefs.lastRegisteredFcmToken = fcmToken
                fcmPrefs.pendingTokenRefresh    = false
                Log.d("VaultHubVM", "FCM token registrado en servidor")
            } else {
                Log.w("VaultHubVM", "FCM token registration falló — se reintentará")
            }
        } catch (e: Exception) {
            Log.d("VaultHubVM", "registerFcmTokenIfNeeded skipped: ${e.message}")
        }
    }

    fun checkPendingHeirFromPrefs(fromResume: Boolean = false) {
        val pendingOwner = fcmPrefs.pendingHeirConsentOwnerEmail
        val waitingOwner = if (fromResume) null else fcmPrefs.waitingFingerprintConfirmationOwnerEmail
        Log.d("VaultHubVM", "FCM prefs check: pendingOwner=$pendingOwner waitingOwner=$waitingOwner fromResume=$fromResume")
        if (pendingOwner != null || waitingOwner != null) {
            appConsentController.requestConsent()
        }
    }

    fun refreshPlanStatus() {
        planPrefs.refresh()
    }


    fun checkPendingFamilyInviteFromPrefs() {
        if (MainActivity.pendingFamilyConsentArgs.value != null) return
        val admin = fcmPrefs.pendingFamilyInviteAdminEmail ?: return
        val token = fcmPrefs.pendingFamilyInviteToken       ?: return
        Log.d("VaultHubVM", "Restaurando invite familiar desde FcmPrefs — admin=$admin")
        MainActivity.pendingFamilyConsentArgs.value = admin to token
    }

    fun checkPendingMemberPremiumRevokedFromPrefs() {
        if (!fcmPrefs.pendingMemberPremiumRevoked) return
        fcmPrefs.pendingMemberPremiumRevoked = false
        _uiState.value = _uiState.value.copy(memberPremiumRevokedSnackbar = true)
        viewModelScope.launch {
            inAppNotificationRepository.save(
                NotificationType.FAMILY_PREMIUM_REVOKED,
                context.getString(com.thormakk.keyarmor.R.string.family_premium_revoked_notif_title),
                context.getString(com.thormakk.keyarmor.R.string.family_premium_revoked_notif_body)
            )
        }
    }

    fun checkPendingMemberAccessRevokedFromPrefs() {
        if (!fcmPrefs.pendingMemberAccessRevoked) return
        fcmPrefs.pendingMemberAccessRevoked = false
        val adminEmail = fcmPrefs.pendingMemberAccessRevokedAdminEmail
        fcmPrefs.pendingMemberAccessRevokedAdminEmail = null
        _uiState.value = _uiState.value.copy(
            memberRevokedSnackbar = true,
            memberRevokedAdminEmail = adminEmail
        )
        viewModelScope.launch {
            val body = if (!adminEmail.isNullOrBlank())
                context.getString(com.thormakk.keyarmor.R.string.family_access_revoked_notif_body_named, adminEmail)
            else
                context.getString(com.thormakk.keyarmor.R.string.family_access_revoked_notif_body)
            inAppNotificationRepository.save(
                NotificationType.FAMILY_ACCESS_REVOKED,
                context.getString(com.thormakk.keyarmor.R.string.family_access_revoked_notif_title),
                body
            )
        }
    }

    fun clearMemberRevokedSnackbar() {
        _uiState.value = _uiState.value.copy(memberRevokedSnackbar = false, memberRevokedAdminEmail = null)
    }

    fun clearMemberPremiumRevokedSnackbar() {
        _uiState.value = _uiState.value.copy(memberPremiumRevokedSnackbar = false)
    }

    fun dismissWhatsNew() {
        _uiState.value = _uiState.value.copy(showWhatsNew = false)
    }

    fun dismissDriveReconnectNotice() {
        _uiState.value = _uiState.value.copy(showDriveReconnectNotice = false)
    }

    fun lockNow() {
        sessionManager.lock()
    }

    fun reorderPinnedEntries(fromIndex: Int, toIndex: Int) {
        pinnedHubPrefs.reorder(activeVaultPrefs.activeVaultId, fromIndex, toIndex)
    }

    fun clearDriveAuthForInheritance() {
        _uiState.value = _uiState.value.copy(driveAuthForInheritanceIntent = null)
    }

    fun clearDriveAuthForEncrypt() {
        _uiState.value = _uiState.value.copy(driveAuthForEncryptIntent = null)
    }

    @Suppress("EmptyMethod")
    fun retryInheritedVaultImport() {   }
    @Suppress("EmptyMethod")
    fun retryEncryptAndUpload()     {                        }

    fun consolidateVault(vaultId: String) {
        viewModelScope.launch { consolidateVaultUseCase(vaultId) }
    }

    fun switchVault(vaultId: String) {
        val isInherited = _uiState.value.availableVaults.find { it.id == vaultId }?.isInherited == true
        activeVaultPrefs.setActiveVault(vaultId, isInherited)
        val isFree = planPrefs.effectivePlan() == UserPlan.FREE
        _uiState.value = _uiState.value.copy(
            activeVaultId         = vaultId,
            isActiveVaultReadOnly = isInherited || (isFree && vaultId != VaultMetadata.PRIMARY_VAULT_ID)
        )
    }

    private fun observarBovedaActiva() {
        viewModelScope.launch {
            combine(
                activeVaultPrefs.activeVaultIdFlow,
                activeVaultPrefs.activeVaultIsInheritedFlow,
                planPrefs.planFlow,
            ) { id, heredada, plan -> Triple(id, heredada, plan) }.collect { (id, heredada, plan) ->
                val isFree = plan == UserPlan.FREE
                val soloLectura = heredada ||
                    (isFree && id != VaultMetadata.PRIMARY_VAULT_ID)
                val estado = _uiState.value
                if (estado.activeVaultId == id && estado.isActiveVaultReadOnly == soloLectura) return@collect
                _uiState.value = estado.copy(
                    activeVaultId         = id,
                    isActiveVaultReadOnly = soloLectura,
                )
            }
        }
    }

    private fun ofuscar(email: String?): String {
        if (email.isNullOrBlank()) return "(sin email)"
        val arroba = email.indexOf('@')
        if (arroba <= 0) return "***"
        val visible = email.take(minOf(2, arroba))
        return "$visible***${email.substring(arroba)}"
    }

}