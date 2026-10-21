package com.talseg.chargeindicator.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Starts the overlay service after the phone boots, and after the app is updated
 * (an update stops the running service), unless the user has switched it off.
 * Declared in the manifest, so Android delivers these broadcasts even when the app
 * isn't running.
 *
 * On Xiaomi phones, BOOT_COMPLETED only arrives if "Background autostart" is enabled.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Reading the setting is asynchronous; goAsync() keeps the broadcast (and its
                // permission to start a foreground service) alive until finish() is called.
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        ChargeOverlayService.startIfEnabled(context.applicationContext)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
