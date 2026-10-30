@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.vault

import com.thormakk.keyarmor.presentation.util.ServiceIconProvider
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.screens.settings.contrastRatio
import com.thormakk.keyarmor.ui.theme.SuccessGreen
import com.thormakk.keyarmor.ui.theme.SuccessGreenBright
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Visibility
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.presentation.components.EntryGroupDivider
import com.thormakk.keyarmor.presentation.components.InfoChip
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.chipCategoryColor
import com.thormakk.keyarmor.presentation.components.chipTagColor
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.entryItemShape
import com.thormakk.keyarmor.presentation.components.infoColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.bank.EntryMetadata
import com.thormakk.keyarmor.presentation.util.BiometricGate
import com.thormakk.keyarmor.presentation.util.formatDateLocalized
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.presentation.util.LocalSearchQuery
import com.thormakk.keyarmor.presentation.util.TotpGenerator
import com.thormakk.keyarmor.presentation.util.highlightText
import com.thormakk.keyarmor.presentation.util.openUrl
import com.thormakk.keyarmor.presentation.util.rememberBiometricGate
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import com.thormakk.keyarmor.ui.theme.PasswordStrengthFair
import com.thormakk.keyarmor.ui.theme.PasswordStrengthGood
import com.thormakk.keyarmor.ui.theme.PasswordStrengthStrong
import com.thormakk.keyarmor.ui.theme.PasswordStrengthWeak
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

private val MergedCardRadius = 14.dp

private val MergedCardStartInset = 48.dp
private val MergedCardEndInset   = 15.dp

@Composable
private fun MergedFieldRow(
    label:     String,
    value:     String,
    shape:     RoundedCornerShape,
    modifier:  Modifier = Modifier,
    multiline: Boolean  = false,
    onCopy:    (() -> Unit)? = null,
    leading:   @Composable (() -> Unit)? = null
) {
    if (value.isBlank()) return
    val searchQuery    = LocalSearchQuery.current
    val highlightColor = MaterialTheme.colorScheme.primary
    Surface(
        shape    = shape,
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(
                start  = 16.dp,
                end    = if (onCopy != null) 4.dp else 16.dp,
                top    = 14.dp,
                bottom = 14.dp
            ),
            verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically
        ) {
            if (leading != null) {
                Box(modifier = Modifier.padding(top = if (multiline) 2.dp else 0.dp)) {
                    leading()
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text  = highlightText(value, searchQuery, highlightColor),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector        = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.action_copy),
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordDetailScreen(
    onBack:              () -> Unit,
    onEdit:              (String) -> Unit,
    onDeleted:           () -> Unit,
    onCloneClick:        (String) -> Unit = {},
    onShareClick:        (String) -> Unit = {},
    onAttachmentsClick:  (String) -> Unit = {},
    onHistoryClick:      (String) -> Unit = {},
    onPlansClick:        () -> Unit = {},
    onPasskeyClick:      (String) -> Unit = {},
    viewModel:           AddEditEntryViewModel = hiltViewModel(),
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState              by viewModel.uiState.collectAsStateWithLifecycle()
    val availableTags        by viewModel.availableTags.collectAsStateWithLifecycle()
    val isFreeUser           by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val pinLimitReached      by viewModel.pinLimitReached.collectAsStateWithLifecycle()
    val isHubPinned          by viewModel.isHubPinned.collectAsStateWithLifecycle()
    val hubPinLimitReached   by viewModel.hubPinLimitReached.collectAsStateWithLifecycle()
    val hubPinMsg            = stringResource(R.string.hub_pin_limit_toast)
    val isPasswordDuplicated by viewModel.isPasswordDuplicated.collectAsStateWithLifecycle()
    val linkedTwoFactor      by viewModel.linkedTwoFactor.collectAsStateWithLifecycle()
    val linkedPasskey        by viewModel.linkedPasskey.collectAsStateWithLifecycle()
    val hibpState            by viewModel.hibpState.collectAsStateWithLifecycle()
    val isReadOnly           by viewModel.isReadOnly.collectAsStateWithLifecycle()

    val clipCtx      = LocalContext.current
    val clipClear  = LocalClipboardClearManager.current
    val clipContext = LocalContext.current

    val otherVaults      by viewModel.otherVaults.collectAsStateWithLifecycle()
    var showDeleteDialog          by remember { mutableStateOf(false) }
    var protectedNotice           by remember { mutableStateOf<Int?>(null) }
    var showUnlockDialog          by remember { mutableStateOf(false) }
    var showProtectPremium        by remember { mutableStateOf(false) }
    var showMenu                 by remember { mutableStateOf(false) }
    var showOptionsSheet         by remember { mutableStateOf(false) }
    var showVaultSheet           by remember { mutableStateOf(false) }
    var vaultActionIsMove        by remember { mutableStateOf(true) }
    var premiumFeature           by remember { mutableStateOf<String?>(null) }
    var passwordVisible          by remember { mutableStateOf(false) }
    var countdownText            by remember { mutableStateOf("") }
    var showRemoveAttributionDialog by remember { mutableStateOf(false) }

    val labelAttachments = stringResource(R.string.premium_attachments)
    val labelHistory     = stringResource(R.string.plans_feat_password_history)
    val labelShare       = stringResource(R.string.menu_share)
    val labelArchive     = stringResource(R.string.plans_feature_archive)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    val pinMsg            = stringResource(R.string.pin_limit_reached)

    LaunchedEffect(uiState.success) { if (uiState.success) onDeleted() }
    LaunchedEffect(uiState.password) {
        if (uiState.password.isNotBlank()) viewModel.checkHibp(uiState.password)
    }
    LaunchedEffect(pinLimitReached) {
        if (pinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(pinMsg) }
            viewModel.onPinLimitDismissed()
        }
    }
    LaunchedEffect(hubPinLimitReached) {
        if (hubPinLimitReached) {
            scope.launch { snackbarHostState.showSnackbar(hubPinMsg) }
            viewModel.clearHubPinLimitReached()
        }
    }

    val passGate: BiometricGate = rememberBiometricGate(
        section              = BiometricPrefs.SECTION_PASSWORDS,
        prefs                = viewModel.biometricPrefs,
        verifyMasterPassword = viewModel::verifyMasterPassword
    )
    val gateSessionExpiry by passGate.sessionExpiresAt
    LaunchedEffect(gateSessionExpiry) {
        if (gateSessionExpiry > 0L) {
            val remaining = gateSessionExpiry - System.currentTimeMillis()
            if (remaining > 0) delay(remaining)
            passwordVisible = false
        }
    }
    LaunchedEffect(passwordVisible, gateSessionExpiry) {
        if (!passwordVisible || gateSessionExpiry <= 0L) { countdownText = ""; return@LaunchedEffect }
        while (true) {
            val remaining = gateSessionExpiry - System.currentTimeMillis()
            if (remaining <= 0) { countdownText = "0:00"; break }
            val totalSecs = (remaining / 1000).toInt()
            countdownText = "${totalSecs / 60}:${(totalSecs % 60).toString().padStart(2, '0')}"
            delay(500L)
        }
    }
    val biometricEnabled by remember {
        derivedStateOf { viewModel.biometricPrefs.isSectionEnabled(BiometricPrefs.SECTION_PASSWORDS) }
    }

    var totpCode       by remember { mutableStateOf<TotpGenerator.TotpCode?>(null) }
    LaunchedEffect(linkedTwoFactor) {
        if (linkedTwoFactor == null || !linkedTwoFactor!!.hasTotp) { totpCode = null; return@LaunchedEffect }
        while (true) {
            totpCode = TotpGenerator.generate(
                secret    = linkedTwoFactor!!.secret,
                digits    = linkedTwoFactor!!.digits,
                period    = linkedTwoFactor!!.period,
                algorithm = linkedTwoFactor!!.algorithm
            )
            delay(500L)
        }
    }

    val ageToday    = stringResource(R.string.password_age_today)
    val ageDaysFmt  = stringResource(R.string.password_age_days)
    val ageMonthsFmt= stringResource(R.string.password_age_months)
    val ageYearsFmt = stringResource(R.string.password_age_years)
    val passwordAgeTs = uiState.passwordChangedAt ?: uiState.createdAt
    val passwordAgeText = remember(passwordAgeTs) {
        if (passwordAgeTs <= 0L) return@remember null
        val days = ((System.currentTimeMillis() - passwordAgeTs) / 86_400_000L)
            .toInt().coerceAtLeast(0)
        when {
            days < 1   -> ageToday
            days < 30  -> ageDaysFmt.format(days)
            days < 365 -> ageMonthsFmt.format(days / 30)
            else       -> ageYearsFmt.format(days / 365)
        }
    }
    val passwordAgeStale = remember(passwordAgeTs) {
        if (passwordAgeTs <= 0L) false
        else (System.currentTimeMillis() - passwordAgeTs) / 86_400_000L >= 90
    }

    val passwordStrength = remember(uiState.password) {
        PasswordStrengthCalculator.calculate(uiState.password)
    }
    val strengthLabelRes = when (passwordStrength) {
        0    -> R.string.strength_very_weak
        1    -> R.string.strength_weak
        2    -> R.string.strength_fair
        3    -> R.string.strength_strong
        else -> R.string.strength_very_strong
    }
    val strengthColor = when (passwordStrength) {
        0, 1 -> PasswordStrengthWeak
        2    -> PasswordStrengthFair
        3    -> PasswordStrengthGood
        else -> PasswordStrengthStrong
    }

    val locale = LocalConfiguration.current.locales[0]
    val expiryFormatted = remember(uiState.expiryDate, locale) {
        uiState.expiryDate?.let {
            formatDateLocalized(it, locale, utc = true)
        }
    }
    val isExpired = uiState.expiryDate != null && uiState.expiryDate!! < System.currentTimeMillis()
    val expiryDangerColor = dangerColor()

    protectedNotice?.let { msgRes ->
        AlertDialog(
            onDismissRequest = { protectedNotice = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_locked_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(msgRes), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { protectedNotice = null }) { Text(stringResource(R.string.action_understood)) }
            }
        )
    }
    if (showUnlockDialog) {
        AlertDialog(
            onDismissRequest = { showUnlockDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_unlock_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(if (viewModel.canProtectNew) R.string.protect_unlock_body else R.string.protect_unlock_body_free),
                color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showUnlockDialog = false; viewModel.setProtected(false) }) {
                    Text(stringResource(R.string.lock_action_unlock))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }
    if (showProtectPremium) {
        AlertDialog(
            onDismissRequest = { showProtectPremium = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.protect_premium_title), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground) },
            text  = { Text(stringResource(R.string.protect_premium_body), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                TextButton(onClick = { showProtectPremium = false; onPlansClick() }) {
                    Text(stringResource(R.string.plans_upgrade_cta))
                }
            },
            dismissButton = {
                TextButton(onClick = { showProtectPremium = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text       = stringResource(if (uiState.isHidden) R.string.hidden_entries_delete_confirm_title else R.string.dialog_delete_entry_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text  = stringResource(if (uiState.isHidden) R.string.hidden_entries_delete_confirm_message else R.string.dialog_delete_entry_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; viewModel.delete() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(if (uiState.isHidden) R.string.action_delete else R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showOptionsSheet) {
        EntryOptionsSheet(
            initialCategory = uiState.category,
            initialTags     = uiState.tags,
            availableTags   = availableTags,
            onSave          = { cat, tags ->
                viewModel.saveMetaOnly(cat, tags)
                showOptionsSheet = false
            },
            onDismiss     = { showOptionsSheet = false },
            onAddTag       = { viewModel.addTag(it) },
            onRenameTag    = { id, name -> viewModel.renameTag(id, name) },
            onDeleteTag    = { viewModel.deleteTag(it) },
            tagConflictKey = viewModel::tagConflictKey,
            canCreateTags  = !isFreeUser,
            onUpgradeClick = onPlansClick
        )
    }

    premiumFeature?.let { featureName ->
        AlertDialog(
            onDismissRequest = { premiumFeature = null },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(featureName, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                }
            },
            text = {
                Text(stringResource(R.string.premium_locked_dialog_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                Button(
                    onClick = { premiumFeature = null; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { premiumFeature = null }) {
                    Text(stringResource(R.string.action_understood),
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showVaultSheet) {
        MoveToVaultBottomSheet(
            vaults    = otherVaults,
            isMove    = vaultActionIsMove,
            onSelect  = { targetId ->
                showVaultSheet = false
                if (vaultActionIsMove) viewModel.moveEntryTo(targetId)
                else                   viewModel.copyEntryTo(targetId)
            },
            onDismiss = { showVaultSheet = false }
        )
    }

    if (showRemoveAttributionDialog) {
        val ownerEmail = uiState.inheritedFrom
        if (ownerEmail != null) {
            AlertDialog(
                onDismissRequest = { showRemoveAttributionDialog = false },
                containerColor   = MaterialTheme.colorScheme.background,
                title = {
                    Text(
                        text       = stringResource(R.string.remove_attribution_title),
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                },
                text = {
                    Text(
                        text  = stringResource(R.string.remove_attribution_body, ownerEmail),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
                },
                confirmButton = {
                    Button(
                        onClick = { showRemoveAttributionDialog = false; viewModel.removeInheritedFrom() },
                        shape   = RoundedCornerShape(8.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = MaterialTheme.colorScheme.primary
                        ),
                        border  = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) { Text(stringResource(R.string.action_remove_attribution)) }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveAttributionDialog = false }) {
                        Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    }

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
                        TextoDeUnaLinea(
                            text       = uiState.title.ifBlank { stringResource(R.string.field_title) },
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
                        IconButton(onClick = { viewModel.togglePin() }) {
                            Icon(
                                imageVector        = Icons.Default.PushPin,
                                contentDescription = null,
                                tint               = if (uiState.isPinned) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!isReadOnly) IconButton(onClick = {
                            if (uiState.isProtected) protectedNotice = R.string.protect_locked_edit else onEdit(uiState.id)
                        }) {
                            Icon(
                                imageVector        = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.action_edit),
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector        = Icons.Default.MoreVert,
                                    contentDescription = null
                                )
                            }
                            DropdownMenu(
                                expanded         = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor   = MaterialTheme.colorScheme.background
                            ) {
                                DropdownMenuItem(
                                    text        = { Text(if (isHubPinned) stringResource(R.string.hub_unpin_from_hub) else stringResource(R.string.hub_pin_to_hub)) },
                                    leadingIcon = { Icon(if (isHubPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, null) },
                                    onClick     = { showMenu = false; viewModel.toggleHubPin() }
                                )
                                HorizontalDivider()
                                if (!isReadOnly) {
                                    DropdownMenuItem(
                                        text        = { Text(stringResource(R.string.menu_categories_tags)) },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null) },
                                        onClick     = { showMenu = false; showOptionsSheet = true }
                                    )
                                    DropdownMenuItem(
                                        text        = { Text(labelAttachments) },
                                        leadingIcon = {
                                            Icon(Icons.Default.AttachFile, null,
                                                modifier = Modifier.size(20.dp))
                                        },
                                        onClick = { showMenu = false; onAttachmentsClick(uiState.id) }
                                    )
                                    DropdownMenuItem(
                                        text        = { Text(stringResource(R.string.menu_clone)) },
                                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                        onClick     = { showMenu = false; onCloneClick(uiState.id) }
                                    )
                                    DropdownMenuItem(
                                        text         = { Text(labelShare) },
                                        leadingIcon  = { Icon(Icons.Default.Share, null) },
                                        onClick = {
                                            showMenu = false
                                            onShareClick(uiState.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text         = { Text(labelHistory) },
                                        leadingIcon  = { Icon(Icons.Default.History, null) },
                                        onClick = {
                                            showMenu = false
                                            onHistoryClick(uiState.id)
                                        }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text        = { Text(stringResource(R.string.action_move_to)) },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, null) },
                                        onClick     = { showMenu = false; vaultActionIsMove = true; showVaultSheet = true }
                                    )
                                }
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.action_copy_to)) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick     = { showMenu = false; vaultActionIsMove = false; showVaultSheet = true }
                                )
                                if (!isReadOnly) {
                                    HorizontalDivider()
                                    if (!uiState.isHidden) {
                                        DropdownMenuItem(
                                            text         = { Text(stringResource(R.string.menu_archive)) },
                                            leadingIcon  = { Icon(Icons.Default.Archive, null) },
                                            trailingIcon = if (isFreeUser) ({
                                                Icon(Icons.Default.Lock, null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint     = MaterialTheme.colorScheme.primary)
                                            }) else null,
                                            onClick      = {
                                                showMenu = false
                                                if (isFreeUser) premiumFeature = labelArchive else viewModel.archive()
                                            }
                                        )
                                    }
                                }
                                DropdownMenuItem(
                                    text        = {
                                        Text(stringResource(R.string.menu_delete),
                                            color = dangerColor())
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Delete, null,
                                            tint = dangerColor())
                                    },
                                    onClick = {
                                        showMenu = false
                                        if (uiState.isProtected) protectedNotice = R.string.protect_locked_delete
                                        else showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 7.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val mergedGapColor  = MaterialTheme.colorScheme.background
            val hasUsername = uiState.username.isNotBlank()
            val hasEmail    = uiState.email.isNotBlank()
            val hasPassword = uiState.password.isNotBlank()
            val hasUrls     = uiState.urls.isNotEmpty()
            val hasTotp     = linkedTwoFactor != null && totpCode != null
            val hasPasskey  = linkedPasskey != null
            val hasExpiry   = expiryFormatted != null
            val hasNotes    = uiState.notes.isNotBlank()
            val lastFieldKey = when {
                hasNotes    -> "notes"
                hasExpiry   -> "expiry"
                hasPasskey  -> "passkey"
                hasTotp     -> "totp"
                hasUrls     -> "url_last"
                hasPassword -> "password"
                hasEmail    -> "email"
                hasUsername -> "username"
                else        -> null
            }
            val heroIsLast = lastFieldKey == null

            Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape          = entryItemShape(isFirst = true, isLast = heroIsLast, radius = MergedCardRadius),
                color          = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier       = Modifier.fillMaxWidth()
            ) {
                Column {
                Row(
                    modifier          = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState      = rememberSharedContentState("entry_avatar_${uiState.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    } else Modifier

                    ServiceAwareAvatar(
                        title    = uiState.title,
                        url      = ServiceIconProvider.bestUrl(uiState.urls),
                        size     = 48.dp,
                        modifier = avatarModifier
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = uiState.title.ifBlank { stringResource(R.string.field_title) },
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                        if (expiryFormatted != null) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint               = if (isExpired) expiryDangerColor
                                                         else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier           = Modifier.size(14.dp)
                                )
                                Text(
                                    text  = expiryFormatted,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isExpired) expiryDangerColor
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (uiState.isEditing && (!isReadOnly || uiState.isProtected)) {
                        val greenLock = if (contrastRatio(MaterialTheme.colorScheme.background, SuccessGreen) < 3f)
                            SuccessGreenBright else SuccessGreen
                        IconButton(onClick = {
                            when {
                                uiState.isProtected      -> showUnlockDialog = true
                                viewModel.canProtectNew  -> viewModel.setProtected(true)
                                else                     -> showProtectPremium = true
                            }
                        }) {
                            Icon(
                                imageVector        = if (uiState.isProtected) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = stringResource(if (uiState.isProtected) R.string.protect_cd_unlock else R.string.protect_cd_lock),
                                tint               = if (uiState.isProtected) greenLock else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { if (!isReadOnly) viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector        = if (uiState.isFavorite) Icons.Default.Star
                                                 else Icons.Default.StarBorder,
                            contentDescription = stringResource(R.string.action_favorite),
                            tint               = if (uiState.isFavorite) MaterialTheme.colorScheme.primary
                                                 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                uiState.inheritedFrom?.let { ownerEmail ->
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null
                            ) { showRemoveAttributionDialog = true }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text  = stringResource(R.string.inherited_from_label, ownerEmail),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector        = Icons.Default.Close,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier           = Modifier.size(12.dp)
                        )
                    }
                }
                }
            }
            if (!heroIsLast) {
                EntryGroupDivider(
                    startInset = 0.dp,
                    endInset   = 0.dp,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )
            }

            MergedFieldRow(
                label   = stringResource(R.string.field_username),
                value   = uiState.username,
                shape   = entryItemShape(isFirst = false, isLast = lastFieldKey == "username", radius = MergedCardRadius),
                leading = {
                    Icon(Icons.Default.Person, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                },
                onCopy = if (uiState.username.isNotBlank()) {
                    { ClipboardUtil.copySensitive(clipCtx, uiState.username); clipClear.scheduleClear() }
                } else null
            )
            if (hasUsername && lastFieldKey != "username") {
                EntryGroupDivider(
                    startInset = MergedCardStartInset,
                    endInset   = MergedCardEndInset,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )
            }

            MergedFieldRow(
                label   = stringResource(R.string.field_email),
                value   = uiState.email,
                shape   = entryItemShape(isFirst = false, isLast = lastFieldKey == "email", radius = MergedCardRadius),
                leading = {
                    Icon(Icons.Default.Email, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                },
                onCopy = if (uiState.email.isNotBlank()) {
                    { ClipboardUtil.copySensitive(clipCtx, uiState.email); clipClear.scheduleClear() }
                } else null
            )
            if (hasEmail && lastFieldKey != "email") {
                EntryGroupDivider(
                    startInset = MergedCardStartInset,
                    endInset   = MergedCardEndInset,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )
            }

            if (uiState.password.isNotBlank()) {
                val maskedPassword = "•".repeat(minOf(uiState.password.length, 20))
                Surface(
                    shape    = entryItemShape(isFirst = false, isLast = lastFieldKey == "password", radius = MergedCardRadius),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(
                            start  = 16.dp,
                            end    = 4.dp,
                            top    = 10.dp,
                            bottom = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Lock,
                            contentDescription = null,
                            tint               = strengthColor,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text  = stringResource(R.string.field_password),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text  = stringResource(strengthLabelRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = strengthColor
                                )
                            }
                            if (hibpState is HibpCheckState.Pwned) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text  = "☠ ${stringResource(R.string.hibp_pwned, (hibpState as HibpCheckState.Pwned).count)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = dangerColor()
                                )
                            }
                            if (passwordAgeText != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text  = "📅 $passwordAgeText",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (passwordAgeStale) Color(0xFFFF9800)
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text  = if (passwordVisible) uiState.password else maskedPassword,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isPasswordDuplicated) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text  = stringResource(R.string.detail_password_reused),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                        if (countdownText.isNotEmpty()) {
                            Text(
                                text  = countdownText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick  = {
                                if (passwordVisible) passwordVisible = false
                                else passGate.launch { passwordVisible = true }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector        = if (passwordVisible) Icons.Default.VisibilityOff
                                                     else Icons.Default.Visibility,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = if (biometricEnabled) 0.7f else 1f
                                ),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick  = {
                                if (passwordVisible) {
                                    ClipboardUtil.copySensitive(clipContext, uiState.password)
                                    clipClear.scheduleClear()
                                } else {
                                    passGate.launch {
                                        ClipboardUtil.copySensitive(clipContext, uiState.password)
                                        clipClear.scheduleClear()
                                    }
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.action_copy),
                                tint               = MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (biometricEnabled) 0.7f else 1f
                                ),
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            if (hasPassword && lastFieldKey != "password") {
                EntryGroupDivider(
                    startInset = MergedCardStartInset,
                    endInset   = MergedCardEndInset,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )
            }

            if (uiState.urls.isNotEmpty()) {
                val context = LocalContext.current
                uiState.urls.forEachIndexed { index, url ->
                    val isLastUrlRow = index == uiState.urls.lastIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(entryItemShape(
                                isFirst = false,
                                isLast  = isLastUrlRow && lastFieldKey == "url_last",
                                radius  = MergedCardRadius
                            ))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { context.openUrl(url) }
                            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Language,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = stringResource(R.string.field_url),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text  = url,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (index == 0 && uiState.urls.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text     = stringResource(R.string.label_primary_url),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (!(isLastUrlRow && lastFieldKey == "url_last")) {
                        EntryGroupDivider(
                            startInset = MergedCardStartInset,
                            endInset   = MergedCardEndInset,
                            cardColor  = mergedCardColor,
                            gapColor   = mergedGapColor
                        )
                    }
                }
            }

            if (linkedTwoFactor != null && totpCode != null) {
                val code = totpCode!!
                Surface(
                    shape    = entryItemShape(isFirst = false, isLast = lastFieldKey == "totp", radius = MergedCardRadius),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(
                            start  = 16.dp,
                            end    = 4.dp,
                            top    = 14.dp,
                            bottom = 14.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Key,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text  = linkedTwoFactor!!.name.ifBlank { linkedTwoFactor!!.issuer },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LinearProgressIndicator(
                                    progress         = { 1f - code.progress },
                                    modifier         = Modifier
                                        .weight(1f)
                                        .height(3.dp),
                                    color            = when {
                                        code.secondsRemaining <= 5  -> dangerColor()
                                        code.secondsRemaining <= 10 -> Color(0xFFFF9800)
                                        else                        -> MaterialTheme.colorScheme.primary
                                    },
                                    trackColor       = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeCap        = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                Text(
                                    text  = "${code.secondsRemaining}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text  = TotpGenerator.formatCode(code.code),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick  = {
                                ClipboardUtil.copySensitive(clipContext, code.code)
                                clipClear.scheduleClear()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.action_copy),
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            if (hasTotp && lastFieldKey != "totp") {
                EntryGroupDivider(
                    startInset = MergedCardStartInset,
                    endInset   = MergedCardEndInset,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )
            }

            if (linkedPasskey != null) {
                Surface(
                    shape    = entryItemShape(isFirst = false, isLast = lastFieldKey == "passkey", radius = MergedCardRadius),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(
                            start  = 16.dp,
                            end    = 4.dp,
                            top    = 14.dp,
                            bottom = 14.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = stringResource(R.string.linked_passkey_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = linkedPasskey!!.customAccountLabel.ifBlank {
                                    linkedPasskey!!.rpName.ifBlank { linkedPasskey!!.userName }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick  = { onPasskeyClick(linkedPasskey!!.credentialId) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            if (hasPasskey && lastFieldKey != "passkey") {
                EntryGroupDivider(
                    startInset = MergedCardStartInset,
                    endInset   = MergedCardEndInset,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )
            }

            if (expiryFormatted != null) {
                MergedFieldRow(
                    label   = stringResource(R.string.entry_expiry_date),
                    value   = expiryFormatted,
                    shape   = entryItemShape(isFirst = false, isLast = lastFieldKey == "expiry", radius = MergedCardRadius),
                    leading = {
                        Icon(Icons.Default.CalendarToday, null,
                            tint     = if (isExpired) expiryDangerColor
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp))
                    }
                )
                if (hasExpiry && lastFieldKey != "expiry") {
                    EntryGroupDivider(
                        startInset = MergedCardStartInset,
                        endInset   = MergedCardEndInset,
                        cardColor  = mergedCardColor,
                        gapColor   = mergedGapColor
                    )
                }
            }

            MergedFieldRow(
                label     = stringResource(R.string.field_notes),
                value     = uiState.notes,
                shape     = entryItemShape(isFirst = false, isLast = lastFieldKey == "notes", radius = MergedCardRadius),
                multiline = true,
                leading   = {
                    Icon(Icons.AutoMirrored.Filled.Notes, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                },
                onCopy = if (uiState.notes.isNotBlank()) {
                    { ClipboardUtil.copySensitive(clipCtx, uiState.notes); clipClear.scheduleClear() }
                } else null
            )
            }

            EntryMetadata(createdAt = uiState.createdAt, updatedAt = uiState.updatedAt)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp)
            ) {
                if (uiState.category != EntryCategory.SIN_CATEGORIZAR)
                    InfoChip(stringResource(uiState.category.labelRes), color = chipCategoryColor())
                uiState.tags.forEach { tag -> InfoChip(tag, color = chipTagColor()) }
                if (!isReadOnly) Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .clickable { showOptionsSheet = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text  = "+ ${stringResource(R.string.options_sheet_tags)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Surface(
                shape          = RoundedCornerShape(12.dp),
                color          = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier       = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text       = stringResource(R.string.visibility_section_title).uppercase(),
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = infoColor(),
                        modifier   = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp)
                    )
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Luggage,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text       = stringResource(R.string.travel_mode_safe_label),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            modifier   = Modifier.weight(1f)
                        )
                        Switch(
                            checked         = uiState.isTravelSafe,
                            onCheckedChange = viewModel::saveTravelSafeOnly
                        )
                    }
                    if (viewModel.isHiddenFeatureEnabled) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier
                                    .padding(top = 2.dp)
                                    .size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = stringResource(R.string.hidden_entries_toggle_label),
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text  = stringResource(R.string.hidden_entries_toggle_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked         = uiState.isHidden,
                                onCheckedChange = viewModel::saveHiddenOnly,
                                enabled         = uiState.isHidden || viewModel.canHideNewEntries
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
