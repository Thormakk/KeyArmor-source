package com.thormakk.keyarmor.data.drive

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object BackupNetworkPolicy {

    const val HEAVY_BACKUP_BYTES: Long = 20L * 1024 * 1024

    fun isMetered(context: Context): Boolean = runCatching {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
            ?.isActiveNetworkMetered ?: false
    }.getOrDefault(false)

    fun isOnline(context: Context): Boolean = runCatching {
        val cm   = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        caps != null &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }.getOrDefault(true)

    fun shouldWaitForWifi(context: Context, attachmentBytes: Long): Boolean =
        attachmentBytes > HEAVY_BACKUP_BYTES && isMetered(context)
}
