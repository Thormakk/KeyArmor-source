package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.Tag

@Composable
fun TagDeleteButton(
    tag:      Tag,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!tag.canDelete) return

    var showInUseDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick  = { if (tag.isInUse) showInUseDialog = true else onDelete() },
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            imageVector        = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.vault_delete_tag),
            tint               = dangerColor().copy(alpha = 0.7f),
            modifier           = Modifier.size(18.dp)
        )
    }

    if (showInUseDialog) {
        AlertDialog(
            onDismissRequest = { showInUseDialog = false },
            title            = { Text(stringResource(R.string.vault_delete_tag)) },
            text             = { Text(stringResource(R.string.tag_in_use_warning), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton    = {
                TextButton(onClick = { showInUseDialog = false }) {
                    Text(stringResource(R.string.action_understood))
                }
            }
        )
    }
}
