@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
package com.thormakk.keyarmor.presentation.screens.vehicle

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
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.PushPin
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.model.Vehicle
import com.thormakk.keyarmor.presentation.components.ChipData
import com.thormakk.keyarmor.presentation.components.ChipsRowWithFunnel
import com.thormakk.keyarmor.presentation.components.EntryChipsRow
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.FuzzySectionDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.VioletaDelFab
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.selection.BulkCategorySheet
import com.thormakk.keyarmor.presentation.components.selection.BulkMode
import com.thormakk.keyarmor.presentation.components.selection.BulkTagSheet
import com.thormakk.keyarmor.presentation.components.selection.SelectionTopBar
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.truncateTag
import com.thormakk.keyarmor.presentation.screens.vault.CategoryChip
import com.thormakk.keyarmor.presentation.screens.vault.ChipTagColor
import com.thormakk.keyarmor.presentation.screens.vault.FiltersSheet
import com.thormakk.keyarmor.presentation.screens.vault.tagIcon
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun VehiclesScreen(
    onBack:         () -> Unit,
    onAddVehicle:   () -> Unit,
    onEditVehicle:  (String) -> Unit,
    onPlansClick:   () -> Unit = {},
    viewModel:      VehiclesViewModel = hiltViewModel()
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    val autoLockManager   = LocalAutoLockManager.current
    val pinLimitReached   by viewModel.pinLimitReached.collectAsStateWithLifecycle()
    val isFreeUser        by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val isSectionReadOnly = isActiveVaultReadOnly || isFreeUser
    val otherVaults        by viewModel.otherVaults.collectAsStateWithLifecycle()
    val availableTags     by viewModel.availableTags.collectAsStateWithLifecycle()
    val topFilterTags     by viewModel.topFilterTags.collectAsStateWithLifecycle()
    val tagCountMap       by viewModel.tagCountMap.collectAsStateWithLifecycle()
    val attachmentIds     by viewModel.attachmentIds.collectAsStateWithLifecycle()
    val selectedIds       by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isSelectionMode   = selectedIds.isNotEmpty()

    var showSortMenu          by remember { mutableStateOf(false) }
    var showSearch            by remember { mutableStateOf(false) }
    val searchFocusRequester  =  remember { FocusRequester() }
    var showFiltersSheet      by remember { mutableStateOf(false) }
    var showBulkTagSheet      by remember { mutableStateOf(false) }
    var showBulkCategorySheet by remember { mutableStateOf(false) }
    var vehicleToDelete       by remember { mutableStateOf<Vehicle?>(null) }
    var showArchiveLocked     by remember { mutableStateOf(false) }

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

    val vehiclePinLimitMsg = stringResource(R.string.vehicle_pin_limit_toast)
    LaunchedEffect(pinLimitReached) {
        if (pinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(vehiclePinLimitMsg) }
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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(MaterialTheme.colorScheme.surface))
                if (isSelectionMode) {
                    SelectionTopBar(
                        selectedCount    = selectedIds.size,
                        totalCount       = uiState.vehicles.size,
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
                        protectedCount   = uiState.vehicles.count { it.id in selectedIds && it.isProtected },
                        allProtected     = uiState.vehicles.filter { it.id in selectedIds }.let { sel -> sel.isNotEmpty() && sel.all { it.isProtected } },
                        soleEntryTitle   = uiState.vehicles.filter { it.id in selectedIds && !it.isProtected }
                                               .singleOrNull()?.let { listOf(it.make, it.model).filter { p -> p.isNotBlank() }.joinToString(" ").ifBlank { null } },
                        onToggleProtect  = {
                            val sel = uiState.vehicles.filter { it.id in selectedIds }
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
                        title = { TextoDeUnaLinea(stringResource(R.string.vehicles_title),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = MaterialTheme.colorScheme.onSurface)
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
                    .coachMarkAnchor("vehicles", "fab"),
                onClick = { if (!isSectionReadOnly) onAddVehicle() },
                containerColor = VioletaDelFab,
                contentColor   = ContenidoDelFab
            ) {
                Icon(
                    if (isSectionReadOnly) Icons.Default.Lock else Icons.Default.Add,
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
                placeholder   = { Text(stringResource(R.string.vehicle_search_placeholder)) },
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

            val activeFilterCount = (if (uiState.selectedCategory != null) 1 else 0) + uiState.selectedTags.size +
                (if (uiState.onlyFavorites) 1 else 0) + (if (uiState.onlyWithAttachments) 1 else 0) +
                (if (uiState.onlyProtected) 1 else 0)

            val sortAzLabel     = stringResource(R.string.sort_az_short)
            val sortZaLabel     = stringResource(R.string.sort_za_short)
            val sortNewestLabel = stringResource(R.string.vault_sort_newest_short)
            val sortOldestLabel = stringResource(R.string.vault_sort_oldest_short)
            val sortLabel = when (uiState.sortOrder) {
                VehicleSortOrder.A_Z    -> sortAzLabel
                VehicleSortOrder.Z_A    -> sortZaLabel
                VehicleSortOrder.NEWEST -> sortNewestLabel
                VehicleSortOrder.OLDEST -> sortOldestLabel
            }

            var renderedChipKeys by remember(topFilterTags) {
                mutableStateOf(
                    buildSet<String> {
                        topFilterTags.forEach { add("chip_tag:$it") }
                    }
                )
            }
            val favoritesLabel   = stringResource(R.string.filter_favorites)
            val attachmentsLabel = stringResource(R.string.filter_attachments)
            val filtersLabel     = stringResource(R.string.action_filters)
            val vehicleChips = buildList {
                topFilterTags.forEach { tagName ->
                    add(ChipData(
                        key           = "chip_tag:$tagName",
                        label         = tagName.truncateTag(),
                        selected      = tagName in uiState.selectedTags,
                        selectedColor = ChipTagColor,
                        onClick       = {
                            val newTags = if (tagName in uiState.selectedTags)
                                uiState.selectedTags - tagName
                            else
                                uiState.selectedTags + tagName
                            viewModel.onTagsApplied(newTags)
                        },
                        count         = tagCountMap[tagName] ?: 0,
                        icon          = tagIcon(tagName)
                    ))
                }
            }
            ChipsRowWithFunnel(
                chips                    = vehicleChips,
                activeFilterCount        = activeFilterCount,
                onFunnelClick            = { showFiltersSheet = true },
                funnelContentDescription = filtersLabel,
                onVisibleKeysChanged     = { renderedChipKeys = it },
                modifier                 = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) { data ->
                CategoryChip(
                    label         = data.label,
                    selected      = data.selected,
                    selectedColor = data.selectedColor,
                    onClick       = data.onClick,
                    leadingIcon   = data.icon,
                    count         = data.count
                )
            }

            val showFavChip    = uiState.onlyFavorites       && "chip_fav" !in renderedChipKeys
            val showAttachChip = uiState.onlyWithAttachments && "chip_att" !in renderedChipKeys
            val showProtChip   = uiState.onlyProtected
            val hiddenTagChips = uiState.selectedTags.filter { "chip_tag:$it" !in renderedChipKeys }
            if (showFavChip || showAttachChip || showProtChip || hiddenTagChips.isNotEmpty()) {
                val activeChipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    selectedLabelColor        = MaterialTheme.colorScheme.primary,
                    selectedTrailingIconColor = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    if (showFavChip) {
                        FilterChip(
                            selected     = true,
                            onClick      = {},
                            label        = { Text(favoritesLabel, style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.onFavoritesToggle(false) })
                            },
                            colors = activeChipColors
                        )
                    }
                    if (showAttachChip) {
                        FilterChip(
                            selected     = true,
                            onClick      = {},
                            label        = { Text(attachmentsLabel, style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.onAttachmentFilterToggle() })
                            },
                            colors = activeChipColors
                        )
                    }
                    if (showProtChip) {
                        FilterChip(
                            selected     = true,
                            onClick      = {},
                            label        = { Text(stringResource(R.string.filter_protected), style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.onProtectedFilterToggle(false) })
                            },
                            colors = activeChipColors
                        )
                    }
                    hiddenTagChips.forEach { tagName ->
                        FilterChip(
                            selected     = true,
                            onClick      = {},
                            label        = { Text(tagName, style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null,
                                    modifier = Modifier.size(16.dp).clickable {
                                        viewModel.onTagsApplied(uiState.selectedTags - tagName)
                                    })
                            },
                            colors = activeChipColors
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = sortHeaderVisible,
                enter   = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit    = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val vehicleCount = uiState.vehicles.size
                Text(
                    text  = if (vehicleCount == 1) stringResource(R.string.vehicle_count_one) else stringResource(R.string.vehicle_count, vehicleCount),
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
                            text  = sortLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
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
                        DropdownMenuItem(
                            text = { Text(sortAzLabel, fontWeight = if (uiState.sortOrder == VehicleSortOrder.A_Z) FontWeight.SemiBold else FontWeight.Normal, color = if (uiState.sortOrder == VehicleSortOrder.A_Z) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground) },
                            onClick = { viewModel.onSortOrderChange(VehicleSortOrder.A_Z); showSortMenu = false },
                            trailingIcon = if (uiState.sortOrder == VehicleSortOrder.A_Z) ({ Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }) else null
                        )
                        DropdownMenuItem(
                            text = { Text(sortZaLabel, fontWeight = if (uiState.sortOrder == VehicleSortOrder.Z_A) FontWeight.SemiBold else FontWeight.Normal, color = if (uiState.sortOrder == VehicleSortOrder.Z_A) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground) },
                            onClick = { viewModel.onSortOrderChange(VehicleSortOrder.Z_A); showSortMenu = false },
                            trailingIcon = if (uiState.sortOrder == VehicleSortOrder.Z_A) ({ Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }) else null
                        )
                        DropdownMenuItem(
                            text = { Text(sortNewestLabel, fontWeight = if (uiState.sortOrder == VehicleSortOrder.NEWEST) FontWeight.SemiBold else FontWeight.Normal, color = if (uiState.sortOrder == VehicleSortOrder.NEWEST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground) },
                            onClick = { viewModel.onSortOrderChange(VehicleSortOrder.NEWEST); showSortMenu = false },
                            trailingIcon = if (uiState.sortOrder == VehicleSortOrder.NEWEST) ({ Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }) else null
                        )
                        DropdownMenuItem(
                            text = { Text(sortOldestLabel, fontWeight = if (uiState.sortOrder == VehicleSortOrder.OLDEST) FontWeight.SemiBold else FontWeight.Normal, color = if (uiState.sortOrder == VehicleSortOrder.OLDEST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground) },
                            onClick = { viewModel.onSortOrderChange(VehicleSortOrder.OLDEST); showSortMenu = false },
                            trailingIcon = if (uiState.sortOrder == VehicleSortOrder.OLDEST) ({ Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }) else null
                        )
                    }
                }
            }
            }

            if (uiState.vehicles.isEmpty() && uiState.fuzzyVehicles.isEmpty()) {
                val hasFilters = uiState.selectedCategory != null || uiState.selectedTags.isNotEmpty()
                val hasQuery   = uiState.query.isNotEmpty()
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
                        Icon(
                            when { hasQuery -> Icons.Default.SearchOff; hasFilters -> Icons.Default.FilterList; else -> Icons.Outlined.DirectionsCar },
                            contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            when { hasQuery -> stringResource(R.string.empty_no_results); hasFilters -> stringResource(R.string.empty_no_filtered_results); else -> stringResource(R.string.vehicles_empty_title) },
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center
                        )
                        if (!hasQuery && !hasFilters) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stringResource(R.string.vehicles_empty_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { if (!isSectionReadOnly) onAddVehicle() },
                                colors  = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary),
                                border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                shape   = RoundedCornerShape(8.dp)
                            ) {
                                Icon(if (isSectionReadOnly) Icons.Default.Lock else Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.vehicles_add_button))
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state               = listState,
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(start = 7.dp, end = 7.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(uiState.vehicles, key = { _, it -> it.id }) { index, vehicle ->
                        val isFirst = index == 0
                        val isLast  = index == uiState.vehicles.lastIndex
                        VehicleItem(
                            vehicle         = vehicle,
                            isFirst         = isFirst,
                            isLast          = isLast,
                            isSelected      = vehicle.id in selectedIds,
                            isSelectionMode = isSelectionMode,
                            hasAttachment   = vehicle.id in attachmentIds,
                            onClick         = { if (isSelectionMode) viewModel.toggleSelection(vehicle.id) else onEditVehicle(vehicle.id) },
                            onLongClick     = { viewModel.toggleSelection(vehicle.id) },
                            onMenuEdit      = { onEditVehicle(vehicle.id) },
                            onMenuPin       = { viewModel.togglePin(vehicle) },
                            onMenuFavorite  = { viewModel.setFavorite(vehicle.id, !vehicle.isFavorite) },
                            onMenuArchive   = { if (isFreeUser) showArchiveLocked = true else viewModel.archive(vehicle.id) },
                            onMenuDelete    = { vehicleToDelete = vehicle },
                            onMenuMoveTo    = { entryActionId = vehicle.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                            onMenuCopyTo    = { entryActionId = vehicle.id; entryActionIsMove = false; showEntryVaultSheet = true },
                            isReadOnly      = isSectionReadOnly,
                            isHubPinned     = vehicle.id in pinnedHubIds,
                            onToggleHubPin  = { viewModel.toggleHubPin(vehicle.id, HubEntryType.VEHICLE) }
                        )
                    }

                    if (uiState.fuzzyVehicles.isNotEmpty()) {
                        item(key = "fuzzy_header") {
                            FuzzySectionDivider(stringResource(R.string.search_similar_results))
                        }
                        itemsIndexed(uiState.fuzzyVehicles, key = { _, it -> "fz_${it.id}" }) { index, vehicle ->
                            val isFirst = index == 0
                            val isLast  = index == uiState.fuzzyVehicles.lastIndex
                            VehicleItem(
                            vehicle = vehicle,
                            isFirst = isFirst,
                            isLast  = isLast,
                            isSelected = false,
                            isSelectionMode = false,
                            hasAttachment = vehicle.id in attachmentIds,
                            onClick        = { onEditVehicle(vehicle.id) },
                            onLongClick    = {},
                            onMenuEdit     = { onEditVehicle(vehicle.id) },
                            onMenuPin      = { viewModel.togglePin(vehicle) },
                            onMenuFavorite = { viewModel.setFavorite(vehicle.id, !vehicle.isFavorite) },
                            onMenuArchive  = { if (isFreeUser) showArchiveLocked = true else viewModel.archive(vehicle.id) },
                            onMenuDelete   = { vehicleToDelete = vehicle },
                            onMenuMoveTo   = { entryActionId = vehicle.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                            onMenuCopyTo   = { entryActionId = vehicle.id; entryActionIsMove = false; showEntryVaultSheet = true },
                            isReadOnly     = isSectionReadOnly,
                            isHubPinned    = vehicle.id in pinnedHubIds,
                            onToggleHubPin = { viewModel.toggleHubPin(vehicle.id, HubEntryType.VEHICLE) }
                        )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }


    if (showBulkTagSheet) {
        BulkTagSheet(availableTags = availableTags.map { it.name }, onApply = { mode, tags -> viewModel.bulkUpdateTags(tags, mode); showBulkTagSheet = false }, onDismiss = { showBulkTagSheet = false })
    }
    if (showBulkCategorySheet) {
        BulkCategorySheet(
            onApply = { mode, category ->
                if (category != null || mode == BulkMode.REMOVE) viewModel.bulkUpdateCategory(category ?: EntryCategory.SIN_CATEGORIZAR, mode)
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
            onProtectedToggle  = { viewModel.onProtectedFilterToggle(it) },
            onApply            = { category, tags, fav, attach ->
                viewModel.onCategorySelected(category)
                viewModel.onTagsApplied(tags)
                viewModel.onFavoritesToggle(fav)
                if (attach != uiState.onlyWithAttachments) viewModel.onAttachmentFilterToggle()
                showFiltersSheet = false
            },
            onClearAll    = { viewModel.onCategorySelected(null); viewModel.onTagsApplied(emptySet()); viewModel.onFavoritesToggle(false); if (uiState.onlyWithAttachments) viewModel.onAttachmentFilterToggle(); viewModel.onProtectedFilterToggle(false); showFiltersSheet = false },
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

    vehicleToDelete?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.dialog_delete_vehicle_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.dialog_delete_vehicle_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { viewModel.softDelete(vehicle.id); vehicleToDelete = null },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }) {
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
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.plans_feature_archive), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            },
            text = { Text(stringResource(R.string.premium_locked_dialog_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { showArchiveLocked = false; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary),
                    border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveLocked = false }) {
                    Text(stringResource(R.string.action_understood), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }
}

@Composable
private fun VehicleItem(
    vehicle:         Vehicle,
    isFirst:         Boolean = false,
    isLast:          Boolean = false,
    isSelected:      Boolean = false,
    isSelectionMode: Boolean = false,
    hasAttachment:   Boolean = false,
    onClick:         () -> Unit,
    onLongClick:     () -> Unit = {},
    onMenuEdit:      () -> Unit = {},
    onMenuPin:       () -> Unit = {},
    onMenuFavorite:  () -> Unit = {},
    onMenuArchive:   () -> Unit = {},
    onMenuDelete:    () -> Unit = {},
    onMenuMoveTo:    () -> Unit = {},
    onMenuCopyTo:    () -> Unit = {},
    isReadOnly:      Boolean = false,
    isHubPinned:     Boolean = false,
    onToggleHubPin:  () -> Unit = {}
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

    val entryCardColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surface

    Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(entryItemShape(isFirst, isLast))
            .background(entryCardColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(checked = isSelected, onCheckedChange = { onClick() }, modifier = Modifier.size(40.dp))
        } else {
            Box(
                modifier         = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                VehicleAwareAvatar(make = vehicle.make, size = 32.dp)
                if (vehicle.isProtected) {
                    ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val title = listOf(vehicle.make, vehicle.model).filter { it.isNotEmpty() }.joinToString(" ").ifEmpty { stringResource(R.string.vehicle_unnamed) }
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (vehicle.licensePlate.isNotEmpty()) {
                Text(text = vehicle.licensePlate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            EntryChipsRow(
                category = vehicle.category.takeIf { it != EntryCategory.SIN_CATEGORIZAR },
                tags     = vehicle.tags
            )
        }
        if (hasAttachment) {
            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(2.dp))
        }
        if (vehicle.isPinned) {
            Icon(Icons.Default.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(2.dp))
        }
        if (!isSelectionMode) {
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.action_more_options),
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor   = MaterialTheme.colorScheme.surface
                ) {
                    if (!isReadOnly) {
                        DropdownMenuItem(
                            text        = { Text(stringResource(R.string.action_edit)) },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick     = {
                                showMenu = false
                                if (vehicle.isProtected) protectedNotice = R.string.protect_locked_edit else onMenuEdit()
                            }
                        )
                        DropdownMenuItem(
                            text        = { Text(if (vehicle.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin)) },
                            leadingIcon = { Icon(Icons.Default.PushPin, null) },
                            onClick     = { showMenu = false; onMenuPin() }
                        )
                    }
                    DropdownMenuItem(
                        text        = { Text(if (isHubPinned) stringResource(R.string.hub_unpin_from_hub) else stringResource(R.string.hub_pin_to_hub)) },
                        leadingIcon = { Icon(if (isHubPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, null, modifier = Modifier.size(18.dp)) },
                        onClick     = { showMenu = false; onToggleHubPin() }
                    )
                    if (!isReadOnly) {
                        DropdownMenuItem(
                            text        = { Text(if (vehicle.isFavorite) stringResource(R.string.action_remove_favorite) else stringResource(R.string.action_add_favorite)) },
                            leadingIcon = { Icon(if (vehicle.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, null) },
                            onClick     = { showMenu = false; onMenuFavorite() }
                        )
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
                            text        = { Text(stringResource(R.string.action_archive)) },
                            leadingIcon = { Icon(Icons.Default.Archive, null) },
                            onClick     = { showMenu = false; onMenuArchive() }
                        )
                    }
                    DropdownMenuItem(
                        text        = { Text(stringResource(R.string.action_move_to_trash), color = dangerColor()) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = dangerColor()) },
                        onClick     = {
                            showMenu = false
                            if (vehicle.isProtected) protectedNotice = R.string.protect_locked_delete else onMenuDelete()
                        }
                    )
                }
            }
        }
    }
    if (!isLast) {
        EntryGroupDivider(
            startInset = 52.dp,
            endInset   = 12.dp,
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
