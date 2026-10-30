package com.thormakk.keyarmor.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.database.dao.ReminderDao
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import com.thormakk.keyarmor.domain.model.ReminderSchedule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderDao:  ReminderDao
    @Inject lateinit var scheduler:    ReminderAlarmScheduler

    override fun onReceive(contextDelSistema: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        val context = contextoDelIdioma(contextDelSistema)
        val defaultName = context.getString(R.string.reminder_alarm_default_name)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                reminderDao.getActiveWithSchedules().forEach { entity ->
                    ReminderSchedule.listFromJson(entity.alertSchedulesJson)
                        .forEach { schedule ->
                            scheduler.schedule(
                                context        = context,
                                reminderId     = entity.id,
                                reminderName   = defaultName,
                                reminderDateMs = entity.date,
                                schedule       = schedule
                            )
                        }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
