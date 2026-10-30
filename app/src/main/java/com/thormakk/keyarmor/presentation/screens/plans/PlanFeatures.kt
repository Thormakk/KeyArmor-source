package com.thormakk.keyarmor.presentation.screens.plans

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.UserPlan

data class PlanFeatureItem(val text: String, val isHeader: Boolean = false)

@Composable
fun freePlanFeatures(): List<PlanFeatureItem> = listOf(
    PlanFeatureItem(stringResource(R.string.plans_free_feat_entries)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_types)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_vaults)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_totp_limit)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_encryption)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_biometric)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_generator)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_autolock)),
    PlanFeatureItem(stringResource(R.string.plans_feat_share_encrypted)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_import_export)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_tags_categories)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_languages)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_clipboard)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_autofill)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_widget)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_crypto_balance)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_screen_capture)),
    PlanFeatureItem(stringResource(R.string.plans_free_feat_voice_dictation))
)

@Composable
fun premiumPlanFeatures(): List<PlanFeatureItem> = listOf(
    PlanFeatureItem(stringResource(R.string.plans_feat_everything_free), isHeader = true),

    PlanFeatureItem(stringResource(R.string.plans_feat_group_keep), isHeader = true),
    PlanFeatureItem(stringResource(R.string.plans_feat_drive_backup)),
    PlanFeatureItem(stringResource(R.string.plans_feat_time_machine)),
    PlanFeatureItem(stringResource(R.string.plans_feat_password_history)),
    PlanFeatureItem(stringResource(R.string.plans_feat_archive)),

    PlanFeatureItem(stringResource(R.string.plans_feat_group_legacy), isHeader = true),
    PlanFeatureItem(stringResource(R.string.plans_feat_digital_inheritance)),

    PlanFeatureItem(stringResource(R.string.plans_feat_group_privacy), isHeader = true),
    PlanFeatureItem(stringResource(R.string.plans_feat_hidden_entries)),
    PlanFeatureItem(stringResource(R.string.plans_feat_calculator_disguise)),
    PlanFeatureItem(stringResource(R.string.plans_feat_panic_pin)),
    PlanFeatureItem(stringResource(R.string.plans_feat_travel_mode)),
    PlanFeatureItem(stringResource(R.string.plans_feat_biometric_gates)),
    PlanFeatureItem(stringResource(R.string.plans_feat_multiple_vaults)),

    PlanFeatureItem(stringResource(R.string.plans_feat_group_more), isHeader = true),
    PlanFeatureItem(stringResource(R.string.plans_feat_unlimited_totp)),
    PlanFeatureItem(stringResource(R.string.plans_feat_all_entry_types)),
    PlanFeatureItem(stringResource(R.string.plans_feat_breach_monitoring)),
    PlanFeatureItem(stringResource(R.string.plans_feat_full_audit)),
    PlanFeatureItem(stringResource(R.string.plans_feat_expiry_alerts)),
    PlanFeatureItem(stringResource(R.string.plans_feat_reminders)),
    PlanFeatureItem(stringResource(R.string.plans_feat_attachments_detail)),
    PlanFeatureItem(stringResource(R.string.plans_feat_unlimited_tags)),
    PlanFeatureItem(stringResource(R.string.plans_feat_crypto_secrets)),
    PlanFeatureItem(stringResource(R.string.plans_feat_themes_detail)),
    PlanFeatureItem(stringResource(R.string.plans_feat_priority_support))
)

@Composable
fun familyPlanFeatures(): List<PlanFeatureItem> = listOf(
    PlanFeatureItem(stringResource(R.string.plans_feat_everything_premium), isHeader = true),
    PlanFeatureItem(stringResource(R.string.plans_family_feat_members)),
    PlanFeatureItem(stringResource(R.string.plans_family_feat_premium_access)),
    PlanFeatureItem(stringResource(R.string.plans_family_feat_encrypted)),
    PlanFeatureItem(stringResource(R.string.plans_family_feat_drive)),
    PlanFeatureItem(stringResource(R.string.plans_family_feat_admin_only)),
    PlanFeatureItem(stringResource(R.string.plans_family_feat_revoke))
)

@Composable
fun planFeaturesFor(plan: UserPlan): List<PlanFeatureItem> = when (plan) {
    UserPlan.FREE     -> freePlanFeatures()
    UserPlan.PREMIUM  -> premiumPlanFeatures()
    UserPlan.FAMILY   -> familyPlanFeatures()
}
