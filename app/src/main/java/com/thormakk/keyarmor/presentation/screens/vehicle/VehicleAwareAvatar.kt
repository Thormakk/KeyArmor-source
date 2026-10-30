package com.thormakk.keyarmor.presentation.screens.vehicle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thormakk.keyarmor.presentation.components.SectionColorIdentity

@Composable
fun VehicleAwareAvatar(
    make:     String,
    modifier: Modifier = Modifier,
    size:     Dp       = 36.dp
) {
    val normalised  = remember(make) { make.trim() }
    val drawableRes = remember(normalised) { VehicleBrandProvider.drawableResForBrand(normalised) }

    if (drawableRes != null) {
        Box(
            modifier         = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter            = painterResource(drawableRes),
                contentDescription = make,
                contentScale       = ContentScale.Fit,
                modifier           = Modifier.size(size * 0.97f)
            )
        }
    } else {
        Box(
            modifier         = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.22f))
                .background(SectionColorIdentity.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.DirectionsCar,
                contentDescription = make,
                tint               = SectionColorIdentity,
                modifier           = Modifier.size(size * 0.6f)
            )
        }
    }
}
