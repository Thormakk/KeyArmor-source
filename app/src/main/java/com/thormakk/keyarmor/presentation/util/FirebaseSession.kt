package com.thormakk.keyarmor.presentation.util

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

suspend fun establishFirebaseSession(googleIdToken: String) {
    try {
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).await()
        Log.d("FirebaseSession", "Sesion de Firebase Auth establecida/renovada")
    } catch (e: Exception) {
        Log.w("FirebaseSession", "establishFirebaseSession fallo: ${e.message}")
    }
}

data class FirebaseIdentityToken(val idToken: String, val email: String?)

@Suppress("UNUSED_PARAMETER")
suspend fun getFirebaseIdentityTokenOrNull(context: Context): FirebaseIdentityToken? {
    return try {
        val user = FirebaseAuth.getInstance().currentUser
        val idToken = user?.getIdToken(false)?.await()?.token
        if (user != null && idToken != null) FirebaseIdentityToken(idToken = idToken, email = user.email) else null
    } catch (e: Exception) {
        Log.w("FirebaseSession", "getFirebaseIdentityTokenOrNull fallo: ${e.message}")
        null
    }
}

fun extractGoogleSubFromJwt(idToken: String): String? = try {
    val parts = idToken.split(".")
    if (parts.size < 2) {
        null
    } else {
        var payload = parts[1].replace('-', '+').replace('_', '/')
        while (payload.length % 4 != 0) payload += "="
        val json = org.json.JSONObject(
            String(android.util.Base64.decode(payload, android.util.Base64.DEFAULT))
        )
        val googleSub = json.optJSONObject("firebase")
            ?.optJSONObject("identities")
            ?.optJSONArray("google.com")
            ?.optString(0)
            ?.takeIf { it.isNotBlank() }
        googleSub ?: json.optString("sub").ifBlank { null }
    }
} catch (e: Exception) {
    null
}
