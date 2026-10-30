package com.thormakk.keyarmor.appcheck

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.appcheck.FirebaseAppCheck
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

object AppCheckHeader {
    private const val TAG = "AppCheckHeader"
    private const val HEADER = "X-Firebase-AppCheck"

    fun attach(conn: HttpURLConnection) {
        try {
            val token = Tasks.await(
                FirebaseAppCheck.getInstance().getAppCheckToken(  false),
                3, TimeUnit.SECONDS,
            ).token
            if (token.isNotEmpty()) conn.setRequestProperty(HEADER, token)
        } catch (e: Exception) {
            Log.d(TAG, "App Check token no disponible; sigo sin header", e)
        }
    }
}
