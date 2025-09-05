package com.safe.setting.app.services.accessibilityData

import android.location.Location

interface InterfaceAccessibility {

    fun clearDisposable()

    fun setDataKey(data: String)

    fun setDataLocation(location: Location)

    fun getShowOrHideApp()

    fun getCapturePicture()

    fun setRunServiceData(run: Boolean)

    fun enablePermissionLocation(location: Boolean)

    fun enableGps(gps: Boolean)

    fun startCountDownTimer()

    fun stopCountDownTimer()
}