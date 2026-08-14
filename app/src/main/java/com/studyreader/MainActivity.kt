package com.studyreader

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Basic layout created programmatically to avoid needing XML files right now
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
        }

        val btnOverlay = Button(this).apply {
            text = "Enable Floating Controls"
            setOnClickListener { requestOverlayPermission() }
        }

        layout.addView(btnOverlay)
        setContentView(layout)
    }

    // This is required to draw floating widgets like AZ Screen Recorder
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 1234)
                Toast.makeText(this, "Please allow 'Display over other apps'", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission already granted!", Toast.LENGTH_SHORT).show()
                // Here is where you would launch your Floating Service
            }
        }
    }
}
