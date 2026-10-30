@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.plans

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import androidx.activity.compose.LocalActivity
import androidx.credentials.CredentialManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.presentation.coachmark.coachMarkAnchor
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.GoogleConnectResult
import com.thormakk.keyarmor.presentation.util.requestGoogleAccountConnect
import kotlinx.coroutines.launch
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun PlansScreen(
    onBack: () -> Unit,
    viewModel: PlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val verificationFailedMsg = stringResource(R.string.error_google_verification_failed)

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            viewModel.clearSnackbar()
        }
    }

    fun purchaseWithGoogleGate(purchaseAction: () -> Unit) {
        if (uiState.hasConnectedAccount) {
            purchaseAction()
            return
        }
        scope.launch {
            when (val result = requestGoogleAccountConnect(context, credentialManager)) {
                is GoogleConnectResult.Success -> {
                    val connected = viewModel.connectAccount(result.email, result.idToken)
                    if (connected) purchaseAction()
                }
                is GoogleConnectResult.Cancelled -> Unit
                is GoogleConnectResult.Error -> {
                    snackbarHostState.showSnackbar(verificationFailedMsg)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 40.dp)) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
                TopAppBar(
                    actions = {
                        CaptureAllowedBadge()
                        ThemeTrialBadge()
                    },
                    windowInsets = WindowInsets(0),
                    title = {
                        TextoDeUnaLinea(
                            text  = stringResource(R.string.plans_screen_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = SectionTitleFontSize),
                            color = sectionTitleColor()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector        = Icons.Default.ChevronLeft,
                                contentDescription = stringResource(R.string.action_back),
                                modifier           = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = topBarColors()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 7.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Icon(
                imageVector        = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text       = stringResource(R.string.plans_headline),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text      = stringResource(R.string.plans_headline_sub),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            FreePlanCard(isCurrentPlan = uiState.currentPlan == UserPlan.FREE)

            Spacer(modifier = Modifier.height(14.dp))

            PremiumPlanCard(
                period           = uiState.selectedBillingPeriod,
                monthlyPrice     = uiState.premiumMonthlyPrice,
                yearlyPrice      = uiState.premiumYearlyPrice,
                savingLabel      = uiState.premiumYearlySaving,
                isCurrentPlan    = uiState.currentPlan == UserPlan.PREMIUM,
                isBillingReady   = uiState.isBillingReady,
                isPurchasing     = uiState.isPurchasing,
                onPurchase       = { purchaseWithGoogleGate { activity?.let { viewModel.purchasePremium(it) } } },
                onTogglePeriod   = viewModel::toggleBillingPeriod
            )

            Spacer(modifier = Modifier.height(14.dp))

            FamilyPlanCard(
                price          = uiState.familyYearlyPrice,
                isCurrentPlan  = uiState.currentPlan == UserPlan.FAMILY,
                isBillingReady = uiState.isBillingReady,
                isPurchasing   = uiState.isPurchasing,
                onPurchase     = { purchaseWithGoogleGate { activity?.let { viewModel.purchaseFamily(it) } } }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text      = stringResource(R.string.plans_legal_note),
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BillingPeriodToggle(
    selected:    BillingPeriod,
    savingLabel: String?,
    onSelect:    (BillingPeriod) -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
            .coachMarkAnchor("plans", "billing"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleOption(
            label      = stringResource(R.string.plans_toggle_monthly),
            isSelected = selected == BillingPeriod.MONTHLY,
            onClick    = { onSelect(BillingPeriod.MONTHLY) }
        )
        ToggleOption(
            label      = stringResource(R.string.plans_toggle_yearly),
            badge      = savingLabel,
            isSelected = selected == BillingPeriod.YEARLY,
            onClick    = { onSelect(BillingPeriod.YEARLY) }
        )
    }
}

@Composable
private fun ToggleOption(
    label:     String,
    badge:     String? = null,
    isSelected: Boolean,
    onClick:   () -> Unit
) {
    val primary   = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) primary else Color.Transparent,
        animationSpec = tween(200),
        label         = "toggleBg"
    )
    val textColor by animateColorAsState(
        targetValue   = if (isSelected) onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label         = "toggleText"
    )

    TextButton(
        onClick  = onClick,
        shape    = RoundedCornerShape(50),
        colors   = ButtonDefaults.textButtonColors(containerColor = bgColor),
        modifier = Modifier.defaultMinSize(minWidth = 120.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text       = label,
                color      = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) onPrimary.copy(alpha = 0.22f)
                            else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                ) {
                    Text(
                        text       = badge,
                        style      = MaterialTheme.typography.labelSmall,
                        color      = if (isSelected) onPrimary
                                     else MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FreePlanCard(isCurrentPlan: Boolean) {
    PlanCard(
        name          = stringResource(R.string.plans_free_name),
        badge         = null,
        priceText     = "$0",
        priceSub      = stringResource(R.string.plans_free_price_sub),
        isHighlighted = false,
        isCurrentPlan = isCurrentPlan,
        features       = freePlanFeatures(),
        ctaLabel       = if (isCurrentPlan) stringResource(R.string.plans_cta_current) else null,
        isBillingReady = true,
        isPurchasing   = false,
        onPurchase     = {}
    )
}

@Composable
private fun PremiumPlanCard(
    period:           BillingPeriod,
    monthlyPrice:     String?,
    yearlyPrice:      String?,
    savingLabel:      String?,
    isCurrentPlan:    Boolean,
    isBillingReady:   Boolean,
    isPurchasing:     Boolean,
    onPurchase:       () -> Unit,
    onTogglePeriod:   (BillingPeriod) -> Unit
) {
    val displayPrice = when (period) {
        BillingPeriod.MONTHLY -> monthlyPrice ?: "…"
        BillingPeriod.YEARLY  -> yearlyPrice  ?: "…"
    }
    val monthlySuffix = stringResource(R.string.plans_period_monthly_suffix)
    val yearlySuffix  = stringResource(R.string.plans_period_yearly_suffix)
    val priceSuffix = when (period) {
        BillingPeriod.MONTHLY -> monthlySuffix
        BillingPeriod.YEARLY  -> if (savingLabel != null) "$yearlySuffix · $savingLabel" else yearlySuffix
    }

    PlanCard(
        name          = stringResource(R.string.plans_premium_name),
        badge         = stringResource(R.string.plans_most_popular),
        priceText     = displayPrice,
        priceSub      = priceSuffix,
        isHighlighted = true,
        isCurrentPlan = isCurrentPlan,
        extraHeader   = {
            BillingPeriodToggle(
                selected    = period,
                savingLabel = savingLabel,
                onSelect    = onTogglePeriod
            )
        },
        features       = premiumPlanFeatures(),
        ctaLabel       = if (isCurrentPlan) stringResource(R.string.plans_cta_current)
                         else               stringResource(R.string.plans_cta_premium),
        isBillingReady = isBillingReady,
        isPurchasing   = isPurchasing,
        onPurchase     = onPurchase
    )
}

@Composable
private fun FamilyPlanCard(
    price:          String?,
    isCurrentPlan:  Boolean,
    isBillingReady: Boolean,
    isPurchasing:   Boolean,
    onPurchase:     () -> Unit
) {
    PlanCard(
        name          = stringResource(R.string.plans_family_name),
        badge         = stringResource(R.string.plans_family_badge),
        priceText     = price ?: "…",
        priceSub      = stringResource(R.string.plans_family_price_sub),
        isHighlighted = false,
        isCurrentPlan = isCurrentPlan,
        features       = familyPlanFeatures(),
        ctaLabel       = if (isCurrentPlan) stringResource(R.string.plans_cta_current)
                         else               stringResource(R.string.plans_cta_family),
        isBillingReady = isBillingReady,
        isPurchasing   = isPurchasing,
        onPurchase     = onPurchase,
        ctaIsWarm      = true
    )
}


@Composable
private fun PlanCard(
    name:           String,
    badge:          String?,
    priceText:      String,
    priceSub:       String,
    isHighlighted:  Boolean,
    isCurrentPlan:  Boolean,
    features:       List<PlanFeatureItem>,
    ctaLabel:       String?,
    isBillingReady: Boolean,
    isPurchasing:   Boolean,
    onPurchase:     () -> Unit,
    ctaIsWarm:      Boolean = false,
    extraHeader:    (@Composable () -> Unit)? = null
) {
    val primary   = MaterialTheme.colorScheme.primary
    val tertiary  = MaterialTheme.colorScheme.tertiary

    val borderColor = when {
        isHighlighted -> primary
        ctaIsWarm     -> tertiary
        else          -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }
    val containerColor = when {
        isHighlighted -> primary.copy(alpha = 0.06f)
        ctaIsWarm     -> tertiary.copy(alpha = 0.05f)
        else          -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .border(
                width = if (isHighlighted || ctaIsWarm) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = name,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = when {
                        isHighlighted -> primary
                        ctaIsWarm     -> tertiary
                        else          -> MaterialTheme.colorScheme.onSurface
                    }
                )
                if (isCurrentPlan) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text       = stringResource(R.string.plans_badge_active),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else if (badge != null) {
                    val badgeBg = if (ctaIsWarm) tertiary else primary
                    Surface(shape = RoundedCornerShape(20.dp), color = badgeBg) {
                        Text(
                            text       = badge,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (extraHeader != null) {
                Spacer(modifier = Modifier.height(14.dp))
                extraHeader()
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text       = priceText,
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text     = priceSub,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            HorizontalDivider(
                modifier  = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            )

            features.forEach { feature ->
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (feature.isHeader) {
                        Spacer(modifier = Modifier.size(18.dp))
                        Text(
                            text       = feature.text,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Icon(
                            imageVector        = Icons.Default.Check,
                            contentDescription = null,
                            tint               = when {
                                isHighlighted -> primary
                                ctaIsWarm     -> tertiary
                                else          -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier           = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text  = feature.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (ctaLabel != null) {
                Spacer(modifier = Modifier.height(20.dp))

                if (isCurrentPlan) {
                    OutlinedButton(
                        onClick  = {},
                        enabled  = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(ctaLabel)
                    }
                } else {
                    val btnColors = when {
                        ctaIsWarm     -> ButtonDefaults.buttonColors(containerColor = tertiary)
                        isHighlighted -> appButtonColors()
                        else          -> ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                    Button(
                        onClick  = onPurchase,
                        enabled  = isBillingReady && !isPurchasing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape  = RoundedCornerShape(10.dp),
                        colors = btnColors
                    ) {
                        if (isPurchasing) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text       = ctaLabel,
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
