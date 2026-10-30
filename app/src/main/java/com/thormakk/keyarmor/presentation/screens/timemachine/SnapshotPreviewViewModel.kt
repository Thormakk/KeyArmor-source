package com.thormakk.keyarmor.presentation.screens.timemachine

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.export.ImportParser
import com.thormakk.keyarmor.data.repository.RestoreSummary
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.SnapshotRepository
import com.thormakk.keyarmor.domain.model.VaultSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SnapshotSectionType {
    PASSWORD, TWO_FACTOR, PASSKEY,
    CREDIT_CARD, BANK_ACCOUNT, CRYPTO_WALLET,
    SECURE_NOTE, CUSTOM_ENTRY, IDENTITY_DOC, VEHICLE
}

data class SnapshotEntry(
    val name:       String,
    val isDeleted:  Boolean = false,
    val isArchived: Boolean = false
)

data class SnapshotSection(
    val type:    SnapshotSectionType,
    val entries: List<SnapshotEntry>
) {
    val activeCount: Int get() = entries.count { !it.isDeleted && !it.isArchived }
    val totalCount:  Int get() = entries.size
}

data class SnapshotPreviewUiState(
    val isLoading: Boolean              = true,
    val snapshot:  VaultSnapshot?       = null,
    val sections:  List<SnapshotSection> = emptyList(),
    val error:     String?              = null
)

@HiltViewModel
class SnapshotPreviewViewModel @Inject constructor(
    private val snapshotRepository: SnapshotRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContextInyectado: Context
) : ViewModel() {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    private val snapshotId: String = savedStateHandle["id"] ?: ""

    val initialQuery: String = savedStateHandle["sq"] ?: ""

    private val _uiState = MutableStateFlow(SnapshotPreviewUiState())
    val uiState: StateFlow<SnapshotPreviewUiState> = _uiState.asStateFlow()

    private val _restoreSuccess = MutableStateFlow(false)
    val restoreSuccess: StateFlow<Boolean> = _restoreSuccess.asStateFlow()

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

    private val _workingIsRestore = MutableStateFlow(true)
    val workingIsRestore: StateFlow<Boolean> = _workingIsRestore.asStateFlow()

    private val _mergeResult = MutableStateFlow<RestoreSummary?>(null)
    val mergeResult: StateFlow<RestoreSummary?> = _mergeResult.asStateFlow()

    fun restoreReplace() {
        viewModelScope.launch {
            _workingIsRestore.value = true; _isWorking.value = true
            try {
                snapshotRepository.restoreReplace(snapshotId)
                _restoreSuccess.value = true
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: appContext.getString(R.string.error_unknown))
            } finally { _isWorking.value = false }
        }
    }

    fun restoreMerge() {
        viewModelScope.launch {
            _workingIsRestore.value = true; _isWorking.value = true
            try {
                _mergeResult.value = snapshotRepository.restoreMerge(snapshotId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: appContext.getString(R.string.error_unknown))
            } finally { _isWorking.value = false }
        }
    }

    fun restoreAddAll() {
        viewModelScope.launch {
            _workingIsRestore.value = true; _isWorking.value = true
            try {
                snapshotRepository.restoreAddAll(snapshotId)
                _restoreSuccess.value = true
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: appContext.getString(R.string.error_unknown))
            } finally { _isWorking.value = false }
        }
    }

    fun clearRestoreSuccess() { _restoreSuccess.value = false }
    fun clearMergeResult()    { _mergeResult.value = null }

    fun deleteSnapshot(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _workingIsRestore.value = false; _isWorking.value = true
            try {
                snapshotRepository.deleteSnapshot(snapshotId)
                onDeleted()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: appContext.getString(R.string.error_unknown))
            } finally { _isWorking.value = false }
        }
    }

    init { loadPreview() }

    private fun loadPreview() {
        viewModelScope.launch {
            try {
                val snapshot = snapshotRepository.getSnapshotById(snapshotId)
                val json     = snapshotRepository.loadSnapshotData(snapshotId)
                val r        = ImportParser.parseKeyArmorJson(json)

                fun <T> toEntries(
                    list:       List<T>,
                    name:       (T) -> String,
                    isDeleted:  (T) -> Boolean,
                    isArchived: (T) -> Boolean = { false }
                ) = list.map { SnapshotEntry(name(it).trim(), isDeleted(it), isArchived(it)) }

                fun parts(vararg values: String, sep: String = " ") =
                    values.filter { it.isNotBlank() }.joinToString(sep)

                val sections = buildList {
                    if (r.passwords.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.PASSWORD,
                            toEntries(r.passwords, { it.title }, { it.isDeleted })))

                    if (r.twoFactor.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.TWO_FACTOR,
                            toEntries(r.twoFactor,
                                { parts(it.name, it.issuer, sep = " · ").ifBlank { it.accountLabel } },
                                { it.isDeleted })))

                    if (r.passkeys.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.PASSKEY,
                            toEntries(r.passkeys,
                                { parts(it.rpName.ifBlank { it.rpId }, it.userName, sep = " · ") },
                                { it.isDeleted },
                                { it.isArchived })))

                    if (r.creditCards.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.CREDIT_CARD,
                            toEntries(r.creditCards,
                                { parts(it.brand, it.issuer) },
                                { it.isDeleted })))

                    if (r.bankAccounts.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.BANK_ACCOUNT,
                            toEntries(r.bankAccounts, { it.bankName }, { it.isDeleted })))

                    if (r.cryptoWallets.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.CRYPTO_WALLET,
                            toEntries(r.cryptoWallets,
                                { parts(it.asset, it.network, sep = " · ") },
                                { it.isDeleted })))

                    if (r.secureNotes.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.SECURE_NOTE,
                            toEntries(r.secureNotes, { it.title }, { it.isDeleted })))

                    if (r.customEntries.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.CUSTOM_ENTRY,
                            toEntries(r.customEntries, { it.title }, { it.isDeleted })))

                    if (r.identityDocuments.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.IDENTITY_DOC,
                            toEntries(r.identityDocuments,
                                { parts(it.docType, it.holderName, sep = " · ") },
                                { it.isDeleted })))

                    if (r.vehicles.isNotEmpty())
                        add(SnapshotSection(SnapshotSectionType.VEHICLE,
                            toEntries(r.vehicles,
                                { parts(it.make, it.model, it.year) },
                                { it.isDeleted })))
                }

                _uiState.value = SnapshotPreviewUiState(
                    isLoading = false,
                    snapshot  = snapshot,
                    sections  = sections
                )
            } catch (e: Exception) {
                _uiState.value = SnapshotPreviewUiState(
                    isLoading = false,
                    error     = e.message ?: appContext.getString(R.string.time_machine_error_loading_restore_point)
                )
            }
        }
    }
}
