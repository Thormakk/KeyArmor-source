package com.thormakk.keyarmor.presentation.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.thormakk.keyarmor.data.drive.GoogleIdentityVerifier

sealed class GoogleConnectResult {
    data class Success(val email: String, val idToken: String) : GoogleConnectResult()
    object Cancelled : GoogleConnectResult()
    object NoCredential : GoogleConnectResult()
    data class Error(val message: String?) : GoogleConnectResult()
}

suspend fun requestGoogleAccountConnect(
    context: Context,
    credentialManager: CredentialManager,
    filterByAuthorizedAccounts: Boolean = false
): GoogleConnectResult {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(GoogleIdentityVerifier.GOOGLE_WEB_CLIENT_ID)
        .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
        .setAutoSelectEnabled(filterByAuthorizedAccounts)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        when (val credential = result.credential) {
            is GoogleIdTokenCredential ->
                GoogleConnectResult.Success(email = credential.id, idToken = credential.idToken)

            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                        GoogleConnectResult.Success(email = googleIdToken.id, idToken = googleIdToken.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        GoogleConnectResult.Error(e.message)
                    }
                } else {
                    GoogleConnectResult.Error(null)
                }
            }

            else -> GoogleConnectResult.Error(null)
        }
    } catch (e: NoCredentialException) {
        if (filterByAuthorizedAccounts) GoogleConnectResult.Cancelled
        else GoogleConnectResult.NoCredential
    } catch (e: GetCredentialException) {
        GoogleConnectResult.Error(e.message)
    }
}
