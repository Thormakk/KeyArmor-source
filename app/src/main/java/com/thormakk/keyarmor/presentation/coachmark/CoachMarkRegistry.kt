package com.thormakk.keyarmor.presentation.coachmark

import com.thormakk.keyarmor.R

data class CoachMarkSpec(
    val anchorId: String,
    val titleRes: Int,
    val bodyRes:  Int,
)

object CoachMarkRegistry {
    val byScreen: Map<String, List<CoachMarkSpec>> = mapOf(
        "vault_hub" to listOf(
            CoachMarkSpec("health", R.string.cm_vault_hub_health_t, R.string.cm_vault_hub_health_b),
            CoachMarkSpec("add_inline", R.string.cm_vault_hub_add_inline_t, R.string.cm_vault_hub_add_inline_b),
        ),
        "search" to listOf(
            CoachMarkSpec("field", R.string.cm_search_field_t, R.string.cm_search_field_b),
        ),
        "generator" to listOf(
            CoachMarkSpec("mode", R.string.cm_generator_mode_t, R.string.cm_generator_mode_b),
            CoachMarkSpec("actions", R.string.cm_generator_actions_t, R.string.cm_generator_actions_b),
        ),
        "settings" to listOf(
            CoachMarkSpec("disguise", R.string.cm_settings_disguise_t, R.string.cm_settings_disguise_b),
            CoachMarkSpec("security", R.string.cm_settings_security_t, R.string.cm_settings_security_b),
        ),
        "vault" to listOf(
            CoachMarkSpec("filters", R.string.cm_vault_filters_t, R.string.cm_vault_filters_b),
            CoachMarkSpec("multiselect", R.string.cm_vault_multiselect_t, R.string.cm_vault_multiselect_b),
        ),
        "two_factor" to listOf(
            CoachMarkSpec("code", R.string.cm_two_factor_code_t, R.string.cm_two_factor_code_b),
            CoachMarkSpec("fab", R.string.cm_two_factor_fab_t, R.string.cm_two_factor_fab_b),
        ),
        "credit_cards" to listOf(
            CoachMarkSpec("fab", R.string.cm_credit_cards_fab_t, R.string.cm_credit_cards_fab_b),
        ),
        "crypto_wallets" to listOf(
            CoachMarkSpec("refresh", R.string.cm_crypto_wallets_refresh_t, R.string.cm_crypto_wallets_refresh_b),
            CoachMarkSpec("fab", R.string.cm_crypto_wallets_fab_t, R.string.cm_crypto_wallets_fab_b),
        ),
        "secure_notes" to listOf(
            CoachMarkSpec("fab", R.string.cm_secure_notes_fab_t, R.string.cm_secure_notes_fab_b),
        ),
        "custom_entries" to listOf(
            CoachMarkSpec("fab", R.string.cm_custom_entries_fab_t, R.string.cm_custom_entries_fab_b),
        ),
        "identity_documents" to listOf(
            CoachMarkSpec("fab", R.string.cm_identity_documents_fab_t, R.string.cm_identity_documents_fab_b),
        ),
        "passkeys" to listOf(
            CoachMarkSpec("empty", R.string.cm_passkeys_empty_t, R.string.cm_passkeys_empty_b),
            CoachMarkSpec("list", R.string.cm_passkeys_list_t, R.string.cm_passkeys_list_b),
        ),
        "reminders" to listOf(
            CoachMarkSpec("calendar", R.string.cm_reminders_calendar_t, R.string.cm_reminders_calendar_b),
            CoachMarkSpec("fab", R.string.cm_reminders_fab_t, R.string.cm_reminders_fab_b),
        ),
        "archive" to listOf(
            CoachMarkSpec("empty", R.string.cm_archive_empty_t, R.string.cm_archive_empty_b),
            CoachMarkSpec("restore", R.string.cm_archive_restore_t, R.string.cm_archive_restore_b),
        ),
        "trash" to listOf(
            CoachMarkSpec("auto_empty", R.string.cm_trash_auto_empty_t, R.string.cm_trash_auto_empty_b),
        ),
        "audit" to listOf(
            CoachMarkSpec("hibp", R.string.cm_audit_hibp_t, R.string.cm_audit_hibp_b),
            CoachMarkSpec("score", R.string.cm_audit_score_t, R.string.cm_audit_score_b),
        ),
        "plans" to listOf(
            CoachMarkSpec("billing", R.string.cm_plans_billing_t, R.string.cm_plans_billing_b),
        ),
        "drive_sync" to listOf(
            CoachMarkSpec("connect", R.string.cm_drive_sync_connect_t, R.string.cm_drive_sync_connect_b),
            CoachMarkSpec("backup", R.string.cm_drive_sync_backup_t, R.string.cm_drive_sync_backup_b),
        ),
        "time_machine" to listOf(
            CoachMarkSpec("snapshot", R.string.cm_time_machine_snapshot_t, R.string.cm_time_machine_snapshot_b),
        ),
        "vault_management" to listOf(
            CoachMarkSpec("create", R.string.cm_vault_management_create_t, R.string.cm_vault_management_create_b),
        ),
        "family_admin" to listOf(
            CoachMarkSpec("invite", R.string.cm_family_admin_invite_t, R.string.cm_family_admin_invite_b),
        ),
        "travel_mode" to listOf(
            CoachMarkSpec("switch", R.string.cm_travel_mode_switch_t, R.string.cm_travel_mode_switch_b),
        ),
        "panic_pin" to listOf(
            CoachMarkSpec("mode", R.string.cm_panic_pin_mode_t, R.string.cm_panic_pin_mode_b),
        ),
        "calculator_disguise" to listOf(
            CoachMarkSpec("switch", R.string.cm_calculator_disguise_switch_t, R.string.cm_calculator_disguise_switch_b),
            CoachMarkSpec("code", R.string.cm_calculator_disguise_code_t, R.string.cm_calculator_disguise_code_b),
        ),
        "hidden_entries" to listOf(
            CoachMarkSpec("passphrase", R.string.cm_hidden_entries_passphrase_t, R.string.cm_hidden_entries_passphrase_b),
        ),
        "digital_inheritance" to listOf(
            CoachMarkSpec("add_heir", R.string.cm_digital_inheritance_add_heir_t, R.string.cm_digital_inheritance_add_heir_b),
        ),
        "import_export" to listOf(
            CoachMarkSpec("export", R.string.cm_import_export_export_t, R.string.cm_import_export_export_b),
            CoachMarkSpec("import", R.string.cm_import_export_import_t, R.string.cm_import_export_import_b),
        ),
        "notifications" to listOf(
            CoachMarkSpec("empty", R.string.cm_notifications_empty_t, R.string.cm_notifications_empty_b),
            CoachMarkSpec("multiselect", R.string.cm_notifications_multiselect_t, R.string.cm_notifications_multiselect_b),
        ),
    )

    fun forScreen(screenId: String): List<CoachMarkSpec> = byScreen[screenId].orEmpty()
}
