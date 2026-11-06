package com.thormakk.keyarmor.presentation.screens.inheritance

import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HowToReg
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun HeirConsentScreen(
    onDone:    () -> Unit,
    viewModel: HeirConsentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeclineConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is HeirConsentUiState.Done || uiState is HeirConsentUiState.Error) {
            onDone()
        }
    }

    when (val state = uiState) {
        HeirConsentUiState.Loading,
        HeirConsentUiState.Error,
        HeirConsentUiState.Done -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HeirConsentUiState.Loaded -> {
            HeirConsentContent(
                state            = state,
                onAccept         = { viewModel.accept() },
                onDeclineRequest = { showDeclineConfirm = true }
            )
            state.ownerKeyChange?.let { cambio ->
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text(stringResource(R.string.heir_consent_owner_key_changed_title)) },
                    text  = {
                        Column {
                            Text(
                                text  = stringResource(
                                    R.string.heir_consent_owner_key_changed_body,
                                    state.ownerEmail
                                ),
                                style = MaterialTheme.typography.bodyMedium.justified()
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text       = stringResource(
                                    R.string.heir_consent_owner_key_changed_previous,
                                    cambio.huellaAnterior
                                ),
                                style      = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text          = cambio.huellaNueva,
                                style         = MaterialTheme.typography.titleMedium,
                                fontFamily    = FontFamily.Monospace,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.onOwnerKeyChangeConfirmed() },
                            shape   = RoundedCornerShape(8.dp),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.heir_consent_owner_key_changed_confirm), fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.onOwnerKeyChangeRejected() }) {
                            Text(
                                text  = stringResource(R.string.heir_consent_owner_key_changed_reject),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        }
        is HeirConsentUiState.ShowRecoveryCode -> {
            HeirRecoveryCodeScreen(
                code   = state.code,
                onDone = { viewModel.onRecoveryCodeSaved() },
            )
        }
        is HeirConsentUiState.WaitingFingerprintConfirmation -> {
            HeirFingerprintWaitingScreen(
                fingerprint = viewModel.getMyKeyFingerprint(),
                onCancel    = { viewModel.cancelWhileWaiting() },
            )
        }
    }

    if (showDeclineConfirm) {
        AlertDialog(
            onDismissRequest = { showDeclineConfirm = false },
            title   = { Text(stringResource(R.string.heir_consent_decline_confirm_title)) },
            text    = { Text(stringResource(R.string.heir_consent_decline_confirm_body), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeclineConfirm = false
                        viewModel.decline()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.heir_consent_decline)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineConfirm = false }) { Text(stringResource(android.R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun HeirConsentContent(
    state:            HeirConsentUiState.Loaded,
    onAccept:         () -> Unit,
    onDeclineRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 7.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Outlined.HowToReg,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text       = stringResource(R.string.inheritance_title),
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = stringResource(R.string.heir_consent_subtitle),
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text      = stringResource(R.string.heir_consent_body, state.ownerEmail),
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Card(
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text     = stringResource(R.string.heir_consent_disclaimer),
                style    = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
                color    = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        state.errorRes?.let { res ->
            Spacer(Modifier.height(24.dp))
            Text(
                text      = stringResource(res),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(36.dp))

        if (state.isSubmitting) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick  = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp), colors = appButtonColors()
            ) {
                Text(stringResource(R.string.heir_consent_accept))
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick  = onDeclineRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(R.string.heir_consent_decline))
            }
        }
    }
}

@Composable
private fun HeirFingerprintWaitingScreen(
    fingerprint: String?,
    onCancel:    () -> Unit,
) {
    var showCancelConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 7.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Outlined.HowToReg,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text       = stringResource(R.string.heir_fingerprint_wait_title),
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        fingerprint?.let { fp ->
            Text(
                text          = stringResource(R.string.heir_recovery_code_fingerprint_label, fp),
                style         = MaterialTheme.typography.titleMedium,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                textAlign     = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.heir_recovery_code_fingerprint_help),
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(28.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                text  = stringResource(R.string.heir_fingerprint_wait_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(36.dp))

        OutlinedButton(
            onClick  = { showCancelConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(stringResource(R.string.heir_fingerprint_wait_cancel))
        }
    }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text(stringResource(R.string.heir_fingerprint_wait_cancel_confirm_title)) },
            text  = { Text(stringResource(R.string.heir_fingerprint_wait_cancel_confirm_body), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirm = false
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.heir_fingerprint_wait_cancel)) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) { Text(stringResource(android.R.string.cancel)) }
            }
        )
    }
}
