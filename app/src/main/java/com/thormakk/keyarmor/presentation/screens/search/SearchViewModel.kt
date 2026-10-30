@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
package com.thormakk.keyarmor.presentation.screens.search

import com.thormakk.keyarmor.presentation.util.ServiceIconProvider
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.export.ImportParser
import com.thormakk.keyarmor.data.export.ImportResult
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.RecentEntriesPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AttachmentRepository
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.ReminderRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.SnapshotRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.CustomEntry
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.IdentityDocument
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.Reminder
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.Vehicle
import com.thormakk.keyarmor.presentation.screens.vault.QuickFilter
import com.thormakk.keyarmor.presentation.screens.vault.composeTopQuickFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BackupSearchResult(
    val snapshotId: String,
    val trigger:    SnapshotTrigger,
    val timestamp:  Long,
    val entryId:    String,
    val title:      String
)

data class RecentHubEntry(
    val id:           String,
    val type:         HubEntryType,
    val displayTitle: String,
    val subtitle:     String? = null,
    val url:          String  = "",
    val avatarLabel:  String  = ""
)

data class FuzzyResults(
    val passwords:         List<VaultEntry>       = emptyList(),
    val bankAccounts:      List<BankAccount>      = emptyList(),
    val creditCards:       List<CreditCard>       = emptyList(),
    val secureNotes:       List<SecureNote>       = emptyList(),
    val customEntries:     List<CustomEntry>      = emptyList(),
    val twoFactorAccounts: List<TwoFactorAccount> = emptyList(),
    val cryptoWallets:     List<CryptoWallet>     = emptyList(),
    val passkeys:          List<PasskeyCredential> = emptyList(),
    val identityDocuments: List<IdentityDocument> = emptyList(),
    val vehicles:          List<Vehicle>          = emptyList(),
    val reminders:         List<Reminder>         = emptyList()
) {
    val totalCount: Int
        get() = passwords.size + bankAccounts.size + creditCards.size +
                secureNotes.size + customEntries.size + twoFactorAccounts.size +
                cryptoWallets.size + passkeys.size + identityDocuments.size + vehicles.size +
                reminders.size
}

data class SearchUiState(
    val query: String                              = "",
    val selectedCategory: EntryCategory?           = null,
    val selectedTags: Set<String>                  = emptySet(),
    val passwords: List<VaultEntry>                = emptyList(),
    val bankAccounts: List<BankAccount>            = emptyList(),
    val creditCards: List<CreditCard>              = emptyList(),
    val secureNotes: List<SecureNote>              = emptyList(),
    val customEntries: List<CustomEntry>           = emptyList(),
    val twoFactorAccounts: List<TwoFactorAccount>  = emptyList(),
    val cryptoWallets: List<CryptoWallet>          = emptyList(),
    val passkeys: List<PasskeyCredential>          = emptyList(),
    val identityDocuments: List<IdentityDocument>  = emptyList(),
    val vehicles: List<Vehicle>                    = emptyList(),
    val reminders: List<Reminder>                  = emptyList(),
    val onlyFavorites: Boolean                     = false,
    val onlyWithAttachments: Boolean               = false,
    val onlyProtected: Boolean                     = false,
    val fuzzy: FuzzyResults                        = FuzzyResults(),
    val vaults: List<SearchVaultRef>               = emptyList()
) {
    val isInitial: Boolean
        get() = query.isBlank() && selectedCategory == null && selectedTags.isEmpty()
                 && !onlyFavorites && !onlyWithAttachments && !onlyProtected

    val totalCount: Int
        get() = passwords.size + bankAccounts.size + creditCards.size +
                secureNotes.size + customEntries.size + twoFactorAccounts.size +
                cryptoWallets.size + passkeys.size + identityDocuments.size + vehicles.size +
                reminders.size

}

data class SearchVaultRef(val id: String, val name: String)

private data class SearchFilters(
    val query: String,
    val category: EntryCategory?,
    val tags: Set<String>,
    val onlyFavorites: Boolean,
    val onlyWithAttachments: Boolean,
    val onlyProtected: Boolean
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    vaultRepository:          VaultRepository,
    bankAccountRepository:    BankAccountRepository,
    creditCardRepository:     CreditCardRepository,
    secureNoteRepository:     SecureNoteRepository,
    customEntryRepository:    CustomEntryRepository,
    twoFactorRepository:      TwoFactorRepository,
    cryptoWalletRepository:   CryptoWalletRepository,
    passkeyRepository:        PasskeyRepository,
    private val tagRepository: TagRepository,
    attachmentRepository:     AttachmentRepository,
    private val identityDocRepository: IdentityDocumentRepository,
    private val vehicleRepository:     VehicleRepository,
    private val reminderRepository:    ReminderRepository,
    planPrefs:    PlanPrefs,
    activeVaultPrefs: ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val recentEntriesPrefs: RecentEntriesPrefs,
    private val snapshotRepository: SnapshotRepository,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    @ApplicationContext private val appContextInyectado: Context
) : ViewModel() {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)

    private val _query            = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<EntryCategory?>(null)
    private val _selectedTags     = MutableStateFlow<Set<String>>(emptySet())
    private val _onlyFavorites        = MutableStateFlow(false)
    private val _onlyWithAttachments  = MutableStateFlow(false)
    private val _onlyProtected        = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            _query
                .debounce(250)
                .collectLatest { q ->
                    val trimmed = q.trim()
                    val matches = if (trimmed.isEmpty()) false
                        else withContext(Dispatchers.Default) { hiddenEntriesPrefs.verifyPhrase(trimmed) }
                    hiddenEntriesPrefs.setRevealActive(matches)
                }
        }
    }

    override fun onCleared() {
        hiddenEntriesPrefs.setRevealActive(false)
    }

    val attachmentIds: StateFlow<Set<String>> =
        attachmentRepository.observeEntryIdsWithAttachments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    private val _searchBackupsEnabled = MutableStateFlow(false)
    val searchBackupsEnabled: StateFlow<Boolean> = _searchBackupsEnabled.asStateFlow()

    private val _isLoadingBackups = MutableStateFlow(false)
    val isLoadingBackups: StateFlow<Boolean> = _isLoadingBackups.asStateFlow()

    private var snapshotCache: List<Pair<com.thormakk.keyarmor.domain.model.VaultSnapshot, ImportResult>>? = null
    private val _backupsCacheVersion = MutableStateFlow(0)

    fun setSearchBackupsEnabled(enabled: Boolean) {
        _searchBackupsEnabled.value = enabled
        if (enabled && snapshotCache == null && !_isLoadingBackups.value) {
            loadSnapshotCache()
        }
    }

    private fun loadSnapshotCache() {
        viewModelScope.launch {
            _isLoadingBackups.value = true
            try {
                val snapshots = snapshotRepository.observeSnapshots().first()
                val loaded = withContext(Dispatchers.Default) {
                    snapshots.mapNotNull { snap ->
                        runCatching {
                            val json = snapshotRepository.loadSnapshotData(snap.id)
                            snap to ImportParser.parseKeyArmorJson(json)
                        }.getOrNull()
                    }
                }
                snapshotCache = loaded
            } catch (_: Exception) {
            } finally {
                _isLoadingBackups.value = false
                _backupsCacheVersion.update { it + 1 }
            }
        }
    }

    private fun ImportResult.matchingEntries(q: String): List<Pair<String, String>> = buildList {
        passwords.filter { it.exactMatches(q) }.forEach { add(it.id to it.title.ifBlank { "—" }) }
        bankAccounts.filter { it.exactMatches(q) }.forEach { add(it.id to it.bankName.ifBlank { it.alias }) }
        creditCards.filter { it.exactMatches(q) }.forEach { add(it.id to it.issuer.ifBlank { it.brand }) }
        secureNotes.filter { it.exactMatches(q) }.forEach { add(it.id to it.title) }
        customEntries.filter { it.exactMatches(q) }.forEach { add(it.id to it.title) }
        twoFactor.filter { it.exactMatches(q) }.forEach { add(it.id to it.issuer.ifBlank { it.name }) }
        cryptoWallets.filter { it.exactMatches(q) }.forEach {
            add(it.id to listOf(it.network, it.asset).filter { s -> s.isNotBlank() }.joinToString(" / ").ifBlank { it.address.take(12) })
        }
        passkeys.filter { it.exactMatches(q) }.forEach { add(it.credentialId to it.rpName.ifBlank { it.userName }) }
        identityDocuments.filter { it.exactMatches(q) }.forEach { add(it.id to it.holderName.ifBlank { it.docType }) }
        vehicles.filter { it.exactMatches(q) }.forEach {
            add(it.id to listOf(it.make, it.model).filter { s -> s.isNotBlank() }.joinToString(" ").ifBlank { it.licensePlate })
        }
    }

    val backupResults: StateFlow<List<BackupSearchResult>> = combine(
        _query, _searchBackupsEnabled, _backupsCacheVersion
    ) { q, enabled, _ ->
        val trimmed = q.trim()
        if (!enabled || trimmed.length < 2) return@combine emptyList()
        val cache = snapshotCache ?: return@combine emptyList()
        cache.flatMap { (snap, result) ->
            result.matchingEntries(trimmed).map { (entryId, title) ->
                BackupSearchResult(snapshotId = snap.id, trigger = snap.trigger, timestamp = snap.timestamp, entryId = entryId, title = title)
            }
        }.sortedByDescending { it.timestamp }.take(100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val filters: StateFlow<SearchFilters> = combine(
        _query, _selectedCategory, _selectedTags, _onlyFavorites, _onlyWithAttachments, _onlyProtected
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        SearchFilters(
            arr[0] as String, arr[1] as EntryCategory?, arr[2] as Set<String>,
            arr[3] as Boolean, arr[4] as Boolean, arr[5] as Boolean
        )
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SearchFilters("", null, emptySet(), onlyFavorites = false, onlyWithAttachments = false, onlyProtected = false))

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val quickFilters: StateFlow<List<QuickFilter>> = combine(
        combine(
            combine(
                vaultRepository.observeEntries(),
                bankAccountRepository.observeAccounts()
            ) { e, a -> e to a },
            combine(
                creditCardRepository.observeCards(),
                secureNoteRepository.observeNotes()
            ) { c, n -> c to n },
            combine(
                customEntryRepository.observeEntries(),
                twoFactorRepository.observeAccounts()
            ) { c, t -> c to t }
        ) { (entries, accounts), (cards, notes), (custom, twoFactor) ->
            val categoryCounts: List<Pair<QuickFilter, Int>> = EntryCategory.entries
                .filter { it != EntryCategory.SIN_CATEGORIZAR }
                .map { cat ->
                    val count = entries.count { it.category == cat } +
                        accounts.count { it.category == cat } +
                        cards.count { it.category == cat } +
                        notes.count { it.category == cat } +
                        custom.count { it.category == cat } +
                        twoFactor.count { it.category == cat }
                    QuickFilter.Category(cat) to count
                }

            val tagCounts: List<Pair<QuickFilter, Int>> = (
                entries.flatMap { it.tags } +
                    accounts.flatMap { it.tags } +
                    cards.flatMap { it.tags } +
                    notes.flatMap { it.tags } +
                    custom.flatMap { it.tags } +
                    twoFactor.flatMap { it.tags }
                )
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .map { (tag, count) -> QuickFilter.Tag(tag) to count }

            (categoryCounts + tagCounts)
                .filter { it.second > 0 }
                .sortedWith(
                    compareByDescending<Pair<QuickFilter, Int>> { it.second }
                        .thenBy { it.first.key }
                )
                .map { it.first }
        },
        tagRepository.observeDefaultTagNames()
    ) { ranked, defaultTags ->
        val tagImportant = defaultTags.first
        val tagWork      = defaultTags.third
        val defaults: List<QuickFilter> = listOf(
            QuickFilter.Tag(tagImportant),
            QuickFilter.Tag(defaultTags.second),
            QuickFilter.Tag(tagWork)
        )
        composeTopQuickFilters(ranked, defaults)
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val tagCountMap: StateFlow<Map<String, Int>> = combine(
        combine(
            vaultRepository.observeEntries(),
            bankAccountRepository.observeAccounts()
        ) { e, b -> e.flatMap { it.tags } + b.flatMap { it.tags } },
        combine(
            creditCardRepository.observeCards(),
            secureNoteRepository.observeNotes()
        ) { c, n -> c.flatMap { it.tags } + n.flatMap { it.tags } },
        combine(
            customEntryRepository.observeEntries(),
            twoFactorRepository.observeAccounts()
        ) { c, t -> c.flatMap { it.tags } + t.flatMap { it.tags } }
    ) { a, b, c ->
        (a + b + c)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val categoryCountMap: StateFlow<Map<EntryCategory, Int>> = combine(
        combine(
            vaultRepository.observeEntries(),
            bankAccountRepository.observeAccounts()
        ) { e, b -> e.map { it.category } + b.map { it.category } },
        combine(
            creditCardRepository.observeCards(),
            secureNoteRepository.observeNotes()
        ) { c, n -> c.map { it.category } + n.map { it.category } },
        combine(
            customEntryRepository.observeEntries(),
            twoFactorRepository.observeAccounts()
        ) { c, t -> c.map { it.category } + t.map { it.category } }
    ) { a, b, c ->
        (a + b + c).groupingBy { it }.eachCount()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private data class SearchData(
        val entries: List<VaultEntry>,
        val accounts: List<BankAccount>,
        val cards: List<CreditCard>,
        val notes: List<SecureNote>,
        val custom: List<CustomEntry>,
        val twoFactor: List<TwoFactorAccount>,
        val cryptoWallets: List<CryptoWallet>,
        val passkeys: List<PasskeyCredential>,
        val identityDocuments: List<IdentityDocument>,
        val vehicles: List<Vehicle>,
        val reminders: List<Reminder>,
        val filters: SearchFilters
    )

    private data class SearchRaw(
        val entries: List<VaultEntry>,
        val accounts: List<BankAccount>,
        val cards: List<CreditCard>,
        val notes: List<SecureNote>,
        val custom: List<CustomEntry>,
        val twoFactor: List<TwoFactorAccount>,
        val cryptoWallets: List<CryptoWallet>,
        val passkeys: List<PasskeyCredential>,
        val identityDocs: List<IdentityDocument>,
        val vehicles: List<Vehicle>,
        val reminders: List<Reminder>
    )

    private val _rawData: StateFlow<SearchRaw> =
        combine(
            activeVaultPrefs.activeVaultIdFlow,
            vaultMetadataRepository.observeAll()
        ) { activeVaultId, vaults ->
            val activeIsPrivate = vaults.firstOrNull { it.id == activeVaultId }?.isPrivate == true
            if (activeIsPrivate) vaults.map { it.id }.toSet()
            else                 vaults.filter { !it.isPrivate }.map { it.id }.toSet()
        }.distinctUntilChanged().flatMapLatest { allowedVaultIds ->
            combine(
                combine(vaultRepository.observeAllForSearch(), bankAccountRepository.observeAllForSearch()) { a, b -> a to b },
                combine(creditCardRepository.observeAllForSearch(), secureNoteRepository.observeAllForSearch()) { a, b -> a to b },
                combine(customEntryRepository.observeAllForSearch(), twoFactorRepository.observeAllForSearch()) { a, b -> a to b },
                combine(cryptoWalletRepository.observeAllForSearch(), passkeyRepository.observeAllForSearch()) { a, b -> a to b },
                combine(identityDocRepository.observeAllForSearch(), vehicleRepository.observeAllForSearch(), reminderRepository.observeActive()) { a, b, c -> Triple(a, b, c) }
            ) { p1, p2, p3, p4, p5 ->
                val (entries, accounts)       = p1
                val (cards, notes)            = p2
                val (custom, twoFactor)       = p3
                val (cryptoWallets, passkeys) = p4
                val (identityDocs, vehicles, reminders) = p5
                SearchRaw(
                    entries          = entries.filter          { it.vaultId in allowedVaultIds },
                    accounts         = accounts.filter         { it.vaultId in allowedVaultIds },
                    cards            = cards.filter            { it.vaultId in allowedVaultIds },
                    notes            = notes.filter            { it.vaultId in allowedVaultIds },
                    custom           = custom.filter           { it.vaultId in allowedVaultIds },
                    twoFactor        = twoFactor.filter        { it.vaultId in allowedVaultIds },
                    cryptoWallets    = cryptoWallets.filter    { it.vaultId in allowedVaultIds },
                    passkeys         = passkeys.filter         { it.vaultId in allowedVaultIds },
                    identityDocs     = identityDocs.filter     { it.vaultId in allowedVaultIds },
                    vehicles         = vehicles.filter         { it.vaultId in allowedVaultIds },
                    reminders        = reminders
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchRaw(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))

    private val _searchData: StateFlow<SearchData> = combine(_rawData, filters) { raw, f ->
        SearchData(raw.entries, raw.accounts, raw.cards, raw.notes, raw.custom, raw.twoFactor, raw.cryptoWallets, raw.passkeys, raw.identityDocs, raw.vehicles, raw.reminders, f)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), SearchFilters("", null, emptySet(), onlyFavorites = false, onlyWithAttachments = false, onlyProtected = false)))

    val uiState: StateFlow<SearchUiState> = combine(
        _searchData,
        attachmentIds,
        hiddenEntriesPrefs.revealActiveFlow,
        vaultMetadataRepository.observeAll()
    ) { data, attachIds, revealActive, vaultMetas ->
        val f       = data.filters
        val q       = f.query.trim()
        val cat     = f.category
        val tags    = f.tags
        val vaultRefs = vaultMetas.sortedBy { it.sortOrder }.map { SearchVaultRef(it.id, it.name) }

        if (revealActive) {
            return@combine SearchUiState(
                query               = f.query,
                selectedCategory    = cat,
                selectedTags        = tags,
                passwords           = data.entries,
                bankAccounts        = data.accounts,
                creditCards         = data.cards,
                secureNotes         = data.notes,
                customEntries       = data.custom,
                twoFactorAccounts   = data.twoFactor,
                cryptoWallets       = data.cryptoWallets,
                passkeys            = data.passkeys,
                identityDocuments   = data.identityDocuments,
                vehicles            = data.vehicles,
                reminders           = emptyList(),
                onlyFavorites       = f.onlyFavorites,
                onlyWithAttachments = f.onlyWithAttachments,
                onlyProtected       = f.onlyProtected,
                fuzzy               = FuzzyResults(),
                vaults              = vaultRefs
            )
        }

        val passwords         = data.entries.filter      { it.exactMatches(q, cat, tags) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) }
        val bankAccounts      = data.accounts.filter     { it.exactMatches(q, cat, tags) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) }
        val creditCards       = data.cards.filter        { it.exactMatches(q, cat, tags) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) }
        val secureNotes       = data.notes.filter        { it.exactMatches(q, cat, tags) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) }
        val customEntries     = data.custom.filter       { it.exactMatches(q, cat, tags) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) }
        val twoFactorAccounts = data.twoFactor.filter    { it.exactMatches(q, cat, tags) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) }
        val cryptoWallets     = data.cryptoWallets.filter { it.exactMatches(q, cat, tags) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) }
        val passkeys          = if (f.onlyFavorites || f.onlyWithAttachments) emptyList() else
                                    data.passkeys.filter { it.exactMatches(q, cat, tags) && (!f.onlyProtected || it.isProtected) }
        val identityDocuments = data.identityDocuments.filter { it.exactMatches(q, cat, tags) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) }
        val vehicles          = data.vehicles.filter { it.exactMatches(q, cat, tags) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) }
        val reminders         = if (cat == null && tags.isEmpty() && !f.onlyFavorites && !f.onlyWithAttachments)
                                    data.reminders.filter { it.exactMatches(q) && (!f.onlyProtected || it.isProtected) } else emptyList()

        val fuzzy = if (shouldRunFuzzy(q) && cat == null && tags.isEmpty()) {
            val exactPasswordIds     = passwords.mapTo(HashSet())         { it.id }
            val exactBankIds         = bankAccounts.mapTo(HashSet())      { it.id }
            val exactCardIds         = creditCards.mapTo(HashSet())       { it.id }
            val exactNoteIds         = secureNotes.mapTo(HashSet())       { it.id }
            val exactCustomIds       = customEntries.mapTo(HashSet())     { it.id }
            val exactTwoFaIds        = twoFactorAccounts.mapTo(HashSet()) { it.id }
            val exactCryptoIds       = cryptoWallets.mapTo(HashSet())     { it.id }
            val exactPasskeyIds      = passkeys.mapTo(HashSet())          { it.credentialId }
            val exactIdentityIds     = identityDocuments.mapTo(HashSet()) { it.id }
            val exactVehicleIds      = vehicles.mapTo(HashSet())          { it.id }
            val exactReminderIds     = reminders.mapTo(HashSet())         { it.id }

            FuzzyResults(
                passwords         = data.entries.filter       { it.id !in exactPasswordIds && it.fuzzyMatchesPrimary(q) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) },
                bankAccounts      = data.accounts.filter      { it.id !in exactBankIds && it.fuzzyMatchesPrimary(q) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) },
                creditCards       = data.cards.filter         { it.id !in exactCardIds && it.fuzzyMatchesPrimary(q) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) },
                secureNotes       = data.notes.filter         { it.id !in exactNoteIds && it.fuzzyMatchesPrimary(q) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) },
                customEntries     = data.custom.filter        { it.id !in exactCustomIds && it.fuzzyMatchesPrimary(q) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) },
                twoFactorAccounts = data.twoFactor.filter     { it.id !in exactTwoFaIds && it.fuzzyMatchesPrimary(q) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) },
                cryptoWallets     = data.cryptoWallets.filter { it.id !in exactCryptoIds && it.fuzzyMatchesPrimary(q) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) },
                passkeys          = if (f.onlyFavorites || f.onlyWithAttachments) emptyList() else data.passkeys.filter { it.credentialId !in exactPasskeyIds && it.fuzzyMatchesPrimary(q) && (!f.onlyProtected || it.isProtected) },
                identityDocuments = data.identityDocuments.filter { it.id !in exactIdentityIds && it.fuzzyMatchesPrimary(q) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) },
                vehicles          = data.vehicles.filter      { it.id !in exactVehicleIds && it.fuzzyMatchesPrimary(q) && (!f.onlyFavorites || it.isFavorite) && (!f.onlyWithAttachments || it.id in attachIds) && (!f.onlyProtected || it.isProtected) },
                reminders         = if (f.onlyFavorites || f.onlyWithAttachments || tags.isNotEmpty()) emptyList()
                                    else data.reminders.filter { it.id !in exactReminderIds && it.fuzzyMatchesPrimary(q) && (!f.onlyProtected || it.isProtected) }
            )
        } else {
            FuzzyResults()
        }

        SearchUiState(
            query               = f.query,
            selectedCategory    = cat,
            selectedTags        = tags,
            passwords           = passwords,
            bankAccounts        = bankAccounts,
            creditCards         = creditCards,
            secureNotes         = secureNotes,
            customEntries       = customEntries,
            twoFactorAccounts   = twoFactorAccounts,
            cryptoWallets       = cryptoWallets,
            passkeys            = passkeys,
            identityDocuments   = identityDocuments,
            vehicles            = vehicles,
            reminders           = reminders,
            onlyFavorites       = f.onlyFavorites,
            onlyWithAttachments = f.onlyWithAttachments,
            onlyProtected       = f.onlyProtected,
            fuzzy               = fuzzy,
            vaults              = vaultRefs
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(value: String)            { _query.value = value }
    fun onCategorySelected(cat: EntryCategory?) { _selectedCategory.value = cat }
    fun onTagToggled(tag: String) {
        _selectedTags.value = _selectedTags.value.toMutableSet().apply {
            if (contains(tag)) remove(tag) else add(tag)
        }
    }
    fun onTagsApplied(tags: Set<String>) { _selectedTags.value = tags }


    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String)                   { viewModelScope.launch { tagRepository.addTag(name) } }
    fun renameTag(id: String, newName: String) { viewModelScope.launch { tagRepository.renameTag(id, newName) } }
    fun deleteTag(id: String)                  { viewModelScope.launch { tagRepository.deleteTag(id) } }

    fun onFavoritesToggle()        { _onlyFavorites.value = !_onlyFavorites.value }
    fun onAttachmentFilterToggle() { _onlyWithAttachments.value = !_onlyWithAttachments.value }
    fun onProtectedFilterToggle(value: Boolean) { _onlyProtected.value = value }

    val recentEntries: StateFlow<List<RecentHubEntry>> = combine(
        _rawData,
        recentEntriesPrefs.recentRefsFlow
    ) { raw, refs ->
        refs.mapNotNull { ref ->
            when (ref.type) {
                HubEntryType.PASSWORD ->
                    raw.entries.find { it.id == ref.id && !it.isDeleted && it.archivedAt == null }
                        ?.let { e ->
                            RecentHubEntry(
                                id           = ref.id,
                                type         = ref.type,
                                displayTitle = e.title,
                                url          = ServiceIconProvider.bestUrl(e.urls),
                                avatarLabel  = e.title,
                                subtitle     = e.email.ifEmpty { e.username }.ifBlank { null }
                            )
                        }
                HubEntryType.BANK_ACCOUNT ->
                    raw.accounts.find { it.id == ref.id && !it.isDeleted && it.archivedAt == null }
                        ?.let { e ->
                            val title = e.bankName.ifEmpty { e.alias }
                            RecentHubEntry(
                                id           = ref.id,
                                type         = ref.type,
                                displayTitle = title,
                                avatarLabel  = title,
                                subtitle     = e.accountType.ifBlank { null }
                            )
                        }
                HubEntryType.CREDIT_CARD ->
                    raw.cards.find { it.id == ref.id && !it.isDeleted && it.archivedAt == null }
                        ?.let { e ->
                            val title = e.issuer.ifEmpty { e.brand }
                            RecentHubEntry(
                                id           = ref.id,
                                type         = ref.type,
                                displayTitle = title,
                                avatarLabel  = e.brand.ifEmpty { title },
                                subtitle     = e.brand.takeIf { it.isNotBlank() && it != title }
                            )
                        }
                HubEntryType.SECURE_NOTE ->
                    raw.notes.find { it.id == ref.id && !it.isDeleted && it.archivedAt == null }
                        ?.let { e ->
                            RecentHubEntry(
                                id           = ref.id,
                                type         = ref.type,
                                displayTitle = e.title,
                                avatarLabel  = e.title,
                                subtitle     = e.content.lineSequence()
                                    .firstOrNull { it.isNotBlank() }?.take(40)
                            )
                        }
                HubEntryType.CUSTOM_ENTRY ->
                    raw.custom.find { it.id == ref.id && !it.isDeleted && it.archivedAt == null }
                        ?.let { e ->
                            RecentHubEntry(
                                id           = ref.id,
                                type         = ref.type,
                                displayTitle = e.title,
                                avatarLabel  = e.title
                            )
                        }
                HubEntryType.CRYPTO_WALLET ->
                    raw.cryptoWallets.find { it.id == ref.id && !it.isDeleted && it.archivedAt == null }
                        ?.let { e ->
                            val title = listOf(e.network, e.asset)
                                .filter { it.isNotBlank() }.joinToString(" / ")
                                .ifEmpty { e.address.take(12).ifEmpty { appContext.getString(R.string.hub_pinned_fallback_crypto_wallet) } }
                            RecentHubEntry(
                                id           = ref.id,
                                type         = ref.type,
                                displayTitle = title,
                                avatarLabel  = e.asset.ifEmpty { e.network }.ifEmpty { title },
                                subtitle     = e.address.takeIf { it.isNotBlank() }?.let {
                                    if (it.length > 16) it.take(8) + "…" + it.takeLast(6) else it
                                }
                            )
                        }
                HubEntryType.IDENTITY_DOCUMENT ->
                    raw.identityDocs.find { it.id == ref.id && !it.isDeleted && it.archivedAt == null }
                        ?.let { e ->
                            val title = e.holderName.ifEmpty { e.docType }.ifEmpty { appContext.getString(R.string.hub_pinned_fallback_identity_document) }
                            RecentHubEntry(
                                id           = ref.id,
                                type         = ref.type,
                                displayTitle = title,
                                avatarLabel  = title,
                                subtitle     = e.docType.takeIf { it.isNotBlank() && it != title }
                                    ?: e.number.ifBlank { null }
                            )
                        }
                HubEntryType.VEHICLE ->
                    raw.vehicles.find { it.id == ref.id && !it.isDeleted && it.archivedAt == null }
                        ?.let { e ->
                            val name = listOf(e.make, e.model).filter { it.isNotBlank() }
                                .joinToString(" ")
                                .ifEmpty { e.licensePlate.ifEmpty { appContext.getString(R.string.hub_pinned_fallback_vehicle) } }
                            RecentHubEntry(
                                id           = ref.id,
                                type         = ref.type,
                                displayTitle = name,
                                avatarLabel  = name,
                                subtitle     = e.licensePlate.ifBlank { null }
                            )
                        }
                HubEntryType.TWO_FACTOR ->
                    raw.twoFactor.find { it.id == ref.id && !it.isDeleted && it.archivedAt == null }
                        ?.let { e ->
                            val title = e.name.ifEmpty { e.issuer }.ifEmpty { appContext.getString(R.string.hub_pinned_fallback_two_factor) }
                            RecentHubEntry(
                                id           = ref.id,
                                type         = ref.type,
                                displayTitle = title,
                                avatarLabel  = title,
                                subtitle     = e.accountLabel.ifBlank { null }
                            )
                        }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun recordRecentAccess(id: String, type: HubEntryType) =
        recentEntriesPrefs.recordAccess(id, type)
}
