@file:OptIn(ExperimentalMaterial3Api::class)
package com.thormakk.keyarmor.presentation.screens.plans

import com.thormakk.keyarmor.ui.theme.appButtonColors
import com.thormakk.keyarmor.presentation.components.CaptureAllowedBadge
import com.thormakk.keyarmor.presentation.components.ThemeTrialBadge
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.presentation.components.SectionTitleFontSize
import com.thormakk.keyarmor.presentation.components.sectionTitleColor
import com.thormakk.keyarmor.presentation.components.topBarColors
import com.thormakk.keyarmor.presentation.util.formatDateLocalized
import java.util.Locale
import com.thormakk.keyarmor.presentation.components.TextoDeUnaLinea

@Composable
fun MyPlanScreen(
    onBack: () -> Unit,
    onViewPlansClick: () -> Unit,
    onManageFamilyClick: () -> Unit = {},
    onManageGoogleAccount: () -> Unit = {},
    viewModel: MyPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]

    fun openPlayStoreSubscription() {
        val sku = uiState.productId
        val url = if (sku != null) {
            "https://play.google.com/store/account/subscriptions?sku=$sku&package=${context.packageName}"
        } else {
            "https://play.google.com/store/account/subscriptions?package=${context.packageName}"
        }
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
    }

    Scaffold(
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
                            text  = stringResource(R.string.my_plan_screen_title),
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 7.dp, vertical = 16.dp)
        ) {
            PlanSummaryCard(
                uiState     = uiState,
                locale      = locale,
                onManage    = { openPlayStoreSubscription() },
                onManageFamily = onManageFamilyClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            GoogleAccountCard(
                connectedEmail = uiState.driveAccount,
                onManage       = onManageGoogleAccount
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.currentPlan == UserPlan.FREE) {
                Button(
                    onClick  = onViewPlansClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp), colors = appButtonColors()
                ) {
                    Text(stringResource(R.string.my_plan_view_plans))
                }
            } else {
                Text(
                    text       = stringResource(R.string.my_plan_features_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                FeaturesList(planFeaturesFor(uiState.currentPlan))

                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick  = onViewPlansClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.my_plan_view_plans))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlanSummaryCard(
    uiState:        MyPlanUiState,
    locale:         Locale,
    onManage:       () -> Unit,
    onManageFamily: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = if (uiState.currentPlan == UserPlan.FAMILY) Icons.Default.FamilyRestroom else Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text       = planDisplayName(uiState.currentPlan),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            when (uiState.currentPlan) {
                UserPlan.FREE -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text  = stringResource(R.string.my_plan_free_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                UserPlan.PREMIUM, UserPlan.FAMILY -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    uiState.purchaseDateMs?.let {
                        DetailRow(stringResource(R.string.my_plan_purchase_date), formatDateLocalized(it, locale))
                    }
                    uiState.currentPrice?.let { price ->
                        val periodSuffix = when (uiState.period) {
                            SubscriptionPeriod.MONTHLY -> stringResource(R.string.my_plan_period_monthly)
                            SubscriptionPeriod.YEARLY  -> stringResource(R.string.my_plan_period_yearly)
                            null                        -> null
                        }
                        DetailRow(
                            stringResource(R.string.my_plan_current_price),
                            if (periodSuffix != null) "$price · $periodSuffix" else price
                        )
                    }
                    uiState.expirationDateMs?.let { exp ->
                        val formatted = formatDateLocalized(exp, locale)
                        DetailRow(
                            stringResource(R.string.my_plan_expiration_date),
                            formatted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = if (uiState.willRenew)
                                stringResource(R.string.my_plan_renews_on, formatted)
                            else
                                stringResource(R.string.my_plan_ends_on, formatted),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick  = onManage,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.my_plan_manage_subscription))
                    }

                    if (uiState.currentPlan == UserPlan.FAMILY) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick  = onManageFamily,
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(10.dp),
                            border   = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(R.string.my_plan_manage_family))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleAccountCard(
    connectedEmail: String?,
    onManage:       () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = if (connectedEmail != null) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                contentDescription = null,
                tint               = if (connectedEmail != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(R.string.settings_google_account),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = connectedEmail ?: stringResource(R.string.drive_sync_not_connected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick  = onManage,
                shape    = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text  = if (connectedEmail != null)
                        stringResource(R.string.action_manage)
                    else
                        stringResource(R.string.drive_sync_connect_google),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FeaturesList(features: List<PlanFeatureItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            features.forEachIndexed { index, feature ->
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment     = Alignment.Top,
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
                            tint               = MaterialTheme.colorScheme.primary,
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
                if (index != features.lastIndex) {
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun planDisplayName(plan: UserPlan): String = when (plan) {
    UserPlan.FREE     -> stringResource(R.string.plans_free_name)
    UserPlan.PREMIUM  -> stringResource(R.string.plans_premium_name)
    UserPlan.FAMILY   -> stringResource(R.string.plans_family_name)
}
