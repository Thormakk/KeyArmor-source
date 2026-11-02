package com.thormakk.keyarmor.presentation.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

object CopyFeedback {
    private val events = MutableSharedFlow<Offset>(extraBufferCapacity = 1)
    internal val flow get() = events

    @Volatile internal var lastTouch: Offset? = null
    @Volatile internal var lastTouchAt: Long = 0L

    private const val TOUCH_MAX_AGE_MS = 2_000L

    fun show(context: Context) {
        val touch = lastTouch
        val fresh = touch != null && SystemClock.elapsedRealtime() - lastTouchAt <= TOUCH_MAX_AGE_MS
        val shown = fresh && events.subscriptionCount.value > 0 && events.tryEmit(touch)
        if (!shown) toast(context)
    }

    private fun toast(context: Context) {
        runCatching {
            val texto = context.getString(R.string.action_copied)
            val app = context.applicationContext
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(app, texto, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun CopyFeedbackHost(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf(Offset.Zero) }
    var pillSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(Unit) {
        CopyFeedback.flow.collectLatest { touch ->
            anchor = touch
            visible = true
            delay(1_300)
            visible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull() ?: continue
                        if (change.pressed || change.previousPressed) {
                            CopyFeedback.lastTouch = change.position
                            CopyFeedback.lastTouchAt = SystemClock.elapsedRealtime()
                        }
                    }
                }
            }
    ) {
        content()
        AnimatedVisibility(visible = visible) {
            val gap = 28.dp.value
            Text(
                text  = stringResource(R.string.action_copied),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .offset {
                        val x = (anchor.x - pillSize.width - gap * density).coerceAtLeast(8f * density)
                        val y = (anchor.y - pillSize.height / 2f).coerceAtLeast(0f)
                        IntOffset(x.roundToInt(), y.roundToInt())
                    }
                    .onSizeChanged { pillSize = it }
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}
