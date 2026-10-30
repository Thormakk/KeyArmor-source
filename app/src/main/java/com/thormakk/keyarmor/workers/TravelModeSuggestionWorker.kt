package com.thormakk.keyarmor.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.TravelModePrefs
import com.thormakk.keyarmor.data.prefs.TravelSuggestionPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.domain.model.NotificationType
import com.thormakk.keyarmor.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltWorker
class TravelModeSuggestionWorker @AssistedInject constructor(
    @Assisted private val contextInyectado: Context,
    @Assisted params: WorkerParameters,
    private val suggestionPrefs: TravelSuggestionPrefs,
    private val travelModePrefs: TravelModePrefs,
    private val notifRepository: InAppNotificationRepository,
    private val planPrefs: PlanPrefs
) : CoroutineWorker(contextInyectado, params) {

    private val context: Context get() = contextoDelIdioma(contextInyectado)

    override suspend fun doWork(): Result {
        if (!suggestionPrefs.isEnabled()) return Result.success()
        if (planPrefs.effectivePlan() == UserPlan.FREE) return Result.success()
        if (travelModePrefs.isEnabled()) return Result.success()

        val candidate = withContext(Dispatchers.IO) {
            TravelEventDetector.findUpcomingTrip(
                context    = context,
                nowMillis  = System.currentTimeMillis(),
                windowDays = LOOKAHEAD_DAYS
            )
        } ?: return Result.success()

        val notifiedKeys = suggestionPrefs.getNotifiedKeys()
        if (candidate.eventKey in notifiedKeys) return Result.success()
        if (suggestionPrefs.isDismissed(candidate.eventKey)) return Result.success()

        suggestionPrefs.setPending(candidate.eventKey, candidate.startMillis)
        suggestionPrefs.setNotifiedKeys(notifiedKeys + candidate.eventKey)

        val dateText = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            .format(Date(candidate.startMillis))

        NotificationHelper.notifyTravelSuggestion(context, dateText)
        notifRepository.save(
            type  = NotificationType.TRAVEL_SUGGESTION,
            title = context.getString(R.string.notif_travel_suggestion_title),
            body  = context.getString(R.string.notif_travel_suggestion_body, dateText)
        )

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "travel_mode_suggestion"

        private const val LOOKAHEAD_DAYS = 30

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TravelModeSuggestionWorker>(1, TimeUnit.DAYS).build()
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
