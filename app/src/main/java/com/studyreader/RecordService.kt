package com.yourname.studyreader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RecordService : Service() {

    private val CHANNEL_ID = "ScreenRecordChannel"
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            "START_RECORDING" -> startRecording(intent)
            "PAUSE_RECORDING" -> pauseRecording()
            "STOP_RECORDING" -> stopRecording()
            else -> startForeground(1, createNotification("Ready to Record"))
        }
        return START_NOT_STICKY
    }

    private fun createNotification(statusText: String): Notification {
        val channel = NotificationChannel(CHANNEL_ID, "Recording Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        // Intents for the notification buttons
        val stopIntent = Intent(this, RecordService::class.java).setAction("STOP_RECORDING")
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StudyReader Recorder")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_record) // Replace with your icon
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .addAction(R.drawable.ic_pen, "Pen Tool", /* pending intent for pen */ null)
            .build()
    }

    private fun startRecording(intent: Intent) {
        // 1. Update Notification
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, createNotification("Recording in progress..."))

        // 2. Setup Media Recorder (MP4)
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncodingBitRate(512 * 1000)
            setVideoFrameRate(30)
            setVideoSize(1280, 720) // 16:9 Aspect Ratio
            setOutputFile(getExternalFilesDir(null)?.absolutePath + "/study_session.mp4")
            prepare()
        }

        // 3. Start Projection
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val resultCode = intent.getIntExtra("code", -1)
        val data = intent.getParcelableExtra<Intent>("data")
        
        mediaProjection = projectionManager.getMediaProjection(resultCode, data!!)
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenRecord",
            1280, 720, resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface, null, null
        )

        mediaRecorder?.start()
    }

    private fun pauseRecording() {
        mediaRecorder?.pause()
        // Update notification to show "Paused"
    }

    private fun stopRecording() {
        mediaRecorder?.stop()
        mediaRecorder?.reset()
        virtualDisplay?.release()
        mediaProjection?.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
