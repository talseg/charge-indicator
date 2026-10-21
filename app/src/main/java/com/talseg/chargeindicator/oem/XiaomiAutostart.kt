package com.talseg.chargeindicator.oem

import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log

/**
 * Xiaomi's "Background autostart" switch (MIUI / HyperOS). Without it, MIUI blocks
 * BOOT_COMPLETED, so the overlay wouldn't come back after a reboot.
 *
 * Android has no public API for this switch; both functions use well-known workarounds
 * and fail safe: an unreadable state is [State.UNKNOWN], and if MIUI's screen can't be
 * opened, the app's own info page is opened instead.
 */
object XiaomiAutostart {

    enum class State { ENABLED, DISABLED, UNKNOWN }

    /** MIUI's private app-op number for "Autostart". */
    private const val OP_AUTO_START = 10008
    private const val TAG = "XiaomiAutostart"

    fun isXiaomiDevice(): Boolean =
        Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) // Also Redmi and POCO phones.

    fun getState(context: Context): State {
        if (!isXiaomiDevice()) return State.UNKNOWN
        return try {
            // Hidden method AppOpsManager.checkOpNoThrow(int op, int uid, String packageName).
            val appOps = context.getSystemService(AppOpsManager::class.java)
            val checkOp = AppOpsManager::class.java.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            when (checkOp.invoke(appOps, OP_AUTO_START, Process.myUid(), context.packageName)) {
                AppOpsManager.MODE_ALLOWED -> State.ENABLED
                AppOpsManager.MODE_IGNORED -> State.DISABLED
                else -> State.UNKNOWN
            }
        } catch (e: Exception) {
            Log.w(TAG, "Can't read the autostart state", e)
            State.UNKNOWN
        }
    }

    /** Opens MIUI's "Background autostart" list, or the app's info page as a fallback. */
    fun openSettings(context: Context) {
        val autostartScreen = Intent().setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        )
        val appInfoScreen = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        try {
            context.startActivity(autostartScreen)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Autostart screen not found; opening app info", e)
            context.startActivity(appInfoScreen)
        } catch (e: SecurityException) {
            Log.w(TAG, "Autostart screen not accessible; opening app info", e)
            context.startActivity(appInfoScreen)
        }
    }
}
