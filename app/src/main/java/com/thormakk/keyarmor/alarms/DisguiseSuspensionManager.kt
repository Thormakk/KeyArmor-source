package com.thormakk.keyarmor.alarms

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import com.thormakk.keyarmor.data.repository.ReminderRepository
import com.thormakk.keyarmor.di.ApplicationScope
import com.thormakk.keyarmor.widget.KeyArmorWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisguiseSuspensionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepository: ReminderRepository,
    private val calculatorDisguisePrefs: CalculatorDisguisePrefs,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    fun onDisguiseChanged(active: Boolean) {
        runCatching { calculatorDisguisePrefs.setWidgetOffered(!active) }
            .onFailure { Log.w(TAG, "No se pudo ajustar el receptor del widget", it) }
        applicationScope.launch {
            runCatching {
                if (active) reminderRepository.cancelAllAlarms()
                else reminderRepository.rescheduleAllAlarms()
            }.onFailure { Log.w(TAG, "No se pudieron ajustar las alarmas al cambiar el disfraz", it) }
            runCatching { KeyArmorWidget().updateAll(context) }
                .onFailure { Log.w(TAG, "No se pudo actualizar el widget al cambiar el disfraz", it) }
        }
    }

    private companion object {
        const val TAG = "DisguiseSuspension"
    }
}
