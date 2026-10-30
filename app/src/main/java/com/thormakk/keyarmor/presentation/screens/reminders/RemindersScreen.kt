@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
package com.thormakk.keyarmor.presentation.screens.reminders
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import com.thormakk.keyarmor.alarms.ReminderAlarmReceiver
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.VioletaDelFab
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.PushPin
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Reminder
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.presentation.components.ChipData
import com.thormakk.keyarmor.presentation.components.ChipsRowWithFunnel
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.TagDeleteButton
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.components.chipCategoryColor
import com.thormakk.keyarmor.presentation.components.chipTagColor
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.notesAccentColor
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.components.selection.protegidaVerde
import com.thormakk.keyarmor.presentation.screens.vault.CategoryChip
import com.thormakk.keyarmor.presentation.screens.vault.ProtectedLockBadge
import com.thormakk.keyarmor.presentation.screens.vault.ChipCategoryColor
import com.thormakk.keyarmor.presentation.screens.vault.ChipTagColor
import com.thormakk.keyarmor.presentation.screens.vault.SortOrder
import com.thormakk.keyarmor.presentation.screens.vault.TAG_MAX_LENGTH
import com.thormakk.keyarmor.presentation.screens.vault.TAG_MIN_LENGTH
import com.thormakk.keyarmor.presentation.screens.vault.normalizeTag
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.formatDateLocalized
import com.thormakk.keyarmor.presentation.util.highlightText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.TextoQueSeAjusta

private fun reminderDatetimeFmt() = SimpleDateFormat("dd/MM/yyyy - HH:mm 'hs.'", Locale.getDefault())

@Composable
private fun ExactAlarmsBanner(
    onEnableClick: () -> Unit,
    onDismiss:     () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector        = Icons.Default.Alarm,
            contentDescription = null,
            tint               = warningColor(),
            modifier           = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = stringResource(R.string.reminder_exact_alarms_banner_title),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text      = stringResource(R.string.reminder_exact_alarms_banner_body),
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick  = onEnableClick,
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text       = stringResource(R.string.reminder_exact_alarms_banner_action),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector        = Icons.Default.Close,
                contentDescription = stringResource(R.string.action_close),
                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

@SuppressLint("InlinedApi")
@Composable
fun RemindersScreen(
    onBack:       () -> Unit,
    onAddClick:   (initialDateMs: Long?) -> Unit,
    onEditClick:  (String) -> Unit,
    onPlansClick: () -> Unit = {},
    viewModel:    RemindersViewModel = hiltViewModel()
) {
    val filteredReminders    by viewModel.filteredReminders.collectAsStateWithLifecycle()
    val isFreeUser           by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val searchQuery          by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTags         by viewModel.selectedTags.collectAsStateWithLifecycle()
    val availableTags        by viewModel.availableTags.collectAsStateWithLifecycle()
    val topFilterTags        by viewModel.topFilterTags.collectAsStateWithLifecycle()
    val tagCountMap          by viewModel.tagCountMap.collectAsStateWithLifecycle()
    val sortOrder            by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedIds          by viewModel.selectedIds.collectAsStateWithLifecycle()
    val onlyFavorites        by viewModel.onlyFavorites.collectAsStateWithLifecycle()
    val onlyAttachments      by viewModel.onlyAttachments.collectAsStateWithLifecycle()
    val onlyProtected        by viewModel.onlyProtected.collectAsStateWithLifecycle()
    val calendarStart        by viewModel.calendarStart.collectAsStateWithLifecycle()
    val calendarEnd          by viewModel.calendarEnd.collectAsStateWithLifecycle()
    val categoryFilter       by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val reminderCountByDay        by viewModel.reminderCountByDay.collectAsStateWithLifecycle()
    val entryIdsWithAttachments   by viewModel.entryIdsWithAttachments.collectAsStateWithLifecycle()
    val isSelectionMode            = selectedIds.isNotEmpty()
    val autoLockManager = LocalAutoLockManager.current
    val exactAlarmsGranted  by viewModel.exactAlarmsGranted.collectAsStateWithLifecycle()
    val context         = LocalContext.current
    val lifecycleOwner  = LocalLifecycleOwner.current
    var exactAlarmsBannerDismissed by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshExactAlarmsState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showSortMenu         by remember { mutableStateOf(false) }
    var showFiltersSheet     by remember { mutableStateOf(false) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var protectedNotice      by remember { mutableStateOf<Int?>(null) }
    var unlockTargetId       by remember { mutableStateOf<String?>(null) }
    var showProtectPremium   by remember { mutableStateOf(false) }
    var showBulkUnlock       by remember { mutableStateOf(false) }
    val snackbarHostState    = remember { SnackbarHostState() }
    var calendarVisible      by remember { mutableStateOf(false) }
    var showSearch           by remember { mutableStateOf(false) }
    val searchFocusRequester  = remember { FocusRequester() }
    val listState             = rememberLazyListState()
    var sortHeaderVisible    by remember { mutableStateOf(true) }
    LaunchedEffect(showSearch) { if (showSearch) searchFocusRequester.requestFocus() }
    BackHandler(enabled = showSearch && !calendarVisible) { showSearch = false; viewModel.setSearchQuery("") }

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

    var pendingStart         by remember { mutableStateOf<Long?>(null) }
    var pendingEnd           by remember { mutableStateOf<Long?>(null) }

    val pinnedReminders   = filteredReminders.filter { it.isPinned }
    val unpinnedReminders = filteredReminders.filter { !it.isPinned }

    val activeFilterCount = selectedTags.size +
        (if (onlyFavorites) 1 else 0) +
        (if (onlyAttachments) 1 else 0) +
        (if (onlyProtected) 1 else 0) +
        (if (categoryFilter != null) 1 else 0)

    val selectedReminders = filteredReminders.filter { it.id in selectedIds }
    val protectedCount    = selectedReminders.count { it.isProtected }
    val allProtected      = selectedReminders.isNotEmpty() && protectedCount == selectedReminders.size
    val soleEntryTitle    = selectedReminders.firstOrNull { !it.isProtected }?.name

    var protectedSkippedCount by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { viewModel.protectedSkipped.collect { protectedSkippedCount = it } }
    val protectedSkippedMsg = protectedSkippedCount?.let { pluralStringResource(R.plurals.protect_bulk_skipped, it, it) }
    LaunchedEffect(protectedSkippedCount) {
        if (protectedSkippedMsg != null) {
            protectedSkippedCount = null
            snackbarHostState.showSnackbar(protectedSkippedMsg)
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
    unlockTargetId?.let { id ->
        AlertDialog(
            onDismissRequest = { unlockTargetId = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_unlock_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(if (!isFreeUser) R.string.protect_unlock_body else R.string.protect_unlock_body_free),
                color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { unlockTargetId = null; viewModel.setProtected(id, false) }) {
                    Text(stringResource(R.string.lock_action_unlock))
                }
            },
            dismissButton = {
                TextButton(onClick = { unlockTargetId = null }) {
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
                TextButton(onClick = { showBulkUnlock = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showBulkDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.reminder_delete_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                val aMover = selectedIds.size - protectedCount
                Text(
                    (if (aMover == 1 && protectedCount > 0 && !soleEntryTitle.isNullOrBlank())
                        stringResource(R.string.protect_bulk_single_named, soleEntryTitle)
                    else
                        stringResource(R.string.bulk_delete_confirm_multiple, aMover)) +
                    (if (protectedCount > 0) "\n\n" + pluralStringResource(R.plurals.protect_bulk_confirm_extra, protectedCount, protectedCount) else ""),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = { showBulkDeleteDialog = false; viewModel.bulkDelete() },
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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        TextoDeUnaLinea(
                            stringResource(R.string.reminder_selected_count, selectedIds.size),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel),
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        IconButton(onClick = { viewModel.bulkArchive() }) {
                            Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.action_archive),
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = {
                            if (allProtected) protectedNotice = R.string.protect_bulk_all
                            else showBulkDeleteDialog = true
                        }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_move_to_trash),
                                tint = dangerColor())
                        }
                        IconButton(onClick = {
                            when {
                                allProtected -> showBulkUnlock = true
                                isFreeUser   -> showProtectPremium = true
                                else         -> viewModel.bulkSetProtected(true)
                            }
                        }) {
                            Icon(
                                imageVector        = if (allProtected) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = stringResource(if (allProtected) R.string.protect_cd_unlock else R.string.protect_cd_lock),
                                tint               = if (allProtected) protegidaVerde() else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = topBarColors()
                )
            } else {
                TopAppBar(
                    title = {
                        TextoQueSeAjusta(
                            stringResource(R.string.reminder_screen_title),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        IconButton(onClick = {
                            ReminderAlarmReceiver.ensureChannel(context)
                            val chIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                putExtra(Settings.EXTRA_CHANNEL_ID, ReminderAlarmReceiver.CHANNEL_ID)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(chIntent)
                            } catch (_: Exception) {
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    )
                                } catch (_: Exception) {   }
                            }
                        }) {
                            Icon(
                                imageVector        = Icons.Default.NotificationsActive,
                                contentDescription = stringResource(R.string.reminder_notif_settings_desc),
                                tint               = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (!calendarVisible) {
                            IconButton(onClick = {
                                showSearch = !showSearch
                                if (!showSearch) viewModel.setSearchQuery("")
                            }) {
                                Icon(
                                    imageVector        = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                            if (!calendarVisible) {
                                pendingStart = calendarStart
                                pendingEnd   = calendarEnd
                            }
                            calendarVisible = !calendarVisible
                            },
                            modifier = Modifier.coachMarkAnchor("reminders", "calendar")
                        ) {
                            Icon(
                                imageVector        = Icons.Default.CalendarMonth,
                                contentDescription = stringResource(R.string.reminder_calendar_desc),
                                tint               = if (calendarVisible || calendarStart != null)
                                                         MaterialTheme.colorScheme.primary
                                                     else
                                                         MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        floatingActionButton = {
            val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            if (!isSelectionMode) {
                FloatingActionButton(
                    modifier = Modifier
                        .offset(y = -navBarHeight)
                        .coachMarkAnchor("reminders", "fab"),
                    onClick        = { if (!isFreeUser) onAddClick(null) },
                    containerColor = if (isFreeUser) MaterialTheme.colorScheme.surfaceVariant
                                     else VioletaDelFab,
                    contentColor   = if (isFreeUser) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                     else ContenidoDelFab
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.reminder_add_fab))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(
                visible = !isSelectionMode && !exactAlarmsGranted && !exactAlarmsBannerDismissed,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                ExactAlarmsBanner(
                    onEnableClick = {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                            context.startActivity(intent)
                        } catch (_: android.content.ActivityNotFoundException) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                            } catch (_: android.content.ActivityNotFoundException) {   }
                        }
                    },
                    onDismiss = { exactAlarmsBannerDismissed = true }
                )
            }

            AnimatedVisibility(
                visible = !calendarVisible && showSearch,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                TextField(
                    value         = searchQuery,
                    onValueChange = { autoLockManager?.recordInteraction(); viewModel.setSearchQuery(it) },
                    placeholder   = { Text(stringResource(R.string.vault_search_placeholder)) },
                    leadingIcon   = {
                        Icon(Icons.Default.Search, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon  = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
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

            AnimatedVisibility(
                visible = calendarVisible,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    ReminderCalendar(
                        countByDay   = reminderCountByDay,
                        pendingStart = pendingStart,
                        pendingEnd   = pendingEnd,
                        onDayClick   = { dayMs ->
                            when {
                                pendingStart == null -> {
                                    pendingStart = dayMs; pendingEnd = null
                                }
                                pendingEnd == null && dayMs == pendingStart -> {
                                    pendingStart = null
                                }
                                pendingEnd == null -> {
                                    if (dayMs < pendingStart!!) {
                                        pendingEnd = pendingStart; pendingStart = dayMs
                                    } else {
                                        pendingEnd = dayMs
                                    }
                                }
                                else -> { pendingStart = dayMs; pendingEnd = null }
                            }
                        }
                    )
                    Button(
                        onClick = {
                            viewModel.setCalendarRange(pendingStart, pendingEnd)
                            calendarVisible = false
                        },
                        enabled  = pendingStart != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .height(48.dp),
                        shape  = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor        = Color.Transparent,
                            contentColor          = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (pendingStart != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text       = stringResource(R.string.action_apply),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            var renderedChipKeys by remember(topFilterTags) {
                mutableStateOf(
                    buildSet<String> {
                        topFilterTags.forEach { add("chip_tag:$it") }
                    }
                )
            }
            val reminderChips = buildList {
                topFilterTags.forEach { tagName ->
                    add(ChipData(
                        key           = "chip_tag:$tagName",
                        label         = tagName,
                        selected      = tagName in selectedTags,
                        selectedColor = ChipTagColor,
                        onClick       = { viewModel.toggleTag(tagName) },
                        count         = tagCountMap[tagName] ?: 0
                    ))
                }
            }
            if (!calendarVisible) {
                ChipsRowWithFunnel(
                    chips                    = reminderChips,
                    activeFilterCount        = activeFilterCount,
                    onFunnelClick            = { showFiltersSheet = true },
                    funnelContentDescription = stringResource(R.string.filter_short),
                    onVisibleKeysChanged     = { renderedChipKeys = it },
                    modifier                 = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) { data ->
                    CategoryChip(
                        label         = data.label,
                        selected      = data.selected,
                        selectedColor = data.selectedColor,
                        onClick       = data.onClick,
                        count         = data.count
                    )
                }
            }

            val snapStart = calendarStart
            val snapEnd   = calendarEnd
            val showDateChip   = snapStart != null && !calendarVisible
            val showFavChip    = onlyFavorites
            val showAttachChip = onlyAttachments
            val showProtChip   = onlyProtected
            val showCatChip    = categoryFilter != null
            val hiddenTagChips = selectedTags.filter { "chip_tag:$it" !in renderedChipKeys }
            if (showDateChip || showFavChip || showAttachChip || showProtChip || showCatChip || hiddenTagChips.isNotEmpty()) {
                val activeChipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    selectedLabelColor        = MaterialTheme.colorScheme.primary,
                    selectedTrailingIconColor = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    if (showDateChip) {
                        val currentLocale = LocalConfiguration.current.locales[0]
                        val singleDayFmt = remember(currentLocale) { SimpleDateFormat("d MMM yyyy", currentLocale).apply { timeZone = TimeZone.getTimeZone("UTC") } }
                        val chipLabel = if (snapEnd == null) {
                            singleDayFmt.format(Date(snapStart))
                        } else {
                            stringResource(R.string.reminder_date_range_chip, formatDateLocalized(snapStart, currentLocale), formatDateLocalized(snapEnd, currentLocale))
                        }
                        FilterChip(
                            selected     = true,
                            onClick      = {},
                            label        = { Text(chipLabel, style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.setCalendarRange(null, null) })
                            },
                            colors = activeChipColors
                        )
                    }
                    if (showFavChip) {
                        FilterChip(
                            selected     = true,
                            onClick      = {},
                            label        = { Text(stringResource(R.string.filter_favorites), style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.setFavorites(false) })
                            },
                            colors = activeChipColors
                        )
                    }
                    if (showAttachChip) {
                        FilterChip(
                            selected     = true,
                            onClick      = {},
                            label        = { Text(stringResource(R.string.filter_attachments), style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.setAttachments(false) })
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
                                    modifier = Modifier.size(16.dp).clickable { viewModel.setOnlyProtected(false) })
                            },
                            colors = activeChipColors
                        )
                    }
                    if (showCatChip) {
                        FilterChip(
                            selected     = true,
                            onClick      = {},
                            label        = { Text(stringResource(categoryFilter!!.labelRes), style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.setCategoryFilter(null) })
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
                                    modifier = Modifier.size(16.dp).clickable { viewModel.toggleTag(tagName) })
                            },
                            colors = activeChipColors
                        )
                    }
                }
            }

            if (!calendarVisible) {
                Spacer(Modifier.height(4.dp))

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
                    val count = filteredReminders.size
                    Text(
                        text  = if (count == 1) stringResource(R.string.reminder_count_one) else stringResource(R.string.reminder_count, count),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            listOf(SortOrder.NEWEST, SortOrder.OLDEST, SortOrder.A_Z, SortOrder.Z_A).forEach { order ->
                                val label = when (order) {
                                    SortOrder.A_Z    -> stringResource(R.string.vault_sort_az)
                                    SortOrder.Z_A    -> stringResource(R.string.vault_sort_za)
                                    SortOrder.NEWEST -> stringResource(R.string.vault_sort_newest)
                                    SortOrder.OLDEST -> stringResource(R.string.vault_sort_oldest)
                                }
                                DropdownMenuItem(
                                    text    = { Text(label) },
                                    onClick = { viewModel.setSortOrder(order); showSortMenu = false },
                                    trailingIcon = if (sortOrder == order) ({
                                        Icon(Icons.Default.Check, contentDescription = null,
                                            modifier = Modifier.size(16.dp))
                                    }) else null
                                )
                            }
                        }
                    }
                }
                }
            }

            if (!calendarVisible) LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                state               = listState,
                contentPadding      = PaddingValues(start = 7.dp, end = 7.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(0.5.dp)
            ) {

                if (isFreeUser) {
                    item { RemindersPremiumBanner(onPlansClick = onPlansClick) }
                }

                if (filteredReminders.isEmpty() && !isFreeUser) {
                    item {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.NotificationsNone, contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(56.dp))
                                Text(stringResource(R.string.reminder_empty_title),
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onBackground)
                                Text(stringResource(R.string.reminder_empty_message),
                                    style     = MaterialTheme.typography.bodyMedium,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier  = Modifier.padding(horizontal = 40.dp))
                            }
                        }
                    }
                }

                if (pinnedReminders.isNotEmpty()) {
                    item(key = "pinned_header") {
                        Text(
                            text     = stringResource(R.string.section_pinned),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.animateItem().padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    itemsIndexed(pinnedReminders, key = { _, it -> it.id }) { index, reminder ->
                        ReminderCard(
                            reminder        = reminder,
                            isFirst         = index == 0,
                            isLast          = index == pinnedReminders.lastIndex,
                            isSelected      = reminder.id in selectedIds,
                            isSelectionMode = isSelectionMode,
                            hasAttachments  = reminder.id in entryIdsWithAttachments,
                            onClick         = {
                                if (!isFreeUser) {
                                    if (isSelectionMode) viewModel.toggleSelected(reminder.id)
                                    else if (reminder.isProtected) protectedNotice = R.string.protect_locked_edit
                                    else onEditClick(reminder.id)
                                }
                            },
                            onLongClick     = { if (!isFreeUser) viewModel.toggleSelected(reminder.id) },
                            onEdit          = {
                                if (reminder.isProtected) protectedNotice = R.string.protect_locked_edit
                                else onEditClick(reminder.id)
                            },
                            onProtectedDelete = { protectedNotice = R.string.protect_locked_delete },
                            isReadOnly = isFreeUser,
                            onToggleProtect = {
                                when {
                                    reminder.isProtected -> unlockTargetId = reminder.id
                                    !isFreeUser          -> viewModel.setProtected(reminder.id, true)
                                    else                 -> showProtectPremium = true
                                }
                            },
                            onPin           = { viewModel.togglePin(reminder) },
                            onFavorite      = { viewModel.toggleFavorite(reminder) },
                            onArchive       = { viewModel.toggleArchive(reminder) },
                            onDelete        = { viewModel.delete(reminder.id) },
                            modifier        = Modifier.animateItem()
                        )
                    }
                    item(key = "pinned_gap") { Spacer(Modifier.height(4.dp)) }
                }

                itemsIndexed(unpinnedReminders, key = { _, it -> it.id }) { index, reminder ->
                    ReminderCard(
                        reminder        = reminder,
                        isFirst         = index == 0,
                        isLast          = index == unpinnedReminders.lastIndex,
                        isSelected      = reminder.id in selectedIds,
                        isSelectionMode = isSelectionMode,
                        hasAttachments  = reminder.id in entryIdsWithAttachments,
                        onClick         = {
                            if (!isFreeUser) {
                                if (isSelectionMode) viewModel.toggleSelected(reminder.id)
                                else if (reminder.isProtected) protectedNotice = R.string.protect_locked_edit
                                else onEditClick(reminder.id)
                            }
                        },
                        onLongClick     = { if (!isFreeUser) viewModel.toggleSelected(reminder.id) },
                        onEdit          = {
                            if (reminder.isProtected) protectedNotice = R.string.protect_locked_edit
                            else onEditClick(reminder.id)
                        },
                        onProtectedDelete = { protectedNotice = R.string.protect_locked_delete },
                        isReadOnly = isFreeUser,
                        onToggleProtect = {
                            when {
                                reminder.isProtected -> unlockTargetId = reminder.id
                                !isFreeUser          -> viewModel.setProtected(reminder.id, true)
                                else                 -> showProtectPremium = true
                            }
                        },
                        onPin           = { viewModel.togglePin(reminder) },
                        onFavorite      = { viewModel.toggleFavorite(reminder) },
                        onArchive       = { viewModel.toggleArchive(reminder) },
                        onDelete        = { viewModel.delete(reminder.id) },
                        modifier        = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (showBulkDeleteDialog) {
        val count = selectedIds.size
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.reminder_delete_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    (if (count == 1) stringResource(R.string.reminder_delete_message)
                    else             stringResource(R.string.reminder_delete_message)) +
                    (if (protectedCount > 0) "\n\n" + pluralStringResource(R.plurals.protect_bulk_confirm_extra, protectedCount, protectedCount) else ""),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = { showBulkDeleteDialog = false; viewModel.bulkDelete() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showFiltersSheet) {
        RemindersFiltersSheet(
            availableTags   = availableTags,
            selectedTags    = selectedTags,
            onlyFavorites   = onlyFavorites,
            onlyAttachments = onlyAttachments,
            onlyProtected   = onlyProtected,
            onProtectedChange = { viewModel.setOnlyProtected(it) },
            categoryFilter  = categoryFilter,
            canCreateTags   = !isFreeUser,
            onApply         = { tags, favs, atts, cat ->
                viewModel.setTags(tags)
                viewModel.setFavorites(favs)
                viewModel.setAttachments(atts)
                viewModel.setCategoryFilter(cat)
                showFiltersSheet = false
            },
            onClearAll     = {
                viewModel.clearAllFilters()
                showFiltersSheet = false
            },
            onAddTag       = { viewModel.addTag(it) },
            onRenameTag    = { old, new -> viewModel.renameTag(old, new) },
            onDeleteTag    = { tag -> viewModel.deleteTag(tag) },
            onUpgradeClick = onPlansClick,
            onDismiss      = { showFiltersSheet = false }
        )
    }
}

@Composable
private fun RemindersFiltersSheet(
    availableTags:   List<Tag>,
    selectedTags:    Set<String>,
    onlyFavorites:   Boolean,
    onlyAttachments: Boolean,
    onlyProtected:   Boolean,
    onProtectedChange: (Boolean) -> Unit,
    categoryFilter:  EntryCategory?,
    onApply:         (tags: Set<String>, favorites: Boolean, attachments: Boolean, category: EntryCategory?) -> Unit,
    onClearAll:     () -> Unit,
    onAddTag:       (String) -> Unit,
    onRenameTag:    (old: String, new: String) -> Unit,
    onDeleteTag:    (tag: String) -> Unit,
    onDismiss:      () -> Unit,
    canCreateTags:  Boolean = true,
    onUpgradeClick: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val autoLockManager = LocalAutoLockManager.current

    var checkedTags      by remember(selectedTags)    { mutableStateOf(selectedTags) }
    var localFavorites   by remember(onlyFavorites)   { mutableStateOf(onlyFavorites) }
    var localAttachments by remember(onlyAttachments) { mutableStateOf(onlyAttachments) }
    var localProtected   by remember(onlyProtected)   { mutableStateOf(onlyProtected) }
    var localCategory    by remember(categoryFilter)  { mutableStateOf(categoryFilter) }

    var searchQuery    by remember { mutableStateOf("") }
    var newTagText     by remember { mutableStateOf("") }
    var newTagError    by remember { mutableStateOf<String?>(null) }
    var renameTarget   by remember { mutableStateOf<Tag?>(null) }
    var renameText     by remember { mutableStateOf("") }
    var renameError    by remember { mutableStateOf<String?>(null) }
    var showLockedDialog by remember { mutableStateOf(false) }

    val errDuplicate = stringResource(R.string.tag_error_duplicate)
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
            availableTags.any { it.name.equals(normalized, ignoreCase = true) } ->
                newTagError = errDuplicate
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
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
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
                if (checkedTags.isNotEmpty() || localFavorites || localAttachments || localProtected || localCategory != null) {
                    TextButton(onClick = {
                        checkedTags      = emptySet()
                        localFavorites   = false
                        localAttachments = false
                        localProtected   = false
                        localCategory    = null
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    label         = stringResource(R.string.filter_favorites),
                    selected      = localFavorites,
                    selectedColor = ChipCategoryColor,
                    leadingIcon   = Icons.Default.Star,
                    onClick       = { localFavorites = !localFavorites }
                )
                CategoryChip(
                    label         = stringResource(R.string.filter_attachments),
                    selected      = localAttachments,
                    selectedColor = ChipCategoryColor,
                    leadingIcon   = Icons.Default.AttachFile,
                    onClick       = { localAttachments = !localAttachments }
                )
                CategoryChip(
                    label         = stringResource(R.string.filter_protected),
                    selected      = localProtected,
                    selectedColor = ChipCategoryColor,
                    leadingIcon   = Icons.Default.Lock,
                    onClick       = { localProtected = !localProtected }
                )
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
                modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp)
            )
            val catChipColor = chipCategoryColor()
            FlowRow(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp)
            ) {
                EntryCategory.entries
                    .filter { it != EntryCategory.SIN_CATEGORIZAR }
                    .forEach { cat ->
                        val selected = localCategory == cat
                        Surface(
                            shape  = RoundedCornerShape(50),
                            color  = if (selected) catChipColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (selected) catChipColor else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable { localCategory = if (selected) null else cat }
                        ) {
                            Text(
                                text     = cat.label,
                                style    = MaterialTheme.typography.labelMedium,
                                color    = if (selected) catChipColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

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
                            onClick  = { renameTarget = tag; renameText = tag.name },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Edit,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
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
                                value          = renameText,
                                onValueChange  = { autoLockManager?.recordInteraction(); renameText = it; renameError = null },
                                singleLine     = true,
                                label          = { Text(stringResource(R.string.tag_name_hint)) },
                                shape          = RoundedCornerShape(8.dp),
                                modifier       = Modifier.fillMaxWidth(),
                                isError        = renameError != null,
                                supportingText = { renameError?.let { Text(it) } }
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
                                        availableTags.any { it.id != target.id && it.name.equals(normalized, ignoreCase = true) } ->
                                            renameError = errDuplicate
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
                    onClick  = { onProtectedChange(localProtected); onApply(checkedTags, localFavorites, localAttachments, localCategory) },
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

                Spacer(modifier = Modifier.height(8.dp))
            }

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
                            cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary)
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
private fun ReminderCard(
    reminder:        Reminder,
    isSelected:      Boolean,
    isSelectionMode: Boolean,
    hasAttachments:  Boolean,
    onClick:         () -> Unit,
    onLongClick:     () -> Unit,
    onEdit:          () -> Unit,
    onPin:           () -> Unit,
    onFavorite:      () -> Unit,
    onArchive:       () -> Unit,
    onDelete:        () -> Unit,
    onProtectedDelete: () -> Unit,
    onToggleProtect: () -> Unit,
    modifier:        Modifier = Modifier,
    isReadOnly:      Boolean = false,
    isFirst:         Boolean = false,
    isLast:          Boolean = false
) {
    var showMenu          by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.reminder_delete_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.reminder_delete_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(entryItemShape(isFirst, isLast))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                else MaterialTheme.colorScheme.surface
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            if (isSelectionMode) {
                Checkbox(
                    checked         = isSelected,
                    onCheckedChange = { onClick() },
                    colors          = CheckboxDefaults.colors(
                        checkedColor   = MaterialTheme.colorScheme.primary,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Box(
                    modifier         = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.EditCalendar,
                        contentDescription = null,
                        tint               = notesAccentColor(),
                        modifier           = Modifier.size(24.dp)
                    )
                    if (reminder.isProtected) {
                        ProtectedLockBadge(Modifier.align(Alignment.TopEnd))
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = reminder.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text  = reminderDatetimeFmt().format(Date(reminder.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val hasCat  = reminder.category != EntryCategory.SIN_CATEGORIZAR
                val hasTags = reminder.tags.isNotEmpty()
                if (hasCat || hasTags) {
                    val catColor = chipCategoryColor()
                    val tagColor = chipTagColor()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        modifier              = Modifier.padding(top = 3.dp)
                    ) {
                        if (hasCat) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = catColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text     = reminder.category.label,
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = catColor,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        reminder.tags.take(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = tagColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text     = tag,
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = tagColor,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasAttachments) {
                Icon(
                    imageVector        = Icons.Default.AttachFile,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(2.dp))
            }
            if (reminder.isPinned) {
                Icon(
                    imageVector        = Icons.Default.PushPin,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(2.dp))
            }

            if (!isSelectionMode) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded         = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text        = { Text(stringResource(R.string.action_edit)) },
                            onClick     = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text        = { Text(if (reminder.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin)) },
                            onClick     = { showMenu = false; onPin() },
                            leadingIcon = { Icon(if (reminder.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text        = { Text(if (reminder.isFavorite) stringResource(R.string.action_remove_favorite) else stringResource(R.string.action_favorite)) },
                            onClick     = { showMenu = false; onFavorite() },
                            leadingIcon = { Icon(if (reminder.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text        = { Text(if (reminder.isArchived) stringResource(R.string.action_unarchive) else stringResource(R.string.action_archive)) },
                            onClick     = { showMenu = false; onArchive() },
                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                        )
                        if (!isReadOnly || reminder.isProtected) DropdownMenuItem(
                            text        = { Text(stringResource(if (reminder.isProtected) R.string.protect_cd_unlock else R.string.protect_cd_lock)) },
                            onClick     = { showMenu = false; onToggleProtect() },
                            leadingIcon = {
                                Icon(
                                    if (reminder.isProtected) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (reminder.isProtected) protegidaVerde() else LocalContentColor.current
                                )
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text        = { Text(stringResource(R.string.action_move_to_trash),
                                color = dangerColor()) },
                            onClick     = {
                                showMenu = false
                                if (reminder.isProtected) onProtectedDelete() else showDeleteConfirm = true
                            },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null,
                                tint = dangerColor()) }
                        )
                    }
                }
            }
            }
    }
}

@Composable
private fun ReminderCalendar(
    countByDay:   Map<Long, Int>,
    pendingStart: Long?,
    pendingEnd:   Long?,
    onDayClick:   (dayMs: Long) -> Unit
) {
    val currentLocale = LocalConfiguration.current.locales[0]
    val monthNames = remember(currentLocale) {
        java.text.DateFormatSymbols(currentLocale).months.filter { it.isNotEmpty() }
            .map { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(currentLocale) else c.toString() } }
    }
    val dayHeaders = remember(currentLocale) {
        val symbols = java.text.DateFormatSymbols(currentLocale).shortWeekdays
        listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
               Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
            .map { symbols[it].take(2).replaceFirstChar { c -> c.titlecase(currentLocale) } }
    }

    val todayCal = remember { Calendar.getInstance() }
    var displayedMonthKey by remember {
        mutableIntStateOf(todayCal.get(Calendar.YEAR) * 12 + todayCal.get(Calendar.MONTH))
    }

    val dispYear  = displayedMonthKey / 12
    val dispMonth = displayedMonthKey % 12 + 1

    fun prevMonth() { displayedMonthKey-- }
    fun nextMonth() { displayedMonthKey++ }

    val todayYear  = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH) + 1
    val todayDay   = todayCal.get(Calendar.DAY_OF_MONTH)

    val rangeStart = if (pendingStart != null && pendingEnd != null) minOf(pendingStart, pendingEnd) else pendingStart
    val rangeEnd   = if (pendingStart != null && pendingEnd != null) maxOf(pendingStart, pendingEnd) else null

    var showYearMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text      = stringResource(R.string.reminder_today_header, todayDay.toString(), monthNames[todayMonth - 1]),
            style     = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Normal,
            color     = MaterialTheme.colorScheme.onSurface,
            modifier  = Modifier.padding(start = 8.dp, bottom = 2.dp)
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box {
                Row(
                    modifier          = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showYearMenu = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "${monthNames[dispMonth - 1]} $dispYear",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector        = if (showYearMenu) Icons.Default.ArrowDropUp
                                             else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded         = showYearMenu,
                    onDismissRequest = { showYearMenu = false },
                    containerColor   = MaterialTheme.colorScheme.surface
                ) {
                    val yearRange = (todayYear - 5)..(todayYear + 10)
                    yearRange.forEach { year ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    year.toString(),
                                    fontWeight = if (year == dispYear) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (year == dispYear) MaterialTheme.colorScheme.primary
                                                 else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick      = { displayedMonthKey = year * 12 + (displayedMonthKey % 12); showYearMenu = false },
                            trailingIcon = if (year == dispYear) ({
                                Icon(Icons.Default.Check, contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp))
                            }) else null
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = ::prevMonth) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.reminder_prev_month),
                        tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = ::nextMonth) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.reminder_next_month),
                        tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { hdr ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text       = hdr,
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        AnimatedContent(
            targetState  = displayedMonthKey,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } togetherWith
                    slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith
                    slideOutHorizontally { it }
                }
            },
            label = "calendar_month_grid"
        ) { monthKey ->
            val year  = monthKey / 12
            val month = monthKey % 12 + 1

            val firstDayCalInner = Calendar.getInstance().apply { set(year, month - 1, 1) }
            val daysInMonthInner = firstDayCalInner.getActualMaximum(Calendar.DAY_OF_MONTH)
            val javaDowInner     = firstDayCalInner.get(Calendar.DAY_OF_WEEK)
            val startOffsetInner = if (javaDowInner == Calendar.SUNDAY) 6 else javaDowInner - 2

            val totalCells = startOffsetInner + daysInMonthInner
            val rows       = (totalCells + 6) / 7

            Column {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIdx = row * 7 + col
                            val dayNum  = cellIdx - startOffsetInner + 1

                            if (dayNum < 1 || dayNum > daysInMonthInner) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else {
                                val dayMs = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                    set(year, month - 1, dayNum, 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                val count        = countByDay[dayMs] ?: 0
                                val isRangeStart = dayMs == rangeStart
                                val isRangeEnd   = rangeEnd != null && dayMs == rangeEnd
                                val isInRange    = rangeStart != null && rangeEnd != null &&
                                                   dayMs > rangeStart && dayMs < rangeEnd
                                val isToday      = todayYear == year &&
                                                   todayMonth == month &&
                                                   todayDay   == dayNum

                                CalendarDayCell(
                                    day          = dayNum,
                                    count        = count,
                                    isRangeStart = isRangeStart,
                                    isRangeEnd   = isRangeEnd,
                                    isInRange    = isInRange,
                                    hasRange     = rangeEnd != null,
                                    isToday      = isToday,
                                    onClick      = { onDayClick(dayMs) },
                                    modifier     = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun CalendarDayCell(
    day:          Int,
    count:        Int,
    isRangeStart: Boolean,
    isRangeEnd:   Boolean,
    isInRange:    Boolean,
    hasRange:     Boolean,
    isToday:      Boolean,
    onClick:      () -> Unit,
    modifier:     Modifier = Modifier
) {
    val isEndpoint  = isRangeStart || isRangeEnd
    val primary     = MaterialTheme.colorScheme.primary
    val bandColor   = primary

    Box(
        modifier         = modifier
            .aspectRatio(1f)
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isInRange ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(vertical = 3.dp)
                        .background(bandColor)
                )
            isRangeStart && hasRange ->
                Box(
                    Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .padding(vertical = 3.dp)
                        .align(Alignment.CenterEnd)
                        .background(bandColor)
                )
            isRangeEnd ->
                Box(
                    Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .padding(vertical = 3.dp)
                        .align(Alignment.CenterStart)
                        .background(bandColor)
                )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isEndpoint || isInRange -> primary
                        isToday                 -> primary.copy(alpha = 0.35f)
                        else                    -> Color.Transparent
                    }
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = day.toString(),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isEndpoint) FontWeight.Bold else FontWeight.SemiBold,
                color      = if (isEndpoint || isInRange) MaterialTheme.colorScheme.onPrimary
                             else MaterialTheme.colorScheme.onSurface
            )
        }

        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(17.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                )
            }
        }
    }
}

@Composable
private fun RemindersPremiumBanner(onPlansClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onPlansClick)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null,
                tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.premium_gate_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(stringResource(R.string.premium_gate_body),
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
