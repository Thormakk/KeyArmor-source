package com.thormakk.keyarmor.data.time

import android.content.Context
import com.google.android.gms.time.TrustedTime
import com.google.android.gms.time.TrustedTimeClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TrustedTimeProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @Volatile private var client: TrustedTimeClient? = null

    fun warmUp() {
        if (client != null) return
        TrustedTime.createClient(context).addOnSuccessListener { client = it }
    }

    suspend fun nowMillis(): Long? = (client ?: awaitClient())?.computeCurrentUnixEpochMillis()

    suspend fun isTrusted(): Boolean = nowMillis() != null

    private suspend fun awaitClient(): TrustedTimeClient? = suspendCancellableCoroutine { cont ->
        val task = TrustedTime.createClient(context)
        task.addOnSuccessListener { result ->
            client = result
            if (cont.isActive) cont.resume(result)
        }
        task.addOnFailureListener {
            if (cont.isActive) cont.resume(null)
        }
    }
}
