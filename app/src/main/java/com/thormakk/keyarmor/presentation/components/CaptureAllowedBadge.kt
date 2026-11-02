package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.util.LocalScreenCaptureManager
import com.thormakk.keyarmor.ui.theme.WarningAmber
import kotlinx.coroutines.delay

@Composable
fun CaptureAllowedBadge() {
    val manager = LocalScreenCaptureManager.current
    val active by manager.permitActive.collectAsStateWithLifecycle()
    if (!active) return

    var showConfirm by remember { mutableStateOf(false) }
    var remaining by remember { mutableStateOf(manager.remainingMillis()) }
    LaunchedEffect(active) {
        while (true) {
            remaining = manager.remainingMillis()
            delay(1000L)
        }
    }

    Box(
        modifier = Modifier
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { showConfirm = true }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_capture_camera),
            contentDescription = stringResource(R.string.screen_capture_badge_desc),
            tint               = WarningAmber,
            modifier           = Modifier
                .align(Alignment.Center)
                .size(22.dp)
        )
        remaining?.let { ms ->
            Text(
                text       = formatRemaining(ms),
                color      = WarningAmber,
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.screen_capture_disable_title)) },
            text  = { Text(stringResource(R.string.screen_capture_disable_body)) },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        manager.revoke()
                        showConfirm = false
                    },
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text(stringResource(R.string.screen_capture_disable_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

private fun formatRemaining(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
