package com.thormakk.keyarmor.presentation.navigation

sealed class Screen(val route: String) {
    object Language : Screen("language")
    object Register : Screen("register")
    object Login          : Screen("login")
    object Lock           : Screen("lock")
    object ConnectGoogle  : Screen("connect_google")
    object OnboardingWelcome : Screen("onboarding_welcome")
    object VaultHub       : Screen("vault_hub")
    object Vault          : Screen("vault?strengthFilter={strengthFilter}") {
        const val PLAIN_ROUTE = "vault"
        fun withFilter(filter: String) = "vault?strengthFilter=$filter"
    }
    object Generator      : Screen("generator")
    object Settings       : Screen("settings?highlight={highlight}") {
        const val PLAIN_ROUTE = "settings"
        fun routeWithHighlight(key: String) = "settings?highlight=$key"
    }
    object ThemeSelector   : Screen("theme_selector")
    object LanguagePicker  : Screen("language_picker")
    object ScreenCapture   : Screen("screen_capture")
    object VoiceDictation  : Screen("voice_dictation")
    object Archive : Screen("archive?scrollToId={scrollToId}") {
        const val PLAIN_ROUTE = "archive"
        fun withScrollTo(id: String) = "archive?scrollToId=$id"
    }
    object Trash : Screen("trash?scrollToId={scrollToId}") {
        const val PLAIN_ROUTE = "trash"
        fun withScrollTo(id: String) = "trash?scrollToId=$id"
    }
    object PanicPin       : Screen("panic_pin")
    object Calculator          : Screen("calculator")
    object CalculatorDisguise  : Screen("calculator_disguise")
    object Audit          : Screen("audit")
    object TravelMode     : Screen("travel_mode")
    object ChangeMasterPassword : Screen("change_master_password")
    object HiddenEntries  : Screen("hidden_entries")
    object AutofillSettings : Screen("autofill_settings")
    object Donate         : Screen("donate")
    object AppHub         : Screen("app_hub")
    object LegalDoc       : Screen("legal_doc/{docKey}") {
        fun createRoute(docKey: String) = "legal_doc/$docKey"
    }

    object AddEditEntry : Screen("edit_entry/{id}?cloneOf={cloneOf}&prefillUrl={prefillUrl}") {
        fun createRoute(id: String, cloneOf: String = "", prefillUrl: String = ""): String {
            val params = buildList {
                if (cloneOf.isNotEmpty()) add("cloneOf=$cloneOf")
                if (prefillUrl.isNotEmpty()) add("prefillUrl=$prefillUrl")
            }
            return if (params.isEmpty()) "edit_entry/$id" else "edit_entry/$id?" + params.joinToString("&")
        }
    }
    object ShareEntry : Screen("share_entry/{type}/{id}") {
        fun createRoute(type: String, id: String) = "share_entry/$type/$id"
    }

    object ReceiveEntry : Screen("receive_entry")

    object BankAccounts : Screen("bank_accounts")

    object AddEditBankAccount : Screen("edit_bank_account/{id}?cloneOf={cloneOf}") {
        fun createRoute(id: String, cloneOf: String = "") =
            if (cloneOf.isEmpty()) "edit_bank_account/$id" else "edit_bank_account/$id?cloneOf=$cloneOf"
    }

    object LinkedEntry : Screen("linked_entry/{id}") {
        fun createRoute(id: String) = "linked_entry/$id"
    }

    object CustomEntries : Screen("custom_entries")

    object AddEditCustomEntry : Screen("edit_custom_entry/{id}?cloneOf={cloneOf}") {
        fun createRoute(id: String, cloneOf: String = "") =
            if (cloneOf.isEmpty()) "edit_custom_entry/$id" else "edit_custom_entry/$id?cloneOf=$cloneOf"
    }

    object SecureNotes : Screen("secure_notes")

    object AddEditSecureNote : Screen("edit_secure_note/{id}?cloneOf={cloneOf}") {
        fun createRoute(id: String, cloneOf: String = "") =
            if (cloneOf.isEmpty()) "edit_secure_note/$id" else "edit_secure_note/$id?cloneOf=$cloneOf"
    }

    object CreditCards : Screen("credit_cards")

    object AddEditCreditCard : Screen("edit_credit_card/{id}?cloneOf={cloneOf}") {
        fun createRoute(id: String, cloneOf: String = "") =
            if (cloneOf.isEmpty()) "edit_credit_card/$id" else "edit_credit_card/$id?cloneOf=$cloneOf"
    }

    object CryptoWallets : Screen("crypto_wallets")

    object AddEditCryptoWallet : Screen("edit_crypto_wallet/{id}?cloneOf={cloneOf}") {
        fun createRoute(id: String, cloneOf: String = "") =
            if (cloneOf.isEmpty()) "edit_crypto_wallet/$id" else "edit_crypto_wallet/$id?cloneOf=$cloneOf"
    }

    object Plans : Screen("plans")

    object MyPlan : Screen("my_plan")

    object Search    : Screen("search")

    object ImportExport   : Screen("import_export")
    object PartialExport  : Screen("partial_export")

    object DriveSync               : Screen("drive_sync")
    object BackupKey               : Screen("backup_key")

    object DigitalInheritance      : Screen("digital_inheritance")
    object HeirConsent             : Screen("heir_consent")

    object FamilyAdmin : Screen("family_admin")
    object FamilyMemberConsent : Screen("family_consent?adminEmail={adminEmail}&token={token}") {
        fun createRoute(adminEmail: String, token: String): String {
            val encAdmin = java.net.URLEncoder.encode(adminEmail, "UTF-8")
            val encToken = java.net.URLEncoder.encode(token, "UTF-8")
            return "family_consent?adminEmail=$encAdmin&token=$encToken"
        }
    }

    object VaultManagement : Screen("vault_management")

    object TimeMachine : Screen("time_machine")
    object SnapshotPreview : Screen("snapshot_preview/{id}") {
        fun createRoute(id: String): String = "snapshot_preview/$id"
    }

    object TwoFactor : Screen("two_factor")

    object AddEditTwoFactor : Screen("edit_two_factor/{id}") {
        fun createRoute(id: String) = "edit_two_factor/$id"
    }

    object Attachments : Screen("attachments/{entryId}") {
        fun createRoute(entryId: String) = "attachments/$entryId"
    }

    object PasswordHistory : Screen("password_history/{entryId}") {
        fun createRoute(entryId: String) = "password_history/$entryId"
    }

    object Notifications : Screen("notifications")

    object PasswordDetail     : Screen("password_detail/{id}") {
        fun createRoute(id: String): String = "password_detail/$id"
    }
    object BankAccountDetail  : Screen("bank_account_detail/{id}") {
        fun createRoute(id: String): String = "bank_account_detail/$id"
    }
    object CreditCardDetail   : Screen("credit_card_detail/{id}") {
        fun createRoute(id: String): String = "credit_card_detail/$id"
    }
    object CryptoWalletDetail : Screen("crypto_wallet_detail/{id}") {
        fun createRoute(id: String): String = "crypto_wallet_detail/$id"
    }
    object SecureNoteHistory : Screen("secure_note_history/{noteId}") {
        fun createRoute(noteId: String) = "secure_note_history/$noteId"
    }

    object SecureNoteDetail   : Screen("secure_note_detail/{id}") {
        fun createRoute(id: String): String = "secure_note_detail/$id"
    }
    object CustomEntryDetail  : Screen("custom_entry_detail/{id}") {
        fun createRoute(id: String): String = "custom_entry_detail/$id"
    }
    object TwoFactorRecoveryCodes : Screen("two_factor_recovery/{id}") {
        fun createRoute(id: String): String = "two_factor_recovery/$id"
    }
    object TwoFactorDetail    : Screen("two_factor_detail/{id}") {
        fun createRoute(id: String): String = "two_factor_detail/$id"
    }

    object Passkeys : Screen("passkeys")
    object PasskeyDetail : Screen("passkey_detail/{id}") {
        fun createRoute(id: String): String = "passkey_detail/$id"
    }
    object PasskeyEdit : Screen("passkey_edit/{id}") {
        fun createRoute(id: String) = "passkey_edit/$id"
    }

    object IdentityDocuments : Screen("identity_documents")
    object AddEditIdentityDocument : Screen("edit_identity_document/{id}?cloneOf={cloneOf}") {
        fun createRoute(id: String, cloneOf: String = "") =
            if (cloneOf.isEmpty()) "edit_identity_document/$id" else "edit_identity_document/$id?cloneOf=$cloneOf"
    }
    object IdentityDocumentDetail : Screen("identity_document_detail/{id}") {
        fun createRoute(id: String): String = "identity_document_detail/$id"
    }

    object Reminders : Screen("reminders")
    object ReminderAddEdit : Screen("reminder_edit/{id}?initialDate={initialDate}") {
        fun createRoute(id: String, initialDateMs: Long? = null): String {
            val base = "reminder_edit/$id"
            return if (initialDateMs != null) "$base?initialDate=$initialDateMs" else base
        }
        const val NEW_ID = "new"
    }

    object Vehicles : Screen("vehicles")
    object AddEditVehicle : Screen("edit_vehicle/{id}?cloneOf={cloneOf}") {
        fun createRoute(id: String, cloneOf: String = "") =
            if (cloneOf.isEmpty()) "edit_vehicle/$id" else "edit_vehicle/$id?cloneOf=$cloneOf"
    }
    object VehicleDetail : Screen("vehicle_detail/{id}") {
        fun createRoute(id: String): String = "vehicle_detail/$id"
    }
}
