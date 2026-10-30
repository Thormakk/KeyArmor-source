@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_VALUE")
package com.thormakk.keyarmor.presentation.components.selection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.TextoQueSeAjusta
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun SelectionTopBar(
    selectedCount:    Int,
    totalCount:       Int,
    isReadOnly:       Boolean = false,
    isArchiveLocked:  Boolean = false,
    onClearSelection: () -> Unit,
    onSelectAll:      () -> Unit,
    onArchive:        () -> Unit,
    onArchiveLocked:  () -> Unit = {},
    onDelete:         () -> Unit,
    onTag:            () -> Unit,
    onCategorize:     () -> Unit,
    onMoveTo:         () -> Unit = {},
    onCopyTo:         () -> Unit = {},
    protectedCount:   Int = 0,
    onToggleProtect:  (() -> Unit)? = null,
    allProtected:     Boolean = false,
    soleEntryTitle:   String? = null,
) {
    val allSelected = selectedCount >= totalCount && totalCount > 0
    var showMenu          by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && protectedCount >= selectedCount && selectedCount > 0) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_locked_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.protect_bulk_all), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.action_understood)) }
            }
        )
    } else if (showDeleteConfirm) {
        val aMover = selectedCount - protectedCount
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text       = stringResource(R.string.dialog_delete_entry_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text  = (if (aMover == 1 && protectedCount > 0 && !soleEntryTitle.isNullOrBlank())
                                stringResource(R.string.protect_bulk_single_named, soleEntryTitle)
                            else if (aMover == 1)
                                stringResource(R.string.bulk_delete_confirm_single)
                            else
                                stringResource(R.string.bulk_delete_confirm_multiple, aMover)) +
                            (if (protectedCount > 0) "\n\n" + pluralStringResource(R.plurals.protect_bulk_confirm_extra, protectedCount, protectedCount) else ""),
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
                ) { Text(stringResource(R.string.action_move_to_trash)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    Column {
    TopAppBar(
        windowInsets = WindowInsets(0),
        title = {
            TextoQueSeAjusta(
                text  = stringResource(R.string.bulk_selected_count, selectedCount),
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_cancel)
                )
            }
        },
        actions = {
            if (!isReadOnly) {
                IconButton(onClick = { if (isArchiveLocked) onArchiveLocked() else onArchive() }) {
                    Icon(
                        imageVector        = if (isArchiveLocked) Icons.Default.Lock else Icons.Default.Archive,
                        contentDescription = stringResource(R.string.bulk_archive)
                    )
                }
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector        = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.bulk_delete),
                    tint               = dangerColor()
                )
            }
            if (onToggleProtect != null && (!isReadOnly || allProtected)) {
                IconButton(onClick = onToggleProtect) {
                    Icon(
                        imageVector        = if (allProtected) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = stringResource(if (allProtected) R.string.protect_cd_unlock else R.string.protect_cd_lock),
                        tint               = if (allProtected) com.thormakk.keyarmor.presentation.components.selection.protegidaVerde()
                                             else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector        = Icons.Default.MoreVert,
                    contentDescription = null
                )
            }
            DropdownMenu(
                expanded         = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor   = MaterialTheme.colorScheme.surface
            ) {
                if (!isReadOnly) {
                    DropdownMenuItem(
                        text         = { Text(stringResource(R.string.bulk_categorize), style = MaterialTheme.typography.bodyLarge) },
                        leadingIcon  = {
                            Icon(
                                imageVector        = Icons.Default.CheckBox,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            showMenu = false
                            onCategorize()
                        }
                    )
                    DropdownMenuItem(
                        text         = { Text(stringResource(R.string.bulk_tag_sheet_title), style = MaterialTheme.typography.bodyLarge) },
                        leadingIcon  = {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.Label,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = { showMenu = false; onTag() }
                    )
                    DropdownMenuItem(
                        text        = { Text(stringResource(R.string.action_move_to), style = MaterialTheme.typography.bodyLarge) },
                        leadingIcon = {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.DriveFileMove,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = { showMenu = false; onMoveTo() }
                    )
                }
                DropdownMenuItem(
                    text        = { Text(stringResource(R.string.action_copy_to), style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = {
                        Icon(
                            imageVector        = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = { showMenu = false; onCopyTo() }
                )
                DropdownMenuItem(
                    text        = {
                        Text(
                            stringResource(
                                if (allSelected) R.string.bulk_deselect_all
                                else             R.string.bulk_select_all
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector        = Icons.Default.SelectAll,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        showMenu = false
                        if (allSelected) onClearSelection() else onSelectAll()
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor             = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            titleContentColor          = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor     = MaterialTheme.colorScheme.onSurface,
        )
    )
    HorizontalDivider(
        thickness = 1.dp,
        color     = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    )
    }
}

@Composable
internal fun protegidaVerde(): androidx.compose.ui.graphics.Color =
    if (com.thormakk.keyarmor.presentation.screens.settings.contrastRatio(
            MaterialTheme.colorScheme.background, com.thormakk.keyarmor.ui.theme.SuccessGreen) < 3f)
        com.thormakk.keyarmor.ui.theme.SuccessGreenBright
    else com.thormakk.keyarmor.ui.theme.SuccessGreen
