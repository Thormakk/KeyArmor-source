package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

fun Modifier.cuadroConTeclado(): Modifier = this.systemBarsPadding().imePadding()

fun propiedadesConTeclado(usePlatformDefaultWidth: Boolean = true): DialogProperties =
    DialogProperties(
        decorFitsSystemWindows = false,
        usePlatformDefaultWidth = usePlatformDefaultWidth,
    )
