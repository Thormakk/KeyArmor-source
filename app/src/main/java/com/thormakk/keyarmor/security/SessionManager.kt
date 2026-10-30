package com.thormakk.keyarmor.security

import com.thormakk.keyarmor.data.remote.HibpPasswordChecker
import com.thormakk.keyarmor.data.storage.AttachmentStorage
import com.thormakk.keyarmor.di.ApplicationScope
import com.thormakk.keyarmor.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val attachmentStorage: AttachmentStorage,
    private val hibpPasswordChecker: HibpPasswordChecker,
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    private val _sessionKeyBytes = MutableStateFlow<ByteArray?>(null)

    private val _isUnlockedFlow = MutableStateFlow(false)

    val isUnlockedFlow: StateFlow<Boolean> = _isUnlockedFlow

    fun setSecretKey(key: SecretKeySpec) {
        _sessionKeyBytes.value?.fill(0)
        _sessionKeyBytes.value = key.encoded
        _isUnlockedFlow.value = true
    }

    fun getSecretKey(): SecretKeySpec? = _sessionKeyBytes.value?.let { SecretKeySpec(it, "AES") }

    fun getSecretKeyBytes(): ByteArray? = _sessionKeyBytes.value?.copyOf()

    fun isUnlocked(): Boolean = _sessionKeyBytes.value != null

    fun lock() {
        _sessionKeyBytes.value?.fill(0)
        _sessionKeyBytes.value = null
        _isUnlockedFlow.value = false

        hibpPasswordChecker.clearCache()

        BusquedaEnCurso.limpiar()

        com.thormakk.keyarmor.presentation.util.BiometricSessionCache.invalidateAll()

        appScope.launch(io) { runCatching { attachmentStorage.clearTempFiles() } }
    }
}
