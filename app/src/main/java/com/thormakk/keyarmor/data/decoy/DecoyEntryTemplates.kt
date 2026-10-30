package com.thormakk.keyarmor.data.decoy

import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.EntryCategory
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.VaultMetadata
import java.security.SecureRandom
import java.util.UUID
import kotlin.random.asKotlinRandom

object DecoyEntryTemplates {

    data class MainVaultSeed(
        val entries:      List<VaultEntry>,
        val bankAccount:  BankAccount,
        val creditCard:   CreditCard,
        val emails:       List<String>
    )

    private val FIRST_NAMES = listOf(
        "Martina", "Lucas", "Sofia", "Mateo", "Valentina", "Nicolas",
        "Camila", "Tomas", "Julieta", "Franco", "Agustina", "Bruno"
    )
    private val LAST_NAMES = listOf(
        "Gomez", "Fernandez", "Rodriguez", "Lopez", "Diaz",
        "Martinez", "Perez", "Sanchez", "Romero", "Alvarez"
    )
    private val EMAIL_DOMAINS = listOf("gmail.com", "outlook.com", "hotmail.com")

    private data class ServiceTemplate(val name: String, val url: String)

    private val SOCIAL_POOL = listOf(
        ServiceTemplate("Instagram",     "instagram.com"),
        ServiceTemplate("Facebook",      "facebook.com"),
        ServiceTemplate("X (Twitter)",   "x.com"),
        ServiceTemplate("LinkedIn",      "linkedin.com"),
        ServiceTemplate("TikTok",        "tiktok.com"),
        ServiceTemplate("Pinterest",     "pinterest.com"),
        ServiceTemplate("Reddit",        "reddit.com"),
        ServiceTemplate("Snapchat",      "snapchat.com")
    )

    private val SHOPPING_POOL = listOf(
        ServiceTemplate("Mercado Libre", "mercadolibre.com"),
        ServiceTemplate("Amazon",        "amazon.com"),
        ServiceTemplate("Netflix",       "netflix.com"),
        ServiceTemplate("Spotify",       "spotify.com"),
        ServiceTemplate("Rappi",         "rappi.com"),
        ServiceTemplate("Uber Eats",     "ubereats.com"),
        ServiceTemplate("Booking.com",   "booking.com"),
        ServiceTemplate("Despegar",      "despegar.com")
    )

    private val BANK_LOGIN_POOL = listOf(
        ServiceTemplate("Banco Santander", "santander.com.ar"),
        ServiceTemplate("Banco Galicia",   "bancogalicia.com.ar"),
        ServiceTemplate("BBVA",            "bbva.com.ar"),
        ServiceTemplate("Banco Nación",    "bna.com.ar"),
        ServiceTemplate("Mercado Pago",    "mercadopago.com"),
        ServiceTemplate("Banco Itaú",      "itau.com.ar")
    )

    private val secureRandom = SecureRandom()
    private val rng = secureRandom.asKotlinRandom()

    private fun randomPassword(length: Int = 14): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#\$%&*"
        return (1..length).map { chars[secureRandom.nextInt(chars.length)] }.joinToString("")
    }

    private fun randomHandle(): String =
        "${FIRST_NAMES.random(rng).lowercase()}${rng.nextInt(10, 999)}"

    private fun randomEmail(): String =
        "${randomHandle()}@${EMAIL_DOMAINS.random(rng)}"

    private fun personalEmailPair(): List<String> {
        val first  = randomEmail()
        var second = randomEmail()
        while (second == first) second = randomEmail()
        return listOf(first, second)
    }

    private fun nextEmailFrom(emails: List<String>, index: Int): String =
        emails[index % emails.size]

    private fun randomFullName(): String =
        "${FIRST_NAMES.random(rng)} ${LAST_NAMES.random(rng)}"

    private fun randomPastTimestamp(): Long {
        val daysAgo = rng.nextLong(30, 700)
        return System.currentTimeMillis() - daysAgo * 24 * 60 * 60 * 1000
    }

    private fun randomUpdatedAt(createdAt: Long): Long {
        if (rng.nextInt(100) >= 40) return createdAt
        val now = System.currentTimeMillis()
        if (now <= createdAt) return createdAt
        return createdAt + (rng.nextDouble() * (now - createdAt)).toLong()
    }

    private fun randomDigits(count: Int): String =
        (1..count).map { secureRandom.nextInt(10) }.joinToString("")

    private fun baseEntry(
        vaultId:  String,
        title:    String,
        url:      String,
        category: EntryCategory,
        email:    String = randomEmail(),
        username: String = ""
    ): VaultEntry {
        val ts = randomPastTimestamp()
        return VaultEntry(
            id           = UUID.randomUUID().toString(),
            title        = title,
            username     = username,
            email        = email,
            password     = randomPassword(),
            urls         = listOf(url),
            notes        = "",
            category     = category,
            isFavorite   = false,
            isTravelSafe = false,
            isPinned     = false,
            createdAt    = ts,
            updatedAt    = randomUpdatedAt(ts),
            isDeleted    = false,
            deletedAt    = null,
            vaultId      = vaultId
        )
    }

    fun generateMainVaultSeed(): MainVaultSeed {
        val vaultId = VaultMetadata.DECOY_MAIN_VAULT_ID
        val entries = mutableListOf<VaultEntry>()
        val emails  = personalEmailPair()
        var emailIx = 0

        SOCIAL_POOL.shuffled(rng).take(6).forEach { svc ->
            entries += baseEntry(vaultId, svc.name, svc.url, EntryCategory.REDES_SOCIALES, email = nextEmailFrom(emails, emailIx++))
        }
        SHOPPING_POOL.shuffled(rng).take(4).forEach { svc ->
            entries += baseEntry(vaultId, svc.name, svc.url, EntryCategory.COMPRAS_WEB, email = nextEmailFrom(emails, emailIx++))
        }
        BANK_LOGIN_POOL.shuffled(rng).take(2).forEach { svc ->
            entries += baseEntry(vaultId, svc.name, svc.url, EntryCategory.BANCOS_BILLETERAS, email = nextEmailFrom(emails, emailIx++))
        }
        emails.forEach { addr ->
            entries += baseEntry(
                vaultId  = vaultId,
                title    = addr.substringAfter("@").substringBefore(".").replaceFirstChar { it.uppercase() },
                url      = addr.substringAfter("@"),
                category = EntryCategory.SIN_CATEGORIZAR,
                email    = addr,
                username = addr
            )
        }

        val bankTemplate = BANK_LOGIN_POOL.random(rng)
        val ts = randomPastTimestamp()
        val bankAccount = BankAccount(
            id            = UUID.randomUUID().toString(),
            bankName      = bankTemplate.name,
            swiftBic      = "",
            accountType   = listOf("Caja de ahorro", "Cuenta corriente").random(rng),
            accountNumber = randomDigits(11),
            cbuIban       = randomDigits(22),
            alias         = "${FIRST_NAMES.random(rng).lowercase()}.${LAST_NAMES.random(rng).lowercase()}",
            notes         = "",
            category      = EntryCategory.BANCOS_BILLETERAS,
            createdAt     = ts,
            updatedAt     = randomUpdatedAt(ts),
            vaultId       = vaultId
        )

        val cardTs = randomPastTimestamp()
        val isVisa = rng.nextBoolean()
        val currentYear2Digits = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100
        val creditCard = CreditCard(
            id         = UUID.randomUUID().toString(),
            brand      = if (isVisa) "Visa" else "Mastercard",
            issuer     = bankTemplate.name,
            cardNumber = (if (isVisa) "4" else "5") + randomDigits(15),
            cardholder = randomFullName().uppercase(),
            expiryDate = "%02d/%02d".format(rng.nextInt(1, 13), currentYear2Digits + rng.nextInt(2, 5)),
            cvv        = randomDigits(3),
            bankPhone  = "",
            category   = EntryCategory.BANCOS_BILLETERAS,
            createdAt  = cardTs,
            updatedAt  = randomUpdatedAt(cardTs),
            vaultId    = vaultId
        )

        return MainVaultSeed(entries, bankAccount, creditCard, emails)
    }

    fun generatePrivateVaultEntries(emails: List<String>): List<VaultEntry> {
        val vaultId = VaultMetadata.DECOY_PRIVATE_VAULT_ID
        val entries = mutableListOf<VaultEntry>()
        var emailIx = 0

        val addr = nextEmailFrom(emails, emailIx++)
        entries += baseEntry(
            vaultId  = vaultId,
            title    = addr.substringAfter("@").substringBefore(".").replaceFirstChar { it.uppercase() },
            url      = addr.substringAfter("@"),
            category = EntryCategory.SIN_CATEGORIZAR,
            email    = addr,
            username = addr
        )
        SOCIAL_POOL.shuffled(rng).take(rng.nextInt(2, 4)).forEach { svc ->
            entries += baseEntry(vaultId, svc.name, svc.url, EntryCategory.REDES_SOCIALES, email = nextEmailFrom(emails, emailIx++))
        }

        return entries
    }
}
