@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.vehicle

import com.thormakk.keyarmor.presentation.components.ReadOnlyGateDialog
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.ui.theme.SuccessGreenBright
import com.thormakk.keyarmor.ui.theme.SuccessGreen
import com.thormakk.keyarmor.presentation.screens.settings.contrastRatio
import androidx.compose.material.icons.filled.LockOpen
import com.thormakk.keyarmor.domain.model.EntryCategory
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
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

private val MergedCardRadius     = 14.dp
private val MergedCardStartInset = 16.dp
private val MergedCardEndInset   = 15.dp

private sealed interface VehicleCardItem {
    data class Title(val text: String) : VehicleCardItem
    data class Row(
        val label:     String,
        val value:     String,
        val onCopy:    (() -> Unit)?,
        val multiline: Boolean = false
    ) : VehicleCardItem
}

@Composable
fun VehicleDetailScreen(
    onBack:             () -> Unit,
    onEdit:             (String) -> Unit,
    onDeleted:          () -> Unit,
    onPlansClick:       () -> Unit = {},
    onAttachmentsClick: (String) -> Unit = {},
    onShareClick:       (String) -> Unit = {},
    viewModel: AddEditVehicleViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val isFreeUser    by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val isReadOnly    by viewModel.isReadOnly.collectAsStateWithLifecycle()
    val isFreeUserForGate by viewModel.isFreeUser.collectAsStateWithLifecycle()
    var showReadOnlyGate by remember { mutableStateOf(false) }
    if (showReadOnlyGate) ReadOnlyGateDialog(
        premiumSection = isFreeUserForGate,
        onDismiss = { showReadOnlyGate = false },
        onPlans = onPlansClick
    )
    val otherVaults   by viewModel.otherVaults.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val clipCtx = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val isHubPinned        by viewModel.isHubPinned.collectAsStateWithLifecycle()
    val hubPinLimitReached by viewModel.hubPinLimitReached.collectAsStateWithLifecycle()
    val hubPinMsg          = stringResource(R.string.hub_pin_limit_toast)
    val scope              = rememberCoroutineScope()
    LaunchedEffect(hubPinLimitReached) {
        if (hubPinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(hubPinMsg) }
            viewModel.clearHubPinLimitReached()
        }
    }

    LaunchedEffect(uiState.isSaved)   { if (uiState.isSaved)   onDeleted() }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onDeleted() }

    var showDeleteDialog             by remember { mutableStateOf(false) }
    var protectedNotice              by remember { mutableStateOf<Int?>(null) }
    var showUnlockDialog             by remember { mutableStateOf(false) }
    var showProtectPremium           by remember { mutableStateOf(false) }
    var showMenu                     by remember { mutableStateOf(false) }
    var showArchiveLockedDialog      by remember { mutableStateOf(false) }
    var showOptionsSheet             by remember { mutableStateOf(false) }
    var showVaultSheet               by remember { mutableStateOf(false) }
    var showRemoveAttributionDialog  by remember { mutableStateOf(false) }
    var vaultActionIsMove       by remember { mutableStateOf(true) }

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
            title = { Text(stringResource(if (uiState.isHidden) R.string.hidden_entries_delete_confirm_title else R.string.dialog_delete_vehicle_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(if (uiState.isHidden) R.string.hidden_entries_delete_confirm_message else R.string.dialog_delete_vehicle_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(onClick = { showDeleteDialog = false; viewModel.delete() }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(if (uiState.isHidden) R.string.action_delete else R.string.action_move_to_trash))
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground) } }
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
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveLockedDialog = false }) {
                    Text(stringResource(R.string.action_understood), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showOptionsSheet) {
        VehicleOptionsSheet(
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

    val vehicleFallbackTitle = stringResource(R.string.vehicle_fallback_title)
    val title = listOf(uiState.make, uiState.model).filter { it.isNotEmpty() }.joinToString(" ").ifEmpty { vehicleFallbackTitle }

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
                Spacer(modifier = Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(MaterialTheme.colorScheme.surface))
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = { TextoDeUnaLinea(title,
                        style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                        fontWeight = FontWeight.SemiBold,
                        color      = sectionTitleColor()) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = MaterialTheme.colorScheme.onSurface) }
                    },
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        IconButton(onClick = { if (isReadOnly) showReadOnlyGate = true else if (uiState.isProtected) protectedNotice = R.string.protect_locked_edit else onEdit(uiState.id) }) { androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isReadOnly) 0.38f else 1f)) } }
                        Box {
                            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.action_more_options), tint = MaterialTheme.colorScheme.onSurface) }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = MaterialTheme.colorScheme.surface) {
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
                                        text        = { Text(stringResource(R.string.premium_attachments)) },
                                        leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                        onClick     = { showMenu = false; onAttachmentsClick(uiState.id) }
                                    )
                                    DropdownMenuItem(
                                        text        = { Text(stringResource(R.string.menu_share)) },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                        onClick     = { showMenu = false; onShareClick(uiState.id) }
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
                                            text = { Text(stringResource(R.string.menu_archive)) },
                                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                                            onClick = {
                                                showMenu = false
                                                if (isFreeUser) showArchiveLockedDialog = true else viewModel.archive()
                                            }
                                        )
                                    }
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_move_to_trash), color = dangerColor()) },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = dangerColor()) },
                                    onClick = {
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
        val sortedWarningDays = remember(viewModel.expiryWarningDays) { viewModel.expiryWarningDays.sorted() }
        val alertLabel = if (sortedWarningDays.isEmpty()) "" else stringResource(R.string.vehicle_alert_days_before, sortedWarningDays.joinToString(", ") { "$it" })
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 7.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val mergedGapColor  = MaterialTheme.colorScheme.background

            val brandModel = listOf(uiState.make, uiState.model).filter { it.isNotEmpty() }.joinToString(" – ")
            val heroShown  = brandModel.isNotEmpty() || uiState.inheritedFrom != null
            val hasYear    = uiState.year.isNotEmpty()
            val hasPlate   = uiState.licensePlate.isNotEmpty()
            val hasVin     = uiState.vin.isNotEmpty()

            val hasInspection    = uiState.inspectionExpiryDate.isNotEmpty()
            val hasInsurance     = uiState.insuranceExpiryDate.isNotEmpty() || uiState.insuranceCompany.isNotEmpty() || uiState.policyNumber.isNotEmpty()
            val hasDriverLicense = uiState.driverLicenseExpiryDate.isNotEmpty()
            val hasNotes         = uiState.notes.isNotEmpty()

            val items = buildList<VehicleCardItem> {
                if (hasYear)  add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_year), uiState.year, { ClipboardUtil.copySensitive(clipCtx, uiState.year) }))
                if (hasPlate) add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_license_plate), uiState.licensePlate, { ClipboardUtil.copySensitive(clipCtx, uiState.licensePlate) }))
                if (hasVin)   add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_vin), uiState.vin, { ClipboardUtil.copySensitive(clipCtx, uiState.vin) }))
                if (hasInspection) {
                    add(VehicleCardItem.Title(stringResource(R.string.vehicle_field_inspection)))
                    add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_expiry_date), uiState.inspectionExpiryDate, { ClipboardUtil.copySensitive(clipCtx, uiState.inspectionExpiryDate) }))
                    if (alertLabel.isNotEmpty()) add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_alert), alertLabel, null))
                }
                if (hasInsurance) {
                    add(VehicleCardItem.Title(stringResource(R.string.vehicle_field_insurance)))
                    if (uiState.insuranceCompany.isNotEmpty()) add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_company), uiState.insuranceCompany, { ClipboardUtil.copySensitive(clipCtx, uiState.insuranceCompany) }))
                    if (uiState.policyNumber.isNotEmpty())     add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_policy_number), uiState.policyNumber, { ClipboardUtil.copySensitive(clipCtx, uiState.policyNumber) }))
                    if (uiState.insuranceExpiryDate.isNotEmpty()) {
                        add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_expiry_date), uiState.insuranceExpiryDate, { ClipboardUtil.copySensitive(clipCtx, uiState.insuranceExpiryDate) }))
                        if (alertLabel.isNotEmpty()) add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_alert), alertLabel, null))
                    }
                }
                if (hasDriverLicense) {
                    add(VehicleCardItem.Title(stringResource(R.string.vehicle_field_driver_license)))
                    add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_expiry_date), uiState.driverLicenseExpiryDate, { ClipboardUtil.copySensitive(clipCtx, uiState.driverLicenseExpiryDate) }))
                    if (alertLabel.isNotEmpty()) add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_alert), alertLabel, null))
                }
                if (hasNotes) add(VehicleCardItem.Row(stringResource(R.string.vehicle_field_notes), uiState.notes, { ClipboardUtil.copySensitive(clipCtx, uiState.notes) }, multiline = true))
            }

            if (heroShown || items.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (heroShown) {
                        val heroIsLast = items.isEmpty()
                        Surface(
                            shape          = entryItemShape(isFirst = true, isLast = heroIsLast, radius = MergedCardRadius),
                            color          = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier       = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                if (brandModel.isNotEmpty()) {
                                    Row(
                                        modifier          = Modifier.padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        VehicleAwareAvatar(make = uiState.make, size = 40.dp)
                                        Spacer(Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(vehicleFallbackTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(Modifier.height(5.dp))
                                            Text(brandModel, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        if ((!isReadOnly || uiState.isProtected) && !uiState.isNew) {
                                            val greenLock = if (contrastRatio(MaterialTheme.colorScheme.background, SuccessGreen) < 3f)
                                                SuccessGreenBright else SuccessGreen
                                            IconButton(onClick = {
                                                when {
                                                    uiState.isProtected      -> showUnlockDialog = true
                                                    viewModel.canProtectNew  -> viewModel.setProtected(true)
                                                    else                     -> showProtectPremium = true
                                                }
                                            }, modifier = Modifier.size(40.dp)) {
                                                Icon(
                                                    imageVector        = if (uiState.isProtected) Icons.Default.Lock else Icons.Default.LockOpen,
                                                    contentDescription = stringResource(if (uiState.isProtected) R.string.protect_cd_unlock else R.string.protect_cd_lock),
                                                    tint               = if (uiState.isProtected) greenLock else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(onClick = { viewModel.setFavorite(!uiState.isFavorite) }, modifier = Modifier.size(40.dp)) {
                                            Icon(
                                                imageVector        = if (uiState.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = if (uiState.isFavorite) stringResource(R.string.action_remove_favorite) else stringResource(R.string.action_add_favorite),
                                                tint               = if (uiState.isFavorite) MaterialTheme.colorScheme.primary
                                                                     else MaterialTheme.colorScheme.onSurfaceVariant
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
                            EntryGroupDivider(startInset = 0.dp, endInset = 0.dp, cardColor = mergedCardColor, gapColor = mergedGapColor)
                        }
                    }
                    items.forEachIndexed { index, item ->
                        val isFirstItem = !heroShown && index == 0
                        val isLastItem  = index == items.lastIndex
                        when (item) {
                            is VehicleCardItem.Title -> {
                                Surface(
                                    shape    = entryItemShape(isFirst = isFirstItem, isLast = isLastItem, radius = MergedCardRadius),
                                    color    = mergedCardColor,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text       = item.text.uppercase(),
                                        style      = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = infoColor(),
                                        modifier   = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 10.dp)
                                    )
                                }
                            }
                            is VehicleCardItem.Row -> {
                                DetailRow(
                                    label     = item.label,
                                    value     = item.value,
                                    onCopy    = item.onCopy,
                                    multiline = item.multiline,
                                    shape     = entryItemShape(isFirst = isFirstItem, isLast = isLastItem, radius = MergedCardRadius)
                                )
                            }
                        }
                        if (!isLastItem) {
                            val nextIsTitle = items[index + 1] is VehicleCardItem.Title
                            val fullWidth   = item is VehicleCardItem.Title || nextIsTitle
                            if (fullWidth) {
                                EntryGroupDivider(startInset = 0.dp, endInset = 0.dp, cardColor = mergedCardColor, gapColor = mergedGapColor)
                            } else {
                                EntryGroupDivider(startInset = MergedCardStartInset, endInset = MergedCardEndInset, cardColor = mergedCardColor, gapColor = mergedGapColor)
                            }
                        }
                    }
                }
            }

            EntryMetadata(createdAt = uiState.createdAt, updatedAt = uiState.updatedAt)

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
        }
    }
}

