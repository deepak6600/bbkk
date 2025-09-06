package com.safe.setting.app.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderUtil(
    private val context: Context,
    private val callback: (File?) -> Unit
) {
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null

    companion object {
        private const val TAG = "AudioRecorderUtil"
    }

    fun startRecording() {
        try {
            recordingFile = File(context.cacheDir, "audio_record_${System.currentTimeMillis()}.mp3")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recordingFile!!.absolutePath)
                try {
                    prepare()
                    start()
                    // 1 मिनट के बाद रिकॉर्डिंग बंद करें
                    Handler(Looper.getMainLooper()).postDelayed({ stopRecording() }, 60000)
                } catch (e: IOException) {
                    Log.e(TAG, "MediaRecorder prepare() failed: ${e.message}")
                    cleanup()
                    callback(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio recording: ${e.message}")
            cleanup()
            callback(null)
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            callback(recordingFile)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Error stopping audio recording: ${e.message}")
            recordingFile?.delete()
            callback(null)
        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        mediaRecorder = null
    }
}

