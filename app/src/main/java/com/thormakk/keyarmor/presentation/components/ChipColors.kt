package com.thormakk.keyarmor.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal const val TAG_CHIP_MAX_CHARS = 12

internal fun String.truncateTag(): String =
    if (length > TAG_CHIP_MAX_CHARS) take(TAG_CHIP_MAX_CHARS) + "…" else this

@Composable
fun chipTagColor(): Color {
    val bg  = MaterialTheme.colorScheme.background
    val lum = 0.2126f * bg.red + 0.7152f * bg.green + 0.0722f * bg.blue
    return if (lum > 0.5f) Color(0xFF7B1FA2) else Color(0xFFCE93D8)
}

@Composable
fun chipCategoryColor(): Color {
    val bg  = MaterialTheme.colorScheme.background
    val lum = 0.2126f * bg.red + 0.7152f * bg.green + 0.0722f * bg.blue
    return if (lum > 0.5f) Color(0xFF1565C0) else Color(0xFF90CAF9)
}
