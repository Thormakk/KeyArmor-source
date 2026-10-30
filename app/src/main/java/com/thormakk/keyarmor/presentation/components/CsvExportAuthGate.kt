@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.components
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import kotlinx.coroutines.launch

@Composable
fun CsvExportAuthGate(
    onAuthorized:         () -> Unit,
    onDismiss:            () -> Unit,
    verifyMasterPassword: suspend (String) -> Boolean,
    promptTitle:    String = stringResource(R.string.import_export_csv_confirm_title),
    promptSubtitle: String = stringResource(R.string.import_export_csv_confirm_subtitle),
    confirmLabel:   String = stringResource(R.string.import_export_confirm_export),
    fallbackPrompt: String = stringResource(R.string.import_export_master_password_prompt),
) {
    val context  = LocalContext.current
    val activity = context as FragmentActivity
    val scope    = rememberCoroutineScope()
    val autoLockManager = LocalAutoLockManager.current

    val showPasswordFallback = remember { mutableStateOf(false) }

    val callback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onAuthorized()
            }
            override fun onAuthenticationFailed() {   }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_NO_BIOMETRICS,
                    BiometricPrompt.ERROR_HW_NOT_PRESENT,
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> showPasswordFallback.value = true
                    else -> onDismiss()
                }
            }
        }
    }

    val prompt = remember(activity) {
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
    }

    val useMasterPasswordLabel   = stringResource(R.string.import_export_use_master_password)

    val promptInfo = remember(promptTitle, promptSubtitle, useMasterPasswordLabel) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptTitle)
            .setSubtitle(promptSubtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText(useMasterPasswordLabel)
            .build()
    }

    LaunchedEffect(Unit) {
        val canAuth = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            prompt.authenticate(promptInfo)
        } else {
            showPasswordFallback.value = true
        }
    }

    if (showPasswordFallback.value) {
        var input   by remember { mutableStateOf("") }
        var visible by remember { mutableStateOf(false) }
        var error   by remember { mutableStateOf(false) }

        AlertDialog(
            modifier = Modifier.cuadroConTeclado(),
            properties = propiedadesConTeclado(),
            onDismissRequest = { showPasswordFallback.value = false; onDismiss() },
            containerColor   = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector        = Icons.Default.Lock,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text       = stringResource(R.string.import_export_verify_identity_title),
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text  = fallbackPrompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        value         = input,
                        onValueChange = { autoLockManager?.recordInteraction(); input = it; error = false },
                        label         = { Text(stringResource(R.string.field_password)) },
                        singleLine    = true,
                        isError       = error,
                        supportingText = if (error) ({
                            Text(
                                text  = stringResource(R.string.autofill_error_wrong_password),
                                color = warningColor()
                            )
                        }) else null,
                        visualTransformation = if (visible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { visible = !visible }) {
                                Icon(
                                    imageVector = if (visible) Icons.Default.VisibilityOff
                                                  else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        shape  = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            errorBorderColor       = warningColor(),
                            errorLabelColor        = warningColor(),
                            errorTrailingIconColor = warningColor(),
                            errorCursorColor       = warningColor()
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick  = {
                        scope.launch {
                            if (verifyMasterPassword(input)) {
                                showPasswordFallback.value = false
                                input = ""
                                onAuthorized()
                            } else {
                                error = true
                            }
                        }
                    },
                    enabled = input.isNotEmpty(),
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text       = confirmLabel,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { input = ""; showPasswordFallback.value = false; onDismiss() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
