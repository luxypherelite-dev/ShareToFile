package com.aiexport.shareordirect.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import kotlin.math.abs

class OverlayManager(private val ctx: Context, private val onTap: () -> Unit) {

    private val wm: WindowManager? = try {
        ctx.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    } catch (e: Exception) { null }

    private var overlayView: View? = null
    private var isCapturing = false

    @Suppress("DEPRECATION")
    private fun buildParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; x = 24; y = 300 }

    private var params = buildParams()

    fun show() {
        if (wm == null || overlayView != null) return
        try {
            val dp56 = (56 * ctx.resources.displayMetrics.density).toInt()
            val container = RelativeLayout(ctx)
            val btn = ImageView(ctx).apply {
                setBackgroundColor(Color.parseColor("#6650A4"))
                setImageResource(android.R.drawable.ic_menu_share)
                setColorFilter(Color.WHITE)
                layoutParams = RelativeLayout.LayoutParams(dp56, dp56).apply {
                    addRule(RelativeLayout.CENTER_IN_PARENT)
                }
            }
            container.addView(btn)

            var downX = 0; var downY = 0; var lastX = 0; var lastY = 0
            container.setOnTouchListener { _, ev ->
                when (ev.action) {
                    MotionEvent.ACTION_DOWN -> { downX = ev.rawX.toInt(); downY = ev.rawY.toInt(); lastX = downX; lastY = downY }
                    MotionEvent.ACTION_MOVE -> {
                        params.x += ev.rawX.toInt() - lastX
                        params.y += ev.rawY.toInt() - lastY
                        lastX = ev.rawX.toInt(); lastY = ev.rawY.toInt()
                        try { if (overlayView != null) wm.updateViewLayout(container, params) } catch (_: Exception) {}
                    }
                    MotionEvent.ACTION_UP -> {
                        if (abs(ev.rawX.toInt() - downX) < 12 && abs(ev.rawY.toInt() - downY) < 12 && !isCapturing) onTap()
                    }
                }
                true
            }
            overlayView = container
            wm.addView(container, params)
        } catch (e: Exception) {
            overlayView = null
        }
    }

    fun hide() {
        try { overlayView?.let { wm?.removeView(it) } } catch (_: Exception) {}
        overlayView = null
    }

    fun setCapturing(capturing: Boolean) { isCapturing = capturing }
    fun isShowing() = overlayView != null
}
