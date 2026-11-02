@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.thormakk.keyarmor.presentation.screens.family

import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.interaction.MutableInteractionSource

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import com.thormakk.keyarmor.presentation.components.sigueAlTeclado
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoContenido
import com.thormakk.keyarmor.presentation.components.rememberSigueAlTeclado
import com.thormakk.keyarmor.presentation.components.sigueAlTecladoObjetivo
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Delete
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.data.prefs.FamilyPrefs
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.dangerColor
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.rememberMandatoryBiometricGate
import kotlinx.coroutines.delay
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun FamilyScreen(
    onBack:           () -> Unit,
    onNavigatePlans:  () -> Unit = {},
    viewModel:        FamilyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var inviteError by remember { mutableStateOf<String?>(null) }
    val inviteSentTemplate    = stringResource(R.string.family_admin_invite_sent)
    val memberRevokedMsg      = stringResource(R.string.family_member_revoked)
    val memberRevokedNamedFmt = stringResource(R.string.family_member_revoked_named)
    val memberPremiumRevokedMsg = stringResource(R.string.family_premium_revoked_snackbar)
    val memberLeftTemplate    = stringResource(R.string.family_member_left_notification)
    val memberDeclinedTemplate = stringResource(R.string.family_member_declined_notification)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FamilyEvent.InviteSent -> {
                    inviteError = null
                    snackbarHostState.showSnackbar(String.format(inviteSentTemplate, event.email))
                }
                is FamilyEvent.Error      ->
                    inviteError = event.message
                is FamilyEvent.MemberPremiumRevoked ->
                    snackbarHostState.showSnackbar(memberPremiumRevokedMsg)
                is FamilyEvent.MemberAccessRevoked ->
                    snackbarHostState.showSnackbar(
                        if (!event.adminEmail.isNullOrBlank())
                            String.format(memberRevokedNamedFmt, event.adminEmail)
                        else
                            memberRevokedMsg
                    )
                is FamilyEvent.MemberLeft ->
                    snackbarHostState.showSnackbar(String.format(memberLeftTemplate, event.email))
                is FamilyEvent.MemberDeclined ->
                    snackbarHostState.showSnackbar(String.format(memberDeclinedTemplate, event.email))
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.syncMemberStatusFromServer()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var revokeTarget by remember { mutableStateOf<String?>(null) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    var showSendConfirmDialog by remember { mutableStateOf(false) }
    val sendInviteGate = rememberMandatoryBiometricGate(
        verifyMasterPassword = viewModel::verifyMasterPassword
    )

    val bringButtonIntoView = remember { BringIntoViewRequester() }
    val sigueTeclado = rememberSigueAlTeclado()
    val scrollForm = androidx.compose.foundation.rememberScrollState()
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            delay(300L)
            bringButtonIntoView.bringIntoView()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            TopAppBar(
                actions = {
                    CaptureAllowedBadge()
                    ThemeTrialBadge()
                },
                title = { TextoDeUnaLinea(stringResource(R.string.family_admin_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                    color = sectionTitleColor()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                .sigueAlTeclado(scrollForm, sigueTeclado, margen = 8.dp)
                .verticalScroll(scrollForm)
                .sigueAlTecladoContenido(sigueTeclado)
                .padding(padding)
                .padding(horizontal = 7.dp, vertical = 8.dp)
        ) {

            if (uiState.isMember) {
                MemberRoleCard(
                    memberAdminEmail = uiState.memberAdminEmail,
                    isLoading        = uiState.isLoading,
                    onLeaveClick     = { showLeaveDialog = true }
                )
                Spacer(Modifier.height(24.dp))
            }

            if (!uiState.isAdminPlan) {
                AdminPaywallCard(onNavigatePlans = onNavigatePlans)
            } else {
                Box(Modifier.coachMarkAnchor("family_admin", "invite")) {
                    AdminPlanCard(
                        members             = uiState.members,
                        inviteEmail         = uiState.inviteEmail,
                        isLoading           = uiState.isLoading,
                        bringButtonIntoView = bringButtonIntoView,
                        sigueTeclado        = sigueTeclado,
                        onInviteEmailChange = { inviteError = null; viewModel.onInviteEmailChange(it) },
                        onSendClick         = { showSendConfirmDialog = true },
                        onRevokeClick       = { email -> revokeTarget = email }
                    )
                }
            }

            inviteError?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title   = { Text(stringResource(R.string.family_leave_plan_title)) },
            text    = { Text(stringResource(R.string.family_leave_plan_confirm_message), style = MaterialTheme.typography.bodyMedium.justified()) },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveDialog = false
                        viewModel.leaveFamilyPlan()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.family_leave_plan_confirm_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    revokeTarget?.let { email ->
        AlertDialog(
            onDismissRequest = { revokeTarget = null },
            title   = { Text(stringResource(R.string.family_admin_revoke_confirm_title)) },
            text    = { Text(stringResource(R.string.family_admin_revoke_confirm_body, email)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revokeMember(email)
                        revokeTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text(stringResource(R.string.family_admin_revoke_btn)) }
            },
            dismissButton = {
                TextButton(onClick = { revokeTarget = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    if (showSendConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSendConfirmDialog = false },
            title   = { Text(stringResource(R.string.family_send_invite_confirm_title)) },
            text    = {
                Text(stringResource(
                    R.string.family_send_invite_confirm_body,
                    uiState.inviteEmail.trim()), style = MaterialTheme.typography.bodyMedium.justified())
            },
            confirmButton = {
                Button(onClick = {
                    showSendConfirmDialog = false
                    sendInviteGate.launch { viewModel.sendInvite() }
                }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)) { Text(stringResource(R.string.action_continue), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showSendConfirmDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun FamilySectionHeader(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun MemberRoleCard(
    memberAdminEmail: String?,
    isLoading:        Boolean,
    onLeaveClick:      () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape   = MaterialTheme.shapes.large,
        colors  = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.Groups,
                    contentDescription = null,
                    modifier           = Modifier.size(28.dp),
                    tint               = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text       = stringResource(R.string.family_member_title),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (memberAdminEmail != null) {
                        Text(
                            text  = stringResource(R.string.family_member_managed_by, memberAdminEmail),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector        = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text      = stringResource(R.string.family_member_readonly_access_body),
                        style     = MaterialTheme.typography.bodyMedium.justified(),
                        color     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick  = onLeaveClick,
                        enabled  = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.family_leave_plan_title))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPlanCard(
    members:             List<FamilyPrefs.MemberInfo>,
    inviteEmail:         String,
    isLoading:           Boolean,
    bringButtonIntoView: BringIntoViewRequester,
    sigueTeclado:        com.thormakk.keyarmor.presentation.components.SigueAlTeclado,
    onInviteEmailChange: (String) -> Unit,
    onSendClick:         () -> Unit,
    onRevokeClick:       (String) -> Unit
) {
    val autoLockManager = LocalAutoLockManager.current
    Card(
        shape   = MaterialTheme.shapes.large,
        colors  = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.Groups,
                    contentDescription = null,
                    modifier           = Modifier.size(28.dp),
                    tint               = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = stringResource(R.string.family_admin_own_title),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            FamilySectionHeader(text = stringResource(R.string.family_admin_members_header))
            Spacer(Modifier.height(8.dp))

            if (members.isEmpty()) {
                Text(
                    text  = stringResource(R.string.family_admin_no_members),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                members.forEach { member ->
                    FamilyMemberRow(
                        member   = member,
                        onRevoke = { onRevokeClick(member.memberEmail) }
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            if (members.size < FamilyPrefs.MAX_MEMBERS) {
                FamilySectionHeader(text = stringResource(R.string.family_admin_invite_btn))
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value         = inviteEmail,
                    onValueChange = { autoLockManager?.recordInteraction(); onInviteEmailChange(it) },
                    label         = { Text(stringResource(R.string.family_admin_invite_hint)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        autoCorrectEnabled = false,
                        imeAction    = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSendClick() }
                    ),
                    singleLine = true,
                    modifier   = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick  = onSendClick,
                    enabled  = inviteEmail.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sigueAlTecladoObjetivo(sigueTeclado)
                        .bringIntoViewRequester(bringButtonIntoView), colors = appButtonColors()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.family_admin_invite_btn))
                    }
                }
            } else {
                Text(
                    text  = stringResource(R.string.family_admin_max_members),
                    style = MaterialTheme.typography.bodySmall,
                    color = dangerColor()
                )
            }
        }
    }
}

@Composable
private fun AdminPaywallCard(onNavigatePlans: () -> Unit) {
    Card(
        shape   = MaterialTheme.shapes.large,
        colors  = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = Icons.Default.Lock,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text       = stringResource(R.string.family_admin_title),
                style      = MaterialTheme.typography.titleLarge.justified(),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text      = stringResource(R.string.family_admin_paywall_body),
                style     = MaterialTheme.typography.bodyMedium.justified(),
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick  = onNavigatePlans,
                modifier = Modifier.fillMaxWidth(), colors = appButtonColors()
            ) {
                Text(stringResource(R.string.premium_gate_cta))
            }
        }
    }
}

@Composable
private fun FamilyMemberRow(
    member:   FamilyPrefs.MemberInfo,
    onRevoke: () -> Unit
) {
    val statusRes = when (member.status) {
        "active"  -> R.string.family_admin_status_active
        "pending" -> R.string.family_admin_status_pending
        else      -> R.string.family_admin_status_revoked
    }
    val ribbonColor = when (member.status) {
        "active"  -> Color(0xFF16A34A)
        "pending" -> Color(0xFFD97706)
        else      -> Color.Transparent
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box {
            if (member.status != "revoked") {
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
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.Groups,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text       = member.memberEmail,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = stringResource(statusRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (member.status) {
                        "active"  -> MaterialTheme.colorScheme.primary
                        "pending" -> MaterialTheme.colorScheme.onSurfaceVariant
                        else      -> dangerColor()
                    }
                )
            }
            if (member.status != "revoked") {
                IconButton(onClick = onRevoke) {
                    Icon(
                        imageVector        = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.family_admin_revoke_btn),
                        tint               = dangerColor()
                    )
                }
            }
        }
        }
    }
}
