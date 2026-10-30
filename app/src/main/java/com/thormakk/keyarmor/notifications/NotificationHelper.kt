package com.thormakk.keyarmor.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.thormakk.keyarmor.MainActivity
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.language.contextoDelIdioma
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import com.thormakk.keyarmor.notifications.NotificationHelper.EXTRA_ENTRY_ID
import com.thormakk.keyarmor.notifications.NotificationHelper.EXTRA_REMINDER_ID
import com.thormakk.keyarmor.notifications.NotificationHelper.notifyExpiring

object NotificationHelper {

    private const val CHANNEL_ID             = "keyarmor_expiry"
    private const val CHANNEL_BREACH_ID      = "keyarmor_breach"
    private const val CHANNEL_FAMILY_ID      = "keyarmor_family"
    private const val CHANNEL_TRAVEL_ID      = "keyarmor_travel"

    private const val NOTIF_ID_EXPIRING_BASE = 2000
    private const val NOTIF_ID_BREACH        = 3000
    private const val NOTIF_ID_REMINDER_BASE = 4000
    private const val NOTIF_ID_FAMILY_INVITE       = 5000
    private const val NOTIF_ID_FAMILY_MEMBER_LEFT  = 5001
    private const val NOTIF_ID_FAMILY_DECLINED     = 5002
    private const val NOTIF_ID_FAMILY_REVOKED      = 5003
    private const val NOTIF_ID_FAMILY_PREMIUM_REVOKED = 5005
    private const val NOTIF_ID_TRAVEL_SUGGESTION       = 6000

    const val EXTRA_ENTRY_ID = "keyarmor_entry_id"

    const val EXTRA_REMINDER_ID = "keyarmor_reminder_id"

    const val EXTRA_FAMILY_ADMIN_EMAIL  = "keyarmor_family_admin_email"
    const val EXTRA_FAMILY_INVITE_TOKEN = "keyarmor_family_invite_token"

    const val EXTRA_TRAVEL_SUGGESTION = "keyarmor_travel_suggestion"

    const val EXTRA_FROM_NOTIFICATION = "keyarmor_from_notification"

    fun createAllChannels(contextInyectado: Context) {
        val context = contextoDelIdioma(contextInyectado)
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.notif_channel_desc) }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BREACH_ID,
                context.getString(R.string.notif_breach_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.notif_breach_channel_desc) }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_FAMILY_ID,
                context.getString(R.string.notif_family_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.notif_family_channel_desc) }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TRAVEL_ID,
                context.getString(R.string.notif_travel_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.notif_travel_channel_desc) }
        )
    }

    fun notifyFamilyInvite(contextInyectado: Context, adminEmail: String, inviteToken: String) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_FAMILY_ADMIN_EMAIL,  adminEmail)
            putExtra(EXTRA_FAMILY_INVITE_TOKEN, inviteToken)
        }
        val pi = PendingIntent.getActivity(
            context, NOTIF_ID_FAMILY_INVITE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_FAMILY_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(context.getString(R.string.notif_family_invite_title))
            .setContentText(context.getString(R.string.notif_family_invite_text, adminEmail))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.notif_family_invite_big_text, adminEmail)))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_FAMILY_INVITE, notif)
    }

    fun notifyMemberLeft(contextInyectado: Context, memberEmail: String) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }
        val pi = PendingIntent.getActivity(
            context, NOTIF_ID_FAMILY_MEMBER_LEFT, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_FAMILY_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(context.getString(R.string.family_member_left_notif_title, memberEmail))
            .setContentText(context.getString(R.string.family_member_left_notif_body, memberEmail))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_FAMILY_MEMBER_LEFT, notif)
    }

    fun notifyMemberDeclined(contextInyectado: Context, memberEmail: String) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }
        val pi = PendingIntent.getActivity(
            context, NOTIF_ID_FAMILY_DECLINED, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_FAMILY_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(context.getString(R.string.family_member_declined_notif_title))
            .setContentText(context.getString(R.string.family_member_declined_notif_body, memberEmail))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_FAMILY_DECLINED, notif)
    }

    fun notifyAccessRevoked(contextInyectado: Context, adminEmail: String?) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }
        val pi = PendingIntent.getActivity(
            context, NOTIF_ID_FAMILY_REVOKED, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body = if (!adminEmail.isNullOrBlank())
            context.getString(R.string.family_access_revoked_notif_body_named, adminEmail)
        else
            context.getString(R.string.family_access_revoked_notif_body)
        val notif = NotificationCompat.Builder(context, CHANNEL_FAMILY_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(context.getString(R.string.family_access_revoked_notif_title))
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_FAMILY_REVOKED, notif)
    }

    fun notifyPremiumRevoked(contextInyectado: Context) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }
        val pi = PendingIntent.getActivity(
            context, NOTIF_ID_FAMILY_PREMIUM_REVOKED, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_FAMILY_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(context.getString(R.string.family_premium_revoked_notif_title))
            .setContentText(context.getString(R.string.family_premium_revoked_notif_body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_FAMILY_PREMIUM_REVOKED, notif)
    }


    fun breachBody(context: Context, passwords: Int, entradas: Int): String =
        if (entradas > passwords)
            context.getString(
                R.string.breach_two_figures,
                context.resources.getQuantityString(R.plurals.breach_passwords_count, passwords, passwords),
                context.resources.getQuantityString(R.plurals.breach_entries_count, entradas, entradas)
            )
        else
            context.resources.getQuantityString(R.plurals.notif_breach_body, passwords, passwords)

    fun notifyBreach(contextInyectado: Context, breachedCount: Int, entradas: Int = breachedCount) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }
        val pi = PendingIntent.getActivity(
            context, NOTIF_ID_BREACH, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_BREACH_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(context.getString(R.string.notif_breach_title))
            .setContentText(breachBody(context, breachedCount, entradas))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(breachBody(context, breachedCount, entradas)))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_BREACH, notif)
    }

    fun notifyExpiring(contextInyectado: Context, count: Int, daysAhead: Int) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }
        val pi = PendingIntent.getActivity(
            context, daysAhead, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = context.resources.getQuantityString(
            R.plurals.notif_expiring_title, count, count
        )
        val body = context.getString(R.string.notif_expiring_body, daysAhead)
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_EXPIRING_BASE + daysAhead, notif)
    }

    fun notifyExpiringEntry(contextInyectado: Context, entryId: String, entryTitle: String, daysAhead: Int) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
            putExtra(EXTRA_ENTRY_ID, entryId)
        }
        val pi = PendingIntent.getActivity(
            context, NOTIF_ID_EXPIRING_BASE + daysAhead, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = context.getString(R.string.notif_expiring_entry_title, entryTitle)
        val body  = context.getString(R.string.notif_expiring_body_entry, entryTitle, daysAhead)
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_EXPIRING_BASE + daysAhead, notif)
    }

    fun notifyExpiringReminder(contextInyectado: Context, reminderId: String, reminderName: String, daysAhead: Int) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val pi = PendingIntent.getActivity(
            context, NOTIF_ID_REMINDER_BASE + daysAhead, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = context.getString(R.string.notif_reminder_title, reminderName)
        val body  = context.getString(R.string.notif_reminder_body, daysAhead)
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_REMINDER_BASE + daysAhead, notif)
    }

    fun notifyTravelSuggestion(contextInyectado: Context, dateText: String) {
        val context = contextoDelIdioma(contextInyectado)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_TRAVEL_SUGGESTION, true)
        }
        val pi = PendingIntent.getActivity(
            context, NOTIF_ID_TRAVEL_SUGGESTION, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body = context.getString(R.string.notif_travel_suggestion_body, dateText)
        val notif = NotificationCompat.Builder(context, CHANNEL_TRAVEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(context.getString(R.string.notif_travel_suggestion_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled() && !CalculatorDisguisePrefs.isDisguiseActive(context)) nm.notify(NOTIF_ID_TRAVEL_SUGGESTION, notif)
    }
}
