package com.thormakk.keyarmor.alarms

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.thormakk.keyarmor.appEntryIntent
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import com.thormakk.keyarmor.domain.model.Reminder
import com.thormakk.keyarmor.domain.model.ReminderSchedule
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderAlarmScheduler @Inject constructor() {

    companion object {
        const val EXTRA_REMINDER_ID   = "alarm_reminder_id"
        const val EXTRA_REMINDER_NAME = "alarm_reminder_name"
        const val EXTRA_REMINDER_NOTE = "alarm_reminder_note"
        const val EXTRA_SCHEDULE_ID   = "alarm_schedule_id"
    }

    fun scheduleAll(context: Context, reminder: Reminder) {
        reminder.schedules.forEach { schedule ->
            schedule(
                context        = context,
                reminderId     = reminder.id,
                reminderName   = reminder.name,
                reminderNote   = reminder.note,
                reminderDateMs = reminder.date,
                schedule       = schedule
            )
        }
    }

    fun cancelAll(context: Context, reminder: Reminder) {
        reminder.schedules.forEach { cancel(context, it.id) }
    }

    @SuppressLint("ScheduleExactAlarm", "MissingPermission")
    fun schedule(
        context: Context,
        reminderId: String,
        reminderName: String,
        reminderNote: String = "",
        reminderDateMs: Long,
        schedule: ReminderSchedule
    ) {
        if (CalculatorDisguisePrefs.isDisguiseActive(context)) return

        val triggerMs = nextTriggerMs(schedule, reminderDateMs) ?: return
        if (triggerMs <= System.currentTimeMillis()) return

        val am = context.getSystemService(AlarmManager::class.java)

        val alarmPi = alarmPendingIntent(context, reminderId, reminderName, reminderNote, schedule.id)
        val showPi  = showPendingIntent(context, reminderId, schedule.id)

        val canUseExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canUseExact) {
            try {
                am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerMs, showPi), alarmPi)
                return
            } catch (e: SecurityException) {
                Log.w("ReminderAlarmScheduler", "setAlarmClock rechazado pese a canScheduleExactAlarms()==true — cae a alarma inexacta", e)
            }
        }
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, alarmPi)
    }

    fun cancel(context: Context, scheduleId: String) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode(),
            Intent(context, ReminderAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) am.cancel(pi)
    }

    private fun alarmPendingIntent(
        context: Context,
        reminderId: String,
        reminderName: String,
        reminderNote: String,
        scheduleId: String
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        scheduleId.hashCode(),
        Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID,   reminderId)
            putExtra(EXTRA_REMINDER_NAME, reminderName)
            putExtra(EXTRA_REMINDER_NOTE, reminderNote)
            putExtra(EXTRA_SCHEDULE_ID,   scheduleId)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun showPendingIntent(
        context: Context,
        reminderId: String,
        scheduleId: String
    ): PendingIntent = PendingIntent.getActivity(
        context,
        scheduleId.hashCode(),
        appEntryIntent(context).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_REMINDER_ID, reminderId)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun nextTriggerMs(schedule: ReminderSchedule, reminderDateMs: Long): Long? {
        val now = System.currentTimeMillis()
        return when (schedule) {

            is ReminderSchedule.Punctual ->
                schedule.triggerAtMs.takeIf { it > now }

            is ReminderSchedule.Weekly -> {
                for (offset in 0..6) {
                    val cal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, offset)
                        set(Calendar.HOUR_OF_DAY, schedule.hour)
                        set(Calendar.MINUTE,      schedule.minute)
                        set(Calendar.SECOND,      0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val javaDow = cal.get(Calendar.DAY_OF_WEEK)
                    val isoDow  = if (javaDow == Calendar.SUNDAY) 7 else javaDow - 1
                    if (isoDow in schedule.daysOfWeek && cal.timeInMillis > now) {
                        return cal.timeInMillis
                    }
                }
                null
            }

            is ReminderSchedule.Monthly -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, minOf(schedule.dayOfMonth, getActualMaximum(Calendar.DAY_OF_MONTH)))
                    set(Calendar.HOUR_OF_DAY, schedule.hour)
                    set(Calendar.MINUTE,      schedule.minute)
                    set(Calendar.SECOND,      0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (cal.timeInMillis <= now) {
                    cal.add(Calendar.MONTH, 1)
                    cal.set(Calendar.DAY_OF_MONTH, minOf(schedule.dayOfMonth, cal.getActualMaximum(Calendar.DAY_OF_MONTH)))
                }
                cal.timeInMillis
            }

            is ReminderSchedule.Yearly -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.MONTH,        schedule.month - 1)
                    set(Calendar.DAY_OF_MONTH, schedule.dayOfMonth)
                    set(Calendar.HOUR_OF_DAY,  schedule.hour)
                    set(Calendar.MINUTE,       schedule.minute)
                    set(Calendar.SECOND,       0)
                    set(Calendar.MILLISECOND,  0)
                }
                if (cal.timeInMillis <= now) cal.add(Calendar.YEAR, 1)
                cal.timeInMillis
            }

            is ReminderSchedule.Interval -> {
                if (schedule.intervalDays <= 0) return null
                val intervalMs = schedule.intervalDays * 86_400_000L
                var next = Calendar.getInstance().apply {
                    timeInMillis = schedule.startMs
                    set(Calendar.HOUR_OF_DAY, schedule.hour)
                    set(Calendar.MINUTE,      schedule.minute)
                    set(Calendar.SECOND,      0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                while (next <= now) next += intervalMs
                next
            }

            is ReminderSchedule.BeforeDeadline -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = reminderDateMs - schedule.daysBeforeCount * 86_400_000L
                    set(Calendar.HOUR_OF_DAY, schedule.hour)
                    set(Calendar.MINUTE,      schedule.minute)
                    set(Calendar.SECOND,      0)
                    set(Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis.takeIf { it > now }
            }
        }
    }
}
