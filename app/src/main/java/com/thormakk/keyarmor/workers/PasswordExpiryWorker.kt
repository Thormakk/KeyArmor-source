package com.thormakk.keyarmor.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import com.thormakk.keyarmor.data.prefs.ExpiryNotificationPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.IdentityDocumentRepository
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.data.repository.ReminderRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.data.repository.VehicleRepository
import com.thormakk.keyarmor.domain.model.NotificationType
import com.thormakk.keyarmor.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class PasswordExpiryWorker @AssistedInject constructor(
    @Assisted private val contextInyectado: Context,
    @Assisted params: WorkerParameters,
    private val vaultRepository:            VaultRepository,
    private val identityDocumentRepository: IdentityDocumentRepository,
    private val vehicleRepository:          VehicleRepository,
    private val reminderRepository:         ReminderRepository,
    private val prefs:                      ExpiryNotificationPrefs,
    private val notifRepository:            InAppNotificationRepository,
    private val planPrefs:                  PlanPrefs
) : CoroutineWorker(contextInyectado, params) {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    override suspend fun doWork(): Result {
        if (!prefs.isEnabled()) return Result.success()
        if (planPrefs.effectivePlan() == UserPlan.FREE) return Result.success()

        val now   = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000

        val timestamps = vaultRepository.getExpiryTimestamps() +
            identityDocumentRepository.getExpiryTimestamps() +
            vehicleRepository.getExpiryTimestamps()

        val notifiedKeys = prefs.getNotifiedKeys().toMutableSet()

        val idsWithCustomSchedules = reminderRepository.getIdsWithCustomSchedules()

        prefs.getWarningDaySet().sorted().forEach { days ->
            val warnMs = days * dayMs
            val bucketIds = reminderRepository.getExpiringIds(now, warnMs)
                .filter { it !in idsWithCustomSchedules }
                .toSet()

            notifiedKeys.removeAll { key ->
                key.endsWith(":$days") && key.startsWith("reminder:") &&
                    key.removePrefix("reminder:").removeSuffix(":$days") !in bucketIds
            }
            val newIds = bucketIds.filter { "reminder:$it:$days" !in notifiedKeys }
            if (newIds.isEmpty()) return@forEach

            val namedPairs = reminderRepository.getExpiringReminderPairs(now, warnMs)
                .filter { it.first in newIds }

            if (namedPairs.isNotEmpty()) {
                val single = namedPairs.singleOrNull().takeIf { newIds.size == 1 }
                if (single != null) {
                    NotificationHelper.notifyExpiringReminder(
                        context, single.first, single.second, days
                    )
                    val title = context.getString(R.string.notif_reminder_title, single.second)
                    val body  = context.getString(R.string.notif_reminder_body, days)
                    notifRepository.save(NotificationType.EXPIRING, title, body)
                } else {
                    NotificationHelper.notifyExpiring(context, newIds.size, days)
                    val title = context.resources.getQuantityString(
                        R.plurals.notif_expiring_title, newIds.size, newIds.size
                    )
                    val body = context.getString(R.string.notif_expiring_body, days)
                    notifRepository.save(NotificationType.EXPIRING, title, body)
                }
            } else {
                if (newIds.isNotEmpty()) {
                    NotificationHelper.notifyExpiring(context, newIds.size, days)
                    val title = context.resources.getQuantityString(
                        R.plurals.notif_expiring_title, newIds.size, newIds.size
                    )
                    val body = context.getString(R.string.notif_expiring_body, days)
                    notifRepository.save(NotificationType.EXPIRING, title, body)
                }
            }
            newIds.forEach { notifiedKeys.add("reminder:$it:$days") }
        }

        notifRepository.deleteActiveExpiringWithNullTarget()

        prefs.getWarningDaySet().sorted().forEach { days ->
            val warnMs   = days * dayMs
            val expiring = timestamps.filter { ts -> ts > now && (ts - now) <= warnMs }
            if (expiring.isEmpty()) return@forEach

            val vaultIds    = vaultRepository.getExpiringEntryIds(now, warnMs)
            val identityIds = identityDocumentRepository.getExpiringEntryIds(now, warnMs)
            val vehicleIds  = vehicleRepository.getExpiringEntryIds(now, warnMs)
            val allIds      = (vaultIds + identityIds + vehicleIds).toSet()

            notifiedKeys.removeAll { key ->
                key.endsWith(":$days") && key.startsWith("entry:") &&
                    key.removePrefix("entry:").removeSuffix(":$days") !in allIds
            }
            val newIds = allIds.filter { "entry:$it:$days" !in notifiedKeys }
            if (newIds.isEmpty()) return@forEach

            val vaultPairs    = vaultRepository.getExpiringEntryPairs(now, warnMs)
            val identityPairs = identityDocumentRepository.getExpiringEntryPairs(now, warnMs)
            val vehiclePairs  = vehicleRepository.getExpiringEntryPairs(now, warnMs)
            val namedPairs    = (vaultPairs + identityPairs + vehiclePairs).filter { it.first in newIds }

            val singleId = newIds.singleOrNull()
            val targetId = when {
                singleId != null && singleId in vaultIds    -> "password:$singleId"
                singleId != null && singleId in identityIds -> "identity:$singleId"
                singleId != null && singleId in vehicleIds  -> "vehicle:$singleId"
                else                                        -> null
            }

            val single = namedPairs.singleOrNull().takeIf { newIds.size == 1 }

            if (single != null) {
                NotificationHelper.notifyExpiringEntry(context, single.first, single.second, days)
                val title = context.getString(R.string.notif_expiring_entry_title, single.second)
                val body  = context.getString(R.string.notif_expiring_body_entry, single.second, days)
                notifRepository.save(NotificationType.EXPIRING, title, body, targetId)
            } else {
                NotificationHelper.notifyExpiring(context, newIds.size, days)
                val title = context.resources.getQuantityString(
                    R.plurals.notif_expiring_title, newIds.size, newIds.size
                )
                val body = context.getString(R.string.notif_expiring_body, days)
                notifRepository.save(NotificationType.EXPIRING, title, body, targetId)
            }
            newIds.forEach { notifiedKeys.add("entry:$it:$days") }
        }

        prefs.setNotifiedKeys(notifiedKeys)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "PasswordExpiryWork"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PasswordExpiryWorker>(
                1, TimeUnit.DAYS
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<PasswordExpiryWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
