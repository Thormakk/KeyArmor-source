package com.thormakk.keyarmor.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsBlackChrome = staticCompositionLocalOf { false }

private val CreamColorScheme = lightColorScheme(
    primary              = Brown700,
    onPrimary            = Cream50,
    primaryContainer     = Brown200,
    onPrimaryContainer   = Brown800,

    secondary            = Brown400,
    onSecondary          = Cream50,
    secondaryContainer   = Cream200,
    onSecondaryContainer = Brown800,

    background           = Cream50,
    onBackground         = Brown800,

    surface                  = Cream100,
    onSurface                = Brown800,
    surfaceContainer         = Cream100,
    surfaceContainerHigh     = Cream100,
    surfaceContainerHighest  = Cream200,

    surfaceVariant       = Cream200,
    onSurfaceVariant     = Brown600,

    outline              = Brown400,
    outlineVariant       = Cream300,

    error                = ErrorRed,
    onError              = Cream50,
)

private val DarkColorScheme = darkColorScheme(
    primary              = DarkAccent,
    onPrimary            = Color.White,
    primaryContainer     = Dark700,
    onPrimaryContainer   = DarkAccent,

    secondary            = DarkAccent,
    onSecondary          = Color.White,
    secondaryContainer   = Dark700,
    onSecondaryContainer = DarkTextPrimary,

    background           = Dark900,
    onBackground         = DarkTextPrimary,

    surface                  = Dark800,
    onSurface                = DarkTextPrimary,
    surfaceContainer         = Dark800,
    surfaceContainerHigh     = Dark800,
    surfaceContainerHighest  = Dark700,

    surfaceVariant       = Dark700,
    onSurfaceVariant     = DarkTextSecondary,

    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,

    error                = ErrorRed,
    onError              = Color.White,
)

private val NavyColorScheme = darkColorScheme(
    primary              = BlancoAccent,
    onPrimary            = BlancoOnAccent,
    primaryContainer     = Navy700,
    onPrimaryContainer   = BlancoAccent,

    secondary            = BlancoAccent,
    onSecondary          = BlancoOnAccent,
    secondaryContainer   = Navy700,
    onSecondaryContainer = DarkTextPrimary,

    background           = Navy900,
    onBackground         = DarkTextPrimary,

    surface                  = Navy800,
    onSurface                = DarkTextPrimary,
    surfaceContainer         = Navy800,
    surfaceContainerHigh     = Navy800,
    surfaceContainerHighest  = Navy700,

    surfaceVariant       = Navy700,
    onSurfaceVariant     = DarkTextSecondary,

    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,

    error                = ErrorRed,
    onError              = Color.White,
)

private val RoseColorScheme = lightColorScheme(
    primary              = BurgundyAccent,
    onPrimary            = Rose50,
    primaryContainer     = Rose200,
    onPrimaryContainer   = Burgundy900,

    secondary            = Burgundy700,
    onSecondary          = Rose50,
    secondaryContainer   = Rose200,
    onSecondaryContainer = Burgundy900,

    background           = Rose50,
    onBackground         = Burgundy900,

    surface                  = Rose100,
    onSurface                = Burgundy800,
    surfaceContainer         = Rose100,
    surfaceContainerHigh     = Rose100,
    surfaceContainerHighest  = Rose200,

    surfaceVariant       = Rose200,
    onSurfaceVariant     = Burgundy700,

    outline              = Burgundy700,
    outlineVariant       = Rose300,

    error                = ErrorRed,
    onError              = Rose50,
)

private val SkyColorScheme = lightColorScheme(
    primary              = SkyAccent,
    onPrimary            = Sky50,
    primaryContainer     = Sky200,
    onPrimaryContainer   = NavyText900,

    secondary            = NavyText700,
    onSecondary          = Sky50,
    secondaryContainer   = Sky200,
    onSecondaryContainer = NavyText900,

    background           = Sky50,
    onBackground         = NavyText900,

    surface                  = Sky100,
    onSurface                = NavyText800,
    surfaceContainer         = Sky100,
    surfaceContainerHigh     = Sky100,
    surfaceContainerHighest  = Sky200,

    surfaceVariant       = Sky200,
    onSurfaceVariant     = NavyText700,

    outline              = NavyText700,
    outlineVariant       = Sky300,

    error                = ErrorRed,
    onError              = Sky50,
)

private val WineColorScheme = darkColorScheme(
    primary              = WineAccent,
    onPrimary            = Color.White,
    primaryContainer     = Wine700,
    onPrimaryContainer   = WineAccent,

    secondary            = WineAccent,
    onSecondary          = Color.White,
    secondaryContainer   = Wine700,
    onSecondaryContainer = DarkTextPrimary,

    background           = Wine900,
    onBackground         = DarkTextPrimary,

    surface                  = Wine800,
    onSurface                = DarkTextPrimary,
    surfaceContainer         = Wine800,
    surfaceContainerHigh     = Wine800,
    surfaceContainerHighest  = Wine700,

    surfaceVariant       = Wine700,
    onSurfaceVariant     = DarkTextSecondary,

    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,

    error                = ErrorRed,
    onError              = Color.White,
)

private val WhiteColorScheme = lightColorScheme(
    primary              = WhiteTealAccent,
    onPrimary            = Color.White,
    primaryContainer     = White200,
    onPrimaryContainer   = WhiteText900,

    secondary            = WhiteTealAccent,
    onSecondary          = Color.White,
    secondaryContainer   = White200,
    onSecondaryContainer = WhiteText900,

    background           = Color.White,
    onBackground         = WhiteText900,

    surface                  = White100,
    onSurface                = WhiteText800,
    surfaceContainer         = White100,
    surfaceContainerHigh     = White100,
    surfaceContainerHighest  = White200,

    surfaceVariant       = White200,
    onSurfaceVariant     = WhiteText700,

    outline              = WhiteOutline,
    outlineVariant       = White300,

    error                = ErrorRed,
    onError              = Color.White,
)

private val LilaColorScheme = lightColorScheme(
    primary              = LilaAccent,
    onPrimary            = Lila50,
    primaryContainer     = Lila200,
    onPrimaryContainer   = LilaText900,

    secondary            = LilaText700,
    onSecondary          = Lila50,
    secondaryContainer   = Lila200,
    onSecondaryContainer = LilaText900,

    background           = Lila50,
    onBackground         = LilaText900,

    surface                  = Lila100,
    onSurface                = LilaText800,
    surfaceContainer         = Lila100,
    surfaceContainerHigh     = Lila100,
    surfaceContainerHighest  = Lila200,

    surfaceVariant       = Lila200,
    onSurfaceVariant     = LilaText700,

    outline              = LilaOutline,
    outlineVariant       = Lila300,

    error                = ErrorRed,
    onError              = Lila50,
)

private val MentaColorScheme = lightColorScheme(
    primary              = TealAccent,
    onPrimary            = Menta50,
    primaryContainer     = Menta200,
    onPrimaryContainer   = MentaText900,

    secondary            = MentaText700,
    onSecondary          = Menta50,
    secondaryContainer   = Menta200,
    onSecondaryContainer = MentaText900,

    background           = Menta50,
    onBackground         = MentaText900,

    surface                  = Menta100,
    onSurface                = MentaText800,
    surfaceContainer         = Menta100,
    surfaceContainerHigh     = Menta100,
    surfaceContainerHighest  = Menta200,

    surfaceVariant       = Menta200,
    onSurfaceVariant     = MentaText700,

    outline              = MentaOutline,
    outlineVariant       = Menta300,

    error                = ErrorRed,
    onError              = Menta50,
)

private val PetroleoColorScheme = darkColorScheme(
    primary              = BlancoAccent,
    onPrimary            = IntermediateOnAccent,
    primaryContainer     = Petroleo700,
    onPrimaryContainer   = BlancoAccent,

    secondary            = BlancoAccent,
    onSecondary          = IntermediateOnAccent,
    secondaryContainer   = Petroleo700,
    onSecondaryContainer = DarkTextPrimary,

    background           = Petroleo900,
    onBackground         = DarkTextPrimary,

    surface                  = Petroleo800,
    onSurface                = DarkTextPrimary,
    surfaceContainer         = Petroleo800,
    surfaceContainerHigh     = Petroleo800,
    surfaceContainerHighest  = Petroleo700,

    surfaceVariant       = Petroleo700,
    onSurfaceVariant     = DarkTextSecondary,

    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,

    error                = ErrorRed,
    onError              = Color.White,
)

private val CafeColorScheme = darkColorScheme(
    primary              = BlancoAccent,
    onPrimary            = IntermediateOnAccent,
    primaryContainer     = Cafe700,
    onPrimaryContainer   = BlancoAccent,

    secondary            = BlancoAccent,
    onSecondary          = IntermediateOnAccent,
    secondaryContainer   = Cafe700,
    onSecondaryContainer = DarkTextPrimary,

    background           = Cafe900,
    onBackground         = DarkTextPrimary,

    surface                  = Cafe800,
    onSurface                = DarkTextPrimary,
    surfaceContainer         = Cafe800,
    surfaceContainerHigh     = Cafe800,
    surfaceContainerHighest  = Cafe700,

    surfaceVariant       = Cafe700,
    onSurfaceVariant     = DarkTextSecondary,

    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,

    error                = ErrorRed,
    onError              = Color.White,
)

private val CrepusculoColorScheme = darkColorScheme(
    primary              = BlancoAccent,
    onPrimary            = IntermediateOnAccent,
    primaryContainer     = Crepusculo700,
    onPrimaryContainer   = BlancoAccent,

    secondary            = BlancoAccent,
    onSecondary          = IntermediateOnAccent,
    secondaryContainer   = Crepusculo700,
    onSecondaryContainer = DarkTextPrimary,

    background           = Crepusculo900,
    onBackground         = DarkTextPrimary,

    surface                  = Crepusculo800,
    onSurface                = DarkTextPrimary,
    surfaceContainer         = Crepusculo800,
    surfaceContainerHigh     = Crepusculo800,
    surfaceContainerHighest  = Crepusculo700,

    surfaceVariant       = Crepusculo700,
    onSurfaceVariant     = DarkTextSecondary,

    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,

    error                = ErrorRed,
    onError              = Color.White,
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun KeyArmorTheme(
    themeId:    String = "dark",
    accentArgb: Long   = 0L,
    content: @Composable () -> Unit
) {
    val baseScheme = when (themeId) {
        "cream"    -> CreamColorScheme
        "amoled"   -> NavyColorScheme
        "rose"     -> RoseColorScheme
        "sky"      -> SkyColorScheme
        "wine"     -> WineColorScheme
        "white"    -> WhiteColorScheme
        "lila"     -> LilaColorScheme
        "menta"    -> MentaColorScheme
        "petroleo" -> PetroleoColorScheme
        "cafe"     -> CafeColorScheme
        "crepusculo" -> CrepusculoColorScheme
        else       -> DarkColorScheme
    }

    val accent = if (accentArgb != 0L) Color(accentArgb.toInt()) else null

    val isBlackChrome = false

    val colorScheme = when {
        accent != null -> {
            val onAccent = if (accent == Color.White) Color(0xFF1A1A1A) else Color.White
            baseScheme.copy(
                primary              = accent,
                onPrimary            = onAccent,
                primaryContainer     = baseScheme.primaryContainer,
                onPrimaryContainer   = accent,
                secondary            = accent,
                onSecondary          = onAccent,
                secondaryContainer   = baseScheme.secondaryContainer,
                onSecondaryContainer = baseScheme.onSecondaryContainer
            )
        }
        else -> baseScheme
    }

    val isLightTheme = themeId in setOf("cream", "rose", "sky", "white", "lila", "menta")

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.setBackgroundDrawable(ColorDrawable(colorScheme.background.toArgb()))
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = isLightTheme
        }
    }

    CompositionLocalProvider(LocalIsBlackChrome provides isBlackChrome) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = KeyArmorTypography,
            content     = {
                CompositionLocalProvider(
                    androidx.compose.foundation.LocalIndication provides SinOndaDeToque,
                    androidx.compose.material3.LocalRippleConfiguration provides null,
                    content = content
                )
            }
        )
    }
}

@Suppress("UNUSED_PARAMETER")
private object SinOndaDeToque : androidx.compose.foundation.IndicationNodeFactory {
    override fun create(interactionSource: androidx.compose.foundation.interaction.InteractionSource):
        androidx.compose.ui.node.DelegatableNode = object : androidx.compose.ui.Modifier.Node() {}
    override fun equals(other: Any?) = other === this
    override fun hashCode() = -1
}
