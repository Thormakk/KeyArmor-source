@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.timemachine
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.draw.clip
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.components.InfoChip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.data.drive.DriveSnapshotInfo
import com.thormakk.keyarmor.data.prefs.SnapshotPrefs
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.domain.model.VaultSnapshot
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.settings.contrastRatio
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

private val TIMELINE_GUTTER  = 34.dp
private val DOT_SIZE         = 12.dp
private val LINE_WIDTH       = 2.dp

@Composable
fun TimeMachineScreen(
    onBack:         () -> Unit,
    onPreviewClick: (String) -> Unit = {},
    onPlansClick:   () -> Unit = {},
    viewModel: TimeMachineViewModel = hiltViewModel()
) {
    val timeline       by viewModel.timeline.collectAsStateWithLifecycle()
    val locale = LocalConfiguration.current.locales.get(0)
    val isCreating      by viewModel.isCreating.collectAsStateWithLifecycle()
    val isRestoring     by viewModel.isRestoring.collectAsStateWithLifecycle()
    val isLoadingDrive  by viewModel.isLoadingDrive.collectAsStateWithLifecycle()
    val driveLoadError  by viewModel.driveLoadError.collectAsStateWithLifecycle()
    val mergeResult     by viewModel.mergeResult.collectAsStateWithLifecycle()
    val error           by viewModel.error.collectAsStateWithLifecycle()
    val restoreSuccess  by viewModel.restoreSuccess.collectAsStateWithLifecycle()
    val isFreeUser      by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val selectedKeys    by viewModel.selectedKeys.collectAsStateWithLifecycle()
    val isDeleting      by viewModel.isDeleting.collectAsStateWithLifecycle()
    val deleteDone      by viewModel.deleteDone.collectAsStateWithLifecycle()
    val isSelectionMode = selectedKeys.isNotEmpty()
    BackHandler(enabled = isSelectionMode) { viewModel.clearSelection() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshDriveSnapshots()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val successMsg          = stringResource(R.string.restore_result_success)
    val mergeMsgTemplate    = stringResource(R.string.restore_result_merge)
    val mergeMsgTemplateOne = stringResource(R.string.restore_result_merge_one)
    val noPasswordMsg       = stringResource(R.string.time_machine_drive_no_password)
    val wrongPasswordMsg    = stringResource(R.string.drive_error_wrong_password)
    val corruptedFileMsg    = stringResource(R.string.import_export_error_corrupted_file)
    val deleteDoneMsg       = stringResource(R.string.time_machine_delete_done)
    val deleteDriveFailMsg  = stringResource(R.string.time_machine_delete_drive_failed)
    val deleteOfflineMsg    = stringResource(R.string.time_machine_delete_offline)
    val todayLabel          = stringResource(R.string.time_relative_today)
    val yesterdayLabel      = stringResource(R.string.time_relative_yesterday)

    LaunchedEffect(Unit) {
        snapshotFlow { restoreSuccess }
            .filter { it }
            .collect {
                viewModel.clearRestoreSuccess()
                snackbarHostState.showSnackbar(successMsg)
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { deleteDone }
            .filter { it }
            .collect {
                viewModel.clearDeleteDone()
                snackbarHostState.showSnackbar(deleteDoneMsg)
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { mergeResult }
            .filterNotNull()
            .collect { result ->
                val template = if (result.restored == 1) mergeMsgTemplateOne else mergeMsgTemplate
                val msg = String.format(locale, template, result.restored)
                viewModel.clearMergeResult()
                snackbarHostState.showSnackbar(msg)
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { error }
            .filterNotNull()
            .collect { msg ->
                viewModel.clearError()
                val displayMsg = when (msg) {
                    TimeMachineViewModel.NO_BACKUP_PASSWORD_SENTINEL     -> noPasswordMsg
                    TimeMachineViewModel.DRIVE_WRONG_PASSWORD_SENTINEL   -> wrongPasswordMsg
                    TimeMachineViewModel.CORRUPTED_FILE_SENTINEL         -> corruptedFileMsg
                    TimeMachineViewModel.DELETE_DRIVE_FAILED_SENTINEL    -> deleteDriveFailMsg
                    TimeMachineViewModel.DELETE_OFFLINE_SENTINEL         -> deleteOfflineMsg
                    else -> msg
                }
                snackbarHostState.showSnackbar(displayMsg)
            }
    }

    var snapshotForRestore by remember { mutableStateOf<VaultSnapshot?>(null) }
    var snapshotForReplace by remember { mutableStateOf<VaultSnapshot?>(null) }
    var snapshotReplaceAuth by remember { mutableStateOf<VaultSnapshot?>(null) }
    var driveReplaceAuth    by remember { mutableStateOf<DriveSnapshotInfo?>(null) }
    var snapshotForDelete  by remember { mutableStateOf<VaultSnapshot?>(null) }
    var driveForRestore    by remember { mutableStateOf<DriveSnapshotInfo?>(null) }
    var driveForReplace    by remember { mutableStateOf<DriveSnapshotInfo?>(null) }
    var driveForDelete     by remember { mutableStateOf<DriveSnapshotInfo?>(null) }
    var showDriveError by remember { mutableStateOf(false) }
    LaunchedEffect(isLoadingDrive) {
        if (!isLoadingDrive && showDriveError && !driveLoadError) showDriveError = false
    }
    var confirmDeleteSelected by remember { mutableStateOf(false) }
    var confirmDeleteAll      by remember { mutableStateOf(false) }
    var authDeleteSelected    by remember { mutableStateOf(false) }
    var authDeleteAll         by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        TextoDeUnaLinea(stringResource(R.string.bulk_selected_count, selectedKeys.size),
                            style = MaterialTheme.typography.titleMedium)
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_cancel))
                        }
                    },
                    actions = {
                        val allSelected = timeline.isNotEmpty() && selectedKeys.size >= timeline.size
                        IconButton(onClick = { viewModel.toggleSelectAll() }) {
                            Icon(if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                                contentDescription = stringResource(
                                    if (allSelected) R.string.bulk_deselect_all else R.string.bulk_select_all))
                        }
                        IconButton(onClick = { if (viewModel.canStartDelete()) confirmDeleteSelected = true }, enabled = !isDeleting) {
                            Icon(Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.time_machine_delete_selected_cd),
                                tint = warningColor())
                        }
                    },
                    colors = topBarColors()
                )
            } else {
            TopAppBar(
                title = { TextoDeUnaLinea(stringResource(R.string.time_machine_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                    color = sectionTitleColor()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (timeline.isNotEmpty()) {
                        IconButton(onClick = { if (viewModel.canStartDelete(deleteAll = true)) confirmDeleteAll = true }, enabled = !isDeleting && !isCreating && !isRestoring) {
                            Icon(Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.time_machine_delete_all_cd),
                                tint = warningColor())
                        }
                    }
                    CaptureAllowedBadge()
                    ThemeTrialBadge()
                },
                colors = topBarColors()
            )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 7.dp, end = 7.dp, bottom = 32.dp)
        ) {

            item {
                val snapshotPrefs = viewModel.snapshotPrefs
                var infoExpanded by remember { mutableStateOf(!snapshotPrefs.hasSeenInfoCard()) }
                LaunchedEffect(Unit) {
                    if (!snapshotPrefs.hasSeenInfoCard()) snapshotPrefs.markInfoCardSeen()
                }
                TimeMachineInfoCard(
                    expanded = infoExpanded,
                    onToggle = { infoExpanded = !infoExpanded },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (isFreeUser) {
                item {
                    UpgradeBanner(
                        onPlansClick = onPlansClick,
                        modifier     = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            if (isCreating || isRestoring) {
                item {
                    val label = if (isCreating)
                        stringResource(R.string.time_machine_creating)
                    else
                        stringResource(R.string.time_machine_restoring)
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            item {
                SettingsTogglesCard(
                    snapshotPrefs = viewModel.snapshotPrefs,
                    isFreeUser    = isFreeUser,
                    modifier      = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                OutlinedButton(
                    onClick  = { viewModel.createManualSnapshot() },
                    enabled  = !isCreating && !isRestoring && !isFreeUser,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .coachMarkAnchor("time_machine", "snapshot"),
                    border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary),
                    colors  = ButtonDefaults.outlinedButtonColors(
                        contentColor         = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.time_machine_create_snapshot))
                }
            }

            if (isLoadingDrive) {
                item {
                    Row(
                        modifier          = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Text(
                            text  = stringResource(R.string.time_machine_syncing_drive),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showDriveError && !isLoadingDrive) {
                item {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp),
                            tint               = warningColor()
                        )
                        Text(
                            text     = stringResource(R.string.time_machine_drive_load_error),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = warningColor(),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick        = { viewModel.refreshDriveSnapshots() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors         = ButtonDefaults.textButtonColors(contentColor = warningColor())
                        ) {
                            Text(
                                text  = stringResource(R.string.time_machine_drive_retry),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            if (timeline.isEmpty() && !isCreating && !isLoadingDrive) {
                item {
                    Column(
                        modifier                = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment     = Alignment.CenterHorizontally,
                        verticalArrangement     = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector  = Icons.Default.History,
                            contentDescription = null,
                            modifier     = Modifier.size(64.dp),
                            tint         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text      = stringResource(R.string.time_machine_empty_title),
                            style     = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text      = stringResource(R.string.time_machine_empty_subtitle),
                            style     = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier  = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
                return@LazyColumn
            }

            data class TimelineItem(
                val entry:     TimelineEntry?,
                val dateLabel: String?
            )

            val dayFormatter  = SimpleDateFormat("yyyyMMdd", locale)
            val timelineItems = buildList {
                var lastDay = ""
                timeline.forEach { entry ->
                    val day = dayFormatter.format(Date(entry.timestamp))
                    if (day != lastDay) {
                        add(TimelineItem(null, formatDayLabel(entry.timestamp, todayLabel, yesterdayLabel)))
                        lastDay = day
                    }
                    add(TimelineItem(entry, null))
                }
            }

            itemsIndexed(timelineItems, key = { _, item ->
                when (val e = item.entry) {
                    is TimelineEntry.Local     -> e.snapshot.id
                    is TimelineEntry.DriveOnly -> "drive_${e.info.driveFileId}"
                    null -> "sep_${item.dateLabel}"
                }
            }) { index, item ->

                if (item.entry == null) {
                    DaySeparator(label = item.dateLabel ?: "", isFirst = index == 0)
                } else {
                    val drawLineAbove = index > 1
                    val drawLineBelow = index < timelineItems.lastIndex
                    val prevIsEntry = index > 0 && timelineItems[index - 1].entry != null
                    val nextIsEntry = index < timelineItems.lastIndex && timelineItems[index + 1].entry != null
                    val isFirstInGroup = !prevIsEntry
                    val isLastInGroup  = !nextIsEntry

                    when (val entry = item.entry) {
                        is TimelineEntry.Local ->
                            TimelineNode(
                                snapshot       = entry.snapshot,
                                drawLineAbove  = drawLineAbove,
                                drawLineBelow  = drawLineBelow,
                                isFirstInGroup = isFirstInGroup,
                                isLastInGroup  = isLastInGroup,
                                enabled        = true,
                                isSelectionMode = isSelectionMode,
                                isSelected     = entry.snapshot.id in selectedKeys,
                                onLongClick    = { viewModel.toggleSelection(entry.snapshot.id) },
                                onClick        = {
                                    if (isSelectionMode) viewModel.toggleSelection(entry.snapshot.id)
                                    else onPreviewClick(entry.snapshot.id)
                                }
                            )
                        is TimelineEntry.DriveOnly ->
                            DriveOnlyTimelineNode(
                                info           = entry.info,
                                drawLineAbove  = drawLineAbove,
                                drawLineBelow  = drawLineBelow,
                                isFirstInGroup = isFirstInGroup,
                                isLastInGroup  = isLastInGroup,
                                enabled        = true,
                                isSelectionMode = isSelectionMode,
                                isSelected     = "drive_${entry.info.driveFileId}" in selectedKeys,
                                onLongClick    = { viewModel.toggleSelection("drive_${entry.info.driveFileId}") },
                                onClick        = {
                                    if (isSelectionMode) viewModel.toggleSelection("drive_${entry.info.driveFileId}")
                                    else if (viewModel.driveUnreachable()) showDriveError = true
                                    else driveForRestore = entry.info
                                }
                            )
                    }
                }
            }
        }

        if (isDeleting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment   = Alignment.CenterHorizontally,
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text  = stringResource(R.string.time_machine_deleting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        }
    }

    snapshotForRestore?.let { snapshot ->
        RestoreConfirmationSheet(
            onReplace = { snapshotForRestore = null; snapshotForReplace = snapshot },
            onMerge   = { snapshotForRestore = null; viewModel.restoreMerge(snapshot.id) },
            onAddAll  = { snapshotForRestore = null; viewModel.restoreAddAll(snapshot.id) },
            onDismiss = { snapshotForRestore = null }
        )
    }

    driveForRestore?.let { info ->
        DriveRestoreConfirmationSheet(
            onReplace = { driveForRestore = null; driveForReplace = info },
            onMerge   = { driveForRestore = null; viewModel.restoreMergeFromDrive(info) },
            onAddAll  = { driveForRestore = null; viewModel.restoreAddAllFromDrive(info) },
            onDelete  = { driveForRestore = null; driveForDelete = info },
            onDismiss = { driveForRestore = null }
        )
    }

    val clavePendiente by viewModel.clavePendiente.collectAsStateWithLifecycle()
    clavePendiente?.let { pend ->
        var palabras by remember(pend.info.driveFileId) { mutableStateOf("") }
        AlertDialog(
        modifier = Modifier.cuadroConTeclado(),
        properties = propiedadesConTeclado(),
            onDismissRequest = { if (!isRestoring) viewModel.cancelarClavePendiente() },
            title = { Text(stringResource(R.string.drive_restore_password_title), fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isRestoring) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    androidx.compose.material3.OutlinedTextField(
                        value         = palabras,
                        onValueChange = { palabras = it },
                        label         = { Text(stringResource(R.string.key_restore_hint)) },
                        minLines      = 2,
                        isError       = pend.error != null,
                        supportingText = pend.error?.let { { Text(it, color = warningColor()) } },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            errorBorderColor = warningColor(),
                            errorLabelColor  = warningColor(),
                            errorCursorColor = warningColor()
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType       = KeyboardType.Text,
                            capitalization     = androidx.compose.ui.text.input.KeyboardCapitalization.None,
                            autoCorrectEnabled = false
                        )
                    )
                    val ctx = androidx.compose.ui.platform.LocalContext.current
                    androidx.compose.animation.AnimatedVisibility(visible = palabras.isEmpty()) {
                        androidx.compose.material3.TextButton(onClick = {
                            val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            cm.primaryClip?.getItemAt(0)?.coerceToText(ctx)?.toString()?.let { palabras = it }
                        }) { Text(stringResource(R.string.key_paste)) }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.confirmarClavePendiente(palabras) },
                    enabled  = palabras.isNotBlank() && !isRestoring,
                    colors   = com.thormakk.keyarmor.ui.theme.appButtonColors()
                ) { Text(stringResource(R.string.drive_action_restore)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { viewModel.cancelarClavePendiente() }, enabled = !isRestoring
                ) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    driveForReplace?.let { info ->
        ReplaceConfirmationDialog(
            onConfirm = { driveForReplace = null; driveReplaceAuth = info },
            onDismiss = { driveForReplace = null },
            warning   = stringResource(R.string.time_machine_replace_no_attachments_warning)
        )
    }

    driveReplaceAuth?.let { info ->
        ReplaceAuthGate(
            onAuthorized         = { driveReplaceAuth = null; viewModel.restoreReplaceFromDrive(info) },
            onDismiss            = { driveReplaceAuth = null },
            verifyMasterPassword = viewModel::verifyMasterPassword
        )
    }

    snapshotReplaceAuth?.let { snapshot ->
        ReplaceAuthGate(
            onAuthorized         = { snapshotReplaceAuth = null; viewModel.restoreReplace(snapshot.id) },
            onDismiss            = { snapshotReplaceAuth = null },
            verifyMasterPassword = viewModel::verifyMasterPassword
        )
    }

    driveForDelete?.let { info ->
        AlertDialog(
            onDismissRequest = { driveForDelete = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.time_machine_delete_confirm),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = { driveForDelete = null; viewModel.deleteFromDrive(info) },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { driveForDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (confirmDeleteSelected) {
        DeleteSnapshotsConfirmDialog(
            title     = stringResource(R.string.time_machine_delete_selected_title),
            body      = stringResource(
                if (viewModel.deleteTouchesDrive()) R.string.time_machine_delete_selected_body
                else R.string.time_machine_delete_selected_body_local
            ),
            onConfirm = { confirmDeleteSelected = false; authDeleteSelected = true },
            onDismiss = { confirmDeleteSelected = false }
        )
    }
    if (confirmDeleteAll) {
        DeleteSnapshotsConfirmDialog(
            title     = stringResource(R.string.time_machine_delete_all_title),
            body      = stringResource(
                if (viewModel.deleteTouchesDrive(deleteAll = true)) R.string.time_machine_delete_all_body
                else R.string.time_machine_delete_all_body_local
            ),
            onConfirm = { confirmDeleteAll = false; authDeleteAll = true },
            onDismiss = { confirmDeleteAll = false }
        )
    }
    if (authDeleteSelected) {
        DeleteSnapshotsAuthGate(
            onAuthorized         = { authDeleteSelected = false; viewModel.deleteSelected() },
            onDismiss            = { authDeleteSelected = false },
            verifyMasterPassword = viewModel::verifyMasterPassword
        )
    }
    if (authDeleteAll) {
        DeleteSnapshotsAuthGate(
            onAuthorized         = { authDeleteAll = false; viewModel.deleteAll() },
            onDismiss            = { authDeleteAll = false },
            verifyMasterPassword = viewModel::verifyMasterPassword
        )
    }

    snapshotForReplace?.let { snapshot ->
        ReplaceConfirmationDialog(
            onConfirm = { snapshotForReplace = null; snapshotReplaceAuth = snapshot },
            onDismiss = { snapshotForReplace = null },
            warning   = if (snapshot.includesAttachments) null
                        else stringResource(R.string.time_machine_replace_no_attachments_warning)
        )
    }

    snapshotForDelete?.let { snapshot ->
        AlertDialog(
            onDismissRequest = { snapshotForDelete = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.time_machine_delete_confirm),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = if (snapshot.syncedToDrive) ({
                Text(
                    text  = stringResource(R.string.time_machine_delete_confirm_drive),
                    style = MaterialTheme.typography.bodyMedium.justified(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }) else null,
            confirmButton = {
                Button(
                    onClick = { snapshotForDelete = null; viewModel.deleteSnapshot(snapshot.id) },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { snapshotForDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }
}

@Composable
private fun DeleteSnapshotsConfirmDialog(
    title:     String,
    body:      String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        },
        text = {
            Text(body, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
            }
        }
    )
}

private val SLOT_SIZE = 24.dp

private val TARJETA_ALTO_MIN = 48.dp

@Composable
private fun SelectionSlot(
    enabled:         Boolean,
    isSelectionMode: Boolean,
    isSelected:      Boolean,
    onToggle:        () -> Unit
) {
    Box(modifier = Modifier.size(SLOT_SIZE), contentAlignment = Alignment.Center) {
        if (isSelectionMode) {
            Checkbox(
                checked         = isSelected,
                onCheckedChange = { onToggle() },
                modifier        = Modifier.size(SLOT_SIZE),
                colors          = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
        } else {
            Icon(
                imageVector        = Icons.Default.History,
                contentDescription = null,
                modifier           = Modifier.size(18.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.6f else 0.3f)
            )
        }
    }
}

@Composable
private fun DriveOnlyTimelineNode(
    info:           DriveSnapshotInfo,
    drawLineAbove:  Boolean,
    drawLineBelow:  Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup:  Boolean,
    enabled:        Boolean = true,
    isSelectionMode: Boolean = false,
    isSelected:     Boolean = false,
    onLongClick:    () -> Unit = {},
    onClick:        () -> Unit
) {
    val locale    = LocalConfiguration.current.locales.get(0)
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val dotColor  = triggerColor(info.trigger)

    val dateFormatter = remember(locale) { SimpleDateFormat("HH:mm", locale) }
    val formattedTime = remember(info.timestamp) { dateFormatter.format(Date(info.timestamp)) }

    val topCorner    = if (isFirstInGroup) 8.dp else 0.dp
    val bottomCorner = if (isLastInGroup)  8.dp else 0.dp
    val cardShape    = RoundedCornerShape(
        topStart    = topCorner,    topEnd    = topCorner,
        bottomStart = bottomCorner, bottomEnd = bottomCorner
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val cx         = TIMELINE_GUTTER.toPx() / 2f
                val dotCenterY = size.height / 2f
                val lineStart  = if (drawLineAbove) 0f          else dotCenterY
                val lineEnd    = if (drawLineBelow) size.height else dotCenterY
                if (lineStart < lineEnd) {
                    drawLine(
                        color       = lineColor,
                        start       = Offset(cx, lineStart),
                        end         = Offset(cx, lineEnd),
                        strokeWidth = LINE_WIDTH.toPx()
                    )
                }
            }
    ) {
        Box(
            modifier         = Modifier.width(TIMELINE_GUTTER).align(Alignment.CenterVertically),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(DOT_SIZE).background(dotColor, CircleShape))
        }

        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 1.dp, bottom = 1.dp)
                    .clip(cardShape)
                    .combinedClickable(enabled = enabled, onLongClick = onLongClick, onClick = onClick),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                                     else MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TARJETA_ALTO_MIN)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionSlot(enabled = enabled, isSelectionMode = isSelectionMode, isSelected = isSelected, onToggle = onClick)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text     = triggerLabel(info.trigger),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = if (enabled) dotColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector        = Icons.Default.Cloud,
                        contentDescription = null,
                        modifier           = Modifier.size(12.dp),
                        tint               = if (enabled) MaterialTheme.colorScheme.primary
                                             else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text       = stringResource(R.string.time_machine_drive_only),
                        style      = MaterialTheme.typography.labelSmall,
                        color      = if (enabled) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (enabled) 1f else 0.5f
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DriveRestoreConfirmationSheet(
    onReplace: () -> Unit,
    onMerge:   () -> Unit,
    onAddAll:  () -> Unit,
    onDelete:  () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier            = Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text       = stringResource(R.string.restore_sheet_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            RestoreModeRow(stringResource(R.string.restore_mode_replace),  stringResource(R.string.restore_mode_replace_desc),  onReplace)
            RestoreModeRow(stringResource(R.string.restore_mode_merge),    stringResource(R.string.restore_mode_merge_desc),    onMerge)
            RestoreModeRow(stringResource(R.string.restore_mode_add_all),  stringResource(R.string.restore_mode_add_all_desc),  onAddAll)

            HorizontalDivider(
                modifier  = Modifier.padding(top = 4.dp),
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick  = onDelete,
                shape    = RoundedCornerShape(8.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = stringResource(R.string.time_machine_delete_from_drive))
            }
        }
    }
}

@Composable
private fun DaySeparator(label: String, isFirst: Boolean) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (!isFirst) {
                    val cx = TIMELINE_GUTTER.toPx() / 2f
                    drawLine(
                        color       = lineColor,
                        start       = Offset(cx, 0f),
                        end         = Offset(cx, size.height),
                        strokeWidth = LINE_WIDTH.toPx()
                    )
                }
            }
            .padding(top = if (isFirst) 8.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(TIMELINE_GUTTER))
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
private fun TimelineNode(
    snapshot:       VaultSnapshot,
    drawLineAbove:  Boolean,
    drawLineBelow:  Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup:  Boolean,
    enabled:        Boolean = true,
    isSelectionMode: Boolean = false,
    isSelected:     Boolean = false,
    onLongClick:    () -> Unit = {},
    onClick:        () -> Unit
) {
    val locale    = LocalConfiguration.current.locales.get(0)
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val dotColor  = triggerColor(snapshot.trigger)

    val dateFormatter = remember(locale) { SimpleDateFormat("HH:mm", locale) }
    val formattedTime = remember(snapshot.timestamp) { dateFormatter.format(Date(snapshot.timestamp)) }
    val sizeKb        = String.format(locale, "%.1f", snapshot.fileSizeBytes / 1024f)

    val topCorner    = if (isFirstInGroup) 8.dp else 0.dp
    val bottomCorner = if (isLastInGroup)  8.dp else 0.dp
    val cardShape    = RoundedCornerShape(
        topStart    = topCorner,    topEnd    = topCorner,
        bottomStart = bottomCorner, bottomEnd = bottomCorner
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val cx         = TIMELINE_GUTTER.toPx() / 2f
                val dotCenterY = size.height / 2f
                val lineStart  = if (drawLineAbove) 0f          else dotCenterY
                val lineEnd    = if (drawLineBelow) size.height else dotCenterY
                if (lineStart < lineEnd) {
                    drawLine(
                        color       = lineColor,
                        start       = Offset(cx, lineStart),
                        end         = Offset(cx, lineEnd),
                        strokeWidth = LINE_WIDTH.toPx()
                    )
                }
            }
    ) {
        Box(
            modifier         = Modifier.width(TIMELINE_GUTTER).align(Alignment.CenterVertically),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(DOT_SIZE).background(dotColor, CircleShape))
        }

        val disabledAlpha = 0.45f
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 1.dp, bottom = 1.dp)
                    .clip(cardShape)
                    .combinedClickable(enabled = enabled, onLongClick = onLongClick, onClick = onClick),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                                     else MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                elevation  = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TARJETA_ALTO_MIN)
                        .padding(horizontal = 12.dp, vertical = if (snapshot.includesAttachments) 12.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionSlot(enabled = enabled, isSelectionMode = isSelectionMode, isSelected = isSelected, onToggle = onClick)
                    Spacer(Modifier.width(8.dp))
                    val effectiveDot  = if (enabled) dotColor else dotColor.copy(alpha = disabledAlpha)
                    val noAttColor    = warningColor()
                    val metaColor     = MaterialTheme.colorScheme.onSurfaceVariant.let {
                        if (enabled) it else it.copy(alpha = disabledAlpha)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = effectiveDot, fontWeight = FontWeight.SemiBold)) {
                                    append(triggerLabel(snapshot.trigger))
                                }
                                withStyle(SpanStyle(color = metaColor)) {
                                    append(" · ")
                                    append(
                                        pluralStringResource(
                                            R.plurals.vault_entries_count,
                                            snapshot.entryCount,
                                            snapshot.entryCount
                                        )
                                    )
                                    append(" · ")
                                    append(stringResource(R.string.time_machine_size_kb, sizeKb))
                                }
                            },
                            style    = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!snapshot.includesAttachments) {
                            InfoChip(
                                label    = stringResource(R.string.time_machine_no_attachments_label),
                                color    = if (enabled) noAttColor else noAttColor.copy(alpha = disabledAlpha),
                                modifier = Modifier.padding(top = 2.dp),
                                verticalPadding = 1.dp
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = metaColor
                    )
                    if (snapshot.syncedToDrive) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector        = Icons.Default.Cloud,
                            contentDescription = stringResource(R.string.time_machine_synced_drive),
                            modifier           = Modifier.size(12.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (enabled) 0.5f else disabledAlpha * 0.5f
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun triggerColor(trigger: SnapshotTrigger): Color {
    val background = MaterialTheme.colorScheme.background

    fun readable(base: Color, bright: Color): Color =
        if (contrastRatio(background, base) < 3f) bright else base

    return when (trigger) {
        SnapshotTrigger.WEEKLY            -> MaterialTheme.colorScheme.primary
        SnapshotTrigger.MANUAL            -> readable(Color(0xFF00897B), Color(0xFF4DB6AC))
        SnapshotTrigger.PRE_BULK_DELETE   -> readable(Color(0xFFE65100), Color(0xFFFFA726))
        SnapshotTrigger.PRE_MASTER_CHANGE -> readable(Color(0xFFC62828), Color(0xFFEF5350))
        SnapshotTrigger.PRE_RESTORE       -> readable(Color(0xFF6A1B9A), Color(0xFFBA68C8))
        SnapshotTrigger.PRE_IMPORT_CSV,
        SnapshotTrigger.PRE_IMPORT_JSON,
        SnapshotTrigger.PRE_IMPORT_KAB    -> readable(Color(0xFF1565C0), Color(0xFF64B5F6))
    }
}

@Composable
internal fun triggerLabel(trigger: SnapshotTrigger): String = when (trigger) {
    SnapshotTrigger.WEEKLY            -> stringResource(R.string.time_machine_trigger_weekly)
    SnapshotTrigger.MANUAL            -> stringResource(R.string.time_machine_trigger_manual)
    SnapshotTrigger.PRE_BULK_DELETE   -> stringResource(R.string.time_machine_trigger_pre_bulk_delete)
    SnapshotTrigger.PRE_MASTER_CHANGE -> stringResource(R.string.time_machine_trigger_pre_master_change)
    SnapshotTrigger.PRE_RESTORE       -> stringResource(R.string.time_machine_trigger_pre_restore)
    SnapshotTrigger.PRE_IMPORT_CSV    -> stringResource(R.string.time_machine_trigger_pre_import) + " (CSV)"
    SnapshotTrigger.PRE_IMPORT_JSON   -> stringResource(R.string.time_machine_trigger_pre_import) + " (JSON)"
    SnapshotTrigger.PRE_IMPORT_KAB    -> stringResource(R.string.time_machine_trigger_pre_import) + " (KAB)"
}

private fun formatDayLabel(timestamp: Long, todayLabel: String, yesterdayLabel: String): String {
    val cal   = Calendar.getInstance()
    val today = Calendar.getInstance()
    cal.timeInMillis = timestamp
    return when {
        cal.get(Calendar.YEAR)         == today.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR)  == today.get(Calendar.DAY_OF_YEAR) -> todayLabel
        cal.get(Calendar.YEAR)         == today.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR)  == today.get(Calendar.DAY_OF_YEAR) - 1 -> yesterdayLabel
        else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
private fun TimeMachineInfoCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onToggle
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector        = Icons.Default.History,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.secondary,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = stringResource(R.string.settings_time_machine),
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier   = Modifier.weight(1f)
                )
                Icon(
                    imageVector        = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text      = stringResource(R.string.time_machine_info_body),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )

                    Text(
                        text      = stringResource(R.string.hidden_entries_import_note),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Lock,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.secondary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text       = stringResource(R.string.time_machine_encryption_title),
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text      = stringResource(R.string.time_machine_encryption_local),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text      = stringResource(R.string.time_machine_encryption_drive),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text      = stringResource(R.string.time_machine_encryption_priority),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Info,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.secondary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text       = stringResource(R.string.time_machine_legend_title),
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    LegendLine(stringResource(R.string.time_machine_legend_cloud))
                    LegendLine(stringResource(R.string.time_machine_legend_local))
                    LegendLine(stringResource(R.string.time_machine_legend_drive_only))
                }
            }
        }
    }
}

@Composable
private fun LegendLine(text: String) {
    val separatorIndex = text.indexOf(':')
    val annotated = if (separatorIndex >= 0) {
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(text.substring(0, separatorIndex + 1))
            }
            append(text.substring(separatorIndex + 1))
        }
    } else {
        buildAnnotatedString { append(text) }
    }
    Text(
        text      = annotated,
        style     = MaterialTheme.typography.bodySmall.justified(),
        color     = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
    )
}

@Composable
private fun UpgradeBanner(onPlansClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier      = modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(8.dp),
        color         = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        contentColor  = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Lock,
                contentDescription = null,
                modifier           = Modifier.size(16.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
            Text(
                text     = stringResource(R.string.drive_sync_free_locked),
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick        = onPlansClick,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text  = stringResource(R.string.plans_upgrade_cta),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SettingsTogglesCard(
    snapshotPrefs: SnapshotPrefs,
    modifier:      Modifier = Modifier,
    isFreeUser:    Boolean = false
) {
    var weeklyEnabled by remember { mutableStateOf(snapshotPrefs.isWeeklyEnabled()) }
    var driveEnabled  by remember { mutableStateOf(snapshotPrefs.isSyncToDriveEnabled()) }
    var attachEnabled by remember { mutableStateOf(snapshotPrefs.isIncludeAttachmentsEnabled()) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = stringResource(R.string.time_machine_weekly_toggle),
                    modifier = Modifier.weight(1f),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = if (isFreeUser) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                               else MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked         = weeklyEnabled && !isFreeUser,
                    onCheckedChange = { weeklyEnabled = it; snapshotPrefs.setWeeklyEnabled(it) },
                    enabled         = !isFreeUser
                )
            }
            HorizontalDivider()
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = stringResource(R.string.time_machine_drive_toggle),
                    modifier = Modifier.weight(1f),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = if (isFreeUser) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                               else MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked         = driveEnabled && !isFreeUser,
                    onCheckedChange = { driveEnabled = it; snapshotPrefs.setSyncToDriveEnabled(it) },
                    enabled         = !isFreeUser
                )
            }
            HorizontalDivider()
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = stringResource(R.string.time_machine_attachments_toggle),
                    modifier = Modifier.weight(1f),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = if (isFreeUser) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                               else MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked         = attachEnabled && !isFreeUser,
                    onCheckedChange = { attachEnabled = it; snapshotPrefs.setIncludeAttachmentsEnabled(it) },
                    enabled         = !isFreeUser
                )
            }
        }
    }
}

@Composable
private fun RestoreConfirmationSheet(
    onReplace: () -> Unit,
    onMerge:   () -> Unit,
    onAddAll:  () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text       = stringResource(R.string.restore_sheet_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            RestoreModeRow(stringResource(R.string.restore_mode_replace),  stringResource(R.string.restore_mode_replace_desc),  onReplace)
            RestoreModeRow(stringResource(R.string.restore_mode_merge),    stringResource(R.string.restore_mode_merge_desc),    onMerge)
            RestoreModeRow(stringResource(R.string.restore_mode_add_all),  stringResource(R.string.restore_mode_add_all_desc),  onAddAll)
        }
    }
}

@Composable
private fun RestoreModeRow(title: String, description: String, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun ReplaceConfirmationDialog(
    onConfirm:   () -> Unit,
    onDismiss:   () -> Unit,
    description: String = stringResource(R.string.restore_mode_replace_desc),
    warning:     String? = null
) {
    val confirmWord = stringResource(R.string.restore_mode_replace_confirm_word)
    var typedText   by remember { mutableStateOf("") }
    val autoLockManager = LocalAutoLockManager.current

    AlertDialog(
        modifier = Modifier.cuadroConTeclado(),
        properties = propiedadesConTeclado(),
        onDismissRequest = onDismiss,
        title            = { Text(stringResource(R.string.restore_mode_replace)) },
        text             = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(description,
                    style = MaterialTheme.typography.bodyMedium)
                if (warning != null) {
                    Text(warning,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = warningColor())
                }
                OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                    value         = typedText,
                    onValueChange = { autoLockManager?.recordInteraction(); typedText = it },
                    label         = { Text(stringResource(R.string.restore_mode_replace_confirm_hint)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = typedText == confirmWord,
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.restore_mode_replace))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
