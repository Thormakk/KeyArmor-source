package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun Modifier.seguirTextoMultilinea(): Modifier {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    var enfocado by remember { mutableStateOf(false) }
    var ultimaAltura by remember { mutableIntStateOf(0) }
    val bandaPx = with(LocalDensity.current) { 48.dp.toPx() }
    return this
        .bringIntoViewRequester(requester)
        .onFocusChanged { enfocado = it.hasFocus }
        .onSizeChanged { tam ->
            val crecio = ultimaAltura in 1 until tam.height
            ultimaAltura = tam.height
            if (crecio && enfocado) {
                scope.launch {
                    requester.bringIntoView(
                        Rect(0f, (tam.height - bandaPx).coerceAtLeast(0f), tam.width.toFloat(), tam.height.toFloat())
                    )
                }
            }
        }
}
