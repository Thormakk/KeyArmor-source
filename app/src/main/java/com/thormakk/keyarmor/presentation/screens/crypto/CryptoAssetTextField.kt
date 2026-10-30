package com.thormakk.keyarmor.presentation.screens.crypto

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoAssetTextField(
    value:              String,
    onValueChange:      (String) -> Unit,
    modifier:           Modifier        = Modifier,
    label:              String          = stringResource(R.string.crypto_field_asset),
    compatibleTickers:  Set<String>     = emptySet(),
    colors:             androidx.compose.material3.TextFieldColors = OutlinedTextFieldDefaults.colors(),
    shape:              Shape           = RoundedCornerShape(8.dp),
    leadingIcon:        @Composable (() -> Unit)? = null
) {
    val focusManager   = LocalFocusManager.current
    val autoLockManager = LocalAutoLockManager.current
    var expanded     by remember { mutableStateOf(false) }

    val suggestions  = remember(value, compatibleTickers) {
        val raw = CryptoIconProvider.suggestions(value, limit = if (compatibleTickers.isEmpty()) 6 else 12)
        if (compatibleTickers.isEmpty()) raw
        else raw.filter { it.ticker in compatibleTickers }.take(6)
    }

    val showDropdown = expanded && suggestions.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded         = showDropdown,
        onExpandedChange = {   },
        modifier         = modifier
    ) {
        OutlinedTextField(
            value         = value,
            onValueChange = { newText ->
                autoLockManager?.recordInteraction()
                onValueChange(newText)
                expanded = newText.isNotBlank()
            },
            placeholder   = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            singleLine    = true,
            leadingIcon   = leadingIcon,
            modifier      = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
            shape         = shape,
            colors        = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = {
                    expanded = false
                    focusManager.moveFocus(FocusDirection.Down)
                }
            )
        )

        ExposedDropdownMenu(
            expanded         = showDropdown,
            onDismissRequest = { expanded = false },
            modifier         = Modifier.heightIn(max = 300.dp),
            containerColor   = MaterialTheme.colorScheme.background
        ) {
            suggestions.forEach { coin ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier.padding(vertical = 2.dp)
                        ) {
                            CryptoAwareAvatar(
                                asset    = coin.ticker,
                                size     = 28.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text       = coin.name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize   = 14.sp,
                                    maxLines   = 1
                                )
                                Text(
                                    text     = coin.ticker,
                                    fontSize = 11.sp,
                                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    onClick = {
                        onValueChange(coin.ticker)
                        expanded = false
                    }
                )
            }
        }
    }
}
