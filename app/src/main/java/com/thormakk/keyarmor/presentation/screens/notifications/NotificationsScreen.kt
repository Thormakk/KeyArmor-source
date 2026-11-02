@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.notifications

import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.InAppNotification
import com.thormakk.keyarmor.domain.model.NotificationType
import com.thormakk.keyarmor.presentation.screens.audit.AuditViewModel
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.vault.CategoryChip
import com.thormakk.keyarmor.presentation.screens.vault.SortOrder
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import java.text.SimpleDateFormat
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.TextoQueSeAjusta

private val NOTIF_TYPE_LABEL_MAP = mapOf(
    NotificationType.EXPIRING                 to R.string.notif_type_expiring,
    NotificationType.EXPIRED                  to R.string.notif_type_expired,
    NotificationType.BREACH                   to R.string.notif_type_breach,
    NotificationType.HEIR_ACCEPTED            to R.string.notif_type_heir_accepted,
    NotificationType.HEIR_DECLINED            to R.string.notif_type_heir_declined,
    NotificationType.REMINDER                 to R.string.notif_type_reminder,
    NotificationType.HEIR_REMOVED             to R.string.notif_type_heir_removed,
    NotificationType.HEIR_RESIGNED            to R.string.notif_type_heir_resigned,
    NotificationType.VAULT_INHERITED          to R.string.notif_type_vault_inherited,
    NotificationType.INHERITANCE_SYNC         to R.string.notif_type_inheritance_sync,
    NotificationType.INHERITANCE_ERROR        to R.string.notif_type_inheritance_error,
    NotificationType.FAMILY_INVITE_SENT       to R.string.notif_type_family_invite_sent,
    NotificationType.FAMILY_ERROR             to R.string.notif_type_family_error,
    NotificationType.FAMILY_MEMBER_LEFT       to R.string.notif_type_family_member_left,
    NotificationType.FAMILY_ACCESS_REVOKED    to R.string.notif_type_family_access_revoked,
    NotificationType.FAMILY_PREMIUM_REVOKED   to R.string.notif_type_family_premium_revoked,
    NotificationType.FAMILY_MEMBER_DECLINED   to R.string.notif_type_family_member_declined,
    NotificationType.TRAVEL_SUGGESTION        to R.string.notif_type_travel_suggestion
)

@Composable
fun NotificationsScreen(
    onBack:          () -> Unit,
    onAuditClick:    () -> Unit = {},
    onReminderClick: (id: String) -> Unit = {},
    onEntryClick:    (id: String) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications  by viewModel.notifications.collectAsStateWithLifecycle()
    val selectedIds    by viewModel.selectedIds.collectAsStateWithLifecycle()
    val searchQuery    by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder      by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedTypes  by viewModel.selectedTypes.collectAsStateWithLifecycle()
    val availableTypes by viewModel.availableTypes.collectAsStateWithLifecycle()
    val isMultiSelect   = selectedIds.isNotEmpty()
    val allSelected     = selectedIds.size >= notifications.size && notifications.isNotEmpty()
    val autoLockManager = LocalAutoLockManager.current

    var showBulkDeleteDialog  by remember { mutableStateOf(false) }
    var notifToDelete         by remember { mutableStateOf<Long?>(null) }
    var showSortMenu          by remember { mutableStateOf(false) }
    var showSelectionMenu     by remember { mutableStateOf(false) }
    var showSearchBar         by remember { mutableStateOf(false) }
    var showFiltersSheet      by remember { mutableStateOf(false) }
    val focusManager           = LocalFocusManager.current
    val searchFocusRequester   = remember { FocusRequester() }

    LaunchedEffect(showSearchBar) {
        if (showSearchBar) searchFocusRequester.requestFocus()
    }

    if (showFiltersSheet) {
        NotificationsFiltersSheet(
            availableTypes = availableTypes,
            selectedTypes  = selectedTypes,
            onApply        = { types -> viewModel.applyTypeFilter(types); showFiltersSheet = false },
            onClearAll     = { viewModel.clearTypeFilter(); showFiltersSheet = false },
            onDismiss      = { showFiltersSheet = false }
        )
    }

    LaunchedEffect(Unit) { viewModel.markAllRead() }

    if (showBulkDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.notif_delete_selected_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.notif_delete_selected_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showBulkDeleteDialog = false; viewModel.deleteSelected() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    val idToDelete = notifToDelete
    if (idToDelete != null) {
        AlertDialog(
            onDismissRequest = { notifToDelete = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.notif_delete_selected_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.notif_delete_selected_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { notifToDelete = null; viewModel.delete(idToDelete) },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { notifToDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (isMultiSelect) {
                TopAppBar(
                    title = {
                        TextoQueSeAjusta(
                            stringResource(R.string.notif_selected_count, selectedIds.size),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_cancel),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        IconButton(onClick = { viewModel.archiveSelected() }) {
                            Icon(
                                Icons.Default.Archive,
                                contentDescription = stringResource(R.string.action_archive),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showBulkDeleteDialog = true }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = dangerColor()
                            )
                        }
                        Box {
                            IconButton(onClick = { showSelectionMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.vault_open_menu),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded         = showSelectionMenu,
                                onDismissRequest = { showSelectionMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(stringResource(if (allSelected) R.string.bulk_deselect_all else R.string.bulk_select_all))
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.SelectAll, contentDescription = null)
                                    },
                                    onClick = {
                                        showSelectionMenu = false
                                        if (allSelected) viewModel.clearSelection() else viewModel.selectAll()
                                    }
                                )
                            }
                        }
                    },
                    colors = topBarColors()
                )
            } else {
                TopAppBar(
                    title = {
                        TextoDeUnaLinea(
                            stringResource(R.string.notif_screen_title),
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
                        IconButton(onClick = {
                            if (showSearchBar) {
                                viewModel.setSearchQuery("")
                                showSearchBar = false
                            } else {
                                showSearchBar = true
                            }
                        }) {
                            Icon(
                                imageVector        = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = stringResource(if (showSearchBar) R.string.action_cancel else R.string.notif_search_hint),
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 7.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(visible = showSearchBar) {
                Column {
                    TextField(
                        value         = searchQuery,
                        onValueChange = { autoLockManager?.recordInteraction(); viewModel.setSearchQuery(it) },
                        placeholder   = {
                            Text(
                                stringResource(R.string.notif_search_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint   = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint   = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine    = true,
                        shape         = RoundedCornerShape(50.dp),
                        colors        = TextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor  = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester)
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (notifications.isNotEmpty() || searchQuery.isNotEmpty() || selectedTypes.isNotEmpty()) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val count = notifications.size
                    Text(
                        text  = pluralStringResource(R.plurals.notif_count, count, count),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.weight(1f))

                    if (availableTypes.isNotEmpty()) {
                        CategoryChip(
                            label    = if (selectedTypes.isNotEmpty())
                                stringResource(R.string.filter_short_with_count, selectedTypes.size)
                            else
                                stringResource(R.string.filter_short),
                            selected = selectedTypes.isNotEmpty(),
                            onClick  = { showFiltersSheet = true }
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Box {
                        val sortLabel = when (sortOrder) {
                            SortOrder.A_Z    -> stringResource(R.string.vault_sort_az)
                            SortOrder.Z_A    -> stringResource(R.string.vault_sort_za)
                            SortOrder.NEWEST -> stringResource(R.string.vault_sort_newest_short)
                            SortOrder.OLDEST -> stringResource(R.string.vault_sort_oldest_short)
                        }
                        TextButton(
                            onClick        = { showSortMenu = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                sortLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector        = if (showSortMenu) Icons.Default.ArrowDropUp
                                                     else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier           = Modifier.size(16.dp),
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded         = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            listOf(
                                SortOrder.A_Z    to stringResource(R.string.vault_sort_az),
                                SortOrder.Z_A    to stringResource(R.string.vault_sort_za),
                                SortOrder.NEWEST to stringResource(R.string.vault_sort_newest),
                                SortOrder.OLDEST to stringResource(R.string.vault_sort_oldest)
                            ).forEach { (order, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sortOrder == order) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = { viewModel.setSortOrder(order); showSortMenu = false },
                                    trailingIcon = if (sortOrder == order) ({
                                        Icon(Icons.Default.Check, contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }) else null
                                )
                            }
                        }
                    }
                }
            }

            if (notifications.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier            = Modifier.coachMarkAnchor("notifications", "empty"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier           = Modifier.size(56.dp)
                        )
                        Text(
                            text       = stringResource(R.string.notif_empty_title),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text      = stringResource(R.string.notif_empty_message),
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(horizontal = 40.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier            = Modifier
                        .fillMaxSize()
                        .coachMarkAnchor("notifications", "multiselect"),
                    contentPadding      = PaddingValues(top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.5.dp)
                ) {
                    itemsIndexed(notifications, key = { _, it -> it.id }) { index, notif ->
                        val isSelected = notif.id in selectedIds
                        val isFirst    = index == 0
                        val isLast     = index == notifications.lastIndex
                        val r          = 12.dp
                        val itemShape  = when {
                            notifications.size == 1 -> RoundedCornerShape(r)
                            isFirst -> RoundedCornerShape(topStart = r, topEnd = r, bottomStart = 0.dp, bottomEnd = 0.dp)
                            isLast  -> RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = r, bottomEnd = r)
                            else    -> RoundedCornerShape(0.dp)
                        }
                        NotificationCard(
                            notification      = notif,
                            isSelected        = isSelected,
                            isMultiSelectMode = isMultiSelect,
                            shape             = itemShape,
                            onArchive         = { viewModel.archive(notif.id) },
                            onDeleteRequest   = { notifToDelete = notif.id },
                            modifier          = Modifier.combinedClickable(
                                onClick = {
                                    if (isMultiSelect) {
                                        viewModel.toggleSelected(notif.id)
                                    } else {
                                        when (notif.type) {
                                            NotificationType.REMINDER ->
                                                notif.targetId?.let { onReminderClick(it) } ?: onAuditClick()
                                            NotificationType.BREACH -> {
                                                AuditViewModel.abrirBrechasAlEntrar = true
                                                onAuditClick()
                                            }
                                            NotificationType.EXPIRED,
                                            NotificationType.EXPIRING ->
                                                notif.targetId?.let { onEntryClick(it) } ?: onAuditClick()
                                            else -> Unit
                                        }
                                    }
                                },
                                onLongClick = { viewModel.toggleSelected(notif.id) }
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification:      InAppNotification,
    isSelected:        Boolean,
    isMultiSelectMode: Boolean,
    shape:             RoundedCornerShape,
    onArchive:         () -> Unit,
    onDeleteRequest:   () -> Unit,
    modifier:          Modifier = Modifier
) {
    val (icon, iconColor) = notifIconAndColor(notification.type)
    val dateFormat        = remember { SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault()) }
    var menuExpanded      by remember { mutableStateOf(false) }

    Surface(
        shape    = shape,
        color    = when {
            isSelected           -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            !notification.isRead -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
            else                 -> MaterialTheme.colorScheme.surface
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (isMultiSelectMode) {
                Checkbox(
                    checked         = isSelected,
                    onCheckedChange = null,
                    modifier        = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically),
                    colors          = CheckboxDefaults.colors(
                        checkedColor   = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(Modifier.width(10.dp))
            } else {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconColor,
                    modifier           = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = notification.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (notification.body.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = notification.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = dateFormat.format(java.util.Date(notification.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (!isMultiSelectMode) {
                Box(
                    modifier         = Modifier.align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .offset(x = (-2).dp, y = (-12).dp)
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector        = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                            expanded         = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text    = { Text(stringResource(R.string.action_archive), color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = {
                                    Icon(Icons.Default.Archive, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                },
                                onClick = { menuExpanded = false; onArchive() }
                            )
                            DropdownMenuItem(
                                text    = { Text(stringResource(R.string.action_delete), color = dangerColor()) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, contentDescription = null,
                                        tint = dangerColor(), modifier = Modifier.size(18.dp))
                                },
                                onClick = { menuExpanded = false; onDeleteRequest() }
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun notifIconAndColor(type: NotificationType): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> =
    when (type) {
        NotificationType.EXPIRED        -> Icons.Default.Lock              to warningColor()
        NotificationType.EXPIRING       -> Icons.Default.Schedule          to MaterialTheme.colorScheme.tertiary
        NotificationType.BREACH         -> Icons.Default.Warning           to warningColor()
        NotificationType.HEIR_ACCEPTED  -> Icons.Default.HowToReg          to MaterialTheme.colorScheme.primary
        NotificationType.HEIR_DECLINED  -> Icons.Default.PersonOff         to MaterialTheme.colorScheme.onSurfaceVariant
        NotificationType.REMINDER       -> Icons.Default.NotificationsNone to MaterialTheme.colorScheme.primary
        NotificationType.HEIR_REMOVED     -> Icons.Default.PersonRemove    to warningColor()
        NotificationType.HEIR_RESIGNED    -> Icons.AutoMirrored.Filled.ExitToApp      to MaterialTheme.colorScheme.onSurfaceVariant
        NotificationType.VAULT_INHERITED  -> Icons.Default.MoveToInbox    to MaterialTheme.colorScheme.primary
        NotificationType.INHERITANCE_SYNC  -> Icons.Default.CheckCircle   to MaterialTheme.colorScheme.primary
        NotificationType.INHERITANCE_ERROR -> Icons.Default.ErrorOutline  to warningColor()
        NotificationType.FAMILY_INVITE_SENT     -> Icons.Default.PersonAdd    to MaterialTheme.colorScheme.primary
        NotificationType.FAMILY_ERROR           -> Icons.Default.ErrorOutline to warningColor()
        NotificationType.FAMILY_MEMBER_LEFT     -> Icons.AutoMirrored.Filled.ExitToApp    to MaterialTheme.colorScheme.onSurfaceVariant
        NotificationType.FAMILY_ACCESS_REVOKED  -> Icons.Default.PersonRemove to warningColor()
        NotificationType.FAMILY_PREMIUM_REVOKED -> Icons.Default.WorkspacePremium to MaterialTheme.colorScheme.tertiary
        NotificationType.FAMILY_MEMBER_DECLINED -> Icons.Default.PersonOff    to MaterialTheme.colorScheme.onSurfaceVariant
        NotificationType.TRAVEL_SUGGESTION      -> Icons.Default.Flight       to MaterialTheme.colorScheme.primary
    }

@Composable
private fun NotificationsFiltersSheet(
    availableTypes: Set<NotificationType>,
    selectedTypes:  Set<NotificationType>,
    onApply:        (Set<NotificationType>) -> Unit,
    onClearAll:     () -> Unit,
    onDismiss:      () -> Unit
) {
    var pendingTypes by remember { mutableStateOf(selectedTypes) }
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                if (pendingTypes.isNotEmpty()) {
                    TextButton(onClick = {
                        pendingTypes = emptySet()
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

            FlowRow(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp)
            ) {
                NOTIF_TYPE_LABEL_MAP.entries
                    .filter { it.key in availableTypes }
                    .forEach { (type, labelRes) ->
                        CategoryChip(
                            label    = stringResource(labelRes),
                            selected = type in pendingTypes,
                            onClick  = {
                                pendingTypes = if (type in pendingTypes)
                                    pendingTypes - type
                                else
                                    pendingTypes + type
                            }
                        )
                    }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick  = { onApply(pendingTypes) },
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
