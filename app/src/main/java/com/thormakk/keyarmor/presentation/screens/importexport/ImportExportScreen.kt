@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
package com.thormakk.keyarmor.presentation.screens.importexport

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.screens.auth.PasswordStrengthBar
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import com.thormakk.keyarmor.presentation.components.CsvExportAuthGate
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.screens.vaultmanagement.VaultNameDialog
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.components.topBarColors
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.crypto.LecturaDeLaClave

private enum class ExportOption { PARTIAL, JSON, CSV }

@Composable
fun ImportExportScreen(
    onBack:               () -> Unit,
    onPartialExportClick: () -> Unit = {},
    viewModel:            ImportExportViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val autoLockManager = LocalAutoLockManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var selectedExportOption by rememberSaveable { mutableStateOf(ExportOption.JSON) }

    val jsonSaveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let { viewModel.onExportUriSelected(it, context) } }

    val csvSaveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { viewModel.onExportUriSelected(it, context) } }

    val importFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.onImportUriSelected(it, context) } }

    val locale = LocalConfiguration.current.locales[0]
    val today = java.text.SimpleDateFormat("yyyyMMdd", locale)
        .format(java.util.Date())

    val exportSuccessMsg   = stringResource(R.string.ie_export_success)
    val importSuccessZeroFmt   = stringResource(R.string.ie_import_success_zero)
    val importSuccessOneFmt    = stringResource(R.string.ie_import_success_one)
    val importSuccessFmt       = stringResource(R.string.ie_import_success)
    val importDuplicatesOneFmt = stringResource(R.string.ie_import_duplicates_skipped_one)
    val importDuplicatesFmt    = stringResource(R.string.ie_import_duplicates_skipped)
    val importSkippedOneFmt    = stringResource(R.string.ie_import_skipped_warning_one)
    val importSkippedFmt       = stringResource(R.string.ie_import_skipped_warning)
    val importProtectedOneFmt  = stringResource(R.string.ie_import_protected_warning_one)
    val importProtectedFmt     = stringResource(R.string.ie_import_protected_warning)
    val importAttachmentsOneFmt = stringResource(R.string.ie_import_attachments_restored_one)
    val importAttachmentsFmt    = stringResource(R.string.ie_import_attachments_restored)

    LaunchedEffect(uiState.event) {
        when (val ev = uiState.event) {
            is ImportExportEvent.LaunchJsonSave -> {
                val ext = if (uiState.exportConClave || (uiState.hasAttachments && uiState.includeAttachmentsInExport)) "kab" else "json"
                jsonSaveLauncher.launch("keyarmor_export_$today.$ext")
                viewModel.onEventConsumed()
            }
            is ImportExportEvent.LaunchCsvSave  -> {
                csvSaveLauncher.launch("keyarmor_passwords_$today.csv")
                viewModel.onEventConsumed()
            }
            is ImportExportEvent.LaunchFilePick -> {
                val mimeTypes = if (uiState.importSource == ImportSource.KEYARMOR) {
                    arrayOf(
                        "application/json",
                        "application/octet-stream",
                        "application/zip",
                        "application/x-zip-compressed",
                        "text/csv",
                        "text/comma-separated-values",
                        "text/plain"
                    )
                } else {
                    arrayOf(
                        "text/csv",
                        "text/comma-separated-values",
                        "text/plain",
                        "application/vnd.ms-excel"
                    )
                }
                importFileLauncher.launch(mimeTypes)
                viewModel.onEventConsumed()
            }
            is ImportExportEvent.ExportSuccess  -> {
                Toast.makeText(context, exportSuccessMsg, Toast.LENGTH_SHORT).show()
                viewModel.onEventConsumed()
            }
            is ImportExportEvent.ImportSuccess  -> {
                val importedMsg = when (ev.count) {
                    0    -> importSuccessZeroFmt.format(ev.count)
                    1    -> importSuccessOneFmt.format(ev.count)
                    else -> importSuccessFmt.format(ev.count)
                }
                val parts = buildList {
                    add(importedMsg)
                    if (ev.duplicates == 1) add(importDuplicatesOneFmt.format(ev.duplicates))
                    else if (ev.duplicates > 1) add(importDuplicatesFmt.format(ev.duplicates))
                    if (ev.skipped == 1) add(importSkippedOneFmt.format(ev.skipped))
                    else if (ev.skipped > 1) add(importSkippedFmt.format(ev.skipped))
                    if (ev.protectedSkipped == 1) add(importProtectedOneFmt.format(ev.protectedSkipped))
                    else if (ev.protectedSkipped > 1) add(importProtectedFmt.format(ev.protectedSkipped))
                    if (ev.attachmentsRestored == 1) add(importAttachmentsOneFmt.format(ev.attachmentsRestored))
                    else if (ev.attachmentsRestored > 1) add(importAttachmentsFmt.format(ev.attachmentsRestored))
                }
                snackbarHostState.showSnackbar(
                    message  = parts.joinToString(" · "),
                    duration = SnackbarDuration.Long
                )
                viewModel.onEventConsumed()
            }
            is ImportExportEvent.Error          -> {
                Toast.makeText(context, ev.message, Toast.LENGTH_LONG).show()
                viewModel.onEventConsumed()
            }
            is ImportExportEvent.ImportPreview  -> {   }
            null -> {}
        }
    }

    if (uiState.event is ImportExportEvent.ImportPreview) {
        val result = (uiState.event as ImportExportEvent.ImportPreview).result
        AlertDialog(
            onDismissRequest = { viewModel.onImportDismissPreview() },
            title = { Text(stringResource(R.string.ie_import_preview_title)) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.ie_import_preview_subtitle),
                        style = MaterialTheme.typography.bodySmall.justified(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    if (result.passwords.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_passwords),    result.passwords.size)
                    if (result.twoFactor.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_twofactor),    result.twoFactor.size)
                    if (result.creditCards.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_creditcards),  result.creditCards.size)
                    if (result.bankAccounts.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_bankaccounts), result.bankAccounts.size)
                    if (result.cryptoWallets.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_crypto),       result.cryptoWallets.size)
                    if (result.secureNotes.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_notes),        result.secureNotes.size)
                    if (result.customEntries.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_custom),       result.customEntries.size)
                    if (result.passkeys.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_passkeys),     result.passkeys.size)
                    if (result.identityDocuments.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_identity),     result.identityDocuments.size)
                    if (result.vehicles.isNotEmpty())
                        ImportPreviewRow(stringResource(R.string.ie_type_vehicles),     result.vehicles.size)
                    val attCount = if (result.attachments.isNotEmpty()) result.attachments.size else result.attachmentCount
                    if (attCount > 0)
                        ImportPreviewRow(stringResource(R.string.ie_type_attachments),  attCount)
                    if (result.totalCount == 0)
                        Text(stringResource(R.string.ie_import_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (result.skippedRows > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text  = importSkippedFmt.format(result.skippedRows),
                            style = MaterialTheme.typography.bodySmall,
                            color = warningColor()
                        )
                    }
                    if (uiState.pendingImportSource == ImportSource.CSV_GENERIC && result.totalCount > 0) {
                        Spacer(Modifier.height(12.dp))
                        ImportVaultSelector(
                            vaults           = uiState.availableVaults,
                            selectedVaultId  = uiState.selectedVaultId,
                            canCreateVault   = uiState.canCreateVault,
                            onSelect         = viewModel::onSelectImportVault,
                            onCreateNewClick = viewModel::onShowCreateVaultDialog
                        )
                    }
                }
            },
            confirmButton = {
                if (result.totalCount > 0) {
                    Button(
                        onClick = { viewModel.onImportConfirm() },
                        shape   = RoundedCornerShape(8.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = MaterialTheme.colorScheme.primary
                        ),
                        border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.ie_import_confirm), fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onImportDismissPreview() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (uiState.showCreateVaultDialog) {
        VaultNameDialog(
            title        = stringResource(R.string.vault_mgmt_create_title),
            initialName  = "",
            confirmLabel = stringResource(R.string.vault_mgmt_create_confirm),
            onConfirm    = { name -> viewModel.onCreateVaultForImport(name) },
            onDismiss    = { viewModel.onDismissCreateVaultDialog() }
        )
    }

    if (uiState.showCsvAuthGate) {
        CsvExportAuthGate(
            onAuthorized = viewModel::onCsvExportAuthorized,
            onDismiss    = viewModel::onCsvAuthGateDismissed,
            verifyMasterPassword = viewModel::verifyMasterPassword
        )
    }

    if (uiState.showCsvConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onCsvExportDismissed() },
            icon  = {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    tint               = warningColor()
                )
            },
            title = { Text(stringResource(R.string.ie_csv_confirm_title)) },
            text  = {
                Text(
                    if (uiState.csvHasHiddenEntries)
                        stringResource(R.string.ie_csv_confirm_body) + "\n\n" + stringResource(R.string.ie_csv_confirm_hidden_note)
                    else
                        stringResource(R.string.ie_csv_confirm_body), style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onCsvExportConfirmed() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.ie_csv_confirm_proceed))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCsvExportDismissed() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                TopAppBar(
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                    },
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            stringResource(R.string.ie_screen_title),
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
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier          = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment  = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val scrollState = key(selectedTab) { rememberScrollState() }

        val imeVisible = WindowInsets.isImeVisible

        LaunchedEffect(imeVisible, selectedTab) {
            if (imeVisible && selectedTab == 1 && uiState.importSource == ImportSource.KEYARMOR) {
                kotlinx.coroutines.delay(300)
                scope.launch { scrollState.animateScrollTo(scrollState.maxValue) }
            }
        }

        val showTooShortHint = uiState.exportPassword.isNotEmpty() && uiState.exportPassword.length < 8
        val showMismatchHint = uiState.exportPasswordMismatch
        val bringExportButtonIntoView = remember { BringIntoViewRequester() }
        LaunchedEffect(imeVisible, selectedTab, selectedExportOption, showTooShortHint, showMismatchHint) {
            if (imeVisible && selectedTab == 0 && selectedExportOption == ExportOption.JSON) {
                kotlinx.coroutines.delay(300)
                bringExportButtonIntoView.bringIntoView()
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            AnimatedVisibility(
                visible = !(imeVisible && selectedTab == 0 && selectedExportOption == ExportOption.JSON),
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick  = { selectedTab = 0 },
                        shape    = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.coachMarkAnchor("import_export", "export")
                    ) {
                        Text(stringResource(R.string.ie_section_export))
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick  = { selectedTab = 1 },
                        shape    = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.coachMarkAnchor("import_export", "import")
                    ) {
                        Text(stringResource(R.string.ie_section_import))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 7.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

            if (selectedTab == 0) {

                IeSectionHeader(stringResource(R.string.ie_export_type_label))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectableOptionCard(
                        label    = stringResource(R.string.pe_entry_point_title),
                        subtitle = stringResource(R.string.pe_entry_point_desc),
                        selected = selectedExportOption == ExportOption.PARTIAL,
                        onClick  = { selectedExportOption = ExportOption.PARTIAL }
                    )
                    SelectableOptionCard(
                        label    = stringResource(R.string.ie_format_json),
                        subtitle = stringResource(R.string.ie_format_json_desc),
                        selected = selectedExportOption == ExportOption.JSON,
                        onClick  = {
                            selectedExportOption = ExportOption.JSON
                            viewModel.onExportFormatChange(ExportFormat.JSON)
                        }
                    )
                    SelectableOptionCard(
                        label    = stringResource(R.string.ie_format_csv),
                        subtitle = stringResource(R.string.ie_format_csv_desc),
                        selected = selectedExportOption == ExportOption.CSV,
                        onClick  = {
                            selectedExportOption = ExportOption.CSV
                            viewModel.onExportFormatChange(ExportFormat.CSV)
                        }
                    )
                }

                when (selectedExportOption) {

                    ExportOption.PARTIAL -> {
                        Button(
                            onClick  = onPartialExportClick,
                            modifier = Modifier.fillMaxWidth(), colors = appButtonColors()
                        ) {
                            Text(stringResource(R.string.action_continue))
                        }
                    }

                    ExportOption.JSON -> {
                        if (uiState.puedeUsarClave) {
                            IeSectionHeader(stringResource(R.string.ie_cierre_label))
                            CierrePill(
                                conClave = uiState.exportConClave,
                                onChange = viewModel::onExportConClaveChange
                            )
                        }

                        androidx.compose.animation.AnimatedContent(
                            targetState = uiState.exportConClave,
                            modifier = Modifier.fillMaxWidth(),
                            transitionSpec = {
                (androidx.compose.animation.fadeIn() togetherWith androidx.compose.animation.fadeOut())
                    .using(androidx.compose.animation.SizeTransform(clip = false))
            },
                            label = "cierre_export"
                        ) { conClaveSel ->
                        if (conClaveSel) {
                            Text(
                                text  = stringResource(R.string.ie_export_key_note),
                                style = MaterialTheme.typography.bodySmall.justified(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        IeSectionHeader(stringResource(R.string.ie_export_password_label))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value         = uiState.exportPassword,
                                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onExportPasswordChange(it) },
                                label         = { Text(stringResource(R.string.ie_export_password_hint)) },
                                singleLine    = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                                visualTransformation = if (uiState.exportPasswordVisible)
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon  = {
                                    IconButton(onClick = viewModel::onExportPasswordVisibilityToggle) {
                                        Icon(
                                            imageVector = if (uiState.exportPasswordVisible)
                                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (uiState.exportPassword.isNotEmpty()) {
                                PasswordStrengthBar(
                                    strength = PasswordStrengthCalculator.calculate(uiState.exportPassword)
                                )
                            }
                        }

                        OutlinedTextField(
                            value         = uiState.exportConfirmPassword,
                            onValueChange = { autoLockManager?.recordInteraction(); viewModel.onExportConfirmPasswordChange(it) },
                            label         = { Text(stringResource(R.string.ie_export_confirm_hint)) },
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                            isError       = uiState.exportPasswordMismatch,
                            supportingText = if (uiState.exportPasswordMismatch) ({
                                Text(stringResource(R.string.ie_passwords_mismatch), color = warningColor())
                            }) else null,
                            visualTransformation = if (uiState.exportConfirmPwdVisible)
                                VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon  = {
                                IconButton(onClick = viewModel::onExportConfirmPwdVisibilityToggle) {
                                    Icon(
                                        imageVector = if (uiState.exportConfirmPwdVisible)
                                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                errorBorderColor         = warningColor(),
                                errorLabelColor          = warningColor(),
                                errorSupportingTextColor = warningColor(),
                                errorTrailingIconColor   = warningColor()
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.exportPassword.isNotEmpty() && uiState.exportPassword.length < 8) {
                            Text(
                                text  = stringResource(R.string.ie_password_too_short),
                                style = MaterialTheme.typography.bodySmall,
                                color = warningColor()
                            )
                        }

                        Text(
                            text  = stringResource(R.string.ie_export_password_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        }
                        }
                        }

                        if (uiState.hasAttachments) {
                            val attMb = (uiState.attachmentsBytes / (1024 * 1024)).coerceAtLeast(1)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text  = stringResource(R.string.ie_export_include_attachments),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text  = stringResource(
                                            R.string.ie_export_include_attachments_note,
                                            uiState.attachmentsCount, attMb
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked         = uiState.includeAttachmentsInExport,
                                    onCheckedChange = { viewModel.onIncludeAttachmentsToggle() }
                                )
                            }
                        }

                        ExportSubmitButton(
                            enabled  = uiState.canExport,
                            onClick  = viewModel::onExportClick,
                            modifier = Modifier.bringIntoViewRequester(bringExportButtonIntoView)
                        )
                    }

                    ExportOption.CSV -> {
                        val csvWarningColor = warningColor()
                        Row(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(csvWarningColor.copy(alpha = 0.15f))
                                .padding(12.dp),
                            verticalAlignment   = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Warning,
                                contentDescription = null,
                                tint               = csvWarningColor,
                                modifier           = Modifier.size(18.dp)
                            )
                            Text(
                                text  = stringResource(R.string.ie_csv_warning),
                                style = MaterialTheme.typography.bodySmall,
                                color = csvWarningColor
                            )
                        }

                        ExportSubmitButton(
                            enabled = uiState.canExport,
                            onClick = viewModel::onExportClick
                        )
                    }
                }
            }

            if (selectedTab == 1) {

                IeSectionHeader(stringResource(R.string.ie_import_source_label))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectableOptionCard(
                        label    = stringResource(R.string.ie_source_keyarmor),
                        subtitle = stringResource(R.string.ie_source_keyarmor_desc),
                        selected = uiState.importSource == ImportSource.KEYARMOR,
                        onClick  = { viewModel.onImportSourceChange(ImportSource.KEYARMOR) }
                    )
                    SelectableOptionCard(
                        label    = stringResource(R.string.ie_source_csv_generic),
                        subtitle = stringResource(R.string.ie_source_csv_generic_desc),
                        selected = uiState.importSource == ImportSource.CSV_GENERIC,
                        onClick  = { viewModel.onImportSourceChange(ImportSource.CSV_GENERIC) }
                    )
                }

                if (uiState.importSource == ImportSource.KEYARMOR) {
                    IeSectionHeader(stringResource(R.string.ie_cierre_label))
                    CierrePill(
                        conClave = uiState.importConClave,
                        onChange = viewModel::onImportConClaveChange
                    )
                    val modoImport = if (!uiState.importConClave) 0 else if (uiState.tieneLaClave) 1 else 2
                    androidx.compose.animation.AnimatedContent(
                        targetState = modoImport,
                        modifier = Modifier.fillMaxWidth(),
                        transitionSpec = {
                (androidx.compose.animation.fadeIn() togetherWith androidx.compose.animation.fadeOut())
                    .using(androidx.compose.animation.SizeTransform(clip = false))
            },
                        label = "cierre_import"
                    ) { modo ->
                    when (modo) {
                    0 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    OutlinedTextField(
                        value         = uiState.importPassword,
                        onValueChange = { autoLockManager?.recordInteraction(); viewModel.onImportPasswordChange(it) },
                        label         = { Text(stringResource(R.string.ie_import_password_hint)) },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        visualTransformation = if (uiState.importPasswordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon  = {
                            IconButton(onClick = viewModel::onImportPasswordVisibilityToggle) {
                                Icon(
                                    imageVector = if (uiState.importPasswordVisible)
                                        Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.importPassword.isNotEmpty() && uiState.importPassword.length < 8) {
                        Text(
                            text  = stringResource(R.string.ie_password_too_short),
                            style = MaterialTheme.typography.bodySmall,
                            color = warningColor()
                        )
                    }
                    Text(
                        text  = stringResource(R.string.ie_import_password_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    }
                    }
                    1 -> {
                        Text(
                            text  = stringResource(R.string.ie_cierre_clave_detail),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        val lectura = remember(uiState.importPalabras) { viewModel.leerPalabras(uiState.importPalabras) }
                        val completas = uiState.importPalabras.trim().split(Regex("\\s+")).size >= 12
                        val mensaje: String? = if (!completas || lectura is LecturaDeLaClave.Clave) null else when (lectura) {
                            is LecturaDeLaClave.PalabraDesconocida ->
                                stringResource(R.string.key_err_unknown_word, lectura.posicion, lectura.palabra)
                            is LecturaDeLaClave.CantidadEquivocada ->
                                stringResource(R.string.key_err_wrong_count, lectura.cuantas)
                            else -> stringResource(R.string.key_err_no_close)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value         = uiState.importPalabras,
                                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onImportPalabrasChange(it) },
                                label         = { Text(stringResource(R.string.key_restore_hint)) },
                                minLines      = 2,
                                isError       = mensaje != null,
                                colors        = OutlinedTextFieldDefaults.colors(
                                    errorBorderColor = warningColor(),
                                    errorLabelColor  = warningColor(),
                                    errorCursorColor = warningColor()
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType       = KeyboardType.Text,
                                    capitalization     = androidx.compose.ui.text.input.KeyboardCapitalization.None,
                                    autoCorrectEnabled = false
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            val pasteCtx = androidx.compose.ui.platform.LocalContext.current
                            AnimatedVisibility(visible = uiState.importPalabras.isEmpty()) {
                                TextButton(onClick = {
                                    val cm = pasteCtx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    cm.primaryClip?.getItemAt(0)?.coerceToText(pasteCtx)?.toString()
                                        ?.let { viewModel.onImportPalabrasChange(it) }
                                }) { Text(stringResource(R.string.key_paste)) }
                            }
                            if (mensaje != null) {
                                Text(mensaje, style = MaterialTheme.typography.bodySmall, color = warningColor())
                            }
                        }
                    }
                    }
                    }
                    Text(
                        text  = stringResource(R.string.ie_import_google_reconnect_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = stringResource(R.string.hidden_entries_import_note),
                        style = MaterialTheme.typography.bodySmall.justified(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (uiState.importSource == ImportSource.CSV_GENERIC) {
                    Text(
                        text  = stringResource(R.string.ie_import_csv_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick  = viewModel::onImportClick,
                    enabled  = uiState.importSource != ImportSource.KEYARMOR ||
                               if (uiState.importConClave) {
                                   uiState.tieneLaClave ||
                                       viewModel.leerPalabras(uiState.importPalabras) is LecturaDeLaClave.Clave
                               } else uiState.importPassword.length >= 8,
                    modifier = Modifier.fillMaxWidth(), colors = appButtonColors()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.ie_import_button))
                }
            }

            Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun CierrePill(conClave: Boolean, onChange: (Boolean) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        listOf(false to R.string.ie_cierre_password, true to R.string.ie_cierre_clave)
            .forEachIndexed { index, (esClave, rotulo) ->
                SegmentedButton(
                    selected = conClave == esClave,
                    onClick  = { onChange(esClave) },
                    shape    = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                    icon     = {},
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text      = stringResource(rotulo),
                        maxLines  = 2,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style     = MaterialTheme.typography.labelLarge.copy(lineHeight = 16.sp),
                        overflow  = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
    }
}

@Composable
private fun IeSectionHeader(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ExportSubmitButton(
    enabled:  Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.fillMaxWidth(), colors = appButtonColors()
    ) {
        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.ie_export_button))
    }
}

@Composable
private fun SelectableOptionCard(
    label:    String,
    subtitle: String = "",
    selected: Boolean,
    onClick:  () -> Unit
) {
    val borderColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else          MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (selected) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ImportVaultSelector(
    vaults:           List<VaultMetadata>,
    selectedVaultId:  String,
    canCreateVault:   Boolean,
    onSelect:         (String) -> Unit,
    onCreateNewClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = vaults.firstOrNull { it.id == selectedVaultId }?.name.orEmpty()

    Column {
        Text(
            text  = stringResource(R.string.ie_import_vault_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(selectedName, style = MaterialTheme.typography.bodyMedium)
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                vaults.forEach { vault ->
                    DropdownMenuItem(
                        text    = { Text(vault.name) },
                        onClick = { onSelect(vault.id); expanded = false }
                    )
                }
                if (canCreateVault) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.vault_mgmt_create_title),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector        = Icons.Default.Add,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = { expanded = false; onCreateNewClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportPreviewRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
