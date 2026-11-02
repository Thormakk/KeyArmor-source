package com.thormakk.keyarmor.presentation.screens.family

import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Groups
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun FamilyMemberConsentScreen(
    adminEmail:  String,
    inviteToken: String,
    onDone:      () -> Unit,
    viewModel:   FamilyMemberConsentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeclineConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(adminEmail, inviteToken) {
        viewModel.init(adminEmail, inviteToken)
    }

    LaunchedEffect(uiState) {
        if (uiState is FamilyMemberConsentUiState.Done || uiState is FamilyMemberConsentUiState.Error) {
            onDone()
        }
    }

    when (val state = uiState) {
        FamilyMemberConsentUiState.Loading,
        FamilyMemberConsentUiState.Error,
        FamilyMemberConsentUiState.Done -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        FamilyMemberConsentUiState.Cancelled -> {
            Box(Modifier.fillMaxSize().padding(horizontal = 7.dp, vertical = 32.dp), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Cancel,
                        contentDescription = null,
                        tint               = warningColor(),
                        modifier           = Modifier.size(64.dp)
                    )
                    Text(
                        text      = stringResource(R.string.family_invite_cancelled_title),
                        style     = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center
                    )
                    Text(
                        text      = stringResource(R.string.family_invite_cancelled_body),
                        style     = MaterialTheme.typography.bodyLarge,
                        textAlign  = TextAlign.Center,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth(), colors = appButtonColors()) {
                        Text(stringResource(R.string.action_understood))
                    }
                }
            }
        }
        is FamilyMemberConsentUiState.Loaded -> {
            FamilyMemberConsentContent(
                state            = state,
                onAccept         = { viewModel.accept() },
                onDeclineRequest = { showDeclineConfirm = true }
            )
        }
    }

    if (showDeclineConfirm) {
        AlertDialog(
            onDismissRequest = { showDeclineConfirm = false },
            title   = { Text(stringResource(R.string.family_consent_decline_confirm_title)) },
            text    = { Text(stringResource(R.string.family_consent_decline_confirm_body), style = MaterialTheme.typography.bodyMedium.justified()) },
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
                ) { Text(stringResource(R.string.family_consent_decline)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineConfirm = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun FamilyMemberConsentContent(
    state:            FamilyMemberConsentUiState.Loaded,
    onAccept:         () -> Unit,
    onDeclineRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Outlined.Groups,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text       = stringResource(R.string.family_consent_title),
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = state.memberEmail,
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text      = stringResource(R.string.family_consent_body, state.adminEmail),
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
                text     = stringResource(R.string.family_consent_disclaimer),
                style    = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
                color    = MaterialTheme.colorScheme.onSecondaryContainer
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
                Text(stringResource(R.string.family_consent_accept))
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick  = onDeclineRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(R.string.family_consent_decline))
            }
        }
    }
}
