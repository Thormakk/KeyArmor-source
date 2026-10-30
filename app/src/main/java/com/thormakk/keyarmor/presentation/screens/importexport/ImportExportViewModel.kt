package com.thormakk.keyarmor.presentation.screens.importexport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.crypto.LecturaDeLaClave
import com.thormakk.keyarmor.crypto.PalabrasDeLaClave
import com.thormakk.keyarmor.data.backup.ClaveDeRespaldo
import com.thormakk.keyarmor.data.backup.KabFormatException
import com.thormakk.keyarmor.data.backup.ListaDePalabras
import com.thormakk.keyarmor.data.export.ImportResult
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import com.thormakk.keyarmor.data.repository.ExportImportRepository
import com.thormakk.keyarmor.data.repository.VaultMetadataRepository
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.usecase.GetTotalActiveEntryCountUseCase
import com.thormakk.keyarmor.presentation.screens.vaultmanagement.VaultManagementUiState
import com.thormakk.keyarmor.security.CorruptedDataException
import org.json.JSONException
import java.util.UUID
import javax.crypto.BadPaddingException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExportFormat { JSON, CSV }
enum class ImportSource { KEYARMOR, CSV_GENERIC }

data class ImportExportUiState(
    val exportFormat:              ExportFormat = ExportFormat.JSON,
    val exportPassword:            String       = "",
    val exportConfirmPassword:     String       = "",
    val exportPasswordVisible:     Boolean      = false,
    val exportConfirmPwdVisible:   Boolean      = false,
    val exportConClave:            Boolean      = false,
    val puedeUsarClave:            Boolean      = false,
    val tieneLaClave:              Boolean      = false,
    val importConClave:            Boolean      = false,
    val importPalabras:            String       = "",
    val hasAttachments:            Boolean      = false,
    val attachmentsCount:          Int          = 0,
    val attachmentsBytes:          Long         = 0L,
    val includeAttachmentsInExport: Boolean     = true,
    val showCsvConfirmDialog:      Boolean      = false,
    val csvHasHiddenEntries:       Boolean      = false,
    val showCsvAuthGate:           Boolean      = false,
    val importSource:              ImportSource = ImportSource.KEYARMOR,
    val importPassword:            String       = "",
    val importPasswordVisible:     Boolean      = false,
    val pendingImport:             ImportResult? = null,
    val pendingImportSource:       ImportSource? = null,
    val importUri:                 Uri?         = null,
    val availableVaults:           List<VaultMetadata> = emptyList(),
    val canCreateVault:            Boolean      = false,
    val selectedVaultId:           String       = VaultMetadata.PRIMARY_VAULT_ID,
    val showCreateVaultDialog:     Boolean      = false,
    val isLoading:                 Boolean      = false,
    val event:                     ImportExportEvent? = null
) {
    val exportPasswordMismatch: Boolean
        get() = exportFormat == ExportFormat.JSON &&
                exportConfirmPassword.isNotEmpty() &&
                exportPassword != exportConfirmPassword

    val canExport: Boolean
        get() = when (exportFormat) {
            ExportFormat.JSON -> exportConClave ||
                                 (exportPassword.length >= 8 &&
                                  exportConfirmPassword == exportPassword)
            ExportFormat.CSV  -> true
        }
}

sealed class ImportExportEvent {
    object LaunchJsonSave : ImportExportEvent()
    object LaunchCsvSave  : ImportExportEvent()
    object LaunchFilePick : ImportExportEvent()
    object ExportSuccess : ImportExportEvent()
    data class ImportPreview(val result: ImportResult) : ImportExportEvent()
    data class ImportSuccess(val count: Int, val skipped: Int = 0, val duplicates: Int = 0, val attachmentsRestored: Int = 0, val protectedSkipped: Int = 0) : ImportExportEvent()
    data class Error(val message: String)     : ImportExportEvent()
}

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val repository:            ExportImportRepository,
    private val authRepository:        AuthRepository,
    private val totalEntryCount:       GetTotalActiveEntryCountUseCase,
    private val vaultMetadataRepository: VaultMetadataRepository,
    private val activeVaultPrefs:      ActiveVaultPrefs,
    private val planPrefs:             PlanPrefs,
    private val claveDeRespaldo:       ClaveDeRespaldo,
    private val listaDePalabras:       ListaDePalabras,
    @dagger.hilt.android.qualifiers.ApplicationContext
    private val appContext:      Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportExportUiState())
    val uiState: StateFlow<ImportExportUiState> = _uiState.asStateFlow()

    private var importClave: ByteArray? = null

    init {
        _uiState.update { it.copy(
            puedeUsarClave = claveDeRespaldo.confirmada(),
            tieneLaClave   = claveDeRespaldo.hay()
        ) }
        viewModelScope.launch {
            combine(vaultMetadataRepository.observeAll(), planPrefs.planFlow) { vaults, plan -> vaults to plan }
                .collect { (vaults, plan) ->
                    val isFree = plan == UserPlan.FREE
                    val writable = vaults.filter { vault ->
                        !vault.isInherited &&
                            (!isFree || vault.id == VaultMetadata.PRIMARY_VAULT_ID)
                    }
                    val ownVaultCount = vaults.count { !it.isInherited && !it.isPrivate }
                    val maxOwn = if (isFree) VaultManagementUiState.MAX_OWN_VAULTS_FREE
                                 else VaultManagementUiState.MAX_OWN_VAULTS_PREMIUM
                    _uiState.update { state ->
                        val stillValid = writable.any { it.id == state.selectedVaultId }
                        val fallback = writable.firstOrNull { it.id == activeVaultPrefs.activeVaultId }?.id
                            ?: VaultMetadata.PRIMARY_VAULT_ID
                        state.copy(
                            availableVaults = writable,
                            canCreateVault  = !isFree && ownVaultCount < maxOwn,
                            selectedVaultId = if (stillValid) state.selectedVaultId else fallback
                        )
                    }
                }
        }
        viewModelScope.launch {
            val count = repository.attachmentCount()
            if (count > 0) {
                val bytes = repository.attachmentsTotalBytes()
                _uiState.update { it.copy(hasAttachments = true, attachmentsCount = count, attachmentsBytes = bytes) }
            }
        }
    }

    fun onExportFormatChange(format: ExportFormat) =
        _uiState.update { it.copy(exportFormat = format) }

    fun onExportConClaveChange(value: Boolean) =
        _uiState.update { it.copy(exportConClave = value) }

    fun onImportConClaveChange(value: Boolean) =
        _uiState.update { it.copy(importConClave = value) }

    fun onImportPalabrasChange(value: String) =
        _uiState.update { it.copy(importPalabras = value) }

    fun leerPalabras(texto: String): LecturaDeLaClave =
        PalabrasDeLaClave.desdePalabras(texto, listaDePalabras.palabras).also {
            if (it is LecturaDeLaClave.Clave) it.bytes.fill(0)
        }

    fun onExportPasswordChange(value: String) =
        _uiState.update { it.copy(exportPassword = value) }

    fun onExportConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(exportConfirmPassword = value) }

    fun onExportPasswordVisibilityToggle() =
        _uiState.update { it.copy(exportPasswordVisible = !it.exportPasswordVisible) }

    fun onExportConfirmPwdVisibilityToggle() =
        _uiState.update { it.copy(exportConfirmPwdVisible = !it.exportConfirmPwdVisible) }

    fun onIncludeAttachmentsToggle() =
        _uiState.update { it.copy(includeAttachmentsInExport = !it.includeAttachmentsInExport) }

    fun onExportClick() {
        if (_uiState.value.exportFormat == ExportFormat.CSV) {
            viewModelScope.launch {
                val hasHidden = repository.countHiddenEntries() > 0
                _uiState.update { it.copy(showCsvConfirmDialog = true, csvHasHiddenEntries = hasHidden) }
            }
        } else {
            _uiState.update { it.copy(event = ImportExportEvent.LaunchJsonSave) }
        }
    }

    fun onCsvExportConfirmed() =
        _uiState.update { it.copy(showCsvConfirmDialog = false, showCsvAuthGate = true) }

    fun onCsvExportDismissed() =
        _uiState.update { it.copy(showCsvConfirmDialog = false) }

    fun onCsvExportAuthorized() =
        _uiState.update { it.copy(showCsvAuthGate = false, event = ImportExportEvent.LaunchCsvSave) }

    fun onCsvAuthGateDismissed() =
        _uiState.update { it.copy(showCsvAuthGate = false) }

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    fun onExportUriSelected(uri: Uri, context: Context) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, event = null) }
            runCatching {
                if (state.exportFormat == ExportFormat.JSON) {
                    val conAdjuntos = state.hasAttachments && state.includeAttachmentsInExport
                    if (state.exportConClave) {
                        claveDeRespaldo.paraRespaldo { clave ->
                            requireNotNull(clave) { "backup_key_locked" }
                            repository.exportManual(
                                password = "", uri = uri, includeAttachments = conAdjuntos,
                                context = context, clave = clave
                            )
                        }
                    } else {
                        repository.exportManual(
                            password = state.exportPassword,
                            uri = uri,
                            includeAttachments = conAdjuntos,
                            context = context
                        )
                    }
                    val count = totalEntryCount()
                    _uiState.update { it.copy(
                        isLoading               = false,
                        exportPassword          = "",
                        exportConfirmPassword   = "",
                        exportPasswordVisible   = false,
                        exportConfirmPwdVisible = false,
                        event                   = ImportExportEvent.ExportSuccess
                    ) }
                } else {
                    repository.exportCsv(uri, context)
                    _uiState.update { it.copy(
                        isLoading = false,
                        event     = ImportExportEvent.ExportSuccess
                    ) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(
                    isLoading = false,
                    event     = ImportExportEvent.Error(e.message ?: context.getString(com.thormakk.keyarmor.R.string.import_export_error_exporting))
                ) }
            }
        }
    }

    fun onImportSourceChange(source: ImportSource) =
        _uiState.update { it.copy(importSource = source) }

    fun onImportPasswordChange(value: String) =
        _uiState.update { it.copy(importPassword = value) }

    fun onImportPasswordVisibilityToggle() =
        _uiState.update { it.copy(importPasswordVisible = !it.importPasswordVisible) }

    fun onImportClick() =
        _uiState.update { it.copy(event = ImportExportEvent.LaunchFilePick) }

    fun onImportUriSelected(uri: Uri, context: Context) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, event = null) }
            runCatching {
                importClave?.fill(0); importClave = null
                val claveDelArchivo: ByteArray? =
                    if (state.importSource == ImportSource.KEYARMOR && state.importConClave) {
                        if (state.tieneLaClave) claveDeRespaldo.claveParaRestaurar()
                        else (PalabrasDeLaClave.desdePalabras(state.importPalabras, listaDePalabras.palabras)
                            as? LecturaDeLaClave.Clave)?.bytes
                    } else null
                if (state.importSource == ImportSource.KEYARMOR && state.importConClave && claveDelArchivo == null) {
                    throw KabFormatException("kab_needs_key")
                }
                importClave = claveDelArchivo
                val result = when (state.importSource) {
                    ImportSource.KEYARMOR    -> repository.importKeyArmor(
                        if (state.importConClave) "" else state.importPassword, uri, context, claveDelArchivo)
                    ImportSource.CSV_GENERIC -> repository.importGenericCsv(uri, context)
                }
                _uiState.update { it.copy(
                    isLoading           = false,
                    pendingImport       = result,
                    pendingImportSource = state.importSource,
                    importUri           = uri,
                    event               = ImportExportEvent.ImportPreview(result)
                ) }
            }.onFailure { e ->
                _uiState.update { it.copy(
                    isLoading = false,
                    event     = ImportExportEvent.Error(
                        when (e) {
                            is KabFormatException      -> when (e.message) {
                                "kab_wrong_key" -> context.getString(com.thormakk.keyarmor.R.string.key_err_wrong_key)
                                "kab_needs_key" -> context.getString(com.thormakk.keyarmor.R.string.ie_import_error_needs_key)
                                else            -> context.getString(com.thormakk.keyarmor.R.string.import_export_error_corrupted_file)
                            }
                            is BadPaddingException     -> context.getString(com.thormakk.keyarmor.R.string.import_export_error_wrong_password)
                            is CorruptedDataException  -> context.getString(com.thormakk.keyarmor.R.string.import_export_error_corrupted_file)
                            is JSONException           -> context.getString(com.thormakk.keyarmor.R.string.import_export_error_corrupted_file)
                            else                       -> e.message ?: context.getString(com.thormakk.keyarmor.R.string.import_export_error_reading_file)
                        }
                    )
                ) }
            }
        }
    }

    fun onImportConfirm() {
        val state   = _uiState.value
        val pending = state.pendingImport ?: return
        val localizedContext = contextoDelIdioma(appContext)
        val vaultOverride = if (state.pendingImportSource == ImportSource.CSV_GENERIC)
            state.selectedVaultId else null
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, event = null) }
            runCatching {
                if (pending.isKabContainer) {
                    val uri = state.importUri
                        ?: error(localizedContext.getString(com.thormakk.keyarmor.R.string.import_export_error_reading_file))
                    repository.persistKabImport(
                        uri      = uri,
                        password = if (state.importConClave) "" else state.importPassword,
                        context  = localizedContext,
                        preview  = pending,
                        vaultId  = vaultOverride,
                        clave    = importClave
                    )
                } else {
                    repository.persistImport(pending, vaultId = vaultOverride, esCsv = state.pendingImportSource == ImportSource.CSV_GENERIC)
                }
            }
                .onSuccess { outcome ->
                    importClave?.fill(0); importClave = null
                    val duplicates = (pending.totalCount - outcome.insertedEntries - pending.skippedRows -
                        outcome.skippedProtected).coerceAtLeast(0)
                    _uiState.update { it.copy(
                        isLoading            = false,
                        pendingImport        = null,
                        pendingImportSource  = null,
                        importPassword       = "",
                        importPalabras       = "",
                        event                = ImportExportEvent.ImportSuccess(
                            count               = outcome.insertedEntries,
                            skipped             = pending.skippedRows,
                            duplicates          = duplicates,
                            attachmentsRestored = outcome.restoredAttachments,
                            protectedSkipped    = outcome.skippedProtected,
                        )
                    ) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        event     = ImportExportEvent.Error(e.message ?: localizedContext.getString(com.thormakk.keyarmor.R.string.import_export_error_importing))
                    ) }
                }
        }
    }


    fun onEventConsumed() {
        _uiState.update { it.copy(event = null) }
    }

    override fun onCleared() {
        importClave?.fill(0); importClave = null
    }

    fun onImportDismissPreview() {
        importClave?.fill(0); importClave = null
        _uiState.update { it.copy(pendingImport = null, pendingImportSource = null, event = null) }
    }

    fun onSelectImportVault(vaultId: String) =
        _uiState.update { it.copy(selectedVaultId = vaultId) }

    fun onShowCreateVaultDialog() =
        _uiState.update { it.copy(showCreateVaultDialog = true) }

    fun onDismissCreateVaultDialog() =
        _uiState.update { it.copy(showCreateVaultDialog = false) }

    fun onCreateVaultForImport(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank() || !_uiState.value.canCreateVault) return
        viewModelScope.launch {
            val sortOrder = _uiState.value.availableVaults.count { !it.isInherited && !it.isPrivate }
            val newVault = VaultMetadata(
                id        = UUID.randomUUID().toString(),
                name      = trimmed,
                sortOrder = sortOrder,
            )
            vaultMetadataRepository.save(newVault)
            _uiState.update { it.copy(selectedVaultId = newVault.id, showCreateVaultDialog = false) }
        }
    }
}
