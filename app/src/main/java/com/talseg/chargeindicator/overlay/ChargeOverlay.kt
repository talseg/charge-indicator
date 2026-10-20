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
 *
 * The position is the battery's center, so changing the size or toggling the
 * circle grows or shrinks the icon around the same point instead of shifting it.
 */
class ChargeOverlay(context: Context) {

    /** Called with the new center position (pixels) when the user finishes dragging. */
    var onDragEnd: ((centerX: Int, centerY: Int) -> Unit)? = null

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
        // Window x/y are its top-left corner; we derive them from the center.
        gravity = Gravity.TOP or Gravity.START
    }

    private var isShown = false
    private var sizeDp = OverlaySettings.DEFAULT_ICON_SIZE_DP
    private var showCircleBackground = true
    private var centerX = 0
    private var centerY = 0

    init {
        // Default position: near the top-right corner, until the user drags it elsewhere.
        val batteryHeight = dp(sizeDp)
        centerX = screenWidth - dp(16) - batteryHeight
        centerY = dp(120) + batteryHeight
        applyLayout()
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

    /** Sets the battery's height. The circle, if shown, is drawn around it. */
    fun setSizeDp(sizeDp: Int) {
        if (this.sizeDp == sizeDp) return
        this.sizeDp = sizeDp
        applyLayout()
    }

    fun setShowCircleBackground(show: Boolean) {
        if (showCircleBackground == show) return
        showCircleBackground = show
        applyLayout()
    }

    fun setCenter(centerX: Int, centerY: Int) {
        if (this.centerX == centerX && this.centerY == centerY) return
        this.centerX = centerX
        this.centerY = centerY
        applyLayout()
    }

    /**
     * Without the circle the window is exactly the battery's shape, so it can touch
     * the screen edges. With it, the window is the circle's square and the battery
     * sits in the middle, at the same size.
     */
    private fun applyLayout() {
        val batteryHeight = dp(sizeDp)
        val batteryWidth = (batteryHeight * OverlayStyle.BATTERY_ASPECT_RATIO).toInt()

        if (showCircleBackground) {
            val diameter = (batteryHeight * OverlayStyle.CIRCLE_DIAMETER_RATIO).toInt()
            // Uniform padding leaves a batteryHeight-tall square inside; the image fits
            // its height and centers horizontally.
            val padding = (diameter - batteryHeight) / 2
            iconView.setPadding(padding, padding, padding, padding)
            iconView.background = circleBackground
            layoutParams.width = diameter
            layoutParams.height = diameter
        } else {
            iconView.setPadding(0, 0, 0, 0)
            iconView.background = null
            layoutParams.width = batteryWidth
            layoutParams.height = batteryHeight
        }

        layoutParams.x = centerX - layoutParams.width / 2
        layoutParams.y = centerY - layoutParams.height / 2
        updateWindow()
    }

    private fun updateWindow() {
        if (isShown) windowManager.updateViewLayout(iconView, layoutParams)
    }

    private fun dp(value: Int): Int = (value * density).toInt()

    /** Moves the overlay window with the finger. */
    private inner class DragListener : View.OnTouchListener {
        private var centerStartX = 0
        private var centerStartY = 0
        private var touchStartX = 0f
        private var touchStartY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    centerStartX = centerX
                    centerStartY = centerY
                    // rawX/rawY are screen coordinates, unaffected by the window moving.
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    // Keep the whole window on screen (Android would clamp it anyway), so the
                    // stored center never drifts past an edge.
                    val metrics = view.resources.displayMetrics
                    val halfWidth = layoutParams.width / 2
                    val halfHeight = layoutParams.height / 2
                    centerX = (centerStartX + (event.rawX - touchStartX).toInt())
                        .coerceIn(halfWidth, metrics.widthPixels - halfWidth)
                    centerY = (centerStartY + (event.rawY - touchStartY).toInt())
                        .coerceIn(halfHeight, metrics.heightPixels - halfHeight)
                    applyLayout()
                }
                MotionEvent.ACTION_UP -> {
                    if (centerX != centerStartX || centerY != centerStartY) {
                        onDragEnd?.invoke(centerX, centerY)
                    }
                    view.performClick()
                }
            }
            return true
        }
    }
}
