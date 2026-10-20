package com.talseg.chargeindicator.overlay

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.talseg.chargeindicator.R

/**
 * A small floating battery icon drawn over all other apps. Drag it to move it.
 *
 * It lives in its own window, sized to the icon only, so touches anywhere else
 * go to the app underneath. Requires the "Display over other apps" permission.
 */
class ChargeOverlay(context: Context) {

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val density = context.resources.displayMetrics.density
    private val screenWidth = context.resources.displayMetrics.widthPixels

    private val iconView = ImageView(context).apply {
        setImageResource(R.drawable.ic_battery_charging_full)
        // A dark circle behind the icon keeps it visible on light and dark apps alike.
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(BACKGROUND_COLOR)
        }
        val padding = dp(10)
        setPadding(padding, padding, padding, padding)
        setOnTouchListener(DragListener())
    }

    private val layoutParams = WindowManager.LayoutParams(
        dp(ICON_SIZE_DP),
        dp(ICON_SIZE_DP),
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        // Not focusable: never takes the keyboard, and touches outside the window
        // pass through to the app underneath (this flag implies FLAG_NOT_TOUCH_MODAL).
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        // x/y are measured from the top-left corner, which keeps dragging math simple.
        gravity = Gravity.TOP or Gravity.START
        x = screenWidth - dp(ICON_SIZE_DP) - dp(16)
        y = dp(120)
    }

    private var isShown = false

    fun show() {
        if (isShown) return
        windowManager.addView(iconView, layoutParams)
        isShown = true
    }

    fun hide() {
        if (!isShown) return
        windowManager.removeView(iconView)
        isShown = false
    }

    fun setColor(@ColorInt color: Int) {
        iconView.imageTintList = ColorStateList.valueOf(color)
    }

    private fun dp(value: Int): Int = (value * density).toInt()

    /** Moves the overlay window with the finger. */
    private inner class DragListener : View.OnTouchListener {
        private var windowStartX = 0
        private var windowStartY = 0
        private var touchStartX = 0f
        private var touchStartY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    windowStartX = layoutParams.x
                    windowStartY = layoutParams.y
                    // rawX/rawY are screen coordinates, unaffected by the window moving.
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = windowStartX + (event.rawX - touchStartX).toInt()
                    layoutParams.y = windowStartY + (event.rawY - touchStartY).toInt()
                    windowManager.updateViewLayout(view, layoutParams)
                }
                MotionEvent.ACTION_UP -> view.performClick()
            }
            return true
        }
    }

    private companion object {
        const val ICON_SIZE_DP = 56
        const val BACKGROUND_COLOR = 0x99000000.toInt() // 60% black
    }
}
