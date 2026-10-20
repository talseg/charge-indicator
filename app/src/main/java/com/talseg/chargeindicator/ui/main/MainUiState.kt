package com.talseg.chargeindicator.ui.main

import com.talseg.chargeindicator.battery.ChargingSpeed

/** Everything the main screen needs to draw itself. Produced by [MainViewModel]. */
data class MainUiState(
    val chargingSpeed: ChargingSpeed = ChargingSpeed.NONE
)
