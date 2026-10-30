package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

private const val PISO = 0.85f

private const val INTENTOS = 6

@Composable
fun recordarTamanoQueEntra(
    textos: List<String>,
    estilo: TextStyle,
    disponible: Dp,
): TextUnit {
    val medidor = rememberTextMeasurer()
    val densidad = LocalDensity.current
    return remember(medidor, textos, estilo, disponible, densidad.density, densidad.fontScale) {
        tamanoQueEntra(medidor, densidad, textos, estilo, disponible)
    }
}

private fun tamanoQueEntra(
    medidor: TextMeasurer,
    densidad: Density,
    textos: List<String>,
    estilo: TextStyle,
    disponible: Dp,
): TextUnit {
    val pedido = estilo.fontSize
    if (pedido == TextUnit.Unspecified) return pedido
    if (disponible == Dp.Infinity || disponible.value.isNaN() || disponible <= 0.dp) return pedido
    return with(densidad) {
        val disponiblePx = disponible.toPx()
        val pisoPx = (pedido.value * PISO).dp.toPx().coerceAtMost(pedido.toPx())
        var px = pedido.toPx()
        var listo = false
        var intento = 0
        while (!listo && intento < INTENTOS) {
            val ancho = textos.maxOfOrNull { t -> anchoDe(medidor, t, estilo, px.toSp()) } ?: 0f
            if (ancho <= disponiblePx || px <= pisoPx) {
                listo = true
            } else {
                px = (px * (disponiblePx / ancho) * 0.98f).coerceAtLeast(pisoPx)
            }
            intento++
        }
        px.toSp()
    }
}

private fun anchoDe(
    medidor: TextMeasurer,
    texto: String,
    estilo: TextStyle,
    tamano: TextUnit,
): Float = medidor.measure(
    text = texto,
    style = estilo.copy(fontSize = tamano),
    maxLines = 1,
    softWrap = false,
).size.width.toFloat()

@Composable
fun TextoQueSeAjusta(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val estilo = if (fontWeight != null) style.copy(fontWeight = fontWeight) else style
    BoxWithConstraints(modifier = modifier) {
        TextoDeUnaLinea(
            text = text,
            color = color,
            style = estilo.copy(fontSize = recordarTamanoQueEntra(listOf(text), estilo, maxWidth)),
        )
    }
}
