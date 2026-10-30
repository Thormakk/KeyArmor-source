package com.thormakk.keyarmor.presentation.screens.auth

import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.repository.AccountResetRepository
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.warningColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import com.thormakk.keyarmor.presentation.components.justified
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ForgotMasterPasswordViewModel @Inject constructor(
    private val accountResetRepository: AccountResetRepository
) : ViewModel() {

    var inProgress by mutableStateOf(false)
        private set

    fun wipeAndRestart() {
        if (inProgress) return
        inProgress = true
        viewModelScope.launch {
            accountResetRepository.wipeLocalEverything(includeDrive = false, includeDownloadsCopies = false)
            accountResetRepository.restartApp()
        }
    }
}

@Composable
fun ForgotMasterPasswordLink(
    linkColor: Color,
    modifier: Modifier = Modifier,
    viewModel: ForgotMasterPasswordViewModel = hiltViewModel()
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var typed by rememberSaveable { mutableStateOf("") }

    TextButton(onClick = { typed = ""; step = 1 }, modifier = modifier) {
        Text(
            text  = stringResource(R.string.forgot_master_link),
            style = MaterialTheme.typography.bodySmall,
            color = linkColor
        )
    }

    if (step == 1) {
        AlertDialog(
            onDismissRequest = { step = 0 },
            title = { Text(stringResource(R.string.forgot_master_title)) },
            text  = {
                Column {
                    Text(stringResource(R.string.forgot_master_body_nopossible), style = MaterialTheme.typography.bodyMedium.justified())
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.forgot_master_body_backup), style = MaterialTheme.typography.bodyMedium.justified())
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text       = stringResource(R.string.forgot_master_body_wipe),
                        style      = MaterialTheme.typography.bodyMedium.justified(),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                AmberPillButton(
                    text    = stringResource(R.string.forgot_master_continue),
                    onClick = { step = 2 }
                )
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }) { Text(stringResource(R.string.forgot_master_cancel)) }
            }
        )
    }

    if (step == 2) {
        val phrase = stringResource(R.string.delete_all_confirm_phrase)
        val matches = typed.trim().equals(phrase, ignoreCase = true)
        AlertDialog(
        modifier = Modifier.cuadroConTeclado(),
        properties = propiedadesConTeclado(),
            onDismissRequest = { if (!viewModel.inProgress) step = 0 },
            title = { Text(stringResource(R.string.forgot_master_confirm_title)) },
            text  = {
                Column {
                    Text(stringResource(R.string.forgot_master_confirm_body), style = MaterialTheme.typography.bodyMedium.justified())
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = typed,
                        onValueChange = { typed = it },
                        label         = { Text(stringResource(R.string.delete_all_confirm_label, phrase)) },
                        singleLine    = true,
                        enabled       = !viewModel.inProgress,
                        modifier      = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                DangerPillButton(
                    text       = stringResource(R.string.forgot_master_confirm_button),
                    onClick    = { viewModel.wipeAndRestart() },
                    enabled    = matches && !viewModel.inProgress,
                    inProgress = viewModel.inProgress
                )
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }, enabled = !viewModel.inProgress) {
                    Text(stringResource(R.string.forgot_master_cancel))
                }
            }
        )
    }
}

@Composable
private fun AmberPillButton(
    text:       String,
    onClick:    () -> Unit,
    modifier:   Modifier = Modifier,
    enabled:    Boolean = true,
    inProgress: Boolean = false
) {
    val amber  = warningColor()
    val tint   = if (enabled || inProgress) amber else amber.copy(alpha = 0.38f)
    TextButton(
        onClick        = onClick,
        enabled        = enabled,
        modifier       = modifier.heightIn(min = 36.dp),
        shape          = RoundedCornerShape(50),
        border         = BorderStroke(0.5.dp, tint),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
    ) {
        if (inProgress) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = amber)
        } else {
            Text(text = text, color = tint, maxLines = 1)
        }
    }
}

@Composable
private fun DangerPillButton(
    text:       String,
    onClick:    () -> Unit,
    modifier:   Modifier = Modifier,
    enabled:    Boolean = true,
    inProgress: Boolean = false
) {
    val fill     = MaterialTheme.colorScheme.error
    val loose    = dangerColor()
    val apagado  = loose.copy(alpha = 0.38f)
    val lleno    = enabled || inProgress
    OutlinedButton(
        onClick        = onClick,
        enabled        = enabled,
        modifier       = modifier,
        shape          = RoundedCornerShape(50),
        border         = BorderStroke(0.5.dp, if (lleno) fill else apagado),
        colors         = ButtonDefaults.outlinedButtonColors(
            containerColor         = if (lleno) fill else Color.Transparent,
            contentColor           = if (lleno) Color.White else loose,
            disabledContainerColor = if (inProgress) fill else Color.Transparent,
            disabledContentColor   = if (inProgress) Color.White else apagado
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (inProgress) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Text(text = text, maxLines = 1, fontWeight = FontWeight.Medium)
        }
    }
}
