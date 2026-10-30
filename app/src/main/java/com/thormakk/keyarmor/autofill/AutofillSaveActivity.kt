package com.thormakk.keyarmor.autofill

import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.data.repository.VaultRepository
import com.thormakk.keyarmor.security.AutoLockManager
import com.thormakk.keyarmor.ui.theme.KeyArmorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.thormakk.keyarmor.presentation.components.justified

@AndroidEntryPoint
class AutofillSaveActivity : ComponentActivity() {

    @Inject lateinit var vaultRepository: VaultRepository
    @Inject lateinit var authRepository:  AuthRepository
    @Inject lateinit var autoLockManager: AutoLockManager

    private var themeId    by mutableStateOf("dark")
    private var accentArgb by mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pending = PendingSaveStore.take(intent?.getStringExtra(EXTRA_TOKEN))
        if (pending == null) {
            finish()
            return
        }

        lifecycleScope.launch {
            themeId    = authRepository.getThemeId()
            accentArgb = authRepository.getAccentColor()
        }

        setContent {
            KeyArmorTheme(themeId = themeId, accentArgb = accentArgb) {
                var pidiendoNombre by remember { mutableStateOf(pending.target == null) }

                if (!pidiendoNombre) {
                    val destino = pending.target!!
                    AlertDialog(
                        onDismissRequest = { finish() },
                        title = { Text(stringResource(R.string.autofill_save_conflict_title, pending.baseTitle)) },
                        text  = { Text(stringResource(R.string.autofill_save_conflict_body, destino.title), style = MaterialTheme.typography.bodyMedium.justified()) },
                        confirmButton = {
                            ChipPositivo(
                                texto   = stringResource(R.string.autofill_save_add_separately),
                                onClick = { pidiendoNombre = true },
                            )
                        },
                        dismissButton = {
                            TextButton(onClick = { actualizar(pending) }) {
                                Text(stringResource(R.string.autofill_save_update))
                            }
                        },
                    )
                } else {
                    PedirNombre(pending)
                }
            }
        }
    }

    @Composable
    private fun PedirNombre(pending: PendingSave) {
        var nombre by remember { mutableStateOf(pending.suggestedTitle) }
        val repetido = SaveDecision.isTitleTaken(nombre, pending.takenTitles)
        val valido   = nombre.isNotBlank() && !repetido
        val apoyo: (@Composable () -> Unit)? = if (repetido) {
            @Composable {
                Text(
                    stringResource(R.string.autofill_save_name_taken),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else null

        AlertDialog(
        modifier = Modifier.cuadroConTeclado(),
        properties = propiedadesConTeclado(),
            onDismissRequest = { finish() },
            title = { Text(stringResource(R.string.autofill_save_name_title)) },
            text  = {
                Column {
                    Text(stringResource(R.string.autofill_save_name_body, pending.baseTitle), style = MaterialTheme.typography.bodyMedium.justified())
                    OutlinedTextField(
                        value         = nombre,
                        onValueChange = { nombre = it },
                        label         = { Text(stringResource(R.string.autofill_save_name_label)) },
                        singleLine    = true,
                        isError       = repetido,
                        supportingText = apoyo,
                    )
                }
            },
            confirmButton = {
                ChipPositivo(
                    texto   = stringResource(R.string.autofill_save_confirm),
                    enabled = valido,
                    onClick = { guardarNueva(pending, nombre.trim()) },
                )
            },
            dismissButton = {
                TextButton(onClick = { finish() }) {
                    Text(stringResource(R.string.autofill_save_cancel))
                }
            },
        )
    }

    @Composable
    private fun ChipPositivo(texto: String, onClick: () -> Unit, enabled: Boolean = true) {
        val acento = MaterialTheme.colorScheme.primary
        val color  = if (enabled) acento else acento.copy(alpha = 0.38f)
        Button(
            onClick  = onClick,
            enabled  = enabled,
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = Color.Transparent,
                contentColor           = acento,
                disabledContainerColor = Color.Transparent,
                disabledContentColor   = color,
            ),
            border   = BorderStroke(1.5.dp, color),
        ) {
            Text(texto, fontWeight = FontWeight.SemiBold)
        }
    }

    private fun actualizar(p: PendingSave) {
        val destino = p.target ?: return finish()
        lifecycleScope.launch {
            autoLockManager.enforceIfExpired()
            runCatching {
                val actual = vaultRepository.getEntry(destino.id)
                if (actual != null) {
                    val guardada = vaultRepository.saveEntry(
                        actual.copy(
                            password          = p.password,
                            passwordChangedAt = System.currentTimeMillis(),
                            fieldLabel        = actual.fieldLabel.ifBlank { p.fieldLabel },
                        )
                    )
                    if (guardada) aviso(R.string.autofill_save_toast_updated)
                }
            }
            finish()
        }
    }

    private fun guardarNueva(p: PendingSave, titulo: String) {
        lifecycleScope.launch {
            autoLockManager.enforceIfExpired()
            runCatching {
                vaultRepository.saveEntry(
                    AutofillEntryFactory.create(
                        title      = titulo,
                        url        = p.url,
                        username   = p.username,
                        email      = p.email,
                        password   = p.password,
                        fieldLabel = p.fieldLabel,
                    )
                )
                aviso(R.string.autofill_save_toast_saved)
            }
            finish()
        }
    }

    private fun aviso(res: Int) =
        Toast.makeText(this, getString(res), Toast.LENGTH_SHORT).show()

    companion object {
        private const val EXTRA_TOKEN = "pending_save_token"

        fun createIntent(context: Context, token: String): Intent =
            Intent(context, AutofillSaveActivity::class.java)
                .putExtra(EXTRA_TOKEN, token)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
