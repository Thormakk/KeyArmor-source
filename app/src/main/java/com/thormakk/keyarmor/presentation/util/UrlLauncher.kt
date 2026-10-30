package com.thormakk.keyarmor.presentation.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun normalizeUrl(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ""
    val lower = trimmed.lowercase()
    return if (lower.startsWith("http://") || lower.startsWith("https://")) {
        trimmed
    } else {
        "https://$trimmed"
    }
}

fun Context.openUrl(rawUrl: String): Boolean {
    val normalized = normalizeUrl(rawUrl)
    if (normalized.isBlank()) return false
    return try {
        val intent = Intent(Intent.ACTION_VIEW, normalized.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        true
    } catch (_: Throwable) {
        false
    }
}
