@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.thormakk.keyarmor.presentation.screens.settings

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
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
import com.thormakk.keyarmor.presentation.screens.auth.PasswordStrengthBar
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun ChangeMasterPasswordScreen(
    onBack:    () -> Unit,
    viewModel: ChangeMasterPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentVisible by remember { mutableStateOf(false) }
    var newVisible     by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var currentFieldWasFocused by remember { mutableStateOf(false) }

    val showCurrentHint = uiState.errorCurrent == null &&
        currentPassword.isNotEmpty() && currentPassword.length < ChangeMasterPasswordViewModel.MIN_LENGTH
    val showNewHint = uiState.errorNew == null &&
        newPassword.length < ChangeMasterPasswordViewModel.MIN_LENGTH
    val liveMismatch = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() &&
        confirmPassword != newPassword

    val listo = currentPassword.isNotEmpty() &&
        newPassword.length >= ChangeMasterPasswordViewModel.MIN_LENGTH &&
        confirmPassword == newPassword

    val bringButtonIntoView = remember { BringIntoViewRequester() }
    val bringCurrentFieldIntoView = remember { BringIntoViewRequester() }
    val imeVisible     = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val showStrengthBar = newPassword.isNotEmpty()
    LaunchedEffect(
        imeVisible, showStrengthBar, showCurrentHint, showNewHint, liveMismatch,
        uiState.errorCurrent, uiState.errorNew, uiState.errorConfirm
    ) {
        if (imeVisible) {
            delay(300L)
            bringButtonIntoView.bringIntoView()
        }
    }

    if (uiState.isSuccess) {
        SuccessDialog(onDismiss = onBack)
    }

    Scaffold(
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
                            text       = stringResource(R.string.change_master_password_title),
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

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text  = stringResource(R.string.change_master_password_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PasswordField(
                    value         = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        viewModel.clearErrors()
                    },
                    label         = stringResource(R.string.change_master_password_current),
                    visible       = currentVisible,
                    onVisibilityToggle = { currentVisible = !currentVisible },
                    errorMessage  = uiState.errorCurrent?.let { resolveError(it) },
                    supportingText = if (showCurrentHint) stringResource(
                        R.string.change_master_password_min_length,
                        ChangeMasterPasswordViewModel.MIN_LENGTH
                    ) else null,
                    isVerifying = uiState.isVerifyingCurrent,
                    modifier = Modifier
                        .bringIntoViewRequester(bringCurrentFieldIntoView)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                currentFieldWasFocused = true
                                coroutineScope.launch {
                                    delay(300L)
                                    bringCurrentFieldIntoView.bringIntoView()
                                }
                            } else if (currentFieldWasFocused) {
                                currentFieldWasFocused = false
                                if (currentPassword.isNotBlank()) viewModel.verifyCurrentPassword(currentPassword)
                            }
                        }
                )

                PasswordField(
                    value         = newPassword,
                    onValueChange = {
                        newPassword = it
                        viewModel.clearErrors()
                    },
                    label         = stringResource(R.string.change_master_password_new),
                    visible       = newVisible,
                    onVisibilityToggle = { newVisible = !newVisible },
                    errorMessage  = uiState.errorNew?.let { resolveError(it) },
                    supportingText = if (showNewHint) stringResource(
                        R.string.change_master_password_min_length,
                        ChangeMasterPasswordViewModel.MIN_LENGTH
                    ) else null
                )

                if (showStrengthBar) {
                    PasswordStrengthBar(
                        strength = PasswordStrengthCalculator.calculate(newPassword)
                    )
                }

                PasswordField(
                    value         = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        viewModel.clearErrors()
                    },
                    label         = stringResource(R.string.change_master_password_confirm),
                    visible       = confirmVisible,
                    onVisibilityToggle = { confirmVisible = !confirmVisible },
                    errorMessage  = uiState.errorConfirm?.let { resolveError(it) }
                        ?: if (liveMismatch) stringResource(R.string.change_master_password_error_mismatch) else null
                )

                Button(
                    onClick = {
                        viewModel.onSubmit(currentPassword, newPassword, confirmPassword)
                    },
                    enabled  = !uiState.isLoading && listo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .bringIntoViewRequester(bringButtonIntoView),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text       = stringResource(R.string.change_master_password_cta),
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            text  = stringResource(R.string.change_master_password_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                text       = stringResource(R.string.change_master_password_success_title),
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text  = stringResource(R.string.change_master_password_success_message),
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape   = RoundedCornerShape(8.dp), colors = appButtonColors()
            ) {
                Text(stringResource(R.string.action_understood))
            }
        }
    )
}

@Composable
private fun PasswordField(
    value:              String,
    onValueChange:      (String) -> Unit,
    label:              String,
    visible:            Boolean,
    onVisibilityToggle: () -> Unit,
    modifier:           Modifier = Modifier,
    errorMessage:       String?  = null,
    supportingText:     String?  = null,
    isVerifying:        Boolean  = false
) {
    val autoLockManager = LocalAutoLockManager.current
    OutlinedTextField(
        value         = value,
        onValueChange = { autoLockManager?.recordInteraction(); onValueChange(it) },
        label         = { Text(label) },
        singleLine    = true,
        isError       = errorMessage != null,
        visualTransformation = if (visible) VisualTransformation.None
                               else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
        trailingIcon = {
            if (isVerifying) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector        = if (visible) Icons.Default.VisibilityOff
                                            else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            }
        },
        supportingText = {
            when {
                errorMessage    != null -> Text(errorMessage,    color = warningColor())
                supportingText  != null -> Text(supportingText,  color = warningColor())
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            errorBorderColor         = warningColor(),
            errorSupportingTextColor = warningColor(),
            errorLabelColor          = warningColor(),
            errorTrailingIconColor   = warningColor(),
            errorCursorColor         = warningColor()
        ),
        shape    = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun resolveError(code: String): String = when (code) {
    ChangeMasterPasswordViewModel.ERROR_EMPTY          -> stringResource(R.string.error_field_required)
    ChangeMasterPasswordViewModel.ERROR_MIN_LENGTH     -> stringResource(
        R.string.change_master_password_error_min_length,
        ChangeMasterPasswordViewModel.MIN_LENGTH
    )
    ChangeMasterPasswordViewModel.ERROR_SAME           -> stringResource(R.string.change_master_password_error_same)
    ChangeMasterPasswordViewModel.ERROR_MISMATCH       -> stringResource(R.string.change_master_password_error_mismatch)
    ChangeMasterPasswordViewModel.ERROR_WRONG_PASSWORD -> stringResource(R.string.change_master_password_error_wrong)
    ChangeMasterPasswordViewModel.ERROR_SNAPSHOT_FAILED -> stringResource(R.string.change_master_password_error_snapshot)
    else                                               -> stringResource(R.string.change_master_password_error_generic)
}
