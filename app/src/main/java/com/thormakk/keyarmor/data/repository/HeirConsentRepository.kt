package com.thormakk.keyarmor.data.repository

import androidx.appcompat.app.AppCompatDelegate
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
class HeirConsentRepository @Inject constructor() {

    companion object {
        private const val HEIR_CONSENT_URL = "<URL DEL SERVIDOR DE GOOGLE>"
    }

    suspend fun submitConsent(
        heirEmail:              String,
        encryptedHeirSubBase64: String?,
        ownerEmail:             String,
        accepted:               Boolean,
        heirIdToken:            String,
        heirLocale:             String = AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language,
        heirPublicKeyB64:       String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val decision = if (accepted) "accepted" else "declined"
        val encryptedField = if (accepted && encryptedHeirSubBase64 != null)
            ""","encryptedHeirSubBase64":"${encryptedHeirSubBase64.esc()}""""
        else ""
        val publicKeyField = if (accepted && heirPublicKeyB64 != null)
            ""","heirPublicKeyBase64":"${heirPublicKeyB64.esc()}""""
        else ""
        val body = """{"heirEmail":"${heirEmail.esc()}","ownerEmail":"${ownerEmail.esc()}","decision":"$decision","heirIdToken":"${heirIdToken.esc()}","heirLocale":"${heirLocale.esc()}"$encryptedField$publicKeyField}"""
        val (code, _) = postJsonWithBody(body)
        code in 200..299
    }

    private fun postJsonWithBody(body: String): Pair<Int, String> {
        val conn = URL(HEIR_CONSENT_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        AppCheckHeader.attach(conn)
        conn.connectTimeout = 15_000
        conn.readTimeout    = 15_000
        conn.doOutput       = true
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val code = conn.responseCode
        val responseBody = runCatching {
            conn.inputStream.bufferedReader().readText()
        }.getOrElse {
            conn.errorStream?.bufferedReader()?.readText() ?: ""
        }
        return code to responseBody
    }
}
