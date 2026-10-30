package com.thormakk.keyarmor.presentation.screens.onboarding

import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.util.GoogleConnectResult
import com.thormakk.keyarmor.presentation.util.requestGoogleAccountConnect
import kotlinx.coroutines.launch

@Composable
fun ConnectGoogleScreen(
    onContinue: (skipTutorial: Boolean) -> Unit,
    viewModel: ConnectGoogleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val verificationFailedMsg = stringResource(R.string.error_google_verification_failed)

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = if (uiState.connectedEmail != null) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text       = stringResource(R.string.connect_google_title),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.connect_google_subtitle),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            val connectedEmail = uiState.connectedEmail
            if (connectedEmail != null) {
                Text(
                    text       = stringResource(R.string.connect_google_connected_as, connectedEmail),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick  = { onContinue(viewModel.shouldSkipTutorial()) },
                    modifier = Modifier.fillMaxWidth(), colors = appButtonColors()
                ) {
                    Text(stringResource(R.string.action_continue))
                }
            } else {
                Button(
                    enabled  = !uiState.isConnecting,
                    modifier = Modifier.fillMaxWidth(),
                    onClick  = {
                        scope.launch {
                            when (val result = requestGoogleAccountConnect(context, credentialManager)) {
                                is GoogleConnectResult.Success -> viewModel.connectAccount(
                                    email   = result.email,
                                    idToken = result.idToken
                                )
                                is GoogleConnectResult.Cancelled -> Unit
                                is GoogleConnectResult.Error -> viewModel.setError(
                                    result.message ?: verificationFailedMsg
                                )
                            }
                        }
                    }, colors = appButtonColors()
                ) {
                    if (uiState.isConnecting) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.drive_sync_connect_google))
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    enabled  = !uiState.isConnecting,
                    modifier = Modifier.fillMaxWidth(),
                    onClick  = { onContinue(false) }
                ) {
                    Text(stringResource(R.string.connect_google_skip))
                }
            }
        }
    }
}
