@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.custom

import com.thormakk.keyarmor.presentation.components.ReadOnlyGateDialog
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Visibility
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.screens.settings.contrastRatio
import com.thormakk.keyarmor.ui.theme.SuccessGreen
import com.thormakk.keyarmor.ui.theme.SuccessGreenBright
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.domain.model.CustomField
import com.thormakk.keyarmor.domain.model.CustomFieldType
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.validate
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.InfoChip
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.chipCategoryColor
import com.thormakk.keyarmor.presentation.components.chipTagColor
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.infoColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.bank.DetailRow
import com.thormakk.keyarmor.presentation.screens.bank.EntryMetadata
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.presentation.util.rememberBiometricGate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

private val MergedCardRadius     = 14.dp
private val MergedCardStartInset = 16.dp
private val MergedCardEndInset   = 15.dp

@Composable
fun CustomEntryDetailScreen(
    onBack:             () -> Unit,
    onEdit:             (String) -> Unit,
    onDeleted:          () -> Unit,
    onCloneClick:       (String) -> Unit = {},
    onShareClick:       (String) -> Unit = {},
    onAttachmentsClick: (String) -> Unit = {},
    onPlansClick:       () -> Unit = {},
    viewModel:          AddEditCustomEntryViewModel = hiltViewModel()
) {
    val uiState         by viewModel.uiState.collectAsStateWithLifecycle()
    val availableTags   by viewModel.availableTags.collectAsStateWithLifecycle()
    val isFreeUser      by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val isReadOnly      by viewModel.isReadOnly.collectAsStateWithLifecycle()
    var showReadOnlyGate by remember { mutableStateOf(false) }
    if (showReadOnlyGate) ReadOnlyGateDialog(
        premiumSection = false,
        onDismiss = { showReadOnlyGate = false },
        onPlans = onPlansClick
    )
    val otherVaults       by viewModel.otherVaults.collectAsStateWithLifecycle()
    val pinLimitReached by viewModel.pinLimitReached.collectAsStateWithLifecycle()
    val isHubPinned        by viewModel.isHubPinned.collectAsStateWithLifecycle()
    val hubPinLimitReached by viewModel.hubPinLimitReached.collectAsStateWithLifecycle()
    val hubPinMsg          = stringResource(R.string.hub_pin_limit_toast)
    val clipCtx      = LocalContext.current
    val clipClear = LocalClipboardClearManager.current
    val context   = LocalContext.current

    val visibleSecrets = remember { mutableStateMapOf<String, Boolean>() }

    val customFieldGate = rememberBiometricGate(
        section              = BiometricPrefs.SECTION_CUSTOM,
        prefs                = viewModel.biometricPrefs,
        verifyMasterPassword = viewModel::verifyMasterPassword
    )
    val customSessionExpiry by customFieldGate.sessionExpiresAt

    LaunchedEffect(customSessionExpiry) {
        if (customSessionExpiry > 0L) {
            val remaining = customSessionExpiry - System.currentTimeMillis()
            if (remaining > 0) delay(remaining)
            visibleSecrets.keys.toList().forEach { visibleSecrets[it] = false }
        }
    }

    var showDeleteDialog             by remember { mutableStateOf(false) }
    var protectedNotice              by remember { mutableStateOf<Int?>(null) }
    var showUnlockDialog             by remember { mutableStateOf(false) }
    var showProtectPremium           by remember { mutableStateOf(false) }
    var showMenu                     by remember { mutableStateOf(false) }
    var showOptionsSheet             by remember { mutableStateOf(false) }
    var showVaultSheet               by remember { mutableStateOf(false) }
    var showRemoveAttributionDialog  by remember { mutableStateOf(false) }
    var vaultActionIsMove by remember { mutableStateOf(true) }
    var premiumFeature   by remember { mutableStateOf<String?>(null) }

    val labelAttachments  = stringResource(R.string.premium_attachments)
    val labelShare        = stringResource(R.string.menu_share)
    val labelArchive      = stringResource(R.string.plans_feature_archive)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    val pinMsg            = stringResource(R.string.pin_limit_reached)

    LaunchedEffect(uiState.isSaved)   { if (uiState.isSaved)   onDeleted() }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onDeleted() }
    LaunchedEffect(pinLimitReached) {
        if (pinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(pinMsg) }
            viewModel.onPinLimitDismissed()
        }
    }

    LaunchedEffect(hubPinLimitReached) {
        if (hubPinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(hubPinMsg) }
            viewModel.clearHubPinLimitReached()
        }
    }

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
            title = { Text(stringResource(if (uiState.isHidden) R.string.hidden_entries_delete_confirm_title else R.string.dialog_delete_custom_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(if (uiState.isHidden) R.string.hidden_entries_delete_confirm_message else R.string.dialog_delete_custom_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; viewModel.delete() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
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

    if (showOptionsSheet) {
        CustomEntryOptionsSheet(
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

    premiumFeature?.let { featureName ->
        AlertDialog(
            onDismissRequest = { premiumFeature = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(featureName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            },
            text = { Text(stringResource(R.string.premium_locked_dialog_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { premiumFeature = null; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary),
                    border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { premiumFeature = null }) {
                    Text(stringResource(R.string.action_understood), color = MaterialTheme.colorScheme.onBackground)
                }
            }
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
                        TextoDeUnaLinea(
                            text       = uiState.title.ifBlank { stringResource(R.string.custom_screen_title) },
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
                        IconButton(onClick = { viewModel.togglePin() }) {
                            Icon(
                                imageVector        = Icons.Default.PushPin,
                                contentDescription = null,
                                tint               = if (uiState.isPinned) MaterialTheme.colorScheme.primary
                                                      else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            if (isReadOnly) showReadOnlyGate = true else if (uiState.isProtected) protectedNotice = R.string.protect_locked_edit else onEdit(uiState.id)
                        }) {
                            androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center) { Icon(
                                imageVector        = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.action_edit),
                                tint               = MaterialTheme.colorScheme.primary.copy(alpha = if (isReadOnly) 0.38f else 1f)
                            ) }
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded         = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor   = MaterialTheme.colorScheme.background
                            ) {
                                DropdownMenuItem(
                                    text        = { Text(if (isHubPinned) stringResource(R.string.hub_unpin_from_hub) else stringResource(R.string.hub_pin_to_hub)) },
                                    leadingIcon = { Icon(if (isHubPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, null) },
                                    onClick     = { showMenu = false; viewModel.toggleHubPin() }
                                )
                                HorizontalDivider()
                                if (!isReadOnly) {
                                    DropdownMenuItem(
                                        text        = { Text(stringResource(R.string.menu_categories_tags)) },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) },
                                        onClick     = { showMenu = false; showOptionsSheet = true }
                                    )
                                    DropdownMenuItem(
                                        text        = { Text(labelAttachments) },
                                        leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                        onClick     = { showMenu = false; onAttachmentsClick(uiState.id) }
                                    )
                                    DropdownMenuItem(
                                        text        = { Text(stringResource(R.string.menu_clone)) },
                                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                        onClick     = { showMenu = false; onCloneClick(uiState.id) }
                                    )
                                    DropdownMenuItem(
                                        text        = { Text(labelShare) },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                        onClick = {
                                            showMenu = false
                                            onShareClick(uiState.id)
                                        }
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
                                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                                            onClick     = {
                                                showMenu = false
                                                if (isFreeUser) premiumFeature = labelArchive else viewModel.archive()
                                            }
                                        )
                                    }
                                }
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.menu_delete), color = dangerColor()) },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = dangerColor()) },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val mergedGapColor  = MaterialTheme.colorScheme.background
            val visibleFields = uiState.fields.filter { it.value.isNotBlank() }
            val heroIsLast    = visibleFields.isEmpty()

            Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape          = entryItemShape(isFirst = true, isLast = heroIsLast, radius = MergedCardRadius),
                color          = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier       = Modifier.fillMaxWidth()
            ) {
                Column {
                    if (uiState.title.isNotBlank()) {
                        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                            DetailRow(
                                label = stringResource(R.string.custom_field_name_placeholder),
                                value = uiState.title
                            )
                            if ((!isReadOnly || uiState.isProtected) && !uiState.isNew) {
                                val greenLock = if (contrastRatio(MaterialTheme.colorScheme.background, SuccessGreen) < 3f)
                                    SuccessGreenBright else SuccessGreen
                                IconButton(
                                    onClick = {
                                        when {
                                            uiState.isProtected      -> showUnlockDialog = true
                                            viewModel.canProtectNew  -> viewModel.setProtected(true)
                                            else                     -> showProtectPremium = true
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 36.dp).size(36.dp)
                                ) {
                                    Icon(
                                        imageVector        = if (uiState.isProtected) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = stringResource(if (uiState.isProtected) R.string.protect_cd_unlock else R.string.protect_cd_lock),
                                        tint               = if (uiState.isProtected) greenLock else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick  = { if (!isReadOnly) viewModel.saveFavoriteOnly(!uiState.isFavorite) },
                                modifier = Modifier.align(Alignment.CenterEnd).size(36.dp)
                            ) {
                                Icon(
                                    imageVector        = if (uiState.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = stringResource(R.string.action_favorite),
                                    tint               = if (uiState.isFavorite) MaterialTheme.colorScheme.primary
                                                          else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
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
            if (!heroIsLast) {
                EntryGroupDivider(
                    startInset = 0.dp,
                    endInset   = 0.dp,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )
            }

            visibleFields.forEachIndexed { index, field ->
                val isLastField = index == visibleFields.lastIndex
                CustomFieldDetailRow(
                    field       = field,
                    shape       = entryItemShape(isFirst = false, isLast = isLastField, radius = MergedCardRadius),
                    isVisible   = visibleSecrets[field.id] == true,
                    onToggleVisibility = {
                        if (visibleSecrets[field.id] == true) {
                            visibleSecrets[field.id] = false
                        } else if (field.type.isSecret) {
                            customFieldGate.launch { visibleSecrets[field.id] = true }
                        } else {
                            visibleSecrets[field.id] = true
                        }
                    },
                    onCopy      = {
                        val doCopy = {
                            ClipboardUtil.copySensitive(clipCtx, field.value)
                            clipClear.scheduleClear()
                        }
                        if (field.type.isSecret) customFieldGate.launch(doCopy) else doCopy()
                    },
                    onDial      = {
                        val dialUri = "tel:${field.value.replace(" ", "")}".toUri()
                        context.startActivity(Intent(Intent.ACTION_DIAL, dialUri))
                    },
                    onOpenUrl   = {
                        runCatching {
                            val url = if (field.value.startsWith("http")) field.value else "https://${field.value}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        }
                    }
                )
                if (!isLastField) {
                    EntryGroupDivider(
                        startInset = MergedCardStartInset,
                        endInset   = MergedCardEndInset,
                        cardColor  = mergedCardColor,
                        gapColor   = mergedGapColor
                    )
                }
            }
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
                                border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
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
                            onCheckedChange = viewModel::saveTravelSafeOnly
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
                                onCheckedChange = viewModel::saveHiddenOnly,
                                enabled         = uiState.isHidden || viewModel.canHideNewEntries
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CustomFieldDetailRow(
    field:             CustomField,
    isVisible:         Boolean,
    onToggleVisibility: () -> Unit,
    onCopy:            () -> Unit,
    onDial:            () -> Unit,
    onOpenUrl:         () -> Unit,
    shape:             Shape = RoundedCornerShape(8.dp)
) {
    val type        = field.type
    val displayValue = when (type) {
        CustomFieldType.DATE -> {
            val digits = field.value.filter { it.isDigit() }
            if (digits.length >= 5) "${digits.take(2)}/${digits.drop(2).take(2)}/${digits.drop(4)}"
            else field.value
        }
        else -> field.value
    }
    val isValid = type.validate(field.value)

    Surface(
        shape    = shape,
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(
                start  = 16.dp,
                end    = if (type.isSecret || type.hasCopy || type == CustomFieldType.URL || type == CustomFieldType.PHONE) 4.dp else 16.dp,
                top    = 12.dp,
                bottom = 12.dp
            ),
            verticalAlignment = if (type.isMultiline) Alignment.Top else Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = field.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text  = if (type.isSecret && !isVisible) "••••••••" else displayValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (type.isSecret) {
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector        = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isVisible) stringResource(R.string.action_hide) else stringResource(R.string.action_show),
                        tint               = if (!isVisible) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
            if (type.hasCopy) {
                IconButton(
                    onClick  = onCopy,
                    enabled  = field.value.isNotEmpty(),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.action_copy),
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
            if (type == CustomFieldType.URL) {
                val canOpen = field.value.isNotBlank() && isValid
                IconButton(onClick = onOpenUrl, enabled = canOpen, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.action_open_url),
                        tint               = if (canOpen) MaterialTheme.colorScheme.primary
                                             else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
            if (type == CustomFieldType.PHONE) {
                val canDial = field.value.isNotBlank() && isValid
                IconButton(onClick = onDial, enabled = canDial, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector        = Icons.Default.Call,
                        contentDescription = null,
                        tint               = if (canDial) MaterialTheme.colorScheme.primary
                                             else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
