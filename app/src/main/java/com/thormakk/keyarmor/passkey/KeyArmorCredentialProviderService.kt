package com.thormakk.keyarmor.passkey

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.provider.BeginCreateCredentialRequest
import androidx.credentials.provider.BeginCreateCredentialResponse
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetCredentialResponse
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
import androidx.credentials.provider.CreateEntry
import androidx.credentials.provider.CredentialProviderService
import androidx.credentials.provider.ProviderClearCredentialStateRequest
import androidx.credentials.provider.PublicKeyCredentialEntry
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.presentation.screens.passkeys.PasskeysViewModel
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class KeyArmorCredentialProviderService : CredentialProviderService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PasskeyEntryPoint {
        fun passkeyRepository(): PasskeyRepository
        fun sessionManager(): SessionManager
        fun autoLockManager(): com.thormakk.keyarmor.security.AutoLockManager
    }

    private val entryPoint: PasskeyEntryPoint by lazy {
        EntryPointAccessors.fromApplication(applicationContext, PasskeyEntryPoint::class.java)
    }

    private val passkeyRepository get() = entryPoint.passkeyRepository()
    private val sessionManager    get() = entryPoint.sessionManager()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBeginCreateCredentialRequest(
        request:            BeginCreateCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginCreateCredentialResponse, androidx.credentials.exceptions.CreateCredentialException>
    ) {
        markProviderActive()
        Log.d(TAG, "onBeginCreateCredentialRequest: type=${request::class.simpleName}")
        try {
            val createEntries = listOf(buildCreateEntry())
            Log.d(TAG, "onBeginCreateCredentialRequest: devolviendo ${createEntries.size} entrada(s)")
            callback.onResult(
                BeginCreateCredentialResponse(createEntries = createEntries)
            )
        } catch (e: Exception) {
            Log.e(TAG, "onBeginCreateCredentialRequest: error", e)
            callback.onError(
                androidx.credentials.exceptions.CreateCredentialUnknownException(
                    e.message ?: getString(R.string.passkey_create_error_generic)
                )
            )
        }
    }

    override fun onBeginGetCredentialRequest(
        request:            BeginGetCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>
    ) {
        markProviderActive()
        Log.d(TAG, "onBeginGetCredentialRequest: ${request.beginGetCredentialOptions.size} opción(es)")
        serviceScope.launch {
            try {
                val response = processGetRequest(request)
                Log.d(TAG, "onBeginGetCredentialRequest: ${response.credentialEntries.size} entrada(s)")
                callback.onResult(response)
            } catch (e: Exception) {
                Log.e(TAG, "onBeginGetCredentialRequest: error", e)
                callback.onError(
                    androidx.credentials.exceptions.GetCredentialUnknownException(e.message ?: getString(R.string.error_unknown))
                )
            }
        }
    }

    private suspend fun processGetRequest(request: BeginGetCredentialRequest): BeginGetCredentialResponse {
        entryPoint.autoLockManager().enforceIfExpired()
        val vaultUnlocked = sessionManager.isUnlocked()
        val entries       = mutableListOf<PublicKeyCredentialEntry>()

        for (option in request.beginGetCredentialOptions) {
            val pkOption = option as? BeginGetPublicKeyCredentialOption ?: continue

            val rpId = runCatching {
                JSONObject(pkOption.requestJson).getString("rpId")
            }.getOrNull() ?: continue

            if (vaultUnlocked) {
                for (passkey in passkeyRepository.getPasskeysForRp(rpId)) {
                    entries.add(
                        PublicKeyCredentialEntry.Builder(
                            context  = this,
                            username = passkey.accountLabel.ifBlank { passkey.rpId },
                            pendingIntent = buildGetPendingIntent(
                                passkey.credentialId, passkey.rpId, pkOption.requestJson
                            ),
                            beginGetPublicKeyCredentialOption = pkOption
                        )
                        .setDisplayName(passkey.rpName.ifBlank { passkey.rpId })
                        .setLastUsedTime(passkey.lastUsedAt?.let { Instant.ofEpochMilli(it) })
                        .build()
                    )
                }
            } else {
                for (entity in passkeyRepository.getPasskeyEntityListForRp(rpId)) {
                    entries.add(
                        PublicKeyCredentialEntry.Builder(
                            context  = this,
                            username = entity.rpId,
                            pendingIntent = buildGetPendingIntent(
                                entity.credentialId, entity.rpId, pkOption.requestJson
                            ),
                            beginGetPublicKeyCredentialOption = pkOption
                        )
                        .setDisplayName(entity.rpId)
                        .build()
                    )
                }
            }
        }

        return BeginGetCredentialResponse(
            credentialEntries     = entries,
            authenticationActions = emptyList(),
            remoteEntry           = null
        )
    }

    private fun buildGetPendingIntent(
        credentialId: String,
        rpId:         String,
        requestJson:  String
    ): PendingIntent {
        val intent = Intent(this, PasskeyGetActivity::class.java).apply {
            putExtra(PasskeyGetActivity.EXTRA_CREDENTIAL_ID,    credentialId)
            putExtra(PasskeyGetActivity.EXTRA_RP_ID,            rpId)
            putExtra(PasskeyGetActivity.EXTRA_GET_REQUEST_JSON, requestJson)
        }
        return PendingIntent.getActivity(
            this,
            credentialId.hashCode(),
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onClearCredentialStateRequest(
        request:            ProviderClearCredentialStateRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<Void?, ClearCredentialException>
    ) {
        callback.onResult(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun buildCreateEntry(): CreateEntry {
        val createIntent = Intent(this, PasskeyCreationActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            PENDING_INTENT_CREATE_CODE,
            createIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return CreateEntry.Builder(
            accountName   = getString(R.string.app_name),
            pendingIntent = pendingIntent
        ).build()
    }

    private fun markProviderActive() {
        applicationContext
            .getSharedPreferences(PasskeysViewModel.PREFS_NAME, MODE_PRIVATE)
            .edit { putBoolean(PasskeysViewModel.PREF_PROVIDER_ACTIVE, true) }
    }

    companion object {
        private const val TAG                        = "KeyArmorCredProvider"
        private const val PENDING_INTENT_CREATE_CODE = 1001
    }
}
