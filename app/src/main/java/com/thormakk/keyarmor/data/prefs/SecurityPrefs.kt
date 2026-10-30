package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    fun wasNotificationPermissionAsked(): Boolean = prefs.getBoolean(KEY_NOTIF_PERM_ASKED, false)
    fun setNotificationPermissionAsked() = prefs.edit { putBoolean(KEY_NOTIF_PERM_ASKED, true) }

    private companion object {
        const val KEY_NOTIF_PERM_ASKED = "notification_permission_asked"
    }

    fun getLastReviewAt(): Long = prefs.getLong("last_review_at", 0L)

    fun updateLastReview() {
        prefs.edit { putLong("last_review_at", System.currentTimeMillis()) }
    }

    fun getLastBackupAt(): Long = prefs.getLong("last_backup_at", 0L)

    fun markBackupDone() {
        prefs.edit { putLong("last_backup_at", System.currentTimeMillis()) }
    }
}
