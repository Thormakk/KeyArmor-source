@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.drivesync

import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import com.thormakk.keyarmor.presentation.screens.auth.PasswordStrengthBar
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.LocalCoachMarks
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.RestoreMode
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.util.GoogleConnectResult
import com.thormakk.keyarmor.presentation.util.rememberMandatoryBiometricGate
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.requestGoogleAccountConnect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.TextoQueSeAjusta

@Composable
fun DriveSyncScreen(
    onBack:              () -> Unit,
    onNavigatePlans:     () -> Unit = {},
    onTimeMachineClick:  () -> Unit = {},
    onOpenBackupKey:     () -> Unit = {},
    viewModel:           DriveSyncViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()
    val context        = LocalContext.current

    var cambiandoContrasena by rememberSaveable { mutableStateOf(false) }

    val securityGate = rememberMandatoryBiometricGate(
        verifyMasterPassword = viewModel::verifyMasterPassword
    )
    val snackbar       = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val coachMarks = LocalCoachMarks.current
    val notConnected = uiState.driveAccount == null
    val isFreePlan   = uiState.userPlan == UserPlan.FREE
    LaunchedEffect(coachMarks?.currentScreen, notConnected, isFreePlan) {
        val cm = coachMarks ?: return@LaunchedEffect
        val ids = when {
            notConnected -> listOf("connect")
            isFreePlan   -> emptyList()
            else         -> listOf("backup")
        }
        cm.setScreenSpecs("drive_sync", ids)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.reload()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val authLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.reload()
    }

    val downloadsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setDualLocalBackup(enabled = true, granted = granted)
    }
    val restorePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.showRestoreDialog()
    }

    var explicarPermiso by remember { mutableStateOf<MotivoDelPermiso?>(null) }

    val pedirRestore: () -> Unit = {
        if (viewModel.canWriteLocalBackup()) {
            viewModel.showRestoreDialog()
        } else {
            explicarPermiso = MotivoDelPermiso.RESTAURAR
        }
    }

    explicarPermiso?.let { motivo ->
        ExplicacionDelPermiso(
            motivo = motivo,
            onContinuar = {
                explicarPermiso = null
                when (motivo) {
                    MotivoDelPermiso.COPIA_LOCAL ->
                        downloadsPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    MotivoDelPermiso.RESTAURAR ->
                        restorePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            },
            onCancelar = {
                explicarPermiso = null
                if (motivo == MotivoDelPermiso.RESTAURAR) viewModel.showRestoreDialog()
            },
        )
    }

    val backupSuccessMsg       = stringResource(R.string.drive_backup_success)
    val passwordSavedMsg      = stringResource(R.string.drive_sync_password_saved)
    val claveActivadaMsg      = stringResource(R.string.key_confirmed_msg)
    val passwordGoneMsg       = stringResource(R.string.drive_change_password_gone)
    val dualBackupSavedFmt    = stringResource(R.string.drive_dual_backup_saved)
    val deleteBackupSuccessMsg = stringResource(R.string.drive_delete_backup_success)
    val importSuccessZeroFmt   = stringResource(R.string.ie_import_success_zero)
    val importSuccessOneFmt    = stringResource(R.string.ie_import_success_one)
    val importSuccessFmt       = stringResource(R.string.ie_import_success)
    val importDuplicatesOneFmt = stringResource(R.string.ie_import_duplicates_skipped_one)
    val importDuplicatesFmt    = stringResource(R.string.ie_import_duplicates_skipped)
    val importSkippedOneFmt    = stringResource(R.string.ie_import_skipped_warning_one)
    val importSkippedFmt       = stringResource(R.string.ie_import_skipped_warning)
    val importProtectedOneFmt  = stringResource(R.string.ie_import_protected_warning_one)
    val importProtectedFmt     = stringResource(R.string.ie_import_protected_warning)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DriveSyncEvent.BackupSuccess       -> snackbar.showSnackbar(backupSuccessMsg)
                is DriveSyncEvent.DeleteBackupSuccess -> snackbar.showSnackbar(deleteBackupSuccessMsg)
                is DriveSyncEvent.RestoreSuccess -> {
                    val importedMsg = when (event.count) {
                        0    -> importSuccessZeroFmt.format(event.count)
                        1    -> importSuccessOneFmt.format(event.count)
                        else -> importSuccessFmt.format(event.count)
                    }
                    val parts = buildList {
                        add(importedMsg)
                        if (event.duplicates == 1) add(importDuplicatesOneFmt.format(event.duplicates))
                        else if (event.duplicates > 1) add(importDuplicatesFmt.format(event.duplicates))
                        if (event.skipped == 1) add(importSkippedOneFmt.format(event.skipped))
                        else if (event.skipped > 1) add(importSkippedFmt.format(event.skipped))
                        if (event.protectedSkipped == 1) add(importProtectedOneFmt.format(event.protectedSkipped))
                        else if (event.protectedSkipped > 1) add(importProtectedFmt.format(event.protectedSkipped))
                    }
                    snackbar.showSnackbar(parts.joinToString(" · "))
                }
                is DriveSyncEvent.ClaveActivada         -> snackbar.showSnackbar(claveActivadaMsg)
                is DriveSyncEvent.PasswordSaved         -> {
                    cambiandoContrasena = false
                    snackbar.showSnackbar(passwordSavedMsg)
                }
                is DriveSyncEvent.Error                 -> snackbar.showSnackbar(event.message.asString(context))
                is DriveSyncEvent.NavigateToPlans       -> onNavigatePlans()
                is DriveSyncEvent.AuthorizationRequired -> authLauncher.launch(event.intent)
                is DriveSyncEvent.LocalBackupSaved      -> snackbar.showSnackbar(
                    dualBackupSavedFmt.format(event.fileName)
                )
                is DriveSyncEvent.BackupPasswordGone    -> {
                    cambiandoContrasena = false
                    viewModel.reload()
                    snackbar.showSnackbar(passwordGoneMsg)
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar, modifier = Modifier.padding(bottom = 40.dp)) },
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
                        TextoQueSeAjusta(
                            stringResource(R.string.drive_sync_screen_title),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 7.dp, top = 7.dp, end = 7.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val isFree = uiState.userPlan == UserPlan.FREE
                val credentialTypeUnrecognized  = stringResource(R.string.drive_sync_error_credential_type_unrecognized)
                val accountSelectErrorFmt       = stringResource(R.string.drive_sync_error_select_account)

                when {
                    uiState.driveAccount == null -> {
                        NotConnectedCard(
                            onConnect = {
                                coroutineScope.launch {
                                    when (val result = requestGoogleAccountConnect(context, credentialManager)) {
                                        is GoogleConnectResult.Success -> viewModel.onAccountSelected(
                                            email   = result.email,
                                            idToken = result.idToken
                                        )
                                        is GoogleConnectResult.Cancelled -> {
                                        }
                                        is GoogleConnectResult.Error -> viewModel.setError(
                                            if (result.message != null) String.format(accountSelectErrorFmt, result.message)
                                            else credentialTypeUnrecognized
                                        )
                                    }
                                }
                            }
                        )
                    }

                    !uiState.hasBackupPassword || uiState.showSetupPassword -> {
                        ConnectedStatusCard(
                            account      = uiState.driveAccount ?: "",
                            lastSync     = uiState.lastDriveSync,
                            showBadge    = false,
                            onDisconnect = { viewModel.showDisconnectDialog() }
                        )
                        if (uiState.userPlan != UserPlan.FREE) {
                            ClaveSetupCard(
                                words     = remember { viewModel.palabrasDeLaClave() },
                                account   = uiState.driveAccount,
                                snackbar  = snackbar,
                                onConfirm = { viewModel.confirmarClaveGuardada() },
                                onUseWords = { viewModel.usarPalabrasPropias(it) }
                            )
                        }
                        if (uiState.showRestoreSetupCard) {
                            RestoreSetupCard(
                                onRestore = pedirRestore
                            )
                        }
                    }

                    else -> {
                        DriveMainCard(
                            account          = uiState.driveAccount ?: "",
                            lastSync         = uiState.lastDriveSync,
                            isFree           = isFree,
                            canViewKey       = !isFree || uiState.hasBackupPassword,
                            waitingWifi      = uiState.backupWaitingWifi && !uiState.isWorking && !uiState.autoBackupRunning,
                            driveFull        = uiState.driveFull && !uiState.isWorking && !uiState.autoBackupRunning,
                            onBackup         = { viewModel.backup() },
                            onRestore        = pedirRestore,
                            onViewKey        = onOpenBackupKey,
                            onDeleteBackup   = { viewModel.showDeleteBackupDialog() },
                            onDisconnect     = { viewModel.showDisconnectDialog() },
                            onUpgrade        = { viewModel.onUpgradeTapped() }
                        )

                        if (!isFree) {
                            DualBackupToggle(
                                enabled  = uiState.dualLocalBackupEnabled,
                                onToggle = {
                                    val encender = !uiState.dualLocalBackupEnabled
                                    if (encender && !viewModel.canWriteLocalBackup()) {
                                        explicarPermiso = MotivoDelPermiso.COPIA_LOCAL
                                    } else {
                                        viewModel.setDualLocalBackup(enabled = encender)
                                    }
                                }
                            )
                        }

                        Surface(
                            onClick        = onTimeMachineClick,
                            shape          = RoundedCornerShape(12.dp),
                            color          = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier       = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier          = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.History,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text       = stringResource(R.string.drive_time_machine_notice),
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color      = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text  = stringResource(R.string.drive_time_machine_go),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector        = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isWorking || uiState.autoBackupRunning) {
                Box(
                    modifier           = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment   = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    }

    var replaceStep by remember { mutableIntStateOf(0) }
    var replacePendingPassword by remember { mutableStateOf<String?>(null) }
    if (replaceStep == 1) {
        com.thormakk.keyarmor.presentation.screens.timemachine.ReplaceConfirmationDialog(
            onConfirm   = { replaceStep = 2 },
            onDismiss   = { replaceStep = 0 },
            description = stringResource(R.string.drive_restore_mode_replace_desc)
        )
    }
    if (replaceStep == 2) {
        com.thormakk.keyarmor.presentation.screens.timemachine.ReplaceAuthGate(
            onAuthorized         = { replaceStep = 0; viewModel.restore(replacePendingPassword) },
            onDismiss            = { replaceStep = 0 },
            verifyMasterPassword = viewModel::verifyMasterPassword
        )
    }
    if (uiState.showRestoreDialog) {
        RestoreDialog(
            selectedMode  = uiState.restoreMode,
            isFreePlan    = uiState.userPlan == UserPlan.FREE,
            needsPassword = uiState.restoreNeedsPassword,
            passwordError = uiState.restorePasswordError || uiState.restoreKeyError != null,
            keyError      = uiState.restoreKeyError,
            isWorking     = uiState.isWorking,
            onModeChange  = { if (!uiState.isWorking) viewModel.setRestoreMode(it) },
            onConfirm     = { manualPassword ->
                if (uiState.restoreMode == RestoreMode.REPLACE) {
                    replacePendingPassword = manualPassword
                    replaceStep = 1
                } else {
                    viewModel.restore(manualPassword)
                }
            },
            onDismiss     = { viewModel.hideRestoreDialog() }
        )
    }

    val localBackupTs = uiState.localBackupTimestamp
    val cloudBackupTs = uiState.cloudBackupTimestamp
    if (uiState.showRestoreSourceDialog && localBackupTs != null && cloudBackupTs != null) {
        RestoreSourceDialog(
            localTimestamp = localBackupTs,
            cloudTimestamp = cloudBackupTs,
            onChoose       = { viewModel.chooseRestoreSource(it) },
            onDismiss      = { viewModel.hideRestoreSourceDialog() }
        )
    }

    if (uiState.showHeavyBackupDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideHeavyBackupDialog() },
            title = {
                Text(
                    stringResource(R.string.drive_heavy_backup_title),
                    fontWeight = FontWeight.SemiBold
                )
            },
            text  = { Text(stringResource(R.string.drive_heavy_backup_message, uiState.heavyBackupMb), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmHeavyBackup() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.drive_heavy_backup_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.waitForWifiForBackup() }) {
                    Text(stringResource(R.string.drive_heavy_backup_wait))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (uiState.showDeleteBackupDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteBackupDialog() },
            title = {
                Text(
                    stringResource(R.string.drive_delete_backup_confirm_title),
                    fontWeight = FontWeight.SemiBold
                )
            },
            text  = { Text(stringResource(R.string.drive_delete_backup_confirm_msg), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.hideDeleteBackupDialog()
                        securityGate.launch { viewModel.deleteBackup() }
                    },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.drive_action_delete_backup))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteBackupDialog() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (uiState.showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDisconnectDialog() },
            title = { Text(stringResource(R.string.drive_disconnect_confirm_title), fontWeight = FontWeight.SemiBold) },
            text  = {
                Text(
                    if (uiState.userPlan != UserPlan.FREE) {
                        stringResource(R.string.drive_disconnect_confirm_msg_premium)
                    } else {
                        stringResource(R.string.drive_disconnect_confirm_msg)
                    }, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.hideDisconnectDialog()
                        securityGate.launch { viewModel.disconnect() }
                    },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.drive_action_disconnect))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDisconnectDialog() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

}

@Composable
private fun NotConnectedCard(onConnect: () -> Unit) {
    SyncCard {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Cloud,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(48.dp)
            )
            Text(
                text       = stringResource(R.string.drive_sync_not_connected),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedButton(
                onClick  = onConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .coachMarkAnchor("drive_sync", "connect")
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.drive_sync_connect_google))
            }
        }
    }
}

@Composable
private fun ConnectedStatusCard(
    account:      String,
    lastSync:     Long,
    showBadge:    Boolean,
    onDisconnect: (() -> Unit)? = null
) {
    SyncCard {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector        = Icons.Default.Cloud,
                contentDescription = null,
                tint               = if (showBadge)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .padding(top = 2.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = stringResource(R.string.drive_sync_connected_as),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    when {
                        onDisconnect != null -> Surface(
                            shape    = RoundedCornerShape(6.dp),
                            color    = MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable(onClick = onDisconnect)
                        ) {
                            Text(
                                text     = stringResource(R.string.drive_action_disconnect),
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        showBadge -> Icon(
                            imageVector        = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text       = account,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier   = Modifier.fillMaxWidth()
                )
                if (lastSync > 0L) {
                    Text(
                        text  = "${stringResource(R.string.drive_sync_last_backup)}: ${syncSubtitle(lastSync)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DriveMainCard(
    account:          String,
    lastSync:         Long,
    isFree:           Boolean,
    canViewKey:       Boolean,
    waitingWifi:      Boolean,
    driveFull:        Boolean,
    onBackup:         () -> Unit,
    onRestore:        () -> Unit,
    onViewKey:        () -> Unit,
    onDeleteBackup:   () -> Unit,
    onDisconnect:     () -> Unit,
    onUpgrade:        () -> Unit
) {
    SyncCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector        = Icons.Default.Cloud,
                    contentDescription = null,
                    tint               = if (!isFree)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(top = 2.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier             = Modifier.weight(1f),
                    verticalArrangement  = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text  = stringResource(R.string.drive_sync_connected_as),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text       = account,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier   = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector        = Icons.Default.Sync,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier
                        .size(36.dp)
                        .padding(top = 2.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text  = if (lastSync > 0L)
                        "${stringResource(R.string.drive_sync_auto_subtitle)} ${stringResource(R.string.drive_sync_last_backup)}: ${syncSubtitle(lastSync)}."
                    else
                        stringResource(R.string.drive_sync_auto_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (isFree) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Lock,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = stringResource(R.string.drive_sync_free_locked),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth(), colors = appButtonColors()) {
                        Text(stringResource(R.string.plans_upgrade_cta))
                    }
                }
            } else {
                Column {
                    Box(Modifier.coachMarkAnchor("drive_sync", "backup")) {
                        ActionButton(
                            icon    = Icons.Default.Cloud,
                            label   = stringResource(R.string.drive_action_backup),
                            onClick = onBackup
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = stringResource(R.string.drive_backup_no_attachments_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (waitingWifi) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = stringResource(R.string.drive_backup_waiting_wifi_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (driveFull) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = stringResource(R.string.drive_storage_full),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Column {
                ActionButton(
                    icon    = Icons.Default.Restore,
                    label   = stringResource(R.string.drive_action_restore),
                    onClick = onRestore
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = stringResource(R.string.drive_restore_priority_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = stringResource(R.string.hidden_entries_import_note),
                    style = MaterialTheme.typography.bodySmall.justified(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (canViewKey) {
                OutlinedActionButton(
                    label   = stringResource(R.string.key_view_button),
                    color   = MaterialTheme.colorScheme.primary,
                    onClick = onViewKey
                )
            }

            Button(
                onClick  = onDeleteBackup,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    imageVector        = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.drive_action_delete_backup))
            }

            OutlinedActionButton(
                label   = stringResource(R.string.drive_action_disconnect),
                color   = warningColor(),
                onClick = onDisconnect
            )
        }
    }
}

@Composable
private fun ClaveSetupCard(
    words:     List<String>?,
    account:   String?,
    snackbar:  SnackbarHostState,
    onConfirm: () -> Unit,
    onUseWords: (String) -> String?
) {
    var guardada by rememberSaveable { mutableStateOf(false) }
    var tengoLasMias by rememberSaveable { mutableStateOf(false) }
    if (tengoLasMias) {
        TengoMisPalabrasCard(onUse = onUseWords, onBack = { tengoLasMias = false })
        return
    }
    SyncCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text       = stringResource(R.string.key_setup_title),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            Text(
                text  = stringResource(R.string.key_setup_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape        = RoundedCornerShape(8.dp),
                color        = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier     = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = stringResource(R.string.key_setup_warning),
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }
            if (words != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    words.chunked(3).forEachIndexed { fila, grupo ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            grupo.forEachIndexed { i, palabra ->
                                Surface(
                                    shape    = RoundedCornerShape(8.dp),
                                    color    = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text       = "${fila * 3 + i + 1}. $palabra",
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (words != null) EmergencyKitActions(words = words, account = account, snackbar = snackbar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { guardada = !guardada },
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Checkbox(checked = guardada, onCheckedChange = { guardada = it })
                Text(
                    text     = stringResource(R.string.key_setup_checkbox),
                    style    = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick  = onConfirm,
                enabled  = guardada && words != null,
                modifier = Modifier.fillMaxWidth(), colors = appButtonColors()
            ) {
                Text(stringResource(R.string.key_setup_button))
            }
            OutlinedButton(
                onClick  = { tengoLasMias = true },
                border   = acentoBorde(),
                colors   = acentoOutlinedColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.key_have_button))
            }
        }
    }
}

@Composable
private fun TengoMisPalabrasCard(
    onUse:  (String) -> String?,
    onBack: () -> Unit
) {
    var texto by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    SyncCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text       = stringResource(R.string.key_have_button),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            Text(
                text  = stringResource(R.string.key_have_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value           = texto,
                onValueChange   = { texto = it; error = null },
                label           = { Text(stringResource(R.string.key_restore_hint)) },
                minLines        = 2,
                isError         = error != null,
                colors          = OutlinedTextFieldDefaults.colors(
                    errorBorderColor = warningColor(),
                    errorLabelColor  = warningColor(),
                    errorCursorColor = warningColor()
                ),
                modifier        = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType       = KeyboardType.Text,
                    capitalization     = androidx.compose.ui.text.input.KeyboardCapitalization.None,
                    autoCorrectEnabled = false
                )
            )
            androidx.compose.animation.AnimatedVisibility(visible = texto.isEmpty()) {
                TextButton(onClick = {
                    val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    cm.primaryClip?.getItemAt(0)?.coerceToText(ctx)?.toString()?.let { texto = it; error = null }
                }) { Text(stringResource(R.string.key_paste)) }
            }
            error?.let {
                Text(
                    text  = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = warningColor()
                )
            }
            Button(
                onClick  = { error = onUse(texto) },
                enabled  = texto.isNotBlank(),
                modifier = Modifier.fillMaxWidth(), colors = appButtonColors()
            ) {
                Text(stringResource(R.string.key_have_use))
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon:    ImageVector,
    label:   String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun OutlinedActionButton(
    label:   String,
    color:   Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(0.5.dp, color),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Text(label)
    }
}

@Composable
private fun SyncCard(
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape          = RoundedCornerShape(12.dp),
        color          = containerColor,
        tonalElevation = 1.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content  = content
        )
    }
}

@Composable
private fun RestoreSetupCard(onRestore: () -> Unit) {
    SyncCard(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.Restore,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = stringResource(R.string.drive_restore_setup_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text  = stringResource(R.string.drive_restore_setup_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = stringResource(R.string.hidden_entries_import_note),
                style = MaterialTheme.typography.bodySmall.justified(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick  = onRestore,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.drive_action_restore))
            }
        }
    }
}

@Composable
private fun RestoreDialog(
    selectedMode:  RestoreMode,
    isFreePlan:    Boolean = false,
    needsPassword: Boolean = false,
    passwordError: Boolean = false,
    keyError:      String? = null,
    isWorking:     Boolean = false,
    onModeChange:  (RestoreMode) -> Unit,
    onConfirm:     (manualPassword: String?) -> Unit,
    onDismiss:     () -> Unit
) {
    var manualPassword by remember { mutableStateOf("") }

    AlertDialog(
        modifier = Modifier.cuadroConTeclado(),
        properties = propiedadesConTeclado(),
        onDismissRequest = { if (!isWorking) onDismiss() },
        title   = {
            Text(
                stringResource(R.string.drive_restore_password_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                if (isWorking) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color       = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text       = stringResource(R.string.time_machine_restoring),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (needsPassword) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value           = manualPassword,
                            onValueChange   = { manualPassword = it },
                            label           = { Text(stringResource(R.string.key_restore_hint)) },
                            minLines        = 2,
                            isError         = passwordError,
                            colors          = OutlinedTextFieldDefaults.colors(
                                errorBorderColor    = warningColor(),
                                errorLabelColor     = warningColor(),
                                errorCursorColor    = warningColor()
                            ),
                            modifier        = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType       = KeyboardType.Text,
                                capitalization     = androidx.compose.ui.text.input.KeyboardCapitalization.None,
                                autoCorrectEnabled = false
                            )
                        )
                        val pasteCtx = androidx.compose.ui.platform.LocalContext.current
                        androidx.compose.animation.AnimatedVisibility(visible = manualPassword.isEmpty()) {
                            TextButton(onClick = {
                                val cm = pasteCtx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                cm.primaryClip?.getItemAt(0)?.coerceToText(pasteCtx)?.toString()?.let { manualPassword = it }
                            }) { Text(stringResource(R.string.key_paste)) }
                        }
                        if (passwordError) {
                            Text(
                                text  = keyError ?: stringResource(R.string.key_err_wrong_key),
                                style = MaterialTheme.typography.bodySmall,
                                color = warningColor()
                            )
                        }
                    }
                }

                if (isFreePlan) {
                    Surface(
                        shape    = RoundedCornerShape(8.dp),
                        color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier          = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Info,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text  = stringResource(R.string.drive_restore_free_note),
                                style = MaterialTheme.typography.bodySmall.justified(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text       = stringResource(R.string.drive_restore_mode_title),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )

                RestoreModeOption(
                    selected    = selectedMode == RestoreMode.MERGE,
                    title       = stringResource(R.string.drive_restore_mode_merge),
                    description = stringResource(R.string.drive_restore_mode_merge_desc),
                    isDestructive = false,
                    onClick     = { onModeChange(RestoreMode.MERGE) }
                )
                RestoreModeOption(
                    selected    = selectedMode == RestoreMode.IMPORT_ALL,
                    title       = stringResource(R.string.drive_restore_mode_all),
                    description = stringResource(R.string.drive_restore_mode_all_desc),
                    isDestructive = false,
                    onClick     = { onModeChange(RestoreMode.IMPORT_ALL) }
                )
                RestoreModeOption(
                    selected    = selectedMode == RestoreMode.REPLACE,
                    title       = stringResource(R.string.drive_restore_mode_replace),
                    description = stringResource(R.string.drive_restore_mode_replace_desc),
                    isDestructive = false,
                    onClick     = { onModeChange(RestoreMode.REPLACE) }
                )
            }
        },
        confirmButton = {
            val reemplazaTodo = selectedMode == RestoreMode.REPLACE
            Button(
                onClick = { onConfirm(if (needsPassword) manualPassword else null) },
                enabled = !isWorking && (!needsPassword || manualPassword.isNotBlank()),
                shape   = RoundedCornerShape(8.dp),
                colors  = if (reemplazaTodo) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    )
                },
                border = if (reemplazaTodo) null
                         else BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.drive_action_restore))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isWorking) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Suppress("SameParameterValue")
@Composable
private fun RestoreModeOption(
    selected:     Boolean,
    title:        String,
    description:  String,
    isDestructive: Boolean,
    onClick:      () -> Unit
) {
    val borderColor = when {
        selected && isDestructive -> MaterialTheme.colorScheme.error
        selected                  -> MaterialTheme.colorScheme.primary
        else                      -> MaterialTheme.colorScheme.outlineVariant
    }
    val bgColor = when {
        selected && isDestructive -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        selected                  -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else                      -> Color.Transparent
    }

    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(8.dp),
        color   = bgColor,
        border  = androidx.compose.foundation.BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = borderColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick  = null,
                colors   = RadioButtonDefaults.colors(
                    selectedColor = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isDestructive && selected)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RestoreSourceDialog(
    localTimestamp: Long,
    cloudTimestamp: Long,
    onChoose:       (RestoreSource) -> Unit,
    onDismiss:      () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.drive_restore_source_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text  = stringResource(R.string.drive_restore_source_desc),
                    style = MaterialTheme.typography.bodySmall.justified(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RestoreSourceOption(
                    title    = stringResource(R.string.drive_restore_source_local),
                    subtitle = "${stringResource(R.string.drive_sync_last_backup)}: ${syncSubtitle(localTimestamp)}",
                    onClick  = { onChoose(RestoreSource.LOCAL) }
                )
                RestoreSourceOption(
                    title    = stringResource(R.string.drive_restore_source_cloud),
                    subtitle = "${stringResource(R.string.drive_sync_last_backup)}: ${syncSubtitle(cloudTimestamp)}",
                    onClick  = { onChoose(RestoreSource.CLOUD) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun RestoreSourceOption(
    title:    String,
    subtitle: String,
    onClick:  () -> Unit
) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(8.dp),
        border   = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private enum class MotivoDelPermiso { COPIA_LOCAL, RESTAURAR }

@Composable
private fun ExplicacionDelPermiso(
    motivo:      MotivoDelPermiso,
    onContinuar: () -> Unit,
    onCancelar:  () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                stringResource(R.string.drive_storage_permission_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                stringResource(
                    when (motivo) {
                        MotivoDelPermiso.COPIA_LOCAL -> R.string.drive_storage_permission_body_backup
                        MotivoDelPermiso.RESTAURAR   -> R.string.drive_storage_permission_body_restore
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onContinuar,
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = MaterialTheme.colorScheme.primary
                ),
                border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.action_continue), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun DualBackupToggle(
    enabled:  Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick        = onToggle,
        shape          = RoundedCornerShape(12.dp),
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = stringResource(R.string.drive_dual_backup_toggle),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onBackground,
                    modifier   = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked         = enabled,
                    onCheckedChange = { onToggle() },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor    = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor    = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text      = stringResource(R.string.drive_dual_backup_subtitle),
                   style     = MaterialTheme.typography.bodySmall.justified(),
                fontSize  = 13.sp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun syncSubtitle(lastSync: Long): String {
    if (lastSync == 0L) return stringResource(R.string.drive_never_synced)
    val diff    = System.currentTimeMillis() - lastSync
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours   = TimeUnit.MILLISECONDS.toHours(diff)
    val days    = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        minutes < 1  -> stringResource(R.string.drive_synced_seconds)
        minutes < 60 -> stringResource(R.string.drive_synced_minutes, minutes)
        hours < 24   -> stringResource(R.string.drive_synced_hours, hours)
        days == 1L   -> stringResource(R.string.drive_synced_yesterday)
        else         -> stringResource(R.string.drive_synced_days, days)
    }
}
