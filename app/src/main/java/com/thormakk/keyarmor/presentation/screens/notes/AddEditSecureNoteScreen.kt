@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.notes
import androidx.compose.ui.text.input.KeyboardType

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.highlightText
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.thormakk.keyarmor.presentation.screens.voicedictation.VoiceDictationViewModel
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.TagDeleteButton
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

private val MergedCardRadius = 14.dp

@Composable
fun AddEditSecureNoteScreen(
    onBack:      () -> Unit,
    onSaved:     () -> Unit,
    viewModel: AddEditSecureNoteViewModel = hiltViewModel()
) {
    val uiState              by viewModel.uiState.collectAsStateWithLifecycle()
    val globalHistoryEnabled by viewModel.globalHistoryEnabled.collectAsStateWithLifecycle()

    val focusManager   = LocalFocusManager.current
    val autoLockManager = LocalAutoLockManager.current
    val nativeState   = rememberNativeRichTextState()

    val titleFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        if (viewModel.isNewNote) {
            kotlinx.coroutines.delay(250)
            titleFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) nativeState.setHtml(uiState.content)
    }

    val dictationVm: VoiceDictationViewModel = hiltViewModel()
    val dictationEnabled by dictationVm.enabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val msgPermDenied  = stringResource(R.string.voice_dictation_permission_denied)
    val msgListening   = stringResource(R.string.voice_dictation_listening)
    val msgErrLanguage = stringResource(R.string.voice_dictation_error_language)
    val msgErrGeneric  = stringResource(R.string.voice_dictation_error_generic)
    var dictationError by remember { mutableStateOf<Int?>(null) }
    val dictation = rememberNoteDictationController(
        languageTag = { dictationVm.languageTag.value },
        onText = { txt, isFinal ->
            autoLockManager?.recordInteraction()
            nativeState.updateDictation(txt)
            if (isFinal) {
                nativeState.endDictation()
                viewModel.onContentChange(nativeState.toHtml())
            }
        },
        onError = { code -> dictationError = code }
    )
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
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) { nativeState.beginDictation(); dictation.start() }
        else Toast.makeText(context, msgPermDenied, Toast.LENGTH_LONG).show()
    }
    LaunchedEffect(dictation.isListening) {
        if (dictation.isListening) Toast.makeText(context, msgListening, Toast.LENGTH_SHORT).show()
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
    val showMic = dictationEnabled && dictation.available
    val onDictate: () -> Unit = {
        autoLockManager?.recordInteraction()
        when {
            dictation.isListening -> dictation.stop()
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED -> { nativeState.beginDictation(); dictation.start() }
            else -> micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }

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
                            if (uiState.isNew) stringResource(R.string.notes_new) else stringResource(R.string.notes_edit),
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
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        if (globalHistoryEnabled) {
                            IconButton(onClick = { viewModel.toggleNoteHistory() }) {
                                Icon(
                                    imageVector        = Icons.Default.History,
                                    contentDescription = stringResource(R.string.note_history_per_note_enable),
                                    tint               = if (uiState.historyEnabled)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick  = { focusManager.clearFocus(); viewModel.onContentChange(nativeState.toHtml()); viewModel.save() },
                            enabled  = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Save,
                                contentDescription = stringResource(R.string.action_save),
                                tint               = if (!uiState.isLoading) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

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
                .padding(horizontal = 7.dp, vertical = 8.dp)
        ) {
            val mergedGapColor = MaterialTheme.colorScheme.background

            OutlinedTextField(
                value         = uiState.title,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onTitleChange(it) },
                placeholder   = { Text(stringResource(R.string.notes_field_title_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                isError         = uiState.titleError || uiState.cloneNameError,
                supportingText  = when {
                    uiState.cloneNameError -> { { Text(stringResource(R.string.clone_name_error), color = MaterialTheme.colorScheme.error) } }
                    uiState.titleError     -> { { Text(stringResource(R.string.error_title_required), color = MaterialTheme.colorScheme.error) } }
                    else                   -> null
                },
                singleLine      = true,
                modifier        = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType   = KeyboardType.Uri,
                    autoCorrectEnabled = false,
                    imeAction      = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape  = entryItemShape(isFirst = true, isLast = false, radius = MergedCardRadius),
                colors = titleFieldColors()
            )

            EntryGroupDivider(
                startInset = 0.dp,
                endInset   = 0.dp,
                cardColor  = MaterialTheme.colorScheme.surface,
                gapColor   = mergedGapColor
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .verticalScroll(rememberScrollState())
            ) {
                NativeRichTextEditor(
                    state                   = nativeState,
                    modifier                = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp)
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 40.dp),
                    textColor               = MaterialTheme.colorScheme.onSurface,
                    fontSizeSp              = MaterialTheme.typography.bodyLarge.fontSize.value,
                    lineHeightSp            = MaterialTheme.typography.bodyLarge.lineHeight.value,
                    selectionHighlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            }

            EntryGroupDivider(
                startInset = 0.dp,
                endInset   = 0.dp,
                cardColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                gapColor   = mergedGapColor
            )

            NoteFormatToolbar(
                state            = nativeState,
                shape            = entryItemShape(isFirst = false, isLast = true, radius = MergedCardRadius),
                dictationEnabled = showMic,
                isDictating      = dictation.isListening,
                onDictate        = onDictate
            )

        }
    }
}

@Composable
internal fun NoteOptionsSheet(
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
    var newTagText       by remember { mutableStateOf("") }
    var newTagError      by remember { mutableStateOf<String?>(null) }
    var renameTarget     by remember { mutableStateOf<Tag?>(null) }
    var renameText       by remember { mutableStateOf("") }
    var renameError      by remember { mutableStateOf<String?>(null) }
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
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
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
                    title = { Text(stringResource(R.string.tag_rename_title)) },
                    text  = {
                        OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                            value           = renameText,
                            onValueChange   = { autoLockManager?.recordInteraction(); renameText = it; renameError = null },
                            singleLine      = true,
                            label           = { Text(stringResource(R.string.tag_name_hint)) },
                            shape           = RoundedCornerShape(8.dp),
                colors        = fieldColors(),
                            modifier        = Modifier.fillMaxWidth(),
                            isError         = renameError != null,
                            supportingText  = { renameError?.let { Text(it) } }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick  = {
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
                            enabled = renameText.trim().isNotBlank(),
                            shape   = RoundedCornerShape(8.dp),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
@Composable
private fun NoteFormatToolbar(
    state: NativeRichTextState,
    shape: Shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    dictationEnabled: Boolean = false,
    isDictating:      Boolean = false,
    onDictate:        () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NoteFormatButton(Icons.AutoMirrored.Outlined.Undo, stringResource(R.string.action_undo),
            enabled = state.canUndo) { state.undo() }
        NoteFormatButton(Icons.AutoMirrored.Outlined.Redo, stringResource(R.string.format_redo),
            enabled = state.canRedo) { state.redo() }
        FormatToolbarDivider()
        NoteFormatButton(Icons.Outlined.FormatBold, stringResource(R.string.format_bold), state.isBold) {
            state.toggleBold()
        }
        NoteFormatButton(Icons.Outlined.FormatItalic, stringResource(R.string.format_italic), state.isItalic) {
            state.toggleItalic()
        }
        NoteFormatButton(Icons.Outlined.FormatUnderlined, stringResource(R.string.format_underline), state.isUnderline) {
            state.toggleUnderline()
        }
        NoteFormatButton(Icons.Outlined.FormatStrikethrough, stringResource(R.string.format_strikethrough), state.isStrikethrough) {
            state.toggleStrikethrough()
        }
        FormatToolbarDivider()
        NoteFormatButton(Icons.AutoMirrored.Outlined.FormatListBulleted, stringResource(R.string.format_bullet_list), state.isBulletList) {
            state.toggleBulletList()
        }
        NoteFormatButton(Icons.Outlined.FormatListNumbered, stringResource(R.string.format_numbered_list), state.isNumberedList) {
            state.toggleNumberedList()
        }
        if (dictationEnabled) {
            FormatToolbarDivider()
            NoteFormatButton(
                imageVector        = Icons.Default.Mic,
                contentDescription = stringResource(R.string.voice_dictation_mic_desc),
                isActive           = isDictating,
                onClick            = onDictate
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text     = "${state.textLength}",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
private fun NoteFormatButton(
    imageVector:        ImageVector,
    contentDescription: String,
    isActive:           Boolean = false,
    enabled:            Boolean = true,
    onClick:            () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp), enabled = enabled) {
        Icon(
            imageVector        = imageVector,
            contentDescription = contentDescription,
            tint               = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                isActive -> MaterialTheme.colorScheme.primary
                else     -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier           = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun FormatToolbarDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(0.5.dp)
            .height(20.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    )
}

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
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor             = MaterialTheme.colorScheme.primary
)