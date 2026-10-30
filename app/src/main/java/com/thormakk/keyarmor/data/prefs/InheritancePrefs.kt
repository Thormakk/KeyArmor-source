package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.thormakk.keyarmor.data.prefs.InheritancePrefs.Companion.GRACE_PERIOD_DAYS
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InheritancePrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "inheritance_prefs"

        const val MAX_HEIRS = 3

        val INACTIVITY_OPTIONS = listOf(180, 270, 365)
        const val DEFAULT_INACTIVITY_DAYS = 180

        const val GRACE_PERIOD_DAYS = 7

        private const val KEY_ENABLED          = "enabled"
        private const val KEY_PENDING_DISARM   = "pending_disarm"
        private const val KEY_PENDING_FIRST_SEAL = "pending_first_seal"
        private const val KEY_DISARMED_DOWNGRADE = "disarmed_by_downgrade"
        private const val KEY_DISARMED_ALREADY_INHERITED = "disarmed_by_already_inherited"
        private const val KEY_INACTIVITY_DAYS  = "inactivity_days"
        private const val KEY_HEIR_EMAIL       = "heir_email"
        private const val KEY_HEIR_EMAILS      = "heir_emails"
        private const val KEY_DESIGNATED_VAULT  = "designated_vault_id"
        private const val KEY_DEVICE_TOKEN          = "device_token"
        private const val KEY_HEIR_ENCRYPTION_DATA  = "heir_encryption_data"

        private const val KEY_HEIR_OF_OWNER_EMAIL       = "heir_of_owner_email"
        private const val KEY_HEIR_OF_OWNER_ACCEPTED_AT = "heir_of_owner_accepted_at"
        private const val KEY_HEIR_OF_OWNER_CONFIRMED_AT = "heir_of_owner_confirmed_at"
        private const val KEY_HEIR_OF_OWNER_INHERITED_AT = "heir_of_owner_inherited_at"

        private const val KEY_DISMISSED_HEIR_VAULTS = "dismissed_heir_vaults"

        private const val KEY_LAST_HEARTBEAT_SENT_AT = "last_heartbeat_sent_at"
        private const val KEY_LAST_OWNER_NOTIF_POLL_AT = "last_owner_notif_poll_at"
        private const val KEY_LAST_HEIR_POLL_AT        = "last_heir_poll_at"
        private const val KEY_LAST_OWNER_RESUME_CHECK  = "last_owner_resume_check_at"
        private const val KEY_HEARTBEAT_INTERVAL_MIGRATED = "heartbeat_interval_v2"
        private const val KEY_INTRO_EXPANDED = "intro_expanded"

        private const val KEY_HEIR_IDENTITY_PUBLIC   = "heir_identity_public"
        private const val KEY_HEIR_IDENTITY_PRIV_ENC = "heir_identity_priv_enc"
        private const val KEY_OWNER_SIGN_PUBLIC      = "owner_sign_public"
        private const val KEY_OWNER_SIGN_PRIV_ENC    = "owner_sign_priv_enc"
        private const val KEY_OWNER_SIGN_PROVISIONAL = "owner_sign_provisional"
        private const val KEY_PINNED_OWNER_SIGN      = "pinned_owner_sign_map"
        private const val KEY_DISARM_OWNER_EMAIL     = "disarm_owner_email"
        private const val KEY_DISARM_REASON          = "disarm_reason"
        private const val KEY_HEIR_PIN_WARNED        = "heir_pin_missing_warned"
        private const val KEY_HEIR_KEY_CONFIRMED     = "heir_key_confirmed_map"
        private const val KEY_HEIR_KEY_PENDING       = "heir_key_pending_map"

        private const val MS_PER_DAY = 86_400_000L
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean   = prefs.getBoolean(KEY_ENABLED, false)
    fun setEnabled(v: Boolean) = prefs.edit { putBoolean(KEY_ENABLED, v) }

    fun isPendingDisarm(): Boolean   = prefs.getBoolean(KEY_PENDING_DISARM, false)
    fun setPendingDisarm(v: Boolean) = prefs.edit { putBoolean(KEY_PENDING_DISARM, v) }

    fun isPendingFirstSeal(): Boolean   = prefs.getBoolean(KEY_PENDING_FIRST_SEAL, false)
    fun setPendingFirstSeal(v: Boolean) = prefs.edit { putBoolean(KEY_PENDING_FIRST_SEAL, v) }

    fun setDisarmRequest(ownerEmail: String?, reason: String) = prefs.edit {
        if (ownerEmail != null) putString(KEY_DISARM_OWNER_EMAIL, ownerEmail)
        putString(KEY_DISARM_REASON, reason)
    }
    fun getDisarmOwnerEmail(): String? = prefs.getString(KEY_DISARM_OWNER_EMAIL, null)
    fun getDisarmReason(): String?     = prefs.getString(KEY_DISARM_REASON, null)
    fun clearDisarmRequest() = prefs.edit {
        remove(KEY_DISARM_OWNER_EMAIL)
        remove(KEY_DISARM_REASON)
    }

    fun wasHeirPinWarned(): Boolean  = prefs.getBoolean(KEY_HEIR_PIN_WARNED, false)
    fun setHeirPinWarned(v: Boolean) = prefs.edit { putBoolean(KEY_HEIR_PIN_WARNED, v) }

    fun wasDisarmedByDowngrade(): Boolean   = prefs.getBoolean(KEY_DISARMED_DOWNGRADE, false)
    fun setDisarmedByDowngrade(v: Boolean)  = prefs.edit { putBoolean(KEY_DISARMED_DOWNGRADE, v) }

    fun wasDisarmedByAlreadyInherited(): Boolean  = prefs.getBoolean(KEY_DISARMED_ALREADY_INHERITED, false)
    fun setDisarmedByAlreadyInherited(v: Boolean) = prefs.edit { putBoolean(KEY_DISARMED_ALREADY_INHERITED, v) }

    fun getInactivityDays(): Int     = prefs.getInt(KEY_INACTIVITY_DAYS, DEFAULT_INACTIVITY_DAYS)
    fun setInactivityDays(days: Int) = prefs.edit { putInt(KEY_INACTIVITY_DAYS, days) }

    fun getHeirEmails(): List<String> {
        val legacyEmail = prefs.getString(KEY_HEIR_EMAIL, null)
        if (legacyEmail != null) {
            val migrated = listOf(legacyEmail)
            persistHeirEmails(migrated)
            prefs.edit { remove(KEY_HEIR_EMAIL) }
            return migrated
        }
        val json = prefs.getString(KEY_HEIR_EMAILS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) { emptyList() }
    }

    fun addHeirEmail(email: String): Boolean {
        val current = getHeirEmails().toMutableList()
        if (current.size >= MAX_HEIRS || email in current) return false
        current.add(email)
        persistHeirEmails(current)
        return true
    }

    fun removeHeirEmail(email: String) {
        val current = getHeirEmails().toMutableList()
        if (current.remove(email)) persistHeirEmails(current)
    }

    private fun persistHeirEmails(emails: List<String>) {
        val arr = JSONArray()
        emails.forEach { arr.put(it) }
        prefs.edit { putString(KEY_HEIR_EMAILS, arr.toString()) }
    }

    fun ensureDeviceToken(): String {
        var token = prefs.getString(KEY_DEVICE_TOKEN, null)
        if (token == null) {
            token = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_DEVICE_TOKEN, token) }
        }
        return token
    }

    fun getDeviceToken(): String? = prefs.getString(KEY_DEVICE_TOKEN, null)

    data class HeirEncryptionInfo(
        val heirEmail:             String,
        val encryptedHeirSubBase64: String,
        val pepperBase64:          String = "",
        val heirPublicKeyB64:      String = ""
    )

    fun getHeirEncryptionData(): List<HeirEncryptionInfo> {
        val json = prefs.getString(KEY_HEIR_ENCRYPTION_DATA, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                HeirEncryptionInfo(
                    heirEmail              = obj.getString("heirEmail"),
                    encryptedHeirSubBase64 = obj.getString("encryptedHeirSubBase64"),
                    pepperBase64           = obj.optString("pepperBase64", ""),
                    heirPublicKeyB64       = obj.optString("heirPublicKeyB64", "")
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    fun saveHeirEncryptionEntry(info: HeirEncryptionInfo) {
        val current = getHeirEncryptionData().toMutableList()
        val idx = current.indexOfFirst { it.heirEmail == info.heirEmail }
        if (idx >= 0) current[idx] = info else current.add(info)
        persistHeirEncryptionData(current)
    }

    fun removeHeirEncryptionEntry(heirEmail: String) {
        val current = getHeirEncryptionData().toMutableList()
        if (current.removeAll { it.heirEmail == heirEmail }) persistHeirEncryptionData(current)
    }

    private fun persistHeirEncryptionData(data: List<HeirEncryptionInfo>) {
        val arr = JSONArray()
        data.forEach { info ->
            arr.put(JSONObject().apply {
                put("heirEmail",              info.heirEmail)
                put("encryptedHeirSubBase64", info.encryptedHeirSubBase64)
                put("pepperBase64",           info.pepperBase64)
                put("heirPublicKeyB64",       info.heirPublicKeyB64)
            })
        }
        prefs.edit { putString(KEY_HEIR_ENCRYPTION_DATA, arr.toString()) }
    }

    fun getHeirOfOwnerEmail(): String? = prefs.getString(KEY_HEIR_OF_OWNER_EMAIL, null)

    fun setHeirOfOwnerEmail(ownerEmail: String) =
        prefs.edit { putString(KEY_HEIR_OF_OWNER_EMAIL, ownerEmail) }

    fun clearHeirOfOwnerEmail() = prefs.edit {
        remove(KEY_HEIR_OF_OWNER_EMAIL)
        remove(KEY_HEIR_OF_OWNER_ACCEPTED_AT)
        remove(KEY_HEIR_OF_OWNER_CONFIRMED_AT)
        remove(KEY_HEIR_OF_OWNER_INHERITED_AT)
    }

    fun getHeirOfOwnerAcceptedAt(): Long = prefs.getLong(KEY_HEIR_OF_OWNER_ACCEPTED_AT, 0L)
    fun setHeirOfOwnerAcceptedAt(ms: Long) = prefs.edit { putLong(KEY_HEIR_OF_OWNER_ACCEPTED_AT, ms) }

    fun getHeirOfOwnerConfirmedAt(): Long = prefs.getLong(KEY_HEIR_OF_OWNER_CONFIRMED_AT, 0L)

    fun setHeirOfOwnerConfirmedAt(ms: Long) = prefs.edit { putLong(KEY_HEIR_OF_OWNER_CONFIRMED_AT, ms) }

    fun getHeirOfOwnerInheritedAt(): Long = prefs.getLong(KEY_HEIR_OF_OWNER_INHERITED_AT, 0L)

    fun setHeirOfOwnerInheritedAt(ms: Long) = prefs.edit { putLong(KEY_HEIR_OF_OWNER_INHERITED_AT, ms) }

    fun saveHeirIdentity(publicKeyB64: String, encryptedPrivateB64: String) = prefs.edit {
        putString(KEY_HEIR_IDENTITY_PUBLIC, publicKeyB64)
        putString(KEY_HEIR_IDENTITY_PRIV_ENC, encryptedPrivateB64)
    }

    fun getHeirIdentityPublic(): String?      = prefs.getString(KEY_HEIR_IDENTITY_PUBLIC, null)
    fun getHeirIdentityPrivateEnc(): String?  = prefs.getString(KEY_HEIR_IDENTITY_PRIV_ENC, null)
    fun hasHeirIdentity(): Boolean            = getHeirIdentityPublic() != null && getHeirIdentityPrivateEnc() != null

    fun saveOwnerSigningIdentity(
        publicKeyB64: String,
        encryptedPrivateB64: String,
        provisional: Boolean = false,
    ) = prefs.edit {
        putString(KEY_OWNER_SIGN_PUBLIC, publicKeyB64)
        putString(KEY_OWNER_SIGN_PRIV_ENC, encryptedPrivateB64)
        putBoolean(KEY_OWNER_SIGN_PROVISIONAL, provisional)
    }

    fun isOwnerSigningProvisional(): Boolean = prefs.getBoolean(KEY_OWNER_SIGN_PROVISIONAL, false)

    fun confirmOwnerSigningIdentity() = prefs.edit { putBoolean(KEY_OWNER_SIGN_PROVISIONAL, false) }
    fun getOwnerSigningPublic(): String?     = prefs.getString(KEY_OWNER_SIGN_PUBLIC, null)
    fun getOwnerSigningPrivateEnc(): String? = prefs.getString(KEY_OWNER_SIGN_PRIV_ENC, null)
    fun clearOwnerSigningIdentity() = prefs.edit {
        remove(KEY_OWNER_SIGN_PUBLIC)
        remove(KEY_OWNER_SIGN_PRIV_ENC)
        remove(KEY_OWNER_SIGN_PROVISIONAL)
    }

    fun savePinnedOwnerSigningKey(ownerEmail: String, keyB64: String) {
        val map = try { org.json.JSONObject(prefs.getString(KEY_PINNED_OWNER_SIGN, "{}") ?: "{}") }
                  catch (_: Exception) { org.json.JSONObject() }
        map.put(ownerEmail.lowercase().trim(), keyB64)
        prefs.edit { putString(KEY_PINNED_OWNER_SIGN, map.toString()) }
    }
    fun getPinnedOwnerSigningKey(ownerEmail: String): String? {
        val map = try { org.json.JSONObject(prefs.getString(KEY_PINNED_OWNER_SIGN, "{}") ?: "{}") }
                  catch (_: Exception) { return null }
        return map.optString(ownerEmail.lowercase().trim(), "").ifEmpty { null }
    }

    private fun readKeyMap(prefKey: String): org.json.JSONObject =
        try { org.json.JSONObject(prefs.getString(prefKey, "{}") ?: "{}") }
        catch (_: Exception) { org.json.JSONObject() }

    private fun normEmail(email: String) = email.lowercase().trim()

    fun getConfirmedHeirKey(heirEmail: String): String? =
        readKeyMap(KEY_HEIR_KEY_CONFIRMED).optString(normEmail(heirEmail), "").ifEmpty { null }

    fun confirmHeirKey(heirEmail: String, keyB64: String) {
        val confirmed = readKeyMap(KEY_HEIR_KEY_CONFIRMED)
        confirmed.put(normEmail(heirEmail), keyB64)
        val pending = readKeyMap(KEY_HEIR_KEY_PENDING)
        pending.remove(normEmail(heirEmail))
        prefs.edit {
            putString(KEY_HEIR_KEY_CONFIRMED, confirmed.toString())
            putString(KEY_HEIR_KEY_PENDING, pending.toString())
        }
    }

    fun getPendingHeirKey(heirEmail: String): String? =
        readKeyMap(KEY_HEIR_KEY_PENDING).optString(normEmail(heirEmail), "").ifEmpty { null }

    fun getPendingHeirKeys(): Map<String, String> {
        val map = readKeyMap(KEY_HEIR_KEY_PENDING)
        return map.keys().asSequence().mapNotNull { k ->
            map.optString(k, "").ifEmpty { null }?.let { k to it }
        }.toMap()
    }

    fun setPendingHeirKey(heirEmail: String, keyB64: String) {
        val pending = readKeyMap(KEY_HEIR_KEY_PENDING)
        pending.put(normEmail(heirEmail), keyB64)
        prefs.edit { putString(KEY_HEIR_KEY_PENDING, pending.toString()) }
    }

    fun clearPendingHeirKey(heirEmail: String) {
        val pending = readKeyMap(KEY_HEIR_KEY_PENDING)
        pending.remove(normEmail(heirEmail))
        prefs.edit { putString(KEY_HEIR_KEY_PENDING, pending.toString()) }
    }

    fun clearHeirKeyState(heirEmail: String) {
        val confirmed = readKeyMap(KEY_HEIR_KEY_CONFIRMED)
        val pending   = readKeyMap(KEY_HEIR_KEY_PENDING)
        confirmed.remove(normEmail(heirEmail))
        pending.remove(normEmail(heirEmail))
        prefs.edit {
            putString(KEY_HEIR_KEY_CONFIRMED, confirmed.toString())
            putString(KEY_HEIR_KEY_PENDING, pending.toString())
        }
    }

    fun clearHeirIdentity() = prefs.edit {
        remove(KEY_HEIR_IDENTITY_PUBLIC)
        remove(KEY_HEIR_IDENTITY_PRIV_ENC)
    }

    fun markHeirVaultDismissed(ownerEmail: String) {
        val current = prefs.getStringSet(KEY_DISMISSED_HEIR_VAULTS, emptySet())!!.toMutableSet()
        current.add(ownerEmail)
        prefs.edit { putStringSet(KEY_DISMISSED_HEIR_VAULTS, current) }
    }

    fun isHeirVaultDismissed(ownerEmail: String): Boolean =
        prefs.getStringSet(KEY_DISMISSED_HEIR_VAULTS, emptySet())!!.contains(ownerEmail)

    fun clearDismissedHeirVault(ownerEmail: String) {
        val current = prefs.getStringSet(KEY_DISMISSED_HEIR_VAULTS, emptySet())!!.toMutableSet()
        if (current.remove(ownerEmail)) {
            prefs.edit { putStringSet(KEY_DISMISSED_HEIR_VAULTS, current) }
        }
    }

    fun getLastOwnerNotifPollAt(): Long = prefs.getLong(KEY_LAST_OWNER_NOTIF_POLL_AT, 0L)
    fun setLastOwnerNotifPollAt(ms: Long) = prefs.edit { putLong(KEY_LAST_OWNER_NOTIF_POLL_AT, ms) }

    fun getLastHeirPollAt(): Long = prefs.getLong(KEY_LAST_HEIR_POLL_AT, 0L)
    fun setLastHeirPollAt(ms: Long) = prefs.edit { putLong(KEY_LAST_HEIR_POLL_AT, ms) }

    fun getLastOwnerResumeCheckAt(): Long = prefs.getLong(KEY_LAST_OWNER_RESUME_CHECK, 0L)
    fun setLastOwnerResumeCheckAt(ms: Long) = prefs.edit { putLong(KEY_LAST_OWNER_RESUME_CHECK, ms) }

    fun getLastHeartbeatSentAt(): Long = prefs.getLong(KEY_LAST_HEARTBEAT_SENT_AT, 0L)

    fun setLastHeartbeatSentAt(ms: Long) = prefs.edit { putLong(KEY_LAST_HEARTBEAT_SENT_AT, ms) }

    fun isApproachingGracePeriod(): Boolean {
        if (!isEnabled()) return false
        val lastMs = getLastHeartbeatSentAt()
        if (lastMs == 0L) return false
        val daysSinceLast = (System.currentTimeMillis() - lastMs) / MS_PER_DAY
        return daysSinceLast >= getInactivityDays() - GRACE_PERIOD_DAYS
    }

    fun isHeartbeatIntervalMigrated(): Boolean =
        prefs.getBoolean(KEY_HEARTBEAT_INTERVAL_MIGRATED, false)

    fun markHeartbeatIntervalMigrated() =
        prefs.edit { putBoolean(KEY_HEARTBEAT_INTERVAL_MIGRATED, true) }

    fun isIntroExpanded(): Boolean  = prefs.getBoolean(KEY_INTRO_EXPANDED, true)
    fun setIntroExpanded(v: Boolean) = prefs.edit { putBoolean(KEY_INTRO_EXPANDED, v) }

    fun clear() = prefs.edit {
        remove(KEY_ENABLED)
        remove(KEY_PENDING_DISARM)
        remove(KEY_DISARMED_DOWNGRADE)
        remove(KEY_DISARMED_ALREADY_INHERITED)
        remove(KEY_INACTIVITY_DAYS)
        remove(KEY_HEIR_EMAIL)
        remove(KEY_HEIR_EMAILS)
        remove(KEY_DESIGNATED_VAULT)
        remove(KEY_DEVICE_TOKEN)
        remove(KEY_HEIR_ENCRYPTION_DATA)
        remove(KEY_DISMISSED_HEIR_VAULTS)
        remove(KEY_LAST_HEARTBEAT_SENT_AT)
        remove(KEY_HEIR_OF_OWNER_EMAIL)
    }

}
