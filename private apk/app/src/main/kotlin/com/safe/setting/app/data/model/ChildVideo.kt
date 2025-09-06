package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * यह क्लास Firebase से वीडियो रिकॉर्डिंग के लिए कमांड प्राप्त करती है।
 * recordVideo: रिकॉर्डिंग शुरू करने के लिए फ्लैग।
 * facing: 0 = बैक कैमरा, 1 = फ्रंट कैमरा।
 */
@IgnoreExtraProperties
data class ChildVideo(
    var recordVideo: Boolean? = null,
    var facing: Int? = null
)

