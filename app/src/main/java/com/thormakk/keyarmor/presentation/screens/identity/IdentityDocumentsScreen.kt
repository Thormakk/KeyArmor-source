@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.thormakk.keyarmor.presentation.screens.identity

import com.thormakk.keyarmor.presentation.components.ReadOnlyGateDialog
import com.thormakk.keyarmor.presentation.components.FabAddIcon
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.screens.vault.ProtectedLockBadge
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.IdentityDocument
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.components.EntryChipsRow
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.FuzzySectionDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.VioletaDelFab
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.selection.BulkCategorySheet
import com.thormakk.keyarmor.presentation.components.selection.BulkMode
import com.thormakk.keyarmor.presentation.components.selection.BulkTagSheet
import com.thormakk.keyarmor.presentation.components.selection.SelectionTopBar
import com.thormakk.keyarmor.presentation.components.topBarColors
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun IdentityDocumentsScreen(
    onBack:          () -> Unit,
    onAddDocument:   () -> Unit,
    onEditDocument:  (String) -> Unit,
    onPlansClick:    () -> Unit = {},
    viewModel:       IdentityDocumentsViewModel = hiltViewModel()
) {
    val uiState            by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState   = remember { SnackbarHostState() }
    val scope               = rememberCoroutineScope()
    val pinLimitReached    by viewModel.pinLimitReached.collectAsStateWithLifecycle()
    val isFreeUser         by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val isSectionReadOnly = isActiveVaultReadOnly || isFreeUser
    var showReadOnlyGate by remember { mutableStateOf(false) }
    if (showReadOnlyGate) ReadOnlyGateDialog(
        premiumSection = isFreeUser,
        onDismiss = { showReadOnlyGate = false },
        onPlans = onPlansClick
    )
    val availableTags      by viewModel.availableTags.collectAsStateWithLifecycle()
    val attachmentIds      by viewModel.attachmentIds.collectAsStateWithLifecycle()
    val selectedIds        by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isSelectionMode     = selectedIds.isNotEmpty()
    val otherVaults        by viewModel.otherVaults.collectAsStateWithLifecycle()

    var showBulkTagSheet       by remember { mutableStateOf(false) }
    var showBulkCategorySheet  by remember { mutableStateOf(false) }
    var docToDelete            by remember { mutableStateOf<IdentityDocument?>(null) }
    var showArchiveLocked      by remember { mutableStateOf(false) }

    var showBulkUnlock         by remember { mutableStateOf(false) }
    var showBulkProtectPremium by remember { mutableStateOf(false) }

    var protectedSkippedCount by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { viewModel.protectedSkipped.collect { protectedSkippedCount = it } }
    val protectedSkippedMsg = protectedSkippedCount?.let { pluralStringResource(R.plurals.protect_bulk_skipped, it, it) }
    LaunchedEffect(protectedSkippedCount) {
        if (protectedSkippedMsg != null) {
            protectedSkippedCount = null
            snackbarHostState.showSnackbar(protectedSkippedMsg)
        }
    }

    val pinLimitMsg = stringResource(R.string.entry_pin_limit_toast)
    LaunchedEffect(pinLimitReached) {
        if (pinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(pinLimitMsg) }
            viewModel.onPinLimitDismissed()
        }
    }

    val pinnedHubIds       by viewModel.pinnedIds.collectAsStateWithLifecycle()
    val hubPinLimitReached by viewModel.hubPinLimitReached.collectAsStateWithLifecycle()
    val hubPinMsg          = stringResource(R.string.hub_pin_limit_toast)
    LaunchedEffect(hubPinLimitReached) {
        if (hubPinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(hubPinMsg) }
            viewModel.clearHubPinLimitReached()
        }
    }

    var bulkVaultActionIsMove  by remember { mutableStateOf(true) }
    var showBulkVaultSheet     by remember { mutableStateOf(false) }
    var entryActionId          by remember { mutableStateOf("") }
    var entryActionIsMove      by remember { mutableStateOf(true) }
    var showEntryVaultSheet    by remember { mutableStateOf(false) }
    BackHandler(enabled = isSelectionMode) { viewModel.clearSelection() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                if (isSelectionMode) {
                    SelectionTopBar(
                        selectedCount    = selectedIds.size,
                        totalCount       = uiState.documents.size,
                        onClearSelection = viewModel::clearSelection,
                        onSelectAll      = viewModel::selectAll,
                        onArchive        = viewModel::bulkArchive,
                        isArchiveLocked  = isFreeUser,
                        onArchiveLocked  = { showArchiveLocked = true },
                        onDelete         = viewModel::bulkDelete,
                        onTag            = { showBulkTagSheet = true },
                        onCategorize     = { showBulkCategorySheet = true },
                        isReadOnly       = isSectionReadOnly,
                        onMoveTo         = { bulkVaultActionIsMove = true;  showBulkVaultSheet = true },
                        onCopyTo         = { bulkVaultActionIsMove = false; showBulkVaultSheet = true },
                        protectedCount   = uiState.documents.count { it.id in selectedIds && it.isProtected },
                        allProtected     = uiState.documents.filter { it.id in selectedIds }.let { sel -> sel.isNotEmpty() && sel.all { it.isProtected } },
                        soleEntryTitle   = uiState.documents.filter { it.id in selectedIds && !it.isProtected }
                                               .singleOrNull()?.let { it.holderName.ifBlank { it.docType }.ifBlank { null } },
                        onToggleProtect  = {
                            val sel = uiState.documents.filter { it.id in selectedIds }
                            when {
                                sel.isNotEmpty() && sel.all { it.isProtected } -> showBulkUnlock = true
                                isFreeUser                                     -> showBulkProtectPremium = true
                                else                                           -> viewModel.bulkSetProtected(true)
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        windowInsets = WindowInsets(0),
                        title = {
                            Column {
                                TextoDeUnaLinea(
                                    stringResource(R.string.identity_docs_title),
                                    style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                                    fontWeight = FontWeight.SemiBold,
                                    color      = sectionTitleColor()
                                )
                                if (uiState.documents.isNotEmpty()) {
                                    TextoDeUnaLinea(
                                        text  = if (uiState.documents.size == 1) stringResource(R.string.identity_docs_count_one)
                                                else stringResource(R.string.identity_docs_count, uiState.documents.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.action_back),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            CaptureAllowedBadge()
                            ThemeTrialBadge()
                        },
                        colors = topBarColors()
                    )
                }
            }
        },
        floatingActionButton = {
            val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            Box(Modifier.offset(y = -navBarHeight)) {
                FloatingActionButton(
                    modifier = Modifier
                        .coachMarkAnchor("identity_documents", "fab"),
                    onClick = { if (isSectionReadOnly) showReadOnlyGate = true else onAddDocument() },
                    containerColor = VioletaDelFab,
                    contentColor   = ContenidoDelFab
                ) {
                    FabAddIcon(locked = isSectionReadOnly, contentDescription = stringResource(R.string.action_add))
                }
                if (isSectionReadOnly) com.thormakk.keyarmor.presentation.components.FabLockBadge(Modifier.align(Alignment.TopEnd))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        if (uiState.documents.isEmpty() && uiState.fuzzyDocuments.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier           = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text      = stringResource(R.string.identity_docs_empty_title),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = stringResource(R.string.identity_docs_empty_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { if (isSectionReadOnly) showReadOnlyGate = true else onAddDocument() },
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary),
                        shape  = RoundedCornerShape(8.dp)
                    ) {
                        Icon(if (isSectionReadOnly) Icons.Default.Lock else Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.identity_docs_add_button))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(padding),
                contentPadding      = PaddingValues(start = 7.dp, end = 7.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                itemsIndexed(uiState.documents, key = { _, it -> it.id }) { index, doc ->
                    val isFirst = index == 0
                    val isLast  = index == uiState.documents.lastIndex
                    IdentityDocumentCard(
                        doc             = doc,
                        isFirst         = isFirst,
                        isLast          = isLast,
                        isSelected      = doc.id in selectedIds,
                        isSelectionMode = isSelectionMode,
                        hasAttachment   = doc.id in attachmentIds,
                        onClick         = {
                            if (isSelectionMode) viewModel.toggleSelection(doc.id)
                            else onEditDocument(doc.id)
                        },
                        onLongClick   = { viewModel.toggleSelection(doc.id) },
                        onMenuEdit    = { onEditDocument(doc.id) },
                        onMenuPin     = { viewModel.togglePin(doc) },
                        onMenuArchive = { if (isFreeUser) showArchiveLocked = true else viewModel.archive(doc.id) },
                        onMenuDelete  = { docToDelete = doc },
                        onMenuMoveTo  = { entryActionId = doc.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                        onMenuCopyTo  = { entryActionId = doc.id; entryActionIsMove = false; showEntryVaultSheet = true },
                        isHubPinned   = doc.id in pinnedHubIds,
                        onMenuHubPin  = { viewModel.toggleHubPin(doc.id, HubEntryType.IDENTITY_DOCUMENT) },
                        isReadOnly    = isSectionReadOnly
                    )
                }

                if (uiState.fuzzyDocuments.isNotEmpty()) {
                    item(key = "fuzzy_header") {
                        FuzzySectionDivider(stringResource(R.string.search_similar_results))
                    }
                    itemsIndexed(uiState.fuzzyDocuments, key = { _, it -> "fz_${it.id}" }) { index, doc ->
                        val isFirst = index == 0
                        val isLast  = index == uiState.fuzzyDocuments.lastIndex
                        IdentityDocumentCard(
                            doc             = doc,
                            isFirst         = isFirst,
                            isLast          = isLast,
                            isSelected      = false,
                            isSelectionMode = false,
                            hasAttachment   = doc.id in attachmentIds,
                            onClick         = { onEditDocument(doc.id) },
                            onLongClick     = {},
                            onMenuEdit      = { onEditDocument(doc.id) },
                            onMenuPin       = { viewModel.togglePin(doc) },
                            onMenuArchive   = { if (isFreeUser) showArchiveLocked = true else viewModel.archive(doc.id) },
                            onMenuDelete    = { docToDelete = doc },
                            onMenuMoveTo    = { entryActionId = doc.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                            onMenuCopyTo    = { entryActionId = doc.id; entryActionIsMove = false; showEntryVaultSheet = true },
                            isHubPinned     = doc.id in pinnedHubIds,
                            onMenuHubPin    = { viewModel.toggleHubPin(doc.id, HubEntryType.IDENTITY_DOCUMENT) },
                            isReadOnly      = isSectionReadOnly
                        )
                    }
                }
            }
        }
    }

    docToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.dialog_move_to_trash_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.dialog_delete_entry_named_message, doc.holderName.ifEmpty { doc.docType }),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.softDelete(doc.id); docToDelete = null },
                    shape  = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showBulkUnlock) {
        AlertDialog(
            onDismissRequest = { showBulkUnlock = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_bulk_unlock_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(if (isFreeUser) R.string.protect_bulk_unlock_body_free else R.string.protect_bulk_unlock_body), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showBulkUnlock = false; viewModel.bulkSetProtected(false) }) { Text(stringResource(R.string.lock_action_unlock)) }
            },
            dismissButton = {
                TextButton(onClick = { showBulkUnlock = false }) { Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground) }
            }
        )
    }
    if (showBulkProtectPremium) {
        AlertDialog(
            onDismissRequest = { showBulkProtectPremium = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_premium_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.protect_premium_body), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                TextButton(onClick = { showBulkProtectPremium = false; onPlansClick() }) { Text(stringResource(R.string.plans_upgrade_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { showBulkProtectPremium = false }) { Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground) }
            }
        )
    }

    if (showArchiveLocked) {
        AlertDialog(
            onDismissRequest = { showArchiveLocked = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.premium_gate_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.archive_locked_message_generic), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { showArchiveLocked = false; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveLocked = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showBulkTagSheet) {
        BulkTagSheet(
            availableTags = availableTags.map { it.name },
            onApply   = { mode, tags -> viewModel.bulkUpdateTags(tags, mode); showBulkTagSheet = false },
            onDismiss = { showBulkTagSheet = false }
        )
    }

    if (showBulkCategorySheet) {
        BulkCategorySheet(
            onApply = { mode, category ->
                if (category != null || mode == BulkMode.REMOVE)
                    viewModel.bulkUpdateCategory(category ?: EntryCategory.SIN_CATEGORIZAR, mode)
                showBulkCategorySheet = false
            },
            onDismiss = { showBulkCategorySheet = false }
        )
    }

    if (showBulkVaultSheet) {
        MoveToVaultBottomSheet(
            vaults     = otherVaults,
            isMove     = bulkVaultActionIsMove,
            onSelect   = { targetVaultId ->
                showBulkVaultSheet = false
                if (bulkVaultActionIsMove) viewModel.bulkMoveTo(targetVaultId)
                else                       viewModel.bulkCopyTo(targetVaultId)
            },
            onDismiss  = { showBulkVaultSheet = false }
        )
    }

    if (showEntryVaultSheet) {
        MoveToVaultBottomSheet(
            vaults     = otherVaults,
            isMove     = entryActionIsMove,
            onSelect   = { targetVaultId ->
                showEntryVaultSheet = false
                val id = entryActionId
                if (id.isNotEmpty()) {
                    if (entryActionIsMove) viewModel.moveEntryTo(id, targetVaultId)
                    else                   viewModel.copyEntryTo(id, targetVaultId)
                }
                entryActionId = ""
            },
            onDismiss  = { showEntryVaultSheet = false; entryActionId = "" }
        )
    }
}

@Composable
private fun IdentityDocumentCard(
    doc:             IdentityDocument,
    isFirst:         Boolean = false,
    isLast:          Boolean = false,
    isSelected:      Boolean = false,
    isSelectionMode: Boolean = false,
    hasAttachment:   Boolean = false,
    onClick:         () -> Unit,
    onLongClick:     () -> Unit = {},
    onMenuEdit:      () -> Unit = {},
    onMenuPin:       () -> Unit = {},
    onMenuArchive:   () -> Unit = {},
    onMenuDelete:    () -> Unit = {},
    onMenuMoveTo:    () -> Unit = {},
    onMenuCopyTo:    () -> Unit = {},
    isReadOnly:      Boolean = false,
    isHubPinned:     Boolean = false,
    onMenuHubPin:    () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var protectedNotice by remember { mutableStateOf<Int?>(null) }
    protectedNotice?.let { msgRes ->
        AlertDialog(
            onDismissRequest = { protectedNotice = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_locked_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(msgRes), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { protectedNotice = null }) { Text(stringResource(R.string.action_understood)) }
            }
        )
    }
    val now             = System.currentTimeMillis()
    val daysUntilExpiry = doc.expiryTimestamp?.let {
        ((it - now) / (1000L * 60 * 60 * 24)).toInt()
    }
    val expiryColor: Color? = when {
        daysUntilExpiry == null -> null
        daysUntilExpiry < 0    -> Color(0xFFFFB300)
        daysUntilExpiry < 30   -> Color(0xFFFFB300)
        daysUntilExpiry < 90   -> Color(0xFFFFB300)
        else                   -> Color(0xFF43A047)
    }

    val entryCardColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                         else MaterialTheme.colorScheme.surface

    Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(entryItemShape(isFirst, isLast))
            .background(entryCardColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked         = isSelected,
                onCheckedChange = { onClick() },
                modifier        = Modifier.size(40.dp),
                colors          = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
        } else {
            Box(
                modifier         = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Badge,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(24.dp)
                )
                if (doc.isProtected) {
                    ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
                }
            }
        }
        Spacer(Modifier.width(12.dp))

        Column(
            modifier            = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = doc.docType.uppercase(),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                val middle = buildList {
                    if (doc.issuingCountry.isNotEmpty()) add(doc.issuingCountry)
                    if (doc.number.isNotEmpty()) add(doc.number)
                }.joinToString(" · ")
                if (middle.isNotEmpty()) {
                    Text(
                        text     = " · $middle",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                if (doc.expiryDate.isNotEmpty()) {
                    val expiryPrefix = stringResource(R.string.label_expiry_short)
                    Text(
                        text     = " · $expiryPrefix ${doc.expiryDate}",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = expiryColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Text(
                text       = doc.holderName.ifEmpty { "—" },
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            EntryChipsRow(
                category = doc.category.takeIf { it != EntryCategory.SIN_CATEGORIZAR },
                tags     = doc.tags
            )
        }

        if (doc.isPinned) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.PushPin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        }
        if (hasAttachment) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        }
        if (!isSelectionMode) {
            Box {
                IconButton(
                    onClick  = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.action_more_options),
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor   = MaterialTheme.colorScheme.surface
                ) {
                    if (!isReadOnly) {
                        DropdownMenuItem(
                            text         = { Text(stringResource(R.string.action_edit)) },
                            leadingIcon  = { Icon(Icons.Default.Edit, null) },
                            onClick      = {
                                showMenu = false
                                if (doc.isProtected) protectedNotice = R.string.protect_locked_edit else onMenuEdit()
                            }
                        )
                        DropdownMenuItem(
                            text         = { Text(if (doc.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin)) },
                            leadingIcon  = { Icon(Icons.Default.PushPin, null) },
                            onClick      = { showMenu = false; onMenuPin() }
                        )
                    }
                    DropdownMenuItem(
                        text         = { Text(if (isHubPinned) stringResource(R.string.hub_unpin_from_hub) else stringResource(R.string.hub_pin_to_hub)) },
                        leadingIcon  = { Icon(if (isHubPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, null) },
                        onClick      = { showMenu = false; onMenuHubPin() }
                    )
                    if (!isReadOnly) {
                        DropdownMenuItem(
                            text        = { Text(stringResource(R.string.action_move_to)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, null) },
                            onClick     = { showMenu = false; onMenuMoveTo() }
                        )
                    }
                    DropdownMenuItem(
                        text        = { Text(stringResource(R.string.action_copy_to)) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                        onClick     = { showMenu = false; onMenuCopyTo() }
                    )
                    if (!isReadOnly) {
                        DropdownMenuItem(
                            text         = { Text(stringResource(R.string.action_archive)) },
                            leadingIcon  = { Icon(Icons.Default.Archive, null) },
                            onClick      = { showMenu = false; onMenuArchive() }
                        )
                    }
                    DropdownMenuItem(
                        text         = { Text(stringResource(R.string.action_move_to_trash), color = dangerColor()) },
                        leadingIcon  = { Icon(Icons.Outlined.Delete, null, tint = dangerColor()) },
                        onClick      = {
                            showMenu = false
                            if (doc.isProtected) protectedNotice = R.string.protect_locked_delete else onMenuDelete()
                        }
                    )
                }
            }
        }
    }
    if (!isLast) {
        EntryGroupDivider(
            startInset = 50.dp,
            endInset   = 18.dp,
            cardColor  = entryCardColor,
            gapColor   = MaterialTheme.colorScheme.background
        )
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MoveToVaultBottomSheet(
    vaults:    List<VaultMetadata>,
    isMove:    Boolean,
    onSelect:  (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text       = if (isMove) stringResource(R.string.action_move_to) else stringResource(R.string.action_copy_to),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Text(
            text     = stringResource(R.string.move_copy_select_vault),
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        if (vaults.isEmpty()) {
            Text(text = "—", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp))
        } else {
            vaults.forEach { vault ->
                Row(
                    modifier              = Modifier.fillMaxWidth()
                        .clickable { onSelect(vault.id) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector        = if (vault.isInherited) Icons.Default.Shield
                                             else if (vault.isPrivate) Icons.Default.Lock
                                             else Icons.Default.Folder,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(22.dp)
                    )
                    Text(text = vault.name, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
