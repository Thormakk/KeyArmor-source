@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.twofactor

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Shield
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultMetadata
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
import com.thormakk.keyarmor.presentation.screens.vault.ProtectedLockBadge
import com.thormakk.keyarmor.presentation.screens.vault.ServiceAwareAvatar
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.presentation.util.TotpGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun TwoFactorScreen(
    onBack:        () -> Unit,
    onAddAccount:  () -> Unit,
    onEditAccount:    (String) -> Unit,
    onPlansClick:      () -> Unit = {},
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel:              TwoFactorViewModel     = hiltViewModel(),
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val isFreeUser       by viewModel.isFreeUser.collectAsStateWithLifecycle()
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
    val pinLimitReached   by viewModel.pinLimitReached.collectAsStateWithLifecycle()
    val pinMsg            = stringResource(R.string.pin_limit_reached)
    LaunchedEffect(pinLimitReached) {
        if (pinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(pinMsg) }
            viewModel.onPinLimitDismissed()
        }
    }
    var showLimitDialog  by remember { mutableStateOf(false) }

    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val isSelectionMode = selectedIds.isNotEmpty()
    val otherVaults           by viewModel.otherVaults.collectAsStateWithLifecycle()
    var bulkVaultActionIsMove  by remember { mutableStateOf(true) }
    var showBulkVaultSheet     by remember { mutableStateOf(false) }
    var showBulkTagSheet      by remember { mutableStateOf(false) }
    var showBulkCategorySheet by remember { mutableStateOf(false) }
    var showBulkUnlock          by remember { mutableStateOf(false) }
    var showBulkProtectPremium  by remember { mutableStateOf(false) }
    BackHandler(enabled = isSelectionMode) { viewModel.clearSelection() }

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
                        totalCount       = uiState.accounts.size,
                        onClearSelection = viewModel::clearSelection,
                        onSelectAll      = viewModel::selectAll,
                        onArchive        = viewModel::bulkArchive,
                        isArchiveLocked  = isFreeUser,
                        onArchiveLocked  = { showLimitDialog = true },
                        onDelete         = viewModel::bulkDelete,
                        onTag            = { showBulkTagSheet = true },
                        onCategorize     = { showBulkCategorySheet = true },
                        isReadOnly       = isActiveVaultReadOnly,
                        onMoveTo         = { bulkVaultActionIsMove = true;  showBulkVaultSheet = true },
                        onCopyTo         = { bulkVaultActionIsMove = false; showBulkVaultSheet = true },
                        protectedCount   = uiState.accounts.count { it.id in selectedIds && it.isProtected },
                        allProtected     = uiState.accounts.filter { it.id in selectedIds }.let { sel -> sel.isNotEmpty() && sel.all { it.isProtected } },
                        soleEntryTitle   = uiState.accounts.filter { it.id in selectedIds && !it.isProtected }
                                               .singleOrNull()?.let { (it.name.ifBlank { it.issuer }).ifBlank { null } },
                        onToggleProtect  = {
                            val sel = uiState.accounts.filter { it.id in selectedIds }
                            when {
                                sel.isNotEmpty() && sel.all { it.isProtected } -> showBulkUnlock = true
                                isFreeUser                                     -> showBulkProtectPremium = true
                                else                                           -> viewModel.bulkSetProtected(true)
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        actions = {
                            CaptureAllowedBadge()
                            ThemeTrialBadge()
                        },
                        windowInsets = WindowInsets(0),
                        title = {
                            TextoDeUnaLinea(
                                stringResource(R.string.twofa_screen_title),
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
                    .coachMarkAnchor("two_factor", "fab"),
                onClick = {
                    if (!isActiveVaultReadOnly) {
                        if (uiState.isAtFreeTotpLimit) showLimitDialog = true
                        else onAddAccount()
                    }
                },
                containerColor = VioletaDelFab,
                contentColor   = ContenidoDelFab
            ) {
                Icon(
                    imageVector        = if (isActiveVaultReadOnly || uiState.isAtFreeTotpLimit) Icons.Default.Lock else Icons.Default.Add,
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

        if (uiState.accounts.isEmpty() && uiState.fuzzyAccounts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    modifier            = Modifier.padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector        = Icons.Default.Pin,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier           = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text       = stringResource(R.string.twofa_empty_title),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (!isActiveVaultReadOnly) {
                                if (uiState.isAtFreeTotpLimit) showLimitDialog = true else onAddAccount()
                            }
                        },
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
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .coachMarkAnchor("two_factor", "code"),
            contentPadding      = PaddingValues(horizontal = 7.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(uiState.accounts, key = { _, it -> it.id }) { index, account ->
                val isFirst = index == 0
                val isLast  = index == uiState.accounts.lastIndex
                TwoFactorRow(
                    account         = account,
                    isFirst         = isFirst,
                    isLast          = isLast,
                    isSelected      = account.id in selectedIds,
                    isSelectionMode = isSelectionMode,
                    isReadOnly      = isActiveVaultReadOnly,
                    onClick         = { if (isSelectionMode) viewModel.toggleSelection(account.id) else onEditAccount(account.id) },
                    onLongClick     = { viewModel.toggleSelection(account.id) },
                    sharedTransitionScope   = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }

            if (uiState.fuzzyAccounts.isNotEmpty()) {
                item(key = "fuzzy_header") {
                    FuzzySectionDivider(stringResource(R.string.search_similar_results))
                }
                itemsIndexed(uiState.fuzzyAccounts, key = { _, it -> "fz_${it.id}" }) { index, account ->
                    val isFirst = index == 0
                    val isLast  = index == uiState.fuzzyAccounts.lastIndex
                    TwoFactorRow(
                        account         = account,
                        isFirst         = isFirst,
                        isLast          = isLast,
                        isSelected      = false,
                        isSelectionMode = false,
                        isReadOnly      = isActiveVaultReadOnly,
                        onClick         = { onEditAccount(account.id) },
                        onLongClick     = {},
                        sharedTransitionScope   = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }
        }
    }

    if (showBulkTagSheet) {
        BulkTagSheet(
            availableTags = uiState.accounts.flatMap { it.tags }.distinct(),
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
            vaults   = otherVaults,
            isMove   = bulkVaultActionIsMove,
            onSelect = { targetId ->
                showBulkVaultSheet = false
                if (bulkVaultActionIsMove) viewModel.bulkMoveTo(targetId)
                else                      viewModel.bulkCopyTo(targetId)
            },
            onDismiss = { showBulkVaultSheet = false }
        )
    }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            containerColor   = MaterialTheme.colorScheme.surface,
            title   = { Text(stringResource(R.string.twofa_free_limit_title), fontWeight = FontWeight.Bold) },
            text    = { Text(stringResource(R.string.twofa_free_limit_message, TWO_FACTOR_FREE_TOTP_LIMIT), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = { showLimitDialog = false; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.premium_gate_cta), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text(stringResource(R.string.action_understood))
                }
            }
        )
    }
}

@Composable
private fun TwoFactorRow(
    account:         TwoFactorAccount,
    isFirst:         Boolean = false,
    isLast:          Boolean = false,
    isSelected:      Boolean = false,
    isSelectionMode: Boolean = false,
    isReadOnly:      Boolean = false,
    onClick:         () -> Unit,
    onLongClick:     () -> Unit = {},
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val clipClear = LocalClipboardClearManager.current
    val clipContext = LocalContext.current
    val title = account.name.ifEmpty { account.issuer }

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(account.id, account.secret) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val totp = remember(account.secret, account.digits, account.period, account.algorithm, nowMillis) {
        if (account.hasTotp) TotpGenerator.generate(
            secret    = account.secret,
            digits    = account.digits,
            period    = account.period,
            algorithm = account.algorithm,
            nowMillis = nowMillis
        ) else null
    }

    val entryCardColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                         else MaterialTheme.colorScheme.surface

    Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(entryItemShape(isFirst, isLast))
            .background(entryCardColor)
            .combinedClickable(
                onClick     = onClick,
                onLongClick = onLongClick
            )
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
            Box(
                modifier         = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState      = rememberSharedContentState("twofa_avatar_${account.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier
                ServiceAwareAvatar(title = title, url = account.url, size = 40.dp, modifier = avatarModifier)
                if (account.isProtected) {
                    ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = title.ifEmpty { "—" },
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary,
                maxLines   = 1
            )
            if (account.accountLabel.isNotBlank()) {
                Text(
                    text     = account.accountLabel,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    maxLines = 1
                )
            }
            if (totp != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = TotpGenerator.formatCode(totp.code),
                    style      = MaterialTheme.typography.titleLarge.copy(
                        fontFeatureSettings = "tnum",
                        fontSize            = 33.sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color      = if (totp.secondsRemaining <= 5)
                                     dangerColor()
                                 else MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        if (totp != null) {
            val animatedProgress by animateFloatAsState(
                targetValue   = totp.progress,
                animationSpec = tween(durationMillis = 900, easing = LinearEasing),
                label         = "totp_progress"
            )
            val arcColor = if (totp.secondsRemaining <= 5)
                dangerColor()
            else
                MaterialTheme.colorScheme.primary

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                Canvas(modifier = Modifier.size(34.dp)) {
                    drawArc(
                        color      = arcColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter  = false,
                        style      = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color      = arcColor,
                        startAngle = -90f,
                        sweepAngle = -(1f - animatedProgress) * 360f,
                        useCenter  = false,
                        style      = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text  = "${totp.secondsRemaining}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = arcColor
                )
            }
            Spacer(Modifier.width(15.dp))
            IconButton(
                onClick  = {
                    ClipboardUtil.copySensitive(clipContext, totp.code)
                    clipClear.scheduleClear()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.twofa_copy_code_desc),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
    if (!isLast) {
        EntryGroupDivider(
            startInset = 64.dp,
            endInset   = 27.dp,
            cardColor  = entryCardColor,
            gapColor   = MaterialTheme.colorScheme.background
        )
    }
    }
}

@Composable
private fun MoveToVaultBottomSheet(
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
