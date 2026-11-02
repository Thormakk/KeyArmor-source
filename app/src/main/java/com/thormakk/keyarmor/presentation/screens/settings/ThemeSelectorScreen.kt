@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UnusedAssignment")
package com.thormakk.keyarmor.presentation.screens.settings

import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import com.thormakk.keyarmor.presentation.components.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.security.ThemeTrialManager
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea
import com.thormakk.keyarmor.presentation.components.justified

@Composable
internal fun AppThemeOption.displayName(): String = when (id) {
    "white"       -> stringResource(R.string.theme_name_white)
    "dark"        -> stringResource(R.string.theme_name_dark)
    "lila"        -> stringResource(R.string.theme_name_lila)
    "menta"       -> stringResource(R.string.theme_name_mint)
    "cream"       -> stringResource(R.string.theme_name_cream)
    "sky"         -> stringResource(R.string.theme_name_sky)
    "rose"        -> stringResource(R.string.theme_name_rose)
    "amoled"      -> stringResource(R.string.theme_name_navy)
    "wine"        -> stringResource(R.string.theme_name_wine)
    "petroleo"    -> stringResource(R.string.theme_name_petroleum)
    "cafe"        -> stringResource(R.string.theme_name_cafe)
    "crepusculo"  -> stringResource(R.string.theme_name_twilight)
    else          -> id
}

@Composable
fun ThemeSelectorScreen(
    onBack: () -> Unit,
    onPlansClick: () -> Unit = {},
    viewModel: ThemeSelectorViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val isPremiumUser by viewModel.isPremiumUser.collectAsStateWithLifecycle()
    val trialThemeId  by viewModel.trialThemeId.collectAsStateWithLifecycle()
    var premiumFeature by remember { mutableStateOf<String?>(null) }
    var trialCandidate by remember { mutableStateOf<AppThemeOption?>(null) }

    var previewTheme  by remember { mutableStateOf<AppThemeOption?>(null) }
    val savedAccentColor = remember(uiState.savedAccentArgb) {
        if (uiState.savedAccentArgb != 0L) Color(uiState.savedAccentArgb) else null
    }
    var previewAccent by remember(savedAccentColor) { mutableStateOf(savedAccentColor) }

    val savedTheme = remember(uiState.savedThemeId) {
        ALL_THEMES.find { it.id == uiState.savedThemeId } ?: ALL_THEMES[0]
    }

    val trialTheme = remember(trialThemeId) { ALL_THEMES.find { it.id == trialThemeId } }

    val effectiveTheme = trialTheme ?: previewTheme ?: savedTheme

    val compatibleAccents = remember(effectiveTheme) {
        val override = effectiveTheme.accentOverride
        if (override != null) {
            ALL_ACCENT_COLORS.filter { it.color in override }
        } else {
            ALL_ACCENT_COLORS.filter { accent ->
                contrastRatio(effectiveTheme.previewBg, accent.color) >= 3.0f &&
                accent.color !in effectiveTheme.excludedAccents
            }
        }
    }

    val effectiveAccent = trialTheme?.defaultAccent ?: previewAccent ?: effectiveTheme.defaultAccent

    val baseScheme = MaterialTheme.colorScheme
    val dynamicScheme = baseScheme.copy(
        background       = effectiveTheme.previewBg,
        surface          = effectiveTheme.previewBg,
        surfaceVariant   = effectiveTheme.previewBar1,
        primary          = effectiveAccent,
        onPrimary        = Color.White,
        onBackground     = effectiveTheme.labelColor,
        onSurface        = effectiveTheme.labelColor,
        onSurfaceVariant = effectiveTheme.labelColor.copy(alpha = 0.65f),
        outline          = effectiveTheme.previewBar2,
        outlineVariant   = effectiveTheme.previewBar2.copy(alpha = 0.5f)
    )

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val freeThemes    = ALL_THEMES.filter { !it.isPremium }
    val premiumThemes = ALL_THEMES.filter { it.isPremium }

    MaterialTheme(colorScheme = dynamicScheme) {
        premiumFeature?.let { featureName ->
            AlertDialog(
                onDismissRequest = { premiumFeature = null },
                containerColor   = MaterialTheme.colorScheme.background,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(featureName,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onBackground)
                    }
                },
                text = {
                    Text(stringResource(R.string.premium_locked_dialog_message),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                confirmButton = {
                    Button(
                        onClick = { premiumFeature = null; onPlansClick() },
                        shape   = RoundedCornerShape(8.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = MaterialTheme.colorScheme.primary
                        ),
                        border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                    ) { Text(stringResource(R.string.premium_gate_cta)) }
                },
                dismissButton = {
                    TextButton(onClick = { premiumFeature = null }) {
                        Text(stringResource(R.string.action_understood),
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }

        trialCandidate?.let { theme ->
            AlertDialog(
                onDismissRequest = { trialCandidate = null },
                containerColor   = MaterialTheme.colorScheme.background,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(theme.displayName(),
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onBackground)
                    }
                },
                text = {
                    Text(stringResource(R.string.theme_trial_dialog_body, ThemeTrialManager.TRIAL_MINUTES),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.justified())
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.startThemeTrial(theme)
                            previewTheme   = null
                            previewAccent  = null
                            trialCandidate = null
                        },
                        shape   = RoundedCornerShape(8.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = MaterialTheme.colorScheme.primary
                        ),
                        border  = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
                    ) { Text(stringResource(R.string.action_accept)) }
                },
                dismissButton = {
                    TextButton(onClick = { trialCandidate = null }) {
                        Text(stringResource(R.string.action_cancel),
                            color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                    },
                    title = { TextoDeUnaLinea(stringResource(R.string.settings_theme),
                        style      = MaterialTheme.typography.titleLarge.copy(fontSize = SectionTitleFontSize),
                        fontWeight = FontWeight.Bold,
                        color      = sectionTitleColor()) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor             = effectiveTheme.previewBg,
                        titleContentColor          = effectiveTheme.labelColor,
                        navigationIconContentColor = effectiveTheme.labelColor
                    )
                )
            },
            containerColor = effectiveTheme.previewBg
        ) { padding ->
            val isAccentCompatible = compatibleAccents.any { it.color == effectiveAccent }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 7.dp, end = 7.dp, top = 6.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                for (row in freeThemes.chunked(2)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (theme in row) {
                            ThemeCard(
                                theme              = theme,
                                isEffective        = effectiveTheme.id == theme.id,
                                accentColor        = effectiveAccent,
                                isAccentCompatible = isAccentCompatible,
                                isPremiumUser      = isPremiumUser,
                                onClick            = {
                                    previewAccent = null
                                    previewTheme  = theme
                                    viewModel.saveTheme(theme)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(4.dp))
                if (isPremiumUser) {
                    for (row in premiumThemes.chunked(2)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (theme in row) {
                                ThemeCard(
                                    theme              = theme,
                                    isEffective        = effectiveTheme.id == theme.id,
                                    accentColor        = effectiveAccent,
                                    isAccentCompatible = isAccentCompatible,
                                    isPremiumUser      = true,
                                    onClick            = {
                                        previewAccent = null
                                        previewTheme  = theme
                                        viewModel.saveTheme(theme)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                } else {
                    Surface(
                        shape  = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
                    ) {
                        Column(
                            modifier            = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier              = Modifier
                                    .clickable(onClick = onPlansClick)
                                    .padding(vertical = 2.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(14.dp)
                                )
                                Text(
                                    text       = stringResource(R.string.theme_section_premium),
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text  = "·",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                Text(
                                    text  = stringResource(R.string.premium_gate_cta),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                                )
                            }

                            for (row in premiumThemes.chunked(2)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (theme in row) {
                                        ThemeCard(
                                            theme              = theme,
                                            isEffective        = effectiveTheme.id == theme.id,
                                            accentColor        = effectiveAccent,
                                            isAccentCompatible = isAccentCompatible,
                                            isPremiumUser      = false,
                                            onClick            = {
                                                if (theme.id != trialThemeId) trialCandidate = theme
                                            },
                                            modifier           = Modifier.weight(1f)
                                        )
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Text(
                    text       = stringResource(R.string.theme_accent_color),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Text(
                    text  = stringResource(R.string.theme_accent_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    compatibleAccents.forEach { accent ->
                        val isDecorative = !isPremiumUser && effectiveTheme.isPremium
                        val isSelected   = accent.color == effectiveAccent
                        val isWhite      = accent.color == Color.White
                        val borderTint   = if (isWhite) Color(0xFF3D3128) else Color.White
                        val checkTint    = if (isWhite) Color(0xFF3D3128) else Color.White

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accent.color)
                                .then(
                                    when {
                                        isSelected && !isDecorative -> Modifier.border(3.dp, borderTint, CircleShape)
                                        isWhite && !isDecorative    -> Modifier.border(1.dp, Color(0xFFCCC0A0), CircleShape)
                                        else                        -> Modifier
                                    }
                                )
                                .then(
                                    if (!isDecorative) Modifier.clickable {
                                        previewAccent = accent.color
                                        viewModel.saveAccent(accent.color, effectiveTheme.isPremium)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected && !isDecorative) {
                                Icon(
                                    imageVector        = Icons.Default.Check,
                                    contentDescription = null,
                                    tint               = checkTint,
                                    modifier           = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: AppThemeOption,
    isEffective: Boolean,
    accentColor: Color,
    isAccentCompatible: Boolean,
    isPremiumUser: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isEffective && isAccentCompatible -> accentColor
        isEffective                       -> theme.previewBar2
        else                              -> Color.Transparent
    }
    val dimAlpha = if (theme.isPremium && !isPremiumUser && !isEffective) 0.6f else 1f

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(theme.previewBg)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(theme.previewBg.copy(alpha = dimAlpha))
                .padding(6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.fillMaxWidth().height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(theme.previewBar1.copy(alpha = dimAlpha)))
                Box(Modifier.fillMaxWidth(0.6f).height(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(theme.previewBar2.copy(alpha = dimAlpha)))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.previewBar1.copy(alpha = dimAlpha))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = theme.displayName(),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = theme.labelColor.copy(alpha = dimAlpha)
            )

            when {
                isEffective && isAccentCompatible -> {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null,
                            tint = Color.White, modifier = Modifier.size(9.dp))
                    }
                }
                theme.isPremium && !isPremiumUser -> {
                    Icon(
                        imageVector        = Icons.Default.Lock,
                        contentDescription = null,
                        tint               = theme.labelColor.copy(alpha = dimAlpha),
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
