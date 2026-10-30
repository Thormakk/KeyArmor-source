package com.thormakk.keyarmor.security

import android.util.Base64
import java.security.MessageDigest

object KeyFingerprint {

    private const val BYTES = 8

    private const val GROUP = 4

    fun of(publicKeyB64: String?): String? {
        if (publicKeyB64.isNullOrBlank()) return null
        val bytes = runCatching { Base64.decode(publicKeyB64, Base64.NO_WRAP) }.getOrNull()
            ?: runCatching { Base64.decode(publicKeyB64, Base64.DEFAULT) }.getOrNull()
            ?: return null
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes)
        val hex = hash.take(BYTES).joinToString("") { "%02X".format(it) }
        return hex.chunked(GROUP).joinToString("-")
    }
}
