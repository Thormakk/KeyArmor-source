package com.thormakk.keyarmor.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.thormakk.keyarmor.ui.theme.LocalIsBlackChrome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topBarColors(
    baseContainerColor: Color = MaterialTheme.colorScheme.surface
): TopAppBarColors {
    val isBlackChrome = LocalIsBlackChrome.current
    return if (isBlackChrome) {
        TopAppBarDefaults.topAppBarColors(
            containerColor             = Color.Black,
            scrolledContainerColor     = Color.Black,
            titleContentColor          = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor     = Color.White
        )
    } else {
        TopAppBarDefaults.topAppBarColors(containerColor = baseContainerColor)
    }
}
