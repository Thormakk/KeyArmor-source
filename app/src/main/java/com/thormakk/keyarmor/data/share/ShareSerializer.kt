package com.thormakk.keyarmor.data.share

import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.CustomEntry
import com.thormakk.keyarmor.domain.model.CustomField
import com.thormakk.keyarmor.domain.model.CustomFieldType
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.RecoveryCode
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.domain.model.TotpAlgorithm
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.Vehicle
import com.thormakk.keyarmor.security.CryptoManager
import org.json.JSONArray
import org.json.JSONObject
import javax.crypto.spec.SecretKeySpec

class ShareExpiredException : Exception("El enlace compartido expiró")

object ShareSerializer {

    enum class EntryType {
        PASSWORD, TWO_FACTOR, CREDIT_CARD, BANK_ACCOUNT,
        CRYPTO_WALLET, SECURE_NOTE, CUSTOM_ENTRY, VEHICLE
    }

    data class SharedEntry(
        val type:  EntryType,
        val entry: Any
    )

    fun encrypt(
        shared: SharedEntry,
        password: String,
        crypto: CryptoManager,
        expiresAtMillis: Long? = null
    ): String {
        val entryJson = when (shared.type) {
            EntryType.PASSWORD      -> (shared.entry as VaultEntry).toJson()
            EntryType.TWO_FACTOR    -> (shared.entry as TwoFactorAccount).toJson()
            EntryType.CREDIT_CARD   -> (shared.entry as CreditCard).toJson()
            EntryType.BANK_ACCOUNT  -> (shared.entry as BankAccount).toJson()
            EntryType.CRYPTO_WALLET -> (shared.entry as CryptoWallet).toJson()
            EntryType.SECURE_NOTE   -> (shared.entry as SecureNote).toJson()
            EntryType.CUSTOM_ENTRY  -> (shared.entry as CustomEntry).toJson()
            EntryType.VEHICLE       -> (shared.entry as Vehicle).toJson()
        }
        val payload = wrap(shared.type, entryJson, expiresAtMillis).toString()

        val salt = crypto.generateSalt()
        val key  = crypto.deriveKeyArgon2id(
            password, salt,
            CryptoManager.ARGON2_DEFAULT_MEMORY_KB,
            CryptoManager.ARGON2_DEFAULT_ITERATIONS,
            CryptoManager.ARGON2_DEFAULT_PARALLELISM
        )
        val encryptedData = crypto.encrypt(payload, key)

        return JSONObject().apply {
            put("v",   2)
            put("kdf", "argon2id")
            put("mem", CryptoManager.ARGON2_DEFAULT_MEMORY_KB)
            put("it",  CryptoManager.ARGON2_DEFAULT_ITERATIONS)
            put("par", CryptoManager.ARGON2_DEFAULT_PARALLELISM)
            put("s", crypto.saltToBase64(salt))
            put("d", encryptedData)
        }.toString()
    }

    fun decrypt(token: String, password: String, crypto: CryptoManager): SharedEntry {
        val outer = JSONObject(token)
        val version = outer.getInt("v")
        require(version == 2) { "Unsupported share version: $version" }

        val salt = crypto.base64ToSalt(outer.getString("s"))
        val key  = deriveWrapperKey(outer, password, salt, crypto)
        val plaintext = crypto.decrypt(outer.getString("d"), key)

        val inner = JSONObject(plaintext)

        val expiresAt = inner.optLong("expiresAt", 0L)
        if (expiresAt > 0L && System.currentTimeMillis() > expiresAt) {
            throw ShareExpiredException()
        }

        val type  = EntryType.valueOf(inner.getString("type"))
        val obj   = inner.getJSONObject("entry")

        val entry: Any = when (type) {
            EntryType.PASSWORD      -> obj.toVaultEntry()
            EntryType.TWO_FACTOR    -> obj.toTwoFactorAccount()
            EntryType.CREDIT_CARD   -> obj.toCreditCard()
            EntryType.BANK_ACCOUNT  -> obj.toBankAccount()
            EntryType.CRYPTO_WALLET -> obj.toCryptoWallet()
            EntryType.SECURE_NOTE   -> obj.toSecureNote()
            EntryType.CUSTOM_ENTRY  -> obj.toCustomEntry()
            EntryType.VEHICLE       -> obj.toVehicle()
        }
        return SharedEntry(type, entry)
    }

    private fun deriveWrapperKey(
        outer: JSONObject,
        password: String,
        salt: ByteArray,
        crypto: CryptoManager
    ): SecretKeySpec =
        crypto.deriveKeyArgon2id(
            password, salt,
            outer.getInt("mem"),
            outer.getInt("it"),
            outer.getInt("par")
        )

    private fun wrap(type: EntryType, entry: JSONObject, expiresAtMillis: Long? = null) = JSONObject().apply {
        put("type",  type.name)
        put("entry", entry)
        if (expiresAtMillis != null) put("expiresAt", expiresAtMillis)
    }

    private fun VaultEntry.toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("username", username)
        put("email", email); put("password", password)
        put("url", url)
        put("urls", JSONArray(urls))
        put("notes", notes); put("category", category.name)
        put("tags", JSONArray(tags)); put("isFavorite", isFavorite)
        put("isTravelSafe", isTravelSafe)
        put("createdAt", createdAt); put("updatedAt", updatedAt)
    }

    private fun TwoFactorAccount.toJson() = JSONObject().apply {
        put("id", id); put("name", name); put("issuer", issuer)
        put("accountLabel", accountLabel); put("secret", secret)
        put("digits", digits); put("period", period)
        put("algorithm", algorithm.name)
        put("recoveryCodes", JSONArray().also { a ->
            recoveryCodes.forEach { rc ->
                a.put(JSONObject().apply { put("code", rc.code); put("used", rc.used) })
            }
        })
        put("url", url)
        put("linkedPasswordId", linkedPasswordId)
        put("notes", notes); put("category", category.name)
        put("tags", JSONArray(tags)); put("isFavorite", isFavorite)
        put("isTravelSafe", isTravelSafe)
        put("createdAt", createdAt); put("updatedAt", updatedAt)
    }

    private fun CreditCard.toJson() = JSONObject().apply {
        put("id", id); put("issuer", issuer)
        put("brand", brand); put("cardNumber", cardNumber)
        put("cardholder", cardholder); put("expiryDate", expiryDate)
        put("cvv", cvv); put("bankPhone", bankPhone)
        put("category", category.name); put("tags", JSONArray(tags))
        put("isFavorite", isFavorite); put("isTravelSafe", isTravelSafe)
        put("createdAt", createdAt); put("updatedAt", updatedAt)
    }

    private fun BankAccount.toJson() = JSONObject().apply {
        put("id", id); put("bankName", bankName)
        put("swiftBic", swiftBic); put("accountType", accountType)
        put("accountNumber", accountNumber); put("cbuIban", cbuIban)
        put("alias", alias); put("notes", notes)
        put("linkedPasswordId", linkedPasswordId)
        put("category", category.name); put("tags", JSONArray(tags))
        put("isFavorite", isFavorite); put("isTravelSafe", isTravelSafe)
        put("createdAt", createdAt); put("updatedAt", updatedAt)
    }

    private fun CryptoWallet.toJson() = JSONObject().apply {
        put("id", id); put("address", address)
        put("seedPhrase", seedPhrase); put("privateKey", privateKey)
        put("notes", notes); put("network", network); put("asset", asset)
        put("category", category.name); put("tags", JSONArray(tags))
        put("isFavorite", isFavorite); put("isTravelSafe", isTravelSafe)
        put("createdAt", createdAt); put("updatedAt", updatedAt)
    }

    private fun SecureNote.toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("content", content)
        put("category", category.name); put("tags", JSONArray(tags))
        put("isFavorite", isFavorite); put("isTravelSafe", isTravelSafe)
        put("createdAt", createdAt); put("updatedAt", updatedAt)
    }

    private fun CustomEntry.toJson() = JSONObject().apply {
        put("id", id); put("title", title)
        put("fields", JSONArray().also { a ->
            fields.forEach { f ->
                a.put(JSONObject().apply {
                    put("id", f.id); put("label", f.label)
                    put("type", f.type.name); put("value", f.value)
                })
            }
        })
        put("category", category.name); put("tags", JSONArray(tags))
        put("isFavorite", isFavorite); put("isTravelSafe", isTravelSafe)
        put("createdAt", createdAt); put("updatedAt", updatedAt)
    }

    private fun JSONObject.toVaultEntry() = VaultEntry(
        id          = getString("id"),
        title       = optString("title"),
        username    = optString("username"),
        email       = optString("email"),
        password    = optString("password"),
        urls        = buildList {
                          val arr = optJSONArray("urls")
                          if (arr != null) {
                              for (i in 0 until arr.length()) {
                                  val u = arr.optString(i).trim()
                                  if (u.isNotEmpty()) add(u)
                              }
                          } else {
                              val legacy = optString("url").trim()
                              if (legacy.isNotEmpty()) add(legacy)
                          }
                      },
        notes       = optString("notes"),
        category    = EntryCategory.fromString(optString("category")),
        tags        = optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
        isFavorite  = optBoolean("isFavorite"),
        isTravelSafe = optBoolean("isTravelSafe"),
        createdAt   = optLong("createdAt", System.currentTimeMillis()),
        updatedAt   = optLong("updatedAt", System.currentTimeMillis()),
        isDeleted   = false,
        deletedAt   = null
    )

    private fun JSONObject.toTwoFactorAccount(): TwoFactorAccount {
        val rcArray = optJSONArray("recoveryCodes")
        val codes   = if (rcArray != null) {
            (0 until rcArray.length()).map { i ->
                val o = rcArray.getJSONObject(i)
                RecoveryCode(code = o.getString("code"), used = o.optBoolean("used"))
            }
        } else emptyList()
        return TwoFactorAccount(
            id            = getString("id"),
            name          = optString("name"),
            issuer        = optString("issuer"),
            accountLabel  = optString("accountLabel"),
            secret        = optString("secret"),
            digits        = optInt("digits", 6),
            period        = optInt("period", 30),
            algorithm     = runCatching { TotpAlgorithm.valueOf(optString("algorithm")) }.getOrDefault(TotpAlgorithm.SHA1),
            recoveryCodes = codes,
            url           = optString("url"),
            linkedPasswordId = optString("linkedPasswordId"),
            notes         = optString("notes"),
            category      = EntryCategory.fromString(optString("category")),
            tags          = optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
            isFavorite    = optBoolean("isFavorite"),
            isTravelSafe  = optBoolean("isTravelSafe"),
            createdAt     = optLong("createdAt", System.currentTimeMillis()),
            updatedAt     = optLong("updatedAt", System.currentTimeMillis()),
            isDeleted     = false,
            deletedAt     = null
        )
    }

    private fun JSONObject.toCreditCard() = CreditCard(
        id          = getString("id"),
        issuer      = optString("issuer"),
        brand       = optString("brand"),
        cardNumber  = optString("cardNumber"),
        cardholder  = optString("cardholder"),
        expiryDate  = optString("expiryDate"),
        cvv         = optString("cvv"),
        bankPhone   = optString("bankPhone"),
        category    = EntryCategory.fromString(optString("category")),
        tags        = optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
        isFavorite  = optBoolean("isFavorite"),
        isTravelSafe = optBoolean("isTravelSafe"),
        createdAt   = optLong("createdAt", System.currentTimeMillis()),
        updatedAt   = optLong("updatedAt", System.currentTimeMillis()),
        isDeleted   = false,
        deletedAt   = null
    )

    private fun JSONObject.toBankAccount() = BankAccount(
        id            = getString("id"),
        bankName      = optString("bankName"),
        swiftBic      = optString("swiftBic"),
        accountType   = optString("accountType"),
        accountNumber = optString("accountNumber"),
        cbuIban       = optString("cbuIban"),
        alias         = optString("alias"),
        notes         = optString("notes"),
        linkedPasswordId = optString("linkedPasswordId"),
        category      = EntryCategory.fromString(optString("category")),
        tags          = optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
        isFavorite    = optBoolean("isFavorite"),
        isTravelSafe  = optBoolean("isTravelSafe"),
        createdAt     = optLong("createdAt", System.currentTimeMillis()),
        updatedAt     = optLong("updatedAt", System.currentTimeMillis()),
        isDeleted     = false,
        deletedAt     = null
    )

    private fun JSONObject.toCryptoWallet() = CryptoWallet(
        id          = getString("id"),
        address     = optString("address"),
        seedPhrase  = optString("seedPhrase"),
        privateKey  = optString("privateKey"),
        notes       = optString("notes"),
        network     = optString("network"),
        asset       = optString("asset"),
        category    = EntryCategory.fromString(optString("category")),
        tags        = optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
        isFavorite  = optBoolean("isFavorite"),
        isTravelSafe = optBoolean("isTravelSafe"),
        createdAt   = optLong("createdAt", System.currentTimeMillis()),
        updatedAt   = optLong("updatedAt", System.currentTimeMillis()),
        isDeleted   = false,
        deletedAt   = null
    )

    private fun JSONObject.toSecureNote() = SecureNote(
        id          = getString("id"),
        title       = optString("title"),
        content     = optString("content"),
        category    = EntryCategory.fromString(optString("category")),
        tags        = optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
        isFavorite  = optBoolean("isFavorite"),
        isTravelSafe = optBoolean("isTravelSafe"),
        createdAt   = optLong("createdAt", System.currentTimeMillis()),
        updatedAt   = optLong("updatedAt", System.currentTimeMillis()),
        isDeleted   = false,
        deletedAt   = null
    )

    private fun Vehicle.toJson() = JSONObject().apply {
        put("id", id); put("make", make); put("model", model)
        put("year", year); put("licensePlate", licensePlate); put("vin", vin)
        put("inspectionExpiryDate", inspectionExpiryDate)
        put("insuranceExpiryDate", insuranceExpiryDate)
        put("insuranceCompany", insuranceCompany)
        put("policyNumber", policyNumber)
        put("driverLicenseExpiryDate", driverLicenseExpiryDate)
        put("notes", notes)
        put("category", category.name); put("tags", JSONArray(tags))
        put("isFavorite", isFavorite); put("isTravelSafe", isTravelSafe)
        put("createdAt", createdAt); put("updatedAt", updatedAt)
    }

    private fun JSONObject.toVehicle() = Vehicle(
        id                       = getString("id"),
        make                     = optString("make"),
        model                    = optString("model"),
        year                     = optString("year"),
        licensePlate             = optString("licensePlate"),
        vin                      = optString("vin"),
        inspectionExpiryDate     = optString("inspectionExpiryDate"),
        insuranceExpiryDate      = optString("insuranceExpiryDate"),
        insuranceCompany         = optString("insuranceCompany"),
        policyNumber             = optString("policyNumber"),
        driverLicenseExpiryDate  = optString("driverLicenseExpiryDate"),
        notes                    = optString("notes"),
        category                 = EntryCategory.fromString(optString("category")),
        tags                     = optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
        isFavorite               = optBoolean("isFavorite"),
        isTravelSafe             = optBoolean("isTravelSafe"),
        createdAt                = optLong("createdAt", System.currentTimeMillis()),
        updatedAt                = optLong("updatedAt", System.currentTimeMillis()),
        isDeleted                = false,
        deletedAt                = null
    )

    private fun JSONObject.toCustomEntry(): CustomEntry {
        val fieldsArr = optJSONArray("fields")
        val fields = if (fieldsArr != null) {
            (0 until fieldsArr.length()).map { i ->
                val o = fieldsArr.getJSONObject(i)
                CustomField(
                    id    = o.optString("id"),
                    label = o.optString("label"),
                    type  = runCatching { CustomFieldType.valueOf(o.optString("type")) }.getOrDefault(CustomFieldType.TEXT),
                    value = o.optString("value")
                )
            }
        } else emptyList()
        return CustomEntry(
            id          = getString("id"),
            title       = optString("title"),
            fields      = fields,
            category    = EntryCategory.fromString(optString("category")),
            tags        = optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
            isFavorite  = optBoolean("isFavorite"),
            isTravelSafe = optBoolean("isTravelSafe"),
            createdAt   = optLong("createdAt", System.currentTimeMillis()),
            updatedAt   = optLong("updatedAt", System.currentTimeMillis()),
            isDeleted   = false,
            deletedAt   = null
        )
    }
}
