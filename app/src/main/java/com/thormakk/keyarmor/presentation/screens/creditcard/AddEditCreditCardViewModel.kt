package com.thormakk.keyarmor.presentation.screens.creditcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.CreditCardRepository
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditCreditCardUiState(
    val id:               String        = "",
    val brand:            String        = "",
    val issuer:           String        = "",
    val cardNumber:       String        = "",
    val cardholder:       String        = "",
    val expiryDate:       String        = "",
    val cvv:              String        = "",
    val bankPhonePrefix:  String        = "+",
    val bankPhoneNumber:  String        = "",
    val category:    EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val tags:        List<String>  = emptyList(),
    val isFavorite:   Boolean       = false,
    val isTravelSafe: Boolean       = false,
    val isPinned:     Boolean       = false,
    val isHidden:     Boolean       = false,
    val isProtected:  Boolean       = false,
    val isNew:             Boolean       = true,
    val isSaved:           Boolean       = false,
    val isDeleted:         Boolean       = false,
    val createdAt:         Long          = System.currentTimeMillis(),
    val updatedAt:         Long          = System.currentTimeMillis(),
    val isClone:           Boolean       = false,
    val inheritedFrom:     String?       = null
) {
    val bankPhone: String get() =
        listOf(bankPhonePrefix.trim(), bankPhoneNumber.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")
}

@HiltViewModel
class AddEditCreditCardViewModel @Inject constructor(
    private val repository:      CreditCardRepository,
    private val tagRepository:   TagRepository,
    private val planPrefs: PlanPrefs,
    private val authRepository:  AuthRepository,
    val biometricPrefs:          BiometricPrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val pinnedHubPrefs: PinnedHubPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    savedStateHandle:            SavedStateHandle
) : ViewModel() {

    private val cardId:  String = savedStateHandle["id"] ?: "new"
    private val cloneOf: String = savedStateHandle["cloneOf"] ?: ""

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    private val _uiState = MutableStateFlow(AddEditCreditCardUiState())
    val uiState: StateFlow<AddEditCreditCardUiState> = _uiState

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
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        when {
            cloneOf.isNotEmpty() -> viewModelScope.launch {
                repository.getCard(cloneOf)?.let { src ->
                    _uiState.value = AddEditCreditCardUiState(
                        id           = java.util.UUID.randomUUID().toString(),
                        brand        = src.brand,
                        issuer       = src.issuer,
                        cardNumber   = src.cardNumber,
                        cardholder   = src.cardholder,
                        expiryDate   = src.expiryDate.filter { it.isDigit() },
                        cvv          = src.cvv,
                        bankPhonePrefix = src.bankPhone.substringBefore(" ").takeIf { it.startsWith("+") } ?: "",
                        bankPhoneNumber = if (src.bankPhone.startsWith("+")) src.bankPhone.substringAfter(" ", missingDelimiterValue = "") else src.bankPhone,
                        category     = src.category,
                        tags         = src.tags,
                        isFavorite   = false,
                        isTravelSafe = src.isTravelSafe,
                        isNew        = true,
                        isClone      = true
                    )
                }
            }
            cardId != "new" -> viewModelScope.launch {
                repository.observeCardById(cardId)
                    .collect { card ->
                        val current = _uiState.value
                        if (current.isSaved || current.isDeleted) return@collect
                        if (card != null) {
                            _uiState.value = AddEditCreditCardUiState(
                                id           = card.id,
                                brand        = card.brand,
                                issuer       = card.issuer,
                                cardNumber   = card.cardNumber,
                                cardholder   = card.cardholder,
                                expiryDate   = card.expiryDate.filter { it.isDigit() },
                                cvv          = card.cvv,
                                bankPhonePrefix = card.bankPhone.substringBefore(" ").takeIf { it.startsWith("+") } ?: "",
                                bankPhoneNumber = if (card.bankPhone.startsWith("+")) card.bankPhone.substringAfter(" ", missingDelimiterValue = "") else card.bankPhone,
                                category     = card.category,
                                tags         = card.tags,
                                isFavorite   = card.isFavorite,
                                isTravelSafe = card.isTravelSafe,
                                isPinned      = card.isPinned,
                                isHidden      = card.isHidden,
                                isProtected   = card.isProtected,
                                isNew         = false,
                                createdAt     = card.createdAt,
                                updatedAt     = card.updatedAt,
                                inheritedFrom = card.inheritedFrom
                            )
                        }
                    }
            }
        }
    }

    fun onBrandChange(v: String)      { _uiState.value = _uiState.value.copy(brand = v) }
    fun onIssuerChange(v: String)     { _uiState.value = _uiState.value.copy(issuer = v) }
    fun onCardNumberChange(v: String) { _uiState.value = _uiState.value.copy(cardNumber = v) }
    fun onCardholderChange(v: String) { _uiState.value = _uiState.value.copy(cardholder = v) }
    fun onExpiryDateChange(v: String) { _uiState.value = _uiState.value.copy(expiryDate = v) }
    fun onCvvChange(v: String)        { _uiState.value = _uiState.value.copy(cvv = v) }
    fun onBankPhonePrefixChange(v: String) { _uiState.value = _uiState.value.copy(bankPhonePrefix = v) }
    fun onBankPhoneNumberChange(v: String) { _uiState.value = _uiState.value.copy(bankPhoneNumber = v) }
    fun toggleFavorite()              { _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite) }

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
            .map { refs -> refs.any { it.id == cardId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), pinnedHubPrefs.isPinned(cardId))

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin() {
        if (cardId == "new") return
        if (pinnedHubPrefs.isPinned(cardId)) {
            pinnedHubPrefs.unpin(cardId)
        } else {
            val ok = pinnedHubPrefs.pin(cardId, com.thormakk.keyarmor.domain.model.HubEntryType.CREDIT_CARD, activeVaultPrefs.activeVaultId)
            if (!ok) _hubPinLimitReached.value = true
        }
    }

    fun clearHubPinLimitReached() { _hubPinLimitReached.value = false }


    fun onTravelSafeChange(v: Boolean) { _uiState.value = _uiState.value.copy(isTravelSafe = v) }

    fun saveTravelSafeOnly(v: Boolean) {
        val s = _uiState.value.copy(isTravelSafe = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setTravelSafe(s.id, v) }
    }

    fun saveHiddenOnly(v: Boolean) {
        val s = _uiState.value.copy(isHidden = v)
        _uiState.value = s
        if (!s.isNew) viewModelScope.launch { repository.setHidden(s.id, v) }
    }


    fun tagConflictKey(name: String): String = tagRepository.canonicalTagKey(name)
    fun addTag(name: String) {
        viewModelScope.launch { tagRepository.addTag(name) }
    }

    fun renameTag(id: String, newName: String) {
        viewModelScope.launch {
            val oldName = tagRepository.observeTags().first().find { it.id == id }?.name
            tagRepository.renameTag(id, newName)
            oldName?.let { name ->
                val current = _uiState.value.tags
                if (current.contains(name)) {
                    _uiState.value = _uiState.value.copy(
                        tags = current.map { if (it == name) newName.trim() else it }
                    )
                }
            }
        }
    }

    fun deleteTag(id: String) {
        viewModelScope.launch { tagRepository.deleteTag(id) }
    }

    fun saveMetaOnly(category: EntryCategory, tags: List<String>) {
        _uiState.value = _uiState.value.copy(category = category, tags = tags)
        val s = _uiState.value
        if (!s.isNew) {
            viewModelScope.launch {
                repository.saveCard(buildCard(s).copy(category = category, tags = tags), soloMetadatos = true)
            }
        }
    }

    fun save() {
        if (isReadOnly.value) return
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveCard(buildCard(s))
            _uiState.value = _uiState.value.copy(
                isSaved   = true,
                updatedAt = System.currentTimeMillis()
            )
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

    private fun buildCard(s: AddEditCreditCardUiState): CreditCard = CreditCard(
        id           = if (s.id.isEmpty()) java.util.UUID.randomUUID().toString() else s.id,
        brand        = s.brand,
        issuer       = s.issuer,
        cardNumber   = s.cardNumber,
        cardholder   = s.cardholder,
        expiryDate   = s.expiryDate.let { d -> if (d.length >= 3) d.take(2) + "/" + d.drop(2) else d },
        cvv          = s.cvv,
        bankPhone    = s.bankPhone,
        category     = s.category,
        tags         = s.tags,
        isFavorite   = s.isFavorite,
        isTravelSafe = s.isTravelSafe,
        isPinned     = s.isPinned,
        isHidden     = s.isHidden,
        isProtected  = s.isProtected,
        createdAt    = s.createdAt,
        updatedAt    = System.currentTimeMillis(),
        vaultId      = activeVaultPrefs.activeVaultId,
    )


    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)


    val otherVaults: StateFlow<List<VaultMetadata>> = combine(
        vaultMetadataRepository.observeAll(),
        activeVaultPrefs.activeVaultIdFlow
    ) { all, currentId ->
        all.filter { it.id != currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moveEntryTo(targetVaultId: String) {
        viewModelScope.launch { repository.moveEntriesToVault(setOf(cardId), targetVaultId) }
    }

    fun removeInheritedFrom() {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.removeInheritedFrom(id) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(cardId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
}
