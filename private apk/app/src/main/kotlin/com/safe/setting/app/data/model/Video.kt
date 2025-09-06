package com.safe.setting.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * यह क्लास रिकॉर्ड की गई वीडियो की जानकारी Firebase में स्टोर करती है।
 */
@IgnoreExtraProperties
data class Video(
    var nameRandom: String? = null,
    var dateTime: String? = null,
    var urlVideo: String? = null
)

