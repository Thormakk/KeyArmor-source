@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.share

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.share.ShareSerializer.EntryType
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import kotlinx.coroutines.flow.collectLatest
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun ReceiveEntryScreen(
    onBack:       () -> Unit,
    onSaved:      () -> Unit,
    viewModel: ReceiveEntryViewModel = hiltViewModel()
) {
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val autoLockManager = LocalAutoLockManager.current
    val snackHost = remember { SnackbarHostState() }

    val copiedText  = stringResource(R.string.receive_saved)

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.onQrScanned(result.contents)
        } else {
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ReceiveEntryEvent.SavedSuccessfully -> {
                    snackHost.showSnackbar(copiedText)
                    onSaved()
                }
                is ReceiveEntryEvent.Error ->
                    snackHost.showSnackbar(event.msg)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            TopAppBar(
                actions = {
                    CaptureAllowedBadge()
                    ThemeTrialBadge()
                },
                title = { TextoDeUnaLinea(stringResource(R.string.receive_title),
                    style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                    fontWeight = FontWeight.SemiBold,
                    color      = sectionTitleColor()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = topBarColors()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 7.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text  = stringResource(R.string.receive_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick  = {
                    viewModel.prepareForQrScan()
                    val opts = ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt("")
                        setBeepEnabled(false)
                        setOrientationLocked(false)
                    }
                    scanLauncher.launch(opts)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.receive_scan_btn))
            }

            Row(
                modifier            = Modifier.fillMaxWidth(),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(stringResource(R.string.label_or), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                value         = uiState.pastedText,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onPastedTextChange(it) },
                label         = { Text(stringResource(R.string.receive_paste_hint)) },
                minLines      = 4,
                maxLines      = 8,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            OutlinedTextField(
                value         = uiState.password,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onPasswordChange(it) },
                label         = { Text(stringResource(R.string.receive_password_hint)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                visualTransformation = if (uiState.isPasswordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon  = {
                    IconButton(onClick = viewModel::togglePasswordVisible) {
                        Icon(
                            if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors   = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            uiState.errorMessage?.let { msg ->
                Text(
                    text  = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick  = viewModel::onDecrypt,
                enabled  = uiState.canDecrypt,
                modifier = Modifier.fillMaxWidth(), colors = appButtonColors()
            ) {
                if (uiState.isDecrypting) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.receive_decrypt_btn))
                }
            }

            if (uiState.showPreview && uiState.previewType != null) {
                HorizontalDivider()

                Text(
                    text       = stringResource(R.string.receive_preview_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReceivePreviewRow(
                            label = stringResource(R.string.receive_preview_type),
                            value = entryTypeLabel(uiState.previewType!!)
                        )
                        ReceivePreviewRow(
                            label = stringResource(R.string.receive_preview_name),
                            value = uiState.previewName
                        )
                    }
                }

                Button(
                    onClick  = viewModel::onSave,
                    enabled  = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(), colors = appButtonColors()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.receive_save_btn))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReceivePreviewRow(label: String, value: String) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment   = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun entryTypeLabel(type: EntryType): String = stringResource(
    when (type) {
        EntryType.PASSWORD      -> R.string.receive_type_password
        EntryType.TWO_FACTOR    -> R.string.receive_type_twofactor
        EntryType.CREDIT_CARD   -> R.string.receive_type_creditcard
        EntryType.BANK_ACCOUNT  -> R.string.receive_type_bankaccount
        EntryType.CRYPTO_WALLET -> R.string.receive_type_cryptowallet
        EntryType.SECURE_NOTE   -> R.string.receive_type_securenote
        EntryType.CUSTOM_ENTRY -> R.string.receive_type_customentry
        EntryType.VEHICLE      -> R.string.receive_type_vehicle
    }
)
