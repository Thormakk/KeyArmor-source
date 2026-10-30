package com.thormakk.keyarmor.presentation.screens.identity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.ExpiryNotificationPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.IdentityDocument
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class AddEditIdentityDocumentUiState(
    val id:               String        = "",
    val docType:          String        = "",
    val number:           String        = "",
    val issuingCountry:   String        = "",
    val issueDate:        String        = "",
    val expiryDate:       String        = "",
    val birthDate:        String        = "",
    val holderName:       String        = "",
    val notes:            String        = "",
    val expiryAlertDays:  Int           = 90,
    val category:         EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val tags:             List<String>  = emptyList(),
    val isFavorite:       Boolean       = false,
    val isTravelSafe:     Boolean       = false,
    val isPinned:         Boolean       = false,
    val isHidden:         Boolean       = false,
    val isProtected:      Boolean       = false,
    val isNew:            Boolean       = true,
    val isSaved:          Boolean       = false,
    val isDeleted:        Boolean       = false,
    val createdAt:        Long          = System.currentTimeMillis(),
    val updatedAt:        Long          = System.currentTimeMillis(),
    val frontPhotoPath:   String?       = null,
    val backPhotoPath:    String?       = null,
    val inheritedFrom:    String?       = null,
)

@HiltViewModel
class AddEditIdentityDocumentViewModel @Inject constructor(
    private val repository:        IdentityDocumentRepository,
    private val tagRepository:     TagRepository,
    private val planPrefs: PlanPrefs,
    private val activeVaultPrefs:  ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    expiryNotifPrefs:  ExpiryNotificationPrefs,
    private val pinnedHubPrefs: PinnedHubPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    savedStateHandle:              SavedStateHandle
) : ViewModel() {

    val settingsExpiryAlertDaySet: Set<Int> = expiryNotifPrefs.getWarningDaySet()

    private val docId:   String = savedStateHandle["id"] ?: "new"
    private val cloneOf: String = savedStateHandle["cloneOf"] ?: ""

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    private val _uiState = MutableStateFlow(AddEditIdentityDocumentUiState())
    val uiState: StateFlow<AddEditIdentityDocumentUiState> = _uiState

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)
    val isReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, _, isInherited ->
        isInherited || plan == UserPlan.FREE
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited || planPrefs.effectivePlan() == UserPlan.FREE
    )

    val availableTags: StateFlow<List<Tag>> = tagRepository.observeTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        when {
            cloneOf.isNotEmpty() -> viewModelScope.launch {
                repository.getDocument(cloneOf)?.let { src ->
                    _uiState.value = AddEditIdentityDocumentUiState(
                        id             = java.util.UUID.randomUUID().toString(),
                        docType        = src.docType,
                        number         = src.number,
                        issuingCountry = src.issuingCountry,
                        issueDate      = src.issueDate,
                        expiryDate     = src.expiryDate,
                        birthDate      = src.birthDate,
                        holderName     = src.holderName,
                        notes          = src.notes,
                        expiryAlertDays = src.expiryAlertDays,
                        category       = src.category,
                        tags           = src.tags,
                        isFavorite     = false,
                        isTravelSafe   = src.isTravelSafe,
                        isNew          = true
                    )
                }
            }
            docId != "new" -> viewModelScope.launch {
                repository.observeDocumentById(docId)
                    .collect { doc ->
                        val current = _uiState.value
                        if (current.isSaved || current.isDeleted) return@collect
                        if (doc != null) {
                            _uiState.value = AddEditIdentityDocumentUiState(
                                id              = doc.id,
                                docType         = doc.docType,
                                number          = doc.number,
                                issuingCountry  = doc.issuingCountry,
                                issueDate       = doc.issueDate,
                                expiryDate      = doc.expiryDate,
                                birthDate       = doc.birthDate,
                                holderName      = doc.holderName,
                                notes           = doc.notes,
                                expiryAlertDays = doc.expiryAlertDays,
                                category        = doc.category,
                                tags            = doc.tags,
                                isFavorite      = doc.isFavorite,
                                isTravelSafe    = doc.isTravelSafe,
                                isPinned        = doc.isPinned,
                                isHidden        = doc.isHidden,
                                isProtected     = doc.isProtected,
                                isNew           = false,
                                createdAt       = doc.createdAt,
                                updatedAt       = doc.updatedAt,
                                frontPhotoPath  = doc.frontPhotoPath,
                                backPhotoPath   = doc.backPhotoPath,
                                inheritedFrom   = doc.inheritedFrom,
                            )
                        }
                    }
            }
        }
    }

    fun onDocTypeChange(v: String)        { _uiState.value = _uiState.value.copy(docType = v) }
    fun onNumberChange(v: String)         { _uiState.value = _uiState.value.copy(number = v) }
    fun onIssuingCountryChange(v: String) { _uiState.value = _uiState.value.copy(issuingCountry = v) }
    fun onIssueDateChange(v: String)      { _uiState.value = _uiState.value.copy(issueDate = v) }
    fun onExpiryDateChange(v: String)     { _uiState.value = _uiState.value.copy(expiryDate = v) }
    fun onBirthDateChange(v: String)      { _uiState.value = _uiState.value.copy(birthDate = v) }
    fun onHolderNameChange(v: String)     { _uiState.value = _uiState.value.copy(holderName = v) }
    fun onNotesChange(v: String)          { _uiState.value = _uiState.value.copy(notes = v) }
    fun toggleFavorite()                  { _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite) }
    fun onTravelSafeChange(v: Boolean)    { _uiState.value = _uiState.value.copy(isTravelSafe = v) }

    fun togglePin() {
        val s = _uiState.value
        if (s.isNew) return
        viewModelScope.launch {
            if (!s.isPinned && repository.countPinned() >= MAX_PINNED) {
                _pinLimitReached.value = true
                return@launch
            }
            repository.setPin(s.id, !s.isPinned)
            _uiState.value = _uiState.value.copy(isPinned = !s.isPinned)
        }
    }

    private val _pinLimitReached = MutableStateFlow(false)
    val pinLimitReached: StateFlow<Boolean> = _pinLimitReached
    fun onPinLimitDismissed() { _pinLimitReached.value = false }

    val isHubPinned: StateFlow<Boolean> =
        pinnedHubPrefs.pinnedRefsFlow
            .map { refs -> refs.any { it.id == docId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), pinnedHubPrefs.isPinned(docId))

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin() {
        if (docId == "new") return
        if (pinnedHubPrefs.isPinned(docId)) {
            pinnedHubPrefs.unpin(docId)
        } else {
            val ok = pinnedHubPrefs.pin(docId, com.thormakk.keyarmor.domain.model.HubEntryType.IDENTITY_DOCUMENT, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }



    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String)                   { viewModelScope.launch { tagRepository.addTag(name) } }
    fun renameTag(id: String, newName: String) { viewModelScope.launch { tagRepository.renameTag(id, newName) } }
    fun deleteTag(id: String)                  { viewModelScope.launch { tagRepository.deleteTag(id) } }

    fun saveMetaOnly(category: EntryCategory, tags: List<String>) {
        _uiState.value = _uiState.value.copy(category = category, tags = tags)
        val s = _uiState.value
        if (!s.isNew) {
            viewModelScope.launch { repository.saveDocument(buildDocument(s).copy(category = category, tags = tags), soloMetadatos = true) }
        }
    }

    fun save() {
        if (isReadOnly.value) return
        viewModelScope.launch {
            val s  = _uiState.value
            val doc = buildDocument(s)
            repository.saveDocument(doc)
            pendingFrontPhotoBytes?.let { repository.savePhoto(doc.id, it, isFront = true)  }
            pendingBackPhotoBytes?.let  { repository.savePhoto(doc.id, it, isFront = false) }
            pendingFrontPhotoBytes = null
            pendingBackPhotoBytes  = null
            _uiState.value = _uiState.value.copy(isSaved = true, updatedAt = System.currentTimeMillis())
        }
    }


    fun delete() {
        viewModelScope.launch {
            val id = _uiState.value.id
            if (id.isNotEmpty()) {
                if (!repository.softDelete(id)) return@launch
                pinnedHubPrefs.unpin(id)
            }
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    fun archive() {
        viewModelScope.launch {
            val id = _uiState.value.id
            if (!_uiState.value.isNew) { repository.archive(id); pinnedHubPrefs.unpin(id) }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    val canProtectNew: Boolean get() = planPrefs.effectivePlan() != UserPlan.FREE

    fun setProtected(value: Boolean) {
        val s = _uiState.value
        if (s.isNew) return
        if (value && !canProtectNew) return
        viewModelScope.launch {
            repository.setProtected(s.id, value)
            _uiState.value = _uiState.value.copy(isProtected = value)
        }
    }

    fun setFavorite(isFavorite: Boolean) {
        val s = _uiState.value.copy(isFavorite = isFavorite)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setFavorite(s.id, isFavorite) }
    }

    fun setTravelSafe(isTravelSafe: Boolean) {
        val s = _uiState.value.copy(isTravelSafe = isTravelSafe)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setTravelSafe(s.id, isTravelSafe) }
    }

    fun setHidden(isHidden: Boolean) {
        val s = _uiState.value.copy(isHidden = isHidden)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setHidden(s.id, isHidden) }
    }

    private var pendingFrontPhotoBytes: ByteArray? = null
    private var pendingBackPhotoBytes:  ByteArray? = null

    fun setPendingPhotos(front: ByteArray?, back: ByteArray?) {
        val s = _uiState.value
        if (s.isNew) {
            pendingFrontPhotoBytes = front
            pendingBackPhotoBytes  = back
        } else {
            viewModelScope.launch {
                front?.let { repository.savePhoto(s.id, it, isFront = true) }
                back?.let  { repository.savePhoto(s.id, it, isFront = false) }
            }
        }
    }

    fun savePhoto(bytes: ByteArray, isFront: Boolean) {
        val s = _uiState.value
        if (s.isNew) return
        viewModelScope.launch { repository.savePhoto(s.id, bytes, isFront) }
    }

    fun setPendingPhoto(bytes: ByteArray, isFront: Boolean) {
        val s = _uiState.value
        if (s.isNew) {
            if (isFront) pendingFrontPhotoBytes = bytes else pendingBackPhotoBytes = bytes
        } else {
            savePhoto(bytes, isFront)
        }
    }

    fun deletePhoto(isFront: Boolean) {
        val s    = _uiState.value
        val path = if (isFront) s.frontPhotoPath else s.backPhotoPath
        path ?: return
        _uiState.value = if (isFront) s.copy(frontPhotoPath = null) else s.copy(backPhotoPath = null)
        viewModelScope.launch { repository.deletePhoto(s.id, path, isFront) }
    }

    suspend fun decryptPhoto(path: String): ByteArray? = repository.decryptPhoto(path)

    fun applyOcrResult(result: com.thormakk.keyarmor.ocr.OcrResult) {
        _uiState.value = _uiState.value.let { s ->
            s.copy(
                docType        = result.docType?.takeIf        { it.isNotBlank() } ?: s.docType,
                number         = result.number?.takeIf         { it.isNotBlank() } ?: s.number,
                holderName     = result.holderName?.takeIf     { it.isNotBlank() } ?: s.holderName,
                issuingCountry = result.issuingCountry?.takeIf { it.isNotBlank() } ?: s.issuingCountry,
                birthDate      = result.birthDate?.takeIf      { it.isNotBlank() } ?: s.birthDate,
                issueDate      = result.issueDate?.takeIf      { it.isNotBlank() } ?: s.issueDate,
                expiryDate     = result.expiryDate?.takeIf     { it.isNotBlank() } ?: s.expiryDate,
            )
        }
    }

    private fun buildDocument(s: AddEditIdentityDocumentUiState): IdentityDocument {
        val expiry = parseDate(s.expiryDate)
        return IdentityDocument(
            id             = s.id.ifEmpty { java.util.UUID.randomUUID().toString() },
            docType        = s.docType,
            number         = s.number,
            issuingCountry = s.issuingCountry,
            issueDate      = s.issueDate,
            expiryDate     = s.expiryDate,
            birthDate      = s.birthDate,
            holderName     = s.holderName,
            notes          = s.notes,
            expiryTimestamp  = expiry,
            expiryAlertDays  = s.expiryAlertDays,
            category       = s.category,
            tags           = s.tags,
            isFavorite     = s.isFavorite,
            isTravelSafe   = s.isTravelSafe,
            isPinned       = s.isPinned,
            isHidden       = s.isHidden,
            isProtected    = s.isProtected,
            createdAt      = s.createdAt,
            updatedAt      = System.currentTimeMillis(),
            vaultId        = activeVaultPrefs.activeVaultId,
            frontPhotoPath = s.frontPhotoPath,
            backPhotoPath  = s.backPhotoPath,
        )
    }

    private fun parseDate(text: String): Long? {
        if (text.isBlank()) return null
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(text)?.time
        } catch (e: Exception) { null }
    }


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveEntryTo(targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(docId), targetVaultId) }
    }

    fun removeInheritedFrom() {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.removeInheritedFrom(id) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(docId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
}
