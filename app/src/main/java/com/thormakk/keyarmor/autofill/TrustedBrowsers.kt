package com.thormakk.keyarmor.autofill

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

object TrustedBrowsers {

    private val TRUSTED: Map<String, Set<String>> = mapOf(
        "com.android.chrome" to setOf(
            "F0:FD:6C:5B:41:0F:25:CB:25:C3:B5:33:46:C8:97:2F:AE:30:F8:EE:74:11:DF:91:04:80:AD:6B:2D:60:DB:83"
        ),
        "com.chrome.beta" to setOf(
            "DA:63:3D:34:B6:9E:63:AE:21:03:B4:9D:53:CE:05:2F:C5:F7:F3:C5:3A:AB:94:FD:C2:A2:08:BD:FD:14:24:9C",
            "3D:7A:12:23:01:9A:A3:9D:9E:A0:E3:43:6A:B7:C0:89:6B:FB:4F:B6:79:F4:DE:5F:E7:C2:3F:32:6C:8F:99:4A"
        ),
        "com.chrome.dev" to setOf(
            "90:44:EE:5F:EE:4B:BC:5E:21:DD:44:66:54:31:C4:EB:1F:1F:71:A3:27:16:A0:BC:92:7B:CB:B3:92:33:CA:BF",
            "3D:7A:12:23:01:9A:A3:9D:9E:A0:E3:43:6A:B7:C0:89:6B:FB:4F:B6:79:F4:DE:5F:E7:C2:3F:32:6C:8F:99:4A"
        ),
        "com.chrome.canary" to setOf(
            "20:19:DF:A1:FB:23:EF:BF:70:C5:BC:D1:44:3C:5B:EA:B0:4F:3F:2F:F4:36:6E:9A:C1:E3:45:76:39:A2:4C:FC"
        ),
        "org.chromium.chrome" to setOf(
            "C6:AD:B8:B8:3C:6D:4C:17:D2:92:AF:DE:56:FD:48:8A:51:D3:16:FF:8F:2C:11:C5:41:02:23:BF:F8:A7:DB:B3"
        ),
        "org.mozilla.firefox" to setOf(
            "A7:8B:62:A5:16:5B:44:94:B2:FE:AD:9E:76:A2:80:D2:2D:93:7F:EE:62:51:AE:CE:59:94:46:B2:EA:31:9B:04"
        ),
        "org.mozilla.firefox_beta" to setOf(
            "A7:8B:62:A5:16:5B:44:94:B2:FE:AD:9E:76:A2:80:D2:2D:93:7F:EE:62:51:AE:CE:59:94:46:B2:EA:31:9B:04"
        ),
        "org.mozilla.fenix" to setOf(
            "50:04:77:90:88:E7:F9:88:D5:BC:5C:C5:F8:79:8F:EB:F4:F8:CD:08:4A:1B:2A:46:EF:D4:C8:EE:4A:EA:F2:11"
        ),
        "org.mozilla.focus" to setOf(
            "62:03:A4:73:BE:36:D6:4E:E3:7F:87:FA:50:0E:DB:C7:9E:AB:93:06:10:AB:9B:9F:A4:CA:7D:5C:1F:1B:4F:FC"
        ),
        "com.microsoft.emmx" to setOf(
            "01:E1:99:97:10:A8:2C:27:49:B4:D5:0C:44:5D:C8:5D:67:0B:61:36:08:9D:0A:76:6A:73:82:7C:82:A1:EA:C9"
        ),
        "com.microsoft.emmx.beta" to setOf(
            "01:E1:99:97:10:A8:2C:27:49:B4:D5:0C:44:5D:C8:5D:67:0B:61:36:08:9D:0A:76:6A:73:82:7C:82:A1:EA:C9"
        ),
        "com.microsoft.emmx.dev" to setOf(
            "01:E1:99:97:10:A8:2C:27:49:B4:D5:0C:44:5D:C8:5D:67:0B:61:36:08:9D:0A:76:6A:73:82:7C:82:A1:EA:C9"
        ),
        "com.microsoft.emmx.canary" to setOf(
            "01:E1:99:97:10:A8:2C:27:49:B4:D5:0C:44:5D:C8:5D:67:0B:61:36:08:9D:0A:76:6A:73:82:7C:82:A1:EA:C9"
        ),
        "com.sec.android.app.sbrowser" to setOf(
            "C8:A2:E9:BC:CF:59:7C:2F:B6:DC:66:BE:E2:93:FC:13:F2:FC:47:EC:77:BC:6B:2B:0D:52:C1:1F:51:19:2A:B8",
            "34:DF:0E:7A:9F:1C:F1:89:2E:45:C0:56:B4:97:3C:D8:1C:CF:14:8A:40:50:D1:1A:EA:4A:C5:A6:5F:90:0A:42"
        ),
        "com.sec.android.app.sbrowser.beta" to setOf(
            "C8:A2:E9:BC:CF:59:7C:2F:B6:DC:66:BE:E2:93:FC:13:F2:FC:47:EC:77:BC:6B:2B:0D:52:C1:1F:51:19:2A:B8",
            "34:DF:0E:7A:9F:1C:F1:89:2E:45:C0:56:B4:97:3C:D8:1C:CF:14:8A:40:50:D1:1A:EA:4A:C5:A6:5F:90:0A:42"
        ),
        "com.opera.browser" to setOf(
            "5D:6A:FB:F8:7F:65:2A:F0:46:47:AD:A0:DF:63:4C:F2:23:70:90:0B:16:4B:09:D5:0B:D2:3A:A2:CB:52:85:B8"
        ),
        "com.opera.browser.beta" to setOf(
            "5D:6A:FB:F8:7F:65:2A:F0:46:47:AD:A0:DF:63:4C:F2:23:70:90:0B:16:4B:09:D5:0B:D2:3A:A2:CB:52:85:B8"
        ),
        "com.opera.mini.native" to setOf(
            "57:AC:BC:52:5F:1B:2E:BD:19:19:6C:D6:F0:14:39:7C:C9:10:FD:18:84:1E:0A:E8:50:FE:BC:3E:1E:59:3F:F2"
        ),
        "com.brave.browser" to setOf(
            "9C:2D:B7:05:13:51:5F:DB:FB:BC:58:5B:3E:DF:3D:71:23:D4:DC:67:C9:4F:FD:30:63:61:C1:D7:9B:BF:18:AC"
        ),
        "com.brave.browser_beta" to setOf(
            "9C:2D:B7:05:13:51:5F:DB:FB:BC:58:5B:3E:DF:3D:71:23:D4:DC:67:C9:4F:FD:30:63:61:C1:D7:9B:BF:18:AC"
        ),
        "com.brave.browser_nightly" to setOf(
            "9C:2D:B7:05:13:51:5F:DB:FB:BC:58:5B:3E:DF:3D:71:23:D4:DC:67:C9:4F:FD:30:63:61:C1:D7:9B:BF:18:AC"
        ),
        "com.duckduckgo.mobile.android" to setOf(
            "BB:7B:B3:1C:57:3C:46:A1:DA:7F:C5:C5:28:A6:AC:F4:32:10:84:56:FE:EC:50:81:0C:7F:33:69:4E:B3:D2:D4"
        ),
        "com.vivaldi.browser" to setOf(
            "E8:A7:85:44:65:5B:A8:C0:98:17:F7:32:76:8F:56:89:B1:66:2E:C4:B2:BC:5A:0B:C0:EC:13:8D:33:CA:3D:1E"
        ),
        "com.vivaldi.browser.snapshot" to setOf(
            "E8:A7:85:44:65:5B:A8:C0:98:17:F7:32:76:8F:56:89:B1:66:2E:C4:B2:BC:5A:0B:C0:EC:13:8D:33:CA:3D:1E"
        ),
        "com.yandex.browser" to setOf(
            "AC:A4:05:DE:D8:B2:5C:B2:E8:C6:DA:69:42:5D:2B:43:07:D0:87:C1:27:6F:C0:6A:D5:94:27:31:CC:C5:1D:BA"
        ),
        "com.android.browser" to setOf(
            "C9:00:9D:01:EB:F9:F5:D0:30:2B:C7:1B:2F:E9:AA:9A:47:A4:32:BB:A1:73:08:A3:11:1B:75:D7:B2:14:90:25"
        ),
    )

    fun isTrusted(packageName: String, context: Context): Boolean {
        val knownHashes = TRUSTED[packageName] ?: return false
        val actualHashes = signingSha256(packageName, context.packageManager)
        if (actualHashes.isEmpty()) return false
        val knownNorm = knownHashes.map { it.replace(":", "").uppercase() }.toSet()
        return actualHashes.any { it in knownNorm }
    }

    private fun signingSha256(packageName: String, pm: PackageManager): List<String> = try {
        val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val si = info.signingInfo
            (si?.apkContentsSigners) ?: emptyArray()
        } else {
            @Suppress("DEPRECATION")
            (pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures ?: emptyArray())
        }
        signatures.map { sha256Hex(it.toByteArray()) }
    } catch (_: Exception) {
        emptyList()
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02X".format(it.toInt() and 0xFF) }

    val privilegedAllowlistJson: String by lazy {
        val apps = JSONArray()
        for ((pkg, hashes) in TRUSTED) {
            val signatures = JSONArray()
            for (h in hashes) {
                signatures.put(
                    JSONObject()
                        .put("build", "release")
                        .put("cert_fingerprint_sha256", h)
                )
            }
            apps.put(
                JSONObject()
                    .put("type", "android")
                    .put(
                        "info",
                        JSONObject()
                            .put("package_name", pkg)
                            .put("signatures", signatures)
                    )
            )
        }
        JSONObject().put("apps", apps).toString()
    }
}
