package com.thormakk.keyarmor.data.repository

import android.util.Log
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.di.ApplicationScope
import com.thormakk.keyarmor.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

enum class DisarmReason {
    PLAN_DOWNGRADE,
    DRIVE_DISCONNECT,
    ACCOUNT_CHANGE,
    PANIC_PIN,
}

@Singleton
class InheritanceDisarmer @Inject constructor(
    private val inheritancePrefs: InheritancePrefs,
    private val drivePrefs: DrivePrefs,
    private val inheritanceRepository: InheritanceRepository,
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    private val mutex = Mutex()

    fun requestDisarmOnDowngrade() = requestDisarm(DisarmReason.PLAN_DOWNGRADE)

    fun requestDisarm(reason: DisarmReason, ownerEmail: String? = null) {
        if (!inheritancePrefs.isEnabled() && !inheritancePrefs.isPendingDisarm()) return
        inheritancePrefs.setDisarmRequest(
            ownerEmail = ownerEmail ?: drivePrefs.getConnectedEmail(),
            reason     = reason.name
        )
        inheritancePrefs.setPendingDisarm(true)
        appScope.launch { runPendingDisarm() }
    }

    suspend fun runPendingDisarm(): Boolean = mutex.withLock {
        if (!inheritancePrefs.isPendingDisarm()) return@withLock true

        val ownerEmail = inheritancePrefs.getDisarmOwnerEmail() ?: drivePrefs.getConnectedEmail()
        val reason = inheritancePrefs.getDisarmReason() ?: DisarmReason.PLAN_DOWNGRADE.name
        if (ownerEmail.isNullOrBlank()) {
            Log.w(TAG, "Desarme pendiente sin identificador: se apaga solo el estado local.")
            inheritancePrefs.setEnabled(false)
            inheritancePrefs.setPendingDisarm(false)
            inheritancePrefs.clearDisarmRequest()
            if (reason == DisarmReason.PLAN_DOWNGRADE.name) inheritancePrefs.setDisarmedByDowngrade(true)
            return@withLock true
        }

        return@withLock try {
            val resultado = withContext(io) {
                inheritanceRepository.disableOwner(ownerEmail)
            }
            if (resultado == DisableOwnerResult.ALREADY_INHERITED) {
                inheritancePrefs.setEnabled(false)
                inheritancePrefs.setPendingDisarm(false)
                inheritancePrefs.clearDisarmRequest()
                inheritancePrefs.setDisarmedByAlreadyInherited(true)
                Log.e(TAG, "Desarme (motivo: $reason): la herencia ya fue entregada -- no hay nada que reintentar.")
                return@withLock true
            }
            inheritancePrefs.setEnabled(false)
            inheritancePrefs.setPendingDisarm(false)
            inheritancePrefs.clearDisarmRequest()
            if (reason == DisarmReason.PLAN_DOWNGRADE.name) {
                inheritancePrefs.setDisarmedByDowngrade(true)
                drivePrefs.clear()
            }
            Log.i(TAG, "Herencia desarmada (motivo: $reason).")
            true
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo desarmar la herencia; queda pendiente y se reintenta.", e)
            false
        }
    }

    private companion object {
        const val TAG = "InheritanceDisarmer"
    }

}
