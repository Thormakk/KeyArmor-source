package com.thormakk.keyarmor.data.prefs

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdatePrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)

    fun shouldShowWhatsNew(): Boolean {
        val lastSeen = prefs.getInt(KEY_LAST_SEEN_VERSION, 0)
        return lastSeen > 0 && currentVersionCode() > lastSeen
    }

    fun markCurrentVersionAsSeen() {
        prefs.edit { putInt(KEY_LAST_SEEN_VERSION, currentVersionCode()) }
    }

    fun initForNewInstall() {
        if (prefs.getInt(KEY_LAST_SEEN_VERSION, 0) == 0) {
            prefs.edit { putInt(KEY_LAST_SEEN_VERSION, currentVersionCode()) }
        }
    }

    @Suppress("DEPRECATION")
    private fun currentVersionCode(): Int = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        else
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
    }.getOrDefault(0)

    companion object {
        private const val KEY_LAST_SEEN_VERSION = "last_seen_version_code"
    }
}
