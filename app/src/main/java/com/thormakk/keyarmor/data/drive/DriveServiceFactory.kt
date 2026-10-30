package com.thormakk.keyarmor.data.drive

import android.accounts.Account
import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

private const val APP_NAME = "KeyArmor"

internal fun buildDriveService(context: Context, account: Account): Drive {
    val credential = GoogleAccountCredential
        .usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
        .also { it.selectedAccount = account }
    return Drive.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
        credential
    ).setApplicationName(APP_NAME).build()
}
