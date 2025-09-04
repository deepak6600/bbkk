package com.safe.setting.app.utils.hiddenCameraServiceUtils.config

import androidx.annotation.IntDef


class CameraFacing {

    init {
        throw RuntimeException("Cannot initialize this class.")
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(REAR_FACING_CAMERA, FRONT_FACING_CAMERA)
    annotation class SupportedCameraFacing

    companion object {
        const val REAR_FACING_CAMERA = 0
        const val FRONT_FACING_CAMERA = 1
    }
}
