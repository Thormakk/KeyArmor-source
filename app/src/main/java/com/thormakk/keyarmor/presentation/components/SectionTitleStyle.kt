package com.thormakk.keyarmor.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.sp

val SectionTitleFontSize = 19.55.sp

@Composable
fun sectionTitleColor(): Color =
    if (MaterialTheme.colorScheme.background.luminance() > 0.5f)
        Color.Black.copy(alpha = 0.9f)
    else
        Color.White.copy(alpha = 0.9f)
