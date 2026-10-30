package com.thormakk.keyarmor.data.drive

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.ExportImportRepository
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveAutoBackupManager @Inject constructor(
    private val driveRepo:        DriveBackupRepository,
    private val drivePrefs:       DrivePrefs,
    private val planPrefs:        PlanPrefs,
    private val authRepository:   AuthRepository,
    private val exportImportRepo: ExportImportRepository,
    private val sessionManager:   SessionManager,
    private val claveDeRespaldo:  com.thormakk.keyarmor.data.backup.ClaveDeRespaldo,
    @ApplicationContext private val contextInyectado: Context
) {
    private val backupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val corriendo = AtomicInteger(0)
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    init {
        backupScope.launch {
            sessionManager.isUnlockedFlow.collect { unlocked ->
                if (unlocked &&
                    planPrefs.effectivePlan() != UserPlan.FREE &&
                    drivePrefs.isBackupPending()
                ) {
                    Log.d(TAG, "Pending backup detected on unlock — running now")
                    if (runBackup()) drivePrefs.setBackupPending(false)
                }
            }
        }
    }

    fun triggerOnAppBackground() {
        if (planPrefs.effectivePlan() == UserPlan.FREE) return
        drivePrefs.setBackupPending(true)
        backupScope.launch {
            if (runBackup()) drivePrefs.setBackupPending(false)
        }
    }

    suspend fun subirAhora(): Boolean {
        drivePrefs.setBackupPending(true)
        val ok = runBackup(manual = true)
        if (ok) drivePrefs.setBackupPending(false)
        return ok
    }

    @Volatile var ultimoFallo: String? = null
        private set

    private suspend fun runBackup(manual: Boolean = false): Boolean {
        ultimoFallo = null
        if (!claveDeRespaldo.confirmada() && !drivePrefs.hasPassword()) { ultimoFallo = "no_backup_key"; return false }

        if (!manual && !drivePrefs.hasCompletedManualBackup()) {
            Log.d(TAG, "Skipping auto-backup — waiting for the first manual backup")
            ultimoFallo = "first_manual_backup"
            return false
        }

        if (exportImportRepo.countAllEntries() == 0) {
            Log.d(TAG, "Skipping auto-backup — local vault is empty, won't overwrite Drive backup")
            ultimoFallo = "empty_vault"
            return false
        }

        val usarClave = claveDeRespaldo.confirmada()
        if (usarClave && sessionManager.getSecretKey() == null) { ultimoFallo = "vault_locked"; return false }
        val password = if (usarClave) "" else when (val r = drivePrefs.loadPassword()) {
            is DrivePrefs.LoadResult.Success        -> r.password
            is DrivePrefs.LoadResult.NotConfigured,
            is DrivePrefs.LoadResult.KeyInvalidated -> return false
            DrivePrefs.LoadResult.Locked            -> return false
        }
        val email = drivePrefs.getConnectedEmail() ?: run { ultimoFallo = "no_google_account"; return false }

        if (BackupNetworkPolicy.shouldWaitForWifi(context, exportImportRepo.attachmentsTotalBytes())) {
            Log.d(TAG, "Skipping auto-backup — heavy backup on a metered network, waiting for Wi-Fi")
            drivePrefs.setBackupWaitingWifi(true)
            ultimoFallo = "waiting_for_wifi"
            return false
        }

        val account = android.accounts.Account(email, "com.google")

        drivePrefs.setBackupWaitingWifi(false)
        _isRunning.value = corriendo.incrementAndGet() > 0
        return try {
            val kab = claveDeRespaldo.paraRespaldo { clave -> exportImportRepo.buildKabBackupFile(password, context, clave) }
            val ts  = try { driveRepo.uploadBackupFile(account, kab) } finally { kab.delete() }
            authRepository.setLastDriveSync(ts)
            drivePrefs.setBackupWaitingWifi(false)
            drivePrefs.setDriveFull(false)
            if (usarClave) claveDeRespaldo.resolverRespaldoPendiente()
            if (manual) drivePrefs.markManualBackupCompleted()

            if (drivePrefs.isDualLocalBackupEnabled() && puedeEscribirCopiaLocal()) {
                try {
                    claveDeRespaldo.paraRespaldo { clave -> exportImportRepo.exportJsonToDownloads(password, context, clave) }
                } catch (e: Exception) {
                    Log.w(TAG, "Dual local backup failed: ${e::class.simpleName}", e)
                }
            }

            true
        } catch (e: com.thormakk.keyarmor.data.drive.DriveStorageFullException) {
            Log.w(TAG, "Auto-backup failed: Google Drive is full")
            drivePrefs.setDriveFull(true)
            ultimoFallo = "drive_full"
            false
        } catch (e: Exception) {
            ultimoFallo = e.message?.takeIf { it.isNotBlank() } ?: e::class.simpleName
            Log.e(TAG, "Auto-backup failed: ${e::class.simpleName}", e)
            false
        } finally {
            _isRunning.value = corriendo.decrementAndGet() > 0
        }
    }

    private fun puedeEscribirCopiaLocal(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val TAG = "DriveAutoBackupManager"
    }
}
