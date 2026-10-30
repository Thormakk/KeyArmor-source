@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.panicpin

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.data.prefs.PanicMode
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.components.warningColor
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun PanicPinScreen(
    onBack:       () -> Unit,
    onPlansClick: () -> Unit = {},
    viewModel:    PanicPinViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val isFreeUser by viewModel.isFreeUser.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val savedMessage = stringResource(R.string.panic_pin_saved_success)
    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.onSavedSuccessConsumed()
        }
    }

    if (uiState.showDisableDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDisable,
            containerColor   = MaterialTheme.colorScheme.background,
            icon = {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    tint               = warningColor()
                )
            },
            title = { Text(stringResource(R.string.panic_pin_disable_title)) },
            text  = { Text(stringResource(R.string.panic_pin_disable_message)) },
            confirmButton = {
                Button(
                    onClick = viewModel::onConfirmDisable,
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.panic_pin_disable_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDisable) {
                    Text(
                        stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
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
                            text       = stringResource(R.string.panic_pin_title),
                            style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                            fontWeight = FontWeight.SemiBold,
                            color      = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (uiState.step != PanicPinStep.IDLE) viewModel.onCancelSetup()
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
            label = "PanicPinStep"
        ) { step ->
            when (step) {
                PanicPinStep.IDLE        -> IdleContent(
                    modifier      = Modifier.padding(padding),
                    isEnabled     = uiState.isEnabled,
                    panicMode     = uiState.panicMode,
                    isFreeUser    = isFreeUser,
                    isGeneratingDecoyContent = uiState.isGeneratingDecoyContent,
                    onModeChange  = viewModel::onModeChange,
                    onSetup       = viewModel::onStartSetup,
                    onDisable     = viewModel::onRequestDisable,
                    onPlansClick  = onPlansClick
                )
                PanicPinStep.ENTER_PIN   -> PinEntryContent(
                    modifier    = Modifier.padding(padding),
                    title       = stringResource(R.string.panic_pin_enter_title),
                    subtitle    = stringResource(R.string.panic_pin_enter_subtitle, PANIC_PIN_LENGTH),
                    pin         = uiState.pin,
                    pinLength   = PANIC_PIN_LENGTH,
                    error       = uiState.pinError,
                    onDigit     = viewModel::onDigitEnter,
                    onBackspace = viewModel::onBackspace
                )
                PanicPinStep.CONFIRM_PIN -> PinEntryContent(
                    modifier    = Modifier.padding(padding),
                    title       = stringResource(R.string.panic_pin_confirm_title),
                    subtitle    = stringResource(R.string.panic_pin_confirm_subtitle),
                    pin         = uiState.confirmPin,
                    pinLength   = PANIC_PIN_LENGTH,
                    error       = uiState.pinError,
                    onDigit     = viewModel::onDigitEnter,
                    onBackspace = viewModel::onBackspace,
                    onRetry     = viewModel::onRestartSetup
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    modifier     : Modifier,
    isEnabled    : Boolean,
    panicMode    : PanicMode,
    isFreeUser   : Boolean = false,
    isGeneratingDecoyContent: Boolean = false,
    onModeChange : (PanicMode) -> Unit,
    onSetup      : () -> Unit,
    onDisable    : () -> Unit,
    onPlansClick : () -> Unit = {}
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
                        imageVector        = Icons.Default.Lock,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text       = stringResource(R.string.panic_pin_what_is_title),
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
                    Text(
                        text      = stringResource(R.string.panic_pin_what_is_body, PANIC_PIN_LENGTH),
                        style     = MaterialTheme.typography.bodySmall.justified(),
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier  = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }
            }
        }

        if (isFreeUser) {
            PanicPinPremiumBanner(onPlansClick = onPlansClick)
        }

        Text(
            text       = stringResource(R.string.panic_pin_mode_section_title),
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.primary,
            modifier   = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )

        Box(Modifier.coachMarkAnchor("panic_pin", "mode")) {
            PanicModeCard(
                icon        = Icons.Default.VisibilityOff,
                title       = stringResource(R.string.panic_pin_mode_hide_title),
                description = stringResource(R.string.panic_pin_mode_hide_body),
                selected    = panicMode == PanicMode.HIDE,
                enabled     = !isFreeUser,
                onClick     = { if (!isFreeUser) onModeChange(PanicMode.HIDE) }
            )
        }

        PanicModeCard(
            icon        = Icons.Default.Delete,
            title       = stringResource(R.string.panic_pin_mode_wipe_title),
            description = stringResource(R.string.panic_pin_mode_wipe_body),
            selected    = panicMode == PanicMode.WIPE,
            enabled     = !isFreeUser,
            danger      = true,
            onClick     = { if (!isFreeUser) onModeChange(PanicMode.WIPE) }
        )

        PanicModeCard(
            icon        = Icons.Default.TheaterComedy,
            title       = stringResource(R.string.panic_pin_mode_decoy_title),
            description = stringResource(R.string.panic_pin_mode_decoy_body),
            selected    = panicMode == PanicMode.DECOY,
            enabled     = !isFreeUser,
            onClick     = { if (!isFreeUser) onModeChange(PanicMode.DECOY) }
        )

        if (isGeneratingDecoyContent) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text  = stringResource(R.string.panic_pin_decoy_generating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                    text       = stringResource(R.string.panic_pin_status_label),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text  = if (isEnabled)
                        stringResource(R.string.panic_pin_status_active)
                    else
                        stringResource(R.string.panic_pin_status_inactive),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEnabled) Color(0xFF43A047)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked         = isEnabled,
                onCheckedChange = { checked ->
                    if (!isFreeUser) { if (checked) onSetup() else onDisable() }
                },
                enabled = !isFreeUser,
                colors  = SwitchDefaults.colors(
                    checkedThumbColor       = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor       = Color(0xFF43A047),
                    uncheckedThumbColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor     = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        if (isEnabled) {
            TextButton(
                onClick  = onSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = stringResource(R.string.panic_pin_change_button),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(
                onClick  = onSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = stringResource(R.string.panic_pin_reset_button),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PanicModeCard(
    icon        : ImageVector,
    title       : String,
    description : String,
    selected    : Boolean,
    enabled     : Boolean = true,
    danger      : Boolean = false,
    onClick     : () -> Unit
) {
    val borderColor = when {
        danger   -> MaterialTheme.colorScheme.onError.copy(alpha = if (selected && enabled) 1f else 0.5f)
        !enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        selected -> MaterialTheme.colorScheme.primary
        else     -> MaterialTheme.colorScheme.outlineVariant
    }
    val bgColor = when {
        danger   -> MaterialTheme.colorScheme.error.copy(alpha = if (enabled) 1f else 0.5f)
        !enabled -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        else     -> MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = if (selected && enabled) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = when {
                danger              -> MaterialTheme.colorScheme.onError
                selected && enabled -> MaterialTheme.colorScheme.primary
                else                -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.5f)
            },
            modifier           = Modifier
                .size(22.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = when {
                    danger   -> MaterialTheme.colorScheme.onError
                    selected -> MaterialTheme.colorScheme.primary
                    else     -> MaterialTheme.colorScheme.onBackground
                }
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text      = description,
                style     = MaterialTheme.typography.bodySmall.justified(),
                color     = if (danger) MaterialTheme.colorScheme.onError
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        RadioButton(
            selected = selected,
            onClick  = onClick,
            colors   = if (danger)
                RadioButtonDefaults.colors(
                    selectedColor   = MaterialTheme.colorScheme.onError,
                    unselectedColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.7f)
                )
            else
                RadioButtonDefaults.colors(
                    selectedColor   = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.outlineVariant
                )
        )
    }
}

@Suppress("SameParameterValue")
@Composable
private fun PinEntryContent(
    modifier    : Modifier,
    title       : String,
    subtitle    : String,
    pin         : String,
    pinLength   : Int,
    error       : String?,
    onDigit     : (Char) -> Unit,
    onBackspace : () -> Unit,
    onRetry     : (() -> Unit)? = null
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text       = title,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onBackground,
            textAlign  = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text      = subtitle,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(pinLength) { index ->
                val filled = index < pin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .then(
                            if (filled) Modifier.background(MaterialTheme.colorScheme.primary)
                            else Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                )
            }
        }

        AnimatedVisibility(visible = error != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text      = error ?: "",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = warningColor(),
                    textAlign = TextAlign.Center
                )
                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = onRetry) {
                        Text(
                            text  = stringResource(R.string.panic_pin_retry),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        NumericKeypad(
            onDigit     = onDigit,
            onBackspace = onBackspace,
            enabled     = pin.length < pinLength
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun NumericKeypad(
    onDigit     : (Char) -> Unit,
    onBackspace : () -> Unit,
    enabled     : Boolean
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { digit ->
                    KeypadButton(
                        label   = digit.toString(),
                        onClick = { onDigit(digit) },
                        enabled = enabled
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(modifier = Modifier.size(72.dp))

            KeypadButton(
                label   = "0",
                onClick = { onDigit('0') },
                enabled = enabled
            )

            Box(
                modifier         = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackspace),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.action_delete),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label   : String,
    onClick : () -> Unit,
    enabled : Boolean = true
) {
    Box(
        modifier         = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 26.sp,
            fontWeight = FontWeight.Medium,
            color      = if (enabled) MaterialTheme.colorScheme.onBackground
                         else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PanicPinPremiumBanner(onPlansClick: () -> Unit) {
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
