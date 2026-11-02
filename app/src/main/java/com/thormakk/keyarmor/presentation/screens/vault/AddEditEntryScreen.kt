@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.vault

import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.util.ServiceIconProvider
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoObjetivo
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoContenido
import com.thormakk.keyarmor.presentation.components.sigueAlTeclado
import com.thormakk.keyarmor.presentation.components.rememberSigueAlTeclado
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.formatDateLocalized
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.TagDeleteButton
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.auth.PasswordStrengthBar
import com.thormakk.keyarmor.presentation.screens.generator.PasswordGeneratorBottomSheet
import com.thormakk.keyarmor.presentation.util.BiometricGate
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.presentation.util.highlightText
import com.thormakk.keyarmor.presentation.util.openUrl
import com.thormakk.keyarmor.presentation.util.rememberBiometricGate
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

private val MergedCardRadius = 14.dp

private val MergedCardStartInset = 16.dp
private val MergedCardEndInset   = 16.dp

@Composable
private fun titleFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)

@Composable
private fun transparentFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)

@Composable
fun AddEditEntryScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: (String) -> Unit = {},
    onPlansClick: () -> Unit = {},
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: AddEditEntryViewModel = hiltViewModel()
) {
    val uiState                    by viewModel.uiState.collectAsStateWithLifecycle()
    val availableTags              by viewModel.availableTags.collectAsStateWithLifecycle()
    val isFreeUser                 by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val linkedTwoFactor            by viewModel.linkedTwoFactor.collectAsStateWithLifecycle()
    val linkedPasskey              by viewModel.linkedPasskey.collectAsStateWithLifecycle()
    val availableTwoFactorAccounts by viewModel.availableTwoFactorAccounts.collectAsStateWithLifecycle()
    val availablePasskeys          by viewModel.availablePasskeys.collectAsStateWithLifecycle()

    val focusManager   = LocalFocusManager.current
    val clipCtx      = LocalContext.current
    val clipClear      = LocalClipboardClearManager.current
    val autoLockManager = LocalAutoLockManager.current
    val context        = LocalContext.current
    var showPassword      by remember { mutableStateOf(false) }
    var showOptionsSheet  by remember { mutableStateOf(false) }
    var showGeneratorSheet by remember { mutableStateOf(false) }
    var showDeleteDialog  by remember { mutableStateOf(false) }
    var premiumFeature    by remember { mutableStateOf<String?>(null) }

    val passwordGate: BiometricGate = rememberBiometricGate(
        section              = BiometricPrefs.SECTION_PASSWORDS,
        prefs                = viewModel.biometricPrefs,
        verifyMasterPassword = viewModel::verifyMasterPassword
    )
    val passwordSessionExpiry by passwordGate.sessionExpiresAt

    LaunchedEffect(passwordSessionExpiry) {
        if (passwordSessionExpiry > 0L) {
            val remaining = passwordSessionExpiry - System.currentTimeMillis()
            if (remaining > 0) delay(remaining)
            showPassword = false
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val breachCleanMsg = stringResource(R.string.breach_check_clean)
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            if (uiState.breachSaveState is BreachSaveState.Clean) {
                snackbarHostState.showSnackbar(breachCleanMsg)
            }
            if (uiState.isEditing) viewModel.returnToViewMode()
            else onSaved()
        }
    }
    LaunchedEffect(uiState.success) {
        if (uiState.success) onSaved()
    }


    val breachState = uiState.breachSaveState
    if (breachState is BreachSaveState.PwnedPendingConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBreachDialog() },
            containerColor   = MaterialTheme.colorScheme.background,
            icon = {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    tint               = warningColor(),
                    modifier           = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    stringResource(R.string.breach_dialog_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.breach_dialog_message, breachState.count),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveAnyway() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.breach_dialog_save_anyway)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissBreachDialog() }) {
                    Text(
                        stringResource(R.string.breach_dialog_change),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    val sigueTeclado = rememberSigueAlTeclado()
    val scrollForm = rememberScrollState()
    var notaEnfocada by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            text = when {
                                uiState.isEditing  -> stringResource(R.string.vault_entry_edit)
                                else               -> stringResource(R.string.vault_entry_new)
                            },
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        IconButton(onClick = { focusManager.clearFocus(); viewModel.save() }) {
                            Icon(
                                imageVector        = Icons.Default.Save,
                                contentDescription = stringResource(R.string.action_save),
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (showOptionsSheet) {
            EntryOptionsSheet(
                initialCategory = uiState.category,
                initialTags     = uiState.tags,
                availableTags   = availableTags,
                onSave          = { cat, tags ->
                    viewModel.saveMetaOnly(cat, tags)
                    showOptionsSheet = false
                },
                onDismiss     = { showOptionsSheet = false },
                onAddTag      = { viewModel.addTag(it) },
                onRenameTag   = { id, name -> viewModel.renameTag(id, name) },
                onDeleteTag   = { viewModel.deleteTag(it) },
                tagConflictKey = viewModel::tagConflictKey,
                canCreateTags = !isFreeUser,
                onUpgradeClick = onPlansClick
            )
        }

        if (showGeneratorSheet) {
            PasswordGeneratorBottomSheet(
                onDismiss     = { showGeneratorSheet = false },
                onUsePassword = { generated ->
                    viewModel.onPasswordChange(generated)
                    showGeneratorSheet = false
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor   = MaterialTheme.colorScheme.background,
                title = {
                    Text(stringResource(R.string.dialog_delete_entry_title), fontWeight = FontWeight.Bold,
                         color = MaterialTheme.colorScheme.onBackground)
                },
                text = {
                    Text(
                        stringResource(R.string.dialog_delete_entry_message),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDeleted(uiState.id)
                        },
                        shape  = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor   = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action_move_to_trash))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }

        if (uiState.duplicateNameError) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDuplicateNameError() },
                containerColor   = MaterialTheme.colorScheme.background,
                title = { Text(stringResource(R.string.clone_duplicate_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                text  = { Text(stringResource(R.string.clone_duplicate_message), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified()) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissDuplicateNameError() }) {
                        Text(stringResource(R.string.action_accept))
                    }
                }
            )
        }

        premiumFeature?.let { featureName ->
            AlertDialog(
                onDismissRequest = { premiumFeature = null },
                containerColor   = MaterialTheme.colorScheme.background,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Lock,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = featureName,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                text = {
                    Text(
                        text  = stringResource(R.string.premium_locked_dialog_message),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { premiumFeature = null; onPlansClick() },
                        shape   = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text  = stringResource(R.string.premium_gate_cta)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { premiumFeature = null }) {
                        Text(
                            text  = stringResource(R.string.action_understood),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }

        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .sigueAlTeclado(scrollForm, sigueTeclado, margen = 8.dp, habilitado = { notaEnfocada })
                .verticalScroll(scrollForm)
                .sigueAlTecladoContenido(sigueTeclado)
                .padding(horizontal = 7.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            val mergedCardColor    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val mergedGapColor     = MaterialTheme.colorScheme.background
            val passwordNotBlank   = uiState.password.isNotEmpty()
            Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onTitleChange(it) },
                placeholder = {
                    Text(
                        text  = stringResource(R.string.hint_entry_title),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                isError = uiState.titleError || uiState.cloneNameError,
                supportingText = when {
                    uiState.cloneNameError -> { { Text(stringResource(R.string.clone_name_error), color = MaterialTheme.colorScheme.error) } }
                    uiState.titleError     -> { { Text(stringResource(R.string.error_title_required), color = MaterialTheme.colorScheme.error) } }
                    else                   -> null
                },
                leadingIcon = {
                    val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState      = rememberSharedContentState("entry_avatar_${uiState.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    } else Modifier
                    ServiceAwareAvatar(
                        title    = uiState.title,
                        url      = ServiceIconProvider.bestUrl(uiState.urls),
                        size     = 32.dp,
                        modifier = avatarModifier
                    )
                },
                singleLine = true,
                readOnly = false,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Uri,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { viewModel.confirmUrlBeingAdded(); focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape  = entryItemShape(isFirst = true, isLast = false, radius = MergedCardRadius),
                colors = titleFieldColors()
            )
            EntryGroupDivider(
                startInset = 0.dp,
                endInset   = 0.dp,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            OutlinedTextField(
                    value         = uiState.username,
                    onValueChange = { autoLockManager?.recordInteraction(); viewModel.onUsernameChange(it) },
                    placeholder   = { Text(stringResource(R.string.field_username), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    singleLine    = true,
                    readOnly      = false,
                    trailingIcon  = {
                        IconButton(
                            onClick  = { ClipboardUtil.copySensitive(clipCtx, uiState.username); clipClear.scheduleClear() },
                            enabled  = uiState.username.isNotEmpty()
                        ) {
                            Icon(
                                imageVector        = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.action_copy),
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier      = Modifier.fillMaxWidth().height(58.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        autoCorrectEnabled = false,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape  = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                    colors = fieldColors()
                )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            OutlinedTextField(
                    value         = uiState.email,
                    onValueChange = { autoLockManager?.recordInteraction(); viewModel.onEmailChange(it) },
                    placeholder   = { Text(stringResource(R.string.field_email), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    isError       = uiState.emailError,
                    supportingText = if (uiState.emailError) {
                        { Text(stringResource(R.string.error_email_invalid), color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine    = true,
                    readOnly      = false,
                    trailingIcon  = {
                        IconButton(
                            onClick  = { ClipboardUtil.copySensitive(clipCtx, uiState.email); clipClear.scheduleClear() },
                            enabled  = uiState.email.isNotEmpty()
                        ) {
                            Icon(
                                imageVector        = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.action_copy),
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        autoCorrectEnabled = false,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape  = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                    colors = fieldColors()
                )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            Surface(
                shape    = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    OutlinedTextField(
                        value                = uiState.password,
                        onValueChange        = { autoLockManager?.recordInteraction(); viewModel.onPasswordChange(it) },
                        placeholder          = { Text(stringResource(R.string.field_password), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        singleLine           = true,
                        readOnly             = false,
                        visualTransformation = if (showPassword) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showGeneratorSheet = true }) {
                                    Icon(
                                        imageVector        = Icons.Default.Casino,
                                        contentDescription = stringResource(R.string.generator_title),
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    if (showPassword) showPassword = false
                                    else passwordGate.launch { showPassword = true }
                                }) {
                                    Icon(
                                        imageVector        = if (showPassword) Icons.Default.VisibilityOff
                                                             else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint               = if (viewModel.biometricPrefs.isSectionEnabled(BiometricPrefs.SECTION_PASSWORDS))
                                                                 MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick  = { passwordGate.launch { ClipboardUtil.copySensitive(context, uiState.password); clipClear.scheduleClear() } },
                                    enabled  = uiState.password.isNotEmpty(),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.action_copy),
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(62.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Next,
                            autoCorrectEnabled  = false
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape  = RoundedCornerShape(MergedCardRadius),
                        colors = transparentFieldColors()
                    )
                    if (passwordNotBlank) {
                        PasswordStrengthBar(
                            strength  = viewModel.passwordStrength(uiState.password),
                            modifier  = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                            barHeight = 3.dp
                        )
                    }
                }
            }
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            Surface(
                shape          = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                color          = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier       = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier          = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Language,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text  = stringResource(R.string.field_url),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    uiState.urls.forEachIndexed { index, url ->
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text     = url,
                                style    = MaterialTheme.typography.bodyMedium,
                                color    = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            if (index == 0 && uiState.urls.size > 1) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text     = stringResource(R.string.url_label_primary),
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                            }
                            IconButton(
                                onClick  = { viewModel.removeUrl(index) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.RemoveCircleOutline,
                                    contentDescription = stringResource(R.string.action_delete),
                                    tint               = dangerColor().copy(alpha = 0.7f),
                                    modifier           = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (index < uiState.urls.lastIndex) {
                            HorizontalDivider(
                                modifier  = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val toastMsg = stringResource(R.string.action_open_url_copied)
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.urlBeingAdded.isEmpty()) {
                                Text(
                                    text  = "https://",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            BasicTextField(
                                value         = uiState.urlBeingAdded,
                                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onUrlBeingAddedChange(it) },
                                singleLine    = true,
                                textStyle     = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction    = ImeAction.Done,
                                    autoCorrectEnabled  = false
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { viewModel.confirmUrlBeingAdded() }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        val canOpen = uiState.urlBeingAdded.isNotBlank() && !uiState.urlBeingAddedError
                        IconButton(
                            onClick = {
                                if (uiState.password.isNotEmpty()) {
                                    passwordGate.launch {
                                        ClipboardUtil.copySensitive(context, uiState.password)
                                        clipClear.scheduleClear()
                                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                context.openUrl(uiState.urlBeingAdded)
                            },
                            enabled  = canOpen,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = stringResource(R.string.action_open_url),
                                tint               = if (canOpen) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick  = { viewModel.confirmUrlBeingAdded() },
                            enabled  = uiState.urlBeingAdded.isNotBlank(),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.AddCircleOutline,
                                contentDescription = stringResource(R.string.action_add),
                                tint               = if (uiState.urlBeingAdded.isNotBlank())
                                                         MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (uiState.urlBeingAddedError) {
                        Text(
                            text     = stringResource(R.string.error_url_invalid),
                            color    = MaterialTheme.colorScheme.error,
                            style    = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            val expiryDate = uiState.expiryDate
            val locale = LocalConfiguration.current.locales[0]
            val expiryDateFormatted = expiryDate?.let {
                formatDateLocalized(it, locale, utc = true)
            }
            var showExpiryDatePicker by remember { mutableStateOf(false) }
            val openExpiryPicker = { showExpiryDatePicker = true }

            if (showExpiryDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = expiryDate
                )
                DatePickerDialog(
                    onDismissRequest = { showExpiryDatePicker = false },
                    confirmButton    = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { viewModel.onExpiryDateChange(it) }
                            showExpiryDatePicker = false
                        }) { Text(stringResource(R.string.action_accept)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExpiryDatePicker = false }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            val trailingEndPad = if (expiryDate != null) 92.dp else 44.dp
            Box(modifier = Modifier.fillMaxWidth().height(58.dp)) {
                OutlinedTextField(
                    value         = expiryDateFormatted ?: "",
                    onValueChange = {},
                    readOnly      = true,
                    placeholder   = {
                        Text(
                            text  = stringResource(R.string.entry_expiry_date_none),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    trailingIcon  = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (expiryDate != null) {
                                IconButton(onClick = { viewModel.onExpiryDateChange(null) }) {
                                    Icon(
                                        imageVector        = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.entry_expiry_clear),
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick  = { openExpiryPicker() },
                                enabled  = true
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    isError  = expiryDate != null && expiryDate < System.currentTimeMillis(),
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape    = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                    colors   = fieldColors()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(end = trailingEndPad)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                openExpiryPicker()
                            }
                        }
                )
            }
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            Box(modifier = Modifier.sigueAlTecladoObjetivo(sigueTeclado).onFocusChanged { notaEnfocada = it.hasFocus }) {
                    OutlinedTextField(
                        value         = uiState.notes,
                        onValueChange = { autoLockManager?.recordInteraction(); viewModel.onNotesChange(it) },
                        placeholder   = { Text(stringResource(R.string.field_notes), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        readOnly      = false,
                        modifier      = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines      = 5,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction      = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        shape  = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius),
                        colors = fieldColors()
                    )
                    Text(
                        text  = "${uiState.notes.length}/300",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 8.dp)
                    )
            }
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            var show2FAPicker by remember { mutableStateOf(false) }
            LinkingSection(
                unlinkedTitle = stringResource(R.string.link_2fa_action),
                icon          = Icons.Default.Key,
                linkedName    = linkedTwoFactor?.name?.ifBlank { linkedTwoFactor?.issuer } ?: linkedTwoFactor?.issuer,
                onUnlink      = { viewModel.unlinkTwoFactor() },
                onPickClick   = { if (availableTwoFactorAccounts.isNotEmpty()) show2FAPicker = true },
                shape         = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius)
            )
            if (show2FAPicker) {
                TwoFactorPickerSheet(
                    accounts  = availableTwoFactorAccounts,
                    onPick    = { id -> viewModel.linkTwoFactor(id); show2FAPicker = false },
                    onDismiss = { show2FAPicker = false }
                )
            }
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            var showPasskeyPicker by remember { mutableStateOf(false) }
            LinkingSection(
                unlinkedTitle = stringResource(R.string.link_passkey_action),
                icon          = Icons.Default.Fingerprint,
                linkedName    = linkedPasskey?.rpId?.ifBlank { linkedPasskey?.userName } ?: linkedPasskey?.userName,
                onUnlink      = { viewModel.unlinkPasskey() },
                onPickClick   = { if (availablePasskeys.isNotEmpty()) showPasskeyPicker = true },
                shape         = entryItemShape(isFirst = false, isLast = true, radius = MergedCardRadius)
            )
            if (showPasskeyPicker) {
                PasskeyPickerSheet(
                    passkeys  = availablePasskeys,
                    onPick    = { id -> viewModel.linkPasskey(id); showPasskeyPicker = false },
                    onDismiss = { showPasskeyPicker = false }
                )
            }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun EntryOptionsSheet(
    initialCategory: EntryCategory,
    initialTags:     List<String>,
    availableTags:   List<Tag>,
    onSave:          (EntryCategory, List<String>) -> Unit,
    onDismiss:       () -> Unit,
    onAddTag:        (String) -> Unit,
    onRenameTag:     (String, String) -> Unit,
    onDeleteTag:     (String) -> Unit,
    tagConflictKey:  (String) -> String,
    canCreateTags:   Boolean = true,
    onUpgradeClick:  () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val autoLockManager = LocalAutoLockManager.current

    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedTags     by remember { mutableStateOf(initialTags.toSet()) }
    var searchQuery      by remember { mutableStateOf("") }
    var newTagText       by remember { mutableStateOf("") }
    var newTagError      by remember { mutableStateOf<String?>(null) }
    var renameTarget     by remember { mutableStateOf<Tag?>(null) }
    var renameText       by remember { mutableStateOf("") }
    var renameError      by remember { mutableStateOf<String?>(null) }
    var showLockedDialog by remember { mutableStateOf(false) }

    val errLength    = stringResource(R.string.tag_error_length)
    val errDuplicate = stringResource(R.string.tag_error_duplicate)
    val errCategory  = stringResource(R.string.tag_error_category_conflict)

    val filteredTags = remember(availableTags, searchQuery, selectedTags) {
        if (searchQuery.isBlank()) availableTags
        else availableTags.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true) || it.name in selectedTags
        }
    }
    val searchHasNoMatches = searchQuery.isNotBlank() &&
        availableTags.none { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    val tryCreateTag: (String) -> Unit = { raw ->
        val trimmed    = raw.trim()
        val normalized = trimmed.normalizeTag()
        when {
            trimmed.isBlank() -> {}
            trimmed.length !in TAG_MIN_LENGTH..TAG_MAX_LENGTH ->
                newTagError = errLength
            availableTags.any { tagConflictKey(it.name) == tagConflictKey(normalized) } ->
                newTagError = errDuplicate
            EntryCategory.entries.any { it.label.equals(normalized, ignoreCase = true) } ->
                newTagError = errCategory
            else -> {
                onAddTag(normalized)
                selectedTags = selectedTags + normalized
                newTagText   = ""
                newTagError  = null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text       = stringResource(R.string.options_sheet_category),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EntryCategory.entries
                    .filter { it != EntryCategory.SIN_CATEGORIZAR }
                    .forEach { cat ->
                        val selected = selectedCategory == cat
                        Surface(
                            onClick      = {
                                selectedCategory = if (selectedCategory == cat) EntryCategory.SIN_CATEGORIZAR else cat
                            },
                            shape        = RoundedCornerShape(8.dp),
                            color        = if (selected) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                                           else MaterialTheme.colorScheme.onBackground
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier         = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text       = stringResource(cat.labelRes),
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    textAlign  = TextAlign.Center,
                                    maxLines   = 1
                                )
                            }
                        }
                    }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text       = stringResource(R.string.options_sheet_tags),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { autoLockManager?.recordInteraction(); searchQuery = it },
                placeholder   = { Text(stringResource(R.string.vault_search_tags)) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                },
                modifier   = Modifier.fillMaxWidth(),
                shape      = CircleShape,
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(modifier = Modifier.animateContentSize()) {
            if (filteredTags.isEmpty()) {
                TagSearchEmptyState(
                    query       = searchQuery,
                    canCreate   = canCreateTags,
                    onCreateTag = tryCreateTag
                )
            } else {
                filteredTags.forEach { tag ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTags = selectedTags.toMutableSet().apply {
                                    if (contains(tag.name)) remove(tag.name) else add(tag.name)
                                }
                            }
                            .padding(start = 0.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = selectedTags.contains(tag.name),
                            onCheckedChange = {
                                selectedTags = selectedTags.toMutableSet().apply {
                                    if (it) add(tag.name) else remove(tag.name)
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text     = highlightText(tag.name, searchQuery, MaterialTheme.colorScheme.primary),
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick  = { renameTarget = tag; renameText = tag.name },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Edit,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                        TagDeleteButton(
                            tag      = tag,
                            onDelete = { selectedTags = selectedTags - tag.name; onDeleteTag(tag.id) }
                        )
                    }
                }
                if (searchHasNoMatches) {
                    TagSearchEmptyState(
                        query       = searchQuery,
                        canCreate   = canCreateTags,
                        onCreateTag = tryCreateTag
                    )
                }
            }

            renameTarget?.let { target ->
                AlertDialog(
                    modifier = Modifier.cuadroConTeclado(),
                    properties = propiedadesConTeclado(),
                    onDismissRequest = { renameTarget = null; renameError = null },
                    title = { Text(stringResource(R.string.tag_rename_title)) },
                    text  = {
                        OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                            value          = renameText,
                            onValueChange  = { autoLockManager?.recordInteraction(); renameText = it; renameError = null },
                            singleLine     = true,
                            label          = { Text(stringResource(R.string.tag_name_hint)) },
                            shape          = RoundedCornerShape(8.dp),
                            modifier       = Modifier.fillMaxWidth(),
                            isError        = renameError != null,
                            supportingText = { renameError?.let { Text(it) } }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val trimmed    = renameText.trim()
                                val normalized = trimmed.normalizeTag()
                                when {
                                    trimmed.isBlank() -> {}
                                    trimmed.length !in TAG_MIN_LENGTH..TAG_MAX_LENGTH ->
                                        renameError = errLength
                                    availableTags.any { it.id != target.id && tagConflictKey(it.name) == tagConflictKey(normalized) } ->
                                        renameError = errDuplicate
                                    EntryCategory.entries.any { it.label.equals(normalized, ignoreCase = true) } ->
                                        renameError = errCategory
                                    else -> {
                                        if (selectedTags.contains(target.name)) {
                                            selectedTags = (selectedTags - target.name) + normalized
                                        }
                                        onRenameTag(target.id, normalized)
                                        renameTarget = null
                                        renameError  = null
                                    }
                                }
                            },
                            enabled = renameText.trim().isNotBlank(),
                            shape   = RoundedCornerShape(8.dp),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                        ) { Text(stringResource(R.string.action_save), fontWeight = FontWeight.SemiBold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { renameTarget = null; renameError = null }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    }
                )
            }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTags.isNotEmpty()) {
                Button(
                    onClick  = { onSave(selectedCategory, selectedTags.toList()) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text       = stringResource(R.string.action_apply),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!searchHasNoMatches) {
            if (canCreateTags) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (newTagText.isEmpty()) {
                            Text(
                                text  = stringResource(R.string.vault_tag_name_placeholder),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        BasicTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                            value         = newTagText,
                            onValueChange = { autoLockManager?.recordInteraction(); newTagText = it; newTagError = null },
                            singleLine    = true,
                            textStyle     = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    )
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .fillMaxHeight()
                            .clickable { tryCreateTag(newTagText) }
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier              = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Add,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(16.dp)
                            )
                            Text(
                                text       = stringResource(R.string.options_sheet_create_tag),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.primary,
                                textAlign  = TextAlign.Center
                            )
                        }
                    }
                }
                if (newTagError != null) {
                    Text(
                        text     = newTagError!!,
                        color    = MaterialTheme.colorScheme.error,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                        .clickable { showLockedDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text  = stringResource(R.string.vault_tag_name_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .fillMaxHeight()
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier              = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Lock,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier           = Modifier.size(16.dp)
                            )
                            Text(
                                text       = stringResource(R.string.options_sheet_create_tag),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                textAlign  = TextAlign.Center
                            )
                        }
                    }
                }
            }
            }

            if (showLockedDialog) {
                AlertDialog(
                    onDismissRequest = { showLockedDialog = false },
                    containerColor   = MaterialTheme.colorScheme.background,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null,
                                tint     = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.plans_feature_tags),
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    text = {
                        Text(stringResource(R.string.premium_locked_dialog_message),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    confirmButton = {
                        Button(
                            onClick = { showLockedDialog = false; onUpgradeClick() },
                            shape   = RoundedCornerShape(8.dp),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                        ) { Text(stringResource(R.string.premium_gate_cta)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLockedDialog = false }) {
                            Text(stringResource(R.string.action_understood),
                                color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)

@Composable
private fun LinkingSection(
    unlinkedTitle: String,
    icon:          androidx.compose.ui.graphics.vector.ImageVector,
    linkedName:    String?,
    onUnlink:      () -> Unit,
    onPickClick:   () -> Unit,
    shape:         RoundedCornerShape
) {
    val isLinked = linkedName != null
    Surface(
        shape    = shape,
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable(onClick = if (isLinked) onUnlink else onPickClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = if (isLinked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = if (isLinked) linkedName else unlinkedTitle,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isLinked) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (isLinked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isLinked) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TwoFactorPickerSheet(
    accounts:  List<TwoFactorAccount>,
    onPick:    (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text       = stringResource(R.string.link_2fa_sheet_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            accounts.forEach { account ->
                val label = account.name.ifBlank { account.issuer }.ifBlank { account.accountLabel }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(account.id) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Default.Key,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (account.issuer.isNotBlank() && account.issuer != label) {
                            Text(
                                text  = account.issuer,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun PasskeyPickerSheet(
    passkeys:  List<PasskeyCredential>,
    onPick:    (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text       = stringResource(R.string.link_passkey_sheet_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            passkeys.forEach { passkey ->
                val label = passkey.rpId.ifBlank { passkey.userName }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(passkey.credentialId) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (passkey.userName.isNotBlank() && passkey.userName != label) {
                            Text(
                                text  = passkey.userName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}
