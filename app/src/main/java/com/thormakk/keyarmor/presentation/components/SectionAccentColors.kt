package com.thormakk.keyarmor.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val SectionColorFinance  = Color(0xFF26C485)

val SectionColorIdentity = Color(0xFF5B9CF6)

val SectionColorNotesLight = Color(0xFFB36A00)

val SectionColorNotesDark  = Color(0xFFFFB946)

@Composable
fun notesAccentColor(): Color {
    val bg = MaterialTheme.colorScheme.background
    return if (bg.luminance() > 0.5f) SectionColorNotesLight else SectionColorNotesDark
}
