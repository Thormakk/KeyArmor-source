package com.thormakk.keyarmor.presentation.screens.vault

import com.thormakk.keyarmor.security.OrdenEnSesion
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AttachmentRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.SnapshotRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.di.ApplicationScope
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.HubEntryType
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.components.selection.BulkMode
import com.thormakk.keyarmor.presentation.screens.search.fuzzyMatchesPrimary
import com.thormakk.keyarmor.presentation.screens.search.shouldRunFuzzy
import com.thormakk.keyarmor.presentation.screens.search.textContains
import com.thormakk.keyarmor.security.PasswordStrengthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

private val TITLE_COLLATOR: Collator = Collator.getInstance(Locale.forLanguageTag("es")).apply {
    strength = Collator.PRIMARY
}

private val TITLE_COMPARATOR: Comparator<VaultEntry> = Comparator { a, b ->
    val aIsDigit = a.title.firstOrNull()?.isDigit() ?: false
    val bIsDigit = b.title.firstOrNull()?.isDigit() ?: false
    when {
        aIsDigit && !bIsDigit -> -1
        !aIsDigit && bIsDigit ->  1
        else                  -> TITLE_COLLATOR.compare(a.title, b.title)
    }
}

enum class StrengthFilter { WEAK, MEDIUM, STRONG, NO_PASSWORD }

enum class SortOrder {
    A_Z,
    Z_A,
    NEWEST,
    OLDEST
}

private fun passwordStrength(password: String): Int =
    PasswordStrengthCalculator.calculate(password)

private data class FiltersSnapshot(
    val tags: Set<String>,
    val onlyFavorites: Boolean,
    val onlyWithAttachments: Boolean,
    val onlyWithTwoFactor: Boolean,
    val onlyWithPasskey: Boolean,
    val onlyProtected: Boolean,
    val pendingDeleteId: String?,
    val strengthFilters: Set<StrengthFilter>,
    val sortOrder: SortOrder
)

private data class BoolFilterFlags(
    val favorites:   Boolean,
    val attachments: Boolean,
    val twoFactor:   Boolean,
    val passkey:     Boolean,
    val protected:   Boolean
)

sealed class QuickFilter {
    abstract val key: String
    data class Category(val category: EntryCategory) : QuickFilter() {
        override val key: String = "cat:${category.name}"
    }
    data class Tag(val tag: String) : QuickFilter() {
        override val key: String = "tag:$tag"
    }
}

fun composeTopQuickFilters(
    ranked: List<QuickFilter>,
    defaults: List<QuickFilter>,
): List<QuickFilter> {
    fun normKey(q: QuickFilter): String = when (q) {
        is QuickFilter.Tag      -> "tag:" + q.tag.trim().lowercase(Locale.ROOT)
        is QuickFilter.Category -> q.key
    }
    return (ranked + defaults).distinctBy { normKey(it) }.take(3)
}

data class VaultUiState(
    val entries: List<VaultEntry>          = emptyList(),
    val fuzzyEntries: List<VaultEntry>     = emptyList(),
    val query: String                      = "",
    val selectedCategory: EntryCategory?   = null,
    val selectedTags: Set<String>          = emptySet(),
    val onlyFavorites: Boolean             = false,
    val onlyWithAttachments: Boolean       = false,
    val onlyWithTwoFactor: Boolean         = false,
    val onlyWithPasskey: Boolean           = false,
    val onlyProtected: Boolean             = false,
    val isLoading: Boolean                 = true,
    val activeStrengthFilters: Set<StrengthFilter> = emptySet(),
    val sortOrder: SortOrder               = SortOrder.A_Z,
    val entryIdsWithAttachments: Set<String> = emptySet(),
    val entryIdsWithTwoFactor:   Set<String> = emptySet(),
    val entryIdsWithPasskey:     Set<String> = emptySet()
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository:             VaultRepository,
    private val tagRepository:          TagRepository,
    planPrefs:              PlanPrefs,
    private val activeVaultPrefs:       ActiveVaultPrefs,
    private val snapshotRepository:     SnapshotRepository,
    private val vaultMetadataRepository: VaultMetadataRepository,
    attachmentRepo:         AttachmentRepository,
    twoFactorRepository:    TwoFactorRepository,
    passkeyRepository:      PasskeyRepository,
    @ApplicationScope private val appScope: CoroutineScope,
    private val pinnedHubPrefs:         PinnedHubPrefs,
    savedStateHandle:                   SavedStateHandle
) : ViewModel() {

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


    private val _query            = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<EntryCategory?>(null)
    private val _selectedTags     = MutableStateFlow<Set<String>>(emptySet())
    private val _onlyFavorites       = MutableStateFlow(false)
    private val _onlyWithAttachments = MutableStateFlow(false)
    private val _onlyWithTwoFactor   = MutableStateFlow(false)
    private val _onlyWithPasskey     = MutableStateFlow(false)
    private val _onlyProtected       = MutableStateFlow(false)
    private val _sortOrder           = MutableStateFlow(OrdenEnSesion.leer("vault", SortOrder.A_Z))

    private val _strengthFilters  = MutableStateFlow<Set<StrengthFilter>>(
        savedStateHandle.get<String>("strengthFilter")
            ?.takeIf { it.isNotEmpty() }
            ?.let { name -> runCatching { StrengthFilter.valueOf(name) }.getOrNull() }
            ?.let { f -> setOf(f) }
            ?: emptySet()
    )

    private val _pendingDeleteId = MutableStateFlow<String?>(null)
    val pendingDeleteId: StateFlow<String?> = _pendingDeleteId

    private var deleteJob: Job? = null

    fun startPendingDelete(entryId: String) {
        commitPendingDelete()
        _pendingDeleteId.value = entryId
        scheduleDelete()
    }

    fun undoDelete() {
        deleteJob?.cancel()
        deleteJob = null
        _pendingDeleteId.value = null
    }

    fun resetDeleteTimer() {
        if (_pendingDeleteId.value != null) scheduleDelete()
    }

    private fun commitPendingDelete() {
        val id = _pendingDeleteId.value ?: return
        deleteJob?.cancel()
        deleteJob = null
        _pendingDeleteId.value = null
        viewModelScope.launch { repository.softDelete(id); pinnedHubPrefs.unpin(id) }
    }

    private fun scheduleDelete() {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            delay(5_000L)
            val id = _pendingDeleteId.value ?: return@launch
            repository.softDelete(id)
            pinnedHubPrefs.unpin(id)
            _pendingDeleteId.value = null
        }
    }

    override fun onCleared() {
        val id = _pendingDeleteId.value ?: return
        deleteJob?.cancel()
        appScope.launch(Dispatchers.IO) { repository.softDelete(id); pinnedHubPrefs.unpin(id) }
    }

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val quickFilters: StateFlow<List<QuickFilter>> = combine(
        repository.observeEntries(),
        tagRepository.observeDefaultTagNames()
    ) { entries, defaultTags ->
        val tagImportant = defaultTags.first
        val tagWork      = defaultTags.third

        val categoryCounts: List<Pair<QuickFilter, Int>> = EntryCategory.entries
            .filter { it != EntryCategory.SIN_CATEGORIZAR }
            .map { cat -> QuickFilter.Category(cat) to entries.count { it.category == cat } }

        val tagCounts: List<Pair<QuickFilter, Int>> = entries
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

    val categoryCountMap: StateFlow<Map<EntryCategory, Int>> = repository.observeEntries()
        .map { list -> list.groupingBy { it.category }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val tagCountMap: StateFlow<Map<String, Int>> = repository.observeEntries()
        .map { entries ->
            entries.flatMap { it.tags }
                .filter  { it.isNotBlank() }
                .groupingBy { it.trim() }
                .eachCount()
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    private val _hiddenIds = MutableStateFlow<Set<String>>(emptySet())
    private val _bulkDeleting = MutableStateFlow(false)
    val bulkDeleting: StateFlow<Boolean> = _bulkDeleting.asStateFlow()

    val uiState: StateFlow<VaultUiState> = combine(
        combine(repository.observeEntries().sinOcultos(_hiddenIds) { it.id }, _query, _selectedCategory) { e, q, c -> Triple(e, q, c) },
        combine(
            combine(
                _selectedTags,
                combine(_onlyFavorites, _onlyWithAttachments, _onlyWithTwoFactor, _onlyWithPasskey, _onlyProtected) { f, a, tf, pk, pr ->
                    BoolFilterFlags(f, a, tf, pk, pr)
                }
            ) { t, bf -> Pair(t, bf) },
            combine(_pendingDeleteId, _strengthFilters, _sortOrder) { p, s, o -> Triple(p, s, o) }
        ) { (t, bf), (p, s, o) ->
            FiltersSnapshot(t, bf.favorites, bf.attachments, bf.twoFactor, bf.passkey, bf.protected, p, s, o)
        },
        combine(
            attachmentRepo.observeEntryIdsWithAttachments().map { it.toSet() },
            twoFactorRepository.observeLinkedPasswordIds(),
            passkeyRepository.observeLinkedPasswordIds()
        ) { attach, tf, pk -> Triple(attach, tf, pk) }
    ) { (entries, query, category), filters, (attachIds, tfIds, pkIds) ->

        val duplicatePasswords = entries
            .filter { it.password.isNotEmpty() }
            .groupBy { it.password }
            .filter { it.value.size > 1 }
            .keys.toSet()

        val filtered = entries
            .filter { it.id != filters.pendingDeleteId }
            .filter { category == null || it.category == category }
            .filter { entry ->
                filters.tags.isEmpty() || filters.tags.all { tag ->
                    entry.tags.any { it.equals(tag, ignoreCase = true) }
                }
            }
            .filter { !filters.onlyFavorites || it.isFavorite }
            .filter { !filters.onlyWithAttachments || it.id in attachIds }
            .filter { !filters.onlyWithTwoFactor || it.id in tfIds }
            .filter { !filters.onlyWithPasskey || it.id in pkIds }
            .filter { !filters.onlyProtected || it.isProtected }
            .filter {
                val q = query.trim()
                query.isBlank() ||
                textContains(it.title, q) || textContains(it.username, q) ||
                textContains(it.email, q) || textContains(it.url, q) || textContains(it.notes, q)
            }
            .filter { entry ->
                filters.strengthFilters.isEmpty() || filters.strengthFilters.any { f ->
                    when (f) {
                        StrengthFilter.WEAK        -> entry.password.isNotEmpty() && passwordStrength(entry.password) in 0..1
                        StrengthFilter.MEDIUM      -> entry.password.isNotEmpty() &&
                            passwordStrength(entry.password) == 2
                        StrengthFilter.STRONG      -> entry.password.isNotEmpty() && passwordStrength(entry.password) >= 3
                        StrengthFilter.NO_PASSWORD -> entry.password.isEmpty()
                    }
                }
            }
            .let { list ->
                val sorted = when (filters.sortOrder) {
                    SortOrder.A_Z    -> list.sortedWith(TITLE_COMPARATOR)
                    SortOrder.Z_A    -> list.sortedWith(TITLE_COMPARATOR).reversed()
                    SortOrder.NEWEST -> list.sortedByDescending { it.updatedAt }
                    SortOrder.OLDEST -> list.sortedBy { it.updatedAt }
                }
                val pinned   = sorted.filter { it.isPinned }
                val unpinned = sorted.filter { !it.isPinned }
                pinned + unpinned
            }
        val fuzzyEntries = if (shouldRunFuzzy(query)) {
            val exactIds = filtered.mapTo(HashSet()) { it.id }
            entries
                .filter { it.id !in exactIds }
                .filter { it.id != filters.pendingDeleteId }
                .filter { category == null || it.category == category }
                .filter { entry -> filters.tags.isEmpty() || filters.tags.all { tag -> entry.tags.any { it.equals(tag, ignoreCase = true) } } }
                .filter { !filters.onlyFavorites || it.isFavorite }
                .filter { !filters.onlyWithAttachments || it.id in attachIds }
                .filter { !filters.onlyWithTwoFactor || it.id in tfIds }
                .filter { !filters.onlyWithPasskey || it.id in pkIds }
                .filter { !filters.onlyProtected || it.isProtected }
                .filter { it.fuzzyMatchesPrimary(query) }
                .let { list ->
                    val sorted = when (filters.sortOrder) {
                        SortOrder.A_Z    -> list.sortedWith(TITLE_COMPARATOR)
                        SortOrder.Z_A    -> list.sortedWith(TITLE_COMPARATOR).reversed()
                        SortOrder.NEWEST -> list.sortedByDescending { it.updatedAt }
                        SortOrder.OLDEST -> list.sortedBy { it.updatedAt }
                    }
                    sorted.filter { it.isPinned } + sorted.filter { !it.isPinned }
                }
        } else emptyList()

        VaultUiState(
            entries               = filtered,
            fuzzyEntries          = fuzzyEntries,
            query                 = query,
            selectedCategory      = category,
            selectedTags          = filters.tags,
            onlyFavorites         = filters.onlyFavorites,
            onlyWithAttachments   = filters.onlyWithAttachments,
            onlyWithTwoFactor     = filters.onlyWithTwoFactor,
            onlyWithPasskey       = filters.onlyWithPasskey,
            onlyProtected         = filters.onlyProtected,
            isLoading             = false,
            activeStrengthFilters       = filters.strengthFilters,
            sortOrder                   = filters.sortOrder,
            entryIdsWithAttachments     = attachIds,
            entryIdsWithTwoFactor       = tfIds,
            entryIdsWithPasskey         = pkIds
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = VaultUiState(isLoading = true)
    )

    fun onQueryChange(value: String)                  { _query.value = value }
    fun onFavoritesToggle(value: Boolean)             { _onlyFavorites.value = value }
    fun onAttachmentFilterToggle()                    { _onlyWithAttachments.value = !_onlyWithAttachments.value }
    fun onTwoFactorFilterToggle()                     { _onlyWithTwoFactor.value = !_onlyWithTwoFactor.value }
    fun onPasskeyFilterToggle()                       { _onlyWithPasskey.value = !_onlyWithPasskey.value }
    fun onProtectedFilterToggle(value: Boolean)       { _onlyProtected.value = value }
    fun onCategorySelected(cat: EntryCategory?)       { _selectedCategory.value = cat }
    fun onStrengthFiltersChange(filters: Set<StrengthFilter>) { _strengthFilters.value = filters }
    fun onSortOrderChange(order: SortOrder)           { OrdenEnSesion.guardar("vault", order); _sortOrder.value = order }

    fun onTagToggled(tag: String) {
        _selectedTags.value = _selectedTags.value.toMutableSet().apply {
            if (contains(tag)) remove(tag) else add(tag)
        }
    }

    fun onTagsApplied(tags: Set<String>) {
        _selectedTags.value = tags
    }


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

    fun toggleFavorite(entry: VaultEntry) {
        viewModelScope.launch { repository.setFavorite(entry.id, !entry.isFavorite) }
    }

    private val _pinLimitReached = MutableStateFlow(false)
    val pinLimitReached: StateFlow<Boolean> = _pinLimitReached

    fun togglePin(entry: VaultEntry) {
        viewModelScope.launch {
            if (!entry.isPinned && repository.countPinned() >= MAX_PINNED) {
                _pinLimitReached.value = true
                return@launch
            }
            repository.setPin(entry.id, !entry.isPinned)
        }
    }

    fun onPinLimitDismissed() { _pinLimitReached.value = false }

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

    companion object { private const val MAX_PINNED = 3 }

    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    private val _emptyVaultEvent = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val emptyVaultEvent: SharedFlow<Pair<String, String>> = _emptyVaultEvent.asSharedFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    val isSelectionMode: Boolean get() = _selectedIds.value.isNotEmpty()

    fun toggleSelection(id: String) {
        _selectedIds.update { if (id in it) it - id else it + id }
    }

    fun selectAll() {
        _selectedIds.value = uiState.value.entries.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun archiveEntry(entry: VaultEntry) {
        viewModelScope.launch { repository.archive(entry.id); pinnedHubPrefs.unpin(entry.id) }
    }

    fun softDeleteEntry(entry: VaultEntry) {
        viewModelScope.launch { repository.softDelete(entry.id); pinnedHubPrefs.unpin(entry.id) }
    }

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
            uiState.value.entries
                .filter { it.id in ids }
                .forEach { entry ->
                    val newCategory = when (mode) {
                        BulkMode.ADDITIVE -> if (entry.category == EntryCategory.SIN_CATEGORIZAR) category else entry.category
                        BulkMode.REMOVE   -> EntryCategory.SIN_CATEGORIZAR
                        BulkMode.REPLACE  -> category
                    }
                    if (newCategory != entry.category) {
                        repository.saveEntry(entry.copy(category = newCategory), soloMetadatos = true)
                    }
                }
            _selectedIds.value = emptySet()
        }
    }

    fun bulkUpdateTags(tags: Set<String>, mode: BulkMode) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            uiState.value.entries
                .filter { it.id in ids }
                .forEach { entry ->
                    val newTags = when (mode) {
                        BulkMode.ADDITIVE -> (entry.tags + tags).distinct()
                        BulkMode.REMOVE   -> entry.tags.filter { it !in tags }
                        BulkMode.REPLACE  -> tags.toList()
                    }
                    if (newTags != entry.tags) {
                        repository.saveEntry(entry.copy(tags = newTags), soloMetadatos = true)
                    }
                }
            _selectedIds.value = emptySet()
        }
    }

    fun bulkMoveTo(targetVaultId: String) {
        val ids = _selectedIds.value.toSet()
        val currentVaultId = activeVaultPrefs.activeVaultId
        viewModelScope.launch {
            repository.moveEntriesToVault(ids, targetVaultId)
            _selectedIds.value = emptySet()
            checkAndNotifyEmptyVault(currentVaultId)
        }
    }

    fun bulkCopyTo(targetVaultId: String) {
        val ids = _selectedIds.value.toSet()
        val inheritedFrom = inheritedFromCurrentVault()
        viewModelScope.launch {
            repository.copyEntriesToVault(ids, targetVaultId, inheritedFrom)
            _selectedIds.value = emptySet()
        }
    }

    fun moveEntryTo(entryId: String, targetVaultId: String) {
        val currentVaultId = activeVaultPrefs.activeVaultId
        viewModelScope.launch {
            repository.moveEntriesToVault(setOf(entryId), targetVaultId)
            checkAndNotifyEmptyVault(currentVaultId)
        }
    }

    fun copyEntryTo(entryId: String, targetVaultId: String) {
        val inheritedFrom = inheritedFromCurrentVault()
        viewModelScope.launch {
            repository.copyEntriesToVault(setOf(entryId), targetVaultId, inheritedFrom)
        }
    }

    fun removeInheritedFrom(entryId: String) {
        viewModelScope.launch { repository.removeInheritedFrom(entryId) }
    }

    private fun inheritedFromCurrentVault(): String? {
        if (!activeVaultPrefs.activeVaultIsInherited) return null
        return activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
    }

    private suspend fun checkAndNotifyEmptyVault(vaultId: String) {
        val remaining = repository.countActiveByVault(vaultId)
        if (remaining == 0) {
            val name = vaultMetadataRepository.getById(vaultId)?.name ?: return
            _emptyVaultEvent.tryEmit(vaultId to name)
        }
    }
}