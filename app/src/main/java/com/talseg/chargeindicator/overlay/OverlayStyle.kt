package com.talseg.chargeindicator.overlay

/**
 * Visual constants of the floating icon. Shared by the real overlay (a View) and
 * its preview on the settings screen (Compose), so both look the same.
 *
 * "Icon size" is the overall height: the circle's diameter, or, without the circle,
 * the battery's own height (the window is then exactly the battery's shape).
 */
object OverlayStyle {
    /** Space between the circle's edge and the battery, as a percentage of the icon size. */
    const val CIRCLE_PADDING_PERCENT = 22

    /** The circle behind the icon: 60% black. */
    const val CIRCLE_BACKGROUND_COLOR = 0x99000000.toInt()

    /** Width / height of the cropped battery drawable (ic_overlay_battery: 10 x 20). */
    const val BATTERY_ASPECT_RATIO = 0.5f
}
