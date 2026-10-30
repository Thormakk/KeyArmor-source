package com.thormakk.keyarmor.passkey

import com.thormakk.keyarmor.ui.theme.appButtonColors
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.provider.CallingAppInfo
import androidx.credentials.provider.PendingIntentHandler
import androidx.lifecycle.lifecycleScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.util.rememberMandatoryBiometricGate
import com.thormakk.keyarmor.data.passkey.PasskeyCrypto
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.security.SessionManager
import com.thormakk.keyarmor.ui.theme.KeyArmorTheme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class PasskeyCreationActivity : FragmentActivity() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CreationEntryPoint {
        fun passkeyRepository(): PasskeyRepository
        fun sessionManager(): SessionManager
        fun autoLockManager(): com.thormakk.keyarmor.security.AutoLockManager
        fun passkeyCrypto(): PasskeyCrypto
        fun publicSuffixList(): com.thormakk.keyarmor.autofill.PublicSuffixList
        fun authRepository(): com.thormakk.keyarmor.data.repository.AuthRepository
    }

    private val entryPoint: CreationEntryPoint by lazy {
        EntryPointAccessors.fromApplication(applicationContext, CreationEntryPoint::class.java)
    }

    private val passkeyRepository get() = entryPoint.passkeyRepository()
    private val publicSuffixList  get() = entryPoint.publicSuffixList()
    private val sessionManager    get() = entryPoint.sessionManager()
    private val passkeyCrypto     get() = entryPoint.passkeyCrypto()

    private var callingAppInfo: CallingAppInfo? = null

    private var themeId    by mutableStateOf("dark")
    private var accentArgb by androidx.compose.runtime.mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)

        val isUnlockRequest = intent.getBooleanExtra(EXTRA_IS_UNLOCK, false)

        if (isUnlockRequest) {
            val mainIntent = packageManager.getLaunchIntentForPackage(packageName)
            mainIntent?.let { startActivity(it) }
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val providerRequest = PendingIntentHandler.retrieveProviderCreateCredentialRequest(intent)
        if (providerRequest == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        callingAppInfo = providerRequest.callingAppInfo

        val createRequest = providerRequest.callingRequest
            as? androidx.credentials.CreatePublicKeyCredentialRequest
        if (createRequest == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        lifecycleScope.launch {
            val auth = entryPoint.authRepository()
            themeId    = auth.getThemeId()
            accentArgb = auth.getAccentColor()
        }

        entryPoint.autoLockManager().enforceIfExpired()
        if (!sessionManager.isUnlocked()) {
            setContent {
                KeyArmorTheme(themeId = themeId, accentArgb = accentArgb) {
                    VaultLockedScreen(
                        onOpenApp = {
                            packageManager.getLaunchIntentForPackage(packageName)
                                ?.let { startActivity(it) }
                            val cancelIntent = Intent()
                            PendingIntentHandler.setCreateCredentialException(
                                intent    = cancelIntent,
                                exception = CreateCredentialCancellationException()
                            )
                            setResult(RESULT_CANCELED, cancelIntent)
                            finish()
                        },
                        onCancel = {
                            val cancelIntent = Intent()
                            PendingIntentHandler.setCreateCredentialException(
                                intent    = cancelIntent,
                                exception = CreateCredentialCancellationException()
                            )
                            setResult(RESULT_CANCELED, cancelIntent)
                            finish()
                        }
                    )
                }
            }
            return
        }

        setContent {
            KeyArmorTheme(themeId = themeId, accentArgb = accentArgb) {
                val gateDeCreacion = rememberMandatoryBiometricGate(
                    verifyMasterPassword = { entryPoint.authRepository().verifyPassword(it) }
                )
                PasskeyCreationScreen(
                    requestJson = createRequest.requestJson,
                    onConfirm   = { gateDeCreacion.launch { createPasskey(createRequest) } },
                    onCancel    = {
                        val cancelIntent = Intent()
                        PendingIntentHandler.setCreateCredentialException(
                            intent    = cancelIntent,
                            exception = CreateCredentialCancellationException()
                        )
                        setResult(RESULT_CANCELED, cancelIntent)
                        finish()
                    }
                )
            }
        }
    }

    private fun createPasskey(
        request:         androidx.credentials.CreatePublicKeyCredentialRequest
    ) {
        lifecycleScope.launch {
            try {
                val json     = JSONObject(request.requestJson)
                val rpObj    = json.getJSONObject("rp")
                val rpId     = rpObj.getString("id")
                val rpName   = rpObj.optString("name", rpId)
                val userObj  = json.getJSONObject("user")
                val userId   = userObj.getString("id")
                val userName = userObj.optString("name", "")
                val userDisplayName = userObj.optString("displayName", userName)
                val challengeB64    = json.getString("challenge")

                val origin = PasskeyOriginResolver.resolve(callingAppInfo)
                    ?: throw SecurityException(
                        getString(R.string.passkey_error_origin_unverified)
                    )
                if (origin.startsWith("https://")) {
                    val host = runCatching { java.net.URI(origin).host }.getOrNull() ?: ""
                    val registrable = publicSuffixList.getRegistrableDomain(host)
                    val rpIdAdmisible = host == rpId || (
                        host.endsWith(".$rpId") &&
                        registrable != null &&
                        (rpId == registrable || rpId.endsWith(".$registrable"))
                    )
                    if (!rpIdAdmisible) {
                        throw SecurityException(
                            getString(
                                R.string.passkey_error_rpid_mismatch,
                                rpId,
                                origin,
                                registrable ?: getString(R.string.passkey_error_registrable_unresolved)
                            )
                        )
                    }
                }

                val kp = passkeyCrypto.generateKeyPair()

                passkeyRepository.saveNewPasskey(
                    credentialId      = kp.credentialId,
                    rpId              = rpId,
                    rpName            = rpName,
                    userId            = userId,
                    userName          = userName,
                    userDisplayName   = userDisplayName,
                    privateKeyPkcs8   = kp.privateKeyPkcs8,
                    publicKeyCoseBytes = kp.publicKeyCose
                )
                kp.privateKeyPkcs8.fill(0)

                val authData = passkeyCrypto.buildAttestationAuthData(
                    rpId          = rpId,
                    credentialId  = kp.credentialId,
                    cosePublicKey = kp.publicKeyCose,
                    signCount     = 0L
                )
                val attestationObject = passkeyCrypto.buildAttestationObject(authData)

                val clientDataJson = passkeyCrypto.buildCreateClientDataJson(
                    challenge = challengeB64,
                    origin    = origin
                )

                val clientDataB64    = Base64.encodeToString(
                    clientDataJson.toByteArray(Charsets.UTF_8),
                    Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                )
                val attestationB64   = Base64.encodeToString(
                    attestationObject,
                    Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                )
                val authDataB64      = Base64.encodeToString(
                    authData,
                    Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                )
                val publicKeyB64     = Base64.encodeToString(
                    kp.publicKeySubjectPublicKeyInfo,
                    Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                )

                val responseJson = """
                {
                  "id": "${kp.credentialId}",
                  "rawId": "${kp.credentialId}",
                  "type": "public-key",
                  "authenticatorAttachment": "platform",
                  "response": {
                    "clientDataJSON": "$clientDataB64",
                    "attestationObject": "$attestationB64",
                    "authenticatorData": "$authDataB64",
                    "publicKey": "$publicKeyB64",
                    "publicKeyAlgorithm": -7,
                    "transports": ["internal"]
                  },
                  "clientExtensionResults": {}
                }
                """.trimIndent()


                val resultIntent = Intent()
                PendingIntentHandler.setCreateCredentialResponse(
                    intent   = resultIntent,
                    response = CreatePublicKeyCredentialResponse(registrationResponseJson = responseJson)
                )
                setResult(RESULT_OK, resultIntent)
                finish()

            } catch (e: Exception) {
                val resultIntent = Intent()
                PendingIntentHandler.setCreateCredentialException(
                    intent    = resultIntent,
                    exception = androidx.credentials.exceptions.CreateCredentialUnknownException(
                        e.message ?: getString(R.string.passkey_create_error_generic)
                    )
                )
                setResult(RESULT_CANCELED, resultIntent)
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_IS_UNLOCK = "is_unlock"
    }
}

@Composable
private fun PasskeyCreationScreen(
    requestJson: String,
    onConfirm:   () -> Unit,
    onCancel:    () -> Unit
) {
    val rpName = remember(requestJson) {
        runCatching {
            JSONObject(requestJson).getJSONObject("rp").optString("name", "")
        }.getOrDefault("")
    }
    val userName = remember(requestJson) {
        runCatching {
            val u = JSONObject(requestJson).getJSONObject("user")
            u.optString("displayName", u.optString("name", ""))
        }.getOrDefault("")
    }

    var isCreating by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter            = painterResource(R.drawable.logo_keyarmor),
                contentDescription = null,
                modifier           = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text       = stringResource(R.string.passkey_create_title),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            if (rpName.isNotBlank()) {
                Text(
                    text  = stringResource(R.string.passkey_create_for_rp, rpName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (userName.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = userName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                text  = stringResource(R.string.passkey_create_storage_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            if (isCreating) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { isCreating = true; onConfirm() },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp), colors = appButtonColors()
                ) {
                    Text(stringResource(R.string.passkey_create_save_button))
                }
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick  = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    }
}

@Composable
private fun VaultLockedScreen(
    onOpenApp: () -> Unit,
    onCancel:  () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter            = painterResource(R.drawable.logo_keyarmor),
                contentDescription = null,
                modifier           = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text       = stringResource(R.string.passkey_get_vault_locked_title),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = stringResource(R.string.passkey_create_vault_locked_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick  = onOpenApp,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp), colors = appButtonColors()
            ) {
                Text(stringResource(R.string.passkey_create_open_keyarmor_button))
            }
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick  = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
}
