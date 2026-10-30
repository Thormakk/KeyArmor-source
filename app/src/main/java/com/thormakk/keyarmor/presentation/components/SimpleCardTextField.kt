package com.thormakk.keyarmor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.presentation.util.LocalAutoLockManager

@Composable
fun SimpleCardTextField(
    value:                String,
    onValueChange:        (String) -> Unit,
    placeholder:          String,
    modifier:             Modifier                          = Modifier,
    leadingIcon:          @Composable (() -> Unit)?         = null,
    trailingContent:      @Composable (RowScope.() -> Unit)? = null,
    singleLine:           Boolean                           = true,
    keyboardOptions:      KeyboardOptions                    = KeyboardOptions.Default,
    keyboardActions:      KeyboardActions                   = KeyboardActions.Default,
    visualTransformation: VisualTransformation              = VisualTransformation.None,
    readOnly:             Boolean                           = false,
    enabled:              Boolean                           = true,
    shape:                Shape                              = RoundedCornerShape(8.dp),
    containerColor:       Color?                             = null,
) {
    val autoLockManager = LocalAutoLockManager.current

    Row(
        modifier          = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(containerColor ?: MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Box(
                modifier         = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                leadingIcon()
            }
        } else {
            Spacer(Modifier.width(16.dp))
        }

        Box(
            modifier         = Modifier
                .weight(1f)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text  = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            BasicTextField(
                value                = value,
                onValueChange        = { autoLockManager?.recordInteraction(); onValueChange(it) },
                singleLine           = singleLine,
                readOnly             = readOnly,
                enabled              = enabled,
                textStyle            = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions      = keyboardOptions,
                keyboardActions      = keyboardActions,
                visualTransformation = visualTransformation,
                cursorBrush          = SolidColor(MaterialTheme.colorScheme.primary),
                modifier             = Modifier.fillMaxWidth()
            )
        }

        if (trailingContent != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content           = trailingContent
            )
        } else {
            Spacer(Modifier.width(16.dp))
        }
    }
}
