@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.components.selection

import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.domain.model.EntryCategory

@Composable
fun BulkCategorySheet(
    onApply:   (BulkMode, EntryCategory?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var tabIndex         by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf<EntryCategory?>(null) }

    LaunchedEffect(tabIndex) { selectedCategory = null }

    val mode = when (tabIndex) {
        1    -> BulkMode.REMOVE
        2    -> BulkMode.REPLACE
        else -> BulkMode.ADDITIVE
    }

    val canApply = mode == BulkMode.REMOVE || selectedCategory != null

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
                text     = stringResource(R.string.bulk_category_sheet_title),
                style    = MaterialTheme.typography.titleMedium,
                modifier = Modifier
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

            if (mode != BulkMode.REMOVE) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                ) {
                    EntryCategory.entries
                        .filter { it != EntryCategory.SIN_CATEGORIZAR }
                        .forEach { category ->
                            RadioListItem(
                                label    = stringResource(category.labelRes),
                                selected = selectedCategory == category,
                                onClick  = { selectedCategory = category }
                            )
                        }
                }
            } else {
                Text(
                    text     = stringResource(R.string.bulk_category_remove_hint),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick  = { onApply(mode, selectedCategory) },
                enabled  = canApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp), colors = appButtonColors()
            ) {
                Text(stringResource(R.string.bulk_apply))
            }
        }
    }
}

@Composable
private fun RadioListItem(
    label:    String,
    selected: Boolean,
    onClick:  () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick  = onClick,
            colors   = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}
