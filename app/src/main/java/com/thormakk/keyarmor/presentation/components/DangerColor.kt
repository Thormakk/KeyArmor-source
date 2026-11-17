package com.thormakk.keyarmor.presentation.components

import androidx.compose.ui.graphics.compositeOver
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.thormakk.keyarmor.presentation.screens.settings.contrastRatio
import com.thormakk.keyarmor.ui.theme.InfoBlueMuted
import com.thormakk.keyarmor.ui.theme.InfoBlueMutedDark
import com.thormakk.keyarmor.ui.theme.WarningAmber
import com.thormakk.keyarmor.ui.theme.WarningAmberDark

@Composable
fun dangerColor(): Color = warningColor()

@Composable
fun warningColor(): Color =
    if (contrastRatio(MaterialTheme.colorScheme.background, WarningAmber) < 3f)
        WarningAmberDark
    else
        WarningAmber

@Composable
fun infoColor(): Color =
    if (contrastRatio(MaterialTheme.colorScheme.background, InfoBlueMuted) < 3f)
        InfoBlueMutedDark
    else
        InfoBlueMuted

@Composable
fun AmberErrors(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.let { cs ->
            val ambar = warningColor()
            cs.copy(
                error            = ambar,
                errorContainer   = ambar.copy(alpha = 0.18f).compositeOver(cs.surface),
                onErrorContainer = cs.onSurface
            )
        },
        typography  = MaterialTheme.typography,
        shapes      = MaterialTheme.shapes,
        content     = content
    )
}
