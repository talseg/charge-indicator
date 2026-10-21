package com.talseg.chargeindicator.overlay

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.talseg.chargeindicator.MainActivity
import com.talseg.chargeindicator.R
import com.talseg.chargeindicator.battery.BatteryMonitor
import com.talseg.chargeindicator.battery.ChargingSpeed
import com.talseg.chargeindicator.settings.OverlaySettingsRepository
import com.talseg.chargeindicator.ui.colorArgb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Keeps the floating charge icon alive independently of the app's screen.
 *
 * Runs as a foreground service (with a persistent notification) so Android keeps
 * the process alive after the Activity is closed or swiped away from Recents.
 */
class ChargeOverlayService : Service() {

    // Services have no lifecycleScope, so we own a scope and cancel it in onDestroy.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val settingsRepository by lazy { OverlaySettingsRepository(this) }
    private var overlay: ChargeOverlay? = null

    override fun onBind(intent: Intent?): IBinder? = null // Started service, not bound.

    override fun onCreate() {
        super.onCreate()
        // Must be called promptly after startForegroundService(), or Android kills the app.
        startInForeground()

        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "No 'Display over other apps' permission; stopping")
            stopSelf()
            return
        }

        val overlay = ChargeOverlay(this).also { this.overlay = it }
        overlay.onDragEnd = { centerX, centerY ->
            scope.launch { settingsRepository.setCenter(centerX, centerY) }
        }

        val chargingSpeed = BatteryMonitor(this).batteryState
            .map { it.chargingSpeed }
            .distinctUntilChanged()

        scope.launch {
            // Settings are applied before showing, so the icon appears at its saved size and place.
            combine(chargingSpeed, settingsRepository.settings) { speed, settings -> speed to settings }
                .collect { (speed, settings) ->
                    if (!settings.overlayEnabled) {
                        // Turned off in the app (or with the notification's Stop): shut down fully.
                        overlay.hide()
                        stopSelf()
                        return@collect
                    }
                    overlay.setSizeDp(settings.iconSizeDp)
                    overlay.setShowCircleBackground(settings.showCircleBackground)
                    if (settings.centerX != null && settings.centerY != null) {
                        overlay.setCenter(settings.centerX, settings.centerY)
                    }
                    // Visible only while charging; colored by charging speed.
                    if (speed == ChargingSpeed.NONE) {
                        overlay.hide()
                    } else {
                        overlay.setColor(speed.colorArgb())
                        overlay.show()
                    }
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            // Saved as a setting, so reopening the app or rebooting doesn't bring it back.
            scope.launch {
                settingsRepository.setOverlayEnabled(false)
                stopSelf()
            }
            return START_NOT_STICKY
        }
        // If Android kills the process to reclaim memory, recreate the service later.
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel() // Stops collecting, which unregisters the battery receiver.
        overlay?.hide()
        super.onDestroy()
    }

    private fun startInForeground() {
        NotificationManagerCompat.from(this).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(getString(R.string.overlay_notification_channel))
                .build()
        )

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, ChargeOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_battery_charging_full)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.overlay_notification_text))
            .setContentIntent(openAppIntent)
            .addAction(0, getString(R.string.overlay_notification_stop), stopIntent)
            .setOngoing(true)
            .build()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
    }

    companion object {
        private const val TAG = "ChargeOverlayService"
        private const val CHANNEL_ID = "charge_overlay"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_STOP = "com.talseg.chargeindicator.action.STOP_OVERLAY"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ChargeOverlayService::class.java)
            )
        }

        /** Starts the service only if the user has it switched on and the permission is granted. */
        suspend fun startIfEnabled(context: Context) {
            if (!Settings.canDrawOverlays(context)) {
                Log.w(TAG, "No 'Display over other apps' permission; not starting")
                return
            }
            if (OverlaySettingsRepository(context).settings.first().overlayEnabled) {
                start(context)
            }
        }
    }
}
