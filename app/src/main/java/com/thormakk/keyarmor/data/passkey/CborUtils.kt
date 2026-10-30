package com.thormakk.keyarmor.data.passkey

import java.io.ByteArrayOutputStream

object CborUtils {

    fun encodeMap(entries: Map<Any, Any>): ByteArray {
        val out = ByteArrayOutputStream()
        writeMajorType(out, 5, entries.size.toLong())
        for ((k, v) in entries) {
            writeValue(out, k)
            writeValue(out, v)
        }
        return out.toByteArray()
    }

    private fun writeValue(out: ByteArrayOutputStream, value: Any) {
        when (value) {
            is Int        -> writeInteger(out, value.toLong())
            is Long       -> writeInteger(out, value)
            is ByteArray  -> {
                writeMajorType(out, 2, value.size.toLong())
                out.write(value)
            }
            is String     -> {
                val bytes = value.toByteArray(Charsets.UTF_8)
                writeMajorType(out, 3, bytes.size.toLong())
                out.write(bytes)
            }
            is Map<*, *>  -> {
                @Suppress("UNCHECKED_CAST")
                val m = value as Map<Any, Any>
                writeMajorType(out, 5, m.size.toLong())
                for ((k, v) in m) {
                    writeValue(out, k)
                    writeValue(out, v)
                }
            }
            else -> error("Tipo CBOR no soportado: ${value::class.simpleName}")
        }
    }

    private fun writeInteger(out: ByteArrayOutputStream, n: Long) {
        if (n >= 0) {
            writeMajorType(out, 0, n)
        } else {
            writeMajorType(out, 1, -1L - n)
        }
    }

    private fun writeMajorType(out: ByteArrayOutputStream, major: Int, value: Long) {
        val mt = (major shl 5)
        when {
            value <= 23L    -> out.write(mt or value.toInt())
            value <= 0xFFL  -> {
                out.write(mt or 24)
                out.write(value.toInt())
            }
            value <= 0xFFFFL -> {
                out.write(mt or 25)
                out.write((value shr 8).toInt())
                out.write(value.toInt())
            }
            else             -> {
                out.write(mt or 26)
                out.write((value shr 24).toInt())
                out.write((value shr 16).toInt())
                out.write((value shr  8).toInt())
                out.write(value.toInt())
            }
        }
    }
}
