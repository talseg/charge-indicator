package com.talseg.chargeindicator.ui.main

import com.talseg.chargeindicator.battery.ChargingSpeed
import com.talseg.chargeindicator.settings.OverlaySettings

/** Everything the main screen needs to draw itself. Produced by [MainViewModel]. */
data class MainUiState(
    val chargingSpeed: ChargingSpeed = ChargingSpeed.NONE,
    val iconSizeDp: Int = OverlaySettings.DEFAULT_ICON_SIZE_DP
)
