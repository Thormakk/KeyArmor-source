package com.thormakk.keyarmor.presentation.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.thormakk.keyarmor.data.prefs.FcmPrefs
import com.thormakk.keyarmor.data.repository.FcmRepository
import kotlinx.coroutines.tasks.await

suspend fun registerFcmTokenFromLogin(
    idToken: String,
    fcmRepository: FcmRepository,
    fcmPrefs: FcmPrefs
) {
    try {
        val fcmToken = FirebaseMessaging.getInstance().token.await()
        if (fcmToken == fcmPrefs.lastRegisteredFcmToken && !fcmPrefs.pendingTokenRefresh) return
        val ok = fcmRepository.registerToken(idToken, fcmToken)
        if (ok) {
            fcmPrefs.lastRegisteredFcmToken = fcmToken
            fcmPrefs.pendingTokenRefresh    = false
            Log.d("FcmLoginRegistration", "FCM token registrado tras login interactivo")
        } else {
            Log.w("FcmLoginRegistration", "FCM token registration (login) fallo, se reintentara")
        }
    } catch (e: Exception) {
        Log.d("FcmLoginRegistration", "registerFcmTokenFromLogin skipped: ${e.message}")
    }
}
