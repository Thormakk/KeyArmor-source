package com.thormakk.keyarmor.presentation.components

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

fun TextStyle.justified(): TextStyle = copy(
    letterSpacing = 0.sp,
    textAlign     = TextAlign.Justify
)
