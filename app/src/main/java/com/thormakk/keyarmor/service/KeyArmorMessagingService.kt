package com.thormakk.keyarmor.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.thormakk.keyarmor.MainActivity
import com.thormakk.keyarmor.data.prefs.FamilyPrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.FamilyVaultRepository
import com.thormakk.keyarmor.data.repository.FcmEventBus
import com.thormakk.keyarmor.data.repository.FcmRepository
import com.thormakk.keyarmor.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class KeyArmorMessagingService : FirebaseMessagingService() {

    @Inject lateinit var fcmPrefs:             FcmPrefs
    @Inject lateinit var fcmEventBus:          FcmEventBus
    @Inject lateinit var fcmRepository:        FcmRepository
    @Inject lateinit var familyPrefs:          FamilyPrefs
    @Inject lateinit var planPrefs:            PlanPrefs
    @Inject lateinit var familyVaultRepository: FamilyVaultRepository
    @Inject lateinit var inheritancePrefs:     InheritancePrefs

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token renovado — pendiente de registro en próximo silentSignIn")
        fcmPrefs.pendingTokenRefresh = true
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"] ?: return
        Log.d(TAG, "FCM mensaje recibido: type=$type")

        when (type) {
            "heir_consent" -> {
                val ownerEmail = message.data["ownerEmail"] ?: return
                val publicKey  = message.data["publicKeyBase64"]
                val ownerSignKey = message.data["ownerSigningPublicKeyBase64"]
                Log.d(TAG, "heir_consent de owner=$ownerEmail")

                fcmPrefs.pendingHeirConsentOwnerEmail = ownerEmail
                fcmPrefs.pendingHeirConsentPublicKey  = publicKey
                fcmPrefs.pendingHeirConsentOwnerSigningKey = ownerSignKey?.takeIf { it.isNotEmpty() }

                fcmEventBus.emitHeirConsent(ownerEmail)
            }
            "heir_accepted" -> {
                val heirEmail = message.data["heirEmail"] ?: return
                val encSub    = message.data["encryptedHeirSubBase64"]
                val heirPub   = message.data["heirPublicKeyBase64"]
                Log.d(TAG, "heir_accepted de heir=$heirEmail")

                fcmPrefs.pendingHeirAcceptedEmail  = heirEmail
                fcmPrefs.pendingHeirAcceptedEncSub = encSub
                fcmPrefs.pendingHeirAcceptedPubKey = heirPub

                fcmEventBus.emitHeirAccepted(heirEmail, encSub, heirPub)
            }
            "heir_declined" -> {
                val heirEmail = message.data["heirEmail"] ?: return
                Log.d(TAG, "heir_declined de heir=$heirEmail")

                fcmPrefs.pendingHeirDeclinedEmail = heirEmail

                fcmEventBus.emitHeirDeclined(heirEmail)
            }
            "inheritance_activated" -> {
                val ownerEmail  = message.data["ownerEmail"] ?: return
                val driveFileId = message.data["driveFileId"]
                Log.d(TAG, "inheritance_activated de owner=$ownerEmail driveFileId=$driveFileId")

                fcmPrefs.pendingInheritanceOwnerEmail  = ownerEmail
                fcmPrefs.pendingInheritanceDriveFileId = driveFileId

                fcmEventBus.emitInheritanceActivated(ownerEmail, driveFileId)
            }
            "inheritance_delivered" -> {
                val heirEmails = message.data["heirEmails"]
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()
                Log.d(TAG, "inheritance_delivered — bóveda entregada a: $heirEmails")

                fcmPrefs.pendingInheritanceDeliveredHeirEmails = heirEmails.joinToString(",")

                fcmEventBus.emitInheritanceDelivered(heirEmails)
            }
            "family_invite" -> {
                val adminEmail  = message.data["adminEmail"]  ?: return
                val inviteToken = message.data["inviteToken"] ?: return
                Log.d(TAG, "family_invite de admin=$adminEmail")

                fcmPrefs.pendingFamilyInviteAdminEmail = adminEmail
                fcmPrefs.pendingFamilyInviteToken      = inviteToken

                MainActivity.pendingFamilyConsentArgs.value = adminEmail to inviteToken

                if (!MainActivity.isAppForegrounded) {
                    NotificationHelper.notifyFamilyInvite(applicationContext, adminEmail, inviteToken)
                }
            }

            "family_member_accepted" -> {
                val memberEmail = message.data["memberEmail"] ?: return
                Log.d(TAG, "family_member_accepted de member=$memberEmail")

                fcmPrefs.pendingMemberAcceptedEmail = memberEmail

                fcmEventBus.emitMemberAccepted(memberEmail)
            }

            "family_access_revoked" -> {
                val revokedAdminEmail = familyPrefs.getAdminEmail()
                Log.d(TAG, "family_access_revoked — admin=$revokedAdminEmail, limpiando estado de miembro")
                if (revokedAdminEmail != null) {
                    serviceScope.launch {
                        familyVaultRepository.deleteMemberVault(revokedAdminEmail)
                    }
                }
                familyPrefs.clearMemberRole()
                planPrefs.refresh()
                fcmPrefs.pendingMemberAccessRevoked = true
                fcmPrefs.pendingMemberAccessRevokedAdminEmail = revokedAdminEmail
                fcmEventBus.emitMemberAccessRevoked(revokedAdminEmail)
                if (!MainActivity.isAppForegrounded) {
                    NotificationHelper.notifyAccessRevoked(applicationContext, revokedAdminEmail)
                }
            }

            "family_member_left" -> {
                val memberEmail = message.data["memberEmail"] ?: return
                Log.d(TAG, "family_member_left — miembro se fue: $memberEmail")

                fcmPrefs.pendingMemberLeftEmail = memberEmail

                fcmEventBus.emitMemberLeft(memberEmail)

                if (!MainActivity.isAppForegrounded) {
                    NotificationHelper.notifyMemberLeft(applicationContext, memberEmail)
                }
            }

            "family_member_declined" -> {
                val memberEmail = message.data["memberEmail"] ?: return
                Log.d(TAG, "family_member_declined — miembro rechazó: $memberEmail")

                fcmPrefs.pendingMemberDeclinedEmail = memberEmail

                fcmEventBus.emitMemberDeclined(memberEmail)

                if (!MainActivity.isAppForegrounded) {
                    NotificationHelper.notifyMemberDeclined(applicationContext, memberEmail)
                }
            }

            "member_premium_revoked" -> {
                val fromAdmin = message.data["adminEmail"]
                val myAdmin   = familyPrefs.getAdminEmail()
                if (myAdmin == null || !myAdmin.equals(fromAdmin, ignoreCase = true)) {
                    Log.w(TAG, "member_premium_revoked ignorado — adminEmail no coincide (from=$fromAdmin)")
                    return
                }

                Log.d(TAG, "member_premium_revoked recibido — el admin bajó de plan")

                familyPrefs.isPremiumMember = false

                planPrefs.refresh()

                if (planPrefs.effectivePlan() == UserPlan.FREE) {
                    fcmPrefs.pendingMemberPremiumRevoked = true
                    fcmEventBus.emitMemberPremiumRevoked()
                    if (!MainActivity.isAppForegrounded) {
                        NotificationHelper.notifyPremiumRevoked(applicationContext)
                    }
                }
            }
            "heir_resigned" -> {
                val heirEmail = message.data["heirEmail"] ?: return
                Log.d(TAG, "heir_resigned — heredero=$heirEmail renunció")

                fcmPrefs.pendingHeirResignedEmail = heirEmail

                fcmEventBus.emitHeirResigned(heirEmail)
            }
            "heir_fingerprint_confirmed" -> {
                Log.d(TAG, "heir_fingerprint_confirmed — el titular confirmó la huella")

                fcmPrefs.pendingHeirFingerprintConfirmed = true
                inheritancePrefs.setHeirOfOwnerConfirmedAt(System.currentTimeMillis())

                fcmEventBus.emitHeirFingerprintConfirmed()
            }
            "heir_removed" -> {
                Log.d(TAG, "heir_removed — el owner nos eliminó como heredero")

                inheritancePrefs.clearHeirOfOwnerEmail()
                fcmPrefs.pendingHeirRemovedByOwner = true

                fcmEventBus.emitHeirRemovedByOwner()
            }
        }
    }

    companion object {
        private const val TAG = "FCM"
    }
}
