package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmPrefs @Inject constructor(
    @ApplicationContext ctx: Context
) {
    private val prefs = ctx.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)

    var pendingHeirConsentOwnerEmail: String?
        get()  = prefs.getString("pending_heir_owner", null)
        set(v) = prefs.edit { putString("pending_heir_owner", v) }

    fun clearPendingHeirConsent() {
        prefs.edit { remove("pending_heir_owner") }
    }

    var lastRegisteredFcmToken: String?
        get()  = prefs.getString("last_registered_fcm_token", null)
        set(v) = prefs.edit { putString("last_registered_fcm_token", v) }

    var pendingTokenRefresh: Boolean
        get()  = prefs.getBoolean("pending_token_refresh", false)
        set(v) = prefs.edit { putBoolean("pending_token_refresh", v) }

    var pendingMemberPremiumRevoked: Boolean
        get()  = prefs.getBoolean("pending_member_premium_revoked", false)
        set(v) = prefs.edit { putBoolean("pending_member_premium_revoked", v) }

    var pendingMemberAccessRevoked: Boolean
        get()  = prefs.getBoolean("pending_member_access_revoked", false)
        set(v) = prefs.edit { putBoolean("pending_member_access_revoked", v) }

    var pendingMemberAccessRevokedAdminEmail: String?
        get()  = prefs.getString("pending_member_access_revoked_admin_email", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_member_access_revoked_admin_email", v)
            else           remove("pending_member_access_revoked_admin_email")
        }

    var pendingFamilyInviteAdminEmail: String?
        get()  = prefs.getString("pending_family_invite_admin", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_family_invite_admin", v)
            else           remove("pending_family_invite_admin")
        }

    var pendingFamilyInviteToken: String?
        get()  = prefs.getString("pending_family_invite_token", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_family_invite_token", v)
            else           remove("pending_family_invite_token")
        }

    var pendingHeirConsentPublicKey: String?
        get()  = prefs.getString("pending_heir_public_key", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_heir_public_key", v)
            else           remove("pending_heir_public_key")
        }

    var pendingHeirConsentOwnerSigningKey: String?
        get()  = prefs.getString("pending_heir_owner_sign_key", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_heir_owner_sign_key", v)
            else           remove("pending_heir_owner_sign_key")
        }

    var pendingMemberAcceptedEmail: String?
        get()  = prefs.getString("pending_member_accepted_email", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_member_accepted_email", v)
            else           remove("pending_member_accepted_email")
        }

    fun clearPendingMemberAccepted() {
        prefs.edit { remove("pending_member_accepted_email") }
    }

    var pendingHeirAcceptedEmail: String?
        get()  = prefs.getString("pending_heir_accepted_email", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_heir_accepted_email", v)
            else           remove("pending_heir_accepted_email")
        }

    var pendingHeirAcceptedEncSub: String?
        get()  = prefs.getString("pending_heir_accepted_enc_sub", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_heir_accepted_enc_sub", v)
            else           remove("pending_heir_accepted_enc_sub")
        }

    var pendingHeirAcceptedPubKey: String?
        get()  = prefs.getString("pending_heir_accepted_pubkey", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_heir_accepted_pubkey", v)
            else           remove("pending_heir_accepted_pubkey")
        }

    fun clearPendingHeirAccepted() {
        prefs.edit {
            remove("pending_heir_accepted_email")
                .remove("pending_heir_accepted_enc_sub")
                .remove("pending_heir_accepted_pubkey")
        }
    }

    var pendingHeirDeclinedEmail: String?
        get()  = prefs.getString("pending_heir_declined_email", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_heir_declined_email", v)
            else           remove("pending_heir_declined_email")
        }

    fun clearPendingHeirDeclined() {
        prefs.edit { remove("pending_heir_declined_email") }
    }

    var pendingInheritanceOwnerEmail: String?
        get()  = prefs.getString("pending_inheritance_owner", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_inheritance_owner", v)
            else           remove("pending_inheritance_owner")
        }

    var pendingInheritanceDriveFileId: String?
        get()  = prefs.getString("pending_inheritance_drive_file_id", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_inheritance_drive_file_id", v)
            else           remove("pending_inheritance_drive_file_id")
        }

    fun clearPendingInheritance() {
        prefs.edit {
            remove("pending_inheritance_owner")
                .remove("pending_inheritance_drive_file_id")
        }
    }


    fun clearPendingFamilyVaultUpdate() {
        prefs.edit { remove("pending_family_vault_admin") }
    }

    var pendingMemberLeftEmail: String?
        get()  = prefs.getString("pending_member_left_email", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_member_left_email", v)
            else           remove("pending_member_left_email")
        }

    fun clearPendingMemberLeft() {
        prefs.edit { remove("pending_member_left_email") }
    }

    var pendingMemberDeclinedEmail: String?
        get()  = prefs.getString("pending_member_declined_email", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_member_declined_email", v)
            else           remove("pending_member_declined_email")
        }

    fun clearPendingMemberDeclined() {
        prefs.edit { remove("pending_member_declined_email") }
    }

    var pendingHeirResignedEmail: String?
        get()  = prefs.getString("pending_heir_resigned_email", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_heir_resigned_email", v)
            else           remove("pending_heir_resigned_email")
        }

    fun clearPendingHeirResigned() {
        prefs.edit { remove("pending_heir_resigned_email") }
    }

    var pendingHeirRemovedByOwner: Boolean
        get()  = prefs.getBoolean("pending_heir_removed_by_owner", false)
        set(v) = prefs.edit { putBoolean("pending_heir_removed_by_owner", v) }

    var pendingInheritanceDeliveredHeirEmails: String?
        get()  = prefs.getString("pending_inheritance_delivered_heirs", null)
        set(v) = prefs.edit {
            if (v != null) putString("pending_inheritance_delivered_heirs", v)
            else           remove("pending_inheritance_delivered_heirs")
        }

    fun clearPendingInheritanceDelivered() {
        prefs.edit { remove("pending_inheritance_delivered_heirs") }
    }

    var waitingFingerprintConfirmationOwnerEmail: String?
        get()  = prefs.getString("waiting_fingerprint_confirmation_owner", null)
        set(v) = prefs.edit {
            if (v != null) putString("waiting_fingerprint_confirmation_owner", v)
            else           remove("waiting_fingerprint_confirmation_owner")
        }

    fun clearWaitingFingerprintConfirmation() {
        prefs.edit { remove("waiting_fingerprint_confirmation_owner") }
    }

    var pendingHeirFingerprintConfirmed: Boolean
        get()  = prefs.getBoolean("pending_heir_fingerprint_confirmed", false)
        set(v) = prefs.edit { putBoolean("pending_heir_fingerprint_confirmed", v) }

    fun clearPendingHeirFingerprintConfirmed() {
        prefs.edit { remove("pending_heir_fingerprint_confirmed") }
    }

}
