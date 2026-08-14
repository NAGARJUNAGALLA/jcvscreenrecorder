package com.studyreader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var floatingUI: FloatingUI? = null
    private val OVERLAY_PERMISSION_REQ_CODE = 1234
    private val SCREEN_RECORD_REQ_CODE = 5678

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Floating UI with callback when "Record" is tapped
        floatingUI = FloatingUI(this) {
            startScreenCaptureRequest()
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val btnOverlay = Button(this).apply {
            text = "Enable Floating Controls"
            setOnClickListener { checkAndShowFloatingControls() }
        }

        layout.addView(btnOverlay)
        setContentView(layout)
    }

    private fun checkAndShowFloatingControls() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
                Toast.makeText(this, "Please allow 'Display over other apps'", Toast.LENGTH_LONG).show()
            } else {
                // Permission is granted -> Show floating widget immediately!
                floatingUI?.show()
                Toast.makeText(this, "Floating controls activated!", Toast.LENGTH_SHORT).show()
            }
        } else {
            floatingUI?.show()
        }
    }

    private fun startScreenCaptureRequest() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_RECORD_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                floatingUI?.show()
                Toast.makeText(this, "Floating controls activated!", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == SCREEN_RECORD_REQ_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Launch RecordService with media projection result
            val serviceIntent = Intent(this, RecordService::class.java).apply {
                action = "START_RECORDING"
                putExtra("code", resultCode)
                putExtra("data", data)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }
}
