@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.thormakk.keyarmor.presentation.screens.reminders

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.ReminderSchedule
import com.thormakk.keyarmor.presentation.components.chipCategoryColor
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.formatDateLocalized
import com.thormakk.keyarmor.presentation.components.chipTagColor
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.vault.EntryOptionsSheet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

private enum class AlertType { PUNCTUAL, WEEKLY, MONTHLY, YEARLY, INTERVAL, BEFORE_DEADLINE }

@Composable
fun ReminderAddEditScreen(
    onBack:             () -> Unit,
    onAttachmentsClick: () -> Unit = {},
    viewModel:          ReminderAddEditViewModel = hiltViewModel()
) {
    val autoLockManager = LocalAutoLockManager.current
    val name           by viewModel.name.collectAsStateWithLifecycle()
    val dateMs         by viewModel.dateMs.collectAsStateWithLifecycle()
    val timeHour       by viewModel.timeHour.collectAsStateWithLifecycle()
    val timeMinute     by viewModel.timeMinute.collectAsStateWithLifecycle()
    val note           by viewModel.note.collectAsStateWithLifecycle()
    val schedules      by viewModel.schedules.collectAsStateWithLifecycle()
    val done           by viewModel.done.collectAsStateWithLifecycle()
    val isLoading      by viewModel.isLoading.collectAsStateWithLifecycle()
    val isPinned       by viewModel.isPinned.collectAsStateWithLifecycle()
    val isFavorite     by viewModel.isFavorite.collectAsStateWithLifecycle()
    val tags           by viewModel.tags.collectAsStateWithLifecycle()
    val category       by viewModel.category.collectAsStateWithLifecycle()
    val availableTags  by viewModel.availableTags.collectAsStateWithLifecycle()

    var showDeleteDialog   by remember { mutableStateOf(false) }
    var showDatePicker     by remember { mutableStateOf(false) }
    var showTimePicker     by remember { mutableStateOf(false) }
    var showAlertSheet     by remember { mutableStateOf(false) }
    var showOptionsMenu    by remember { mutableStateOf(false) }
    var showTagsSheet      by remember { mutableStateOf(false) }
    var editingSchedule    by remember { mutableStateOf<ReminderSchedule?>(null) }

    LaunchedEffect(done) { if (done) onBack() }

    val lockedByProtection by viewModel.lockedByProtection.collectAsStateWithLifecycle()
    if (lockedByProtection) {
        AlertDialog(
            onDismissRequest = onBack,
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_locked_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.protect_locked_edit), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_understood)) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
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
                    onClick = { showDeleteDialog = false; viewModel.delete() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showTimePicker) {
        val tpState = rememberTimePickerState(
            initialHour   = timeHour ?: 9,
            initialMinute = timeMinute ?: 0,
            is24Hour      = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.reminder_field_time)) },
            text  = { TimeInput(state = tpState) },
            confirmButton = {
                Button(onClick = {
                    viewModel.onTimeChange(tpState.hour, tpState.minute)
                    showTimePicker = false
                }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)) { Text(stringResource(R.string.action_accept), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showAlertSheet) {
        AddAlertSheet(
            initialSchedule = editingSchedule,
            onDismiss       = { showAlertSheet = false; editingSchedule = null },
            onAdd           = { schedule ->
                if (editingSchedule != null) viewModel.updateSchedule(schedule)
                else viewModel.addSchedule(schedule)
                editingSchedule = null
                showAlertSheet = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextoDeUnaLinea(
                        stringResource(
                            if (viewModel.isNew) R.string.reminder_add_title
                            else                 R.string.reminder_edit_title
                        ),
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
                    if (!viewModel.isNew) {
                        IconButton(onClick = { viewModel.togglePin() }) {
                            Icon(
                                imageVector        = Icons.Default.PushPin,
                                contentDescription = stringResource(if (isPinned) R.string.action_unpin else R.string.action_pin),
                                tint               = if (isPinned) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded         = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.action_tags_and_categories)) },
                                    onClick     = { showOptionsMenu = false; showTagsSheet = true },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null) }
                                )
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.action_attachments)) },
                                    onClick     = { showOptionsMenu = false; onAttachmentsClick() },
                                    leadingIcon = { Icon(Icons.Default.AttachFile, null) }
                                )
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.action_clone)) },
                                    onClick     = { showOptionsMenu = false; viewModel.clone() },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.action_archive)) },
                                    onClick     = { showOptionsMenu = false; viewModel.toggleArchive() },
                                    leadingIcon = { Icon(Icons.Default.Archive, null) }
                                )
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.action_move_to_trash), color = dangerColor()) },
                                    onClick     = { showOptionsMenu = false; showDeleteDialog = true },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = dangerColor()) }
                                )
                            }
                        }
                    }
                },
                colors = topBarColors()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { autoLockManager?.recordInteraction(); viewModel.onNameChange(it) },
                    placeholder   = { Text(stringResource(R.string.reminder_field_name), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(8.dp),
                    colors        = fieldColors(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                    trailingIcon  = {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector        = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = stringResource(if (isFavorite) R.string.action_remove_favorite else R.string.action_favorite),
                                tint               = if (isFavorite) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1.6f)) {
                        OutlinedTextField(
                            value         = dateMs?.let { formatDateLocalized(it, Locale.getDefault(), utc = true) } ?: "",
                            onValueChange = {},
                            placeholder   = { Text(stringResource(R.string.reminder_field_date), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(8.dp),
                            colors        = fieldColors(),
                            readOnly      = true,
                            trailingIcon  = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value         = if (timeHour != null && timeMinute != null) "%02d:%02d".format(timeHour, timeMinute) else "",
                            onValueChange = {},
                            placeholder   = { Text(stringResource(R.string.reminder_field_time), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(8.dp),
                            colors        = fieldColors(),
                            readOnly      = true,
                            trailingIcon  = {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
                    }
                }

                OutlinedTextField(
                    value         = note,
                    onValueChange = { autoLockManager?.recordInteraction(); viewModel.onNoteChange(it) },
                    placeholder   = { Text(stringResource(R.string.reminder_field_note), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape         = RoundedCornerShape(8.dp),
                    colors        = fieldColors(),
                    maxLines      = 10,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                FlowRow(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    if (category != EntryCategory.SIN_CATEGORIZAR) {
                        val catColor = chipCategoryColor()
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = catColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text     = stringResource(category.labelRes),
                                style    = MaterialTheme.typography.labelMedium,
                                color    = catColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    val tagColor = chipTagColor()
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = tagColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text     = tag,
                                style    = MaterialTheme.typography.labelMedium,
                                color    = tagColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .clip(RoundedCornerShape(50))
                            .clickable { showTagsSheet = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text  = "+ ${stringResource(R.string.options_sheet_tags)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = stringResource(R.string.reminder_alerts_section),
                        style      = MaterialTheme.typography.titleSmall,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text     = stringResource(R.string.reminder_alert_add),
                        style    = MaterialTheme.typography.labelMedium,
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(0.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                            .clickable { showAlertSheet = true }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                schedules.forEach { schedule ->
                    ScheduleRow(
                        schedule      = schedule,
                        reminderName  = name,
                        onDelete      = { viewModel.removeSchedule(schedule.id) },
                        onEdit        = { editingSchedule = schedule; showAlertSheet = true }
                    )
                    Spacer(Modifier.height(4.dp))
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick  = viewModel::save,
                    enabled  = name.isNotBlank() && dateMs != null,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        stringResource(R.string.action_apply),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    if (showTagsSheet) {
        EntryOptionsSheet(
            initialCategory = category,
            initialTags     = tags,
            availableTags   = availableTags,
            onSave          = { cat, newTags ->
                viewModel.updateTagsAndCategory(cat, newTags)
                showTagsSheet = false
            },
            onDismiss      = { showTagsSheet = false },
            onAddTag       = { viewModel.addAvailableTag(it) },
            onRenameTag    = { old, new -> viewModel.renameAvailableTag(old, new) },
            onDeleteTag    = { viewModel.deleteAvailableTag(it) },
            tagConflictKey = { viewModel.tagConflictKey(it) },
            canCreateTags  = true,
            onUpgradeClick = {}
        )
    }
}
}

@Composable
private fun ScheduleRow(
    schedule:     ReminderSchedule,
    reminderName: String,
    onDelete:     () -> Unit,
    onEdit:       () -> Unit = {}
) {
    Surface(
        shape    = RoundedCornerShape(8.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = schedule.typeLabel(),
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (reminderName.isNotBlank()) {
                    Text(
                        text       = reminderName,
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text  = schedule.description(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.reminder_alert_delete_cd),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddAlertSheet(
    initialSchedule: ReminderSchedule? = null,
    onDismiss:       () -> Unit,
    onAdd:           (ReminderSchedule) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val autoLockManager = LocalAutoLockManager.current

    fun initHour(): String = when (initialSchedule) {
        is ReminderSchedule.Punctual -> {
            val c = Calendar.getInstance().apply { timeInMillis = initialSchedule.triggerAtMs }
            "%02d".format(c.get(Calendar.HOUR_OF_DAY))
        }
        is ReminderSchedule.Weekly         -> "%02d".format(initialSchedule.hour)
        is ReminderSchedule.Monthly        -> "%02d".format(initialSchedule.hour)
        is ReminderSchedule.Yearly         -> "%02d".format(initialSchedule.hour)
        is ReminderSchedule.Interval       -> "%02d".format(initialSchedule.hour)
        is ReminderSchedule.BeforeDeadline -> "%02d".format(initialSchedule.hour)
        null                               -> ""
    }
    fun initMinute(): String = when (initialSchedule) {
        is ReminderSchedule.Punctual -> {
            val c = Calendar.getInstance().apply { timeInMillis = initialSchedule.triggerAtMs }
            "%02d".format(c.get(Calendar.MINUTE))
        }
        is ReminderSchedule.Weekly         -> "%02d".format(initialSchedule.minute)
        is ReminderSchedule.Monthly        -> "%02d".format(initialSchedule.minute)
        is ReminderSchedule.Yearly         -> "%02d".format(initialSchedule.minute)
        is ReminderSchedule.Interval       -> "%02d".format(initialSchedule.minute)
        is ReminderSchedule.BeforeDeadline -> "%02d".format(initialSchedule.minute)
        null                               -> ""
    }

    var selectedType by remember {
        mutableStateOf(when (initialSchedule) {
            is ReminderSchedule.Punctual       -> AlertType.PUNCTUAL
            is ReminderSchedule.Weekly         -> AlertType.WEEKLY
            is ReminderSchedule.Monthly        -> AlertType.MONTHLY
            is ReminderSchedule.Yearly         -> AlertType.YEARLY
            is ReminderSchedule.Interval       -> AlertType.INTERVAL
            is ReminderSchedule.BeforeDeadline -> AlertType.BEFORE_DEADLINE
            null                               -> AlertType.PUNCTUAL
        })
    }

    var hourText   by remember { mutableStateOf(initHour()) }
    var minuteText by remember { mutableStateOf(initMinute()) }

    var punctualDateMs  by remember {
        mutableStateOf<Long?>(
            if (initialSchedule is ReminderSchedule.Punctual) initialSchedule.triggerAtMs
            else null
        )
    }
    var selectedDays    by remember {
        mutableStateOf(
            if (initialSchedule is ReminderSchedule.Weekly) initialSchedule.daysOfWeek
            else setOf()
        )
    }
    var dayOfMonth      by remember {
        mutableStateOf(
            when (initialSchedule) {
                is ReminderSchedule.Monthly -> initialSchedule.dayOfMonth.toString()
                is ReminderSchedule.Yearly  -> initialSchedule.dayOfMonth.toString()
                else                        -> "1"
            }
        )
    }
    var monthNum        by remember {
        mutableStateOf(
            if (initialSchedule is ReminderSchedule.Yearly) initialSchedule.month.toString() else "1"
        )
    }
    var intervalDays    by remember {
        mutableStateOf(
            if (initialSchedule is ReminderSchedule.Interval) initialSchedule.intervalDays.toString() else "7"
        )
    }
    var intervalStartMs by remember {
        mutableStateOf<Long?>(
            if (initialSchedule is ReminderSchedule.Interval) initialSchedule.startMs
            else null
        )
    }
    var daysBeforeCount by remember {
        mutableStateOf(
            if (initialSchedule is ReminderSchedule.BeforeDeadline) initialSchedule.daysBeforeCount.toString() else "1"
        )
    }

    var showStartDatePicker   by remember { mutableStateOf(false) }
    var showPunctualDatePicker by remember { mutableStateOf(false) }

    if (showPunctualDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = punctualDateMs ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showPunctualDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { punctualDateMs = it }
                    showPunctualDatePicker = false
                }) { Text(stringResource(R.string.action_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showPunctualDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showStartDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = intervalStartMs ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { intervalStartMs = it }
                    showStartDatePicker = false
                }) { Text(stringResource(R.string.action_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) { DatePicker(state = pickerState) }
    }

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = sheetState,
        containerColor    = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text       = stringResource(
                    if (initialSchedule != null) R.string.reminder_alert_sheet_title_edit
                    else                         R.string.reminder_alert_sheet_title
                ),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            val types = listOf(
                AlertType.PUNCTUAL       to stringResource(R.string.reminder_alert_type_punctual),
                AlertType.WEEKLY         to stringResource(R.string.reminder_alert_type_weekly),
                AlertType.MONTHLY        to stringResource(R.string.reminder_alert_type_monthly),
                AlertType.YEARLY         to stringResource(R.string.reminder_alert_type_yearly),
                AlertType.INTERVAL       to stringResource(R.string.reminder_alert_type_interval),
                AlertType.BEFORE_DEADLINE to stringResource(R.string.reminder_alert_type_before)
            )
            FlowRow(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow     = 3
            ) {
                types.forEach { (type, label) ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick  = { selectedType = type },
                        label    = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            HorizontalDivider()

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = hourText,
                    onValueChange = { autoLockManager?.recordInteraction(); if (it.length <= 2) hourText = it.filter(Char::isDigit) },
                    placeholder   = { Text(stringResource(R.string.reminder_alert_field_hour), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    shape         = RoundedCornerShape(8.dp),
                    colors        = fieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value         = minuteText,
                    onValueChange = { autoLockManager?.recordInteraction(); if (it.length <= 2) minuteText = it.filter(Char::isDigit) },
                    placeholder   = { Text(stringResource(R.string.reminder_alert_field_minute), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    shape         = RoundedCornerShape(8.dp),
                    colors        = fieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            when (selectedType) {
                AlertType.PUNCTUAL -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value         = punctualDateMs?.let { formatDateLocalized(it, Locale.getDefault(), utc = true) } ?: "",
                            onValueChange = {},
                            placeholder   = { Text(stringResource(R.string.reminder_field_date), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            modifier      = Modifier.fillMaxWidth(),
                            readOnly      = true,
                            shape         = RoundedCornerShape(8.dp),
                            colors        = fieldColors(),
                            trailingIcon  = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showPunctualDatePicker = true })
                    }
                }

                AlertType.WEEKLY -> {
                    Text(
                        text  = stringResource(R.string.reminder_alert_days_of_week),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val dayLabels = listOf(
                        1 to stringResource(R.string.reminder_alert_mon),
                        2 to stringResource(R.string.reminder_alert_tue),
                        3 to stringResource(R.string.reminder_alert_wed),
                        4 to stringResource(R.string.reminder_alert_thu),
                        5 to stringResource(R.string.reminder_alert_fri),
                        6 to stringResource(R.string.reminder_alert_sat),
                        7 to stringResource(R.string.reminder_alert_sun)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        dayLabels.forEach { (iso, label) ->
                            FilterChip(
                                selected = iso in selectedDays,
                                onClick  = {
                                    selectedDays = if (iso in selectedDays)
                                        selectedDays - iso else selectedDays + iso
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                AlertType.MONTHLY -> {
                    OutlinedTextField(
                        value         = dayOfMonth,
                        onValueChange = { autoLockManager?.recordInteraction(); if (it.length <= 2) dayOfMonth = it.filter(Char::isDigit) },
                        placeholder   = { Text(stringResource(R.string.reminder_alert_field_day_of_month), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(8.dp),
                        colors        = fieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                AlertType.YEARLY -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value         = monthNum,
                            onValueChange = { autoLockManager?.recordInteraction(); if (it.length <= 2) monthNum = it.filter(Char::isDigit) },
                            placeholder   = { Text(stringResource(R.string.reminder_alert_field_month), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            shape         = RoundedCornerShape(8.dp),
                            colors        = fieldColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value         = dayOfMonth,
                            onValueChange = { autoLockManager?.recordInteraction(); if (it.length <= 2) dayOfMonth = it.filter(Char::isDigit) },
                            placeholder   = { Text(stringResource(R.string.reminder_alert_field_day_of_month), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            shape         = RoundedCornerShape(8.dp),
                            colors        = fieldColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                AlertType.INTERVAL -> {
                    OutlinedTextField(
                        value         = intervalDays,
                        onValueChange = { autoLockManager?.recordInteraction(); if (it.length <= 4) intervalDays = it.filter(Char::isDigit) },
                        placeholder   = { Text(stringResource(R.string.reminder_alert_field_interval_days), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(8.dp),
                        colors        = fieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value         = intervalStartMs?.let { formatDateLocalized(it, Locale.getDefault(), utc = true) } ?: "",
                            onValueChange = {},
                            placeholder   = { Text(stringResource(R.string.reminder_alert_field_start_date), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            modifier      = Modifier.fillMaxWidth(),
                            readOnly      = true,
                            shape         = RoundedCornerShape(8.dp),
                            colors        = fieldColors(),
                            trailingIcon  = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                    }
                }

                AlertType.BEFORE_DEADLINE -> {
                    OutlinedTextField(
                        value         = daysBeforeCount,
                        onValueChange = { autoLockManager?.recordInteraction(); if (it.length <= 3) daysBeforeCount = it.filter(Char::isDigit) },
                        placeholder   = { Text(stringResource(R.string.reminder_alert_field_days_before), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(8.dp),
                        colors        = fieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            val hour   = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 0
            val minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0

            val isValid = when (selectedType) {
                AlertType.PUNCTUAL        -> punctualDateMs != null
                AlertType.WEEKLY          -> selectedDays.isNotEmpty()
                AlertType.MONTHLY         -> dayOfMonth.toIntOrNull() in 1..31
                AlertType.YEARLY          -> monthNum.toIntOrNull() in 1..12 &&
                                             dayOfMonth.toIntOrNull() in 1..31
                AlertType.INTERVAL        -> (intervalDays.toIntOrNull() ?: 0) > 0 && intervalStartMs != null
                AlertType.BEFORE_DEADLINE -> (daysBeforeCount.toIntOrNull() ?: 0) > 0
            }

            Button(
                onClick = {
                    if (!isValid) return@Button
                    val schedule: ReminderSchedule = when (selectedType) {

                        AlertType.PUNCTUAL -> {
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = punctualDateMs ?: return@Button
                            }
                            val triggerMs = Calendar.getInstance().apply {
                                set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH),
                                    utcCal.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            ReminderSchedule.Punctual(id = initialSchedule?.id ?: UUID.randomUUID().toString(), triggerAtMs = triggerMs)
                        }
                        AlertType.WEEKLY -> ReminderSchedule.Weekly(
                            id         = initialSchedule?.id ?: UUID.randomUUID().toString(),
                            daysOfWeek = selectedDays,
                            hour       = hour,
                            minute     = minute
                        )
                        AlertType.MONTHLY -> ReminderSchedule.Monthly(
                            id         = initialSchedule?.id ?: UUID.randomUUID().toString(),
                            dayOfMonth = dayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                            hour       = hour,
                            minute     = minute
                        )
                        AlertType.YEARLY -> ReminderSchedule.Yearly(
                            id         = initialSchedule?.id ?: UUID.randomUUID().toString(),
                            month      = monthNum.toIntOrNull()?.coerceIn(1, 12) ?: 1,
                            dayOfMonth = dayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                            hour       = hour,
                            minute     = minute
                        )
                        AlertType.INTERVAL -> {
                            val utcCalInterval = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = intervalStartMs ?: return@Button
                            }
                            val startMsLocal = Calendar.getInstance().apply {
                                set(utcCalInterval.get(Calendar.YEAR), utcCalInterval.get(Calendar.MONTH),
                                    utcCalInterval.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            ReminderSchedule.Interval(
                                id           = initialSchedule?.id ?: UUID.randomUUID().toString(),
                                startMs      = startMsLocal,
                                intervalDays = intervalDays.toIntOrNull() ?: 7,
                                hour         = hour,
                                minute       = minute
                            )
                        }
                        AlertType.BEFORE_DEADLINE -> ReminderSchedule.BeforeDeadline(
                            id               = initialSchedule?.id ?: UUID.randomUUID().toString(),
                            daysBeforeCount  = daysBeforeCount.toIntOrNull() ?: 1,
                            hour             = hour,
                            minute           = minute
                        )
                    }
                    onAdd(schedule)
                },
                enabled  = isValid,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(8.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = Color.Transparent,
                    contentColor           = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor   = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                border = BorderStroke(0.5.dp, if (isValid) MaterialTheme.colorScheme.primary
                                              else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
            ) {
                Text(
                    stringResource(R.string.action_apply),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ReminderSchedule.typeLabel(): String = when (this) {
    is ReminderSchedule.Punctual       -> stringResource(R.string.reminder_type_punctual)
    is ReminderSchedule.Weekly         -> stringResource(R.string.reminder_type_weekly)
    is ReminderSchedule.Monthly        -> stringResource(R.string.reminder_type_monthly)
    is ReminderSchedule.Yearly         -> stringResource(R.string.reminder_type_yearly)
    is ReminderSchedule.Interval       -> stringResource(R.string.reminder_type_interval)
    is ReminderSchedule.BeforeDeadline -> stringResource(R.string.reminder_type_before_deadline)
}

@Composable
private fun ReminderSchedule.description(): String = when (this) {
    is ReminderSchedule.Punctual       -> datetimeLocalFmt().format(Date(triggerAtMs))
    is ReminderSchedule.Weekly         -> {
        val currentLocale = LocalConfiguration.current.locales[0]
        val symbols = java.text.DateFormatSymbols(currentLocale).shortWeekdays
        val isoToCalendar = mapOf(
            1 to Calendar.MONDAY, 2 to Calendar.TUESDAY, 3 to Calendar.WEDNESDAY,
            4 to Calendar.THURSDAY, 5 to Calendar.FRIDAY, 6 to Calendar.SATURDAY, 7 to Calendar.SUNDAY
        )
        val days = daysOfWeek.sorted().mapNotNull { iso ->
            isoToCalendar[iso]?.let { cal ->
                symbols[cal].take(2).replaceFirstChar { c -> c.titlecase(currentLocale) }
            }
        }.joinToString(", ")
        "%s · %02d:%02d".format(days, hour, minute)
    }
    is ReminderSchedule.Monthly        -> stringResource(R.string.reminder_schedule_monthly_desc, dayOfMonth, hour, minute)
    is ReminderSchedule.Yearly         -> stringResource(R.string.reminder_schedule_yearly_desc, dayOfMonth, month, hour, minute)
    is ReminderSchedule.Interval       -> stringResource(R.string.reminder_schedule_interval_desc, intervalDays, hour, minute)
    is ReminderSchedule.BeforeDeadline -> stringResource(R.string.reminder_schedule_before_deadline_desc, daysBeforeCount, hour, minute)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    disabledBorderColor     = Color.Transparent,
    cursorColor             = MaterialTheme.colorScheme.primary
)


private fun datetimeLocalFmt() = SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault())
