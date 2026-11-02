@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_VALUE")
package com.thormakk.keyarmor.presentation.screens.settings
import androidx.compose.foundation.gestures.scrollBy
import com.thormakk.keyarmor.presentation.util.PosicionDeTitulo
import com.thormakk.keyarmor.presentation.util.contenedorDe
import com.thormakk.keyarmor.presentation.util.registrarPosicion
import com.thormakk.keyarmor.presentation.util.EntradaDeApertura
import com.thormakk.keyarmor.presentation.util.SalidaDeApertura
import com.thormakk.keyarmor.presentation.util.rememberSeguidorDeApertura
import androidx.compose.ui.text.input.KeyboardType

import com.thormakk.keyarmor.presentation.components.cuadroConTeclado
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import com.thormakk.keyarmor.presentation.components.CsvExportAuthGate
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.components.dangerColor
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VisibilityOff
import com.thormakk.keyarmor.data.prefs.ScreenCapturePrefs
import com.thormakk.keyarmor.presentation.components.propiedadesConTeclado
import com.thormakk.keyarmor.presentation.screens.privacy.ScreenCaptureViewModel
import com.thormakk.keyarmor.presentation.screens.language.supportedLanguages
import com.thormakk.keyarmor.presentation.util.rememberMandatoryBiometricGate
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import com.thormakk.keyarmor.presentation.util.GoogleConnectResult
import com.thormakk.keyarmor.presentation.util.requestGoogleAccountConnect
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.BuildConfig
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.data.prefs.AutoLockPrefs
import com.thormakk.keyarmor.data.prefs.BiometricPrefs
import com.thormakk.keyarmor.data.prefs.BreachCheckPrefs
import com.thormakk.keyarmor.data.prefs.ClipboardPrefs
import com.thormakk.keyarmor.data.prefs.ExpiryNotificationPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.screens.hub.SecurityLevel
import com.thormakk.keyarmor.presentation.screens.hub.SecurityUiState
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.openUrl
import com.thormakk.keyarmor.ui.theme.SuccessGreen
import com.thormakk.keyarmor.ui.theme.SuccessGreenBright
import com.thormakk.keyarmor.ui.theme.WarningAmber
import com.thormakk.keyarmor.ui.theme.WarningAmberDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

private enum class SettingsMode { SIMPLE, COMPLETO }

private val SettingsGroupCornerRadius = 14.dp
private val SettingsGroupGap = 0.5.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeClick:      () -> Unit = {},
    onLanguageClick:   () -> Unit = {},
    onDriveSyncClick:  () -> Unit = {},
    onBackupKeyClick:  () -> Unit = {},
    onDigitalInheritanceClick: () -> Unit = {},
    onFamilyPlanClick:         () -> Unit = {},
    onPanicPinClick:   () -> Unit = {},
    onCalculatorDisguiseClick: () -> Unit = {},
    onTravelModeClick: () -> Unit = {},
    onImportExportClick: () -> Unit = {},
    onChangeMasterPasswordClick: () -> Unit = {},
    onHiddenEntriesClick: () -> Unit = {},
    onAutofillClick: () -> Unit = {},
    onDonateClick: () -> Unit = {},
    onReceiveEntryClick: () -> Unit = {},
    onPlansClick:      () -> Unit = {},
    onMyPlanClick:     () -> Unit = {},
    onAuditClick:      () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onLegalDocClick:   (String) -> Unit = {},
    onNavigateToWeakPasswords:   () -> Unit = {},
    onNavigateToMediumPasswords: () -> Unit = {},
    highlightDrive:        Boolean = false,
    onVaultManagementClick:  () -> Unit = {},
    onTimeMachineClick:      () -> Unit = {},
    highlightTimeMachine:    Boolean = false,
    onVoiceDictationClick:   () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val secState      by viewModel.securityState.collectAsStateWithLifecycle()
    val unreadNotifs  by viewModel.unreadNotifCount.collectAsStateWithLifecycle()
    val coercionActive by viewModel.coercionActive.collectAsStateWithLifecycle()
    val captureViewModel: ScreenCaptureViewModel = hiltViewModel()
    val capturePermitActive by captureViewModel.permitActive.collectAsStateWithLifecycle()
    val context  = LocalContext.current
    val activity = context as FragmentActivity
    val autoLockManager = LocalAutoLockManager.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Error -> snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshDriveState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var premiumGatedFeature by remember { mutableStateOf<String?>(null) }
    premiumGatedFeature?.let { featureName ->
        PremiumGateDialog(
            featureName = featureName,
            onDismiss   = { premiumGatedFeature = null },
            onUpgrade   = { premiumGatedFeature = null; onPlansClick() }
        )
    }

    val scrollState = rememberScrollState()
    val drivePulse = remember { Animatable(0f) }
    val driveRowRequester = remember { BringIntoViewRequester() }
    val timeMachineRowRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(Unit) {
        if (highlightDrive) {
            snapshotFlow { scrollState.maxValue }
                .first { it in 1 until Int.MAX_VALUE }
            delay(350)
            driveRowRequester.bringIntoView()
            delay(200)
            repeat(2) {
                drivePulse.animateTo(1f, animationSpec = tween(300))
                drivePulse.animateTo(0f, animationSpec = tween(280))
            }
        }
    }

    val timeMachinePulse = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (highlightTimeMachine) {
            snapshotFlow { scrollState.maxValue }
                .first { it in 1 until Int.MAX_VALUE }
            delay(350)
            timeMachineRowRequester.bringIntoView()
            delay(200)
            repeat(2) {
                timeMachinePulse.animateTo(1f, animationSpec = tween(300))
                timeMachinePulse.animateTo(0f, animationSpec = tween(280))
            }
        }
    }

    val bioCallback = remember {
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val state = viewModel.uiState.value
                when {
                    state.biometricPending -> {
                        if (result.authenticationType !=
                            BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) {
                            viewModel.reportBiometricEnableFailure("T1")
                            viewModel.cancelEnableBiometric(); return
                        }
                        result.cryptoObject?.cipher?.let { viewModel.onBiometricSetupSuccess(it) }
                            ?: run {
                                viewModel.reportBiometricEnableFailure("S3")
                                viewModel.cancelEnableBiometric()
                            }
                    }
                    state.gateDisablePending -> viewModel.onGateDisableConfirmed()
                }
            }
            override fun onAuthenticationFailed() {   }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                val state = viewModel.uiState.value
                when {
                    state.biometricPending   -> {
                        val porElUsuario = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                            errorCode == BiometricPrompt.ERROR_CANCELED
                        if (!porElUsuario) viewModel.reportBiometricEnableFailure("A$errorCode: $errString")
                        viewModel.cancelEnableBiometric()
                    }
                    state.gateDisablePending -> viewModel.cancelGateDisable()
                }
            }
        }
    }

    val biometricPrompt = remember(activity) {
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), bioCallback)
    }

    val cancelLabel          = stringResource(R.string.action_cancel)
    val bioTitle             = stringResource(R.string.auth_biometric_title)
    val bioSubtitle          = stringResource(R.string.auth_biometric_subtitle)
    val gateDisableTitle     = stringResource(R.string.auth_biometric_disable_title)
    val gateDisableSubtitle  = stringResource(R.string.auth_biometric_disable_subtitle)

    val enablePromptInfo = remember(bioTitle, bioSubtitle, cancelLabel) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(bioTitle)
            .setSubtitle(bioSubtitle)
            .setNegativeButtonText(cancelLabel)
            .build()
    }

    val gateDisablePromptInfo = remember(gateDisableTitle, gateDisableSubtitle, cancelLabel) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(gateDisableTitle)
            .setSubtitle(gateDisableSubtitle)
            .setNegativeButtonText(cancelLabel)
            .build()
    }

    LaunchedEffect(uiState.biometricPending) {
        if (uiState.biometricPending) {
            val disponibilidad = androidx.biometric.BiometricManager.from(context)
                .canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
            if (disponibilidad != androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                when (disponibilidad) {
                    androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                        viewModel.reportBiometricNotUsable(notEnrolled = true)
                    androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
                    androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                        viewModel.reportBiometricNotUsable(notEnrolled = false)
                    else -> viewModel.reportBiometricEnableFailure("C$disponibilidad")
                }
                viewModel.cancelEnableBiometric()
                return@LaunchedEffect
            }
            val cipher = viewModel.getSetupCipher()
            if (cipher != null) {
                try {
                    biometricPrompt.authenticate(enablePromptInfo, BiometricPrompt.CryptoObject(cipher))
                } catch (e: Exception) {
                    android.util.Log.w("KeyArmorBio", "authenticate lanzó ${e.javaClass.simpleName}")
                    viewModel.reportBiometricEnableFailure("P1")
                    viewModel.cancelEnableBiometric()
                }
            } else {
                viewModel.cancelEnableBiometric()
            }
        }
    }

    LaunchedEffect(uiState.gateDisablePending) {
        if (uiState.gateDisablePending) {
            biometricPrompt.authenticate(gateDisablePromptInfo)
        }
    }

    val isPremium = uiState.userPlan != UserPlan.FREE
    val settingsSimple by viewModel.settingsSimple.collectAsStateWithLifecycle()
    val settingsMode = if (settingsSimple) SettingsMode.SIMPLE else SettingsMode.COMPLETO
    val showPremiumItems = isPremium || settingsMode == SettingsMode.COMPLETO
    val premiumEnter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200))
    val premiumExit  = shrinkVertically(animationSpec = tween(160)) + fadeOut(animationSpec = tween(160))

    var expandedSection by rememberSaveable {
        mutableStateOf<String?>(if (highlightDrive || highlightTimeMachine) "sync" else null)
    }
    var anclaCierre by remember { mutableStateOf<Int?>(null) }
    var compensarCierre by remember { mutableStateOf(false) }
    val ultimoVDibujado = remember { intArrayOf(0) }
    val caidaVista = remember { booleanArrayOf(false) }
    LaunchedEffect(Unit) {
        var previo = scrollState.value
        var restituido = false
        snapshotFlow { scrollState.value to anclaCierre }.collect { (v, a) ->
            if (a == null) { compensarCierre = false; restituido = false }
            else if (!compensarCierre && previo - v > 40) {
                compensarCierre = true
                scrollState.scrollTo(a)
                restituido = true
            }
            else if (compensarCierre && v >= a - 40) { compensarCierre = false; anclaCierre = null }
            else if (compensarCierre && restituido) {
                while (true) {
                    val actual = anclaCierre ?: break
                    val vivo = scrollState.value
                    if (actual - vivo <= 1) { anclaCierre = null; compensarCierre = false; break }
                    anclaCierre = actual - maxOf(1, (actual - vivo) / 6)
                    withFrameNanos { }
                }
            }
            previo = v
        }
    }
    fun desplazamientoDeCierre(): Float {
        val a = anclaCierre
        return if (a != null && caidaVista[0]) (scrollState.value - a).toFloat() else 0f
    }
    val scopeCierre = rememberCoroutineScope()
    val seguidor = rememberSeguidorDeApertura(fraccion = 0.01f) { scrollState.scrollBy(it) }
    seguidor.margenMinimo = with(LocalDensity.current) { 10.dp.toPx() }
    val posiciones = remember { mutableMapOf<String, PosicionDeTitulo>() }
    fun posicionDe(id: String) = posiciones.getOrPut(id) { PosicionDeTitulo().also { it.corregir = { desplazamientoDeCierre() } } }
    fun toggleSection(id: String) {
        val abre = expandedSection != id
        if (!abre || expandedSection != null) {
            anclaCierre = scrollState.value
            ultimoVDibujado[0] = scrollState.value
            caidaVista[0] = false
            compensarCierre = false
            scopeCierre.launch { delay(80); if (!compensarCierre) anclaCierre = null }
            scopeCierre.launch { delay(900); anclaCierre = null; compensarCierre = false }
        }
        expandedSection = if (abre) id else null
        if (id != "account") {
            if (abre) seguidor.abrir(posicionDe(id)) else seguidor.cerrar(posicionDe(id))
        }
    }

    val settingsBg      = MaterialTheme.colorScheme.background
    val statusOnColor   = if (contrastRatio(settingsBg, SuccessGreen) < 3f) SuccessGreenBright else SuccessGreen
    val statusOffColor  = if (contrastRatio(settingsBg, WarningAmber) < 3f) WarningAmberDark else WarningAmber

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                TopAppBar(
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                    },
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(stringResource(R.string.settings_title),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor())
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = topBarColors()
                )
                if (!isPremium) {
                    SettingsViewToggle(
                        currentMode  = settingsMode,
                        onModeChange = { viewModel.setSettingsSimple(it == SettingsMode.SIMPLE) }
                    )
                }
            }
        },
        containerColor      = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .contenedorDe(seguidor)
                    .verticalScroll(scrollState)
                    .graphicsLayer {
                        val ancla = anclaCierre
                        if (ancla != null) {
                            val v = scrollState.value
                            if (ultimoVDibujado[0] - v > 60) caidaVista[0] = true
                            if (v >= ancla - 40) caidaVista[0] = false
                            if (caidaVista[0]) translationY = (v - ancla).toFloat()
                            ultimoVDibujado[0] = v
                        } else {
                            caidaVista[0] = false
                        }
                    }
                    .padding(vertical = 10.dp)
            ) {

                if (uiState.showEditNameDialog) {
                    var editedName by remember { mutableStateOf(uiState.profileName) }
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }

                    AlertDialog(
                        modifier = Modifier.cuadroConTeclado(),
                        properties = propiedadesConTeclado(),
                        onDismissRequest = { viewModel.hideEditNameDialog() },
                        title = { Text(stringResource(R.string.settings_edit_name_title)) },
                        text  = {
                            OutlinedTextField(
                                value         = editedName,
                                onValueChange = { autoLockManager?.recordInteraction(); editedName = it },
                                label         = { Text(stringResource(R.string.auth_your_name)) },
                                singleLine    = true,
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { viewModel.updateName(editedName) }
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.updateName(editedName) },
                                enabled = editedName.isNotBlank(),
                                shape   = RoundedCornerShape(8.dp),
                                colors  = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor   = MaterialTheme.colorScheme.primary
                                ),
                                border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(R.string.action_save), fontWeight = FontWeight.SemiBold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.hideEditNameDialog() }) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        }
                    )
                }

                if (uiState.deleteAllAwaitingAuth) {
                    CsvExportAuthGate(
                        onAuthorized         = viewModel::onDeleteAllAuthorized,
                        onDismiss            = viewModel::onDeleteAllAuthCancelled,
                        verifyMasterPassword = viewModel::verifyMasterPassword,
                        promptTitle          = stringResource(R.string.delete_all_verify_title),
                        promptSubtitle       = stringResource(R.string.delete_all_verify_subtitle),
                        confirmLabel         = stringResource(R.string.delete_all_confirm_button),
                        fallbackPrompt       = stringResource(R.string.delete_all_verify_subtitle),
                    )
                }

                if (uiState.showDeleteAllDialog) {
                    val confirmPhrase = stringResource(R.string.delete_all_confirm_phrase)
                    var typed by remember { mutableStateOf("") }
                    val phraseMatches = typed.trim().equals(confirmPhrase, ignoreCase = true)
                    val inProgress = uiState.deleteAllInProgress
                    val needsSignIn = uiState.deleteAllNeedsSignIn
                    val deleteScope = rememberCoroutineScope()
                    val credentialManager = remember { CredentialManager.create(context) }

                    AlertDialog(
                        modifier = Modifier.cuadroConTeclado(),
                        properties = propiedadesConTeclado(),
                        onDismissRequest = { viewModel.hideDeleteAllDialog() },
                        title = {
                            Surface(
                                color    = MaterialTheme.colorScheme.error,
                                shape    = RoundedCornerShape(50),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.delete_all_dialog_title),
                                        color      = MaterialTheme.colorScheme.onError,
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        text = {
                            Column {
                                if (needsSignIn) {
                                    Text(
                                        stringResource(R.string.delete_all_signin_body),
                                        style = MaterialTheme.typography.bodyMedium.justified()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(
                                        onClick = { viewModel.confirmDeleteLocalOnly() },
                                        enabled = !inProgress
                                    ) {
                                        Text(
                                            stringResource(R.string.delete_all_local_only_button),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                } else {
                                    Text(
                                        stringResource(R.string.delete_all_dialog_body),
                                        style = MaterialTheme.typography.bodyMedium.justified()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        stringResource(R.string.delete_all_dialog_note),
                                        style = MaterialTheme.typography.bodySmall.justified(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text  = stringResource(R.string.delete_all_confirm_label, confirmPhrase),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false),
                                        value         = typed,
                                        onValueChange = { autoLockManager?.recordInteraction(); typed = it },
                                        singleLine    = true,
                                        enabled       = !inProgress,
                                        colors = if (phraseMatches) {
                                            OutlinedTextFieldDefaults.colors()
                                        } else {
                                            OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor   = warningColor(),
                                                unfocusedBorderColor = warningColor(),
                                                cursorColor          = warningColor()
                                            )
                                        },
                                        modifier      = Modifier.fillMaxWidth(),
                                        shape         = RoundedCornerShape(8.dp)
                                    )
                                }
                                if (inProgress) {
                                    Spacer(Modifier.height(16.dp))
                                    Row(
                                        modifier              = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(stringResource(R.string.delete_all_in_progress))
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            if (needsSignIn) {
                                Button(
                                    onClick = {
                                        deleteScope.launch {
                                            when (val res = requestGoogleAccountConnect(context, credentialManager)) {
                                                is GoogleConnectResult.Success ->
                                                    viewModel.onDeleteAllSignInResult(res.idToken, res.email)
                                                else ->
                                                    viewModel.onDeleteAllSignInResult(null, null)
                                            }
                                        }
                                    },
                                    enabled = !inProgress,
                                    shape   = RoundedCornerShape(8.dp),
                                    colors  = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor   = MaterialTheme.colorScheme.primary
                                    ),
                                    border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(stringResource(R.string.delete_all_signin_button), fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.requestDeleteAllAuth() },
                                    enabled = phraseMatches && !inProgress,
                                    colors  = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor   = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Text(stringResource(R.string.delete_all_confirm_button))
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.hideDeleteAllDialog() },
                                enabled = !inProgress
                            ) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        }
                    )
                }

                val gateFeatureName      = stringResource(R.string.settings_biometric_gate)
                val expiryFeatureName    = stringResource(R.string.settings_expiry_notif)
                val breachFeatureName    = stringResource(R.string.settings_breach_check)


                var showGateDialog by remember { mutableStateOf(false) }
                if (showGateDialog) {
                    BiometricGateDialog(
                        isEnabled        = uiState.isGateEnabled,
                        sections         = uiState.gateSections,
                        mode             = uiState.gateMode,
                        timeout          = uiState.gateTimeout,
                        isLocked         = !isPremium,
                        onEnabledChange  = { enabled ->
                            if (!enabled && uiState.isGateEnabled) viewModel.requestDisableGate()
                            else viewModel.setGateEnabled(enabled)
                        },
                        onSectionToggle  = { viewModel.toggleGateSection(it) },
                        onModeChange     = { viewModel.setGateMode(it) },
                        onTimeoutChange  = { viewModel.setGateTimeout(it) },
                        onLockTapped     = { showGateDialog = false; premiumGatedFeature = gateFeatureName },
                        onDismiss        = { showGateDialog = false }
                    )
                }

                val gateSubtitle = when {
                    !uiState.biometricAvailable -> stringResource(R.string.settings_biometric_gate_unavailable)
                    !isPremium                  -> stringResource(R.string.label_premium_gate)
                    !uiState.isGateEnabled      -> stringResource(R.string.settings_biometric_gate_subtitle_off)
                    else -> {
                        val names = buildList {
                            if (BiometricPrefs.SECTION_PASSWORDS in uiState.gateSections)
                                add(stringResource(R.string.biometric_gate_section_passwords))
                            if (BiometricPrefs.SECTION_CARDS in uiState.gateSections)
                                add(stringResource(R.string.biometric_gate_section_cards))
                            if (BiometricPrefs.SECTION_CUSTOM in uiState.gateSections)
                                add(stringResource(R.string.biometric_gate_section_custom))
                            if (BiometricPrefs.SECTION_TWO_FACTOR in uiState.gateSections)
                                add(stringResource(R.string.biometric_gate_section_two_factor))
                        }
                        if (names.isEmpty()) stringResource(R.string.settings_biometric_gate_subtitle_off)
                        else stringResource(R.string.settings_biometric_gate_subtitle_on, names.joinToString(", "))
                    }
                }

                var showAutoLockDialog by remember { mutableStateOf(false) }
                if (showAutoLockDialog) {
                    AutoLockDialog(
                        currentSeconds  = uiState.autoLockSeconds,
                        onSecondsChange = { viewModel.setAutoLockSeconds(it) },
                        onDismiss       = { showAutoLockDialog = false }
                    )
                }
                val autoLockSubtitle = if (uiState.autoLockSeconds == AutoLockPrefs.NEVER)
                    stringResource(R.string.settings_auto_lock_subtitle_never)
                else
                    stringResource(R.string.settings_auto_lock_subtitle, AutoLockPrefs.OPTIONS
                        .firstOrNull { it.first == uiState.autoLockSeconds }
                        ?.second?.let { stringResource(it) } ?: "3 minutos")

                var showClipboardDialog by remember { mutableStateOf(false) }
                if (showClipboardDialog) {
                    ClipboardClearDialog(
                        enabled        = uiState.clipboardClearEnabled,
                        currentSeconds = uiState.clipboardClearSeconds,
                        onEnabledChange = { viewModel.setClipboardClearEnabled(it) },
                        onSecondsChange = { viewModel.setClipboardClearSeconds(it) },
                        onDismiss       = { showClipboardDialog = false }
                    )
                }

                var showScreenCaptureDialog by remember { mutableStateOf(false) }
                if (showScreenCaptureDialog) {
                    ScreenCaptureDialog(onDismiss = { showScreenCaptureDialog = false })
                }

                var showExpiryDialog by remember { mutableStateOf(false) }
                if (showExpiryDialog) {
                    ExpiryNotifDialog(
                        enabled          = uiState.expiryNotifEnabled,
                        warningDays      = uiState.expiryNotifWarningDays,
                        isLocked         = !isPremium,
                        onEnabledChange  = { viewModel.setExpiryNotifEnabled(it) },
                        onDayToggle      = { viewModel.toggleExpiryWarningDay(it) },
                        onLockTapped     = { showExpiryDialog = false; premiumGatedFeature = expiryFeatureName },
                        onDismiss        = { showExpiryDialog = false }
                    )
                }
                val expirySubtitle = if (uiState.expiryNotifEnabled) {
                    val sorted = uiState.expiryNotifWarningDays.sorted()
                    if (sorted.size == 1)
                        stringResource(R.string.settings_expiry_notif_subtitle_on, sorted.first())
                    else
                        stringResource(R.string.settings_expiry_notif_subtitle_days, sorted.joinToString(", "))
                } else {
                    stringResource(R.string.settings_expiry_notif_subtitle_off)
                }

                var showBreachDialog by remember { mutableStateOf(false) }
                if (showBreachDialog) {
                    BreachCheckDialog(
                        enabled           = uiState.breachCheckEnabled,
                        frequencyDays     = uiState.breachCheckFrequencyDays,
                        isLocked          = !isPremium,
                        onEnabledChange   = { viewModel.setBreachCheckEnabled(it) },
                        onFrequencyChange = { viewModel.setBreachCheckFrequencyDays(it) },
                        onLockTapped      = { showBreachDialog = false; premiumGatedFeature = breachFeatureName },
                        onDismiss         = { showBreachDialog = false }
                    )
                }
                val breachSubtitle = if (uiState.breachCheckEnabled) {
                    val freqLabel = when (uiState.breachCheckFrequencyDays) {
                        1    -> stringResource(R.string.breach_check_freq_daily)
                        3    -> stringResource(R.string.breach_check_freq_3days)
                        else -> stringResource(R.string.breach_check_freq_weekly)
                    }
                    stringResource(R.string.settings_breach_check_subtitle_on, freqLabel)
                } else {
                    stringResource(R.string.settings_breach_check_subtitle_off)
                }

                CollapsibleSettingsCard(
                    icon          = Icons.Default.Person,
                    title         = stringResource(R.string.settings_section_profile),
                    isExpanded    = expandedSection == "account",
                    onHeaderClick = { toggleSection("account") },
                    posicion = posicionDe("account"),
                    shape         = RoundedCornerShape(topStart = SettingsGroupCornerRadius, topEnd = SettingsGroupCornerRadius)
                ) {
                    SettingsItem(
                        icon     = Icons.Default.Person,
                        title    = stringResource(R.string.settings_profile_name),
                        subtitle = uiState.profileName.ifEmpty { stringResource(R.string.settings_profile_name_hint) },
                        onClick  = { viewModel.showEditNameDialog() },
                        trailing = {
                            Icon(
                                imageVector        = Icons.Default.Edit,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    )
                    if (!coercionActive) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon     = Icons.Default.WorkspacePremium,
                            title    = stringResource(R.string.my_plan_screen_title),
                            subtitle = when (uiState.userPlan) {
                                UserPlan.FREE     -> stringResource(R.string.plans_free_name)
                                UserPlan.PREMIUM  -> stringResource(R.string.plans_premium_name)
                                UserPlan.FAMILY   -> stringResource(R.string.plans_family_name)
                            },
                            onClick  = onMyPlanClick
                        )
                    }
                    if (!coercionActive) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon          = Icons.Default.AccountCircle,
                            title         = stringResource(R.string.settings_google_account),
                            subtitle      = uiState.driveAccount ?: stringResource(R.string.drive_sync_not_connected),
                            subtitleColor = if (uiState.driveAccount != null) statusOnColor else statusOffColor,
                            onClick       = onDriveSyncClick
                        )
                    }
                    if (uiState.biometricAvailable && !coercionActive) {
                        SettingsItemDivider()
                        SettingsBiometricItem(
                            icon     = Icons.Default.Fingerprint,
                            title    = stringResource(R.string.settings_biometric),
                            enabled  = uiState.biometricEnabled,
                            onToggle = {
                                if (uiState.biometricEnabled) viewModel.disableBiometric()
                                else viewModel.requestEnableBiometric()
                            }
                        )
                    }
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.Layers,
                        title    = stringResource(R.string.settings_vault_management),
                        subtitle = stringResource(R.string.settings_vault_management_subtitle),
                        onClick  = onVaultManagementClick
                    )
                    if (!coercionActive) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon     = Icons.Default.LockReset,
                            title    = stringResource(R.string.settings_change_master_password),
                            subtitle = "",
                            onClick  = onChangeMasterPasswordClick
                        )
                    }
                    if (!coercionActive) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon          = Icons.Default.DeleteForever,
                            title         = stringResource(R.string.settings_delete_all),
                            subtitle      = stringResource(R.string.settings_delete_all_subtitle),
                            subtitleColor = dangerColor(),
                            titleColor    = dangerColor(),
                            iconTint      = dangerColor(),
                            onClick       = { viewModel.showDeleteAllDialog() }
                        )
                    }
                }

                SettingsGroupDivider()

                val securityChevronRotation by animateFloatAsState(
                    targetValue = if (expandedSection == "security") 180f else 0f,
                    label       = "settings_security_chevron"
                )
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .registrarPosicion(posicionDe("security"))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = { toggleSection("security") }
                            )
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint               = securityStatusColor(secState.level, statusOffColor),
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        val securitySuffixColor = when (secState.level) {
                            SecurityLevel.NO_DATA                       -> MaterialTheme.colorScheme.onSurfaceVariant
                            SecurityLevel.SAFE                          -> statusOnColor
                            SecurityLevel.INTERMEDIATE, SecurityLevel.CRITICAL -> statusOffColor
                        }
                        val securitySuffixText = securityHeaderSuffix(secState.level)
                        Text(
                            text       = buildAnnotatedString {
                                append(stringResource(R.string.settings_section_security))
                                append(" - ")
                                withStyle(SpanStyle(color = securitySuffixColor)) {
                                    append(securitySuffixText)
                                }
                            },
                            style      = MaterialTheme.typography.titleSmall,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color      = MaterialTheme.colorScheme.onSurface,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            modifier   = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector        = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier
                                .size(20.dp)
                                .rotate(securityChevronRotation)
                        )
                    }
                    AnimatedVisibility(
                        visible = expandedSection == "security",
                        enter   = EntradaDeApertura,
                        exit    = SalidaDeApertura
                    ) {
                    Column {
                    SecuritySubLines(
                        secState      = secState,
                        onWeakClick   = onNavigateToWeakPasswords,
                        onMediumClick = onNavigateToMediumPasswords
                    )
                    SettingsItem(
                        icon     = Icons.Default.LockClock,
                        title    = stringResource(R.string.settings_auto_lock),
                        subtitle = autoLockSubtitle,
                        subtitleColor = if (uiState.autoLockSeconds != AutoLockPrefs.NEVER) statusOnColor else statusOffColor,
                        onClick  = { showAutoLockDialog = true }
                    )
                    AnimatedVisibility(visible = showPremiumItems, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon          = Icons.Default.AdminPanelSettings,
                            title         = stringResource(R.string.settings_biometric_gate),
                            subtitle      = gateSubtitle,
                            subtitleColor = when {
                                !uiState.biometricAvailable -> null
                                !isPremium                  -> statusOffColor
                                !uiState.isGateEnabled || uiState.gateSections.isEmpty() -> statusOffColor
                                else -> statusOnColor
                            },
                            isLocked = !isPremium && uiState.biometricAvailable,
                            onClick  = {
                                when {
                                    !uiState.biometricAvailable -> {   }
                                    else -> showGateDialog = true
                                }
                            }
                        )
                    }
                    AnimatedVisibility(visible = showPremiumItems, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        SettingsItem(
                            modifier = Modifier.coachMarkAnchor("settings", "security"),
                            icon     = Icons.Default.Flight,
                            title    = stringResource(R.string.settings_travel_mode),
                            subtitle = if (!isPremium) stringResource(R.string.label_premium_gate)
                                else if (uiState.travelModeEnabled) stringResource(R.string.settings_status_enabled)
                                else stringResource(R.string.settings_status_disabled),
                            subtitleColor = if (!isPremium) statusOffColor
                                else if (uiState.travelModeEnabled) statusOnColor else statusOffColor,
                            isLocked = !isPremium,
                            onClick  = onTravelModeClick
                        )
                    }
                    AnimatedVisibility(visible = showPremiumItems && !coercionActive, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon          = Icons.Default.Warning,
                            title         = stringResource(R.string.settings_panic_pin),
                            subtitle      = if (!isPremium) stringResource(R.string.label_premium_gate)
                                            else if (uiState.panicPinEnabled) stringResource(R.string.settings_status_enabled)
                                            else stringResource(R.string.settings_status_disabled),
                            subtitleColor = if (!isPremium) statusOffColor
                                            else if (uiState.panicPinEnabled) statusOnColor else statusOffColor,
                            isLocked      = !isPremium,
                            onClick       = onPanicPinClick
                        )
                    }
                    AnimatedVisibility(visible = showPremiumItems && !coercionActive, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        SettingsItem(
                            modifier      = Modifier.coachMarkAnchor("settings", "disguise"),
                            icon          = Icons.Default.Calculate,
                            title         = stringResource(R.string.settings_calculator_disguise),
                            subtitle      = if (!isPremium) stringResource(R.string.label_premium_gate)
                                            else if (uiState.calculatorDisguiseEnabled) stringResource(R.string.settings_status_enabled)
                                            else stringResource(R.string.settings_status_disabled),
                            subtitleColor = if (!isPremium) statusOffColor
                                            else if (uiState.calculatorDisguiseEnabled) statusOnColor else statusOffColor,
                            isLocked      = !isPremium,
                            onClick       = onCalculatorDisguiseClick
                        )
                    }
                    AnimatedVisibility(visible = (showPremiumItems || uiState.hiddenEntriesEnabled) && !coercionActive, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon          = Icons.Default.VpnKey,
                            title         = stringResource(R.string.settings_hidden_entries),
                            subtitle      = if (uiState.hiddenEntriesEnabled) stringResource(R.string.settings_status_enabled)
                                            else if (!isPremium) stringResource(R.string.label_premium_gate)
                                            else stringResource(R.string.settings_status_disabled),
                            subtitleColor = if (uiState.hiddenEntriesEnabled) statusOnColor else statusOffColor,
                            isLocked = !isPremium && !uiState.hiddenEntriesEnabled,
                            onClick  = onHiddenEntriesClick
                        )
                    }
                    AnimatedVisibility(visible = showPremiumItems, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon     = Icons.Default.GppMaybe,
                            title    = stringResource(R.string.settings_breach_check),
                            subtitle = if (!isPremium) stringResource(R.string.label_premium_gate) else breachSubtitle,
                            subtitleColor = if (!isPremium) statusOffColor else if (uiState.breachCheckEnabled) statusOnColor else statusOffColor,
                            isLocked = !isPremium,
                            onClick  = { showBreachDialog = true }
                        )
                    }
                    AnimatedVisibility(visible = showPremiumItems, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        val auditLocked  = !isPremium
                        val auditTitle   = stringResource(R.string.hub_audit_title)
                        SettingsItem(
                            icon     = Icons.AutoMirrored.Filled.FactCheck,
                            title    = auditTitle,
                            subtitle = if (!isPremium) stringResource(R.string.label_premium_gate)
                                       else stringResource(R.string.hub_audit_subtitle),
                            subtitleColor = if (!isPremium) statusOffColor else null,
                            isLocked = auditLocked,
                            onClick  = {
                                if (auditLocked) premiumGatedFeature = auditTitle
                                else onAuditClick()
                            }
                        )
                    }
                    }
                    }
                }

                SettingsGroupDivider()

                CollapsibleSettingsCard(
                    icon          = Icons.Default.VisibilityOff,
                    title         = stringResource(R.string.settings_section_privacy),
                    isExpanded    = expandedSection == "privacy",
                    onHeaderClick = { toggleSection("privacy") },
                    posicion = posicionDe("privacy"),
                ) {
                    SettingsItem(
                        icon    = Icons.Default.Spellcheck,
                        title   = stringResource(R.string.settings_autofill),
                        subtitle = if (uiState.autofillEnabled)
                            stringResource(R.string.settings_status_enabled)
                        else
                            stringResource(R.string.settings_status_disabled),
                        subtitleColor = if (uiState.autofillEnabled) statusOnColor else statusOffColor,
                        onClick = onAutofillClick
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.Mic,
                        title    = stringResource(R.string.settings_voice_dictation),
                        subtitle = if (uiState.voiceDictationEnabled)
                            stringResource(R.string.voice_dictation_status_on)
                        else
                            stringResource(R.string.voice_dictation_status_off),
                        subtitleColor = if (uiState.voiceDictationEnabled) statusOnColor else statusOffColor,
                        onClick  = onVoiceDictationClick
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon    = Icons.Default.ContentPaste,
                        title   = stringResource(R.string.settings_clipboard_clear),
                        subtitle = if (uiState.clipboardClearEnabled)
                            stringResource(R.string.settings_clipboard_clear_subtitle_on, uiState.clipboardClearSeconds)
                        else
                            stringResource(R.string.settings_clipboard_clear_subtitle_off),
                        subtitleColor = if (uiState.clipboardClearEnabled) statusOnColor else statusOffColor,
                        onClick = { showClipboardDialog = true }
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.CameraAlt,
                        title    = stringResource(R.string.settings_screen_capture),
                        subtitle = if (capturePermitActive)
                            stringResource(R.string.screen_capture_state_allowed)
                        else
                            stringResource(R.string.screen_capture_state_blocked),
                        subtitleColor = if (capturePermitActive) statusOffColor else statusOnColor,
                        onClick  = { showScreenCaptureDialog = true }
                    )
                }

                SettingsGroupDivider()

                CollapsibleSettingsCard(
                    icon          = Icons.Default.Sync,
                    title         = stringResource(R.string.settings_section_sync_data),
                    isExpanded    = expandedSection == "sync",
                    onHeaderClick = { toggleSection("sync") },
                    posicion = posicionDe("sync"),
                ) {
                    AnimatedVisibility(modifier = Modifier.bringIntoViewRequester(driveRowRequester), visible = (showPremiumItems || uiState.lastDriveSync > 0L || uiState.hasDriveBackup) && !coercionActive, enter = premiumEnter, exit = premiumExit) {
                        SettingsItem(
                            icon       = Icons.Default.Cloud,
                            title      = stringResource(R.string.settings_google_drive),
                            subtitle   = if (!isPremium) stringResource(R.string.label_premium_gate)
                                         else if (uiState.driveAccount != null)
                                             uiState.driveAccount + " · " + driveSyncSubtitle(uiState.lastDriveSync)
                                         else
                                             driveSyncSubtitle(uiState.lastDriveSync),
                            subtitleColor = if (!isPremium) statusOffColor else null,
                            isLocked   = !isPremium,
                            onClick    = { onDriveSyncClick() },
                            pulseAlpha = drivePulse.value
                        )
                    }
                    AnimatedVisibility(modifier = Modifier.bringIntoViewRequester(timeMachineRowRequester), visible = (showPremiumItems || uiState.hasSnapshots) && !coercionActive, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon       = Icons.Default.History,
                            title      = stringResource(R.string.settings_time_machine),
                            subtitle   = if (!isPremium) stringResource(R.string.label_premium_gate)
                                         else stringResource(R.string.settings_time_machine_subtitle),
                            subtitleColor = if (!isPremium) statusOffColor else null,
                            isLocked   = !isPremium,
                            onClick    = { onTimeMachineClick() },
                            pulseAlpha = timeMachinePulse.value
                        )
                    }
                    val backupKeyAccessible = isPremium || uiState.backupKeyConfirmed
                    AnimatedVisibility(visible = (showPremiumItems || uiState.backupKeyConfirmed) && !coercionActive, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        val backupKeyName = stringResource(R.string.kit_title)
                        SettingsItem(
                            icon       = Icons.Default.Key,
                            title      = backupKeyName,
                            subtitle   = if (!backupKeyAccessible) stringResource(R.string.label_premium_gate)
                                         else stringResource(R.string.settings_backup_key_subtitle),
                            subtitleColor = if (!backupKeyAccessible) statusOffColor else null,
                            isLocked   = !backupKeyAccessible,
                            onClick    = { if (backupKeyAccessible) onBackupKeyClick() else premiumGatedFeature = backupKeyName }
                        )
                    }
                    AnimatedVisibility(visible = (showPremiumItems || uiState.isHeirOfOwner) && !coercionActive, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon          = Icons.Default.FamilyRestroom,
                            title         = stringResource(R.string.inheritance_title),
                            subtitle      = when {
                                !isPremium              -> stringResource(R.string.label_premium_gate)
                                !uiState.inheritanceEnabled -> stringResource(R.string.settings_status_disabled)
                                !uiState.inheritanceHasHeir -> stringResource(R.string.inheritance_settings_active_unconfigured)
                                else                    -> stringResource(R.string.settings_status_enabled)
                            },
                            subtitleColor = when {
                                !isPremium                  -> statusOffColor
                                !uiState.inheritanceEnabled -> statusOffColor
                                !uiState.inheritanceHasHeir -> statusOffColor
                                else                        -> statusOnColor
                            },
                            isLocked      = !isPremium,
                            onClick       = onDigitalInheritanceClick
                        )
                    }
                    AnimatedVisibility(visible = (uiState.userPlan == UserPlan.FAMILY || showPremiumItems) && !coercionActive, enter = premiumEnter, exit = premiumExit) {
                        SettingsItemDivider()
                        SettingsItem(
                            icon     = Icons.Default.Groups,
                            title    = stringResource(R.string.settings_family_plan),
                            subtitle = stringResource(R.string.settings_family_plan_subtitle),
                            isLocked = uiState.userPlan != UserPlan.FAMILY,
                            onClick  = onFamilyPlanClick
                        )
                    }
                    if (!coercionActive) {
                        AnimatedVisibility(visible = showPremiumItems, enter = premiumEnter, exit = premiumExit) {
                            SettingsItemDivider()
                        }
                        SettingsItem(
                            icon     = Icons.Default.SwapVert,
                            title    = stringResource(R.string.settings_import_export),
                            subtitle = stringResource(R.string.settings_import_export_subtitle),
                            onClick  = onImportExportClick
                        )
                    }
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.QrCodeScanner,
                        title    = stringResource(R.string.receive_title),
                        subtitle = stringResource(R.string.settings_receive_subtitle),
                        onClick  = onReceiveEntryClick
                    )
                }

                SettingsGroupDivider()

                CollapsibleSettingsCard(
                    icon          = Icons.Default.Notifications,
                    title         = stringResource(R.string.settings_section_notifications),
                    isExpanded    = expandedSection == "notifications",
                    onHeaderClick = { toggleSection("notifications") },
                    posicion = posicionDe("notifications"),
                    showBadge     = unreadNotifs > 0
                ) {
                    AnimatedVisibility(visible = showPremiumItems, enter = premiumEnter, exit = premiumExit) {
                        SettingsItem(
                            icon     = Icons.Default.NotificationsActive,
                            title    = stringResource(R.string.settings_expiry_notif),
                            subtitle = if (!isPremium) stringResource(R.string.label_premium_gate) else expirySubtitle,
                            subtitleColor = if (!isPremium) statusOffColor else if (uiState.expiryNotifEnabled) statusOnColor else statusOffColor,
                            isLocked = !isPremium,
                            onClick  = { showExpiryDialog = true }
                        )
                    }
                    if (!coercionActive) {
                        AnimatedVisibility(visible = showPremiumItems, enter = premiumEnter, exit = premiumExit) {
                            SettingsItemDivider()
                        }
                        SettingsItemWithBadge(
                            icon       = Icons.Default.Notifications,
                            title      = stringResource(R.string.notif_screen_title),
                            subtitle   = stringResource(R.string.hub_notifications_subtitle),
                            badgeCount = unreadNotifs,
                            onClick    = onNotificationsClick
                        )
                    }
                }

                SettingsGroupDivider()

                CollapsibleSettingsCard(
                    icon          = Icons.Default.Palette,
                    title         = stringResource(R.string.settings_section_appearance),
                    isExpanded    = expandedSection == "appearance",
                    onHeaderClick = { toggleSection("appearance") },
                    posicion = posicionDe("appearance"),
                ) {
                    SettingsItem(
                        icon     = Icons.Default.Palette,
                        title    = stringResource(R.string.settings_theme),
                        subtitle = ALL_THEMES.find { it.id == uiState.themeId }?.displayName()
                            ?: stringResource(R.string.settings_theme_subtitle),
                        onClick  = onThemeClick
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.Language,
                        title    = stringResource(R.string.settings_language),
                        subtitle = supportedLanguages.find { it.code == uiState.languageCode }?.name ?: "",
                        onClick  = onLanguageClick
                    )
                }

                SettingsGroupDivider()

                CollapsibleSettingsCard(
                    icon          = Icons.Default.Info,
                    title         = stringResource(R.string.settings_section_about),
                    isExpanded    = expandedSection == "about",
                    onHeaderClick = { toggleSection("about") },
                    posicion = posicionDe("about"),
                    shape         = RoundedCornerShape(bottomStart = SettingsGroupCornerRadius, bottomEnd = SettingsGroupCornerRadius)
                ) {
                    SettingsItem(
                        icon     = Icons.Default.Info,
                        title    = stringResource(R.string.settings_version),
                        subtitle = "",
                        onClick  = {},
                        trailing = {
                            Text(
                                text  = BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.Language,
                        title    = stringResource(R.string.settings_website),
                        subtitle = "",
                        onClick  = { context.openUrl("https://thormakk.dev") }
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.PrivacyTip,
                        title    = stringResource(R.string.settings_privacy_policy),
                        subtitle = "",
                        onClick  = { onLegalDocClick("privacy") }
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.Description,
                        title    = stringResource(R.string.settings_terms),
                        subtitle = "",
                        onClick  = { onLegalDocClick("terms") }
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.Gavel,
                        title    = stringResource(R.string.settings_legal_notice),
                        subtitle = "",
                        onClick  = { onLegalDocClick("legal") }
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.AutoMirrored.Filled.Article,
                        title    = stringResource(R.string.settings_license),
                        subtitle = "",
                        onClick  = { onLegalDocClick("license") }
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.Code,
                        title    = stringResource(R.string.settings_opensource),
                        subtitle = "",
                        onClick  = { onLegalDocClick("opensource") }
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.DeleteForever,
                        title    = stringResource(R.string.settings_delete_account_doc),
                        subtitle = "",
                        onClick  = { onLegalDocClick("delete") }
                    )
                    SettingsItemDivider()
                    SettingsItem(
                        icon     = Icons.Default.Favorite,
                        title    = stringResource(R.string.donation_title),
                        subtitle = "",
                        onClick  = onDonateClick
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }
}

@Composable
private fun SettingsCard(
    shape:   RoundedCornerShape = RoundedCornerShape(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 7.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background),
        content = content
    )
}

private fun String.toSentenceCase(): String =
    split(" & ").joinToString(" & ") { part ->
        if (part.isEmpty()) part else part[0].uppercaseChar() + part.substring(1).lowercase()
    }

@Composable
private fun CollapsibleSettingsCard(
    icon:          ImageVector,
    title:         String,
    isExpanded:    Boolean,
    onHeaderClick: () -> Unit,
    posicion:      PosicionDeTitulo? = null,
    showBadge:     Boolean = false,
    shape:         RoundedCornerShape = RoundedCornerShape(0.dp),
    content:       @Composable ColumnScope.() -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label       = "settings_card_chevron"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 7.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showBadge) {
            val ribbonColor = MaterialTheme.colorScheme.error
            Canvas(modifier = Modifier.size(21.dp)) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = ribbonColor)
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .let { if (posicion != null) it.registrarPosicion(posicion) else it }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onHeaderClick
                    )
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text       = title.toSentenceCase(),
                    style      = MaterialTheme.typography.titleSmall,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                Icon(
                    imageVector        = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation)
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter   = if (posicion != null) EntradaDeApertura else expandVertically() + fadeIn(),
                exit    = if (posicion != null) SalidaDeApertura else shrinkVertically() + fadeOut()
            ) {
                Column(content = content)
            }
        }
    }
}

@Composable
private fun SettingsItemDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 55.dp, end = 16.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun SettingsGroupDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 7.dp)
            .height(SettingsGroupGap)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        )
        Box(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
private fun driveSyncSubtitle(lastSync: Long): String {
    if (lastSync == 0L) return stringResource(R.string.drive_never_synced)
    val diffMs  = System.currentTimeMillis() - lastSync
    val minutes = (diffMs / 60_000L).toInt()
    val hours   = (diffMs / 3_600_000L).toInt()
    val days    = (diffMs / 86_400_000L).toInt()
    return when {
        minutes < 1  -> stringResource(R.string.drive_synced_seconds)
        minutes < 60 -> stringResource(R.string.drive_synced_minutes, minutes)
        hours   < 24 -> stringResource(R.string.drive_synced_hours, hours)
        days    == 1 -> stringResource(R.string.drive_synced_yesterday)
        else         -> stringResource(R.string.drive_synced_days, days)
    }
}

private fun securityStatusColor(level: SecurityLevel, criticalColor: Color): Color = when (level) {
    SecurityLevel.NO_DATA      -> Color(0xFF9E9E9E)
    SecurityLevel.SAFE         -> Color(0xFF43A047)
    SecurityLevel.INTERMEDIATE -> Color(0xFFFFB300)
    SecurityLevel.CRITICAL     -> criticalColor
}

@Composable
private fun securityHeaderSuffix(level: SecurityLevel): String = when (level) {
    SecurityLevel.NO_DATA      -> stringResource(R.string.settings_security_header_no_data)
    SecurityLevel.SAFE         -> stringResource(R.string.settings_security_header_ok)
    SecurityLevel.INTERMEDIATE -> stringResource(R.string.settings_security_header_warn)
    SecurityLevel.CRITICAL     -> stringResource(R.string.settings_security_header_risk)
}

@Composable
private fun SecuritySubLines(
    secState:      SecurityUiState,
    onWeakClick:   () -> Unit,
    onMediumClick: () -> Unit
) {
    val showWeakSubLine   = secState.weakCount > 0
    val showMediumSubLine = secState.mediumCount > 0
    if (!showWeakSubLine && !showMediumSubLine) return

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showWeakSubLine) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onWeakClick)
                    .padding(start = 16.dp, end = 12.dp, top = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(14.dp).background(Color(0xFFE53935), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text     = if (secState.weakCount == 1) stringResource(R.string.hub_weak_single)
                               else stringResource(R.string.hub_weak_plural, secState.weakCount),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
        if (showMediumSubLine) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onMediumClick)
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(14.dp).background(Color(0xFFFFB300), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text     = if (secState.mediumCount == 1) stringResource(R.string.hub_medium_single)
                               else stringResource(R.string.hub_medium_plural, secState.mediumCount),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
        SettingsItemDivider()
    }
}

@Composable
private fun SettingsItemWithBadge(
    icon:       ImageVector,
    title:      String,
    subtitle:   String = "",
    badgeCount: Int    = 0,
    onClick:    () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp)) {
            Icon(imageVector = icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxSize())
            if (badgeCount > 0) {
                Box(modifier = Modifier.size(10.dp).align(Alignment.TopEnd)
                    .background(MaterialTheme.colorScheme.error, CircleShape))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontSize = 15.sp,
                    fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                if (badgeCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.error) {
                        Text(text = badgeCount.coerceAtMost(99).toString(),
                            style = MaterialTheme.typography.labelSmall, color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsItem(
    icon:         ImageVector,
    title:        String,
    subtitle:     String,
    onClick:      () -> Unit,
    modifier:     Modifier = Modifier,
    subtitleColor: Color? = null,
    titleColor:   Color? = null,
    iconTint:     Color? = null,
    pulseAlpha:   Float = 0f,
    isLocked:     Boolean = false,
    trailing:     (@Composable () -> Unit)? = null
) {
    val pulseColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (pulseAlpha > 0f)
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = (1.5f * pulseAlpha).dp,
                            color = pulseColor.copy(alpha = pulseAlpha * 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        )
                else Modifier
            )
            .clickable(onClick = onClick)
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = iconTint ?: MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodyMedium,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = titleColor ?: MaterialTheme.colorScheme.onBackground
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text     = subtitle,
                    style    = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color    = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else if (isLocked) {
            Icon(
                imageVector        = Icons.Default.Lock,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(16.dp)
            )
        } else {
            Icon(
                imageVector        = Icons.Default.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AutoLockDialog(
    currentSeconds: Int,
    onSecondsChange: (Int) -> Unit,
    onDismiss:      () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Text(
                stringResource(R.string.auto_lock_dialog_title),
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.auto_lock_dialog_body),
                    style     = MaterialTheme.typography.bodyMedium.justified(),
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AutoLockPrefs.OPTIONS.chunked(2).forEach { rowOptions ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            rowOptions.forEach { (secs, labelRes) ->
                                FilterChip(
                                    selected = secs == currentSeconds,
                                    onClick  = { onSecondsChange(secs) },
                                    label    = {
                                        Text(
                                            text      = stringResource(labelRes),
                                            style     = MaterialTheme.typography.labelMedium,
                                            maxLines  = 1,
                                            textAlign = TextAlign.Center,
                                            modifier  = Modifier.fillMaxWidth()
                                        )
                                    },
                                    colors   = chipColors,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowOptions.size < 2) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_accept))
            }
        }
    )
}

@Composable
private fun ClipboardClearDialog(
    enabled:        Boolean,
    currentSeconds: Int,
    onEnabledChange: (Boolean) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onDismiss:      () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Text(
                stringResource(R.string.clipboard_clear_dialog_title),
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.clipboard_clear_dialog_body),
                    style     = MaterialTheme.typography.bodyMedium.justified(),
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (enabled) stringResource(R.string.label_enabled)
                        else stringResource(R.string.label_disabled),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked         = enabled,
                        onCheckedChange = onEnabledChange,
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor    = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor    = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                }
                if (enabled) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        ClipboardPrefs.OPTIONS_SECONDS.forEach { secs ->
                            val selected = secs == currentSeconds
                            FilterChip(
                                selected = selected,
                                onClick  = { onSecondsChange(secs) },
                                label    = {
                                    Text(
                                        text  = "${secs}s",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                                containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_accept))
            }
        }
    )
}

@Composable
private fun ScreenCaptureDialog(
    onDismiss: () -> Unit,
    vm: ScreenCaptureViewModel = hiltViewModel()
) {
    val permitActive    by vm.permitActive.collectAsStateWithLifecycle()
    val durationSeconds by vm.durationSeconds.collectAsStateWithLifecycle()
    val allowGate = rememberMandatoryBiometricGate(verifyMasterPassword = vm::verifyMasterPassword)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Text(
                stringResource(R.string.screen_capture_title),
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.screen_capture_explanation),
                    style     = MaterialTheme.typography.bodyMedium.justified(),
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (permitActive) stringResource(R.string.screen_capture_state_allowed)
                        else stringResource(R.string.screen_capture_state_blocked),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked         = permitActive,
                        onCheckedChange = { checked ->
                            if (checked) allowGate.launch { vm.grant() } else vm.revoke()
                        },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor    = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor    = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                }
                if (!permitActive) {
                    Text(
                        stringResource(R.string.screen_capture_duration_title),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        ScreenCapturePrefs.OPTIONS.forEach { (secs, labelRes) ->
                            FilterChip(
                                selected = secs == durationSeconds,
                                onClick  = { vm.setDuration(secs) },
                                label    = {
                                    Text(
                                        text  = stringResource(labelRes),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_accept))
            }
        }
    )
}

@Composable
private fun SettingsBiometricItem(
    icon:     ImageVector,
    title:    String,
    enabled:  Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text       = title,
            style      = MaterialTheme.typography.bodyMedium,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onBackground,
            modifier   = Modifier.weight(1f)
        )
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Switch(
                checked         = enabled,
                onCheckedChange = { onToggle() },
                colors          = SwitchDefaults.colors(
                    checkedThumbColor    = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor    = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun ExpiryNotifDialog(
    enabled:         Boolean,
    warningDays:     Set<Int>,
    isLocked:        Boolean = false,
    onEnabledChange: (Boolean) -> Unit,
    onDayToggle:     (Int) -> Unit,
    onLockTapped:    () -> Unit = {},
    onDismiss:       () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Text(
                stringResource(R.string.expiry_notif_dialog_title),
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.expiry_notif_dialog_body),
                    style     = MaterialTheme.typography.bodyMedium.justified(),
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (enabled) stringResource(R.string.label_enabled)
                        else stringResource(R.string.label_disabled),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    if (isLocked) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                    Switch(
                                        checked         = false,
                                        onCheckedChange = null,
                                        enabled         = false,
                                        colors          = SwitchDefaults.colors(
                                            disabledUncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                            disabledUncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                                            disabledUncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            indication        = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { onLockTapped() }
                                )
                            }
                            IconButton(onClick = onLockTapped) {
                                Icon(
                                    imageVector        = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Switch(
                            checked         = enabled,
                            onCheckedChange = onEnabledChange,
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor    = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor    = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                val configAlpha = if (enabled && !isLocked) 1f else 0.38f
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.expiry_notif_days_label),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = configAlpha)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    ExpiryNotificationPrefs.WARNING_DAY_OPTIONS.forEach { day ->
                        FilterChip(
                            selected = day in warningDays,
                            onClick  = { onDayToggle(day) },
                            enabled  = enabled && !isLocked,
                            label    = {
                                Text(
                                    text  = "${day}d",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors   = chipColors
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_accept))
            }
        }
    )
}

@Composable
private fun BiometricGateDialog(
    isEnabled:       Boolean,
    sections:        Set<String>,
    mode:            String,
    timeout:         Int,
    isLocked:        Boolean = false,
    onEnabledChange: (Boolean) -> Unit,
    onSectionToggle: (String) -> Unit,
    onModeChange:    (String) -> Unit,
    onTimeoutChange: (Int) -> Unit,
    onLockTapped:    () -> Unit = {},
    onDismiss:       () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Text(
                stringResource(R.string.biometric_gate_dialog_title),
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.biometric_gate_dialog_body),
                    style     = MaterialTheme.typography.bodyMedium.justified(),
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (isEnabled) stringResource(R.string.label_enabled)
                        else stringResource(R.string.label_disabled),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    if (isLocked) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                    Switch(
                                        checked         = false,
                                        onCheckedChange = null,
                                        enabled         = false,
                                        colors          = SwitchDefaults.colors(
                                            disabledUncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                            disabledUncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                                            disabledUncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            indication        = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { onLockTapped() }
                                )
                            }
                            IconButton(onClick = onLockTapped) {
                                Icon(
                                    imageVector        = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Switch(
                            checked         = isEnabled,
                            onCheckedChange = onEnabledChange,
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor    = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor    = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                val configAlpha = if (isEnabled && !isLocked) 1f else 0.38f

                Text(
                    stringResource(R.string.biometric_gate_sections_title),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = configAlpha)
                )
                val sectionItems = listOf(
                    BiometricPrefs.SECTION_PASSWORDS  to R.string.biometric_gate_section_passwords,
                    BiometricPrefs.SECTION_CARDS      to R.string.biometric_gate_section_cards,
                    BiometricPrefs.SECTION_CUSTOM     to R.string.biometric_gate_section_custom,
                    BiometricPrefs.SECTION_TWO_FACTOR to R.string.biometric_gate_section_two_factor
                )
                sectionItems.forEach { (key, labelRes) ->
                    val sectionInteractive = isEnabled && !isLocked
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .then(if (sectionInteractive) Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSectionToggle(key) } else Modifier)
                            .padding(vertical = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                            Checkbox(
                                checked         = key in sections,
                                onCheckedChange = { if (sectionInteractive) onSectionToggle(key) },
                                enabled         = sectionInteractive,
                                colors          = CheckboxDefaults.colors(
                                    checkedColor         = MaterialTheme.colorScheme.primary,
                                    disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = configAlpha),
                                    disabledUncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = configAlpha)
                                )
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = stringResource(labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = configAlpha)
                        )
                    }
                }

                Text(
                    stringResource(R.string.biometric_gate_mode_title),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = configAlpha)
                )
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == BiometricPrefs.MODE_PER_ACCESS,
                        onClick  = { onModeChange(BiometricPrefs.MODE_PER_ACCESS) },
                        enabled  = isEnabled && !isLocked,
                        label    = { Text(stringResource(R.string.biometric_gate_mode_per_access), style = MaterialTheme.typography.labelMedium) },
                        colors   = chipColors
                    )
                    FilterChip(
                        selected = mode == BiometricPrefs.MODE_SESSION,
                        onClick  = { onModeChange(BiometricPrefs.MODE_SESSION) },
                        enabled  = isEnabled && !isLocked,
                        label    = { Text(stringResource(R.string.biometric_gate_mode_session), style = MaterialTheme.typography.labelMedium) },
                        colors   = chipColors
                    )
                }

                val timeoutEnabled = isEnabled && !isLocked && mode == BiometricPrefs.MODE_SESSION
                val timeoutAlpha   = if (timeoutEnabled) 1f else 0.38f
                Text(
                    stringResource(R.string.biometric_gate_timeout_title),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = timeoutAlpha)
                )
                val timeoutLabels = mapOf(
                    30  to R.string.biometric_gate_timeout_30s,
                    60  to R.string.biometric_gate_timeout_60s,
                    120 to R.string.biometric_gate_timeout_120s
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BiometricPrefs.TIMEOUT_OPTIONS.forEach { secs ->
                        val labelRes = timeoutLabels[secs] ?: R.string.biometric_gate_timeout_60s
                        FilterChip(
                            selected = secs == timeout,
                            onClick  = { onTimeoutChange(secs) },
                            enabled  = timeoutEnabled,
                            label    = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelMedium) },
                            colors   = chipColors
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_accept))
            }
        }
    )
}

@Composable
private fun BreachCheckDialog(
    enabled:           Boolean,
    frequencyDays:     Int,
    isLocked:          Boolean = false,
    onEnabledChange:   (Boolean) -> Unit,
    onFrequencyChange: (Int) -> Unit,
    onLockTapped:      () -> Unit = {},
    onDismiss:         () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Text(
                stringResource(R.string.breach_check_dialog_title),
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.breach_check_dialog_body),
                    style     = MaterialTheme.typography.bodyMedium.justified(),
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (enabled) stringResource(R.string.label_enabled)
                        else stringResource(R.string.label_disabled),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    if (isLocked) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                    Switch(
                                        checked         = false,
                                        onCheckedChange = null,
                                        enabled         = false,
                                        colors          = SwitchDefaults.colors(
                                            disabledUncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                            disabledUncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                                            disabledUncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            indication        = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { onLockTapped() }
                                )
                            }
                            IconButton(onClick = onLockTapped) {
                                Icon(
                                    imageVector        = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Switch(
                            checked         = enabled,
                            onCheckedChange = onEnabledChange,
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor    = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor    = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                val configAlpha = if (enabled && !isLocked) 1f else 0.38f
                Text(
                    stringResource(R.string.breach_check_freq_label),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = configAlpha)
                )
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BreachCheckPrefs.FREQUENCY_OPTIONS.forEach { days ->
                        val label = when (days) {
                            1    -> stringResource(R.string.breach_check_freq_daily)
                            3    -> stringResource(R.string.breach_check_freq_3days)
                            else -> stringResource(R.string.breach_check_freq_weekly)
                        }
                        FilterChip(
                            selected = days == frequencyDays,
                            onClick  = { onFrequencyChange(days) },
                                         enabled  = enabled && !isLocked,
                            label    = {
                                Text(
                                    text  = label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors   = chipColors
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_accept), fontWeight = FontWeight.Medium)
            }
        }
    )
}


@Composable
private fun SettingsViewToggle(
    currentMode:  SettingsMode,
    onModeChange: (SettingsMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 8.dp)
    ) {
        Text(
            text      = stringResource(R.string.settings_tab_caption),
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = currentMode == SettingsMode.SIMPLE,
                onClick  = { onModeChange(SettingsMode.SIMPLE) },
                shape    = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text(stringResource(R.string.settings_tab_simple))
            }
            SegmentedButton(
                selected = currentMode == SettingsMode.COMPLETO,
                onClick  = { onModeChange(SettingsMode.COMPLETO) },
                shape    = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text(stringResource(R.string.settings_tab_completo))
            }
        }
    }
}

@Composable
private fun PremiumGateDialog(
    featureName: String,
    onDismiss:   () -> Unit,
    onUpgrade:   () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.background,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(
                    imageVector        = Icons.Default.Lock,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(featureName)
            }
        },
        text = {
            Text(stringResource(R.string.premium_locked_dialog_message))
        },
        confirmButton = {
            Button(
                onClick = onUpgrade,
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = MaterialTheme.colorScheme.primary
                ),
                border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.premium_locked_dialog_cta), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
