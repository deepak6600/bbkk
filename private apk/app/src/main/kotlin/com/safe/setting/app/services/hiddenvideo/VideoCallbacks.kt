package com.safe.setting.app.services.hiddenvideo

import com.safe.setting.app.utils.hiddenCameraServiceUtils.CameraError
import java.io.File

// यह इंटरफ़ेस वीडियो रिकॉर्डिंग की घटनाओं को वापस भेजता है।
interface VideoCallbacks {
    fun onVideoRecorded(videoFile: File)
    fun onCameraError(@CameraError.CameraErrorCodes errorCode: Int)
}
