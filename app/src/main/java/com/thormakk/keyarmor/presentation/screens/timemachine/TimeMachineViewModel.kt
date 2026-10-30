package com.thormakk.keyarmor.presentation.screens.timemachine

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.drive.DriveSnapshotInfo
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.SnapshotPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.RestoreSummary
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.SnapshotRepository
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.domain.model.VaultSnapshot
import com.thormakk.keyarmor.security.CorruptedDataException
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONException
import javax.crypto.BadPaddingException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TimelineEntry {
    abstract val timestamp: Long
    abstract val trigger:   SnapshotTrigger

    data class Local(val snapshot: VaultSnapshot) : TimelineEntry() {
        override val timestamp: Long          = snapshot.timestamp
        override val trigger:   SnapshotTrigger = snapshot.trigger
    }

    data class DriveOnly(val info: DriveSnapshotInfo) : TimelineEntry() {
        override val timestamp: Long          = info.timestamp
        override val trigger:   SnapshotTrigger = info.trigger
    }
}

@HiltViewModel
class TimeMachineViewModel @Inject constructor(
    private val snapshotRepository: SnapshotRepository,
    private val authRepository: AuthRepository,
    val snapshotPrefs:              SnapshotPrefs,
    planPrefs:          PlanPrefs,
    private val claveDeRespaldo: com.thormakk.keyarmor.data.backup.ClaveDeRespaldo,
    private val listaDePalabras: com.thormakk.keyarmor.data.backup.ListaDePalabras,
    @ApplicationContext private val appContextInyectado: Context
) : ViewModel() {

    private val appContext: Context get() = contextoDelIdioma(appContextInyectado)

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            planPrefs.effectivePlan() == UserPlan.FREE)

    private val localSnapshots: StateFlow<List<VaultSnapshot>> =
        snapshotRepository.observeSnapshots()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _driveSnapshots = MutableStateFlow<List<DriveSnapshotInfo>>(emptyList())

    val timeline: StateFlow<List<TimelineEntry>> =
        combine(localSnapshots, _driveSnapshots) { local, drive ->
            val localIds = local.map { it.id }.toSet()
            buildList {
                addAll(local.map { TimelineEntry.Local(it) })
                drive.filter { it.snapshotId !in localIds }
                     .forEach { add(TimelineEntry.DriveOnly(it)) }
            }.sortedByDescending { it.timestamp }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isCreating     = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _isRestoring    = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _isLoadingDrive   = MutableStateFlow(false)
    val isLoadingDrive: StateFlow<Boolean> = _isLoadingDrive.asStateFlow()

    private val _driveLoadError   = MutableStateFlow<Boolean>(false)
    val driveLoadError: StateFlow<Boolean> = _driveLoadError.asStateFlow()

    private val _mergeResult    = MutableStateFlow<RestoreSummary?>(null)
    val mergeResult: StateFlow<RestoreSummary?> = _mergeResult.asStateFlow()

    private val _error          = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _restoreSuccess = MutableStateFlow(false)
    val restoreSuccess: StateFlow<Boolean> = _restoreSuccess.asStateFlow()

    init { refreshDriveSnapshots() }

    fun createManualSnapshot() {
        if (_isCreating.value || !snapshotRepository.canCreateSnapshots()) return
        viewModelScope.launch {
            _isCreating.value = true
            try {
                snapshotRepository.createSnapshot(SnapshotTrigger.MANUAL)
            } catch (e: Exception) { _error.value = e.message ?: appContext.getString(R.string.error_unknown) }
            finally { _isCreating.value = false }
        }
    }

    fun deleteSnapshot(id: String) {
        viewModelScope.launch {
            try {
                snapshotRepository.deleteSnapshot(id)
                _driveSnapshots.value = _driveSnapshots.value.filter { it.snapshotId != id }
            } catch (e: Exception) { _error.value = e.message ?: appContext.getString(R.string.error_unknown) }
        }
    }

    fun restoreReplace(id: String) {
        if (_isRestoring.value) return
        viewModelScope.launch {
            _isRestoring.value = true
            try { snapshotRepository.restoreReplace(id); _restoreSuccess.value = true }
            catch (e: Exception) { _error.value = e.message ?: appContext.getString(R.string.error_unknown) }
            finally { _isRestoring.value = false }
        }
    }

    fun restoreMerge(id: String) {
        if (_isRestoring.value) return
        viewModelScope.launch {
            _isRestoring.value = true
            try { _mergeResult.value = snapshotRepository.restoreMerge(id) }
            catch (e: Exception) { _error.value = e.message ?: appContext.getString(R.string.error_unknown) }
            finally { _isRestoring.value = false }
        }
    }

    fun restoreAddAll(id: String) {
        if (_isRestoring.value) return
        viewModelScope.launch {
            _isRestoring.value = true
            try { snapshotRepository.restoreAddAll(id); _restoreSuccess.value = true }
            catch (e: Exception) { _error.value = e.message ?: appContext.getString(R.string.error_unknown) }
            finally { _isRestoring.value = false }
        }
    }

    fun refreshDriveSnapshots() {
        viewModelScope.launch {
            _isLoadingDrive.value = true
            _driveLoadError.value = false
            try {
                _driveSnapshots.value = snapshotRepository.fetchDriveSnapshots()
            } catch (_: Exception) {
                if (snapshotRepository.hasDriveAccount()) {
                    _driveLoadError.value = true
                }
            } finally {
                _isLoadingDrive.value = false
            }
        }
    }

    enum class DriveRestoreKind { REPLACE, MERGE, ADD_ALL }

    data class ClavePendiente(val info: DriveSnapshotInfo, val kind: DriveRestoreKind, val error: String? = null)

    private val _clavePendiente = MutableStateFlow<ClavePendiente?>(null)
    val clavePendiente: StateFlow<ClavePendiente?> = _clavePendiente.asStateFlow()

    fun cancelarClavePendiente() { _clavePendiente.value = null }

    fun restoreReplaceFromDrive(info: DriveSnapshotInfo) = restaurarDesdeDrive(info, DriveRestoreKind.REPLACE, null)
    fun restoreMergeFromDrive(info: DriveSnapshotInfo)   = restaurarDesdeDrive(info, DriveRestoreKind.MERGE, null)
    fun restoreAddAllFromDrive(info: DriveSnapshotInfo)  = restaurarDesdeDrive(info, DriveRestoreKind.ADD_ALL, null)

    fun confirmarClavePendiente(texto: String) {
        val p = _clavePendiente.value ?: return
        restaurarDesdeDrive(p.info, p.kind, texto)
    }

    private fun restaurarDesdeDrive(info: DriveSnapshotInfo, kind: DriveRestoreKind, palabras: String?) {
        if (_isRestoring.value) return
        viewModelScope.launch {
            var clave: ByteArray? = null
            if (palabras != null) {
                val lectura = com.thormakk.keyarmor.crypto.PalabrasDeLaClave
                    .desdePalabras(palabras, listaDePalabras.palabras)
                if (lectura is com.thormakk.keyarmor.crypto.LecturaDeLaClave.Clave) {
                    clave = lectura.bytes
                } else {
                    _clavePendiente.value = ClavePendiente(info, kind, mensajeDeLectura(lectura))
                    return@launch
                }
            }
            _isRestoring.value = true
            try {
                when (kind) {
                    DriveRestoreKind.REPLACE -> { snapshotRepository.restoreReplaceFromDrive(info, clave); _restoreSuccess.value = true }
                    DriveRestoreKind.MERGE   -> _mergeResult.value = snapshotRepository.restoreMergeFromDrive(info, clave)
                    DriveRestoreKind.ADD_ALL -> { snapshotRepository.restoreAddAllFromDrive(info, clave); _restoreSuccess.value = true }
                }
                if (clave != null && claveDeRespaldo.adoptar(clave)) claveDeRespaldo.marcarConfirmada()
                _clavePendiente.value = null
            } catch (e: Exception) {
                val m = mapDriveRestoreError(e)
                if (m == NO_BACKUP_PASSWORD_SENTINEL || m == DRIVE_WRONG_PASSWORD_SENTINEL) {
                    _clavePendiente.value = ClavePendiente(
                        info, kind,
                        if (palabras != null) appContext.getString(R.string.key_err_wrong_key) else null
                    )
                } else {
                    _clavePendiente.value = null
                    _error.value = m
                }
            } finally {
                clave?.fill(0)
                _isRestoring.value = false
            }
        }
    }

    private fun mensajeDeLectura(l: com.thormakk.keyarmor.crypto.LecturaDeLaClave): String = when (l) {
        is com.thormakk.keyarmor.crypto.LecturaDeLaClave.PalabraDesconocida ->
            appContext.getString(R.string.key_err_unknown_word, l.posicion, l.palabra)
        is com.thormakk.keyarmor.crypto.LecturaDeLaClave.CantidadEquivocada ->
            appContext.getString(R.string.key_err_wrong_count, l.cuantas)
        com.thormakk.keyarmor.crypto.LecturaDeLaClave.NoCierra ->
            appContext.getString(R.string.key_err_no_close)
        is com.thormakk.keyarmor.crypto.LecturaDeLaClave.Clave -> { l.bytes.fill(0); "" }
    }

    private fun mapDriveRestoreError(e: Exception): String = when {
        e.message == "NO_BACKUP_PASSWORD"                        -> NO_BACKUP_PASSWORD_SENTINEL
        e is BadPaddingException || e is CorruptedDataException -> DRIVE_WRONG_PASSWORD_SENTINEL
        e is JSONException                                      -> CORRUPTED_FILE_SENTINEL
        else -> appContext.getString(R.string.error_unknown)
    }

    companion object {
        const val NO_BACKUP_PASSWORD_SENTINEL = "NO_BACKUP_PASSWORD"
        const val DRIVE_WRONG_PASSWORD_SENTINEL = "DRIVE_WRONG_PASSWORD"
        const val CORRUPTED_FILE_SENTINEL = "CORRUPTED_FILE"
        const val DELETE_DRIVE_FAILED_SENTINEL = "DELETE_DRIVE_FAILED"
        const val DELETE_OFFLINE_SENTINEL = "DELETE_OFFLINE"
    }

    fun deleteFromDrive(info: DriveSnapshotInfo) {
        viewModelScope.launch {
            try {
                snapshotRepository.deleteFromDrive(info)
                _driveSnapshots.value = _driveSnapshots.value.filter { it.driveFileId != info.driveFileId }
            } catch (e: Exception) { _error.value = e.message ?: appContext.getString(R.string.error_unknown) }
        }
    }

    private val _selectedKeys = MutableStateFlow<Set<String>>(emptySet())
    val selectedKeys: StateFlow<Set<String>> = _selectedKeys.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleteDone = MutableStateFlow(false)
    val deleteDone: StateFlow<Boolean> = _deleteDone.asStateFlow()

    private fun TimelineEntry.selectionKey(): String = when (this) {
        is TimelineEntry.Local     -> snapshot.id
        is TimelineEntry.DriveOnly -> "drive_${info.driveFileId}"
    }

    fun deleteTouchesDrive(deleteAll: Boolean = false): Boolean {
        val sel = _selectedKeys.value
        val tocaDrive = deleteAll || timeline.value.any { e ->
            when (e) {
                is TimelineEntry.DriveOnly -> e.selectionKey() in sel
                is TimelineEntry.Local     -> e.snapshot.id in sel && e.snapshot.syncedToDrive
            }
        }
        return tocaDrive && snapshotRepository.hasDriveAccount()
    }

    fun canStartDelete(deleteAll: Boolean = false): Boolean {
        if (deleteTouchesDrive(deleteAll) &&
            !com.thormakk.keyarmor.data.drive.BackupNetworkPolicy.isOnline(appContextInyectado)) {
            _error.value = DELETE_OFFLINE_SENTINEL
            return false
        }
        return true
    }

    fun driveUnreachable(): Boolean =
        _driveLoadError.value ||
            !com.thormakk.keyarmor.data.drive.BackupNetworkPolicy.isOnline(appContextInyectado)

    fun toggleSelection(key: String) {
        _selectedKeys.value = _selectedKeys.value.let { if (key in it) it - key else it + key }
    }

    fun toggleSelectAll() {
        val todos = timeline.value.map { it.selectionKey() }.toSet()
        _selectedKeys.value =
            if (todos.isNotEmpty() && _selectedKeys.value.containsAll(todos)) emptySet() else todos
    }
    fun clearSelection() { _selectedKeys.value = emptySet() }

    fun deleteSelected() = runDelete(deleteAll = false)

    fun deleteAll() = runDelete(deleteAll = true)

    private fun runDelete(deleteAll: Boolean) {
        if (_isDeleting.value) return
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                val sel = _selectedKeys.value
                val entries = timeline.value
                val localIds = entries.filterIsInstance<TimelineEntry.Local>()
                    .map { it.snapshot.id }.filter { deleteAll || it in sel }.toSet()
                val driveFileIds = entries.filterIsInstance<TimelineEntry.DriveOnly>()
                    .map { it.info.driveFileId }.filter { deleteAll || "drive_$it" in sel }.toSet()
                snapshotRepository.deleteSnapshotsBatch(localIds, driveFileIds, deleteAll)
                _selectedKeys.value = emptySet()
                _driveSnapshots.value =
                    if (deleteAll) emptyList()
                    else _driveSnapshots.value.filter { it.driveFileId !in driveFileIds && it.snapshotId !in localIds }
                _deleteDone.value = true
                refreshDriveSnapshots()
            } catch (e: Exception) {
                _error.value = DELETE_DRIVE_FAILED_SENTINEL
            } finally { _isDeleting.value = false }
        }
    }

    fun clearDeleteDone() { _deleteDone.value = false }

    fun clearError()          { _error.value = null }
    fun clearMergeResult()    { _mergeResult.value = null }
    fun clearRestoreSuccess() { _restoreSuccess.value = false }
}
