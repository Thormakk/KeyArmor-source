package com.thormakk.keyarmor.data.passkey

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasskeyCrypto @Inject constructor() {

    @Suppress("ArrayInDataClass")
    data class GeneratedKeyPair(
        val credentialId: String,
        val privateKeyPkcs8: ByteArray,
        val publicKeyCose: ByteArray,
        val publicKeySubjectPublicKeyInfo: ByteArray
    )

    fun generateKeyPair(): GeneratedKeyPair {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"), SecureRandom())
        val kp = kpg.generateKeyPair()

        val ecPub = kp.public as ECPublicKey
        val x = ecPub.w.affineX.toByteArray().normalizeP256Coord()
        val y = ecPub.w.affineY.toByteArray().normalizeP256Coord()

        val coseKey = CborUtils.encodeMap(
            linkedMapOf<Any, Any>(
                1  to 2,
                3  to -7,
                -1 to 1,
                -2 to x,
                -3 to y
            )
        )

        val credIdBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val credentialId = Base64.encodeToString(credIdBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

        return GeneratedKeyPair(
            credentialId                  = credentialId,
            privateKeyPkcs8               = kp.private.encoded,
            publicKeyCose                 = coseKey,
            publicKeySubjectPublicKeyInfo = kp.public.encoded
        )
    }

    fun sign(
        privateKeyPkcs8: ByteArray,
        authData:        ByteArray,
        clientDataHash:  ByteArray
    ): ByteArray {
        val privateKey = loadPrivateKey(privateKeyPkcs8)
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(privateKey)
        sig.update(authData)
        sig.update(clientDataHash)
        return sig.sign()
    }

    fun buildAttestationAuthData(
        rpId:         String,
        credentialId: String,
        cosePublicKey: ByteArray,
        signCount:    Long = 0L
    ): ByteArray {
        val rpIdHash = sha256(rpId.toByteArray(Charsets.UTF_8))
        val flags = 0x5D.toByte()
        val credIdBytes = Base64.decode(credentialId, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

        return buildByteArray {
            write(rpIdHash)
            write(flags.toInt())
            writeUInt32BE(signCount)
            write(ByteArray(16))
            writeUInt16BE(credIdBytes.size.toLong())
            write(credIdBytes)
            write(cosePublicKey)
        }
    }

    fun buildAssertionAuthData(
        rpId:      String,
        signCount: Long
    ): ByteArray {
        val rpIdHash = sha256(rpId.toByteArray(Charsets.UTF_8))
        val flags = 0x1D.toByte()

        return buildByteArray {
            write(rpIdHash)
            write(flags.toInt())
            writeUInt32BE(signCount)
        }
    }

    fun buildAttestationObject(authData: ByteArray): ByteArray =
        CborUtils.encodeMap(
            linkedMapOf<Any, Any>(
                "fmt"      to "none",
                "attStmt"  to emptyMap<Any, Any>(),
                "authData" to authData
            )
        )

    fun buildCreateClientDataJson(challenge: String, origin: String): String =
        """{"type":"webauthn.create","challenge":"${challenge.jsonEscape()}","origin":"${origin.jsonEscape()}","crossOrigin":false}"""

    fun buildGetClientDataJson(challenge: String, origin: String): String =
        """{"type":"webauthn.get","challenge":"${challenge.jsonEscape()}","origin":"${origin.jsonEscape()}","crossOrigin":false}"""

    private fun String.jsonEscape(): String =
        replace("\\", "\\\\").replace("\"", "\\\"")

    private fun loadPrivateKey(pkcs8Bytes: ByteArray): PrivateKey {
        val kf = KeyFactory.getInstance("EC")
        return kf.generatePrivate(PKCS8EncodedKeySpec(pkcs8Bytes))
    }

    private fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun ByteArray.normalizeP256Coord(): ByteArray = when {
        size == 33 && this[0] == 0.toByte() -> copyOfRange(1, 33)
        size < 32  -> ByteArray(32 - size) + this
        size == 32 -> this
        else       -> copyOfRange(size - 32, size)
    }

    private fun buildByteArray(block: java.io.ByteArrayOutputStream.() -> Unit): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        out.block()
        return out.toByteArray()
    }

    private fun java.io.ByteArrayOutputStream.writeUInt32BE(value: Long) {
        write((value shr 24).toInt() and 0xFF)
        write((value shr 16).toInt() and 0xFF)
        write((value shr  8).toInt() and 0xFF)
        write( value.toInt()         and 0xFF)
    }

    private fun java.io.ByteArrayOutputStream.writeUInt16BE(value: Long) {
        write((value shr 8).toInt() and 0xFF)
        write( value.toInt()        and 0xFF)
    }
}
