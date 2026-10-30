package com.thormakk.keyarmor.presentation.screens.archive

import com.thormakk.keyarmor.security.OrdenEnSesion
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.ArchiveItem
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.presentation.screens.vault.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val vaultRepo:       VaultRepository,
    private val bankRepo:        BankAccountRepository,
    private val noteRepo:        SecureNoteRepository,
    private val customRepo:      CustomEntryRepository,
    private val cardRepo:        CreditCardRepository,
    private val twoFactorRepo:   TwoFactorRepository,
    private val cryptoRepo:      CryptoWalletRepository,
    private val identityDocRepo: IdentityDocumentRepository,
    private val vehicleRepo:     VehicleRepository,
    private val passkeyRepo:     PasskeyRepository,
    planPrefs:                   PlanPrefs,
    @ApplicationContext private val appContextInyectado: Context,
) : ViewModel() {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)

    private val fallbackUntitled: String get() = appContext.getString(R.string.archive_fallback_untitled)
    private val fallbackUnnamed:  String get() = appContext.getString(R.string.archive_fallback_unnamed)

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    private val passwordsFlow: Flow<List<ArchiveItem>> =
        vaultRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.Password(
                    id         = e.id,
                    title      = e.title.ifEmpty { fallbackUntitled },
                    archivedAt = e.archivedAt,
                    tags       = e.tags,
                    category   = e.category,
                    isProtected = e.isProtected
                )
            }
        }

    private val bankFlow: Flow<List<ArchiveItem>> =
        bankRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.BankAccount(
                    id         = e.id,
                    title      = e.bankName.ifEmpty { fallbackUnnamed },
                    archivedAt = e.archivedAt,
                    tags       = e.tags,
                    category   = e.category,
                    isProtected = e.isProtected
                )
            }
        }

    private val notesFlow: Flow<List<ArchiveItem>> =
        noteRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.Note(
                    id         = e.id,
                    title      = e.title.ifEmpty { fallbackUntitled },
                    archivedAt = e.archivedAt,
                    tags       = e.tags,
                    category   = e.category,
                    isProtected = e.isProtected
                )
            }
        }

    private val customFlow: Flow<List<ArchiveItem>> =
        customRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.Custom(
                    id         = e.id,
                    title      = e.title.ifEmpty { fallbackUntitled },
                    archivedAt = e.archivedAt,
                    tags       = e.tags,
                    category   = e.category,
                    isProtected = e.isProtected
                )
            }
        }

    private val cardsFlow: Flow<List<ArchiveItem>> =
        cardRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.Card(
                    id         = e.id,
                    title      = e.issuer.ifEmpty { e.brand }.ifEmpty { fallbackUnnamed },
                    archivedAt = e.archivedAt,
                    tags       = e.tags,
                    category   = e.category,
                    isProtected = e.isProtected
                )
            }
        }

    private val twoFactorFlow: Flow<List<ArchiveItem>> =
        twoFactorRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.TwoFactor(
                    id         = e.id,
                    title      = e.name.ifEmpty { e.issuer }.ifEmpty { fallbackUnnamed },
                    archivedAt = e.archivedAt,
                    tags       = e.tags,
                    category   = e.category,
                    isProtected = e.isProtected
                )
            }
        }

    private val cryptoFlow: Flow<List<ArchiveItem>> =
        cryptoRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.CryptoWallet(
                    id         = e.id,
                    title      = e.asset.ifEmpty { e.network }.ifEmpty { fallbackUnnamed },
                    archivedAt = e.archivedAt,
                    tags       = e.tags,
                    category   = e.category,
                    isProtected = e.isProtected
                )
            }
        }

    private val identityDocFlow: Flow<List<ArchiveItem>> =
        identityDocRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.IdentityDocument(
                    id         = e.id,
                    title      = e.holderName.ifEmpty { e.docType }.ifEmpty { fallbackUnnamed },
                    archivedAt = e.archivedAt,
                    tags       = e.tags,
                    category   = e.category,
                    isProtected = e.isProtected
                )
            }
        }

    private val vehicleFlow: Flow<List<ArchiveItem>> =
        vehicleRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.Vehicle(
                    id         = e.id,
                    title      = listOf(e.make, e.model).filter { it.isNotEmpty() }.joinToString(" ").ifEmpty { fallbackUnnamed },
                    archivedAt = e.archivedAt,
                    tags       = e.tags,
                    category   = e.category,
                    isProtected = e.isProtected
                )
            }
        }

    private val passkeyFlow: Flow<List<ArchiveItem>> =
        passkeyRepo.observeArchived().map { list ->
            list.map { e ->
                ArchiveItem.Passkey(
                    id         = e.credentialId,
                    title      = e.rpName.ifEmpty { e.rpId }.ifEmpty { fallbackUnnamed },
                    archivedAt = e.archivedAt,
                    tags       = e.tagList,
                    category   = EntryCategory.fromString(e.category),
                    isProtected = e.isProtected
                )
            }
        }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val archiveItems: StateFlow<List<ArchiveItem>> = combine(
        listOf(passwordsFlow, bankFlow, notesFlow, customFlow, cardsFlow, twoFactorFlow, cryptoFlow, identityDocFlow, vehicleFlow, passkeyFlow)
    ) { arrays ->
        arrays.flatMap { it }.sortedByDescending { it.archivedAt }
    }.onEach { _isLoading.value = false }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val availableTypes: StateFlow<Set<String>> = archiveItems
        .map { items -> items.mapNotNull { it::class.simpleName }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val availableCategories: StateFlow<Set<EntryCategory>> = archiveItems
        .map { items -> items.map { it.category }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val availableTags: StateFlow<List<String>> = archiveItems
        .map { items -> items.flatMap { it.tags }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchQuery        = MutableStateFlow("")
    private val _selectedTypes      = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedCategories = MutableStateFlow<Set<EntryCategory>>(emptySet())
    private val _selectedTags       = MutableStateFlow<Set<String>>(emptySet())
    private val _sortOrder          = MutableStateFlow(OrdenEnSesion.leer("archive", SortOrder.NEWEST))
    private val _onlyProtected      = MutableStateFlow(false)

    val searchQuery:        StateFlow<String>             = _searchQuery.asStateFlow()
    val selectedTypes:      StateFlow<Set<String>>        = _selectedTypes.asStateFlow()
    val selectedCategories: StateFlow<Set<EntryCategory>> = _selectedCategories.asStateFlow()
    val selectedTags:       StateFlow<Set<String>>        = _selectedTags.asStateFlow()
    val sortOrder:          StateFlow<SortOrder>          = _sortOrder.asStateFlow()
    val onlyProtected:      StateFlow<Boolean>            = _onlyProtected.asStateFlow()

    private data class ArchiveFilters(
        val query:      String,
        val types:      Set<String>,
        val categories: Set<EntryCategory>,
        val tags:       Set<String>,
        val sort:       SortOrder,
        val onlyProtected: Boolean
    )

    private val filterState = combine(
        _searchQuery, _selectedTypes, _selectedCategories, _selectedTags, _sortOrder, _onlyProtected
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        ArchiveFilters(
            arr[0] as String, arr[1] as Set<String>, arr[2] as Set<EntryCategory>,
            arr[3] as Set<String>, arr[4] as SortOrder, arr[5] as Boolean
        )
    }

    val filteredItems: StateFlow<List<ArchiveItem>> = combine(archiveItems, filterState) { items, f ->
        val filtered = items.filter { item ->
            val matchesSearch   = f.query.isBlank() || item.title.contains(f.query, ignoreCase = true)
            val matchesType     = f.types.isEmpty()      || item::class.simpleName in f.types
            val matchesCategory = f.categories.isEmpty() || item.category in f.categories
            val matchesTags     = f.tags.isEmpty()       || item.tags.any { it in f.tags }
            val matchesProtected = !f.onlyProtected || item.isProtected
            matchesSearch && matchesType && matchesCategory && matchesTags && matchesProtected
        }
        when (f.sort) {
            SortOrder.A_Z    -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.Z_A    -> filtered.sortedByDescending { it.title.lowercase() }
            SortOrder.NEWEST -> filtered.sortedByDescending { it.archivedAt ?: 0L }
            SortOrder.OLDEST -> filtered.sortedBy { it.archivedAt ?: Long.MAX_VALUE }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSearchQuery(query: String)  { _searchQuery.value = query }
    fun setSortOrder(order: SortOrder) { OrdenEnSesion.guardar("archive", order); _sortOrder.value = order }

    fun applyFilters(types: Set<String>, categories: Set<EntryCategory>, tags: Set<String>, onlyProtected: Boolean) {
        _selectedTypes.value      = types
        _selectedCategories.value = categories
        _selectedTags.value       = tags
        _onlyProtected.value      = onlyProtected
    }

    fun clearFilters() {
        _searchQuery.value        = ""
        _selectedTypes.value      = emptySet()
        _selectedCategories.value = emptySet()
        _selectedTags.value       = emptySet()
        _onlyProtected.value      = false
    }

    fun unarchive(item: ArchiveItem) = viewModelScope.launch {
        when (item) {
            is ArchiveItem.Password     -> vaultRepo.unarchive(item.id)
            is ArchiveItem.BankAccount  -> bankRepo.unarchive(item.id)
            is ArchiveItem.Note         -> noteRepo.unarchive(item.id)
            is ArchiveItem.Custom       -> customRepo.unarchive(item.id)
            is ArchiveItem.Card         -> cardRepo.unarchive(item.id)
            is ArchiveItem.TwoFactor    -> twoFactorRepo.unarchive(item.id)
            is ArchiveItem.CryptoWallet    -> cryptoRepo.unarchive(item.id)
            is ArchiveItem.IdentityDocument -> identityDocRepo.unarchive(item.id)
            is ArchiveItem.Vehicle          -> vehicleRepo.unarchive(item.id)
            is ArchiveItem.Passkey          -> passkeyRepo.unarchive(item.id)
        }
    }

    private val _protectedSkipped = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val protectedSkipped: SharedFlow<Int> = _protectedSkipped.asSharedFlow()

    fun moveToTrash(item: ArchiveItem) = viewModelScope.launch {
        if (item.isProtected) { _protectedSkipped.tryEmit(1); return@launch }
        when (item) {
            is ArchiveItem.Password     -> { vaultRepo.unarchive(item.id);     vaultRepo.softDelete(item.id) }
            is ArchiveItem.BankAccount  -> { bankRepo.unarchive(item.id);      bankRepo.softDelete(item.id) }
            is ArchiveItem.Note         -> { noteRepo.unarchive(item.id);      noteRepo.softDelete(item.id) }
            is ArchiveItem.Custom       -> { customRepo.unarchive(item.id);    customRepo.softDelete(item.id) }
            is ArchiveItem.Card         -> { cardRepo.unarchive(item.id);      cardRepo.softDelete(item.id) }
            is ArchiveItem.TwoFactor    -> { twoFactorRepo.unarchive(item.id); twoFactorRepo.softDelete(item.id) }
            is ArchiveItem.CryptoWallet    -> { cryptoRepo.unarchive(item.id);       cryptoRepo.softDelete(item.id) }
            is ArchiveItem.IdentityDocument -> { identityDocRepo.unarchive(item.id); identityDocRepo.softDelete(item.id) }
            is ArchiveItem.Vehicle          -> { vehicleRepo.unarchive(item.id);     vehicleRepo.softDelete(item.id) }
            is ArchiveItem.Passkey          -> { passkeyRepo.unarchive(item.id);     passkeyRepo.delete(item.id) }
        }
    }

    fun unarchiveAll() = viewModelScope.launch {
        archiveItems.value.forEach { item ->
            when (item) {
                is ArchiveItem.Password         -> vaultRepo.unarchive(item.id)
                is ArchiveItem.BankAccount      -> bankRepo.unarchive(item.id)
                is ArchiveItem.Note             -> noteRepo.unarchive(item.id)
                is ArchiveItem.Custom           -> customRepo.unarchive(item.id)
                is ArchiveItem.Card             -> cardRepo.unarchive(item.id)
                is ArchiveItem.TwoFactor        -> twoFactorRepo.unarchive(item.id)
                is ArchiveItem.CryptoWallet     -> cryptoRepo.unarchive(item.id)
                is ArchiveItem.IdentityDocument -> identityDocRepo.unarchive(item.id)
                is ArchiveItem.Vehicle          -> vehicleRepo.unarchive(item.id)
                is ArchiveItem.Passkey          -> passkeyRepo.unarchive(item.id)
            }
        }
    }

    fun moveAllToTrash() = viewModelScope.launch {
        val (protegidas, borrables) = archiveItems.value.partition { it.isProtected }
        if (protegidas.isNotEmpty()) _protectedSkipped.tryEmit(protegidas.size)
        borrables.forEach { item ->
            when (item) {
                is ArchiveItem.Password         -> { vaultRepo.unarchive(item.id);       vaultRepo.softDelete(item.id) }
                is ArchiveItem.BankAccount      -> { bankRepo.unarchive(item.id);        bankRepo.softDelete(item.id) }
                is ArchiveItem.Note             -> { noteRepo.unarchive(item.id);        noteRepo.softDelete(item.id) }
                is ArchiveItem.Custom           -> { customRepo.unarchive(item.id);      customRepo.softDelete(item.id) }
                is ArchiveItem.Card             -> { cardRepo.unarchive(item.id);        cardRepo.softDelete(item.id) }
                is ArchiveItem.TwoFactor        -> { twoFactorRepo.unarchive(item.id);   twoFactorRepo.softDelete(item.id) }
                is ArchiveItem.CryptoWallet     -> { cryptoRepo.unarchive(item.id);      cryptoRepo.softDelete(item.id) }
                is ArchiveItem.IdentityDocument -> { identityDocRepo.unarchive(item.id); identityDocRepo.softDelete(item.id) }
                is ArchiveItem.Vehicle          -> { vehicleRepo.unarchive(item.id);     vehicleRepo.softDelete(item.id) }
                is ArchiveItem.Passkey          -> { passkeyRepo.unarchive(item.id);     passkeyRepo.delete(item.id) }
            }
        }
    }

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    val isSelectionMode: Boolean get() = _selectedIds.value.isNotEmpty()

    fun toggleSelection(id: String) {
        _selectedIds.update { if (id in it) it - id else it + id }
    }

    fun selectAll() {
        _selectedIds.value = filteredItems.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun bulkUnarchive() {
        val itemsToUnarchive = archiveItems.value.filter { it.id in _selectedIds.value }
        viewModelScope.launch {
            itemsToUnarchive.forEach { item ->
                when (item) {
                    is ArchiveItem.Password     -> vaultRepo.unarchive(item.id)
                    is ArchiveItem.BankAccount  -> bankRepo.unarchive(item.id)
                    is ArchiveItem.Note         -> noteRepo.unarchive(item.id)
                    is ArchiveItem.Custom       -> customRepo.unarchive(item.id)
                    is ArchiveItem.Card         -> cardRepo.unarchive(item.id)
                    is ArchiveItem.TwoFactor    -> twoFactorRepo.unarchive(item.id)
                    is ArchiveItem.CryptoWallet     -> cryptoRepo.unarchive(item.id)
                    is ArchiveItem.IdentityDocument -> identityDocRepo.unarchive(item.id)
                    is ArchiveItem.Vehicle          -> vehicleRepo.unarchive(item.id)
                    is ArchiveItem.Passkey          -> passkeyRepo.unarchive(item.id)
                }
            }
            _selectedIds.value = emptySet()
        }
    }

    fun bulkMoveToTrash() {
        val (protegidas, itemsToMove) = archiveItems.value.filter { it.id in _selectedIds.value }.partition { it.isProtected }
        viewModelScope.launch {
            if (protegidas.isNotEmpty()) _protectedSkipped.tryEmit(protegidas.size)
            itemsToMove.forEach { item ->
                when (item) {
                    is ArchiveItem.Password         -> { vaultRepo.unarchive(item.id);       vaultRepo.softDelete(item.id) }
                    is ArchiveItem.BankAccount      -> { bankRepo.unarchive(item.id);        bankRepo.softDelete(item.id) }
                    is ArchiveItem.Note             -> { noteRepo.unarchive(item.id);        noteRepo.softDelete(item.id) }
                    is ArchiveItem.Custom           -> { customRepo.unarchive(item.id);      customRepo.softDelete(item.id) }
                    is ArchiveItem.Card             -> { cardRepo.unarchive(item.id);        cardRepo.softDelete(item.id) }
                    is ArchiveItem.TwoFactor        -> { twoFactorRepo.unarchive(item.id);   twoFactorRepo.softDelete(item.id) }
                    is ArchiveItem.CryptoWallet     -> { cryptoRepo.unarchive(item.id);      cryptoRepo.softDelete(item.id) }
                    is ArchiveItem.IdentityDocument -> { identityDocRepo.unarchive(item.id); identityDocRepo.softDelete(item.id) }
                    is ArchiveItem.Vehicle          -> { vehicleRepo.unarchive(item.id);     vehicleRepo.softDelete(item.id) }
                    is ArchiveItem.Passkey          -> { passkeyRepo.unarchive(item.id);     passkeyRepo.delete(item.id) }
                }
            }
            _selectedIds.value = emptySet()
        }
    }

    private val _scrollHighlightPending = AtomicBoolean(false)

    fun activateScrollHighlight() { _scrollHighlightPending.set(true) }

    fun consumeScrollHighlight(): Boolean = _scrollHighlightPending.compareAndSet(true, false)

    init { activateScrollHighlight() }

}
