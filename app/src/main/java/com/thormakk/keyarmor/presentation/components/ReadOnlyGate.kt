package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R

@Composable
fun LockBadge(
    modifier:   Modifier = Modifier,
    badgeColor: Color = MaterialTheme.colorScheme.onSurface,
    lockColor:  Color = MaterialTheme.colorScheme.surface,
    size:       Dp    = 15.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .background(badgeColor)
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = lockColor, modifier = Modifier.size(size * 2 / 3))
    }
}

@Composable
fun FabLockBadge(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset(x = 4.dp, y = (-4).dp)
            .size(19.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface)
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(13.dp))
    }
}

@Composable
fun FabAddIcon(locked: Boolean, contentDescription: String?) {
    Icon(
        imageVector        = Icons.Default.Add,
        contentDescription = contentDescription,
        modifier           = Modifier.alpha(if (locked) 0.55f else 1f)
    )
}

@Composable
fun ReadOnlyGateDialog(
    premiumSection: Boolean,
    onDismiss:      () -> Unit,
    onPlans:        () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Text(
                stringResource(R.string.premium_gate_title),
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                stringResource(if (premiumSection) R.string.readonly_gate_premium_section else R.string.vault_read_only_banner),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = { onDismiss(); onPlans() },
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = MaterialTheme.colorScheme.primary
                ),
                border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.premium_gate_cta), fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_understood)) }
        }
    )
}
