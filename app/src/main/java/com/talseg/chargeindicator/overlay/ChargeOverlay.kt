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
import com.talseg.chargeindicator.settings.OverlaySettings

/**
 * A small floating battery icon drawn over all other apps. Drag it to move it.
 *
 * It lives in its own window, sized to the icon only, so touches anywhere else
 * go to the app underneath. Requires the "Display over other apps" permission.
 */
class ChargeOverlay(context: Context) {

    /** Called with the new window position (pixels) when the user finishes dragging. */
    var onDragEnd: ((x: Int, y: Int) -> Unit)? = null

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val density = context.resources.displayMetrics.density
    private val screenWidth = context.resources.displayMetrics.widthPixels

    // A dark circle behind the icon keeps it visible on light and dark apps alike.
    private val circleBackground = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(OverlayStyle.CIRCLE_BACKGROUND_COLOR)
    }

    private val iconView = ImageView(context).apply {
        setImageResource(R.drawable.ic_overlay_battery)
        setOnTouchListener(DragListener())
    }

    private val layoutParams = WindowManager.LayoutParams(
        0,
        0,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        // Not focusable: never takes the keyboard, and touches outside the window
        // pass through to the app underneath (this flag implies FLAG_NOT_TOUCH_MODAL).
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        // x/y are measured from the top-left corner, which keeps dragging math simple.
        gravity = Gravity.TOP or Gravity.START
    }

    private var isShown = false
    private var sizeDp = OverlaySettings.DEFAULT_ICON_SIZE_DP
    private var showCircleBackground = true

    init {
        applyAppearance()
        // Default position: near the top-right corner, until the user drags it elsewhere.
        layoutParams.x = screenWidth - layoutParams.width - dp(16)
        layoutParams.y = dp(120)
    }

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

    fun setSizeDp(sizeDp: Int) {
        if (this.sizeDp == sizeDp) return
        this.sizeDp = sizeDp
        applyAppearance()
    }

    fun setShowCircleBackground(show: Boolean) {
        if (showCircleBackground == show) return
        showCircleBackground = show
        applyAppearance()
    }

    fun setPosition(x: Int, y: Int) {
        if (layoutParams.x == x && layoutParams.y == y) return
        layoutParams.x = x
        layoutParams.y = y
        updateWindow()
    }

    /**
     * With the circle: a square window, the battery centered inside with padding.
     * Without it: the window is exactly the battery's shape, so it can touch the screen edges.
     */
    private fun applyAppearance() {
        val heightPx = dp(sizeDp)
        if (showCircleBackground) {
            val padding = heightPx * OverlayStyle.CIRCLE_PADDING_PERCENT / 100
            layoutParams.width = heightPx
            iconView.setPadding(padding, padding, padding, padding)
            iconView.background = circleBackground
        } else {
            layoutParams.width = (heightPx * OverlayStyle.BATTERY_ASPECT_RATIO).toInt()
            iconView.setPadding(0, 0, 0, 0)
            iconView.background = null
        }
        layoutParams.height = heightPx
        updateWindow()
    }

    private fun updateWindow() {
        if (isShown) windowManager.updateViewLayout(iconView, layoutParams)
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
                MotionEvent.ACTION_UP -> {
                    if (layoutParams.x != windowStartX || layoutParams.y != windowStartY) {
                        onDragEnd?.invoke(layoutParams.x, layoutParams.y)
                    }
                    view.performClick()
                }
            }
            return true
        }
    }
}
