package com.thormakk.keyarmor.presentation.screens.vehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.ExpiryNotificationPrefs
import com.thormakk.keyarmor.data.prefs.HiddenEntriesPrefs
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.Tag
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.model.Vehicle
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

data class AddEditVehicleUiState(
    val id:                       String        = "",
    val make:                     String        = "",
    val model:                    String        = "",
    val year:                     String        = "",
    val licensePlate:             String        = "",
    val vin:                      String        = "",
    val inspectionExpiryDate:     String        = "",
    val insuranceExpiryDate:      String        = "",
    val insuranceCompany:         String        = "",
    val policyNumber:             String        = "",
    val driverLicenseExpiryDate:  String        = "",
    val notes:                    String        = "",
    val category:                 EntryCategory = EntryCategory.SIN_CATEGORIZAR,
    val tags:                     List<String>  = emptyList(),
    val isFavorite:               Boolean       = false,
    val isTravelSafe:             Boolean       = false,
    val isPinned:                 Boolean       = false,
    val isHidden:                 Boolean       = false,
    val isProtected:              Boolean       = false,
    val isNew:                    Boolean       = true,
    val isSaved:                  Boolean       = false,
    val isDeleted:                Boolean       = false,
    val createdAt:                Long          = System.currentTimeMillis(),
    val updatedAt:                Long          = System.currentTimeMillis(),
    val inheritedFrom:            String?       = null,
)

@HiltViewModel
class AddEditVehicleViewModel @Inject constructor(
    private val repository:       VehicleRepository,
    private val tagRepository:    TagRepository,
    private val planPrefs: PlanPrefs,
    private val activeVaultPrefs: ActiveVaultPrefs,
    vaultMetadataRepository: VaultMetadataRepository,
    private val expiryPrefs:      ExpiryNotificationPrefs,
    private val pinnedHubPrefs: PinnedHubPrefs,
    private val hiddenEntriesPrefs: HiddenEntriesPrefs,
    savedStateHandle:             SavedStateHandle
) : ViewModel() {

    val expiryWarningDays: Set<Int> get() = expiryPrefs.getWarningDaySet()

    private val vehicleId: String = savedStateHandle["id"] ?: "new"
    private val cloneOf:   String = savedStateHandle["cloneOf"] ?: ""

    val isHiddenFeatureEnabled: Boolean = hiddenEntriesPrefs.isEnabled()
    val canHideNewEntries: Boolean = hiddenEntriesPrefs.canHideNewEntries()

    private val _uiState = MutableStateFlow(AddEditVehicleUiState())
    val uiState: StateFlow<AddEditVehicleUiState> = _uiState

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
                repository.getVehicle(cloneOf)?.let { src ->
                    _uiState.value = AddEditVehicleUiState(
                        id                      = java.util.UUID.randomUUID().toString(),
                        make                    = src.make,
                        model                   = src.model,
                        year                    = src.year,
                        licensePlate            = src.licensePlate,
                        vin                     = src.vin,
                        inspectionExpiryDate    = src.inspectionExpiryDate,
                        insuranceExpiryDate     = src.insuranceExpiryDate,
                        insuranceCompany        = src.insuranceCompany,
                        policyNumber            = src.policyNumber,
                        driverLicenseExpiryDate = src.driverLicenseExpiryDate,
                        notes                   = src.notes,
                        category                = src.category,
                        tags                    = src.tags,
                        isFavorite              = false,
                        isTravelSafe            = src.isTravelSafe,
                        isNew                   = true
                    )
                }
            }
            vehicleId != "new" -> viewModelScope.launch {
                repository.observeVehicleById(vehicleId)
                    .collect { v ->
                        val current = _uiState.value
                        if (current.isSaved || current.isDeleted) return@collect
                        if (v != null) {
                            _uiState.value = AddEditVehicleUiState(
                                id                      = v.id,
                                make                    = v.make,
                                model                   = v.model,
                                year                    = v.year,
                                licensePlate            = v.licensePlate,
                                vin                     = v.vin,
                                inspectionExpiryDate    = v.inspectionExpiryDate,
                                insuranceExpiryDate     = v.insuranceExpiryDate,
                                insuranceCompany        = v.insuranceCompany,
                                policyNumber            = v.policyNumber,
                                driverLicenseExpiryDate = v.driverLicenseExpiryDate,
                                notes                   = v.notes,
                                category                = v.category,
                                tags                    = v.tags,
                                isFavorite              = v.isFavorite,
                                isTravelSafe            = v.isTravelSafe,
                                isPinned                = v.isPinned,
                                isHidden                = v.isHidden,
                                isProtected             = v.isProtected,
                                isNew                   = false,
                                createdAt               = v.createdAt,
                                updatedAt               = v.updatedAt,
                                inheritedFrom           = v.inheritedFrom
                            )
                        }
                    }
            }
        }
    }

    fun onMakeChange(v: String)                    { _uiState.value = _uiState.value.copy(make = v) }
    fun onModelChange(v: String)                   { _uiState.value = _uiState.value.copy(model = v) }
    fun onYearChange(v: String)                    { _uiState.value = _uiState.value.copy(year = v) }
    fun onLicensePlateChange(v: String)            { _uiState.value = _uiState.value.copy(licensePlate = v) }
    fun onVinChange(v: String)                     { _uiState.value = _uiState.value.copy(vin = v) }
    fun onInspectionExpiryDateChange(v: String)    { _uiState.value = _uiState.value.copy(inspectionExpiryDate = v) }
    fun onInsuranceExpiryDateChange(v: String)     { _uiState.value = _uiState.value.copy(insuranceExpiryDate = v) }
    fun onInsuranceCompanyChange(v: String)        { _uiState.value = _uiState.value.copy(insuranceCompany = v) }
    fun onPolicyNumberChange(v: String)            { _uiState.value = _uiState.value.copy(policyNumber = v) }
    fun onDriverLicenseExpiryDateChange(v: String) { _uiState.value = _uiState.value.copy(driverLicenseExpiryDate = v) }
    fun onNotesChange(v: String)                   { if (v.length <= 300) _uiState.value = _uiState.value.copy(notes = v) }
    fun toggleFavorite()                           { _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite) }
    fun onTravelSafeChange(v: Boolean)             { _uiState.value = _uiState.value.copy(isTravelSafe = v) }
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
            .map { refs -> refs.any { it.id == vehicleId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), pinnedHubPrefs.isPinned(vehicleId))

    private val _hubPinLimitReached = MutableStateFlow(false)
    val hubPinLimitReached: StateFlow<Boolean> = _hubPinLimitReached

    fun toggleHubPin() {
        if (vehicleId == "new") return
        if (pinnedHubPrefs.isPinned(vehicleId)) {
            pinnedHubPrefs.unpin(vehicleId)
        } else {
            val ok = pinnedHubPrefs.pin(vehicleId, com.thormakk.keyarmor.domain.model.HubEntryType.VEHICLE, activeVaultPrefs.activeVaultId)
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
            viewModelScope.launch { repository.saveVehicle(buildVehicle(s).copy(category = category, tags = tags), soloMetadatos = true) }
        }
    }

    fun save() {
        if (isReadOnly.value) return
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveVehicle(buildVehicle(s))
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

    private fun buildVehicle(s: AddEditVehicleUiState): Vehicle {
        return Vehicle(
            id                           = s.id.ifEmpty { java.util.UUID.randomUUID().toString() },
            make                         = s.make,
            model                        = s.model,
            year                         = s.year,
            licensePlate                 = s.licensePlate,
            vin                          = s.vin,
            inspectionExpiryDate         = s.inspectionExpiryDate,
            insuranceExpiryDate          = s.insuranceExpiryDate,
            insuranceCompany             = s.insuranceCompany,
            policyNumber                 = s.policyNumber,
            driverLicenseExpiryDate      = s.driverLicenseExpiryDate,
            notes                        = s.notes,
            inspectionExpiryTimestamp    = parseDate(s.inspectionExpiryDate),
            insuranceExpiryTimestamp     = parseDate(s.insuranceExpiryDate),
            driverLicenseExpiryTimestamp = parseDate(s.driverLicenseExpiryDate),
            category                     = s.category,
            tags                         = s.tags,
            isFavorite                   = s.isFavorite,
            isTravelSafe                 = s.isTravelSafe,
            isPinned                     = s.isPinned,
            isHidden                     = s.isHidden,
            isProtected                  = s.isProtected,
            createdAt                    = s.createdAt,
            updatedAt                    = System.currentTimeMillis(),
            vaultId                      = activeVaultPrefs.activeVaultId,
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
        viewModelScope.launch { repository.moveEntriesToVault(setOf(vehicleId), targetVaultId) }
    }

    fun removeInheritedFrom() {
        val id = _uiState.value.id.takeIf { it.isNotBlank() && it != "new" } ?: return
        viewModelScope.launch { repository.removeInheritedFrom(id) }
    }

    fun copyEntryTo(targetVaultId: String) {
        val inheritedFrom = if (activeVaultPrefs.activeVaultIsInherited)
            activeVaultPrefs.activeVaultId.removePrefix(VaultMetadata.INHERITED_PREFIX)
        else null
        viewModelScope.launch { repository.copyEntriesToVault(setOf(vehicleId), targetVaultId, inheritedFrom) }
    }

    companion object { private const val MAX_PINNED = 3 }
}
