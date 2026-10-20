package com.talseg.chargeindicator.overlay

/**
 * Visual constants of the floating icon. Shared by the real overlay (a View) and
 * its preview on the settings screen (Compose), so both look the same.
 *
 * "Icon size" is the battery's height. The optional circle is drawn around the
 * battery, so toggling it never changes the battery's size.
 */
object OverlayStyle {
    /** Circle diameter as a multiple of the battery's height. */
    const val CIRCLE_DIAMETER_RATIO = 1.8f

    /** The circle behind the icon: 60% black. */
    const val CIRCLE_BACKGROUND_COLOR = 0x99000000.toInt()

    /** Width / height of the cropped battery drawable (ic_overlay_battery: 10 x 20). */
    const val BATTERY_ASPECT_RATIO = 0.5f
}
