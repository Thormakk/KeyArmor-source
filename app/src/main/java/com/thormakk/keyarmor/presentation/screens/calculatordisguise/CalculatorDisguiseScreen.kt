@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.calculatordisguise

import com.thormakk.keyarmor.presentation.components.justified
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.presentation.screens.calculator.CalculatorDisplay
import com.thormakk.keyarmor.presentation.screens.calculator.CalculatorKeypad
import com.thormakk.keyarmor.presentation.screens.calculator.CalculatorState
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.util.rememberMandatoryBiometricGate
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun CalculatorDisguiseScreen(
    onBack:       () -> Unit,
    onPlansClick: () -> Unit = {},
    viewModel:    CalculatorDisguiseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFreeUser by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val securityGate = rememberMandatoryBiometricGate(
        verifyMasterPassword = viewModel::verifyMasterPassword
    )

    val savedMessage = stringResource(R.string.calculator_disguise_saved_success)
    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.onSavedSuccessConsumed()
        }
    }

    Scaffold(
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
                        TextoDeUnaLinea(
                            text       = stringResource(R.string.calculator_disguise_title),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (uiState.step != CalculatorDisguiseStep.IDLE) viewModel.onCancelStep()
                            else onBack()
                        }) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint               = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = topBarColors()
                )
            }
        },
        snackbarHost   = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        AnimatedContent(
            targetState    = uiState.step,
            transitionSpec = {
                (slideInHorizontally(tween(280)) { it / 4 } + fadeIn(tween(280))) togetherWith
                (slideOutHorizontally(tween(200)) { -it / 4 } + fadeOut(tween(200)))
            },
            label = "CalculatorDisguiseStep"
        ) { step ->
            when (step) {
                CalculatorDisguiseStep.IDLE -> IdleContent(
                    modifier               = Modifier.padding(padding),
                    isEnabled              = uiState.isEnabled,
                    hasSequenceConfigured  = uiState.hasSequenceConfigured,
                    isFreeUser             = isFreeUser,
                    onPlansClick           = onPlansClick,
                    onToggle               = { checked ->
                        if (checked) viewModel.onToggle(true)
                        else securityGate.launch { viewModel.onToggle(false) }
                    },
                    onChangeSequence       = { securityGate.launch { viewModel.onChangeSequence() } }
                )
                CalculatorDisguiseStep.RECORD_SEQUENCE -> RecordContent(
                    modifier  = Modifier.padding(padding),
                    title     = stringResource(R.string.calculator_disguise_record_title),
                    subtitle  = stringResource(R.string.calculator_disguise_record_subtitle),
                    calcState = uiState.calcState,
                    error     = null,
                    viewModel = viewModel,
                    onRestart = null
                )
                CalculatorDisguiseStep.CONFIRM_SEQUENCE -> RecordContent(
                    modifier  = Modifier.padding(padding),
                    title     = stringResource(R.string.calculator_disguise_confirm_title),
                    subtitle  = stringResource(R.string.calculator_disguise_confirm_subtitle),
                    calcState = uiState.calcState,
                    error     = uiState.recordError?.asString(),
                    viewModel = viewModel,
                    onRestart = viewModel::onStartRecording
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    modifier              : Modifier,
    isEnabled             : Boolean,
    hasSequenceConfigured : Boolean,
    isFreeUser            : Boolean,
    onPlansClick          : () -> Unit,
    onToggle              : (Boolean) -> Unit,
    onChangeSequence      : () -> Unit
) {
    var explanationExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 7.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Card(
            shape  = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = { explanationExpanded = !explanationExpanded }
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Default.Calculate,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text       = stringResource(R.string.calculator_disguise_what_is_title),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground,
                        modifier   = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector        = if (explanationExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = explanationExpanded,
                    enter   = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit    = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                        Text(
                            text      = stringResource(R.string.calculator_disguise_what_is_body),
                            style     = MaterialTheme.typography.bodySmall.justified(),
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector        = Icons.Default.Warning,
                                contentDescription = null,
                                tint               = warningColor(),
                                modifier           = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text      = stringResource(R.string.calculator_disguise_lockout_warning),
                                style     = MaterialTheme.typography.bodySmall.justified(),
                                fontWeight = FontWeight.Medium,
                                color     = warningColor()
                            )
                        }
                    }
                }
            }
        }

        Card(
            shape  = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier          = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector        = Icons.Default.Info,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text       = stringResource(R.string.calculator_disguise_limitation_title),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text      = stringResource(R.string.calculator_disguise_limitation_body),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        if (isFreeUser) {
            PremiumGateBanner(onPlansClick = onPlansClick)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(R.string.calculator_disguise_status_label),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text  = if (isEnabled)
                        stringResource(R.string.calculator_disguise_status_active)
                    else
                        stringResource(R.string.calculator_disguise_status_inactive),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEnabled) Color(0xFF43A047)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked         = isEnabled,
                onCheckedChange = onToggle,
                enabled         = !isFreeUser,
                modifier        = Modifier.coachMarkAnchor("calculator_disguise", "switch"),
                colors          = SwitchDefaults.colors(
                    checkedThumbColor   = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor   = Color(0xFF43A047),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        if (hasSequenceConfigured) {
            OutlinedButton(
                onClick        = onChangeSequence,
                enabled        = !isFreeUser,
                modifier       = Modifier
                    .align(Alignment.CenterHorizontally)
                    .coachMarkAnchor("calculator_disguise", "code"),
                shape          = RoundedCornerShape(50),
                colors         = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                border         = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text       = stringResource(R.string.calculator_disguise_change_button),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RecordContent(
    modifier  : Modifier,
    title     : String,
    subtitle  : String,
    calcState : CalculatorState,
    error     : String?,
    viewModel : CalculatorDisguiseViewModel,
    onRestart : (() -> Unit)?
) {
    Column(
        modifier            = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = error != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text      = error ?: "",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = warningColor(),
                        textAlign = TextAlign.Center
                    )
                    if (onRestart != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = onRestart) {
                            Text(
                                text  = stringResource(R.string.calculator_disguise_restart),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1C1C1E))
        ) {
            CalculatorDisplay(
                state    = calcState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                compact  = true
            )
            CalculatorKeypad(
                onDigit     = viewModel::onDigit,
                onDecimal   = viewModel::onDecimal,
                onOperator  = viewModel::onOperator,
                onPercent   = viewModel::onPercent,
                onNegate    = viewModel::onNegate,
                onClear     = viewModel::onClearEntry,
                onEquals    = viewModel::onConfirmStep,
                onBackspace = viewModel::onBackspace,
                modifier    = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                compact     = true
            )
        }

        Text(
            text      = stringResource(R.string.calculator_disguise_preview_hint),
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(16.dp)
        )
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
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
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
