package com.thormakk.keyarmor.widget

import android.content.Context
import androidx.annotation.Keep
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import java.security.SecureRandom

@Keep
class RegeneratePasswordCallback : ActionCallback {

    override suspend fun onAction(
        context:    Context,
        glanceId:   GlanceId,
        parameters: ActionParameters
    ) {
        val password = generatePassword()
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[KeyArmorWidget.WIDGET_PASSWORD_KEY] = password
        }
        KeyArmorWidget().update(context, glanceId)
    }

    companion object {

        private val random = SecureRandom()

        private const val UPPER_POOL  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val LOWER_POOL  = "abcdefghijklmnopqrstuvwxyz"
        private const val DIGIT_POOL  = "0123456789"
        private const val SYMBOL_POOL = "!@#\$%^&*-_=+?"

        fun generatePassword(length: Int = 16): String {
            val result = mutableListOf<Char>()
            result.add(UPPER_POOL[random.nextInt(UPPER_POOL.length)])
            result.add(DIGIT_POOL[random.nextInt(DIGIT_POOL.length)])
            result.add(SYMBOL_POOL[random.nextInt(SYMBOL_POOL.length)])
            repeat(length - 3) {
                result.add(LOWER_POOL[random.nextInt(LOWER_POOL.length)])
            }
            for (i in result.indices.reversed()) {
                val j = random.nextInt(i + 1)
                val tmp = result[i]; result[i] = result[j]; result[j] = tmp
            }
            return result.joinToString("")
        }
    }
}
