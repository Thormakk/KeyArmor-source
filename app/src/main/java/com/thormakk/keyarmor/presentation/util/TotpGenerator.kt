package com.thormakk.keyarmor.presentation.util

import com.thormakk.keyarmor.domain.model.TotpAlgorithm
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpGenerator {

    data class TotpCode(
        val code: String,
        val secondsRemaining: Int,
        val period: Int
    ) {
        val progress: Float get() = 1f - secondsRemaining.toFloat() / period
    }

    fun generate(
        secret: String,
        digits: Int = 6,
        period: Int = 30,
        algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
        nowMillis: Long = System.currentTimeMillis()
    ): TotpCode? {
        val keyBytes = decodeBase32(secret) ?: return null
        if (keyBytes.isEmpty()) return null

        val nowSec  = nowMillis / 1000L
        val counter = nowSec / period
        val mac = try {
            Mac.getInstance(algorithm.macName).apply {
                init(SecretKeySpec(keyBytes, algorithm.macName))
            }
        } catch (e: Exception) { return null }

        val counterBytes = ByteArray(8) { i ->
            ((counter ushr ((7 - i) * 8)) and 0xFF).toByte()
        }
        val hmac = try { mac.doFinal(counterBytes) } catch (e: Exception) { return null }

        val offset = (hmac[hmac.size - 1].toInt() and 0x0F)
        val binary = ((hmac[offset].toInt()     and 0x7F) shl 24) or
                     ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
                     ((hmac[offset + 2].toInt() and 0xFF) shl 8)  or
                     ( hmac[offset + 3].toInt() and 0xFF)
        val mod  = 10.0.pow(digits).toLong()
        val code = (binary.toLong() % mod).toString().padStart(digits, '0')

        val secondsRemaining = (period - (nowSec % period)).toInt()
        return TotpCode(code = code, secondsRemaining = secondsRemaining, period = period)
    }

    private val TotpAlgorithm.macName: String
        get() = when (this) {
            TotpAlgorithm.SHA1   -> "HmacSHA1"
            TotpAlgorithm.SHA256 -> "HmacSHA256"
            TotpAlgorithm.SHA512 -> "HmacSHA512"
        }

    private fun decodeBase32(input: String): ByteArray? {
        val cleaned = input.trim()
            .replace(" ", "")
            .replace("-", "")
            .uppercase()
            .trimEnd('=')
        if (cleaned.isEmpty()) return null

        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val out = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (ch in cleaned) {
            val v = alphabet.indexOf(ch)
            if (v < 0) return null
            buffer = (buffer shl 5) or v
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                out.add(((buffer ushr bitsLeft) and 0xFF).toByte())
            }
        }
        return out.toByteArray()
    }

    fun formatCode(code: String): String = when (code.length) {
        6    -> "${code.substring(0, 3)} ${code.substring(3)}"
        8    -> "${code.substring(0, 4)} ${code.substring(4)}"
        else -> code
    }
}
