package com.thormakk.keyarmor.data.prefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnapshotPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("snapshot_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WEEKLY_ENABLED    = "weekly_snapshots_enabled"
        private const val KEY_DRIVE_SYNC        = "sync_to_drive_enabled"
        private const val KEY_INCLUDE_ATTACHMENTS = "include_attachments_local"
        private const val KEY_LAST_WEEKLY_AT    = "last_weekly_snapshot_at"
        private const val KEY_INFO_CARD_SEEN    = "time_machine_info_card_seen"
        private const val KEY_DRIVE_HAS_POINTS  = "drive_has_points"
        private val WEEK_MS                     = 7L * 24 * 60 * 60 * 1000
    }

    fun driveHasPoints(): Boolean = prefs.getBoolean(KEY_DRIVE_HAS_POINTS, false)
    fun setDriveHasPoints(value: Boolean) = prefs.edit { putBoolean(KEY_DRIVE_HAS_POINTS, value) }

    fun isWeeklyEnabled(): Boolean =
        prefs.getBoolean(KEY_WEEKLY_ENABLED, true)

    fun setWeeklyEnabled(enabled: Boolean) =
        prefs.edit { putBoolean(KEY_WEEKLY_ENABLED, enabled) }

    fun isSyncToDriveEnabled(): Boolean =
        prefs.getBoolean(KEY_DRIVE_SYNC, true)

    fun setSyncToDriveEnabled(enabled: Boolean) =
        prefs.edit { putBoolean(KEY_DRIVE_SYNC, enabled) }

    fun isIncludeAttachmentsEnabled(): Boolean =
        prefs.getBoolean(KEY_INCLUDE_ATTACHMENTS, true)

    fun setIncludeAttachmentsEnabled(enabled: Boolean) =
        prefs.edit { putBoolean(KEY_INCLUDE_ATTACHMENTS, enabled) }

    fun getLastWeeklyAt(): Long =
        prefs.getLong(KEY_LAST_WEEKLY_AT, 0L)

    fun markWeeklyDone() =
        prefs.edit { putLong(KEY_LAST_WEEKLY_AT, System.currentTimeMillis()) }

    fun isWeeklyDue(): Boolean =
        System.currentTimeMillis() - getLastWeeklyAt() >= WEEK_MS

    fun hasSeenInfoCard(): Boolean =
        prefs.getBoolean(KEY_INFO_CARD_SEEN, false)

    fun markInfoCardSeen() =
        prefs.edit { putBoolean(KEY_INFO_CARD_SEEN, true) }
}
