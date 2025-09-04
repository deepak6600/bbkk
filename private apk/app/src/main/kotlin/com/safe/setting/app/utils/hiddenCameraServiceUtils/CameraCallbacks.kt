package com.safe.setting.app.utils.hiddenCameraServiceUtils

import java.io.File


interface CameraCallbacks {

    fun onImageCapture(imageFile: File)

    fun onCameraError(@CameraError.CameraErrorCodes errorCode: Int)
}
