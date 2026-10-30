@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
package com.thormakk.keyarmor.presentation.screens.passkeys

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.screens.settings.contrastRatio
import com.thormakk.keyarmor.ui.theme.SuccessGreen
import com.thormakk.keyarmor.ui.theme.SuccessGreenBright
import com.thormakk.keyarmor.presentation.screens.vault.ProtectedLockBadge
import com.thormakk.keyarmor.presentation.screens.vault.ServiceAwareAvatar
import com.thormakk.keyarmor.presentation.util.formatDateLocalized
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Suppress("DEPRECATION")
@Composable
fun PasskeysScreen(
    onBack:                  () -> Unit,
    onPasskeyClick:          (String) -> Unit = {},
    onPasskeyEditClick:      (String) -> Unit = {},
    viewModel:               PasskeysViewModel = hiltViewModel(),
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState               by viewModel.uiState.collectAsStateWithLifecycle()
    val hasDriveBackup        by viewModel.hasDriveBackup.collectAsStateWithLifecycle()
    val isProviderEnabled     by viewModel.isProviderEnabled.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val isFreeUser            by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val otherVaults           by viewModel.otherVaults.collectAsStateWithLifecycle()
    val snackbarHost       = remember { SnackbarHostState() }
    val context             = LocalContext.current

    var showProviderFallbackDialog by remember { mutableStateOf(false) }
    val activateProvider: () -> Unit = {
        try {
            context.startActivity(Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                data = "package:${context.packageName}".toUri()
            })
        } catch (_: ActivityNotFoundException) {
            showProviderFallbackDialog = true
        }
    }

    var entryActionId       by remember { mutableStateOf("") }
    var entryActionIsMove   by remember { mutableStateOf(true) }
    var showEntryVaultSheet by remember { mutableStateOf(false) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.checkProviderEnabled()
        }
    }

    LaunchedEffect(uiState.snackbar) {
        uiState.snackbar?.let {
            snackbarHost.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    if (showProviderFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showProviderFallbackDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text       = stringResource(R.string.passkey_manual_activate_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text  = stringResource(R.string.passkey_manual_activate_body_settings),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProviderFallbackDialog = false
                        try { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
                        catch (_: ActivityNotFoundException) { }
                    },
                    shape  = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.passkey_open_settings_btn), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showProviderFallbackDialog = false }) {
                    Text(stringResource(R.string.action_close), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHost, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                TopAppBar(
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        @Suppress("InlinedApi")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            val passkeyStatusText = stringResource(
                                if (isProviderEnabled) R.string.settings_status_enabled
                                else R.string.settings_status_disabled
                            )
                            IconButton(onClick = activateProvider) {
                                Icon(
                                    imageVector        = Icons.Default.Key,
                                    contentDescription =
                                        stringResource(R.string.passkeys_title) + " · " + passkeyStatusText,
                                    tint = if (isProviderEnabled) statusGreen() else warningColor(),
                                )
                            }
                        }
                    },
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            text       = stringResource(R.string.passkeys_title),
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.passkeys.isEmpty() -> {
                Column(modifier = Modifier.padding(padding)) {
                    if (!isProviderEnabled) {
                        ProviderNotEnabledBanner(onActivateClick = activateProvider)
                    }
                    PasskeysEmptyState(modifier = Modifier.weight(1f))
                }
            }
            else -> {
                var showSortMenu by remember { mutableStateOf(false) }
                LazyColumn(
                    modifier            = Modifier
                        .padding(padding)
                        .padding(horizontal = 7.dp),
                    contentPadding      = PaddingValues(top = 4.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    if (!isProviderEnabled) {
                        item { ProviderNotEnabledBanner(onActivateClick = activateProvider) }
                    }
                    item {
                        Box(Modifier.coachMarkAnchor("passkeys", "list")) {
                            PasskeysInfoBanner(hasDriveBackup = hasDriveBackup)
                        }
                    }

                    item {
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val count = uiState.passkeys.size
                            Text(
                                text  = if (count == 1) stringResource(R.string.passkey_count_one) else stringResource(R.string.passkey_count, count),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.weight(1f))
                            val sortNewestLabel = stringResource(R.string.vault_sort_newest_short)
                            val sortOldestLabel = stringResource(R.string.vault_sort_oldest_short)
                            val sortAzLabel     = stringResource(R.string.vault_sort_az)
                            val sortZaLabel     = stringResource(R.string.vault_sort_za)
                            Box {
                                TextButton(
                                    onClick        = { showSortMenu = true },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text  = when (uiState.sortOrder) {
                                            PasskeySortOrder.NEWEST -> sortNewestLabel
                                            PasskeySortOrder.OLDEST -> sortOldestLabel
                                            PasskeySortOrder.A_Z    -> sortAzLabel
                                            PasskeySortOrder.Z_A    -> sortZaLabel
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                DropdownMenu(
                                    expanded         = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    containerColor   = MaterialTheme.colorScheme.background
                                ) {
                                    listOf(
                                        PasskeySortOrder.NEWEST to sortNewestLabel,
                                        PasskeySortOrder.OLDEST to sortOldestLabel,
                                        PasskeySortOrder.A_Z    to sortAzLabel,
                                        PasskeySortOrder.Z_A    to sortZaLabel
                                    ).forEach { (order, label) ->
                                        DropdownMenuItem(
                                            text    = { Text(label, color = if (uiState.sortOrder == order) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                                            onClick = { viewModel.setSortOrder(order); showSortMenu = false }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(
                        items = uiState.sorted,
                        key   = { _, it -> it.credentialId }
                    ) { index, passkey ->
                        val isFirst = index == 0
                        val isLast  = index == uiState.sorted.lastIndex
                        PasskeyItem(
                            passkey                  = passkey,
                            isFirst                  = isFirst,
                            isLast                   = isLast,
                            isReadOnly               = isActiveVaultReadOnly,
                            onClick                  = { onPasskeyClick(passkey.credentialId) },
                            onEditClick              = { onPasskeyEditClick(passkey.credentialId) },
                            onPinToggle              = { viewModel.togglePin(passkey.credentialId, passkey.isPinned) },
                            onArchive                = { viewModel.archive(passkey.credentialId) },
                            isArchiveLocked          = isFreeUser,
                            onDelete                 = { viewModel.delete(passkey.credentialId) },
                            onMoveTo                 = { entryActionId = passkey.credentialId; entryActionIsMove = true;  showEntryVaultSheet = true },
                            onCopyTo                 = { entryActionId = passkey.credentialId; entryActionIsMove = false; showEntryVaultSheet = true },
                            sharedTransitionScope    = sharedTransitionScope,
                            animatedVisibilityScope  = animatedVisibilityScope
                        )
                    }
                }
            }
        }
    }

    if (showEntryVaultSheet) {
        MoveToVaultBottomSheet(
            vaults   = otherVaults,
            isMove   = entryActionIsMove,
            onSelect = { targetId ->
                showEntryVaultSheet = false
                if (entryActionIsMove) viewModel.moveEntryTo(entryActionId, targetId)
                else                  viewModel.copyEntryTo(entryActionId, targetId)
            },
            onDismiss = { showEntryVaultSheet = false }
        )
    }
}

@Composable
private fun ProviderNotEnabledBanner(onActivateClick: () -> Unit) {
    val passkeyWarningColor = warningColor()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = passkeyWarningColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = passkeyWarningColor,
                modifier           = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(R.string.passkey_provider_disabled_title),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = passkeyWarningColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = stringResource(R.string.passkey_provider_disabled_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = passkeyWarningColor
                )
                @Suppress("InlinedApi")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onActivateClick,
                        colors  = ButtonDefaults.outlinedButtonColors(
                            contentColor = passkeyWarningColor
                        ),
                        border  = androidx.compose.foundation.BorderStroke(
                            1.dp, passkeyWarningColor.copy(alpha = 0.5f)
                        ),
                        shape   = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Settings,
                            contentDescription = null,
                            modifier           = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = stringResource(R.string.passkey_activate_settings_btn),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun statusGreen(): androidx.compose.ui.graphics.Color =
    if (contrastRatio(MaterialTheme.colorScheme.background, SuccessGreen) < 3f) SuccessGreenBright
    else SuccessGreen

@Composable
private fun PasskeysInfoBanner(hasDriveBackup: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.Key,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = if (hasDriveBackup)
                        stringResource(R.string.passkey_info_title_backed_up)
                    else
                        stringResource(R.string.passkey_info_title_plain),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (hasDriveBackup)
                        stringResource(R.string.passkey_info_body_backed_up)
                    else
                        stringResource(R.string.passkey_info_body_plain),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun PasskeyItem(
    passkey:                 PasskeyCredential,
    isFirst:                 Boolean = false,
    isLast:                  Boolean = false,
    isReadOnly:              Boolean = false,
    onClick:                 () -> Unit = {},
    onEditClick:             () -> Unit = {},
    onPinToggle:             () -> Unit = {},
    onArchive:               () -> Unit = {},
    isArchiveLocked:         Boolean = false,
    onDelete:                () -> Unit = {},
    onMoveTo:                () -> Unit = {},
    onCopyTo:                () -> Unit = {},
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
            title = { Text(stringResource(R.string.protect_locked_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
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

    val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState      = rememberSharedContentState("passkey_avatar_${passkey.credentialId}"),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    } else Modifier

    Column {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(entryItemShape(isFirst, isLast))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            ServiceAwareAvatar(
                title    = passkey.displayName,
                url      = "https://${passkey.rpId}",
                modifier = avatarModifier
            )
            if (passkey.isProtected) {
                ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = passkey.displayName,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            if (passkey.accountLabel.isNotBlank()) {
                Text(
                    text     = passkey.accountLabel,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text  = stringResource(R.string.passkey_created_on, formatDate(passkey.createdAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (passkey.isPinned) {
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector        = Icons.Default.PushPin,
                contentDescription = stringResource(R.string.label_pinned),
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(16.dp)
            )
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_options),
                    tint = MaterialTheme.colorScheme.primary)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false },
                containerColor = MaterialTheme.colorScheme.surface) {
                if (!isReadOnly) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_edit)) },
                        onClick = {
                            showMenu = false
                            if (passkey.isProtected) protectedNotice = R.string.protect_locked_edit else onEditClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) })
                    DropdownMenuItem(
                        text = { Text(if (passkey.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin)) },
                        onClick = { showMenu = false; onPinToggle() },
                        leadingIcon = { Icon(Icons.Default.PushPin, null, modifier = Modifier.size(18.dp)) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_move_to)) },
                        onClick = { showMenu = false; onMoveTo() },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, null, modifier = Modifier.size(18.dp)) })
                }
                DropdownMenuItem(text = { Text(stringResource(R.string.action_copy_to)) },
                    onClick = { showMenu = false; onCopyTo() },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp)) })
                if (!isReadOnly) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.menu_archive)) },
                        onClick = { showMenu = false; onArchive() },
                        enabled = !isArchiveLocked,
                        leadingIcon = { Icon(Icons.Default.Archive, null, modifier = Modifier.size(18.dp)) })
                }
                HorizontalDivider(thickness = 0.5.dp)
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_delete), color = dangerColor()) },
                    onClick = {
                        showMenu = false
                        if (passkey.isProtected) protectedNotice = R.string.protect_locked_delete else showDeleteConfirm = true
                    },
                    leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = dangerColor(), modifier = Modifier.size(18.dp)) })
            }
        }
    }
    if (!isLast) {
        EntryGroupDivider(
            startInset = 64.dp,
            endInset   = 28.dp,
            cardColor  = MaterialTheme.colorScheme.surface,
            gapColor   = MaterialTheme.colorScheme.background
        )
    }
    }
}

@Composable
private fun PasskeysEmptyState(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showFallbackDialog by remember { mutableStateOf(false) }

    if (showFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showFallbackDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text       = stringResource(R.string.passkey_manual_activate_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text  = stringResource(R.string.passkey_manual_activate_body_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                TextButton(onClick = { showFallbackDialog = false }) {
                    Text(stringResource(R.string.action_understood))
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .coachMarkAnchor("passkeys", "empty")
        ) {
            Icon(
                imageVector        = Icons.Default.Key,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text      = stringResource(R.string.passkey_empty_title),
                style     = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.passkey_empty_subtitle),
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startActivity(Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                                data = "package:${context.packageName}".toUri()
                            })
                        } else {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    } catch (e: ActivityNotFoundException) {
                        showFallbackDialog = true
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.passkey_activate_provider_btn))
            }
        }
    }
}

private fun formatDate(millis: Long): String =
    formatDateLocalized(millis, Locale.getDefault())

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
