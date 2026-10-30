package com.thormakk.keyarmor.presentation.screens.importexport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.export.ExportEntryRef
import com.thormakk.keyarmor.data.export.ExportEntryRow
import com.thormakk.keyarmor.data.export.ExportEntryType
import com.thormakk.keyarmor.data.export.ExportTypeSummary
import com.thormakk.keyarmor.data.export.ExportVaultSummary
import com.thormakk.keyarmor.data.export.PartialExportFormat
import com.thormakk.keyarmor.data.repository.AttachmentRepository
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.BankAccountRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.CryptoWalletRepository
import com.thormakk.keyarmor.data.repository.CustomEntryRepository
import com.thormakk.keyarmor.data.repository.ExportImportRepository
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.PasskeyRepository
import com.thormakk.keyarmor.data.repository.SecureNoteRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.TwoFactorRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.presentation.screens.vault.QuickFilter
import com.thormakk.keyarmor.presentation.screens.vault.composeTopQuickFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class PartialExportUiState(
    val isLoading:     Boolean                     = true,
    val vaults:        List<ExportVaultSummary>     = emptyList(),
    val expandedVaults: Set<String>                 = emptySet(),
    val expandedTypes:  Set<Pair<String, ExportEntryType>> = emptySet(),
    val loadedTypes:    Set<ExportEntryType>         = emptySet(),
    val selected:        Set<ExportEntryRef>          = emptySet(),
    val event:           PartialExportEvent?          = null,
    val hasHiddenEntries: Boolean                     = false,

    val includeAttachments:       Boolean = true,
    val selectionAttachmentCount: Int     = 0,
    val selectionAttachmentBytes: Long    = 0L,

    val searchQuery:       String              = "",
    val filterFavorites:   Boolean             = false,
    val filterAttachments: Boolean             = false,
    val filterCategory:    EntryCategory?      = null,
    val filterTags:        Set<String>         = emptySet(),
    val availableTags:     List<Tag>           = emptyList(),
    val defaultTagNames:   List<String>        = emptyList(),
    val allRows:           List<ExportEntryRow> = emptyList(),
    val allRowsLoading:    Boolean             = false,
    val allRowsLoaded:     Boolean             = false
) {
    val selectedCount: Int get() = selected.size
    val selectionHasNonPasswordTypes: Boolean
        get() = selected.any { it.type != ExportEntryType.PASSWORD }

    val isFiltering: Boolean
        get() = searchQuery.isNotBlank() || filterFavorites || filterAttachments ||
                filterCategory != null || filterTags.isNotEmpty()

    val filteredResults: List<ExportEntryRow>
        get() {
            if (!isFiltering) return emptyList()
            val query = searchQuery.trim()
            return allRows.filter { row ->
                (query.isBlank() || row.title.contains(query, ignoreCase = true) || row.subtitle.contains(query, ignoreCase = true)) &&
                (!filterFavorites   || row.favorite) &&
                (!filterAttachments || row.hasAttachments) &&
                (filterCategory == null || row.category == filterCategory) &&
                (filterTags.isEmpty() || row.tags.any { it in filterTags })
            }
        }

    val quickFilters: List<QuickFilter>
        get() {
            val categoryCounts = EntryCategory.entries
                .filter { it != EntryCategory.SIN_CATEGORIZAR }
                .map { cat -> QuickFilter.Category(cat) to allRows.count { it.category == cat } }
            val tagCounts = allRows
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
            val defaults = defaultTagNames.map { QuickFilter.Tag(it) }
            return composeTopQuickFilters(ranked, defaults)
        }

    val categoryCounts: Map<EntryCategory, Int>
        get() = allRows.groupingBy { it.category }.eachCount()

    val tagCounts: Map<String, Int>
        get() = allRows.flatMap { it.tags }.filter { it.isNotBlank() }.groupingBy { it }.eachCount()
}

sealed class PartialExportEvent {
    object ExportSuccess : PartialExportEvent()
    data class Error(val message: String) : PartialExportEvent()
}

@HiltViewModel
class PartialExportViewModel @Inject constructor(
    private val vaultMetadataRepository:    VaultMetadataRepository,
    private val vaultRepository:            VaultRepository,
    private val twoFactorRepository:        TwoFactorRepository,
    private val creditCardRepository:       CreditCardRepository,
    private val bankAccountRepository:      BankAccountRepository,
    private val cryptoWalletRepository:     CryptoWalletRepository,
    private val secureNoteRepository:       SecureNoteRepository,
    private val customEntryRepository:      CustomEntryRepository,
    private val passkeyRepository:          PasskeyRepository,
    private val identityDocumentRepository: IdentityDocumentRepository,
    private val vehicleRepository:          VehicleRepository,
    private val attachmentRepository:       AttachmentRepository,
    private val tagRepository:              TagRepository,
    private val exportImportRepository:     ExportImportRepository,
    private val authRepository:             AuthRepository,
    private val claveDeRespaldo:            com.thormakk.keyarmor.data.backup.ClaveDeRespaldo
) : ViewModel() {

    fun puedeUsarClave(): Boolean = claveDeRespaldo.confirmada()

    private val _uiState = MutableStateFlow(PartialExportUiState())
    val uiState: StateFlow<PartialExportUiState> = _uiState.asStateFlow()

    private val typeRowsCache = mutableMapOf<ExportEntryType, List<ExportEntryRow>>()
    private val typeLoadMutex = Mutex()

    private var attachmentEntryIdsCache: Set<String>? = null
    private val attachmentIdsMutex = Mutex()

    init {
        viewModelScope.launch {
            vaultMetadataRepository.observeAll().collectLatest { vaults ->
                loadSummaries(vaults)
            }
        }
        viewModelScope.launch {
            tagRepository.observeTags().collectLatest { tags ->
                _uiState.update { it.copy(availableTags = tags) }
            }
        }
        viewModelScope.launch {
            tagRepository.observeDefaultTagNames().collectLatest { (t1, t2, t3) ->
                _uiState.update { it.copy(defaultTagNames = listOf(t1, t2, t3)) }
            }
        }
        viewModelScope.launch {
            val hasHidden = exportImportRepository.countHiddenEntries() > 0
            _uiState.update { it.copy(hasHiddenEntries = hasHidden) }
        }
    }

    private suspend fun loadSummaries(vaults: List<VaultMetadata>) {
        val summaries = vaults.sortedBy { it.sortOrder }.map { vault ->
            val types = ExportEntryType.entries.mapNotNull { type ->
                val count = countForType(type, vault.id)
                if (count > 0) ExportTypeSummary(type, count) else null
            }
            ExportVaultSummary(vault, types)
        }
        _uiState.update { it.copy(vaults = summaries, isLoading = false) }
    }

    private suspend fun countForType(type: ExportEntryType, vaultId: String): Int = when (type) {
        ExportEntryType.PASSWORD          -> vaultRepository.countAllByVault(vaultId)
        ExportEntryType.TWO_FACTOR        -> twoFactorRepository.countAllByVault(vaultId)
        ExportEntryType.CREDIT_CARD       -> creditCardRepository.countAllByVault(vaultId)
        ExportEntryType.BANK_ACCOUNT      -> bankAccountRepository.countAllByVault(vaultId)
        ExportEntryType.CRYPTO_WALLET     -> cryptoWalletRepository.countAllByVault(vaultId)
        ExportEntryType.SECURE_NOTE       -> secureNoteRepository.countAllByVault(vaultId)
        ExportEntryType.CUSTOM_ENTRY      -> customEntryRepository.countAllByVault(vaultId)
        ExportEntryType.PASSKEY           -> passkeyRepository.countAllByVault(vaultId)
        ExportEntryType.IDENTITY_DOCUMENT -> identityDocumentRepository.countAllByVault(vaultId)
        ExportEntryType.VEHICLE           -> vehicleRepository.countAllByVault(vaultId)
    }

    fun toggleVaultExpanded(vaultId: String) {
        _uiState.update {
            val expanded = it.expandedVaults.toMutableSet()
            if (!expanded.add(vaultId)) expanded.remove(vaultId)
            it.copy(expandedVaults = expanded)
        }
    }

    fun toggleVaultFullSelection(vaultSummary: ExportVaultSummary) {
        val vaultId = vaultSummary.vault.id
        val totalCount = vaultSummary.types.sumOf { it.count }
        val currentlySelected = _uiState.value.selected.count { it.vaultId == vaultId }
        if (totalCount > 0 && currentlySelected == totalCount) {
            _uiState.update { it.copy(selected = it.selected.filterNot { ref -> ref.vaultId == vaultId }.toSet()) }
            return
        }
        viewModelScope.launch {
            vaultSummary.types.forEach { ensureTypeLoaded(it.type) }
            val refs = vaultSummary.types.flatMap { rowsFor(vaultId, it.type) }.map { it.ref }
            _uiState.update { it.copy(selected = it.selected + refs) }
        }
    }

    fun toggleTypeFullSelection(vaultId: String, type: ExportEntryType, totalCount: Int) {
        val actuales = _uiState.value.selected.count { it.vaultId == vaultId && it.type == type }
        if (totalCount > 0 && actuales == totalCount) {
            _uiState.update {
                it.copy(selected = it.selected.filterNot { ref -> ref.vaultId == vaultId && ref.type == type }.toSet())
            }
            return
        }
        viewModelScope.launch {
            ensureTypeLoaded(type)
            val refs = rowsFor(vaultId, type).map { it.ref }
            _uiState.update { it.copy(selected = it.selected + refs) }
        }
    }

    fun toggleTypeExpanded(vaultId: String, type: ExportEntryType) {
        val key = vaultId to type
        val wasExpanded = _uiState.value.expandedTypes.contains(key)
        _uiState.update {
            val expanded = it.expandedTypes.toMutableSet()
            if (!expanded.add(key)) expanded.remove(key)
            it.copy(expandedTypes = expanded)
        }
        if (!wasExpanded) viewModelScope.launch { ensureTypeLoaded(type) }
    }

    fun rowsFor(vaultId: String, type: ExportEntryType): List<ExportEntryRow> =
        typeRowsCache[type]?.filter { it.ref.vaultId == vaultId }.orEmpty()

    private suspend fun ensureTypeLoaded(type: ExportEntryType) {
        if (typeRowsCache.containsKey(type)) return
        typeLoadMutex.withLock {
            if (typeRowsCache.containsKey(type)) return@withLock
            typeRowsCache[type] = fetchRows(type)
            _uiState.update { it.copy(loadedTypes = it.loadedTypes + type) }
        }
    }

    private var allRowsLoadJob: kotlinx.coroutines.Job? = null

    fun ensureAllRowsLoaded() {
        if (_uiState.value.allRowsLoaded) return
        if (allRowsLoadJob?.isActive == true) return
        allRowsLoadJob = viewModelScope.launch { loadAllRows() }
    }

    private suspend fun awaitAllRowsLoaded() {
        if (_uiState.value.allRowsLoaded) return
        val job = allRowsLoadJob
        if (job != null && job.isActive) { job.join(); return }
        loadAllRows()
    }

    private suspend fun loadAllRows() {
        if (_uiState.value.allRowsLoaded) return
        _uiState.update { it.copy(allRowsLoading = true) }
        ExportEntryType.entries.forEach { ensureTypeLoaded(it) }
        val all = ExportEntryType.entries.flatMap { typeRowsCache[it].orEmpty() }
        _uiState.update { it.copy(allRows = all, allRowsLoading = false, allRowsLoaded = true) }
    }

    private suspend fun attachmentEntryIds(): Set<String> {
        attachmentEntryIdsCache?.let { return it }
        return attachmentIdsMutex.withLock {
            attachmentEntryIdsCache ?: attachmentRepository.getAllForBackup()
                .map { it.entryId }.toSet()
                .also { attachmentEntryIdsCache = it }
        }
    }

    private suspend fun fetchRows(type: ExportEntryType): List<ExportEntryRow> {
        val attachIds = if (type == ExportEntryType.TWO_FACTOR || type == ExportEntryType.PASSKEY)
            emptySet() else attachmentEntryIds()

        return when (type) {
            ExportEntryType.PASSWORD -> vaultRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref            = ExportEntryRef(it.vaultId, type, it.id),
                        title          = it.title.ifBlank { it.username },
                        subtitle       = it.username,
                        favorite       = it.isFavorite,
                        category       = it.category,
                        tags           = it.tags,
                        hasAttachments = it.id in attachIds
                    )
                }

            ExportEntryType.TWO_FACTOR -> twoFactorRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref      = ExportEntryRef(it.vaultId, type, it.id),
                        title    = it.name.ifBlank { it.issuer },
                        subtitle = it.issuer,
                        favorite = it.isFavorite,
                        category = it.category,
                        tags     = it.tags
                    )
                }

            ExportEntryType.CREDIT_CARD -> creditCardRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref            = ExportEntryRef(it.vaultId, type, it.id),
                        title          = "${it.issuer} ${it.brand}".trim(),
                        subtitle       = it.cardholder,
                        favorite       = it.isFavorite,
                        category       = it.category,
                        tags           = it.tags,
                        hasAttachments = it.id in attachIds
                    )
                }

            ExportEntryType.BANK_ACCOUNT -> bankAccountRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref            = ExportEntryRef(it.vaultId, type, it.id),
                        title          = it.bankName,
                        subtitle       = it.alias,
                        favorite       = it.isFavorite,
                        category       = it.category,
                        tags           = it.tags,
                        hasAttachments = it.id in attachIds
                    )
                }

            ExportEntryType.CRYPTO_WALLET -> cryptoWalletRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref            = ExportEntryRef(it.vaultId, type, it.id),
                        title          = it.asset.ifBlank { it.network },
                        subtitle       = it.network,
                        favorite       = it.isFavorite,
                        category       = it.category,
                        tags           = it.tags,
                        hasAttachments = it.id in attachIds
                    )
                }

            ExportEntryType.SECURE_NOTE -> secureNoteRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref            = ExportEntryRef(it.vaultId, type, it.id),
                        title          = it.title,
                        favorite       = it.isFavorite,
                        category       = it.category,
                        tags           = it.tags,
                        hasAttachments = it.id in attachIds
                    )
                }

            ExportEntryType.CUSTOM_ENTRY -> customEntryRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref            = ExportEntryRef(it.vaultId, type, it.id),
                        title          = it.title,
                        favorite       = it.isFavorite,
                        category       = it.category,
                        tags           = it.tags,
                        hasAttachments = it.id in attachIds
                    )
                }

            ExportEntryType.PASSKEY -> passkeyRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref      = ExportEntryRef(it.vaultId, type, it.credentialId),
                        title    = it.rpName.ifBlank { it.rpId },
                        subtitle = it.userName,
                        category = EntryCategory.fromString(it.category),
                        tags     = it.tagList
                    )
                }

            ExportEntryType.IDENTITY_DOCUMENT -> identityDocumentRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref            = ExportEntryRef(it.vaultId, type, it.id),
                        title          = it.holderName.ifBlank { it.docType },
                        subtitle       = it.docType,
                        favorite       = it.isFavorite,
                        category       = it.category,
                        tags           = it.tags,
                        hasAttachments = it.id in attachIds
                    )
                }

            ExportEntryType.VEHICLE -> vehicleRepository.getAllForBackup()
                .filter { !it.isDeleted && !it.isHidden }
                .map {
                    ExportEntryRow(
                        ref            = ExportEntryRef(it.vaultId, type, it.id),
                        title          = "${it.make} ${it.model}".trim(),
                        subtitle       = it.licensePlate,
                        favorite       = it.isFavorite,
                        category       = it.category,
                        tags           = it.tags,
                        hasAttachments = it.id in attachIds
                    )
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        if (query.isNotBlank()) ensureAllRowsLoaded()
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFunnelOpened() = ensureAllRowsLoaded()

    fun applyFilters(
        favorites:   Boolean,
        attachments: Boolean,
        category:    EntryCategory?,
        tags:        Set<String>
    ) {
        viewModelScope.launch {
            awaitAllRowsLoaded()
            _uiState.update {
                it.copy(
                    filterFavorites   = favorites,
                    filterAttachments = attachments,
                    filterCategory    = category,
                    filterTags        = tags
                )
            }
            autoSelectFilterMatches(favorites, attachments, category, tags)
        }
    }

    fun toggleQuickCategory(category: EntryCategory) {
        val turningOn = _uiState.value.filterCategory != category
        viewModelScope.launch {
            awaitAllRowsLoaded()
            _uiState.update { it.copy(filterCategory = if (it.filterCategory == category) null else category) }
            if (turningOn) {
                val s = _uiState.value
                autoSelectFilterMatches(s.filterFavorites, s.filterAttachments, s.filterCategory, s.filterTags)
            }
        }
    }

    fun toggleQuickTag(tag: String) {
        val turningOn = tag !in _uiState.value.filterTags
        viewModelScope.launch {
            awaitAllRowsLoaded()
            _uiState.update {
                val tags = it.filterTags.toMutableSet()
                if (!tags.add(tag)) tags.remove(tag)
                it.copy(filterTags = tags)
            }
            if (turningOn) {
                val s = _uiState.value
                autoSelectFilterMatches(s.filterFavorites, s.filterAttachments, s.filterCategory, s.filterTags)
            }
        }
    }

    fun clearSearchAndFilters() {
        _uiState.update {
            it.copy(
                searchQuery       = "",
                filterFavorites   = false,
                filterAttachments = false,
                filterCategory    = null,
                filterTags        = emptySet()
            )
        }
    }

    private fun autoSelectFilterMatches(
        favorites:   Boolean,
        attachments: Boolean,
        category:    EntryCategory?,
        tags:        Set<String>
    ) {
        if (!favorites && !attachments && category == null && tags.isEmpty()) return
        val matches = _uiState.value.allRows
            .filter { row ->
                (!favorites   || row.favorite) &&
                (!attachments || row.hasAttachments) &&
                (category == null || row.category == category) &&
                (tags.isEmpty() || row.tags.any { it in tags })
            }
            .map { it.ref }
        if (matches.isEmpty()) return
        _uiState.update { it.copy(selected = it.selected + matches) }
    }

    fun toggleSelected(ref: ExportEntryRef) {
        _uiState.update {
            val selected = it.selected.toMutableSet()
            if (!selected.add(ref)) selected.remove(ref)
            it.copy(selected = selected)
        }
    }

    fun clearSelection() = _uiState.update { it.copy(selected = emptySet()) }

    private suspend fun visibleRows(): List<ExportEntryRow> {
        awaitAllRowsLoaded()
        val state = _uiState.value
        return if (state.isFiltering) state.filteredResults else state.allRows
    }

    fun selectAllVisible() {
        viewModelScope.launch {
            val refs = visibleRows().map { it.ref }
            _uiState.update { it.copy(selected = it.selected + refs) }
        }
    }

    fun deselectAllVisible() {
        viewModelScope.launch {
            val refs = visibleRows().map { it.ref }.toSet()
            _uiState.update { it.copy(selected = it.selected - refs) }
        }
    }

    fun invertSelectionVisible() {
        viewModelScope.launch {
            val refs = visibleRows().map { it.ref }
            _uiState.update {
                val updated = it.selected.toMutableSet()
                refs.forEach { ref -> if (!updated.add(ref)) updated.remove(ref) }
                it.copy(selected = updated)
            }
        }
    }

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    fun refreshSelectionAttachmentInfo() {
        val refs = _uiState.value.selected
        viewModelScope.launch {
            val (count, bytes) = exportImportRepository.partialAttachmentInfo(refs)
            _uiState.update { it.copy(selectionAttachmentCount = count, selectionAttachmentBytes = bytes) }
        }
    }

    fun onIncludeAttachmentsToggle() =
        _uiState.update { it.copy(includeAttachments = !it.includeAttachments) }

    fun exportSelected(
        format:              PartialExportFormat,
        uri:                 Uri,
        context:             Context,
        password:            String  = "",
        showSensitiveValues: Boolean = false,
        conClave:            Boolean = false
    ) {
        val refs = _uiState.value.selected
        if (refs.isEmpty()) return
        val includeAttachments = _uiState.value.includeAttachments && _uiState.value.selectionAttachmentCount > 0
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, event = null) }
            runCatching {
                when (format) {
                    PartialExportFormat.CSV  -> exportImportRepository.exportPartialCsv(refs, uri, context)
                    PartialExportFormat.JSON ->
                        if (conClave) {
                            claveDeRespaldo.paraRespaldo { clave ->
                                requireNotNull(clave) { "backup_key_locked" }
                                exportImportRepository.exportPartialManual(refs, "", uri, includeAttachments, context, clave)
                            }
                        } else {
                            exportImportRepository.exportPartialManual(refs, password, uri, includeAttachments, context)
                        }
                    PartialExportFormat.PDF  -> exportImportRepository.exportPartialPdf(refs, password, showSensitiveValues, uri, context)
                }
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, event = PartialExportEvent.ExportSuccess) }
            }.onFailure { e ->
                _uiState.update { it.copy(
                    isLoading = false,
                    event     = PartialExportEvent.Error(e.message ?: context.getString(com.thormakk.keyarmor.R.string.import_export_error_exporting))
                ) }
            }
        }
    }

    fun onEventConsumed() = _uiState.update { it.copy(event = null) }
}
