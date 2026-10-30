@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.timemachine

import androidx.compose.foundation.gestures.scrollBy
import com.thormakk.keyarmor.presentation.util.registrarPosicion
import com.thormakk.keyarmor.presentation.util.contenedorDe
import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ContenidoDelFab
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.components.InfoChip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.VioletaDelFab
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Composable
fun SnapshotPreviewScreen(
    onBack: () -> Unit,
    viewModel: SnapshotPreviewViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()
    val restoreSuccess by viewModel.restoreSuccess.collectAsStateWithLifecycle()
    val mergeResult    by viewModel.mergeResult.collectAsStateWithLifecycle()
    val isWorking      by viewModel.isWorking.collectAsStateWithLifecycle()
    val workingIsRestore by viewModel.workingIsRestore.collectAsStateWithLifecycle()

    androidx.activity.compose.BackHandler(enabled = isWorking) { }

    val snackbarHostState = remember { SnackbarHostState() }
    val autoLockManager = LocalAutoLockManager.current

    var showDeleteDialog  by remember { mutableStateOf(false) }
    var showRestoreSheet  by remember { mutableStateOf(false) }
    var showReplaceDialog by remember { mutableStateOf(false) }
    var showReplaceAuth   by remember { mutableStateOf(false) }

    val successMsg = stringResource(R.string.restore_result_success)
    val successMsgMerge = stringResource(R.string.restore_result_merge)

    LaunchedEffect(Unit) {
        snapshotFlow { restoreSuccess }
            .filter { it }
            .collect {
                viewModel.clearRestoreSuccess()
                snackbarHostState.showSnackbar(successMsg)
                onBack()
            }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { mergeResult }
            .filterNotNull()
            .collect { result ->
                val msg = String.format(Locale.getDefault(), successMsgMerge, result.restored)
                viewModel.clearMergeResult()
                snackbarHostState.showSnackbar(msg)
                onBack()
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            TopAppBar(
                title = {
                    val sdf = remember {
                        java.text.SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
                    }
                    val title = uiState.snapshot?.let {
                        stringResource(R.string.snapshot_preview_banner, sdf.format(java.util.Date(it.timestamp)))
                    } ?: stringResource(R.string.time_machine_preview)
                    TextoDeUnaLinea(title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                        color = sectionTitleColor())
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isWorking) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    CaptureAllowedBadge()
                    ThemeTrialBadge()
                    if (!uiState.isLoading && uiState.error == null && !isWorking) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector        = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.time_machine_delete_snapshot),
                                tint               = dangerColor()
                            )
                        }
                    }
                },
                colors = topBarColors()
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading && uiState.error == null && !isWorking) {
                ExtendedFloatingActionButton(
                    onClick        = { showRestoreSheet = true },
                    containerColor = VioletaDelFab,
                    contentColor   = ContenidoDelFab,
                    icon           = { Icon(Icons.Default.Restore, contentDescription = null) },
                    text           = { Text(stringResource(R.string.snapshot_preview_restore_here)) }
                )
            }
        }
    ) { innerPadding ->

        when {
            uiState.isLoading -> Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.error != null -> Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.error ?: "", style = MaterialTheme.typography.bodyMedium,
                    color = dangerColor())
            }

            else -> SnapshotContent(
                sections     = uiState.sections,
                noAttachments = uiState.snapshot?.includesAttachments == false,
                initialQuery = viewModel.initialQuery,
                modifier     = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 7.dp)
            )
        }

        if (isWorking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
                    .pointerInput(Unit) { },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text  = stringResource(
                            if (workingIsRestore) R.string.time_machine_restoring else R.string.time_machine_deleting
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showRestoreSheet) {
        ModalBottomSheet(onDismissRequest = { showRestoreSheet = false }) {
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
                OutlinedCard(onClick = { showRestoreSheet = false; showReplaceDialog = true },
                    modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(stringResource(R.string.restore_mode_replace),
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(2.dp))
                        Text(stringResource(R.string.restore_mode_replace_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedCard(onClick = { showRestoreSheet = false; viewModel.restoreMerge() },
                    modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(stringResource(R.string.restore_mode_merge),
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(2.dp))
                        Text(stringResource(R.string.restore_mode_merge_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedCard(onClick = { showRestoreSheet = false; viewModel.restoreAddAll() },
                    modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(stringResource(R.string.restore_mode_add_all),
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(2.dp))
                        Text(stringResource(R.string.restore_mode_add_all_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showReplaceDialog) {
        val confirmWord = stringResource(R.string.restore_mode_replace_confirm_word)
        var typedText   by remember { mutableStateOf("") }
        AlertDialog(
            modifier = Modifier.cuadroConTeclado(),
            properties = propiedadesConTeclado(),
            onDismissRequest = { showReplaceDialog = false },
            title            = { Text(stringResource(R.string.restore_mode_replace)) },
            text             = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.restore_mode_replace_desc),
                        style = MaterialTheme.typography.bodyMedium.justified())
                    if (uiState.snapshot?.includesAttachments == false) {
                        Text(stringResource(R.string.time_machine_replace_no_attachments_warning),
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
                    onClick = { showReplaceDialog = false; showReplaceAuth = true },
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
                TextButton(onClick = { showReplaceDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showReplaceAuth) {
        ReplaceAuthGate(
            onAuthorized         = { showReplaceAuth = false; viewModel.restoreReplace() },
            onDismiss            = { showReplaceAuth = false },
            verifyMasterPassword = viewModel::verifyMasterPassword
        )
    }

    if (showDeleteDialog) {
        val syncedToDrive = uiState.snapshot?.syncedToDrive == true
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text(stringResource(R.string.time_machine_delete_confirm)) },
            text    = if (syncedToDrive) ({
                Text(
                    text  = stringResource(R.string.time_machine_delete_confirm_drive),
                    style = MaterialTheme.typography.bodyMedium.justified(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }) else null,
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; viewModel.deleteSnapshot { onBack() } },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SnapshotContent(
    sections: List<SnapshotSection>,
    modifier: Modifier = Modifier,
    initialQuery: String = "",
    noAttachments: Boolean = false
) {
    var query by remember { mutableStateOf(initialQuery) }
    val autoLockManager = LocalAutoLockManager.current
    var expandedTypes by remember { mutableStateOf<Set<SnapshotSectionType>>(emptySet()) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val seguidor = com.thormakk.keyarmor.presentation.util.rememberSeguidorDeApertura { listState.scrollBy(it) }
    val posiciones = remember { mutableMapOf<SnapshotSectionType, com.thormakk.keyarmor.presentation.util.PosicionDeTitulo>() }

    val isSearching = query.isNotBlank()

    val visibleSections = remember(sections, query) {
        if (!isSearching) {
            sections
        } else {
            sections.mapNotNull { section ->
                val matching = section.entries.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                if (matching.isEmpty()) null else section.copy(entries = matching)
            }
        }
    }
    val totalResults = remember(visibleSections, isSearching) {
        if (isSearching) visibleSections.sumOf { it.totalCount } else 0
    }

    LazyColumn(
        state          = listState,
        modifier       = modifier.background(MaterialTheme.colorScheme.surface).contenedorDe(seguidor),
        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
    ) {
        if (noAttachments) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        label = stringResource(R.string.time_machine_no_attachments_label),
                        color = warningColor()
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = stringResource(R.string.time_machine_preview_no_attachments),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            TextField(
                value         = query,
                onValueChange = { autoLockManager?.recordInteraction(); query = it },
                placeholder   = { Text(stringResource(R.string.snapshot_preview_search_hint)) },
                leadingIcon   = {
                    Icon(Icons.Default.Search, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon  = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape      = RoundedCornerShape(50),
                colors     = TextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor  = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        if (isSearching) {
            item {
                Text(
                    text     = if (totalResults == 0)
                        stringResource(R.string.search_no_results)
                    else
                        stringResource(R.string.snapshot_preview_search_results, totalResults),
                    style    = MaterialTheme.typography.labelMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                )
            }
        }

        item(key = "sections_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                visibleSections.forEachIndexed { index, section ->
                    val isExpanded = isSearching || section.type in expandedTypes

                    SectionHeader(
                        section    = section,
                        isExpanded = isExpanded,
                        posicion   = posiciones.getOrPut(section.type) { com.thormakk.keyarmor.presentation.util.PosicionDeTitulo() },
                        onToggle   = {
                            if (!isSearching) {
                                val abre = section.type !in expandedTypes
                                expandedTypes = if (abre)
                                    expandedTypes + section.type
                                else
                                    expandedTypes - section.type
                                if (abre) seguidor.abrir(posiciones.getValue(section.type))
                                else seguidor.cerrar(posiciones.getValue(section.type))
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter   = expandVertically() + fadeIn(),
                        exit    = com.thormakk.keyarmor.presentation.util.SalidaDeApertura
                    ) {
                        Column(modifier = Modifier.padding(bottom = 4.dp)) {
                            section.entries.forEach { entry ->
                                EntryRow(entry = entry)
                            }
                        }
                    }

                    if (index < visibleSections.lastIndex) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color     = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    section:    SnapshotSection,
    isExpanded: Boolean,
    posicion:   com.thormakk.keyarmor.presentation.util.PosicionDeTitulo,
    onToggle:   () -> Unit
) {
    Surface(
        color   = Color.Transparent
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .registrarPosicion(posicion)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = section.type.icon(),
                contentDescription = null,
                modifier           = Modifier.size(20.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
            Text(
                text       = section.type.label(),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.weight(1f)
            )
            Text(
                text  = section.activeCount.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector        = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier           = Modifier.size(20.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EntryRow(entry: SnapshotEntry) {
    val alpha = if (entry.isDeleted || entry.isArchived) 0.45f else 1f
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text           = entry.name.ifBlank { "—" },
            style          = MaterialTheme.typography.bodySmall.copy(
                textDecoration = if (entry.isDeleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color          = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            maxLines       = 1,
            overflow       = TextOverflow.Ellipsis,
            modifier       = Modifier.weight(1f)
        )
        if (entry.isDeleted) {
            Text("🗑", style = MaterialTheme.typography.labelSmall)
        } else if (entry.isArchived) {
            Icon(Icons.Default.Archive, contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun SnapshotSectionType.icon(): ImageVector = when (this) {
    SnapshotSectionType.PASSWORD     -> Icons.Outlined.Lock
    SnapshotSectionType.TWO_FACTOR   -> Icons.Outlined.Smartphone
    SnapshotSectionType.PASSKEY      -> Icons.Outlined.Fingerprint
    SnapshotSectionType.CREDIT_CARD  -> Icons.Outlined.CreditCard
    SnapshotSectionType.BANK_ACCOUNT -> Icons.Outlined.AccountBalance
    SnapshotSectionType.CRYPTO_WALLET-> Icons.Outlined.CurrencyBitcoin
    SnapshotSectionType.SECURE_NOTE  -> Icons.AutoMirrored.Outlined.StickyNote2
    SnapshotSectionType.CUSTOM_ENTRY -> Icons.Outlined.GridView
    SnapshotSectionType.IDENTITY_DOC -> Icons.Outlined.Badge
    SnapshotSectionType.VEHICLE      -> Icons.Outlined.DirectionsCar
}

@Composable
private fun SnapshotSectionType.label(): String = when (this) {
    SnapshotSectionType.PASSWORD     -> stringResource(R.string.snapshot_preview_type_passwords)
    SnapshotSectionType.TWO_FACTOR   -> stringResource(R.string.snapshot_preview_type_twofactor)
    SnapshotSectionType.PASSKEY      -> stringResource(R.string.snapshot_preview_type_passkeys)
    SnapshotSectionType.CREDIT_CARD  -> stringResource(R.string.snapshot_preview_type_cards)
    SnapshotSectionType.BANK_ACCOUNT -> stringResource(R.string.snapshot_preview_type_bank)
    SnapshotSectionType.CRYPTO_WALLET-> stringResource(R.string.snapshot_preview_type_crypto)
    SnapshotSectionType.SECURE_NOTE  -> stringResource(R.string.snapshot_preview_type_notes)
    SnapshotSectionType.CUSTOM_ENTRY -> stringResource(R.string.snapshot_preview_type_custom)
    SnapshotSectionType.IDENTITY_DOC -> stringResource(R.string.snapshot_preview_type_identity)
    SnapshotSectionType.VEHICLE      -> stringResource(R.string.snapshot_preview_type_vehicles)
}