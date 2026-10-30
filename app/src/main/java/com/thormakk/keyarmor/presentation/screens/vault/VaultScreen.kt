@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.vault
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.FuzzySectionDivider
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import com.thormakk.keyarmor.presentation.screens.settings.contrastRatio
import com.thormakk.keyarmor.ui.theme.SuccessGreen
import com.thormakk.keyarmor.ui.theme.SuccessGreenBright
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.components.ChipData
import com.thormakk.keyarmor.presentation.components.ChipsRowWithFunnel
import com.thormakk.keyarmor.presentation.components.EntryChipsRow
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.TagDeleteButton
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.presentation.components.VioletaDelFab
import com.thormakk.keyarmor.presentation.components.chipCategoryColor
import com.thormakk.keyarmor.presentation.components.chipTagColor
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.selection.BulkCategorySheet
import com.thormakk.keyarmor.presentation.components.selection.BulkMode
import com.thormakk.keyarmor.presentation.components.selection.BulkTagSheet
import com.thormakk.keyarmor.presentation.components.selection.SelectionTopBar
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.truncateTag
import com.thormakk.keyarmor.presentation.screens.auth.BiometricSetupDialogHost
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.ServiceIconProvider
import com.thormakk.keyarmor.presentation.util.highlightText
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import com.thormakk.keyarmor.ui.theme.PasswordStrengthFair
import com.thormakk.keyarmor.ui.theme.PasswordStrengthGood
import com.thormakk.keyarmor.ui.theme.PasswordStrengthStrong
import com.thormakk.keyarmor.ui.theme.PasswordStrengthWeak
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun VaultScreen(
    onAddEntry:       () -> Unit,
    onEntryClick:     (String) -> Unit,
    onEditEntry:      (String) -> Unit = onEntryClick,
    onVaultHubClick:  () -> Unit = {},
    onPlansClick:     () -> Unit = {},
    viewModel:               VaultViewModel         = hiltViewModel(),
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val isFreeUser        by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val availableTags   by viewModel.availableTags.collectAsStateWithLifecycle()
    val pendingDeleteId by viewModel.pendingDeleteId.collectAsStateWithLifecycle()

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

    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isSelectionMode = selectedIds.isNotEmpty()
    val otherVaults     by viewModel.otherVaults.collectAsStateWithLifecycle()
    val tagCountMap     by viewModel.tagCountMap.collectAsStateWithLifecycle()
    val categoryCountMap by viewModel.categoryCountMap.collectAsStateWithLifecycle()

    var showFiltersSheet        by remember { mutableStateOf(false) }
    var showStrengthSheet       by remember { mutableStateOf(false) }
    var showSearch              by remember { mutableStateOf(uiState.query.isNotBlank()) }
    val searchFocusRequester    =  remember { FocusRequester() }
    var showSortMenu            by remember { mutableStateOf(false) }
    var showBulkTagSheet        by remember { mutableStateOf(false) }
    var showBulkCategorySheet   by remember { mutableStateOf(false) }
    var showArchiveLockedDialog by remember { mutableStateOf(false) }
    var showBulkUnlock          by remember { mutableStateOf(false) }
    var showBulkProtectPremium  by remember { mutableStateOf(false) }

    var bulkVaultActionIsMove   by remember { mutableStateOf(true) }
    var showBulkVaultSheet      by remember { mutableStateOf(false) }

    var entryActionId           by remember { mutableStateOf<String?>(null) }
    var entryActionIsMove       by remember { mutableStateOf(true) }
    var showEntryVaultSheet     by remember { mutableStateOf(false) }

    var removeAttributionEntryId by remember { mutableStateOf<String?>(null) }

    var emptyVaultInfo          by remember { mutableStateOf<Pair<String, String>?>(null) }

    var protectedSkippedCount by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { viewModel.protectedSkipped.collect { protectedSkippedCount = it } }
    val protectedSkippedMsg = protectedSkippedCount?.let { pluralStringResource(R.plurals.protect_bulk_skipped, it, it) }
    LaunchedEffect(protectedSkippedCount) {
        if (protectedSkippedMsg != null) {
            protectedSkippedCount = null
            snackbarHostState.showSnackbar(protectedSkippedMsg)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.emptyVaultEvent.collect { emptyVaultInfo = it }
    }

    BackHandler(enabled = isSelectionMode) { viewModel.clearSelection() }
    BackHandler(enabled = showSearch) { showSearch = false; viewModel.onQueryChange("") }
    LaunchedEffect(showSearch) { if (showSearch) searchFocusRequester.requestFocus() }
    val quickFilters by viewModel.quickFilters.collectAsStateWithLifecycle()

    val strengthCounts: Map<StrengthFilter, Int> = remember(uiState.entries) {
        val counts = mutableMapOf(
            StrengthFilter.WEAK        to 0,
            StrengthFilter.MEDIUM      to 0,
            StrengthFilter.STRONG      to 0,
            StrengthFilter.NO_PASSWORD to 0
        )
        uiState.entries.forEach { entry ->
            val s = passwordStrength(entry.password)
            val key = when {
                s < 0  -> StrengthFilter.NO_PASSWORD
                s <= 1 -> StrengthFilter.WEAK
                s == 2 -> StrengthFilter.MEDIUM
                else   -> StrengthFilter.STRONG
            }
            counts[key] = counts.getValue(key) + 1
        }
        counts
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

    var trafficAnimHighlight by remember { mutableStateOf<StrengthFilter?>(null) }
    LaunchedEffect(Unit) {
        delay(500L)
        val seq = listOf(StrengthFilter.WEAK, StrengthFilter.MEDIUM, StrengthFilter.STRONG)
        repeat(3) {
            for (filter in seq) {
                trafficAnimHighlight = filter
                delay(350L)
                trafficAnimHighlight = null
                delay(150L)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

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
                        totalCount       = uiState.entries.size,
                        isReadOnly       = isActiveVaultReadOnly,
                        isArchiveLocked  = isFreeUser,
                        onClearSelection = viewModel::clearSelection,
                        onSelectAll      = viewModel::selectAll,
                        onArchive        = { viewModel.bulkArchive(); showFiltersSheet = false },
                        onArchiveLocked  = { showArchiveLockedDialog = true },
                        onDelete         = { viewModel.bulkDelete(); showFiltersSheet = false },
                        onTag            = { showBulkTagSheet = true },
                        onCategorize     = { showBulkCategorySheet = true },
                        onMoveTo         = { bulkVaultActionIsMove = true;  showBulkVaultSheet = true },
                        onCopyTo         = { bulkVaultActionIsMove = false; showBulkVaultSheet = true },
                        protectedCount   = uiState.entries.count { it.id in selectedIds && it.isProtected },
                        allProtected     = uiState.entries.filter { it.id in selectedIds }.let { sel -> sel.isNotEmpty() && sel.all { it.isProtected } },
                        soleEntryTitle   = uiState.entries.filter { it.id in selectedIds && !it.isProtected }
                                               .singleOrNull()?.let { it.title.ifBlank { null } },
                        onToggleProtect  = {
                            val sel = uiState.entries.filter { it.id in selectedIds }
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
                                text       = stringResource(R.string.vault_title),
                                style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                                fontWeight = FontWeight.SemiBold,
                                color      = sectionTitleColor()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onVaultHubClick) {
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
                            Spacer(Modifier.width(5.dp))
                            IconButton(onClick = { showStrengthSheet = true }) {
                                TrafficLightIcon(
                                    activeFilters  = uiState.activeStrengthFilters,
                                    counts         = strengthCounts,
                                    animHighlight  = trafficAnimHighlight
                                )
                            }
                        },
                        colors = topBarColors()
                    )
                }
            }
        },
        floatingActionButton = {
            val navBarHeight = WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding()
            FloatingActionButton(
                onClick        = { if (!isActiveVaultReadOnly) onAddEntry() },
                modifier       = Modifier
                    .offset(y = -navBarHeight)
                    .coachMarkAnchor("vault", "fab"),
                containerColor = VioletaDelFab,
                contentColor   = ContenidoDelFab
            ) {
                Icon(
                    imageVector        = if (isActiveVaultReadOnly) Icons.Default.Lock else Icons.Default.Add,
                    contentDescription = if (isActiveVaultReadOnly) null else stringResource(R.string.vault_entry_new)
                )
            }
        },
        containerColor      = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(
                visible = showSearch,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
            OutlinedTextField(
                value         = uiState.query,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onQueryChange(it) },
                placeholder   = { Text(stringResource(R.string.vault_search_placeholder)) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                imageVector        = Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_clear)
                            )
                        }
                    }
                },
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .focusRequester(searchFocusRequester),
                shape      = CircleShape,
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Color.Transparent
                )
            )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val activeFilterCount =
                (if (uiState.selectedCategory != null) 1 else 0) +
                uiState.selectedTags.size +
                (if (uiState.onlyFavorites) 1 else 0) +
                (if (uiState.onlyWithAttachments) 1 else 0) +
                (if (uiState.onlyWithTwoFactor) 1 else 0) +
                (if (uiState.onlyWithPasskey) 1 else 0) +
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
                modifier                 = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp)
                    .coachMarkAnchor("vault", "filters")
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
                uiState.onlyFavorites || uiState.onlyWithAttachments ||
                uiState.onlyWithTwoFactor || uiState.onlyWithPasskey || uiState.onlyProtected ||
                uiState.activeStrengthFilters.isNotEmpty()
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
                    if (uiState.onlyWithTwoFactor) {
                        ActiveFilterChip(
                            label       = stringResource(R.string.twofa_screen_title),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onRemove    = { viewModel.onTwoFactorFilterToggle() }
                        )
                    }
                    if (uiState.onlyWithPasskey) {
                        ActiveFilterChip(
                            label       = stringResource(R.string.passkey_detail_title),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onRemove    = { viewModel.onPasskeyFilterToggle() }
                        )
                    }
                    if (uiState.onlyProtected) {
                        ActiveFilterChip(
                            label       = stringResource(R.string.filter_protected),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onRemove    = { viewModel.onProtectedFilterToggle(false) }
                        )
                    }
                    if (uiState.activeStrengthFilters.isNotEmpty()) {
                        val strengthLabel = if (uiState.activeStrengthFilters.size == 1) {
                            when (uiState.activeStrengthFilters.first()) {
                                StrengthFilter.WEAK        -> stringResource(R.string.filter_critical)
                                StrengthFilter.MEDIUM      -> stringResource(R.string.filter_medium)
                                StrengthFilter.STRONG      -> stringResource(R.string.filter_safe)
                                StrengthFilter.NO_PASSWORD -> stringResource(R.string.filter_no_password)
                            }
                        } else {
                            stringResource(R.string.filter_states_count, uiState.activeStrengthFilters.size)
                        }
                        Surface(
                            shape           = RoundedCornerShape(8.dp),
                            color           = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation  = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier              = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    if (StrengthFilter.WEAK        in uiState.activeStrengthFilters)
                                        Box(Modifier.size(10.dp).background(Color(0xFFE53935), CircleShape))
                                    if (StrengthFilter.MEDIUM      in uiState.activeStrengthFilters)
                                        Box(Modifier.size(10.dp).background(Color(0xFFFFB300), CircleShape))
                                    if (StrengthFilter.STRONG      in uiState.activeStrengthFilters)
                                        Box(Modifier.size(10.dp).background(Color(0xFF43A047), CircleShape))
                                    if (StrengthFilter.NO_PASSWORD in uiState.activeStrengthFilters)
                                        Box(Modifier.size(10.dp).background(Color(0xFFBDBDBD), CircleShape))
                                }
                                Text(
                                    text       = strengthLabel,
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onBackground
                                )
                                Icon(
                                    imageVector        = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.vault_clear_filters),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier           = Modifier
                                        .size(16.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication        = null,
                                            onClick           = { viewModel.onStrengthFiltersChange(emptySet()) }
                                        )
                                )
                            }
                        }
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

            val sortLabel = when (uiState.sortOrder) {
                SortOrder.A_Z    -> stringResource(R.string.vault_sort_az)
                SortOrder.Z_A    -> stringResource(R.string.vault_sort_za)
                SortOrder.NEWEST -> stringResource(R.string.vault_sort_newest_short)
                SortOrder.OLDEST -> stringResource(R.string.vault_sort_oldest_short)
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
                val entryCount  = uiState.entries.size
                val pinnedCount = uiState.entries.count { it.isPinned }
                val countLabel  = buildString {
                    append(
                        if (entryCount == 1) stringResource(R.string.vault_entry_count_one)
                        else stringResource(R.string.vault_entry_count, entryCount)
                    )
                    if (pinnedCount > 0) {
                        append(" · ")
                        append(
                            if (pinnedCount == 1) stringResource(R.string.vault_pinned_count_one)
                            else stringResource(R.string.vault_pinned_count, pinnedCount)
                        )
                    }
                }
                Text(
                    text  = countLabel,
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
                        SortOrderOption(
                            label    = stringResource(R.string.vault_sort_az),
                            selected = uiState.sortOrder == SortOrder.A_Z,
                            onClick  = { viewModel.onSortOrderChange(SortOrder.A_Z); showSortMenu = false }
                        )
                        SortOrderOption(
                            label    = stringResource(R.string.vault_sort_za),
                            selected = uiState.sortOrder == SortOrder.Z_A,
                            onClick  = { viewModel.onSortOrderChange(SortOrder.Z_A); showSortMenu = false }
                        )
                        SortOrderOption(
                            label    = stringResource(R.string.vault_sort_newest),
                            selected = uiState.sortOrder == SortOrder.NEWEST,
                            onClick  = { viewModel.onSortOrderChange(SortOrder.NEWEST); showSortMenu = false }
                        )
                        SortOrderOption(
                            label    = stringResource(R.string.vault_sort_oldest),
                            selected = uiState.sortOrder == SortOrder.OLDEST,
                            onClick  = { viewModel.onSortOrderChange(SortOrder.OLDEST); showSortMenu = false }
                        )
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.entries.isEmpty() && uiState.fuzzyEntries.isEmpty()) {
                EmptyVault(
                    hasQuery      = uiState.query.isNotEmpty(),
                    onlyFavorites = uiState.onlyFavorites,
                    hasFilters    = uiState.selectedCategory != null ||
                                    uiState.selectedTags.isNotEmpty() ||
                                    uiState.activeStrengthFilters.isNotEmpty(),
                    isReadOnly    = isActiveVaultReadOnly,
                    onAddEntry    = onAddEntry
                )
            } else {
                LazyColumn(
                    modifier       = Modifier.coachMarkAnchor("vault", "multiselect"),
                    state          = listState,
                    contentPadding = PaddingValues(
                        start = 7.dp, end = 7.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    val pinnedEntries   = uiState.entries.filter { it.isPinned }
                    val unpinnedEntries = uiState.entries.filter { !it.isPinned }

                    if (pinnedEntries.isNotEmpty()) {
                        itemsIndexed(pinnedEntries, key = { _, it -> it.id }) { index, entry ->
                            VaultEntryCard(
                                entry                   = entry,
                                isFirst                 = index == 0,
                                isLast                  = index == pinnedEntries.lastIndex,
                                isSelected              = entry.id in selectedIds,
                                isSelectionMode         = isSelectionMode,
                                hasAttachment           = entry.id in uiState.entryIdsWithAttachments,
                                hasTwoFactor            = entry.id in uiState.entryIdsWithTwoFactor,
                                hasPasskey              = entry.id in uiState.entryIdsWithPasskey,
                                onClick                 = {
                                    if (isSelectionMode) viewModel.toggleSelection(entry.id)
                                    else onEntryClick(entry.id)
                                },
                                onLongClick             = { viewModel.toggleSelection(entry.id) },
                                onFavoriteToggle        = { viewModel.toggleFavorite(entry) },
                                onEditClick             = { onEditEntry(entry.id) },
                                onPinToggle             = { viewModel.togglePin(entry) },
                                onArchive               = {
                                    if (isFreeUser) showArchiveLockedDialog = true
                                    else viewModel.archiveEntry(entry)
                                },
                                isArchiveLocked         = isFreeUser,
                                isReadOnly              = isActiveVaultReadOnly,
                                onDelete                = { viewModel.softDeleteEntry(entry) },
                                onMoveTo                = { entryActionId = entry.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                                onCopyTo                = { entryActionId = entry.id; entryActionIsMove = false; showEntryVaultSheet = true },
                                isHubPinned             = entry.id in pinnedHubIds,
                                onToggleHubPin          = { viewModel.toggleHubPin(entry.id, HubEntryType.PASSWORD) },
                                sharedTransitionScope   = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                modifier                = Modifier.animateItem()
                            )
                        }
                        item(key = "pinned_gap") {
                            Spacer(
                                modifier = Modifier
                                    .animateItem()
                                    .height(4.dp)
                            )
                        }
                    }

                    itemsIndexed(unpinnedEntries, key = { _, it -> it.id }) { index, entry ->
                        VaultEntryCard(
                            entry                   = entry,
                            isFirst                 = index == 0,
                            isLast                  = index == unpinnedEntries.lastIndex,
                            isSelected              = entry.id in selectedIds,
                            isSelectionMode         = isSelectionMode,
                            hasAttachment           = entry.id in uiState.entryIdsWithAttachments,
                            hasTwoFactor            = entry.id in uiState.entryIdsWithTwoFactor,
                            hasPasskey              = entry.id in uiState.entryIdsWithPasskey,
                            onClick                 = {
                                if (isSelectionMode) viewModel.toggleSelection(entry.id)
                                else onEntryClick(entry.id)
                            },
                            onLongClick             = { viewModel.toggleSelection(entry.id) },
                            onFavoriteToggle        = { viewModel.toggleFavorite(entry) },
                            onEditClick             = { onEditEntry(entry.id) },
                            onPinToggle             = { viewModel.togglePin(entry) },
                            onArchive               = {
                                if (isFreeUser) showArchiveLockedDialog = true
                                else viewModel.archiveEntry(entry)
                            },
                            isArchiveLocked         = isFreeUser,
                            isReadOnly              = isActiveVaultReadOnly,
                            onDelete                = { viewModel.softDeleteEntry(entry) },
                            onMoveTo                = { entryActionId = entry.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                            onCopyTo                = { entryActionId = entry.id; entryActionIsMove = false; showEntryVaultSheet = true },
                            isHubPinned             = entry.id in pinnedHubIds,
                            onToggleHubPin          = { viewModel.toggleHubPin(entry.id, HubEntryType.PASSWORD) },
                            sharedTransitionScope   = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            modifier                = Modifier.animateItem()
                        )
                    }

                    if (uiState.fuzzyEntries.isNotEmpty()) {
                        item(key = "fuzzy_header") {
                            FuzzySectionDivider(stringResource(R.string.search_similar_results))
                        }
                        itemsIndexed(
                            uiState.fuzzyEntries,
                            key = { _, it -> "fz_${it.id}" }
                        ) { index, entry ->
                            VaultEntryCard(
                                entry                   = entry,
                                isFirst                 = index == 0,
                                isLast                  = index == uiState.fuzzyEntries.lastIndex,
                                isSelected              = false,
                                isSelectionMode         = false,
                                hasAttachment           = entry.id in uiState.entryIdsWithAttachments,
                                hasTwoFactor            = entry.id in uiState.entryIdsWithTwoFactor,
                                hasPasskey              = entry.id in uiState.entryIdsWithPasskey,
                                onClick                 = { onEntryClick(entry.id) },
                                onLongClick             = {},
                                onFavoriteToggle        = { viewModel.toggleFavorite(entry) },
                                onEditClick             = { onEditEntry(entry.id) },
                                onPinToggle             = { viewModel.togglePin(entry) },
                                onArchive               = {
                                    if (isFreeUser) showArchiveLockedDialog = true
                                    else viewModel.archiveEntry(entry)
                                },
                                isArchiveLocked         = isFreeUser,
                                isReadOnly              = isActiveVaultReadOnly,
                                onDelete                = { viewModel.softDeleteEntry(entry) },
                                onMoveTo                = { entryActionId = entry.id; entryActionIsMove = true;  showEntryVaultSheet = true },
                                onCopyTo                = { entryActionId = entry.id; entryActionIsMove = false; showEntryVaultSheet = true },
                                isHubPinned             = entry.id in pinnedHubIds,
                                onToggleHubPin          = { viewModel.toggleHubPin(entry.id, HubEntryType.PASSWORD) },
                                sharedTransitionScope   = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                modifier                = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
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
                val id = entryActionId ?: return@MoveToVaultBottomSheet
                if (entryActionIsMove) viewModel.moveEntryTo(id, targetVaultId)
                else                   viewModel.copyEntryTo(id, targetVaultId)
                entryActionId = null
            },
            onDismiss  = { showEntryVaultSheet = false; entryActionId = null }
        )
    }

    removeAttributionEntryId?.let { rid ->
        val ownerEmail = uiState.entries.find { it.id == rid }?.inheritedFrom ?: rid
        AlertDialog(
            onDismissRequest = { removeAttributionEntryId = null },
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
                    onClick = { viewModel.removeInheritedFrom(rid); removeAttributionEntryId = null },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.action_remove_attribution)) }
            },
            dismissButton = {
                TextButton(onClick = { removeAttributionEntryId = null }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    emptyVaultInfo?.let { (_, vaultName) ->
        AlertDialog(
            onDismissRequest = { emptyVaultInfo = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text       = stringResource(R.string.empty_vault_after_move_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text  = stringResource(R.string.empty_vault_after_move_body, vaultName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { emptyVaultInfo = null },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.action_keep_vault), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        emptyVaultInfo = null
                        onVaultHubClick()
                    }
                ) {
                    Text(
                        stringResource(R.string.action_delete_vault),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
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

    BiometricSetupDialogHost()

    AnimatedVisibility(
        visible = pendingDeleteId != null,
        enter   = slideInVertically(initialOffsetY = { it }),
        exit    = slideOutVertically(targetOffsetY = { it }),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2E2E2E)),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = { viewModel.resetDeleteTimer() }
                    )
                    .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp)
            ) {
                Text(
                    text  = stringResource(R.string.vault_entry_deleted),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = { viewModel.undoDelete() }) {
                Text(
                    text       = stringResource(R.string.action_undo),
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    style      = MaterialTheme.typography.bodyMedium
                )
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
            onApply            = { category, tags, favorites, attachments ->
                viewModel.onCategorySelected(category)
                viewModel.onTagsApplied(tags)
                viewModel.onFavoritesToggle(favorites)
                if (attachments != uiState.onlyWithAttachments) viewModel.onAttachmentFilterToggle()
                showFiltersSheet = false
            },
            onClearAll         = {
                viewModel.onCategorySelected(null)
                viewModel.onTagsApplied(emptySet())
                viewModel.onFavoritesToggle(false)
                if (uiState.onlyWithAttachments) viewModel.onAttachmentFilterToggle()
                viewModel.onProtectedFilterToggle(false)
                showFiltersSheet = false
            },
            onDismiss          = { showFiltersSheet = false },
            canCreateTags      = !isFreeUser,
            isReadOnly         = isActiveVaultReadOnly,
            onUpgradeClick     = onPlansClick,
            onAddTag           = { viewModel.addTag(it) },
            onRenameTag        = { id, name -> viewModel.renameTag(id, name) },
            onDeleteTag        = { viewModel.deleteTag(it) },
            tagConflictKey     = viewModel::tagConflictKey,
            show2FAPasskeyFilters = true,
            initialTwoFactor      = uiState.onlyWithTwoFactor,
            initialPasskey        = uiState.onlyWithPasskey,
            onTwoFactorToggle     = { value -> if (value != uiState.onlyWithTwoFactor) viewModel.onTwoFactorFilterToggle() },
            onPasskeyToggle       = { value -> if (value != uiState.onlyWithPasskey) viewModel.onPasskeyFilterToggle() },
            initialProtected      = uiState.onlyProtected,
            onProtectedToggle     = { value -> viewModel.onProtectedFilterToggle(value) }
        )
    }

    if (showStrengthSheet) {
        StrengthFilterSheet(
            selectedFilters = uiState.activeStrengthFilters,
            counts          = strengthCounts,
            onApply         = { filters ->
                viewModel.onStrengthFiltersChange(filters)
                showStrengthSheet = false
            },
            onDismiss       = {
                showStrengthSheet = false
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

internal val FunnelIcon: ImageVector = ImageVector.Builder(
    name           = "FunnelOutlined",
    defaultWidth   = 24.dp,
    defaultHeight  = 24.dp,
    viewportWidth  = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill            = SolidColor(Color.Transparent),
        stroke          = SolidColor(Color.Black),
        strokeLineWidth = 1.3f,
        strokeLineCap   = StrokeCap.Round,
        strokeLineJoin  = StrokeJoin.Round
    ) {
        moveTo(2f, 3f)
        lineTo(22f, 3f)
        lineTo(14.4f, 11f)
        lineTo(14.4f, 21f)
        lineTo(9.6f, 16f)
        lineTo(9.6f, 11f)
        close()
    }
}.build()

@Composable
private fun TrafficLightIcon(
    activeFilters: Set<StrengthFilter>,
    counts:        Map<StrengthFilter, Int> = emptyMap(),
    animHighlight: StrengthFilter?          = null
) {
    val activeAlpha  = 1.0f
    val dimmedAlpha  = 0.25f
    val neutralAlpha = 0.40f
    val hasAny = activeFilters.isNotEmpty()

    val colorWeak   = Color(0xFFE53935)
    val colorMedium = Color(0xFFFFC107)
    val colorStrong = Color(0xFF43A047)

    val alphaWeak   = when {
        animHighlight == StrengthFilter.WEAK   -> activeAlpha
        StrengthFilter.WEAK   in activeFilters -> activeAlpha
        hasAny                                 -> dimmedAlpha
        else                                   -> neutralAlpha
    }
    val alphaMedium = when {
        animHighlight == StrengthFilter.MEDIUM -> activeAlpha
        StrengthFilter.MEDIUM in activeFilters -> activeAlpha
        hasAny                                 -> dimmedAlpha
        else                                   -> neutralAlpha
    }
    val alphaStrong = when {
        animHighlight == StrengthFilter.STRONG -> activeAlpha
        StrengthFilter.STRONG in activeFilters -> activeAlpha
        hasAny                                 -> dimmedAlpha
        else                                   -> neutralAlpha
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(
            Triple(colorWeak,   alphaWeak,   counts[StrengthFilter.WEAK]   ?: 0),
            Triple(colorMedium, alphaMedium, counts[StrengthFilter.MEDIUM] ?: 0),
            Triple(colorStrong, alphaStrong, counts[StrengthFilter.STRONG] ?: 0)
        ).forEach { (color, alpha, count) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color.copy(alpha = alpha), CircleShape)
                )
                if (counts.isNotEmpty()) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text       = "$count",
                        fontSize   = 9.sp,
                        lineHeight = 9.sp,
                        color      = color.copy(alpha = activeAlpha),
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.width(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SortOrderOption(
    label:    String,
    selected: Boolean,
    onClick:  () -> Unit
) {
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
        onClick        = onClick,
        trailingIcon   = if (selected) ({
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(16.dp)
            )
        }) else null
    )
}

@Composable
internal fun FiltersSheet(
    availableTags:      List<Tag>,
    selectedCategory:   EntryCategory?,
    selectedTags:       Set<String>,
    initialFavorites:   Boolean,
    initialAttachments: Boolean,
    onApply:            (EntryCategory?, Set<String>, Boolean, Boolean) -> Unit,
    onClearAll:         () -> Unit,
    onDismiss:          () -> Unit,
    canCreateTags:      Boolean = true,
    isReadOnly:         Boolean = false,
    onUpgradeClick:     () -> Unit = {},
    onAddTag:           (String) -> Unit,
    onRenameTag:        (String, String) -> Unit,
    onDeleteTag:        (String) -> Unit,
    tagConflictKey:     (String) -> String,
    showSearchBackupsOption: Boolean          = false,
    initialSearchBackups:    Boolean          = false,
    onSearchBackupsToggle:   (Boolean) -> Unit = {},
    show2FAPasskeyFilters:   Boolean          = false,
    initialTwoFactor:        Boolean          = false,
    initialPasskey:          Boolean          = false,
    onTwoFactorToggle:       (Boolean) -> Unit = {},
    onPasskeyToggle:         (Boolean) -> Unit = {},
    initialProtected:        Boolean          = false,
    onProtectedToggle:       ((Boolean) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val autoLockManager = LocalAutoLockManager.current

    var pendingCategory      by remember { mutableStateOf(selectedCategory) }
    var checkedTags          by remember { mutableStateOf(selectedTags) }
    var pendingFavorites     by remember { mutableStateOf(initialFavorites) }
    var pendingAttachments   by remember { mutableStateOf(initialAttachments) }
    var pendingTwoFactor     by remember { mutableStateOf(initialTwoFactor) }
    var pendingPasskey       by remember { mutableStateOf(initialPasskey) }
    var pendingProtected     by remember { mutableStateOf(initialProtected) }
    var pendingSearchBackups by remember { mutableStateOf(initialSearchBackups) }
    var searchQuery     by remember { mutableStateOf("") }
    var newTagText      by remember { mutableStateOf("") }
    var newTagError     by remember { mutableStateOf<String?>(null) }
    val scrollState      = rememberScrollState()
    val sealScrollFromSheet = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
        }
    }
    val density          = LocalDensity.current
    val imeBottomPx      = WindowInsets.ime.getBottom(density)
    val imeBottomPxState = rememberUpdatedState(imeBottomPx)
    LaunchedEffect(Unit) {
        snapshotFlow { imeBottomPxState.value to scrollState.maxValue }
            .collect { (imeBottom, max) ->
                if (imeBottom > 0) scrollState.scrollTo(max)
            }
    }
    var renameTarget    by remember { mutableStateOf<Tag?>(null) }
    var renameText      by remember { mutableStateOf("") }
    var renameError     by remember { mutableStateOf<String?>(null) }
    var showLockedDialog by remember { mutableStateOf(false) }

    val errDuplicate = stringResource(R.string.tag_error_duplicate)
    val errCategory  = stringResource(R.string.tag_error_category_conflict)
    val errLength    = stringResource(R.string.tag_error_length)

    val filteredTags = availableTags.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.name in checkedTags
    }
    val searchHasNoMatches = searchQuery.isNotBlank() &&
        availableTags.none { it.name.contains(searchQuery, ignoreCase = true) }
    val tryCreateTag: (String) -> Unit = { raw ->
        val trimmed    = raw.trim()
        val normalized = trimmed.normalizeTag()
        when {
            trimmed.isBlank() -> {}
            trimmed.length !in TAG_MIN_LENGTH..TAG_MAX_LENGTH ->
                newTagError = errLength
            availableTags.any { tagConflictKey(it.name) == tagConflictKey(normalized) } ->
                newTagError = errDuplicate
            EntryCategory.entries.any { it.label.equals(normalized, ignoreCase = true) } ->
                newTagError = errCategory
            else -> {
                onAddTag(normalized)
                checkedTags = checkedTags + normalized
                newTagText  = ""
                newTagError = null
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
                .nestedScroll(sealScrollFromSheet)
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
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
                if (pendingCategory != null || checkedTags.isNotEmpty() || pendingFavorites || pendingAttachments || pendingTwoFactor || pendingPasskey || pendingProtected || pendingSearchBackups) {
                    TextButton(onClick = {
                        pendingCategory      = null
                        checkedTags          = emptySet()
                        pendingFavorites     = false
                        pendingAttachments   = false
                        pendingTwoFactor     = false
                        pendingPasskey       = false
                        pendingProtected     = false
                        pendingSearchBackups = false
                        onTwoFactorToggle(false)
                        onPasskeyToggle(false)
                        onProtectedToggle?.invoke(false)
                        onSearchBackupsToggle(false)
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

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        label         = stringResource(R.string.filter_favorites),
                        selected      = pendingFavorites,
                        selectedColor = ChipCategoryColor,
                        leadingIcon   = Icons.Default.Star,
                        onClick       = { pendingFavorites = !pendingFavorites }
                    )
                    CategoryChip(
                        label         = stringResource(R.string.filter_attachments),
                        selected      = pendingAttachments,
                        selectedColor = ChipCategoryColor,
                        leadingIcon   = Icons.Default.AttachFile,
                        onClick       = { pendingAttachments = !pendingAttachments }
                    )
                    if (show2FAPasskeyFilters) {
                        CategoryChip(
                            label         = stringResource(R.string.twofa_screen_title),
                            selected      = pendingTwoFactor,
                            selectedColor = ChipCategoryColor,
                            onClick       = { pendingTwoFactor = !pendingTwoFactor }
                        )
                    } else if (onProtectedToggle != null) {
                        CategoryChip(
                            label         = stringResource(R.string.filter_protected),
                            selected      = pendingProtected,
                            selectedColor = ChipCategoryColor,
                            leadingIcon   = Icons.Default.Lock,
                            onClick       = { pendingProtected = !pendingProtected }
                        )
                    }
                }
                if (show2FAPasskeyFilters) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryChip(
                            label         = stringResource(R.string.passkey_detail_title),
                            selected      = pendingPasskey,
                            selectedColor = ChipCategoryColor,
                            leadingIcon   = Icons.Default.Key,
                            onClick       = { pendingPasskey = !pendingPasskey }
                        )
                        if (onProtectedToggle != null) {
                            CategoryChip(
                                label         = stringResource(R.string.filter_protected),
                                selected      = pendingProtected,
                                selectedColor = ChipCategoryColor,
                                leadingIcon   = Icons.Default.Lock,
                                onClick       = { pendingProtected = !pendingProtected }
                            )
                        }
                    }
                }
            }

            if (showSearchBackupsOption) {
                HorizontalDivider(
                    modifier  = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pendingSearchBackups = !pendingSearchBackups }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Default.History,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = stringResource(R.string.search_backups_toggle_title),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text     = stringResource(R.string.search_backups_toggle_desc),
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked         = pendingSearchBackups,
                        onCheckedChange = { pendingSearchBackups = it }
                    )
                }
            }

            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            Text(
                text       = stringResource(R.string.filter_section_categories),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EntryCategory.entries
                    .filter { it != EntryCategory.SIN_CATEGORIZAR }
                    .forEach { cat ->
                        CategoryChip(
                            label    = stringResource(cat.labelRes),
                            selected = pendingCategory == cat,
                            onClick  = {
                                pendingCategory = if (pendingCategory == cat) null else cat
                            }
                        )
                    }
            }

            Text(
                text       = stringResource(R.string.filter_section_tags),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp)
            )

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
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape      = CircleShape,
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier
                .heightIn(max = 168.dp)
                .verticalScroll(rememberScrollState())
                .animateContentSize()) {
            if (filteredTags.isEmpty()) {
                TagSearchEmptyState(
                    query       = searchQuery,
                    canCreate   = canCreateTags,
                    onCreateTag = tryCreateTag
                )
            } else {
                filteredTags.forEach { tag ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                checkedTags = checkedTags.toMutableSet().apply {
                                    if (contains(tag.name)) remove(tag.name) else add(tag.name)
                                }
                            }
                            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = checkedTags.contains(tag.name),
                            onCheckedChange = {
                                checkedTags = checkedTags.toMutableSet().apply {
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
                            onClick  = { if (!isReadOnly) { renameTarget = tag; renameText = tag.name } },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector        = if (isReadOnly) Icons.Default.Lock else Icons.Default.Edit,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isReadOnly) 0.3f else 0.55f),
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                        TagDeleteButton(
                            tag      = tag,
                            onDelete = { checkedTags = checkedTags - tag.name; onDeleteTag(tag.id) }
                        )
                    }
                }

                if (searchHasNoMatches) {
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
                                onClick = {
                                    val trimmed    = renameText.trim()
                                    val normalized = trimmed.normalizeTag()
                                    when {
                                        trimmed.isBlank() -> {}
                                        trimmed.length !in TAG_MIN_LENGTH..TAG_MAX_LENGTH ->
                                            renameError = errLength
                                        availableTags.any { it.id != target.id && tagConflictKey(it.name) == tagConflictKey(normalized) } ->
                                            renameError = errDuplicate
                                        EntryCategory.entries.any { it.label.equals(normalized, ignoreCase = true) } ->
                                            renameError = errCategory
                                        else -> {
                                            if (checkedTags.contains(target.name)) {
                                                checkedTags = (checkedTags - target.name) + normalized
                                            }
                                            onRenameTag(target.id, normalized)
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTags.isNotEmpty()) {
                Button(
                    onClick  = {
                        onSearchBackupsToggle(pendingSearchBackups)
                        onTwoFactorToggle(pendingTwoFactor)
                        onPasskeyToggle(pendingPasskey)
                        onProtectedToggle?.invoke(pendingProtected)
                        onApply(pendingCategory, checkedTags, pendingFavorites, pendingAttachments)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    shape  = RoundedCornerShape(8.dp),
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

            Spacer(modifier = Modifier.height(8.dp))

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
                            text  = stringResource(R.string.vault_tag_name_placeholder),
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
                        cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier      = Modifier.fillMaxWidth()
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
                    color    = dangerColor(),
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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
                            text  = stringResource(R.string.vault_tag_name_placeholder),
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

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
internal fun ActiveFilterChip(
    label:       String,
    onRemove:    () -> Unit,
    accentColor: Color = ChipCategoryColor
) {
    Surface(
        onClick         = onRemove,
        shape           = RoundedCornerShape(8.dp),
        color           = accentColor.copy(alpha = 0.12f),
        contentColor    = accentColor,
        tonalElevation  = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines   = 1
            )
            Icon(
                imageVector        = Icons.Default.Close,
                contentDescription = stringResource(R.string.filter_remove),
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun StrengthFilterSheet(
    selectedFilters: Set<StrengthFilter>,
    counts: Map<StrengthFilter, Int> = emptyMap(),
    onApply: (Set<StrengthFilter>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var checked by remember { mutableStateOf(selectedFilters) }

    data class FilterOption(
        val filter: StrengthFilter,
        val color: Color,
        val label: String
    )

    val options = listOf(
        FilterOption(StrengthFilter.WEAK,        Color(0xFFE53935), stringResource(R.string.filter_critical)),
        FilterOption(StrengthFilter.MEDIUM,      Color(0xFFFFB300), stringResource(R.string.filter_medium)),
        FilterOption(StrengthFilter.STRONG,      Color(0xFF43A047), stringResource(R.string.filter_safe)),
        FilterOption(StrengthFilter.NO_PASSWORD, Color(0xFFBDBDBD), stringResource(R.string.filter_no_password))
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                text       = stringResource(R.string.vault_password_security_status),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            checked = checked.toMutableSet().apply {
                                if (contains(option.filter)) remove(option.filter) else add(option.filter)
                            }
                        }
                        .padding(start = 8.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked         = option.filter in checked,
                        onCheckedChange = {
                            checked = checked.toMutableSet().apply {
                                if (it) add(option.filter) else remove(option.filter)
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(option.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text  = counts[option.filter]?.let { "${option.label} ($it)" } ?: option.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick  = { onApply(checked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape  = RoundedCornerShape(8.dp),
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    selectedColor: Color = ChipCategoryColor,
    leadingIcon: ImageVector? = null,
    count: Int = 0
) {
    Surface(
        onClick         = onClick,
        shape           = RoundedCornerShape(8.dp),
        color           = when {
            locked   -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            selected -> selectedColor
            else     -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor    = when {
            locked   -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            selected -> Color.White
            else     -> MaterialTheme.colorScheme.onBackground
        },
        tonalElevation  = 0.dp,
        shadowElevation = 0.dp,
        modifier        = modifier
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier              = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector        = leadingIcon,
                    contentDescription = null,
                    modifier           = Modifier.size(14.dp)
                )
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

internal val ChipCategoryColor = Color(0xFF1E88E5)
internal val ChipTagColor      = Color(0xFF7B1FA2)

internal const val TAG_MIN_LENGTH    = 2
internal const val TAG_MAX_LENGTH    = 18
internal fun String.normalizeTag(): String =
    trim().replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }

private fun nombresDeEtiqueta(vararg nombres: String): Set<String> =
    nombres.map { it.trim().lowercase(Locale.ROOT) }.toSet()

private val DEFAULT_TAG_IMPORTANT = nombresDeEtiqueta(
    "Important", "Importante", "Důležité", "Wichtig", "महत्वपूर्ण", "Penting", "重要", "중요",
    "Belangrijk", "Ważne", "Важное", "Dôležité", "Viktigt", "Önemli", "Важливе"
)
private val DEFAULT_TAG_PERSONAL = nombresDeEtiqueta(
    "Personal", "Osobní", "Persönlich", "Personnel", "व्यक्तिगत", "Pribadi", "Personale", "個人", "개인",
    "Persoonlijk", "Osobiste", "Pessoal", "Личное", "Osobné", "Personligt", "Kişisel", "Особисте", "个人"
)
private val DEFAULT_TAG_WORK = nombresDeEtiqueta(
    "Work", "Trabajo", "Práce", "Arbeit", "Travail", "काम", "Kerja", "Lavoro", "仕事", "업무",
    "Werk", "Praca", "Trabalho", "Работа", "Práca", "Arbete", "İş", "Робота", "工作"
)

internal fun tagIcon(tag: String): ImageVector? {
    when (tag.trim().lowercase(Locale.ROOT)) {
        in DEFAULT_TAG_WORK      -> return Icons.Default.Work
        in DEFAULT_TAG_PERSONAL  -> return Icons.Default.Person
        in DEFAULT_TAG_IMPORTANT -> return Icons.Default.Star
    }
    val lower = tag.lowercase().trim()
    fun has(vararg kw: String) = kw.any { lower.contains(it) }
    return when {
        has("trabajo", "work", "laboral", "office", "job", "profesional", "empresa")
                                                         -> Icons.Default.Work
        has("personal")                                  -> Icons.Default.Person
        has("importante", "important", "urgente", "urgent", "priorit", "critico", "crítico")
                                                         -> Icons.Default.Star
        has("casa", "home", "hogar", "domicilio")        -> Icons.Default.Home
        has("finanza", "finance", "banco", "bank", "dinero", "money", "pago", "payment")
                                                         -> Icons.Default.AccountBalance
        has("salud", "health", "médico", "medico", "doctor", "clinic")
                                                         -> Icons.Default.Favorite
        has("familia", "family", "familiar")             -> Icons.Default.Group
        has("viaje", "travel", "trip", "vacacion", "vacation", "vuelo")
                                                         -> Icons.Default.Flight
        has("social", "redes", "network")                -> Icons.Default.Public
        has("gaming", "juego", "game", "videojuego")     -> Icons.Default.SportsEsports
        has("compra", "shop", "tienda", "store", "ecommerce")
                                                         -> Icons.Default.ShoppingCart
        has("estudio", "study", "escuela", "school", "universidad", "university", "educacion")
                                                         -> Icons.Default.School
        has("seguro", "insurance", "poliza", "póliza")   -> Icons.Default.Shield
        has("suscripcion", "suscripción", "subscription", "membresia", "membresía")
                                                         -> Icons.Default.Subscriptions
        else                                             -> null
    }
}

private val StrengthWeak   = PasswordStrengthWeak
private val StrengthMedium = PasswordStrengthFair
private val StrengthGood   = PasswordStrengthGood
private val StrengthStrong = PasswordStrengthStrong

private val AvatarPalette = listOf(
    Color(0xFFEF6C00),
    Color(0xFF8E24AA),
    Color(0xFF1E88E5),
    Color(0xFF43A047),
    Color(0xFFC2185B),
    Color(0xFF00897B),
    Color(0xFFD81B60),
    Color(0xFF7CB342),
    Color(0xFF5E35B1),
    Color(0xFFE53935),
    Color(0xFF3949AB),
    Color(0xFF6D4C41)
)

private fun avatarColorFor(title: String): Color {
    if (title.isEmpty()) return AvatarPalette[0]
    val hash = title.hashCode()
    val index = ((hash % AvatarPalette.size) + AvatarPalette.size) % AvatarPalette.size
    return AvatarPalette[index]
}

@Composable
internal fun ServiceAwareAvatar(
    title: String,
    url: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val icon = remember(url, title) {
        ServiceIconProvider.resolveIcon(url)
            ?: ServiceIconProvider.resolveByName(title)
    }
    if (icon != null) {
        if (icon.isAppIcon) {
            val context = LocalContext.current
            val painter = if (icon.res == R.mipmap.ic_launcher) {
                remember {
                    val appIcon = context.packageManager.getApplicationIcon(context.packageName)
                    val sizePx  = 108
                    val bmp     = androidx.core.graphics.createBitmap(sizePx, sizePx)
                    val canvas  = android.graphics.Canvas(bmp)
                    appIcon.setBounds(0, 0, sizePx, sizePx)
                    appIcon.draw(canvas)
                    BitmapPainter(bmp.asImageBitmap())
                }
            } else {
                painterResource(icon.res)
            }
            Image(
                painter            = painter,
                contentDescription = title,
                modifier           = modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            val brandColor = Color(icon.brandColorArgb)
            Box(
                modifier         = modifier
                    .size(size)
                    .background(brandColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val luminance = 0.2126f * brandColor.red + 0.7152f * brandColor.green + 0.0722f * brandColor.blue
                val iconTint  = if (luminance > 0.55f) Color(0xFF1A1A1AL) else Color.White
                val scaledIconSize = (icon.iconSizeDp * size.value / 36f).dp
                Image(
                    painter            = painterResource(icon.res),
                    contentDescription = title,
                    modifier           = Modifier.size(scaledIconSize),
                    colorFilter        = if (icon.tinted) ColorFilter.tint(iconTint) else null
                )
            }
        }
    } else {
        EntryAvatar(title = title, size = size, modifier = modifier)
    }
}

@Composable
internal fun ProtectedLockBadge(modifier: Modifier = Modifier, enLinea: Boolean = false) {
    val bg = MaterialTheme.colorScheme.background
    val green = if (contrastRatio(bg, SuccessGreen) < 3f) SuccessGreenBright else SuccessGreen
    Box(
        modifier = modifier
            .then(if (enLinea) Modifier else Modifier.offset(x = 4.dp, y = (-4).dp))
            .size(19.dp)
            .background(bg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Filled.Lock,
            contentDescription = null,
            tint               = green,
            modifier           = Modifier.size(13.dp)
        )
    }
}

@Composable
internal fun EntryAvatar(
    title: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val color = avatarColorFor(title)
    val letter = title.firstOrNull { it.isLetterOrDigit() }
        ?.uppercaseChar()
        ?.toString()
        ?: "?"
    val fontSize = (size.value * 0.44f).sp
    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = letter,
            color      = color,
            fontWeight = FontWeight.SemiBold,
            fontSize   = fontSize
        )
    }
}

private fun passwordStrength(password: String): Int =
    PasswordStrengthCalculator.calculate(password)

@Composable
private fun VaultEntryCard(
    entry:                   VaultEntry,
    modifier:                Modifier = Modifier,
    isFirst:                 Boolean = false,
    isLast:                  Boolean = false,
    isSelected:              Boolean = false,
    isSelectionMode:         Boolean = false,
    hasAttachment:           Boolean = false,
    hasTwoFactor:            Boolean = false,
    hasPasskey:              Boolean = false,
    onClick:                 () -> Unit,
    onLongClick:             () -> Unit = {},
    onFavoriteToggle:        () -> Unit = {},
    onEditClick:             () -> Unit = {},
    onPinToggle:             () -> Unit = {},
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
    val strength  = passwordStrength(entry.password)
    val lineColor = when {
        strength < 0  -> Color.Transparent
        strength <= 1 -> StrengthWeak
        strength == 2 -> StrengthMedium
        strength == 3 -> StrengthGood
        else          -> StrengthStrong
    }
    val ringColor = if (isSelected) MaterialTheme.colorScheme.primary else lineColor

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

    Column(modifier = modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(entryItemShape(isFirst, isLast))
            .background(entryCardColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 10.dp, end = 0.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked         = isSelected,
                onCheckedChange = { onClick() },
                modifier        = Modifier.size(44.dp)
            )
        } else {
            val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier.sharedElement(
                        sharedContentState      = rememberSharedContentState("entry_avatar_${entry.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            } else Modifier

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .drawBehind {
                        if (ringColor != Color.Transparent) {
                            val avatarRadius = 20.dp.toPx()
                            val strokeWidth  = 1.5.dp.toPx()
                            drawCircle(
                                color  = ringColor,
                                radius = avatarRadius + strokeWidth / 2f,
                                style  = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                ServiceAwareAvatar(
                    title    = entry.title,
                    url      = ServiceIconProvider.bestUrl(entry.urls),
                    size     = 40.dp,
                    modifier = avatarModifier
                )
                if (entry.isProtected) {
                    ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text       = entry.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            val subtitle = entry.email.trim().ifEmpty { entry.username.trim() }
            if (subtitle.isNotEmpty()) {
                Text(
                    text     = subtitle,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val hasCategory = entry.category != EntryCategory.SIN_CATEGORIZAR
            val hasTags     = entry.tags.isNotEmpty()
            if (hasCategory || hasTags) {
                EntryChipsRow(
                    category = if (hasCategory) entry.category else null,
                    tags     = entry.tags
                )
            }
        }

        if (hasTwoFactor || hasPasskey) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hasTwoFactor) {
                    Text(
                        text       = "2FA",
                        fontSize   = 10.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = PasswordStrengthStrong
                    )
                }
                if (hasTwoFactor && hasPasskey) {
                    Spacer(Modifier.height(2.dp))
                }
                if (hasPasskey) {
                    Icon(
                        imageVector        = Icons.Default.Key,
                        contentDescription = null,
                        tint               = PasswordStrengthFair,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.width(if (!hasAttachment && entry.isPinned) 14.dp else 4.dp))
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
        if (entry.isPinned) {
            Icon(
                imageVector        = Icons.Default.PushPin,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(2.dp))
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector        = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.action_options),
                    tint               = MaterialTheme.colorScheme.primary
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
                        onClick     = {
                            showMenu = false
                            if (entry.isProtected) protectedNotice = R.string.protect_locked_edit else onEditClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    DropdownMenuItem(
                        text        = { Text(if (entry.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin)) },
                        onClick     = { showMenu = false; onPinToggle() },
                        leadingIcon = {
                            Icon(
                                if (entry.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                DropdownMenuItem(
                    text        = { Text(if (isHubPinned) stringResource(R.string.hub_unpin_from_hub) else stringResource(R.string.hub_pin_to_hub)) },
                    onClick     = { showMenu = false; onToggleHubPin() },
                    leadingIcon = { Icon(if (isHubPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, null, modifier = Modifier.size(18.dp)) }
                )
                if (!isReadOnly) {
                    DropdownMenuItem(
                        text        = { Text(if (entry.isFavorite) stringResource(R.string.action_remove_favorite) else stringResource(R.string.action_favorite)) },
                        onClick     = { showMenu = false; onFavoriteToggle() },
                        leadingIcon = {
                            Icon(
                                if (entry.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(vertical = 2.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    DropdownMenuItem(
                        text        = { Text(stringResource(R.string.action_move_to)) },
                        onClick     = { showMenu = false; onMoveTo() },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, null, modifier = Modifier.size(18.dp)) }
                    )
                }
                DropdownMenuItem(
                    text        = { Text(stringResource(R.string.action_copy_to)) },
                    onClick     = { showMenu = false; onCopyTo() },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp)) }
                )
                if (!isReadOnly) {
                    HorizontalDivider(
                        modifier  = Modifier.padding(vertical = 2.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    DropdownMenuItem(
                        text        = { Text(stringResource(R.string.menu_archive)) },
                        onClick     = { showMenu = false; onArchive() },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = if (isArchiveLocked) ({
                            Icon(Icons.Default.Lock, contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint     = MaterialTheme.colorScheme.primary)
                        }) else null
                    )
                }
                HorizontalDivider(
                    modifier  = Modifier.padding(vertical = 2.dp),
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                DropdownMenuItem(
                    text        = { Text(stringResource(R.string.menu_delete), color = dangerColor()) },
                    onClick     = {
                        showMenu = false
                        if (entry.isProtected) protectedNotice = R.string.protect_locked_delete else showDeleteConfirm = true
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint     = dangerColor(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
    if (!isLast) {
        EntryGroupDivider(
            startInset = 58.dp,
            endInset   = 12.dp,
            cardColor  = entryCardColor,
            gapColor   = MaterialTheme.colorScheme.background
        )
    }
    }
}
@Composable
private fun EmptyVault(
    hasQuery:      Boolean,
    onlyFavorites: Boolean,
    hasFilters:    Boolean,
    isReadOnly:    Boolean,
    onAddEntry:    () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector        = when {
                    hasQuery      -> Icons.Default.SearchOff
                    hasFilters    -> Icons.Default.FilterList
                    onlyFavorites -> Icons.Default.FavoriteBorder
                    else          -> Icons.Default.Lock
                },
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier           = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when {
                    hasQuery      -> stringResource(R.string.vault_empty_search)
                    hasFilters    -> stringResource(R.string.vault_empty_filtered)
                    onlyFavorites -> stringResource(R.string.vault_empty_favorites)
                    else          -> stringResource(R.string.vault_empty_title)
                },
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            if (!hasQuery && !hasFilters && !onlyFavorites) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { if (!isReadOnly) onAddEntry() },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    shape  = RoundedCornerShape(8.dp)
                ) {
                    Icon(if (isReadOnly) Icons.Default.Lock else Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_add))
                }
            }
        }
    }
}

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
            Text(
                text     = "—",
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp)
            )
        } else {
            vaults.forEach { vault ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
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
                    Text(
                        text  = vault.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
