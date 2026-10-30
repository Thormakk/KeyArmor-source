package com.thormakk.keyarmor.presentation.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R

private val CalcBackground     = Color(0xFF000000)
private val CalcDisplayText    = Color(0xFFFFFFFF)
private val CalcPreviewText    = Color(0xFF6E6E73)
private val CalcHeaderText     = Color(0xFFAEAEB2)
private val CalcDigitBg        = Color(0xFF333333)
private val CalcDigitText      = Color(0xFFFFFFFF)
private val CalcFunctionBg     = Color(0xFFA5A5A5)
private val CalcFunctionText   = Color(0xFF1C1C1E)
private val CalcOperatorBg     = Color(0xFFFF9F0A)
private val CalcOperatorText   = Color(0xFFFFFFFF)

@Composable
fun CalculatorScreen(
    onUnlockTriggered: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.unlockEvent.collect { onUnlockTriggered() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalcBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Text(
            text       = stringResource(R.string.calculator_disguise_header_label),
            color      = CalcHeaderText,
            fontSize   = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(start = 24.dp, top = 32.dp)
        )
        CalculatorDisplay(
            state    = state,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        CalculatorKeypad(
            onDigit     = viewModel::onDigit,
            onDecimal   = viewModel::onDecimal,
            onOperator  = viewModel::onOperator,
            onPercent   = viewModel::onPercent,
            onNegate    = viewModel::onNegate,
            onClear     = viewModel::onClear,
            onEquals    = viewModel::onEquals,
            onBackspace = viewModel::onBackspace,
            modifier   = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 44.dp)
        )
    }
}

@Composable
fun CalculatorDisplay(
    state: CalculatorState,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (state.expressionPreview.isNotEmpty()) {
            Text(
                text       = state.expressionPreview,
                color      = CalcPreviewText,
                fontSize   = if (compact) 18.sp else 24.sp,
                fontWeight = FontWeight.Normal,
                maxLines   = 1,
                textAlign  = TextAlign.End
            )
        }
        Text(
            text       = state.display,
            color      = if (state.isError) CalcOperatorBg else CalcDisplayText,
            fontSize   = displayFontSize(state.display.length, compact),
            fontWeight = FontWeight.Light,
            maxLines   = 1,
            textAlign  = TextAlign.End,
            modifier   = Modifier.padding(top = 4.dp, bottom = if (compact) 14.dp else 40.dp)
        )
    }
}

private fun displayFontSize(length: Int, compact: Boolean = false): TextUnit {
    val (big, medium, small) = if (compact) Triple(54.sp, 40.sp, 30.sp) else Triple(72.sp, 52.sp, 38.sp)
    return when {
        length <= 8  -> big
        length <= 11 -> medium
        else         -> small
    }
}

private const val BUTTON_SCALE   = 0.872f
private const val OVERALL_SHRINK = 0.97f
private val EDGE_MARGIN = 14.dp
private const val COMPACT_SCALE = 0.82f

@Composable
fun CalculatorKeypad(
    onDigit     : (Char) -> Unit,
    onDecimal   : () -> Unit,
    onOperator  : (CalcOperator) -> Unit,
    onPercent   : () -> Unit,
    onNegate    : () -> Unit,
    onClear     : () -> Unit,
    onEquals    : () -> Unit,
    onBackspace : () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    BoxWithConstraints(modifier = modifier) {
        val rowWidth  = maxWidth - EDGE_MARGIN * 2
        val cellFull  = (rowWidth / 4) * BUTTON_SCALE
        val gapFull   = (rowWidth - cellFull * 4) / 3
        val shrink    = OVERALL_SHRINK * (if (compact) COMPACT_SCALE else 1f)
        val cell      = cellFull * shrink
        val gap       = gapFull * shrink

        Column(
            modifier             = Modifier
                .fillMaxWidth()
                .padding(horizontal = EDGE_MARGIN),
            verticalArrangement  = Arrangement.spacedBy(gap),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            CalcButtonRow(gap) {
                CalcButton("AC", CalcFunctionBg, CalcFunctionText, cell, onClick = onClear)
                CalcButton("±",  CalcFunctionBg, CalcFunctionText, cell, onClick = onNegate)
                CalcButton("%",  CalcFunctionBg, CalcFunctionText, cell, onClick = onPercent)
                CalcButton("÷",  CalcOperatorBg, CalcOperatorText, cell, onClick = { onOperator(CalcOperator.DIVIDE) }, fontSize = 34.sp)
            }
            CalcButtonRow(gap) {
                CalcButton("7", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('7') })
                CalcButton("8", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('8') })
                CalcButton("9", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('9') })
                CalcButton("×", CalcOperatorBg, CalcOperatorText, cell, onClick = { onOperator(CalcOperator.MULTIPLY) }, fontSize = 34.sp)
            }
            CalcButtonRow(gap) {
                CalcButton("4", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('4') })
                CalcButton("5", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('5') })
                CalcButton("6", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('6') })
                CalcButton("−", CalcOperatorBg, CalcOperatorText, cell, onClick = { onOperator(CalcOperator.SUBTRACT) }, fontSize = 34.sp)
            }
            CalcButtonRow(gap) {
                CalcButton("1", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('1') })
                CalcButton("2", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('2') })
                CalcButton("3", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('3') })
                CalcButton("+", CalcOperatorBg, CalcOperatorText, cell, onClick = { onOperator(CalcOperator.ADD) }, fontSize = 34.sp)
            }
            CalcButtonRow(gap) {
                CalcIconButton(Icons.AutoMirrored.Filled.Backspace, CalcFunctionBg, CalcFunctionText, cell, onClick = onBackspace)
                CalcButton("0", CalcDigitBg, CalcDigitText, cell, onClick = { onDigit('0') })
                CalcButton(".", CalcDigitBg, CalcDigitText, cell, onClick = onDecimal)
                CalcButton("=", CalcOperatorBg, CalcOperatorText, cell, onClick = onEquals, fontSize = 34.sp)
            }
        }
    }
}

@Composable
private fun CalcButtonRow(gap: Dp, content: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(gap),
        content                = { content() }
    )
}

@Composable
private fun CalcButton(
    label   : String,
    bg      : Color,
    fg      : Color,
    cell    : Dp,
    onClick : () -> Unit,
    fontSize: TextUnit = 30.sp
) {
    Box(
        modifier = Modifier
            .size(cell)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = fontSize,
            fontWeight = FontWeight.Normal,
            color      = fg
        )
    }
}

@Composable
private fun CalcIconButton(
    icon    : androidx.compose.ui.graphics.vector.ImageVector,
    bg      : Color,
    fg      : Color,
    cell    : Dp,
    onClick : () -> Unit
) {
    Box(
        modifier = Modifier
            .size(cell)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = fg,
            modifier           = Modifier.size(cell * 0.42f)
        )
    }
}
