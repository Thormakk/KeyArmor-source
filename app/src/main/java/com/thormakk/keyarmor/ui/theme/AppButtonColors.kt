package com.thormakk.keyarmor.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun appButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(containerColor = appFilledContainer(), contentColor = appFilledContent())

@Composable
fun appFilledContainer(): Color {
    val primary = MaterialTheme.colorScheme.primary
    return if (primary.esBlancoPleno()) Cream200 else primary
}

@Composable
fun appFilledContent(): Color {
    val primary = MaterialTheme.colorScheme.primary
    return if (primary.esBlancoPleno()) Color.Black else MaterialTheme.colorScheme.onPrimary
}

private fun Color.esBlancoPleno() = red > 0.97f && green > 0.97f && blue > 0.97f
