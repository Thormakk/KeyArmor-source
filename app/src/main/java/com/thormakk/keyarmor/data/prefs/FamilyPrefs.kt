package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "family_prefs"

        const val MAX_MEMBERS = 4

        private const val KEY_MEMBER_EMAILS = "member_emails"

        private const val KEY_DEVICE_TOKEN = "device_token"

        private const val KEY_MEMBER_DATA = "member_data"

        private const val KEY_ADMIN_EMAIL = "admin_email"

        private const val KEY_INVITE_TOKEN = "invite_token"

        private const val KEY_IS_PREMIUM_MEMBER = "is_premium_member"
        private const val KEY_LAST_MEMBER_STATUS_POLL_AT = "last_member_status_poll_at"

        private const val KEY_PENDING_MEMBER_REVOKE = "pending_member_premium_revoke"

        private const val KEY_PENDING_REVOKE_EMAILS = "pending_revoke_emails"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getMemberEmails(): List<String> {
        val json = prefs.getString(KEY_MEMBER_EMAILS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) { emptyList() }
    }

    fun addMemberEmail(email: String): Boolean {
        val current = getMemberEmails().toMutableList()
        if (current.size >= MAX_MEMBERS || email in current) return false
        current.add(email)
        persistMemberEmails(current)
        return true
    }

    fun removeMemberEmail(email: String) {
        val current = getMemberEmails().toMutableList()
        if (current.remove(email)) persistMemberEmails(current)
    }

    private fun persistMemberEmails(emails: List<String>) {
        val arr = JSONArray()
        emails.forEach { arr.put(it) }
        prefs.edit { putString(KEY_MEMBER_EMAILS, arr.toString()) }
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

    fun storeDeviceToken(token: String) = prefs.edit { putString(KEY_DEVICE_TOKEN, token) }

    data class MemberInfo(
        val memberEmail: String,
        val status:      String = "active"
    )

    fun getMemberData(): List<MemberInfo> {
        val json = prefs.getString(KEY_MEMBER_DATA, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                MemberInfo(
                    memberEmail = obj.getString("memberEmail"),
                    status      = obj.optString("status", "active")
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    fun saveMemberEntry(info: MemberInfo) {
        val current = getMemberData().toMutableList()
        val idx = current.indexOfFirst { it.memberEmail == info.memberEmail }
        if (idx >= 0) current[idx] = info else current.add(info)
        persistMemberData(current)
    }

    fun removeMemberEntry(memberEmail: String) {
        val current = getMemberData().toMutableList()
        if (current.removeAll { it.memberEmail == memberEmail }) persistMemberData(current)
    }

    private fun persistMemberData(data: List<MemberInfo>) {
        val arr = JSONArray()
        data.forEach { info ->
            arr.put(JSONObject().apply {
                put("memberEmail", info.memberEmail)
                put("status",      info.status)
            })
        }
        prefs.edit { putString(KEY_MEMBER_DATA, arr.toString()) }
    }

    fun getAdminEmail(): String? = prefs.getString(KEY_ADMIN_EMAIL, null)
    fun setAdminEmail(email: String) = prefs.edit { putString(KEY_ADMIN_EMAIL, email) }

    fun setInviteToken(token: String) = prefs.edit { putString(KEY_INVITE_TOKEN, token) }

    fun isMember(): Boolean = prefs.getString(KEY_ADMIN_EMAIL, null) != null

    var isPremiumMember: Boolean
        get()  = prefs.getBoolean(KEY_IS_PREMIUM_MEMBER, false)
        set(v) = prefs.edit { putBoolean(KEY_IS_PREMIUM_MEMBER, v) }

    fun getLastMemberStatusPollAt(): Long = prefs.getLong(KEY_LAST_MEMBER_STATUS_POLL_AT, 0L)
    fun setLastMemberStatusPollAt(ms: Long) = prefs.edit { putLong(KEY_LAST_MEMBER_STATUS_POLL_AT, ms) }

    var pendingMemberPremiumRevoke: Boolean
        get()  = prefs.getBoolean(KEY_PENDING_MEMBER_REVOKE, false)
        set(v) = prefs.edit { putBoolean(KEY_PENDING_MEMBER_REVOKE, v) }

    var pendingRevokeEmails: List<String>
        get() {
            val json = prefs.getString(KEY_PENDING_REVOKE_EMAILS, null) ?: return emptyList()
            return try {
                val arr = JSONArray(json)
                (0 until arr.length()).map { arr.getString(it) }
            } catch (_: Exception) { emptyList() }
        }
        set(emails) {
            val arr = JSONArray()
            emails.forEach { arr.put(it) }
            prefs.edit { putString(KEY_PENDING_REVOKE_EMAILS, arr.toString()) }
        }

    fun clearPendingRevokeEmails() = prefs.edit { remove(KEY_PENDING_REVOKE_EMAILS) }

    fun clearAdminMemberList() = prefs.edit {
        remove(KEY_MEMBER_EMAILS)
        remove(KEY_MEMBER_DATA)
    }

    fun clearMemberRole() = prefs.edit {
        remove(KEY_ADMIN_EMAIL)
        remove(KEY_INVITE_TOKEN)
        remove(KEY_IS_PREMIUM_MEMBER)
    }

    fun clear() = prefs.edit {
        remove(KEY_MEMBER_EMAILS)
        remove(KEY_DEVICE_TOKEN)
        remove(KEY_MEMBER_DATA)
        remove(KEY_ADMIN_EMAIL)
        remove(KEY_INVITE_TOKEN)
        remove(KEY_IS_PREMIUM_MEMBER)
        remove(KEY_PENDING_MEMBER_REVOKE)
        remove(KEY_PENDING_REVOKE_EMAILS)
    }
}
