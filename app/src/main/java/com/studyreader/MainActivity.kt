package com.studyreader

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var floatingUI: FloatingUI? = null
    private val OVERLAY_PERMISSION_REQ_CODE = 1234
    private val SCREEN_RECORD_REQ_CODE = 5678
    private val RUNTIME_PERMISSIONS_REQ_CODE = 9999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        floatingUI = FloatingUI(this) {
            startScreenCaptureRequest()
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val btnOverlay = Button(this).apply {
            text = "ENABLE CONTROLS & PERMISSIONS"
            setOnClickListener { checkRuntimePermissions() }
        }

        layout.addView(btnOverlay)
        setContentView(layout)
    }

    private fun checkRuntimePermissions() {
        val permissionsNeeded = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), RUNTIME_PERMISSIONS_REQ_CODE)
        } else {
            checkAndShowFloatingControls()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RUNTIME_PERMISSIONS_REQ_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                checkAndShowFloatingControls()
            } else {
                Toast.makeText(this, "Microphone/Notification permissions required!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkAndShowFloatingControls() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
            Toast.makeText(this, "Please allow 'Display over other apps'", Toast.LENGTH_LONG).show()
        } else {
            floatingUI?.show()
            Toast.makeText(this, "Floating controls activated!", Toast.LENGTH_SHORT).show()
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
            }
        } else if (requestCode == SCREEN_RECORD_REQ_CODE && resultCode == Activity.RESULT_OK && data != null) {
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
