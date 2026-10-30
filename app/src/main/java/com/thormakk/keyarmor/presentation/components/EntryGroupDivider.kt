package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val EntryGroupDividerDefaultEndInset: Dp = 16.dp

@Composable
fun EntryGroupDivider(
    startInset: Dp,
    endInset:   Dp = EntryGroupDividerDefaultEndInset,
    cardColor:  Color,
    gapColor:   Color,
    thickness:  Dp = 0.5.dp
) {
    Row(modifier = Modifier.fillMaxWidth().height(thickness)) {
        Box(
            modifier = Modifier
                .width(startInset)
                .fillMaxHeight()
                .background(cardColor)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(gapColor)
        )
        Box(
            modifier = Modifier
                .width(endInset)
                .fillMaxHeight()
                .background(cardColor)
        )
    }
}
