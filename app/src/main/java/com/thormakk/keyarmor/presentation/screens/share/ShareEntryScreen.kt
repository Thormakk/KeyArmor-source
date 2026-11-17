@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.thormakk.keyarmor.presentation.screens.share

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.Box
import com.thormakk.keyarmor.presentation.components.rememberSigueAlTeclado
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoContenido
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedVisibility
import com.thormakk.keyarmor.presentation.screens.vehicle.VehicleAwareAvatar
import com.thormakk.keyarmor.presentation.screens.crypto.CryptoAwareAvatar
import com.thormakk.keyarmor.presentation.screens.bank.BankAwareAvatar
import com.thormakk.keyarmor.presentation.screens.creditcard.CreditCardAwareAvatar
import com.thormakk.keyarmor.presentation.screens.vault.ServiceAwareAvatar
import com.thormakk.keyarmor.presentation.components.notesAccentColor
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import com.thormakk.keyarmor.presentation.components.sharedAvatar
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
import androidx.compose.material.icons.filled.Close
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
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: ShareEntryViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val clipCtx = LocalContext.current
    val clipClear = LocalClipboardClearManager.current
    val autoLockManager = LocalAutoLockManager.current
    val snackHost = remember { SnackbarHostState() }
    val scrollForm = rememberScrollState()
    val sigueTeclado = rememberSigueAlTeclado()
    val confirmBringIntoView = remember { BringIntoViewRequester() }
    val tarjetaRef = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val densidad = LocalDensity.current
    val passFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val margenTarjetaPx = with(densidad) { 8.dp.toPx() }
    var passFocused by remember { mutableStateOf(false) }
    var confirmFocused by remember { mutableStateOf(false) }
    var areaPx by remember { mutableStateOf(0) }
    val enfocado = passFocused || confirmFocused
    LaunchedEffect(enfocado) {
        if (!enfocado) return@LaunchedEffect
        repeat(2) { withFrameNanos { } }
        val c = sigueTeclado.contenido
        val k = tarjetaRef[0]
        if (c != null && k != null && c.isAttached && k.isAttached) {
            val destino = (c.localPositionOf(k, Offset.Zero).y - margenTarjetaPx).toInt().coerceAtLeast(0)
            scrollForm.animateScrollTo(destino)
        }
    }
    val showMismatch = uiState.passwordConfirm.isNotEmpty() && !uiState.passwordsMatch
    val avisoGenerado = stringResource(R.string.share_generated_copied)
    var codigoAvisado by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(uiState.isGenerated, uiState.encryptedText) {
        if (uiState.isGenerated && uiState.encryptedText.isNotEmpty() && uiState.encryptedText != codigoAvisado) {
            codigoAvisado = uiState.encryptedText
            ClipboardUtil.copySensitive(clipCtx, uiState.encryptedText, mostrarAviso = false)
            clipClear.scheduleClear()
            snackHost.showSnackbar(avisoGenerado)
        }
    }
    LaunchedEffect(showMismatch) { if (showMismatch) confirmBringIntoView.bringIntoView() }

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
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .onSizeChanged { areaPx = it.height }
                .verticalScroll(scrollForm)
                .sigueAlTecladoContenido(sigueTeclado)
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
                    modifier = Modifier.onGloballyPositioned { tarjetaRef[0] = it },
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
                        val avatarMod = Modifier.sharedAvatar(
                            when (uiState.entryType) {
                                "PASSWORD"      -> "entry_avatar_${uiState.entryId}"
                                "TWO_FACTOR"    -> "twofa_avatar_${uiState.entryId}"
                                "CREDIT_CARD"   -> "card_avatar_${uiState.entryId}"
                                "BANK_ACCOUNT"  -> "bank_avatar_${uiState.entryId}"
                                "CRYPTO_WALLET" -> "crypto_avatar_${uiState.entryId}"
                                "SECURE_NOTE"   -> "note_icon_${uiState.entryId}"
                                "CUSTOM_ENTRY"  -> "custom_icon_${uiState.entryId}"
                                "VEHICLE"       -> "vehicle_avatar_${uiState.entryId}"
                                else            -> "share_entry_${uiState.entryId}"
                            },
                            sharedTransitionScope, animatedVisibilityScope
                        )
                        when (uiState.entryType) {
                            "PASSWORD", "TWO_FACTOR" ->
                                ServiceAwareAvatar(title = uiState.avatarLabel, url = uiState.avatarUrl, modifier = avatarMod)
                            "CREDIT_CARD"   -> CreditCardAwareAvatar(brand = uiState.avatarLabel, modifier = avatarMod)
                            "BANK_ACCOUNT"  -> BankAwareAvatar(bankName = uiState.avatarLabel, modifier = avatarMod)
                            "CRYPTO_WALLET" -> CryptoAwareAvatar(asset = uiState.avatarLabel, modifier = avatarMod)
                            "VEHICLE"       -> VehicleAwareAvatar(make = uiState.avatarLabel, modifier = avatarMod)
                            "SECURE_NOTE"   -> Icon(
                                imageVector        = Icons.AutoMirrored.Outlined.StickyNote2,
                                contentDescription = null,
                                tint               = notesAccentColor(),
                                modifier           = avatarMod.size(24.dp)
                            )
                            "CUSTOM_ENTRY"  -> Icon(
                                imageVector        = Icons.Outlined.GridView,
                                contentDescription = null,
                                tint               = notesAccentColor(),
                                modifier           = avatarMod.size(24.dp)
                            )
                            else -> Icon(
                                imageVector        = Icons.Default.Lock,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = viewModel::togglePasswordVisible) {
                                Icon(
                                    if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = viewModel::regeneratePassword) {
                                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.share_password_regenerate))
                            }
                            IconButton(onClick = {
                                viewModel.onPasswordChange("")
                                viewModel.onPasswordConfirmChange("")
                                passFocusRequester.requestFocus()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                            }
                        }
                    },
                    modifier      = Modifier.fillMaxWidth().focusRequester(passFocusRequester).onFocusChanged { passFocused = it.isFocused },
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Box(Modifier.fillMaxWidth().height(4.dp)) {
                    androidx.compose.animation.AnimatedVisibility(visible = uiState.password.isNotEmpty()) {
                        PasswordStrengthBar(strength = PasswordStrengthCalculator.calculate(uiState.password))
                    }
                }
                Box(Modifier.fillMaxWidth().height(20.dp)) {
                    androidx.compose.animation.AnimatedVisibility(visible = uiState.password.isNotEmpty() && uiState.password.length < 8) {
                        Text(
                            text  = stringResource(R.string.share_password_too_short),
                            style = MaterialTheme.typography.bodySmall,
                            color = warningColor()
                        )
                    }
                }
                Column(
                    modifier = Modifier.bringIntoViewRequester(confirmBringIntoView),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value         = uiState.passwordConfirm,
                        onValueChange = { autoLockManager?.recordInteraction(); viewModel.onPasswordConfirmChange(it) },
                        label         = { Text(stringResource(R.string.share_password_confirm_hint)) },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        visualTransformation = if (uiState.isConfirmVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon  = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = viewModel::toggleConfirmVisible) {
                                    Icon(
                                        if (uiState.isConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                                IconButton(
                                    enabled = uiState.password.isNotEmpty(),
                                    onClick = {
                                        ClipboardUtil.copySensitive(clipCtx, uiState.password)
                                        clipClear.scheduleClear()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.action_copy))
                                }
                            }
                        },
                        isError  = showMismatch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { confirmFocused = it.isFocused },
                        colors   = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor       = MaterialTheme.colorScheme.outlineVariant,
                            errorBorderColor           = warningColor(),
                            errorLabelColor            = warningColor(),
                            errorTrailingIconColor     = warningColor(),
                            errorCursorColor           = warningColor()
                        )
                    )
                    Box(Modifier.fillMaxWidth().height(20.dp)) {
                        androidx.compose.animation.AnimatedVisibility(visible = showMismatch) {
                            Text(
                                text     = stringResource(R.string.share_password_mismatch),
                                style    = MaterialTheme.typography.bodySmall,
                                color    = warningColor(),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick  = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    viewModel.onGenerate()
                },
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

            AnimatedVisibility(visible = uiState.isGenerated) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
            }

            Spacer(Modifier.height(32.dp))
            Spacer(Modifier.height(if (enfocado) with(densidad) { areaPx.toDp() } else 0.dp))
        }
    }
}
