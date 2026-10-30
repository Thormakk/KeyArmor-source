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
class FcmRepository @Inject constructor() {

    companion object {
        private const val REGISTER_FCM_URL =
            "<URL DEL SERVIDOR DE GOOGLE>"
    }

    suspend fun registerToken(idToken: String, fcmToken: String): Boolean =
        withContext(Dispatchers.IO) {
            fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
            val body = """{"idToken":"${idToken.esc()}","fcmToken":"${fcmToken.esc()}"}"""
            runCatching {
                val conn = URL(REGISTER_FCM_URL).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                AppCheckHeader.attach(conn)
                conn.connectTimeout = 15_000
                conn.readTimeout    = 15_000
                conn.doOutput       = true
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
                conn.responseCode in 200..299
            }.getOrDefault(false)
        }
}
