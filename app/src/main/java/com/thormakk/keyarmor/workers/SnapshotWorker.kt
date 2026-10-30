package com.thormakk.keyarmor.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thormakk.keyarmor.data.prefs.SnapshotPrefs
import com.thormakk.keyarmor.data.repository.SnapshotRepository
import com.thormakk.keyarmor.domain.model.SnapshotTrigger
import com.thormakk.keyarmor.security.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SnapshotWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val snapshotRepository: SnapshotRepository,
    private val snapshotPrefs:      SnapshotPrefs,
    private val sessionManager:     SessionManager,
    private val autoLockManager:    com.thormakk.keyarmor.security.AutoLockManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!snapshotRepository.canCreateSnapshots()) return Result.success()
        if (!snapshotPrefs.isWeeklyEnabled()) return Result.success()
        if (!snapshotPrefs.isWeeklyDue())    return Result.success()
        autoLockManager.enforceIfExpired()
        if (!sessionManager.isUnlocked())    return Result.retry()

        return try {
            snapshotRepository.createSnapshot(SnapshotTrigger.WEEKLY)
            snapshotPrefs.markWeeklyDone()
            Result.success()
        } catch (e: com.thormakk.keyarmor.data.repository.EmptyVaultSkippedException) {
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "KeyArmor_SnapshotWeekly"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SnapshotWorker>(7, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
