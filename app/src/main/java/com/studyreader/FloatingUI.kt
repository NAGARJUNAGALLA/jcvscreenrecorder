package com.studyreader

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout

class FloatingUI(
    private val context: Context,
    private val onRecordClick: () -> Unit
) {
    private var windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var floatingView: View? = null

    fun show() {
        if (floatingView != null) return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#CC000000"))
            setPadding(20, 16, 20, 16)
        }

        val btnRecord = Button(context).apply {
            text = "⏺ Record"
            setTextColor(Color.RED)
            setOnClickListener {
                hide() // Hides floating UI so it does NOT appear inside the screen recording video!
                onRecordClick() // Triggers Android screen record permission dialog
            }
        }

        val btnClose = Button(context).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            setOnClickListener {
                hide()
            }
        }

        layout.addView(btnRecord)
        layout.addView(btnClose)

        floatingView = layout
        windowManager.addView(floatingView, params)
    }

    fun hide() {
        floatingView?.let {
            windowManager.removeView(it)
            floatingView = null
        }
    }
}
