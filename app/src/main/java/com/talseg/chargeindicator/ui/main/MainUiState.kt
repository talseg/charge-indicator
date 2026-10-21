package com.talseg.chargeindicator.ui.main

import com.talseg.chargeindicator.battery.ChargingSpeed
import com.talseg.chargeindicator.settings.OverlaySettings

/** A system setting the user still has to turn on, in the order they're asked for. */
enum class SetupStep {
    /** Xiaomi: "Display pop-up windows" + "Open new windows while running in the background". */
    XIAOMI_WINDOWS,

    /** Other phones: Android's "Display over other apps". */
    OVERLAY,

    /** Xiaomi: "Background autostart", so the overlay returns after a reboot. */
    XIAOMI_AUTOSTART
}

/** Everything the main screen needs to draw itself. Produced by [MainViewModel]. */
data class MainUiState(
    /** The charging speed the icon preview shows, chosen with the preview chips. */
    val previewSpeed: ChargingSpeed = MainViewModel.DEFAULT_PREVIEW_SPEED,
    val iconSizeDp: Int = OverlaySettings.DEFAULT_ICON_SIZE_DP,
    val showCircleBackground: Boolean = true,
    /** The setup dialog to show, or null when nothing is missing (or it was postponed). */
    val setupPrompt: SetupStep? = null
)
