package com.thormakk.keyarmor.widget

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback

@Keep
class CopyPasswordCallback : ActionCallback {

    override suspend fun onAction(
        context:    Context,
        glanceId:   GlanceId,
        parameters: ActionParameters
    ) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        val intent = Intent(context, CopyPasswordActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            .putExtra(CopyPasswordActivity.EXTRA_APPWIDGET_ID, appWidgetId)
        context.startActivity(intent)
    }
}
