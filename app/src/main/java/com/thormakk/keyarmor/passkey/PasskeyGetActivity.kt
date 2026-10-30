package com.thormakk.keyarmor.passkey

import com.thormakk.keyarmor.ui.theme.appButtonColors
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.provider.CallingAppInfo
import androidx.credentials.provider.PendingIntentHandler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.util.rememberMandatoryBiometricGate
import com.thormakk.keyarmor.data.passkey.PasskeyCrypto
import com.thormakk.keyarmor.domain.model.ResultadoMaestra
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.screens.vault.ServiceAwareAvatar
import com.thormakk.keyarmor.security.BiometricCryptoManager
import com.thormakk.keyarmor.security.SessionManager
import com.thormakk.keyarmor.ui.theme.KeyArmorTheme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class PasskeyGetActivity : FragmentActivity() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface GetEntryPoint {
        fun passkeyRepository(): PasskeyRepository
        fun passkeyCrypto(): PasskeyCrypto
        fun authRepository(): AuthRepository
        fun sessionManager(): SessionManager
        fun biometricCryptoManager(): BiometricCryptoManager
        fun autoLockManager(): com.thormakk.keyarmor.security.AutoLockManager
        fun panicPinPrefs(): com.thormakk.keyarmor.data.prefs.PanicPinPrefs
        fun publicSuffixList(): com.thormakk.keyarmor.autofill.PublicSuffixList
    }

    private val entryPoint: GetEntryPoint by lazy {
        EntryPointAccessors.fromApplication(applicationContext, GetEntryPoint::class.java)
    }

    private fun mensajeDeEspera(waitMs: Long): String {
        val minutos = ((waitMs + 59_999L) / 60_000L).toInt().coerceAtLeast(1)
        return if (minutos >= 60) getString(R.string.error_login_throttled_hours, (minutos + 59) / 60)
               else               getString(R.string.error_login_throttled_minutes, minutos)
    }

    private val passkeyRepository get() = entryPoint.passkeyRepository()
    private val publicSuffixList  get() = entryPoint.publicSuffixList()
    private val passkeyCrypto     get() = entryPoint.passkeyCrypto()

    private var credentialId: String = ""
    private var rpId:         String = ""
    private var requestJson:  String = ""
    private var requestClientDataHash: ByteArray? = null

    private var callingAppInfo: CallingAppInfo? = null

    private var isSigning by mutableStateOf(false)

    private var unlockedViaBiometricThisSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)

        credentialId = intent.getStringExtra(EXTRA_CREDENTIAL_ID) ?: ""

        val providerRequest = try {
            PendingIntentHandler.retrieveProviderGetCredentialRequest(intent)
        } catch (_: Exception) { null }
        callingAppInfo = providerRequest?.callingAppInfo

        val publicKeyOption = providerRequest?.credentialOptions
            ?.filterIsInstance<GetPublicKeyCredentialOption>()
            ?.firstOrNull()
        val authenticatedRequestJson = publicKeyOption?.requestJson
        requestClientDataHash = publicKeyOption?.clientDataHash
        requestJson = authenticatedRequestJson
            ?: intent.getStringExtra(EXTRA_GET_REQUEST_JSON) ?: ""
        rpId = (runCatching { JSONObject(requestJson).optString("rpId") }
            .getOrNull()?.takeIf { it.isNotEmpty() })
            ?: intent.getStringExtra(EXTRA_RP_ID) ?: ""

        if (credentialId.isEmpty() || rpId.isEmpty()) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        var themeId    by mutableStateOf("dark")
        var accentArgb by mutableLongStateOf(0L)

        var displayRpName  by mutableStateOf(rpId)
        var displayAccount by mutableStateOf("")
        var createdAt      by mutableLongStateOf(0L)
        var lastUsedAt     by mutableStateOf<Long?>(null)
        entryPoint.autoLockManager().enforceIfExpired()
        var isVaultLocked    by mutableStateOf(!entryPoint.sessionManager().isUnlocked())
        var unlockError      by mutableStateOf<String?>(null)
        var biometricEnabled by mutableStateOf(false)
        var biometricKey: String? = null

        fun reloadPasskeyData() {
            lifecycleScope.launch {
                passkeyRepository.getPasskey(credentialId)?.let { pk ->
                    pk.rpName.takeIf { it.isNotBlank() }?.let { displayRpName = it }
                    displayAccount = pk.accountLabel
                    createdAt      = pk.createdAt
                    lastUsedAt     = pk.lastUsedAt
                }
            }
        }

        fun onBiometricUnlockSuccess(cipher: javax.crypto.Cipher) {
            val prefs = entryPoint.panicPinPrefs()
            if (prefs.coercionActiveFlow.value && !prefs.isHideModeExpired()) return
            val key = biometricKey ?: return
            try {
                val masterKeyBytes = entryPoint.biometricCryptoManager().decryptMasterKey(cipher, key)
                entryPoint.authRepository().setSessionKeyFromBytes(masterKeyBytes)
                masterKeyBytes.fill(0)
                entryPoint.autoLockManager().onExternalUnlock()
                unlockedViaBiometricThisSession = true
                reloadPasskeyData()
                isVaultLocked = false
            } catch (e: Exception) {
                lifecycleScope.launch { entryPoint.authRepository().clearBiometricKey() }
                entryPoint.biometricCryptoManager().deleteKey()
                biometricEnabled = false
                biometricKey     = null
            }
        }

        fun launchBiometricPrompt() {
            val key = biometricKey ?: return
            val cipher = try {
                entryPoint.biometricCryptoManager().getDecryptionCipher(key)
            } catch (e: Exception) {
                lifecycleScope.launch { entryPoint.authRepository().clearBiometricKey() }
                entryPoint.biometricCryptoManager().deleteKey()
                biometricEnabled = false
                biometricKey     = null
                return
            }

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (result.authenticationType != BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) return
                    result.cryptoObject?.cipher?.let { onBiometricUnlockSuccess(it) }
                }
                override fun onAuthenticationFailed() {   }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {   }
            }

            val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), callback)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.passkey_get_unlock_keyarmor_title))
                .setSubtitle(getString(R.string.passkey_get_unlock_biometric_subtitle))
                .setNegativeButtonText(getString(R.string.passkey_get_use_password_button))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()

            prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }

        lifecycleScope.launch {
            val auth = entryPoint.authRepository()
            themeId    = auth.getThemeId()
            accentArgb = auth.getAccentColor()
            passkeyRepository.getPasskey(credentialId)?.let { pk ->
                pk.rpName.takeIf { it.isNotBlank() }?.let { displayRpName = it }
                displayAccount = pk.accountLabel
                createdAt      = pk.createdAt
                lastUsedAt     = pk.lastUsedAt
            }

            if (isVaultLocked) {
                val isBioAvailable = BiometricManager.from(this@PasskeyGetActivity)
                    .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                        BiometricManager.BIOMETRIC_SUCCESS
                val storedKey = if (isBioAvailable) auth.getBiometricKey() else null
                if (!storedKey.isNullOrEmpty()) {
                    biometricKey     = storedKey
                    biometricEnabled = true
                    launchBiometricPrompt()
                }
            }
        }

        setContent {
            KeyArmorTheme(themeId = themeId, accentArgb = accentArgb) {
                if (isVaultLocked) {
                    VaultUnlockForPasskeyScreen(
                        rpName           = displayRpName,
                        hasError         = unlockError,
                        biometricEnabled = biometricEnabled,
                        onBiometricUnlock = { launchBiometricPrompt() },
                        onUnlock = { password ->
                            lifecycleScope.launch {
                                unlockError = null
                                when (val r = entryPoint.authRepository().unlockWithPassword(password)) {
                                    is ResultadoMaestra.Correcta -> {
                                        entryPoint.autoLockManager().onExternalUnlock()
                                        reloadPasskeyData()
                                        isVaultLocked = false
                                    }
                                    is ResultadoMaestra.EnEspera ->
                                        unlockError = mensajeDeEspera(r.faltanMs)
                                    is ResultadoMaestra.Incorrecta ->
                                        unlockError = getString(R.string.error_wrong_password)
                                }
                            }
                        },
                        onCancel = {
                            val resultIntent = Intent()
                            PendingIntentHandler.setGetCredentialException(
                                intent    = resultIntent,
                                exception = GetCredentialCancellationException()
                            )
                            setResult(RESULT_CANCELED, resultIntent)
                            finish()
                        }
                    )
                } else {
                    val gateDeFirma = rememberMandatoryBiometricGate(
                        verifyMasterPassword = { entryPoint.authRepository().verifyPassword(it) }
                    )
                    PasskeyGetScreen(
                        rpName       = displayRpName,
                        rpId         = rpId,
                        accountLabel = displayAccount,
                        createdAt    = createdAt,
                        lastUsedAt   = lastUsedAt,
                        isSigning    = isSigning,
                        onConfirm    = {
                            if (unlockedViaBiometricThisSession) {
                                isSigning = true
                                signAndReturn()
                            } else {
                                gateDeFirma.launch {
                                    isSigning = true
                                    signAndReturn()
                                }
                            }
                        },
                        onCancel     = {
                            val resultIntent = Intent()
                            PendingIntentHandler.setGetCredentialException(
                                intent    = resultIntent,
                                exception = GetCredentialCancellationException()
                            )
                            setResult(RESULT_CANCELED, resultIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun signAndReturn() {
        lifecycleScope.launch {
            var privateKeyBytes: ByteArray? = null
            try {
                val json      = JSONObject(requestJson)
                val challenge = json.getString("challenge")

                privateKeyBytes = passkeyRepository.getPrivateKeyBytes(credentialId)
                    ?: throw IllegalStateException(getString(R.string.passkey_get_error_private_key_not_found))
                val pkcs8 = privateKeyBytes

                val passkey = passkeyRepository.getPasskey(credentialId)
                    ?: throw IllegalStateException(getString(R.string.passkey_get_error_passkey_not_found))

                if (!passkey.rpId.equals(rpId, ignoreCase = true)) {
                    throw SecurityException(
                        getString(R.string.passkey_error_rpid_not_owner, passkey.rpId, rpId)
                    )
                }

                val authData = passkeyCrypto.buildAssertionAuthData(
                    rpId      = rpId,
                    signCount = 0L
                )

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
                val clientDataJson = passkeyCrypto.buildGetClientDataJson(
                    challenge = challenge,
                    origin    = origin
                )
                val clientDataBytes = clientDataJson.toByteArray(Charsets.UTF_8)

                val clientDataHash = requestClientDataHash ?: java.security.MessageDigest
                    .getInstance("SHA-256")
                    .digest(clientDataBytes)

                val signature = passkeyCrypto.sign(pkcs8, authData, clientDataHash)
                android.util.Log.i(
                    "PasskeyGet",
                    "DIAG aserción firmada rpId=$rpId origin=$origin " +
                        "requestTraeClientDataHash=${requestClientDataHash != null} " +
                        "coincideConElNuestro=${requestClientDataHash?.contentEquals(clientDataHash)}"
                )

                fun ByteArray.b64url() = Base64.encodeToString(
                    this, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                )

                val assertionResponseJson = JSONObject().apply {
                    put("id", credentialId)
                    put("rawId", credentialId)
                    put("type", "public-key")
                    put("authenticatorAttachment", "platform")
                    put("response", JSONObject().apply {
                        put("clientDataJSON", clientDataBytes.b64url())
                        put("authenticatorData", authData.b64url())
                        put("signature", signature.b64url())
                        put("userHandle", passkey.userId)
                    })
                    put("clientExtensionResults", JSONObject())
                }.toString()

                val resultIntent = Intent()
                PendingIntentHandler.setGetCredentialResponse(
                    intent   = resultIntent,
                    response = GetCredentialResponse(
                        credential = PublicKeyCredential(authenticationResponseJson = assertionResponseJson)
                    )
                )
                setResult(RESULT_OK, resultIntent)
                finish()

            } catch (e: Exception) {
                android.util.Log.e("PasskeyGet", "Falló la aserción: ${e.javaClass.simpleName}: ${e.message}", e)
                val resultIntent = Intent()
                PendingIntentHandler.setGetCredentialException(
                    intent    = resultIntent,
                    exception = androidx.credentials.exceptions.GetCredentialUnknownException(
                        e.message ?: getString(R.string.passkey_get_error_signing)
                    )
                )
                setResult(RESULT_CANCELED, resultIntent)
                finish()
            } finally {
                privateKeyBytes?.fill(0)
            }
        }
    }

    companion object {
        const val EXTRA_CREDENTIAL_ID    = "credential_id"
        const val EXTRA_RP_ID            = "rp_id"
        const val EXTRA_GET_REQUEST_JSON = "get_request_json"
    }
}

@Composable
private fun PasskeyGetScreen(
    rpName:       String,
    rpId:         String,
    accountLabel: String,
    createdAt:    Long,
    lastUsedAt:   Long?,
    isSigning:    Boolean,
    onConfirm:    () -> Unit,
    onCancel:     () -> Unit
) {
    val fmt = remember { SimpleDateFormat("d MMM yyyy  HH:mm", Locale.getDefault()) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.weight(1f))

            Text(
                text       = stringResource(R.string.passkey_get_login_title),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Surface(
                shape          = RoundedCornerShape(12.dp),
                color          = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier       = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ServiceAwareAvatar(
                            title = rpName,
                            url   = "https://$rpId"
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text       = rpName,
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface,
                            modifier   = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(10.dp))
                        Row(
                            modifier          = Modifier.offset(y = (-16).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter            = painterResource(R.drawable.logo_keyarmor),
                                contentDescription = null,
                                modifier           = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text  = buildAnnotatedString {
                                    append("Key")
                                    pushStyle(SpanStyle(color = MaterialTheme.colorScheme.primary))
                                    append("Armor")
                                },
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (accountLabel.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text     = accountLabel,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 50.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color    = MaterialTheme.colorScheme.outlineVariant
                    )

                    PasskeyMetaRow(
                        icon  = Icons.Default.Language,
                        label = stringResource(R.string.passkey_field_domain),
                        value = rpId
                    )

                    if (createdAt > 0L) {
                        PasskeyMetaRow(
                            icon  = Icons.Default.CalendarToday,
                            label = stringResource(R.string.passkey_field_created),
                            value = fmt.format(Date(createdAt))
                        )
                    }

                    if (lastUsedAt != null) {
                        PasskeyMetaRow(
                            icon  = Icons.Default.AccessTime,
                            label = stringResource(R.string.passkey_field_last_used),
                            value = fmt.format(Date(lastUsedAt))
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (isSigning) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
            } else {
                OutlinedButton(
                    onClick  = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.5.dp, MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = stringResource(R.string.action_confirm),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick  = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text  = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VaultUnlockForPasskeyScreen(
    rpName:           String,
    hasError:         String?,
    biometricEnabled: Boolean,
    onBiometricUnlock: () -> Unit,
    onUnlock:         (String) -> Unit,
    onCancel:         () -> Unit
) {
    var password         by remember { mutableStateOf("") }
    var showPasswordForm by remember { mutableStateOf(!biometricEnabled) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = if (biometricEnabled && !showPasswordForm)
                                         Icons.Default.Fingerprint
                                     else
                                         Icons.Default.Lock,
                contentDescription = null,
                modifier           = Modifier.size(56.dp),
                tint               = MaterialTheme.colorScheme.primary
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
                text = when {
                    biometricEnabled && !showPasswordForm ->
                        if (rpName.isNotBlank())
                            stringResource(R.string.passkey_get_confirm_biometric_with_rp, rpName)
                        else
                            stringResource(R.string.passkey_get_confirm_biometric_generic)
                    rpName.isNotBlank() ->
                        stringResource(R.string.passkey_get_enter_password_with_rp, rpName)
                    else ->
                        stringResource(R.string.passkey_get_enter_password_generic)
                },
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            if (biometricEnabled && !showPasswordForm) {
                OutlinedButton(
                    onClick  = onBiometricUnlock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.5.dp, MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = stringResource(R.string.passkey_get_use_biometric_button),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { showPasswordForm = true }) {
                    Text(
                        text  = stringResource(R.string.autofill_use_password),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it },
                    label                = { Text(stringResource(R.string.auth_master_password)) },
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (password.isNotBlank()) onUnlock(password) }
                    ),
                    isError  = hasError != null,
                    colors   = OutlinedTextFieldDefaults.colors(
                        errorBorderColor         = dangerColor(),
                        errorSupportingTextColor = dangerColor()
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                hasError?.let { mensaje ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = mensaje,
                        color = dangerColor(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick  = { if (password.isNotBlank()) onUnlock(password) },
                    enabled  = password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(14.dp), colors = appButtonColors()
                ) {
                    Text(
                        text       = stringResource(R.string.lock_action_unlock),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (biometricEnabled) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showPasswordForm = false; onBiometricUnlock() }) {
                        Icon(
                            imageVector        = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = stringResource(R.string.passkey_get_use_biometric_button),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick  = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PasskeyMetaRow(
    icon:  androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
