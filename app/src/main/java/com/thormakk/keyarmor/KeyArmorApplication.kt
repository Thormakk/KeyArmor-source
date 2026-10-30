package com.thormakk.keyarmor

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.thormakk.keyarmor.data.database.dao.AppMetadataDao
import com.thormakk.keyarmor.data.prefs.CalculatorDisguisePrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.PremiumDowngradeGuard
import com.thormakk.keyarmor.data.storage.AttachmentStorage
import com.thormakk.keyarmor.appcheck.installAppCheck
import com.thormakk.keyarmor.data.time.TrustedTimeProvider
import com.thormakk.keyarmor.notifications.NotificationHelper
import com.thormakk.keyarmor.workers.SnapshotWorker
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class KeyArmorApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory:      HiltWorkerFactory
    @Inject lateinit var attachmentStorage:  AttachmentStorage
    @Inject lateinit var trustedTimeProvider: TrustedTimeProvider
    @Inject lateinit var appMetadataDao:     AppMetadataDao
    @Inject lateinit var calculatorDisguisePrefs: CalculatorDisguisePrefs
    @Inject lateinit var planPrefs:          PlanPrefs
    @Inject lateinit var premiumDowngradeGuard: PremiumDowngradeGuard
    @Inject lateinit var inheritanceDisarmer: com.thormakk.keyarmor.data.repository.InheritanceDisarmer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        com.thormakk.keyarmor.security.OrdenEnSesion.iniciar(this)

        installAppCheck()

        planPrefs.onDowngradeToFree = {
            premiumDowngradeGuard.deactivateAll()
            premiumDowngradeGuard.resetThemeToFreeDefault()
        }

        applicationScope.launch(Dispatchers.IO) { appMetadataDao.get("__warmup__") }

        applicationScope.launch(Dispatchers.Default) {
            NotificationHelper.createAllChannels(this@KeyArmorApplication)

            calculatorDisguisePrefs.applyLauncherIcon()

            PDFBoxResourceLoader.init(applicationContext)

            SnapshotWorker.schedule(this@KeyArmorApplication)
        }

        applicationScope.launch(Dispatchers.IO) { attachmentStorage.clearTempFiles() }

        applicationScope.launch(Dispatchers.IO) { inheritanceDisarmer.runPendingDisarm() }

        trustedTimeProvider.warmUp()

    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
