package com.thormakk.keyarmor.presentation.components

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
