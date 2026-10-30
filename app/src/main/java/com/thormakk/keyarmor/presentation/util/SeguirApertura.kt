package com.thormakk.keyarmor.presentation.util

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

const val DURACION_APERTURA_MS = 450

suspend fun seguirApertura(
    posicion: () -> Float?,
    objetivo: () -> Float,
    desplazar: suspend (Float) -> Unit,
) {
    val y0 = posicion() ?: return
    val meta = objetivo()
    if (y0 <= meta) return
    val inicio = withFrameNanos { it }
    while (true) {
        val p = ((withFrameNanos { it } - inicio) / (DURACION_APERTURA_MS * 1_000_000f)).coerceIn(0f, 1f)
        val deseado = y0 + (meta - y0) * FastOutSlowInEasing.transform(p)
        val actual = posicion() ?: break
        val delta = actual - deseado
        if (abs(delta) >= 1f) desplazar(delta)
        if (p >= 1f) break
    }
}

suspend fun volverApertura(
    posicion: () -> Float?,
    meta: Float,
    desplazar: suspend (Float) -> Unit,
) {
    val y0 = posicion() ?: return
    if (y0 >= meta - 1f) return
    val inicio = withFrameNanos { it }
    while (true) {
        val p = ((withFrameNanos { it } - inicio) / (DURACION_APERTURA_MS * 1_000_000f)).coerceIn(0f, 1f)
        val deseado = y0 + (meta - y0) * FastOutSlowInEasing.transform(p)
        val actual = posicion() ?: break
        val delta = actual - deseado
        if (abs(delta) >= 1f) desplazar(delta)
        if (p >= 1f) break
    }
}

val EntradaDeApertura: EnterTransition =
    expandVertically(
        animationSpec = tween(DURACION_APERTURA_MS, easing = FastOutSlowInEasing),
        expandFrom = Alignment.Top
    ) + fadeIn(animationSpec = tween(DURACION_APERTURA_MS, easing = FastOutSlowInEasing))

val SalidaDeApertura: ExitTransition =
    shrinkVertically(
        animationSpec = tween(DURACION_APERTURA_MS, easing = FastOutSlowInEasing),
        shrinkTowards = Alignment.Top
    ) + fadeOut(animationSpec = tween(DURACION_APERTURA_MS, easing = FastOutSlowInEasing))

class PosicionDeTitulo {
    var y: Float = 0f
    var yAlAbrir: Float? = null
    var corregir: () -> Float = { 0f }
}

fun Modifier.registrarPosicion(destino: PosicionDeTitulo): Modifier =
    onGloballyPositioned { destino.y = it.positionInRoot().y - destino.corregir() }

class SeguidorDeApertura(
    private val scope: CoroutineScope,
    private val fraccion: Float,
    private val desplazar: suspend (Float) -> Unit,
) {
    private var arriba = 0f
    private var alto = 0f

    var margenMinimo: Float = 0f

    fun medirContenedor(arriba: Float, alto: Float) {
        this.arriba = arriba
        this.alto = alto
    }

    fun abrir(titulo: PosicionDeTitulo) {
        titulo.yAlAbrir = titulo.y
        scope.launch {
            seguirApertura(
                posicion = { titulo.y },
                objetivo = { arriba + maxOf(alto * fraccion, margenMinimo) },
                desplazar = desplazar,
            )
        }
    }

    fun cerrar(titulo: PosicionDeTitulo) {
        val meta = titulo.yAlAbrir ?: return
        scope.launch {
            volverApertura(
                posicion = { titulo.y },
                meta = meta,
                desplazar = desplazar,
            )
        }
    }
}

fun Modifier.contenedorDe(seguidor: SeguidorDeApertura): Modifier = onGloballyPositioned {
    seguidor.medirContenedor(it.positionInRoot().y, it.size.height.toFloat())
}

@Composable
fun rememberSeguidorDeApertura(fraccion: Float = 0.2f, desplazar: suspend (Float) -> Unit): SeguidorDeApertura {
    val scope = rememberCoroutineScope()
    val actual = rememberUpdatedState(desplazar)
    return remember(scope, fraccion) { SeguidorDeApertura(scope, fraccion) { actual.value(it) } }
}
