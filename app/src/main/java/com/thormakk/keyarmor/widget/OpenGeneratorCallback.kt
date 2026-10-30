package com.thormakk.keyarmor.widget

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

@Keep
class OpenGeneratorCallback : ActionCallback {

    override suspend fun onAction(
        context:    Context,
        glanceId:   GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, PasswordGeneratorActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
