package com.thormakk.keyarmor.data.drive

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class VerifiedGoogleIdentity(
    val sub:   String,
    val email: String?
)

@Singleton
class GoogleIdentityVerifier @Inject constructor() {

    private val verifier: GoogleIdTokenVerifier by lazy {
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(GOOGLE_WEB_CLIENT_ID))
            .build()
    }

    suspend fun verify(idTokenString: String): VerifiedGoogleIdentity? = withContext(Dispatchers.IO) {
        runCatching {
            val token = verifier.verify(idTokenString) ?: return@withContext null
            val payload = token.payload
            val sub = payload.subject?.takeIf { it.isNotBlank() } ?: return@withContext null
            VerifiedGoogleIdentity(sub = sub, email = payload.email)
        }.getOrNull()
    }

    companion object {
        const val GOOGLE_WEB_CLIENT_ID =
            "<ID DE CLIENTE DE GOOGLE>"
    }
}
