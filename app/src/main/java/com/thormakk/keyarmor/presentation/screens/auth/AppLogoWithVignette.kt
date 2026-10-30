package com.thormakk.keyarmor.presentation.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.R

@Composable
fun AppLogoWithVignette(
    size: Int,
    bgColor: Color = MaterialTheme.colorScheme.background
) {
    val containerSize = (size * 1.30f).toInt()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(containerSize.dp)
    ) {
        Image(
            painter            = painterResource(R.drawable.logo_keyarmor),
            contentDescription = "KeyArmor",
            modifier           = Modifier.size(size.dp),
            contentScale       = ContentScale.Fit
        )

        Box(
            modifier = Modifier
                .size(containerSize.dp)
                .background(
                    Brush.radialGradient(
                        0.00f to Color.Transparent,
                        0.68f to Color.Transparent,
                        1.00f to bgColor
                    )
                )
        )
    }
}
