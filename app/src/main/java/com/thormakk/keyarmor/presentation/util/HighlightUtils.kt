package com.thormakk.keyarmor.presentation.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale

val LocalSearchQuery = compositionLocalOf { "" }

fun highlightText(
    text:           String,
    query:          String,
    highlightColor: Color
): AnnotatedString {
    if (query.isBlank() || text.isBlank()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        val lower      = text.lowercase(Locale.ROOT)
        val lowerQuery = query.trim().lowercase(Locale.ROOT)
        if (lowerQuery.isBlank()) return@buildAnnotatedString
        var start = lower.indexOf(lowerQuery)
        while (start >= 0) {
            addStyle(
                style = SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold),
                start = start,
                end   = start + lowerQuery.length
            )
            start = lower.indexOf(lowerQuery, start + lowerQuery.length)
        }
    }
}
