package com.thormakk.keyarmor.presentation.screens.trash

import com.thormakk.keyarmor.security.OrdenEnSesion
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.TrashAutoEmptyPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.ReminderRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.TrashItem
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.screens.vault.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import com.thormakk.keyarmor.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val vaultRepo:           VaultRepository,
    private val bankRepo:            BankAccountRepository,
    private val noteRepo:            SecureNoteRepository,
    private val customRepo:          CustomEntryRepository,
    private val cardRepo:            CreditCardRepository,
    private val twoFactorRepo:       TwoFactorRepository,
    private val cryptoRepo:          CryptoWalletRepository,
    private val notifRepo:           InAppNotificationRepository,
    private val passkeyRepo:         PasskeyRepository,
    private val identityDocRepo:     IdentityDocumentRepository,
    private val vehicleRepo:         VehicleRepository,
    private val reminderRepo:        ReminderRepository,
    private val trashAutoEmptyPrefs: TrashAutoEmptyPrefs,
    planPrefs:           PlanPrefs,
    activeVaultPrefs:    ActiveVaultPrefs,
    @ApplicationContext private val appContextInyectado: Context,
    @ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)

    private val fallbackUntitled: String get() = appContext.getString(R.string.archive_fallback_untitled)
    private val fallbackUnnamed:  String get() = appContext.getString(R.string.archive_fallback_unnamed)

    val isActiveVaultReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, vaultId, isInherited ->
        isInherited || (plan == UserPlan.FREE && vaultId != VaultMetadata.PRIMARY_VAULT_ID)
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited ||
        (planPrefs.effectivePlan() == UserPlan.FREE && activeVaultPrefs.activeVaultId != VaultMetadata.PRIMARY_VAULT_ID)
    )

    private val passwordsFlow: Flow<List<TrashItem>> =
        vaultRepo.observeTrash().map { list ->
            list.map { e ->
                TrashItem.Password(
                    id        = e.id,
                    title     = e.title.ifEmpty { fallbackUntitled },
                    deletedAt = e.deletedAt,
                    tags      = e.tags,
                    category  = e.category
                )
            }
        }

    private val bankFlow: Flow<List<TrashItem>> =
        bankRepo.observeTrash().map { list ->
            list.map { e ->
                TrashItem.BankAccount(
                    id        = e.id,
                    title     = e.bankName.ifEmpty { fallbackUnnamed },
                    deletedAt = e.deletedAt,
                    tags      = e.tags,
                    category  = e.category
                )
            }
        }

    private val notesFlow: Flow<List<TrashItem>> =
        noteRepo.observeTrash().map { list ->
            list.map { e ->
                TrashItem.Note(
                    id        = e.id,
                    title     = e.title.ifEmpty { fallbackUntitled },
                    deletedAt = e.deletedAt,
                    tags      = e.tags,
                    category  = e.category
                )
            }
        }

    private val customFlow: Flow<List<TrashItem>> =
        customRepo.observeTrash().map { list ->
            list.map { e ->
                TrashItem.Custom(
                    id        = e.id,
                    title     = e.title.ifEmpty { fallbackUntitled },
                    deletedAt = e.deletedAt,
                    tags      = e.tags,
                    category  = e.category
                )
            }
        }

    private val cardsFlow: Flow<List<TrashItem>> =
        cardRepo.observeTrash().map { list ->
            list.map { e ->
                TrashItem.Card(
                    id        = e.id,
                    title     = e.issuer.ifEmpty { e.brand }.ifEmpty { fallbackUnnamed },
                    deletedAt = e.deletedAt,
                    tags      = e.tags,
                    category  = e.category
                )
            }
        }

    private val twoFactorFlow: Flow<List<TrashItem>> =
        twoFactorRepo.observeTrash().map { list ->
            list.map { e ->
                TrashItem.TwoFactor(
                    id        = e.id,
                    title     = e.name.ifEmpty { e.issuer }.ifEmpty { fallbackUnnamed },
                    deletedAt = e.deletedAt,
                    tags      = e.tags,
                    category  = e.category
                )
            }
        }

    private val cryptoFlow: Flow<List<TrashItem>> =
        cryptoRepo.observeTrash().map { list ->
            list.map { e ->
                TrashItem.CryptoWallet(
                    id        = e.id,
                    title     = e.asset.ifEmpty { e.network }.ifEmpty { fallbackUnnamed },
                    deletedAt = e.deletedAt,
                    tags      = e.tags,
                    category  = e.category
                )
            }
        }

    private val notificationsFlow: Flow<List<TrashItem>> =
        notifRepo.observeTrash().map { list ->
            list.map { n ->
                TrashItem.Notification(
                    id                 = n.id.toString(),
                    title              = n.title,
                    deletedAt          = n.trashedAt,
                    notificationType   = n.type
                )
            }
        }

    private val passkeysFlow: Flow<List<TrashItem>> =
        passkeyRepo.observeTrash().map { list ->
            list.map { p ->
                TrashItem.Passkey(
                    id        = p.credentialId,
                    title     = p.userName.ifEmpty { p.rpName }.ifEmpty { fallbackUnnamed },
                    deletedAt = p.deletedAt,
                    tags      = p.tags.split(",").filter { it.isNotBlank() },
                    category  = EntryCategory.valueOf(
                        p.category.ifEmpty { "SIN_CATEGORIZAR" }
                    )
                )
            }
        }

    private val identityDocFlow: Flow<List<TrashItem>> =
        identityDocRepo.observeTrash().map { list ->
            list.map { e ->
                TrashItem.IdentityDocument(
                    id        = e.id,
                    title     = e.holderName.ifEmpty { e.docType }.ifEmpty { fallbackUnnamed },
                    deletedAt = e.deletedAt,
                    tags      = e.tags,
                    category  = e.category
                )
            }
        }

    private val vehicleFlow: Flow<List<TrashItem>> =
        vehicleRepo.observeTrash().map { list ->
            list.map { e ->
                TrashItem.Vehicle(
                    id        = e.id,
                    title     = listOf(e.make, e.model).filter { it.isNotEmpty() }.joinToString(" ").ifEmpty { fallbackUnnamed },
                    deletedAt = e.deletedAt,
                    tags      = e.tags,
                    category  = e.category
                )
            }
        }

    private val reminderFlow: Flow<List<TrashItem>> =
        reminderRepo.observeTrashed().map { list ->
            list.map { r ->
                TrashItem.Reminder(
                    id        = r.id,
                    title     = r.name.ifEmpty { fallbackUntitled },
                    deletedAt = r.trashedAt
                )
            }
        }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _pendingDeleteIds = MutableStateFlow<Set<String>>(emptySet())

    private val allTrashItems: Flow<List<TrashItem>> = combine(
        listOf(passwordsFlow, bankFlow, notesFlow, customFlow, cardsFlow, twoFactorFlow, cryptoFlow, notificationsFlow, passkeysFlow, identityDocFlow, vehicleFlow, reminderFlow)
    ) { arrays ->
        arrays.flatMap { it }.sortedByDescending { it.deletedAt }
    }

    val trashItems: StateFlow<List<TrashItem>> = combine(allTrashItems, _pendingDeleteIds) { items, pending ->
        if (pending.isEmpty()) items else items.filter { it.id !in pending }
    }.onEach { _isLoading.value = false }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val availableTypes: StateFlow<Set<String>> = trashItems
        .map { items -> items.mapNotNull { it::class.simpleName }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val availableCategories: StateFlow<Set<EntryCategory>> = trashItems
        .map { items -> items.map { it.category }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val availableTags: StateFlow<List<String>> = trashItems
        .map { items -> items.flatMap { it.tags }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchQuery        = MutableStateFlow("")
    private val _selectedTypes      = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedCategories = MutableStateFlow<Set<EntryCategory>>(emptySet())
    private val _selectedTags       = MutableStateFlow<Set<String>>(emptySet())
    private val _sortOrder          = MutableStateFlow(OrdenEnSesion.leer("trash", SortOrder.NEWEST))

    val searchQuery:        StateFlow<String>             = _searchQuery.asStateFlow()
    val selectedTypes:      StateFlow<Set<String>>        = _selectedTypes.asStateFlow()
    val selectedCategories: StateFlow<Set<EntryCategory>> = _selectedCategories.asStateFlow()
    val selectedTags:       StateFlow<Set<String>>        = _selectedTags.asStateFlow()
    val sortOrder:          StateFlow<SortOrder>          = _sortOrder.asStateFlow()

    private data class TrashFilters(
        val query:      String,
        val types:      Set<String>,
        val categories: Set<EntryCategory>,
        val tags:       Set<String>,
        val sort:       SortOrder
    )

    private val filterState = combine(
        _searchQuery, _selectedTypes, _selectedCategories, _selectedTags, _sortOrder
    ) { q, types, cats, tags, sort ->
        TrashFilters(q, types, cats, tags, sort)
    }

    val filteredItems: StateFlow<List<TrashItem>> = combine(trashItems, filterState) { items, f ->
        val filtered = items.filter { item ->
            val matchesSearch   = f.query.isBlank() || item.title.contains(f.query, ignoreCase = true)
            val matchesType     = f.types.isEmpty()      || item::class.simpleName in f.types
            val matchesCategory = f.categories.isEmpty() || item.category in f.categories
            val matchesTags     = f.tags.isEmpty()       || item.tags.any { it in f.tags }
            matchesSearch && matchesType && matchesCategory && matchesTags
        }
        when (f.sort) {
            SortOrder.A_Z    -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.Z_A    -> filtered.sortedByDescending { it.title.lowercase() }
            SortOrder.NEWEST -> filtered.sortedByDescending { it.deletedAt ?: 0L }
            SortOrder.OLDEST -> filtered.sortedBy { it.deletedAt ?: Long.MAX_VALUE }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSearchQuery(query: String)  { _searchQuery.value = query }
    fun setSortOrder(order: SortOrder) { OrdenEnSesion.guardar("trash", order); _sortOrder.value = order }

    fun applyFilters(types: Set<String>, categories: Set<EntryCategory>, tags: Set<String>) {
        _selectedTypes.value      = types
        _selectedCategories.value = categories
        _selectedTags.value       = tags
    }

    fun clearFilters() {
        _searchQuery.value        = ""
        _selectedTypes.value      = emptySet()
        _selectedCategories.value = emptySet()
        _selectedTags.value       = emptySet()
    }

    private val _autoEmptyDays = MutableStateFlow(trashAutoEmptyPrefs.getPeriodDays())
    val autoEmptyDays: StateFlow<Int> = _autoEmptyDays.asStateFlow()

    init {
        viewModelScope.launch {
            val days = _autoEmptyDays.value
            if (days > 0) {
                val now      = System.currentTimeMillis()
                val periodMs = days * 86_400_000L
                combine(
                    listOf(passwordsFlow, bankFlow, notesFlow, customFlow, cardsFlow, twoFactorFlow, cryptoFlow, notificationsFlow, passkeysFlow, identityDocFlow, vehicleFlow, reminderFlow)
                ) { arrays -> arrays.flatMap { it } }
                .first()
                .forEach { item ->
                    val deletedAt = item.deletedAt ?: return@forEach
                    if (now - deletedAt >= periodMs) permanentlyDeleteItem(item)
                }
            }
        }
    }

    fun setAutoEmptyDays(days: Int) {
        trashAutoEmptyPrefs.setPeriodDays(days)
        _autoEmptyDays.value = days
    }

    fun restore(item: TrashItem) = viewModelScope.launch {
        when (item) {
            is TrashItem.Password      -> vaultRepo.restore(item.id)
            is TrashItem.BankAccount   -> bankRepo.restore(item.id)
            is TrashItem.Note          -> noteRepo.restore(item.id)
            is TrashItem.Custom        -> customRepo.restore(item.id)
            is TrashItem.Card          -> cardRepo.restore(item.id)
            is TrashItem.TwoFactor     -> twoFactorRepo.restore(item.id)
            is TrashItem.CryptoWallet  -> cryptoRepo.restore(item.id)
            is TrashItem.Notification  -> notifRepo.restoreFromTrash(item.id.toLong())
            is TrashItem.Passkey           -> passkeyRepo.restore(item.id)
            is TrashItem.IdentityDocument  -> identityDocRepo.restore(item.id)
            is TrashItem.Vehicle           -> vehicleRepo.restore(item.id)
            is TrashItem.Reminder          -> reminderRepo.restore(item.id)
        }
    }

    fun deletePermanently(item: TrashItem) = deleteInBackground(listOf(item))

    private fun deleteInBackground(items: List<TrashItem>) {
        val ids = items.map { it.id }.toSet()
        _pendingDeleteIds.update { it + ids }
        appScope.launch {
            try {
                items.forEach { item -> runCatching { permanentlyDeleteItem(item) } }
            } finally {
                _pendingDeleteIds.update { it - ids }
            }
        }
    }

    private suspend fun permanentlyDeleteItem(item: TrashItem) {
        when (item) {
            is TrashItem.Password      -> vaultRepo.deletePermanently(item.id)
            is TrashItem.BankAccount   -> bankRepo.deletePermanently(item.id)
            is TrashItem.Note          -> noteRepo.deletePermanently(item.id)
            is TrashItem.Custom        -> customRepo.deletePermanently(item.id)
            is TrashItem.Card          -> cardRepo.deletePermanently(item.id)
            is TrashItem.TwoFactor     -> twoFactorRepo.deletePermanently(item.id)
            is TrashItem.CryptoWallet  -> cryptoRepo.deletePermanently(item.id)
            is TrashItem.Notification  -> notifRepo.permanentlyDelete(item.id.toLong())
            is TrashItem.Passkey           -> passkeyRepo.deletePermanently(item.id)
            is TrashItem.IdentityDocument  -> identityDocRepo.deletePermanently(item.id)
            is TrashItem.Vehicle           -> vehicleRepo.deletePermanently(item.id)
            is TrashItem.Reminder          -> reminderRepo.permanentlyDelete(item.id)
        }
    }

    fun restoreAll() = viewModelScope.launch {
        trashItems.value.forEach { item ->
            when (item) {
                is TrashItem.Password      -> vaultRepo.restore(item.id)
                is TrashItem.BankAccount   -> bankRepo.restore(item.id)
                is TrashItem.Note          -> noteRepo.restore(item.id)
                is TrashItem.Custom        -> customRepo.restore(item.id)
                is TrashItem.Card          -> cardRepo.restore(item.id)
                is TrashItem.TwoFactor     -> twoFactorRepo.restore(item.id)
                is TrashItem.CryptoWallet  -> cryptoRepo.restore(item.id)
                is TrashItem.Notification  -> notifRepo.restoreFromTrash(item.id.toLong())
                is TrashItem.Passkey           -> passkeyRepo.restore(item.id)
                is TrashItem.IdentityDocument  -> identityDocRepo.restore(item.id)
                is TrashItem.Vehicle           -> vehicleRepo.restore(item.id)
                is TrashItem.Reminder          -> reminderRepo.restore(item.id)
            }
        }
    }

    fun emptyTrash() {
        val visibles = trashItems.value
        val ids = visibles.map { it.id }.toSet()
        _pendingDeleteIds.update { it + ids }
        appScope.launch {
            try { emptyTrashNow() } finally { _pendingDeleteIds.update { it - ids } }
        }
    }

    private suspend fun emptyTrashNow() {
        vaultRepo.emptyTrash()
        bankRepo.emptyTrash()
        noteRepo.emptyTrash()
        customRepo.emptyTrash()
        cardRepo.emptyTrash()
        twoFactorRepo.emptyTrash()
        cryptoRepo.emptyTrash()
        notifRepo.permanentlyDeleteAllTrashed()
        passkeyRepo.emptyTrash()
        identityDocRepo.emptyTrash()
        vehicleRepo.emptyTrash()
        reminderRepo.observeTrashed().first().forEach { r -> reminderRepo.permanentlyDelete(r.id) }
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

    fun bulkRestore() {
        val itemsToRestore = trashItems.value.filter { it.id in _selectedIds.value }
        viewModelScope.launch {
            itemsToRestore.forEach { item ->
                when (item) {
                    is TrashItem.Password      -> vaultRepo.restore(item.id)
                    is TrashItem.BankAccount   -> bankRepo.restore(item.id)
                    is TrashItem.Note          -> noteRepo.restore(item.id)
                    is TrashItem.Custom        -> customRepo.restore(item.id)
                    is TrashItem.Card          -> cardRepo.restore(item.id)
                    is TrashItem.TwoFactor     -> twoFactorRepo.restore(item.id)
                    is TrashItem.CryptoWallet  -> cryptoRepo.restore(item.id)
                    is TrashItem.Notification  -> notifRepo.restoreFromTrash(item.id.toLong())
                    is TrashItem.Passkey           -> passkeyRepo.restore(item.id)
                    is TrashItem.IdentityDocument  -> identityDocRepo.restore(item.id)
                    is TrashItem.Vehicle           -> vehicleRepo.restore(item.id)
                    is TrashItem.Reminder          -> reminderRepo.restore(item.id)
                }
            }
            _selectedIds.value = emptySet()
        }
    }

    fun bulkDeletePermanently() {
        val itemsToDelete = trashItems.value.filter { it.id in _selectedIds.value }
        _selectedIds.value = emptySet()
        deleteInBackground(itemsToDelete)
    }

    private val _scrollHighlightPending = AtomicBoolean(false)
    fun consumeScrollHighlight(): Boolean = _scrollHighlightPending.compareAndSet(true, false)
}