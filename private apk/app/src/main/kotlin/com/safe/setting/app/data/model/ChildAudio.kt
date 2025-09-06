package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * यह क्लास Firebase से ऑडियो रिकॉर्डिंग के लिए कमांड प्राप्त करती है।
 */
@IgnoreExtraProperties
data class ChildAudio(
    var recordAudio: Boolean? = null
)

