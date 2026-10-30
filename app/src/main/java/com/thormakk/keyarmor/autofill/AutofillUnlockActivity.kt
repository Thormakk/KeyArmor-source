package com.thormakk.keyarmor.autofill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thormakk.keyarmor.presentation.screens.auth.AppLogoWithVignette
import com.thormakk.keyarmor.ui.theme.InterFamily
import com.thormakk.keyarmor.ui.theme.NavyAccent
import com.thormakk.keyarmor.ui.theme.WarningAmber
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.ResultadoMaestra
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.util.TotpGenerator
import com.thormakk.keyarmor.security.BiometricCryptoManager
import com.thormakk.keyarmor.ui.theme.KeyArmorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AutofillUnlockActivity : AppCompatActivity() {

    @Inject lateinit var authRepository:          AuthRepository
    @Inject lateinit var biometricCrypto:         BiometricCryptoManager
    @Inject lateinit var vaultRepository:         VaultRepository
    @Inject lateinit var creditCardRepository:    CreditCardRepository
    @Inject lateinit var vaultMetadataRepository: VaultMetadataRepository
    @Inject lateinit var panicPinPrefs:           PanicPinPrefs
    @Inject lateinit var autoLockManager:         com.thormakk.keyarmor.security.AutoLockManager
    @Inject lateinit var twoFactorRepository:     TwoFactorRepository
    @Inject lateinit var publicSuffixList:        PublicSuffixList
    @Inject lateinit var autofillPrefs:           com.thormakk.keyarmor.data.prefs.AutofillPrefs

    private val saveInfoFactory by lazy { AutofillSaveInfoFactory(this, publicSuffixList) }

    private val parsed: ParsedAutofillRequest by lazy { ParsedAutofillRequest.fromIntent(intent) }

    private val uiPassword  = mutableStateOf("")
    private val uiError     = mutableStateOf<String?>(null)
    private val uiIsLoading = mutableStateOf(false)
    private val uiBioAvail  = mutableStateOf(false)
    private val uiNoMatches = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)

        lifecycleScope.launch {
            val bioOk     = BiometricManager.from(this@AutofillUnlockActivity)
                                .canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
            val keyStored = authRepository.isBiometricKeyStored()
            val coercionActive = panicPinPrefs.coercionActiveFlow.value
            uiBioAvail.value = bioOk && keyStored && !coercionActive

            if (uiBioAvail.value) launchBiometricPrompt()
        }

        setContent {
            KeyArmorTheme {
                if (uiNoMatches.value) {
                    AutofillNoMatchesContent(onClose = { finishCanceled() })
                    return@KeyArmorTheme
                }
                AutofillUnlockContent(
                    password         = uiPassword.value,
                    error            = uiError.value,
                    isLoading        = uiIsLoading.value,
                    bioAvailable     = uiBioAvail.value,
                    onPasswordChange = { uiPassword.value = it; uiError.value = null },
                    onUnlock         = { tryPasswordUnlock(uiPassword.value) },
                    onBiometric      = { launchBiometricPrompt() },
                    onCancel         = { finishCanceled() }
                )
            }
        }
    }

    private fun tryPasswordUnlock(password: String) {
        if (password.isBlank()) {
            uiError.value = getString(R.string.autofill_error_empty_password)
            return
        }
        uiIsLoading.value = true
        uiError.value     = null
        lifecycleScope.launch {
            when (val resultado = authRepository.unlockWithPassword(password)) {
                is ResultadoMaestra.Correcta -> onUnlockSuccess()
                is ResultadoMaestra.EnEspera -> {
                    uiIsLoading.value = false
                    uiError.value     = throttleMessage(resultado.faltanMs)
                }
                is ResultadoMaestra.Incorrecta -> {
                    uiIsLoading.value = false
                    uiError.value     = getString(R.string.autofill_error_wrong_password)
                }
            }
        }
    }

    private fun throttleMessage(waitMs: Long): String {
        val minutes = ((waitMs + 59_999L) / 60_000L).toInt().coerceAtLeast(1)
        return if (minutes >= 60) getString(R.string.error_login_throttled_hours, (minutes + 59) / 60)
               else               getString(R.string.error_login_throttled_minutes, minutes)
    }

    private fun launchBiometricPrompt() {
        if (panicPinPrefs.coercionActiveFlow.value) return
        lifecycleScope.launch {
            val storedKey = authRepository.getBiometricKey() ?: return@launch
            val cipher    = runCatching { biometricCrypto.getDecryptionCipher(storedKey) }
                .getOrNull() ?: return@launch

            val executor = ContextCompat.getMainExecutor(this@AutofillUnlockActivity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (result.authenticationType != BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) return
                    val authCipher = result.cryptoObject?.cipher ?: return
                    lifecycleScope.launch {
                        try {
                            val keyBytes = biometricCrypto.decryptMasterKey(authCipher, storedKey)
                            authRepository.setSessionKeyFromBytes(keyBytes)
                            keyBytes.fill(0)
                            onUnlockSuccess()
                        } catch (_: Exception) {
                            uiError.value = getString(R.string.autofill_error_biometric_invalid)
                        }
                    }
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                }
                override fun onAuthenticationFailed() {   }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.autofill_biometric_title))
                .setSubtitle(getString(R.string.autofill_biometric_subtitle))
                .setNegativeButtonText(getString(R.string.autofill_use_password))
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .build()

            BiometricPrompt(this@AutofillUnlockActivity, executor, callback)
                .authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private suspend fun onUnlockSuccess() {
        autoLockManager.onExternalUnlock()
        val response = buildFillResponse()
        if (response != null) {
            val resultIntent = Intent().apply {
                putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, response)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            uiNoMatches.value = true
        }
    }

    private suspend fun buildFillResponse(): FillResponse? {
        val builder     = FillResponse.Builder()
        var hasDatasets = false

        if (parsed.isLoginForm && !parsed.enContrasenaNueva) {
            val entries = runCatching { vaultRepository.observeEntriesForAutofill().first() }
                .getOrDefault(emptyList())
            val deEstaApp = entries.filter { it.urls.any { u -> matchesDomainOrPackage(u) } }
            val etiquetaActual = intent.getStringExtra(EXTRA_FIELD_LABEL) ?: ""
            val matched = deEstaApp.filter { FieldLabel.sameField(it.fieldLabel, etiquetaActual) }.ifEmpty {
                deEstaApp.sortedBy { FieldLabel.rangoPorEtiqueta(it.fieldLabel, etiquetaActual) }
            }

            val multiVault = matched.map { it.vaultId }.toSet().size > 1
            val vaultNames: Map<String, String> = if (multiVault)
                vaultMetadataRepository.getAll().associate { it.id to it.name }
            else emptyMap()

            matched.take(5).forEach { entry ->
                val vaultName = if (multiVault) vaultNames[entry.vaultId] else null
                val ds = buildCredentialDataset(entry, vaultName) ?: return@forEach
                builder.addDataset(ds)
                hasDatasets = true
            }
        }

        if (parsed.isCardForm) {
            val cards = runCatching { creditCardRepository.observeCardsForAutofill().first() }
                .getOrDefault(emptyList())
            cards.take(3).forEach { card ->
                val ds = buildCardDataset(card) ?: return@forEach
                builder.addDataset(ds)
                hasDatasets = true
            }
        }

        if (parsed.isOtpForm) {
            val accounts = runCatching { twoFactorRepository.observeAccounts().first() }
                .getOrDefault(emptyList())
            for (account in accounts.take(5)) {
                if (!account.hasTotp) continue
                if (!matchesTotpAccount(account, parsed, vaultRepository, publicSuffixList)) continue
                val code = TotpGenerator.generate(
                    secret    = account.secret,
                    digits    = account.digits,
                    period    = account.period,
                    algorithm = account.algorithm,
                ) ?: continue
                val ds = buildTotpDataset(account, code.code) ?: continue
                builder.addDataset(ds)
                hasDatasets = true
            }
        }

        val saveInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
            !autofillPrefs.isSaveBlocked(saveInfoFactory.blockTargets(parsed)))
            saveInfoFactory.build(parsed) else null
        saveInfo?.let { builder.setSaveInfo(it) }

        return when {
            hasDatasets -> builder.build()
            parsed.enContrasenaNueva && saveInfo != null -> builder.build()
            else -> null
        }
    }

    private fun buildCredentialDataset(entry: VaultEntry, vaultName: String? = null): Dataset? {
        val displayUser  = entry.username.ifEmpty { entry.email }
        val etiquetaGuardada = entry.fieldLabel
        val tituloVisible = listOf(" – ", " - ")
            .map { it + etiquetaGuardada }
            .firstOrNull { etiquetaGuardada.isNotBlank() && entry.title.endsWith(it, ignoreCase = true) }
            ?.let { entry.title.dropLast(it.length) }
            ?: entry.title
        val baseSubtitle = displayUser.ifEmpty {
            val yaEnTitulo = etiquetaGuardada.isNotBlank() &&
                FieldLabel.normalize(tituloVisible).contains(FieldLabel.normalize(etiquetaGuardada))
            if (etiquetaGuardada.isNotBlank() && !yaEnTitulo) etiquetaGuardada else ""
        }
        val subtitle     = if (vaultName != null) "$baseSubtitle · $vaultName" else baseSubtitle
        val presentation = buildPresentation(
            title    = tituloVisible,
            subtitle = subtitle
        )
        val builder  = Dataset.Builder(presentation)
        var hasValue = false

        val usernameId = parsed.usernameId
        val emailId    = parsed.emailId
        if (usernameId != null && emailId != null) {
            builder.setValue(usernameId, AutofillValue.forText(entry.username))
            builder.setValue(emailId,    AutofillValue.forText(entry.email))
            hasValue = true
        } else {
            parsed.loginFieldId?.let {
                builder.setValue(it, AutofillValue.forText(displayUser))
                hasValue = true
            }
        }
        parsed.passwordId?.let { builder.setValue(it, AutofillValue.forText(entry.password)); hasValue = true }

        return if (hasValue) builder.build() else null
    }

    private fun buildCardDataset(card: CreditCard): Dataset? {
        val title        = card.issuer.ifEmpty { card.brand }.ifEmpty { getString(R.string.hub_pinned_fallback_credit_card) }
        val last4        = card.cardNumber.takeLast(4).let { if (it.isNotEmpty()) "•••• $it" else "" }
        val presentation = buildPresentation(
            title    = title,
            subtitle = "${card.cardholder}  $last4".trim()
        )
        val builder  = Dataset.Builder(presentation)
        var hasValue = false

        parsed.cardNumberId?.let { builder.setValue(it, AutofillValue.forText(card.cardNumber)); hasValue = true }
        parsed.cardholderId?.let { builder.setValue(it, AutofillValue.forText(card.cardholder)); hasValue = true }
        val expiryValue = if (parsed.isBlindCardFallback)
            card.expiryDate.filter { it.isDigit() }
        else
            card.expiryDate
        parsed.expiryDateId?.let { builder.setValue(it, AutofillValue.forText(expiryValue)); hasValue = true }
        parsed.cvvId?.let        { builder.setValue(it, AutofillValue.forText(card.cvv));        hasValue = true }

        return if (hasValue) builder.build() else null
    }

    private fun buildTotpDataset(account: com.thormakk.keyarmor.domain.model.TwoFactorAccount, code: String): Dataset? {
        val title    = account.issuer.ifEmpty { account.name }.ifEmpty { "2FA" }
        val subtitle = account.accountLabel.ifEmpty { account.name }
        val presentation = buildPresentation(title, subtitle)
        val builder = Dataset.Builder(presentation)
        parsed.otpId?.let { builder.setValue(it, AutofillValue.forText(code)) } ?: return null
        return builder.build()
    }

    private fun buildPresentation(title: String, subtitle: String) =
        RemoteViews(packageName, R.layout.autofill_item).apply {
            setTextViewText(R.id.autofill_text_title,    title)
            setTextViewText(R.id.autofill_text_subtitle, subtitle)
        }

    private fun matchesDomainOrPackage(entryUrl: String): Boolean =
        AutofillMatcher.matches(entryUrl, parsed.webDomain, parsed.packageName, publicSuffixList)

    private fun finishCanceled() { setResult(RESULT_CANCELED); finish() }

    companion object
}

private const val EXTRA_PACKAGE    = "ka_af_pkg"
private const val EXTRA_DOMAIN     = "ka_af_domain"
private const val EXTRA_USERNAME   = "ka_af_uid"
private const val EXTRA_EMAIL      = "ka_af_eid"
private const val EXTRA_PASSWORD   = "ka_af_pwd"
private const val EXTRA_CARD_NUM   = "ka_af_cn"
private const val EXTRA_CARDHOLDER = "ka_af_ch"
private const val EXTRA_EXPIRY     = "ka_af_exp"
private const val EXTRA_CVV        = "ka_af_cvv"
private const val EXTRA_OTP               = "ka_af_otp"
private const val EXTRA_BLIND_CARD_FALLBK = "ka_af_bcf"
private const val EXTRA_NEW_PWD              = "ka_af_npw"
private const val EXTRA_FOCUSED              = "ka_af_foc"
private const val EXTRA_FIELD_LABEL           = "ka_af_lbl"

private fun Intent.autofillIdExtra(key: String): android.view.autofill.AutofillId? =
    if (android.os.Build.VERSION.SDK_INT >= 33) getParcelableExtra(key, android.view.autofill.AutofillId::class.java)
    else @Suppress("DEPRECATION") getParcelableExtra(key)

private fun Intent.autofillIdListExtra(key: String): List<android.view.autofill.AutofillId> =
    (if (android.os.Build.VERSION.SDK_INT >= 33)
        getParcelableArrayListExtra(key, android.view.autofill.AutofillId::class.java)
    else @Suppress("DEPRECATION") getParcelableArrayListExtra<android.view.autofill.AutofillId>(key))
        ?: emptyList()

fun ParsedAutofillRequest.Companion.fromIntent(intent: Intent): ParsedAutofillRequest =
    ParsedAutofillRequest(
        packageName  = intent.getStringExtra(EXTRA_PACKAGE)          ?: "",
        webDomain    = intent.getStringExtra(EXTRA_DOMAIN),
        usernameId   = intent.autofillIdExtra(EXTRA_USERNAME),
        emailId      = intent.autofillIdExtra(EXTRA_EMAIL),
        passwordId   = intent.autofillIdExtra(EXTRA_PASSWORD),
        cardNumberId = intent.autofillIdExtra(EXTRA_CARD_NUM),
        cardholderId = intent.autofillIdExtra(EXTRA_CARDHOLDER),
        expiryDateId = intent.autofillIdExtra(EXTRA_EXPIRY),
        cvvId        = intent.autofillIdExtra(EXTRA_CVV),
        otpId               = intent.autofillIdExtra(EXTRA_OTP),
        isBlindCardFallback = intent.getBooleanExtra(EXTRA_BLIND_CARD_FALLBK, false),
        focusedFieldId      = intent.autofillIdExtra(EXTRA_FOCUSED),
        newPasswordIds      = intent.autofillIdListExtra(EXTRA_NEW_PWD).toSet(),
    )

fun AutofillUnlockActivity.Companion.createIntent(
    context: Context,
    parsed:  ParsedAutofillRequest,
    fieldLabel: String = ""
): Intent = Intent(context, AutofillUnlockActivity::class.java).apply {
    putExtra(EXTRA_FIELD_LABEL, fieldLabel)
    putExtra(EXTRA_PACKAGE,    parsed.packageName)
    putExtra(EXTRA_DOMAIN,     parsed.webDomain)
    putExtra(EXTRA_USERNAME,   parsed.usernameId)
    putExtra(EXTRA_EMAIL,      parsed.emailId)
    putExtra(EXTRA_PASSWORD,   parsed.passwordId)
    putExtra(EXTRA_CARD_NUM,   parsed.cardNumberId)
    putExtra(EXTRA_CARDHOLDER, parsed.cardholderId)
    putExtra(EXTRA_EXPIRY,     parsed.expiryDateId)
    putExtra(EXTRA_CVV,        parsed.cvvId)
    putExtra(EXTRA_OTP,               parsed.otpId)
    putExtra(EXTRA_BLIND_CARD_FALLBK, parsed.isBlindCardFallback)
    putExtra(EXTRA_FOCUSED,           parsed.focusedFieldId)
    putParcelableArrayListExtra(EXTRA_NEW_PWD, ArrayList(parsed.newPasswordIds))
}

private val lockBg        = Color(0xFF0D1B2A)
private val lockOnBg      = Color(0xFFE8EAF0)
private val lockOnVariant = Color(0xFF9AA3B2)
private val lockPrimary   = NavyAccent

@Composable
private fun AutofillNoMatchesContent(onClose: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(lockBg)
    ) {
        val logoSize = (maxHeight * 0.20f).coerceIn(90.dp, 180.dp)

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLogoWithVignette(size = logoSize.value.toInt(), bgColor = lockBg)

            Spacer(Modifier.height(20.dp))

            Text(
                text  = stringResource(R.string.autofill_no_matches_title),
                style = TextStyle(
                    fontFamily    = InterFamily,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 22.sp,
                    letterSpacing = 1.sp
                ),
                color     = lockOnBg,
                textAlign = TextAlign.Center
            )
            Text(
                text      = stringResource(R.string.autofill_no_matches),
                style     = MaterialTheme.typography.bodyMedium,
                color     = lockOnVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(28.dp))

            TextButton(
                onClick  = onClose,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    text       = stringResource(R.string.action_understood),
                    style      = MaterialTheme.typography.titleMedium,
                    color      = lockPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AutofillUnlockContent(
    password:         String,
    error:            String?,
    isLoading:        Boolean,
    bioAvailable:     Boolean,
    onPasswordChange: (String) -> Unit,
    onUnlock:         () -> Unit,
    onBiometric:      () -> Unit,
    onCancel:         () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(lockBg)
    ) {
        val logoSize = (maxHeight * 0.20f).coerceIn(90.dp, 180.dp)

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLogoWithVignette(size = logoSize.value.toInt(), bgColor = lockBg)

            Spacer(Modifier.height(20.dp))

            Text(
                text  = stringResource(R.string.autofill_unlock_title),
                style = TextStyle(
                    fontFamily    = InterFamily,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 30.sp,
                    letterSpacing = 1.sp
                ),
                color = lockOnBg
            )
            Text(
                text      = stringResource(R.string.autofill_unlock_subtitle),
                style     = MaterialTheme.typography.bodyMedium,
                color     = lockOnVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(28.dp))

            var visible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value                = password,
                onValueChange        = onPasswordChange,
                label                = { Text(stringResource(R.string.auth_master_password)) },
                singleLine           = true,
                modifier             = Modifier.fillMaxWidth(),
                visualTransformation = if (visible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            imageVector        = if (visible) Icons.Default.VisibilityOff
                                                 else Icons.Default.Visibility,
                            contentDescription = null,
                            tint               = lockOnVariant
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onUnlock() }),
                shape  = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor           = lockOnBg,
                    unfocusedTextColor         = lockOnBg,
                    focusedLabelColor          = lockPrimary,
                    unfocusedLabelColor        = lockOnVariant,
                    focusedBorderColor         = lockPrimary,
                    unfocusedBorderColor       = lockOnVariant,
                    cursorColor                = lockPrimary,
                    focusedTrailingIconColor   = lockOnVariant,
                    unfocusedTrailingIconColor = lockOnVariant
                )
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = error,
                    color    = WarningAmber,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick  = onUnlock,
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = lockPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text       = stringResource(R.string.lock_action_unlock),
                        style      = MaterialTheme.typography.titleMedium,
                        color      = lockPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (bioAvailable) {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color    = lockOnVariant.copy(alpha = 0.25f)
                    )
                    Text(
                        text  = stringResource(R.string.label_or),
                        style = MaterialTheme.typography.bodySmall,
                        color = lockOnVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color    = lockOnVariant.copy(alpha = 0.25f)
                    )
                }
                Spacer(Modifier.height(24.dp))

                TextButton(
                    onClick  = onBiometric,
                    enabled  = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint               = lockPrimary,
                        modifier           = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = stringResource(R.string.lock_action_biometric),
                        style      = MaterialTheme.typography.titleMedium,
                        color      = lockPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onCancel) {
                Text(
                    text  = stringResource(R.string.action_cancel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = lockOnVariant
                )
            }
        }
    }
}
