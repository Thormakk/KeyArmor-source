package com.thormakk.keyarmor.presentation.screens.creditcard

import com.thormakk.keyarmor.security.OrdenEnSesion
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AttachmentRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.SnapshotRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.components.selection.BulkMode
import com.thormakk.keyarmor.presentation.screens.search.fuzzyMatchesPrimary
import com.thormakk.keyarmor.presentation.screens.search.shouldRunFuzzy
import com.thormakk.keyarmor.presentation.screens.search.textContains
import com.thormakk.keyarmor.presentation.screens.vault.QuickFilter
import com.thormakk.keyarmor.presentation.screens.vault.composeTopQuickFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import com.thormakk.keyarmor.presentation.util.sinOcultos
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

private val CARD_COLLATOR: Collator = Collator.getInstance(Locale.forLanguageTag("es")).apply {
    strength = Collator.PRIMARY
}

enum class CardSortOrder { A_Z, Z_A, NEWEST, OLDEST }

private data class CardFiltersSnapshot(
    val tags:               Set<String>,
    val onlyFavorites:      Boolean,
    val onlyWithAttachments: Boolean,
    val sortOrder:          CardSortOrder,
    val onlyProtected:      Boolean
)

data class CreditCardsUiState(
    val cards:               List<CreditCard> = emptyList(),
    val fuzzyCards:          List<CreditCard> = emptyList(),
    val isLoading:           Boolean          = true,
    val onlyFavorites:       Boolean          = false,
    val onlyProtected:       Boolean = false,
    val onlyWithAttachments: Boolean          = false,
    val query:               String           = "",
    val sortOrder:           CardSortOrder    = CardSortOrder.NEWEST,
    val selectedCategory:    EntryCategory?  = null,
    val selectedTags:        Set<String>     = emptySet()
)

@HiltViewModel
class CreditCardsViewModel @Inject constructor(
    private val repository:             CreditCardRepository,
    private val tagRepository:          TagRepository,
    private val snapshotRepository:     SnapshotRepository,
    attachmentRepo:         AttachmentRepository,
    planPrefs:              PlanPrefs,
    private val activeVaultPrefs:       ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val pinnedHubPrefs:              PinnedHubPrefs,
) : ViewModel() {

    val pinnedIds: StateFlow<Set<String>> =
        pinnedHubPrefs.pinnedRefsFlow
            .map { refs -> refs.map { it.id }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin(id: String, type: HubEntryType) {
        if (pinnedHubPrefs.isPinned(id)) {
            pinnedHubPrefs.unpin(id)
        } else {
            val ok = pinnedHubPrefs.pin(id, type, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    val isActiveVaultReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, vaultId, isInherited ->
        isInherited || (plan == UserPlan.FREE && vaultId != VaultMetadata.PRIMARY_VAULT_ID)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited ||
        (planPrefs.effectivePlan() == UserPlan.FREE && activeVaultPrefs.activeVaultId != VaultMetadata.PRIMARY_VAULT_ID))

    val attachmentIds: StateFlow<Set<String>> =
        attachmentRepo.observeEntryIdsWithAttachments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())


    private val _onlyFavorites       = MutableStateFlow(false)
    private val _onlyProtected       = MutableStateFlow(false)
    private val _onlyWithAttachments = MutableStateFlow(false)
    private val _query               = MutableStateFlow("")
    private val _sortOrder           = MutableStateFlow(OrdenEnSesion.leer("creditcard", CardSortOrder.NEWEST))
    private val _selectedCategory = MutableStateFlow<EntryCategory?>(null)
    private val _selectedTags     = MutableStateFlow<Set<String>>(emptySet())

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val categoryCountMap: StateFlow<Map<EntryCategory, Int>> = repository.observeCards()
        .map { list -> list.groupingBy { it.category }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val tagCountMap: StateFlow<Map<String, Int>> = repository.observeCards()
        .map { list -> list.flatMap { it.tags }.filter { it.isNotBlank() }.groupingBy { it.trim() }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val quickFilters: StateFlow<List<QuickFilter>> = combine(
        repository.observeCards(),
        tagRepository.observeDefaultTagNames()
    ) { cards, defaultTags ->
        val tagImportant = defaultTags.first
        val tagWork      = defaultTags.third

        val categoryCounts = EntryCategory.entries
            .asSequence()
            .filter { it != EntryCategory.SIN_CATEGORIZAR }
            .map { cat -> QuickFilter.Category(cat) to cards.count { it.category == cat } }
            .toList()

        val tagCounts = cards
            .flatMap { it.tags.map { t -> t.trim() } }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .map { (tag, count) -> QuickFilter.Tag(tag) to count }

        val ranked = (categoryCounts + tagCounts)
            .filter { it.second > 0 }
            .sortedWith(
                compareByDescending<Pair<QuickFilter, Int>> { it.second }
                    .thenBy { it.first.key }
            )
            .map { it.first }

        val defaults: List<QuickFilter> = listOf(
            QuickFilter.Tag(tagImportant),
            QuickFilter.Tag(defaultTags.second),
            QuickFilter.Tag(tagWork)
        )
        composeTopQuickFilters(ranked, defaults)
    }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _hiddenIds = MutableStateFlow<Set<String>>(emptySet())
    private val _bulkDeleting = MutableStateFlow(false)
    val bulkDeleting: StateFlow<Boolean> = _bulkDeleting.asStateFlow()

    val uiState: StateFlow<CreditCardsUiState> = combine(
        combine(repository.observeCards().sinOcultos(_hiddenIds) { it.id }, _query, _selectedCategory) { c, q, cat -> Triple(c, q, cat) },
        combine(_selectedTags, _onlyFavorites, _onlyWithAttachments, _sortOrder, _onlyProtected) { t, f, a, o, p -> CardFiltersSnapshot(t, f, a, o, p) }
    ) { (cards, query, category), filters ->
        val q = query.trim().lowercase(Locale.ROOT)
        val filtered = cards
            .filter { if (filters.onlyFavorites) it.isFavorite else true }
            .filter { !filters.onlyProtected || it.isProtected }
            .filter { !filters.onlyWithAttachments || it.id in attachmentIds.value }
            .filter { if (category != null) it.category == category else true }
            .filter { card ->
                filters.tags.isEmpty() || filters.tags.all { tag ->
                    card.tags.any { it.trim().equals(tag, ignoreCase = true) }
                }
            }
            .filter { card ->
                q.isBlank() ||
                textContains(card.brand, q) || textContains(card.issuer, q) ||
                textContains(card.cardholder, q) || textContains(card.cardNumber, q)
            }
        val sorted = when (filters.sortOrder) {
            CardSortOrder.A_Z    -> filtered.sortedWith { a, b -> CARD_COLLATOR.compare(a.brand.ifEmpty { a.issuer }, b.brand.ifEmpty { b.issuer }) }
            CardSortOrder.Z_A    -> filtered.sortedWith { a, b -> CARD_COLLATOR.compare(b.brand.ifEmpty { b.issuer }, a.brand.ifEmpty { a.issuer }) }
            CardSortOrder.NEWEST -> filtered.sortedByDescending { it.updatedAt }
            CardSortOrder.OLDEST -> filtered.sortedBy { it.updatedAt }
        }
        val pinned   = sorted.filter { it.isPinned }
        val unpinned = sorted.filter { !it.isPinned }
        val exact = pinned + unpinned
        val fuzzyCards = if (shouldRunFuzzy(query)) {
            val exactIds = exact.mapTo(HashSet()) { it.id }
            cards
                .filter { it.id !in exactIds }
                .filter { if (filters.onlyFavorites) it.isFavorite else true }
                .filter { !filters.onlyProtected || it.isProtected }
                .filter { !filters.onlyWithAttachments || it.id in attachmentIds.value }
                .filter { if (category != null) it.category == category else true }
                .filter { card -> filters.tags.isEmpty() || filters.tags.all { tag -> card.tags.any { it.trim().equals(tag, ignoreCase = true) } } }
                .filter { it.fuzzyMatchesPrimary(query) }
        } else emptyList()
        CreditCardsUiState(
            cards               = exact,
            fuzzyCards          = fuzzyCards,
            isLoading           = false,
            onlyFavorites       = filters.onlyFavorites,
            onlyProtected       = filters.onlyProtected,
            onlyWithAttachments = filters.onlyWithAttachments,
            query               = query,
            sortOrder        = filters.sortOrder,
            selectedCategory = category,
            selectedTags     = filters.tags
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreditCardsUiState()
    )

    fun onFavoritesToggle(value: Boolean)       { _onlyFavorites.value = value }
    fun onProtectedFilterToggle(value: Boolean) { _onlyProtected.value = value }
    fun onAttachmentFilterToggle()              { _onlyWithAttachments.value = !_onlyWithAttachments.value }
    fun onQueryChange(v: String)                { _query.value = v }
    fun onSortOrderChange(o: CardSortOrder)     { OrdenEnSesion.guardar("creditcard", o); _sortOrder.value = o }
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

    fun deleteTag(id: String) {
        viewModelScope.launch {
            val name = tagRepository.observeTags().first().find { it.id == id }?.name
            tagRepository.deleteTag(id)
            if (name != null) _selectedTags.value = _selectedTags.value - name
        }
    }

    fun setFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch { repository.setFavorite(id, isFavorite) }
    }

    fun togglePin(card: CreditCard) {
        viewModelScope.launch {
            if (!card.isPinned && repository.countPinned() >= MAX_PINNED) {
                _pinLimitReached.value = true
                return@launch
            }
            repository.setPin(card.id, !card.isPinned)
        }
    }

    fun onPinLimitDismissed() { _pinLimitReached.value = false }

    private val _pinLimitReached = MutableStateFlow(false)
    val pinLimitReached: StateFlow<Boolean> = _pinLimitReached


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun bulkMoveTo(targetVaultId: String) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            repository.moveEntriesToVault(ids, targetVaultId)
            _selectedIds.value = emptySet()
        }
    }

    fun bulkCopyTo(targetVaultId: String) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited) {
                activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
            } else null
            repository.copyEntriesToVault(ids, targetVaultId, inheritedFrom)
            _selectedIds.value = emptySet()
        }
    }

    fun moveEntryTo(entryId: String, targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(entryId), targetVaultId) }
    }

    fun copyEntryTo(entryId: String, targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited) {
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        } else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(entryId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    val isSelectionMode: Boolean get() = _selectedIds.value.isNotEmpty()

    fun toggleSelection(id: String) {
        _selectedIds.update { if (id in it) it - id else it + id }
    }

    fun selectAll() {
        _selectedIds.value = uiState.value.cards.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun archiveItem(id: String) { viewModelScope.launch { repository.archive(id); pinnedHubPrefs.unpin(id) } }
    fun softDeleteItem(id: String) { viewModelScope.launch { if (repository.softDelete(id)) pinnedHubPrefs.unpin(id) } }

    fun bulkSetProtected(value: Boolean) {
        if (value && isFreeUser.value) return
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            ids.forEach { repository.setProtected(it, value) }
            _selectedIds.value = emptySet()
        }
    }

    private val _protectedSkipped = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val protectedSkipped: SharedFlow<Int> = _protectedSkipped.asSharedFlow()

    fun bulkArchive() {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            repository.archiveBatch(ids)
            ids.forEach { pinnedHubPrefs.unpin(it) }
            _selectedIds.value = emptySet()
        }
    }

    fun bulkDelete() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty() || _bulkDeleting.value) return
        _bulkDeleting.value = true
        viewModelScope.launch {
            try {
                val protegidas = repository.protectedAmong(ids)
                _hiddenIds.value = ids.filter { it !in protegidas }.toSet()
                _selectedIds.value = emptySet()
                withContext(NonCancellable) {
                    if (ids.size >= 3) {
                        snapshotRepository.createSnapshotBestEffort(SnapshotTrigger.PRE_BULK_DELETE)
                    }
                    repository.softDeleteBatch(ids)
                    ids.filter { it !in protegidas }.forEach { pinnedHubPrefs.unpin(it) }
                    if (protegidas.isNotEmpty()) _protectedSkipped.tryEmit(protegidas.size)
                    delay(400)
                }
            } finally {
                _hiddenIds.value = emptySet()
                _bulkDeleting.value = false
            }
        }
    }

    fun bulkUpdateCategory(category: EntryCategory, mode: BulkMode) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            uiState.value.cards
                .filter { it.id in ids }
                .forEach { entry ->
                    val newCategory = when (mode) {
                        BulkMode.ADDITIVE -> if (entry.category == EntryCategory.SIN_CATEGORIZAR) category else entry.category
                        BulkMode.REMOVE   -> EntryCategory.SIN_CATEGORIZAR
                        BulkMode.REPLACE  -> category
                    }
                    if (newCategory != entry.category) {
                        repository.saveCard(entry.copy(category = newCategory), soloMetadatos = true)
                    }
                }
            _selectedIds.value = emptySet()
        }
    }

    fun bulkUpdateTags(tags: Set<String>, mode: BulkMode) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            uiState.value.cards
                .filter { it.id in ids }
                .forEach { entry ->
                    val newTags = when (mode) {
                        BulkMode.ADDITIVE -> (entry.tags + tags).distinct()
                        BulkMode.REMOVE   -> entry.tags.filter { it !in tags }
                        BulkMode.REPLACE  -> tags.toList()
                    }
                    if (newTags != entry.tags) {
                        repository.saveCard(entry.copy(tags = newTags), soloMetadatos = true)
                    }
                }
            _selectedIds.value = emptySet()
        }
    }
}