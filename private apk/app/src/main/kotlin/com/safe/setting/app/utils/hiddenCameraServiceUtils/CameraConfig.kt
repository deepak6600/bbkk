package com.safe.setting.app.utils.hiddenCameraServiceUtils

import android.content.Context
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraFacing
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraImageFormat
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraResolution
import com.safe.setting.app.utils.hiddenCameraServiceUtils.config.CameraRotation
import com.safe.setting.app.utils.hiddenCameraServiceUtils.HiddenCameraUtils.getFileName
import java.io.File


class CameraConfig {

    private var context: Context? = null

    @get:CameraResolution.SupportedResolution
    internal var resolution = CameraResolution.MEDIUM_RESOLUTION
        private set

    @get:CameraFacing.SupportedCameraFacing
    internal var facing = CameraFacing.FRONT_FACING_CAMERA
        private set

    @get:CameraImageFormat.SupportedImageFormat
    internal var imageFormat = CameraImageFormat.FORMAT_JPEG
        private set

    @get:CameraRotation.SupportedRotation
    internal var imageRotation = CameraRotation.ROTATION_0
        private set

    // फोटो और वीडियो के लिए अलग-अलग फाइलें
    internal var imageFile: File? = null
        internal set
    internal var videoFile: File? = null
        internal set


    fun builder(context: Context): Builder {
        this.context = context
        return Builder()
    }

    inner class Builder {

        fun setCameraFacing(@CameraFacing.SupportedCameraFacing cameraFacing: Int): Builder {
            facing = cameraFacing
            return this
        }

        fun setImageRotation(@CameraRotation.SupportedRotation rotation: Int): Builder {
            imageRotation = if (rotation != CameraRotation.ROTATION_0
                && rotation != CameraRotation.ROTATION_90
                && rotation != CameraRotation.ROTATION_180
                && rotation != CameraRotation.ROTATION_270)
                CameraRotation.ROTATION_0
            else rotation
            return this
        }

        fun build(): CameraConfig {
            if (imageFile == null) imageFile = File(context!!.getFileName(".jpeg"))
            if (videoFile == null) videoFile = File(context!!.getFileName(".mp4"))
            return this@CameraConfig
        }
    }
}

