package com.thormakk.keyarmor.data.export

import android.util.Base64
import androidx.core.net.toUri
import com.thormakk.keyarmor.data.database.entity.TagEntity
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PinnedRef
import com.thormakk.keyarmor.data.export.ImportParser.parseCsvLine
import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.CustomEntry
import com.thormakk.keyarmor.domain.model.CustomField
import com.thormakk.keyarmor.domain.model.CustomFieldType
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.IdentityDocument
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.RecoveryCode
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.domain.model.TotpAlgorithm
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.model.Vehicle
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

data class OwnerSigningIdentityBackup(
    val publicKeyB64:    String,
    val privatePkcs8B64: String,
    val ownerEmail:      String? = null,
)

data class HeirIdentityBackup(
    val publicKeyB64:   String,
    val privateKeyCode: String,
    val ownerEmail:            String? = null,
    val pinnedOwnerSigningKey: String? = null,
)

data class ImportResult(
    val passwords:         List<VaultEntry>         = emptyList(),
    val twoFactor:         List<TwoFactorAccount>   = emptyList(),
    val creditCards:       List<CreditCard>         = emptyList(),
    val bankAccounts:      List<BankAccount>        = emptyList(),
    val cryptoWallets:     List<CryptoWallet>       = emptyList(),
    val secureNotes:       List<SecureNote>         = emptyList(),
    val customEntries:     List<CustomEntry>        = emptyList(),
    val passkeys:          List<PasskeyCredential>  = emptyList(),
    val identityDocuments: List<IdentityDocument>   = emptyList(),
    val vehicles:          List<Vehicle>            = emptyList(),
    val vaults:            List<VaultMetadata>      = emptyList(),
    val attachments:       List<AttachmentBackupEntry> = emptyList(),
    val attachmentCount:   Int                      = 0,
    val isKabContainer:    Boolean                  = false,
    val reminders:         List<com.thormakk.keyarmor.domain.model.Reminder> = emptyList(),
    val secureNoteHistory: List<com.thormakk.keyarmor.domain.model.SecureNoteHistoryItem> = emptyList(),
    val tags:              List<TagEntity>          = emptyList(),
    val pinned:            List<PinnedRef>?         = null,
    val inheritanceIdentity: HeirIdentityBackup? = null,
    val ownerSigningIdentity: OwnerSigningIdentityBackup? = null,
    val skippedRows:       Int                      = 0
) {
    val totalCount: Int
        get() = passwords.size + twoFactor.size + creditCards.size +
                bankAccounts.size + cryptoWallets.size + secureNotes.size +
                customEntries.size + passkeys.size +
                identityDocuments.size + vehicles.size
}

object ImportParser {

    fun parseKeyArmorJson(json: String): ImportResult {
        val root              = JSONObject(json)
        val passwords         = root.optJSONArray("passwords")?.toVaultEntries()           ?: emptyList()
        val twoFactor         = root.optJSONArray("twoFactor")?.toTwoFactorAccounts()      ?: emptyList()
        val creditCards       = root.optJSONArray("creditCards")?.toCreditCards()          ?: emptyList()
        val bankAccounts      = root.optJSONArray("bankAccounts")?.toBankAccounts()        ?: emptyList()
        val cryptoWallets     = root.optJSONArray("cryptoWallets")?.toCryptoWallets()      ?: emptyList()
        val secureNotes       = root.optJSONArray("secureNotes")?.toSecureNotes()          ?: emptyList()
        val customEntries     = root.optJSONArray("customEntries")?.toCustomEntries()      ?: emptyList()
        val passkeys          = root.optJSONArray("passkeys")?.toPasskeyCredentials()      ?: emptyList()
        val identityDocuments = root.optJSONArray("identityDocuments")?.toIdentityDocuments() ?: emptyList()
        val vehicles          = root.optJSONArray("vehicles")?.toVehicles()                ?: emptyList()

        val rawTotal = listOf("passwords", "twoFactor", "creditCards", "bankAccounts",
                              "cryptoWallets", "secureNotes", "customEntries", "passkeys",
                              "identityDocuments", "vehicles")
            .sumOf { root.optJSONArray(it)?.length() ?: 0 }
        val parsedTotal = passwords.size + twoFactor.size + creditCards.size +
                          bankAccounts.size + cryptoWallets.size + secureNotes.size +
                          customEntries.size + passkeys.size +
                          identityDocuments.size + vehicles.size

        val vaults      = root.optJSONArray("vaults")?.toVaultMetadataList() ?: emptyList()
        val attachments = root.optJSONArray("attachments")?.toAttachmentBackupEntries() ?: emptyList()
        val remindersParsed = root.optJSONArray("reminders")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = o.optString("id").ifBlank { return@mapNotNull null }
                runCatching {
                    com.thormakk.keyarmor.domain.model.Reminder(
                        id         = id,
                        name       = o.optString("name"),
                        date       = o.optLong("date"),
                        note       = o.optString("note"),
                        createdAt  = o.optLong("createdAt", System.currentTimeMillis()),
                        isTrashed  = o.optBoolean("isTrashed", false),
                        trashedAt  = o.optLong("trashedAt", 0L).takeIf { it > 0L },
                        isPinned   = o.optBoolean("isPinned", false),
                        isFavorite = o.optBoolean("isFavorite", false),
                        isArchived = o.optBoolean("isArchived", false),
                        isProtected = o.optBoolean("isProtected", false),
                        tags       = o.optJSONArray("tags")?.let { t ->
                            (0 until t.length()).mapNotNull { j -> t.optString(j).ifBlank { null } }
                        } ?: emptyList(),
                        category   = com.thormakk.keyarmor.domain.model.EntryCategory.entries
                            .firstOrNull { it.name == o.optString("category") }
                            ?: com.thormakk.keyarmor.domain.model.EntryCategory.SIN_CATEGORIZAR,
                        schedules  = com.thormakk.keyarmor.domain.model.ReminderSchedule
                            .listFromJson(o.optString("schedules").ifBlank { null })
                    )
                }.getOrNull()
            }
        } ?: emptyList()

        val noteHistoryParsed = root.optJSONArray("secureNoteHistory")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val noteId = o.optString("noteId").ifBlank { return@mapNotNull null }
                com.thormakk.keyarmor.domain.model.SecureNoteHistoryItem(
                    id      = 0,
                    noteId  = noteId,
                    content = o.optString("content"),
                    savedAt = o.optLong("savedAt", System.currentTimeMillis())
                )
            }
        } ?: emptyList()

        val tagsParsed = root.optJSONArray("tags")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val o    = arr.optJSONObject(i) ?: return@mapNotNull null
                val name = o.optString("name").trim().ifBlank { return@mapNotNull null }
                TagEntity(
                    id         = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                    name       = name,
                    defaultKey = o.optString("defaultKey")
                )
            }
        } ?: emptyList()

        val pinnedParsed = root.optJSONArray("pinned")?.let { PinnedHubPrefs.fromJson(it) }

        val heirIdentity = root.optJSONObject("inheritanceIdentity")?.let { o ->
            val pub  = o.optString("publicKeyB64").ifBlank { null }
            val priv = o.optString("privateKeyCode").ifBlank { null }
            if (pub != null && priv != null) HeirIdentityBackup(
                publicKeyB64          = pub,
                privateKeyCode        = priv,
                ownerEmail            = o.optString("ownerEmail").ifBlank { null },
                pinnedOwnerSigningKey = o.optString("pinnedOwnerSigningKey").ifBlank { null },
            ) else null
        }
        val ownerSignIdentity = root.optJSONObject("ownerSigningIdentity")?.let { o ->
            val pub  = o.optString("publicKeyB64").ifBlank { null }
            val priv = o.optString("privatePkcs8B64").ifBlank { null }
            if (pub != null && priv != null) OwnerSigningIdentityBackup(
                publicKeyB64    = pub,
                privatePkcs8B64 = priv,
                ownerEmail      = o.optString("ownerEmail").ifBlank { null },
            ) else null
        }

        return ImportResult(
            passwords         = passwords,
            twoFactor         = twoFactor,
            creditCards       = creditCards,
            bankAccounts      = bankAccounts,
            cryptoWallets     = cryptoWallets,
            secureNotes       = secureNotes,
            customEntries     = customEntries,
            passkeys          = passkeys,
            identityDocuments = identityDocuments,
            vehicles          = vehicles,
            vaults            = vaults,
            attachments       = attachments,
            reminders           = remindersParsed,
            secureNoteHistory   = noteHistoryParsed,
            tags                = tagsParsed,
            pinned              = pinnedParsed,
            inheritanceIdentity = heirIdentity,
            ownerSigningIdentity = ownerSignIdentity,
            skippedRows       = rawTotal - parsedTotal
        )
    }

    fun parseGenericCsv(csv: String): ImportResult {
        val firstLine = splitCsvRows(csv).firstOrNull { it.isNotBlank() } ?: return ImportResult()
        val rawHeaders = parseCsvLine(firstLine)
        val source     = CsvFieldMapper.detectSource(rawHeaders)
        val headers    = CsvFieldMapper.normalizeHeaders(rawHeaders, source)
        return parseNormalizedCsv(csv, headers, source)
    }

    private fun parseNormalizedCsv(
        csv:     String,
        headers: List<String>,
        source:  CsvFieldMapper.CsvSource
    ): ImportResult {
        val lines = splitCsvRows(csv).filter { it.isNotBlank() }
        if (lines.size < 2) return ImportResult()
        val now = System.currentTimeMillis()

        val passwords   = mutableListOf<VaultEntry>()
        val twoFactors  = mutableListOf<TwoFactorAccount>()
        var skipped     = 0

        for (line in lines.drop(1)) {
            var isEmptyEntry = false
            runCatching {
                val cols = parseCsvLine(line)
                val raw  = headers.zip(cols).toMap()

                val type = raw["type"]?.lowercase()
                if (type != null && type.isNotBlank() && type != "login") return@runCatching

                val id = UUID.randomUUID().toString()

                val isKeyArmor = source == CsvFieldMapper.CsvSource.KEY_ARMOR
                val entry = VaultEntry(
                    id           = id,
                    title        = raw[CsvFieldMapper.TITLE]    ?: raw[CsvFieldMapper.URL] ?: "",
                    username     = raw[CsvFieldMapper.USERNAME] ?: "",
                    email        = raw[CsvFieldMapper.EMAIL]    ?: "",
                    password     = raw[CsvFieldMapper.PASSWORD] ?: "",
                    urls         = listOfNotNull(raw[CsvFieldMapper.URL]?.takeIf { it.isNotEmpty() }),
                    notes        = raw[CsvFieldMapper.NOTES]    ?: "",
                    category     = EntryCategory.fromString(raw[CsvFieldMapper.FOLDER] ?: ""),
                    tags         = if (isKeyArmor)
                                       raw["tags"]?.split(";")?.filter { it.isNotBlank() } ?: emptyList()
                                   else emptyList(),
                    isFavorite   = raw[CsvFieldMapper.FAVORITE].let { fav ->
                                       fav == "1" || fav?.lowercase() == "true"
                                   },
                    isTravelSafe = false,
                    createdAt    = now,
                    updatedAt    = now,
                    isDeleted    = false,
                    deletedAt    = null
                )

                val totpRaw = raw[CsvFieldMapper.TOTP]?.takeIf { it.isNotBlank() }

                if (entry.title.isBlank()    && entry.username.isBlank() &&
                    entry.email.isBlank()    && entry.password.isBlank() &&
                    entry.notes.isBlank()    && entry.urls.isEmpty()     &&
                    totpRaw == null
                ) {
                    isEmptyEntry = true
                    return@runCatching
                }

                passwords.add(entry)

                totpRaw?.let {
                    parseTotpField(it, id, entry.title, now)
                        ?.let { account -> twoFactors.add(account) }
                }
            }.onFailure { skipped++ }
            if (isEmptyEntry) skipped++
        }

        return ImportResult(passwords = passwords, twoFactor = twoFactors, skippedRows = skipped)
    }

    private fun parseTotpField(
        raw:       String,
        entryId:   String,
        entryTitle: String,
        now:       Long
    ): TwoFactorAccount? = runCatching {
        if (raw.startsWith("otpauth://", ignoreCase = true)) {
            val uri    = raw.toUri()
            val label  = uri.path?.trimStart('/') ?: entryTitle
            val parts  = label.split(":", limit = 2)
            val issuer = uri.getQueryParameter("issuer")
                ?: parts.getOrElse(0) { entryTitle }
            val account = parts.getOrElse(1) { label }
            val secret  = uri.getQueryParameter("secret") ?: return@runCatching null
            val digits  = uri.getQueryParameter("digits")?.toIntOrNull() ?: 6
            val period  = uri.getQueryParameter("period")?.toIntOrNull() ?: 30
            val algo    = when (uri.getQueryParameter("algorithm")?.uppercase(Locale.ROOT)) {
                "SHA256" -> TotpAlgorithm.SHA256
                "SHA512" -> TotpAlgorithm.SHA512
                else     -> TotpAlgorithm.SHA1
            }
            TwoFactorAccount(
                id               = UUID.randomUUID().toString(),
                name             = issuer,
                issuer           = issuer,
                accountLabel     = account,
                secret           = secret,
                digits           = digits,
                period           = period,
                algorithm        = algo,
                recoveryCodes    = emptyList(),
                linkedPasswordId = entryId,
                notes            = "",
                category         = EntryCategory.SIN_CATEGORIZAR,
                tags             = emptyList(),
                isFavorite       = false,
                isTravelSafe     = false,
                createdAt        = now,
                updatedAt        = now,
                isDeleted        = false,
                deletedAt        = null
            )
        } else {
            TwoFactorAccount(
                id               = UUID.randomUUID().toString(),
                name             = entryTitle,
                issuer           = entryTitle,
                accountLabel     = "",
                secret           = raw.trim(),
                digits           = 6,
                period           = 30,
                algorithm        = TotpAlgorithm.SHA1,
                recoveryCodes    = emptyList(),
                linkedPasswordId = entryId,
                notes            = "",
                category         = EntryCategory.SIN_CATEGORIZAR,
                tags             = emptyList(),
                isFavorite       = false,
                isTravelSafe     = false,
                createdAt        = now,
                updatedAt        = now,
                isDeleted        = false,
                deletedAt        = null
            )
        }
    }.getOrNull()

    private fun splitCsvRows(csv: String): List<String> {
        val rows    = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < csv.length) {
            val c = csv[i]
            when {
                c == '"' -> {
                    current.append(c)
                    inQuotes = !inQuotes
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (c == '\r' && i + 1 < csv.length && csv[i + 1] == '\n') i++
                    rows.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        if (current.isNotEmpty()) rows.add(current.toString())
        return rows
    }

    private fun parseCsvLine(line: String): List<String> {
        val result  = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes              -> inQuotes = true
                c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"'); i++
                }
                c == '"'                            -> inQuotes = false
                c == ',' && !inQuotes              -> { result.add(current.toString()); current.clear() }
                else                               -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result.map { it.desneutralizarFormula() }
    }

    private fun String.desneutralizarFormula(): String =
        if (length >= 2 && this[0] == '\'' && this[1] in ExportSerializer.PREFIJOS_DE_FORMULA)
            substring(1)
        else this

    private fun JSONArray.toVaultEntries() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            VaultEntry(
                id           = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                title        = o.optString("title"),
                username     = o.optString("username"),
                email        = o.optString("email"),
                password     = o.optString("password"),
                urls         = buildList {
                                   val arr = o.optJSONArray("urls")
                                   if (arr != null) {
                                       for (j in 0 until arr.length()) {
                                           val u = arr.optString(j).trim()
                                           if (u.isNotEmpty()) add(u)
                                       }
                                   } else {
                                       val legacy = o.optString("url").trim()
                                       if (legacy.isNotEmpty()) add(legacy)
                                   }
                               },
                notes        = o.optString("notes"),
                category     = EntryCategory.fromString(o.optString("category")),
                tags         = o.optJSONArray("tags")?.toStringList() ?: emptyList(),
                isFavorite   = o.optBoolean("isFavorite"),
                isTravelSafe = o.optBoolean("isTravelSafe"),
                isPinned     = o.optBoolean("isPinned"),
                isHidden     = o.optBoolean("isHidden"),
                expiryDate   = o.optLong("expiryDate", 0L).takeIf { it > 0L },
                createdAt    = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt    = o.optLong("updatedAt", System.currentTimeMillis()),
                isDeleted    = o.optBoolean("isDeleted", false),
                deletedAt    = o.optLong("deletedAt", 0L).takeIf { it > 0L },
                archivedAt        = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                vaultId           = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom           = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() },
                passwordChangedAt = o.optLong("passwordChangedAt", 0L).takeIf { it > 0L },
                fieldLabel        = o.optString("fieldLabel", ""),
                isProtected       = o.optBoolean("isProtected", false),
            )
        }.getOrNull()
    }

    private fun JSONArray.toTwoFactorAccounts() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            TwoFactorAccount(
                id               = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                name             = o.optString("name"),
                issuer           = o.optString("issuer"),
                accountLabel     = o.optString("accountLabel"),
                secret           = o.optString("secret"),
                digits           = o.optInt("digits", 6),
                period           = o.optInt("period", 30),
                algorithm        = runCatching { TotpAlgorithm.valueOf(o.optString("algorithm")) }.getOrDefault(TotpAlgorithm.SHA1),
                recoveryCodes    = o.optJSONArray("recoveryCodes")?.let { arr ->
                    (0 until arr.length()).map { j ->
                        val rc = arr.getJSONObject(j)
                        RecoveryCode(code = rc.optString("code"), used = rc.optBoolean("used"))
                    }
                } ?: emptyList(),
                url              = o.optString("url"),
                linkedPasswordId = o.optString("linkedPasswordId"),
                notes            = o.optString("notes"),
                category         = EntryCategory.fromString(o.optString("category")),
                tags             = o.optJSONArray("tags")?.toStringList() ?: emptyList(),
                isFavorite       = o.optBoolean("isFavorite"),
                isTravelSafe     = o.optBoolean("isTravelSafe"),
                isPinned         = o.optBoolean("isPinned"),
                isHidden         = o.optBoolean("isHidden"),
                isProtected = o.optBoolean("isProtected", false),
                createdAt        = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt        = o.optLong("updatedAt", System.currentTimeMillis()),
                isDeleted        = o.optBoolean("isDeleted", false),
                deletedAt        = o.optLong("deletedAt", 0L).takeIf { it > 0L },
                archivedAt       = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                vaultId          = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom          = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() }
            )
        }.getOrNull()
    }

    private fun JSONArray.toCreditCards() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            CreditCard(
                id           = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                issuer       = o.optString("issuer"),
                brand        = o.optString("brand"),
                cardNumber   = o.optString("cardNumber"),
                cardholder   = o.optString("cardholder"),
                expiryDate   = o.optString("expiryDate"),
                cvv          = o.optString("cvv"),
                bankPhone    = o.optString("bankPhone"),
                category     = EntryCategory.fromString(o.optString("category")),
                tags         = o.optJSONArray("tags")?.toStringList() ?: emptyList(),
                isFavorite   = o.optBoolean("isFavorite"),
                isTravelSafe = o.optBoolean("isTravelSafe"),
                isPinned     = o.optBoolean("isPinned"),
                isHidden     = o.optBoolean("isHidden"),
                isProtected = o.optBoolean("isProtected", false),
                createdAt    = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt    = o.optLong("updatedAt", System.currentTimeMillis()),
                isDeleted    = o.optBoolean("isDeleted", false),
                deletedAt    = o.optLong("deletedAt", 0L).takeIf { it > 0L },
                archivedAt        = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                vaultId           = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom           = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() },
            )
        }.getOrNull()
    }

    private fun JSONArray.toBankAccounts() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            BankAccount(
                id               = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                bankName         = o.optString("bankName"),
                swiftBic         = o.optString("swiftBic"),
                accountType      = o.optString("accountType"),
                accountNumber    = o.optString("accountNumber"),
                cbuIban          = o.optString("cbuIban"),
                alias            = o.optString("alias"),
                notes            = o.optString("notes"),
                linkedPasswordId = o.optString("linkedPasswordId"),
                category         = EntryCategory.fromString(o.optString("category")),
                tags             = o.optJSONArray("tags")?.toStringList() ?: emptyList(),
                isFavorite       = o.optBoolean("isFavorite"),
                isTravelSafe     = o.optBoolean("isTravelSafe"),
                isPinned         = o.optBoolean("isPinned"),
                isHidden         = o.optBoolean("isHidden"),
                isProtected = o.optBoolean("isProtected", false),
                createdAt        = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt        = o.optLong("updatedAt", System.currentTimeMillis()),
                isDeleted        = o.optBoolean("isDeleted", false),
                deletedAt        = o.optLong("deletedAt", 0L).takeIf { it > 0L },
                archivedAt       = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                vaultId          = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom          = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() }
            )
        }.getOrNull()
    }

    private fun JSONArray.toCryptoWallets() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            CryptoWallet(
                id               = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                address          = o.optString("address"),
                seedPhrase       = o.optString("seedPhrase"),
                privateKey       = o.optString("privateKey"),
                notes            = o.optString("notes"),
                manualBalance    = o.optString("manualBalance"),
                exchangeUrl      = o.optString("exchangeUrl"),
                linkedPasswordId = o.optString("linkedPasswordId"),
                network          = o.optString("network"),
                asset            = o.optString("asset"),
                category         = EntryCategory.fromString(o.optString("category")),
                tags             = o.optJSONArray("tags")?.toStringList() ?: emptyList(),
                isFavorite       = o.optBoolean("isFavorite"),
                isTravelSafe     = o.optBoolean("isTravelSafe"),
                isPinned         = o.optBoolean("isPinned"),
                isHidden         = o.optBoolean("isHidden"),
                isProtected = o.optBoolean("isProtected", false),
                createdAt        = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt        = o.optLong("updatedAt", System.currentTimeMillis()),
                isDeleted        = o.optBoolean("isDeleted", false),
                deletedAt        = o.optLong("deletedAt", 0L).takeIf { it > 0L },
                archivedAt       = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                vaultId          = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom          = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() }
            )
        }.getOrNull()
    }

    private fun JSONArray.toSecureNotes() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            SecureNote(
                id           = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                title        = o.optString("title"),
                content      = o.optString("content"),
                category     = EntryCategory.fromString(o.optString("category")),
                tags         = o.optJSONArray("tags")?.toStringList() ?: emptyList(),
                isFavorite   = o.optBoolean("isFavorite"),
                isTravelSafe = o.optBoolean("isTravelSafe"),
                isPinned     = o.optBoolean("isPinned"),
                isHidden     = o.optBoolean("isHidden"),
                isProtected = o.optBoolean("isProtected", false),
                createdAt    = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt    = o.optLong("updatedAt", System.currentTimeMillis()),
                isDeleted    = o.optBoolean("isDeleted", false),
                deletedAt    = o.optLong("deletedAt", 0L).takeIf { it > 0L },
                archivedAt        = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                historyEnabled        = o.optBoolean("historyEnabled", false),
                vaultId           = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom           = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() },
            )
        }.getOrNull()
    }

    private fun JSONArray.toCustomEntries() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            CustomEntry(
                id           = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                title        = o.optString("title"),
                fields       = o.optJSONArray("fields")?.let { arr ->
                    (0 until arr.length()).map { j ->
                        val f = arr.getJSONObject(j)
                        CustomField(
                            id    = f.optString("id").ifBlank { UUID.randomUUID().toString() },
                            label = f.optString("label"),
                            type  = CustomFieldType.fromName(f.optString("type")),
                            value = f.optString("value")
                        )
                    }
                } ?: emptyList(),
                category     = EntryCategory.fromString(o.optString("category")),
                tags         = o.optJSONArray("tags")?.toStringList() ?: emptyList(),
                isFavorite   = o.optBoolean("isFavorite"),
                isTravelSafe = o.optBoolean("isTravelSafe"),
                isPinned     = o.optBoolean("isPinned"),
                isHidden     = o.optBoolean("isHidden"),
                isProtected = o.optBoolean("isProtected", false),
                createdAt    = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt    = o.optLong("updatedAt", System.currentTimeMillis()),
                isDeleted    = o.optBoolean("isDeleted", false),
                deletedAt    = o.optLong("deletedAt", 0L).takeIf { it > 0L },
                archivedAt        = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                vaultId           = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom           = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() },
            )
        }.getOrNull()
    }

    private fun JSONArray.toPasskeyCredentials() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            PasskeyCredential(
                credentialId       = o.optString("credentialId"),
                rpId               = o.optString("rpId"),
                rpName             = o.optString("rpName"),
                userId             = o.optString("userId"),
                userName           = o.optString("userName"),
                userDisplayName    = o.optString("userDisplayName"),
                privateKeyPkcs8    = Base64.decode(o.optString("privateKeyPkcs8"), Base64.NO_WRAP),
                publicKeyCose      = Base64.decode(o.optString("publicKeyCose"),   Base64.NO_WRAP),
                signCount          = o.optLong("signCount",  0L),
                createdAt          = o.optLong("createdAt",  System.currentTimeMillis()),
                lastUsedAt         = o.optLong("lastUsedAt", 0L).takeIf { it > 0L },
                notes              = o.optString("notes"),
                urls               = o.optString("urls"),
                linkedPasswordId   = o.optString("linkedPasswordId"),
                category           = o.optString("category", "SIN_CATEGORIZAR"),
                tags               = o.optString("tags"),
                customAccountLabel = o.optString("customAccountLabel"),
                isTravelSafe       = o.optBoolean("isTravelSafe"),
                isPinned           = o.optBoolean("isPinned"),
                isHidden           = o.optBoolean("isHidden"),
                isProtected = o.optBoolean("isProtected", false),
                isArchived         = o.optBoolean("isArchived"),
                archivedAt         = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                isDeleted          = o.optBoolean("isDeleted", false),
                deletedAt          = o.optLong("deletedAt", 0L).takeIf { it > 0L },
                vaultId            = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom            = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() }
            )
        }.getOrNull()
    }

    private fun JSONArray.toIdentityDocuments() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            IdentityDocument(
                id              = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                docType         = o.optString("docType"),
                number          = o.optString("number"),
                issuingCountry  = o.optString("issuingCountry"),
                issueDate       = o.optString("issueDate"),
                expiryDate      = o.optString("expiryDate"),
                holderName      = o.optString("holderName"),
                birthDate      = o.optString("birthDate", ""),
                notes           = o.optString("notes"),
                expiryTimestamp = o.optLong("expiryTimestamp", 0L).takeIf { it > 0L },
                category        = EntryCategory.fromString(o.optString("category")),
                tags            = o.optJSONArray("tags")?.toStringList() ?: emptyList(),
                isFavorite      = o.optBoolean("isFavorite"),
                isTravelSafe    = o.optBoolean("isTravelSafe"),
                isPinned        = o.optBoolean("isPinned"),
                isHidden        = o.optBoolean("isHidden"),
                isProtected = o.optBoolean("isProtected", false),
                createdAt       = o.optLong("createdAt",  System.currentTimeMillis()),
                updatedAt       = o.optLong("updatedAt",  System.currentTimeMillis()),
                isDeleted       = o.optBoolean("isDeleted", false),
                deletedAt       = o.optLong("deletedAt",  0L).takeIf { it > 0L },
                archivedAt      = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                vaultId                     = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom                     = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() }
            )
        }.getOrNull()
    }

    private fun JSONArray.toVehicles() = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            Vehicle(
                id                           = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                make                         = o.optString("make"),
                model                        = o.optString("model"),
                year                         = o.optString("year"),
                licensePlate                 = o.optString("licensePlate"),
                vin                          = o.optString("vin"),
                inspectionExpiryDate         = o.optString("inspectionExpiryDate"),
                insuranceExpiryDate          = o.optString("insuranceExpiryDate"),
                insuranceCompany             = o.optString("insuranceCompany"),
                policyNumber                 = o.optString("policyNumber"),
                driverLicenseExpiryDate      = o.optString("driverLicenseExpiryDate"),
                notes                        = o.optString("notes"),
                inspectionExpiryTimestamp    = o.optLong("inspectionExpiryTimestamp",    0L).takeIf { it > 0L },
                insuranceExpiryTimestamp     = o.optLong("insuranceExpiryTimestamp",     0L).takeIf { it > 0L },
                driverLicenseExpiryTimestamp = o.optLong("driverLicenseExpiryTimestamp", 0L).takeIf { it > 0L },
                category                     = EntryCategory.fromString(o.optString("category")),
                tags                         = o.optJSONArray("tags")?.toStringList() ?: emptyList(),
                isFavorite                   = o.optBoolean("isFavorite"),
                isTravelSafe                 = o.optBoolean("isTravelSafe"),
                isPinned                     = o.optBoolean("isPinned"),
                isHidden                     = o.optBoolean("isHidden"),
                isProtected = o.optBoolean("isProtected", false),
                createdAt                    = o.optLong("createdAt",  System.currentTimeMillis()),
                updatedAt                    = o.optLong("updatedAt",  System.currentTimeMillis()),
                isDeleted                    = o.optBoolean("isDeleted", false),
                deletedAt                    = o.optLong("deletedAt",  0L).takeIf { it > 0L },
                archivedAt                   = o.optLong("archivedAt", 0L).takeIf { it > 0L },
                vaultId                     = o.optString("vaultId", VaultMetadata.PRIMARY_VAULT_ID).ifBlank { VaultMetadata.PRIMARY_VAULT_ID },
                inheritedFrom                     = o.optString("inheritedFrom", "").takeIf { it.isNotBlank() }
            )
        }.getOrNull()
    }

    private fun JSONArray.toVaultMetadataList(): List<VaultMetadata> = (0 until length()).mapNotNull { i ->
        runCatching {
            val o = getJSONObject(i)
            VaultMetadata(
                id             = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                name           = o.optString("name"),
                isInherited    = o.optBoolean("isInherited"),
                isPrivate      = o.optBoolean("isPrivate"),
                ownerEmail     = o.optString("ownerEmail"),
                ownerSub       = o.optString("ownerSub"),
                sortOrder      = o.optInt("sortOrder"),
                createdAt      = o.optLong("createdAt", System.currentTimeMillis())
            )
        }.getOrNull()
    }

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).mapNotNull { i -> optString(i).ifBlank { null } }

    private fun JSONArray.toAttachmentBackupEntries(): List<AttachmentBackupEntry> =
        (0 until length()).mapNotNull { i ->
            runCatching {
                val o    = getJSONObject(i)
                val b64  = o.optString("content", "")
                if (b64.isEmpty()) return@runCatching null
                AttachmentBackupEntry(
                    id        = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                    entryId   = o.optString("entryId"),
                    fileName  = o.optString("fileName"),
                    mimeType  = o.optString("mimeType", "application/octet-stream"),
                    fileSize  = o.optLong("fileSize", 0L),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    content   = Base64.decode(b64, Base64.NO_WRAP)
                )
            }.getOrNull()
        }
}
