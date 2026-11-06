@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.thormakk.keyarmor.presentation.screens.inheritance

import com.thormakk.keyarmor.presentation.components.sigueAlTecladoObjetivo
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoContenido
import com.thormakk.keyarmor.presentation.components.sigueAlTeclado
import com.thormakk.keyarmor.presentation.components.rememberSigueAlTeclado
import com.thormakk.keyarmor.presentation.components.SigueAlTeclado
import androidx.compose.foundation.interaction.MutableInteractionSource

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.util.Patterns
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Warning
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.util.formatDateLocalized
import com.thormakk.keyarmor.presentation.util.rememberMandatoryBiometricGate
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import androidx.credentials.CredentialManager
import com.thormakk.keyarmor.presentation.util.GoogleConnectResult
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import com.thormakk.keyarmor.presentation.util.requestGoogleAccountConnect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun InheritanceScreen(
    onBack:       () -> Unit,
    onPlansClick: () -> Unit = {},
    viewModel:    InheritanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val msgAccountPickerUnavailable = stringResource(R.string.error_google_account_picker_unavailable)
    val scrollState      = rememberScrollState()
    val sigueTeclado     = rememberSigueAlTeclado()

    var heirToRemove     by remember { mutableStateOf<String?>(null) }
    var showResignDialog by remember { mutableStateOf(false) }

    var codigoVisible   by remember { mutableStateOf<String?>(null) }
    var codigoNoLegible by remember { mutableStateOf(false) }

    val gateAccionSensible = rememberMandatoryBiometricGate(
        verifyMasterPassword = viewModel::verifyMasterPassword
    )

    val msgHeirAdded        = stringResource(R.string.inheritance_event_heir_added)
    val msgResignSuccess    = stringResource(R.string.inheritance_heir_resign_success)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.reload()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InheritanceEvent.HeirAdded           -> snackbarHostState.showSnackbar(msgHeirAdded)
                is InheritanceEvent.Error               -> snackbarHostState.showSnackbar(event.message)
                is InheritanceEvent.HeirResignedSuccess -> snackbarHostState.showSnackbar(msgResignSuccess.format(event.ownerEmail))
                is InheritanceEvent.HeirConsentPending  -> onBack()
            }
        }
    }

    codigoVisible?.let { codigo ->
        HeirRecoveryCodeScreen(
            code   = codigo,
            onDone = { codigoVisible = null },
        )
        return
    }

    if (codigoNoLegible) {
        AlertDialog(
            onDismissRequest = { codigoNoLegible = false },
            title = { Text(stringResource(R.string.heir_recovery_code_unavailable_title)) },
            text  = { Text(stringResource(R.string.heir_recovery_code_unavailable_body), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                TextButton(onClick = { codigoNoLegible = false }) {
                    Text(stringResource(R.string.action_understood))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            TopAppBar(
                actions = {
                    CaptureAllowedBadge()
                    ThemeTrialBadge()
                },
                title = { TextoDeUnaLinea(stringResource(R.string.inheritance_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                    color = sectionTitleColor()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.inheritance_back_cd)
                        )
                    }
                },
                colors = topBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .sigueAlTeclado(scrollState, sigueTeclado)
                .verticalScroll(scrollState)
                .sigueAlTecladoContenido(sigueTeclado)
                .padding(padding)
                .padding(horizontal = 7.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { viewModel.setIntroExpanded(!uiState.isIntroExpanded) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text       = stringResource(R.string.inheritance_intro_title),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier   = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = if (uiState.isIntroExpanded)
                            Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.isIntroExpanded,
                    enter   = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit    = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    Column {
                        Text(
                            text       = stringResource(R.string.inheritance_intro_desc),
                            style      = MaterialTheme.typography.bodySmall.justified(),
                            fontSize   = 13.sp,
                            lineHeight = 19.sp,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier   = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        )
                        HorizontalDivider(
                            modifier  = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        )
                        Text(
                            text       = stringResource(R.string.inheritance_how_it_works).uppercase(),
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
                        )
                        HowItWorksStep(number = "1", text = stringResource(R.string.inheritance_step_1))
                        HowItWorksStep(number = "2", text = stringResource(R.string.inheritance_step_2))
                        HowItWorksStep(number = "3", text = stringResource(R.string.inheritance_step_3))
                        HowItWorksStep(number = "4", text = stringResource(R.string.inheritance_step_4))
                        HowItWorksStep(number = "5", text = stringResource(R.string.inheritance_step_5))
                        HowItWorksStep(number = "6", text = stringResource(R.string.inheritance_step_6), isLast = true)
                    }
                }
            }

            uiState.ownerEmailAsHeir?.let { ownerEmail ->
                HeirRoleCard(
                    ownerEmail      = ownerEmail,
                    heirAcceptedAt  = uiState.heirAcceptedAt,
                    heirOfOwnerConfirmedAt = uiState.heirOfOwnerConfirmedAt,
                    heirInheritedAt = uiState.heirInheritedAt,
                    myKeyFingerprint = viewModel.getMyKeyFingerprint(),
                    isLoading       = uiState.isLoading,
                    onResign        = { showResignDialog = true },
                    onDismissReceived = { viewModel.dismissHeirReceivedCard() },
                    onVerRecoveryCode = {
                        gateAccionSensible.launch {
                            val codigo = viewModel.getRecoveryCode()
                            if (codigo != null) codigoVisible = codigo
                            else codigoNoLegible = true
                        }
                    }
                )
            }

            uiState.pendingKeyConfirmations.forEach { pendiente ->
                HeirKeyConfirmationCard(
                    pending   = pendiente,
                    onConfirm = { viewModel.confirmHeirKey(pendiente.heirEmail) },
                    onReject  = { viewModel.removeHeirEmail(pendiente.heirEmail) }
                )
            }

            if (uiState.isEnabled && uiState.attachmentCount > 0) {
                InheritanceWarningCard(
                    message = stringResource(
                        R.string.inheritance_attachments_not_included,
                        uiState.attachmentCount
                    ),
                    justify = true
                )
            }

            if (uiState.wasDisarmedByDowngrade) {
                InheritanceDisarmedCard(
                    title    = stringResource(R.string.inheritance_disarmed_title),
                    body     = stringResource(R.string.inheritance_disarmed_body),
                    onDismiss = { viewModel.dismissDisarmedNotice() }
                )
            }

            if (uiState.wasDisarmedByAlreadyInherited) {
                InheritanceDisarmedCard(
                    title    = stringResource(R.string.inheritance_notif_already_inherited_title),
                    body     = stringResource(R.string.inheritance_notif_already_inherited_body),
                    onDismiss = { viewModel.dismissAlreadyInheritedNotice() }
                )
            }

            if (!uiState.isPremium) {
                PremiumGateBanner(onPlansClick = onPlansClick)
            }

            if (uiState.isDriveNotConnected) {
                if (uiState.isPremium) {
                    InheritanceWarningCard(message = stringResource(R.string.inheritance_warning_drive))
                } else if (!uiState.isGoogleIdentified) {
                    HeirSignInCard(onSignIn = {
                        coroutineScope.launch {
                            when (val result = requestGoogleAccountConnect(context, credentialManager)) {
                                is GoogleConnectResult.Success -> viewModel.handleSignInResult(result.idToken)
                                is GoogleConnectResult.Cancelled -> Unit
                                is GoogleConnectResult.NoCredential ->
                                    snackbarHostState.showSnackbar(msgAccountPickerUnavailable)
                                is GoogleConnectResult.Error -> Unit
                            }
                        }
                    })
                } else {
                    HeirIdentifiedCard()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {

                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector        = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        tint = if (uiState.isPremium) MaterialTheme.colorScheme.onPrimaryContainer
                               else                   MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.wrapContentWidth()) {
                        Text(
                            text       = stringResource(R.string.inheritance_toggle_title),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (!uiState.isPremium) {
                            Text(
                                text     = stringResource(R.string.inheritance_toggle_premium_hint),
                                style    = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.widthIn(max = 180.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    var activando by remember { mutableStateOf(false) }
                    Switch(
                        checked         = uiState.isEnabled || activando,
                        onCheckedChange = { turnedOn ->
                            if (!turnedOn) {
                                viewModel.setEnabled(false)
                            } else {
                                activando = true
                                coroutineScope.launch {
                                  try {
                                    val idTokenFb = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                                        getFirebaseIdentityTokenOrNull(context)?.idToken
                                    }
                                    val idToken = idTokenFb
                                        ?: when (
                                            val result = requestGoogleAccountConnect(
                                                context, credentialManager,
                                                filterByAuthorizedAccounts = true
                                            )
                                        ) {
                                            is GoogleConnectResult.Success -> result.idToken
                                            else                           -> null
                                        }
                                    viewModel.setEnabled(true, idToken)
                                  } finally {
                                    activando = false
                                  }
                                }
                            }
                        },
                        enabled         = !activando && (uiState.isEnabled ||
                            (uiState.isPremium && !uiState.isDriveNotConnected))
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (!uiState.isEnabled && uiState.isPremium && !uiState.isDriveNotConnected) {
                    Text(
                        text     = stringResource(R.string.inheritance_google_confirm_note),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                    )
                }

                if (uiState.isEnabled && uiState.isPremium) {

                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    )

                    if (uiState.pendingFirstSeal) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            InheritanceEmptyVaultCard(
                                body     = stringResource(R.string.inheritance_empty_vault_notice_body),
                                action   = stringResource(R.string.inheritance_empty_vault_notice_action),
                                onAction = { viewModel.sealNow() }
                            )
                        }
                    }

                    Text(
                        text       = stringResource(R.string.inheritance_threshold_title).uppercase(),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InheritancePrefs.INACTIVITY_OPTIONS.forEach { days ->
                            val selected = uiState.inactivityDays == days
                            ElevatedFilterChip(
                                selected = selected,
                                onClick  = { viewModel.setInactivityDays(days) },
                                label    = { Text(stringResource(R.string.inheritance_threshold_days_chip, days)) },
                                modifier = Modifier.weight(1f),
                                colors   = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                    Text(
                        text       = stringResource(R.string.inheritance_threshold_desc, uiState.inactivityDays),
                        style      = MaterialTheme.typography.bodySmall,
                        fontSize   = 13.sp,
                        lineHeight = 19.sp,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp)
                    )

                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    )

                    Text(
                        text       = stringResource(
                            R.string.inheritance_heirs_header,
                            uiState.heirEmails.size,
                            InheritancePrefs.MAX_HEIRS
                        ).uppercase(),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier   = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
                    )

                    uiState.heirEmails.forEachIndexed { index, email ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier  = Modifier.padding(start = 55.dp),
                                thickness = 0.5.dp,
                                color     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                            )
                        }
                        HeirRow(
                            email       = email,
                            isConfirmed = email in uiState.acceptedHeirEmails,
                            fingerprint = uiState.heirKeyFingerprints[email],
                            onRemove    = { heirToRemove = email }
                        )
                    }

                    if (uiState.heirEmails.size < InheritancePrefs.MAX_HEIRS) {
                        if (uiState.heirEmails.isNotEmpty()) {
                            HorizontalDivider(
                                modifier  = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                            )
                        }
                        HeirEmailForm(sigueTeclado = sigueTeclado, onSave = { email ->
                            gateAccionSensible.launch { viewModel.addHeirEmail(email) }
                        })
                    } else {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text     = stringResource(R.string.inheritance_heirs_limit, InheritancePrefs.MAX_HEIRS),
                                style    = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showResignDialog) {
        val ownerEmail = uiState.ownerEmailAsHeir ?: ""
        AlertDialog(
            onDismissRequest = { showResignDialog = false },
            title   = { Text(stringResource(R.string.inheritance_heir_resign_dialog_title)) },
            text    = { Text(stringResource(R.string.inheritance_heir_resign_dialog_body, ownerEmail), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = {
                        showResignDialog = false
                        gateAccionSensible.launch { viewModel.resignAsHeir() }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.inheritance_heir_resign_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResignDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    heirToRemove?.let { email ->
        AlertDialog(
            onDismissRequest = { heirToRemove = null },
            title   = { Text(stringResource(R.string.inheritance_remove_heir_title)) },
            text    = { Text(stringResource(R.string.inheritance_remove_heir_body, email), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = {
                        heirToRemove = null
                        gateAccionSensible.launch { viewModel.removeHeirEmail(email) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.inheritance_remove_heir_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { heirToRemove = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun HeirRoleCard(
    ownerEmail:             String,
    heirAcceptedAt:         Long,
    heirOfOwnerConfirmedAt: Long,
    heirInheritedAt:        Long,
    myKeyFingerprint:       String?,
    isLoading:              Boolean,
    onResign:               () -> Unit,
    onDismissReceived:      () -> Unit,
    onVerRecoveryCode:      () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor   = MaterialTheme.colorScheme.error
    val onSurface    = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val locale  = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val received  = heirInheritedAt > 0L
    val confirmed = heirOfOwnerConfirmedAt > 0L
    val dateStr = if (received) {
        formatDateLocalized(heirInheritedAt, locale)
    } else if (heirAcceptedAt > 0L) {
        formatDateLocalized(heirAcceptedAt, locale)
    } else ""


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(surfaceColor)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text       = stringResource(
                when {
                    received  -> R.string.inheritance_heir_received_title
                    confirmed -> R.string.inheritance_heir_role_title
                    else      -> R.string.inheritance_heir_role_pending_title
                }
            ),
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color      = primaryColor
        )

        Text(
            text  = ownerEmail,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = onSurface
        )

        Text(
            text      = when {
                            received  -> stringResource(R.string.inheritance_heir_received_body, dateStr, ownerEmail)
                            confirmed -> stringResource(R.string.inheritance_heir_role_body, dateStr)
                            else      -> stringResource(R.string.inheritance_heir_role_pending_body, dateStr)
                        },
            style     = MaterialTheme.typography.bodySmall.justified(),
            fontSize  = 13.sp,
            lineHeight = 19.sp,
            color     = onSurface.copy(alpha = 0.7f)
        )

        if (!received && !confirmed && myKeyFingerprint != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text  = stringResource(R.string.inheritance_heir_my_fingerprint_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text          = myKeyFingerprint,
                    style         = MaterialTheme.typography.titleMedium,
                    fontFamily    = FontFamily.Monospace,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color         = primaryColor
                )
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color     = onSurface.copy(alpha = 0.15f)
        )

        if (received) {
            Button(
                onClick  = onDismissReceived,
                modifier = Modifier.align(Alignment.End),
                colors   = appButtonColors()
            ) {
                Text(stringResource(R.string.inheritance_heir_received_dismiss_btn))
            }
        } else {
            if (confirmed) {
                AssistChip(
                    onClick  = onVerRecoveryCode,
                    modifier = Modifier.align(Alignment.End),
                    label    = {
                        Text(
                            text       = stringResource(R.string.heir_recovery_code_view_again),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.Transparent,
                        labelColor     = primaryColor
                    ),
                    border = BorderStroke(0.5.dp, primaryColor)
                )
            }

            Button(
                onClick  = onResign,
                enabled  = !isLoading,
                modifier = Modifier.align(Alignment.End),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = errorColor,
                    contentColor           = Color.White,
                    disabledContainerColor = errorColor.copy(alpha = 0.38f),
                    disabledContentColor   = Color.White.copy(alpha = 0.38f),
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(stringResource(R.string.inheritance_heir_resign_btn))
            }
        }
    }
}

@Composable
private fun HeirRow(
    email:       String,
    isConfirmed: Boolean = false,
    fingerprint: String? = null,
    onRemove:    () -> Unit,
) {
    val confirmedGreen = Color(0xFF4CAF50)
    val textColor = MaterialTheme.colorScheme.onPrimaryContainer
    val statusIconTint = if (isConfirmed) confirmedGreen
                         else             dangerColor()
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = Icons.Default.Person,
            contentDescription = null,
            tint     = statusIconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = email,
                style    = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color    = textColor
            )
            Text(
                text     = if (isConfirmed) stringResource(R.string.inheritance_heir_status_confirmed)
                           else             stringResource(R.string.inheritance_heir_status_pending),
                style    = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color    = if (isConfirmed) confirmedGreen
                           else             textColor.copy(alpha = 0.55f)
            )
            if (isConfirmed && fingerprint != null) {
                Text(
                    text       = stringResource(R.string.inheritance_heir_key_fingerprint, fingerprint),
                    style      = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp,
                    color      = textColor.copy(alpha = 0.55f)
                )
            }
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector        = Icons.Default.PersonRemove,
                contentDescription = stringResource(R.string.inheritance_remove_heir_cd),
                tint     = dangerColor(),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HeirSignInCard(onSignIn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.AccountCircle,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp).padding(top = 2.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text       = stringResource(R.string.inheritance_heir_signin_title),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text      = stringResource(R.string.inheritance_heir_signin_body),
                    style     = MaterialTheme.typography.bodySmall.justified(),
                    fontSize  = 13.sp,
                    color     = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
        }
        Button(
            onClick  = onSignIn,
            modifier = Modifier.align(Alignment.End),
            colors   = appButtonColors()
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.Login,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.inheritance_heir_signin_cta))
        }
    }
}

@Composable
private fun HeirIdentifiedCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = Icons.Default.CheckCircle,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier           = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Text(
            text  = stringResource(R.string.inheritance_heir_signin_done),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun HowItWorksStep(number: String, text: String, isLast: Boolean = false) {
    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier        = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = number,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text      = text,
                style     = MaterialTheme.typography.bodySmall,
                fontSize  = 13.sp,
                lineHeight = 19.sp,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier  = Modifier.weight(1f)
            )
        }
        if (!isLast) {
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun PremiumGateBanner(onPlansClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlansClick)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Lock,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier           = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(R.string.premium_gate_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text  = stringResource(R.string.premium_gate_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Text(
                text       = stringResource(R.string.premium_gate_cta),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun InheritanceWarningCard(message: String, justify: Boolean = false) {
    val warnColor = warningColor()
    Text(
        text  = message,
        style = if (justify) MaterialTheme.typography.bodySmall.justified() else MaterialTheme.typography.bodySmall,
        color = warnColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(warnColor.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    )
}

@Composable
private fun HeirKeyConfirmationCard(
    pending:   PendingHeirKeyConfirmation,
    onConfirm: () -> Unit,
    onReject:  () -> Unit
) {
    val warnColor = warningColor()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(warnColor.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text       = stringResource(
                    if (pending.previousFingerprint != null)
                        R.string.inheritance_heir_key_changed_title
                    else
                        R.string.inheritance_heir_key_confirm_title
                ),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = warnColor
            )
            Text(
                text  = stringResource(
                    R.string.inheritance_heir_key_confirm_body,
                    pending.heirEmail
                ),
                style = MaterialTheme.typography.bodySmall.justified(),
                color = warnColor
            )
            if (pending.previousFingerprint != null) {
                Text(
                    text  = stringResource(
                        R.string.inheritance_heir_key_confirm_previous,
                        pending.previousFingerprint
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = warnColor
                )
            }
            Text(
                text          = if (pending.previousFingerprint != null)
                                    stringResource(R.string.inheritance_heir_key_confirm_new_label, pending.fingerprint)
                                else
                                    pending.fingerprint,
                style         = MaterialTheme.typography.titleMedium,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.sp,
                color         = warnColor
            )
        }
        Row(
            modifier              = Modifier.align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onReject) {
                Text(
                    text  = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            AssistChip(
                onClick = onConfirm,
                label   = {
                    Text(
                        text       = stringResource(R.string.action_confirm),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.Transparent,
                    labelColor     = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun InheritanceDisarmedCard(title: String, body: String, onDismiss: () -> Unit) {
    val warnColor = warningColor()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(warnColor.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = warnColor,
                modifier           = Modifier.size(20.dp).padding(top = 2.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = warnColor
                )
                Text(
                    text  = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = warnColor
                )
            }
        }
        TextButton(
            onClick  = onDismiss,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text  = stringResource(R.string.action_understood),
                color = warnColor
            )
        }
    }
}

@Composable
private fun InheritanceEmptyVaultCard(body: String, action: String, onAction: () -> Unit) {
    val warnColor = warningColor()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(warnColor.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = warnColor,
                modifier           = Modifier.size(20.dp).padding(top = 2.dp)
            )
            Text(
                text  = body,
                style = MaterialTheme.typography.bodySmall,
                color = warnColor
            )
        }
        TextButton(
            onClick  = onAction,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = action, color = warnColor)
        }
    }
}

@Composable
private fun HeirEmailForm(sigueTeclado: SigueAlTeclado, onSave: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    val autoLockManager = LocalAutoLockManager.current
    val isValid = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val density   = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            delay(300L)
            bringIntoViewRequester.bringIntoView()
        }
    }

    fun trySave() {
        if (isValid) {
            onSave(email)
            email = ""
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value         = email,
            onValueChange = { autoLockManager?.recordInteraction(); email = it.trim() },
            label         = { Text(stringResource(R.string.inheritance_heir_email_label)) },
            placeholder   = { Text(stringResource(R.string.inheritance_heir_email_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            singleLine    = true,
            isError       = email.isNotBlank() && !isValid,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                autoCorrectEnabled = false,
                imeAction    = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { trySave() }),
            colors = OutlinedTextFieldDefaults.colors(
                errorBorderColor = dangerColor(),
                errorLabelColor  = dangerColor()
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick  = { trySave() },
            enabled  = isValid,
            modifier = Modifier
                .align(Alignment.End)
                .sigueAlTecladoObjetivo(sigueTeclado)
                .bringIntoViewRequester(bringIntoViewRequester)
                .coachMarkAnchor("digital_inheritance", "add_heir"), colors = appButtonColors()
        ) {
            Text(stringResource(R.string.inheritance_add_heir))
        }
    }
}
