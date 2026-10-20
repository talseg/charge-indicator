package com.talseg.chargeindicator.ui

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.talseg.chargeindicator.R
import com.talseg.chargeindicator.battery.ChargingSpeed

/**
 * How each [ChargingSpeed] looks. Shared by the Compose screen and the overlay View,
 * so the colors are defined once. Color intensity grows with charging speed.
 */
@ColorInt
fun ChargingSpeed.colorArgb(): Int = when (this) {
    ChargingSpeed.NONE -> 0xFFFFFFFF.toInt()       // white
    ChargingSpeed.VERY_SLOW -> 0xFFFFF59D.toInt()  // light yellow
    ChargingSpeed.SLOW -> 0xFFFFEB3B.toInt()       // yellow
    ChargingSpeed.FAST -> 0xFFFF9800.toInt()       // orange
    ChargingSpeed.SUPER_FAST -> 0xFFF44336.toInt() // red
}

@StringRes
fun ChargingSpeed.labelRes(): Int = when (this) {
    ChargingSpeed.NONE -> R.string.charging_speed_none
    ChargingSpeed.VERY_SLOW -> R.string.charging_speed_very_slow
    ChargingSpeed.SLOW -> R.string.charging_speed_slow
    ChargingSpeed.FAST -> R.string.charging_speed_fast
    ChargingSpeed.SUPER_FAST -> R.string.charging_speed_super_fast
}
