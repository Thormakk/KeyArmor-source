@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.util
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import android.content.Context
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.thormakk.keyarmor.security.LoginThrottle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.security.GateCryptoManager
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.util.BiometricSessionCache.invalidateAll
import javax.crypto.Cipher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object BiometricSessionCache {

    private val lastAuthMs = java.util.concurrent.ConcurrentHashMap<String, Long>()

    fun record(section: String) {
        lastAuthMs[section] = android.os.SystemClock.elapsedRealtime()
    }

    fun remainingMs(section: String, timeoutSecs: Int): Long {
        val last = lastAuthMs[section] ?: return 0L
        val left = timeoutSecs * 1000L - (android.os.SystemClock.elapsedRealtime() - last)
        return if (left > 0L) left else 0L
    }

    fun isValid(section: String, timeoutSecs: Int): Boolean {
        val last = lastAuthMs[section] ?: return false
        return android.os.SystemClock.elapsedRealtime() - last < timeoutSecs * 1000L
    }

    fun invalidateAll() { lastAuthMs.clear() }
}

class BiometricGate(
    private val prefs:            BiometricPrefs,
    private val section:          String,
    private val pendingAction:    MutableState<(() -> Unit)?>,
    private val showFallback:     MutableState<Boolean>,
    private val prompt:           BiometricPrompt,
    private val promptInfo:       BiometricPrompt.PromptInfo,
    private val context:          Context,
    private val sessionExpiresState: MutableLongState
) {
    val sessionExpiresAt: State<Long> get() = sessionExpiresState

    fun launch(onGranted: () -> Unit) {
        if (!prefs.isSectionEnabled(section)) { onGranted(); return }
        if (prefs.getMode() == BiometricPrefs.MODE_SESSION &&
            BiometricSessionCache.isValid(section, prefs.getSessionTimeoutSeconds())) {
            sessionExpiresState.longValue = System.currentTimeMillis() +
                BiometricSessionCache.remainingMs(section, prefs.getSessionTimeoutSeconds())
            onGranted(); return
        }
        pendingAction.value = onGranted
        val canAuth = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            prompt.authenticate(promptInfo)
        } else {
            showFallback.value = true
        }
    }
}

@Composable
fun rememberBiometricGate(
    section:              String,
    prefs:                BiometricPrefs,
    verifyMasterPassword: suspend (String) -> Boolean
): BiometricGate {
    val context  = LocalContext.current
    val activity = context as FragmentActivity
    val scope    = rememberCoroutineScope()

    val pendingAction    = remember { mutableStateOf<(() -> Unit)?>(null) }
    val showFallback     = remember { mutableStateOf(false) }
    val sessionExpiresAt = remember { mutableLongStateOf(0L) }

    fun onAuthGranted() {
        if (prefs.getMode() == BiometricPrefs.MODE_SESSION) {
            BiometricSessionCache.record(section)
            sessionExpiresAt.longValue =
                System.currentTimeMillis() + prefs.getSessionTimeoutSeconds() * 1000L
        }
        pendingAction.value?.invoke()
        pendingAction.value = null
    }

    val callback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val tipo = result.authenticationType
                val esBiometriaReal = tipo == BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC
                val toleraDesconocido =
                    android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R &&
                        tipo == BiometricPrompt.AUTHENTICATION_RESULT_TYPE_UNKNOWN
                if (!esBiometriaReal && !toleraDesconocido) {
                    pendingAction.value = null
                    return
                }
                onAuthGranted()
            }
            override fun onAuthenticationFailed() {   }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                    showFallback.value = true
                } else {
                    pendingAction.value = null
                }
            }
        }
    }

    val prompt = remember(activity) {
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
    }

    val gateTitle    = stringResource(R.string.biometric_gate_title)
    val usePassword  = stringResource(R.string.autofill_use_password)

    val promptInfo = remember(gateTitle, usePassword) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(gateTitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText(usePassword)
            .build()
    }

    if (showFallback.value) {
        BiometricGateFallbackDialog(
            scope                = scope,
            verifyMasterPassword = verifyMasterPassword,
            onGranted = {
                showFallback.value = false
                onAuthGranted()
            },
            onDismiss = {
                showFallback.value = false
                pendingAction.value = null
            }
        )
    }

    return remember(section) {
        BiometricGate(
            prefs            = prefs,
            section          = section,
            pendingAction    = pendingAction,
            showFallback     = showFallback,
            prompt           = prompt,
            promptInfo       = promptInfo,
            context          = context,
            sessionExpiresState = sessionExpiresAt
        )
    }
}

class MandatoryBiometricGate(
    private val pendingAction:      MutableState<(() -> Unit)?>,
    private val showFallback:       MutableState<Boolean>,
    private val prompt:             BiometricPrompt,
    private val promptInfo:         BiometricPrompt.PromptInfo,
    private val context:            Context,
    private val gateCryptoManager:  GateCryptoManager,
    private val pendingProofCipher: MutableState<Cipher?>
) {
    fun cancel() {
        pendingAction.value = null
        showFallback.value  = false
    }

    fun launch(onGranted: () -> Unit) {
        pendingAction.value = onGranted
        val canAuth = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            showFallback.value = true
            return
        }
        val cipher = try {
            gateCryptoManager.getProofCipher()
        } catch (e: KeyPermanentlyInvalidatedException) {
            gateCryptoManager.deleteKey()
            null
        } catch (e: Exception) {
            null
        }
        if (cipher == null) {
            showFallback.value = true
            return
        }
        pendingProofCipher.value = cipher
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}

@Composable
fun rememberMandatoryBiometricGate(
    verifyMasterPassword: suspend (String) -> Boolean
): MandatoryBiometricGate {
    val context  = LocalContext.current
    val activity = context as FragmentActivity
    val scope    = rememberCoroutineScope()

    val pendingAction = remember { mutableStateOf<(() -> Unit)?>(null) }
    val showFallback  = remember { mutableStateOf(false) }
    val pendingProofCipher = remember { mutableStateOf<Cipher?>(null) }
    val gateCryptoManager = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext, BiometricGateEntryPoint::class.java
        ).gateCryptoManager()
    }

    fun onAuthGranted() {
        pendingAction.value?.invoke()
        pendingAction.value = null
    }

    val callback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val tipo = result.authenticationType
                val esBiometriaReal = tipo == BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC
                val toleraDesconocido =
                    android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R &&
                        tipo == BiometricPrompt.AUTHENTICATION_RESULT_TYPE_UNKNOWN
                if (!esBiometriaReal && !toleraDesconocido) {
                    pendingAction.value = null
                    return
                }
                val cipher = result.cryptoObject?.cipher
                val expected = pendingProofCipher.value
                pendingProofCipher.value = null
                if (cipher == null || cipher !== expected || !gateCryptoManager.verifyProof(cipher)) {
                    pendingAction.value = null
                    return
                }
                onAuthGranted()
            }
            override fun onAuthenticationFailed() {   }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                    showFallback.value = true
                } else {
                    pendingAction.value = null
                }
            }
        }
    }

    val prompt = remember(activity) {
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
    }

    val gateTitle    = stringResource(R.string.biometric_gate_title)
    val usePassword  = stringResource(R.string.autofill_use_password)

    val promptInfo = remember(gateTitle, usePassword) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(gateTitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText(usePassword)
            .build()
    }

    if (showFallback.value) {
        BiometricGateFallbackDialog(
            scope                = scope,
            verifyMasterPassword = verifyMasterPassword,
            onGranted = {
                showFallback.value = false
                onAuthGranted()
            },
            onDismiss = {
                showFallback.value = false
                pendingAction.value = null
            }
        )
    }

    return remember {
        MandatoryBiometricGate(
            pendingAction      = pendingAction,
            showFallback       = showFallback,
            prompt             = prompt,
            promptInfo         = promptInfo,
            context            = context,
            gateCryptoManager  = gateCryptoManager,
            pendingProofCipher = pendingProofCipher
        )
    }
}

@Composable
private fun BiometricGateFallbackDialog(
    scope:                CoroutineScope,
    verifyMasterPassword: suspend (String) -> Boolean,
    onGranted:            () -> Unit,
    onDismiss:            () -> Unit
) {
    var input   by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    var error   by remember { mutableStateOf<String?>(null) }
    val autoLockManager = LocalAutoLockManager.current
    val context = LocalContext.current
    val throttle = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext, BiometricGateEntryPoint::class.java
        ).loginThrottle()
    }
    val errorPassword = stringResource(R.string.autofill_error_wrong_password)

    AlertDialog(
        conScroll = false,
        modifier = Modifier.cuadroConTeclado(),
        properties = propiedadesConTeclado(),
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text       = stringResource(R.string.biometric_gate_fallback_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text  = stringResource(R.string.biometric_gate_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                    value         = input,
                    onValueChange = { autoLockManager?.recordInteraction(); input = it; error = null },
                    label         = { Text(stringResource(R.string.field_password)) },
                    singleLine    = true,
                    isError       = error != null,
                    supportingText = error?.let { mensaje ->
                        { Text(text = mensaje, color = dangerColor()) }
                    },
                    visualTransformation = if (visible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                imageVector        = if (visible) Icons.Default.VisibilityOff
                                                     else Icons.Default.Visibility,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    },
                    shape  = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor       = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor     = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor        = MaterialTheme.colorScheme.primary,
                        errorBorderColor         = dangerColor(),
                        errorSupportingTextColor = dangerColor()
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    val espera = throttle.remainingWaitMs()
                    if (espera > 0L) {
                        error = mensajeDeEspera(context, espera)
                    } else if (verifyMasterPassword(input)) {
                        input = ""
                        onGranted()
                    } else {
                        error = errorPassword
                    }
                }
            }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor   = MaterialTheme.colorScheme.primary
            ), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)) {
                Text(
                    text       = stringResource(R.string.lock_action_unlock),
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { input = ""; onDismiss() }) {
                Text(
                    text  = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface BiometricGateEntryPoint {
    fun loginThrottle(): LoginThrottle
    fun gateCryptoManager(): GateCryptoManager
}

private fun mensajeDeEspera(context: android.content.Context, waitMs: Long): String {
    val minutos = ((waitMs + 59_999L) / 60_000L).toInt().coerceAtLeast(1)
    return if (minutos >= 60) {
        context.getString(R.string.error_login_throttled_hours, (minutos + 59) / 60)
    } else {
        context.getString(R.string.error_login_throttled_minutes, minutos)
    }
}
