package com.thormakk.keyarmor.presentation.screens.drivesync

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.billing.RevenueCatManager
import com.thormakk.keyarmor.data.drive.BackupNetworkPolicy
import com.thormakk.keyarmor.data.drive.DriveBackupRepository
import com.thormakk.keyarmor.data.drive.GoogleIdentityVerifier
import com.thormakk.keyarmor.data.export.ImportParser
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.ExportImportRepository
import com.thormakk.keyarmor.data.repository.FcmRepository
import com.thormakk.keyarmor.data.repository.RestoreMode
import com.thormakk.keyarmor.presentation.UiText
import com.thormakk.keyarmor.presentation.util.establishFirebaseSession
import com.thormakk.keyarmor.presentation.util.registerFcmTokenFromLogin
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import javax.crypto.BadPaddingException
import javax.inject.Inject

data class DriveSyncUiState(
    val userPlan:             UserPlan = UserPlan.FREE,
    val driveAccount:         String?  = null,
    val hasBackupPassword:    Boolean  = false,
    val lastDriveSync:        Long     = 0L,
    val isWorking:            Boolean  = false,
    val autoBackupRunning:    Boolean  = false,
    val showSetupPassword:    Boolean  = false,
    val showRestoreDialog:    Boolean     = false,
    val restoreMode:          RestoreMode = RestoreMode.MERGE,
    val restoreVaultEmpty:    Boolean = false,
    val restoreNeedsPassword: Boolean     = false,
    val restorePasswordError: Boolean     = false,
    val restoreKeyError:      String?     = null,
    val showRestoreSetupCard: Boolean     = true,
    val showDisconnectDialog:    Boolean  = false,
    val showDeleteBackupDialog:  Boolean  = false,
    val dualLocalBackupEnabled:  Boolean  = false,
    val showRestoreSourceDialog: Boolean  = false,
    val localBackupTimestamp:    Long?    = null,
    val cloudBackupTimestamp:    Long?    = null,
    val showHeavyBackupDialog:   Boolean  = false,
    val heavyBackupMb:           Int      = 0,
    val backupWaitingWifi:       Boolean  = false,
    val driveFull:               Boolean  = false,
)

enum class RestoreSource { LOCAL, CLOUD }

sealed class DriveSyncEvent {
    object BackupSuccess                                          : DriveSyncEvent()
    object DeleteBackupSuccess                                    : DriveSyncEvent()
    data class RestoreSuccess(val count: Int, val skipped: Int = 0, val duplicates: Int = 0, val protectedSkipped: Int = 0) : DriveSyncEvent()
    object PasswordSaved                                          : DriveSyncEvent()
    object ClaveActivada                                          : DriveSyncEvent()
    data class Error(val message: UiText)                         : DriveSyncEvent()
    object NavigateToPlans                                        : DriveSyncEvent()
    data class AuthorizationRequired(val intent: Intent)          : DriveSyncEvent()
    data class LocalBackupSaved(val fileName: String)             : DriveSyncEvent()

    object BackupPasswordGone                                     : DriveSyncEvent()
}

@HiltViewModel
class DriveSyncViewModel @Inject constructor(
    @ApplicationContext private val contextInyectado: Context,
    private val driveRepo:        DriveBackupRepository,
    private val settingsViewPrefs: com.thormakk.keyarmor.data.prefs.SettingsViewPrefs,
    private val drivePrefs:       DrivePrefs,
    private val planPrefs:        PlanPrefs,
    private val authRepository:   AuthRepository,
    private val exportImportRepo: ExportImportRepository,
    private val identityVerifier: GoogleIdentityVerifier,
    private val rcManager:        RevenueCatManager,
    private val fcmRepository:    FcmRepository,
    private val fcmPrefs:         FcmPrefs,
    private val inheritanceDisarmer: com.thormakk.keyarmor.data.repository.InheritanceDisarmer,
    private val inheritancePrefs:    com.thormakk.keyarmor.data.prefs.InheritancePrefs,
    private val autoBackupManager:   com.thormakk.keyarmor.data.drive.DriveAutoBackupManager,
    private val claveDeRespaldo:     com.thormakk.keyarmor.data.backup.ClaveDeRespaldo,
    private val listaDePalabras:     com.thormakk.keyarmor.data.backup.ListaDePalabras
) : ViewModel() {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    private val _uiState = MutableStateFlow(DriveSyncUiState())
    val uiState: StateFlow<DriveSyncUiState> = _uiState

    private val _events = Channel<DriveSyncEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentEmail: String? = drivePrefs.getConnectedEmail()

    private var vinculandoPlan = false

    init {
        reload()
        viewModelScope.launch {
            planPrefs.planFlow.collect { plan ->
                if (_uiState.value.userPlan != plan) {
                    _uiState.value = _uiState.value.copy(userPlan = plan)
                }
            }
        }
        viewModelScope.launch {
            autoBackupManager.isRunning.collect { corriendo ->
                val estabaCorriendo = _uiState.value.autoBackupRunning
                _uiState.value = _uiState.value.copy(autoBackupRunning = corriendo)
                if (estabaCorriendo != corriendo) reload()
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            val plan     = planPrefs.effectivePlan()
            val lastSync = authRepository.getLastDriveSync()
            val email    = drivePrefs.getConnectedEmail()
            currentEmail = email
            val vinc     = vinculandoPlan

            _uiState.value = _uiState.value.copy(
                userPlan              = if (vinc) _uiState.value.userPlan else plan,
                driveAccount          = if (vinc) _uiState.value.driveAccount else email,
                hasBackupPassword     = claveDeRespaldo.confirmada(),
                lastDriveSync         = lastSync,
                backupWaitingWifi     = drivePrefs.isBackupWaitingWifi(),
                driveFull             = drivePrefs.isDriveFull(),
                showSetupPassword     = if (vinc) _uiState.value.showSetupPassword else email != null && !claveDeRespaldo.confirmada(),
                dualLocalBackupEnabled = dualLocalBackupRealmenteActivo()
            )
        }
    }

    private fun dualLocalBackupRealmenteActivo(): Boolean {
        if (!drivePrefs.isDualLocalBackupEnabled()) return false
        if (canWriteLocalBackup()) return true
        drivePrefs.setDualLocalBackupEnabled(false)
        return false
    }

    fun onAccountSelected(email: String, idToken: String) {
        viewModelScope.launch {
            val verified = identityVerifier.verify(idToken)
            if (verified == null) {
                _events.send(DriveSyncEvent.Error(
                    UiText.StringResource(R.string.error_google_verification_failed)
                ))
                return@launch
            }

            val verifiedEmail = verified.email ?: email

            val emailAnterior = drivePrefs.getConnectedEmail()
            if (!emailAnterior.isNullOrBlank() && !emailAnterior.equals(verifiedEmail, ignoreCase = true)
                && (inheritancePrefs.isEnabled() || inheritancePrefs.isPendingDisarm())) {
                inheritanceDisarmer.requestDisarm(
                    com.thormakk.keyarmor.data.repository.DisarmReason.ACCOUNT_CHANGE,
                    ownerEmail = emailAnterior
                )
                _events.send(DriveSyncEvent.Error(
                    UiText.StringResource(R.string.inheritance_disarmed_account_change)
                ))
            }

            drivePrefs.saveConnectedEmail(verifiedEmail)
            drivePrefs.saveOwnerGoogleSub(verified.sub)
            currentEmail = verifiedEmail

            viewModelScope.launch { establishFirebaseSession(idToken) }

            viewModelScope.launch { registerFcmTokenFromLogin(idToken, fcmRepository, fcmPrefs) }

            vinculandoPlan = true
            _uiState.value = _uiState.value.copy(isWorking = true)
            rcManager.linkIdentity(verified.sub) {
                viewModelScope.launch {
                    vinculandoPlan = false
                    _uiState.value = _uiState.value.copy(
                        isWorking         = false,
                        driveAccount      = verifiedEmail,
                        hasBackupPassword = claveDeRespaldo.confirmada(),
                        showSetupPassword = !claveDeRespaldo.confirmada(),
                        userPlan          = planPrefs.effectivePlan()
                    )
                }
            }
        }
    }

    fun palabrasDeLaClave(): List<String>? {
        val clave = claveDeRespaldo.obtenerOCrear() ?: return null
        return try {
            com.thormakk.keyarmor.crypto.PalabrasDeLaClave.aPalabras(clave, listaDePalabras.palabras)
        } finally {
            clave.fill(0)
        }
    }

    fun usarPalabrasPropias(texto: String): String? {
        val lectura = com.thormakk.keyarmor.crypto.PalabrasDeLaClave
            .desdePalabras(texto, listaDePalabras.palabras)
        if (lectura !is com.thormakk.keyarmor.crypto.LecturaDeLaClave.Clave) return mensajeDeLectura(lectura)
        return try {
            if (!claveDeRespaldo.adoptar(lectura.bytes)) {
                context.getString(R.string.import_export_error_corrupted_file)
            } else {
                confirmarClaveGuardada()
                null
            }
        } finally {
            lectura.bytes.fill(0)
        }
    }

    fun confirmarClaveGuardada() {
        claveDeRespaldo.marcarConfirmada()
        _uiState.value = _uiState.value.copy(hasBackupPassword = true, showSetupPassword = false)
        viewModelScope.launch { _events.send(DriveSyncEvent.ClaveActivada) }
    }

    private fun mensajeDeLectura(l: com.thormakk.keyarmor.crypto.LecturaDeLaClave): String = when (l) {
        is com.thormakk.keyarmor.crypto.LecturaDeLaClave.PalabraDesconocida ->
            context.getString(R.string.key_err_unknown_word, l.posicion, l.palabra)
        is com.thormakk.keyarmor.crypto.LecturaDeLaClave.CantidadEquivocada ->
            context.getString(R.string.key_err_wrong_count, l.cuantas)
        com.thormakk.keyarmor.crypto.LecturaDeLaClave.NoCierra ->
            context.getString(R.string.key_err_no_close)
        is com.thormakk.keyarmor.crypto.LecturaDeLaClave.Clave -> { l.bytes.fill(0); "" }
    }

    private fun getAccountForDrive(): android.accounts.Account? {
        val email = currentEmail ?: drivePrefs.getConnectedEmail() ?: return null
        return android.accounts.Account(email, "com.google")
    }

    fun saveBackupPassword(password: String, confirm: String): String? {
        if (password.length < MIN_LENGTH) return "too_short"
        if (password != confirm) return "mismatch"

        drivePrefs.savePassword(password)
        _uiState.value = _uiState.value.copy(
            hasBackupPassword  = true,
            showSetupPassword  = false
        )
        viewModelScope.launch { _events.send(DriveSyncEvent.PasswordSaved) }
        return null
    }

    fun localBackupNeedsPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    fun canWriteLocalBackup(): Boolean =
        !localBackupNeedsPermission() || ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED

    fun setDualLocalBackup(enabled: Boolean, granted: Boolean = true) {
        if (enabled && !granted) {
            viewModelScope.launch {
                _events.send(
                    DriveSyncEvent.Error(UiText.StringResource(R.string.drive_dual_backup_denied)),
                )
            }
            return
        }
        drivePrefs.setDualLocalBackupEnabled(enabled)
        _uiState.value = _uiState.value.copy(dualLocalBackupEnabled = enabled)
    }

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)

    fun confirmHeavyBackup() {
        _uiState.value = _uiState.value.copy(showHeavyBackupDialog = false)
        backup(force = true)
    }

    fun waitForWifiForBackup() {
        val esperaAutomatica = _uiState.value.userPlan != UserPlan.FREE
        if (esperaAutomatica) {
            drivePrefs.setBackupPending(true)
            drivePrefs.setBackupWaitingWifi(true)
        }
        _uiState.value = _uiState.value.copy(
            showHeavyBackupDialog = false,
            backupWaitingWifi     = esperaAutomatica
        )
    }

    fun hideHeavyBackupDialog() {
        _uiState.value = _uiState.value.copy(showHeavyBackupDialog = false)
    }

    fun backup(force: Boolean = false) {
        viewModelScope.launch {
            if (exportImportRepo.countAllEntries() == 0) {
                _events.send(DriveSyncEvent.Error(UiText.StringResource(R.string.error_empty_vault_backup)))
                return@launch
            }
            if (!force) {
                val bytes = exportImportRepo.attachmentsTotalBytes()
                if (BackupNetworkPolicy.shouldWaitForWifi(context, bytes)) {
                    _uiState.value = _uiState.value.copy(
                        showHeavyBackupDialog = true,
                        heavyBackupMb         = ((bytes + 1024L * 1024 - 1) / (1024L * 1024)).toInt()
                    )
                    return@launch
                }
            }
            val account = getAccountForDrive() ?: run {
                _events.send(DriveSyncEvent.Error(UiText.StringResource(R.string.error_no_google_account)))
                return@launch
            }
            val usarClave = claveDeRespaldo.confirmada()
            val password = if (usarClave) "" else when (val r = drivePrefs.loadPassword()) {
                is DrivePrefs.LoadResult.Success        -> r.password
                is DrivePrefs.LoadResult.NotConfigured  -> {
                    _uiState.value = _uiState.value.copy(showSetupPassword = true)
                    return@launch
                }
                is DrivePrefs.LoadResult.KeyInvalidated -> {
                    _uiState.value = _uiState.value.copy(hasBackupPassword = false)
                    _events.send(DriveSyncEvent.Error(
                        UiText.StringResource(R.string.error_backup_key_invalidated)
                    ))
                    return@launch
                }
                DrivePrefs.LoadResult.Locked -> return@launch
            }
            _uiState.value = _uiState.value.copy(isWorking = true)
            try {
                val kab = claveDeRespaldo.paraRespaldo { clave -> exportImportRepo.buildKabBackupFile(password, context, clave) }
                val ts  = try { driveRepo.uploadBackupFile(account, kab) } finally { kab.delete() }
                authRepository.setLastDriveSync(ts)
                if (usarClave) claveDeRespaldo.resolverRespaldoPendiente()
                drivePrefs.markManualBackupCompleted()
                drivePrefs.setBackupWaitingWifi(false)
                drivePrefs.setDriveFull(false)
                _uiState.value = _uiState.value.copy(isWorking = false, lastDriveSync = ts, backupWaitingWifi = false, driveFull = false)
                _events.send(DriveSyncEvent.BackupSuccess)

                if (_uiState.value.dualLocalBackupEnabled) {
                    if (!canWriteLocalBackup()) {
                        drivePrefs.setDualLocalBackupEnabled(false)
                        _uiState.value = _uiState.value.copy(dualLocalBackupEnabled = false)
                        _events.send(DriveSyncEvent.Error(
                            UiText.StringResource(R.string.drive_dual_backup_denied)
                        ))
                    } else {
                        try {
                            val fileName = claveDeRespaldo.paraRespaldo { clave -> exportImportRepo.exportJsonToDownloads(password, context, clave) }
                            _events.send(DriveSyncEvent.LocalBackupSaved(fileName))
                        } catch (e: Exception) {
                            _events.send(DriveSyncEvent.Error(
                                UiText.StringResource(R.string.drive_dual_backup_local_error)
                            ))
                        }
                    }
                }
            } catch (e: com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException) {
                _uiState.value = _uiState.value.copy(isWorking = false)
                _events.send(DriveSyncEvent.AuthorizationRequired(e.intent))
            } catch (e: com.thormakk.keyarmor.data.drive.DriveStorageFullException) {
                drivePrefs.setDriveFull(true)
                _uiState.value = _uiState.value.copy(isWorking = false, driveFull = true)
                _events.send(DriveSyncEvent.Error(UiText.StringResource(R.string.drive_storage_full)))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isWorking = false)
                val cause = e.cause?.let { context.getString(R.string.drive_sync_error_cause_suffix, it::class.simpleName, it.message) } ?: ""
                val msg = e.message?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.drive_sync_error_no_message, e::class.simpleName)
                _events.send(DriveSyncEvent.Error(UiText.DynamicString(context.getString(R.string.drive_sync_error_backup_prefix, "$msg$cause"))))
            }
        }
    }

    fun showRestoreDialog() {
        _uiState.value = _uiState.value.copy(
            showRestoreDialog    = true,
            restoreNeedsPassword = !claveDeRespaldo.confirmada(),
            restorePasswordError = false,
            restoreKeyError      = null
        )
        viewModelScope.launch {
            val vacia = exportImportRepo.countAllEntries() == 0
            _uiState.value = _uiState.value.copy(
                restoreVaultEmpty = vacia,
                restoreMode       = if (vacia) RestoreMode.MERGE else _uiState.value.restoreMode
            )
        }
    }
    fun hideRestoreDialog()  { _uiState.value = _uiState.value.copy(showRestoreDialog = false) }

    fun setRestoreMode(mode: RestoreMode) {
        _uiState.value = _uiState.value.copy(restoreMode = mode)
    }

    private var restorePendiente: Boolean = false
    private var pendingRestoreClave: ByteArray? = null

    fun restore(manualPassword: String? = null) {
        val mode = _uiState.value.restoreMode
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWorking = true)
            val account = getAccountForDrive() ?: run {
                _uiState.value = _uiState.value.copy(isWorking = false)
                _events.send(DriveSyncEvent.Error(UiText.StringResource(R.string.error_no_google_account)))
                return@launch
            }
            var claveManual: ByteArray? = null
            if (manualPassword != null) {
                val lectura = com.thormakk.keyarmor.crypto.PalabrasDeLaClave
                    .desdePalabras(manualPassword, listaDePalabras.palabras)
                if (lectura is com.thormakk.keyarmor.crypto.LecturaDeLaClave.Clave) {
                    claveManual = lectura.bytes
                } else {
                    _uiState.value = _uiState.value.copy(
                        isWorking            = false,
                        restoreNeedsPassword = true,
                        restoreKeyError      = mensajeDeLectura(lectura)
                    )
                    return@launch
                }
            }
            pendingRestoreClave?.fill(0)
            pendingRestoreClave = claveManual
            restorePendiente    = true

            val localInfo = runCatching { exportImportRepo.findLatestDownloadsBackup(context) }.getOrNull()
            val cloudTs   = runCatching { driveRepo.getCloudBackupTimestamp(account) }.getOrNull()

            val source = when {
                localInfo == null -> RestoreSource.CLOUD
                cloudTs   == null -> RestoreSource.LOCAL
                sameMinute(localInfo.timestampMs, cloudTs) -> RestoreSource.LOCAL
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isWorking               = false,
                        showRestoreSourceDialog = true,
                        localBackupTimestamp    = localInfo.timestampMs,
                        cloudBackupTimestamp    = cloudTs
                    )
                    return@launch
                }
            }
            executeRestore(source, mode, account, claveManual, localInfo)
        }
    }

    fun chooseRestoreSource(source: RestoreSource) {
        val mode     = _uiState.value.restoreMode
        if (!restorePendiente) return
        val claveManual = pendingRestoreClave
        viewModelScope.launch {
            val account = getAccountForDrive() ?: return@launch
            _uiState.value = _uiState.value.copy(showRestoreSourceDialog = false)
            val localInfo = if (source == RestoreSource.LOCAL) {
                runCatching { exportImportRepo.findLatestDownloadsBackup(context) }.getOrNull()
            } else null
            executeRestore(source, mode, account, claveManual, localInfo)
        }
    }

    fun hideRestoreSourceDialog() {
        pendingRestoreClave?.fill(0)
        pendingRestoreClave = null
        restorePendiente    = false
        _uiState.value = _uiState.value.copy(showRestoreSourceDialog = false)
    }

    private fun sameMinute(a: Long, b: Long): Boolean = a / 60_000L == b / 60_000L

    private suspend fun executeRestore(
        source:         RestoreSource,
        mode:           RestoreMode,
        account:        android.accounts.Account,
        claveManual:    ByteArray?,
        localInfo:      ExportImportRepository.LocalBackupInfo?
    ) {
        _uiState.value = _uiState.value.copy(isWorking = true)
        try {
            val (result, outcome) = when (source) {
                RestoreSource.LOCAL -> {
                    val info = localInfo ?: exportImportRepo.findLatestDownloadsBackup(context)
                        ?: error(context.getString(R.string.drive_error_backup_not_found))
                    conClave(claveManual) { clave -> exportImportRepo.restoreLocalBackup(info.uri, "", context, mode, clave) }
                }
                RestoreSource.CLOUD -> {
                    val tmp = driveRepo.downloadBackupToFile(account, context.cacheDir)
                    try {
                        conClave(claveManual) { clave -> exportImportRepo.restoreLocalBackup(android.net.Uri.fromFile(tmp), "", context, mode, clave) }
                    } finally {
                        tmp.delete()
                    }
                }
            }

            if (claveManual != null) {
                claveDeRespaldo.adoptar(claveManual)
                claveDeRespaldo.marcarConfirmada()
            }

            val insertedCount = outcome.insertedEntries
            val duplicates = (result.totalCount - insertedCount - result.skippedRows -
                outcome.skippedProtected).coerceAtLeast(0)
            _uiState.value = _uiState.value.copy(
                isWorking             = false,
                showRestoreDialog     = false,
                restoreNeedsPassword  = false,
                restorePasswordError  = false,
                restoreKeyError       = null,
                showSetupPassword     = false,
                hasBackupPassword = claveDeRespaldo.confirmada()
            )
            _events.send(DriveSyncEvent.RestoreSuccess(insertedCount, result.skippedRows, duplicates, outcome.skippedProtected))
        } catch (e: com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException) {
            _uiState.value = _uiState.value.copy(isWorking = false)
            _events.send(DriveSyncEvent.AuthorizationRequired(e.intent))
        } catch (e: com.thormakk.keyarmor.data.backup.KabFormatException) {
            if (e.message == "kab_wrong_key" || e.message == "kab_needs_key") {
                _uiState.value = _uiState.value.copy(
                    isWorking            = false,
                    restoreNeedsPassword = true,
                    restoreKeyError      = if (e.message == "kab_wrong_key")
                        context.getString(R.string.key_err_wrong_key) else null
                )
            } else {
                _uiState.value = _uiState.value.copy(isWorking = false)
                _events.send(DriveSyncEvent.Error(UiText.StringResource(R.string.import_export_error_corrupted_file)))
            }
        } catch (e: BadPaddingException) {
            _uiState.value = _uiState.value.copy(
                isWorking            = false,
                restoreNeedsPassword = true,
                restorePasswordError = true,
                restoreKeyError      = context.getString(R.string.key_err_wrong_key)
            )
        } catch (e: JSONException) {
            _uiState.value = _uiState.value.copy(isWorking = false)
            _events.send(DriveSyncEvent.Error(UiText.StringResource(R.string.import_export_error_corrupted_file)))
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isWorking = false)
            val msg = e.message?.takeIf { it.isNotBlank() }
                ?: e::class.simpleName ?: context.getString(R.string.error_unknown)
            _events.send(DriveSyncEvent.Error(UiText.DynamicString(msg)))
        } finally {
            claveManual?.fill(0)
            pendingRestoreClave = null
            restorePendiente    = false
        }
    }

    private inline fun <T> conClave(manual: ByteArray?, bloque: (ByteArray?) -> T): T =
        if (manual != null) bloque(manual) else claveDeRespaldo.paraRestaurar(bloque)

    fun showDisconnectDialog()    { _uiState.value = _uiState.value.copy(showDisconnectDialog = true)   }
    fun hideDisconnectDialog()    { _uiState.value = _uiState.value.copy(showDisconnectDialog = false)  }

    fun showDeleteBackupDialog()  { _uiState.value = _uiState.value.copy(showDeleteBackupDialog = true)  }
    fun hideDeleteBackupDialog()  { _uiState.value = _uiState.value.copy(showDeleteBackupDialog = false) }

    fun deleteBackup() {
        viewModelScope.launch {
            val account = getAccountForDrive() ?: run {
                _events.send(DriveSyncEvent.Error(UiText.StringResource(R.string.error_no_google_account)))
                return@launch
            }
            _uiState.value = _uiState.value.copy(isWorking = true, showDeleteBackupDialog = false)
            try {
                driveRepo.deleteBackup(account)
                authRepository.setLastDriveSync(0L)
                settingsViewPrefs.setDriveHasBackup(false)
                _uiState.value = _uiState.value.copy(isWorking = false, lastDriveSync = 0L)
                _events.send(DriveSyncEvent.DeleteBackupSuccess)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isWorking = false)
                _events.send(DriveSyncEvent.Error(UiText.DynamicString(e.message ?: context.getString(R.string.inheritance_notif_generic_error))))
            }
        }
    }

    fun disconnect() {
        val teniaHerencia = inheritancePrefs.isEnabled() || inheritancePrefs.isPendingDisarm()
        if (teniaHerencia) {
            inheritanceDisarmer.requestDisarm(
                com.thormakk.keyarmor.data.repository.DisarmReason.DRIVE_DISCONNECT
            )
        }

        currentEmail = null
        drivePrefs.clear()

        if (teniaHerencia) {
            viewModelScope.launch {
                _events.send(DriveSyncEvent.Error(
                    UiText.StringResource(R.string.inheritance_disarmed_drive_disconnect)
                ))
            }
        }

        rcManager.unlinkIdentity { }

        _uiState.value = _uiState.value.copy(
            driveAccount         = null,
            hasBackupPassword    = false,
            lastDriveSync        = 0L,
            showDisconnectDialog = false,
            showSetupPassword    = false,
            userPlan             = planPrefs.effectivePlan()
        )
    }

    fun onUpgradeTapped() {
        viewModelScope.launch { _events.send(DriveSyncEvent.NavigateToPlans) }
    }

    fun setError(msg: String) {
        viewModelScope.launch { _events.send(DriveSyncEvent.Error(UiText.DynamicString(msg))) }
    }

    companion object {
        const val MIN_LENGTH = 8
    }
}
