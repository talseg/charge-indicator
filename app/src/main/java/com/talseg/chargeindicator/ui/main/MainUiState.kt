package com.talseg.chargeindicator.ui.main

import com.talseg.chargeindicator.battery.ChargingSpeed
import com.talseg.chargeindicator.settings.OverlaySettings

/** Everything the main screen needs to draw itself. Produced by [MainViewModel]. */
data class MainUiState(
    /** The speed to display: the simulated one if set, otherwise the real one. */
    val chargingSpeed: ChargingSpeed = ChargingSpeed.NONE,
    /** Null means "live": show the real charging state. */
    val simulatedSpeed: ChargingSpeed? = null,
    val iconSizeDp: Int = OverlaySettings.DEFAULT_ICON_SIZE_DP,
    val showCircleBackground: Boolean = true
)
