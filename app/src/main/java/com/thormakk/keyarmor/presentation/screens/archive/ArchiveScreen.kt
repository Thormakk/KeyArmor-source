@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.archive

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.screens.vault.ProtectedLockBadge
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.ArchiveItem
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.vault.CategoryChip
import com.thormakk.keyarmor.presentation.screens.vault.ChipCategoryColor
import com.thormakk.keyarmor.presentation.screens.vault.SortOrder
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.highlightText
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import androidx.compose.ui.graphics.lerp as lerpColor
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.TextoQueSeAjusta
import com.thormakk.keyarmor.presentation.components.justified

private val archiveHighlightMutex = Mutex()

private val ARCHIVE_TYPE_LABEL_MAP = mapOf(
    "Password"    to R.string.archive_type_password,
    "BankAccount" to R.string.archive_type_bank,
    "Note"        to R.string.archive_type_note,
    "Custom"      to R.string.archive_type_custom,
    "Card"        to R.string.archive_type_card,
    "TwoFactor"   to R.string.archive_type_twofactor,
    "CryptoWallet" to R.string.archive_type_crypto
)

@Composable
fun ArchiveScreen(
    onBack:       () -> Unit,
    scrollToId:   String?    = null,
    onPlansClick: () -> Unit = {},
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val isLoading           by viewModel.isLoading.collectAsStateWithLifecycle()
    val isFreeUser          by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val items               by viewModel.archiveItems.collectAsStateWithLifecycle()
    val filteredItems       by viewModel.filteredItems.collectAsStateWithLifecycle()
    val searchQuery         by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTypes       by viewModel.selectedTypes.collectAsStateWithLifecycle()
    val selectedCategories  by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val selectedTags        by viewModel.selectedTags.collectAsStateWithLifecycle()
    val onlyProtected       by viewModel.onlyProtected.collectAsStateWithLifecycle()
    val sortOrder           by viewModel.sortOrder.collectAsStateWithLifecycle()
    val availableTypes      by viewModel.availableTypes.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val availableTags       by viewModel.availableTags.collectAsStateWithLifecycle()
    val selectedIds         by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isSelectionMode      = selectedIds.isNotEmpty()
    val autoLockManager = LocalAutoLockManager.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val listState        = rememberLazyListState()
    var viewportHeightPx by remember { mutableIntStateOf(0) }
    val highlightPulse   = remember { Animatable(0f) }

    val capturedScrollToId = remember { scrollToId }

    LaunchedEffect(Unit) {
        if (capturedScrollToId == null) return@LaunchedEffect
        val consumed = viewModel.consumeScrollHighlight()
        if (!consumed) return@LaunchedEffect

        while (filteredItems.isEmpty()) delay(50)

        val index = filteredItems.indexOfFirst { it.id == capturedScrollToId }
        if (index < 0) return@LaunchedEffect

        delay(100)
        listState.scrollToItem(index)
        delay(50)

        val itemInfo    = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
        val viewportMid = viewportHeightPx / 2
        if (itemInfo != null && (itemInfo.offset + itemInfo.size / 2) > viewportMid) {
            val offset = -(viewportMid - itemInfo.size / 2)
            listState.animateScrollToItem(index, scrollOffset = offset)
        }

        val deadline = System.currentTimeMillis() + 2_000L
        while (lifecycleOwner.lifecycle.currentState < Lifecycle.State.RESUMED) {
            if (System.currentTimeMillis() > deadline) return@LaunchedEffect
            delay(50)
        }
        delay(150)

        archiveHighlightMutex.withLock {
            repeat(2) {
                highlightPulse.snapTo(1f)
                highlightPulse.animateTo(0f, animationSpec = tween(500))
                if (it == 0) delay(180)
            }
        }
    }

    BackHandler(enabled = isSelectionMode) { viewModel.clearSelection() }

    var showOptionsSheet       by remember { mutableStateOf(false) }
    var showFiltersSheet       by remember { mutableStateOf(false) }
    var showSortMenu           by remember { mutableStateOf(false) }
    var showUnarchiveAllDialog by remember { mutableStateOf(false) }
    var showMoveAllDialog      by remember { mutableStateOf(false) }
    var pendingTrash           by remember { mutableStateOf<ArchiveItem?>(null) }
    var protectedSkippedCount  by remember { mutableStateOf<Int?>(null) }
    var protectedDeleteNotice  by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.protectedSkipped.collect { protectedSkippedCount = it } }
    protectedSkippedCount?.let { n ->
        AlertDialog(
            onDismissRequest = { protectedSkippedCount = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_locked_title), fontWeight = FontWeight.Bold) },
            text  = { Text(pluralStringResource(R.plurals.protect_bulk_skipped, n, n)) },
            confirmButton = {
                TextButton(onClick = { protectedSkippedCount = null }) { Text(stringResource(R.string.action_understood)) }
            }
        )
    }

    val activeFilterCount = selectedTypes.size + selectedCategories.size + selectedTags.size +
        (if (onlyProtected) 1 else 0)

    if (showFiltersSheet) {
        ArchiveFiltersSheet(
            availableTypes      = availableTypes,
            availableCategories = availableCategories,
            availableTags       = availableTags,
            selectedTypes       = selectedTypes,
            selectedCategories  = selectedCategories,
            selectedTags        = selectedTags,
            onlyProtected       = onlyProtected,
            onApply             = { types, cats, tags, protectedOnly ->
                viewModel.applyFilters(types, cats, tags, protectedOnly)
                showFiltersSheet = false
            },
            onClearAll          = {
                viewModel.applyFilters(emptySet(), emptySet(), emptySet(), false)
                showFiltersSheet = false
            },
            onDismiss           = { showFiltersSheet = false }
        )
    }

    if (showOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            containerColor   = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (items.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showOptionsSheet = false
                                showUnarchiveAllDialog = true
                            }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Unarchive,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text  = stringResource(R.string.archive_options_unarchive_all),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showOptionsSheet = false
                                showMoveAllDialog = true
                            }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint     = dangerColor(),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text  = stringResource(R.string.archive_options_move_all_to_trash),
                            style = MaterialTheme.typography.bodyLarge,
                            color = dangerColor()
                        )
                    }
                }
            }
        }
    }

    if (showUnarchiveAllDialog) {
        AlertDialog(
            onDismissRequest = { showUnarchiveAllDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.archive_confirm_unarchive_all_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.archive_confirm_unarchive_all_message)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.unarchiveAll(); showUnarchiveAllDialog = false },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        stringResource(R.string.archive_unarchive),
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnarchiveAllDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showMoveAllDialog) {
        AlertDialog(
            onDismissRequest = { showMoveAllDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.archive_confirm_move_all_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.archive_confirm_move_all_message), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = { viewModel.moveAllToTrash(); showMoveAllDialog = false },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.archive_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveAllDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (protectedDeleteNotice) {
        AlertDialog(
            onDismissRequest = { protectedDeleteNotice = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_locked_title), fontWeight = FontWeight.Bold) },
            text  = { Text(stringResource(R.string.protect_locked_delete_archive), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                TextButton(onClick = { protectedDeleteNotice = false }) { Text(stringResource(R.string.action_understood)) }
            }
        )
    }

    pendingTrash?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingTrash = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.archive_confirm_trash_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.archive_confirm_trash_message), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = { viewModel.moveToTrash(item); pendingTrash = null },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.archive_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingTrash = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                if (isSelectionMode) {
                    ArchiveSelectionTopBar(
                        selectedCount    = selectedIds.size,
                        totalCount       = filteredItems.size,
                        onClearSelection = viewModel::clearSelection,
                        onSelectAll      = viewModel::selectAll,
                        onUnarchive      = viewModel::bulkUnarchive,
                        onMoveToTrash    = viewModel::bulkMoveToTrash
                    )
                } else {
                    TopAppBar(
                        windowInsets = WindowInsets(0),
                        title = {
                            TextoDeUnaLinea(
                                text       = stringResource(R.string.archive_title),
                                style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                                fontWeight = FontWeight.SemiBold,
                                color      = sectionTitleColor()
                            )
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
                            IconButton(onClick = { showOptionsSheet = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.action_options),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = topBarColors()
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onGloballyPositioned { viewportHeightPx = it.size.height }
        ) {
            TextField(
                value         = searchQuery,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.setSearchQuery(it) },
                placeholder   = { Text(stringResource(R.string.archive_search_placeholder)) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                },
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
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

            if (items.isNotEmpty()) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val count = filteredItems.size
                    Text(
                        text  = if (count == 1)
                            stringResource(R.string.vault_entry_count_one)
                        else
                            stringResource(R.string.vault_entry_count, count),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    CategoryChip(
                        label    = if (activeFilterCount > 0)
                            stringResource(R.string.filter_short_with_count, activeFilterCount)
                        else
                            stringResource(R.string.filter_short),
                        selected = activeFilterCount > 0,
                        onClick  = { showFiltersSheet = true }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    val sortLabel = when (sortOrder) {
                        SortOrder.A_Z    -> stringResource(R.string.vault_sort_az)
                        SortOrder.Z_A    -> stringResource(R.string.vault_sort_za)
                        SortOrder.NEWEST -> stringResource(R.string.vault_sort_newest_short)
                        SortOrder.OLDEST -> stringResource(R.string.vault_sort_oldest_short)
                    }
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
                                imageVector        = if (showSortMenu) Icons.Default.ArrowDropUp
                                                     else Icons.Default.ArrowDropDown,
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
                            ArchiveSortOption(
                                label    = stringResource(R.string.vault_sort_newest),
                                selected = sortOrder == SortOrder.NEWEST,
                                onClick  = { viewModel.setSortOrder(SortOrder.NEWEST); showSortMenu = false }
                            )
                            ArchiveSortOption(
                                label    = stringResource(R.string.vault_sort_oldest),
                                selected = sortOrder == SortOrder.OLDEST,
                                onClick  = { viewModel.setSortOrder(SortOrder.OLDEST); showSortMenu = false }
                            )
                            ArchiveSortOption(
                                label    = stringResource(R.string.vault_sort_az),
                                selected = sortOrder == SortOrder.A_Z,
                                onClick  = { viewModel.setSortOrder(SortOrder.A_Z); showSortMenu = false }
                            )
                            ArchiveSortOption(
                                label    = stringResource(R.string.vault_sort_za),
                                selected = sortOrder == SortOrder.Z_A,
                                onClick  = { viewModel.setSortOrder(SortOrder.Z_A); showSortMenu = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when {
                isLoading -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                items.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier            = Modifier.coachMarkAnchor("archive", "empty"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Archive,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier           = Modifier.size(72.dp)
                            )
                            Text(
                                text       = stringResource(R.string.archive_empty_title),
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = stringResource(R.string.archive_empty_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                filteredItems.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier           = Modifier.size(64.dp)
                            )
                            Text(
                                text       = stringResource(R.string.archive_no_results),
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text(stringResource(R.string.action_clear_all))
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state               = listState,
                        modifier            = Modifier
                            .fillMaxSize()
                            .coachMarkAnchor("archive", "restore"),
                        contentPadding      = PaddingValues(horizontal = 7.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        if (isFreeUser) {
                            item(key = "free_banner") {
                                ArchiveFreeBanner(onPlansClick = onPlansClick)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                        itemsIndexed(filteredItems, key = { _, it -> it.id + it::class.simpleName }) { index, item ->
                            ArchiveItemRow(
                                item            = item,
                                isFirst         = index == 0,
                                isLast          = index == filteredItems.lastIndex,
                                isSelected      = item.id in selectedIds,
                                isSelectionMode = isSelectionMode,
                                highlightAlpha  = if (item.id == capturedScrollToId) highlightPulse.value else 0f,
                                onLongClick     = { viewModel.toggleSelection(item.id) },
                                onClick         = { if (isSelectionMode) viewModel.toggleSelection(item.id) },
                                onUnarchive     = { viewModel.unarchive(item) },
                                onMoveToTrash   = { if (item.isProtected) protectedDeleteNotice = true else pendingTrash = item }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveFreeBanner(onPlansClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onPlansClick)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null,
                tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.archive_free_banner_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(stringResource(R.string.archive_free_banner_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
            Text(stringResource(R.string.premium_gate_cta),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                tint     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ArchiveSelectionTopBar(
    selectedCount:    Int,
    totalCount:       Int,
    onClearSelection: () -> Unit,
    onSelectAll:      () -> Unit,
    onUnarchive:      () -> Unit,
    onMoveToTrash:    () -> Unit
) {
    val allSelected = selectedCount >= totalCount && totalCount > 0
    var showMenu              by remember { mutableStateOf(false) }
    var showMoveTrashConfirm  by remember { mutableStateOf(false) }

    if (showMoveTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showMoveTrashConfirm = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text       = stringResource(R.string.archive_bulk_trash_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text  = if (selectedCount == 1) stringResource(R.string.archive_bulk_delete_confirm_message_one)
                            else stringResource(R.string.archive_bulk_delete_confirm_message_other, selectedCount),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showMoveTrashConfirm = false; onMoveToTrash() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.archive_bulk_move_confirm_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showMoveTrashConfirm = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    Column {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                TextoQueSeAjusta(
                    text  = stringResource(R.string.bulk_selected_count, selectedCount),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = SectionTitleFontSize),
                    color = sectionTitleColor()
                )
            },
            navigationIcon = {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
                }
            },
            actions = {
                CaptureAllowedBadge()
                ThemeTrialBadge()
                IconButton(onClick = onUnarchive) {
                    Icon(
                        Icons.Default.Unarchive,
                        contentDescription = stringResource(R.string.archive_unarchive),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showMoveTrashConfirm = true }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.archive_move_to_trash),
                        tint = dangerColor()
                    )
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor   = MaterialTheme.colorScheme.surface
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(if (allSelected) R.string.bulk_deselect_all else R.string.bulk_select_all),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.SelectAll,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {
                            showMenu = false
                            if (allSelected) onClearSelection() else onSelectAll()
                        }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor             = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                titleContentColor          = MaterialTheme.colorScheme.primary,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor     = MaterialTheme.colorScheme.onSurface,
            )
        )
        HorizontalDivider(
            thickness = 1.dp,
            color     = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun ArchiveFiltersSheet(
    availableTypes:      Set<String>,
    availableCategories: Set<EntryCategory>,
    availableTags:       List<String>,
    selectedTypes:       Set<String>,
    selectedCategories:  Set<EntryCategory>,
    selectedTags:        Set<String>,
    onlyProtected:       Boolean,
    onApply:             (Set<String>, Set<EntryCategory>, Set<String>, Boolean) -> Unit,
    onClearAll:          () -> Unit,
    onDismiss:           () -> Unit
) {
    var pendingTypes      by remember { mutableStateOf(selectedTypes) }
    var pendingCategories by remember { mutableStateOf(selectedCategories) }
    var pendingTags       by remember { mutableStateOf(selectedTags) }
    var pendingProtected  by remember { mutableStateOf(onlyProtected) }
    var tagSearch         by remember { mutableStateOf("") }

    val filteredTags = availableTags.filter {
        tagSearch.isBlank() || it.contains(tagSearch, ignoreCase = true)
    }
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasAnyPending = pendingTypes.isNotEmpty() || pendingCategories.isNotEmpty() || pendingTags.isNotEmpty() || pendingProtected
    val autoLockManager = LocalAutoLockManager.current

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
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = stringResource(R.string.filter_title),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground,
                    modifier   = Modifier.weight(1f)
                )
                if (hasAnyPending) {
                    TextButton(onClick = {
                        pendingTypes      = emptySet()
                        pendingCategories = emptySet()
                        pendingTags       = emptySet()
                        pendingProtected  = false
                        onClearAll()
                    }) {
                        Text(
                            text  = stringResource(R.string.action_clear_all),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            if (availableTypes.isNotEmpty()) {
                Text(
                    text       = stringResource(R.string.filter_section_type),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
                )
                FlowRow(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    ARCHIVE_TYPE_LABEL_MAP.entries
                        .filter { it.key in availableTypes }
                        .forEach { (typeKey, labelRes) ->
                            CategoryChip(
                                label         = stringResource(labelRes),
                                selected      = typeKey in pendingTypes,
                                selectedColor = ChipCategoryColor,
                                onClick       = {
                                    pendingTypes = if (typeKey in pendingTypes)
                                        pendingTypes - typeKey
                                    else
                                        pendingTypes + typeKey
                                }
                            )
                        }
                    CategoryChip(
                        label         = stringResource(R.string.filter_protected),
                        selected      = pendingProtected,
                        selectedColor = ChipCategoryColor,
                        onClick       = { pendingProtected = !pendingProtected }
                    )
                }
            }

            if (availableCategories.isNotEmpty()) {
                Text(
                    text       = stringResource(R.string.filter_section_categories),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp)
                )
                FlowRow(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    EntryCategory.entries
                        .filter { it in availableCategories }
                        .forEach { cat ->
                            CategoryChip(
                                label         = stringResource(cat.labelRes),
                                selected      = cat in pendingCategories,
                                selectedColor = ChipCategoryColor,
                                onClick       = {
                                    pendingCategories = if (cat in pendingCategories)
                                        pendingCategories - cat
                                    else
                                        pendingCategories + cat
                                }
                            )
                        }
                }
            }

            if (availableTags.isNotEmpty()) {
                Text(
                    text       = stringResource(R.string.filter_section_tags),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp)
                )
                if (availableTags.size > 5) {
                    OutlinedTextField(
                        value         = tagSearch,
                        onValueChange = { autoLockManager?.recordInteraction(); tagSearch = it },
                        placeholder   = { Text(stringResource(R.string.vault_search_tags)) },
                        leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon  = {
                            if (tagSearch.isNotEmpty()) {
                                IconButton(onClick = { tagSearch = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                                }
                            }
                        },
                        modifier   = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape      = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Column(modifier = Modifier.heightIn(max = 168.dp).verticalScroll(rememberScrollState())) {
                filteredTags.forEach { tagName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                pendingTags = pendingTags.toMutableSet().apply {
                                    if (contains(tagName)) remove(tagName) else add(tagName)
                                }
                            }
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = tagName in pendingTags,
                            onCheckedChange = {
                                pendingTags = pendingTags.toMutableSet().apply {
                                    if (it) add(tagName) else remove(tagName)
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text     = highlightText(tagName, tagSearch, MaterialTheme.colorScheme.primary),
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick  = { onApply(pendingTypes, pendingCategories, pendingTags, pendingProtected) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape  = RoundedCornerShape(8.dp),
                colors = appButtonColors()
            ) {
                Text(
                    text  = stringResource(R.string.action_apply),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ArchiveSortOption(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) MaterialTheme.colorScheme.primary
                            else         MaterialTheme.colorScheme.onSurface
                )
                if (selected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        onClick = onClick
    )
}

@Composable
private fun ArchiveItemRow(
    item:            ArchiveItem,
    isFirst:         Boolean = false,
    isLast:          Boolean = false,
    isSelected:      Boolean = false,
    isSelectionMode: Boolean = false,
    highlightAlpha:  Float   = 0f,
    onLongClick:     () -> Unit = {},
    onClick:         () -> Unit = {},
    onUnarchive:     () -> Unit,
    onMoveToTrash:   () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val hiColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    val bgColor = when {
        isSelected          -> MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        highlightAlpha > 0f -> lerpColor(surface, hiColor, highlightAlpha)
        else                -> surface
    }
    Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(entryItemShape(isFirst, isLast))
            .background(bgColor)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = item.icon(),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(24.dp)
                )
                if (item.isProtected) ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = item.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text  = item.typeLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!isSelectionMode) Box {
            var expanded by remember { mutableStateOf(false) }

            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector        = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.action_options),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false },
                containerColor   = MaterialTheme.colorScheme.surface
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text  = stringResource(R.string.archive_unarchive),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = { expanded = false; onUnarchive() }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text  = stringResource(R.string.archive_move_to_trash),
                            style = MaterialTheme.typography.bodyLarge,
                            color = dangerColor()
                        )
                    },
                    onClick = { expanded = false; onMoveToTrash() }
                )
            }
        }
    }
    if (!isLast) {
        EntryGroupDivider(
            startInset = 38.dp,
            endInset   = 26.dp,
            cardColor  = bgColor,
            gapColor   = MaterialTheme.colorScheme.background
        )
    }
    }
}

private fun ArchiveItem.icon(): ImageVector = when (this) {
    is ArchiveItem.Password         -> Icons.Default.Lock
    is ArchiveItem.BankAccount      -> Icons.Default.AccountBalance
    is ArchiveItem.Note             -> Icons.Default.Description
    is ArchiveItem.Custom           -> Icons.Default.Category
    is ArchiveItem.Card             -> Icons.Default.CreditCard
    is ArchiveItem.TwoFactor        -> Icons.Default.VpnKey
    is ArchiveItem.CryptoWallet     -> Icons.Default.CurrencyBitcoin
    is ArchiveItem.IdentityDocument -> Icons.Default.Badge
    is ArchiveItem.Vehicle          -> Icons.Default.DirectionsCar
    is ArchiveItem.Passkey          -> Icons.Default.Key
}

@Composable
private fun ArchiveItem.typeLabel(): String = when (this) {
    is ArchiveItem.Password         -> stringResource(R.string.archive_type_password)
    is ArchiveItem.BankAccount      -> stringResource(R.string.archive_type_bank)
    is ArchiveItem.Note             -> stringResource(R.string.archive_type_note)
    is ArchiveItem.Custom           -> stringResource(R.string.archive_type_custom)
    is ArchiveItem.Card             -> stringResource(R.string.archive_type_card)
    is ArchiveItem.TwoFactor        -> stringResource(R.string.archive_type_twofactor)
    is ArchiveItem.CryptoWallet     -> stringResource(R.string.archive_type_crypto)
    is ArchiveItem.IdentityDocument -> stringResource(R.string.archive_type_identity_document)
    is ArchiveItem.Vehicle          -> stringResource(R.string.archive_type_vehicle)
    is ArchiveItem.Passkey          -> stringResource(R.string.archive_type_passkey)
}