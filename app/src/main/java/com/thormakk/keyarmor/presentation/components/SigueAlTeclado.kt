package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class SigueAlTeclado internal constructor() {
    internal var contenido: LayoutCoordinates? = null
    internal var objetivo: LayoutCoordinates? = null
    internal var ultimaAltura: Int = -1
    internal var enfocado: Boolean = false
    internal var dueno: Any? = null
    val campoEnfocado: Boolean get() = enfocado
}

@Composable
fun rememberSigueAlTeclado(): SigueAlTeclado = remember { SigueAlTeclado() }

fun Modifier.sigueAlTecladoContenido(s: SigueAlTeclado): Modifier =
    this.onGloballyPositioned { s.contenido = it }

fun Modifier.sigueAlTecladoObjetivo(s: SigueAlTeclado): Modifier =
    this.onGloballyPositioned { s.objetivo = it }

@Composable
fun Modifier.sigueAlTecladoObjetivoEnfocado(s: SigueAlTeclado): Modifier {
    val token = remember { Any() }
    val coords = remember { arrayOfNulls<LayoutCoordinates>(1) }
    return this
        .onGloballyPositioned {
            coords[0] = it
            if (s.dueno === token) s.objetivo = it
        }
        .onFocusChanged {
            if (it.hasFocus) {
                s.dueno = token
                s.enfocado = true
                s.objetivo = coords[0]
            } else if (s.dueno === token) {
                s.dueno = null
                s.enfocado = false
            }
        }
}

fun Modifier.sigueAlTeclado(
    scrollState: ScrollState,
    s: SigueAlTeclado,
    margen: Dp = 16.dp,
    alFondo: () -> Boolean = { false },
    habilitado: () -> Boolean = { true },
    objetivoY: (() -> Float?)? = null,
): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val altura = placeable.height
    val anterior = s.ultimaAltura
    s.ultimaAltura = altura
    if (anterior > 0 && altura < anterior && habilitado()) {
        val actual = Snapshot.withoutReadObservation { scrollState.value }.toFloat()
        val yDirecto = objetivoY?.invoke()
        val necesario: Float = if (yDirecto != null) {
            yDirecto + margen.toPx() - altura
        } else if (alFondo()) {
            Snapshot.withoutReadObservation { scrollState.maxValue }.toFloat()
        } else {
            val c = s.contenido
            val t = s.objetivo
            if (c != null && t != null && c.isAttached && t.isAttached) {
                c.localPositionOf(t, Offset.Zero).y + t.size.height + margen.toPx() - altura
            } else {
                actual
            }
        }
        val falta = necesario - actual
        if (falta > 0f) scrollState.dispatchRawDelta(falta)
    }
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}
