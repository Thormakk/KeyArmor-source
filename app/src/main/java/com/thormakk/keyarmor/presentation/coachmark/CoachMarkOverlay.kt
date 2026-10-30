package com.thormakk.keyarmor.presentation.coachmark

import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun CoachMarkOverlay(controller: CoachMarkController) {
    if (!controller.active) return
    val spec = controller.specs.getOrNull(controller.stepIndex) ?: return

    val density = LocalDensity.current
    val scrim = Color.Black.copy(alpha = 0.72f)
    val accent = MaterialTheme.colorScheme.primary
    val padPx = with(density) { 12.dp.toPx() }
    val ringPx = with(density) { 2.dp.toPx() }
    val cornerPx = with(density) { 12.dp.toPx() }
    val gapPx = with(density) { 12.dp.toPx() }

    LaunchedEffect(controller.currentScreen, controller.stepIndex) {
        if (controller.anchorRect(spec.anchorId) == null) {
            delay(700)
            if (controller.active && controller.anchorRect(spec.anchorId) == null) {
                controller.autoSkip()
            }
        }
    }

    val rect = controller.anchorRect(spec.anchorId)
    if (rect == null) {
        return
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxHpx = constraints.maxHeight.toFloat()

        val sysBars = WindowInsets.systemBars
        val topInsetPx    = sysBars.getTop(density).toFloat()
        val bottomInsetPx = sysBars.getBottom(density).toFloat()
        val marginPx      = with(density) { 16.dp.toPx() }
        val safeTop    = topInsetPx + marginPx
        val safeBottom = maxHpx - bottomInsetPx - marginPx
        val availH     = (safeBottom - safeTop).coerceAtLeast(1f)

        val maxWpx = constraints.maxWidth.toFloat()
        val edgePx      = with(density) { 4.dp.toPx() }
        val hLeftBound  = edgePx
        val hRightBound = maxWpx - edgePx
        val hTopBound   = topInsetPx + edgePx
        val hBotBound   = maxHpx - bottomInsetPx - edgePx

        val rawHole = Rect(
            left   = rect.left   - padPx,
            top    = rect.top    - padPx,
            right  = rect.right  + padPx,
            bottom = rect.bottom + padPx,
        )
        val targetVisible =
            rawHole.right  > hLeftBound && rawHole.left < hRightBound &&
            rawHole.bottom > hTopBound  && rawHole.top  < hBotBound
        val maxHoleH      = availH * 0.4f
        val holeTop       = rawHole.top.coerceIn(hTopBound, hBotBound)
        val holeBottomRaw = if (rawHole.height > maxHoleH) holeTop + maxHoleH else rawHole.bottom
        val hole: Rect? = if (!targetVisible) null else Rect(
            left   = rawHole.left.coerceIn(hLeftBound, hRightBound - 1f),
            top    = holeTop,
            right  = rawHole.right.coerceIn(hLeftBound + 1f, hRightBound),
            bottom = holeBottomRaw.coerceIn(hTopBound + 1f, hBotBound),
        )

        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {   }
                }
        ) {
            if (hole == null) {
                drawRect(scrim)
            } else {
                val path = Path().apply {
                    addRoundRect(RoundRect(hole, CornerRadius(cornerPx, cornerPx)))
                }
                clipPath(path, clipOp = ClipOp.Difference) {
                    drawRect(scrim)
                }
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(hole.left, hole.top),
                    size = Size(hole.width, hole.height),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = ringPx),
                )
            }
        }

        var bubbleH by remember(controller.stepIndex, controller.currentScreen) {
            mutableIntStateOf(0)
        }
        val needed  = bubbleH + gapPx
        val yTarget = if (hole == null) {
            (safeTop + safeBottom - bubbleH) / 2f
        } else {
            val spaceBelow = safeBottom - hole.bottom
            val spaceAbove = hole.top - safeTop
            when {
                spaceBelow < needed && spaceAbove < needed -> (safeTop + safeBottom - bubbleH) / 2f
                spaceBelow >= needed                       -> hole.bottom + gapPx
                else                                       -> hole.top - gapPx - bubbleH
            }
        }
        val yPx = yTarget.coerceIn(safeTop, (safeBottom - bubbleH).coerceAtLeast(safeTop))
        val isLast = controller.stepIndex == controller.specs.lastIndex

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 20.dp)
                .offset { IntOffset(0, yPx.roundToInt()) }
                .fillMaxWidth()
                .alpha(if (bubbleH == 0) 0f else 1f)
                .onGloballyPositioned { bubbleH = it.size.height }
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(R.string.cm_step, controller.stepIndex + 1, controller.specs.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(spec.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(spec.bodyRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { controller.skip() }) {
                        Text(stringResource(R.string.cm_skip))
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { controller.next() }, colors = appButtonColors()) {
                        Text(stringResource(if (isLast) R.string.cm_got_it else R.string.cm_next))
                    }
                }
            }
        }
    }
}
