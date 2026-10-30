package com.thormakk.keyarmor.presentation.screens.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.SectionColorFinance

@Composable
fun BankAwareAvatar(
    bankName: String,
    modifier: Modifier = Modifier,
    size:     Dp       = 36.dp
) {
    val shape      = CircleShape
    val iconSize   = (size.value * 0.50f).dp
    val brandInfo  = remember(bankName) { BankBrandProvider.resolve(bankName) }

    if (brandInfo != null) {
        val brandColor = Color(brandInfo.brandColorArgb)
        val luminance  = 0.2126f * brandColor.red +
                         0.7152f * brandColor.green +
                         0.0722f * brandColor.blue
        val iconTint   = if (luminance > 0.55f) Color(0xFF1A1A1AL) else Color.White

        Box(
            modifier         = modifier
                .size(size)
                .background(brandColor, shape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.AccountBalance,
                contentDescription = bankName,
                tint               = iconTint,
                modifier           = Modifier.size(iconSize)
            )
        }
    } else {
        val bgColor   = SectionColorFinance.copy(alpha = 0.15f)
        val iconTint  = SectionColorFinance

        Box(
            modifier         = modifier
                .size(size)
                .background(bgColor, shape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.AccountBalance,
                contentDescription = bankName.ifEmpty { stringResource(R.string.bank_field_bank_name) },
                tint               = iconTint,
                modifier           = Modifier.size(iconSize)
            )
        }
    }
}
