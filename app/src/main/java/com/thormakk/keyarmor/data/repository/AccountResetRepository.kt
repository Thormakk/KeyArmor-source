package com.thormakk.keyarmor.data.repository

import android.accounts.Account
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.thormakk.keyarmor.alarms.ReminderAlarmScheduler
import com.thormakk.keyarmor.domain.model.ReminderSchedule
import com.thormakk.keyarmor.data.billing.RevenueCatManager
import com.thormakk.keyarmor.data.database.KeyArmorDatabase
import com.thormakk.keyarmor.data.drive.DriveBackupRepository
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.PanicPinPrefs
import com.thormakk.keyarmor.data.storage.AttachmentStorage
import com.thormakk.keyarmor.security.BiometricCryptoManager
import com.thormakk.keyarmor.security.HeirIdentityManager
import com.thormakk.keyarmor.security.OwnerSigningIdentityManager
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountResetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database:                    KeyArmorDatabase,
    private val attachmentRepository:        AttachmentRepository,
    private val snapshotRepository:          SnapshotRepository,
    private val driveBackupRepository:       DriveBackupRepository,
    private val drivePrefs:                  DrivePrefs,
    private val claveDeRespaldo:             com.thormakk.keyarmor.data.backup.ClaveDeRespaldo,
    private val heirIdentityManager:         HeirIdentityManager,
    private val ownerSigningIdentityManager: OwnerSigningIdentityManager,
    private val biometricCryptoManager:      BiometricCryptoManager,
    private val panicPinPrefs:               PanicPinPrefs,
    private val sessionManager:              SessionManager,
    private val attachmentStorage:           AttachmentStorage,
    private val revenueCatManager:           RevenueCatManager,
    private val exportImportRepository:      ExportImportRepository,
    private val reminderAlarmScheduler:      ReminderAlarmScheduler,
) {

    suspend fun wipeLocalEverything(
        includeDrive: Boolean = true,
        includeDownloadsCopies: Boolean = true,
    ) = withContext(Dispatchers.IO) {
        if (includeDrive) runCatching {
            val email = drivePrefs.getConnectedEmail()
            if (email != null) {
                val account = Account(email, "com.google")
                runCatching { driveBackupRepository.deleteBackup(account) }
                runCatching {
                    driveBackupRepository.listDriveSnapshots(account).forEach { snap ->
                        runCatching { driveBackupRepository.deleteSnapshotFromDrive(account, snap.driveFileId) }
                    }
                }
            }
        }

        if (includeDrive) runCatching {
            snapshotRepository.observeSnapshots().first().forEach { snap ->
                runCatching { snapshotRepository.deleteSnapshot(snap.id) }
            }
        }

        runCatching { attachmentRepository.deleteAllPermanently() }
        runCatching { File(context.filesDir, "snapshots").deleteRecursively() }

        runCatching {
            database.reminderDao().getAllEntities().forEach { entidad ->
                ReminderSchedule.listFromJson(entidad.alertSchedulesJson).forEach { schedule ->
                    runCatching { reminderAlarmScheduler.cancel(context, schedule.id) }
                }
            }
        }

        runCatching { database.clearAllTables() }

        runCatching { drivePrefs.clear() }
        runCatching { claveDeRespaldo.clear() }
        runCatching { heirIdentityManager.clear() }
        runCatching { ownerSigningIdentityManager.clear() }
        runCatching { biometricCryptoManager.deleteKey() }
        runCatching { panicPinPrefs.clearAllCoercion() }
        runCatching { panicPinPrefs.clearDecoyKeyMaterial() }
        runCatching { panicPinPrefs.clearHideKeyMaterial() }

        runCatching {
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            prefsDir.listFiles()?.forEach { f ->
                if (f.name.endsWith(".xml")) {
                    val name = f.name.removeSuffix(".xml")
                    runCatching { context.deleteSharedPreferences(name) }
                }
            }
        }

        runCatching { attachmentStorage.clearTempFiles() }
        runCatching { WorkManager.getInstance(context).cancelAllWork() }
        runCatching { revenueCatManager.unlinkIdentity() }

        if (includeDownloadsCopies) {
            runCatching { exportImportRepository.deleteLocalDownloadsBackup(context) }
        }

        runCatching { sessionManager.lock() }
    }

    fun restartApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}
