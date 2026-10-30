@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.identity

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Visibility
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.ui.theme.SuccessGreenBright
import com.thormakk.keyarmor.ui.theme.SuccessGreen
import com.thormakk.keyarmor.presentation.screens.settings.contrastRatio
import androidx.compose.material.icons.filled.LockOpen
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.InfoChip
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.chipCategoryColor
import com.thormakk.keyarmor.presentation.components.chipTagColor
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.infoColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.bank.EntryMetadata
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

private val MergedCardRadius     = 14.dp
private val MergedCardStartInset = 16.dp
private val MergedCardEndInset   = 15.dp

@Composable
fun IdentityDocumentDetailScreen(
    onBack:             () -> Unit,
    onEdit:             (String) -> Unit,
    onDeleted:          () -> Unit,
    onAttachmentsClick: (String) -> Unit = {},
    onShare:            (String) -> Unit = {},
    onPlansClick:       () -> Unit = {},
    viewModel:          AddEditIdentityDocumentViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val isFreeUser    by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val isReadOnly    by viewModel.isReadOnly.collectAsStateWithLifecycle()
    val otherVaults   by viewModel.otherVaults.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val clipCtx      = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val isHubPinned        by viewModel.isHubPinned.collectAsStateWithLifecycle()
    val hubPinLimitReached by viewModel.hubPinLimitReached.collectAsStateWithLifecycle()
    val hubPinMsg          = stringResource(R.string.hub_pin_limit_toast)
    val scope              = rememberCoroutineScope()
    LaunchedEffect(hubPinLimitReached) {
        if (hubPinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(hubPinMsg) }
            viewModel.clearHubPinLimitReached()
        }
    }

    LaunchedEffect(uiState.isSaved)   { if (uiState.isSaved)   onDeleted() }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onDeleted() }

    var showDeleteDialog             by remember { mutableStateOf(false) }
    var protectedNotice              by remember { mutableStateOf<Int?>(null) }
    var showUnlockDialog             by remember { mutableStateOf(false) }
    var showProtectPremium           by remember { mutableStateOf(false) }
    var showMenu                     by remember { mutableStateOf(false) }
    var showArchiveLockedDialog      by remember { mutableStateOf(false) }
    var showOptionsSheet             by remember { mutableStateOf(false) }
    var showVaultSheet               by remember { mutableStateOf(false) }
    var showRemoveAttributionDialog  by remember { mutableStateOf(false) }
    var vaultActionIsMove       by remember { mutableStateOf(true) }
    var fullscreenBitmap        by remember { mutableStateOf<Bitmap?>(null) }

    protectedNotice?.let { msgRes ->
        AlertDialog(
            onDismissRequest = { protectedNotice = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_locked_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(msgRes), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { protectedNotice = null }) { Text(stringResource(R.string.action_understood)) }
            }
        )
    }
    if (showUnlockDialog) {
        AlertDialog(
            onDismissRequest = { showUnlockDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_unlock_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(if (viewModel.canProtectNew) R.string.protect_unlock_body else R.string.protect_unlock_body_free),
                color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showUnlockDialog = false; viewModel.setProtected(false) }) {
                    Text(stringResource(R.string.lock_action_unlock))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }
    if (showProtectPremium) {
        AlertDialog(
            onDismissRequest = { showProtectPremium = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_premium_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.protect_premium_body), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                TextButton(onClick = { showProtectPremium = false; onPlansClick() }) {
                    Text(stringResource(R.string.plans_upgrade_cta))
                }
            },
            dismissButton = {
                TextButton(onClick = { showProtectPremium = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(stringResource(if (uiState.isHidden) R.string.hidden_entries_delete_confirm_title else R.string.dialog_move_to_trash_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            },
            text = {
                Text(stringResource(if (uiState.isHidden) R.string.hidden_entries_delete_confirm_message else R.string.identity_doc_delete_message), color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; viewModel.delete() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(if (uiState.isHidden) R.string.action_delete else R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showArchiveLockedDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveLockedDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.plans_feature_archive),
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
                    onClick = { showArchiveLockedDialog = false; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveLockedDialog = false }) {
                    Text(stringResource(R.string.action_understood), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showOptionsSheet) {
        IdentityOptionsSheet(
            initialCategory = uiState.category,
            initialTags     = uiState.tags,
            availableTags   = availableTags,
            onSave          = { cat, tags -> viewModel.saveMetaOnly(cat, tags); showOptionsSheet = false },
            onDismiss       = { showOptionsSheet = false },
            onAddTag        = { viewModel.addTag(it) },
            onRenameTag     = { id, name -> viewModel.renameTag(id, name) },
            onDeleteTag     = { viewModel.deleteTag(it) },
            tagConflictKey  = viewModel::tagConflictKey,
            canCreateTags   = !isFreeUser,
            onUpgradeClick  = onPlansClick
        )
    }

    if (showVaultSheet) {
        MoveToVaultBottomSheet(
            vaults    = otherVaults,
            isMove    = vaultActionIsMove,
            onSelect  = { targetId ->
                showVaultSheet = false
                if (vaultActionIsMove) viewModel.moveEntryTo(targetId)
                else                   viewModel.copyEntryTo(targetId)
            },
            onDismiss = { showVaultSheet = false }
        )
    }

    Scaffold(
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
                        val identityDocFallbackTitle = stringResource(R.string.identity_doc_fallback_title)
                        val topBarTitle = listOf(uiState.docType, uiState.number)
                            .filter { it.isNotEmpty() }
                            .joinToString(" - ")
                            .ifEmpty { identityDocFallbackTitle }
                        TextoDeUnaLinea(
                            topBarTitle,
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                            CaptureAllowedBadge()
                            ThemeTrialBadge()
                            if ((!isReadOnly || uiState.isProtected) && !uiState.isNew) {
                                val greenLock = if (contrastRatio(MaterialTheme.colorScheme.background, SuccessGreen) < 3f)
                                    SuccessGreenBright else SuccessGreen
                                IconButton(onClick = {
                                    when {
                                        uiState.isProtected      -> showUnlockDialog = true
                                        viewModel.canProtectNew  -> viewModel.setProtected(true)
                                        else                     -> showProtectPremium = true
                                    }
                                }) {
                                    Icon(
                                        imageVector        = if (uiState.isProtected) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = stringResource(if (uiState.isProtected) R.string.protect_cd_unlock else R.string.protect_cd_lock),
                                        tint               = if (uiState.isProtected) greenLock else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (!isReadOnly) IconButton(onClick = {
                                if (uiState.isProtected) protectedNotice = R.string.protect_locked_edit else onEdit(uiState.id)
                            }) {
                                Icon(Icons.Default.Edit, stringResource(R.string.action_edit), tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurface)
                                }
                                DropdownMenu(
                                    expanded         = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    containerColor   = MaterialTheme.colorScheme.surface
                                ) {
                                    DropdownMenuItem(
                                        text        = { Text(if (isHubPinned) stringResource(R.string.hub_unpin_from_hub) else stringResource(R.string.hub_pin_to_hub)) },
                                        leadingIcon = { Icon(if (isHubPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, null) },
                                        onClick     = { showMenu = false; viewModel.toggleHubPin() }
                                    )
                                    HorizontalDivider()
                                    if (!isReadOnly) {
                                        DropdownMenuItem(
                                            text        = { Text(stringResource(R.string.action_attachments)) },
                                            leadingIcon = { Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(20.dp)) },
                                            onClick     = { showMenu = false; onAttachmentsClick(uiState.id) }
                                        )
                                        DropdownMenuItem(
                                            text        = { Text(stringResource(R.string.menu_share)) },
                                            leadingIcon = { Icon(Icons.Default.Share, null) },
                                            onClick     = { showMenu = false; onShare(uiState.id) }
                                        )
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text        = { Text(stringResource(R.string.action_move_to)) },
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, null) },
                                            onClick     = { showMenu = false; vaultActionIsMove = true; showVaultSheet = true }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text        = { Text(stringResource(R.string.action_copy_to)) },
                                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                        onClick     = { showMenu = false; vaultActionIsMove = false; showVaultSheet = true }
                                    )
                                    if (!isReadOnly) {
                                        HorizontalDivider()
                                        if (!uiState.isHidden) {
                                            DropdownMenuItem(
                                                text        = { Text(stringResource(R.string.menu_archive)) },
                                                leadingIcon = { Icon(Icons.Default.Archive, null) },
                                                onClick     = {
                                                    showMenu = false
                                                    if (isFreeUser) showArchiveLockedDialog = true else viewModel.archive()
                                                }
                                            )
                                        }
                                    }
                                    DropdownMenuItem(
                                        text        = { Text(stringResource(R.string.action_move_to_trash), color = dangerColor()) },
                                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = dangerColor()) },
                                        onClick     = {
                                            showMenu = false
                                            if (uiState.isProtected) protectedNotice = R.string.protect_locked_delete
                                            else showDeleteDialog = true
                                        }
                                    )
                                }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surface
            val mergedGapColor  = MaterialTheme.colorScheme.background

            val settingsAlertDays = viewModel.settingsExpiryAlertDaySet
            val sortedAlertDays = settingsAlertDays.sorted()
            val expiryAlertLabel = if (sortedAlertDays.isEmpty()) "" else stringResource(R.string.alert_days_before, sortedAlertDays.joinToString(", "))

            val docTypeCountryLabelBoth    = stringResource(R.string.identity_doc_field_type_country)
            val docTypeCountryLabelType    = stringResource(R.string.identity_doc_field_type)
            val docTypeCountryLabelCountry = stringResource(R.string.identity_doc_field_issuing_country)
            val holderNameLabel  = stringResource(R.string.identity_doc_field_holder_name)
            val docNumberLabel   = stringResource(R.string.identity_doc_field_number)
            val birthDateLabel   = stringResource(R.string.identity_doc_field_birth_date)
            val issueDateLabel   = stringResource(R.string.identity_doc_field_issue_date)
            val expiryDateLabel  = stringResource(R.string.identity_doc_field_expiry_date)
            val expiryAlertLbl   = stringResource(R.string.identity_doc_field_expiry_alert)
            val notesLabel       = stringResource(R.string.identity_doc_field_notes)

            val rows: List<Triple<String, String, (() -> Unit)?>> = buildList {
                val docTypeCountry = listOf(uiState.docType, uiState.issuingCountry)
                    .filter { it.isNotEmpty() }
                    .joinToString(" - ")
                if (docTypeCountry.isNotEmpty()) {
                    val docTypeCountryLabel = when {
                        uiState.docType.isNotEmpty() && uiState.issuingCountry.isNotEmpty() -> docTypeCountryLabelBoth
                        uiState.docType.isNotEmpty() -> docTypeCountryLabelType
                        else -> docTypeCountryLabelCountry
                    }
                    add(Triple(docTypeCountryLabel, docTypeCountry, null))
                }
                if (uiState.holderName.isNotEmpty())
                    add(Triple(holderNameLabel, uiState.holderName, { ClipboardUtil.copySensitive(clipCtx, uiState.holderName) }))
                if (uiState.number.isNotEmpty())
                    add(Triple(docNumberLabel, uiState.number, { ClipboardUtil.copySensitive(clipCtx, uiState.number) }))
                if (uiState.birthDate.isNotEmpty())
                    add(Triple(birthDateLabel, uiState.birthDate, { ClipboardUtil.copySensitive(clipCtx, uiState.birthDate) }))
                if (uiState.issueDate.isNotEmpty())
                    add(Triple(issueDateLabel, uiState.issueDate, { ClipboardUtil.copySensitive(clipCtx, uiState.issueDate) }))
                if (uiState.expiryDate.isNotEmpty()) {
                    add(Triple(expiryDateLabel, uiState.expiryDate, null))
                    add(Triple(expiryAlertLbl, expiryAlertLabel, null))
                }
                if (uiState.notes.isNotEmpty())
                    add(Triple(notesLabel, uiState.notes, { ClipboardUtil.copySensitive(clipCtx, uiState.notes) }))
            }

            if (rows.isNotEmpty()) {
                Surface(
                    shape    = RoundedCornerShape(MergedCardRadius),
                    color    = mergedCardColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        rows.forEachIndexed { index, (label, value, onCopy) ->
                            IdentityDetailRow(
                                label  = label,
                                value  = value,
                                onCopy = onCopy
                            )
                            if (index < rows.lastIndex) {
                                EntryGroupDivider(
                                    startInset = MergedCardStartInset,
                                    endInset   = MergedCardEndInset,
                                    cardColor  = mergedCardColor,
                                    gapColor   = mergedGapColor
                                )
                            }
                        }
                        uiState.inheritedFrom?.let { ownerEmail ->
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier              = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication        = null
                                    ) { showRemoveAttributionDialog = true }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text     = stringResource(R.string.inherited_from_label, ownerEmail),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector        = Icons.Default.Close,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                    modifier           = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.frontPhotoPath != null || uiState.backPhotoPath != null) {
                DocumentPhotoSection(
                    frontPhotoPath   = uiState.frontPhotoPath,
                    backPhotoPath    = uiState.backPhotoPath,
                    onViewFullscreen = { bmp -> fullscreenBitmap = bmp },
                    decryptPhoto     = viewModel::decryptPhoto
                )
            }

            EntryMetadata(createdAt = uiState.createdAt, updatedAt = uiState.updatedAt)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp)
            ) {
                if (uiState.category != EntryCategory.SIN_CATEGORIZAR)
                    InfoChip(stringResource(uiState.category.labelRes), color = chipCategoryColor())
                uiState.tags.forEach { tag -> InfoChip(tag, color = chipTagColor()) }
                if (!isReadOnly) Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .clickable { showOptionsSheet = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text  = "+ ${stringResource(R.string.options_sheet_tags)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showRemoveAttributionDialog) {
                val ownerEmail = uiState.inheritedFrom
                if (ownerEmail != null) {
                    AlertDialog(
                        onDismissRequest = { showRemoveAttributionDialog = false },
                        containerColor   = MaterialTheme.colorScheme.background,
                        title = {
                            Text(
                                text       = stringResource(R.string.remove_attribution_title),
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        text = {
                            Text(
                                text  = stringResource(R.string.remove_attribution_body, ownerEmail),
                                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
                        },
                        confirmButton = {
                            Button(
                                onClick = { showRemoveAttributionDialog = false; viewModel.removeInheritedFrom() },
                                shape   = RoundedCornerShape(8.dp),
                                colors  = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor   = MaterialTheme.colorScheme.primary
                                ),
                                border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            ) { Text(stringResource(R.string.action_remove_attribution)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRemoveAttributionDialog = false }) {
                                Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Surface(
                shape          = RoundedCornerShape(12.dp),
                color          = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier       = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text       = stringResource(R.string.visibility_section_title).uppercase(),
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = infoColor(),
                        modifier   = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp)
                    )
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Luggage,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text       = stringResource(R.string.travel_mode_safe_label),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            modifier   = Modifier.weight(1f)
                        )
                        Switch(
                            checked         = uiState.isTravelSafe,
                            onCheckedChange = { viewModel.setTravelSafe(it) },
                            enabled         = !isReadOnly
                        )
                    }
                    if (viewModel.isHiddenFeatureEnabled) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier
                                    .padding(top = 2.dp)
                                    .size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = stringResource(R.string.hidden_entries_toggle_label),
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text  = stringResource(R.string.hidden_entries_toggle_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked         = uiState.isHidden,
                                onCheckedChange = { viewModel.setHidden(it) },
                                enabled         = !isReadOnly && (uiState.isHidden || viewModel.canHideNewEntries)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    fullscreenBitmap?.let { bmp ->
        Dialog(
            onDismissRequest = { fullscreenBitmap = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fullscreenBitmap = null },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { fullscreenBitmap = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DocumentPhotoSection(
    frontPhotoPath:   String?,
    backPhotoPath:    String?,
    onViewFullscreen: (Bitmap) -> Unit,
    decryptPhoto:     suspend (String) -> ByteArray?
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (frontPhotoPath != null) {
            DocumentPhotoSlot(
                label            = stringResource(R.string.identity_doc_photo_label_front),
                path             = frontPhotoPath,
                modifier         = Modifier.weight(1f),
                onViewFullscreen = onViewFullscreen,
                decryptPhoto     = decryptPhoto
            )
        }
        if (backPhotoPath != null) {
            DocumentPhotoSlot(
                label            = stringResource(R.string.identity_doc_photo_label_back),
                path             = backPhotoPath,
                modifier         = Modifier.weight(1f),
                onViewFullscreen = onViewFullscreen,
                decryptPhoto     = decryptPhoto
            )
        }
    }
}

@Composable
private fun DocumentPhotoSlot(
    label:            String,
    path:             String,
    modifier:         Modifier = Modifier,
    onViewFullscreen: (Bitmap) -> Unit,
    decryptPhoto:     suspend (String) -> ByteArray?
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, path) {
        value = decryptPhoto(path)?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
    }

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.586f)
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (bitmap != null) Modifier.clickable { bitmap?.let { onViewFullscreen(it) } }
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap             = bitmap!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.identity_doc_photo_content_desc, label),
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun IdentityDetailRow(
    label:  String,
    value:  String,
    onCopy: (() -> Unit)? = null
) {
    if (value.isBlank()) return
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end   = if (onCopy != null) 4.dp else 16.dp,
                top   = 12.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (onCopy != null) {
            IconButton(onClick = onCopy, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector        = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}
