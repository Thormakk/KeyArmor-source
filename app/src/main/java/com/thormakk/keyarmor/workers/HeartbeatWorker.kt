package com.thormakk.keyarmor.workers

import android.content.Context
import com.thormakk.keyarmor.presentation.util.getFirebaseIdentityTokenOrNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.thormakk.keyarmor.R
import com.thormakk.keyarmor.data.prefs.DrivePrefs
import com.thormakk.keyarmor.data.prefs.InheritancePrefs
import com.thormakk.keyarmor.data.repository.HeirVaultEncryptionRepository
import com.thormakk.keyarmor.security.InheritanceKeyManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.thormakk.keyarmor.appcheck.AppCheckHeader

@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val drivePrefs:         DrivePrefs,
    private val inheritancePrefs:   InheritancePrefs,
    private val heirVaultRepo:      HeirVaultEncryptionRepository,
    private val inheritanceKeyMgr:  InheritanceKeyManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!inheritancePrefs.isEnabled()) return Result.success()

        val lastSentAt = inheritancePrefs.getLastHeartbeatSentAt()
        val minIntervalMs = 29L * 24 * 60 * 60 * 1000
        if (lastSentAt > 0L && System.currentTimeMillis() - lastSentAt < minIntervalMs) {
            return Result.success()
        }

        val email = drivePrefs.getConnectedEmail() ?: return Result.success()

        val deviceToken = inheritancePrefs.ensureDeviceToken()

        val ownerIdToken: String? = getFirebaseIdentityTokenOrNull(context)?.idToken

        val inactivityDays = inheritancePrefs.getInactivityDays()
        val heirEmails     = inheritancePrefs.getHeirEmails()
        val publicKey      = runCatching { inheritanceKeyMgr.getOrCreatePublicKeyBase64() }.getOrNull()
        val locale         = AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language
        val payload = buildJsonPayload(
            ownerEmail     = email,
            deviceToken    = deviceToken,
            inactivityDays = inactivityDays,
            heirEmails     = heirEmails,
            publicKey      = publicKey,
            ownerIdToken   = ownerIdToken,
            locale         = locale
        )

        return try {
            val code = postJson(payload)
            when {
                code in 200..299 -> {
                    runCatching { heirVaultRepo.reEncryptAll() }
                    inheritancePrefs.setLastHeartbeatSentAt(System.currentTimeMillis())
                    cancelBackup(context)
                    Result.success()
                }
                code == 401 || code == 403 -> {
                    Result.failure()
                }
                else -> {
                    Result.retry()
                }
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private suspend fun postJson(body: String): Int =
        withContext(Dispatchers.IO) {
            val conn = URL(HEARTBEAT_URL).openConnection() as HttpURLConnection
            conn.requestMethod  = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            AppCheckHeader.attach(conn)
            conn.connectTimeout = 15_000
            conn.readTimeout    = 15_000
            conn.doOutput       = true
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
            conn.responseCode
        }

    private fun buildJsonPayload(
        ownerEmail:     String,
        deviceToken:    String,
        inactivityDays: Int,
        heirEmails:     List<String> = emptyList(),
        publicKey:      String?      = null,
        ownerIdToken:   String?      = null,
        locale:         String       = AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language
    ): String {
        fun String.esc() = replace("\\", "\\\\").replace("\"", "\\\"")
        val heirsJson    = heirEmails.joinToString(",") { "\"${it.esc()}\"" }
        val pkField      = if (publicKey   != null) ""","publicKey":"${publicKey.esc()}"""" else ""
        val idTokenField = if (ownerIdToken != null) ""","ownerIdToken":"${ownerIdToken.esc()}"""" else ""
        return """{"ownerEmail":"${ownerEmail.esc()}","deviceToken":"${deviceToken.esc()}","inactivityDays":$inactivityDays,"heirEmails":[$heirsJson],"locale":"${locale.esc()}","updateHeartbeat":true$pkField$idTokenField}"""
    }

    companion object {
        const val WORK_NAME = "inheritance_heartbeat"

        private const val PROD_URL     = "<URL DEL SERVIDOR DE GOOGLE>"
        private const val HEARTBEAT_URL = PROD_URL

        private const val INTERVAL_DAYS = 30L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(
                    INTERVAL_DAYS, TimeUnit.DAYS
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(WORK_NAME)
        }

        fun cancelBackup(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME)
        }

        fun scheduleBackup(context: Context) {
            val request = OneTimeWorkRequestBuilder<HeartbeatWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(BACKUP_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }

        private const val BACKUP_WORK_NAME = "inheritance_heartbeat_backup"
    }
}
