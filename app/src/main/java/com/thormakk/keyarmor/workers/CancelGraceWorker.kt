package com.thormakk.keyarmor.workers

import android.content.Context
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.repository.CancelGraceResult
import com.thormakk.keyarmor.data.repository.InheritanceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class CancelGraceWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val drivePrefs:            DrivePrefs,
    private val inheritancePrefs:      InheritancePrefs,
    private val inheritanceRepository: InheritanceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!inheritancePrefs.isEnabled()) return Result.success()

        if (!inheritancePrefs.isApproachingGracePeriod()) {
            cancelRetries(context)
            return Result.success()
        }

        val ownerEmail = drivePrefs.getConnectedEmail() ?: return Result.success()

        val ownerIdToken = getFirebaseIdentityTokenOrNull(context)?.idToken

        return try {
            val resultado = inheritanceRepository.cancelGrace(ownerEmail, ownerIdToken)
            if (resultado == CancelGraceResult.ALREADY_INHERITED) {
                android.util.Log.e("CancelGraceWorker", "cancelGrace: la herencia YA se entregó")
                cancelRetries(context)
                return Result.success()
            }
            inheritancePrefs.setLastHeartbeatSentAt(System.currentTimeMillis())
            cancelRetries(context)
            HeartbeatWorker.cancelBackup(context)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val RETRY_D1 = "cancel_grace_retry_d1"
        private const val RETRY_D3 = "cancel_grace_retry_d3"
        private const val RETRY_D5 = "cancel_grace_retry_d5"

        fun scheduleRetries(context: Context) {
            val wm = WorkManager.getInstance(context)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            listOf(1L to RETRY_D1, 3L to RETRY_D3, 5L to RETRY_D5).forEach { (days, name) ->
                val request = OneTimeWorkRequestBuilder<CancelGraceWorker>()
                    .setInitialDelay(days, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .build()
                wm.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
            }
        }

        fun cancelRetries(context: Context) {
            val wm = WorkManager.getInstance(context)
            listOf(RETRY_D1, RETRY_D3, RETRY_D5).forEach { wm.cancelUniqueWork(it) }
        }
    }
}
