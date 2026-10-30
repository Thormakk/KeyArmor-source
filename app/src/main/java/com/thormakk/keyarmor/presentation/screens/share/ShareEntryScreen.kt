@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.share

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.TextButton
import com.thormakk.keyarmor.presentation.screens.auth.PasswordStrengthBar
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import kotlinx.coroutines.flow.collectLatest
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

private fun shareExpiryLabelRes(expiry: ShareExpiry): Int = when (expiry) {
    ShareExpiry.ONE_HOUR      -> R.string.share_expiry_1h
    ShareExpiry.ONE_DAY       -> R.string.share_expiry_1d
    ShareExpiry.SEVEN_DAYS    -> R.string.share_expiry_7d
    ShareExpiry.FOURTEEN_DAYS -> R.string.share_expiry_14d
    ShareExpiry.THIRTY_DAYS   -> R.string.share_expiry_30d
}

@Composable
fun ShareEntryScreen(
    onBack: () -> Unit,
    viewModel: ShareEntryViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val clipCtx = LocalContext.current
    val clipClear = LocalClipboardClearManager.current
    val autoLockManager = LocalAutoLockManager.current
    val snackHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ShareEntryEvent.Error ->
                    snackHost.showSnackbar(event.message)
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
                title = { TextoDeUnaLinea(stringResource(R.string.share_title),
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
                text  = stringResource(R.string.share_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.entryName.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Lock,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Text(
                            text       = uiState.entryName,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (uiState.hasAttachments) {
                val attachmentWarningColor = warningColor()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = attachmentWarningColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.AttachFile,
                            contentDescription = null,
                            tint               = attachmentWarningColor,
                            modifier           = Modifier.size(20.dp)
                        )
                        Text(
                            text  = stringResource(R.string.share_attachment_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = attachmentWarningColor
                        )
                    }
                }
            }

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value         = uiState.password,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onPasswordChange(it) },
                label         = { Text(stringResource(R.string.share_password_hint)) },
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
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            if (uiState.password.isNotEmpty()) {
                PasswordStrengthBar(strength = PasswordStrengthCalculator.calculate(uiState.password))
            }
            }
            TextButton(onClick = viewModel::regeneratePassword) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.share_password_regenerate))
            }
            if (uiState.password.isNotEmpty() && uiState.password.length < 8) {
                Text(
                    text  = stringResource(R.string.share_password_too_short),
                    style = MaterialTheme.typography.bodySmall,
                    color = warningColor()
                )
            }

            OutlinedTextField(
                value         = uiState.passwordConfirm,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onPasswordConfirmChange(it) },
                label         = { Text(stringResource(R.string.share_password_confirm_hint)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                visualTransformation = if (uiState.isConfirmVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon  = {
                    IconButton(onClick = viewModel::toggleConfirmVisible) {
                        Icon(
                            if (uiState.isConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                isError  = uiState.passwordConfirm.isNotEmpty() && !uiState.passwordsMatch,
                supportingText = if (uiState.passwordConfirm.isNotEmpty() && !uiState.passwordsMatch) {
                    { Text(stringResource(R.string.share_password_mismatch), color = warningColor()) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors   = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Text(
                text     = stringResource(R.string.share_expiry_label),
                style    = MaterialTheme.typography.labelLarge,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier              = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShareExpiry.entries.forEach { expiry ->
                    FilterChip(
                        selected = uiState.selectedExpiry == expiry,
                        onClick  = { viewModel.onExpiryChange(expiry) },
                        label    = { Text(stringResource(shareExpiryLabelRes(expiry))) }
                    )
                }
            }

            Button(
                onClick  = viewModel::onGenerate,
                enabled  = uiState.canGenerate,
                modifier = Modifier.fillMaxWidth(), colors = appButtonColors()
            ) {
                if (uiState.isGenerating) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color     = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.share_generating))
                } else {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.share_generate_btn))
                }
            }

            if (uiState.isGenerated) {
                HorizontalDivider()

                uiState.qrBitmap?.let { bmp ->
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text  = stringResource(R.string.share_qr_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Image(
                            bitmap             = bmp.asImageBitmap(),
                            contentDescription = stringResource(R.string.share_qr_label),
                            modifier           = Modifier
                                .size(220.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text  = stringResource(R.string.share_text_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text    = uiState.encryptedText,
                            style   = MaterialTheme.typography.bodySmall,
                            color   = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp),
                            maxLines = 6,
                            softWrap = true
                        )
                    }
                    OutlinedButton(
                        onClick  = {
                            ClipboardUtil.copySensitive(clipCtx, uiState.encryptedText)
                            clipClear.scheduleClear()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.share_copy_btn))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
