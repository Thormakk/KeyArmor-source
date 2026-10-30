@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi::class)
package com.thormakk.keyarmor.presentation.screens.notes

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.SecureNoteHistoryItem
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun SecureNoteHistoryScreen(
    onBack:    () -> Unit,
    viewModel: SecureNoteHistoryViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isSelectionMode = selectedIds.isNotEmpty()

    val snackbarHostState = remember { SnackbarHostState() }
    val restoredMsg = stringResource(R.string.note_history_restored)
    val deletedMsg  = stringResource(R.string.note_history_deleted)

    LaunchedEffect(uiState.restored) {
        if (uiState.restored) snackbarHostState.showSnackbar(restoredMsg)
    }
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) {
            snackbarHostState.showSnackbar(deletedMsg)
            viewModel.onDeletedConsumed()
        }
    }

    BackHandler(enabled = isSelectionMode) { viewModel.clearSelection() }

    var itemToRestore         by remember { mutableStateOf<SecureNoteHistoryItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    itemToRestore?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToRestore = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.note_history_restore_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.note_history_restore_confirm_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restore(item); itemToRestore = null },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.note_history_restore_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { itemToRestore = null }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.note_history_delete_selected_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.note_history_delete_selected_confirm_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = false; viewModel.deleteSelected() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
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
                        .then(Modifier)
                )
                if (isSelectionMode) {
                    TopAppBar(
                        windowInsets = WindowInsets(0),
                        title = {
                            TextoDeUnaLinea(
                                stringResource(R.string.note_history_selected_count, selectedIds.size),
                                style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                                fontWeight = FontWeight.SemiBold,
                                color      = sectionTitleColor()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(
                                    imageVector        = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.action_cancel),
                                    tint               = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            CaptureAllowedBadge()
                            ThemeTrialBadge()
                            IconButton(onClick = { viewModel.selectAll() }) {
                                Icon(
                                    imageVector        = Icons.Default.SelectAll,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                Icon(
                                    imageVector        = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.action_delete),
                                    tint               = dangerColor()
                                )
                            }
                        },
                        colors = topBarColors()
                    )
                } else {
                    TopAppBar(
                        windowInsets = WindowInsets(0),
                        title = {
                            TextoDeUnaLinea(
                                stringResource(R.string.note_history_title),
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        if (uiState.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.note_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            contentPadding      = PaddingValues(horizontal = 7.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(uiState.items) { index, item ->
                NoteHistoryItemCard(
                    item            = item,
                    isSelected      = item.id in selectedIds,
                    isSelectionMode = isSelectionMode,
                    onRestore       = { itemToRestore = item },
                    onLongClick     = { viewModel.toggleSelection(item.id) },
                    onClick         = {
                        if (isSelectionMode) viewModel.toggleSelection(item.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun NoteHistoryItemCard(
    item:            SecureNoteHistoryItem,
    isSelected:      Boolean = false,
    isSelectionMode: Boolean = false,
    onRestore:       () -> Unit,
    onLongClick:     () -> Unit = {},
    onClick:         () -> Unit = {}
) {
    val dateStr = remember(item.savedAt) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.savedAt))
    }
    val richState = rememberRichTextState()
    LaunchedEffect(item.content) { richState.setHtml(item.content) }

    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick     = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked         = isSelected,
                            onCheckedChange = { onClick() },
                            modifier        = Modifier.size(20.dp),
                            colors          = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text       = stringResource(R.string.note_history_version, dateStr),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
                if (!isSelectionMode) {
                    TextButton(onClick = onRestore) {
                        Icon(
                            imageVector        = Icons.Default.Restore,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.note_history_restore))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            RichText(
                state    = richState,
                style    = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}
