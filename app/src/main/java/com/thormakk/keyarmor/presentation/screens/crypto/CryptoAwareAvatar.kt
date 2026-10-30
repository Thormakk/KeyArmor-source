package com.thormakk.keyarmor.presentation.screens.crypto

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thormakk.keyarmor.presentation.screens.vault.EntryAvatar

@Composable
fun CryptoAwareAvatar(
    asset:    String,
    modifier: Modifier = Modifier,
    size:     Dp       = 36.dp
) {
    val shape       = CircleShape
    val normalised  = remember(asset) { CryptoIconProvider.canonicalTicker(asset) }
    val drawableRes = remember(normalised) { CryptoIconProvider.drawableResForTicker(normalised) }

    when {
        drawableRes != null -> {
            Image(
                painter            = painterResource(drawableRes),
                contentDescription = asset,
                contentScale       = ContentScale.Crop,
                modifier           = modifier
                    .size(size)
                    .clip(shape)
            )
        }

        else -> {
            val info = remember(normalised) { CryptoIconProvider.resolve(asset) }

            if (info != null) {
                val brandColor = Color(info.brandColorArgb)
                val luminance  = 0.2126f * brandColor.red +
                                 0.7152f * brandColor.green +
                                 0.0722f * brandColor.blue
                val textColor  = if (luminance > 0.55f) Color(0xFF1A1A1AL) else Color.White

                val baseFontSize = size.value * when (info.symbol.length) {
                    1    -> 0.50f
                    2    -> 0.39f
                    3    -> 0.31f
                    else -> 0.25f
                }

                Box(
                    modifier         = modifier
                        .size(size)
                        .background(brandColor, shape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = info.symbol,
                        color      = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize   = baseFontSize.sp,
                        maxLines   = 1
                    )
                }
            } else {
                EntryAvatar(title = asset.ifEmpty { "?" }, size = size, modifier = modifier)
            }
        }
    }
}
