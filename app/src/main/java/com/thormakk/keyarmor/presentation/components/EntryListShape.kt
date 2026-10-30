package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun entryItemShape(isFirst: Boolean, isLast: Boolean, radius: Dp = 8.dp): RoundedCornerShape = when {
    isFirst && isLast -> RoundedCornerShape(radius)
    isFirst           -> RoundedCornerShape(topStart = radius, topEnd = radius, bottomStart = 0.dp, bottomEnd = 0.dp)
    isLast            -> RoundedCornerShape(topStart = 0.dp,   topEnd = 0.dp,   bottomStart = radius, bottomEnd = radius)
    else              -> RoundedCornerShape(0.dp)
}
