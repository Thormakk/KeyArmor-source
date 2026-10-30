package com.thormakk.keyarmor.data.backup

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import com.thormakk.keyarmor.crypto.PalabrasDeLaClave
import com.thormakk.keyarmor.security.CryptoManager
import com.thormakk.keyarmor.security.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

data class ClaveRetirada(val id: String, val desde: Long, val hasta: Long)

@Singleton
class ClaveDeRespaldo @Inject constructor(
    @ApplicationContext context: Context,
    private val session: SessionManager,
    private val crypto: CryptoManager,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val candado = Any()

    fun hay(): Boolean = prefs.contains(KEY_ENCRYPTED)

    fun obtenerOCrear(): ByteArray? = synchronized(candado) {
        val envoltorio = envoltorioActual() ?: return null
        leer(envoltorio)?.let { return it }
        val nueva = ByteArray(PalabrasDeLaClave.BYTES).also { azar.nextBytes(it) }
        escribir(nueva, envoltorio)
        nueva
    }

    fun adoptar(clave: ByteArray): Boolean = synchronized(candado) {
        require(clave.size == PalabrasDeLaClave.BYTES) {
            "la clave del respaldo tiene ${PalabrasDeLaClave.BYTES} bytes"
        }
        val envoltorio = envoltorioActual() ?: return false
        escribir(clave, envoltorio)
        prefs.edit { putLong(KEY_SINCE, System.currentTimeMillis()) }
        true
    }

    fun prepararRotacion(): ByteArray? = synchronized(candado) {
        val envoltorio = envoltorioActual() ?: return null
        abrir(prefs.getString(KEY_PENDING, null), envoltorio)?.let { return it }
        val nueva = ByteArray(PalabrasDeLaClave.BYTES).also { azar.nextBytes(it) }
        prefs.edit { putString(KEY_PENDING, sellar(nueva, envoltorio)) }
        nueva
    }

    fun prepararRotacionDesdeRetirada(id: String): ByteArray? = synchronized(candado) {
        val envoltorio = envoltorioActual() ?: return null
        val bytes = claveRetirada(id) ?: return null
        prefs.edit { putString(KEY_PENDING, sellar(bytes, envoltorio)) }
        bytes
    }

    fun claveNuevaPendiente(): ByteArray? = synchronized(candado) {
        val envoltorio = envoltorioActual() ?: return null
        abrir(prefs.getString(KEY_PENDING, null), envoltorio)
    }

    fun descartarRotacion() {
        synchronized(candado) { prefs.edit { remove(KEY_PENDING) } }
    }

    fun confirmarRotacion(guardarAnterior: Boolean): Boolean = synchronized(candado) {
        val envoltorio = envoltorioActual() ?: return false
        val nueva = abrir(prefs.getString(KEY_PENDING, null), envoltorio) ?: return false
        val vieja = leer(envoltorio)
        try {
            val historial = JSONArray().also { rehecho ->
                val viejo = leerHistorial()
                for (i in 0 until viejo.length()) {
                    val o = viejo.getJSONObject(i)
                    val b = abrir(o.getString("enc"), envoltorio)
                    val igual = b != null && b.contentEquals(nueva)
                    b?.fill(0)
                    if (!igual) rehecho.put(o)
                }
            }
            if (guardarAnterior && vieja != null) {
                historial.put(JSONObject().apply {
                    put("id", java.util.UUID.randomUUID().toString())
                    put("enc", sellar(vieja, envoltorio))
                    put("desde", prefs.getLong(KEY_SINCE, 0L))
                    put("hasta", System.currentTimeMillis())
                })
            }
            val ahora = System.currentTimeMillis()
            prefs.edit {
                putString(KEY_ENCRYPTED, sellar(nueva, envoltorio))
                putString(KEY_HISTORY, historial.toString())
                putLong(KEY_SINCE, ahora)
                putBoolean(KEY_UPLOAD_PENDING, true)
                remove(KEY_PENDING)
            }
            true
        } finally {
            nueva.fill(0); vieja?.fill(0)
        }
    }

    fun respaldoConClaveNuevaPendiente(): Boolean = prefs.getBoolean(KEY_UPLOAD_PENDING, false)

    fun resolverRespaldoPendiente() {
        if (prefs.getBoolean(KEY_UPLOAD_PENDING, false)) prefs.edit { putBoolean(KEY_UPLOAD_PENDING, false) }
    }

    fun historial(): List<ClaveRetirada> = synchronized(candado) {
        val arr = leerHistorial()
        (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            ClaveRetirada(o.getString("id"), o.optLong("desde", 0L), o.optLong("hasta", 0L))
        }.sortedByDescending { it.hasta }
    }

    fun claveRetirada(id: String): ByteArray? = synchronized(candado) {
        val envoltorio = envoltorioActual() ?: return null
        val arr = leerHistorial()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") == id) return abrir(o.getString("enc"), envoltorio)
        }
        null
    }

    fun borrarRetirada(id: String) {
        synchronized(candado) {
            val arr = leerHistorial()
            val nuevo = JSONArray()
            for (i in 0 until arr.length()) if (arr.getJSONObject(i).getString("id") != id) nuevo.put(arr.getJSONObject(i))
            prefs.edit { putString(KEY_HISTORY, nuevo.toString()) }
        }
    }

    fun vigenteDesde(): Long = prefs.getLong(KEY_SINCE, 0L)

    private fun leerHistorial(): JSONArray =
        try { JSONArray(prefs.getString(KEY_HISTORY, "[]")) } catch (_: Exception) { JSONArray() }

    fun rewrap(viejo: SecretKey, nuevo: SecretKey) {
        synchronized(candado) {
            abrir(prefs.getString(KEY_PENDING, null), viejo)?.let { p ->
                try { prefs.edit { putString(KEY_PENDING, sellar(p, nuevo)) } } finally { p.fill(0) }
            }
            val arr = leerHistorial()
            if (arr.length() > 0) {
                val rehecho = JSONArray()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val b = abrir(o.getString("enc"), viejo) ?: continue
                    try { o.put("enc", sellar(b, nuevo)); rehecho.put(o) } finally { b.fill(0) }
                }
                prefs.edit { putString(KEY_HISTORY, rehecho.toString()) }
            }
            val clave = leer(viejo) ?: return
            try { escribir(clave, nuevo) } finally { clave.fill(0) }
        }
    }

    fun clear() {
        synchronized(candado) { prefs.edit { remove(KEY_ENCRYPTED); remove(KEY_CONFIRMED); remove(KEY_PENDING); remove(KEY_HISTORY); remove(KEY_SINCE); remove(KEY_UPLOAD_PENDING) } }
    }

    fun confirmada(): Boolean = prefs.getBoolean(KEY_CONFIRMED, false)

    fun marcarConfirmada() {
        prefs.edit {
            putBoolean(KEY_CONFIRMED, true)
            if (!prefs.contains(KEY_SINCE)) putLong(KEY_SINCE, System.currentTimeMillis())
        }
    }

    fun claveParaRespaldar(): ByteArray? {
        if (!confirmada()) return null
        return obtenerOCrear() ?: throw IllegalStateException("backup_key_locked")
    }

    fun claveParaRestaurar(): ByteArray? = synchronized(candado) {
        val envoltorio = envoltorioActual() ?: return null
        leer(envoltorio)
    }

    inline fun <T> paraRespaldo(bloque: (ByteArray?) -> T): T {
        val c = claveParaRespaldar()
        try { return bloque(c) } finally { c?.fill(0) }
    }

    inline fun <T> paraRestaurar(bloque: (ByteArray?) -> T): T {
        val c = claveParaRestaurar()
        try { return bloque(c) } finally { c?.fill(0) }
    }

    private fun envoltorioActual(): SecretKey? =
        session.getSecretKey()?.let { crypto.deriveBackupKeyWrapKey(it) }

    private fun leer(envoltorio: SecretKey): ByteArray? = abrir(prefs.getString(KEY_ENCRYPTED, null), envoltorio)

    private fun escribir(clave: ByteArray, envoltorio: SecretKey) {
        prefs.edit { putString(KEY_ENCRYPTED, sellar(clave, envoltorio)) }
    }

    private fun abrir(guardado: String?, envoltorio: SecretKey): ByteArray? {
        if (guardado == null) return null
        return try {
            val combinado = Base64.decode(guardado, Base64.NO_WRAP)
            val iv        = combinado.copyOfRange(0, IV_SIZE)
            val cifrado   = combinado.copyOfRange(IV_SIZE, combinado.size)
            val cipher    = Cipher.getInstance(AES_GCM).apply {
                init(Cipher.DECRYPT_MODE, envoltorio, GCMParameterSpec(GCM_TAG_BITS, iv))
                updateAAD(CONTEXTO)
            }
            cipher.doFinal(cifrado).takeIf { it.size == PalabrasDeLaClave.BYTES }
        } catch (_: Exception) {
            null
        }
    }

    private fun sellar(clave: ByteArray, envoltorio: SecretKey): String {
        val cipher = Cipher.getInstance(AES_GCM).apply {
            init(Cipher.ENCRYPT_MODE, envoltorio)
            updateAAD(CONTEXTO)
        }
        val cifrado = cipher.doFinal(clave)
        return Base64.encodeToString(cipher.iv + cifrado, Base64.NO_WRAP)
    }

    private companion object {
        const val PREFS_NAME    = "backup_key_prefs"
        const val KEY_ENCRYPTED = "backup_key_enc_v1"
        const val KEY_CONFIRMED = "backup_key_confirmed_v1"
        const val KEY_PENDING   = "backup_key_pending_v1"
        const val KEY_HISTORY   = "backup_key_history_v1"
        const val KEY_SINCE     = "backup_key_since_v1"
        const val KEY_UPLOAD_PENDING = "backup_key_upload_pending_v1"
        const val AES_GCM       = "AES/GCM/NoPadding"
        const val IV_SIZE       = 12
        const val GCM_TAG_BITS  = 128

        val CONTEXTO = "backup:key".toByteArray(Charsets.UTF_8)

        val azar = SecureRandom()
    }
}
