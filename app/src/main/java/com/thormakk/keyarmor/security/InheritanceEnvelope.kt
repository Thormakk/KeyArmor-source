package com.thormakk.keyarmor.security

import android.util.Base64
import org.json.JSONObject
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPrivateKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InheritanceEnvelope @Inject constructor(
    private val crypto: CryptoManager,
) {
    private val secureRandom = SecureRandom()

    data class HeirKeyPairCodes(
        val publicKeyB64: String,
        val privateKeyCode: String,
    )

    fun generateHeirKeyPair(): HeirKeyPairCodes {
        val kp = generateEcKeyPair()
        val pubB64 = Base64.encodeToString(kp.public.encoded, Base64.NO_WRAP)
        val scalar = (kp.private as ECPrivateKey).s
        val privCode = Base64.encodeToString(bigIntTo32(scalar), Base64.NO_WRAP)
        return HeirKeyPairCodes(publicKeyB64 = pubB64, privateKeyCode = privCode)
    }

    fun sealForHeir(
        plainVaultJson:   String,
        heirPublicKeyB64: String,
        ownerSign:        (ByteArray) -> ByteArray
    ): String {
        val heirPub = ecPublicFromX509(heirPublicKeyB64)

        val dekBytes = ByteArray(32).also { secureRandom.nextBytes(it) }
        val dek      = SecretKeySpec(dekBytes, "AES")
        val vaultCt  = crypto.encrypt(plainVaultJson, dek)

        val ephemeral = generateEcKeyPair()
        val epkBytes  = ephemeral.public.encoded
        val shared    = ecdh(ephemeral.private as java.security.PrivateKey, heirPub)
        val wrapKey   = hkdfAesKey(shared, infoFor(epkBytes))

        val wrappedDek = crypto.encryptBytes(dekBytes, wrapKey)

        dekBytes.fill(0)
        shared.fill(0)

        val epkB64  = Base64.encodeToString(epkBytes,   Base64.NO_WRAP)
        val wrapB64 = Base64.encodeToString(wrappedDek, Base64.NO_WRAP)
        val sig = ownerSign(signingBytes(ENVELOPE_VERSION, epkB64, wrapB64, vaultCt))

        return JSONObject().apply {
            put("version", ENVELOPE_VERSION)
            put("alg",     ALG)
            put("epk",     epkB64)
            put("wrap",    wrapB64)
            put("vault",   vaultCt)
            put("sig",     Base64.encodeToString(sig, Base64.NO_WRAP))
        }.toString()
    }

    fun openAsHeir(
        blobJson:                 String,
        privateKeyCode:           String,
        ownerSigningPublicKeyB64: String
    ): String {
        val json = JSONObject(blobJson)
        val version = json.getInt("version")
        require(version == ENVELOPE_VERSION) { "Versión de envelope no soportada: $version" }

        val epkB64  = json.getString("epk")
        val wrapB64 = json.getString("wrap")
        val vaultCt = json.getString("vault")

        val sigOk = verifyOwnerSig(
            ownerSigningPublicKeyB64,
            signingBytes(version, epkB64, wrapB64, vaultCt),
            json.getString("sig")
        )
        if (!sigOk) throw java.security.SignatureException("Firma del titular inválida (origen no verificado)")

        val heirPriv = ecPrivateFromScalar(privateKeyCode)
        val epkBytes = Base64.decode(epkB64, Base64.NO_WRAP)
        val ePub     = ecPublicFromX509Bytes(epkBytes)

        val shared  = ecdh(heirPriv, ePub)
        val wrapKey = hkdfAesKey(shared, infoFor(epkBytes))
        shared.fill(0)

        val dekBytes = crypto.decryptBytes(Base64.decode(wrapB64, Base64.NO_WRAP), wrapKey)
        val dek      = SecretKeySpec(dekBytes, "AES")
        val plain    = crypto.decrypt(vaultCt, dek)
        dekBytes.fill(0)
        return plain
    }

    private fun signingBytes(version: Int, epkB64: String, wrapB64: String, vaultCt: String): ByteArray =
        "$version|$epkB64|$wrapB64|$vaultCt".toByteArray(Charsets.UTF_8)

    private fun verifyOwnerSig(ownerPubB64: String, data: ByteArray, sigB64: String): Boolean = try {
        val pub = ecPublicFromX509(ownerPubB64)
        val v   = java.security.Signature.getInstance("SHA256withECDSA")
        v.initVerify(pub)
        v.update(data)
        v.verify(Base64.decode(sigB64, Base64.NO_WRAP))
    } catch (_: Exception) {
        false
    }

    private fun generateEcKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec(CURVE))
        return kpg.generateKeyPair()
    }

    private fun ecdh(priv: java.security.PrivateKey, pub: java.security.PublicKey): ByteArray {
        val ka = KeyAgreement.getInstance("ECDH")
        ka.init(priv)
        ka.doPhase(pub, true)
        return ka.generateSecret()
    }

    private fun ecPublicFromX509(b64: String) =
        ecPublicFromX509Bytes(Base64.decode(b64, Base64.NO_WRAP))

    private fun ecPublicFromX509Bytes(der: ByteArray): java.security.PublicKey {
        val pub = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(der))
        validatePointOnCurve(pub as java.security.interfaces.ECPublicKey)
        return pub
    }

    private fun validatePointOnCurve(pub: java.security.interfaces.ECPublicKey) {
        val w = pub.w
        if (w == java.security.spec.ECPoint.POINT_INFINITY) {
            throw java.security.GeneralSecurityException("Punto EC en el infinito")
        }
        val curve = pub.params.curve
        val p = (curve.field as java.security.spec.ECFieldFp).p
        val a = curve.a
        val b = curve.b
        val x = w.affineX
        val y = w.affineY
        val two   = BigInteger.valueOf(2)
        val three = BigInteger.valueOf(3)
        if (x.signum() < 0 || x >= p || y.signum() < 0 || y >= p) {
            throw java.security.GeneralSecurityException("Coordenada EC fuera de rango")
        }
        val lhs = y.modPow(two, p)
        val rhs = x.modPow(three, p).add(a.multiply(x)).add(b).mod(p)
        if (lhs != rhs) {
            throw java.security.GeneralSecurityException("Punto EC no pertenece a la curva")
        }
    }

    private fun ecPrivateFromScalar(codeB64: String): java.security.PrivateKey {
        val s = BigInteger(1, Base64.decode(codeB64, Base64.NO_WRAP))
        return KeyFactory.getInstance("EC").generatePrivate(ECPrivateKeySpec(s, p256Params()))
    }

    private fun p256Params(): ECParameterSpec {
        val ap = AlgorithmParameters.getInstance("EC")
        ap.init(ECGenParameterSpec(CURVE))
        return ap.getParameterSpec(ECParameterSpec::class.java)
    }

    private fun hkdfAesKey(ikm: ByteArray, info: ByteArray): SecretKeySpec {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(ByteArray(32), "HmacSHA256"))
        val prk = mac.doFinal(ikm)
        mac.init(SecretKeySpec(prk, "HmacSHA256"))
        mac.update(info)
        mac.update(0x01.toByte())
        val okm = mac.doFinal()
        val claveDeEnvoltura = SecretKeySpec(okm.copyOf(32), "AES")
        prk.fill(0)
        okm.fill(0)
        return claveDeEnvoltura
    }

    private fun infoFor(epkBytes: ByteArray): ByteArray = INFO_PREFIX + epkBytes

    private fun bigIntTo32(v: BigInteger): ByteArray {
        val raw = v.toByteArray()
        return when {
            raw.size == 32 -> raw
            raw.size == 33 && raw[0] == 0.toByte() -> raw.copyOfRange(1, 33)
            raw.size < 32 -> ByteArray(32 - raw.size) + raw
            else -> raw.copyOfRange(raw.size - 32, raw.size)
        }
    }

    companion object {
        private const val ENVELOPE_VERSION = 5
        private const val CURVE = "secp256r1"
        private const val ALG   = "ECDH-ES+A256GCM/P-256+ECDSA-P256"
        private val INFO_PREFIX = "keyarmor-inheritance-v5".toByteArray(Charsets.UTF_8)
    }
}
