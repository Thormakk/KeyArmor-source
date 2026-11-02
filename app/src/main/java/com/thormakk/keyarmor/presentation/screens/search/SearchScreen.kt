@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.search

import com.thormakk.keyarmor.presentation.util.ServiceIconProvider
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import com.thormakk.keyarmor.domain.model.VaultMetadata
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.CustomEntry
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.IdentityDocument
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.Reminder
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.Vehicle
import com.thormakk.keyarmor.presentation.components.ChipData
import com.thormakk.keyarmor.presentation.components.ChipsRowWithFunnel
import com.thormakk.keyarmor.presentation.components.FuzzySectionDivider
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.truncateTag
import com.thormakk.keyarmor.presentation.screens.bank.BankAwareAvatar
import com.thormakk.keyarmor.presentation.screens.creditcard.CreditCardAwareAvatar
import com.thormakk.keyarmor.presentation.screens.crypto.CryptoAwareAvatar
import com.thormakk.keyarmor.presentation.screens.timemachine.triggerLabel
import com.thormakk.keyarmor.presentation.screens.vault.ActiveFilterChip
import com.thormakk.keyarmor.presentation.screens.vault.CategoryChip
import com.thormakk.keyarmor.presentation.screens.vault.ChipCategoryColor
import com.thormakk.keyarmor.presentation.screens.vault.ChipTagColor
import com.thormakk.keyarmor.presentation.screens.vault.EntryAvatar
import com.thormakk.keyarmor.presentation.screens.vault.FiltersSheet
import com.thormakk.keyarmor.presentation.screens.vault.QuickFilter
import com.thormakk.keyarmor.presentation.screens.vault.ServiceAwareAvatar
import com.thormakk.keyarmor.presentation.screens.vault.tagIcon
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.LocalSearchQuery
import com.thormakk.keyarmor.presentation.util.highlightText
import java.util.Date
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun SearchScreen(
    onPlansClick:      () -> Unit = {},
    onBack:                () -> Unit,
    onPasswordClick:       (String, String) -> Unit,
    onBankAccountClick:    (String, String) -> Unit,
    onCreditCardClick:     (String, String) -> Unit,
    onSecureNoteClick:     (String, String) -> Unit,
    onCustomEntryClick:    (String, String) -> Unit,
    onTwoFactorClick:          (String, String) -> Unit = { _, _ -> },
    onCryptoWalletClick:       (String, String) -> Unit = { _, _ -> },
    onPasskeyClick:            (String, String) -> Unit = { _, _ -> },
    onIdentityDocumentClick:   (String, String) -> Unit = { _, _ -> },
    onVehicleClick:            (String, String) -> Unit = { _, _ -> },
    onReminderClick:           (String) -> Unit = {},
    onTrashClick:              (String) -> Unit = {},
    onArchiveClick:            (String) -> Unit = {},
    onBackupResultClick:       (String, String) -> Unit = { _, _ -> },
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()
    val autoLockManager = LocalAutoLockManager.current
    val quickFilters   by viewModel.quickFilters.collectAsStateWithLifecycle()
    val availableTags  by viewModel.availableTags.collectAsStateWithLifecycle()
    val isFreeUser     by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val tagCountMap    by viewModel.tagCountMap.collectAsStateWithLifecycle()
    val categoryCountMap by viewModel.categoryCountMap.collectAsStateWithLifecycle()
    val recentEntries  by viewModel.recentEntries.collectAsStateWithLifecycle()
    val searchBackupsEnabled by viewModel.searchBackupsEnabled.collectAsStateWithLifecycle()
    val isLoadingBackups     by viewModel.isLoadingBackups.collectAsStateWithLifecycle()
    val backupResults        by viewModel.backupResults.collectAsStateWithLifecycle()

    var showFiltersSheet by remember { mutableStateOf(false) }
    var showArchiveLockedDialog by remember { mutableStateOf(false) }
    val rawOnArchiveClick = onArchiveClick
    val guardedArchiveClick: (String) -> Unit = { id ->
        if (isFreeUser) showArchiveLockedDialog = true else rawOnArchiveClick(id)
    }
    val focusRequester   = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
                    Text(
                        stringResource(R.string.plans_feature_archive),
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
                    onClick = { showArchiveLockedDialog = false; onPlansClick() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
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

    CompositionLocalProvider(LocalSearchQuery provides uiState.query) {
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
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                    },
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            stringResource(R.string.search_screen_title),
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
        },
        containerColor      = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            TextField(
                value         = uiState.query,
                onValueChange = { autoLockManager?.recordInteraction(); viewModel.onQueryChange(it) },
                placeholder   = { Text(stringResource(R.string.search_field_placeholder)) },
                leadingIcon   = {
                    Icon(Icons.Default.Search, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon  = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
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
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .focusRequester(focusRequester)
                    .coachMarkAnchor("search", "field")
            )

            val activeFilterCount =
                (if (uiState.selectedCategory != null) 1 else 0) + uiState.selectedTags.size +
                (if (uiState.onlyFavorites) 1 else 0) + (if (uiState.onlyWithAttachments) 1 else 0) +
                (if (uiState.onlyProtected) 1 else 0)

            var renderedChipKeys by remember(quickFilters) { mutableStateOf(quickFilters.map { it.key }.toSet()) }
            val filterChips = quickFilters.map { qf ->
                when (qf) {
                    is QuickFilter.Category -> ChipData(
                        key           = qf.key,
                        label         = stringResource(qf.category.labelRes),
                        selected      = uiState.selectedCategory == qf.category,
                        selectedColor = ChipCategoryColor,
                        onClick       = { viewModel.onCategorySelected(if (uiState.selectedCategory == qf.category) null else qf.category) },
                        count         = categoryCountMap[qf.category] ?: 0
                    )
                    is QuickFilter.Tag -> ChipData(
                        key           = qf.key,
                        label         = qf.tag.truncateTag(),
                        selected      = uiState.selectedTags.contains(qf.tag),
                        selectedColor = ChipTagColor,
                        onClick       = { viewModel.onTagToggled(qf.tag) },
                        count         = tagCountMap[qf.tag] ?: 0,
                        icon          = tagIcon(qf.tag)
                    )
                }
            }
            ChipsRowWithFunnel(
                chips                    = filterChips,
                activeFilterCount        = activeFilterCount,
                onFunnelClick            = { showFiltersSheet = true },
                funnelContentDescription = stringResource(R.string.filter_short),
                onVisibleKeysChanged     = { renderedChipKeys = it },
                modifier                 = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp)
            ) { data ->
                CategoryChip(
                    label         = data.label,
                    selected      = data.selected,
                    selectedColor = data.selectedColor,
                    onClick       = data.onClick,
                    leadingIcon   = data.icon,
                    count         = data.count
                )
            }

            val visibleQuickKeys = renderedChipKeys
            val hiddenActiveFilters: List<QuickFilter> = buildList {
                uiState.selectedCategory?.let { cat ->
                    val qf = QuickFilter.Category(cat)
                    if (qf.key !in visibleQuickKeys) add(qf)
                }
                uiState.selectedTags.forEach { tag ->
                    val qf = QuickFilter.Tag(tag)
                    if (qf.key !in visibleQuickKeys) add(qf)
                }
            }
            val hasAnyActiveFilter = hiddenActiveFilters.isNotEmpty() ||
                uiState.onlyFavorites || uiState.onlyWithAttachments || uiState.onlyProtected
            if (hasAnyActiveFilter) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.onlyFavorites) {
                        ActiveFilterChip(
                            label       = stringResource(R.string.filter_favorites),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onRemove    = { viewModel.onFavoritesToggle() }
                        )
                    }
                    if (uiState.onlyWithAttachments) {
                        ActiveFilterChip(
                            label       = stringResource(R.string.filter_attachments),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onRemove    = { viewModel.onAttachmentFilterToggle() }
                        )
                    }
                    if (uiState.onlyProtected) {
                        ActiveFilterChip(
                            label       = stringResource(R.string.filter_protected),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onRemove    = { viewModel.onProtectedFilterToggle(false) }
                        )
                    }
                    hiddenActiveFilters.forEach { qf ->
                        ActiveFilterChip(
                            label    = when (qf) {
                                is QuickFilter.Category -> stringResource(qf.category.labelRes)
                                is QuickFilter.Tag      -> qf.tag.truncateTag()
                            },
                            onRemove = {
                                when (qf) {
                                    is QuickFilter.Category -> viewModel.onCategorySelected(null)
                                    is QuickFilter.Tag      -> viewModel.onTagToggled(qf.tag)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val hasExact       = uiState.totalCount > 0
            val hasFuzzy       = uiState.fuzzy.totalCount > 0
            val hasBackupsInfo = searchBackupsEnabled && (isLoadingBackups || backupResults.isNotEmpty())

            when {
                uiState.isInitial -> {
                    if (recentEntries.isNotEmpty()) {
                        RecentEntriesSection(
                            entries                 = recentEntries,
                            onPasswordClick         = onPasswordClick,
                            onBankAccountClick      = onBankAccountClick,
                            onCreditCardClick       = onCreditCardClick,
                            onSecureNoteClick       = onSecureNoteClick,
                            onCustomEntryClick      = onCustomEntryClick,
                            onCryptoWalletClick     = onCryptoWalletClick,
                            onIdentityDocumentClick = onIdentityDocumentClick,
                            onVehicleClick          = onVehicleClick,
                            onTwoFactorClick        = onTwoFactorClick,
                            onRecordAccess          = viewModel::recordRecentAccess
                        )
                    } else {
                        EmptyHint(text = stringResource(R.string.search_empty_hint))
                    }
                }
                !hasExact && !hasFuzzy && !hasBackupsInfo -> {
                    EmptyHint(text = stringResource(R.string.search_no_results))
                }
                else -> {
                    val primaryVaultName = stringResource(R.string.vault_name_primary)
                    val privateVaultName = stringResource(R.string.vault_name_private)
                    fun vaultLabel(ref: SearchVaultRef): String = when (ref.id) {
                        VaultMetadata.PRIMARY_VAULT_ID -> primaryVaultName
                        VaultMetadata.PRIVATE_VAULT_ID -> privateVaultName
                        else                           -> ref.name
                    }
                    fun hasExactInVault(vId: String): Boolean =
                        uiState.passwords.any { it.vaultId == vId } ||
                        uiState.bankAccounts.any { it.vaultId == vId } ||
                        uiState.creditCards.any { it.vaultId == vId } ||
                        uiState.secureNotes.any { it.vaultId == vId } ||
                        uiState.customEntries.any { it.vaultId == vId } ||
                        uiState.twoFactorAccounts.any { it.vaultId == vId } ||
                        uiState.cryptoWallets.any { it.vaultId == vId } ||
                        uiState.passkeys.any { it.vaultId == vId } ||
                        uiState.identityDocuments.any { it.vaultId == vId } ||
                        uiState.vehicles.any { it.vaultId == vId }
                    fun hasFuzzyInVault(vId: String): Boolean =
                        uiState.fuzzy.passwords.any { it.vaultId == vId } ||
                        uiState.fuzzy.bankAccounts.any { it.vaultId == vId } ||
                        uiState.fuzzy.creditCards.any { it.vaultId == vId } ||
                        uiState.fuzzy.secureNotes.any { it.vaultId == vId } ||
                        uiState.fuzzy.customEntries.any { it.vaultId == vId } ||
                        uiState.fuzzy.twoFactorAccounts.any { it.vaultId == vId } ||
                        uiState.fuzzy.cryptoWallets.any { it.vaultId == vId } ||
                        uiState.fuzzy.passkeys.any { it.vaultId == vId } ||
                        uiState.fuzzy.identityDocuments.any { it.vaultId == vId } ||
                        uiState.fuzzy.vehicles.any { it.vaultId == vId }

                    fun LazyListScope.emitExactForVault(vId: String) {
                        val pw = uiState.passwords.filter { it.vaultId == vId }
                        if (pw.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_passwords), count = pw.size) }
                            items(pw, key = { "p_${it.id}" }) { entry ->
                                PasswordResultRow(entry = entry, onClick = {
                                    when {
                                        entry.isDeleted          -> onTrashClick(entry.id)
                                        entry.archivedAt != null -> guardedArchiveClick(entry.id)
                                        else                     -> { viewModel.recordRecentAccess(entry.id, HubEntryType.PASSWORD); onPasswordClick(entry.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val acc = uiState.bankAccounts.filter { it.vaultId == vId }
                        if (acc.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_bank_accounts), count = acc.size) }
                            items(acc, key = { "b_${it.id}" }) { account ->
                                BankAccountResultRow(account = account, onClick = {
                                    when {
                                        account.isDeleted          -> onTrashClick(account.id)
                                        account.archivedAt != null -> guardedArchiveClick(account.id)
                                        else                       -> { viewModel.recordRecentAccess(account.id, HubEntryType.BANK_ACCOUNT); onBankAccountClick(account.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val cards = uiState.creditCards.filter { it.vaultId == vId }
                        if (cards.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_credit_cards), count = cards.size) }
                            items(cards, key = { "c_${it.id}" }) { card ->
                                CreditCardResultRow(card = card, onClick = {
                                    when {
                                        card.isDeleted          -> onTrashClick(card.id)
                                        card.archivedAt != null -> guardedArchiveClick(card.id)
                                        else                    -> { viewModel.recordRecentAccess(card.id, HubEntryType.CREDIT_CARD); onCreditCardClick(card.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val notes = uiState.secureNotes.filter { it.vaultId == vId }
                        if (notes.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_secure_notes), count = notes.size) }
                            items(notes, key = { "n_${it.id}" }) { note ->
                                SecureNoteResultRow(note = note, onClick = {
                                    when {
                                        note.isDeleted          -> onTrashClick(note.id)
                                        note.archivedAt != null -> guardedArchiveClick(note.id)
                                        else                    -> { viewModel.recordRecentAccess(note.id, HubEntryType.SECURE_NOTE); onSecureNoteClick(note.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val custom = uiState.customEntries.filter { it.vaultId == vId }
                        if (custom.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_custom_entries), count = custom.size) }
                            items(custom, key = { "x_${it.id}" }) { entry ->
                                CustomEntryResultRow(entry = entry, onClick = {
                                    when {
                                        entry.isDeleted          -> onTrashClick(entry.id)
                                        entry.archivedAt != null -> guardedArchiveClick(entry.id)
                                        else                     -> { viewModel.recordRecentAccess(entry.id, HubEntryType.CUSTOM_ENTRY); onCustomEntryClick(entry.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val twoFa = uiState.twoFactorAccounts.filter { it.vaultId == vId }
                        if (twoFa.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.twofa_section_search), count = twoFa.size) }
                            items(twoFa, key = { "t_${it.id}" }) { account ->
                                TwoFactorResultRow(account = account, onClick = {
                                    when {
                                        account.isDeleted          -> onTrashClick(account.id)
                                        account.archivedAt != null -> guardedArchiveClick(account.id)
                                        else                       -> onTwoFactorClick(account.id, uiState.query)
                                    }
                                })
                            }
                        }
                        val crypto = uiState.cryptoWallets.filter { it.vaultId == vId }
                        if (crypto.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_crypto_wallets), count = crypto.size) }
                            items(crypto, key = { "w_${it.id}" }) { wallet ->
                                CryptoWalletResultRow(wallet = wallet, onClick = {
                                    when {
                                        wallet.isDeleted          -> onTrashClick(wallet.id)
                                        wallet.archivedAt != null -> guardedArchiveClick(wallet.id)
                                        else                      -> { viewModel.recordRecentAccess(wallet.id, HubEntryType.CRYPTO_WALLET); onCryptoWalletClick(wallet.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val pks = uiState.passkeys.filter { it.vaultId == vId }
                        if (pks.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_passkeys), count = pks.size) }
                            items(pks, key = { "pk_${it.credentialId}" }) { passkey ->
                                PasskeyResultRow(passkey = passkey, onClick = {
                                    if (passkey.isDeleted) onTrashClick(passkey.credentialId) else onPasskeyClick(passkey.credentialId, uiState.query)
                                })
                            }
                        }
                        val ids = uiState.identityDocuments.filter { it.vaultId == vId }
                        if (ids.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_identity_documents), count = ids.size) }
                            items(ids, key = { "id_${it.id}" }) { doc ->
                                IdentityDocumentResultRow(doc = doc, onClick = {
                                    when {
                                        doc.isDeleted          -> onTrashClick(doc.id)
                                        doc.archivedAt != null -> guardedArchiveClick(doc.id)
                                        else                   -> { viewModel.recordRecentAccess(doc.id, HubEntryType.IDENTITY_DOCUMENT); onIdentityDocumentClick(doc.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val vhs = uiState.vehicles.filter { it.vaultId == vId }
                        if (vhs.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_vehicles), count = vhs.size) }
                            items(vhs, key = { "v_${it.id}" }) { vehicle ->
                                VehicleResultRow(vehicle = vehicle, onClick = {
                                    when {
                                        vehicle.isDeleted          -> onTrashClick(vehicle.id)
                                        vehicle.archivedAt != null -> guardedArchiveClick(vehicle.id)
                                        else                       -> { viewModel.recordRecentAccess(vehicle.id, HubEntryType.VEHICLE); onVehicleClick(vehicle.id, uiState.query) }
                                    }
                                })
                            }
                        }
                    }

                    fun LazyListScope.emitFuzzyForVault(vId: String) {
                        val pw = uiState.fuzzy.passwords.filter { it.vaultId == vId }
                        if (pw.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_passwords), count = pw.size) }
                            items(pw, key = { "fp_${it.id}" }) { entry ->
                                PasswordResultRow(entry = entry, onClick = {
                                    when {
                                        entry.isDeleted          -> onTrashClick(entry.id)
                                        entry.archivedAt != null -> guardedArchiveClick(entry.id)
                                        else                     -> { viewModel.recordRecentAccess(entry.id, HubEntryType.PASSWORD); onPasswordClick(entry.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val acc = uiState.fuzzy.bankAccounts.filter { it.vaultId == vId }
                        if (acc.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_bank_accounts), count = acc.size) }
                            items(acc, key = { "fb_${it.id}" }) { account ->
                                BankAccountResultRow(account = account, onClick = {
                                    when {
                                        account.isDeleted          -> onTrashClick(account.id)
                                        account.archivedAt != null -> guardedArchiveClick(account.id)
                                        else                       -> { viewModel.recordRecentAccess(account.id, HubEntryType.BANK_ACCOUNT); onBankAccountClick(account.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val cards = uiState.fuzzy.creditCards.filter { it.vaultId == vId }
                        if (cards.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_credit_cards), count = cards.size) }
                            items(cards, key = { "fc_${it.id}" }) { card ->
                                CreditCardResultRow(card = card, onClick = {
                                    when {
                                        card.isDeleted          -> onTrashClick(card.id)
                                        card.archivedAt != null -> guardedArchiveClick(card.id)
                                        else                    -> { viewModel.recordRecentAccess(card.id, HubEntryType.CREDIT_CARD); onCreditCardClick(card.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val notes = uiState.fuzzy.secureNotes.filter { it.vaultId == vId }
                        if (notes.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_secure_notes), count = notes.size) }
                            items(notes, key = { "fn_${it.id}" }) { note ->
                                SecureNoteResultRow(note = note, onClick = {
                                    when {
                                        note.isDeleted          -> onTrashClick(note.id)
                                        note.archivedAt != null -> guardedArchiveClick(note.id)
                                        else                    -> { viewModel.recordRecentAccess(note.id, HubEntryType.SECURE_NOTE); onSecureNoteClick(note.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val custom = uiState.fuzzy.customEntries.filter { it.vaultId == vId }
                        if (custom.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_custom_entries), count = custom.size) }
                            items(custom, key = { "fx_${it.id}" }) { entry ->
                                CustomEntryResultRow(entry = entry, onClick = {
                                    when {
                                        entry.isDeleted          -> onTrashClick(entry.id)
                                        entry.archivedAt != null -> guardedArchiveClick(entry.id)
                                        else                     -> { viewModel.recordRecentAccess(entry.id, HubEntryType.CUSTOM_ENTRY); onCustomEntryClick(entry.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val twoFa = uiState.fuzzy.twoFactorAccounts.filter { it.vaultId == vId }
                        if (twoFa.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.twofa_section_search), count = twoFa.size) }
                            items(twoFa, key = { "ft_${it.id}" }) { account ->
                                TwoFactorResultRow(account = account, onClick = {
                                    when {
                                        account.isDeleted          -> onTrashClick(account.id)
                                        account.archivedAt != null -> guardedArchiveClick(account.id)
                                        else                       -> onTwoFactorClick(account.id, uiState.query)
                                    }
                                })
                            }
                        }
                        val crypto = uiState.fuzzy.cryptoWallets.filter { it.vaultId == vId }
                        if (crypto.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_crypto_wallets), count = crypto.size) }
                            items(crypto, key = { "fw_${it.id}" }) { wallet ->
                                CryptoWalletResultRow(wallet = wallet, onClick = {
                                    when {
                                        wallet.isDeleted          -> onTrashClick(wallet.id)
                                        wallet.archivedAt != null -> guardedArchiveClick(wallet.id)
                                        else                      -> { viewModel.recordRecentAccess(wallet.id, HubEntryType.CRYPTO_WALLET); onCryptoWalletClick(wallet.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val pks = uiState.fuzzy.passkeys.filter { it.vaultId == vId }
                        if (pks.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_passkeys), count = pks.size) }
                            items(pks, key = { "fpk_${it.credentialId}" }) { passkey ->
                                PasskeyResultRow(passkey = passkey, onClick = {
                                    if (passkey.isDeleted) onTrashClick(passkey.credentialId) else onPasskeyClick(passkey.credentialId, uiState.query)
                                })
                            }
                        }
                        val ids = uiState.fuzzy.identityDocuments.filter { it.vaultId == vId }
                        if (ids.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_identity_documents), count = ids.size) }
                            items(ids, key = { "fid_${it.id}" }) { doc ->
                                IdentityDocumentResultRow(doc = doc, onClick = {
                                    when {
                                        doc.isDeleted          -> onTrashClick(doc.id)
                                        doc.archivedAt != null -> guardedArchiveClick(doc.id)
                                        else                   -> { viewModel.recordRecentAccess(doc.id, HubEntryType.IDENTITY_DOCUMENT); onIdentityDocumentClick(doc.id, uiState.query) }
                                    }
                                })
                            }
                        }
                        val vhs = uiState.fuzzy.vehicles.filter { it.vaultId == vId }
                        if (vhs.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_vehicles), count = vhs.size) }
                            items(vhs, key = { "fv_${it.id}" }) { vehicle ->
                                VehicleResultRow(vehicle = vehicle, onClick = {
                                    when {
                                        vehicle.isDeleted          -> onTrashClick(vehicle.id)
                                        vehicle.archivedAt != null -> guardedArchiveClick(vehicle.id)
                                        else                       -> { viewModel.recordRecentAccess(vehicle.id, HubEntryType.VEHICLE); onVehicleClick(vehicle.id, uiState.query) }
                                    }
                                })
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 7.dp, end = 7.dp, bottom = 24.dp)
                    ) {
                        if (!hasExact) {
                            item {
                                NoExactMatchBanner(
                                    text = stringResource(R.string.search_no_exact_match)
                                )
                            }
                        }

                        uiState.vaults.forEach { v ->
                            if (hasExactInVault(v.id)) {
                                item(key = "vh_e_${v.id}") { VaultGroupHeader(name = vaultLabel(v)) }
                                emitExactForVault(v.id)
                            }
                        }
                        if (uiState.reminders.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_reminders), count = uiState.reminders.size) }
                            items(uiState.reminders, key = { "rem_${it.id}" }) { reminder ->
                                ReminderResultRow(reminder = reminder, onClick = { onReminderClick(reminder.id) })
                            }
                        }

                        if (hasFuzzy && hasExact) {
                            item {
                                FuzzySectionDivider(
                                    text = stringResource(R.string.search_similar_results)
                                )
                            }
                        }
                        uiState.vaults.forEach { v ->
                            if (hasFuzzyInVault(v.id)) {
                                item(key = "vh_f_${v.id}") { VaultGroupHeader(name = vaultLabel(v)) }
                                emitFuzzyForVault(v.id)
                            }
                        }
                        if (uiState.fuzzy.reminders.isNotEmpty()) {
                            item { SectionHeader(title = stringResource(R.string.search_section_reminders), count = uiState.fuzzy.reminders.size) }
                            items(uiState.fuzzy.reminders, key = { "frem_${it.id}" }) { reminder ->
                                ReminderResultRow(reminder = reminder, onClick = { onReminderClick(reminder.id) })
                            }
                        }

                        if (searchBackupsEnabled) {
                            item { BackupsSectionHeader(count = backupResults.size, isLoading = isLoadingBackups) }
                            if (isLoadingBackups) {
                                item { BackupsLoadingRow() }
                            } else {
                                items(backupResults, key = { "bk_${it.snapshotId}_${it.entryId}" }) { result ->
                                    BackupResultRow(
                                        result  = result,
                                        onClick = { onBackupResultClick(result.snapshotId, uiState.query) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFiltersSheet) {
        FiltersSheet(
            availableTags      = availableTags,
            selectedCategory   = uiState.selectedCategory,
            selectedTags       = uiState.selectedTags,
            initialFavorites   = uiState.onlyFavorites,
            initialAttachments = uiState.onlyWithAttachments,
            initialProtected   = uiState.onlyProtected,
            onProtectedToggle  = { value -> viewModel.onProtectedFilterToggle(value) },
            onApply            = { category, tags, fav, attach ->
                viewModel.onCategorySelected(category)
                viewModel.onTagsApplied(tags)
                if (fav != uiState.onlyFavorites) viewModel.onFavoritesToggle()
                if (attach != uiState.onlyWithAttachments) viewModel.onAttachmentFilterToggle()
                showFiltersSheet = false
            },
            onClearAll         = {
                viewModel.onCategorySelected(null)
                viewModel.onTagsApplied(emptySet())
                if (uiState.onlyFavorites) viewModel.onFavoritesToggle()
                if (uiState.onlyWithAttachments) viewModel.onAttachmentFilterToggle()
                viewModel.onProtectedFilterToggle(false)
                showFiltersSheet = false
            },
            onDismiss          = { showFiltersSheet = false },
            onUpgradeClick     = onPlansClick,
            onAddTag           = { viewModel.addTag(it) },
            onRenameTag        = { id, name -> viewModel.renameTag(id, name) },
            onDeleteTag        = { viewModel.deleteTag(it) },
            tagConflictKey     = viewModel::tagConflictKey,
            canCreateTags      = !isFreeUser,
            showSearchBackupsOption = !isFreeUser,
            initialSearchBackups    = searchBackupsEnabled,
            onSearchBackupsToggle   = { viewModel.setSearchBackupsEnabled(it) }
        )
    }
    }
}

@Composable
private fun NoExactMatchBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = Icons.Default.Search,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier           = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text  = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier   = Modifier.weight(1f)
            )
            Text(
                text       = stringResource(R.string.search_section_count, count),
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(
            modifier  = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun VaultGroupHeader(name: String) {
    Text(
        text       = name,
        style      = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.primary,
        modifier   = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 2.dp)
    )
}

@Composable
private fun ResultRowBase(
    title:       String,
    subtitle:    String?,
    url:         String  = "",
    statusLabel: String? = null,
    onClick:     () -> Unit
) {
    val searchQuery    = LocalSearchQuery.current
    val highlightColor = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServiceAwareAvatar(title = title, url = url)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = highlightText(title.ifEmpty { "—" }, searchQuery, highlightColor),
                style      = MaterialTheme.typography.bodyLarge,
                color      = MaterialTheme.colorScheme.onBackground,
                maxLines   = 1
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text     = highlightText(subtitle, searchQuery, highlightColor),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    maxLines = 1
                )
            }
        }
        if (statusLabel != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text     = statusLabel,
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun entryStatusLabel(isDeleted: Boolean, isArchived: Boolean): String? = when {
    isDeleted  -> stringResource(R.string.search_status_trash)
    isArchived -> stringResource(R.string.search_status_archived)
    else       -> null
}

@Composable
private fun PasswordResultRow(entry: VaultEntry, onClick: () -> Unit) {
    val sub    = entry.email.ifEmpty { entry.username }.ifEmpty { entry.url }
    val status = entryStatusLabel(entry.isDeleted, entry.archivedAt != null)
    ResultRowBase(title = entry.title, subtitle = sub, url = ServiceIconProvider.bestUrl(entry.urls), statusLabel = status, onClick = onClick)
}

@Composable
private fun BankAccountResultRow(account: BankAccount, onClick: () -> Unit) {
    val bankLabel = account.bankName.ifEmpty { account.alias }
    val detail    = account.accountType.trim()
    val title     = if (detail.isNotEmpty()) "$bankLabel – $detail" else bankLabel
    val sub       = account.swiftBic.takeIf { it.isNotBlank() }
        ?: account.alias.takeIf { it.isNotBlank() }
    val status    = entryStatusLabel(account.isDeleted, account.archivedAt != null)
    ResultRowBase(title = title, subtitle = sub, statusLabel = status, onClick = onClick)
}

@Composable
private fun CreditCardResultRow(card: CreditCard, onClick: () -> Unit) {
    val title  = card.issuer.ifEmpty { card.brand }
    val sub    = listOf(card.brand, card.issuer).filter { it.isNotBlank() && it != title }.joinToString(" · ")
    val status = entryStatusLabel(card.isDeleted, card.archivedAt != null)
    ResultRowBase(title = title, subtitle = sub.ifBlank { null }, statusLabel = status, onClick = onClick)
}

@Composable
private fun SecureNoteResultRow(note: SecureNote, onClick: () -> Unit) {
    val sub    = note.content.lineSequence().firstOrNull { it.isNotBlank() }?.take(60)
    val status = entryStatusLabel(note.isDeleted, note.archivedAt != null)
    ResultRowBase(title = note.title, subtitle = sub, statusLabel = status, onClick = onClick)
}

@Composable
private fun CustomEntryResultRow(entry: CustomEntry, onClick: () -> Unit) {
    val status = entryStatusLabel(entry.isDeleted, entry.archivedAt != null)
    ResultRowBase(title = entry.title, subtitle = null, statusLabel = status, onClick = onClick)
}

@Composable
private fun TwoFactorResultRow(account: TwoFactorAccount, onClick: () -> Unit) {
    val title  = account.issuer.ifEmpty { account.name }
    val sub    = account.accountLabel.takeIf { it.isNotBlank() }
        ?: account.name.takeIf { it.isNotBlank() && it != title }
    val status = entryStatusLabel(account.isDeleted, account.archivedAt != null)
    ResultRowBase(title = title, subtitle = sub, statusLabel = status, onClick = onClick)
}

@Composable
private fun CryptoWalletResultRow(wallet: CryptoWallet, onClick: () -> Unit) {
    val title  = listOf(wallet.network, wallet.asset).filter { it.isNotBlank() }.joinToString(" / ")
        .ifEmpty { wallet.address.take(12).ifEmpty { stringResource(R.string.hub_pinned_fallback_crypto_wallet) } }
    val sub    = wallet.address.takeIf { it.isNotBlank() }?.let {
        if (it.length > 20) it.take(10) + "…" + it.takeLast(8) else it
    }
    val status = entryStatusLabel(wallet.isDeleted, wallet.archivedAt != null)
    ResultRowBase(title = title, subtitle = sub, statusLabel = status, onClick = onClick)
}

@Composable
private fun PasskeyResultRow(passkey: PasskeyCredential, onClick: () -> Unit) {
    val title  = passkey.rpName.ifEmpty { passkey.userName }
    val sub    = passkey.userName.takeIf { it.isNotBlank() && it != title }
    val status = entryStatusLabel(passkey.isDeleted, passkey.archivedAt != null)
    ResultRowBase(title = title, subtitle = sub, statusLabel = status, onClick = onClick)
}

@Composable
private fun IdentityDocumentResultRow(doc: IdentityDocument, onClick: () -> Unit) {
    val title  = doc.holderName.ifEmpty { doc.docType }
    val sub    = doc.docType.takeIf { it.isNotBlank() && it != title }
        ?: doc.number.takeIf { it.isNotBlank() }
    val status = entryStatusLabel(doc.isDeleted, doc.archivedAt != null)
    ResultRowBase(title = title, subtitle = sub, statusLabel = status, onClick = onClick)
}

@Composable
private fun VehicleResultRow(vehicle: Vehicle, onClick: () -> Unit) {
    val title  = listOf(vehicle.make, vehicle.model).filter { it.isNotBlank() }.joinToString(" ")
        .ifEmpty { vehicle.licensePlate.ifEmpty { stringResource(R.string.hub_pinned_fallback_vehicle) } }
    val sub    = vehicle.licensePlate.takeIf { it.isNotBlank() }
    val status = entryStatusLabel(vehicle.isDeleted, vehicle.archivedAt != null)
    ResultRowBase(title = title, subtitle = sub, statusLabel = status, onClick = onClick)
}

@Composable
private fun ReminderResultRow(reminder: Reminder, onClick: () -> Unit) {
    val sub = reminder.note.lineSequence().firstOrNull { it.isNotBlank() }?.take(60)
    ResultRowBase(title = reminder.name, subtitle = sub, onClick = onClick)
}

@Composable
private fun BackupsSectionHeader(count: Int, isLoading: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.History,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text       = stringResource(R.string.search_backups_section_header),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier   = Modifier.weight(1f)
            )
            if (!isLoading && count > 0) {
                Text(
                    text       = stringResource(R.string.search_section_count, count),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
        }
        HorizontalDivider(
            modifier  = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun BackupsLoadingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text  = stringResource(R.string.search_backups_loading),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BackupResultRow(result: BackupSearchResult, onClick: () -> Unit) {
    val searchQuery    = LocalSearchQuery.current
    val highlightColor = MaterialTheme.colorScheme.primary
    val sdf = remember { java.text.SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.History,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = highlightText(result.title.ifEmpty { "—" }, searchQuery, highlightColor),
                style    = MaterialTheme.typography.bodyLarge,
                color    = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                text     = "${triggerLabel(result.trigger)} · ${sdf.format(Date(result.timestamp))}",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun RecentEntriesSection(
    entries:                 List<RecentHubEntry>,
    onPasswordClick:         (String, String) -> Unit,
    onBankAccountClick:      (String, String) -> Unit,
    onCreditCardClick:       (String, String) -> Unit,
    onSecureNoteClick:       (String, String) -> Unit,
    onCustomEntryClick:      (String, String) -> Unit,
    onCryptoWalletClick:     (String, String) -> Unit,
    onIdentityDocumentClick: (String, String) -> Unit,
    onVehicleClick:          (String, String) -> Unit,
    onTwoFactorClick:        (String, String) -> Unit,
    onRecordAccess:          (String, HubEntryType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text  = stringResource(R.string.search_recents_section),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val rows = entries.chunked(4)
        rows.forEach { row ->
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { entry ->
                    RecentEntryCard(
                        entry    = entry,
                        modifier = Modifier.weight(1f),
                        onClick  = {
                            onRecordAccess(entry.id, entry.type)
                            when (entry.type) {
                                HubEntryType.PASSWORD          -> onPasswordClick(entry.id, "")
                                HubEntryType.BANK_ACCOUNT      -> onBankAccountClick(entry.id, "")
                                HubEntryType.CREDIT_CARD       -> onCreditCardClick(entry.id, "")
                                HubEntryType.SECURE_NOTE       -> onSecureNoteClick(entry.id, "")
                                HubEntryType.CUSTOM_ENTRY      -> onCustomEntryClick(entry.id, "")
                                HubEntryType.CRYPTO_WALLET     -> onCryptoWalletClick(entry.id, "")
                                HubEntryType.IDENTITY_DOCUMENT -> onIdentityDocumentClick(entry.id, "")
                                HubEntryType.VEHICLE           -> onVehicleClick(entry.id, "")
                                HubEntryType.TWO_FACTOR         -> onTwoFactorClick(entry.id, "")
                            }
                        }
                    )
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RecentEntryCard(
    entry:    RecentHubEntry,
    modifier: Modifier = Modifier,
    onClick:  () -> Unit
) {
    Column(
        modifier              = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier         = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            when (entry.type) {
                HubEntryType.PASSWORD ->
                    ServiceAwareAvatar(title = entry.displayTitle, url = entry.url, size = 40.dp)
                HubEntryType.BANK_ACCOUNT ->
                    BankAwareAvatar(bankName = entry.avatarLabel, size = 40.dp)
                HubEntryType.CREDIT_CARD ->
                    CreditCardAwareAvatar(brand = entry.avatarLabel, size = 40.dp)
                HubEntryType.CRYPTO_WALLET ->
                    CryptoAwareAvatar(asset = entry.avatarLabel, size = 40.dp)
                else ->
                    EntryAvatar(title = entry.avatarLabel.ifEmpty { entry.displayTitle }, size = 40.dp)
            }
        }
        Text(
            text      = entry.displayTitle,
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onBackground,
            maxLines  = 1,
            textAlign = TextAlign.Center,
            overflow  = TextOverflow.Ellipsis
        )
        if (entry.subtitle != null) {
            Text(
                text      = entry.subtitle,
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines  = 1,
                textAlign = TextAlign.Center,
                overflow  = TextOverflow.Ellipsis
            )
        }
    }
}
