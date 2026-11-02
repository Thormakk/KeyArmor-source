package com.thormakk.keyarmor.presentation.screens.vaultmanagement

import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.VioletaDelFab
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultManagementScreen(
    onBack:      () -> Unit,
    onPlansClick: () -> Unit,
    viewModel:   VaultManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        VaultNameDialog(
            title         = stringResource(R.string.vault_mgmt_create_title),
            initialName   = "",
            confirmLabel  = stringResource(R.string.vault_mgmt_create_confirm),
            onConfirm     = { name -> viewModel.createVault(name); showCreateDialog = false },
            onDismiss     = { showCreateDialog = false }
        )
    }

    uiState.pendingRenameVault?.let { vault ->
        VaultNameDialog(
            title         = stringResource(R.string.vault_mgmt_rename_title),
            initialName   = vault.name,
            confirmLabel  = stringResource(R.string.vault_mgmt_rename_confirm),
            onConfirm     = { name -> viewModel.confirmRename(name) },
            onDismiss     = { viewModel.cancelRename() }
        )
    }

    if (uiState.protectedKeptCount > 0) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissProtectedKept() },
            title = { Text(pluralStringResource(R.plurals.protect_vault_kept_title, uiState.protectedKeptCount, uiState.protectedKeptCount)) },
            text  = { Text(pluralStringResource(R.plurals.protect_vault_kept_body, uiState.protectedKeptCount, uiState.protectedKeptCount)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissProtectedKept() }) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }

    uiState.pendingDeleteVault?.let { vault ->
        if (uiState.pendingDeleteVaultIsEmpty) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDelete() },
                title = { Text(stringResource(R.string.vault_mgmt_delete_title, vault.name)) },
                text  = { Text(stringResource(R.string.vault_mgmt_delete_body_empty), style = MaterialTheme.typography.bodyMedium.justified()) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDelete() },
                        shape   = RoundedCornerShape(8.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor   = MaterialTheme.colorScheme.onError
                        )
                    ) { Text(stringResource(R.string.vault_mgmt_delete_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDelete() }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDelete() },
                title = { Text(stringResource(R.string.vault_mgmt_delete_title, vault.name)) },
                text  = { Text(stringResource(R.string.vault_mgmt_delete_body)) },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { viewModel.confirmDelete() },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor   = MaterialTheme.colorScheme.primary
                            ),
                            border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                        ) { Text(stringResource(R.string.vault_mgmt_delete_move), fontWeight = FontWeight.SemiBold) }

                        Button(
                            onClick  = { viewModel.confirmDeleteTrash() },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor   = MaterialTheme.colorScheme.onError
                            )
                        ) { Text(stringResource(R.string.vault_mgmt_delete_trash)) }

                        TextButton(
                            onClick  = { viewModel.cancelDelete() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.action_cancel)) }
                    }
                },
                dismissButton = {}
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                actions = {
                    CaptureAllowedBadge()
                    ThemeTrialBadge()
                },
                title = { TextoDeUnaLinea(stringResource(R.string.vault_mgmt_screen_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                    color = sectionTitleColor()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = topBarColors()
            )
        },
        floatingActionButton = {
            if (uiState.canCreateVault) {
                FloatingActionButton(
                    onClick        = { showCreateDialog = true },
                    modifier       = Modifier.coachMarkAnchor("vault_management", "create"),
                    containerColor = VioletaDelFab,
                    contentColor   = ContenidoDelFab
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.vault_mgmt_create_title))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding      = PaddingValues(top = 8.dp, bottom = 88.dp)
        ) {
            if (uiState.vaults.isNotEmpty()) item(key = "bovedas") {
                VaultsSection(stringResource(R.string.vault_mgmt_screen_title)) {
                    uiState.vaults.forEachIndexed { index, vault ->
                        if (index > 0) VaultRowDivider()
                        key(vault.id) {
                            VaultManagementItem(
                                vault      = vault,
                                entryCount = uiState.entryCounts[vault.id],
                                isSystem   = vault.id == VaultMetadata.PRIMARY_VAULT_ID || vault.isPrivate,
                                isActive   = vault.id == uiState.activeVaultId,
                                onActivate = { viewModel.setActiveVault(vault.id) },
                                onRename   = { viewModel.startRename(vault) },
                                onDelete   = { viewModel.startDelete(vault) },
                            )
                        }
                    }
                }
            }

            if (!uiState.isPremium) {
                item { PremiumVaultUpsell(onClick = onPlansClick) }
            }

            if (uiState.isPremium && !uiState.canCreateVault) {
                item {
                    Text(
                        text  = stringResource(
                            R.string.vault_mgmt_limit_reached,
                            VaultManagementUiState.MAX_OWN_VAULTS_PREMIUM
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 23.dp, vertical = 8.dp)
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 6.dp)) {
        Text(
            text     = title,
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.background),
            content  = content
        )
    }
}

@Composable
private fun VaultRowDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 55.dp, end = 16.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun VaultManagementItem(
    vault:      VaultMetadata,
    entryCount: Int?,
    isSystem:   Boolean,
    isActive:   Boolean,
    onActivate: () -> Unit,
    onRename:   () -> Unit,
    onDelete:   () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onActivate)
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when {
            isActive          -> Icons.Default.Check
            vault.isInherited -> Icons.Default.Shield
            vault.isPrivate   -> Icons.Default.Lock
            else              -> Icons.Default.Folder
        }
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            val vaultDisplayName = when {
                vault.id == VaultMetadata.PRIMARY_VAULT_ID -> stringResource(R.string.vault_name_primary)
                vault.id == VaultMetadata.PRIVATE_VAULT_ID -> stringResource(R.string.vault_name_private)
                else                                       -> vault.name
            }
            Text(
                text       = vaultDisplayName,
                style      = MaterialTheme.typography.bodyMedium,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium
            )

            val cuenta = entryCount?.let {
                pluralStringResource(R.plurals.vault_entries_count, it, it)
            }
            val etiqueta = when {
                vault.isPrivate   -> stringResource(R.string.vault_private_inheritance_label)
                vault.isInherited -> stringResource(R.string.vault_mgmt_label_inherited)
                else              -> null
            }
            val subtitulo = listOfNotNull(cuenta, etiqueta).joinToString(" · ")
            if (subtitulo.isNotEmpty()) {
                Text(
                    text     = subtitulo,
                    style    = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!isSystem) {
            Box {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = { menuExpanded = true }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.action_more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded         = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (!vault.isInherited) {
                        DropdownMenuItem(
                            text         = { Text(stringResource(R.string.vault_mgmt_rename_title)) },
                            leadingIcon  = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick      = { menuExpanded = false; onRename() }
                        )
                    }
                    DropdownMenuItem(
                        text        = {
                            Text(
                                stringResource(R.string.vault_mgmt_delete_confirm),
                                color = dangerColor()
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                tint     = dangerColor()
                            )
                        },
                        onClick     = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
fun VaultNameDialog(
    title:        String,
    initialName:  String,
    confirmLabel: String,
    onConfirm:    (String) -> Unit,
    onDismiss:    () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }
    val autoLockManager = LocalAutoLockManager.current

    AlertDialog(
        modifier = Modifier.cuadroConTeclado(),
        properties = propiedadesConTeclado(),
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text  = {
            OutlinedTextField(
                value         = name,
                onValueChange = { autoLockManager?.recordInteraction(); name = it },
                label         = { Text(stringResource(R.string.vault_mgmt_name_hint)) },
                singleLine    = true,
                modifier      = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm(name) })
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = MaterialTheme.colorScheme.primary
                ),
                border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            ) { Text(confirmLabel, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
private fun PremiumVaultUpsell(onClick: () -> Unit) {
    VaultsSection(stringResource(R.string.vault_mgmt_premium_title)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text  = stringResource(R.string.vault_mgmt_premium_subtitle),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Button(onClick = onClick, shape = RoundedCornerShape(10.dp), colors = appButtonColors()) {
                Text(stringResource(R.string.vault_mgmt_premium_cta))
            }
        }
    }
}
