@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.generator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.ClipboardUtil
import com.thormakk.keyarmor.presentation.util.LocalClipboardClearManager
import com.thormakk.keyarmor.ui.theme.Cream300
import com.thormakk.keyarmor.ui.theme.MonoFontFamily
import com.thormakk.keyarmor.ui.theme.PasswordStrengthFair
import com.thormakk.keyarmor.ui.theme.PasswordStrengthGood
import com.thormakk.keyarmor.ui.theme.PasswordStrengthStrong
import com.thormakk.keyarmor.ui.theme.PasswordStrengthWeak
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
private fun GeneratorMode.displayLabel(): String = when (this) {
    GeneratorMode.STANDARD      -> stringResource(R.string.generator_mode_standard)
    GeneratorMode.PRONOUNCEABLE -> stringResource(R.string.generator_mode_pronounceable)
    GeneratorMode.PASSPHRASE    -> stringResource(R.string.generator_mode_passphrase)
}

@Composable
private fun passwordStrengthLabel(strength: Int): String = when (strength) {
    0    -> stringResource(R.string.password_strength_very_short)
    1    -> stringResource(R.string.password_strength_weak)
    2    -> stringResource(R.string.password_strength_fair)
    3    -> stringResource(R.string.password_strength_good)
    4    -> stringResource(R.string.password_strength_strong)
    else -> ""
}

@Composable
fun PasswordGeneratorScreen(
    fromEntry: Boolean,
    onBack: () -> Unit,
    onUsePassword: (String) -> Unit,
    viewModel: PasswordGeneratorViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { TextoDeUnaLinea(stringResource(R.string.generator_title),
                    style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                    fontWeight = FontWeight.Bold,
                    color      = sectionTitleColor()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = topBarColors(MaterialTheme.colorScheme.background)
            )
        },
        containerColor      = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            PasswordGeneratorContent(
                viewModel     = viewModel,
                fromEntry     = fromEntry,
                onUsePassword = onUsePassword
            )
        }
    }
}

@Composable
fun PasswordGeneratorBottomSheet(
    onDismiss:     () -> Unit,
    onUsePassword: (String) -> Unit,
    viewModel:     PasswordGeneratorViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        PasswordGeneratorContent(
            viewModel     = viewModel,
            fromEntry     = true,
            onUsePassword = onUsePassword,
            bottomSheet   = true
        )
    }
}

@Composable
internal fun PasswordGeneratorContent(
    viewModel:   PasswordGeneratorViewModel,
    fromEntry:   Boolean,
    onUsePassword: (String) -> Unit,
    bottomSheet: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipClear = LocalClipboardClearManager.current
    val clipContext = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .then(if (bottomSheet) Modifier.navigationBarsPadding() else Modifier)
            .padding(horizontal = 7.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                val letterColor = MaterialTheme.colorScheme.onSurface
                val digitColor  = MaterialTheme.colorScheme.primary
                Text(
                    text      = colorizePassword(uiState.password, letterColor, digitColor),
                    style     = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontFamily = MonoFontFamily),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val strengthColor = when (uiState.strength) {
                    0    -> PasswordStrengthWeak
                    1    -> PasswordStrengthWeak
                    2    -> PasswordStrengthFair
                    3    -> PasswordStrengthGood
                    4    -> PasswordStrengthStrong
                    else -> Color.Transparent
                }
                Row(
                    modifier              = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(
                                    if (i < uiState.strength.coerceAtLeast(1)) strengthColor else Cream300,
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
                val strengthLabel = passwordStrengthLabel(uiState.strength)
                if (strengthLabel.isNotEmpty()) {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text  = strengthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = strengthColor
                    )
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier
                .fillMaxWidth()
                .coachMarkAnchor("generator", "mode")
            ) {
                GeneratorMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = uiState.mode == mode,
                        onClick  = { viewModel.onModeChange(mode) },
                        shape    = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = GeneratorMode.entries.size
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor   = Color.Transparent,
                            activeContentColor     = MaterialTheme.colorScheme.primary,
                            activeBorderColor      = MaterialTheme.colorScheme.primary,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            inactiveContentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                            inactiveBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text  = mode.displayLabel(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (uiState.mode == mode) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            when (uiState.mode) {

                GeneratorMode.STANDARD, GeneratorMode.PRONOUNCEABLE -> {

                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = stringResource(R.string.generator_length),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text       = "${uiState.length}",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick  = { if (uiState.length > 6) viewModel.onLengthChange(uiState.length - 1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("−", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value         = uiState.length.toFloat(),
                            onValueChange = { viewModel.onLengthChange(it.toInt()) },
                            valueRange    = 6f..32f,
                            steps         = 25,
                            modifier      = Modifier.weight(1f).height(20.dp),
                            colors        = SliderDefaults.colors(
                                thumbColor                = MaterialTheme.colorScheme.primary,
                                activeTrackColor          = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor        = MaterialTheme.colorScheme.surfaceVariant,
                                activeTickColor           = Color.Transparent,
                                inactiveTickColor         = Color.Transparent
                            ),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(50)
                                        )
                                )
                            },
                            track = { sliderState ->
                                SliderDefaults.Track(
                                    sliderState  = sliderState,
                                    modifier     = Modifier.height(2.dp),
                                    colors       = SliderDefaults.colors(
                                        activeTrackColor   = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        activeTickColor    = Color.Transparent,
                                        inactiveTickColor  = Color.Transparent
                                    )
                                )
                            }
                        )
                        IconButton(
                            onClick  = { if (uiState.length < 32) viewModel.onLengthChange(uiState.length + 1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 36.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("6",  style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("32", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (uiState.mode == GeneratorMode.STANDARD) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CharTypeChip(
                                symbol   = "Aa",
                                label    = stringResource(R.string.generator_uppercase),
                                selected = uiState.useUpper,
                                onClick  = { viewModel.onUpperToggle(!uiState.useUpper) },
                                modifier = Modifier.weight(1f)
                            )
                            CharTypeChip(
                                symbol   = "aa",
                                label    = stringResource(R.string.generator_lowercase),
                                selected = uiState.useLower,
                                onClick  = { viewModel.onLowerToggle(!uiState.useLower) },
                                modifier = Modifier.weight(1f)
                            )
                            CharTypeChip(
                                symbol   = "123",
                                label    = stringResource(R.string.generator_numbers),
                                selected = uiState.useDigits,
                                onClick  = { viewModel.onDigitsToggle(!uiState.useDigits) },
                                modifier = Modifier.weight(1f)
                            )
                            CharTypeChip(
                                symbol   = "!@#",
                                label    = stringResource(R.string.generator_symbols),
                                selected = uiState.useSymbols,
                                onClick  = { viewModel.onSymbolsToggle(!uiState.useSymbols) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CharTypeChip(
                                symbol   = "Aa",
                                label    = stringResource(R.string.generator_uppercase),
                                selected = uiState.useUpper,
                                onClick  = { viewModel.onUpperToggle(!uiState.useUpper) },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.weight(2f))
                        }

                        if (uiState.useUpper) {
                            val maxUpper = (uiState.length / 2).coerceAtLeast(1)
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text     = stringResource(R.string.generator_upper_count_label),
                                    style    = MaterialTheme.typography.bodyMedium,
                                    color    = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick  = { viewModel.onUpperCountChange(uiState.upperCount - 1) },
                                    enabled  = uiState.upperCount > 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("−", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text       = "${uiState.upperCount}",
                                    style      = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.primary,
                                    modifier   = Modifier.width(28.dp),
                                    textAlign  = TextAlign.Center
                                )
                                IconButton(
                                    onClick  = { viewModel.onUpperCountChange(uiState.upperCount + 1) },
                                    enabled  = uiState.upperCount < maxUpper,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("+", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (uiState.mode == GeneratorMode.STANDARD &&
                        (uiState.useUpper || uiState.useDigits || uiState.useSymbols)) {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            if (uiState.useUpper) {
                                MinCountRow(
                                    label        = stringResource(R.string.generator_count_uppercase_label),
                                    count        = uiState.countUpper,
                                    canDecrement = uiState.countUpper > 1,
                                    canIncrement = uiState.countUpper < uiState.maxCountUpper(),
                                    onDecrement  = { viewModel.onCountUpperChange(uiState.countUpper - 1) },
                                    onIncrement  = { viewModel.onCountUpperChange(uiState.countUpper + 1) }
                                )
                            }
                            if (uiState.useDigits) {
                                MinCountRow(
                                    label        = stringResource(R.string.generator_count_digits_label),
                                    count        = uiState.countDigits,
                                    canDecrement = uiState.countDigits > 1,
                                    canIncrement = uiState.countDigits < uiState.maxCountDigits(),
                                    onDecrement  = { viewModel.onCountDigitsChange(uiState.countDigits - 1) },
                                    onIncrement  = { viewModel.onCountDigitsChange(uiState.countDigits + 1) }
                                )
                            }
                            if (uiState.useSymbols) {
                                MinCountRow(
                                    label        = stringResource(R.string.generator_count_symbols_label),
                                    count        = uiState.countSymbols,
                                    canDecrement = uiState.countSymbols > 1,
                                    canIncrement = uiState.countSymbols < uiState.maxCountSymbols(),
                                    onDecrement  = { viewModel.onCountSymbolsChange(uiState.countSymbols - 1) },
                                    onIncrement  = { viewModel.onCountSymbolsChange(uiState.countSymbols + 1) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Cream300)

                    ToggleOptionRow(
                        title    = stringResource(R.string.generator_exclude_ambiguous),
                        subtitle = stringResource(R.string.generator_exclude_ambiguous_subtitle),
                        checked  = uiState.excludeAmbiguous,
                        onToggle = { viewModel.onExcludeAmbiguousToggle(it) }
                    )
                }

                GeneratorMode.PASSPHRASE -> {

                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = stringResource(R.string.generator_words_label),
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick  = { viewModel.onWordCountChange(uiState.wordCount - 1) },
                            enabled  = uiState.wordCount > 2,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("−", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text       = "${uiState.wordCount}",
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary,
                            modifier   = Modifier.width(28.dp),
                            textAlign  = TextAlign.Center
                        )
                        IconButton(
                            onClick  = { viewModel.onWordCountChange(uiState.wordCount + 1) },
                            enabled  = uiState.wordCount < 8,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("+", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text  = stringResource(R.string.generator_word_separator_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val hyphenDashLabel      = stringResource(R.string.generator_separator_hyphen_dash)
                        val hyphenUnderscoreLabel = stringResource(R.string.generator_separator_hyphen_underscore)
                        val dotLabel             = stringResource(R.string.generator_separator_dot)
                        val spaceLabel           = stringResource(R.string.generator_separator_space)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "-"  to hyphenDashLabel,
                                "_"  to hyphenUnderscoreLabel,
                                "."  to dotLabel,
                                " "  to spaceLabel
                            ).forEach { (sep, label) ->
                                val selected = uiState.separator == sep
                                FilterChip(
                                    selected = selected,
                                    onClick  = { viewModel.onSeparatorChange(sep) },
                                    label    = {
                                        Text(
                                            text      = label,
                                            style     = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color.Transparent,
                                        selectedLabelColor     = MaterialTheme.colorScheme.primary,
                                        labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = if (selected)
                                        BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                                    else
                                        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Cream300)

                    ToggleOptionRow(
                        title    = stringResource(R.string.generator_capitalize_words_title),
                        subtitle = stringResource(R.string.generator_capitalize_words_subtitle),
                        checked  = uiState.capitalize,
                        onToggle = { viewModel.onCapitalizeToggle(it) }
                    )
                }
            }

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .coachMarkAnchor("generator", "actions"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick  = { viewModel.regenerate() },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.generator_regenerate),
                        fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick  = {
                        if (fromEntry) onUsePassword(uiState.password)
                        else {
                            ClipboardUtil.copySensitive(clipContext, uiState.password)
                            clipClear.scheduleClear()
                            copied = true
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape    = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor   = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        if (fromEntry) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = if (fromEntry) stringResource(R.string.action_use_password)
                                     else if (copied) stringResource(R.string.action_copied) else stringResource(R.string.action_copy),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

private val SymbolAmber = Color(0xFFE88C30)

private fun colorizePassword(
    password:    String,
    letterColor: Color,
    digitColor:  Color
): AnnotatedString = buildAnnotatedString {
    password.forEach { char ->
        val color = when {
            char.isLetter() -> letterColor
            char.isDigit()  -> digitColor
            else            -> SymbolAmber
        }
        withStyle(SpanStyle(color = color)) { append(char) }
    }
}

@Composable
private fun CharTypeChip(
    symbol:   String,
    label:    String,
    selected: Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape        = RoundedCornerShape(8.dp),
        color        = if (selected) Color.Transparent
                       else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
        border       = if (selected)
                           BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                       else null,
        modifier     = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier         = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text       = symbol,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines  = 1
            )
        }
    }
}

@Composable
private fun MinCountRow(
    label:        String,
    count:        Int,
    canDecrement: Boolean,
    canIncrement: Boolean,
    onDecrement:  () -> Unit,
    onIncrement:  () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick  = onDecrement,
            enabled  = canDecrement,
            modifier = Modifier.size(36.dp)
        ) {
            Text(
                text       = "−",
                fontSize   = 20.sp,
                color      = if (canDecrement) MaterialTheme.colorScheme.primary
                             else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text       = "$count",
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary,
            modifier   = Modifier.width(28.dp),
            textAlign  = TextAlign.Center
        )
        IconButton(
            onClick  = onIncrement,
            enabled  = canIncrement,
            modifier = Modifier.size(36.dp)
        ) {
            Text(
                text       = "+",
                fontSize   = 20.sp,
                color      = if (canIncrement) MaterialTheme.colorScheme.primary
                             else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ToggleOptionRow(
    title:    String,
    subtitle: String,
    checked:  Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(
                checkedThumbColor  = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor  = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor  = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )
    }
}