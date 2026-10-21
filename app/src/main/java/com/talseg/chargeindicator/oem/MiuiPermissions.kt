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
 * Xiaomi-specific permissions (MIUI / HyperOS) that stock Android doesn't have:
 * "Background autostart" and "Open new windows while running in the background".
 *
 * Android has no public API for them; reading uses MIUI's private app-ops and opening
 * uses MIUI's own screens. Everything fails safe: an unreadable state is [State.UNKNOWN],
 * and if a MIUI screen can't be opened, the app's info page is opened instead.
 */
object MiuiPermissions {

    enum class State { ENABLED, DISABLED, UNKNOWN }

    // MIUI's private app-op numbers.
    private const val OP_AUTO_START = 10008
    private const val OP_BACKGROUND_START_ACTIVITY = 10021
    private const val TAG = "MiuiPermissions"

    fun isXiaomiDevice(): Boolean =
        Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) // Also Redmi and POCO phones.

    /** "Background autostart": without it, MIUI blocks BOOT_COMPLETED. */
    fun autostartState(context: Context): State = opState(context, OP_AUTO_START)

    /** "Open new windows while running in the background". */
    fun backgroundWindowsState(context: Context): State =
        opState(context, OP_BACKGROUND_START_ACTIVITY)

    /** Opens MIUI's "Background autostart" list. */
    fun openAutostartSettings(context: Context) {
        open(
            context,
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            )
        )
    }

    /** Opens the app's "Other permissions" page (pop-up windows, background windows, ...). */
    fun openOtherPermissions(context: Context) {
        open(
            context,
            Intent("miui.intent.action.APP_PERM_EDITOR")
                .setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                .putExtra("extra_pkgname", context.packageName)
        )
    }

    private fun opState(context: Context, op: Int): State {
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
            when (checkOp.invoke(appOps, op, Process.myUid(), context.packageName)) {
                AppOpsManager.MODE_ALLOWED -> State.ENABLED
                AppOpsManager.MODE_IGNORED -> State.DISABLED
                else -> State.UNKNOWN
            }
        } catch (e: Exception) {
            Log.w(TAG, "Can't read MIUI app-op $op", e)
            State.UNKNOWN
        }
    }

    private fun open(context: Context, miuiScreen: Intent) {
        try {
            context.startActivity(miuiScreen)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "MIUI screen not found; opening app info", e)
            context.startActivity(appInfoIntent(context))
        } catch (e: SecurityException) {
            Log.w(TAG, "MIUI screen not accessible; opening app info", e)
            context.startActivity(appInfoIntent(context))
        }
    }

    private fun appInfoIntent(context: Context) = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
}
