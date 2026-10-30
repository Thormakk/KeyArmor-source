package com.thormakk.keyarmor.data.repository

import androidx.appcompat.app.AppCompatDelegate
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import com.thormakk.keyarmor.appcheck.AppCheckHeader

@Singleton
class InheritanceRepository @Inject constructor(
    private val inheritancePrefs: InheritancePrefs,
    private val ownerSigningIdentityManager: com.thormakk.keyarmor.security.OwnerSigningIdentityManager
) {
    companion object {

        private const val HEARTBEAT_URL     = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val CANCEL_GRACE_URL  = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val HEIR_RESIGN_URL   = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val CONFIRM_FINGERPRINT_URL = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val POLL_OWNER_URL    = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val CHECK_PENDING_URL = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val CHECK_ACTIVE_URL  = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val KEY_REPUBLISH_URL = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val DELETE_ALL_URL    = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val RESUME_OWNER_URL  = "<URL DEL SERVIDOR DE GOOGLE>"
    }

    data class PendingDesignation(
        val ownerEmail:       String,
        val publicKeyBase64:  String?,
        val ownerSigningPublicKeyBase64: String? = null
    )

    data class ActiveHeirInfo(val ownerEmail: String)

    suspend fun checkHeirActive(heirEmail: String, heirIdToken: String): ActiveHeirInfo? =
        withContext(Dispatchers.IO) {
            val payload = org.json.JSONObject().apply {
                put("heirEmail", heirEmail)
                put("heirIdToken", heirIdToken)
            }.toString()
            val conn = URL(CHECK_ACTIVE_URL).openConnection() as HttpURLConnection
            conn.requestMethod  = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            val code = conn.responseCode
            if (code !in 200..299) throw Exception("onCheckHeirActive HTTP $code")
            val body = conn.inputStream.bufferedReader().readText()
            val json = org.json.JSONObject(body)
            if (!json.optBoolean("isActiveHeir", false)) return@withContext null
            val ownerEmail = json.optString("ownerEmail").ifEmpty { return@withContext null }
            ActiveHeirInfo(ownerEmail = ownerEmail)
        }

    suspend fun republishHeirKey(heirEmail: String, heirIdToken: String, heirPublicKeyB64: String): Boolean =
        withContext(Dispatchers.IO) {
            val payload = org.json.JSONObject().apply {
                put("heirEmail", heirEmail)
                put("heirIdToken", heirIdToken)
                put("heirPublicKeyBase64", heirPublicKeyB64)
            }.toString()
            val conn = URL(KEY_REPUBLISH_URL).openConnection() as HttpURLConnection
            conn.requestMethod  = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            conn.responseCode in 200..299
        }

    suspend fun checkHeirPending(heirEmail: String, heirIdToken: String): PendingDesignation? =
        withContext(Dispatchers.IO) {
            val payload = org.json.JSONObject().apply {
                put("heirEmail", heirEmail)
                put("heirIdToken", heirIdToken)
            }.toString()
            val conn = URL(CHECK_PENDING_URL).openConnection() as HttpURLConnection
            conn.requestMethod  = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            val code = conn.responseCode
            if (code !in 200..299) throw Exception("onCheckHeirPending HTTP $code")
            val body = conn.inputStream.bufferedReader().readText()
            val json = org.json.JSONObject(body)
            if (!json.optBoolean("hasPending", false)) return@withContext null
            val ownerEmail = json.optString("ownerEmail").ifEmpty { return@withContext null }
            val pub = if (json.isNull("publicKeyBase64")) null
                      else json.optString("publicKeyBase64").ifEmpty { null }
            val signPub = if (json.isNull("ownerSigningPublicKeyBase64")) null
                          else json.optString("ownerSigningPublicKeyBase64").ifEmpty { null }
            PendingDesignation(
                ownerEmail                  = ownerEmail,
                publicKeyBase64             = pub,
                ownerSigningPublicKeyBase64 = signPub
            )
        }

    data class ResumedHeir(
        val email:     String,
        val publicKey: String?,
        val status:    String
    )

    data class OwnerResumeResult(
        val hasInheritance: Boolean,
        val status:         String?,
        val inactivityDays: Int?,
        val heirs:          List<ResumedHeir>
    )

    suspend fun resumeOwnerInheritance(ownerIdToken: String): OwnerResumeResult? =
        withContext(Dispatchers.IO) {
            val deviceToken = inheritancePrefs.ensureDeviceToken()
            val payload = org.json.JSONObject().apply {
                put("ownerIdToken", ownerIdToken)
                put("deviceToken", deviceToken)
            }.toString()
            val conn = URL(RESUME_OWNER_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            val code = conn.responseCode
            if (code !in 200..299) throw Exception("onResumeOwnerInheritance HTTP $code")
            val json = org.json.JSONObject(conn.inputStream.bufferedReader().readText())
            if (!json.optBoolean("hasInheritance", false)) {
                return@withContext OwnerResumeResult(false, null, null, emptyList())
            }
            val status = json.optString("status").ifEmpty { null }
            val inactivity = if (json.isNull("inactivityDays")) null
                             else json.optInt("inactivityDays").takeIf { it > 0 }
            val heirsArr = json.optJSONArray("heirs")
            val heirs = if (heirsArr == null) emptyList() else
                (0 until heirsArr.length()).mapNotNull { i ->
                    val o = heirsArr.optJSONObject(i) ?: return@mapNotNull null
                    val email = o.optString("email").ifEmpty { return@mapNotNull null }
                    val pub = if (o.isNull("publicKey")) null
                              else o.optString("publicKey").ifEmpty { null }
                    ResumedHeir(email = email, publicKey = pub, status = o.optString("status"))
                }
            OwnerResumeResult(true, status, inactivity, heirs)
        }

    data class OwnerNotification(
        val type:                 String,
        val heirEmail:            String,
        val heirPublicKeyBase64:  String?
    )

    suspend fun pollOwnerNotifications(ownerEmail: String): List<OwnerNotification> =
        withContext(Dispatchers.IO) {
            val deviceToken = inheritancePrefs.ensureDeviceToken()
            val payload = org.json.JSONObject().apply {
                put("ownerEmail", ownerEmail)
                put("deviceToken", deviceToken)
            }.toString()
            val conn = URL(POLL_OWNER_URL).openConnection() as HttpURLConnection
            conn.requestMethod  = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            val code = conn.responseCode
            if (code !in 200..299) throw Exception("onPollOwnerNotifications HTTP $code")
            val body = conn.inputStream.bufferedReader().readText()
            val arr = org.json.JSONObject(body).optJSONArray("notifications")
                ?: return@withContext emptyList()
            (0 until arr.length()).mapNotNull { i ->
                val o         = arr.optJSONObject(i) ?: return@mapNotNull null
                val type      = o.optString("type").ifEmpty { return@mapNotNull null }
                val heirEmail = o.optString("heirEmail").ifEmpty { return@mapNotNull null }
                val pub       = if (o.isNull("heirPublicKeyBase64")) null
                                else o.optString("heirPublicKeyBase64").ifEmpty { null }
                OwnerNotification(type = type, heirEmail = heirEmail, heirPublicKeyBase64 = pub)
            }
        }

    suspend fun upsertOwner(
        email:           String,
        inactivityDays:  Int          = inheritancePrefs.getInactivityDays(),
        heirEmails:      List<String> = inheritancePrefs.getHeirEmails(),
        publicKeyBase64: String?      = null,
        ownerIdToken:    String?      = null,
        updateHeartbeat: Boolean      = false,
        locale:          String       = AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language
    ): List<String> {
        val deviceToken = inheritancePrefs.ensureDeviceToken()
        val ownerSigningPublicKeyBase64 = runCatching {
            ownerSigningIdentityManager.getOrCreatePublicKeyB64()
        }.getOrNull()
        val payload = buildPayload(
            deviceToken     = deviceToken,
            email           = email,
            inactivityDays  = inactivityDays,
            heirEmails      = heirEmails,
            publicKeyBase64 = publicKeyBase64,
            ownerSigningPublicKeyBase64 = ownerSigningPublicKeyBase64,
            ownerIdToken    = ownerIdToken,
            updateHeartbeat = updateHeartbeat,
            locale          = locale
        )
        val (code, respuesta) = postJson(payload)
        if (code !in 200..299) {
            throw Exception("Cloud Function respondió HTTP $code al hacer upsertOwner")
        }
        val rechazados: List<String> = runCatching {
            val arr = org.json.JSONObject(respuesta).optJSONArray("rejectedHeirs")
            if (arr == null) emptyList<String>()
            else (0 until arr.length()).map { arr.getString(it) }
        }.getOrDefault(emptyList())
        inheritancePrefs.setLastHeartbeatSentAt(System.currentTimeMillis())
        return rechazados
    }

    suspend fun sendHeartbeat(email: String) {
        upsertOwner(email = email, updateHeartbeat = true)
    }

    suspend fun cancelGrace(ownerEmail: String, ownerIdToken: String? = null): CancelGraceResult =
        withContext(Dispatchers.IO) {
            val deviceToken = inheritancePrefs.ensureDeviceToken()
            val payload = org.json.JSONObject().apply {
                put("ownerEmail", ownerEmail)
                put("deviceToken", deviceToken)
                if (ownerIdToken != null) put("ownerIdToken", ownerIdToken)
            }.toString()

            val conn = URL(CANCEL_GRACE_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

            val code = conn.responseCode
            val body = runCatching { conn.inputStream.bufferedReader().readText() }.getOrElse { "" }

            if (code !in 200..299) throw Exception("onCancelGrace respondió HTTP $code: $body")

            runCatching {
                val json = org.json.JSONObject(body)
                when {
                    json.optString("status") == "inherited" -> CancelGraceResult.ALREADY_INHERITED
                    json.optBoolean("cancelled", false)     -> CancelGraceResult.CANCELLED
                    else                                    -> CancelGraceResult.ALREADY_ACTIVE
                }
            }.getOrDefault(CancelGraceResult.ALREADY_ACTIVE)
        }

    suspend fun disableOwner(ownerEmail: String, ownerIdToken: String? = null): DisableOwnerResult {
        val deviceToken = inheritancePrefs.ensureDeviceToken()
        val payload = org.json.JSONObject().apply {
            put("ownerEmail", ownerEmail)
            put("deviceToken", deviceToken)
            put("action", "disable")
            if (ownerIdToken != null) put("ownerIdToken", ownerIdToken)
        }.toString()
        val (code, respuesta) = postJson(payload)
        if (code == 409) {
            val yaEntregada = runCatching {
                org.json.JSONObject(respuesta).optString("error") == "inheritance_already_delivered"
            }.getOrDefault(false)
            if (yaEntregada) return DisableOwnerResult.ALREADY_INHERITED
        }
        if (code !in 200..299) {
            throw Exception("Cloud Function respondió HTTP $code al deshabilitar owner")
        }
        return DisableOwnerResult.DISABLED
    }

    suspend fun resignAsHeir(heirIdToken: String, ownerEmail: String) {
        withContext(Dispatchers.IO) {
            val payload = org.json.JSONObject().apply {
                put("heirIdToken", heirIdToken)
                put("ownerEmail", ownerEmail)
            }.toString()
            val conn = URL(HEIR_RESIGN_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            val code = conn.responseCode
            if (code !in 200..299) {
                val body = runCatching { conn.errorStream?.bufferedReader()?.readText() ?: "" }.getOrElse { "" }
                throw Exception("onHeirResign respondió HTTP $code: $body")
            }
        }
    }

    suspend fun confirmHeirFingerprint(ownerIdToken: String, ownerEmail: String, heirEmail: String) {
        withContext(Dispatchers.IO) {
            val payload = org.json.JSONObject().apply {
                put("ownerIdToken", ownerIdToken)
                put("ownerEmail", ownerEmail)
                put("heirEmail", heirEmail)
            }.toString()
            val conn = URL(CONFIRM_FINGERPRINT_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            val code = conn.responseCode
            if (code !in 200..299) {
                val body = runCatching { conn.errorStream?.bufferedReader()?.readText() ?: "" }.getOrElse { "" }
                throw Exception("onHeirFingerprintConfirm respondió HTTP $code: $body")
            }
        }
    }

    suspend fun deleteAllUserData(
        idToken: String,
        heirOfOwnerEmails: List<String>,
        memberOfAdminEmails: List<String>,
    ) {
        withContext(Dispatchers.IO) {
            val payload = org.json.JSONObject().apply {
                put("idToken", idToken)
                put("heirOfOwnerEmails", org.json.JSONArray(heirOfOwnerEmails))
                put("memberOfAdminEmails", org.json.JSONArray(memberOfAdminEmails))
            }.toString()
            val conn = URL(DELETE_ALL_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 30_000
            conn.readTimeout    = 60_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            val code = conn.responseCode
            if (code != 200) {
                val body = runCatching {
                    conn.errorStream?.bufferedReader()?.readText()
                        ?: conn.inputStream.bufferedReader().readText()
                }.getOrElse { "" }
                throw Exception("onDeleteAllUserData respondió HTTP $code: $body")
            }
        }
    }

    private suspend fun postJson(body: String): Pair<Int, String> =
        withContext(Dispatchers.IO) {
            val conn = URL(HEARTBEAT_URL).openConnection() as HttpURLConnection
            conn.requestMethod  = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
            val code = conn.responseCode
            val respuesta = runCatching {
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                stream?.bufferedReader()?.readText() ?: ""
            }.getOrDefault("")
            code to respuesta
        }

    private fun buildPayload(
        deviceToken:     String,
        email:           String,
        inactivityDays:  Int,
        heirEmails:      List<String> = emptyList(),
        publicKeyBase64: String?      = null,
        ownerSigningPublicKeyBase64: String? = null,
        ownerIdToken:    String?      = null,
        updateHeartbeat: Boolean      = false,
        locale:          String       = Locale.getDefault().language
    ): String {
        return org.json.JSONObject().apply {
            put("ownerEmail", email)
            put("deviceToken", deviceToken)
            put("inactivityDays", inactivityDays)
            put("heirEmails", org.json.JSONArray(heirEmails))
            put("locale", locale)
            if (publicKeyBase64 != null) put("publicKey", publicKeyBase64)
            if (ownerSigningPublicKeyBase64 != null) put("ownerSigningPublicKey", ownerSigningPublicKeyBase64)
            if (ownerIdToken    != null) put("ownerIdToken", ownerIdToken)
            if (updateHeartbeat) put("updateHeartbeat", true)
        }.toString()
    }
}

enum class CancelGraceResult {
    CANCELLED,

    ALREADY_ACTIVE,

    ALREADY_INHERITED,
}

enum class DisableOwnerResult {
    DISABLED,

    ALREADY_INHERITED,
}
