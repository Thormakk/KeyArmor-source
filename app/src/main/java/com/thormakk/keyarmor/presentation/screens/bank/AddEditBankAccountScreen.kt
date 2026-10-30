@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.bank

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.SimpleCardTextField
import com.thormakk.keyarmor.presentation.components.TagDeleteButton
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.presentation.util.highlightText
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

private val MergedCardRadius     = 14.dp
private val MergedCardStartInset = 48.dp
private val MergedCardEndInset   = 14.dp

@Composable
fun AddEditBankAccountScreen(
    onBack:           () -> Unit,
    onSaved:          () -> Unit,
    onViewCredential: (String) -> Unit,
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: AddEditBankAccountViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val vaultEntries by viewModel.vaultEntries.collectAsStateWithLifecycle()

    val clipCtx      = LocalContext.current
    val clipClear    = LocalClipboardClearManager.current
    val autoLockManager = LocalAutoLockManager.current

    var showPickerDialog   by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved)   { if (uiState.isSaved)   onSaved() }

    val linkedEntryName = remember(uiState.linkedPasswordId, vaultEntries) {
        vaultEntries.firstOrNull { it.id == uiState.linkedPasswordId }?.title ?: ""
    }

    if (showPickerDialog) {
        CredentialPickerDialog(
            entries   = vaultEntries,
            onSelect  = { entry ->
                viewModel.onLinkedPasswordIdChange(entry.id)
                showPickerDialog = false
            },
            onDismiss = { showPickerDialog = false }
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
                            if (uiState.isNew) stringResource(R.string.bank_new) else stringResource(R.string.bank_edit),
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
                        IconButton(onClick = { viewModel.save() }) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 7.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val mergedGapColor  = MaterialTheme.colorScheme.background

            Column(modifier = Modifier.fillMaxWidth()) {
            SimpleCardTextField(
                value         = uiState.bankName,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onBankNameChange(it) },
                placeholder   = stringResource(R.string.bank_field_bank_name),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                leadingIcon   = {
                    val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState      = rememberSharedContentState("bank_avatar_${uiState.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    } else Modifier
                    BankAwareAvatar(bankName = uiState.bankName, size = 32.dp, modifier = avatarModifier)
                },
                shape          = entryItemShape(isFirst = true, isLast = false, radius = MergedCardRadius),
                containerColor = MaterialTheme.colorScheme.surface
            )
            EntryGroupDivider(
                startInset = 0.dp,
                endInset   = 0.dp,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            SimpleCardTextField(
                value         = uiState.swiftBic,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onSwiftBicChange(it) },
                placeholder   = stringResource(R.string.bank_field_swift),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                leadingIcon   = {
                    Icon(Icons.Default.Tag, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                shape = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius)
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            SimpleCardTextField(
                value         = uiState.accountType,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onAccountTypeChange(it) },
                placeholder   = stringResource(R.string.bank_field_account_type),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                leadingIcon   = {
                    Icon(Icons.Default.Category, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                shape = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius)
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            SimpleCardTextField(
                value         = uiState.cbuIban,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onCbuIbanChange(it) },
                placeholder   = stringResource(R.string.bank_field_cbu_iban),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                leadingIcon   = {
                    Icon(Icons.Default.Key, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingContent = {
                    IconButton(
                        onClick  = { ClipboardUtil.copySensitive(clipCtx, uiState.cbuIban); clipClear.scheduleClear() },
                        enabled  = uiState.cbuIban.isNotEmpty()
                    ) {
                        Icon(
                            imageVector        = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.action_copy),
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                },
                shape = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius)
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            SimpleCardTextField(
                value         = uiState.alias,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onAliasChange(it) },
                placeholder   = stringResource(R.string.bank_field_alias),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                leadingIcon   = {
                    Icon(Icons.Default.Badge, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingContent = {
                    IconButton(
                        onClick  = { ClipboardUtil.copySensitive(clipCtx, uiState.alias); clipClear.scheduleClear() },
                        enabled  = uiState.alias.isNotEmpty()
                    ) {
                        Icon(
                            imageVector        = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.action_copy),
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                },
                shape = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius)
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            CredentialLinkField(
                linkedName   = linkedEntryName,
                hasLink      = uiState.linkedPasswordId.isNotEmpty(),
                onPickClick  = { showPickerDialog = true },
                onViewClick  = { onViewCredential(uiState.linkedPasswordId) },
                onClearClick = { viewModel.onLinkedPasswordIdChange("") },
                shape        = entryItemShape(isFirst = false, isLast = false, radius = MergedCardRadius)
            )
            EntryGroupDivider(
                startInset = MergedCardStartInset,
                endInset   = MergedCardEndInset,
                cardColor  = mergedCardColor,
                gapColor   = mergedGapColor
            )

            Box {
                OutlinedTextField(
                    value         = uiState.notes,
                    onValueChange = { autoLockManager?.recordInteraction(); viewModel.onNotesChange(it) },
                    placeholder   = { Text(stringResource(R.string.bank_field_notes), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    minLines      = 3,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = entryItemShape(isFirst = false, isLast = true, radius = MergedCardRadius),
                    colors        = fieldColors()
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
            }

            Spacer(modifier = Modifier.height(24.dp))
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
private fun CredentialLinkField(
    linkedName:   String,
    hasLink:      Boolean,
    onPickClick:  () -> Unit,
    onViewClick:  () -> Unit,
    onClearClick: () -> Unit,
    shape:        Shape = RoundedCornerShape(8.dp)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = stringResource(R.string.bank_link_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = if (hasLink) linkedName.ifEmpty { stringResource(R.string.bank_link_linked) }
                         else stringResource(R.string.bank_link_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasLink) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }

        if (hasLink) {
            IconButton(
                onClick  = onViewClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.action_view_credential),
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick  = onClearClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.LinkOff,
                    contentDescription = stringResource(R.string.action_unlink),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
        } else {
            IconButton(
                onClick  = onPickClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Link,
                    contentDescription = stringResource(R.string.action_link),
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CredentialPickerDialog(
    entries:   List<VaultEntry>,
    onSelect:  (VaultEntry) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.bank_link_dialog_title)) },
        text  = {
            if (entries.isEmpty()) {
                Text(
                    stringResource(R.string.bank_link_dialog_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column {
                    entries.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(entry) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Lock,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text  = entry.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (entry.username.isNotEmpty() || entry.email.isNotEmpty()) {
                                    Text(
                                        text  = entry.username.ifEmpty { entry.email },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color     = MaterialTheme.colorScheme.outlineVariant
                        )
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
internal fun BankOptionsSheet(
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

    var searchQuery by remember { mutableStateOf("") }
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
                trailingIcon  = if (searchQuery.isNotEmpty()) ({
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                    }
                }) else null,
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
        }
    }
}
