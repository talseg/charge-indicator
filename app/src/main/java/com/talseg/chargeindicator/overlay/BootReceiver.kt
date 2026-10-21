package com.talseg.chargeindicator.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * Starts the overlay service after the phone boots, and after the app is updated
 * (an update stops the running service). Declared in the manifest, so Android
 * delivers these broadcasts even when the app isn't running.
 *
 * On Xiaomi phones, BOOT_COMPLETED only arrives if "Background autostart" is enabled.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (Settings.canDrawOverlays(context)) {
                    ChargeOverlayService.start(context)
                } else {
                    Log.w(TAG, "No 'Display over other apps' permission; not starting overlay")
                }
            }
        }
    }

    private companion object {
        const val TAG = "BootReceiver"
    }
}
