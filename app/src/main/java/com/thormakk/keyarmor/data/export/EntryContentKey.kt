package com.thormakk.keyarmor.data.export

import com.thormakk.keyarmor.data.repository.TagRepository
import com.thormakk.keyarmor.domain.model.BankAccount
import com.thormakk.keyarmor.domain.model.CreditCard
import com.thormakk.keyarmor.domain.model.CryptoWallet
import com.thormakk.keyarmor.domain.model.CustomEntry
import com.thormakk.keyarmor.domain.model.IdentityDocument
import com.thormakk.keyarmor.domain.model.PasskeyCredential
import com.thormakk.keyarmor.domain.model.SecureNote
import com.thormakk.keyarmor.domain.model.TwoFactorAccount
import com.thormakk.keyarmor.domain.model.VaultEntry
import com.thormakk.keyarmor.domain.model.Vehicle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryContentKey @Inject constructor(
    private val tagRepository: TagRepository,
) {
    private fun tagKeys(tags: List<String>): Set<String> =
        tags.map { tagRepository.canonicalTagKey(it) }.toSet()

    fun of(e: VaultEntry): List<Any?> = listOf(
        e.title, e.username, e.email, e.password,
        e.urls.toSet(), e.notes, e.category, tagKeys(e.tags),
        e.vaultId, e.inheritedFrom
    )

    fun of(e: TwoFactorAccount): List<Any?> = listOf(
        e.name, e.issuer, e.accountLabel, e.secret, e.digits, e.period, e.algorithm,
        e.recoveryCodes.map { it.code }.toSet(),
        e.url, e.linkedPasswordId, e.notes, e.category, tagKeys(e.tags),
        e.vaultId, e.inheritedFrom
    )

    fun of(e: PasskeyCredential): List<Any?> = listOf(
        e.rpId, e.rpName, e.userId, e.userName, e.userDisplayName,
        e.privateKeyPkcs8.toList(), e.publicKeyCose.toList(),
        e.notes, e.urls, e.linkedPasswordId, e.category, tagKeys(e.tagList),
        e.customAccountLabel, e.vaultId, e.inheritedFrom
    )

    fun of(e: BankAccount): List<Any?> = listOf(
        e.bankName, e.swiftBic, e.accountType, e.accountNumber, e.cbuIban, e.alias,
        e.notes, e.linkedPasswordId, e.category, tagKeys(e.tags),
        e.vaultId, e.inheritedFrom
    )

    fun of(e: CreditCard): List<Any?> = listOf(
        e.brand, e.issuer, e.cardNumber, e.cardholder, e.expiryDate, e.cvv, e.bankPhone,
        e.category, tagKeys(e.tags), e.vaultId, e.inheritedFrom
    )

    fun of(e: SecureNote): List<Any?> = listOf(
        e.title, e.content, e.category, tagKeys(e.tags),
        e.vaultId, e.inheritedFrom, e.historyEnabled
    )

    fun of(e: CustomEntry): List<Any?> = listOf(
        e.title, e.fields.map { Triple(it.label, it.type, it.value) },
        e.category, tagKeys(e.tags), e.vaultId, e.inheritedFrom
    )

    fun of(e: IdentityDocument): List<Any?> = listOf(
        e.docType, e.number, e.issuingCountry, e.issueDate, e.expiryDate, e.birthDate,
        e.holderName, e.notes,
        e.category, tagKeys(e.tags), e.vaultId, e.inheritedFrom
    )

    fun of(e: Vehicle): List<Any?> = listOf(
        e.make, e.model, e.year, e.licensePlate, e.vin,
        e.inspectionExpiryDate, e.insuranceExpiryDate, e.insuranceCompany, e.policyNumber,
        e.driverLicenseExpiryDate, e.notes,
        e.category, tagKeys(e.tags), e.vaultId, e.inheritedFrom
    )

    fun of(e: CryptoWallet): List<Any?> = listOf(
        e.address, e.seedPhrase, e.privateKey, e.notes, e.manualBalance, e.exchangeUrl,
        e.linkedPasswordId, e.network, e.asset,
        e.category, tagKeys(e.tags), e.vaultId, e.inheritedFrom
    )
}
