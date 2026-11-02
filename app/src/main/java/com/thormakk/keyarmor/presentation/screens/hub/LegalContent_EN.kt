package com.thormakk.keyarmor.presentation.screens.hub

fun getLegalDocContentEN(docKey: String): List<LegalSection> = when (docKey) {
    "privacy"    -> privacyEN()
    "terms"      -> termsEN()
    "legal"      -> legalEN()
    "license"    -> licenseEN()
    "opensource" -> opensourceEN()
    "delete"     -> deleteDataEN()
    else         -> listOf(LegalSection("Document not found", ""))
}

private fun privacyEN() = listOf(
    LegalSection("Privacy Policy", ""),
    LegalSection("Data Collection", """
KeyArmor does not collect, transmit or share any personal data to external servers. All information you enter in the app stays exclusively on your device, unless you explicitly enable Google Drive synchronization.

KeyArmor does not collect your email address or any account credentials for the core vault, autofill, security audit or breach-monitoring features — those work entirely without any account. If you choose to enable Digital Inheritance or the Family Plan, those features require Google Sign-In: your Google account email is then stored on Google Firebase to identify you and match you with the heirs or family members you designate, and to send the corresponding notification emails through our email provider (Brevo). This email is used exclusively for those two features and is never sold or shared with anyone else. Outside of Digital Inheritance and the Family Plan, the only personal identifier is an optional name or nickname you may set during setup, which is stored exclusively on your device in encrypted form.
    """.trimIndent()),
    LegalSection("Local Storage", """
All your data (passwords, passkeys, notes, cards, bank accounts, crypto wallets, attachments and password history) is stored encrypted using AES-256-GCM with a key derived from your master password using Argon2id, which never leaves your device. If you turn on biometric unlock, a copy of that key is also protected by an Android Keystore key on your device. No data leaves your device without your explicit consent.
    """.trimIndent()),
    LegalSection("Master Password", """
Your master password is never stored in plain text or in any recoverable form. It is used solely to derive the vault encryption key via Argon2id. If you lose your master password, your data cannot be recovered.
    """.trimIndent()),
    LegalSection("Biometrics", """
Biometric authentication in KeyArmor is managed entirely by the Android operating system via the BiometricPrompt API. KeyArmor does not access, store or process biometric data at any time.

Biometrics are used in two contexts within the app:

• Vault unlock: to authenticate access to the app at startup, available on all plans.
• Advanced Biometric Protection (Premium): an optional gate that requires biometric verification before revealing sensitive fields in specific vault sections (passwords, credit cards, custom fields). Configurable in Settings — supports per-access or session mode with a customizable timeout. The same privacy guarantees apply: no biometric data is stored or processed by KeyArmor.
    """.trimIndent()),
    LegalSection("Google Drive Synchronization", """
Google Drive sync is entirely optional. When enabled, KeyArmor:

• Requests a single, minimal permission (DRIVE_APPDATA) to store your own encrypted backups and Time Machine restore points in a private, per-app folder in your Google Drive. This folder is hidden — no other app, and not you while browsing Drive, can see or modify it.
• Encrypts the vault with a 12-word backup key before uploading it. The key is created on your device and only you have it: neither Google nor the developer can read the file or recover the key.
• If you save the PDF of your backup key to Google Docs, Drive or another app, you do it yourself through the Android share screen; that app is governed by its own privacy policy.
• If you change your key, you can keep the previous ones, encrypted on your phone; the kit PDF may include them. KeyArmor does not send them to any server.
• Never accesses any other Drive file, never creates visible folders, and never shares files.
• Optionally keeps a second local copy of the backup in your device's Downloads/KeyArmor/ folder, which never leaves the device.
• On-demand sync is available on all plans (triggered manually). Automatic daily sync is available on Premium.

Digital Inheritance and the Family Plan use Google Firebase, not your Google Drive. Firebase handles consent, invitations, and notifications. The inheritance vault is end-to-end encrypted on your device using the heir's public key, and the resulting encrypted block is uploaded to the developer's private Firebase Cloud Storage bucket via short-lived (15-minute) signed links. The server stores that block but never receives the heir's key, so it cannot read it.

You can disconnect your Google account at any time from the sync screen.
    """.trimIndent()),
    LegalSection("Attachments", """
The attachments feature lets you associate files (up to 5 per entry, max 5 MB each) with your vault entries. All attachments are encrypted with AES-256-GCM before being saved to the device's internal storage. If you enable Google Drive sync, your attachments and document photos are included in the backup you upload, encrypted with your backup key. They are never sent to any other service.
    """.trimIndent()),
    LegalSection("Password History", """
KeyArmor keeps a local history of previous passwords for each entry. This history is stored encrypted on the device and is never transmitted to any external server.
    """.trimIndent()),
    LegalSection("In-App Notifications", """
KeyArmor generates internal notifications to alert you about important security events:

• Expiry warnings: sent when one or more passwords are approaching their expiry date.
• Expired passwords: sent when one or more passwords have already passed their expiry date.
• Breach alerts: sent when your passwords are detected in known data breaches.

These three notification types are generated entirely on your device and are never transmitted to external servers. If you use Digital Inheritance or the Family Plan, you'll also receive notifications about those features (an heir accepting, a family member being added, etc.); those are triggered by Google Firebase and delivered via Firebase Cloud Messaging, since they depend on events initiated by another account. All notifications, regardless of origin, are automatically deleted after 30 days. You can delete any notification by swiping it sideways, or clear all at once from the Notifications screen. Push notifications require system permission to be sent.
    """.trimIndent()),
    LegalSection("Security Audit", """
The security audit feature analyzes your passwords locally on the device. Common password checking is done by comparing against a predefined list stored within the app itself, with no queries to external services.
    """.trimIndent()),
    LegalSection("Breach Monitoring (HIBP)", """
KeyArmor can check whether your passwords have appeared in known data breaches using the Have I Been Pwned (HIBP) service, operated by security researcher Troy Hunt.

This check uses a k-anonymity model: only the first 5 characters of a SHA-1 hash of your password are sent to the HIBP API. The full hash is never transmitted. Matching is performed locally on your device. No usernames, email addresses or account associations are ever sent. HIBP responses are padded with decoy hashes so that even network traffic analysis reveals nothing about the password being checked.
    """.trimIndent()),
    LegalSection("Autofill Service", """
KeyArmor includes an optional Android Autofill service. When enabled in system settings, it reads the app package name or website URL of the currently active login screen in order to suggest matching credentials from your vault. This matching is performed entirely on-device. No app names, website URLs or credentials are transmitted to any external server. The autofill service does not read the content of other apps beyond what is strictly necessary to identify the login context.
    """.trimIndent()),
    LegalSection("Crypto Wallet Live Balance", """
When you store a crypto wallet entry and enable live balance monitoring, KeyArmor queries public blockchain nodes or APIs to retrieve the balance associated with the wallet address you entered. Only the wallet address is transmitted — no private keys, seed phrases or personal data are sent. These queries go directly to public infrastructure; KeyArmor does not operate any intermediate server.
    """.trimIndent()),
    LegalSection("Peer-to-Peer Entry Sharing", """
KeyArmor allows you to share individual vault entries directly with other KeyArmor users via QR code or encrypted text. This transfer is end-to-end encrypted and peer-to-peer — no server or intermediary is involved. You are solely responsible for choosing who you share entries with and for the security of the channel used to transmit the encrypted text (if not using QR directly).
    """.trimIndent()),
    LegalSection("Third-Party Libraries", """
KeyArmor uses open-source libraries (see Open Source Notice). None of these libraries include telemetry, analytics or data collection mechanisms in the configuration used by this app.

KeyArmor also relies on two third-party services for specific features: RevenueCat, which verifies your Premium subscription status and receives only purchase-related information (never your vault, master password or recovery key); and Brevo, which sends the transactional emails required by Digital Inheritance and the Family Plan (invitations, consent confirmations, notifications) and, for that purpose, receives the email address of the people involved in those features. Neither service is used for advertising or behavioral analytics.
    """.trimIndent()),    LegalSection("Home Screen Widget", """
KeyArmor includes an optional Android home screen widget that displays the total number of entries stored in your vault. This widget accesses only local device data. It does not transmit any information to external servers. The entry count is aggregated data that does not reveal the contents of any individual entry. You can choose whether to place the widget on your home screen.
    """.trimIndent()),

    LegalSection("Permissions", """
KeyArmor requests only the permissions necessary for its operation:

• Biometrics (optional): for fingerprint unlock.
• Internet (optional): used only for features you explicitly enable — Google Drive sync, Digital Inheritance and the Family Plan (via Google Firebase), live crypto balance queries, Have I Been Pwned breach checks, and subscription verification (Google Play Billing / RevenueCat).
• File access (optional): for vault import/export and adding attachments, only when you initiate it.
• Camera (optional): to scan QR codes when setting up two-factor authentication (2FA/TOTP) or when receiving shared entries. Activated only upon explicit user request.
• Autofill service (optional): enabled manually in Android system settings. Used solely to suggest credentials in login screens of other apps.
• Microphone (optional): only for voice dictation in Secure Notes. Speech is recognized on your device; KeyArmor does not record, store or send audio to any server.
• Calendar, read-only (optional): used by Safe Travel Mode to look for trips in your events. Events are processed on your device and never transmitted.
• Notifications and exact alarms (optional): for security alerts and for the reminders you schedule. The device boot event is used to reschedule them after a restart.
• Network state (optional): only to know whether you are on mobile data or Wi-Fi, so that large Google Drive backups wait for Wi-Fi. No information about your network is collected or sent.

KeyArmor does not request access to contacts, location or any other personal data not mentioned above.
    """.trimIndent()),
    LegalSection("Other services that receive data", """
Besides the services described above, KeyArmor uses the following, only for the functions that need them:

• App integrity check (Firebase App Check / Google Play Integrity): Google provides an attestation of your device so that our server accepts only requests from the genuine app.
• RevenueCat: in addition to purchase information, it receives the identifier of your Google account (not your email address) as a user ID, to link your subscription across devices.
• CoinGecko: when you use live crypto prices, the tickers of your portfolio are requested from CoinGecko, which can see your IP address. The blockchain explorers and nodes mentioned above can also see it.
• Google Fonts: the app downloads its Inter and JetBrains Mono typefaces through Google Play services, so Google can see your IP address when they are requested.
• Google Play services trusted time: used to detect tampering with the device clock. No personal data is sent.
• Text recognition (OCR) of documents runs on your device with ML Kit. No image is sent to any server.
• Ko-fi: only if you open the donation link, in which case their own privacy policy applies.

Our servers may temporarily log your IP address to limit abuse.
    """.trimIndent()),
    LegalSection("Passkeys", """
KeyArmor can act as an Android passkey provider (Credential Manager), only if you choose it in your system settings. When you create or use a passkey, KeyArmor reads the domain of the site or app that requests it, to tie the passkey to that site and to sign the challenge the site sends. The private key is generated on your device and stored encrypted along with the rest of your vault; it is not sent to any server or third party. It only leaves your device inside the backups you create yourself (for example, the encrypted backup on Google Drive). To sign with a passkey, KeyArmor asks you to verify your identity with your fingerprint or your master password. The site you sign in to receives only the public key and the signature, as with any passkey.
    """.trimIndent()),
    LegalSection("Data Deletion and Retention", """
Your vault is kept on your device (and in your Google Drive, if you turn on backup) until you delete it. On our servers there is only coordination data: the push-notification token identified by your email and, if you use Digital Inheritance or the Family Plan, the invitation and consent records and the encrypted inheritance vault block. They are kept while you use those features or until you ask for them to be deleted. You can delete them from Settings → Delete all my data, or by writing to support@thormakk.dev from your Google account; we process those requests within 30 days. The full details are on the “Delete Account & Data” page of our website.
    """.trimIndent()),
    LegalSection("Children", """
KeyArmor is not directed to children under 13 (or the minimum age your country's law sets for digital consent), and we do not knowingly collect data from children. If you believe a child has given us data, write to support@thormakk.dev and we will delete it.
    """.trimIndent()),
    LegalSection("Contact", "For any questions related to this policy, you can write to support@thormakk.dev"),
    LegalSection("Last Updated", "October 2026 (rev. 8)")
)

private fun termsEN() = listOf(
    LegalSection("Terms and Conditions", ""),
    LegalSection("Acceptance", """
By installing or using KeyArmor you accept these terms in their entirety. If you disagree with any part, do not use the app.
    """.trimIndent()),
    LegalSection("Permitted Use", """
KeyArmor is designed for personal use as a password manager and secure vault. Use for illegal activities, storing information that facilitates unlawful acts, or attempting to reverse-engineer the app is prohibited.
    """.trimIndent()),
    LegalSection("User Responsibility", """
You are solely responsible for:

• The security of your master password. If lost, there is no recovery mechanism and your data will be permanently inaccessible.
• The 12-word backup key (if you use that feature). If you lose it and also lose your phone, your Drive backups cannot be opened, not even by the developer.
• Maintaining up-to-date backups of your vault via the export function or Drive sync.
    """.trimIndent()),
    LegalSection("Sync Functionality", """
Google Drive sync is an auxiliary backup feature. It does not replace good personal security practices. The developer does not guarantee continuous availability of this feature, as it depends on third-party services (Google).
    """.trimIndent()),
    LegalSection("Premium Subscriptions", """
KeyArmor offers optional paid plans (Monthly and Annual) that unlock additional features. Subscriptions are processed exclusively through Google Play Billing. By purchasing a subscription you also agree to Google Play's Terms of Service.

• Monthly and Annual plans are auto-renewing subscriptions. You can cancel at any time through Google Play; cancellation takes effect at the end of the current billing period.
• Refunds are subject to Google Play's standard refund policy. The developer does not process refunds independently.
• Premium features may evolve over time. The developer reserves the right to add, modify or discontinue specific premium features with reasonable notice.
    """.trimIndent()),
    LegalSection("Travel Safe Mode", """
Travel Safe Mode allows you to mark specific entries as travel-safe and hide all other entries while the mode is active. You are solely responsible for determining which entries to mark as travel-safe and for the consequences of activating or deactivating this mode. The developer assumes no liability for entries accidentally left visible or hidden.
    """.trimIndent()),
    LegalSection("Panic PIN", """
The Panic PIN feature triggers an irreversible action (wiping or hiding sensitive vault data) when a designated PIN is entered. Once activated, hidden or deleted data may not be recoverable depending on your configuration. You assume full responsibility for configuring this feature and for any data loss resulting from its intentional or accidental activation.
    """.trimIndent()),
    LegalSection("Crypto Balance Information", """
The live crypto balance feature retrieves balance information from public blockchain infrastructure for informational purposes only. This information does not constitute financial advice. The developer does not guarantee the accuracy, completeness or timeliness of balance data. Do not make financial decisions based solely on information displayed in this app.
    """.trimIndent()),
    LegalSection("No Warranties", """
The app is provided "as is", without warranties of any kind, express or implied. The developer does not warrant that the app is error-free, operates without interruption, or is fit for a specific purpose.
    """.trimIndent()),
    LegalSection("Limitation of Liability", """
To the fullest extent permitted by applicable law, the developer shall not be liable for data loss, loss of vault access, or any direct, indirect, incidental, special or consequential damages arising from the use or inability to use the app.
    """.trimIndent()),
    LegalSection("Updates to Terms", """
These terms may be updated at any time. Continued use of the app after a published update constitutes acceptance of the new terms.
    """.trimIndent()),
    LegalSection("Last Updated", "May 2026 (rev. 2)")
)

private fun legalEN() = listOf(
    LegalSection("Legal Notice and Disclaimer", ""),
    LegalSection("Software Provided As Is", """
KeyArmor is distributed "as is", without warranties of any kind, express or implied, including but not limited to warranties of merchantability, fitness for a particular purpose or non-infringement.
    """.trimIndent()),
    LegalSection("Limitation of Liability", """
The developer of KeyArmor shall not be liable under any circumstances for:

• Loss or corruption of data stored in the app
• Loss of vault access due to forgotten or lost master password or backup key
• Vulnerabilities or failures in Android OS components
• Failures, interruptions or changes in Google Drive services
• Any direct, indirect, incidental, special or consequential damages arising from use or inability to use the app
• Any damage resulting from unauthorized access to user data by third parties
• Data loss resulting from intentional or accidental activation of the Panic PIN feature
    """.trimIndent()),
    LegalSection("Google Drive and Firebase Data Transmission", """
When the user enables Google Drive sync (backups) or uses Digital Inheritance / the Family Plan (Google Firebase), data is transmitted already-encrypted to Google's infrastructure under Google's Terms of Service. In every case the data leaves the device as an opaque encrypted blob. The developer has no access to or control over Google's infrastructure and cannot read the transmitted contents.
    """.trimIndent()),
    LegalSection("Android Keystore and Hardware Dependency", """
KeyArmor's encryption security depends in part on the Android Keystore implementation of the user's device. This implementation varies by manufacturer, chipset and Android version. The developer does not control or guarantee the security of third-party hardware implementations. Keeping the device OS updated is strongly recommended.
    """.trimIndent()),
    LegalSection("Autofill Service Limitations", """
The KeyArmor Autofill service may not function correctly with all applications or browsers. In particular, Google Chrome does not support Android's standard autofill framework — this is a Chrome limitation that affects all third-party password managers equally and is outside the developer's control. The developer does not guarantee that the autofill service will work with any specific application.
    """.trimIndent()),
    LegalSection("Live Crypto Balance Disclaimer", """
Live balance data is retrieved from public third-party blockchain nodes and APIs. The developer does not operate or control these nodes. Balance information is provided for informational purposes only and may be delayed, inaccurate or unavailable. This information does not constitute financial or investment advice. The developer assumes no liability for decisions made based on balance data displayed in the app.
    """.trimIndent()),
    LegalSection("Peer-to-Peer Sharing", """
The P2P entry sharing feature facilitates direct, encrypted exchange of vault entries between users. The developer assumes no responsibility for the content of entries shared between users, for the identity or trustworthiness of the recipient, or for any consequences arising from sharing vault data with third parties.
    """.trimIndent()),
    LegalSection("Third-Party Libraries", """
KeyArmor uses third-party open-source libraries considered secure at the time of publication. The developer assumes no responsibility for failures, vulnerabilities or unexpected behavior originating in third-party components.
    """.trimIndent()),
    LegalSection("Governing Law", """
This legal notice is governed by the laws in force in the Argentine Republic. Any dispute arising from the use of the app shall be submitted to the competent courts of that jurisdiction.
    """.trimIndent()),
    LegalSection("Last Updated", "May 2026 (rev. 2)")
)

private fun licenseEN() = listOf(
    LegalSection("Source Available License", ""),
    LegalSection("Source Code Published for Security Audit Purposes Only", """
The source code of KeyArmor is publicly available solely so that security researchers and users can inspect and audit its security practices. No rights of any kind are granted beyond that — all rights are reserved by the copyright holder.
    """.trimIndent()),
    LegalSection("What You May Do", """
• Read and inspect the source code for security research and personal auditing
• Report security vulnerabilities to the KeyArmor team through official channels
    """.trimIndent()),
    LegalSection("What You May Not Do", """
Without explicit prior written permission from the copyright holder, you may not:

• Use, execute, or run the software outside of its official distribution
• Copy, reproduce, or extract the source code for any purpose other than reading
• Modify, adapt, translate, or create derivative works based on it
• Distribute, publish, sublicense, sell, or otherwise make it available to third parties
• Use the source code as the basis for another product, service, or project
• Use the KeyArmor name, logo, or any associated trademarks
    """.trimIndent()),
    LegalSection("Source Code", """
KeyArmor's source code is publicly available.
https://github.com/Thormakk/KeyArmor-source
    """.trimIndent()),
    LegalSection("No Warranty", """
The source code is provided "as is", without warranty of any kind, express or implied. The copyright holder makes no representations regarding its fitness for any particular purpose.
    """.trimIndent()),
    LegalSection("Copyright", "© 2026 KeyArmor. All rights reserved. Source Available.")
)

private fun opensourceEN() = listOf(
    LegalSection("Open Source Notice", ""),
    LegalSection("Libraries Used", """
KeyArmor uses the following open-source libraries. Unless otherwise noted, they are distributed under the Apache 2.0 license.
    """.trimIndent()),
    LegalSection("Jetpack Compose", """
Version: BOM 2026.09.00 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/compose
    """.trimIndent()),
    LegalSection("Jetpack Glance (AppWidget)", """
Versión: 1.2.0 | Licencia: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/glance
    """.trimIndent()),
    LegalSection("Room Database", """
Version: 2.8.5 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/training/data-storage/room
    """.trimIndent()),
    LegalSection("Hilt (Dagger)", """
Version: 2.60.1 | License: Apache 2.0
Copyright © The Dagger Authors
https://dagger.dev/hilt/
    """.trimIndent()),
    LegalSection("Kotlin Coroutines", """
Version: 1.11.0 | License: Apache 2.0
Copyright © JetBrains s.r.o.
https://github.com/Kotlin/kotlinx.coroutines
    """.trimIndent()),
    LegalSection("AndroidX Biometric", """
Version: 1.1.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/biometric
    """.trimIndent()),
    LegalSection("AndroidX Navigation", """
Version: 2.10.2 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/guide/navigation
    """.trimIndent()),
    LegalSection("AndroidX Lifecycle / ViewModel", """
Version: 2.11.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/lifecycle
    """.trimIndent()),
    LegalSection("AndroidX WorkManager", """
Version: 2.12.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/topic/libraries/architecture/workmanager
    """.trimIndent()),
    LegalSection("Google Play Services Auth", """
Version: 22.0.0 | License: Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/android/guides/setup
    """.trimIndent()),
    LegalSection("Google API Client Android", """
Version: 2.9.2 | License: Apache 2.0
Copyright © Google LLC
https://github.com/googleapis/google-api-java-client
    """.trimIndent()),
    LegalSection("Google Drive API v3", """
Version: v3-rev20240123-2.0.0 | License: Apache 2.0
Copyright © Google LLC
https://developers.google.com/drive/api/v3/about-sdk
    """.trimIndent()),
    LegalSection("Google HTTP Client GSON", """
Version: 2.2.0 | License: Apache 2.0
Copyright © Google LLC
https://github.com/googleapis/google-http-java-client
    """.trimIndent()),
    LegalSection("ZXing Android Embedded", """
Version: 4.3.0 | License: Apache 2.0
Copyright © ZXing authors, journeyapps
https://github.com/journeyapps/zxing-android-embedded
    """.trimIndent()),
    LegalSection("Accompanist Permissions", """
Version: 0.37.3 | License: Apache 2.0
Copyright © The Android Open Source Project
https://google.github.io/accompanist/permissions/
    """.trimIndent()),
    LegalSection("Compose Material Icons Extended", """
Version: BOM 2026.09.00 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/compose/graphics/images/material
    """.trimIndent()),
    LegalSection("AndroidX Core KTX", """
Version: 1.19.1 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/core
    """.trimIndent()),
    LegalSection("AndroidX Activity Compose", """
Version: 1.13.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/activity
    """.trimIndent()),
    LegalSection("AndroidX Autofill", """
Version: 1.3.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/autofill
    """.trimIndent()),
    LegalSection("AndroidX DataStore", """
Version: 1.3.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/datastore
    """.trimIndent()),
    LegalSection("AndroidX AppCompat", """
Version: 1.8.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/appcompat
    """.trimIndent()),
    LegalSection("AndroidX Hilt (Navigation, Work)", """
Version: 1.4.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/hilt
    """.trimIndent()),
    LegalSection("Kotlin Standard Library", """
Version: 2.4.21 | License: Apache 2.0
Copyright © JetBrains s.r.o.
https://github.com/JetBrains/kotlin
    """.trimIndent()),
    LegalSection("AndroidX Core Splashscreen", """
Version: 1.2.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/core
    """.trimIndent()),
    LegalSection("AndroidX Profileinstaller", """
Version: 1.4.1 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/profileinstaller
    """.trimIndent()),
    LegalSection("AndroidX Credential Manager", """
Version: 1.6.0 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/jetpack/androidx/releases/credentials
    """.trimIndent()),
    LegalSection("CameraX", """
Version: 1.6.2 | License: Apache 2.0
Copyright © The Android Open Source Project
https://developer.android.com/training/camerax
    """.trimIndent()),
    LegalSection("Firebase Android SDK (Messaging, Auth, App Check)", """
Version: BOM 35.1.0 | License: Apache 2.0
Copyright © Google LLC
https://github.com/firebase/firebase-android-sdk
    """.trimIndent()),
    LegalSection("Joda-Time", """
Version: 2.15.0 | License: Apache 2.0
Copyright © Joda.org
https://www.joda.org/joda-time/
    """.trimIndent()),
    LegalSection("Joda-Convert", """
Version: 3.0.1 | License: Apache 2.0
Copyright © Joda.org
https://www.joda.org/joda-convert/
    """.trimIndent()),
    LegalSection("PDFBox-Android", """
Version: 2.0.27.0 | License: Apache 2.0
Copyright © Tom Roush
https://github.com/TomRoush/PdfBox-Android
    """.trimIndent()),
    LegalSection("RichEditor Compose", """
Version: 1.2.1 | License: MIT
Copyright © Mohamed Rejeb
https://github.com/MohamedRejeb/compose-rich-editor
    """.trimIndent()),
    LegalSection("Bouncy Castle Provider", """
Version: 1.86 | License: Bouncy Castle License (MIT-style)
Copyright © The Legion of the Bouncy Castle Inc.
https://www.bouncycastle.org/about/license/
    """.trimIndent()),
    LegalSection("RevenueCat Purchases Android SDK", """
Version: 10.29.0 | License: MIT
Copyright © RevenueCat, Inc.
https://github.com/RevenueCat/purchases-android
    """.trimIndent()),
    LegalSection("LazySodium for Android", """
Version: 5.2.0 | License: MPL-2.0
Copyright © Terl Tech Ltd
https://github.com/terl/lazysodium-android
    """.trimIndent()),
    LegalSection("JNA (Java Native Access)", """
Version: 5.19.1 | License: Apache 2.0 / LGPL 2.1 (dual-licensed)
Copyright © The JNA Project Contributors
https://github.com/java-native-access/jna
    """.trimIndent()),
    LegalSection("Google Identity Services (GoogleId)", """
Version: 1.2.1 | License: Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/identity/android-credential-manager
    """.trimIndent()),
    LegalSection("Play Services Time", """
Version: 16.0.1 | License: Google Play Services Terms
Copyright © Google LLC
https://developers.google.com/android/reference/com/google/android/gms/time/package-summary
    """.trimIndent()),
    LegalSection("ML Kit Text Recognition", """
Version: 16.0.1 | License: ML Kit Terms
Copyright © Google LLC
https://developers.google.com/ml-kit/terms
    """.trimIndent()),
    LegalSection("Public Suffix List", """
Version: 2026-07-09 | License: MPL-2.0
Copyright © Mozilla Foundation and contributors
https://publicsuffix.org
    """.trimIndent()),
    LegalSection("Full Apache 2.0 Text", """
The full text of the Apache 2.0 license is available at:
https://www.apache.org/licenses/LICENSE-2.0
MIT: https://opensource.org/license/mit
MPL-2.0: https://www.mozilla.org/en-US/MPL/2.0/
    """.trimIndent())
)

private fun deleteDataEN() = listOf(
    LegalSection("Delete Account & Data", ""),
    LegalSection("What KeyArmor stores about you", """
Your vault (passwords, notes, cards, wallets, attachments and password history) is never stored on our servers. It lives encrypted on your device, and if you turn on backup, in your own Google Drive — not somewhere we can read or delete it, because it is encrypted before it ever leaves your device.

The only data associated with your Google account that reaches our servers (Firebase) is tied to connecting that account for an optional feature — Google Drive backup, Digital Inheritance, or Family Plan. Simply connecting a Google account for any of these registers a minimal record there (a push-notification token, keyed by your email), even before you configure Inheritance or Family Plan. If you have never connected a Google account to KeyArmor, there is nothing about you on our servers to delete.
    """.trimIndent()),
    LegalSection("Deleting from the app", """
You can remove all your data at any time from Settings → Delete all my data inside the app. If you never connected a Google account to KeyArmor (for Drive backup, Digital Inheritance, or Family Plan), this immediately and permanently erases your vault from the device.

If you did connect one, the app asks you to sign in with Google so that, in the same action, it can erase the associated server-side records (Firebase) and the backup and Time Machine copies that KeyArmor saved in your Google Drive. Only if you're unable to sign in does the app fall back to erasing local data only — and it warns you beforehand that your server-side records will remain until removed separately, using the request below.
    """.trimIndent()),
    LegalSection("What is not deleted", """
Deleting your data does not remove what lives outside the app:

• Files you exported yourself (CSV, PDF or .kab files you saved or shared). They are yours and are stored wherever you put them, so you need to delete them there.
• Backup copies that an earlier installation of KeyArmor left in your Downloads folder. Android no longer lets a new installation touch them; delete them from your Downloads folder.
• Your subscription. It is managed by Google Play, so deleting your data does not cancel it; cancel it in Google Play → Subscriptions.
    """.trimIndent()),
    LegalSection("Requesting deletion of your account data", """
If you connected a Google account to KeyArmor but can't sign in to use the in-app deletion above (for example, you no longer have the app installed), request deletion of the associated server-side data by emailing us from that same Google account.

Write to: support@thormakk.dev

We process deletion requests within 30 days and confirm by email once completed. You do not need the app installed to make this request.
    """.trimIndent())
)
