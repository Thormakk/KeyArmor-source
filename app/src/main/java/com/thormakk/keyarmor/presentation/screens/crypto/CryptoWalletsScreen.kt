@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.crypto

import androidx.compose.foundation.interaction.MutableInteractionSource

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.FuzzySectionDivider
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.thormakk.keyarmor.data.prefs.CryptoPortfolioPrefs
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.presentation.components.ChipData
import com.thormakk.keyarmor.presentation.components.ChipsRowWithFunnel
import com.thormakk.keyarmor.presentation.components.EntryChipsRow
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.VioletaDelFab
import com.thormakk.keyarmor.presentation.components.chipCategoryColor
import com.thormakk.keyarmor.presentation.components.chipTagColor
import com.thormakk.keyarmor.presentation.components.selection.BulkCategorySheet
import com.thormakk.keyarmor.presentation.components.selection.BulkMode
import com.thormakk.keyarmor.presentation.components.selection.BulkTagSheet
import com.thormakk.keyarmor.presentation.components.selection.SelectionTopBar
import com.thormakk.keyarmor.presentation.screens.vault.ProtectedLockBadge
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.truncateTag
import com.thormakk.keyarmor.presentation.screens.vault.ActiveFilterChip
import com.thormakk.keyarmor.presentation.screens.vault.ChipCategoryColor
import com.thormakk.keyarmor.presentation.screens.vault.ChipTagColor
import com.thormakk.keyarmor.presentation.screens.vault.FiltersSheet
import com.thormakk.keyarmor.presentation.screens.vault.QuickFilter
import com.thormakk.keyarmor.presentation.screens.vault.tagIcon
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun CryptoWalletsScreen(
    onBack:       () -> Unit,
    onAddWallet:  () -> Unit,
    onEditWallet: (String) -> Unit,
    onPlansClick:            () -> Unit = {},
    viewModel:               CryptoWalletsViewModel = hiltViewModel(),
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val pedirConsentimientoSaldos by viewModel.showBalanceConsent.collectAsStateWithLifecycle()
    val saldosEnVivoActivos      by viewModel.isBalanceConsentGranted.collectAsStateWithLifecycle()
    var mostrarMenuCripto by remember { mutableStateOf(false) }
    val alternarSaldosEnVivo: (Boolean) -> Unit = { activar ->
        if (activar) viewModel.onEnableLiveBalanceRequested() else viewModel.setBalanceConsent(false)
    }

    if (pedirConsentimientoSaldos) {
        AlertDialog(
            onDismissRequest = { viewModel.onBalanceConsentDismissed() },
            title = { Text(stringResource(R.string.crypto_balance_consent_title)) },
            text  = { Text(stringResource(R.string.crypto_balance_consent_body), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = { viewModel.onBalanceConsent(true) },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.crypto_balance_consent_allow), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onBalanceConsent(false) }) {
                    Text(stringResource(R.string.crypto_balance_consent_deny))
                }
            }
        )
    }
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

    val quickFilters  by viewModel.quickFilters.collectAsStateWithLifecycle()
    val tagCountMap   by viewModel.tagCountMap.collectAsStateWithLifecycle()
    val categoryCountMap by viewModel.categoryCountMap.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val isFreeUser    by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val otherVaults    by viewModel.otherVaults.collectAsStateWithLifecycle()

    var showSortMenu     by remember { mutableStateOf(false) }
    var showSearch        by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    val selectedIds   by viewModel.selectedIds.collectAsStateWithLifecycle()
    val attachmentIds by viewModel.attachmentIds.collectAsStateWithLifecycle()
    val isSelectionMode = selectedIds.isNotEmpty()
    var showBulkTagSheet      by remember { mutableStateOf(false) }
    var showBulkCategorySheet by remember { mutableStateOf(false) }
    var bulkVaultActionIsMove  by remember { mutableStateOf(true) }
    var showBulkVaultSheet     by remember { mutableStateOf(false) }
    var showArchiveLockedDialog by remember { mutableStateOf(false) }
    var showBulkUnlock          by remember { mutableStateOf(false) }
    var showBulkProtectPremium  by remember { mutableStateOf(false) }
    var protectedSkippedCount by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { viewModel.protectedSkipped.collect { protectedSkippedCount = it } }
    val protectedSkippedMsg = protectedSkippedCount?.let { pluralStringResource(R.plurals.protect_bulk_skipped, it, it) }
    LaunchedEffect(protectedSkippedCount) {
        if (protectedSkippedMsg != null) {
            protectedSkippedCount = null
            snackbarHostState.showSnackbar(protectedSkippedMsg)
        }
    }
    BackHandler(enabled = isSelectionMode) { viewModel.clearSelection() }
    BackHandler(enabled = showSearch) { showSearch = false; viewModel.onQueryChange("") }
    LaunchedEffect(showSearch) { if (showSearch) searchFocusRequester.requestFocus() }
    var showFiltersSheet by remember { mutableStateOf(false) }

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
                        totalCount       = uiState.wallets.size,
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
                        protectedCount   = uiState.wallets.count { it.id in selectedIds && it.isProtected },
                        allProtected     = uiState.wallets.filter { it.id in selectedIds }.let { sel -> sel.isNotEmpty() && sel.all { it.isProtected } },
                        soleEntryTitle   = uiState.wallets.filter { it.id in selectedIds && !it.isProtected }
                                               .singleOrNull()?.let { it.asset.ifEmpty { it.network }.ifBlank { null } },
                        onToggleProtect  = {
                            val sel = uiState.wallets.filter { it.id in selectedIds }
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
                                stringResource(R.string.crypto_screen_title),
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
                                IconButton(onClick = { mostrarMenuCripto = true }) {
                                    Icon(
                                        imageVector        = Icons.Default.MoreVert,
                                        contentDescription = stringResource(R.string.crypto_menu_more),
                                        tint               = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DropdownMenu(
                                    expanded         = mostrarMenuCripto,
                                    onDismissRequest = { mostrarMenuCripto = false },
                                    containerColor   = MaterialTheme.colorScheme.surface
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.crypto_balance_onchain_toggle)) },
                                        onClick = {
                                            alternarSaldosEnVivo(!saldosEnVivoActivos)
                                            mostrarMenuCripto = false
                                        },
                                        trailingIcon = {
                                            Switch(
                                                checked         = saldosEnVivoActivos,
                                                onCheckedChange = {
                                                    alternarSaldosEnVivo(it)
                                                    mostrarMenuCripto = false
                                                }
                                            )
                                        }
                                    )
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
                    .coachMarkAnchor("crypto_wallets", "fab"),
                onClick = { if (!isActiveVaultReadOnly) onAddWallet() },
                containerColor = VioletaDelFab,
                contentColor   = ContenidoDelFab
            ) {
                Icon(
                    imageVector        = if (isActiveVaultReadOnly) Icons.Default.Lock else Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_add)
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                placeholder   = { Text(stringResource(R.string.crypto_search_placeholder)) },
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
                CryptoFilterChip(
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

            AnimatedVisibility(
                visible = sortHeaderVisible,
                enter   = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit    = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
            val sortLabel = when (uiState.sortOrder) {
                CryptoSortOrder.A_Z          -> stringResource(R.string.vault_sort_az)
                CryptoSortOrder.Z_A          -> stringResource(R.string.vault_sort_za)
                CryptoSortOrder.NEWEST       -> stringResource(R.string.vault_sort_newest_short)
                CryptoSortOrder.OLDEST       -> stringResource(R.string.vault_sort_oldest_short)
                CryptoSortOrder.BALANCE_HIGH -> stringResource(R.string.crypto_sort_balance_high)
                CryptoSortOrder.BALANCE_LOW  -> stringResource(R.string.crypto_sort_balance_low)
            }
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = if (uiState.portfolioGroups.isNotEmpty())
                                stringResource(R.string.crypto_count_portfolio, uiState.portfolioGroups.size, uiState.wallets.size)
                            else {
                                val cryptoCount = uiState.wallets.size
                                if (cryptoCount == 1) stringResource(R.string.crypto_count_one)
                                else stringResource(R.string.crypto_count, cryptoCount)
                            },
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
                        CryptoSortOption(stringResource(R.string.vault_sort_az),     uiState.sortOrder == CryptoSortOrder.A_Z)    { viewModel.onSortOrderChange(CryptoSortOrder.A_Z);    showSortMenu = false }
                        CryptoSortOption(stringResource(R.string.vault_sort_za),     uiState.sortOrder == CryptoSortOrder.Z_A)    { viewModel.onSortOrderChange(CryptoSortOrder.Z_A);    showSortMenu = false }
                        CryptoSortOption(stringResource(R.string.vault_sort_newest), uiState.sortOrder == CryptoSortOrder.NEWEST) { viewModel.onSortOrderChange(CryptoSortOrder.NEWEST); showSortMenu = false }
                        CryptoSortOption(stringResource(R.string.vault_sort_oldest), uiState.sortOrder == CryptoSortOrder.OLDEST) { viewModel.onSortOrderChange(CryptoSortOrder.OLDEST); showSortMenu = false }
                        CryptoSortOption(stringResource(R.string.crypto_sort_balance_high), uiState.sortOrder == CryptoSortOrder.BALANCE_HIGH) { viewModel.onSortOrderChange(CryptoSortOrder.BALANCE_HIGH); showSortMenu = false }
                        CryptoSortOption(stringResource(R.string.crypto_sort_balance_low),  uiState.sortOrder == CryptoSortOrder.BALANCE_LOW)  { viewModel.onSortOrderChange(CryptoSortOrder.BALANCE_LOW);  showSortMenu = false }
                    }
                }
            }
            }

            val hasActiveFilters = uiState.query.isNotBlank() ||
                uiState.selectedCategory != null ||
                uiState.selectedTags.isNotEmpty() ||
                uiState.onlyFavorites ||
                uiState.onlyWithAttachments ||
                uiState.onlyProtected

            val showPortfolioHeader = uiState.portfolioGroups.isNotEmpty() && !hasActiveFilters

            if (showPortfolioHeader) {
                CryptoPortfolioHeader(
                    totalFiat            = uiState.totalPortfolioFiat,
                    selectedCurrency     = uiState.selectedCurrency,
                    isLoading            = uiState.isLoadingPrices,
                    lastRefreshedAt      = uiState.lastRefreshedAt,
                    isLiveBalanceEnabled = saldosEnVivoActivos,
                    onEnableLiveBalance  = { viewModel.onEnableLiveBalanceRequested() },
                    onCurrencySelected   = viewModel::onCurrencySelected,
                    onRefresh            = viewModel::refreshPrices
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier       = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp),
                shape          = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                color          = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                LazyColumn(
                    state          = listState,
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {

                    if (uiState.wallets.isEmpty() && uiState.fuzzyWallets.isEmpty()) {
                        item(key = "empty_state") {
                            val hasFilters = uiState.selectedCategory != null || uiState.selectedTags.isNotEmpty()
                            val hasQuery   = uiState.query.isNotEmpty()
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .fillParentMaxHeight(0.7f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                ) {
                                    Icon(
                                        imageVector        = when {
                                            hasQuery   -> Icons.Default.SearchOff
                                            hasFilters -> Icons.Default.FilterList
                                            else       -> Icons.Default.CurrencyBitcoin
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
                                            else       -> stringResource(R.string.crypto_empty_title)
                                        },
                                        style     = MaterialTheme.typography.bodyMedium,
                                        color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                                    if (!hasQuery && !hasFilters) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { if (!isActiveVaultReadOnly) onAddWallet() },
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
                        }
                    }

                    if (uiState.wallets.isNotEmpty() && showPortfolioHeader) {
                        uiState.portfolioGroups.forEachIndexed { groupIdx, group ->
                            if (groupIdx > 0) {
                                item(key = "div_g_$groupIdx") { WalletItemDivider() }
                            }
                            if (group.wallets.size > 1) {
                                val groupIds    = group.wallets.map { it.wallet.id }
                                val allSelected = isSelectionMode && groupIds.all { it in selectedIds }
                                val someSelected = isSelectionMode && !allSelected && groupIds.any { it in selectedIds }
                                item(key = "group_${group.assetTicker}") {
                                    CryptoGroupHeader(
                                        group           = group,
                                        isSelectionMode = isSelectionMode,
                                        allSelected     = allSelected,
                                        someSelected    = someSelected,
                                        onClick  = {
                                            if (isSelectionMode) viewModel.toggleGroupSelection(groupIds)
                                            else viewModel.onToggleAssetExpanded(group.assetTicker)
                                        },
                                        onLongClick = { if (!isActiveVaultReadOnly) viewModel.toggleGroupSelection(groupIds) }
                                    )
                                }
                                val hijosVisibles = group.isExpanded || isSelectionMode
                                val mixedNetworks = group.wallets
                                    .map { it.wallet.network }.distinct().size > 1
                                group.wallets.forEach { wwp ->
                                    item(key = "child_${wwp.wallet.id}") {
                                        AnimatedVisibility(visible = hijosVisibles) {
                                            Column {
                                                WalletItemDivider()
                                                CryptoChildItem(
                                                    wwp             = wwp,
                                                    isSelected      = wwp.wallet.id in selectedIds,
                                                    isSelectionMode = isSelectionMode,
                                                    showNetwork     = mixedNetworks,
                                                    onClick         = { if (isSelectionMode) viewModel.toggleSelection(wwp.wallet.id) else onEditWallet(wwp.wallet.id) },
                                                    onLongClick     = { if (!isActiveVaultReadOnly) viewModel.toggleGroupSelection(groupIds) }
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                val wwp = group.wallets[0]
                                item(key = "wallet_${wwp.wallet.id}") {
                                    CryptoWalletItem(
                                        wallet                  = wwp.wallet,
                                        fiatValue               = wwp.fiatValue,
                                        isManual                = wwp.isManual,
                                        isSelected              = wwp.wallet.id in selectedIds,
                                        isSelectionMode         = isSelectionMode,
                                        hasAttachment           = wwp.wallet.id in attachmentIds,
                                        onClick                 = { if (isSelectionMode) viewModel.toggleSelection(wwp.wallet.id) else onEditWallet(wwp.wallet.id) },
                                        onLongClick             = { viewModel.toggleSelection(wwp.wallet.id) },
                                        sharedTransitionScope   = sharedTransitionScope,
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.wallets.isNotEmpty() && !showPortfolioHeader) {
                        val wwpMap = uiState.portfolioGroups.flatMap { it.wallets }
                            .associateBy { it.wallet.id }
                        uiState.wallets.forEachIndexed { index, wallet ->
                            if (index > 0) item("div_f_$index") { WalletItemDivider() }
                            item(key = "flat_${wallet.id}") {
                                val wwp = wwpMap[wallet.id]
                                CryptoWalletItem(
                                    wallet                  = wallet,
                                    fiatValue               = wwp?.fiatValue,
                                    isManual                = wwp?.isManual ?: false,
                                    isSelected              = wallet.id in selectedIds,
                                    isSelectionMode         = isSelectionMode,
                                    hasAttachment           = wallet.id in attachmentIds,
                                    onClick                 = { if (isSelectionMode) viewModel.toggleSelection(wallet.id) else onEditWallet(wallet.id) },
                                    onLongClick             = { viewModel.toggleSelection(wallet.id) },
                                    sharedTransitionScope   = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        }
                    }

                    if (uiState.fuzzyWallets.isNotEmpty()) {
                        val fuzzyFiat = uiState.portfolioGroups.flatMap { it.wallets }
                            .associateBy { it.wallet.id }
                        item(key = "fuzzy_header") {
                            FuzzySectionDivider(stringResource(R.string.search_similar_results))
                        }
                        uiState.fuzzyWallets.forEachIndexed { index, wallet ->
                            if (index > 0) item("div_fz_$index") { WalletItemDivider() }
                            item(key = "fz_${wallet.id}") {
                                val wwp = fuzzyFiat[wallet.id]
                                CryptoWalletItem(
                                    wallet                  = wallet,
                                    fiatValue               = wwp?.fiatValue,
                                    isManual                = wwp?.isManual ?: false,
                                    isSelected              = false,
                                    isSelectionMode         = false,
                                    hasAttachment           = wallet.id in attachmentIds,
                                    onClick                 = { onEditWallet(wallet.id) },
                                    onLongClick             = {},
                                    sharedTransitionScope   = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
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
            isReadOnly    = isActiveVaultReadOnly
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
private fun WalletItemDivider() {
    EntryGroupDivider(
        startInset = 64.dp,
        cardColor  = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        gapColor   = MaterialTheme.colorScheme.background
    )
}

private fun relativeTimeLabel(
    refreshedAt: Long,
    now: Long,
    lessThan1m: String,
    minutesFmt: String,
    hoursFmt: String,
    daysFmt: String
): String {
    val diffMs  = now - refreshedAt
    val diffMin = (diffMs / 60_000).toInt()
    val diffHrs = diffMin / 60
    return when {
        diffMin < 1  -> lessThan1m
        diffHrs < 1  -> minutesFmt.format(diffMin)
        diffHrs < 24 -> hoursFmt.format(diffHrs)
        else         -> daysFmt.format(diffHrs / 24)
    }
}

private fun formatFiatAmount(amount: Double, currency: String): String {
    val nf = java.text.NumberFormat.getNumberInstance(AppCompatDelegate.getApplicationLocales().get(0) ?: java.util.Locale.getDefault()).apply {
        isGroupingUsed        = true
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    val formatted = when {
        amount >= 1_000_000 -> "${nf.apply { maximumFractionDigits = 2 }.format(amount / 1_000_000)}M"
        else                -> nf.format(amount)
    }
    return "$formatted $currency"
}

private fun formatFiatAmountShort(amount: Double): String {
    val nf = java.text.NumberFormat.getNumberInstance(AppCompatDelegate.getApplicationLocales().get(0) ?: java.util.Locale.getDefault()).apply {
        isGroupingUsed        = true
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    return when {
        amount >= 1_000_000 -> "${nf.apply { maximumFractionDigits = 2 }.format(amount / 1_000_000)}M"
        else                -> nf.format(amount)
    }
}

@Composable
private fun CryptoPortfolioHeader(
    totalFiat: Double?,
    selectedCurrency: String,
    isLoading: Boolean,
    lastRefreshedAt: Long?,
    isLiveBalanceEnabled: Boolean,
    onEnableLiveBalance: () -> Unit,
    onCurrencySelected: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var showCurrencyPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .coachMarkAnchor("crypto_wallets", "refresh")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = stringResource(R.string.crypto_portfolio_total),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isLoading && totalFiat == null) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text       = if (totalFiat != null) formatFiatAmount(totalFiat, selectedCurrency) else "—",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick  = onRefresh,
                    enabled  = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(
                            imageVector        = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.crypto_balance_refresh),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (!isLoading && lastRefreshedAt != null) {
                    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            kotlinx.coroutines.delay(30_000L)
                            now = System.currentTimeMillis()
                        }
                    }
                    val lessThan1mLabel = stringResource(R.string.time_relative_less_than_1m)
                    val minutesFmt      = stringResource(R.string.time_relative_minutes)
                    val hoursFmt        = stringResource(R.string.time_relative_hours)
                    val daysFmt         = stringResource(R.string.time_relative_days)
                    Text(
                        text  = relativeTimeLabel(lastRefreshedAt, now, lessThan1mLabel, minutesFmt, hoursFmt, daysFmt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))

            Box {
                Surface(
                    onClick = { showCurrencyPicker = true },
                    shape   = RoundedCornerShape(8.dp),
                    color   = MaterialTheme.colorScheme.surface,
                    border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text  = selectedCurrency,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector        = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded         = showCurrencyPicker,
                    onDismissRequest = { showCurrencyPicker = false },
                    containerColor   = MaterialTheme.colorScheme.surface
                ) {
                    CryptoPortfolioPrefs.SUPPORTED_CURRENCIES.forEach { cur ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text       = cur,
                                    fontWeight = if (cur == selectedCurrency) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (cur == selectedCurrency) MaterialTheme.colorScheme.primary
                                                 else MaterialTheme.colorScheme.onBackground
                                )
                            },
                            onClick = { onCurrencySelected(cur); showCurrencyPicker = false },
                            trailingIcon = if (cur == selectedCurrency) ({
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }) else null
                        )
                    }
                }
            }
            }
        }

        if (!isLiveBalanceEnabled) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = stringResource(R.string.crypto_balance_onchain_disabled),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick         = onEnableLiveBalance,
                    contentPadding  = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text       = stringResource(R.string.crypto_balance_onchain_enable),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CryptoGroupHeader(
    group: CryptoAssetGroup,
    isSelectionMode: Boolean = false,
    allSelected: Boolean = false,
    someSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (allSelected || someSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else Color.Transparent
            )
            .combinedClickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            if (someSelected) {
                IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector        = Icons.Default.IndeterminateCheckBox,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(24.dp)
                    )
                }
            } else {
                Checkbox(
                    checked         = allSelected,
                    onCheckedChange = { onClick() },
                    modifier        = Modifier.size(40.dp)
                )
            }
        } else {
            CryptoAwareAvatar(asset = group.assetTicker, size = 40.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text       = group.assetName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text       = "${group.wallets.size}",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onPrimary,
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

        }
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val manualCount = group.wallets.count { it.isManual }
            val allManual   = manualCount == group.wallets.size
            if (manualCount > 0) {
                BadgedBox(
                    badge = {
                        if (!allManual) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor   = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(
                                    text  = "$manualCount",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = androidx.compose.ui.unit.TextUnit(
                                            8f, androidx.compose.ui.unit.TextUnitType.Sp
                                        )
                                    )
                                )
                            }
                        }
                    }
                ) {
                    Box(modifier = Modifier.padding(end = 8.dp, top = 4.dp)) {
                        ManualBalanceTag()
                    }
                }
            }

            if (!isSelectionMode) {
                Icon(
                    imageVector        = if (group.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
            if (group.totalFiatValue != null) {
                Text(
                    text  = formatFiatAmountShort(group.totalFiatValue),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun CryptoChildItem(
    wwp: WalletWithPrice,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    showNetwork: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else Color.Transparent
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = if (isSelectionMode) 56.dp else 32.dp, end = 16.dp),
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
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            val addressDisplay = if (wwp.wallet.address.length > 22)
                wwp.wallet.address.take(13) + "…" + wwp.wallet.address.takeLast(9)
            else wwp.wallet.address
            Text(
                text  = addressDisplay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            val displayName = wwp.wallet.notes.lines().firstOrNull()?.take(30)?.takeIf { it.isNotBlank() }
                ?: if (showNetwork) wwp.wallet.network.ifEmpty { null } else null
            if (!displayName.isNullOrEmpty()) {
                Text(
                    text  = displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (wwp.wallet.isProtected) {
            ProtectedLockBadge(enLinea = true)
            Spacer(Modifier.width(8.dp))
        }
        if (wwp.nominalBalance.isNotEmpty() || wwp.isManual) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (wwp.isManual) ManualBalanceTag()
                if (wwp.fiatValue != null) {
                    Text(
                        text  = formatFiatAmountShort(wwp.fiatValue),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun CryptoFilterChip(
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
private fun CryptoSortOption(label: String, selected: Boolean, onClick: () -> Unit) {
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
private fun CryptoWalletItem(
    wallet:                  CryptoWallet,
    fiatValue:               Double? = null,
    isManual:                Boolean = false,
    isSelected:              Boolean = false,
    isSelectionMode:         Boolean = false,
    hasAttachment:           Boolean = false,
    onClick:                 () -> Unit,
    onLongClick:             () -> Unit = {},
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else Color.Transparent
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            sharedContentState      = rememberSharedContentState("crypto_avatar_${wallet.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier
                Box(
                    modifier         = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CryptoAwareAvatar(
                        asset    = wallet.asset.ifEmpty { wallet.network },
                        size     = 40.dp,
                        modifier = avatarModifier
                    )
                    if (wallet.isProtected) {
                        ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
                    }
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text       = wallet.asset.ifEmpty { wallet.network }.ifEmpty { stringResource(R.string.crypto_default_name) },
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary
                )
                val assetLabel = wallet.asset.trim()
                val networkLabel = wallet.network.trim()
                val subtitle = when {
                    assetLabel.isNotEmpty() && networkLabel.isNotEmpty() && !networkLabel.startsWith(assetLabel, ignoreCase = true) -> networkLabel
                    assetLabel.isEmpty() && networkLabel.isNotEmpty() -> networkLabel
                    else -> ""
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EntryChipsRow(
                    category = wallet.category.takeIf { it != EntryCategory.SIN_CATEGORIZAR },
                    tags     = wallet.tags
                )
            }
            if (hasAttachment) {
                Icon(
                    imageVector        = Icons.Default.AttachFile,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(2.dp))
            }
            if (wallet.isPinned) {
                Icon(
                    imageVector        = Icons.Default.PushPin,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(2.dp))
            }
            if (fiatValue != null || isManual) {
                if (isManual) {
                    Spacer(Modifier.width(12.dp))
                    ManualBalanceTag()
                    Spacer(Modifier.width(12.dp))
                }
                if (fiatValue != null) {
                    Text(
                        text  = formatFiatAmountShort(fiatValue),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
}

@Composable
private fun ManualBalanceTag() {
    val color = MaterialTheme.colorScheme.primary
    Row(
        modifier              = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector        = Icons.Default.Edit,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(10.dp)
        )
        Text(
            text  = stringResource(R.string.crypto_manual_tag),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
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