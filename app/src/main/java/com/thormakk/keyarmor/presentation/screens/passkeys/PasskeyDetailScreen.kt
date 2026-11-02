@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
package com.thormakk.keyarmor.presentation.screens.passkeys

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.screens.settings.contrastRatio
import com.thormakk.keyarmor.ui.theme.SuccessGreen
import com.thormakk.keyarmor.ui.theme.SuccessGreenBright
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
import com.thormakk.keyarmor.presentation.screens.bank.DetailRow
import com.thormakk.keyarmor.presentation.screens.notes.NoteOptionsSheet
import com.thormakk.keyarmor.presentation.screens.vault.ServiceAwareAvatar
import com.thormakk.keyarmor.presentation.util.LocalSearchQuery
import com.thormakk.keyarmor.presentation.util.highlightText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.TextoQueSeAjusta
import com.thormakk.keyarmor.presentation.components.justified

private val MergedCardRadius     = 14.dp
private val MergedCardStartInset = 48.dp
private val MergedCardEndInset   = 15.dp

@Suppress("DEPRECATION")
@Composable
fun PasskeyDetailScreen(
    onBack:                  () -> Unit,
    onEdit:                  (String) -> Unit,
    onViewCredential:        (String) -> Unit,
    onDeleted:               () -> Unit,
    onArchived:              () -> Unit = {},
    onPlansClick:            () -> Unit = {},
    viewModel:               PasskeyDetailViewModel = hiltViewModel(),
    sharedTransitionScope:   SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState               by viewModel.uiState.collectAsStateWithLifecycle()
    val vaultEntries          by viewModel.vaultEntries.collectAsStateWithLifecycle()
    val availableTags         by viewModel.availableTags.collectAsStateWithLifecycle()
    val isFreeUser            by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val pinLimitReached       by viewModel.pinLimitReached.collectAsStateWithLifecycle()
    val isActiveVaultReadOnly by viewModel.isActiveVaultReadOnly.collectAsStateWithLifecycle()
    val otherVaults           by viewModel.otherVaults.collectAsStateWithLifecycle()
    val searchQuery    = LocalSearchQuery.current
    val highlightColor = MaterialTheme.colorScheme.primary
    val snackbarHost   = remember { SnackbarHostState() }
    val pinMsg         = stringResource(R.string.pin_limit_reached)

    LaunchedEffect(uiState.snackbar) {
        uiState.snackbar?.let { snackbarHost.showSnackbar(it); viewModel.dismissSnackbar() }
    }
    LaunchedEffect(pinLimitReached) {
        if (pinLimitReached) {
            snackbarHost.showSnackbar(pinMsg)
            viewModel.onPinLimitDismissed()
        }
    }

    val context  = LocalContext.current
    val activity = context as? FragmentActivity

    var showOptionsSheet             by remember { mutableStateOf(false) }
    var showDeleteDialog             by remember { mutableStateOf(false) }
    var protectedNotice              by remember { mutableStateOf<Int?>(null) }
    var showUnlockDialog             by remember { mutableStateOf(false) }
    var showProtectPremium           by remember { mutableStateOf(false) }
    var showMoreMenu                 by remember { mutableStateOf(false) }
    var showArchiveLockedDialog      by remember { mutableStateOf(false) }
    var showVaultSheet               by remember { mutableStateOf(false) }
    var showRemoveAttributionDialog  by remember { mutableStateOf(false) }
    var vaultActionIsMove       by remember { mutableStateOf(true) }

    val deleteCallback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) {
                viewModel.delete(); onDeleted()
            }
            override fun onAuthenticationFailed() {}
            override fun onAuthenticationError(code: Int, msg: CharSequence) {}
        }
    }
    val biometricPrompt = remember(activity) {
        activity?.let { BiometricPrompt(it, ContextCompat.getMainExecutor(it), deleteCallback) }
    }
    val deleteBioTitle    = stringResource(R.string.passkey_delete_biometric_title)
    val deleteBioSubtitle = stringResource(R.string.passkey_delete_biometric_subtitle)
    val cancelLabel       = stringResource(R.string.action_cancel)
    val deletePromptInfo = remember(deleteBioTitle, deleteBioSubtitle, cancelLabel) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(deleteBioTitle)
            .setSubtitle(deleteBioSubtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText(cancelLabel)
            .build()
    }

    fun onDeleteTapped() {
        if (uiState.isProtected) { protectedNotice = R.string.protect_locked_delete; return }
        val canAuth = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS && biometricPrompt != null)
            biometricPrompt.authenticate(deletePromptInfo)
        else
            showDeleteDialog = true
    }

    val linkedEntryName = remember(uiState.passkey?.linkedPasswordId, vaultEntries) {
        vaultEntries.firstOrNull { it.id == uiState.passkey?.linkedPasswordId }?.title ?: ""
    }

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
                    stringResource(if (uiState.passkey?.isHidden == true) R.string.hidden_entries_delete_confirm_title else R.string.dialog_delete_entry_title),
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    if (uiState.passkey?.isHidden == true) stringResource(R.string.hidden_entries_delete_confirm_message)
                    else stringResource(R.string.passkey_delete_confirm_message, uiState.passkey?.displayName ?: ""),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; viewModel.delete(); onDeleted() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(if (uiState.passkey?.isHidden == true) R.string.action_delete else R.string.action_move_to_trash))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showArchiveLockedDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveLockedDialog = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.plans_feature_archive),
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground)
                }
            },
            text = {
                Text(stringResource(R.string.premium_locked_dialog_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                Button(
                    onClick = { showArchiveLockedDialog = false; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.premium_gate_cta)) }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveLockedDialog = false }) {
                    Text(stringResource(R.string.action_understood), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    if (showOptionsSheet) {
        NoteOptionsSheet(
            initialCategory = uiState.category,
            initialTags     = uiState.tags,
            availableTags   = availableTags,
            onSave          = { cat, tags ->
                viewModel.onCategoryAndTagsSave(cat, tags)
                showOptionsSheet = false
            },
            onDismiss       = { showOptionsSheet = false },
            onAddTag        = viewModel::addTag,
            onRenameTag     = viewModel::renameTag,
            onDeleteTag     = viewModel::deleteTag,
            tagConflictKey  = viewModel::tagConflictKey,
            canCreateTags   = !isFreeUser,
            onUpgradeClick  = onPlansClick
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost, modifier = Modifier.padding(bottom = 40.dp)) },
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
                    title = { TextoQueSeAjusta(stringResource(R.string.passkey_detail_title),
                        style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                        fontWeight = FontWeight.SemiBold,
                        color      = sectionTitleColor()) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                        IconButton(onClick = viewModel::togglePin) {
                            Icon(
                                imageVector = if (uiState.isPinned) Icons.Default.PushPin
                                              else Icons.Default.PushPin,
                                contentDescription = if (uiState.isPinned) stringResource(R.string.action_unpin) else stringResource(R.string.action_pin),
                                tint = if (uiState.isPinned) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!isActiveVaultReadOnly || uiState.isProtected) {
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
                        if (!isActiveVaultReadOnly) {
                            IconButton(onClick = {
                                if (uiState.isProtected) protectedNotice = R.string.protect_locked_edit
                                else uiState.passkey?.let { onEdit(it.credentialId) }
                            }) {
                                Icon(Icons.Default.Edit, stringResource(R.string.action_edit),
                                    tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Default.MoreVert, stringResource(R.string.action_more_options),
                                    tint = MaterialTheme.colorScheme.onSurface)
                            }
                            DropdownMenu(
                                expanded         = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                containerColor   = MaterialTheme.colorScheme.background
                            ) {
                                if (!isActiveVaultReadOnly) {
                                    DropdownMenuItem(
                                        text    = { Text(stringResource(R.string.menu_categories_tags)) },
                                        onClick = { showMoreMenu = false; showOptionsSheet = true },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null) }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text        = { Text(stringResource(R.string.action_move_to)) },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, null) },
                                        onClick     = { showMoreMenu = false; vaultActionIsMove = true; showVaultSheet = true }
                                    )
                                }
                                DropdownMenuItem(
                                    text        = { Text(stringResource(R.string.action_copy_to)) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick     = { showMoreMenu = false; vaultActionIsMove = false; showVaultSheet = true }
                                )
                                if (!isActiveVaultReadOnly) {
                                    HorizontalDivider()
                                    if (uiState.passkey?.isHidden != true) {
                                        DropdownMenuItem(
                                            text        = { Text(stringResource(R.string.menu_archive)) },
                                            leadingIcon = { Icon(Icons.Default.Archive, null) },
                                            onClick     = {
                                                showMoreMenu = false
                                                if (isFreeUser) showArchiveLockedDialog = true
                                                else { viewModel.archive(); onArchived() }
                                            }
                                        )
                                    }
                                }
                                DropdownMenuItem(
                                    text    = { Text(stringResource(R.string.menu_delete),
                                        color = dangerColor()) },
                                    onClick = { showMoreMenu = false; onDeleteTapped() },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Delete, null,
                                            tint = dangerColor())
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

        if (uiState.isLoading || uiState.passkey == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        val pk  = uiState.passkey!!
        val fmt = remember { SimpleDateFormat("d MMM yyyy  HH:mm", Locale.getDefault()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 7.dp, vertical = 12.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val mergedCardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val mergedGapColor  = MaterialTheme.colorScheme.background
            val hasNotes           = pk.notes.isNotBlank()
            val hasLinkedPassword  = pk.linkedPasswordId.isNotBlank() && linkedEntryName.isNotBlank()
            val lastFieldKey = when {
                hasLinkedPassword -> "linked"
                hasNotes          -> "notes"
                else              -> null
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
                Column(modifier = Modifier.padding(20.dp)) {
                    val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState      = rememberSharedContentState("passkey_avatar_${pk.credentialId}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    } else Modifier

                    Row(verticalAlignment = Alignment.Top) {
                        ServiceAwareAvatar(
                            title    = pk.displayName,
                            url      = "https://${pk.rpId}",
                            modifier = avatarModifier
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = highlightText(pk.displayName, searchQuery, highlightColor),
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                            if (pk.accountLabel.isNotBlank()) {
                                Text(
                                    text  = highlightText(pk.accountLabel, searchQuery, highlightColor),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color    = MaterialTheme.colorScheme.outlineVariant
                    )

                    PasskeyMetaRow(
                        icon  = Icons.Default.Language,
                        label = stringResource(R.string.passkey_field_domain),
                        value = pk.rpId
                    )

                    PasskeyMetaRow(
                        icon  = Icons.Default.CalendarToday,
                        label = stringResource(R.string.passkey_field_created),
                        value = fmt.format(Date(pk.createdAt))
                    )

                    if (pk.lastUsedAt != null) {
                        PasskeyMetaRow(
                            icon  = Icons.Default.AccessTime,
                            label = stringResource(R.string.passkey_field_last_used),
                            value = fmt.format(Date(pk.lastUsedAt))
                        )
                    }
                }
                pk.inheritedFrom?.let { ownerEmail ->
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
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication        = null
                            ) { showRemoveAttributionDialog = true }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text     = stringResource(R.string.inherited_from_label, ownerEmail),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
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

            if (pk.notes.isNotBlank()) {
                DetailRow(
                    label     = stringResource(R.string.passkey_field_notes),
                    value     = pk.notes,
                    multiline = true,
                    leading   = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                    shape     = entryItemShape(isFirst = false, isLast = lastFieldKey == "notes", radius = MergedCardRadius)
                )
            }
            if (hasNotes && lastFieldKey != "notes") {
                EntryGroupDivider(
                    startInset = MergedCardStartInset,
                    endInset   = MergedCardEndInset,
                    cardColor  = mergedCardColor,
                    gapColor   = mergedGapColor
                )
            }

            if (pk.linkedPasswordId.isNotBlank() && linkedEntryName.isNotBlank()) {
                Surface(
                    shape    = entryItemShape(isFirst = false, isLast = true, radius = MergedCardRadius),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Key, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.passkey_field_linked_password),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(5.dp))
                            Text(linkedEntryName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(
                            onClick  = { onViewCredential(pk.linkedPasswordId) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, null,
                                tint     = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            }

            if (showRemoveAttributionDialog) {
                val ownerEmail = pk.inheritedFrom
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
                                border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
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

            FlowRow(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp)
            ) {
                if (uiState.category != EntryCategory.SIN_CATEGORIZAR)
                    InfoChip(uiState.category.label, color = chipCategoryColor())
                uiState.tags.forEach { tag -> InfoChip(tag, color = chipTagColor()) }
                if (!isActiveVaultReadOnly) {
                    Box(
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
            }


            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
                            modifier   = Modifier.weight(1f),
                            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
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
                                checked         = uiState.passkey?.isHidden == true,
                                onCheckedChange = viewModel::saveHiddenOnly,
                                enabled         = uiState.passkey?.isHidden == true || viewModel.canHideNewEntries
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

@Composable
private fun PasskeyMetaRow(
    icon:  androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    val searchQuery    = LocalSearchQuery.current
    val highlightColor = MaterialTheme.colorScheme.primary
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp))
        Text(highlightText(value, searchQuery, highlightColor),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface)
    }

}

@Composable
private fun MoveToVaultBottomSheet(
    vaults:    List<com.thormakk.keyarmor.domain.model.VaultMetadata>,
    isMove:    Boolean,
    onSelect:  (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Text(text = if (isMove) stringResource(R.string.action_move_to) else stringResource(R.string.action_copy_to),
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        Text(text = stringResource(R.string.move_copy_select_vault), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        if (vaults.isEmpty()) {
            Text(text = "—", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(20.dp))
        } else {
            vaults.forEach { vault ->
                Row(modifier = Modifier.fillMaxWidth()
                        .clickable { onSelect(vault.id) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(imageVector = if (vault.isInherited) Icons.Default.Shield
                                       else if (vault.isPrivate) Icons.Default.Lock
                                       else Icons.Default.Folder,
                        contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp))
                    Text(text = vault.name, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
