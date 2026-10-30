package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ProvideTextStyle

private val MARGEN_TARJETA = 20.dp
private val MARGEN_CONTENIDO = 12.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: androidx.compose.ui.unit.Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
    conScroll: Boolean = true,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = properties.dismissOnClickOutside,
            securePolicy = properties.securePolicy,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = properties.decorFitsSystemWindows,
        ),
    ) {
        val llenaVentana = !properties.decorFitsSystemWindows
        Box(
            modifier = modifier
                .then(if (llenaVentana) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                .then(
                    if (llenaVentana && properties.dismissOnClickOutside)
                        Modifier.pointerInput(Unit) { detectTapGestures { onDismissRequest() } }
                    else Modifier
                )
                .padding(horizontal = MARGEN_TARJETA, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
                    .then(if (llenaVentana) Modifier.pointerInput(Unit) { detectTapGestures { } } else Modifier),
                shape = shape,
                color = containerColor,
                tonalElevation = tonalElevation,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = MARGEN_CONTENIDO, vertical = 24.dp)
                ) {
                    icon?.let {
                        Box(
                            Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
                        ) { Colored(iconContentColor, null, it) }
                    }
                    title?.let {
                        Box(
                            Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Colored(
                                titleContentColor,
                                MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                                it
                            )
                        }
                    }
                    text?.let {
                        Box(
                            Modifier
                                .weight(1f, fill = false)
                                .padding(bottom = 24.dp)
                                .align(Alignment.Start)
                                .then(if (conScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                        ) { Colored(textContentColor, MaterialTheme.typography.bodyMedium, it) }
                    }
                    FlowRow(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Composable
private fun Colored(color: Color, style: TextStyle?, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalContentColor provides color) {
        if (style != null) ProvideTextStyle(style, content) else content()
    }
}
