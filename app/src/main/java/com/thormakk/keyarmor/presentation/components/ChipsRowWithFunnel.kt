@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.presentation.screens.vault.FunnelIcon

data class ChipData(
    val key:           String,
    val label:         String,
    val selected:      Boolean,
    val selectedColor: Color,
    val onClick:       () -> Unit,
    val count:         Int          = 0,
    val icon:          ImageVector? = null
)

fun determineVisibleChipIndices(
    chips:            List<ChipData>,
    chipWidthsPx:     List<Int>,
    availableWidthPx: Int,
    spacingPx:        Int
): List<Int> {
    val candidate = chips.indices.toMutableList()

    fun totalWidth(): Int {
        if (candidate.isEmpty()) return 0
        return candidate.sumOf { chipWidthsPx[it] } +
               (candidate.size - 1).coerceAtLeast(0) * spacingPx
    }

    while (totalWidth() > availableWidthPx && candidate.isNotEmpty()) {
        val toRemove =
            candidate.filter { !chips[it].selected }.maxByOrNull { chips[it].label.length }
            ?: candidate.maxByOrNull { chips[it].label.length }
            ?: break
        candidate.remove(toRemove)
    }

    return candidate
}

@Composable
fun ChipsRowWithFunnel(
    chips:                    List<ChipData>,
    activeFilterCount:        Int,
    onFunnelClick:            () -> Unit,
    funnelContentDescription: String,
    modifier:                 Modifier = Modifier,
    onVisibleKeysChanged:     (Set<String>) -> Unit = {},
    spacing:                  Dp = 5.dp,
    chipSpacing:              Dp = 8.dp,
    chipContent:              @Composable (ChipData) -> Unit
) {
    val density      = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val chipStyle    = MaterialTheme.typography.labelMedium

    val chipHPadPx      = remember(density) { with(density) { 12.dp.roundToPx() } }
    val spacingPx       = remember(spacing, density)     { with(density) { spacing.roundToPx() } }
    val chipSpacingPx   = remember(chipSpacing, density) { with(density) { chipSpacing.roundToPx() } }
    val funnelWidthPx = remember(density) { with(density) { 36.dp.roundToPx() } }
    val iconExtraPx   = remember(density) { with(density) { 18.dp.roundToPx() } }
    val countStyle    = MaterialTheme.typography.labelSmall
    val countGapPx    = remember(density) { with(density) { 3.dp.roundToPx() } }

    val labelKey      = remember(chips) { chips.joinToString(",") { it.label } }
    val iconCountKey  = remember(chips) { chips.joinToString(",") { "${it.icon != null}:${it.count}" } }
    val chipWidthsPx  = remember(labelKey, iconCountKey) {
        chips.map { chip ->
            val labelW = textMeasurer.measure(chip.label, chipStyle).size.width
            val iconW  = if (chip.icon != null) iconExtraPx else 0
            val countW = if (chip.count > 0) {
                textMeasurer.measure("(${chip.count})", countStyle).size.width + countGapPx
            } else 0
            labelW + iconW + countW + chipHPadPx
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val availableWidthPx = constraints.maxWidth
        val availableForChipsPx = availableWidthPx - funnelWidthPx - spacingPx

        val minChipSpacingPx = remember(density) { with(density) { 3.dp.roundToPx() } }
        val effectiveChipSpacingPx = remember(availableForChipsPx, labelKey, iconCountKey) {
            val gaps = (chips.size - 1).coerceAtLeast(0)
            if (gaps == 0) chipSpacingPx
            else {
                val allWidth = chipWidthsPx.sum()
                var sp = chipSpacingPx
                while (sp > minChipSpacingPx && allWidth + gaps * sp > availableForChipsPx) sp--
                sp
            }
        }
        val effectiveChipSpacing = with(density) { effectiveChipSpacingPx.toDp() }

        val selectionKey = remember(chips) { chips.joinToString(",") { it.key + it.selected } }
        val visibleIndices = remember(selectionKey, availableForChipsPx, labelKey, iconCountKey, effectiveChipSpacingPx) {
            if (availableForChipsPx <= 0) emptyList()
            else determineVisibleChipIndices(chips, chipWidthsPx, availableForChipsPx, effectiveChipSpacingPx)
        }

        SideEffect {
            onVisibleKeysChanged(visibleIndices.map { chips[it].key }.toSet())
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            visibleIndices.forEachIndexed { i, idx ->
                chipContent(chips[idx])
                if (i < visibleIndices.lastIndex) Spacer(Modifier.width(effectiveChipSpacing))
            }
            if (visibleIndices.isNotEmpty()) Spacer(Modifier.width(spacing))
            BadgedBox(
                badge = {
                    if (activeFilterCount > 0) Badge { Text(activeFilterCount.toString()) }
                }
            ) {
                IconButton(
                    onClick  = onFunnelClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector        = FunnelIcon,
                        contentDescription = funnelContentDescription,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
