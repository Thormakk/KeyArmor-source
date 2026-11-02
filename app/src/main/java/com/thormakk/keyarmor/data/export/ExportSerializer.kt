package com.thormakk.keyarmor.data.export

import android.util.Base64
import com.thormakk.keyarmor.data.database.entity.TagEntity
import com.thormakk.keyarmor.data.prefs.PinnedHubPrefs
import com.thormakk.keyarmor.data.prefs.PinnedRef
import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.CustomEntry
import com.thormakk.keyarmor.domain.model.IdentityDocument
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
import com.thormakk.keyarmor.domain.model.Vehicle
import org.json.JSONArray
import org.json.JSONObject

object ExportSerializer {

    val PREFIJOS_DE_FORMULA = charArrayOf('=', '+', '-', '@', '\t', '\r')

    fun toJson(
        passwords:          List<VaultEntry>,
        twoFactor:          List<TwoFactorAccount>,
        creditCards:        List<CreditCard>,
        bankAccounts:       List<BankAccount>,
        cryptoWallets:      List<CryptoWallet>,
        secureNotes:        List<SecureNote>,
        customEntries:      List<CustomEntry>,
        passkeys:           List<PasskeyCredential>  = emptyList(),
        identityDocuments:  List<IdentityDocument>   = emptyList(),
        vehicles:           List<Vehicle>            = emptyList(),
        vaults:             List<VaultMetadata>      = emptyList(),
        attachments:        List<AttachmentBackupEntry> = emptyList(),
        reminders:           List<com.thormakk.keyarmor.domain.model.Reminder> = emptyList(),
        secureNoteHistory:   List<com.thormakk.keyarmor.domain.model.SecureNoteHistoryItem> = emptyList(),
        tags:                List<TagEntity> = emptyList(),
        heirIdentityPublicB64:    String? = null,
        heirIdentityPrivateCode:  String? = null,
        heirOfOwnerEmail:         String? = null,
        pinnedOwnerSigningKey:    String? = null,
        ownerSigningPublicB64:    String? = null,
        ownerSigningPrivatePkcs8: String? = null,
        ownerSigningEmail:        String? = null,
        pinned:                   List<PinnedRef>? = null
    ): String = JSONObject().apply {
        put("version",           1)
        put("exportedAt",        System.currentTimeMillis())
        put("passwords",          JSONArray().also { a -> passwords.forEach          { a.put(it.toJson()) } })
        put("twoFactor",          JSONArray().also { a -> twoFactor.forEach          { a.put(it.toJson()) } })
        put("creditCards",        JSONArray().also { a -> creditCards.forEach        { a.put(it.toJson()) } })
        put("bankAccounts",       JSONArray().also { a -> bankAccounts.forEach       { a.put(it.toJson()) } })
        put("cryptoWallets",      JSONArray().also { a -> cryptoWallets.forEach      { a.put(it.toJson()) } })
        put("secureNotes",        JSONArray().also { a -> secureNotes.forEach        { a.put(it.toJson()) } })
        put("customEntries",      JSONArray().also { a -> customEntries.forEach      { a.put(it.toJson()) } })
        put("passkeys",           JSONArray().also { a -> passkeys.forEach           { a.put(it.toJson()) } })
        put("identityDocuments",  JSONArray().also { a -> identityDocuments.forEach  { a.put(it.toJson()) } })
        put("vehicles",           JSONArray().also { a -> vehicles.forEach           { a.put(it.toJson()) } })
        put("vaults",             JSONArray().also { a -> vaults.forEach             { a.put(it.toJson()) } })
        if (reminders.isNotEmpty()) {
            put("reminders", JSONArray().also { a -> reminders.forEach { a.put(it.toJson()) } })
        }
        if (secureNoteHistory.isNotEmpty()) {
            put("secureNoteHistory", JSONArray().also { a ->
                secureNoteHistory.forEach { h ->
                    a.put(JSONObject().apply {
                        put("noteId",  h.noteId)
                        put("content", h.content)
                        put("savedAt", h.savedAt)
                    })
                }
            })
        }
        if (pinned != null) put("pinned", PinnedHubPrefs.toJson(pinned))
        if (tags.isNotEmpty()) {
            put("tags", JSONArray().also { a ->
                tags.forEach { t ->
                    a.put(JSONObject().apply {
                        put("id",         t.id)
                        put("name",       t.name)
                        put("defaultKey", t.defaultKey)
                    })
                }
            })
        }
        if (attachments.isNotEmpty()) {
            put("attachments", JSONArray().also { a -> attachments.forEach { a.put(it.toJson()) } })
        }
        if (heirIdentityPublicB64 != null && heirIdentityPrivateCode != null) {
            put("inheritanceIdentity", JSONObject().apply {
                put("publicKeyB64",  heirIdentityPublicB64)
                put("privateKeyCode", heirIdentityPrivateCode)
                if (heirOfOwnerEmail != null)      put("ownerEmail", heirOfOwnerEmail)
                if (pinnedOwnerSigningKey != null) put("pinnedOwnerSigningKey", pinnedOwnerSigningKey)
            })
        }
        if (ownerSigningPublicB64 != null && ownerSigningPrivatePkcs8 != null) {
            put("ownerSigningIdentity", JSONObject().apply {
                put("publicKeyB64",    ownerSigningPublicB64)
                put("privatePkcs8B64", ownerSigningPrivatePkcs8)
                if (ownerSigningEmail != null) put("ownerEmail", ownerSigningEmail)
            })
        }
    }.toString()

    fun toCsv(passwords: List<VaultEntry>): String = buildString {
        appendLine("name,url,username,password,notes,category,tags,isFavorite")
        passwords.forEach { e ->
            appendLine(
                listOf(
                    e.title.csvEscape(),
                    e.url.csvEscape(),
                    e.username.csvEscape(),
                    e.password.csvEscape(),
                    e.notes.csvEscape(),
                    e.category.name,
                    e.tags.joinToString(";").csvEscape(),
                    e.isFavorite.toString()
                ).joinToString(",")
            )
        }
    }

    private fun VaultMetadata.toJson() = JSONObject().apply {
        put("id",             id)
        put("name",           name)
        put("isInherited",    isInherited)
        put("isPrivate",      isPrivate)
        put("ownerEmail",     ownerEmail)
        put("ownerSub",       ownerSub)
        put("sortOrder",      sortOrder)
        put("createdAt",      createdAt)
    }

    private fun AttachmentBackupEntry.toJson() = JSONObject().apply {
        put("id",        id)
        put("entryId",   entryId)
        put("fileName",  fileName)
        put("mimeType",  mimeType)
        put("fileSize",  fileSize)
        put("createdAt", createdAt)
        put("content",   Base64.encodeToString(content, Base64.NO_WRAP))
    }

    private fun String.csvEscape(): String {
        val neutralizado =
            if (isNotEmpty() && first() in PREFIJOS_DE_FORMULA) "'" + this else this
        return if (neutralizado.contains(",") || neutralizado.contains("\"") || neutralizado.contains("\n"))
            "\"${neutralizado.replace("\"", "\"\"")}\""
        else neutralizado
    }

    private fun VaultEntry.toJson() = JSONObject().apply {
        put("id",          id);          put("title",    title)
        put("username",    username);    put("email",    email)
        put("password",    password)
        put("url",         url)
        put("urls",        JSONArray(urls))
        put("notes",       notes);       put("category", category.name)
        put("tags",        JSONArray(tags))
        put("isFavorite",  isFavorite);  put("isTravelSafe", isTravelSafe)
        put("isPinned",    isPinned);    put("isHidden",     isHidden)
        put("expiryDate",  expiryDate ?: 0L)
        put("createdAt",        createdAt);   put("updatedAt",    updatedAt)
        put("passwordChangedAt", passwordChangedAt ?: 0L)
        put("isDeleted",   isDeleted);   put("deletedAt",    deletedAt ?: 0L)
        put("archivedAt",  archivedAt ?: 0L)
        put("vaultId",     vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
        put("fieldLabel",    fieldLabel)
        put("isProtected",   isProtected)
    }

    private fun TwoFactorAccount.toJson() = JSONObject().apply {
        put("id",            id);            put("name",         name)
        put("issuer",        issuer);        put("accountLabel", accountLabel)
        put("secret",        secret);        put("digits",       digits)
        put("period",        period);        put("algorithm",    algorithm.name)
        put("recoveryCodes", JSONArray().also { a ->
            recoveryCodes.forEach { rc ->
                a.put(JSONObject().apply { put("code", rc.code); put("used", rc.used) })
            }
        })
        put("url",          url)
        put("linkedPasswordId", linkedPasswordId)
        put("notes",    notes);    put("category",     category.name)
        put("tags",     JSONArray(tags))
        put("isFavorite", isFavorite); put("isTravelSafe", isTravelSafe)
        put("isPinned",   isPinned);   put("isHidden",     isHidden)
        put("isProtected", isProtected)
        put("createdAt", createdAt);   put("updatedAt",    updatedAt)
        put("isDeleted", isDeleted);   put("deletedAt",    deletedAt ?: 0L)
        put("archivedAt", archivedAt ?: 0L)
        put("vaultId",    vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
    }

    private fun CreditCard.toJson() = JSONObject().apply {
        put("id", id)
        put("issuer",     issuer);     put("brand",      brand)
        put("cardNumber", cardNumber); put("cardholder", cardholder)
        put("expiryDate", expiryDate); put("cvv",        cvv)
        put("bankPhone",  bankPhone);  put("category",   category.name)
        put("tags",       JSONArray(tags))
        put("isFavorite", isFavorite); put("isTravelSafe", isTravelSafe)
        put("isPinned",   isPinned);   put("isHidden",     isHidden)
        put("isProtected", isProtected)
        put("createdAt",  createdAt);  put("updatedAt",    updatedAt)
        put("isDeleted",  isDeleted);  put("deletedAt",    deletedAt ?: 0L)
        put("archivedAt", archivedAt ?: 0L)
        put("vaultId",    vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
    }

    private fun BankAccount.toJson() = JSONObject().apply {
        put("id", id)
        put("bankName",      bankName);      put("swiftBic",     swiftBic)
        put("accountType",   accountType);   put("accountNumber",accountNumber)
        put("cbuIban",       cbuIban);       put("alias",        alias)
        put("notes",         notes)
        put("linkedPasswordId", linkedPasswordId)
        put("category",      category.name); put("tags",         JSONArray(tags))
        put("isFavorite",    isFavorite);    put("isTravelSafe", isTravelSafe)
        put("isPinned",      isPinned);      put("isHidden",     isHidden)
        put("isProtected", isProtected)
        put("createdAt",     createdAt);     put("updatedAt",    updatedAt)
        put("isDeleted",     isDeleted);     put("deletedAt",    deletedAt ?: 0L)
        put("archivedAt",    archivedAt ?: 0L)
        put("vaultId",       vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
    }

    private fun CryptoWallet.toJson() = JSONObject().apply {
        put("id",              id)
        put("address",         address);        put("seedPhrase",      seedPhrase)
        put("privateKey",      privateKey);     put("notes",           notes)
        put("manualBalance",   manualBalance);  put("exchangeUrl",     exchangeUrl)
        put("linkedPasswordId",linkedPasswordId)
        put("network",         network);        put("asset",           asset)
        put("category",        category.name);  put("tags",            JSONArray(tags))
        put("isFavorite",      isFavorite);     put("isTravelSafe",    isTravelSafe)
        put("isPinned",        isPinned);       put("isHidden",        isHidden)
        put("isProtected", isProtected)
        put("createdAt",       createdAt);      put("updatedAt",       updatedAt)
        put("isDeleted",       isDeleted);      put("deletedAt",       deletedAt ?: 0L)
        put("archivedAt",      archivedAt ?: 0L)
        put("vaultId",         vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
    }

    private fun SecureNote.toJson() = JSONObject().apply {
        put("id",       id);       put("title",   title)
        put("content",  content);  put("category",category.name)
        put("tags",     JSONArray(tags))
        put("isFavorite", isFavorite); put("isTravelSafe", isTravelSafe)
        put("isPinned",   isPinned);   put("isHidden",     isHidden)
        put("isProtected", isProtected)
        put("createdAt", createdAt);   put("updatedAt",    updatedAt)
        put("isDeleted", isDeleted);   put("deletedAt",    deletedAt ?: 0L)
        put("archivedAt", archivedAt ?: 0L)
        put("historyEnabled", historyEnabled)
        put("vaultId",    vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
    }

    private fun PasskeyCredential.toJson() = JSONObject().apply {
        put("credentialId",       credentialId)
        put("rpId",               rpId)
        put("rpName",             rpName)
        put("userId",             userId)
        put("userName",           userName)
        put("userDisplayName",    userDisplayName)
        put("privateKeyPkcs8",    Base64.encodeToString(privateKeyPkcs8, Base64.NO_WRAP))
        put("publicKeyCose",      Base64.encodeToString(publicKeyCose,   Base64.NO_WRAP))
        put("signCount",          signCount)
        put("createdAt",          createdAt)
        put("lastUsedAt",         lastUsedAt ?: 0L)
        put("notes",              notes)
        put("urls",               urls)
        put("linkedPasswordId",   linkedPasswordId)
        put("category",           category)
        put("tags",               tags)
        put("customAccountLabel", customAccountLabel)
        put("isTravelSafe",       isTravelSafe)
        put("isPinned",           isPinned)
        put("isHidden",           isHidden)
        put("isProtected", isProtected)
        put("isArchived",         isArchived)
        put("archivedAt",         archivedAt ?: 0L)
        put("isDeleted",          isDeleted)
        put("deletedAt",          deletedAt ?: 0L)
        put("vaultId",            vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
    }

    private fun IdentityDocument.toJson() = JSONObject().apply {
        put("id",              id)
        put("docType",         docType)
        put("number",          number)
        put("issuingCountry",  issuingCountry)
        put("issueDate",       issueDate)
        put("expiryDate",      expiryDate)
        put("holderName",      holderName)
        put("birthDate",       birthDate)
        put("notes",           notes)
        put("expiryTimestamp", expiryTimestamp ?: 0L)
        put("category",        category.name)
        put("tags",            JSONArray(tags))
        put("isFavorite",      isFavorite)
        put("isTravelSafe",    isTravelSafe)
        put("isPinned",        isPinned)
        put("isHidden",        isHidden)
        put("isProtected", isProtected)
        put("createdAt",       createdAt)
        put("updatedAt",       updatedAt)
        put("isDeleted",       isDeleted)
        put("deletedAt",       deletedAt ?: 0L)
        put("archivedAt",      archivedAt ?: 0L)
        put("vaultId",         vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
    }

    private fun Vehicle.toJson() = JSONObject().apply {
        put("id",                           id)
        put("make",                         make)
        put("model",                        model)
        put("year",                         year)
        put("licensePlate",                 licensePlate)
        put("vin",                          vin)
        put("inspectionExpiryDate",         inspectionExpiryDate)
        put("insuranceExpiryDate",          insuranceExpiryDate)
        put("insuranceCompany",             insuranceCompany)
        put("policyNumber",                 policyNumber)
        put("driverLicenseExpiryDate",      driverLicenseExpiryDate)
        put("notes",                        notes)
        put("inspectionExpiryTimestamp",    inspectionExpiryTimestamp ?: 0L)
        put("insuranceExpiryTimestamp",     insuranceExpiryTimestamp ?: 0L)
        put("driverLicenseExpiryTimestamp", driverLicenseExpiryTimestamp ?: 0L)
        put("category",                     category.name)
        put("tags",                         JSONArray(tags))
        put("isFavorite",                   isFavorite)
        put("isTravelSafe",                 isTravelSafe)
        put("isPinned",                     isPinned)
        put("isHidden",                     isHidden)
        put("isProtected", isProtected)
        put("createdAt",                    createdAt)
        put("updatedAt",                    updatedAt)
        put("isDeleted",                    isDeleted)
        put("deletedAt",                    deletedAt ?: 0L)
        put("archivedAt",                   archivedAt ?: 0L)
        put("vaultId",                     vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
    }

    private fun com.thormakk.keyarmor.domain.model.Reminder.toJson() = JSONObject().apply {
        put("id",         id)
        put("name",       name)
        put("date",       date)
        put("note",       note)
        put("createdAt",  createdAt)
        put("isTrashed",  isTrashed)
        put("trashedAt",  trashedAt ?: 0L)
        put("isPinned",   isPinned)
        put("isFavorite", isFavorite)
        put("isArchived", isArchived)
        put("isProtected", isProtected)
        put("tags",       JSONArray(tags))
        put("category",   category.name)
        put("schedules",  com.thormakk.keyarmor.domain.model.ReminderSchedule.listToJson(schedules))
    }

    private fun CustomEntry.toJson() = JSONObject().apply {
        put("id",    id);    put("title", title)
        put("fields", JSONArray().also { a ->
            fields.forEach { f ->
                a.put(JSONObject().apply {
                    put("id",    f.id)
                    put("label", f.label)
                    put("type",  f.type.name)
                    put("value", f.value)
                })
            }
        })
        put("category",    category.name)
        put("tags",        JSONArray(tags))
        put("isFavorite",  isFavorite)
        put("isTravelSafe", isTravelSafe)
        put("isPinned",    isPinned)
        put("isHidden",    isHidden)
        put("isProtected", isProtected)
        put("createdAt",   createdAt)
        put("updatedAt",   updatedAt)
        put("isDeleted",   isDeleted)
        put("deletedAt",   deletedAt ?: 0L)
        put("archivedAt",  archivedAt ?: 0L)
        put("vaultId",     vaultId)
        put("inheritedFrom", inheritedFrom ?: "")
    }

}
