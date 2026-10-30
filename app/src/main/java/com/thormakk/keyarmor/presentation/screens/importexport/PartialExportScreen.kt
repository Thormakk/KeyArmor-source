@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.thormakk.keyarmor.presentation.screens.importexport

import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.scrollBy
import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.export.ExportEntryRow
import com.thormakk.keyarmor.data.export.ExportEntryType
import com.thormakk.keyarmor.data.export.PartialExportFormat
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.screens.auth.PasswordStrengthBar
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import com.thormakk.keyarmor.presentation.components.ChipData
import com.thormakk.keyarmor.presentation.components.ChipsRowWithFunnel
import com.thormakk.keyarmor.presentation.components.CsvExportAuthGate
import com.thormakk.keyarmor.presentation.components.TagSearchEmptyState
import com.thormakk.keyarmor.presentation.components.chipCategoryColor
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.truncateTag
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.screens.vault.CategoryChip
import com.thormakk.keyarmor.presentation.screens.vault.ChipCategoryColor
import com.thormakk.keyarmor.presentation.screens.vault.ChipTagColor
import com.thormakk.keyarmor.presentation.screens.vault.QuickFilter
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.highlightText
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

private enum class ExportDialogStep { NONE, FORMAT, CSV_WARNING, CSV_AUTH, PASSWORD }

@Composable
fun PartialExportScreen(
    onBack:    () -> Unit,
    viewModel: PartialExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val autoLockManager = LocalAutoLockManager.current

    val locale = LocalConfiguration.current.locales[0]
    val today  = java.text.SimpleDateFormat("yyyyMMdd", locale).format(java.util.Date())

    var step          by remember { mutableStateOf(ExportDialogStep.NONE) }
    var chosenFormat   by remember { mutableStateOf<PartialExportFormat?>(null) }
    var exportPassword by remember { mutableStateOf("") }
    var exportConfirm  by remember { mutableStateOf("") }
    var pwdVisible      by rememberSaveable { mutableStateOf(false) }
    var confirmVisible  by rememberSaveable { mutableStateOf(false) }
    var exportConClave  by remember { mutableStateOf(false) }
    var hideSensitive   by rememberSaveable { mutableStateOf(false) }

    var showFiltersSheet by remember { mutableStateOf(false) }

    var showSelectionMenu by remember { mutableStateOf(false) }

    fun resetDialogFlow() {
        step = ExportDialogStep.NONE
        chosenFormat = null
        exportPassword = ""
        exportConfirm = ""
        pwdVisible = false
        confirmVisible = false
        hideSensitive = false
        exportConClave = false
    }

    val csvSaveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { viewModel.exportSelected(PartialExportFormat.CSV, it, context) } }

    val jsonSaveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let { viewModel.exportSelected(PartialExportFormat.JSON, it, context, exportPassword, conClave = exportConClave) } }

    val pdfSaveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> uri?.let { viewModel.exportSelected(PartialExportFormat.PDF, it, context, exportPassword, !hideSensitive) } }

    val successMsg = stringResource(R.string.export_selection_success)
    LaunchedEffect(uiState.event) {
        when (val ev = uiState.event) {
            is PartialExportEvent.ExportSuccess -> {
                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                exportPassword = ""
                exportConfirm  = ""
                hideSensitive  = false
                viewModel.onEventConsumed()
            }
            is PartialExportEvent.Error -> {
                Toast.makeText(context, ev.message, Toast.LENGTH_LONG).show()
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    if (step == ExportDialogStep.FORMAT) {
        AlertDialog(
            onDismissRequest = { resetDialogFlow() },
            title = { Text(stringResource(R.string.export_selection_dialog_title)) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FormatOption(
                        title    = stringResource(R.string.export_selection_format_json),
                        subtitle = stringResource(R.string.export_selection_format_json_desc),
                        onClick  = { chosenFormat = PartialExportFormat.JSON; step = ExportDialogStep.PASSWORD }
                    )
                    FormatOption(
                        title    = stringResource(R.string.export_selection_format_csv),
                        subtitle = stringResource(R.string.export_selection_format_csv_desc),
                        onClick  = { chosenFormat = PartialExportFormat.CSV; step = ExportDialogStep.CSV_WARNING }
                    )
                    if (uiState.selectionHasNonPasswordTypes) {
                        Text(
                            text  = stringResource(R.string.pe_csv_only_passwords_warning),
                            style = MaterialTheme.typography.bodySmall.justified(),
                            color = warningColor(),
                            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                        )
                    }
                    FormatOption(
                        title    = stringResource(R.string.export_selection_format_pdf),
                        subtitle = stringResource(R.string.export_selection_format_pdf_desc),
                        onClick  = { chosenFormat = PartialExportFormat.PDF; step = ExportDialogStep.PASSWORD }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { resetDialogFlow() }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (step == ExportDialogStep.CSV_WARNING) {
        AlertDialog(
            onDismissRequest = { resetDialogFlow() },
            icon  = { Icon(Icons.Default.Warning, contentDescription = null, tint = warningColor()) },
            title = { Text(stringResource(R.string.export_selection_plain_warning_title)) },
            text  = { Text(stringResource(R.string.export_selection_plain_warning_body), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = { step = ExportDialogStep.CSV_AUTH },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.ie_csv_confirm_proceed)) }
            },
            dismissButton = {
                TextButton(onClick = { resetDialogFlow() }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (step == ExportDialogStep.CSV_AUTH) {
        CsvExportAuthGate(
            onAuthorized = {
                resetDialogFlow()
                csvSaveLauncher.launch("keyarmor_partial_export_$today.csv")
            },
            onDismiss = { resetDialogFlow() },
            verifyMasterPassword = viewModel::verifyMasterPassword
        )
    }

    LaunchedEffect(step, chosenFormat) {
        if (step == ExportDialogStep.PASSWORD && chosenFormat == PartialExportFormat.JSON) {
            viewModel.refreshSelectionAttachmentInfo()
        }
    }

    if (showFiltersSheet) {
        PartialExportFiltersSheet(
            availableTags   = uiState.availableTags,
            selectedTags    = uiState.filterTags,
            onlyFavorites   = uiState.filterFavorites,
            onlyAttachments = uiState.filterAttachments,
            categoryFilter  = uiState.filterCategory,
            onApply         = { tags, favs, atts, cat ->
                viewModel.applyFilters(favs, atts, cat, tags)
                showFiltersSheet = false
            },
            onClearAll = {
                viewModel.clearSearchAndFilters()
                showFiltersSheet = false
            },
            onDismiss = { showFiltersSheet = false }
        )
    }

    Box(Modifier.fillMaxSize()) {
    Scaffold(
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
                            stringResource(R.string.pe_screen_title),
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
                        Box {
                            IconButton(onClick = { showSelectionMenu = true }) {
                                Icon(
                                    imageVector        = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.action_options),
                                    tint               = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded         = showSelectionMenu,
                                onDismissRequest = { showSelectionMenu = false },
                                containerColor   = MaterialTheme.colorScheme.surface
                            ) {
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.pe_menu_select_all)) },
                                    onClick     = { showSelectionMenu = false; viewModel.selectAllVisible() },
                                    leadingIcon = { Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.pe_menu_deselect_all)) },
                                    onClick     = { showSelectionMenu = false; viewModel.deselectAllVisible() },
                                    leadingIcon = { Icon(Icons.Default.Deselect, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.pe_menu_invert_selection)) },
                                    onClick     = { showSelectionMenu = false; viewModel.invertSelectionVisible() },
                                    leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        val countText = when (uiState.selectedCount) {
                            0    -> stringResource(R.string.pe_selected_count_zero)
                            1    -> stringResource(R.string.pe_selected_count_one).format(uiState.selectedCount)
                            else -> stringResource(R.string.pe_selected_count).format(uiState.selectedCount)
                        }
                        Text(countText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        if (uiState.selectedCount > 0) {
                            Text(
                                text = stringResource(R.string.pe_clear_selection),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { viewModel.clearSelection() }
                            )
                        }
                    }
                    Button(
                        onClick = { step = ExportDialogStep.FORMAT },
                        enabled = uiState.selectedCount > 0, colors = appButtonColors()
                    ) {
                        Text(stringResource(R.string.ie_export_button))
                    }
                }
            }
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.vaults.all { it.types.isEmpty() }) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.pe_empty_state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            OutlinedTextField(
                value         = uiState.searchQuery,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onSearchQueryChange(it) },
                placeholder   = { Text(stringResource(R.string.pe_search_placeholder)) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                },
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp, vertical = 8.dp),
                shape      = CircleShape,
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedBorderColor    = Color.Transparent,
                    focusedBorderColor      = Color.Transparent
                )
            )

            if (uiState.hasHiddenEntries) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment      = Alignment.Top,
                    horizontalArrangement  = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Text(
                        text      = stringResource(R.string.pe_hidden_entries_note),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier  = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            val activeFilterCount =
                (if (uiState.filterCategory != null) 1 else 0) +
                uiState.filterTags.size +
                (if (uiState.filterFavorites) 1 else 0) +
                (if (uiState.filterAttachments) 1 else 0)

            val filterChips = uiState.quickFilters.map { qf ->
                when (qf) {
                    is QuickFilter.Category -> ChipData(
                        key           = qf.key,
                        label         = stringResource(qf.category.labelRes),
                        selected      = uiState.filterCategory == qf.category,
                        selectedColor = ChipCategoryColor,
                        onClick       = { viewModel.toggleQuickCategory(qf.category) },
                        count         = uiState.categoryCounts[qf.category] ?: 0
                    )
                    is QuickFilter.Tag -> ChipData(
                        key           = qf.key,
                        label         = qf.tag.truncateTag(),
                        selected      = uiState.filterTags.contains(qf.tag),
                        selectedColor = ChipTagColor,
                        onClick       = { viewModel.toggleQuickTag(qf.tag) },
                        count         = uiState.tagCounts[qf.tag] ?: 0
                    )
                }
            }
            ChipsRowWithFunnel(
                chips                    = filterChips,
                activeFilterCount        = activeFilterCount,
                onFunnelClick            = { viewModel.onFunnelOpened(); showFiltersSheet = true },
                funnelContentDescription = stringResource(R.string.filter_short),
                modifier                 = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
            ) { data ->
                CategoryChip(
                    label         = data.label,
                    selected      = data.selected,
                    selectedColor = data.selectedColor,
                    onClick       = data.onClick,
                    count         = data.count
                )
            }

            Spacer(Modifier.height(8.dp))

            if (uiState.isFiltering) {
                val vaultNames = remember(uiState.vaults) {
                    uiState.vaults.associate { it.vault.id to it.vault }
                }
                when {
                    uiState.allRowsLoading -> Box(
                        modifier         = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }

                    uiState.filteredResults.isEmpty() -> Box(
                        modifier         = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector        = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.pe_search_no_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> LazyColumn(
                        modifier        = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding  = PaddingValues(horizontal = 7.dp, vertical = 8.dp)
                    ) {
                        items(
                            uiState.filteredResults,
                            key = { "result_${it.ref.vaultId}_${it.ref.type}_${it.ref.id}" }
                        ) { row ->
                            val vault = vaultNames[row.ref.vaultId]
                            SearchResultRow(
                                row       = row,
                                vaultName = vault?.let { vaultDisplayName(it) } ?: row.ref.vaultId,
                                checked   = row.ref in uiState.selected,
                                onToggle  = { viewModel.toggleSelected(row.ref) }
                            )
                        }
                    }
                }
            } else {
                val arbolState = androidx.compose.foundation.lazy.rememberLazyListState()
                val arbolScope = androidx.compose.runtime.rememberCoroutineScope()
                fun seguirClave(clave: String) {
                    arbolScope.launch {
                        com.thormakk.keyarmor.presentation.util.seguirApertura(
                            posicion  = { arbolState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == clave }?.offset?.toFloat() },
                            objetivo  = { arbolState.layoutInfo.viewportSize.height * 0.2f },
                            desplazar = { arbolState.scrollBy(it) }
                        )
                    }
                }
                LazyColumn(
                    state           = arbolState,
                    modifier        = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding  = PaddingValues(horizontal = 7.dp, vertical = 8.dp)
                ) {
                    uiState.vaults.forEach { vaultSummary ->
                        if (vaultSummary.types.isEmpty()) return@forEach
                        val vaultId = vaultSummary.vault.id
                        val vaultExpanded = vaultId in uiState.expandedVaults
                        val vaultSelectedCount = uiState.selected.count { it.vaultId == vaultId }

                        item(key = "vault_$vaultId") {
                            VaultHeaderRow(
                                vault                  = vaultSummary.vault,
                                totalCount             = vaultSummary.types.sumOf { it.count },
                                selectedCount          = vaultSelectedCount,
                                expanded               = vaultExpanded,
                                onClick                = {
                                    viewModel.toggleVaultExpanded(vaultId)
                                    if (!vaultExpanded) seguirClave("vault_$vaultId")
                                },
                                onToggleFullSelection   = { viewModel.toggleVaultFullSelection(vaultSummary) }
                            )
                        }

                        if (vaultExpanded) {
                            vaultSummary.types.forEach { typeSummary ->
                                val typeKey = vaultId to typeSummary.type
                                val typeExpanded = typeKey in uiState.expandedTypes

                                item(key = "type_${vaultId}_${typeSummary.type}") {
                                    Box(Modifier.animateItem()) { TypeHeaderRow(
                                        label    = typeLabel(typeSummary.type),
                                        count    = typeSummary.count,
                                        selectedCount = uiState.selected.count { it.vaultId == vaultId && it.type == typeSummary.type },
                                        onToggleFullSelection = { viewModel.toggleTypeFullSelection(vaultId, typeSummary.type, typeSummary.count) },
                                        expanded = typeExpanded,
                                        onClick  = {
                                            viewModel.toggleTypeExpanded(vaultId, typeSummary.type)
                                            if (!typeExpanded) seguirClave("type_${vaultId}_${typeSummary.type}")
                                        }
                                    ) }
                                }

                                if (typeExpanded) {
                                    val loaded = typeSummary.type in uiState.loadedTypes
                                    if (!loaded) {
                                        item(key = "loading_${vaultId}_${typeSummary.type}") {
                                            Box(
                                                modifier = Modifier.animateItem().fillMaxWidth().padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
                                        }
                                    } else {
                                        val rows = viewModel.rowsFor(vaultId, typeSummary.type)
                                        items(rows, key = { "entry_${it.ref.vaultId}_${it.ref.type}_${it.ref.id}" }) { row ->
                                            Box(Modifier.animateItem()) { EntryCheckRow(
                                                row     = row,
                                                checked = row.ref in uiState.selected,
                                                onToggle = { viewModel.toggleSelected(row.ref) }
                                            ) }
                                        }
                                    }
                                }
                            }
                        }
                        item(key = "divider_$vaultId") { HorizontalDivider() }
                    }
                }
            }
        }
    }

    if (step == ExportDialogStep.PASSWORD && chosenFormat != null) {
        val format = chosenFormat!!
        val mismatch  = exportConfirm.isNotEmpty() && exportPassword != exportConfirm
        val tooShort  = exportPassword.isNotEmpty() && exportPassword.length < 8
        val conClaveAhora = exportConClave && format == PartialExportFormat.JSON
        val canConfirm = conClaveAhora || (exportPassword.length >= 8 && exportConfirm == exportPassword)

        var pwEnFoco      by remember { mutableStateOf(false) }
        var confirmEnFoco by remember { mutableStateOf(false) }
        val camposEnFoco = pwEnFoco || confirmEnFoco

        BackHandler(onBack = { resetDialogFlow() })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) { detectTapGestures(onTap = { resetDialogFlow() }) }
                .systemBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .pointerInput(Unit) { detectTapGestures(onTap = {}) },
                shape           = RoundedCornerShape(28.dp),
                color           = MaterialTheme.colorScheme.background,
                tonalElevation  = 6.dp,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 24.dp)) {
                    Text(
                        text       = stringResource(R.string.export_selection_password_dialog_title),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(weight = 1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (format == PartialExportFormat.JSON && viewModel.puedeUsarClave()) {
                            CierrePill(conClave = exportConClave, onChange = { exportConClave = it })
                        }
                        androidx.compose.animation.AnimatedContent(
                            targetState = conClaveAhora,
                            modifier = Modifier.fillMaxWidth(),
                            transitionSpec = {
                            (androidx.compose.animation.fadeIn() togetherWith androidx.compose.animation.fadeOut())
                                .using(androidx.compose.animation.SizeTransform(clip = false))
                        },
                            label = "cierre_parcial"
                        ) { conClaveSel ->
                        if (conClaveSel) {
                            Text(
                                text  = stringResource(R.string.ie_export_key_note),
                                style = MaterialTheme.typography.bodySmall.justified(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!camposEnFoco) {
                            Text(
                                text  = stringResource(R.string.pe_password_dialog_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedTextField(
                            value         = exportPassword,
                            onValueChange = { autoLockManager?.recordInteraction(); exportPassword = it },
                            label         = { Text(stringResource(R.string.ie_export_password_hint)) },
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                            visualTransformation = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon  = {
                                IconButton(onClick = { pwdVisible = !pwdVisible }) {
                                    Icon(if (pwdVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { pwEnFoco = it.isFocused }
                        )
                        if (exportPassword.isNotEmpty()) {
                            PasswordStrengthBar(
                                strength = PasswordStrengthCalculator.calculate(exportPassword)
                            )
                        }
                        OutlinedTextField(
                            value         = exportConfirm,
                            onValueChange = { autoLockManager?.recordInteraction(); exportConfirm = it },
                            label         = { Text(stringResource(R.string.ie_export_confirm_hint)) },
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                            isError       = mismatch,
                            supportingText = if (mismatch) ({
                                Text(stringResource(R.string.ie_passwords_mismatch), color = warningColor())
                            }) else null,
                            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon  = {
                                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                    Icon(if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                errorBorderColor       = warningColor(),
                                errorLabelColor        = warningColor(),
                                errorTrailingIconColor = warningColor(),
                                errorCursorColor       = warningColor()
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { confirmEnFoco = it.isFocused }
                        )
                        if (tooShort) {
                            Text(stringResource(R.string.ie_password_too_short), style = MaterialTheme.typography.bodySmall, color = warningColor())
                        }
                        }
                        }
                        }
                        if (format == PartialExportFormat.PDF) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text     = stringResource(R.string.pe_hide_sensitive_toggle),
                                    style    = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Switch(checked = hideSensitive, onCheckedChange = { hideSensitive = it })
                            }
                        }
                        if (format == PartialExportFormat.JSON && uiState.selectionAttachmentCount > 0) {
                            val attMb = (uiState.selectionAttachmentBytes / (1024 * 1024)).coerceAtLeast(1)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text  = stringResource(R.string.ie_export_include_attachments),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text  = stringResource(
                                            R.string.ie_export_include_attachments_note,
                                            uiState.selectionAttachmentCount, attMb
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Switch(
                                    checked         = uiState.includeAttachments,
                                    onCheckedChange = { viewModel.onIncludeAttachmentsToggle() }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(onClick = { resetDialogFlow() }) { Text(stringResource(R.string.action_cancel)) }
                        TextButton(
                            enabled = canConfirm,
                            onClick = {
                                step = ExportDialogStep.NONE
                                when (format) {
                                    PartialExportFormat.JSON -> {
                                        val ext = if (conClaveAhora || (uiState.includeAttachments && uiState.selectionAttachmentCount > 0)) "kab" else "json"
                                        jsonSaveLauncher.launch("keyarmor_partial_export_$today.$ext")
                                    }
                                    PartialExportFormat.PDF  -> pdfSaveLauncher.launch("keyarmor_partial_export_$today.pdf")
                                    PartialExportFormat.CSV  -> Unit
                                }
                            }
                        ) { Text(stringResource(R.string.ie_export_button)) }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun vaultDisplayName(vault: VaultMetadata): String = when {
    vault.id == VaultMetadata.PRIMARY_VAULT_ID -> stringResource(R.string.vault_name_primary)
    vault.id == VaultMetadata.PRIVATE_VAULT_ID -> stringResource(R.string.vault_name_private)
    else                                       -> vault.name
}

@Composable
private fun typeLabel(type: ExportEntryType): String = stringResource(
    when (type) {
        ExportEntryType.PASSWORD          -> R.string.ie_type_passwords
        ExportEntryType.TWO_FACTOR        -> R.string.ie_type_twofactor
        ExportEntryType.CREDIT_CARD       -> R.string.ie_type_creditcards
        ExportEntryType.BANK_ACCOUNT      -> R.string.ie_type_bankaccounts
        ExportEntryType.CRYPTO_WALLET     -> R.string.ie_type_crypto
        ExportEntryType.SECURE_NOTE       -> R.string.ie_type_notes
        ExportEntryType.CUSTOM_ENTRY      -> R.string.ie_type_custom
        ExportEntryType.PASSKEY           -> R.string.ie_type_passkeys
        ExportEntryType.IDENTITY_DOCUMENT -> R.string.ie_type_identity
        ExportEntryType.VEHICLE           -> R.string.ie_type_vehicles
    }
)

@Composable
private fun VaultHeaderRow(
    vault:                 VaultMetadata,
    totalCount:            Int,
    selectedCount:         Int,
    expanded:              Boolean,
    onClick:               () -> Unit,
    onToggleFullSelection: () -> Unit
) {
    val icon = when {
        vault.isInherited -> Icons.Default.Shield
        vault.isPrivate   -> Icons.Default.Lock
        else              -> Icons.Default.Folder
    }
    val allSelected  = totalCount > 0 && selectedCount == totalCount
    val someSelected = selectedCount > 0 && selectedCount < totalCount
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (someSelected) {
            IconButton(onClick = onToggleFullSelection, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector        = Icons.Default.IndeterminateCheckBox,
                    contentDescription = stringResource(R.string.pe_select_whole_vault),
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(24.dp)
                )
            }
        } else {
            Checkbox(
                checked         = allSelected,
                onCheckedChange = { onToggleFullSelection() },
                modifier        = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.width(4.dp))
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(vaultDisplayName(vault), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            val subtitle = if (selectedCount > 0) "$selectedCount / $totalCount" else totalCount.toString()
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector        = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TypeHeaderRow(
    label:    String,
    count:    Int,
    selectedCount: Int,
    onToggleFullSelection: () -> Unit,
    expanded: Boolean,
    onClick:  () -> Unit
) {
    val allSelected  = count > 0 && selectedCount == count
    val someSelected = selectedCount > 0 && selectedCount < count
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (someSelected) {
            IconButton(onClick = onToggleFullSelection, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector        = Icons.Default.IndeterminateCheckBox,
                    contentDescription = label,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(24.dp)
                )
            }
        } else {
            Checkbox(
                checked         = allSelected,
                onCheckedChange = { onToggleFullSelection() },
                modifier        = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(if (selectedCount > 0) "$selectedCount / $count" else "$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector        = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EntryCheckRow(
    row:      ExportEntryRow,
    checked:  Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 56.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = row.title.ifBlank { "—" },
                style    = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            if (row.subtitle.isNotBlank()) {
                Text(
                    text     = row.subtitle,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    row:       ExportEntryRow,
    vaultName: String,
    checked:   Boolean,
    onToggle:  () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = row.title.ifBlank { "—" },
                style    = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Row {
                Text(
                    text       = vaultName,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.primary,
                    maxLines   = 1
                )
                if (row.subtitle.isNotBlank()) {
                    Text(
                        text     = " · ${row.subtitle}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatOption(
    title:    String,
    subtitle: String,
    onClick:  () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PartialExportFiltersSheet(
    availableTags:   List<Tag>,
    selectedTags:    Set<String>,
    onlyFavorites:   Boolean,
    onlyAttachments: Boolean,
    categoryFilter:  EntryCategory?,
    onApply:         (tags: Set<String>, favorites: Boolean, attachments: Boolean, category: EntryCategory?) -> Unit,
    onClearAll:      () -> Unit,
    onDismiss:       () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val autoLockManager = LocalAutoLockManager.current

    var checkedTags      by remember(selectedTags)    { mutableStateOf(selectedTags) }
    var localFavorites   by remember(onlyFavorites)   { mutableStateOf(onlyFavorites) }
    var localAttachments by remember(onlyAttachments) { mutableStateOf(onlyAttachments) }
    var localCategory    by remember(categoryFilter)  { mutableStateOf(categoryFilter) }
    var searchQuery       by remember { mutableStateOf("") }

    val filteredTags = availableTags.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.name in checkedTags
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
                if (checkedTags.isNotEmpty() || localFavorites || localAttachments || localCategory != null) {
                    TextButton(onClick = {
                        checkedTags      = emptySet()
                        localFavorites   = false
                        localAttachments = false
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

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            if (filteredTags.isEmpty()) {
                TagSearchEmptyState(query = searchQuery, canCreate = false, onCreateTag = {})
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
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTags.isNotEmpty()) {
                Button(
                    onClick  = { onApply(checkedTags, localFavorites, localAttachments, localCategory) },
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
        }
    }
}
