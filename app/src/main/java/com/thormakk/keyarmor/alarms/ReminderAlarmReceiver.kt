package com.thormakk.keyarmor.alarms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.thormakk.keyarmor.MainActivity
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import com.thormakk.keyarmor.data.repository.InAppNotificationRepository
import com.thormakk.keyarmor.domain.model.NotificationType
import com.thormakk.keyarmor.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notifRepository: InAppNotificationRepository

    companion object {
        const val CHANNEL_ID = "keyarmor_reminders_v2"
        private const val OLD_CHANNEL_ID = "keyarmor_reminders"

        fun ensureChannel(contextInyectado: Context) {
            val context = contextoDelIdioma(contextInyectado)
            val nm = context.getSystemService(NotificationManager::class.java)
            runCatching { nm.deleteNotificationChannel(OLD_CHANNEL_ID) }
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_reminder_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notif_reminder_channel_desc)
                    enableVibration(true)
                    enableLights(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val audioAttrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(soundUri, audioAttrs)
                }
            )
        }
    }

    override fun onReceive(contextDelSistema: Context, intent: Intent) {
        if (!contextDelSistema.getDatabasePath("keyarmor.db").exists()) return

        val context = contextoDelIdioma(contextDelSistema)
        val reminderId   = intent.getStringExtra(ReminderAlarmScheduler.EXTRA_REMINDER_ID)   ?: return
        val reminderName = intent.getStringExtra(ReminderAlarmScheduler.EXTRA_REMINDER_NAME)
            ?: context.getString(R.string.reminder_alarm_default_name)
        val reminderNote = intent.getStringExtra(ReminderAlarmScheduler.EXTRA_REMINDER_NOTE) ?: ""
        val scheduleId   = intent.getStringExtra(ReminderAlarmScheduler.EXTRA_SCHEDULE_ID)   ?: return

        ensureChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId)
        }
        val pi = PendingIntent.getActivity(
            context,
            scheduleId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmBody = reminderNote.ifBlank { context.getString(R.string.reminder_alarm_body) }

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminderName)
            .setContentText(alarmBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alarmBody))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.reminder_alarm_body))
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()
            )
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nmCompat = NotificationManagerCompat.from(context)
        if (nmCompat.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) {
            nmCompat.notify(scheduleId.hashCode(), notif)
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notifRepository.save(
                    type     = NotificationType.REMINDER,
                    title    = reminderName,
                    body     = alarmBody,
                    targetId = reminderId
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
