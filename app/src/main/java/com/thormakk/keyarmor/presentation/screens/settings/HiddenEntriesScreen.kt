@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.thormakk.keyarmor.presentation.screens.settings

import com.thormakk.keyarmor.presentation.components.sigueAlTecladoObjetivo
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoContenido
import com.thormakk.keyarmor.presentation.components.sigueAlTeclado
import com.thormakk.keyarmor.presentation.components.rememberSigueAlTeclado
import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.rememberMandatoryBiometricGate
import kotlinx.coroutines.delay
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun HiddenEntriesScreen(
    onBack:       () -> Unit,
    onPlansClick: () -> Unit = {},
    viewModel:    HiddenEntriesViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val isFreeUser by viewModel.isFreeUser.collectAsStateWithLifecycle()

    var newPhrase     by remember { mutableStateOf("") }
    var confirmPhrase by remember { mutableStateOf("") }

    var newVisible     by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var explanationExpanded by remember { mutableStateOf(!uiState.isEnabled) }

    val focusManager = LocalFocusManager.current

    val newTooShort      = newPhrase.isNotEmpty() && newPhrase.length < HiddenEntriesViewModel.MIN_LENGTH
    val confirmMismatch  = confirmPhrase.isNotEmpty() && confirmPhrase != newPhrase

    val frasesListas     = newPhrase.length >= HiddenEntriesViewModel.MIN_LENGTH &&
        confirmPhrase == newPhrase

    val disableGate = rememberMandatoryBiometricGate(
        verifyMasterPassword = viewModel::verifyMasterPassword
    )
    val saveGate = disableGate

    val bringButtonIntoView = remember { BringIntoViewRequester() }
    val sigueTeclado = rememberSigueAlTeclado()
    val scrollForm = rememberScrollState()
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(imeVisible, newTooShort, confirmMismatch) {
        if (imeVisible) {
            delay(300L)
            bringButtonIntoView.bringIntoView()
        }
    }

    if (uiState.isSuccess) {
        SuccessDialog(onDismiss = {
            newPhrase     = ""
            confirmPhrase = ""
            focusManager.clearFocus()
            explanationExpanded = false
            viewModel.onSuccessConsumed()
        })
    }

    if (uiState.showDisableDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDisable,
            containerColor   = MaterialTheme.colorScheme.background,
            icon = {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    tint               = warningColor()
                )
            },
            title = { Text(stringResource(R.string.hidden_entries_disable_title)) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.hidden_entries_disable_message), style = MaterialTheme.typography.bodyMedium.justified())
                    if (isFreeUser) {
                        Text(stringResource(R.string.hidden_entries_disable_free_note), style = MaterialTheme.typography.bodyMedium.justified())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDismissDisable()
                        disableGate.launch {
                            viewModel.onConfirmDisable()
                            newPhrase = ""; confirmPhrase = ""
                        }
                    },
                    shape  = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.hidden_entries_disable_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDisable) {
                    Text(
                        stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                TopAppBar(
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                    },
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            text       = stringResource(R.string.hidden_entries_title),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint               = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .sigueAlTeclado(scrollForm, sigueTeclado)
                .verticalScroll(scrollForm)
                .sigueAlTecladoContenido(sigueTeclado)
                .padding(horizontal = 7.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = { explanationExpanded = !explanationExpanded }
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Search,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text       = stringResource(R.string.hidden_entries_what_is_title),
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onBackground,
                            modifier   = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector        = if (explanationExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = explanationExpanded,
                        enter   = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit    = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                    ) {
                        Text(
                            text      = stringResource(R.string.hidden_entries_what_is_body),
                            style     = MaterialTheme.typography.bodySmall.justified(),
                            color     = warningColor(),
                            modifier  = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        )
                    }
                }
            }

            if (isFreeUser) {
                PremiumGateBanner(onPlansClick = onPlansClick)
            }

            if (uiState.isEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = stringResource(R.string.hidden_entries_status_label),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text  = stringResource(R.string.hidden_entries_status_active),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF43A047)
                        )
                    }
                    Switch(
                        checked         = true,
                        onCheckedChange = { viewModel.onRequestDisable() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = Color(0xFF43A047)
                        )
                    )
                }
            }

            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text  = if (uiState.isEnabled)
                            stringResource(R.string.hidden_entries_change_section_title)
                        else
                            stringResource(R.string.hidden_entries_setup_section_title),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )

                    PhraseField(
                        value         = newPhrase,
                        enabled       = !isFreeUser,
                        onValueChange = { newPhrase = it; viewModel.clearErrors() },
                        label         = stringResource(
                            if (uiState.isEnabled) R.string.hidden_entries_new
                            else R.string.hidden_entries_new_first_time
                        ),
                        visible       = newVisible,
                        onVisibilityToggle = { newVisible = !newVisible },
                        errorMessage   = uiState.errorNew
                            ?.takeIf { it != HiddenEntriesViewModel.ERROR_MIN_LENGTH }
                            ?.let { resolveError(it) },
                        warningMessage = if (newTooShort)
                            stringResource(R.string.hidden_entries_min_length, HiddenEntriesViewModel.MIN_LENGTH)
                        else null,
                        supportingText = stringResource(R.string.hidden_entries_min_length, HiddenEntriesViewModel.MIN_LENGTH),
                        modifier       = Modifier.coachMarkAnchor("hidden_entries", "passphrase")
                    )

                    PhraseField(
                        value         = confirmPhrase,
                        enabled       = !isFreeUser,
                        onValueChange = { confirmPhrase = it; viewModel.clearErrors() },
                        label         = stringResource(R.string.hidden_entries_confirm),
                        visible       = confirmVisible,
                        onVisibilityToggle = { confirmVisible = !confirmVisible },
                        errorMessage  = if (confirmMismatch)
                            stringResource(R.string.hidden_entries_error_mismatch)
                        else
                            uiState.errorConfirm?.let { resolveError(it) }
                    )

                    Box(
                        modifier         = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            onClick = { saveGate.launch { viewModel.onSubmit(newPhrase, confirmPhrase) } },
                            enabled        = !isFreeUser && frasesListas,
                            modifier       = Modifier.sigueAlTecladoObjetivo(sigueTeclado).bringIntoViewRequester(bringButtonIntoView),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            shape  = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text       = if (uiState.isEnabled)
                                    stringResource(R.string.hidden_entries_cta_change)
                                else
                                    stringResource(R.string.hidden_entries_cta_activate),
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        icon = {
            Icon(
                imageVector        = Icons.Default.CheckCircle,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text       = stringResource(R.string.hidden_entries_success_title),
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text  = stringResource(R.string.hidden_entries_success_message),
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp), colors = appButtonColors()) {
                Text(stringResource(R.string.action_understood))
            }
        }
    )
}

@Composable
private fun PhraseField(
    value:              String,
    onValueChange:      (String) -> Unit,
    label:              String,
    visible:            Boolean,
    onVisibilityToggle: () -> Unit,
    modifier:           Modifier = Modifier,
    enabled:            Boolean = true,
    errorMessage:       String?  = null,
    warningMessage:     String?  = null,
    supportingText:     String?  = null
) {
    val autoLockManager = LocalAutoLockManager.current
    val warning = warningColor()
    val tone: Color? = when {
        errorMessage   != null -> warning
        warningMessage != null -> warning
        else                    -> null
    }

    OutlinedTextField(
        value         = value,
        enabled       = enabled,
        onValueChange = { autoLockManager?.recordInteraction(); onValueChange(it) },
        label         = { Text(label) },
        singleLine    = true,
        visualTransformation = if (visible) VisualTransformation.None
                               else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle, enabled = enabled) {
                Icon(
                    imageVector        = if (visible) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                    contentDescription = null,
                    tint               = tone ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        supportingText = {
            Text(
                text  = errorMessage ?: warningMessage ?: supportingText ?: "",
                color = if (errorMessage != null || warningMessage != null) warning
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = tone ?: MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = tone ?: MaterialTheme.colorScheme.outline,
            focusedLabelColor    = tone ?: MaterialTheme.colorScheme.primary,
            unfocusedLabelColor  = tone ?: MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape    = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun PremiumGateBanner(onPlansClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlansClick)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Lock,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier           = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(R.string.premium_gate_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text  = stringResource(R.string.premium_gate_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Text(
                text       = stringResource(R.string.premium_gate_cta),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun resolveError(code: String): String = when (code) {
    HiddenEntriesViewModel.ERROR_EMPTY        -> stringResource(R.string.error_field_required)
    HiddenEntriesViewModel.ERROR_MIN_LENGTH   -> stringResource(
        R.string.hidden_entries_error_min_length, HiddenEntriesViewModel.MIN_LENGTH
    )
    HiddenEntriesViewModel.ERROR_MISMATCH     -> stringResource(R.string.hidden_entries_error_mismatch)
    else                                       -> stringResource(R.string.change_master_password_error_generic)
}
