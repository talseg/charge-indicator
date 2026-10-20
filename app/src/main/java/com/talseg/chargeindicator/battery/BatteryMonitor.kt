package com.talseg.chargeindicator.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class ChargerType { NONE, AC, USB, WIRELESS, OTHER }

data class BatteryState(
    val chargerType: ChargerType,
    val levelPercent: Int,
    /** What the charger negotiated, in microamps. Null if the device doesn't report it. */
    val maxChargingCurrentMicroAmps: Int? = null,
    /** What the charger negotiated, in microvolts. Null if the device doesn't report it. */
    val maxChargingVoltageMicroVolts: Int? = null
) {
    val isPluggedIn: Boolean get() = chargerType != ChargerType.NONE

    /** Negotiated charger power, computed the same way Android's lock screen does. */
    val maxChargingWatts: Double?
        get() {
            val current = maxChargingCurrentMicroAmps ?: return null
            // Like SystemUI: if the voltage isn't reported, assume USB's 5 V.
            val voltage = maxChargingVoltageMicroVolts ?: DEFAULT_CHARGING_VOLTAGE_MICRO_VOLTS
            return current.toDouble() * voltage / 1_000_000_000_000.0
        }

    val chargingSpeed: ChargingSpeed
        get() = ChargingSpeed.classify(chargerType, maxChargingWatts)
}

/**
 * Turns the system's ACTION_BATTERY_CHANGED broadcast into a Flow of [BatteryState].
 * The receiver is registered when someone starts collecting and unregistered when they stop.
 */
class BatteryMonitor(private val context: Context) {

    val batteryState: Flow<BatteryState> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(intent.toBatteryState())
            }
        }

        // ACTION_BATTERY_CHANGED is sticky: registering delivers the current state right away.
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}

// These extras exist in the broadcast but their constants are hidden in the public SDK.
private const val EXTRA_MAX_CHARGING_CURRENT = "max_charging_current"
private const val EXTRA_MAX_CHARGING_VOLTAGE = "max_charging_voltage"
private const val DEFAULT_CHARGING_VOLTAGE_MICRO_VOLTS = 5_000_000

private fun Intent.toBatteryState(): BatteryState {
    val plugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, 100)

    val chargerType = when (plugged) {
        0 -> ChargerType.NONE
        BatteryManager.BATTERY_PLUGGED_AC -> ChargerType.AC
        BatteryManager.BATTERY_PLUGGED_USB -> ChargerType.USB
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargerType.WIRELESS
        else -> ChargerType.OTHER
    }

    return BatteryState(
        chargerType = chargerType,
        levelPercent = if (level >= 0 && scale > 0) level * 100 / scale else -1,
        maxChargingCurrentMicroAmps = getIntExtra(EXTRA_MAX_CHARGING_CURRENT, -1).takeIf { it > 0 },
        maxChargingVoltageMicroVolts = getIntExtra(EXTRA_MAX_CHARGING_VOLTAGE, -1).takeIf { it > 0 }
    )
}
