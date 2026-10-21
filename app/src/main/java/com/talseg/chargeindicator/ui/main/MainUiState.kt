package com.talseg.chargeindicator.ui.main

import com.talseg.chargeindicator.battery.ChargingSpeed
import com.talseg.chargeindicator.settings.OverlaySettings

/** Everything the main screen needs to draw itself. Produced by [MainViewModel]. */
data class MainUiState(
    /** The charging speed the icon preview shows, chosen with the preview chips. */
    val previewSpeed: ChargingSpeed = MainViewModel.DEFAULT_PREVIEW_SPEED,
    val iconSizeDp: Int = OverlaySettings.DEFAULT_ICON_SIZE_DP,
    val showCircleBackground: Boolean = true,
    /** Xiaomi only: ask the user to enable "Background autostart". */
    val showAutostartDialog: Boolean = false
)
