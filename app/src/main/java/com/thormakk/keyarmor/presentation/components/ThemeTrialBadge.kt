package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.util.LocalThemeTrialManager

@Composable
fun ThemeTrialBadge() {
    val manager = LocalThemeTrialManager.current
    val trialThemeId by manager.trialThemeId.collectAsStateWithLifecycle()
    if (trialThemeId == null) return

    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { showConfirm = true }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Default.Palette,
            contentDescription = stringResource(R.string.theme_trial_badge_desc),
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier
                .align(Alignment.Center)
                .size(22.dp)
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor   = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.theme_trial_end_title)) },
            text  = { Text(stringResource(R.string.theme_trial_end_body)) },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        manager.endTrial()
                        showConfirm = false
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text(stringResource(R.string.theme_trial_end_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
