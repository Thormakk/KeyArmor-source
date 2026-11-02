@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.vaulthub

import androidx.compose.foundation.interaction.MutableInteractionSource
import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.util.openUrl
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.BuildConfig
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.bank.BankAwareAvatar
import com.thormakk.keyarmor.presentation.screens.creditcard.CreditCardAwareAvatar
import com.thormakk.keyarmor.presentation.screens.crypto.CryptoAwareAvatar
import com.thormakk.keyarmor.presentation.screens.vault.EntryAvatar
import com.thormakk.keyarmor.presentation.screens.vault.ServiceAwareAvatar
import com.thormakk.keyarmor.ui.theme.PasswordStrengthFair
import com.thormakk.keyarmor.ui.theme.PasswordStrengthStrong
import com.thormakk.keyarmor.ui.theme.PasswordStrengthWeak
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import com.thormakk.keyarmor.presentation.components.justified

@Suppress("DEPRECATION")
@Composable
fun VaultHubScreen(
    onPasswordsClick:        () -> Unit,
    onPasswordStrengthClick: (String) -> Unit = {},
    onBankAccountsClick:     () -> Unit,
    onCustomEntriesClick: () -> Unit,
    onSecureNotesClick:   () -> Unit,
    onCreditCardsClick:   () -> Unit,
    onRemindersClick:     () -> Unit = {},
    onArchiveClick:       () -> Unit = {},
    onTrashClick:         () -> Unit = {},
    onTwoFactorClick:     () -> Unit = {},
    onCryptoWalletsClick: () -> Unit = {},
    onPlansClick:         () -> Unit = {},
    onManageVaultsClick:  () -> Unit = {},
    onPasskeysClick:          () -> Unit = {},
    onIdentityDocumentsClick: () -> Unit = {},
    onVehiclesClick:          () -> Unit = {},
    onAddPasswordClick:         (() -> Unit)? = null,
    onAddTwoFactorClick:        (() -> Unit)? = null,
    onAddCreditCardClick:       (() -> Unit)? = null,
    onAddBankAccountClick:      (() -> Unit)? = null,
    onAddCryptoWalletClick:     (() -> Unit)? = null,
    onAddReminderClick:         (() -> Unit)? = null,
    onAddSecureNoteClick:       (() -> Unit)? = null,
    onAddCustomEntryClick:      (() -> Unit)? = null,
    onAddIdentityDocumentClick: (() -> Unit)? = null,
    onAddVehicleClick:          (() -> Unit)? = null,
    onNotificationsClick:       () -> Unit = {},
    onNavigateToDriveSync:      () -> Unit = {},
    onPinnedEntryClick:         (String, HubEntryType) -> Unit = { _, _ -> },
    sharedTransitionScope:      SharedTransitionScope?        = null,
    animatedVisibilityScope:    AnimatedVisibilityScope?      = null,
    viewModel: VaultHubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notesColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5f)
        Color(0xFFB36A00)
    else
        HubColorNotes
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showAboutMenu           by remember { mutableStateOf(false) }
    var showRemindersLockedDialog by remember { mutableStateOf(false) }
    var lockedSectionTitle by remember { mutableStateOf<String?>(null) }
    var showVaultSelector       by remember { mutableStateOf(false) }
    var showConsolidateDialog   by remember { mutableStateOf(false) }
    var isPinnedDragActive      by remember { mutableStateOf(false) }
    var pinnedBoundsInRoot      by remember { mutableStateOf(Rect.Zero) }

    var healthCardVisible by remember { mutableStateOf(false) }
    var group1Visible     by remember { mutableStateOf(false) }
    var group2Visible     by remember { mutableStateOf(false) }
    var group3Visible     by remember { mutableStateOf(false) }
    var group4Visible     by remember { mutableStateOf(false) }
    var group5Visible     by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        healthCardVisible = true
        delay(80);  group1Visible = true
        delay(80);  group2Visible = true
        delay(80);  group3Visible = true
        delay(80);  group4Visible = true
        delay(80);  group5Visible = true
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkPendingHeirFromPrefs(fromResume = true)
                viewModel.checkPendingFamilyInviteFromPrefs()
                viewModel.refreshPlanStatus()
                viewModel.checkPendingMemberPremiumRevokedFromPrefs()
                viewModel.checkPendingMemberAccessRevokedFromPrefs()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val driveAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.clearDriveAuthForInheritance()
        viewModel.retryInheritedVaultImport()
    }
    LaunchedEffect(uiState.driveAuthForInheritanceIntent) {
        val intent = uiState.driveAuthForInheritanceIntent ?: return@LaunchedEffect
        driveAuthLauncher.launch(intent)
    }

    val encryptAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.clearDriveAuthForEncrypt()
        viewModel.retryEncryptAndUpload()
    }
    LaunchedEffect(uiState.driveAuthForEncryptIntent) {
        val intent = uiState.driveAuthForEncryptIntent ?: return@LaunchedEffect
        encryptAuthLauncher.launch(intent)
    }

    if (uiState.showInheritanceRecoveryPrompt) {
        var recoveryCode by remember { mutableStateOf("") }
        AlertDialog(
            modifier = Modifier.cuadroConTeclado(),
            properties = propiedadesConTeclado(),
            onDismissRequest = { viewModel.dismissInheritanceRecoveryPrompt() },
            title = { Text(stringResource(R.string.inheritance_recovery_prompt_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.inheritance_recovery_prompt_body), style = MaterialTheme.typography.bodyMedium.justified())
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                        value = recoveryCode,
                        onValueChange = { recoveryCode = it },
                        singleLine = true,
                        isError = uiState.inheritanceRecoveryKeyError,
                        label = { Text(stringResource(R.string.inheritance_recovery_prompt_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.inheritanceRecoveryKeyError) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.inheritance_recovery_prompt_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    uiState.inheritanceImportErrorRes?.let { res ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(res),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitInheritanceRecoveryKey(recoveryCode) },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.inheritance_recovery_prompt_submit), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissInheritanceRecoveryPrompt() }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    val memberRevokedMsg      = stringResource(R.string.family_member_revoked)
    val memberRevokedNamedFmt = stringResource(R.string.family_member_revoked_named)
    LaunchedEffect(Unit) {
        snapshotFlow { uiState.memberRevokedSnackbar }
            .filter { it }
            .collect {
                val adminEmail = uiState.memberRevokedAdminEmail
                viewModel.clearMemberRevokedSnackbar()
                val msg = if (!adminEmail.isNullOrBlank())
                    String.format(memberRevokedNamedFmt, adminEmail)
                else
                    memberRevokedMsg
                snackbarHostState.showSnackbar(msg)
            }
    }

    val memberPremiumRevokedMsg = stringResource(R.string.family_premium_revoked_snackbar)
    LaunchedEffect(Unit) {
        snapshotFlow { uiState.memberPremiumRevokedSnackbar }
            .filter { it }
            .collect {
                viewModel.clearMemberPremiumRevokedSnackbar()
                snackbarHostState.showSnackbar(memberPremiumRevokedMsg)
            }
    }



    if (uiState.showWhatsNew) {
        WhatsNewBottomSheet(onDismiss = viewModel::dismissWhatsNew)
    }

    if (showConsolidateDialog) {
        AlertDialog(
            onDismissRequest = { showConsolidateDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.vault_consolidate_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.vault_consolidate_confirm_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConsolidateDialog = false
                        viewModel.consolidateVault(uiState.activeVaultId)
                    },
                    shape  = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.vault_consolidate_confirm_ok), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showConsolidateDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showRemindersLockedDialog) {
        AlertDialog(
            onDismissRequest = { showRemindersLockedDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.settings_reminders),
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            text = {
                Text(
                    stringResource(R.string.premium_locked_dialog_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRemindersLockedDialog = false; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { showRemindersLockedDialog = false }) {
                    Text(stringResource(R.string.action_understood), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    lockedSectionTitle?.let { sectionTitle ->
        AlertDialog(
            onDismissRequest = { lockedSectionTitle = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            text = {
                Text(
                    stringResource(R.string.premium_locked_dialog_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { lockedSectionTitle = null; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { lockedSectionTitle = null }) {
                    Text(stringResource(R.string.action_understood), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (uiState.showDriveReconnectNotice) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDriveReconnectNotice,
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    stringResource(R.string.drive_reconnect_notice_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    stringResource(R.string.drive_reconnect_notice_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissDriveReconnectNotice()
                        onNavigateToDriveSync()
                    },
                    shape  = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.drive_sync_connect_google), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDriveReconnectNotice) {
                    Text(stringResource(R.string.lock_not_now), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    val scrimAlpha by animateFloatAsState(
        targetValue   = if (isPinnedDragActive) 0.45f else 0f,
        animationSpec = tween(250),
        label         = "drag_scrim_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
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
                        VaultSelectorTitle(
                            activeVaultId         = uiState.activeVaultId,
                            availableVaults       = uiState.availableVaults,
                            expanded              = showVaultSelector,
                            isActiveVaultReadOnly = uiState.isActiveVaultReadOnly,
                            isFreeUser            = uiState.isFreeUser,
                            onToggle              = { showVaultSelector = !showVaultSelector },
                            onSelect              = { vaultId ->
                                viewModel.switchVault(vaultId)
                                showVaultSelector = false
                            },
                            onManageVaults        = onManageVaultsClick
                        )
                    },
                    navigationIcon = {
                        Box {
                            IconButton(onClick = { showAboutMenu = true }) {
                                Icon(
                                    imageVector        = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.action_more_options),
                                    tint               = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded         = showAboutMenu,
                                onDismissRequest = { showAboutMenu = false },
                                containerColor   = MaterialTheme.colorScheme.surface
                            ) {
                                val sendEmailChooserTitle = stringResource(R.string.action_send_email)
                                val helpEmailSubject      = stringResource(R.string.support_help_email_subject)
                                val bugEmailSubject       = stringResource(R.string.support_bug_email_subject, BuildConfig.VERSION_NAME)
                                val bugEmailBody          = stringResource(
                                    R.string.support_bug_email_body,
                                    Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME
                                )
                                val shareAppText          = stringResource(R.string.share_app_text)
                                val shareAppTitle         = stringResource(R.string.menu_share_app)
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.QuestionMark, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text        = { Text(stringResource(R.string.menu_faq), style = MaterialTheme.typography.bodyMedium) },
                                    onClick     = {
                                        showAboutMenu = false
                                        context.openUrl("https://keyarmor.thormakk.dev/#faq")
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Outlined.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text        = { Text(stringResource(R.string.menu_help), style = MaterialTheme.typography.bodyMedium) },
                                    onClick     = {
                                        showAboutMenu = false
                                        val subject = Uri.encode(helpEmailSubject)
                                        context.startActivity(
                                            Intent.createChooser(
                                                Intent(Intent.ACTION_SENDTO,
                                                    "mailto:support@thormakk.dev?subject=$subject".toUri()),
                                                sendEmailChooserTitle
                                            )
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text        = { Text(stringResource(R.string.menu_report_bug), style = MaterialTheme.typography.bodyMedium) },
                                    onClick     = {
                                        showAboutMenu = false
                                        val subject = Uri.encode(bugEmailSubject)
                                        val body    = Uri.encode(bugEmailBody)
                                        context.startActivity(
                                            Intent.createChooser(
                                                Intent(Intent.ACTION_SENDTO,
                                                    "mailto:support@thormakk.dev?subject=$subject&body=$body".toUri()),
                                                sendEmailChooserTitle
                                            )
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text        = { Text(stringResource(R.string.menu_share_app), style = MaterialTheme.typography.bodyMedium) },
                                    onClick     = {
                                        showAboutMenu = false
                                        context.startActivity(
                                            Intent.createChooser(
                                                Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, shareAppText)
                                                },
                                                shareAppTitle
                                            )
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text        = { Text(stringResource(R.string.menu_rate_app), style = MaterialTheme.typography.bodyMedium) },
                                    onClick     = {
                                        showAboutMenu = false
                                        context.openUrl("https://play.google.com/store/apps/details?id=${context.packageName}")
                                    }
                                )
                            }
                        }
                    },
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        IconButton(onClick = { viewModel.lockNow() }) {
                            Icon(
                                imageVector        = Icons.Default.Logout,
                                contentDescription = stringResource(R.string.action_lock_app),
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        containerColor      = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {

            val enterAnim = fadeIn(tween(350, easing = FastOutSlowInEasing)) +
                            slideInVertically(tween(350, easing = FastOutSlowInEasing)) { it / 4 }

            AnimatedVisibility(visible = healthCardVisible, enter = enterAnim) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.coachMarkAnchor("vault_hub", "health")) {
                        VaultHealthCard(
                            weakCount       = uiState.weakPasswordCount,
                            mediumCount     = uiState.mediumPasswordCount,
                            strongCount     = uiState.strongPasswordCount,
                            onStrengthClick = onPasswordStrengthClick
                        )
                    }
                }
            }

            if (uiState.pinnedEntries.isNotEmpty()) {
                AnimatedVisibility(visible = group1Visible, enter = enterAnim) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                AnimatedVisibility(visible = group1Visible, enter = enterAnim) {
                    PinnedEntriesSection(
                        pinnedEntries           = uiState.pinnedEntries,
                        onEntryClick            = onPinnedEntryClick,
                        onReorder               = { from, to -> viewModel.reorderPinnedEntries(from, to) },
                        onDragActiveChange      = { isPinnedDragActive = it },
                        onBoundsChange          = { pinnedBoundsInRoot = it },
                        sharedTransitionScope   = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }

            AnimatedVisibility(visible = group1Visible, enter = enterAnim) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            AnimatedVisibility(visible = group1Visible, enter = enterAnim) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                VaultGroupHeader(
                    stringResource(R.string.hub_group_access_auth), HubColorAccess,
                    uiState.passwordCount + uiState.twoFactorCount + uiState.passkeyCount
                )
                VaultCategoryItem(
                    icon        = Icons.Outlined.Lock,
                    title       = stringResource(R.string.vault_title),
                    count       = uiState.passwordCount,
                    onClick     = onPasswordsClick,
                    onAddClick  = onAddPasswordClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorAccess,
                    modifier    = Modifier
                        .height(56.dp)
                        .coachMarkAnchor("vault_hub", "add_inline")
                )
                VaultItemDivider()
                VaultCategoryItem(
                    icon        = Icons.Outlined.Smartphone,
                    title       = stringResource(R.string.twofa_screen_title),
                    count       = uiState.twoFactorCount,
                    onClick     = onTwoFactorClick,
                    onAddClick  = onAddTwoFactorClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorAccess,
                    modifier    = Modifier.height(56.dp)
                )
                VaultItemDivider()
                VaultCategoryItem(
                    icon        = Icons.Outlined.Fingerprint,
                    title       = stringResource(R.string.ie_type_passkeys),
                    count       = uiState.passkeyCount,
                    onClick     = onPasskeysClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorAccess,
                    modifier    = Modifier.height(56.dp)
                )
            }
            }

            AnimatedVisibility(visible = group2Visible, enter = enterAnim) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            AnimatedVisibility(visible = group2Visible, enter = enterAnim) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                VaultGroupHeader(
                    stringResource(R.string.hub_group_finance), HubColorFinance,
                    uiState.creditCardCount + uiState.bankAccountCount + uiState.cryptoWalletCount
                )
                val cardsTitle = stringResource(R.string.vault_hub_credit_cards)
                VaultCategoryItem(
                    icon        = Icons.Outlined.CreditCard,
                    title       = cardsTitle,
                    count       = uiState.creditCardCount,
                    locked      = uiState.isFreeUser && uiState.creditCardCount == 0,
                    onClick     = {
                        if (uiState.isFreeUser && uiState.creditCardCount == 0) lockedSectionTitle = cardsTitle
                        else onCreditCardsClick()
                    },
                    onAddClick  = if (uiState.isFreeUser) null else onAddCreditCardClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorFinance,
                    modifier    = Modifier.height(56.dp)
                )
                VaultItemDivider()
                val bankTitle = stringResource(R.string.bank_title)
                VaultCategoryItem(
                    icon        = Icons.Outlined.AccountBalance,
                    title       = bankTitle,
                    count       = uiState.bankAccountCount,
                    locked      = uiState.isFreeUser && uiState.bankAccountCount == 0,
                    onClick     = {
                        if (uiState.isFreeUser && uiState.bankAccountCount == 0) lockedSectionTitle = bankTitle
                        else onBankAccountsClick()
                    },
                    onAddClick  = if (uiState.isFreeUser) null else onAddBankAccountClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorFinance,
                    modifier    = Modifier.height(56.dp)
                )
                VaultItemDivider()
                VaultCategoryItem(
                    icon        = Icons.Outlined.CurrencyBitcoin,
                    title       = stringResource(R.string.crypto_screen_title),
                    count       = uiState.cryptoWalletCount,
                    onClick     = onCryptoWalletsClick,
                    onAddClick  = onAddCryptoWalletClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorFinance,
                    modifier    = Modifier.height(56.dp)
                )
            }
            }

            AnimatedVisibility(visible = group3Visible, enter = enterAnim) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            AnimatedVisibility(visible = group3Visible, enter = enterAnim) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                VaultGroupHeader(
                    stringResource(R.string.hub_group_notes), notesColor,
                    uiState.reminderCount + uiState.secureNoteCount + uiState.customEntryCount
                )
                VaultCategoryItem(
                    icon        = Icons.Outlined.EditCalendar,
                    title       = stringResource(R.string.settings_reminders),
                    count       = if (uiState.isFreeUser) null else uiState.reminderCount,
                    locked      = uiState.isFreeUser,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = notesColor,
                    onClick     = {
                        if (uiState.isFreeUser) showRemindersLockedDialog = true
                        else onRemindersClick()
                    },
                    onAddClick  = if (uiState.isFreeUser) null else onAddReminderClick,
                    modifier    = Modifier.height(56.dp)
                )
                VaultItemDivider()
                VaultCategoryItem(
                    icon        = Icons.AutoMirrored.Outlined.StickyNote2,
                    title       = stringResource(R.string.notes_title),
                    count       = uiState.secureNoteCount,
                    onClick     = onSecureNotesClick,
                    onAddClick  = onAddSecureNoteClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = notesColor,
                    modifier    = Modifier.height(56.dp)
                )
                VaultItemDivider()
                VaultCategoryItem(
                    icon        = Icons.Outlined.GridView,
                    title       = stringResource(R.string.custom_title),
                    count       = uiState.customEntryCount,
                    onClick     = onCustomEntriesClick,
                    onAddClick  = onAddCustomEntryClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = notesColor,
                    modifier    = Modifier.height(56.dp)
                )
            }
            }

            AnimatedVisibility(visible = group4Visible, enter = enterAnim) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            AnimatedVisibility(visible = group4Visible, enter = enterAnim) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                VaultGroupHeader(
                    stringResource(R.string.hub_group_identity), HubColorIdentity,
                    uiState.identityDocumentCount + uiState.vehicleCount
                )
                val idTitle = stringResource(R.string.ie_type_identity)
                VaultCategoryItem(
                    icon        = Icons.Outlined.Badge,
                    title       = idTitle,
                    count       = uiState.identityDocumentCount,
                    locked      = uiState.isFreeUser && uiState.identityDocumentCount == 0,
                    onClick     = {
                        if (uiState.isFreeUser && uiState.identityDocumentCount == 0) lockedSectionTitle = idTitle
                        else onIdentityDocumentsClick()
                    },
                    onAddClick  = if (uiState.isFreeUser) null else onAddIdentityDocumentClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorIdentity,
                    modifier    = Modifier.height(56.dp)
                )
                VaultItemDivider()
                val vehicleTitle = stringResource(R.string.ie_type_vehicles)
                VaultCategoryItem(
                    icon        = Icons.Outlined.DirectionsCar,
                    title       = vehicleTitle,
                    count       = uiState.vehicleCount,
                    locked      = uiState.isFreeUser && uiState.vehicleCount == 0,
                    onClick     = {
                        if (uiState.isFreeUser && uiState.vehicleCount == 0) lockedSectionTitle = vehicleTitle
                        else onVehiclesClick()
                    },
                    onAddClick  = if (uiState.isFreeUser) null else onAddVehicleClick,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorIdentity,
                    modifier    = Modifier.height(56.dp)
                )
            }
            }

            AnimatedVisibility(visible = group5Visible, enter = enterAnim) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            AnimatedVisibility(visible = group5Visible, enter = enterAnim) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                VaultGroupHeader(stringResource(R.string.hub_archive_trash_group), HubColorArchive)
                VaultCategoryItem(
                    icon        = Icons.Outlined.Archive,
                    title       = stringResource(R.string.hub_archive_title),
                    count       = null,
                    locked      = uiState.isFreeUser,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorArchive,
                    onClick     = onArchiveClick,
                    modifier    = Modifier.height(56.dp)
                )
                VaultItemDivider()
                VaultCategoryItem(
                    icon        = Icons.Outlined.Delete,
                    title       = stringResource(R.string.hub_trash_title),
                    count       = null,
                    isReadOnly  = uiState.isActiveVaultReadOnly,
                    accentColor = HubColorArchive,
                    onClick     = onTrashClick,
                    modifier    = Modifier.height(56.dp)
                )
            }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (scrimAlpha > 0f) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isPinnedDragActive, pinnedBoundsInRoot) {
                    if (isPinnedDragActive) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                event.changes.forEach { change ->
                                    val y = change.position.y
                                    val inPinned = pinnedBoundsInRoot != Rect.Zero
                                        && y >= pinnedBoundsInRoot.top
                                        && y <= pinnedBoundsInRoot.bottom
                                    if (!inPinned) change.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            val scrimColor = Color.Black.copy(alpha = scrimAlpha)
            val bounds     = pinnedBoundsInRoot

            if (bounds == Rect.Zero) {
                drawRect(scrimColor)
            } else {
                if (bounds.top > 0f)
                    drawRect(scrimColor, topLeft = Offset.Zero, size = Size(size.width, bounds.top))
                if (bounds.bottom < size.height)
                    drawRect(scrimColor, topLeft = Offset(0f, bounds.bottom),
                        size = Size(size.width, size.height - bounds.bottom))
            }
        }
    }
    }
}

@Composable
private fun PinnedEntriesSection(
    pinnedEntries:           List<PinnedHubEntry>,
    onEntryClick:            (String, HubEntryType) -> Unit,
    onReorder:               (Int, Int) -> Unit             = { _, _ -> },
    onDragActiveChange:      (Boolean) -> Unit              = {},
    onBoundsChange:          (Rect) -> Unit                 = {},
    sharedTransitionScope:   SharedTransitionScope?   = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    var draggingIndex  by remember { mutableIntStateOf(-1) }
    var dragOffsetX    by remember { mutableFloatStateOf(0f) }
    var targetSlot     by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 7.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
            .onGloballyPositioned { onBoundsChange(it.boundsInRoot()) }
    ) {
        VaultGroupHeader(
            title       = stringResource(R.string.hub_pinned_section_title),
            accentColor = MaterialTheme.colorScheme.primary
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 14.dp)
        ) {
            val count        = pinnedEntries.size
            if (count == 0) return@BoxWithConstraints

            val density      = LocalDensity.current
            val gapPx        = with(density) { 8.dp.toPx() }
            val totalWidthPx = constraints.maxWidth.toFloat()
            val slots        = if (count >= 3) count else 4
            val itemWidthPx  = (totalWidthPx - gapPx * (slots - 1)) / slots

            fun slotFor(realIndex: Int): Int {
                if (draggingIndex < 0) return realIndex
                return when {
                    realIndex == draggingIndex -> targetSlot
                    targetSlot > draggingIndex && realIndex in (draggingIndex + 1)..targetSlot -> realIndex - 1
                    targetSlot < draggingIndex && realIndex in targetSlot until draggingIndex  -> realIndex + 1
                    else -> realIndex
                }
            }

            fun translationFor(realIndex: Int): Float {
                if (draggingIndex < 0) return 0f
                val isDragging = realIndex == draggingIndex
                return if (isDragging) {
                    dragOffsetX
                } else {
                    val naturalX = realIndex * (itemWidthPx + gapPx)
                    val targetX  = slotFor(realIndex) * (itemWidthPx + gapPx)
                    targetX - naturalX
                }
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val shadowElevPx = with(density) { 6.dp.toPx() }

                pinnedEntries.forEachIndexed { index, entry ->
                    val isDragging = index == draggingIndex

                    val animTranslation by animateFloatAsState(
                        targetValue   = translationFor(index),
                        animationSpec = when {
                            isDragging         -> tween(0)
                            draggingIndex >= 0 -> tween(200, easing = FastOutSlowInEasing)
                            else               -> tween(0)
                        },
                        label         = "pinned_tx_$index"
                    )
                    val animatedScale by animateFloatAsState(
                        targetValue   = if (isDragging) 1.10f else 1f,
                        animationSpec = if (draggingIndex >= 0) tween(150) else tween(0),
                        label         = "pinned_scale_$index"
                    )

                    Column(
                        modifier = Modifier
                            .then(
                                if (count >= 3) Modifier.weight(1f)
                                else Modifier.width(with(density) { itemWidthPx.toDp() })
                            )
                            .zIndex(if (isDragging) 2f else 1f)
                            .graphicsLayer {
                                translationX    = animTranslation
                                scaleX          = animatedScale
                                scaleY          = animatedScale
                                shadowElevation = if (isDragging) shadowElevPx else 0f
                                shape           = RoundedCornerShape(10.dp)
                                clip            = true
                            }
                            .then(
                                if (isDragging)
                                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                else Modifier
                            )
                            .pointerInput(entry.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { _ ->
                                        draggingIndex = index
                                        dragOffsetX   = 0f
                                        targetSlot    = index
                                        onDragActiveChange(true)
                                    },
                                    onDrag = { change, drag ->
                                        change.consume()
                                        dragOffsetX += drag.x
                                        val originX = index * (itemWidthPx + gapPx)
                                        val center  = originX + itemWidthPx / 2f + dragOffsetX
                                        targetSlot  = (center / (itemWidthPx + gapPx))
                                            .toInt().coerceIn(0, count - 1)
                                    },
                                    onDragEnd = {
                                        val originX  = draggingIndex * (itemWidthPx + gapPx)
                                        val center   = originX + itemWidthPx / 2f + dragOffsetX
                                        val inBounds = center >= 0f && center <= totalWidthPx
                                        if (inBounds && targetSlot != draggingIndex) {
                                            onReorder(draggingIndex, targetSlot)
                                        }
                                        draggingIndex = -1
                                        dragOffsetX   = 0f
                                        targetSlot    = -1
                                        onDragActiveChange(false)
                                    },
                                    onDragCancel = {
                                        draggingIndex = -1
                                        dragOffsetX   = 0f
                                        targetSlot    = -1
                                        onDragActiveChange(false)
                                    }
                                )
                            }
                            .clickable(enabled = draggingIndex < 0) {
                                onEntryClick(entry.id, entry.type)
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PinnedEntryAvatar(
                            entry                   = entry,
                            sharedTransitionScope   = if (!isDragging) sharedTransitionScope else null,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        Text(
                            text      = entry.displayTitle,
                            style     = MaterialTheme.typography.labelSmall,
                            color     = MaterialTheme.colorScheme.onBackground,
                            maxLines  = 2,
                            textAlign = TextAlign.Center,
                            overflow  = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PinnedEntryAvatar(
    entry:                   PinnedHubEntry,
    sharedTransitionScope:   SharedTransitionScope?   = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val ringColor = when {
        entry.type != HubEntryType.PASSWORD -> Color.Transparent
        entry.strengthScore < 0             -> Color.Transparent
        entry.strengthScore <= 1            -> PasswordStrengthWeak
        entry.strengthScore == 2            -> PasswordStrengthFair
        else                                -> PasswordStrengthStrong
    }
    val sharedKey = when (entry.type) {
        HubEntryType.PASSWORD      -> "entry_avatar_${entry.id}"
        HubEntryType.BANK_ACCOUNT  -> "bank_avatar_${entry.id}"
        HubEntryType.CREDIT_CARD   -> "card_avatar_${entry.id}"
        HubEntryType.CRYPTO_WALLET -> "crypto_avatar_${entry.id}"
        else                       -> null
    }
    val sharedMod = if (sharedKey != null && sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState      = rememberSharedContentState(sharedKey),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    } else Modifier
    Box(
        modifier = Modifier
            .size(48.dp)
            .drawBehind {
                if (ringColor != Color.Transparent) {
                    val avatarRadius = 20.dp.toPx()
                    val strokeWidth  = 1.5.dp.toPx()
                    drawCircle(
                        color  = ringColor,
                        radius = avatarRadius + strokeWidth / 2f,
                        style  = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (entry.type) {
            HubEntryType.PASSWORD ->
                ServiceAwareAvatar(title = entry.displayTitle, url = entry.url, size = 40.dp, modifier = sharedMod)
            HubEntryType.BANK_ACCOUNT ->
                BankAwareAvatar(bankName = entry.avatarLabel, size = 40.dp, modifier = sharedMod)
            HubEntryType.CREDIT_CARD ->
                CreditCardAwareAvatar(brand = entry.avatarLabel, size = 40.dp, modifier = sharedMod)
            HubEntryType.CRYPTO_WALLET ->
                CryptoAwareAvatar(asset = entry.avatarLabel, size = 40.dp, modifier = sharedMod)
            else ->
                EntryAvatar(title = entry.avatarLabel.ifEmpty { entry.displayTitle }, size = 40.dp)
        }
    }
}

private val HubColorAccess   = Color(0xFF8B6BE8)
private val HubColorFinance  = Color(0xFF26C485)
private val HubColorNotes    = Color(0xFFFFB946)
private val HubColorIdentity = Color(0xFF5B9CF6)
private val HubColorArchive  = Color(0xFF90A4AE)

@Composable
private fun VaultSelectorTitle(
    activeVaultId:         String,
    availableVaults:       List<VaultMetadata>,
    expanded:              Boolean,
    isActiveVaultReadOnly: Boolean = false,
    isFreeUser:            Boolean = false,
    onToggle:              () -> Unit,
    onSelect:              (String) -> Unit,
    onManageVaults:        () -> Unit
) {
    val activeVault = availableVaults.firstOrNull { it.id == activeVaultId }
    val displayName = when {
        activeVault?.id == VaultMetadata.PRIMARY_VAULT_ID -> stringResource(R.string.vault_name_primary)
        activeVault?.id == VaultMetadata.PRIVATE_VAULT_ID -> stringResource(R.string.vault_name_private)
        activeVault == null -> when {
            activeVaultId.startsWith(VaultMetadata.INHERITED_PREFIX) ->
                stringResource(R.string.vault_name_inherited)
            else -> stringResource(R.string.vault_hub_title)
        }
        else -> activeVault.name
    }
    val hasMultiple = availableVaults.size > 1
    val titleColor = sectionTitleColor()

    if (!hasMultiple) {
        Column(horizontalAlignment = Alignment.Start) {
            TextoDeUnaLinea(
                text       = displayName,
                style      = MaterialTheme.typography.titleLarge,
                color      = titleColor
            )
            if (isActiveVaultReadOnly) {
                TextoDeUnaLinea(
                    text  = stringResource(R.string.vault_read_only_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = dangerColor()
                )
            }
        }
        return
    }

    Box {
        Row(
            modifier          = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeVault?.isInherited == true) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text       = stringResource(R.string.vault_hub_inherited_from_label),
                        style      = MaterialTheme.typography.titleLarge,
                        color      = titleColor,
                        maxLines   = 1
                    )
                    Text(
                        text     = activeVault.ownerEmail,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    if (isActiveVaultReadOnly) {
                        TextoDeUnaLinea(
                            text  = stringResource(R.string.vault_read_only_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = dangerColor()
                        )
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text       = displayName,
                        style      = MaterialTheme.typography.titleLarge,
                        color      = titleColor,
                        maxLines   = 1
                    )
                    if (isActiveVaultReadOnly) {
                        TextoDeUnaLinea(
                            text  = stringResource(R.string.vault_read_only_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = dangerColor()
                        )
                    }
                }
            }
            Spacer(Modifier.width(2.dp))
            Icon(
                imageVector        = Icons.Default.ExpandMore,
                contentDescription = null,
                tint               = titleColor,
                modifier           = Modifier.size(23.dp)
            )
        }

        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = onToggle,
            containerColor   = MaterialTheme.colorScheme.surface,
            modifier         = Modifier.widthIn(min = ANCHO_MENU_BOVEDAS)
        ) {
            availableVaults.forEach { vault ->
                val isActive = vault.id == activeVaultId
                val isVaultReadOnly =
                    (isFreeUser && !vault.isInherited &&
                    vault.id != VaultMetadata.PRIMARY_VAULT_ID)
                DropdownMenuItem(
                    text = {
                        val vaultDisplayName = when {
                            vault.id == VaultMetadata.PRIMARY_VAULT_ID -> stringResource(R.string.vault_name_primary)
                            vault.id == VaultMetadata.PRIVATE_VAULT_ID -> stringResource(R.string.vault_name_private)
                            else                                       -> vault.name
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isActive) {
                                    Icon(
                                        imageVector        = Icons.Default.Check,
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                } else {
                                    Spacer(Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text       = vaultDisplayName,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (isActive) MaterialTheme.colorScheme.primary
                                                 else MaterialTheme.colorScheme.onSurface,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis,
                                    modifier   = Modifier.weight(1f, fill = false)
                                )
                            }
                            if (isVaultReadOnly) {
                                Text(
                                    text  = stringResource(R.string.vault_read_only_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = dangerColor()
                                )
                            }
                        }
                    },
                    onClick = { onSelect(vault.id) }
                )
            }
            HorizontalDivider(thickness = 0.5.dp)
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Folder,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.vaults_manage))
                    }
                },
                onClick = { onToggle(); onManageVaults() }
            )
        }
    }
}

private val ANCHO_MENU_BOVEDAS = 250.dp

@Composable
private fun VaultItemDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 62.dp, end = 20.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun VaultGroupHeader(title: String, accentColor: Color = MaterialTheme.colorScheme.primary, count: Int? = null) {
    var targetCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(count) { targetCount = count ?: 0 }
    val animatedCount by animateIntAsState(
        targetValue   = targetCount,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label         = "groupCountAnim"
    )
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.SemiBold)) {
                append(title.uppercase())
            }
            if (count != null) {
                withStyle(SpanStyle(color = accentColor)) {
                    append("  ·  $animatedCount")
                }
            }
        },
        style    = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp)
    )
}

@Composable
private fun VaultCategoryItem(
    icon:        ImageVector,
    title:       String,
    count:       Int?,
    onClick:     () -> Unit,
    modifier:    Modifier = Modifier,
    locked:      Boolean = false,
    isReadOnly:  Boolean = false,
    accentColor: Color   = MaterialTheme.colorScheme.primary,
    onAddClick:  (() -> Unit)? = null
) {
    val isEmpty = !locked && !isReadOnly && count == 0 && onAddClick == null
    val isZero  = !locked && count != null && count == 0
    val effectiveRowClick: () -> Unit = when {
        isReadOnly && (count == null || count == 0) -> ({})
        !locked && count == 0 && onAddClick != null -> onAddClick
        else -> onClick
    }

    var targetCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(count) { targetCount = count ?: 0 }
    val animatedCount by animateIntAsState(
        targetValue   = targetCount,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label         = "countAnim"
    )

    val effectiveIconColor = when {
        locked -> MaterialTheme.colorScheme.onSurfaceVariant
        isZero -> accentColor.copy(alpha = 0.4f)
        else   -> accentColor
    }
    val iconBgColor = when {
        locked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f)
        isZero -> accentColor.copy(alpha = 0.06f)
        else   -> accentColor.copy(alpha = 0.14f)
    }
    val titleColor = when {
        locked -> MaterialTheme.colorScheme.onSurfaceVariant
        isZero -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else   -> MaterialTheme.colorScheme.onSurface
    }
    val addSquareColor = remember(accentColor) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(accentColor.toArgb(), hsv)
        hsv[1] = (hsv[1] * 0.85f).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * 0.85f).coerceIn(0f, 1f)
        val alpha255 = (accentColor.alpha * 255f).toInt().coerceIn(0, 255)
        Color(AndroidColor.HSVToColor(alpha255, hsv))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = effectiveRowClick)
            .padding(start = 16.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = effectiveIconColor,
                modifier           = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text       = title,
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            color      = titleColor,
            modifier   = Modifier.weight(1f)
        )
        when {
            locked  -> Icon(
                imageVector        = Icons.Default.Lock,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(16.dp)
            )
            isEmpty -> Text(
                text       = "+",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = accentColor.copy(alpha = 0.4f)
            )
            count != null -> {
                if (onAddClick != null && !isReadOnly) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset(x = (-5).dp)
                            .pointerInput(Unit) { detectTapGestures { onAddClick() } }
                            .size(25.76.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(addSquareColor)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(14.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                }
                Text(
                    text       = animatedCount.toString(),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor
                )
            }
        }
    }
}

@Composable
private fun VaultHealthCard(
    weakCount:        Int,
    mediumCount:      Int,
    strongCount:      Int,
    onStrengthClick:  (String) -> Unit = {}
) {
    val rawAnalyzedCount = weakCount + mediumCount + strongCount

    val weakFracFinal   = if (rawAnalyzedCount > 0) weakCount.toFloat()   / rawAnalyzedCount else 0f
    val mediumFracFinal = if (rawAnalyzedCount > 0) mediumCount.toFloat() / rawAnalyzedCount else 0f
    val strongFracFinal = if (rawAnalyzedCount > 0) strongCount.toFloat() / rawAnalyzedCount else 0f

    var targetAnalyzed by remember { mutableIntStateOf(0) }
    var targetScale    by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(rawAnalyzedCount) {
        targetAnalyzed = rawAnalyzedCount
        if (rawAnalyzedCount > 0) targetScale = 1f
    }
    val analyzedCount by animateIntAsState(
        targetValue   = targetAnalyzed,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label         = "analyzedCountAnim"
    )
    val animScale by animateFloatAsState(
        targetValue   = targetScale,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label         = "barScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 7.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        if (rawAnalyzedCount == 1) {
            Text(
                text       = stringResource(R.string.hub_health_card_title_one),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (rawAnalyzedCount > 1) {
            val titleMany  = stringResource(R.string.hub_health_card_title_many)
            val titleParts = titleMany.split("%d")
            Text(
                text = buildAnnotatedString {
                    append(titleParts.getOrElse(0) { "" })
                    withStyle(SpanStyle(color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.White else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append(analyzedCount.toString())
                    }
                    append(titleParts.getOrElse(1) { "" })
                },
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))

        if (rawAnalyzedCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(animScale)
                        .fillMaxHeight()
                ) {
                    if (weakFracFinal   > 0f) Box(Modifier.weight(weakFracFinal).fillMaxHeight().background(Color(0xFFE53935)))
                    if (mediumFracFinal > 0f) Box(Modifier.weight(mediumFracFinal).fillMaxHeight().background(Color(0xFFFFB300)))
                    if (strongFracFinal > 0f) Box(Modifier.weight(strongFracFinal).fillMaxHeight().background(Color(0xFF43A047)))
                }
            }
            Spacer(Modifier.height(12.dp))
            if (weakCount   > 0) HealthStatRow(Color(0xFFE53935), stringResource(R.string.filter_critical), weakCount,   onClick = { onStrengthClick("WEAK")   })
            if (weakCount   > 0 && mediumCount > 0) Spacer(Modifier.height(6.dp))
            if (mediumCount > 0) HealthStatRow(Color(0xFFFFB300), stringResource(R.string.filter_medium),   mediumCount, onClick = { onStrengthClick("MEDIUM") })
            if (mediumCount > 0 && strongCount > 0) Spacer(Modifier.height(6.dp))
            if (strongCount > 0) HealthStatRow(Color(0xFF43A047), stringResource(R.string.filter_safe),     strongCount, onClick = { onStrengthClick("STRONG") })
        } else {
            Text(
                text       = stringResource(R.string.vault_no_passwords_to_analyze),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HealthStatRow(color: Color, label: String, count: Int, onClick: () -> Unit = {}) {
    var targetCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(count) { targetCount = count }
    val animatedCount by animateIntAsState(
        targetValue   = targetCount,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label         = "healthCountAnim"
    )
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text       = animatedCount.toString(),
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}

@Composable
private fun WhatsNewBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Icon(Icons.Default.Cloud, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(text = stringResource(R.string.whats_new_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            WhatsNewFeatureRow(Icons.Default.Backup, stringResource(R.string.whats_new_drive_title), stringResource(R.string.whats_new_drive_body))
            Spacer(Modifier.height(20.dp))
            WhatsNewFeatureRow(Icons.Default.Restore, stringResource(R.string.whats_new_restore_title), stringResource(R.string.whats_new_restore_body))
            Spacer(Modifier.height(28.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(10.dp), colors = appButtonColors()) {
                Text(text = stringResource(R.string.whats_new_close), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun WhatsNewFeatureRow(icon: ImageVector, title: String, body: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp).padding(top = 2.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text(text = body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
