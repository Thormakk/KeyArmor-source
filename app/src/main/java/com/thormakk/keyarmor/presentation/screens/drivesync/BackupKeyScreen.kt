package com.thormakk.keyarmor.presentation.screens.drivesync

import androidx.core.content.edit
import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.compose.foundation.BorderStroke
import com.thormakk.keyarmor.presentation.components.justified
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.crypto.PalabrasDeLaClave
import com.thormakk.keyarmor.data.backup.ClaveDeRespaldo
import com.thormakk.keyarmor.data.backup.ClaveRetirada
import com.thormakk.keyarmor.data.backup.ListaDePalabras
import com.thormakk.keyarmor.data.drive.DriveAutoBackupManager
import com.thormakk.keyarmor.data.drive.DriveBackupRepository
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.repository.AuthRepository
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.TextoQueSeAjusta
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.rememberMandatoryBiometricGate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BackupKeyViewModel @Inject constructor(
    private val claveDeRespaldo: ClaveDeRespaldo,
    private val listaDePalabras: ListaDePalabras,
    private val drivePrefs:      DrivePrefs,
    private val authRepository:  AuthRepository,
    private val planPrefs:       PlanPrefs,
    private val driveRepo:       DriveBackupRepository,
    private val autoBackup:      DriveAutoBackupManager
) : ViewModel() {

    enum class Rotacion { OK, OK_SIN_SUBIR, ERROR }

    fun puedeRotar(): Boolean =
        planPrefs.effectivePlan() != UserPlan.FREE && claveDeRespaldo.confirmada()

    fun prepararNueva(): List<String>? {
        val clave = claveDeRespaldo.prepararRotacion() ?: return null
        return try { PalabrasDeLaClave.aPalabras(clave, listaDePalabras.palabras) } finally { clave.fill(0) }
    }

    fun descartarNueva() = claveDeRespaldo.descartarRotacion()

    suspend fun cambiarClave(guardarAnterior: Boolean): Rotacion {
        val nueva = claveDeRespaldo.claveNuevaPendiente() ?: return Rotacion.ERROR
        val vieja = claveDeRespaldo.claveParaRestaurar()
        try {
            val email = drivePrefs.getConnectedEmail()
            if (email != null && vieja != null) {
                try {
                    driveRepo.rewrapSnapshots(android.accounts.Account(email, "com.google"), vieja, nueva)
                } catch (e: Exception) {
                    return Rotacion.ERROR
                }
            }
            if (!claveDeRespaldo.confirmarRotacion(guardarAnterior)) return Rotacion.ERROR
            if (email == null) { claveDeRespaldo.resolverRespaldoPendiente(); return Rotacion.OK }
            return if (autoBackup.subirAhora()) Rotacion.OK else Rotacion.OK_SIN_SUBIR
        } finally {
            nueva.fill(0); vieja?.fill(0)
        }
    }

    fun respaldoPendiente(): Boolean = claveDeRespaldo.respaldoConClaveNuevaPendiente()

    suspend fun subirRespaldoAhora(): Boolean = autoBackup.subirAhora()

    fun clavesParaElKit(ctx: android.content.Context): List<KitPreviousKey> {
        val fmt = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM)
        return historial().mapNotNull { k ->
            val w = palabrasRetirada(k.id) ?: return@mapNotNull null
            val hasta = fmt.format(java.util.Date(k.hasta))
            val label = if (k.desde > 0L) ctx.getString(R.string.bk_hist_range, fmt.format(java.util.Date(k.desde)), hasta)
                        else ctx.getString(R.string.bk_hist_until, hasta)
            KitPreviousKey(label, w)
        }
    }

    fun ultimoFallo(): String? = autoBackup.ultimoFallo

    fun prepararDesdeRetirada(id: String): List<String>? {
        val c = claveDeRespaldo.prepararRotacionDesdeRetirada(id) ?: return null
        return try { PalabrasDeLaClave.aPalabras(c, listaDePalabras.palabras) } finally { c.fill(0) }
    }

    fun historial(): List<ClaveRetirada> = claveDeRespaldo.historial()

    fun palabrasRetirada(id: String): List<String>? {
        val c = claveDeRespaldo.claveRetirada(id) ?: return null
        return try { PalabrasDeLaClave.aPalabras(c, listaDePalabras.palabras) } finally { c.fill(0) }
    }

    fun borrarRetirada(id: String) = claveDeRespaldo.borrarRetirada(id)

    fun activada(): Boolean = claveDeRespaldo.confirmada()

    fun cuenta(): String? = drivePrefs.getConnectedEmail()

    fun palabras(): List<String>? {
        if (!claveDeRespaldo.confirmada()) return null
        val clave = claveDeRespaldo.obtenerOCrear() ?: return null
        return try {
            PalabrasDeLaClave.aPalabras(clave, listaDePalabras.palabras)
        } finally {
            clave.fill(0)
        }
    }

    suspend fun verifyMasterPassword(password: String): Boolean =
        authRepository.verifyPassword(password)
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BackupKeyScreen(
    onBack:       () -> Unit,
    onOpenDrive:  () -> Unit,
    viewModel:    BackupKeyViewModel = hiltViewModel()
) {
    val snackbar = remember { SnackbarHostState() }
    val gate = rememberMandatoryBiometricGate(verifyMasterPassword = viewModel::verifyMasterPassword)
    var desbloqueada by remember { mutableStateOf(false) }
    var palabras by remember { mutableStateOf<List<String>?>(null) }
    var version by remember { mutableIntStateOf(0) }
    var restablecidas by remember { mutableStateOf<List<String>?>(null) }
    var verHistorial by remember { mutableStateOf(false) }
    androidx.activity.compose.BackHandler(enabled = verHistorial) { verHistorial = false }
    val ctxKit = LocalContext.current
    val activada = remember(version) { viewModel.activada() }

    fun desbloquear() = gate.launch {
        palabras = viewModel.palabras()
        desbloqueada = true
    }
    LaunchedEffect(Unit) { desbloquear() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            Column(Modifier.fillMaxWidth()) {
                Spacer(
                    Modifier.fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoQueSeAjusta(
                            stringResource(if (verHistorial) R.string.bk_hist_title else R.string.kit_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { if (verHistorial) verHistorial = false else onBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 7.dp, top = 7.dp, end = 7.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!desbloqueada) {
                Card2 {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.bk_locked), style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(colors = appButtonColors(), onClick = { desbloquear() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.bk_unlock))
                    }
                }
            } else if (verHistorial) {
                HistorialPantalla(
                    viewModel, remember(version) { viewModel.historial() }, snackbar,
                    onRestablecer = { restablecidas = it; verHistorial = false }
                ) {
                    version++
                    if (viewModel.historial().isEmpty()) verHistorial = false
                }
            } else {
                InfoCard()

                if (activada && palabras != null) {
                    val w = palabras!!
                    Card2 {
                        Titulo(stringResource(R.string.bk_words_title))
                        PalabrasGrid(w)
                        EmergencyKitActions(
                            words = w, account = viewModel.cuenta(), snackbar = snackbar,
                            previous = remember(version) { viewModel.clavesParaElKit(ctxKit) }
                        )
                    }
                } else if (!activada) {
                    Card2 {
                        Cuerpo(stringResource(R.string.bk_not_active))
                        Button(colors = appButtonColors(), onClick = onOpenDrive, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.bk_go_drive))
                        }
                    }
                }

                if (activada) {
                    if (remember(version) { viewModel.respaldoPendiente() }) {
                        RespaldoPendienteCard(viewModel, snackbar) { version++ }
                    }
                    if (viewModel.puedeRotar()) {
                        CambiarClaveCard(
                            viewModel, viewModel.cuenta(), snackbar,
                            inicial = restablecidas,
                            onInicialTomada = { restablecidas = null }
                        ) {
                            palabras = viewModel.palabras()
                            version++
                        }
                    }
                    val retiradas = remember(version) { viewModel.historial() }
                    if (retiradas.isNotEmpty()) {
                        Card2 {
                            Titulo(stringResource(R.string.bk_hist_title))
                            Cuerpo(stringResource(R.string.bk_hist_body))
                            Button(colors = appButtonColors(), onClick = { verHistorial = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.bk_hist_show))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("backup_key_ui", android.content.Context.MODE_PRIVATE) }
    var abierta by remember { mutableStateOf(!prefs.getBoolean("info_vista", false)) }
    LaunchedEffect(Unit) { prefs.edit { putBoolean("info_vista", true) } }
    Card2 {
      Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { abierta = !abierta }
        ) {
            Box(Modifier.weight(1f)) { Titulo(stringResource(R.string.bk_info_title)) }
            Icon(
                if (abierta) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
        AnimatedVisibility(
            visible = abierta,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Cuerpo(stringResource(R.string.bk_info_what))
                Cuerpo(stringResource(R.string.bk_info_where))
                Cuerpo(stringResource(R.string.bk_info_tips))
            }
        }
      }
    }
}

@Composable
private fun Card2(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun Titulo(t: String) = Text(
    t, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary
)

@Composable
private fun Cuerpo(t: String) = Text(
    t, style = MaterialTheme.typography.bodyMedium.justified(), color = MaterialTheme.colorScheme.onSurface
)


@Composable
private fun PalabrasGrid(w: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        w.chunked(3).forEachIndexed { fila, grupo ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                grupo.forEachIndexed { i, palabra ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "${fila * 3 + i + 1}. $palabra",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RespaldoPendienteCard(
    viewModel: BackupKeyViewModel,
    snackbar:  SnackbarHostState,
    onDone:    () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val textoRotHecha = stringResource(R.string.bk_rot_done)
    var trabajando by remember { mutableStateOf(false) }
    Card2 {
        Titulo(stringResource(R.string.bk_rot_pending_title))
        Cuerpo(stringResource(R.string.bk_rot_pending_body))
        Button(
            colors = appButtonColors(),
            onClick = {
                trabajando = true
                scope.launch {
                    val ok = viewModel.subirRespaldoAhora()
                    trabajando = false
                    if (ok) { onDone(); snackbar.showSnackbar(textoRotHecha) }
                    else snackbar.showSnackbar(mensajeSinSubir(ctx, viewModel.ultimoFallo()))
                }
            },
            enabled = !trabajando,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.bk_rot_upload)) }
    }
}

@Composable
private fun CambiarClaveCard(
    viewModel: BackupKeyViewModel,
    cuenta:    String?,
    snackbar:  SnackbarHostState,
    inicial:   List<String>? = null,
    onInicialTomada: () -> Unit = {},
    onChanged: () -> Unit
) {
    var nuevas by remember { mutableStateOf<List<String>?>(null) }
    var guardada by remember { mutableStateOf(false) }
    var guardarAnterior by remember { mutableStateOf(true) }
    var trabajando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val textoRotHecha = stringResource(R.string.bk_rot_done)
    val textoRotError = stringResource(R.string.bk_rot_error)
    LaunchedEffect(inicial) {
        if (inicial != null) { nuevas = inicial; guardada = false; onInicialTomada() }
    }
    Card2 {
        val w = nuevas
        if (w == null) {
            Titulo(stringResource(R.string.bk_rot_title))
            Cuerpo(stringResource(R.string.bk_rot_body))
            Button(
                colors = appButtonColors(),
                onClick = { nuevas = viewModel.prepararNueva(); guardada = false },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.bk_rot_button)) }
        } else {
            Titulo(stringResource(R.string.bk_rot_new_title))
            PalabrasGrid(w)
            EmergencyKitActions(words = w, account = cuenta, snackbar = snackbar)
            Row(
                Modifier.fillMaxWidth().clickable(enabled = !trabajando) { guardada = !guardada },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = guardada, onCheckedChange = { guardada = it }, enabled = !trabajando)
                Text(stringResource(R.string.key_setup_checkbox), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
            Row(
                Modifier.fillMaxWidth().clickable(enabled = !trabajando) { guardarAnterior = !guardarAnterior },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = guardarAnterior, onCheckedChange = { guardarAnterior = it }, enabled = !trabajando)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.bk_rot_keep_old), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        stringResource(R.string.bk_rot_keep_old_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trabajando) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    Text(stringResource(R.string.bk_rot_working), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(
                colors = appButtonColors(),
                onClick = {
                    trabajando = true
                    scope.launch {
                        val r = viewModel.cambiarClave(guardarAnterior)
                        trabajando = false
                        when (r) {
                            BackupKeyViewModel.Rotacion.ERROR ->
                                snackbar.showSnackbar(textoRotError)
                            else -> {
                                nuevas = null
                                onChanged()
                                snackbar.showSnackbar(
                                    if (r == BackupKeyViewModel.Rotacion.OK) textoRotHecha
                                    else mensajeSinSubir(ctx, viewModel.ultimoFallo())
                                )
                            }
                        }
                    }
                },
                enabled = guardada && !trabajando,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.bk_rot_confirm)) }
            TextButton(
                onClick = { viewModel.descartarNueva(); nuevas = null },
                enabled = !trabajando,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.action_cancel)) }
        }
    }
}

@Composable
private fun HistorialPantalla(
    viewModel: BackupKeyViewModel,
    retiradas: List<ClaveRetirada>,
    snackbar:  SnackbarHostState,
    onRestablecer: (List<String>) -> Unit,
    onChanged: () -> Unit
) {
    val context   = LocalContext.current
    val textoCopiada = stringResource(R.string.key_kit_copied)
    val clipClear = com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager.current
    val scope     = rememberCoroutineScope()
    var abierta by remember { mutableStateOf<String?>(null) }
    var aBorrar by remember { mutableStateOf<ClaveRetirada?>(null) }
    val fmt = remember { java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM) }

    Cuerpo(stringResource(R.string.bk_hist_body))
    retiradas.forEach { k ->
        val hasta = fmt.format(java.util.Date(k.hasta))
        val rango = if (k.desde > 0L) stringResource(R.string.bk_hist_range, fmt.format(java.util.Date(k.desde)), hasta)
                    else stringResource(R.string.bk_hist_until, hasta)
        val abierto = abierta == k.id
        var palabras by remember(k.id) { mutableStateOf<List<String>?>(null) }
        LaunchedEffect(abierto) {
            if (abierto) palabras = viewModel.palabrasRetirada(k.id)
            else { kotlinx.coroutines.delay(300); palabras = null }
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) { abierta = if (abierto) null else k.id }
                ) {
                    Text(
                        rango, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (abierto) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
                AnimatedVisibility(
                    visible = abierto && palabras != null,
                    enter   = expandVertically() + fadeIn(),
                    exit    = shrinkVertically() + fadeOut()
                ) {
                  val pal = palabras.orEmpty()
                  Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PalabrasGrid(pal)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            border = acentoBorde(),
                            colors = acentoOutlinedColors(),
                            onClick = {
                                com.thormakk.keyarmor.presentation.util.ClipboardUtil.copySensitive(context, pal.joinToString(" "))
                                clipClear.scheduleClear()
                                scope.launch { snackbar.showSnackbar(textoCopiada) }
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.bk_hist_copy), maxLines = 1) }
                        OutlinedButton(
                            border = acentoBorde(),
                            colors = acentoOutlinedColors(),
                            onClick = { viewModel.prepararDesdeRetirada(k.id)?.let(onRestablecer) },
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.bk_hist_restore), maxLines = 1) }
                        OutlinedButton(
                            border = acentoBorde(),
                            colors = acentoOutlinedColors(),
                            onClick = { aBorrar = k },
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.bk_hist_delete), maxLines = 1) }
                    }
                }
                  }
            }
        }
    }
    aBorrar?.let { k ->
        AlertDialog(
            onDismissRequest = { aBorrar = null },
            title = { Text(stringResource(R.string.bk_hist_delete_title)) },
            text  = { Text(stringResource(R.string.bk_hist_delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarRetirada(k.id)
                    if (abierta == k.id) abierta = null
                    aBorrar = null
                    onChanged()
                }) { Text(stringResource(R.string.bk_hist_delete)) }
            },
            dismissButton = { TextButton(onClick = { aBorrar = null }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }
}


private fun mensajeSinSubir(ctx: android.content.Context, fallo: String?): String {
    val base = ctx.getString(R.string.bk_rot_done_pending)
    return if (fallo.isNullOrBlank()) base
    else base + "\n" + ctx.getString(R.string.drive_sync_error_backup_prefix, fallo)
}


@Composable
internal fun acentoBorde() = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)

@Composable
internal fun acentoOutlinedColors() = ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colorScheme.primary
)
