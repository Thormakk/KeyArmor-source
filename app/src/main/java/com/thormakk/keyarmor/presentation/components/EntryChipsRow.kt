package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.domain.model.EntryCategory

@Composable
internal fun EntryCategoryBlock(category: EntryCategory, truncate: Boolean = true) {
    val label = androidx.compose.ui.res.stringResource(category.labelRes)
    val color = chipCategoryColor()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text     = if (truncate) label.truncateTag() else label,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TagBlock(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            maxLines = 1
        )
    }
}

@Composable
private fun TagOverflowBadge(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color.copy(alpha = 0.7f)
    )
}

private class ChipsPlan(val catPlaceable: Placeable?, val catW: Int, val fits: Int)

@Composable
internal fun EntryChipsRow(
    category:    EntryCategory?,
    tags:        List<String>,
    maxTagCount: Int = 3
) {
    if (category == null && tags.isEmpty()) return
    val tagColor = chipTagColor()

    SubcomposeLayout { constraints ->
        val gapPx    = 4.dp.roundToPx()
        val maxAvail = constraints.maxWidth
        val candidateTags = tags.take(maxTagCount)

        val badgeProbeW = if (tags.isNotEmpty()) {
            subcompose("badge_probe") { TagOverflowBadge("+${tags.size}", tagColor) }
                .first().measure(Constraints()).width
        } else 0

        fun plan(full: Boolean): ChipsPlan {
            val v = if (full) "f" else "t"
            val catP = if (category != null) {
                subcompose("cat_probe_$v") { EntryCategoryBlock(category, truncate = !full) }
                    .first().measure(Constraints())
            } else null
            val catW = catP?.width ?: 0

            val tagsAvail = (maxAvail - catW - if (catW > 0 && tags.isNotEmpty()) gapPx else 0)
                .coerceAtLeast(0)

            var usedW = 0
            var fits  = 0
            for (i in candidateTags.indices) {
                val label = if (full) candidateTags[i] else candidateTags[i].truncateTag()
                val w = subcompose("tag_probe_${v}_$i") { TagBlock(label, tagColor) }
                    .first().measure(Constraints()).width
                val hasMoreAfter = tags.size > (i + 1)
                val badgeReserve = if (hasMoreAfter) gapPx + badgeProbeW else 0
                val needed = usedW + (if (usedW > 0) gapPx else 0) + w + badgeReserve
                if (needed <= tagsAvail) {
                    usedW += (if (usedW > 0) gapPx else 0) + w
                    fits++
                } else break
            }
            return ChipsPlan(catP, catW, fits)
        }

        val fullPlan = plan(true)
        val useFull  = fullPlan.catW <= maxAvail && fullPlan.fits == candidateTags.size
        val chosen   = if (useFull) fullPlan else plan(false)

        val catPlaceable = chosen.catPlaceable
        val catW         = chosen.catW
        val fits         = chosen.fits
        val extra        = tags.size - fits

        val tagPlaceables = candidateTags.take(fits).mapIndexed { i, tag ->
            val label = if (useFull) tag else tag.truncateTag()
            subcompose("tag_$i") { TagBlock(label, tagColor) }
                .first().measure(Constraints())
        }

        val badgePlaceable = if (extra > 0) {
            subcompose("badge") { TagOverflowBadge("+$extra", tagColor) }
                .first().measure(Constraints())
        } else null

        val totalHeight = maxOf(
            catPlaceable?.height ?: 0,
            tagPlaceables.maxOfOrNull { it.height } ?: 0,
            badgePlaceable?.height ?: 0
        )

        layout(maxAvail, totalHeight) {
            var x = 0
            catPlaceable?.placeRelative(x, (totalHeight - catPlaceable.height) / 2)
            if (catPlaceable != null) {
                x += catW + if (tagPlaceables.isNotEmpty() || badgePlaceable != null) gapPx else 0
            }
            tagPlaceables.forEach { p ->
                p.placeRelative(x, (totalHeight - p.height) / 2)
                x += p.width + gapPx
            }
            badgePlaceable?.placeRelative(x, (totalHeight - badgePlaceable.height) / 2)
        }
    }
}
