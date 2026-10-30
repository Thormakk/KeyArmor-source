@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.notes

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.presentation.components.ChipData
import com.thormakk.keyarmor.presentation.components.ChipsRowWithFunnel
import com.thormakk.keyarmor.presentation.components.EntryChipsRow
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.FuzzySectionDivider
import com.thormakk.keyarmor.presentation.components.VioletaDelFab
import com.thormakk.keyarmor.presentation.components.chipCategoryColor
import com.thormakk.keyarmor.presentation.components.chipTagColor
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.notesAccentColor
import com.thormakk.keyarmor.presentation.components.selection.BulkCategorySheet
import com.thormakk.keyarmor.presentation.components.selection.BulkMode
import com.thormakk.keyarmor.presentation.components.selection.BulkTagSheet
import com.thormakk.keyarmor.presentation.components.selection.SelectionTopBar
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.truncateTag
import com.thormakk.keyarmor.presentation.screens.vault.ActiveFilterChip
import com.thormakk.keyarmor.presentation.screens.vault.ChipCategoryColor
import com.thormakk.keyarmor.presentation.screens.vault.ChipTagColor
import com.thormakk.keyarmor.presentation.screens.vault.FiltersSheet
import com.thormakk.keyarmor.presentation.screens.vault.ProtectedLockBadge
import com.thormakk.keyarmor.presentation.screens.vault.QuickFilter
import com.thormakk.keyarmor.presentation.screens.vault.tagIcon
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.TextoQueSeAjusta
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun SecureNotesScreen(
    onBack:     () -> Unit,
    onAddNote:  () -> Unit,
    onEditNote: (String) -> Unit,
    onOpenEdit: (String) -> Unit = {},
    onPlansClick:      () -> Unit = {},
    viewModel:              SecureNotesViewModel   = hiltViewModel(),
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val autoLockManager = LocalAutoLockManager.current
    val quickFilters  by viewModel.quickFilters.collectAsStateWithLifecycle()
    val tagCountMap   by viewModel.tagCountMap.collectAsStateWithLifecycle()
    val categoryCountMap by viewModel.categoryCountMap.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val isFreeUser    by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    var protectedSkippedCount by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { viewModel.protectedSkipped.collect { protectedSkippedCount = it } }
    val protectedSkippedMsg = protectedSkippedCount?.let { pluralStringResource(R.plurals.protect_bulk_skipped, it, it) }
    LaunchedEffect(protectedSkippedCount) {
        if (protectedSkippedMsg != null) {
            protectedSkippedCount = null
            snackbarHostState.showSnackbar(protectedSkippedMsg)
        }
    }
    val pinLimitReached      by viewModel.pinLimitReached.collectAsStateWithLifecycle()
    val globalHistoryEnabled by viewModel.globalHistoryEnabled.collectAsStateWithLifecycle()
    val pinMsg            = stringResource(R.string.pin_limit_reached)
    LaunchedEffect(pinLimitReached) {
        if (pinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(pinMsg) }
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

    var showSortMenu          by remember { mutableStateOf(false) }
    var showSearch             by remember { mutableStateOf(false) }
    var showOptionsMenu        by remember { mutableStateOf(false) }
    var showDisableHistoryDialog by remember { mutableStateOf(false) }
    var showDeleteAllHistoryDialog by remember { mutableStateOf(false) }
    var showBulkUnlock          by remember { mutableStateOf(false) }
    var showBulkProtectPremium  by remember { mutableStateOf(false) }

    var menuHistoryEnabled by remember { mutableStateOf(globalHistoryEnabled) }
    LaunchedEffect(showOptionsMenu) {
        if (showOptionsMenu) menuHistoryEnabled = globalHistoryEnabled
    }
    val historyDeletedMsg      = stringResource(R.string.note_history_deleted)
    val searchFocusRequester = remember { FocusRequester() }

    val selectedIds   by viewModel.selectedIds.collectAsStateWithLifecycle()
    val attachmentIds by viewModel.attachmentIds.collectAsStateWithLifecycle()
    val isSelectionMode = selectedIds.isNotEmpty()
    val otherVaults    by viewModel.otherVaults.collectAsStateWithLifecycle()
    var showBulkTagSheet      by remember { mutableStateOf(false) }
    var showBulkCategorySheet by remember { mutableStateOf(false) }
    var bulkVaultActionIsMove  by remember { mutableStateOf(true) }
    var showBulkVaultSheet     by remember { mutableStateOf(false) }
    var entryActionId          by remember { mutableStateOf("") }
    var entryActionIsMove      by remember { mutableStateOf(true) }
    var showEntryVaultSheet    by remember { mutableStateOf(false) }
    var showArchiveLockedDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = isSelectionMode) { viewModel.clearSelection() }
    BackHandler(enabled = showSearch) { showSearch = false; viewModel.onQueryChange("") }
    LaunchedEffect(showSearch) { if (showSearch) searchFocusRequester.requestFocus() }
    if (showDisableHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showDisableHistoryDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.note_history_disable_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.note_history_disable_confirm_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setGlobalHistoryEnabled(false)
                        viewModel.deleteAllNoteHistory()
                        showDisableHistoryDialog = false
                        scope.launch { snackbarHostState.showSnackbar(historyDeletedMsg) }
                    },
                    shape  = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.note_history_disable_and_delete)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setGlobalHistoryEnabled(false)
                    showDisableHistoryDialog = false
                }) {
                    Text(stringResource(R.string.note_history_disable_only), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showDeleteAllHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllHistoryDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.note_history_delete_all_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.note_history_delete_all_confirm_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllNoteHistory()
                        showDeleteAllHistoryDialog = false
                        scope.launch { snackbarHostState.showSnackbar(historyDeletedMsg) }
                    },
                    shape  = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllHistoryDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    val listState = rememberLazyListState()
    var sortHeaderVisible by remember { mutableStateOf(true) }
    LaunchedEffect(listState) {
        var prevIndex  = listState.firstVisibleItemIndex
        var prevOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                sortHeaderVisible = when {
                    index == 0          -> true
                    index < prevIndex   -> true
                    index > prevIndex   -> false
                    offset < prevOffset -> true
                    offset > prevOffset -> false
                    else                -> sortHeaderVisible
                }
                prevIndex  = index
                prevOffset = offset
            }
    }
    var showFiltersSheet by remember { mutableStateOf(false) }

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
                        totalCount       = uiState.notes.size,
                        onClearSelection = viewModel::clearSelection,
                        onSelectAll      = viewModel::selectAll,
                        onArchive        = viewModel::bulkArchive,
                        isArchiveLocked  = isFreeUser,
                        onArchiveLocked  = { showArchiveLockedDialog = true },
                        onDelete         = viewModel::bulkDelete,
                        onTag            = { showBulkTagSheet = true },
                        onCategorize     = { showBulkCategorySheet = true },
                        isReadOnly       = isActiveVaultReadOnly,
                        onMoveTo         = { bulkVaultActionIsMove = true;  showBulkVaultSheet = true },
                        onCopyTo         = { bulkVaultActionIsMove = false; showBulkVaultSheet = true },
                        protectedCount   = uiState.notes.count { it.id in selectedIds && it.isProtected },
                        allProtected     = uiState.notes.filter { it.id in selectedIds }.let { sel -> sel.isNotEmpty() && sel.all { it.isProtected } },
                        soleEntryTitle   = uiState.notes.filter { it.id in selectedIds && !it.isProtected }
                                               .singleOrNull()?.let { it.title.ifBlank { null } },
                        onToggleProtect  = {
                            val sel = uiState.notes.filter { it.id in selectedIds }
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
                            TextoQueSeAjusta(
                                stringResource(R.string.notes_screen_title),
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
                            IconButton(onClick = {
                                showSearch = !showSearch
                                if (!showSearch) viewModel.onQueryChange("")
                            }) {
                                Icon(
                                    imageVector        = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Box {
                                IconButton(onClick = { showOptionsMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = stringResource(R.string.action_options),
                                        tint               = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DropdownMenu(
                                    expanded         = showOptionsMenu,
                                    onDismissRequest = { showOptionsMenu = false },
                                    containerColor   = MaterialTheme.colorScheme.surface
                                ) {
                                    DropdownMenuItem(
                                        text        = {
                                            Text(
                                                if (menuHistoryEnabled) stringResource(R.string.note_history_global_disable)
                                                else stringResource(R.string.note_history_global_enable)
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp)) },
                                        onClick     = {
                                            showOptionsMenu = false
                                            if (menuHistoryEnabled) showDisableHistoryDialog = true
                                            else viewModel.setGlobalHistoryEnabled(true)
                                        }
                                    )
                                    if (menuHistoryEnabled) {
                                        HorizontalDivider(thickness = 0.5.dp)
                                        DropdownMenuItem(
                                            text        = { Text(stringResource(R.string.note_history_delete_all), color = dangerColor()) },
                                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = dangerColor(), modifier = Modifier.size(18.dp)) },
                                            onClick     = { showOptionsMenu = false; showDeleteAllHistoryDialog = true }
                                        )
                                    }
                                }
                            }
                        },
                        colors = topBarColors()
                    )
                }
            }
        },
        floatingActionButton = {
            val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            FloatingActionButton(
                modifier = Modifier
                    .offset(y = -navBarHeight)
                    .coachMarkAnchor("secure_notes", "fab"),
                onClick = { if (!isActiveVaultReadOnly) onAddNote() },
                containerColor = VioletaDelFab,
                contentColor   = ContenidoDelFab
            ) {
                Icon(
                    imageVector        = if (isActiveVaultReadOnly) Icons.Default.Lock else Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_add)
                )
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

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            AnimatedVisibility(
                visible = showSearch,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
            TextField(
                value         = uiState.query,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onQueryChange(it) },
                placeholder   = { Text(stringResource(R.string.notes_search_placeholder)) },
                leadingIcon   = {
                    Icon(Icons.Default.Search, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon  = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .focusRequester(searchFocusRequester),
                shape      = RoundedCornerShape(50),
                singleLine = true,
                colors     = TextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor  = Color.Transparent,
                )
            )
            }

            val activeFilterCount =
                (if (uiState.selectedCategory != null) 1 else 0) + uiState.selectedTags.size +
                (if (uiState.onlyFavorites) 1 else 0) + (if (uiState.onlyWithAttachments) 1 else 0) +
                (if (uiState.onlyProtected) 1 else 0)

            var renderedChipKeys by remember(quickFilters) { mutableStateOf(quickFilters.map { it.key }.toSet()) }
            val filterChips = quickFilters.map { qf ->
                when (qf) {
                    is QuickFilter.Category -> ChipData(
                        key           = qf.key,
                        label         = stringResource(qf.category.labelRes),
                        selected      = uiState.selectedCategory == qf.category,
                        selectedColor = ChipCategoryColor,
                        onClick       = { viewModel.onCategorySelected(if (uiState.selectedCategory == qf.category) null else qf.category) },
                        count         = categoryCountMap[qf.category] ?: 0
                    )
                    is QuickFilter.Tag -> ChipData(
                        key           = qf.key,
                        label         = qf.tag.truncateTag(),
                        selected      = uiState.selectedTags.contains(qf.tag),
                        selectedColor = ChipTagColor,
                        onClick       = { viewModel.onTagToggled(qf.tag) },
                        count         = tagCountMap[qf.tag] ?: 0,
                        icon          = tagIcon(qf.tag)
                    )
                }
            }
            ChipsRowWithFunnel(
                chips                    = filterChips,
                activeFilterCount        = activeFilterCount,
                onFunnelClick            = { showFiltersSheet = true },
                funnelContentDescription = stringResource(R.string.filter_short),
                onVisibleKeysChanged     = { renderedChipKeys = it },
                modifier                 = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) { data ->
                NotesCategoryChip(
                    label         = data.label,
                    selected      = data.selected,
                    selectedColor = data.selectedColor,
                    onClick       = data.onClick,
                    leadingIcon   = data.icon,
                    count         = data.count
                )
            }

            val visibleQuickKeys = renderedChipKeys
            val hiddenActiveFilters: List<QuickFilter> = buildList {
                uiState.selectedCategory?.let { cat ->
                    val qf = QuickFilter.Category(cat)
                    if (qf.key !in visibleQuickKeys) add(qf)
                }
                uiState.selectedTags.forEach { tag ->
                    val qf = QuickFilter.Tag(tag)
                    if (qf.key !in visibleQuickKeys) add(qf)
                }
            }
            val hasAnyActiveFilter = hiddenActiveFilters.isNotEmpty() ||
                uiState.onlyFavorites || uiState.onlyWithAttachments || uiState.onlyProtected
            if (hasAnyActiveFilter) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.onlyFavorites) {
                        ActiveFilterChip(
                            label       = stringResource(R.string.filter_favorites),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onRemove    = { viewModel.onFavoritesToggle(false) }
                        )
                    }
                    if (uiState.onlyWithAttachments) {
                        ActiveFilterChip(
                            label       = stringResource(R.string.filter_attachments),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onRemove    = { viewModel.onAttachmentFilterToggle() }
                        )
                    }
                    if (uiState.onlyProtected) {
                        ActiveFilterChip(
                            label       = stringResource(R.string.filter_protected),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onRemove    = { viewModel.onProtectedFilterToggle(false) }
                        )
                    }
                    hiddenActiveFilters.forEach { qf ->
                        ActiveFilterChip(
                            label       = when (qf) {
                                is QuickFilter.Category -> stringResource(qf.category.labelRes)
                                is QuickFilter.Tag      -> qf.tag.truncateTag()
                            },
                            accentColor = when (qf) {
                                is QuickFilter.Category -> chipCategoryColor()
                                is QuickFilter.Tag      -> chipTagColor()
                            },
                            onRemove    = {
                                when (qf) {
                                    is QuickFilter.Category -> viewModel.onCategorySelected(null)
                                    is QuickFilter.Tag      -> viewModel.onTagToggled(qf.tag)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val sortLabel = when (uiState.sortOrder) {
                NotesSortOrder.A_Z    -> stringResource(R.string.vault_sort_az)
                NotesSortOrder.Z_A    -> stringResource(R.string.vault_sort_za)
                NotesSortOrder.NEWEST -> stringResource(R.string.vault_sort_newest_short)
                NotesSortOrder.OLDEST -> stringResource(R.string.vault_sort_oldest_short)
            }
            AnimatedVisibility(
                visible = sortHeaderVisible,
                enter   = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit    = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val noteCount = uiState.notes.size
                Text(
                    text  = if (noteCount == 1) stringResource(R.string.notes_count_one)
                            else stringResource(R.string.notes_count, noteCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    TextButton(
                        onClick        = { showSortMenu = true },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text       = sortLabel,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector        = if (showSortMenu) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded         = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        containerColor   = MaterialTheme.colorScheme.surface
                    ) {
                        NotesSortOption(stringResource(R.string.vault_sort_az),     uiState.sortOrder == NotesSortOrder.A_Z)    { viewModel.onSortOrderChange(NotesSortOrder.A_Z);    showSortMenu = false }
                        NotesSortOption(stringResource(R.string.vault_sort_za),     uiState.sortOrder == NotesSortOrder.Z_A)    { viewModel.onSortOrderChange(NotesSortOrder.Z_A);    showSortMenu = false }
                        NotesSortOption(stringResource(R.string.vault_sort_newest), uiState.sortOrder == NotesSortOrder.NEWEST) { viewModel.onSortOrderChange(NotesSortOrder.NEWEST); showSortMenu = false }
                        NotesSortOption(stringResource(R.string.vault_sort_oldest), uiState.sortOrder == NotesSortOrder.OLDEST) { viewModel.onSortOrderChange(NotesSortOrder.OLDEST); showSortMenu = false }
                    }
                }
            }
            }

            if (uiState.notes.isEmpty() && uiState.fuzzyNotes.isEmpty()) {
                val hasFilters = uiState.selectedCategory != null || uiState.selectedTags.isNotEmpty()
                val hasQuery   = uiState.query.isNotEmpty()
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
                        Icon(
                            imageVector        = when {
                                hasQuery   -> Icons.Default.SearchOff
                                hasFilters -> Icons.Default.FilterList
                                else       -> Icons.Default.Description
                            },
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier           = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text  = when {
                                hasQuery   -> stringResource(R.string.vault_empty_search)
                                hasFilters -> stringResource(R.string.notes_empty_filtered)
                                else       -> stringResource(R.string.notes_empty_title)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        if (!hasQuery && !hasFilters) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { if (!isActiveVaultReadOnly) onAddNote() },
                                colors  = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor   = MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                shape  = RoundedCornerShape(8.dp)
                            ) {
                                Icon(if (isActiveVaultReadOnly) Icons.Default.Lock else Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.action_add))
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state               = listState,
                    modifier            = Modifier.fillMaxSize().padding(horizontal = 7.dp),
                    contentPadding      = PaddingValues(top = 4.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(uiState.notes, key = { _, it -> it.id }) { index, note ->
                        val isFirst = index == 0
                        val isLast  = index == uiState.notes.lastIndex
                        SecureNoteItem(
                            note            = note,
                            isFirst         = isFirst,
                            isLast          = isLast,
                            isSelected      = note.id in selectedIds,
                            isSelectionMode = isSelectionMode,
                            hasAttachment   = note.id in attachmentIds,
                            onClick         = { if (isSelectionMode) viewModel.toggleSelection(note.id) else onEditNote(note.id) },
                            onLongClick     = { viewModel.toggleSelection(note.id) },
                            onEditClick     = { onOpenEdit(note.id) },
                            onPinToggle     = { viewModel.togglePin(note) },
                            onFavoriteToggle = { viewModel.setFavorite(note.id, !note.isFavorite) },
                            onArchive       = { viewModel.archiveItem(note.id) },
                            isArchiveLocked = isFreeUser,
                            isReadOnly      = isActiveVaultReadOnly,
                            onDelete        = { viewModel.softDeleteItem(note.id) },
                            onMoveTo        = { entryActionId = note.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                            onCopyTo        = { entryActionId = note.id; entryActionIsMove = false; showEntryVaultSheet = true },
                            isHubPinned     = note.id in pinnedHubIds,
                            onToggleHubPin  = { viewModel.toggleHubPin(note.id, HubEntryType.SECURE_NOTE) }
                        )
                    }

                    if (uiState.fuzzyNotes.isNotEmpty()) {
                        item(key = "fuzzy_header") {
                            FuzzySectionDivider(stringResource(R.string.search_similar_results))
                        }
                        itemsIndexed(uiState.fuzzyNotes, key = { _, it -> "fz_${it.id}" }) { index, note ->
                            val isFirst = index == 0
                            val isLast  = index == uiState.fuzzyNotes.lastIndex
                            SecureNoteItem(
                            note = note,
                            isFirst = isFirst,
                            isLast  = isLast,
                            isSelected = false,
                            isSelectionMode = false,
                            hasAttachment = note.id in attachmentIds,
                            onClick = { onEditNote(note.id) },
                            onLongClick = {},
                            onEditClick     = { onOpenEdit(note.id) },
                            onPinToggle     = { viewModel.togglePin(note) },
                            onFavoriteToggle = { viewModel.setFavorite(note.id, !note.isFavorite) },
                            onArchive       = { viewModel.archiveItem(note.id) },
                            isArchiveLocked = isFreeUser,
                            isReadOnly      = isActiveVaultReadOnly,
                            onDelete        = { viewModel.softDeleteItem(note.id) },
                            onMoveTo        = { entryActionId = note.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                            onCopyTo        = { entryActionId = note.id; entryActionIsMove = false; showEntryVaultSheet = true },
                            isHubPinned     = note.id in pinnedHubIds,
                            onToggleHubPin  = { viewModel.toggleHubPin(note.id, HubEntryType.SECURE_NOTE) }
                        )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showFiltersSheet) {
        FiltersSheet(
            availableTags      = availableTags,
            selectedCategory   = uiState.selectedCategory,
            selectedTags       = uiState.selectedTags,
            initialFavorites   = uiState.onlyFavorites,
            initialAttachments = uiState.onlyWithAttachments,
            initialProtected   = uiState.onlyProtected,
            onProtectedToggle  = { viewModel.onProtectedFilterToggle(it) },
            onApply            = { category, tags, fav, attach ->
                viewModel.onCategorySelected(category)
                viewModel.onTagsApplied(tags)
                viewModel.onFavoritesToggle(fav)
                if (attach != uiState.onlyWithAttachments) viewModel.onAttachmentFilterToggle()
                showFiltersSheet = false
            },
            onClearAll = {
                viewModel.onCategorySelected(null)
                viewModel.onTagsApplied(emptySet())
                viewModel.onFavoritesToggle(false)
                if (uiState.onlyWithAttachments) viewModel.onAttachmentFilterToggle()
                viewModel.onProtectedFilterToggle(false)
                showFiltersSheet = false
            },
            onDismiss      = { showFiltersSheet = false },
            onUpgradeClick = onPlansClick,
            onAddTag       = { viewModel.addTag(it) },
            onRenameTag   = { id, name -> viewModel.renameTag(id, name) },
            onDeleteTag   = { viewModel.deleteTag(it) },
            tagConflictKey = viewModel::tagConflictKey,
            canCreateTags = !isFreeUser,
            isReadOnly    = isActiveVaultReadOnly
        )
    }

    if (showBulkTagSheet) {
        BulkTagSheet(
            availableTags = availableTags.map { it.name },
            onApply = { mode, tags ->
                viewModel.bulkUpdateTags(tags, mode)
                showBulkTagSheet = false
            },
            onDismiss = { showBulkTagSheet = false }
        )
    }
    if (showBulkCategorySheet) {
        BulkCategorySheet(
            onApply = { mode, category ->
                if (category != null || mode == BulkMode.REMOVE) {
                    viewModel.bulkUpdateCategory(
                        category ?: EntryCategory.SIN_CATEGORIZAR,
                        mode
                    )
                }
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
                    Text(stringResource(R.string.action_understood),
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }
}

@Composable
private fun NotesCategoryChip(
    label:         String,
    selected:      Boolean,
    onClick:       () -> Unit,
    selectedColor: Color        = ChipCategoryColor,
    leadingIcon:   ImageVector? = null,
    count:         Int          = 0
) {
    Surface(
        onClick         = onClick,
        shape           = RoundedCornerShape(8.dp),
        color           = if (selected) selectedColor
                          else MaterialTheme.colorScheme.surfaceVariant,
        contentColor    = if (selected) Color.White
                          else MaterialTheme.colorScheme.onBackground,
        tonalElevation  = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            if (leadingIcon != null) {
                Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign  = TextAlign.Center,
                maxLines   = 1
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text  = "($count)",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun NotesSortOption(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (selected) MaterialTheme.colorScheme.primary
                             else MaterialTheme.colorScheme.onBackground
            )
        },
        onClick      = onClick,
        trailingIcon = if (selected) ({
            Icon(Icons.Default.Check, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }) else null
    )
}

@Composable
private fun SecureNoteItem(
    note:            SecureNote,
    isFirst:         Boolean = false,
    isLast:          Boolean = false,
    isSelected:      Boolean = false,
    isSelectionMode: Boolean = false,
    hasAttachment:   Boolean = false,
    onClick:         () -> Unit,
    onLongClick:     () -> Unit = {},
    onEditClick:     () -> Unit = {},
    onPinToggle:     () -> Unit = {},
    onFavoriteToggle: () -> Unit = {},
    onArchive:       () -> Unit = {},
    isArchiveLocked: Boolean = false,
    isReadOnly:      Boolean = false,
    onDelete:        () -> Unit = {},
    onMoveTo:        () -> Unit = {},
    onCopyTo:        () -> Unit = {},
    isHubPinned:     Boolean = false,
    onToggleHubPin:  () -> Unit = {}
) {
    var showMenu          by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var protectedNotice   by remember { mutableStateOf<Int?>(null) }

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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.archive_confirm_trash_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.bulk_delete_confirm_single), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    val entryCardColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                         else MaterialTheme.colorScheme.surface

    Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(entryItemShape(isFirst, isLast))
            .background(entryCardColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked         = isSelected,
                onCheckedChange = { onClick() },
                modifier        = Modifier.size(40.dp)
            )
        } else {
            Box(
                modifier         = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Outlined.StickyNote2,
                    contentDescription = null,
                    tint               = notesAccentColor(),
                    modifier           = Modifier.size(24.dp)
                )
                if (note.isProtected) {
                    ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = note.title.ifEmpty { stringResource(R.string.notes_default_title) },
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            val preview = remember(note.content) { note.content.firstLinePreview() }
            if (preview.isNotEmpty()) {
                Text(
                    text     = preview,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            EntryChipsRow(
                category = note.category.takeIf { it != EntryCategory.SIN_CATEGORIZAR },
                tags     = note.tags
            )
        }
        if (hasAttachment) {
            Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(2.dp))
        }
        if (note.isPinned) {
            Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(2.dp))
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_options), tint = MaterialTheme.colorScheme.primary)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = MaterialTheme.colorScheme.surface) {
                if (!isReadOnly) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_edit)) }, onClick = { showMenu = false; if (note.isProtected) protectedNotice = R.string.protect_locked_edit else onEditClick() }, leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) })
                    DropdownMenuItem(text = { Text(if (note.isFavorite) stringResource(R.string.action_remove_favorite) else stringResource(R.string.action_favorite)) }, onClick = { showMenu = false; onFavoriteToggle() }, leadingIcon = { Icon(if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, null, modifier = Modifier.size(18.dp)) })
                    DropdownMenuItem(text = { Text(if (note.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin)) }, onClick = { showMenu = false; onPinToggle() }, leadingIcon = { Icon(Icons.Default.PushPin, null, modifier = Modifier.size(18.dp)) })
                }
                DropdownMenuItem(
                    text        = { Text(if (isHubPinned) stringResource(R.string.hub_unpin_from_hub) else stringResource(R.string.hub_pin_to_hub)) },
                    onClick     = { showMenu = false; onToggleHubPin() },
                    leadingIcon = { Icon(if (isHubPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, null, modifier = Modifier.size(18.dp)) }
                )
                if (!isReadOnly) {
                    HorizontalDivider(thickness = 0.5.dp)
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_move_to)) }, onClick = { showMenu = false; onMoveTo() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, null, modifier = Modifier.size(18.dp)) })
                }
                DropdownMenuItem(text = { Text(stringResource(R.string.action_copy_to)) }, onClick = { showMenu = false; onCopyTo() }, leadingIcon = { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp)) })
                if (!isReadOnly) {
                    HorizontalDivider(thickness = 0.5.dp)
                    DropdownMenuItem(text = { Text(stringResource(R.string.menu_archive)) }, onClick = { showMenu = false; onArchive() }, enabled = !isArchiveLocked, leadingIcon = { Icon(Icons.Default.Archive, null, modifier = Modifier.size(18.dp)) })
                }
                HorizontalDivider(thickness = 0.5.dp)
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_delete), color = dangerColor()) }, onClick = { showMenu = false; if (note.isProtected) protectedNotice = R.string.protect_locked_delete else showDeleteConfirm = true }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = dangerColor(), modifier = Modifier.size(18.dp)) })
            }
        }
    }
    if (!isLast) {
        EntryGroupDivider(
            startInset = 52.dp,
            endInset   = 28.dp,
            cardColor  = entryCardColor,
            gapColor   = MaterialTheme.colorScheme.background
        )
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MoveToVaultBottomSheet(
    vaults:    List<com.thormakk.keyarmor.domain.model.VaultMetadata>,
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

private fun String.firstLinePreview(): String {
    val text = android.text.Html.fromHtml(this, android.text.Html.FROM_HTML_MODE_COMPACT)
        .toString()
    return text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }
        ?: text.trim()
}
