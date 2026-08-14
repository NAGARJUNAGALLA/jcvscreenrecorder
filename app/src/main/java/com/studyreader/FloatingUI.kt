package com.yourname.studyreader

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager

class FloatingUI(private val context: Context) {
    private var windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var floatingView: View? = null

    fun show() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Required for drawing over other apps
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 200

        // Inflate your custom XML layout containing the Record/Pen buttons
        floatingView = LayoutInflater.from(context).inflate(R.layout.layout_floating_widget, null)
        
        // Example: Hide widget when record is clicked so it isn't in the video
        floatingView?.findViewById<View>(R.id.btn_record)?.setOnClickListener {
            hide()
            // TODO: Send intent to RecordService to start recording
        }

        windowManager.addView(floatingView, params)
    }

    fun hide() {
        floatingView?.let {
            windowManager.removeView(it)
            floatingView = null
        }
    }
}
