package com.thormakk.keyarmor.presentation.screens.timemachine

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.CsvExportAuthGate

@Composable
internal fun ReplaceAuthGate(
    onAuthorized:         () -> Unit,
    onDismiss:            () -> Unit,
    verifyMasterPassword: suspend (String) -> Boolean,
) {
    CsvExportAuthGate(
        onAuthorized         = onAuthorized,
        onDismiss            = onDismiss,
        verifyMasterPassword = verifyMasterPassword,
        promptTitle          = stringResource(R.string.delete_all_verify_title),
        promptSubtitle       = stringResource(R.string.restore_replace_verify_subtitle),
        confirmLabel         = stringResource(R.string.restore_mode_replace),
        fallbackPrompt       = stringResource(R.string.restore_replace_verify_subtitle),
    )
}

@Composable
internal fun DeleteSnapshotsAuthGate(
    onAuthorized:         () -> Unit,
    onDismiss:            () -> Unit,
    verifyMasterPassword: suspend (String) -> Boolean,
) {
    CsvExportAuthGate(
        onAuthorized         = onAuthorized,
        onDismiss            = onDismiss,
        verifyMasterPassword = verifyMasterPassword,
        promptTitle          = stringResource(R.string.delete_all_verify_title),
        promptSubtitle       = stringResource(R.string.time_machine_delete_verify_subtitle),
        confirmLabel         = stringResource(R.string.action_delete),
        fallbackPrompt       = stringResource(R.string.time_machine_delete_verify_subtitle),
    )
}
