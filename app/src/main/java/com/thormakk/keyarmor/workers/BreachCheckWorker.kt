package com.thormakk.keyarmor.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import com.thormakk.keyarmor.data.prefs.BreachCheckPrefs
import com.thormakk.keyarmor.data.prefs.BreachResultPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.remote.HibpPasswordChecker
import com.thormakk.keyarmor.data.remote.HibpResult
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.domain.model.NotificationType
import com.thormakk.keyarmor.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class BreachCheckWorker @AssistedInject constructor(
    @Assisted private val contextInyectado: Context,
    @Assisted params: WorkerParameters,
    private val vaultRepository: VaultRepository,
    private val hibpChecker:     HibpPasswordChecker,
    private val prefs:           BreachCheckPrefs,
    private val resultPrefs:     BreachResultPrefs,
    private val notifRepository: InAppNotificationRepository,
    private val planPrefs:       PlanPrefs
) : CoroutineWorker(contextInyectado, params) {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    override suspend fun doWork(): Result {
        if (!prefs.isEnabled()) return Result.success()
        if (planPrefs.effectivePlan() == UserPlan.FREE) return Result.success()

        val entries = vaultRepository.observeEntries().first()

        val seenHashes  = mutableSetOf<String>()
        val comprometidas = mutableMapOf<String, Int>()
        var passwordsComprometidas = 0
        var requestCount  = 0
        var huboErrorDeRed = false

        for (entry in entries) {
            val password = entry.password
            if (password.isEmpty()) continue
            if (!seenHashes.add(hibpChecker.hashFor(password))) continue

            if (requestCount > 0) delay(INTER_REQUEST_DELAY_MS)

            when (val r = hibpChecker.check(password)) {
                is HibpResult.Pwned       -> {
                    passwordsComprometidas++
                    entries.filter { it.password == password }.forEach { comprometidas[it.id] = r.count }
                }
                is HibpResult.RateLimited -> {
                    return Result.retry()
                }
                is HibpResult.NetworkError -> huboErrorDeRed = true
                is HibpResult.Clean        -> Unit
            }
            requestCount++
        }

        resultPrefs.save(if (huboErrorDeRed) resultPrefs.load().conteos + comprometidas else comprometidas)

        if (passwordsComprometidas > 0) {
            val entradas = comprometidas.size
            NotificationHelper.notifyBreach(context, passwordsComprometidas, entradas)
            val title = context.getString(R.string.notif_breach_title)
            val body  = NotificationHelper.breachBody(context, passwordsComprometidas, entradas)
            notifRepository.save(NotificationType.BREACH, title, body)
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "breach_check"

        private const val INTER_REQUEST_DELAY_MS = 300L

        fun schedule(context: Context, frequencyDays: Int, replace: Boolean = false) {
            val wm = WorkManager.getInstance(context)
            val request = PeriodicWorkRequestBuilder<BreachCheckWorker>(
                frequencyDays.toLong(), TimeUnit.DAYS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()
            wm.enqueueUniquePeriodicWork(
                WORK_NAME,
                if (replace) ExistingPeriodicWorkPolicy.REPLACE
                else         ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
