package com.thormakk.keyarmor.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import com.thormakk.keyarmor.appcheck.AppCheckHeader

@Singleton
class FamilyStatusRepository @Inject constructor() {

    enum class MemberStatus {
        ACTIVE,
        REVOKED,
        REMOVED,
        UNKNOWN
    }

    suspend fun checkMemberStatus(memberIdToken: String, adminEmail: String): MemberStatus =
        withContext(Dispatchers.IO) {
            val payload = org.json.JSONObject().apply {
                put("memberIdToken", memberIdToken)
                put("adminEmail", adminEmail)
            }.toString()
            val conn = URL(CHECK_MEMBER_STATUS_URL).openConnection() as HttpURLConnection
            conn.requestMethod  = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            val code = conn.responseCode
            if (code !in 200..299) throw Exception("onCheckMemberStatus HTTP $code")
            val body = conn.inputStream.bufferedReader().readText()
            when (org.json.JSONObject(body).optString("status")) {
                "active"  -> MemberStatus.ACTIVE
                "revoked" -> MemberStatus.REVOKED
                "removed" -> MemberStatus.REMOVED
                else      -> MemberStatus.UNKNOWN
            }
        }

    companion object {
        private const val CHECK_MEMBER_STATUS_URL =
            "<URL DEL SERVIDOR DE GOOGLE>"
    }
}
