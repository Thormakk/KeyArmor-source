@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.custom

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.LinearProgressIndicator
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.screens.notes.NoteDictationController
import com.thormakk.keyarmor.presentation.screens.notes.rememberNoteDictationController
import com.thormakk.keyarmor.presentation.screens.voicedictation.VoiceDictationViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.domain.model.CustomField
import com.thormakk.keyarmor.domain.model.CustomFieldType
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.validate
import com.thormakk.keyarmor.presentation.components.CountryCodePickerDialog
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.TagDeleteButton
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.components.combinePhone
import com.thormakk.keyarmor.presentation.components.defaultDialCode
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.splitPhone
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.formatHintRes
import com.thormakk.keyarmor.presentation.labelRes
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.highlightText
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.presentation.util.openUrl
import com.thormakk.keyarmor.presentation.util.rememberBiometricGate
import kotlinx.coroutines.delay
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

private val MergedCardRadius     = 14.dp
private val MergedCardStartInset = 16.dp
private val MergedCardEndInset   = 16.dp

@Composable
fun AddEditCustomEntryScreen(
    onBack:      () -> Unit,
    onSaved:     () -> Unit,
    viewModel: AddEditCustomEntryViewModel = hiltViewModel()
) {
    val uiState       = viewModel.uiState.collectAsStateWithLifecycle().value

    val focusManager   = LocalFocusManager.current
    val autoLockManager = LocalAutoLockManager.current

    var showTypePickerDialog by remember { mutableStateOf(false) }

    val customFieldGate = rememberBiometricGate(
        section              = BiometricPrefs.SECTION_CUSTOM,
        prefs                = viewModel.biometricPrefs,
        verifyMasterPassword = viewModel::verifyMasterPassword
    )
    val customSessionExpiry by customFieldGate.sessionExpiresAt

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }

    val context = LocalContext.current
    val msgPermDenied  = stringResource(R.string.voice_dictation_permission_denied)
    val msgListening   = stringResource(R.string.voice_dictation_listening)
    val msgErrLanguage = stringResource(R.string.voice_dictation_error_language)
    val msgErrGeneric  = stringResource(R.string.voice_dictation_error_generic)
    val dictationVm: VoiceDictationViewModel = hiltViewModel()
    val dictationEnabled by dictationVm.enabled.collectAsStateWithLifecycle()
    var dictatingFieldId by remember { mutableStateOf<String?>(null) }
    var dictationBase    by remember { mutableStateOf("") }
    var dictationError   by remember { mutableStateOf<Int?>(null) }
    val dictation = rememberNoteDictationController(
        languageTag = { dictationVm.languageTag.value },
        onText = { txt, _ ->
            dictatingFieldId?.let { id ->
                autoLockManager?.recordInteraction()
                viewModel.updateFieldValue(id, dictationBase + txt)
            }
        },
        onError = { code -> dictationError = code }
    )
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) dictation.start()
        else Toast.makeText(context, msgPermDenied, Toast.LENGTH_LONG).show()
    }
    LaunchedEffect(dictation.isListening) {
        if (dictation.isListening) Toast.makeText(context, msgListening, Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(dictationError) {
        val code = dictationError ?: return@LaunchedEffect
        dictationError = null
        when {
            NoteDictationController.isLanguageError(code) && dictation.canRequestDownload ->
                dictation.requestLanguageDownload()
            NoteDictationController.isLanguageError(code) ->
                Toast.makeText(context, msgErrLanguage, Toast.LENGTH_LONG).show()
            else ->
                Toast.makeText(context, msgErrGeneric, Toast.LENGTH_LONG).show()
        }
    }
    val openVoiceSettings: () -> Unit = {
        val candidates = listOf(
            Intent(Settings.ACTION_INPUT_METHOD_SETTINGS),
            Intent("android.settings.VOICE_INPUT_SETTINGS"),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in candidates) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try { context.startActivity(intent); break } catch (_: Exception) {}
        }
        dictation.clearDownload()
    }
    val startNoteDictation: (String) -> Unit = { fieldId ->
        autoLockManager?.recordInteraction()
        if (dictation.isListening) {
            dictation.stop()
        } else {
            val current = viewModel.uiState.value.fields.firstOrNull { it.id == fieldId }?.value.orEmpty()
            dictationBase = if (current.isEmpty() || current.endsWith(" ") || current.endsWith("\n")) current else "$current "
            dictatingFieldId = fieldId
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) dictation.start()
            else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    if (dictation.isDownloading || dictation.downloadFailed) {
        val failed   = dictation.downloadFailed
        val progress = dictation.downloadProgress
        AlertDialog(
            onDismissRequest = { dictation.clearDownload() },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(stringResource(
                    if (failed) R.string.voice_dictation_download_failed_title
                    else        R.string.voice_dictation_download_title
                ))
            },
            text = {
                Column {
                    Text(
                        stringResource(
                            if (failed) R.string.voice_dictation_download_failed
                            else        R.string.voice_dictation_download_body
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!failed) {
                        Spacer(Modifier.height(16.dp))
                        if (progress != null) {
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("$progress%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                if (failed) {
                    Button(
                        onClick = openVoiceSettings,
                        shape   = RoundedCornerShape(8.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = MaterialTheme.colorScheme.primary
                        ),
                        border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.voice_dictation_open_settings), fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    TextButton(onClick = { dictation.clearDownload() }) {
                        Text(stringResource(R.string.action_accept))
                    }
                }
            },
            dismissButton = if (failed) {
                {
                    TextButton(onClick = { dictation.clearDownload() }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            } else null
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

    if (showTypePickerDialog) {
        FieldTypePickerDialog(
            onSelect  = { type, label -> viewModel.addField(type, label); showTypePickerDialog = false },
            onDismiss = { showTypePickerDialog = false }
        )
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
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            if (uiState.isNew) stringResource(R.string.custom_new) else stringResource(R.string.custom_edit),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onSurface)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 7.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surface
            val mergedGapColor  = MaterialTheme.colorScheme.background
            val hasFields       = uiState.fields.isNotEmpty()

            Column(modifier = Modifier.fillMaxWidth()) {
                EntryTitleField(
                    value         = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    onNext        = { focusManager.moveFocus(FocusDirection.Down) },
                    isError       = uiState.titleError,
                    errorMessage  = stringResource(R.string.custom_title_required),
                    shape         = entryItemShape(isFirst = true, isLast = !hasFields, radius = MergedCardRadius)
                )

                if (hasFields) {
                    EntryGroupDivider(
                        startInset = 0.dp,
                        endInset   = 0.dp,
                        cardColor  = mergedCardColor,
                        gapColor   = mergedGapColor
                    )

                    uiState.fields.forEachIndexed { index, field ->
                        val isFirstField = index == 0
                        val isLastField  = index == uiState.fields.lastIndex
                        CustomFieldItem(
                            field         = field,
                            isFirst       = isFirstField,
                            isLast        = isLastField,
                            onValueChange = { viewModel.updateFieldValue(field.id, it) },
                            onDelete      = { viewModel.removeField(field.id) },
                            onMoveUp      = { viewModel.moveFieldUp(field.id) },
                            onMoveDown    = { viewModel.moveFieldDown(field.id) },
                            triggerGate      = { action -> customFieldGate.launch(action) },
                            sessionExpiresAt = customSessionExpiry,
                            noteDictationEnabled = dictationEnabled && dictation.available,
                            isDictating          = dictation.isListening && dictatingFieldId == field.id,
                            onDictate            = { startNoteDictation(field.id) },
                            shape            = entryItemShape(isFirst = false, isLast = isLastField, radius = MergedCardRadius)
                        )
                        if (!isLastField) {
                            EntryGroupDivider(
                                startInset = MergedCardStartInset,
                                endInset   = MergedCardEndInset,
                                cardColor  = mergedCardColor,
                                gapColor   = mergedGapColor
                            )
                        }
                    }
                }
            }

            if (uiState.cloneNameError) {
                Text(
                    text  = stringResource(R.string.clone_name_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                )
            }

            if (!hasFields) {
                Text(
                    text     = stringResource(R.string.custom_fields_empty),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            OutlinedButton(
                onClick  = { showTypePickerDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(8.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.custom_add_field), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EntryTitleField(
    value:         String,
    onValueChange: (String) -> Unit,
    onNext:        () -> Unit,
    isError:       Boolean = false,
    errorMessage:  String  = "",
    shape:         Shape   = RoundedCornerShape(8.dp)
) {
    val autoLockManager = LocalAutoLockManager.current
    OutlinedTextField(
        value           = value,
        onValueChange   = { autoLockManager?.recordInteraction(); onValueChange(it) },
        placeholder     = { Text(stringResource(R.string.custom_field_name_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        singleLine      = true,
        isError         = isError,
        supportingText  = if (isError) {{ Text(errorMessage, color = MaterialTheme.colorScheme.error) }} else null,
        textStyle       = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier        = Modifier.fillMaxWidth(),
        shape           = shape,
        colors          = titleFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { onNext() })
    )
}

@Composable
private fun CustomFieldItem(
    field:         CustomField,
    isFirst:       Boolean,
    isLast:        Boolean,
    onValueChange: (String) -> Unit,
    onDelete:      () -> Unit,
    onMoveUp:      () -> Unit,
    onMoveDown:    () -> Unit,
    triggerGate:      ((onGranted: () -> Unit) -> Unit) = { it() },
    sessionExpiresAt: Long = 0L,
    noteDictationEnabled: Boolean = false,
    isDictating: Boolean = false,
    onDictate: () -> Unit = {},
    shape:            Shape = RoundedCornerShape(8.dp)
) {
    val clipCtx      = LocalContext.current
    val clipClear   = LocalClipboardClearManager.current
    val autoLockManager = LocalAutoLockManager.current
    val context     = LocalContext.current
    var showValue by remember(field.id) { mutableStateOf(!field.type.isSecret) }

    LaunchedEffect(sessionExpiresAt) {
        if (sessionExpiresAt > 0L && field.type.isSecret) {
            val remaining = sessionExpiresAt - System.currentTimeMillis()
            if (remaining > 0) delay(remaining)
            showValue = false
        }
    }
    val isValid   = field.type.validate(field.value)

    Surface(
        shape    = shape,
        color    = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isFirst) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.field_move_up),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp))
                    }
                }
                if (!isFirst && !isLast) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
                if (!isLast) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.field_move_down),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                val showWarning = !isValid && field.value.isNotEmpty() &&
                    (field.type != CustomFieldType.PHONE ||
                        (splitPhone(field.value).second.isNotEmpty() &&
                         (splitPhone(field.value).first + splitPhone(field.value).second).filter { it.isDigit() }.length > 15))
                if (showWarning) {
                    Icon(
                        imageVector        = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.field_invalid_format),
                        tint               = MaterialTheme.colorScheme.error,
                        modifier           = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close,
                        contentDescription = stringResource(R.string.field_delete_field),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp))
                }
            }

            if (field.type == CustomFieldType.PHONE) {
                PhoneSubFields(
                    field         = field,
                    onValueChange = onValueChange
                )
            }

            if (field.type == CustomFieldType.DATE) {
                var showDatePicker by remember(field.id) { mutableStateOf(false) }
                val displayDate = buildString {
                    field.value.forEachIndexed { i, ch -> if (i == 2 || i == 4) append('/'); append(ch) }
                }
                OutlinedTextField(
                    value         = displayDate,
                    onValueChange = {},
                    readOnly      = true,
                    placeholder   = { Text(field.label, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier      = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape         = RoundedCornerShape(8.dp),
                    colors        = fieldColors(),
                    trailingIcon  = if (field.value.isNotEmpty()) ({
                        IconButton(onClick = { autoLockManager?.recordInteraction(); onValueChange("") }) {
                            Icon(Icons.Default.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }) else ({
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    })
                )
                if (showDatePicker) {
                    val pickerState = rememberDatePickerState(initialSelectedDateMillis = field.value.digitsToPickerMillis())
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                pickerState.selectedDateMillis?.let {
                                    autoLockManager?.recordInteraction()
                                    onValueChange(it.toDateDigits())
                                }
                                showDatePicker = false
                            }) { Text(stringResource(R.string.action_ok)) }
                        },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) } }
                    ) { DatePicker(state = pickerState) }
                }
            }

            if (field.type != CustomFieldType.PHONE && field.type != CustomFieldType.DATE) Box {
            OutlinedTextField(
                value                = field.value,
                onValueChange        = { autoLockManager?.recordInteraction(); onValueChange(applyFormatting(field.type, it)) },
                placeholder          = { Text(field.label, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                isError              = !isValid && field.value.isNotEmpty(),
                visualTransformation = when {
                    field.type.isSecret && !showValue      -> PasswordVisualTransformation()
                    else                                   -> VisualTransformation.None
                },
                trailingIcon = if (field.type == CustomFieldType.NOTE && noteDictationEnabled) {
                    {
                        IconButton(onClick = onDictate) {
                            Icon(
                                imageVector        = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.voice_dictation_mic_desc),
                                tint               = if (isDictating) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                } else if (field.type.isSecret || field.type.hasCopy || field.type == CustomFieldType.URL) {
                    {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (field.type.isSecret) {
                                val isGated = field.type.isSecret
                                IconButton(onClick = {
                                    if (showValue) {
                                        showValue = false
                                    } else {
                                        if (isGated) triggerGate { showValue = true }
                                        else showValue = true
                                    }
                                }) {
                                    Icon(
                                        imageVector        = if (showValue) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showValue) stringResource(R.string.action_hide) else stringResource(R.string.action_show),
                                        tint               = if (isGated && !showValue)
                                                                 MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (field.type.hasCopy) {
                                val isGated = field.type.isSecret
                                IconButton(
                                    onClick  = {
                                        if (isGated) triggerGate { ClipboardUtil.copySensitive(clipCtx, field.value); clipClear.scheduleClear() }
                                        else { ClipboardUtil.copySensitive(clipCtx, field.value); clipClear.scheduleClear() }
                                    },
                                    enabled  = field.value.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.action_copy),
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (field.type == CustomFieldType.URL) {
                                val canOpen = field.value.isNotBlank() && isValid
                                IconButton(
                                    onClick  = { context.openUrl(field.value) },
                                    enabled  = canOpen,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = stringResource(R.string.action_open_url),
                                        tint               = if (canOpen) MaterialTheme.colorScheme.primary
                                                             else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else null,
                minLines        = if (field.type.isMultiline) 3 else 1,
                singleLine      = !field.type.isMultiline,
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(8.dp),
                colors          = fieldColors(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = field.type.keyboardType,
                    imeAction    = ImeAction.Next,
                    autoCorrectEnabled  = field.type == CustomFieldType.NOTE
                )
            )
            if (field.type == CustomFieldType.NOTE) {
                Text(
                    text     = "${field.value.length} chars",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 8.dp),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }
            }

            val hintResId = field.type.formatHintRes()
            if (field.type != CustomFieldType.PHONE && field.type != CustomFieldType.DATE &&
                hintResId != null && !isValid && field.value.isNotEmpty()) {
                Text(
                    text     = "${stringResource(R.string.field_format_hint_prefix)}${stringResource(hintResId)}",
                    style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color    = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun FieldTypePickerDialog(
    onSelect:  (CustomFieldType, String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_field_type_dialog_title)) },
        text  = {
            Column {
                CustomFieldType.entries.forEach { type ->
                    val label     = stringResource(type.labelRes())
                    val hintResId = type.formatHintRes()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelect(type, label) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = fieldTypeIcon(type),
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text  = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (hintResId != null) {
                                Text(
                                    text  = stringResource(hintResId),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (type != CustomFieldType.entries.last()) {
                        HorizontalDivider(thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
internal fun CustomEntryOptionsSheet(
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
    var renameTarget     by remember { mutableStateOf<Tag?>(null) }
    var renameText       by remember { mutableStateOf("") }
    var renameError      by remember { mutableStateOf<String?>(null) }
    var newTagText       by remember { mutableStateOf("") }
    var newTagError      by remember { mutableStateOf<String?>(null) }
    var showLockedDialog by remember { mutableStateOf(false) }

    val errDuplicate = stringResource(R.string.tag_error_duplicate)
    val errCategory  = stringResource(R.string.tag_error_category_conflict)

    var searchQuery  by remember { mutableStateOf("") }
    val filteredTags = remember(availableTags, searchQuery, selectedTags) {
        if (searchQuery.isBlank()) availableTags
        else availableTags.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true) || it.name in selectedTags
        }
    }
    val searchHasNoMatches = searchQuery.isNotBlank() &&
        availableTags.none { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    val tryCreateTag: (String) -> Unit = { raw ->
        val trimmed = raw.trim()
        when {
            trimmed.isBlank() -> {}
            availableTags.any { tagConflictKey(it.name) == tagConflictKey(trimmed) } -> newTagError = errDuplicate
            EntryCategory.entries.any { it.label.equals(trimmed, ignoreCase = true) } -> newTagError = errCategory
            else -> {
                onAddTag(trimmed)
                selectedTags = selectedTags + trimmed
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
                            onClick      = { selectedCategory = if (selectedCategory == cat) EntryCategory.SIN_CATEGORIZAR else cat },
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
                text  = stringResource(R.string.options_sheet_tags),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            }
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
                        modifier = Modifier.size(41.dp).offset(x = (-6).dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Edit,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                    TagDeleteButton(
                        tag      = tag,
                        onDelete = { selectedTags = selectedTags - tag.name; onDeleteTag(tag.id) },
                        modifier = Modifier.size(41.dp).offset(x = (-6).dp)
                    )
                }
            }
            if (searchHasNoMatches && filteredTags.isNotEmpty()) {
                TagSearchEmptyState(
                    query       = searchQuery,
                    canCreate   = canCreateTags,
                    onCreateTag = tryCreateTag
                )
            }

            renameTarget?.let { target ->
                AlertDialog(
                    modifier = Modifier.cuadroConTeclado(),
                    properties = propiedadesConTeclado(),
                    onDismissRequest = { renameTarget = null; renameError = null },
                    containerColor   = MaterialTheme.colorScheme.background,
                    title = {
                        Text(
                            stringResource(R.string.tag_rename_title),
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    text = {
                        Column {
                            BasicTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                                value         = renameText,
                                onValueChange = { autoLockManager?.recordInteraction(); renameText = it; renameError = null },
                                singleLine    = true,
                                textStyle     = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { inner ->
                                    if (renameText.isEmpty()) {
                                        Text(
                                            text  = stringResource(R.string.tag_name_hint),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    }
                                    inner()
                                }
                            )
                            if (renameError != null) {
                                Text(
                                    text  = renameError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val trimmed = renameText.trim()
                                when {
                                    trimmed.isBlank() -> {}
                                    availableTags.any { it.id != target.id && tagConflictKey(it.name) == tagConflictKey(trimmed) } ->
                                        renameError = errDuplicate
                                    EntryCategory.entries.any { it.label.equals(trimmed, ignoreCase = true) } ->
                                        renameError = errCategory
                                    else -> {
                                        if (selectedTags.contains(target.name)) {
                                            selectedTags = (selectedTags - target.name) + trimmed
                                        }
                                        onRenameTag(target.id, trimmed)
                                        renameTarget = null
                                        renameError  = null
                                    }
                                }
                            },
                            shape  = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.action_save), fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { renameTarget = null; renameError = null }) {
                            Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTags.isNotEmpty()) {
                Button(
                    onClick  = { onSave(selectedCategory, selectedTags.toList()) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text       = stringResource(R.string.action_apply),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
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
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
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

            if (!searchHasNoMatches) {
            if (canCreateTags) {
            Spacer(modifier = Modifier.height(8.dp))
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
                            text  = stringResource(R.string.field_tag_name),
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
                            modifier           = Modifier.size(18.dp)
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
                    modifier = Modifier.padding(horizontal = 0.dp, vertical = 2.dp)
                )
            }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
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
                            text  = stringResource(R.string.field_tag_name),
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun fieldTypeIcon(type: CustomFieldType): ImageVector =
    when (type) {
        CustomFieldType.TEXT     -> Icons.Default.TextFields
        CustomFieldType.NUMBER   -> Icons.Default.Numbers
        CustomFieldType.PASSWORD -> Icons.Default.Lock
        CustomFieldType.DATE     -> Icons.Default.DateRange
        CustomFieldType.EMAIL    -> Icons.Default.Email
        CustomFieldType.URL      -> Icons.Default.Link
        CustomFieldType.WALLET   -> Icons.Default.AccountBalanceWallet
        CustomFieldType.PHONE    -> Icons.Default.Phone
        CustomFieldType.PIN      -> Icons.Default.Pin
        CustomFieldType.CODE     -> Icons.Default.QrCode
        CustomFieldType.NOTE     -> Icons.AutoMirrored.Filled.Notes
    }

private fun applyFormatting(type: CustomFieldType, value: String): String = when (type) {
    CustomFieldType.NUMBER -> value.filter { it.isDigit() }
    CustomFieldType.PIN    -> value.filter { it.isDigit() }
    CustomFieldType.DATE   -> value.filter { it.isDigit() }.take(8)
    CustomFieldType.PHONE  -> value.filter { it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' || it.isDigit() }
    else -> value
}

@Composable
private fun PhoneSubFields(
    field:         CustomField,
    onValueChange: (String) -> Unit
) {
    val (prefix, localNumber) = remember(field.value) { splitPhone(field.value) }
    var showCountryPicker by remember { mutableStateOf(false) }
    val autoLockManager = LocalAutoLockManager.current

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (field.value.isEmpty()) {
            val code = defaultDialCode(context)
            if (code.isNotEmpty()) onValueChange(code)
        }
    }

    val totalDigits = (prefix + localNumber).filter { it.isDigit() }.length

    if (showCountryPicker) {
        CountryCodePickerDialog(
            onSelect  = { code ->
                val newPrefix = code.dialCode.removePrefix("+").filter { it.isDigit() }
                onValueChange(combinePhone(newPrefix, localNumber))
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }

    Row(
        modifier              = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.Top
    ) {
        OutlinedTextField(
            value         = prefix,
            onValueChange = { v ->
                autoLockManager?.recordInteraction()
                val digits = v.filter { it.isDigit() }.take(4)
                onValueChange(combinePhone(digits, localNumber))
            },
            placeholder = { Text(stringResource(R.string.card_field_bank_phone_prefix), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            prefix = {
                Text(
                    text  = "+",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            leadingIcon = {
                Icon(
                    imageVector        = Icons.Default.Language,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier
                        .size(18.dp)
                        .clickable { showCountryPicker = true }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine      = true,
            modifier        = Modifier.weight(1.5f),
            shape           = RoundedCornerShape(8.dp),
            colors          = fieldColors()
        )

        val context = LocalContext.current
        OutlinedTextField(
            value         = localNumber,
            onValueChange = { v ->
                autoLockManager?.recordInteraction()
                val filtered = v.filter { it.isDigit() || it == ' ' || it == '-' || it == '(' || it == ')' }
                onValueChange(combinePhone(prefix, truncateToMaxDigits(filtered, 13)))
            },
            placeholder     = { Text(field.label, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            isError         = localNumber.isNotEmpty() && totalDigits > 15,
            trailingIcon    = {
                val fullNumber = buildString {
                    if (prefix.isNotEmpty()) append("+$prefix")
                    append(localNumber.trim())
                }
                IconButton(
                    onClick  = {
                        val uri = "tel:${fullNumber.filter { it.isDigit() || it == '+' }}".toUri()
                        context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                    },
                    enabled  = localNumber.isNotEmpty()
                ) {
                    Icon(
                        imageVector        = Icons.Default.Call,
                        contentDescription = stringResource(R.string.action_call),
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine      = true,
            modifier        = Modifier.weight(2f),
            shape           = RoundedCornerShape(8.dp),
            colors          = fieldColors()
        )
    }

    if (localNumber.isNotEmpty() && totalDigits > 15) {
        Text(
            text     = stringResource(R.string.field_phone_hint),
            style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color    = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )
    }
}

private fun dateDigitsFormat() = java.text.SimpleDateFormat("ddMMyyyy", java.util.Locale.getDefault()).apply {
    timeZone = java.util.TimeZone.getTimeZone("UTC")
}

private fun String.digitsToPickerMillis(): Long? =
    if (length != 8) null
    else runCatching { dateDigitsFormat().parse(this)?.time }.getOrNull()

private fun Long.toDateDigits(): String = dateDigitsFormat().format(java.util.Date(this))

@Suppress("SameParameterValue")
private fun truncateToMaxDigits(value: String, maxDigits: Int): String {
    var count = 0
    return buildString {
        for (ch in value) {
            if (ch.isDigit()) {
                if (count < maxDigits) { append(ch); count++ } else return@buildString
            } else {
                append(ch)
            }
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
private fun titleFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)
