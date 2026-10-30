package com.thormakk.keyarmor.passkey

import android.os.Build
import android.util.Base64
import androidx.credentials.provider.CallingAppInfo
import com.thormakk.keyarmor.autofill.TrustedBrowsers
import java.security.MessageDigest

object PasskeyOriginResolver {

    fun resolve(callingAppInfo: CallingAppInfo?): String? {
        if (callingAppInfo == null) return null
        return try {
            val browserOrigin = callingAppInfo.getOrigin(TrustedBrowsers.privilegedAllowlistJson)
            if (!browserOrigin.isNullOrBlank()) normalizeOrigin(browserOrigin)
            else appKeyHashOrigin(callingAppInfo)
        } catch (e: IllegalStateException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizeOrigin(origin: String): String =
        if (origin.length > 1 && origin.endsWith("/")) origin.dropLast(1) else origin

    private fun appKeyHashOrigin(info: CallingAppInfo): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        val info2 = info.signingInfo
        val cert = (if (info2.hasMultipleSigners()) info2.apkContentsSigners?.firstOrNull()
                    else info2.signingCertificateHistory?.lastOrNull()) ?: return null
        val hash = MessageDigest.getInstance("SHA-256").digest(cert.toByteArray())
        return "android:apk-key-hash:" +
            Base64.encodeToString(hash, Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)
    }
}
