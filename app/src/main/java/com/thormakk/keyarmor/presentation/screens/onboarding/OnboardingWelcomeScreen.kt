package com.thormakk.keyarmor.presentation.screens.onboarding

import com.thormakk.keyarmor.ui.theme.appButtonColors
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thormakk.keyarmor.R
import kotlinx.coroutines.launch

private enum class SlideTone { BRAND, WARNING, SUCCESS }

private val WarningAmber = Color(0xFFFFB300)
private val SuccessGreen = Color(0xFF4CAF50)

private data class WelcomeSlide(
    val titleRes: Int,
    val bodyRes:  Int,
    val icon:     ImageVector? = null,
    @DrawableRes val logoRes: Int? = null,
    val tone:     SlideTone = SlideTone.BRAND
)

@Composable
fun OnboardingWelcomeScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingWelcomeViewModel = hiltViewModel()
) {
    val slides = remember {
        listOf(
            WelcomeSlide(R.string.onb_welcome1_title, R.string.onb_welcome1_body, logoRes = R.drawable.logo_keyarmor),
            WelcomeSlide(R.string.onb_welcome2_title, R.string.onb_welcome2_body, icon = Icons.Filled.Lock,        tone = SlideTone.WARNING),
            WelcomeSlide(R.string.onb_welcome3_title, R.string.onb_welcome3_body, icon = Icons.Filled.CloudUpload, tone = SlideTone.WARNING),
            WelcomeSlide(R.string.onb_welcome4_title, R.string.onb_welcome4_body, icon = Icons.Filled.CheckCircle, tone = SlideTone.SUCCESS),
        )
    }
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == slides.lastIndex

    fun finish() {
        viewModel.markWelcomeSeen()
        onFinish()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { finish() }) {
                    Text(stringResource(R.string.onb_skip))
                }
            }

            HorizontalPager(
                state    = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                SlideContent(slides[page])
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(slides.size) { i ->
                    val selected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (isLast) finish()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 72.dp)
                    .height(52.dp), colors = appButtonColors()
            ) {
                Text(stringResource(if (isLast) R.string.onb_get_started else R.string.onb_next))
            }
        }
    }
}

@Composable
private fun SlideContent(slide: WelcomeSlide) {
    val accent = when (slide.tone) {
        SlideTone.BRAND   -> MaterialTheme.colorScheme.primary
        SlideTone.WARNING -> WarningAmber
        SlideTone.SUCCESS -> SuccessGreen
    }
    val isWarning = slide.tone == SlideTone.WARNING
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .clip(RoundedCornerShape(38.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.05f))
                    )
                )
                .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(38.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (slide.logoRes != null) {
                Image(
                    painter            = painterResource(slide.logoRes),
                    contentDescription = null,
                    modifier           = Modifier.size(88.dp)
                )
            } else if (slide.icon != null) {
                Icon(
                    imageVector        = slide.icon,
                    contentDescription = null,
                    tint               = accent,
                    modifier           = Modifier.size(60.dp)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        if (isWarning) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(WarningAmber.copy(alpha = 0.14f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector        = Icons.Filled.Warning,
                    contentDescription = null,
                    tint               = WarningAmber,
                    modifier           = Modifier.size(15.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text       = stringResource(R.string.onb_important),
                    color      = WarningAmber,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        Text(
            text       = stringResource(slide.titleRes),
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text       = stringResource(slide.bodyRes),
            style      = MaterialTheme.typography.bodyLarge,
            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            textAlign  = TextAlign.Center,
            lineHeight = 24.sp,
            modifier   = Modifier.widthIn(max = 340.dp)
        )
    }
}
