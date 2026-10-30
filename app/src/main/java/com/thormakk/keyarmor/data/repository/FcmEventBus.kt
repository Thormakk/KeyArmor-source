package com.thormakk.keyarmor.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class MemberAcceptedPayload(
    val memberEmail: String
)

data class HeirAcceptedPayload(
    val heirEmail:              String,
    val encryptedHeirSubBase64: String?,
    val heirPublicKeyBase64:    String? = null
)

data class InheritanceActivatedPayload(
    val ownerEmail:  String,
    val driveFileId: String?,
)

@Singleton
class FcmEventBus @Inject constructor() {

    private val _heirConsentEvent = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )
    val heirConsentEvent: SharedFlow<String> = _heirConsentEvent.asSharedFlow()

    fun emitHeirConsent(ownerEmail: String) {
        _heirConsentEvent.tryEmit(ownerEmail)
    }

    private val _heirAcceptedEvent = MutableSharedFlow<HeirAcceptedPayload>(
        extraBufferCapacity = 1
    )
    val heirAcceptedEvent: SharedFlow<HeirAcceptedPayload> = _heirAcceptedEvent.asSharedFlow()

    fun emitHeirAccepted(heirEmail: String, encryptedHeirSubBase64: String?, heirPublicKeyBase64: String? = null) {
        _heirAcceptedEvent.tryEmit(HeirAcceptedPayload(heirEmail, encryptedHeirSubBase64, heirPublicKeyBase64))
    }

    private val _heirVaultEncryptedEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val heirVaultEncryptedEvent: SharedFlow<String> = _heirVaultEncryptedEvent.asSharedFlow()

    fun emitHeirVaultEncrypted(heirEmail: String) {
        _heirVaultEncryptedEvent.tryEmit(heirEmail)
    }

    private val _heirDeclinedEvent = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )
    val heirDeclinedEvent: SharedFlow<String> = _heirDeclinedEvent.asSharedFlow()

    fun emitHeirDeclined(heirEmail: String) {
        _heirDeclinedEvent.tryEmit(heirEmail)
    }

    private val _inheritanceActivatedEvent = MutableSharedFlow<InheritanceActivatedPayload>(
        extraBufferCapacity = 1
    )
    val inheritanceActivatedEvent: SharedFlow<InheritanceActivatedPayload> = _inheritanceActivatedEvent.asSharedFlow()

    fun emitInheritanceActivated(ownerEmail: String, driveFileId: String?) {
        _inheritanceActivatedEvent.tryEmit(InheritanceActivatedPayload(ownerEmail, driveFileId))
    }

    private val _memberAcceptedEvent = MutableSharedFlow<MemberAcceptedPayload>(
        extraBufferCapacity = 1
    )
    val memberAcceptedEvent: SharedFlow<MemberAcceptedPayload> = _memberAcceptedEvent.asSharedFlow()

    fun emitMemberAccepted(memberEmail: String) {
        _memberAcceptedEvent.tryEmit(MemberAcceptedPayload(memberEmail))
    }


    private val _memberPremiumRevoked = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    val memberPremiumRevoked: SharedFlow<Unit> = _memberPremiumRevoked.asSharedFlow()

    fun emitMemberPremiumRevoked() {
        _memberPremiumRevoked.tryEmit(Unit)
    }

    private val _memberAccessRevoked = MutableSharedFlow<String?>(
        extraBufferCapacity = 1
    )
    val memberAccessRevoked: SharedFlow<String?> = _memberAccessRevoked.asSharedFlow()

    fun emitMemberAccessRevoked(adminEmail: String?) {
        _memberAccessRevoked.tryEmit(adminEmail)
    }

    private val _memberLeftEvent = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )
    val memberLeftEvent: SharedFlow<String> = _memberLeftEvent.asSharedFlow()

    fun emitMemberLeft(memberEmail: String) {
        _memberLeftEvent.tryEmit(memberEmail)
    }

    private val _memberDeclinedEvent = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )
    val memberDeclinedEvent: SharedFlow<String> = _memberDeclinedEvent.asSharedFlow()

    fun emitMemberDeclined(memberEmail: String) {
        _memberDeclinedEvent.tryEmit(memberEmail)
    }

    private val _heirResignedEvent = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )
    val heirResignedEvent: SharedFlow<String> = _heirResignedEvent.asSharedFlow()

    fun emitHeirResigned(heirEmail: String) {
        _heirResignedEvent.tryEmit(heirEmail)
    }

    private val _heirRemovedByOwnerEvent = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    val heirRemovedByOwnerEvent: SharedFlow<Unit> = _heirRemovedByOwnerEvent.asSharedFlow()

    fun emitHeirRemovedByOwner() {
        _heirRemovedByOwnerEvent.tryEmit(Unit)
    }

    private val _heirConsentAcceptedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val heirConsentAcceptedEvent: SharedFlow<Unit> = _heirConsentAcceptedEvent.asSharedFlow()

    fun emitHeirConsentAccepted() {
        _heirConsentAcceptedEvent.tryEmit(Unit)
    }

    private val _inheritanceDeliveredEvent = MutableSharedFlow<List<String>>(extraBufferCapacity = 1)
    val inheritanceDeliveredEvent: SharedFlow<List<String>> = _inheritanceDeliveredEvent.asSharedFlow()

    fun emitInheritanceDelivered(heirEmails: List<String>) {
        _inheritanceDeliveredEvent.tryEmit(heirEmails)
    }

    private val _memberRoleAcceptedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val memberRoleAcceptedEvent: SharedFlow<Unit> = _memberRoleAcceptedEvent.asSharedFlow()

    fun emitMemberRoleAccepted() {
        _memberRoleAcceptedEvent.tryEmit(Unit)
    }

    private val _heirFingerprintConfirmedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val heirFingerprintConfirmedEvent: SharedFlow<Unit> = _heirFingerprintConfirmedEvent.asSharedFlow()

    fun emitHeirFingerprintConfirmed() {
        _heirFingerprintConfirmedEvent.tryEmit(Unit)
    }

    private val _heirKeyPendingConfirmationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val heirKeyPendingConfirmationEvent: SharedFlow<String> = _heirKeyPendingConfirmationEvent.asSharedFlow()

    fun emitHeirKeyPendingConfirmation(heirEmail: String) {
        _heirKeyPendingConfirmationEvent.tryEmit(heirEmail)
    }

    private val _heirLocallyRemovedEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val heirLocallyRemovedEvent: SharedFlow<String> = _heirLocallyRemovedEvent.asSharedFlow()

    fun emitHeirLocallyRemoved(heirEmail: String) {
        _heirLocallyRemovedEvent.tryEmit(heirEmail)
    }
}
