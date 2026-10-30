@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_VALUE")
package com.thormakk.keyarmor.presentation.components.selection

import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R

@Composable
fun BulkTagSheet(
    availableTags: List<String>,
    onApply:       (BulkMode, Set<String>) -> Unit,
    onDismiss:     () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var tabIndex by remember { mutableIntStateOf(0) }
    val mode = when (tabIndex) {
        1    -> BulkMode.REMOVE
        2    -> BulkMode.REPLACE
        else -> BulkMode.ADDITIVE
    }

    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(tabIndex) { selected = emptySet() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        contentWindowInsets = { WindowInsets(0) },
        containerColor   = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text      = stringResource(R.string.bulk_tag_sheet_title),
                style     = MaterialTheme.typography.titleMedium,
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            )

            PrimaryTabRow(
                selectedTabIndex = tabIndex,
                modifier         = Modifier.fillMaxWidth()
            ) {
                listOf(
                    R.string.bulk_mode_additive,
                    R.string.bulk_mode_remove,
                    R.string.bulk_mode_replace
                ).forEachIndexed { index, labelRes ->
                    Tab(
                        selected = tabIndex == index,
                        onClick  = { tabIndex = index },
                        text     = { Text(stringResource(labelRes)) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (availableTags.isEmpty()) {
                Text(
                    text      = stringResource(R.string.bulk_no_tags_available),
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    availableTags.forEach { tag ->
                        val isSelected = tag in selected
                        FilterChip(
                            selected = isSelected,
                            onClick  = {
                                selected = if (isSelected) selected - tag else selected + tag
                            },
                            label    = { Text(tag) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor  = MaterialTheme.colorScheme.primary,
                                selectedLabelColor      = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick  = { onApply(mode, selected) },
                enabled  = selected.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp), colors = appButtonColors()
            ) {
                Text(stringResource(R.string.bulk_apply))
            }
        }
    }
}
