@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.creditcard

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.VaultMetadata
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
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun CreditCardsScreen(
    onBack:                  () -> Unit,
    onAddCard:               () -> Unit,
    onEditCard:              (String) -> Unit,
    onOpenEdit:              (String) -> Unit = {},
    onPlansClick:            () -> Unit = {},
    viewModel:               CreditCardsViewModel  = hiltViewModel(),
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    val autoLockManager   = LocalAutoLockManager.current
    val pinLimitReached   by viewModel.pinLimitReached.collectAsStateWithLifecycle()
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

    val quickFilters  by viewModel.quickFilters.collectAsStateWithLifecycle()
    val tagCountMap   by viewModel.tagCountMap.collectAsStateWithLifecycle()
    val categoryCountMap by viewModel.categoryCountMap.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val isFreeUser    by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val isSectionReadOnly = isActiveVaultReadOnly || isFreeUser
    val otherVaults    by viewModel.otherVaults.collectAsStateWithLifecycle()

    var showSortMenu     by remember { mutableStateOf(false) }
    var showSearch        by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    val selectedIds   by viewModel.selectedIds.collectAsStateWithLifecycle()
    val attachmentIds by viewModel.attachmentIds.collectAsStateWithLifecycle()
    val isSelectionMode = selectedIds.isNotEmpty()
    var showBulkTagSheet      by remember { mutableStateOf(false) }
    var showBulkUnlock        by remember { mutableStateOf(false) }
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
                        totalCount       = uiState.cards.size,
                        onClearSelection = viewModel::clearSelection,
                        onSelectAll      = viewModel::selectAll,
                        onArchive        = viewModel::bulkArchive,
                        isArchiveLocked  = isFreeUser,
                        onArchiveLocked  = { showArchiveLockedDialog = true },
                        onDelete         = viewModel::bulkDelete,
                        onTag            = { showBulkTagSheet = true },
                        onCategorize     = { showBulkCategorySheet = true },
                        isReadOnly       = isSectionReadOnly,
                        onMoveTo         = { bulkVaultActionIsMove = true;  showBulkVaultSheet = true },
                        onCopyTo         = { bulkVaultActionIsMove = false; showBulkVaultSheet = true },
                        protectedCount   = uiState.cards.count { it.id in selectedIds && it.isProtected },
                        allProtected     = uiState.cards.filter { it.id in selectedIds }.let { sel -> sel.isNotEmpty() && sel.all { it.isProtected } },
                        soleEntryTitle   = uiState.cards.filter { it.id in selectedIds && !it.isProtected }
                                               .singleOrNull()?.let { it.brand.ifBlank { null } },
                        onToggleProtect  = {
                            val sel = uiState.cards.filter { it.id in selectedIds }
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
                            TextoDeUnaLinea(
                                stringResource(R.string.card_screen_title),
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
                    .coachMarkAnchor("credit_cards", "fab"),
                onClick = { if (!isSectionReadOnly) onAddCard() },
                containerColor = VioletaDelFab,
                contentColor   = ContenidoDelFab
            ) {
                Icon(
                    imageVector        = if (isSectionReadOnly) Icons.Default.Lock else Icons.Default.Add,
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
                placeholder   = { Text(stringResource(R.string.card_search_placeholder)) },
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
                CardFilterChip(
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
                CardSortOrder.A_Z    -> stringResource(R.string.vault_sort_az)
                CardSortOrder.Z_A    -> stringResource(R.string.vault_sort_za)
                CardSortOrder.NEWEST -> stringResource(R.string.vault_sort_newest_short)
                CardSortOrder.OLDEST -> stringResource(R.string.vault_sort_oldest_short)
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
                val cardCount = uiState.cards.size
                Text(
                    text  = if (cardCount == 1) stringResource(R.string.card_count_one)
                            else stringResource(R.string.card_count, cardCount),
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
                        CardSortOption(stringResource(R.string.vault_sort_az),     uiState.sortOrder == CardSortOrder.A_Z)    { viewModel.onSortOrderChange(CardSortOrder.A_Z);    showSortMenu = false }
                        CardSortOption(stringResource(R.string.vault_sort_za),     uiState.sortOrder == CardSortOrder.Z_A)    { viewModel.onSortOrderChange(CardSortOrder.Z_A);    showSortMenu = false }
                        CardSortOption(stringResource(R.string.vault_sort_newest), uiState.sortOrder == CardSortOrder.NEWEST) { viewModel.onSortOrderChange(CardSortOrder.NEWEST); showSortMenu = false }
                        CardSortOption(stringResource(R.string.vault_sort_oldest), uiState.sortOrder == CardSortOrder.OLDEST) { viewModel.onSortOrderChange(CardSortOrder.OLDEST); showSortMenu = false }
                    }
                }
            }
            }

            if (uiState.cards.isEmpty() && uiState.fuzzyCards.isEmpty()) {
                val hasFilters = uiState.selectedCategory != null || uiState.selectedTags.isNotEmpty()
                val hasQuery   = uiState.query.isNotEmpty()
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
                        Icon(
                            imageVector        = when {
                                hasQuery   -> Icons.Default.SearchOff
                                hasFilters -> Icons.Default.FilterList
                                else       -> Icons.Default.CreditCard
                            },
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier           = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text  = when {
                                hasQuery   -> stringResource(R.string.vault_empty_search)
                                hasFilters -> stringResource(R.string.vault_empty_filtered)
                                else       -> stringResource(R.string.card_empty_title)
                            },
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        if (!hasQuery && !hasFilters) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { if (!isSectionReadOnly) onAddCard() },
                                colors  = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor   = MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                shape  = RoundedCornerShape(8.dp)
                            ) {
                                Icon(if (isSectionReadOnly) Icons.Default.Lock else Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.action_add))
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state          = listState,
                    modifier       = Modifier.fillMaxSize().padding(horizontal = 7.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(uiState.cards, key = { _, it -> it.id }) { index, card ->
                        val isFirst = index == 0
                        val isLast  = index == uiState.cards.lastIndex
                        CreditCardItem(
                            card                     = card,
                            isFirst                  = isFirst,
                            isLast                   = isLast,
                            isSelected               = card.id in selectedIds,
                            isSelectionMode          = isSelectionMode,
                            hasAttachment            = card.id in attachmentIds,
                            onClick                  = { if (isSelectionMode) viewModel.toggleSelection(card.id) else onEditCard(card.id) },
                            onLongClick              = { viewModel.toggleSelection(card.id) },
                            onEditClick              = { onOpenEdit(card.id) },
                            onPinToggle              = { viewModel.togglePin(card) },
                            onFavoriteToggle         = { viewModel.setFavorite(card.id, !card.isFavorite) },
                            onArchive                = { viewModel.archiveItem(card.id) },
                            isArchiveLocked          = isFreeUser,
                            isReadOnly               = isSectionReadOnly,
                            onDelete                 = { viewModel.softDeleteItem(card.id) },
                            onMoveTo                 = { entryActionId = card.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                            onCopyTo                 = { entryActionId = card.id; entryActionIsMove = false; showEntryVaultSheet = true },
                            sharedTransitionScope    = sharedTransitionScope,
                            animatedVisibilityScope  = animatedVisibilityScope
                        )
                    }

                    if (uiState.fuzzyCards.isNotEmpty()) {
                        item(key = "fuzzy_header") {
                            FuzzySectionDivider(stringResource(R.string.search_similar_results))
                        }
                        itemsIndexed(uiState.fuzzyCards, key = { _, it -> "fz_${it.id}" }) { index, card ->
                            val isFirst = index == 0
                            val isLast  = index == uiState.fuzzyCards.lastIndex
                            CreditCardItem(
                            card = card,
                            isFirst = isFirst,
                            isLast  = isLast,
                            isSelected = false,
                            isSelectionMode = false,
                            hasAttachment = card.id in attachmentIds,
                            onClick = { onEditCard(card.id) },
                            onLongClick = {},
                            onEditClick      = { onOpenEdit(card.id) },
                            onPinToggle      = { viewModel.togglePin(card) },
                            onFavoriteToggle = { viewModel.setFavorite(card.id, !card.isFavorite) },
                            onArchive        = { viewModel.archiveItem(card.id) },
                            isArchiveLocked  = isFreeUser,
                            isReadOnly       = isSectionReadOnly,
                            onDelete         = { viewModel.softDeleteItem(card.id) },
                            onMoveTo         = { entryActionId = card.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                            onCopyTo         = { entryActionId = card.id; entryActionIsMove = false; showEntryVaultSheet = true },
                            isHubPinned      = card.id in pinnedHubIds,
                            onToggleHubPin   = { viewModel.toggleHubPin(card.id, HubEntryType.CREDIT_CARD) },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
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

    if (showFiltersSheet) {
        FiltersSheet(
            availableTags      = availableTags,
            selectedCategory   = uiState.selectedCategory,
            selectedTags       = uiState.selectedTags,
            initialFavorites   = uiState.onlyFavorites,
            initialAttachments = uiState.onlyWithAttachments,
            initialProtected   = uiState.onlyProtected,
            onProtectedToggle  = { v -> viewModel.onProtectedFilterToggle(v) },
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
            isReadOnly    = isSectionReadOnly
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
private fun CardFilterChip(
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
        color           = if (selected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
        contentColor    = if (selected) Color.White else MaterialTheme.colorScheme.onBackground,
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
private fun CardSortOption(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun CreditCardItem(
    card:                    CreditCard,
    isFirst:                 Boolean = false,
    isLast:                  Boolean = false,
    isSelected:              Boolean = false,
    isSelectionMode:         Boolean = false,
    hasAttachment:           Boolean = false,
    onClick:                 () -> Unit,
    onLongClick:             () -> Unit = {},
    onEditClick:             () -> Unit = {},
    onPinToggle:             () -> Unit = {},
    onFavoriteToggle:        () -> Unit = {},
    onArchive:               () -> Unit = {},
    isArchiveLocked:         Boolean = false,
    isReadOnly:              Boolean = false,
    onDelete:                () -> Unit = {},
    onMoveTo:                () -> Unit = {},
    onCopyTo:                () -> Unit = {},
    isHubPinned:             Boolean = false,
    onToggleHubPin:          () -> Unit = {},
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked         = isSelected,
                onCheckedChange = { onClick() },
                modifier        = Modifier.size(40.dp)
            )
        } else {
            val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier.sharedElement(
                        sharedContentState      = rememberSharedContentState("card_avatar_${card.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            } else Modifier
            Box(
                modifier         = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CreditCardAwareAvatar(
                    brand    = card.brand,
                    size     = 40.dp,
                    modifier = avatarModifier
                )
                if (card.isProtected) {
                    ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
                }
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val last4  = card.cardNumber.takeLast(4).takeIf { card.cardNumber.length >= 4 }
            val title  = when {
                card.brand.isNotEmpty() && last4 != null -> "${card.brand}  \u2026$last4"
                card.brand.isNotEmpty()                  -> card.brand
                card.issuer.isNotEmpty()                 -> card.issuer
                else                                     -> stringResource(R.string.card_default_name)
            }
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
            if (card.issuer.isNotEmpty()) {
                Text(
                    text  = card.issuer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EntryChipsRow(
                category = card.category.takeIf { it != EntryCategory.SIN_CATEGORIZAR },
                tags     = card.tags
            )
        }
        if (hasAttachment) {
            Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(2.dp))
        }
        if (card.isPinned) {
            Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(2.dp))
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_options), tint = MaterialTheme.colorScheme.primary)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = MaterialTheme.colorScheme.surface) {
                if (!isReadOnly) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_edit)) }, onClick = { showMenu = false; if (card.isProtected) protectedNotice = R.string.protect_locked_edit else onEditClick() }, leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) })
                    DropdownMenuItem(text = { Text(if (card.isFavorite) stringResource(R.string.action_remove_favorite) else stringResource(R.string.action_favorite)) }, onClick = { showMenu = false; onFavoriteToggle() }, leadingIcon = { Icon(if (card.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, null, modifier = Modifier.size(18.dp)) })
                    DropdownMenuItem(text = { Text(if (card.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin)) }, onClick = { showMenu = false; onPinToggle() }, leadingIcon = { Icon(Icons.Default.PushPin, null, modifier = Modifier.size(18.dp)) })
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
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_delete), color = dangerColor()) }, onClick = { showMenu = false; if (card.isProtected) protectedNotice = R.string.protect_locked_delete else showDeleteConfirm = true }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = dangerColor(), modifier = Modifier.size(18.dp)) })
            }
        }
    }
    if (!isLast) {
        EntryGroupDivider(
            startInset = 64.dp,
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
