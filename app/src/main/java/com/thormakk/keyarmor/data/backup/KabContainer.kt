package com.thormakk.keyarmor.data.backup

import com.thormakk.keyarmor.security.CryptoManager
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.spec.SecretKeySpec

object KabContainer {

    const val FORMAT        = "kab"
    const val VERSION       = 1
    const val MODE_PASSWORD = "password"
    const val MODE_SESSION  = "session"
    const val MODE_KEY      = "key"

    const val ENTRY_META     = "meta.json"
    const val ENTRY_VAULT    = "vault.enc"
    const val ENTRY_MANIFEST = "manifest.enc"
    const val ATT_PREFIX     = "att/"
    const val ATT_SUFFIX     = ".enc"

    const val MAX_ATT_ENTRY_BYTES  = 12L * 1024 * 1024
    const val MAX_META_ENTRY_BYTES = 64L * 1024 * 1024
    const val MAX_ATT_ENTRIES      = 10_000

    fun attEntryName(id: String): String = "$ATT_PREFIX$id$ATT_SUFFIX"
    fun attIdFromEntry(name: String): String? =
        if (name.startsWith(ATT_PREFIX) && name.endsWith(ATT_SUFFIX))
            name.substring(ATT_PREFIX.length, name.length - ATT_SUFFIX.length)
        else null

    private val ZIP_MAGIC = byteArrayOf(0x50, 0x4B, 0x03, 0x04)

    fun looksLikeKab(firstBytes: ByteArray): Boolean =
        firstBytes.size >= 4 &&
            firstBytes[0] == ZIP_MAGIC[0] && firstBytes[1] == ZIP_MAGIC[1] &&
            firstBytes[2] == ZIP_MAGIC[2] && firstBytes[3] == ZIP_MAGIC[3]
}

data class KabKdf(
    val memoryKb: Int,
    val iterations: Int,
    val parallelism: Int,
    val saltB64: String,
)

data class KabHeader(
    val mode: String,
    val attachmentCount: Int,
    val kdf: KabKdf?,
    val wrappedKey: String? = null,
)

class KabAttachmentSource(
    val id: String,
    val storedBytes: () -> ByteArray?,
)

class KabFormatException(message: String) : Exception(message)

class KabContainerWriter(private val crypto: CryptoManager) {

    fun write(
        out: OutputStream,
        header: KabHeader,
        key: SecretKeySpec,
        vaultJson: String,
        manifestJson: String,
        attachments: Sequence<KabAttachmentSource>,
    ) {
        ZipOutputStream(BufferedOutputStream(out)).use { zip ->
            zip.putNextEntry(ZipEntry(KabContainer.ENTRY_META))
            zip.write(buildMeta(header).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry(KabContainer.ENTRY_VAULT))
            zip.write(crypto.encrypt(vaultJson, key).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry(KabContainer.ENTRY_MANIFEST))
            zip.write(crypto.encrypt(manifestJson, key).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            attachments.forEach { src ->
                val bytes = src.storedBytes() ?: return@forEach
                zip.putNextEntry(ZipEntry(KabContainer.attEntryName(src.id)))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
    }

    private fun buildMeta(header: KabHeader): String =
        JSONObject().apply {
            put("format", KabContainer.FORMAT)
            put("version", KabContainer.VERSION)
            put("mode", header.mode)
            put("attachmentCount", header.attachmentCount)
            header.wrappedKey?.let { put("wrappedKey", it) }
            header.kdf?.let { k ->
                put("kdf", "argon2id")
                put("memoryKb", k.memoryKb)
                put("iterations", k.iterations)
                put("parallelism", k.parallelism)
                put("salt", k.saltB64)
            }
        }.toString()
}

class KabContainerReader(private val crypto: CryptoManager) {

    fun readHeader(input: InputStream): KabHeader =
        ZipInputStream(input).use { zip ->
            val first = zip.nextEntry ?: throw KabFormatException("kab_empty")
            if (first.name != KabContainer.ENTRY_META) throw KabFormatException("kab_meta_not_first")
            parseMeta(zip.readLimited(KabContainer.MAX_META_ENTRY_BYTES).toString(Charsets.UTF_8))
        }

    suspend fun readHeaderAndVault(
        input: InputStream,
        keyProvider: suspend (KabHeader) -> SecretKeySpec,
    ): Pair<KabHeader, String> =
        ZipInputStream(input).use { zip ->
            val first = zip.nextEntry ?: throw KabFormatException("kab_empty")
            if (first.name != KabContainer.ENTRY_META) throw KabFormatException("kab_meta_not_first")
            val header = parseMeta(zip.readLimited(KabContainer.MAX_META_ENTRY_BYTES).toString(Charsets.UTF_8))
            val key = keyProvider(header)
            val second = zip.nextEntry ?: throw KabFormatException("kab_no_vault")
            if (second.name != KabContainer.ENTRY_VAULT) throw KabFormatException("kab_vault_not_second")
            header to crypto.decrypt(zip.readLimited(KabContainer.MAX_META_ENTRY_BYTES).toString(Charsets.UTF_8), key)
        }

    suspend fun read(
        input: InputStream,
        keyProvider: suspend (KabHeader) -> SecretKeySpec,
        onVault: suspend (vaultJson: String) -> Unit,
        onManifest: suspend (manifestJson: String) -> Unit,
        onAttachment: suspend (id: String, storedBytes: ByteArray) -> Unit,
    ) {
        ZipInputStream(input).use { zip ->
            val firstEntry = zip.nextEntry ?: throw KabFormatException("kab_empty")
            if (firstEntry.name != KabContainer.ENTRY_META) throw KabFormatException("kab_meta_not_first")
            val header = parseMeta(zip.readLimited(KabContainer.MAX_META_ENTRY_BYTES).toString(Charsets.UTF_8))
            val key = keyProvider(header)

            var attCount = 0
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                when {
                    entry.name == KabContainer.ENTRY_VAULT ->
                        onVault(crypto.decrypt(zip.readLimited(KabContainer.MAX_META_ENTRY_BYTES).toString(Charsets.UTF_8), key))
                    entry.name == KabContainer.ENTRY_MANIFEST ->
                        onManifest(crypto.decrypt(zip.readLimited(KabContainer.MAX_META_ENTRY_BYTES).toString(Charsets.UTF_8), key))
                    KabContainer.attIdFromEntry(entry.name) != null -> {
                        if (++attCount > KabContainer.MAX_ATT_ENTRIES) throw KabFormatException("kab_too_many_attachments")
                        onAttachment(KabContainer.attIdFromEntry(entry.name)!!, zip.readLimited(KabContainer.MAX_ATT_ENTRY_BYTES))
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    private fun ZipInputStream.readLimited(max: Long): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(8192)
        var total = 0L
        while (true) {
            val n = read(buf)
            if (n < 0) break
            total += n
            if (total > max) throw KabFormatException("kab_entry_too_large")
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }

    private fun parseMeta(json: String): KabHeader {
        val o = JSONObject(json)
        if (o.optString("format") != KabContainer.FORMAT) throw KabFormatException("kab_bad_format")
        if (o.optInt("version", KabContainer.VERSION) > KabContainer.VERSION)
            throw KabFormatException("kab_unsupported_version")
        val mode = o.optString("mode")
        val kdf = if (mode == KabContainer.MODE_PASSWORD) {
            val memoryKb    = o.getInt("memoryKb")
            val iterations  = o.getInt("iterations")
            val parallelism = o.getInt("parallelism")
            if (memoryKb !in 1024..262_144 || iterations !in 1..20 || parallelism !in 1..8)
                throw KabFormatException("kab_bad_kdf_params")
            KabKdf(
                memoryKb    = memoryKb,
                iterations  = iterations,
                parallelism = parallelism,
                saltB64     = o.getString("salt"),
            )
        } else null
        val wrappedKey = if (mode == KabContainer.MODE_KEY) {
            o.optString("wrappedKey").takeIf { it.isNotEmpty() } ?: throw KabFormatException("kab_no_wrapped_key")
        } else null
        return KabHeader(mode = mode, attachmentCount = o.optInt("attachmentCount", 0), kdf = kdf, wrappedKey = wrappedKey)
    }
}
